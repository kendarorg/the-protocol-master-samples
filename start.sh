#!/bin/sh

export SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"

chmod +x $SCRIPT_DIR/python/Tpm/start.sh
chmod +x $SCRIPT_DIR/java/Tpm/start.sh
chmod +x $SCRIPT_DIR/golang/Tpm/start.sh
chmod +x $SCRIPT_DIR/net-core/Tpm/start.sh
chmod +x $SCRIPT_DIR/mitm/Tpm/start.sh
