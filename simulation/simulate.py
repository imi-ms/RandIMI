import copy
import itertools
import json
import random
import threading
import time
import tomllib
from concurrent.futures import ThreadPoolExecutor
from pathlib import Path

import requests
from requests.auth import HTTPBasicAuth

BASE_URL = 'http://localhost:8080/RandIMI/api/v2'

SIMULATION_DIR = Path(__file__).parent
CONFIG_FILE = SIMULATION_DIR / 'simulation.toml'
STUDIES_DIR = SIMULATION_DIR / 'studies'
RESULTS_DIR = SIMULATION_DIR / 'results'


def load_config():
    with open(CONFIG_FILE, 'rb') as file:
        return tomllib.load(file)


def create_auth(config):
    return HTTPBasicAuth(config['username'], config['password'])


def load_study_configurations(config):
    configurations = []
    for name in config['studies']:
        with open(STUDIES_DIR / name, encoding='utf-8') as file:
            configurations.append((name, json.load(file)))
    return configurations


def make_unique(configuration, suffix):
    # API IDs and names of studies must be unique, so each run gets its own study
    configuration['name'] = f"{configuration['name']} {suffix}"
    if configuration.get('apiId'):
        configuration['apiId'] = f"{configuration['apiId']}-{suffix}"
    return configuration


def seed_sites(configuration, seed):
    """
    Sets the seeds of the sites without a configured seed, so a replication is reproducible.
    The seeds are drawn from their own generator, so they do not change the order of the subjects.
    """
    rng = random.Random(f'sites-{seed}')
    for site in configuration['sites']:
        if site.get('seed') is None:
            # the seeds are Java longs, i.e., signed 64-bit integers
            site['seed'] = rng.getrandbits(64) - 2 ** 63
    return configuration


def check(response):
    if not response.ok:
        print(response.status_code)
        print(response.reason)
        print(response.content)
    return response.ok


def create_study(session, auth, configuration):
    response = session.post(f'{BASE_URL}/study', json=configuration, auth=auth)
    if not check(response):
        return None
    return response.json()


def activate_study(session, auth, study):
    # active studies randomize like in production, e.g., with the random seeds of the sites instead of the test mode
    response = session.post(f'{BASE_URL}/study/{study["apiId"]}/activate', auth=auth)
    return check(response)


def delete_study(session, auth, study):
    # active studies cannot be deleted, so they are archived first and then marked as deleted
    response = session.post(f'{BASE_URL}/study/{study["apiId"]}/archive', auth=auth)
    if check(response):
        response = session.delete(f'{BASE_URL}/study/{study["apiId"]}', params={'changeReason': 'Simulation'},
                                  auth=auth)
    if check(response):
        print(f"Deleted study '{study['apiId']}'")
    else:
        print(f"Could not delete study '{study['apiId']}'")


def api_id(item):
    # The API uses the name as API ID if no API ID is set
    return item.get('apiId') or item['name']


def stratum_combinations(configuration):
    strata = [[(api_id(stratum), api_id(value)) for value in stratum['values']]
              for stratum in configuration['strata']]
    return [dict(combination) for combination in itertools.product(*strata)]


