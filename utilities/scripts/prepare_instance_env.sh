#!/bin/bash

set -e

source activate base

echo "##teamcity[blockOpened name='ElectroJuniorEnvironment' description='Setting Matterhorn environment']"
cd swebench_matterhorn
/bin/bash ./scripts/prepare_env.sh
echo "##teamcity[blockClosed name='ElectroJuniorEnvironment']"
