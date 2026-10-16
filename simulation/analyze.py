"""
Analyses the studies filled by simulate.py and checks the properties of the randomization algorithms:
correctness, balance, preservation of the allocation ratio, predictability and randomness of the sequences.

The subjects are ordered by the index in their pseudonym, i.e., the order in which simulate.py planned them.
With one thread this is the order in which the server randomized them.

Usage: analyze.py [run directory], by default the latest run in the results directory.
"""
import argparse
import json
import math
from pathlib import Path

import matplotlib
import numpy as np
import pandas as pd
from scipy import stats

from simulate import RESULTS_DIR, api_id

matplotlib.use('Agg')
import matplotlib.pyplot as plt  # noqa: E402 (the backend must be set first)
from matplotlib.lines import Line2D  # noqa: E402
from matplotlib.ticker import MaxNLocator, StrMethodFormatter  # noqa: E402

SITE_FACTOR = 'site'
# interim analyses after these fractions of the capacity of the study
CHECKPOINTS = [0.25, 0.5, 0.75, 1.0]
BALANCE_MEASURES = ['overall', 'list_max', 'site_max', 'stratum_level_max']


def read_json(path):
    with open(path, encoding='utf-8') as file:
        return json.load(file)


def latest_run_dir():
    # the run directories are named by their timestamp
    directories = sorted(path for path in RESULTS_DIR.glob('*/simulation.json'))
    return directories[-1].parent if directories else None


def load_replications(configuration_dir):
    replications = []
    for directory in sorted((configuration_dir / 'replications').glob('*/')):
        if not (directory / 'study.json').is_file():
            # the download of the assignments failed
            continue
        replication = read_json(directory / 'study.json')
        replication['assignments'] = read_json(directory / 'assignments.json')
        replications.append(replication)
    return replications


def arm_ratios(configuration):
    return {api_id(arm): arm['ratio'] for arm in configuration['arms']}


def factors(configuration):
    # stratification by site adds the site as an additional stratum
    names = [stratum['name'] for stratum in configuration['strata']]
    if configuration['stratifyBySite']:
        names.append(SITE_FACTOR)
    return names


def to_frame(configuration, assignments):
    names = factors(configuration)
    rows = []
    for assignment in assignments:
        # the export contains the names of the strata and their values
        levels = tuple((name, assignment['locationApiId'] if name == SITE_FACTOR else assignment[name])
                       for name in names)
        rows.append({
            'index': int(assignment['pseudonym'].rsplit('-no', 1)[1]),
            'pseudonym': assignment['pseudonym'],
            'orderNumber': assignment['orderNumber'],
            'arm': assignment['studyArmApiId'],
            'site': assignment['locationApiId'],
            'list': levels,
        })
    frame = pd.DataFrame(rows).sort_values('index', ignore_index=True)
    frame['position'] = frame.groupby('list').cumcount() + 1
    return frame


def imbalance(counts, ratios):
    # range of the counts weighted by the ratio, e.g., |n_A - n_B| for 1:1
    normalized = [counts.get(arm, 0) / ratio for arm, ratio in ratios.items()]
    return max(normalized) - min(normalized)


def max_imbalance(frame, column, ratios):
    return max(imbalance(group['arm'].value_counts().to_dict(), ratios) for _, group in frame.groupby(column))


def explode_levels(frame):
    # one row per subject and stratum value for the marginal balance, the sites are handled separately
    return frame.explode('level').dropna(subset=['level'])


def balance(frame, ratios):
    """Imbalance of the given subjects and the largest one of their lists, sites and stratum values."""
    levels_frame = explode_levels(frame)
    return {
        'overall': imbalance(frame['arm'].value_counts().to_dict(), ratios),
        'list_max': max_imbalance(frame, 'list', ratios),
        'site_max': max_imbalance(frame, 'site', ratios),
        'stratum_level_max': max_imbalance(levels_frame, 'level', ratios) if not levels_frame.empty else np.nan,
    }


def checkpoint_label(fraction):
    return f'{round(fraction * 100)}pct'


def interim_column(measure, fraction):
    # the balance at 100 % is the final balance
    return f'imbalance_{measure}' if fraction == 1 else f'imbalance_{measure}_{checkpoint_label(fraction)}'


def checkpoint_size(configuration, fraction):
    return max(1, round(fraction * configuration['capacity']))


