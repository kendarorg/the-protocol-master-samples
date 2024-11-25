#!/bin/sh

rm -rf /protocol-runner.jar
wget --no-verbose https://github.com/kendarorg/the-protocol-master/releases/download/v2.1.2/protocol-runner.jar
java -jar /protocol-runner.jar -cfg /settings.json -unattended