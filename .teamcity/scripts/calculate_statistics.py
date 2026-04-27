import json
import os
from pathlib import Path
from typing import Dict, List, Union

def load_json_file(filepath: str) -> Dict:
    with open(filepath, 'r') as f:
        return json.load(f)

def combine_reports(file_paths: List[str]) -> Dict[str, Union[int, float, List[str]]]:
    total_stats = {
        "submitted_instances": 0,
        "resolved_instances": 0,
        "unresolved_instances": 0,
        "error_instances": 0
    }

    # Process each file
    for filepath in file_paths:
        try:
            data = load_json_file(filepath)

            # Add numeric values
            total_stats["submitted_instances"] += data.get("submitted_instances", 0)
            total_stats["resolved_instances"] += data.get("resolved_instances", 0)
            total_stats["unresolved_instances"] += data.get("unresolved_instances", 0)
            total_stats["error_instances"] += data.get("error_instances", 0)

        except Exception as e:
            print(f"Error processing file {filepath}: {e}")

    # Calculate success rate
    if total_stats["submitted_instances"] > 0:
        total_stats["success_rate"] = (total_stats["resolved_instances"] / total_stats["submitted_instances"]) * 100
    else:
        total_stats["success_rate"] = 0.0

    return total_stats

def print_summary(stats: Dict[str, Union[int, float, List[str]]]):
    print(f"##teamcity[buildStatus text='Success rate: {stats['success_rate']:.2f}%']")
    print(f"##teamcity[buildStatisticValue key='SWEBLite_%size%x_SuccessRate' value='{stats['success_rate']:.2f}']")
    print(f"##teamcity[buildStatisticValue key='SWEBLite_%size%x_Resolved' value='{stats['resolved_instances']}']")
    print(f"##teamcity[buildStatisticValue key='SWEBLite_%size%x_Failed' value='{stats['unresolved_instances']}']")
    print(f"##teamcity[buildStatisticValue key='SWEBLite_%size%x_Error' value='{stats['error_instances']}']")


current_dir = Path(".")
json_files = list(current_dir.glob("*.json"))

if json_files:
    file_paths = [str(f) for f in json_files]
    print(f"Found {len(file_paths)} JSON files in current directory")

    combined_stats = combine_reports(file_paths)
    print_summary(combined_stats)

else:
    print("No JSON reports found in current directory.")
