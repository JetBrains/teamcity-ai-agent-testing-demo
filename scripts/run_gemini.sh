cd /testbed || (echo "There is no /testbed directory" && exit 1)

echo "##teamcity[blockOpened name='Running Gemini CLI']"
echo Command:
echo "cat %teamcity.build.workingDir%/%instance_id%_issue.md | %teamcity.build.workingDir%/gemini.js"

export PATH=$PATH:%teamcity.build.workingDir%/node/bin
cat %teamcity.build.workingDir%/%instance_id%_issue.md | %teamcity.build.workingDir%/gemini.js

echo "##teamcity[blockClosed name='Running Gemini CLI']"