def balance_groups(configuration):
    """The study, each site and each stratum value as (factor, value), also if they have no subjects yet."""
    groups = [('overall', 'all')]
    groups += [(SITE_FACTOR, api_id(site)) for site in configuration['sites']]
    groups += [(stratum['name'], value['name']) for stratum in configuration['strata'] for value in stratum['values']]
    return groups


def arm_counts(configuration, frame, ratios):
    """Counts per arm and imbalance of the study, each site and each stratum value."""
    subsets = {('overall', 'all'): frame}
    subsets.update({(SITE_FACTOR, site): group for site, group in frame.groupby('site')})
    subsets.update({level: group for level, group in explode_levels(frame).groupby('level')})
    rows = []
    for factor, value in balance_groups(configuration):
        subset = subsets.get((factor, value), frame.iloc[:0])
        counts = subset['arm'].value_counts().to_dict()
        rows.append({'factor': factor, 'value': value, 'subjects': len(subset),
                     **{f'n_{arm}': counts.get(arm, 0) for arm in ratios},
                     'imbalance': imbalance(counts, ratios)})
    return rows


def minimization_score(values, function):
    if function == 'RANGE':
        return max(values) - min(values)
    return float(np.var(values))


def preferred_arms(configuration, ratios, counts, levels):
    """
    Returns the arms a guesser would predict for the next subject with the given stratum levels.
    For minimization these are the arms with the minimal imbalance like in MinimizationRandomization,
    otherwise the arms that are behind (convergence strategy of Blackwell and Hodges).
    """
    randomization = configuration['randomization']
    arms = list(ratios)
    if randomization['algorithm'] != 'MINIMIZATION':
        normalized = {arm: counts['list', levels].get(arm, 0) / ratios[arm] for arm in arms}
    else:
        # the counts of each stratum level are taken over the entire study, without strata over the list
        keys = [('level', level) for level in levels] or [('list', levels)]
        normalized = {}
        for arm in arms:
            score = 0.0
            for key in keys:
                values = [counts[key].get(other, 0) / ratios[other] for other in arms]
                values[arms.index(arm)] += 1
                score += minimization_score(values, randomization['imbalanceFunction'])
            normalized[arm] = score
    minimum = min(normalized.values())
    return [arm for arm in arms if math.isclose(normalized[arm], minimum, abs_tol=1e-6)]


def runs_z(arms):
    # Wald-Wolfowitz runs test for sequences with two arms
    values = sorted(set(arms))
    if len(values) != 2:
        return None
    n1 = sum(arm == values[0] for arm in arms)
    n2 = len(arms) - n1
    n = n1 + n2
    runs = 1 + sum(a != b for a, b in zip(arms, arms[1:]))
    mean = 2 * n1 * n2 / n + 1
    variance = 2 * n1 * n2 * (2 * n1 * n2 - n) / (n ** 2 * (n - 1))
    return (runs - mean) / math.sqrt(variance) if variance > 0 else None


def list_capacity(configuration, levels):
    combinations = math.prod(len(stratum['values']) for stratum in configuration['strata'])
    if configuration['stratifyBySite']:
        site = next(site for site in configuration['sites'] if api_id(site) == dict(levels)[SITE_FACTOR])
        return site['capacity'] // combinations
    return configuration['capacity'] // combinations


