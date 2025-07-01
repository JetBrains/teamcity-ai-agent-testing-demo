from pathlib import Path
import json


def main() -> None:
    file_path = Path("stats_per_run.json")
    stats = json.loads(file_path.read_text())

    print("TEAMCITY RUN ID: %teamcity.run.id%")
    print(f"Total tasks: {stats['total_tasks']}")
    print(f"Total successful tasks: {stats.get('resolved_sum', 0)}")
    print(f"##teamcity[buildStatus text='Success rate: {stats.get('resolved_avg', 0):.2f} ({stats.get('resolved_sum', 0)} of {stats['total_tasks']})']")

    for key, value in stats.items():
        print(f"##teamcity[buildStatisticValue key='junie_{key}' value='{value}']")


if __name__ == "__main__":
    main()