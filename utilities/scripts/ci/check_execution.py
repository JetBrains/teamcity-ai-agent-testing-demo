import json

from pathlib import Path

STATS_FILE_PATH = Path("stats_per_run.json")


def check() -> None:
    stats = json.loads(STATS_FILE_PATH.read_text())

    total_tasks = stats["total_tasks"]
    resolved_tasks = stats["resolved_sum"]

    if resolved_tasks == total_tasks:
        print(f"##teamcity[buildStatus status='SUCCESS' text='Success ({resolved_tasks} of {total_tasks} passed)']")
    else:
        print(f"##teamcity[buildStatus status='FAILURE' text='Failure ({resolved_tasks} of {total_tasks} passed)']")


if __name__ == "__main__":
    check()