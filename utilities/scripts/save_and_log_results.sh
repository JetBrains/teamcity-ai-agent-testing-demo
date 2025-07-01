#!/bin/bash

set -e

cd "%teamcity.build.checkoutDir%/swebench_matterhorn"

echo "##teamcity[blockOpened name='Activating venv']"
if [ "$EXECUTION_MODE" = "docker" ]; then
  echo "Docker environment, installing dependency..."
  python3.10 -m venv .venv
  source .venv/bin/activate
  pip install -r requirements.txt
else
  echo "TeamCity environment, activating Conda env..."
  source activate base
fi
echo "##teamcity[blockClosed name='Activating venv']"


echo "##teamcity[blockOpened name='Preparing local artifacts']"
set +e

echo "##teamcity[blockOpened name='debug ls']"
echo "pwd is $(pwd)"
ls
echo "##teamcity[blockClosed name='debug ls']"

# Create trajectories directory for local storage
mkdir -p trajectories

# Collect metadata
GIT_BRANCH="%teamcity.build.vcs.branch.Matterhorn_CoreCi_HttpsGithubComJetBrainsMatterhornCore%"
GIT_REVISION="%build.vcs.number.Matterhorn_CoreCi_HttpsGithubComJetBrainsMatterhornCore%"
GIT_URL="%vcsroot.Matterhorn_CoreCi_HttpsGithubComJetBrainsMatterhornCore.url%"
LATEST_COMMIT_TIMESTAMP="$(git show --no-patch --format=%ct)"

# Create trajectory metadata
cat > trajectory_metadata.json << EOF
{
  "trajectory_id": "%teamcity.trajectory.id%",
  "agent_mode": "$AGENT_MODE",
  "git_branch": "$GIT_BRANCH",
  "git_revision": "$GIT_REVISION",
  "git_url": "$GIT_URL",
  "latest_commit_timestamp": "$LATEST_COMMIT_TIMESTAMP",
  "dataset": "python-swe-bench",
  "build_id": "%teamcity.build.id%",
  "run_id": "%teamcity.run.id%"
}
EOF

# Copy trajectory files to trajectories directory with metadata
if [ -f "trajectory.json" ]; then
  cp trajectory.json trajectories/
fi

if [ -f "trajectory_extended.json" ]; then
  cp trajectory_extended.json trajectories/
fi

# For cloud agents, handle multi-trajectories
if [ "$AGENT_MODE" = "ElectricJuniorCloud" ]; then
  if [ -d "trajectory_candidates" ]; then
    cp -r trajectory_candidates trajectories/
  fi
fi

LOCAL_EXIT_CODE=0
echo "##teamcity[blockClosed name='Preparing local artifacts']"


# Do not move up, it uses some artifacts from 'YT save' step
echo "##teamcity[blockOpened name='Matterhorn artifacts' description='Publishing Matterhorn artifacts']"
cd "%teamcity.build.checkoutDir%"
tar -czvf matterhorn.tar.gz .matterhorn
echo "##teamcity[blockClosed name='Matterhorn artifacts']"

exit "$LOCAL_EXIT_CODE"
