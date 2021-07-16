#!/usr/bin/env bash
#
# Verify that System Lambda can be used with different JDKs.

set -euo pipefail

readonly group=`id -g`
readonly user=`id -u`
readonly test_dir="target/test_in_docker"

build_System_Lambda()
{
  ./scripts/mvn_in_docker.sh -Dgpg.skip clean install
}

replace_line()
{
  local file=$1
  local line_number=$2
  local replacement=$3

  sed \
    -i \
    "${line_number}s/.*/${replacement}/" \
    $file
}

clear_line()
{
  local file=$1
  local line_number=$2

  replace_line $file $line_number ""
}

clear_lines()
{
  local file=$1
  local range=($(echo $2 | tr "-" "\n"))
  local start=${range[0]}
  local end=${range[1]}

  for line_number in $(seq $start $end)
  do
    clear_line "$file" "$line_number"
  done
}

delete_line()
{
  local file=$1
  local line_number=$2

  sed -i "${line_number}d" $file
}

delete_lines()
{
  local file=$1
  local range=($(echo $2 | tr "-" "\n"))
  local start=${range[0]}
  local end=${range[1]}

  for line_number in $(seq $start $end)
  do
    delete_line "$file" "$start"
  done
}

insert_line()
{
  local file=$1
  local line_number=$2
  local line=$3

  sed -i "${line_number}i\\${line}\\" $file
}

# The purpose of the test pom.xml is to run the System Lambda tests against a
# System Lambda that was build with Java 8. The Java 8 System Lambda has to be
# published to the local Maven repository. The test pom.xml has a dependency to
# this artifact and also the original test dependencies of System Lambda.
# I decided to build the test pom.xml by copying and modifying System Lambda's
# pom.xml so that I keep System Lambda's test setup.
create_test_pom()
{
  local java_version=$1
  local test_pom="$test_dir/pom.xml"

  cp pom.xml $test_pom

  # Delete everything after <modelVersion>4.0.0</modelVersion> and before the
  # first dependency except groupId, artifactId and version of System Lambda.
  delete_line $test_pom 5 # Line with <parent>
  delete_lines $test_pom "6-9"
  delete_lines $test_pom "8-34"

  # Wrap groupId, artifactId and version of System Lambda with
  # <dependency>...</dependency>
  insert_line $test_pom 5 "    <dependency>"
  insert_line $test_pom 9 "    </dependency>"

  # Add pom.xml "header"
  insert_line $test_pom  5 "  <groupId>dummy</groupId>"
  insert_line $test_pom  6 "  <artifactId>dummy</artifactId>"
  insert_line $test_pom  7 "  <version>0-SNAPSHOT</version>"
  insert_line $test_pom  8 "  <packaging>jar</packaging>"
  insert_line $test_pom  9 ""
  insert_line $test_pom 10 "  <properties>"
  insert_line $test_pom 11 "    <maven.compiler.source>${java_version}</maven.compiler.source>"
  insert_line $test_pom 12 "    <maven.compiler.target>${java_version}</maven.compiler.target>"
  insert_line $test_pom 13 "    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>"
  insert_line $test_pom 14 "  </properties>"
  insert_line $test_pom 15 ""
  insert_line $test_pom 16 "  <dependencies>"
}

# Some methods of SecurityManager had been removed after Java 8. We need to
# delete code that calls or overrides these methods so that we can compile the
# tests with newer JDKs.
remove_code_from_test_classes_that_does_not_work_with_newer_JDKs()
{
  local dir_with_classes="$test_dir/src/test/java/com/github/stefanbirkner/systemlambda"

  # Some methods of SecurityManager had been removed after Java 8. We need to
  # delete code that calls or overrides these methods so that we can compile
  # the tests with newer JDKs.
  clear_lines "${dir_with_classes}/CatchSystemExitTest.java" "242-330"
  clear_lines "${dir_with_classes}/SecurityManagerMock.java" "158-162"
  clear_lines "${dir_with_classes}/SecurityManagerMock.java" "164-168"
  clear_lines "${dir_with_classes}/SecurityManagerMock.java" "203-210"
  clear_lines "${dir_with_classes}/SecurityManagerMock.java" "224-228"
  clear_lines "${dir_with_classes}/SecurityManagerMock.java" "236-241"
}

copy_test_classes()
{
  mkdir -p "$test_dir/src"
  cp -R src/test "$test_dir/src/test"
}

create_test_project()
{
  local java_version=$1

  copy_test_classes
  remove_code_from_test_classes_that_does_not_work_with_newer_JDKs
  create_test_pom $java_version
}

print_headline()
{
  local java_version=$1
  set +u
  local options="$2"
  set -u

  echo ""
  echo "Test System Lambda with Java $java_version $options"
  echo ""
}

test_with_JDK()
{
  local java_version=$1
  set +u
  local options="$2"
  set -u

  print_headline $java_version $options
  create_test_project $java_version
  docker run \
    --rm \
    -e MAVEN_CONFIG=/var/maven/.m2 \
    -u "$user:$group" \
    -v "$(pwd)/$test_dir":/usr/src/system-lambda \
    -v ~/.m2:/var/maven/.m2 \
    -w /usr/src/system-lambda \
    "maven:3.8.3-openjdk-$java_version-slim" \
    mvn -Dgpg.skip \
    -D"maven.compiler.source=$java_version" \
    -D"maven.compiler.target=$java_version" \
    -Duser.home=/var/maven \
    clean test $options
}

# We test with all LTS releases and the latest non-LTS release
build_System_Lambda
test_with_JDK 11
test_with_JDK 17
test_with_JDK 17 "-DargLine=--add-opens=java.base/java.util=ALL-UNNAMED"
