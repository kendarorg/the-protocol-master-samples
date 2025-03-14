#!/bin/sh

if [ -s /protocol-runner.jar ]
then
     echo "Starting with existing protocol-runner"
     java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 -jar /protocol-runner.jar -cfg /settings.json -unattended
else
     echo "Downloading new protocol-runner"
     rm -rf /protocol-runner.jar
     rm -rf /protocol-dns-plugin-3.0.2.jar
     rm -rf /plugins
     wget --no-verbose https://github.com/kendarorg/the-protocol-master/releases/download/main-release/protocol-runner.jar
     mkdir /plugins
     wget --no-verbose https://github.com/kendarorg/the-protocol-master/releases/download/main-release/protocol-dns-plugin-4.0.1.jar
     cp /protocol-dns-plugin-3.0.2.jar /plugins/
     rm -rf /protocol-dns-plugin-3.0.2.jar
     java -jar /protocol-runner.jar -cfg /settings.json -unattended
fi
