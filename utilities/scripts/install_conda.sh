#!/usr/bin/env bash

set -e

# Function to download and install the GPG key
install_gpg_key() {
  echo "Downloading and installing the GPG key..."
  # Download the GPG key and convert it using dearmor
  if ! curl -s https://repo.anaconda.com/pkgs/misc/gpgkeys/anaconda.asc | gpg --dearmor > conda.gpg; then
    echo "Error downloading the GPG key."
    return 1
  fi

  # Install the GPG key to the trusted store
  if ! sudo install -o root -g root -m 644 conda.gpg /usr/share/keyrings/conda-archive-keyring.gpg; then
    echo "Error installing the GPG key."
    return 1
  fi

  echo "Checking the key fingerprint..."
  # Check the fingerprint of the key
  if ! gpg --keyring /usr/share/keyrings/conda-archive-keyring.gpg --no-default-keyring --fingerprint 34161F5BF5EB1D4BFBBB8F0A8AEB4F8B29D82806; then
    echo "Error verifying the key fingerprint."
    return 1
  fi
}

# Attempts to perform the key installation
max_attempts=3
attempt_num=1

until install_gpg_key; do
  if (( attempt_num == max_attempts )); then
    echo "Failed to download and install the GPG key after $max_attempts attempts. Exiting."
    exit 1
  fi
  echo "Retrying attempt $((attempt_num+1)) of $max_attempts in a few seconds..."
  attempt_num=$((attempt_num+1))
  sleep 5
done

echo "GPG key successfully installed!"

# Update APT source list with the conda repository
echo "Updating APT source list with the conda repository..."
echo "deb [arch=amd64 signed-by=/usr/share/keyrings/conda-archive-keyring.gpg] https://repo.anaconda.com/pkgs/misc/debrepo/conda stable main" | sudo tee -a /etc/apt/sources.list.d/conda.list
