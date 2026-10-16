# Simulation

Scripts for validating the randomization algorithms of RandIMI.
- `simulate.py` creates studies through the API, activates them so that they randomize like in production, fills them
  completely, downloads the assignments and deletes the studies again (see `delete` in `simulation.toml`).
  Since active studies cannot be deleted, they are archived first and then marked as deleted.
- `analyze.py` computes metrics and figures from the downloaded assignments.

## Installation

The dependencies are managed with [uv](https://docs.astral.sh/uv/):

```shell
uv sync
```

Copy `simulation.example.toml` to `simulation.toml` and adjust the credentials of the API user.
The studies to simulate are listed in `studies`; each entry references a file in the `studies` directory
that uses the format of the request body of `POST /api/v2/study`.
Files ending in `.example.json` are versioned, all other configurations are ignored by git.

## Usage

Start RandIMI locally (`http://localhost:8080/RandIMI`), then run:

```shell
uv run simulate.py
uv run analyze.py [run directory]
```

Each call of `simulate.py` is a run that creates `replications` studies per configuration.
Replication `i` uses the seed `seed + i` for the order of the subjects and for the seeds of the sites, so each
replication gets new site seeds, but a run with the same `seed` is reproducible. Seeds configured for a site in the
study configuration are kept, so such a site randomizes identically in every replication.
`analyze.py` analyses the given run, by default the latest one, and writes its results next to the data:

```
results/
└── <timestamp>/                     one run of simulate.py
    ├── simulation.json              settings of the run: seed, replications, threads, workers, studies
    ├── analysis/
    │   ├── summary.csv              one row per configuration
    │   ├── tradeoff.png             predictability vs. imbalance of all configurations
    │   └── balance.png              mean allocation and stratum balance of all configurations at the checkpoints
    └── <configuration>/             e.g. blocked.example
        ├── configuration.json       the study configuration
        ├── replications/
        │   └── <replication>/
        │       ├── assignments.json the active subjects exported with GET /study/{studyApiId}/subject-lists
        │       └── study.json       API ID, seed and site seeds of the study
        └── analysis/
            ├── replications.csv     the metrics of each replication
            ├── stratum_balance.csv  counts per arm of the study, each site and stratum value at each checkpoint
            └── final_imbalance.png, interim_imbalance.png, running_imbalance.png, study_imbalance.png,
                allocation_probability.png
```

Use at least a few hundred replications, since most metrics are distributions over replications.
The sequence metrics order the subjects by the index in their pseudonym, i.e., the planned order.
This is the real order only with `threads = 1`, so use several threads only to test concurrency.
To speed up a run, simulate several studies in parallel with `workers` instead: each study (one replication of
one configuration) is handled by a single worker, so its order is still the planned one.

## Metrics

The imbalance of a group of subjects is the range of the counts per arm divided by the arm ratio,
i.e., `|n_A - n_B|` for two arms with ratio 1:1.

| Metric | Description | Expected |
|---|---|---|
| `incomplete`, `capacity_violations` | Studies that were not filled completely; lists or sites with more subjects than their capacity | 0 |
| `duplicate_order_numbers`, `lists_with_invalid_order` | Order numbers used twice within a list; lists whose order numbers are not 1…n | 0 |
| `duplicate_pseudonyms` | Pseudonyms assigned twice | 0 |
| `imbalance_overall`, `imbalance_list_max`, `imbalance_site_max`, `imbalance_stratum_level_max` | Final imbalance of the study, and the largest one of its lists, sites and stratum values (marginal balance) | BLOCKED: 0 per list if its capacity is a multiple of the sum of the ratios; MINIMIZATION: small per stratum value; COINTOSS: about √(2n / (π r_A r_B)) for two arms, √(2n/π) for 1:1 |
| `imbalance_<measure>_25pct`, `_50pct`, `_75pct` | Interim imbalance of the same four measures after 25, 50 and 75 % of the capacity were randomized (100 % is the final imbalance) | Like the final imbalance; COINTOSS grows with n |
| `running_imbalance_max` | Largest imbalance of a list at any time | BLOCKED: at most `maxBlocksize` / sum of the ratios |
| `predictability` | Rate of correct guesses of the next arm (Blackwell–Hodges convergence strategy): the arm that is behind in the list, for MINIMIZATION the arm the algorithm prefers. Ties count as 1/k | COINTOSS: 1/k; higher for smaller blocks and larger `imbalanceBias` |
| `preferred_rate` | Rate of assignments to the preferred arm if there is no tie | MINIMIZATION: `imbalanceBias` |
| `runs_z_mean` | Mean z-statistic of the Wald–Wolfowitz runs test of the lists (two arms only) | COINTOSS: 0; positive if the arms alternate more often than by chance |
| `allocation_max_deviation`, `allocation_p_bonferroni` | Largest deviation of the arm probability at a position in the list from the target ratio, and the Bonferroni-corrected p-value of the χ² tests per position | Small deviation, p not significant |

### Figures

- **Final imbalance:** boxplots of the final imbalances over the replications.
- **Interim imbalance:** boxplots of the overall, list, site and stratum value imbalance after 25, 50, 75 and
  100 % of the capacity, i.e., the allocation and stratum balance at the interim analyses and at the end.
- **Imbalance over time:** median, 5–95 % band and maximum of the imbalance by position in the list, with the
  bound of the largest block (BLOCKED) or the expected imbalance of a random walk (COINTOSS with two arms).
- **Imbalance of the study over time:** the same for the entire study by position in the study, over the
  replications: the allocation balance of the study and, for configurations with strata, the stratum balance,
  i.e., the worst stratum value without the sites. The bound of the largest block is only shown for a single list.
- **Allocation probability:** probability of each arm by position in the list with 95 % Wilson intervals.
  A good algorithm stays at the target ratio at every position.
- **Balance:** mean imbalance of all configurations after 25, 50, 75 and 100 % of the capacity, one color per
  configuration. Solid: allocation balance of the study (`imbalance_overall`); dotted: stratum balance, i.e., the
  worst stratum value (`imbalance_stratum_level_max`), only for configurations with strata. In subjects, not relative.
- **Trade-off:** predictability vs. imbalance, the usual way to compare randomization procedures.

### Stratum balance table

`stratum_balance.csv` has one row per checkpoint and group: the study (`overall`), each site and each stratum
value (e.g. `Sex` = `Female`). It contains the mean number of subjects and of each arm over the replications,
the pooled percentage of each arm and the mean and maximum imbalance. Groups without subjects count as 0.

### References

- W. F. Rosenberger, J. M. Lachin: *Randomization in Clinical Trials*, 2nd ed., Wiley, 2016.
- V. W. Berger et al.: A roadmap to using randomization in clinical trials. *BMC Medical Research Methodology* 21, 168 (2021).
- D. Uschner et al.: randomizeR: An R Package for the Assessment and Implementation of Randomization in
  Clinical Trials. *Journal of Statistical Software* 85(8) (2018).
