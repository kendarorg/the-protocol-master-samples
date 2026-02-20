#!/bin/sh

export SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"

export PROTOCOL_RUNNER_SRC="$SCRIPT_DIR/../../the-protocol-master/protocol-runner/target/protocol-runner.jar"

echo $PROTOCOL_RUNNER_SRC

if [ -s $PROTOCOL_RUNNER_SRC ]
then
     echo "Copying protocol-runner"
     cp -f "$PROTOCOL_RUNNER_SRC" "$SCRIPT_DIR/Tpm/protocol-runner.jar"
fi
cd $SCRIPT_DIR
docker-compose up
