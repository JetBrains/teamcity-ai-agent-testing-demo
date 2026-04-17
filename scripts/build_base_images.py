import re
from pathlib import Path

import docker
from datasets import load_dataset
from swebench.harness.docker_build import build_base_images
from swebench.harness.test_spec.test_spec import make_test_spec

DATASET_NAME = "princeton-nlp/SWE-bench_Lite"
CACHE_DIR = "%teamcity.build.workingDir%/dataset_cache"
TASK_IDS_FILE = Path("dataset/swebench_lite/task_ids.kt")
OUTPUT_FILE = Path("%teamcity.build.workingDir%/base_images.txt")


def load_task_ids() -> list[str]:
    task_ids_text = TASK_IDS_FILE.read_text()
    task_ids = re.findall(r'^\s*"([^"]+)",?\s*$', task_ids_text, flags=re.MULTILINE)
    if not task_ids:
        raise RuntimeError(f"No task IDs found in {TASK_IDS_FILE}")
    return task_ids


def main():
    task_ids = load_task_ids()
    task_ids_set = set(task_ids)

    print(f"Loading {DATASET_NAME} dataset using cache at {CACHE_DIR}")
    dataset = load_dataset(DATASET_NAME, cache_dir=CACHE_DIR, split="test")

    instances = [instance for instance in dataset if instance["instance_id"] in task_ids_set]
    if len(instances) != len(task_ids_set):
        found_ids = {instance["instance_id"] for instance in instances}
        missing_ids = sorted(task_ids_set - found_ids)
        raise RuntimeError(f"Missing task IDs in dataset: {missing_ids}")

    base_images = sorted({make_test_spec(instance).base_image_key for instance in instances})
    print("Preparing shared base images:")
    for image_name in base_images:
        print(f" - {image_name}")

    client = docker.from_env()
    try:
        build_base_images(client, instances, force_rebuild=False)
    finally:
        client.close()

    OUTPUT_FILE.write_text("\n".join(base_images) + "\n")
    print(f"Saved base image manifest to {OUTPUT_FILE}")


if __name__ == "__main__":
    main()
