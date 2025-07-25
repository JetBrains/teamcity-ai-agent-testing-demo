echo "##teamcity[blockOpened name='Creating patch']"
set -e

cd /testbed || (echo "There is no /testbed directory" && exit 1)

echo "Checking status... "
git status
echo ""

echo "Staging changes for commit... "
git add -A
echo ""

echo "Baking diff..."
git diff --cached -- . ':(exclude)tox.ini' > %teamcity.build.workingDir%/%instance_id%.patch

echo "##teamcity[blockOpened name='Patch content']"
cat %teamcity.build.workingDir%/%instance_id%.patch
echo "##teamcity[blockClosed name='Patch content']"
echo "##teamcity[blockClosed name='Creating patch']"
