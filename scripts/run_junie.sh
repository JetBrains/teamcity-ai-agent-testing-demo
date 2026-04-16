cd /testbed || (echo "There is no /testbed directory" && exit 1)

echo "##teamcity[blockOpened name='Installing Junie']"
curl -fsSL https://junie.jetbrains.com/install.sh | bash
export PATH="$HOME/.local/bin:$PATH"
junie --version
echo "##teamcity[blockClosed name='Installing Junie']"

echo "##teamcity[blockOpened name='Running Junie']"
echo Command:
echo "junie --task <issue> -p /testbed"
junie --auth="$JUNIE_API_KEY" \
    --task "$(cat %teamcity.build.workingDir%/%instance_id%_issue.md)" \
    -p /testbed
echo "##teamcity[blockClosed name='Running Junie']"
