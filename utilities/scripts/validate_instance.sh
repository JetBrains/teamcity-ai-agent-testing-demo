#!/bin/bash

set -e

source activate base
cd swebench_matterhorn
/bin/bash ./scripts/validate_instance.sh