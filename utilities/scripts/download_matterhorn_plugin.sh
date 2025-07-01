#!/usr/bin/env bash

set -e

echo "##teamcity[blockOpened name='Downloading IDE']"
IDE_VERSION="%TARGET%"
if [[ $IDE_VERSION == "243" ]]; then
  URL="https://download.jetbrains.com/python/pycharm-professional-2024.3.2.tar.gz"
  ARCHIVE_NAME="target-ide.tar.gz"
  wget -nv "$URL" -O "$ARCHIVE_NAME"
  tar -xzf "$ARCHIVE_NAME"
  mv pycharm-* pycharm
  rm -rf "$ARCHIVE_NAME"
elif [[ $IDE_VERSION == "251" ]]; then
  URL="https://download.jetbrains.com/python/pycharm-2025.1.2.tar.gz"
  ARCHIVE_NAME="target-ide.tar.gz"
  wget -nv "$URL" -O "$ARCHIVE_NAME"
  tar -xzf "$ARCHIVE_NAME"
  mv pycharm-* pycharm
  rm -rf "$ARCHIVE_NAME"
else
  exit 1
fi
echo "##teamcity[blockClosed name='Downloading IDE']"

echo "##teamcity[blockOpened name='Unpack build artifacts']"
unzip ej-*.zip -d idea-external-plugins
rm -rf ej-*.zip
echo "##teamcity[blockClosed name='Unpack build artifacts']"