def plan_subjects(configuration, rng):
    """
    Returns the sites and strata of the subjects that exactly fill the study, in random order.
    The capacities are applied like in RandomizationService.doAssign.
    """
    combinations = stratum_combinations(configuration)
    sites = configuration['sites']
    capacity = configuration['capacity']

    if configuration['stratifyBySite']:
        # each site has one list per stratum combination, limited by the study capacity
        subjects = [(api_id(site), combination)
                    for site in sites
                    for combination in combinations
                    for _ in range(site['capacity'] // len(combinations))]
        rng.shuffle(subjects)
        return subjects[:capacity]

    # one list per stratum combination, the sites are limited independently of the lists
    list_capacity = capacity // len(combinations)
    list_slots = [combination for combination in combinations for _ in range(list_capacity)]
    site_slots = [api_id(site) for site in sites for _ in range(site['capacity'])]
    rng.shuffle(list_slots)
    rng.shuffle(site_slots)
    return list(zip(site_slots, list_slots))


def create_body(thread, index, site, strata):
    body = {
        "pseudonym": f"t{thread}-no{index}",
        "siteApiId": site,
        "studyStrataParams": strata,
    }
    return body


def send(session, auth, study, thread, index, site, strata):
    response = session.post(f'{BASE_URL}/study/{study["apiId"]}/subject',
                            json=create_body(thread, index, site, strata), auth=auth)
    return check(response)


def send_multiple(auth, study, thread, subjects, results):
    session = requests.session()
    for index, (site, strata) in subjects:
        results[index] = send(session, auth, study, thread, index, site, strata)


def fill_study(auth, study, subjects, number_threads):
    # the subjects are distributed round-robin, so a single thread sends them in the planned order
    results = [False] * len(subjects)
    indexed = list(enumerate(subjects))
    threads = [threading.Thread(target=send_multiple,
                                args=(auth, study, thread, indexed[thread::number_threads], results))
               for thread in range(number_threads)]
    for thread in threads:
        thread.start()
    for thread in threads:
        thread.join()
    print(f"Assigned {sum(results)} of {len(subjects)} subjects to study '{study['apiId']}'")


def write_json(path, content):
    with open(path, 'w', encoding='utf-8') as file:
        json.dump(content, file, indent=2)


def create_run_dir(config, timestamp):
    # each call of the script gets its own directory, so runs with different settings are not mixed
    directory = RESULTS_DIR / timestamp
    directory.mkdir(parents=True)
    settings = {key: value for key, value in config.items() if key not in ('username', 'password')}
    write_json(directory / 'simulation.json', settings)
    return directory


def create_configuration_dir(run_dir, name, configuration):
    directory = run_dir / name.removesuffix('.json')
    directory.mkdir()
    write_json(directory / 'configuration.json', configuration)
    return directory


def replication_dir(configuration_dir, replication, replications):
    # zero-padded, so the directories sort in the order of the replications
    width = len(str(replications - 1))
    return configuration_dir / 'replications' / f'{replication:0{width}d}'


def download_assignments(session, auth, study, configuration, directory):
    # without a subject list ID the subjects of all lists are exported, so every site and stratum value is requested
    sites = [api_id(site) for site in configuration['sites']]
    strata = {api_id(stratum): [api_id(value) for value in stratum['values']]
              for stratum in configuration['strata']}
    if configuration['stratifyBySite']:
        # stratification by site adds the stratum 'location' whose values are the API IDs of the sites
        strata['location'] = sites
    params = {
        'format': 'JSON',
        'includeApiIds': 'true',
        'status': 'ACTIVE',
        'sites': sites,
        'strata': json.dumps(strata),
    }
    response = session.get(f'{BASE_URL}/study/{study["apiId"]}/subject-lists', params=params, auth=auth)
    if not check(response):
        print(f"Could not download the assignments of study '{study['apiId']}'")
        return False
    directory.mkdir(parents=True)
    path = directory / 'assignments.json'
    path.write_bytes(response.content)
    print(f"Saved assignments of study '{study['apiId']}' to {path}")
    return True


def save_study(directory, study, configuration, replication, seed):
    site_seeds = {api_id(site): site['seed'] for site in configuration['sites']}
    write_json(directory / 'study.json', {'apiId': study['apiId'], 'replication': replication, 'seed': seed,
                                          'siteSeeds': site_seeds})


def simulate_replication(auth, configuration_dir, template, replication, replications, seed, timestamp,
                         number_threads, delete):
    # every replication has its own session, since requests sessions are not thread-safe
    session = requests.session()
    # each study has its own generators, so its order and site seeds do not depend on the other studies
    replication_seed = seed + replication
    configuration = make_unique(copy.deepcopy(template), f'{timestamp}-{replication}')
    seed_sites(configuration, replication_seed)
    study = create_study(session, auth, configuration)
    if study is None:
        print(f"Skipping study '{configuration['name']}'")
        return
    if not activate_study(session, auth, study):
        print(f"Skipping study '{study['apiId']}'")
        if delete:
            delete_study(session, auth, study)
        return
    print(f"Created and activated study '{study['apiId']}'")
    subjects = plan_subjects(configuration, random.Random(replication_seed))
    fill_study(auth, study, subjects, number_threads)
    directory = replication_dir(configuration_dir, replication, replications)
    if download_assignments(session, auth, study, configuration, directory):
        save_study(directory, study, configuration, replication, replication_seed)
    if delete:
        delete_study(session, auth, study)


def run():
    config = load_config()
    auth = create_auth(config)
    seed = config.get('seed', 0)
    number_threads = config.get('threads', 1)
    number_workers = config.get('workers', 1)
    replications = config.get('replications', 1)
    delete = config.get('delete', True)
    timestamp = time.strftime('%Y%m%d-%H%M%S')
    run_dir = create_run_dir(config, timestamp)
    configurations = [(create_configuration_dir(run_dir, name, template), template)
                      for name, template in load_study_configurations(config)]

    # the workers simulate whole replications, the subjects of a replication are only sent in parallel by its threads
    with ThreadPoolExecutor(max_workers=number_workers) as executor:
        futures = [executor.submit(simulate_replication, auth, configuration_dir, template, replication,
                                   replications, seed, timestamp, number_threads, delete)
                   for replication in range(replications)
                   for configuration_dir, template in configurations]
        for future in futures:
            # raises the exceptions of the workers, e.g. if RandIMI is not reachable
            future.result()

    print(f'Saved the results to {run_dir}')


if __name__ == '__main__':
    run()
