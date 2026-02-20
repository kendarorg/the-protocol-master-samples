#!/bin/sh

export SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"

chmod +x $SCRIPT_DIR/python/Tpm/start.sh
chmod +x $SCRIPT_DIR/java/Tpm/start.sh
chmod +x $SCRIPT_DIR/golang/Tpm/start.sh
chmod +x $SCRIPT_DIR/net-core/Tpm/start.sh
chmod +x $SCRIPT_DIR/mitm/Tpm/start.sh


# Optional override:
# FORCE_ARCH=amd64 sh build.sh
# FORCE_ARCH=arm64 sh build.sh

if [ -n "$FORCE_ARCH" ]; then
  ARCH="$FORCE_ARCH"
else
  ARCH="$(uname -m 2>/dev/null || echo unknown)"
fi

case "$ARCH" in
  x86_64|amd64)
    PLATFORM="linux/amd64"
    ;;
  arm64|aarch64)
    PLATFORM="linux/arm64"
    ;;
  *)
    echo "Unknown architecture '$ARCH'. Defaulting to linux/amd64"
    PLATFORM="linux/amd64"
    ;;
esac

echo "Detected architecture: $ARCH"
echo "Using Docker platform: $PLATFORM"


update_env_platform() {
    TARGET_DIR="$1"

    if [ -z "$TARGET_DIR" ]; then
        echo "Usage: update_env_platform <target-directory>"
        return 1
    fi

    ENV_FILE="$TARGET_DIR/.env"
    TMP_FILE="$TARGET_DIR/.env.tmp"

    # Ensure directory exists
    if [ ! -d "$TARGET_DIR" ]; then
        echo "Directory not found: $TARGET_DIR"
        return 1
    fi

    # Remove old values if they exist
    grep -v '^DOCKER_PLATFORM=' "$ENV_FILE" 2>/dev/null | \
    grep -v '^TARGET_ARCH=' > "$TMP_FILE" || true

    # Append updated values
    echo "DOCKER_PLATFORM=$PLATFORM" >> "$TMP_FILE"
    echo "TARGET_ARCH=$PLATFORM" >> "$TMP_FILE"

    mv "$TMP_FILE" "$ENV_FILE"

    echo "Updated $ENV_FILE"
}

update_env_platform "$SCRIPT_DIR/python"
update_env_platform "$SCRIPT_DIR/net-core"
update_env_platform "$SCRIPT_DIR/golang"
update_env_platform "$SCRIPT_DIR/mitm"
update_env_platform "$SCRIPT_DIR/java"