def analyze_replication(configuration, replication):
    ratios = arm_ratios(configuration)
    frame = to_frame(configuration, replication['assignments'])

    # Sequence: walk through the subjects in randomization order
    counts = {}
    positions = []
    credits = []
    preferred_hits = []
    study_counts = {}
    # stratum values without the sites like imbalance_stratum_level_max, as keys of counts
    stratum_keys = set()
    for study_position, subject in enumerate(frame.itertuples(), start=1):
        keys = [('list', subject.list)] + [('level', level) for level in subject.list]
        for key in keys:
            counts.setdefault(key, {})
        stratum_keys.update(('level', level) for level in subject.list if level[0] != SITE_FACTOR)
        preferred = preferred_arms(configuration, ratios, counts, subject.list)
        credits.append(1 / len(preferred) if subject.arm in preferred else 0.0)
        if len(preferred) < len(ratios):
            preferred_hits.append(subject.arm in preferred)
        for key in keys:
            counts[key][subject.arm] = counts[key].get(subject.arm, 0) + 1
        study_counts[subject.arm] = study_counts.get(subject.arm, 0) + 1
        positions.append({'list': str(subject.list), 'position': subject.position, 'arm': subject.arm,
                          'imbalance': imbalance(counts['list', subject.list], ratios),
                          'study_position': study_position,
                          'study_imbalance': imbalance(study_counts, ratios),
                          'stratum_imbalance': max((imbalance(counts[key], ratios) for key in stratum_keys),
                                                   default=np.nan)})
    positions = pd.DataFrame(positions)

    z_values = [z for _, group in frame.groupby('list') if (z := runs_z(list(group['arm']))) is not None]

    # Correctness
    order_numbers = frame.groupby('list')['orderNumber']
    site_capacities = {api_id(site): site['capacity'] for site in configuration['sites']}
    capacity_violations = (
            sum(len(group) > list_capacity(configuration, levels) for levels, group in frame.groupby('list'))
            + sum(n > site_capacities[site] for site, n in frame['site'].value_counts().items()))

    # marginal balance of each stratum value, the sites are handled separately
    frame['level'] = frame['list'].map(lambda levels: [level for level in levels if level[0] != SITE_FACTOR])

    # Interim balance: the first subjects in randomization order up to each checkpoint
    interim = {}
    counts_rows = []
    for fraction in CHECKPOINTS:
        size = checkpoint_size(configuration, fraction)
        label = checkpoint_label(fraction)
        if size > len(frame):
            # the study was not filled up to the checkpoint
            continue
        prefix = frame.iloc[:size]
        if fraction < 1:
            interim.update({interim_column(measure, fraction): value
                            for measure, value in balance(prefix, ratios).items()})
        counts_rows += [{'checkpoint': label, **row} for row in arm_counts(configuration, prefix, ratios)]
    counts = pd.DataFrame(counts_rows)
    counts['replication'] = replication['replication']

    final = balance(frame, ratios)
    metrics = {
        'replication': replication['replication'],
        'subjects': len(frame),
        'complete': len(frame) == configuration['capacity'],
        'duplicate_order_numbers': int(frame.duplicated(['list', 'orderNumber']).sum()),
        'lists_with_invalid_order': int(sum(sorted(group) != list(range(1, len(group) + 1))
                                            for _, group in order_numbers)),
        'duplicate_pseudonyms': int(frame['pseudonym'].duplicated().sum()),
        'capacity_violations': int(capacity_violations),
        **{f'imbalance_{measure}': value for measure, value in final.items()},
        **{interim_column(measure, fraction): interim.get(interim_column(measure, fraction), np.nan)
           for fraction in CHECKPOINTS if fraction < 1 for measure in BALANCE_MEASURES},
        'running_imbalance_max': positions['imbalance'].max(),
        'predictability': float(np.mean(credits)),
        'preferred_rate': float(np.mean(preferred_hits)) if preferred_hits else np.nan,
        'runs_z_mean': float(np.mean(z_values)) if z_values else np.nan,
    }
    positions['replication'] = replication['replication']
    return metrics, positions, counts


def wilson_interval(successes, n, z=1.96):
    p = successes / n
    center = (p + z ** 2 / (2 * n)) / (1 + z ** 2 / n)
    half = z * np.sqrt(p * (1 - p) / n + z ** 2 / (4 * n ** 2)) / (1 + z ** 2 / n)
    return center - half, center + half


def allocation_by_position(positions, ratios):
    table = pd.crosstab(positions['position'], positions['arm']).reindex(columns=list(ratios), fill_value=0)
    total = sum(ratios.values())
    expected = np.array([ratio / total for ratio in ratios.values()])
    # chi-square test of the arm counts at each position against the target ratio, Bonferroni corrected
    p_values = [stats.chisquare(row, expected * row.sum()).pvalue if len(ratios) > 1 else 1.0
                for row in table.to_numpy()]
    p_min = min(1.0, min(p_values) * len(p_values)) if p_values else np.nan
    deviation = float((table.div(table.sum(axis=1), axis=0) - expected).abs().to_numpy().max())
    return table, p_min, deviation


def plot_final_imbalance(directory, metrics):
    columns = ['imbalance_overall', 'imbalance_list_max', 'imbalance_site_max', 'imbalance_stratum_level_max']
    columns = [column for column in columns if metrics[column].notna().any()]
    fig, ax = plt.subplots(figsize=(7, 4))
    ax.boxplot([metrics[column].dropna() for column in columns])
    ax.set_xticks(range(1, len(columns) + 1), [column.removeprefix('imbalance_') for column in columns])
    ax.set_ylabel('Final imbalance (range of count / ratio)')
    ax.set_title('Final imbalance per replication')
    fig.tight_layout()
    fig.savefig(directory / 'final_imbalance.png', dpi=150)
    plt.close(fig)


