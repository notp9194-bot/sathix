#!/bin/sh
#
# Gradle start up script for POSIX
#
# Note: This is a thin wrapper. If gradle/wrapper/gradle-wrapper.jar is missing,
# run `gradle wrapper --gradle-version 8.7` once to populate it (CI does this automatically).
#

set -e

APP_HOME="$(cd "$(dirname "$0")" && pwd)"
JAR="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"

if [ ! -f "$JAR" ]; then
  if command -v gradle >/dev/null 2>&1; then
    echo "Wrapper jar missing. Generating with system Gradle..."
    (cd "$APP_HOME" && gradle wrapper --gradle-version 8.7)
  else
    echo "ERROR: $JAR not found and 'gradle' is not on PATH."
    echo "Install Gradle 8.x once (https://gradle.org/install/) then re-run this script."
    exit 1
  fi
fi

JAVA_CMD="${JAVA_HOME:+$JAVA_HOME/bin/}java"
exec "$JAVA_CMD" -classpath "$JAR" org.gradle.wrapper.GradleWrapperMain "$@"
