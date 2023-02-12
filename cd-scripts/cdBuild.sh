#!/bin/bash -e

source "include-shared-scripts.sh"

SCRIPT_DIR="$(dirname "${BASH_SOURCE[0]}")"
# shellcheck source=exports.sh
source "$SCRIPT_DIR/exports.sh"

if check_is_on_default_branch; then
  ./gradlew clean :vidar-quarkus-request-intercept-jvm:build
  ./gradlew :vidar-quarkus-request-intercept-jvm:release
  ./gradlew :vidar-quarkus-request-intercept-jvm:pushImageToTest
else
  ./gradlew clean build
  ./gradlew :vidar-quarkus-request-intercept-jvm:pushImageToSnapshot
fi