def plot_interim_imbalance(directory, metrics):
    measures = [measure for measure in BALANCE_MEASURES
                if metrics[[interim_column(measure, f) for f in CHECKPOINTS]].notna().any().any()]
    fig, axes = plt.subplots(1, len(measures), figsize=(3.5 * len(measures), 4), squeeze=False)
    for ax, measure in zip(axes[0], measures):
        ax.boxplot([metrics[interim_column(measure, f)].dropna() for f in CHECKPOINTS])
        ax.set_xticks(range(1, len(CHECKPOINTS) + 1), [f'{f:.0%}' for f in CHECKPOINTS])
        ax.set_xlabel('Share of the capacity randomized')
        ax.set_title(measure)
    axes[0][0].set_ylabel('Imbalance (range of count / ratio)')
    fig.suptitle('Interim imbalance per replication')
    fig.tight_layout()
    fig.savefig(directory / 'interim_imbalance.png', dpi=150)
    plt.close(fig)


def stratum_balance(counts, ratios):
    """Mean counts per arm, pooled percentages and imbalance of each group at each checkpoint."""
    if counts.empty:
        return counts
    n_columns = [f'n_{arm}' for arm in ratios]
    grouped = counts.groupby(['checkpoint', 'factor', 'value'], sort=False)
    table = grouped[['subjects', *n_columns]].mean().add_suffix('_mean')
    sums = grouped[['subjects', *n_columns]].sum()
    for arm, column in zip(ratios, n_columns):
        table[f'percent_{arm}'] = (100 * sums[column] / sums['subjects'].where(sums['subjects'] > 0)).round(1)
    table['imbalance_mean'] = grouped['imbalance'].mean()
    table['imbalance_max'] = grouped['imbalance'].max()
    return table.reset_index()


def list_count(configuration):
    combinations = math.prod(len(stratum['values']) for stratum in configuration['strata'])
    return combinations * (len(configuration['sites']) if configuration['stratifyBySite'] else 1)


def reference_imbalance(configuration, ratios, n):
    """Reference for the imbalance after n assignments as (label, values), or (None, None) without one."""
    randomization = configuration['randomization']
    n = np.asarray(n, dtype=float)
    if randomization['algorithm'] == 'BLOCKED':
        return 'bound of the largest block', np.full_like(n, randomization['maxBlocksize'] / sum(ratios.values()))
    if randomization['algorithm'] == 'COINTOSS' and len(ratios) == 2:
        # n_A / r_A - n_B / r_B is a random walk whose steps have the variance 1 / (r_A r_B),
        # so its absolute value has the mean sqrt(2 n / (pi r_A r_B)) for large n
        r_a, r_b = ratios.values()
        return 'expected |random walk|', np.sqrt(2 * n / (np.pi * r_a * r_b))
    return None, None


def plot_distribution(ax, grouped, band_label):
    ax.fill_between(grouped.quantile(0.05).index, grouped.quantile(0.05), grouped.quantile(0.95),
                    alpha=0.3, label=band_label)
    ax.plot(grouped.median().index, grouped.median(), label='median')
    ax.plot(grouped.max().index, grouped.max(), linestyle=':', label='maximum')


def plot_running_imbalance(directory, positions, configuration, ratios):
    fig, ax = plt.subplots(figsize=(7, 4))
    plot_distribution(ax, positions.groupby('position')['imbalance'], '5–95 % of lists')
    n = np.arange(1, positions['position'].max() + 1)
    label, values = reference_imbalance(configuration, ratios, n)
    if label is not None:
        ax.plot(n, values, color='red', linestyle='--', label=label)
    ax.xaxis.set_major_locator(MaxNLocator(integer=True))
    ax.xaxis.set_major_formatter(StrMethodFormatter('{x:.0f}'))
    ax.set_xlabel('Position in the list')
    ax.set_ylabel('Imbalance after the assignment')
    ax.legend()
    fig.tight_layout()
    fig.savefig(directory / 'running_imbalance.png', dpi=150)
    plt.close(fig)


