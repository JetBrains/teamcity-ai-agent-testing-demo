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


echo "##teamcity[blockOpened name='Publishing to YT']"
export YT_CONFIG_PATCHES="{proxy={force_ipv4=%true}}"
set +e

echo "##teamcity[blockOpened name='debug ls']"
echo "pwd is $(pwd)"
ls
echo "##teamcity[blockClosed name='debug ls']"

GIT_BRANCH="%teamcity.build.vcs.branch.Matterhorn_CoreCi_HttpsGithubComJetBrainsMatterhornCore%"
GIT_REVISION="%build.vcs.number.Matterhorn_CoreCi_HttpsGithubComJetBrainsMatterhornCore%"
GIT_URL="%vcsroot.Matterhorn_CoreCi_HttpsGithubComJetBrainsMatterhornCore.url%"
LATEST_COMMIT_TIMESTAMP="$(git show --no-patch --format=%ct)"



if [[ "$AGENT_MODE" != "ElectricJunior" ]]; then
  FLAG=1
else
  FLAG=
fi

if [ "$AGENT_MODE" = "ElectricJuniorCloud" ]; then
# for cloud we save three trajectories and
# full trajectory (with every candidate and critique (judge) log) as final without any suffix
  python -m scripts.yt_upload save_matterhorn_agent_multitrajectories \
    --git-branch="$GIT_BRANCH" \
    --git-revision="$GIT_REVISION" \
    --git-url="$GIT_URL" \
    --latest-commit-timestamp="$LATEST_COMMIT_TIMESTAMP" \
    --yt-destination="//home/matterhorn/users/robot_matterhorn/all/teamcity/trajectories" \
    --trajectory-id="%teamcity.trajectory.id%" \
    --dataset="python-swe-bench"
else
  python -m scripts.yt_upload save_matterhorn_agent_trajectory_extended \
    --git-branch="$GIT_BRANCH" \
    --git-revision="$GIT_REVISION" \
    --git-url="$GIT_URL" \
    --latest-commit-timestamp="$LATEST_COMMIT_TIMESTAMP" \
    --yt-destination="//home/matterhorn/users/robot_matterhorn/all/teamcity/trajectories" \
    --trajectory-id="%teamcity.trajectory.id%" \
    --dataset="python-swe-bench" \
    --agent_mode="$AGENT_MODE"

  python -m scripts.yt_upload save_matterhorn_agent_trajectory \
    --git-branch="$GIT_BRANCH" \
    --git-revision="$GIT_REVISION" \
    --git-url="$GIT_URL" \
    --latest-commit-timestamp="$LATEST_COMMIT_TIMESTAMP" \
    --yt-destination="//home/matterhorn/users/robot_matterhorn/python/teamcity/trajectories" \
    --yt-destination-caches="//home/matterhorn/users/robot_matterhorn/python/teamcity/caches" \
    --trajectory-id="%teamcity.trajectory.id%" ${FLAG:+--skip-yt-upload}
fi

YT_EXIT_CODE=$?
echo "##teamcity[blockClosed name='Publishing to YT']"


# Do not move up, it uses some artifacts from 'YT save' step
echo "##teamcity[blockOpened name='Matterhorn artifacts' description='Publishing Matterhorn artifacts']"
cd "%teamcity.build.checkoutDir%"
tar -czvf matterhorn.tar.gz .matterhorn
echo "##teamcity[blockClosed name='Matterhorn artifacts']"

exit "$YT_EXIT_CODE"
