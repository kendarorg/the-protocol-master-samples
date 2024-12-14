#!/bin/sh

if [ -s /protocol-runner.jar ]
then
     echo "Starting with existing protocol-runner"
     java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 -jar /protocol-runner.jar -cfg /settings.json -unattended
else
     echo "Downloading new protocol-runner"
     rm -rf /protocol-runner.jar
     wget --no-verbose https://github.com/kendarorg/the-protocol-master/releases/download/v2.2.0/protocol-runner.jar
     java -jar /protocol-runner.jar -cfg /settings.json -unattended
fi