def plot_study_imbalance(directory, positions, configuration, ratios):
    """Like plot_running_imbalance, but for the entire study and its worst stratum value."""
    panels = [('study_imbalance', 'Allocation (study)')]
    if positions['stratum_imbalance'].notna().any():
        panels.append(('stratum_imbalance', 'Stratum (worst value)'))
    fig, axes = plt.subplots(1, len(panels), figsize=(7 * len(panels), 4), squeeze=False, sharey=True)
    for ax, (column, title) in zip(axes[0], panels):
        plot_distribution(ax, positions.groupby('study_position')[column], '5–95 % of replications')
        ax.xaxis.set_major_locator(MaxNLocator(integer=True))
        ax.xaxis.set_major_formatter(StrMethodFormatter('{x:.0f}'))
        ax.set_xlabel('Position in the study')
        ax.set_title(title)
    n = np.arange(1, positions['study_position'].max() + 1)
    label, values = reference_imbalance(configuration, ratios, n)
    # the bound of the blocks holds per list, for several lists only the loose bound of their sum would
    if label is not None and (configuration['randomization']['algorithm'] != 'BLOCKED'
                              or list_count(configuration) == 1):
        axes[0][0].plot(n, values, color='red', linestyle='--', label=label)
    axes[0][0].set_ylabel('Imbalance after the assignment')
    for ax in axes[0]:
        ax.legend()
    fig.tight_layout()
    fig.savefig(directory / 'study_imbalance.png', dpi=150)
    plt.close(fig)


def plot_allocation_probability(directory, table, ratios):
    n = table.sum(axis=1)
    total = sum(ratios.values())
    fig, ax = plt.subplots(figsize=(7, 4))
    for arm, ratio in ratios.items():
        line, = ax.plot(table.index, table[arm] / n, label=arm)
        low, high = wilson_interval(table[arm], n)
        ax.fill_between(table.index, low, high, color=line.get_color(), alpha=0.2)
        ax.axhline(ratio / total, color=line.get_color(), linestyle='--')
    ax.set_ylim(0, 1)
    ax.set_xlabel('Position in the list')
    ax.set_ylabel('Probability of the arm (95 % CI)')
    ax.set_title('Allocation probability by position (dashed: target ratio)')
    ax.legend()
    fig.tight_layout()
    fig.savefig(directory / 'allocation_probability.png', dpi=150)
    plt.close(fig)


def plot_tradeoff(directory, summary):
    fig, ax = plt.subplots(figsize=(7, 5))
    for row in summary.itertuples():
        ax.scatter(row.imbalance_list_max_mean, row.predictability_mean)
        ax.annotate(row.study, (row.imbalance_list_max_mean, row.predictability_mean),
                    textcoords='offset points', xytext=(5, 5))
    ax.set_xlabel('Mean final imbalance of the lists')
    ax.set_ylabel('Mean rate of correct guesses')
    ax.set_title('Predictability vs. imbalance')
    fig.tight_layout()
    fig.savefig(directory / 'tradeoff.png', dpi=150)
    plt.close(fig)


def plot_balance(directory, summary):
    labels = [f'{fraction:.0%}' for fraction in CHECKPOINTS]
    fig, ax = plt.subplots(figsize=(7, 4))
    handles = []
    references = set()
    for row, color in zip(summary.to_dict('records'), plt.rcParams['axes.prop_cycle'].by_key()['color'] * 10):
        overall = [row[f'{interim_column("overall", fraction)}_mean'] for fraction in CHECKPOINTS]
        stratum = [row[f'{interim_column("stratum_level_max", fraction)}_mean'] for fraction in CHECKPOINTS]
        ax.plot(labels, overall, color=color, marker='o')
        if not all(np.isnan(stratum)):
            ax.plot(labels, stratum, color=color, marker='o', linestyle=':')
        if isinstance(row['reference'], str):
            reference = [row[f'reference_imbalance_{checkpoint_label(fraction)}'] for fraction in CHECKPOINTS]
            ax.plot(labels, reference, color='red', linestyle='--')
            references.add(row['reference'])
        handles.append(Line2D([], [], color=color, label=row['study']))
    handles += [Line2D([], [], color='gray', label='allocation (overall)'),
                Line2D([], [], color='gray', linestyle=':', label='stratum (worst value)')]
    handles += [Line2D([], [], color='red', linestyle='--', label=reference) for reference in sorted(references)]
    ax.set_ylim(bottom=0)
    ax.set_xlabel('Share of the capacity randomized')
    ax.set_ylabel('Mean imbalance (range of count / ratio)')
    ax.set_title('Allocation and stratum balance')
    ax.legend(handles=handles)
    fig.tight_layout()
    fig.savefig(directory / 'balance.png', dpi=150)
    plt.close(fig)


