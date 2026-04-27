set -eu

JUNIE_HOME="%teamcity.build.workingDir%/.junie-cli-home"

export HOME="$JUNIE_HOME"
export PATH="$HOME/.local/bin:$PATH"

: "${JUNIE_API_KEY:?JUNIE_API_KEY is required}"

cd /testbed || (echo "There is no /testbed directory" && exit 1)

echo "##teamcity[blockOpened name='Running Junie']"
echo Command:
echo "junie --task <issue> -p /testbed"
junie --auth="$JUNIE_API_KEY" \
    --task "$(cat %teamcity.build.workingDir%/%instance_id%_issue.md)" \
    -p /testbed
echo "##teamcity[blockClosed name='Running Junie']"
