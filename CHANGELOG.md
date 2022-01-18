# Changelog

All notable changes to this project will be documented in this file.

This project adheres to
[Semantic Versioning](https://semver.org/spec/v2.0.0.html).


## 1.2.1 – 2021-12-28

Replace `assertThrownBy` with `assertThrows` in documentation.

`assertThrows` is the correct method name of JUnit Jupiter's assertion for
exceptions. All examples use JUnit Jupiter for assertions.


## 1.2.0 – 2021-01-17

Add methods for tapping `System.err` and `System.out` simultaneously.

The output of command-line applications is usually the output to the standard
output stream and the error output stream intermixed. The new methods allow
capturing the intermixed output.

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


## 1.1.1 – 2020-10-12

Fix examples for tapping `System.err` and `System.out`.


## 1.1.0 – 2020-08-17

Support Callable for `withEnvironmentVariable`.

This allows to write more precise and readable tests. The assertion can be
outside the `withEnvironmentVariable` statement and `execute` can be called with
a one-line Lambda expression. E.g.

    @Test
    void execute_code_with_environment_variables(
    ) throws Exception {
      String value = withEnvironmentVariable("key", "the value")
        .execute(() -> System.getenv("key"));
      assertEquals("the value", value);
    }


## 1.0.0 – 2020-05-17

Initial release with the methods

- assertNothingWrittenToSystemErr
- assertNothingWrittenToSystemOut
- catchSystemExit
- muteSystemErr
- muteSystemOut
- restoreSystemProperties
- tapSystemErr
- tapSystemErrNormalized
- tapSystemOut
- tapSystemOutNormalized
- withEnvironmentVariable
- withSecurityManager
- withTextFromSystemIn
