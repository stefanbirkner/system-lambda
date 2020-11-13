# System Stubs

[![Build status](https://ci.appveyor.com/api/projects/status/r943gjn189rlxts9?svg=true)](https://ci.appveyor.com/project/ashleyfrieze/system-stubs) [![codecov](https://codecov.io/gh/webcompere/system-stubs/branch/master/graph/badge.svg)](https://codecov.io/gh/webcompere/system-stubs)

## Overview
System Stubs is a collection of mechanisms for testing code which uses
`java.lang.System`.

System Stubs is published under the
[MIT license](http://opensource.org/licenses/MIT). It requires at least Java 8.

It is divided into:

- `system-stubs-core` - can be used stand-alone to stub system resources around test code
- [`system-stubs-junit4`](system-stubs-junit4/README.md) - a set of JUnit4 rules that activate the stubs around test code
- [`system-stubs-jupiter`](system-stubs-jupiter/README.md) - a JUnit 5 extension that automatically injects
System Stubbing into JUnit 5 tests.

## History
Based on the excellent work by Stefan Birkner in
[System Rules](https://stefanbirkner.github.io/system-rules/index.html) and [System Lambda](https://github.com/stefanbirkner/system-lambda) this is a remix
of the core techniques, to allow them to be used more
flexibly.

No longer limited to just being a JUnit4 rule (system rules)
and available as a JUnit 5 plugin, this version comes
from solving some problems that were hard to solve with the
originals and were not considered in keeping
with the trajectory of **System Lambda**.

This version comes with the [agreement](https://github.com/stefanbirkner/system-lambda/issues/9) of the original author.
The original author has no responsibility for this version.

### Differences

The main aims of this version:

- Enable environment variables to be set before child test suites
  - allowing environment details to be set in _beforeAll_ or _beforeEach_ hooks
  - as can be necessary for Spring tests
- Support JUnit4 and JUnit5 plugins
- Improve the fluency of the interfaces to reduce boilerplate
- Modularise the code
- Standardise this module's tests around _Mockito_ and _AssertJ_ (removing
home-made alternatives)

## Installation

... tbc - when published to maven central

## Usage with Execute Around

In order to support migration from [System Lambda](https://github.com/stefanbirkner/system-lambda), and
to enable reuse of the original unit tests, the `SystemStubs` facade supports the original
[execute around](https://java-design-patterns.com/patterns/execute-around/) idiom.

It also supports direct usage of each of the different resource stubbers, which can be access
directly as classes, or via the `SystemStubs` facade.

Import System Stubs functions by adding

```java
import static uk.org.webcompere.systemstubs.SystemStubs.*;
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
void application_exits_with_status_42() throws Exception {
  int statusCode = catchSystemExit(() -> {
    System.exit(42);
  });
  assertEquals(42, statusCode);
}
```

The method `catchSystemExit` throws an `AssertionError` if the code under test
does not call `System.exit`. Therefore your test fails with the failure message
"System.exit has not been called."

### `SystemExit` Class

The `SystemExit` class can be used just to ignore a System exit:

```java
new SystemExit()
    .execute(() -> {
        System.exit(0);
    });

// execution continues without error
```

Or an instance can be used to capture the return code, or whether there was an
exit at all.

```java
SystemExit exit = new SystemExit();
exit.execute(() -> {
        System.exit(0);
    });

assertThat(exit.getExitCode()).isEqualTo(0);

// the exit code will be `null` if no System.exit was called
```

This allows the system exit stub to be used in conjunction with other stubs via
the `execute(runnable, stub1, stub2, stub3)` pattern. It also works in conjunction
with the JUnit plugin/extensions.

### Environment Variables

The method `withEnvironmentVariable` allows you to set environment variables
within your test code that are removed after your code under test is executed.

```java
@Test
void execute_code_with_environment_variables() throws Exception {
  List<String> values = withEnvironmentVariable("first", "first value")
    .and("second", "second value")
    .execute(() -> asList(
      System.getenv("first"),
      System.getenv("second")
    ));
  assertEquals(asList("first value", "second value"), values);
}
```
### System Properties

The method `restoreSystemProperties` guarantees that after executing the test
code each System property has the same value as before. Therefore you can
modify System properties inside of the test code without having an impact on
other tests.

```java
@Test
void execute_code_that_manipulates_system_properties() throws Exception {
  restoreSystemProperties(() -> {
    System.setProperty("some.property", "some value");
    //code under test that reads properties (e.g. "some.property") or
    //modifies them.
  });

  //Here the value of "some.property" is the same like before.
  //E.g. it is not set.
}
```

Alternatively, create an instance of the `SystemProperties` object, providing
it with some initial properties, and then call `execute` on it:

```java
SystemProperties someProperties = new SystemProperties(
    "foo", "bar",
    "foz", "boz");
someProperties.execute(() -> {
    // here we expect the properties to have been set

    // we can also call "set" on the "someProperties"
    // to set more system properties - these will be
    // remembered for reuse later with that object

    // any calls to System.setProperty will be undone when
    // "execute" is finished
});

// here the system properties are reverted
```

### System.out and System.err

Command-line applications usually write to the console. If you write such
applications you need to test the output of these applications. The methods
`tapSystemErr`, `tapSystemErrNormalized`, `tapSystemOut` and
`tapSystemOutNormalized` and `tapSystemErrAndOut` allow you to tap the text that is written to
`System.err`/`System.out`. The methods with the suffix `Normalized` normalize
line breaks to `\n` so that you can run tests with the same assertions on
different operating systems.

```java
@Test
void application_writes_text_to_System_err() throws Exception {
  String text = tapSystemErr(() -> {
    System.err.print("some text");
  });
  assertEquals("some text", text);
}

@Test
void application_writes_mutliple_lines_to_System_err() throws Exception {
  String text = tapSystemErrNormalized(() -> {
    System.err.println("first line");
    System.err.println("second line");
  });
  assertEquals("first line\nsecond line\n", text);
}

@Test
void application_writes_text_to_System_out() throws Exception {
  String text = tapSystemOut(() -> {
    System.out.print("some text");
  });
  assertEquals("some text", text);
}

@Test
void application_writes_mutliple_lines_to_System_out() throws Exception {
  String text = tapSystemOutNormalized(() -> {
    System.out.println("first line");
    System.out.println("second line");
  });
  assertEquals("first line\nsecond line\n", text);
}
```

`System.err` and `System.out` can be directed to a single stream:

```java
@Test
void application_writes_text_to_System_err_and_out() throws Exception {
  String text = tapSystemErrAndOut(() -> {
    System.err.print("text from err");
    System.out.print("text from out");
  });
  assertEquals("text from errtext from out", text);
}

// finer control over assertion can be made using the SystemErrAndOut object
@Test
void construct_system_err_and_out_tap() throws Exception {
    SystemErrAndOut stream = withSystemErrAndOut(new TapStream());
    stream.execute(() -> {
        System.err.println("text from err");
        System.out.println("text from out");
    });
    assertThat(stream.getLines())
        .containsExactly("text from err","text from out");
}
```

You can assert that nothing is written to `System.err`/`System.out` by wrapping
code with the function
`assertNothingWrittenToSystemErr`/`assertNothingWrittenToSystemOut`. E.g. the
following tests fail:

```java
@Test
void fails_because_something_is_written_to_System_err() throws Exception {
  assertNothingWrittenToSystemErr(() -> {
    System.err.println("some text");
  });
}

@Test
void fails_because_something_is_written_to_System_out() throws Exception {
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
void nothing_is_written_to_System_err() throws Exception {
  muteSystemErr(() -> {
    System.err.println("some text");
  });
}

@Test
void nothing_is_written_to_System_out() throws Exception {
  muteSystemOut(() -> {
    System.out.println("some text");
  });
}
```

#### Increased Configuration

The methods on the facade provide some useful shortcuts, but there are also
the classes `SystemOut` and `SystemErr` which can be instantiated
with the relevant `Output` types of `NoopStream`, `DisallowWriteStream` or `TapStream`. They
default to using `TapStream`. All of these objects provide functions for getting the text
that arrived at the stream, sliced into lines or whole.

**Note:** The `DisallowWriteStream` cannot capture text as any writes stop the text with an error.
The `NoopStream` does not capture text, so it useful for saving memory/log files during a test.

Example:

```java
SystemOut systemOut = new SystemOut();
systemOut.execute(() -> System.out.print("hello world"));
assertThat(systemOut.getText()).isEqualTo("hello world");
```

The objects can be reused and have a `clear` function to clear captured text between usages.

### System.in

Interactive command-line applications read from `System.in`. If you write such
applications you need to provide input to these applications. You can specify
the lines that are available from `System.in` with the method
`withTextFromSystemIn`

```java
@Test
void Scanner_reads_text_from_System_in() throws Exception {
  withTextFromSystemIn("first line", "second line")
    .execute(() -> {
      Scanner scanner = new Scanner(System.in);
      assertEquals("first line", scanner.nextLine());
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
void System_in_throws_IOException() throws Exception {
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
void System_in_throws_RuntimeException() throws Exception {
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
void execute_code_with_specific_SecurityManager() throws Exception {
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

### Execution

The `execute` function can be called with either `void` function or one which
returns a value. In the latter case, it will return a value. The `execute` function
is available on the stub objects themselves, but also on `SystemStubs`, where
it allows you to pass in both the code to execute, and a collection of the stubbing
objects, each of which is correctly set up and cleaned up around your code:

```java
String myResult = execute(() -> { ... test code ... },
    environmentVariablesStub,
    systemInStub,
    securityManagerStub);
```

The set up is in the order the stubs are declared, and the tear down is in reverse order.

## Contributing

You have two options if you have a feature request, found a bug or
simply have a question.

* [Write an issue.](https://github.com/webcompere/system-stubs/issues/new)
* Create a pull request. (See [Understanding the GitHub Flow](https://guides.github.com/introduction/flow/index.html))

## Development Guide

System Stubs is built with [Maven](http://maven.apache.org/). If you
want to contribute code then

* Please write a test for your change.
* Ensure that you didn't break the build by running `mvnw test`.
* Fork the repo and create a pull request. (See [Understanding the GitHub Flow](https://guides.github.com/introduction/flow/index.html))

The basic coding style is described in the
[EditorConfig](http://editorconfig.org/) file `.editorconfig`.

System Stubs is built with Appveyor: [![Build status](https://ci.appveyor.com/api/projects/status/r943gjn189rlxts9?svg=true)](https://ci.appveyor.com/project/ashleyfrieze/system-stubs)

## Release Guide

* Select a new version according to the
  [Semantic Versioning 2.0.0 Standard](http://semver.org/).
* Set the new version in `pom.xml` and in the `Installation` section of
  this readme.
* Commit the modified `pom.xml` and `README.md`.
* Run `mvnw clean deploy` with JDK 8.
* Add a tag for the release: `git tag system-stubs-X.X.X`
