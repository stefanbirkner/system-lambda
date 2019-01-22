# System Lambda

Work in progress. Please wait for the first release.

[![Build Status Linux](https://travis-ci.org/stefanbirkner/system-lambda.svg?branch=master)](https://travis-ci.org/stefanbirkner/system-lambda) [![Build Status Windows](https://ci.appveyor.com/api/projects/status/4ck6g0triwhvk9dy?svg=true)](https://ci.appveyor.com/project/stefanbirkner/system-lambda)

System Lambda is a collection of functions for testing code which uses
`java.lang.System`.

System Lambda is published under the
[MIT license](http://opensource.org/licenses/MIT). It requires at least Java 8.

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

Import System Lambda's functions by adding

    import static com.github.stefanbirkner.systemlambda.SystemLambda.*;

to your tests.


### Security Manager

The function `withSecurityManager` lets you specify which `SecurityManager` is
returned by `System.getSecurityManger()` while your code under test is
executed.

    @Test
    void execute_code_with_specific_SecurityManager() {
      SecurityManager securityManager = new ASecurityManager();
      withSecurityManager(
        securityManager,
        () -> {
          //code under test
          //e.g. the following assertion is met
          assertSame(
            securityManager,
            System.getSecurityManager()
          );
        }
      );
    }

After the statement `withSecurityManager(...)` is executed
`System.getSecurityManager()` will return the original security manager again.


### System Properties

The function `restoreSystemProperties` guarantees that after executing the test
code each System property has the same value like before. Therefore you
can modify System properties inside of the test code without having an impact on
other tests.

    @Test
    void execute_code_that_manipulates_system_properties() {
      restoreSystemProperties(
        () -> {
          System.setProperty("some.property", "some value");
          //code under test that reads properties (e.g. "some.property") or
          //modifies them.
        }
      );
      
      //Here the value of "some.property" is the same like before.
      //E.g. it is not set.
    }

### System.in, System.out and System.err

You can assert that nothing is written to `System.err`/`System.out` by wrapping
code with the function
`assertNothingWrittenToSystemErr`/`assertNothingWrittenToSystemOut`. E.g. the
following tests fail:

    @Test
    void fails_because_something_is_written_to_System_err() {
      assertNothingWrittenToSystemErr(
        () -> {
          System.err.println("some text");
        }
      );
    }

    @Test
    void fails_because_something_is_written_to_System_out() {
      assertNothingWrittenToSystemOut(
        () -> {
          System.out.println("some text");
        }
      );
    }

If the code under test writes text to `System.err`/`System.out` then it is
intermixed with the output of your build tool. Therefore you may want to avoid
that the code under test writes to `System.err`/`System.out`. You can achieve
this with the function `muteSystemErr`/`muteSystemOut`. E.g. the following tests
don't write anything to `System.err`/`System.out`:

    @Test
    void nothing_is_written_to_System_err() {
      muteSystemErr(
        () -> {
          System.err.println("some text");
        }
      );
    }

    @Test
    void nothing_is_written_to_System_out() {
      muteSystemOut(
        () -> {
          System.out.println("some text");
        }
      );
    }


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
* Run `mvnw clean deploy` with JDK 8.
* Add a tag for the release: `git tag system-lambda-X.X.X`
