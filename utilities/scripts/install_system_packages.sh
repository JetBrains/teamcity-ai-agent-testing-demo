#!/usr/bin/env bash

set -e

# Function to update the apt cache with retry logic
update_apt_cache_with_retries() {
  max_attempts=10
  attempt_num=1

  sleep 5
  echo "##teamcity[blockOpened name='Update cache' description='Update apt cache']"

  until sudo apt update; do
    if (( attempt_num == max_attempts )); then
      echo "Failed to update apt cache after $max_attempts attempts. Exiting."
      echo "##teamcity[blockClosed name='Update cache']"
      exit 1
    fi
    echo "Error updating apt cache. Another process might be using APT."
    echo "Retrying attempt $((attempt_num+1)) of $max_attempts in a few seconds..."
    attempt_num=$((attempt_num+1))
    sleep 60
  done

  echo "##teamcity[blockClosed name='Update cache']"
}

# Function to install packages with retry logic
install_packages_with_retries() {
  max_attempts=10
  attempt_num=1

  sleep 5
  echo "##teamcity[blockOpened name='Install packages' description='Run apt install']"

  until sudo apt install -y jq python3-dev conda; do
    if (( attempt_num == max_attempts )); then
      echo "Failed to install packages after $max_attempts attempts. Exiting."
      echo "##teamcity[blockClosed name='Install packages']"
      exit 1
    fi
    echo "Error installing packages. Another process might be using APT."
    echo "Retrying attempt $((attempt_num+1)) of $max_attempts in a few seconds..."
    attempt_num=$((attempt_num+1))
    sleep 120
  done

  echo "##teamcity[blockClosed name='Install packages']"
}

# Run the apt update with retry
update_apt_cache_with_retries

# Run the package installation with retry
install_packages_with_retries
