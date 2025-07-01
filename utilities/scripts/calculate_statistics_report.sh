#!/usr/bin/env bash

set -e

python3 -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
python -m scripts.statistics.calculate --run_id="%teamcity.run.id%" --cluster_name="all"
