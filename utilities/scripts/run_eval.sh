#!/bin/bash

set -e

cd swebench_matterhorn

GIT_REVISION="%build.vcs.number.Matterhorn_SweBench%"
AGENT_MODE="%env.AGENT_MODE%"


if [ "$EXECUTION_MODE" = "docker" ]; then
  echo "Docker environment, installing dependency..."
  source activate base
  conda activate base
  pip install -r requirements.txt
  apt-get update
  apt-get install -y coreutils rsync

  if [ -d "/testbed" ]; then  # since public docker images use /testbed as project directory
    MATTERHORN_TARGET_PROJECT=/$(python -m scripts.get_issue_path get_repo_name)
    mv "/testbed" "$MATTERHORN_TARGET_PROJECT"
    echo "Using public docker image. Renamed /testbed to $MATTERHORN_TARGET_PROJECT"
  fi

else
  echo "TeamCity environment, activating Conda env..."
  apt update
  apt install -y jq sudo gcc coreutils rsync
  source activate base
  conda activate base
  pip install -r requirements.txt
fi
echo "##teamcity[blockClosed name='Activating venv']"

cd "%swe.bench.matterhorn.path%"

if [ "$AGENT_MODE" = "ElectricJuniorCloud" ]; then

  patches=("a" "b" "c")
  output_values=()

  # for chosen trajectory (final) evaluate one more time to be sure that everything is ok
  echo "##teamcity[blockOpened name='Evaluating cloud prediction for patch_final']"
  cp "${MATTERHORN_SWE_AGENT_OUTPUTS_DIR}/../patch.patch" prediction_patch.patch
  echo "patch looks like this:"
  cat prediction_patch.patch
  timeout 1800 /bin/bash ./scripts/evaluate_predictions.sh 2>&1 | tee evaluation.log
  OUTPUT_VALUE=${PIPESTATUS[0]}
  output_values+=($OUTPUT_VALUE)

  if [ $OUTPUT_VALUE -eq 124 ]; then
    echo "Evaluation timed out for final patch, skipping..."
    echo "##teamcity[blockClosed name='Evaluating cloud prediction for patch_final']"
    bash scripts/reset_hard_repository.sh
  else
    echo "##teamcity[blockClosed name='Evaluating cloud prediction for patch_final']"
    echo "##teamcity[blockOpened name='Publishing to YT for patch_final']"
    python -m scripts.yt_upload save_teamcity_eval_results_extended \
      --result=$OUTPUT_VALUE \
      --yt-destination="//home/matterhorn/users/robot_matterhorn/all/teamcity/evaluation" \
      --swebench-matterhorn-git-revision="$GIT_REVISION" \
      --run-id="%teamcity.run.id%" \
      --logs="evaluation.log" \
      --trajectory_filename="trajectory.json"
    echo "##teamcity[blockClosed name='Publishing to YT for patch_final']"
  fi

  # additionally log cloud trajectories (candidates) evaluation results
  for i in "${patches[@]}"; do
    echo "Sleep for 60 seconds"
    sleep 60

    echo "##teamcity[blockOpened name='Evaluating cloud prediction for patch_cloud_$i']"
    cp "${MATTERHORN_SWE_AGENT_OUTPUTS_DIR}/patch_$i.patch" prediction_patch.patch
    echo "patch looks like this:"
    cat prediction_patch.patch
    timeout 1800 /bin/bash ./scripts/evaluate_predictions.sh "skip_env_prep" 2>&1 | tee evaluation_$i.log
    OUTPUT_VALUE=${PIPESTATUS[0]}
    output_values+=($OUTPUT_VALUE)

    if [ $OUTPUT_VALUE -eq 124 ]; then
        echo "Evaluation timed out for patch $i, skipping..."
        echo "##teamcity[blockClosed name='Evaluating cloud prediction for patch_cloud_$i']"
        bash scripts/reset_hard_repository.sh
        continue
    fi

    echo "##teamcity[blockClosed name='Evaluating cloud prediction for patch_cloud_$i']"

    echo "##teamcity[blockOpened name='Publishing to YT for patch_cloud_$i']"
    python -m scripts.yt_upload save_teamcity_eval_results_extended \
      --result=$OUTPUT_VALUE \
      --yt-destination="//home/matterhorn/users/robot_matterhorn/all/teamcity/evaluation" \
      --swebench-matterhorn-git-revision="$GIT_REVISION" \
      --run-id="%teamcity.run.id%" \
      --logs="evaluation_$i.log" \
      --trajectory_filename="trajectory_$i.json"
    echo "##teamcity[blockClosed name='Publishing to YT for patch_cloud_$i']"
  done
  echo "All output values: ${output_values[@]}"
  OUTPUT_VALUE="${output_values[0]}"
else
  echo "##teamcity[blockOpened name='Evaluating prediction']"
  cp "%patch.path%" prediction_patch.patch
  /bin/bash ./scripts/evaluate_predictions.sh 2>&1 | tee evaluation.log
  OUTPUT_VALUE=${PIPESTATUS[0]}
  echo "##teamcity[blockClosed name='Evaluating prediction']"

  echo "##teamcity[blockOpened name='Publishing to YT']"
  python -m scripts.yt_upload save_teamcity_eval_results_extended \
    --result="$OUTPUT_VALUE" \
    --yt-destination="//home/matterhorn/users/robot_matterhorn/all/teamcity/evaluation" \
    --swebench-matterhorn-git-revision="$GIT_REVISION" \
    --run-id="%teamcity.run.id%" \
    --eval-id="%teamcity.eval.id%"

  if [[ "$AGENT_MODE" = "ElectricJunior" ]]; then
    python -m scripts.yt_upload save_teamcity_eval_results \
      --result="$OUTPUT_VALUE" \
      --yt-destination="//home/matterhorn/users/robot_matterhorn/python/teamcity/evaluation" \
      --swebench-matterhorn-git-revision="$GIT_REVISION" \
      --eval-id="%teamcity.eval.id%"
  fi
  echo "##teamcity[blockClosed name='Publishing to YT']"
fi

# Do not move up, it uses some artifacts from 'YT save' step
echo "##teamcity[blockOpened name='Matterhorn artifacts' description='Publishing Matterhorn artifacts']"
cd "$MATTERHORN_SWE_AGENT_OUTPUTS_DIR"/..
tar -czvf matterhorn.tar.gz .matterhorn
echo "##teamcity[publishArtifacts '$(pwd)/matterhorn.tar.gz']"
echo "##teamcity[blockClosed name='Matterhorn artifacts']"


exit "$OUTPUT_VALUE"