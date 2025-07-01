#!/bin/bash

set -e


cd "%swe.bench.matterhorn.path%"
if [ "$EXECUTION_MODE" = "docker" ]; then
  echo "Docker environment, installing dependency..."
  apt-get update
  apt-get install -y rsync
  source activate base
  pip install -r requirements.txt
else
  mkdir "%junie.artifacts.path%"
  echo "TeamCity environment..."
  sudo apt-get update
  sudo apt-get install -y rsync
  source activate base
fi

echo "Python version: $(python --version)"


echo "##teamcity[blockOpened name='Checking python, exporting issue content as env for agent']"
cd "%swe.bench.matterhorn.path%"
export MATTERHORN_TASK=$(python -m scripts.get_issue_path get_task_content)
export MATTERHORN_TARGET_PROJECT=$(python -m scripts.get_issue_path get_repo_path)

if [ -d "/testbed" ]; then  # since public docker images use /testbed as project directory
    mv "/testbed" "$MATTERHORN_TARGET_PROJECT"
    echo "Using public docker image. Renamed /testbed to $MATTERHORN_TARGET_PROJECT"
fi

export MATTERHORN_TARGET="${MATTERHORN_TARGET_PROJECT}/issue.md"
rm -f "$MATTERHORN_TARGET" || true
echo "##teamcity[blockClosed name='Checking python, exporting issue content as env for agent']"

echo "##teamcity[blockOpened name='Setting-up guidelines & hints']"
cd "%swe.bench.matterhorn.path%"
python -m scripts.setup_guidelines --benchmark_type="python-swe-bench" --guidelines_content="%junie.guidelines.content%"
EXPLICITLY_SPECIFIED_USER_PATHS_FOR_TASK=$(python -m scripts.hints get_user_specified_files --hint_type="$HINT_TYPE")
echo "Explicitly specified user paths: ${EXPLICITLY_SPECIFIED_USER_PATHS_FOR_TASK}"
if [ -n "$EXPLICITLY_SPECIFIED_USER_PATHS_FOR_TASK" ]; then
  export EXPLICITLY_SPECIFIED_USER_PATHS_FOR_TASK
fi
echo "##teamcity[blockClosed name='Setting-up guidelines & hints']"

echo "##teamcity[blockOpened name='Prepare Conda environment']"
# MTRH-408: workaround to inform Pycharm about pre-existing conda environment, see SetupExistingCondaInterpreterWorkaround
CONDA_ENV_NAME="%instance.env.name%"
export CONDA_ENV_NAME
CONDA_BINARIES=$(which conda)
export CONDA_BINARIES
echo "Conda environment name hint for Pycharm: $CONDA_ENV_NAME"
echo "Conda binaries path hint for Pycharm: $CONDA_BINARIES"
echo "##teamcity[blockClosed name='Prepare Conda environment']"


echo "##teamcity[blockOpened name='Junie' description='Running Junie']"
echo "Matterhorn target" "$MATTERHORN_TARGET"
cd "%matterhorn.plugin.path%"
EXTERNAL_PLUGINS_PATH="%matterhorn.plugin.path%/idea-external-plugins"
set +e # to catch agent exit code
bash ./pycharm/bin/pycharm.sh \
  -Didea.platform.prefix=PyCharm \
  -Djava.awt.headless="true" \
  -Didea.plugins.path="$EXTERNAL_PLUGINS_PATH" \
  -Didea.system.path=idea-logs \
  -Djunie.python.skip.deps=true \
  matterhorn --mode="$AGENT_MODE" --target="$MATTERHORN_TARGET"
JUNIE_EXIT_CODE=$?
echo "##teamcity[blockClosed name='Junie']"


echo "##teamcity[blockOpened name='Packing IDEA logs']"
tar -czvf idea-logs.tar.gz idea-logs/log
cp idea-logs.tar.gz "%junie.artifacts.path%/idea-logs.tar.gz"
echo "##teamcity[blockClosed name='Packing IDEA logs']"


echo "##teamcity[blockOpened name='Creating patch']"
set -e
cd "%instance.repo.path%"
# delete .output.txt (dump of long command output, see com.intellij.ml.llm.matterhorn.AgentAction.Companion#truncateLongOutput)
rm -f .output.txt
git add -A
git reset .idea/
# problem with the sphinx repo and the tox.ini file, please see the comment: https://youtrack.jetbrains.com/issue/MTRH-1157
git diff --cached -- . ':(exclude)tox.ini' > patch.patch
cp patch.patch "%junie.artifacts.path%/patch.patch"
echo "##teamcity[blockClosed name='Creating patch']"

exit "$JUNIE_EXIT_CODE"