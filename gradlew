#!/bin/sh

APP_HOME=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd -P)

CLASSPATH="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"

if [ ! -r "$CLASSPATH" ]; then
    echo "ERROR: Gradle wrapper JAR not found: $CLASSPATH" >&2
    exit 1
fi

exec java ${JAVA_OPTS:-} ${GRADLE_OPTS:-} \
    -Dorg.gradle.appname=gradlew \
    -classpath "$CLASSPATH" \
    org.gradle.wrapper.GradleWrapperMain "$@"
