"""
Example script to demonstrate how to get content of a problem statement
for a task from SWEBench datasets.

This script shows how to load SWEBench Lite dataset and extract the problem
statement for a specific task instance ID (e.g. "django__django-10924").
"""

from datasets import load_dataset
import json

def get_problem_statement(instance_id: str, dataset_name: str = "princeton-nlp/SWE-bench_Lite"):
    """
    Get the problem statement for a specific task instance from SWEBench dataset.

    Args:
    instance_id (str): Task instance ID (e.g., "django__django-10924")
    dataset_name (str): Name of the SWEBench dataset to load

    Returns:
    dict: Task instance data including problem statement
    """
    print(f"Loading dataset: {dataset_name}")

    # Load the dataset
    dataset = load_dataset(dataset_name, cache_dir="%env.HF_DATASETS_CACHE%")

    # SWEBench Lite has 'test' split
    test_data = dataset['test']

    print(f"Dataset loaded with {len(test_data)} instances")

    # Find the specific instance
    for instance in test_data:
        if instance['instance_id'] == instance_id:
            return instance

    print(f"Instance ID '{instance_id}' not found in dataset")
    return None


def display_problem_info(instance_data):
    """
    Display key information about the problem instance.

    Args:
    instance_data (dict): Task instance data
    """
    if not instance_data:
        print("No instance data to display")
        return

    print("\n" + "="*80)
    print("PROBLEM INSTANCE INFORMATION")
    print("="*80)

    print(f"Instance ID: {instance_data['instance_id']}")
    print(f"Repository: {instance_data['repo']}")
    print(f"Base Commit: {instance_data['base_commit']}")
    print(f"Version: {instance_data['version']}")

    print("\n" + "-"*80)
    print("PROBLEM STATEMENT:")
    print("-"*80)
    print(instance_data['problem_statement'])

    print("\n" + "-"*80)
    print("ADDITIONAL FIELDS:")
    print("-"*80)
    for key, value in instance_data.items():
        if key not in ['instance_id', 'repo', 'base_commit', 'version', 'problem_statement']:
            if isinstance(value, str) and len(value) > 200:
                print(f"{key}: {value[:200]}... (truncated)")
            else:
                print(f"{key}: {value}")

def main():
    # Target instance ID from SWEBench Lite
    target_instance = "%instance_id%"
    instance_data = get_problem_statement(target_instance)

    if instance_data:
        display_problem_info(instance_data)

        # Save problem statement to Markdown file
        if instance_data['problem_statement']:
            output_file = f"{target_instance}_issue.md"
            with open(output_file, 'w', encoding='utf-8') as f:
                f.write(instance_data["problem_statement"])

            print(f"\nInstance problem statement saved to: {output_file}")

        if instance_data['hints_text']:
            output_file = f"{target_instance}_hints.md"
            with open(output_file, 'w', encoding='utf-8') as f:
                f.write(instance_data["hints_text"])

            print(f"\nInstance problem statement saved to: {output_file}")


        output_file = f"{target_instance}.json"
        with open(output_file, 'w', encoding='utf-8') as f:
            json.dump(instance_data, f, indent=2, ensure_ascii=False)
        print(f"\nFull instance data saved to: {output_file}")

if __name__ == "__main__":
    main()
