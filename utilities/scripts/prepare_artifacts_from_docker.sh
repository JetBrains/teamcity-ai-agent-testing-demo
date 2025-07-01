#!/bin/bash

set -e

mkdir "%instance.env.name%"
cp junie_artifacts/patch.patch "%instance.env.name%/patch.patch"
mv junie_artifacts/.matterhorn .matterhorn
mv junie_artifacts/idea-logs.tar.gz idea-logs.tar.gz
echo "##teamcity[setParameter name='env.MATTERHORN_CACHES' value='%teamcity.build.checkoutDir%']"
