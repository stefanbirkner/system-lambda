# System Lambda

Work in progress. Please wait for the first release.

[![Build Status Linux](https://travis-ci.org/stefanbirkner/system-lambda.svg?branch=master)](https://travis-ci.org/stefanbirkner/system-lambda) [![Build Status Windows](https://ci.appveyor.com/api/projects/status/4ck6g0triwhvk9dy?svg=true)](https://ci.appveyor.com/project/stefanbirkner/system-lambda)

System Lambda is a collection of functions for testing code which uses
`java.lang.System`.

System Lambda is published under the
[MIT license](http://opensource.org/licenses/MIT).

For JUnit 4 there is an alternative to Systen Lambda. Its name is
[System Rules](http://stefanbirkner.github.io/system-rules/index.html).

## Installation

System Lambda is available from
[Maven Central](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.github.stefanbirkner%22%20AND%20a%3A%22system-lambda%22).

    <dependency>
      <groupId>com.github.stefanbirkner</groupId>
      <artifactId>system-lambda</artifactId>
      <version>not released</version>
    </dependency>

Please don't forget to add the scope `test` if you're using System
Lambda for tests only.


## Usage



## Contributing

You have three options if you have a feature request, found a bug or
simply have a question about System Lambda.

* [Write an issue.](https://github.com/stefanbirkner/system-lambda/issues/new)
* Create a pull request. (See [Understanding the GitHub Flow](https://guides.github.com/introduction/flow/index.html))
* [Write a mail to mail@stefan-birkner.de](mailto:mail@stefan-birkner.de)


## Development Guide

System Lambda is build with [Maven](http://maven.apache.org/). If you
want to contribute code than

* Please write a test for your change.
* Ensure that you didn't break the build by running `mvnw test`.
* Fork the repo and create a pull request. (See [Understanding the GitHub Flow](https://guides.github.com/introduction/flow/index.html))

The basic coding style is described in the
[EditorConfig](http://editorconfig.org/) file `.editorconfig`.

System Lambda supports [Travis CI](https://travis-ci.org/) (Linux) and
[AppVeyor](http://www.appveyor.com/) (Windows) for continuous
integration. Your pull request will be automatically build by both CI
servers.


## Release Guide

* Select a new version according to the
  [Semantic Versioning 2.0.0 Standard](http://semver.org/).
* Set the new version in `pom.xml` and in the `Installation` section of
  this readme.
* Commit the modified `pom.xml` and `README.md`.
* Run `mvnw clean deploy` with JDK 6 or 7.
* Add a tag for the release: `git tag system-lambda-X.X.X`
