#!/bin/bash

echo "##teamcity[blockOpened name='The build directory listing']"
find build -type d
echo "##teamcity[blockClosed name='The build directory listing']"
echo "##teamcity[publishArtifacts 'ej/build/idea-sandbox/*/log_runElectricJunior/*=>idea_logs.zip']"