def analyze_configuration(configuration_dir, replications, threads):
    configuration = read_json(configuration_dir / 'configuration.json')
    ratios = arm_ratios(configuration)
    directory = configuration_dir / 'analysis'
    directory.mkdir(exist_ok=True)

    results = [analyze_replication(configuration, replication) for replication in replications]
    metrics = pd.DataFrame([metrics for metrics, _, _ in results])
    positions = pd.concat([positions for _, positions, _ in results], ignore_index=True)
    counts = pd.concat([counts for _, _, counts in results], ignore_index=True)
    table, allocation_p, allocation_deviation = allocation_by_position(positions, ratios)

    metrics.to_csv(directory / 'replications.csv', index=False)
    stratum_balance(counts, ratios).to_csv(directory / 'stratum_balance.csv', index=False)
    plot_final_imbalance(directory, metrics)
    plot_interim_imbalance(directory, metrics)
    plot_running_imbalance(directory, positions, configuration, ratios)
    plot_study_imbalance(directory, positions, configuration, ratios)
    plot_allocation_probability(directory, table, ratios)

    randomization = configuration['randomization']
    summary = {
        'configuration': configuration_dir.name,
        'study': configuration['name'],
        'algorithm': randomization['algorithm'],
        'replications': len(replications),
        'threads': threads,
        'incomplete': int((~metrics['complete']).sum()),
    }
    for column in ['duplicate_order_numbers', 'lists_with_invalid_order', 'duplicate_pseudonyms',
                   'capacity_violations']:
        summary[column] = int(metrics[column].sum())
    interim_columns = [interim_column(measure, fraction)
                       for fraction in CHECKPOINTS if fraction < 1 for measure in BALANCE_MEASURES]
    for column in ['imbalance_overall', 'imbalance_list_max', 'imbalance_site_max', 'imbalance_stratum_level_max',
                   *interim_columns, 'running_imbalance_max']:
        summary[f'{column}_mean'] = metrics[column].mean()
        summary[f'{column}_max'] = metrics[column].max()
    sizes = [checkpoint_size(configuration, fraction) for fraction in CHECKPOINTS]
    summary['reference'], references = reference_imbalance(configuration, ratios, sizes)
    for fraction, value in zip(CHECKPOINTS, references if references is not None else [np.nan] * len(sizes)):
        summary[f'reference_imbalance_{checkpoint_label(fraction)}'] = value
    summary['predictability_mean'] = metrics['predictability'].mean()
    summary['preferred_rate_mean'] = metrics['preferred_rate'].mean()
    summary['expected_preferred_rate'] = randomization.get('imbalanceBias', np.nan)
    summary['runs_z_mean'] = metrics['runs_z_mean'].mean()
    summary['allocation_max_deviation'] = allocation_deviation
    summary['allocation_p_bonferroni'] = allocation_p
    return summary


def run(run_dir):
    settings = read_json(run_dir / 'simulation.json')
    threads = settings.get('threads', 1)

    summaries = []
    for configuration_dir in sorted(path.parent for path in run_dir.glob('*/configuration.json')):
        replications = load_replications(configuration_dir)
        if not replications:
            print(f"No replications of '{configuration_dir.name}' found")
            continue
        print(f"Analysing {len(replications)} replications of '{configuration_dir.name}'")
        summaries.append(analyze_configuration(configuration_dir, replications, threads))
    if not summaries:
        return

    directory = run_dir / 'analysis'
    directory.mkdir(exist_ok=True)
    summary = pd.DataFrame(summaries)
    summary.to_csv(directory / 'summary.csv', index=False)
    plot_tradeoff(directory, summary)
    plot_balance(directory, summary)

    with pd.option_context('display.max_columns', None, 'display.width', 200):
        print(summary.set_index('study').T)
    if threads > 1:
        print('Warning: the run used several threads, so the sequence metrics only approximate the real order.')
    print(f'Saved the analysis to {run_dir}')


def main():
    parser = argparse.ArgumentParser(description='Analyses a run of simulate.py.')
    parser.add_argument('run_dir', nargs='?', type=Path, help='directory of the run, by default the latest one')
    args = parser.parse_args()
    run_dir = args.run_dir or latest_run_dir()
    if run_dir is None:
        print(f'No runs found in {RESULTS_DIR}')
        return
    run(run_dir)


if __name__ == '__main__':
    main()
