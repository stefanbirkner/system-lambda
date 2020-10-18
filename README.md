# System Lambda

![Build Status Linux](https://github.com/stefanbirkner/system-lambda/workflows/Java%20CI%20with%20Maven/badge.svg?branch=master) [![Build Status Windows](https://ci.appveyor.com/api/projects/status/4ck6g0triwhvk9dy?svg=true)](https://ci.appveyor.com/project/stefanbirkner/system-lambda)

System Lambda is a collection of functions for testing code which uses
`java.lang.System`.

System Lambda is published under the
[MIT license](http://opensource.org/licenses/MIT). It requires at least Java 8.

For JUnit 4 there is an alternative to System Lambda. Its name is
[System Rules](http://stefanbirkner.github.io/system-rules/index.html).

## Installation

System Lambda is available from
[Maven Central](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.github.stefanbirkner%22%20AND%20a%3A%22system-lambda%22).

```xml
<dependency>
    <groupId>com.github.stefanbirkner</groupId>
    <artifactId>system-lambda</artifactId>
    <version>1.2.0</version>
</dependency>
```

Please don't forget to add the scope `test` if you're using System Lambda for
tests only.


## Usage

Import System Lambda's functions by adding

```java
import static com.github.stefanbirkner.systemlambda.SystemLambda.*;
```

to your tests.


### System.exit

Command-line applications terminate by calling `System.exit` with some status
code. If you test such an application then the JVM that executes the test exits
when the application under test calls `System.exit`. You can avoid this with
the method `catchSystemExit` which also returns the status code of the
`System.exit` call.

```java
@Test
void application_exits_with_status_42(
) throws Exception {
  int statusCode = catchSystemExit(() -> {
    System.exit(42);
  });
  assertEquals(42, statusCode);
}
```

The method `catchSystemExit` throws an `AssertionError` if the code under test
does not call `System.exit`. Therefore your test fails with the failure message
"System.exit has not been called."


### Environment Variables

The method `withEnvironmentVariable` allows you to set environment variables
within your test code that are removed after your code under test is executed.

```java
@Test
void execute_code_with_environment_variables(
) throws Exception {
  List<String> values = withEnvironmentVariable("first", "first value")
    .and("second", "second value")
    .execute(() -> asList(
      System.getenv("first"),
      System.getenv("second")
    ));
  assertEquals(
    asList("first value", "second value"),
    values
  );
}
```


### System Properties

The method `restoreSystemProperties` guarantees that after executing the test
code each System property has the same value like before. Therefore you can
modify System properties inside of the test code without having an impact on
other tests.

```java
@Test
void execute_code_that_manipulates_system_properties(
) throws Exception {
  restoreSystemProperties(() -> {
    System.setProperty("some.property", "some value");
    //code under test that reads properties (e.g. "some.property") or
    //modifies them.
  });

  //Here the value of "some.property" is the same like before.
  //E.g. it is not set.
}
```


### System.out and System.err

Command-line applications usually write to the console. If you write such
applications you need to test the output of these applications. The methods
`tapSystemErr`, `tapSystemErrNormalized`, `tapSystemOut`,
`tapSystemOutNormalized`, `tapSystemErrAndOut` and
`tapSystemErrAndOutNormalized` allow you to tap the text that is written to
`System.err`/`System.out`. The methods with the suffix `Normalized` normalize
line breaks to `\n` so that you can run tests with the same assertions on
different operating systems.

```java
@Test
void application_writes_text_to_System_err(
) throws Exception {
  String text = tapSystemErr(() -> {
    System.err.print("some text");
  });
  assertEquals("some text", text);
}

@Test
void application_writes_mutliple_lines_to_System_err(
) throws Exception {
  String text = tapSystemErrNormalized(() -> {
    System.err.println("first line");
    System.err.println("second line");
  });
  assertEquals("first line\nsecond line\n", text);
}

@Test
void application_writes_text_to_System_out(
) throws Exception {
  String text = tapSystemOut(() -> {
    System.out.print("some text");
  });
  assertEquals("some text", text);
}

@Test
void application_writes_mutliple_lines_to_System_out(
) throws Exception {
  String text = tapSystemOutNormalized(() -> {
    System.out.println("first line");
    System.out.println("second line");
  });
  assertEquals("first line\nsecond line\n", text);
}

@Test
void application_writes_text_to_System_err_and_out(
) throws Exception {
  String text = tapSystemErrAndOut(() -> {
    System.err.print("text from err");
    System.out.print("text from out");
  });
  assertEquals("text from errtext from out", text);
}

@Test
void application_writes_mutliple_lines_to_System_err_and_out(
) throws Exception {
  String text = tapSystemErrAndOutNormalized(() -> {
    System.err.println("text from err");
    System.out.println("text from out");
  });
  assertEquals("text from err\ntext from out\n", text);
}
```

You can assert that nothing is written to `System.err`/`System.out` by wrapping
code with the function
`assertNothingWrittenToSystemErr`/`assertNothingWrittenToSystemOut`. E.g. the
following tests fail:

```java
@Test
void fails_because_something_is_written_to_System_err(
) throws Exception {
  assertNothingWrittenToSystemErr(() -> {
    System.err.println("some text");
  });
}

@Test
void fails_because_something_is_written_to_System_out(
) throws Exception {
  assertNothingWrittenToSystemOut(() -> {
    System.out.println("some text");
  });
}
```

If the code under test writes text to `System.err`/`System.out` then it is
intermixed with the output of your build tool. Therefore you may want to avoid
that the code under test writes to `System.err`/`System.out`. You can achieve
this with the function `muteSystemErr`/`muteSystemOut`. E.g. the following tests
don't write anything to `System.err`/`System.out`:

```java
@Test
void nothing_is_written_to_System_err(
) throws Exception {
  muteSystemErr(() -> {
    System.err.println("some text");
  });
}

@Test
void nothing_is_written_to_System_out(
) throws Exception {
  muteSystemOut(() -> {
    System.out.println("some text");
  });
}
```

### System.in

Interactive command-line applications read from `System.in`. If you write such
applications you need to provide input to these applications. You can specify
the lines that are available from `System.in` with the method
`withTextFromSystemIn`

```java
@Test
void Scanner_reads_text_from_System_in(
) throws Exception {
  withTextFromSystemIn("first line", "second line")
    .execute(() -> {
      Scanner scanner = new Scanner(System.in);
      scanner.nextLine();
      assertEquals("second line", scanner.nextLine());
    });
}
```

For a complete test coverage you may also want to simulate `System.in` throwing
exceptions when the application reads from it. You can specify such an
exception (either `RuntimeException` or `IOException`) after specifying the
text. The exception will be thrown by the next `read` after the text has been
consumed.

```java
@Test
void System_in_throws_IOException(
) throws Exception {
  withTextFromSystemIn("first line", "second line")
    .andExceptionThrownOnInputEnd(new IOException())
    .execute(() -> {
      Scanner scanner = new Scanner(System.in);
      scanner.nextLine();
      scanner.nextLine();
      assertThrownBy(
        IOException.class,
        () -> scanner.readLine()
      );
  });
}

@Test
void System_in_throws_RuntimeException(
) throws Exception {
  withTextFromSystemIn("first line", "second line")
    .andExceptionThrownOnInputEnd(new RuntimeException())
    .execute(() -> {
      Scanner scanner = new Scanner(System.in);
      scanner.nextLine();
      scanner.nextLine();
      assertThrownBy(
        RuntimeException.class,
        () -> scanner.readLine()
      );
    });
}
```

You can write a test that throws an exception immediately by not providing any
text.

```java
withTextFromSystemIn()
  .andExceptionThrownOnInputEnd(...)
  .execute(() -> {
    Scanner scanner = new Scanner(System.in);
    assertThrownBy(
      ...,
      () -> scanner.readLine()
    );
  });
```

### Security Manager

The function `withSecurityManager` lets you specify the `SecurityManager` that
is returned by `System.getSecurityManger()` while your code under test is
executed.

```java
@Test
void execute_code_with_specific_SecurityManager(
) throws Exception {
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
```

After `withSecurityManager(...)` is executed`System.getSecurityManager()`
returns the original security manager again.


## Contributing

You have three options if you have a feature request, found a bug or
simply have a question about System Lambda.

* [Write an issue.](https://github.com/stefanbirkner/system-lambda/issues/new)
* Create a pull request. (See [Understanding the GitHub Flow](https://guides.github.com/introduction/flow/index.html))
* [Write a mail to mail@stefan-birkner.de](mailto:mail@stefan-birkner.de)


## Development Guide

System Lambda is built with [Maven](http://maven.apache.org/) and must be
compiled under JDK 8. If you want to contribute code then

* Please write a test for your change.
* Ensure that you didn't break the build by running `mvnw clean verify -Dgpg.skip`.
* Fork the repo and create a pull request. (See [Understanding the GitHub Flow](https://guides.github.com/introduction/flow/index.html))

The basic coding style is described in the
[EditorConfig](http://editorconfig.org/) file `.editorconfig`.

System Lambda supports [GitHub Actions](https://help.github.com/en/actions)
(Linux) and [AppVeyor](http://www.appveyor.com/) (Windows) for continuous
integration. Your pull request will be automatically built by both CI
servers.


## Release Guide

* Select a new version according to the
  [Semantic Versioning 2.0.0 Standard](http://semver.org/).
* Set the new version in `pom.xml` and in the `Installation` section of
  this readme.
* Commit the modified `pom.xml` and `README.md`.
* Run `mvnw clean deploy` with JDK 8.
* Add a tag for the release: `git tag system-lambda-X.X.X`
