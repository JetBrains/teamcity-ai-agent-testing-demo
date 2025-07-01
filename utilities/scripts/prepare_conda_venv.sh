#!/bin/bash

set -e

echo "##teamcity[blockOpened name='Conda' description='Setting Python environment']"
cd swebench_matterhorn
export PATH="/opt/conda/bin:${PATH}"
echo "##teamcity[setParameter name='env.PATH' value='${PATH}']"
echo -n "Conda version: "
conda --version

echo "Conda init: "
conda init
echo "Conda activate base: "
source activate base
echo "##teamcity[blockClosed name='Conda']"

# Function to install pip packages with retry logic
install_pip_packages_with_retries() {
  max_attempts=10
  attempt_num=1

  echo "##teamcity[blockOpened name='pip' description='Installing Python dependencies']"

  # First update apt and install flake8
  until sudo apt update && sudo apt install -y flake8; do
    if (( attempt_num == max_attempts )); then
      echo "Failed to update/install system packages after $max_attempts attempts. Exiting."
      echo "##teamcity[blockClosed name='pip']"
      exit 1
    fi
    echo "Error updating/installing system packages. Another process might be using APT."
    echo "Retrying attempt $((attempt_num+1)) of $max_attempts in a few seconds..."
    attempt_num=$((attempt_num+1))
    sleep 60
  done

  # Reset attempt counter for pip installations
  attempt_num=1

  # Then do pip installations
  until pip install flake8 && pip install -r requirements.txt; do
    if (( attempt_num == max_attempts )); then
      echo "Failed to install pip packages after $max_attempts attempts. Exiting."
      echo "##teamcity[blockClosed name='pip']"
      exit 1
    fi
    echo "Error installing pip packages."
    echo "Retrying attempt $((attempt_num+1)) of $max_attempts in a few seconds..."
    attempt_num=$((attempt_num+1))
    sleep 60
  done

  echo "##teamcity[blockClosed name='pip']"
}

# Run the pip installations with retry
install_pip_packages_with_retries