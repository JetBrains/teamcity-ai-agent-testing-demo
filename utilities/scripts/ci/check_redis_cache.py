import json

from pathlib import Path

STATS_FILE_PATH = Path("stats_per_run.json")
CACHE_HIT_RATIO_TH = 0.90


def check() -> None:
    stats = json.loads(STATS_FILE_PATH.read_text())
    cache_hit_ratio = stats["cache_hit_ratio"]

    if cache_hit_ratio >= CACHE_HIT_RATIO_TH:
        print(f"##teamcity[buildStatus status='SUCCESS' text='Success ({cache_hit_ratio} >= {CACHE_HIT_RATIO_TH})']")
    else:
        print(f"##teamcity[buildStatus status='FAILURE' text='Failure ({cache_hit_ratio} < {CACHE_HIT_RATIO_TH})']")


if __name__ == "__main__":
    check()