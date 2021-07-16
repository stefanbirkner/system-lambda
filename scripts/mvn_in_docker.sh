#!/usr/bin/env bash
#
# Executes Maven inside a Docker container with Java 8. The image has to be
# executed with options, phases and goals just like the original mvn command.
# E.g. instead of "mvn verify" you have to execute
#
#     scripts/mvn_in_docker.sh verify
#
# from the project directory. The user's Maven local repository is used by the
# container.

set -euxo pipefail

readonly group=`id -g`
readonly user=`id -u`

mkdir -p ~/.m2
docker run \
  --rm \
  -e MAVEN_CONFIG=/var/maven/.m2 \
  -u "$user:$group" \
  -v "$(pwd)":/usr/src/system-lambda \
  -v ~/.m2:/var/maven/.m2 \
  -w /usr/src/system-lambda \
  maven:3.8.1-openjdk-8-slim \
  mvn -Duser.home=/var/maven "$@"
rm -r '?/' # I don't know why this directory is created.
