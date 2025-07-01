#!/bin/bash

set -e

source activate base
cd /swebench_matterhorn
pip install -r requirements.txt
python -m scripts.save_patches

if [ -d "/testbed" ]; then  # since public docker images use /testbed as project directory
    MATTERHORN_TARGET_PROJECT=$(python -m scripts.get_issue_path get_repo_path)
    mv "/testbed" "$MATTERHORN_TARGET_PROJECT"
    echo "Using public docker image. Renamed /testbed to $MATTERHORN_TARGET_PROJECT"
fi

/bin/bash ./scripts/validate_instance.sh