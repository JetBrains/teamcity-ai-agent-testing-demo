cd /testbed || (echo "There is no /testbed directory" && exit 1)
echo "##teamcity[blockOpened name='Running Junie']"
echo Command:
echo "%teamcity.build.workingDir%/junie/bin/junie -f %teamcity.build.workingDir%/%instance_id%_issue.md -p /testbed"
%teamcity.build.workingDir%/junie/bin/junie \
    -f %teamcity.build.workingDir%/%instance_id%_issue.md \
    -p /testbed
echo "##teamcity[blockClosed name='Running Junie']"
