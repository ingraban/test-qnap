#!/usr/bin/env sh
set -e
MVNW_DIR="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
JAVA_HOME="${JAVA_HOME:-}"
exec "${JAVA_HOME:+$JAVA_HOME/bin/}java" -jar "$MVNW_DIR/.mvn/wrapper/maven-wrapper.jar" "$@"