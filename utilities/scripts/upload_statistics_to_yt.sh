#!/bin/bash

set -e

source .venv/bin/activate
python -m scripts.yt_upload save_run_statistics \
  --yt-destination="//home/matterhorn/users/robot_matterhorn/python/teamcity/statistics"