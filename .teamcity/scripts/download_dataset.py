import os
from datasets import load_dataset
from huggingface_hub import snapshot_download

# Set cache directory
cache_dir = '%teamcity.build.workingDir%/dataset_cache'
os.makedirs(cache_dir, exist_ok=True)

# Download the dataset
print("Downloading SWE-bench_Lite dataset...")
dataset = load_dataset(
    "princeton-nlp/SWE-bench_Lite",
    cache_dir=cache_dir,
    split="test"
)
print(f"Dataset cached to: {cache_dir}")

# download the full dataset metadata
snapshot_download(
    repo_id="princeton-nlp/SWE-bench_Lite",
    repo_type="dataset",
    cache_dir=cache_dir,
    local_dir=f"{cache_dir}/datasets/princeton-nlp/SWE-bench_Lite"
)
