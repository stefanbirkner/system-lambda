package uk.org.webcompere.systemstubs;

import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.properties.SystemProperties;
import uk.org.webcompere.systemstubs.resource.Resources;
import uk.org.webcompere.systemstubs.resource.TestResource;
import uk.org.webcompere.systemstubs.security.SecurityManagerStub;
import uk.org.webcompere.systemstubs.security.SystemExit;
import uk.org.webcompere.systemstubs.stream.*;
import uk.org.webcompere.systemstubs.stream.alt.LinesAltStream;
import uk.org.webcompere.systemstubs.stream.output.DisallowWriteStream;
import uk.org.webcompere.systemstubs.stream.output.NoopStream;
import uk.org.webcompere.systemstubs.stream.output.Output;

import java.io.*;
import java.util.concurrent.Callable;

import static java.lang.System.*;
import static java.util.Arrays.stream;
import static java.util.Collections.singletonMap;
import static java.util.stream.Collectors.joining;

/**
 * {@link SystemStubs} is a collection of functions for testing code
 * that uses {@code java.lang.System}.
 *
 * <h2>System.exit</h2>
 * <p>Command-line applications terminate by calling {@code System.exit} with
 * some status code. If you test such an application then the JVM that runs the
 * test exits when the application under test calls {@code System.exit}. You can
 * avoid this with the method
 * {@link #catchSystemExit(ThrowingRunnable) catchSystemExit} which also returns the
 * status code of the {@code System.exit} call.
 *
 * <pre>
 * &#064;Test
 * void application_exits_with_status_42() throws Exception {
 *   int statusCode = catchSystemExit((){@literal ->} {
 *     System.exit(42);
 *   });
 *   assertEquals(42, statusCode);
 * }
 * </pre>
 *
 * The method {@code catchSystemExit} throws an {@code AssertionError} if the
 * code under test does not call {@code System.exit}. Therefore your test fails
 * with the failure message "System.exit has not been called."
 *
 * <h2>Environment Variables</h2>
 *
 * <p>The method
 * {@link #withEnvironmentVariable(String, String) withEnvironmentVariable}
 * allows you to set environment variables within your test code that are
 * removed after your code under test is executed.
 * <pre>
 * &#064;Test
 * void execute_code_with_environment_variables() throws Exception {
 *  {@literal List<String>} values = withEnvironmentVariable("first", "first value")
 *     .and("second", "second value")
 *     .execute((){@literal ->} asList(
 *       System.getenv("first"),
 *       System.getenv("second")
 *     ));
 *   assertEquals(
 *     asList("first value", "second value"),
 *     values
 *   );
 * }</pre>
 *
 * <h2>System Properties</h2>
 *
 * <p>The function
 * {@link #restoreSystemProperties(ThrowingRunnable) restoreSystemProperties}
 * guarantees that after executing the test code each System property has the
 * same value like before. Therefore you can modify System properties inside of
 * the test code without having an impact on other tests.
 * <pre>
 * &#064;Test
 * void execute_code_that_manipulates_system_properties() throws Exception {
 *   restoreSystemProperties((){@literal ->} {
 *     System.setProperty("some.property", "some value");
 *     //code under test that reads properties (e.g. "some.property") or
 *     //modifies them.
 *   });
 * }
 * </pre>
 *
 * <h2>System.out and System.err</h2>
 * <p>Command-line applications usually write to the console. If you write such
 * applications you need to test the output of these applications. The methods
 * {@link #tapSystemErr(ThrowingRunnable) tapSystemErr},
 * {@link #tapSystemErrNormalized(ThrowingRunnable) tapSystemErrNormalized},
 * {@link #tapSystemOut(ThrowingRunnable) tapSystemOut} and
 * {@link #tapSystemOutNormalized(ThrowingRunnable) tapSystemOutNormalized} allow you
 * to tap the text that is written to {@code System.err}/{@code System.out}. The
 * methods with the suffix {@code Normalized} normalize line breaks to
 * {@code \n} so that you can run tests with the same assertions on different
 * operating systems.
 *
 * <pre>
 * &#064;Test
 * void application_writes_text_to_System_err() throws Exception {
 *   String text = tapSystemErr((){@literal ->} {
 *     System.err.print("some text");
 *   });
 *   assertEquals(text, "some text");
 * }
 *
 * &#064;Test
 * void application_writes_mutliple_lines_to_System_err() throws Exception {
 *   String text = tapSystemErrNormalized((){@literal ->} {
 *     System.err.println("first line");
 *     System.err.println("second line");
 *   });
 *   assertEquals(text, "first line\nsecond line\n");
 * }
 *
 * &#064;Test
 * void application_writes_text_to_System_out() throws Exception {
 *   String text = tapSystemOut((){@literal ->} {
 *     System.out.print("some text");
 *   });
 *   assertEquals(text, "some text");
 * }
 *
 * &#064;Test
 * void application_writes_mutliple_lines_to_System_out() throws Exception {
 *   String text = tapSystemOutNormalized((){@literal ->} {
 *     System.out.println("first line");
 *     System.out.println("second line");
 *   });
 *   assertEquals(text, "first line\nsecond line\n");
 * }</pre>
 *
 * <p>You can assert that nothing is written to
 * {@code System.err}/{@code System.out} by wrapping code with the function
 * {@link #assertNothingWrittenToSystemErr(ThrowingRunnable)
 * assertNothingWrittenToSystemErr}/{@link #assertNothingWrittenToSystemOut(ThrowingRunnable)
 * assertNothingWrittenToSystemOut}. E.g. the following tests fail:
 * <pre>
 * &#064;Test
 * void fails_because_something_is_written_to_System_err() throws Exception {
 *   assertNothingWrittenToSystemErr((){@literal ->} {
 *     System.err.println("some text");
 *   });
 * }
 *
 * &#064;Test
 * void fails_because_something_is_written_to_System_out() throws Exception {
 *   assertNothingWrittenToSystemOut((){@literal ->} {
 *     System.out.println("some text");
 *   });
 * }
 * </pre>
 *
 * <p>If the code under test writes text to
 * {@code System.err}/{@code System.out} then it is intermixed with the output
 * of your build tool. Therefore you may want to avoid that the code under test
 * writes to {@code System.err}/{@code System.out}. You can achieve this with
 * the function {@link #muteSystemErr(ThrowingRunnable)
 * muteSystemErr}/{@link #muteSystemOut(ThrowingRunnable) muteSystemOut}. E.g. the
 * following tests don't write anything to
 * {@code System.err}/{@code System.out}:
 * <pre>
 * &#064;Test
 * void nothing_is_written_to_System_err() throws Exception {
 *   muteSystemErr((){@literal ->} {
 *     System.err.println("some text");
 *   });
 * }
 *
 * &#064;Test
 * void nothing_is_written_to_System_out() throws Exception {
 *   muteSystemOut((){@literal ->} {
 *     System.out.println("some text");
 *   });
 * }
 * </pre>
 *
 * <h2>System.in</h2>
 *
 * <p>Interactive command-line applications read from {@code System.in}. If you
 * write such applications you need to provide input to these applications. You
 * can specify the lines that are available from {@code System.in} with the
 * method {@link #withTextFromSystemIn(String...) withTextFromSystemIn}
 * <pre>
 * &#064;Test
 * void Scanner_reads_text_from_System_in() throws Exception {
 *   withTextFromSystemIn("first line", "second line")
 *     .execute((){@literal ->} {
 *       Scanner scanner = new Scanner(System.in);
 *       scanner.nextLine();
 *       assertEquals("first line", scanner.nextLine());
 *     });
 * }
 * </pre>
 *
 * <p>For complete test coverage you may also want to simulate {@code System.in}
 * throwing exceptions when the application reads from it. You can specify such
 * an exception (either {@code RuntimeException} or {@code IOException}) after
 * specifying the text. The exception will be thrown by the next {@code read}
 * after the text has been consumed.
 * <pre>
 * &#064;Test
 * void System_in_throws_IOException() throws Exception {
 *   withTextFromSystemIn("first line", "second line")
 *     .andExceptionThrownOnInputEnd(new IOException())
 *     .execute((){@literal ->} {
 *       Scanner scanner = new Scanner(System.in);
 *       scanner.nextLine();
 *       scanner.nextLine();
 *       assertThrownBy(
 *         IOException.class,
 *         (){@literal ->} scanner.readLine()
 *       );
 *   });
 * }
 *
 * &#064;Test
 * void System_in_throws_RuntimeException() throws Exception {
 *   withTextFromSystemIn("first line", "second line")
 *     .andExceptionThrownOnInputEnd(new RuntimeException())
 *     .execute((){@literal ->} {
 *       Scanner scanner = new Scanner(System.in);
 *       scanner.nextLine();
 *       scanner.nextLine();
 *       assertThrownBy(
 *	        RuntimeException.class,
 *          (){@literal ->} scanner.readLine()
 *       );
 * 	   });
 * }
 * </pre>
 *
 * <p>You can write a test that throws an exception immediately by not providing
 * any text.
 * <pre>
 * withTextFromSystemIn()
 *   .andExceptionThrownOnInputEnd(...)
 *   .execute((){@literal ->} {
 *     Scanner scanner = new Scanner(System.in);
 *     assertThrownBy(
 *       ...,
 *       (){@literal ->} scanner.readLine()
 *     );
 *   });
 * </pre>
 *
 * <h2>Security Manager</h2>
 *
 * <p>The function
 * {@link #withSecurityManager(SecurityManager, ThrowingRunnable) withSecurityManager}
 * lets you specify the {@code SecurityManager} that is returned by
 * {@code System.getSecurityManger()} while your code under test is executed.
 * <pre>
 * &#064;Test
 * void execute_code_with_specific_SecurityManager() throws Exception {
 *   SecurityManager securityManager = new ASecurityManager();
 *   withSecurityManager(
 *     securityManager,
 *     (){@literal ->} {
 *       //code under test
 *       //e.g. the following assertion is met
 *       assertSame(
 *         securityManager,
 *         System.getSecurityManager()
 *       );
 *     }
 *   );
 * }
 * </pre>
 * <p>After {@code withSecurityManager(...)} is executed
 * {@code System.getSecurityManager()} returns the original security manager
 * again.
 */
public class SystemStubs {

	/**
	 * Executes the statement and fails (throws an {@code AssertionError}) if
	 * the statement tries to write to {@code System.err}.
	 * <p>The following test fails
	 * <pre>
	 * &#064;Test
	 * void fails_because_something_is_written_to_System_err(
	 * ) throws Exception {
	 *   assertNothingWrittenToSystemErr((){@literal ->} {
	 *     System.err.println("some text");
	 *   });
	 * }
	 * </pre>
	 * The test fails with the failure "Tried to write 's' to System.err
	 * although this is not allowed."
	 *
	 * @param throwingRunnable an arbitrary piece of code.
	 * @throws AssertionError if the statements tries to write to
	 *                        {@code System.err}.
	 * @throws Exception any exception thrown by the statement.
	 * @see #assertNothingWrittenToSystemOut(ThrowingRunnable)
	 * @since 1.0.0
	 */
	public static void assertNothingWrittenToSystemErr(ThrowingRunnable throwingRunnable) throws Exception {
		new SystemErr(new DisallowWriteStream())
            .execute(throwingRunnable);
	}

	/**
	 * Executes the statement and fails (throws an {@code AssertionError}) if
	 * the statement tries to write to {@code System.out}.
	 * <p>The following test fails
	 * <pre>
	 * &#064;Test
	 * void fails_because_something_is_written_to_System_out() throws Exception {
	 *   assertNothingWrittenToSystemOut((){@literal ->} {
	 *     System.out.println("some text");
	 *   });
	 * }
	 * </pre>
	 * The test fails with the failure "Tried to write 's' to System.out
	 * although this is not allowed."
	 *
	 * @param throwingRunnable an arbitrary piece of code.
	 * @throws AssertionError if the statements tries to write to
	 *                        {@code System.out}.
	 * @throws Exception any exception thrown by the statement.
	 * @see #assertNothingWrittenToSystemErr(ThrowingRunnable)
	 * @since 1.0.0
	 */
	public static void assertNothingWrittenToSystemOut(ThrowingRunnable throwingRunnable) throws Exception {
		new SystemOut(new DisallowWriteStream())
            .execute(throwingRunnable);
	}

    /**
     * As with {@link #assertNothingWrittenToSystemOut(ThrowingRunnable)} and
     * {@link #assertNothingWrittenToSystemErr(ThrowingRunnable)}, this stops the test
     * with an error if anything is written to either stream
     * @param throwingRunnable
     * @param throwingRunnable an arbitrary piece of code.
     * @throws AssertionError if the statements tries to write to
     *                        {@code System.out}.
     * @throws Exception any exception thrown by the statement.
     * @see #assertNothingWrittenToSystemErr(ThrowingRunnable)
     */
    public static void assertNothingWrittenToSystemErrOrOut(ThrowingRunnable throwingRunnable) throws Exception {
        new SystemErrAndOut(new DisallowWriteStream())
            .execute(throwingRunnable);
    }

	/**
	 * Executes the statement and returns the status code that is provided to
	 * {@code System.exit(int)} within the statement. Additionally it avoids
	 * that the JVM is shut down because of a call to {@code System.exit(int)}.
	 * <pre>
	 *{@literal @Test}
	 * void application_exits_with_status_42() throws Exception {
	 *   int statusCode = catchSystemExit((){@literal ->} {
	 *     System.exit(42);
	 *   });
	 *   assertEquals(42, statusCode);
	 * }
	 * </pre>
	 * @param throwingRunnable an arbitrary piece of code.
	 * @return the status code provided to {@code System.exit(int)}.
	 * @throws AssertionError if the statement does not call
	 *                        {@code System.exit(int)}.
	 * @throws Exception any exception thrown by the statement.
	 * @since 1.0.0
	 */
	public static int catchSystemExit(ThrowingRunnable throwingRunnable) throws Exception {
        SystemExit exit = new SystemExit();
        exit.execute(throwingRunnable);
	    return exit.getSecurityManager().checkSystemExit();
	}

	/**
	 * Executes the statement and suppresses the output of the statement to
	 * {@code System.err}. Use this to avoid that the output of your build tool
	 * gets mixed with the output of the code under test.
	 * <pre>
	 * &#064;Test
	 * void nothing_is_written_to_System_err(
	 * ) throws Exception {
	 *   muteSystemErr((){@literal ->} {
	 *       System.err.println("some text");
	 *     }
	 *   );
	 * }
	 * </pre>
	 *
	 * @param throwingRunnable an arbitrary piece of code.
	 * @throws Exception any exception thrown by the statement.
	 * @see #muteSystemOut(ThrowingRunnable)
	 * @since 1.0.0
	 */
	public static void muteSystemErr(ThrowingRunnable throwingRunnable) throws Exception {
	    new SystemErr(new NoopStream())
            .execute(throwingRunnable);
	}

	/**
	 * Executes the statement and suppresses the output of the statement to
	 * {@code System.out}. Use this to avoid that the output of your build tool
	 * gets mixed with the output of the code under test.
	 * <pre>
	 * &#064;Test
	 * void nothing_is_written_to_System_out(
	 * ) throws Exception {
	 *   muteSystemOut((){@literal ->} {
	 *     System.out.println("some text");
	 *   });
	 * }
	 * </pre>
	 *
	 * @param throwingRunnable an arbitrary piece of code.
	 * @throws Exception any exception thrown by the statement.
	 * @see #muteSystemErr(ThrowingRunnable)
	 * @since 1.0.0
	 */
	public static void muteSystemOut(ThrowingRunnable throwingRunnable) throws Exception {
		new SystemOut(new NoopStream())
            .execute(throwingRunnable);
	}

	/**
	 * Executes the statement and restores the system properties after the
	 * statement has been executed. This allows you to set or clear system
	 * properties within the statement without affecting other tests.
	 * <pre>
	 * &#064;Test
	 * void execute_code_that_manipulates_system_properties(
	 * ) throws Exception {
	 *   System.clearProperty("some property");
	 *   System.setProperty("another property", "value before test");
	 *
	 *   restoreSystemProperties((){@literal ->} {
	 *     System.setProperty("some property", "some value");
	 *     assertEquals(
	 *       "some value",
	 *       System.getProperty("some property")
	 *     );
	 *
	 *     System.clearProperty("another property");
	 *     assertNull(
	 *       System.getProperty("another property")
	 *     );
	 *   });
	 *
	 *   //values are restored after test
	 *   assertNull(
	 *     System.getProperty("some property")
	 *   );
	 *   assertEquals(
	 *     "value before test",
	 *     System.getProperty("another property")
	 *   );
	 * }
	 * </pre>
	 * @param throwingRunnable an arbitrary piece of code.
	 * @throws Exception any exception thrown by the statement.
	 * @since 1.0.0
	 */
	public static void restoreSystemProperties(ThrowingRunnable throwingRunnable) throws Exception {
        new SystemProperties()
            .execute(throwingRunnable.asCallable());
	}

	/**
	 * Executes the statement and returns the text that was written to
	 * {@code System.err} by the statement.
	 * <pre>
	 * &#064;Test
	 * void application_writes_text_to_System_err(
	 * ) throws Exception {
	 *   String textWrittenToSystemErr = tapSystemErr((){@literal ->} {
	 *     System.err.print("some text");
	 *   });
	 *   assertEquals("some text", textWrittenToSystemErr);
	 * }
	 * </pre>
	 *
	 * @param throwingRunnable an arbitrary piece of code.
	 * @return text that is written to {@code System.err} by the statement.
	 * @throws Exception any exception thrown by the statement.
	 * @see #tapSystemOut(ThrowingRunnable)
	 * @since 1.0.0
	 */
	public static String tapSystemErr(ThrowingRunnable throwingRunnable) throws Exception {
        SystemErr systemErr = executeInTappedSystemError(throwingRunnable);
        return systemErr.getText();
	}

    private static SystemErr executeInTappedSystemError(ThrowingRunnable throwingRunnable) throws Exception {
        SystemErr systemErr = new SystemErr();
        systemErr.execute(throwingRunnable);
        return systemErr;
    }

    private static SystemOut executeInTappedSystemOut(ThrowingRunnable throwingRunnable) throws Exception {
        SystemOut systemOut = new SystemOut();
        systemOut.execute(throwingRunnable);
        return systemOut;
    }

    /**
	 * Executes the statement and returns the text that was written to
	 * {@code System.err} by the statement. New line characters are replaced
	 * with a single {@code \n}.
	 * <pre>
	 * &#064;Test
	 * void application_writes_mutliple_lines_to_System_err(
	 * ) throws Exception {
	 *   String textWrittenToSystemErr = tapSystemErrNormalized((){@literal ->} {
	 *     System.err.println("some text");
	 *   });
	 *   assertEquals("some text\n", textWrittenToSystemErr);
	 * }
	 * </pre>
	 *
	 * @param throwingRunnable an arbitrary piece of code.
	 * @return text that is written to {@code System.err} by the statement.
	 * @throws Exception any exception thrown by the statement.
	 * @see #tapSystemOut(ThrowingRunnable)
	 * @since 1.0.0
	 */
	public static String tapSystemErrNormalized(ThrowingRunnable throwingRunnable) throws Exception {
		return executeInTappedSystemError(throwingRunnable)
            .getLinesNormalized();
	}

	/**
	 * Executes the statement and returns the text that was written to
	 * {@code System.out} by the statement.
	 * <pre>
	 * &#064;Test
	 * void application_writes_text_to_System_out(
	 * ) throws Exception {
	 *   String textWrittenToSystemOut = tapSystemOut((){@literal ->} {
	 *     System.out.print("some text");
	 *   });
	 *   assertEquals("some text", textWrittenToSystemOut);
	 * }
	 * </pre>
	 *
	 * @param throwingRunnable an arbitrary piece of code.
	 * @return text that is written to {@code System.out} by the statement.
	 * @throws Exception any exception thrown by the statement.
	 * @see #tapSystemErr(ThrowingRunnable)
	 * @since 1.0.0
	 */
	public static String tapSystemOut(ThrowingRunnable throwingRunnable) throws Exception {
        SystemOut systemOut = new SystemOut();
        systemOut.execute(throwingRunnable);
        return systemOut.getText();
	}

	/**
	 * Executes the statement and returns the text that was written to
	 * {@code System.out} by the statement. New line characters are replaced
	 * with a single {@code \n}.
	 * <pre>
	 * &#064;Test
	 * void application_writes_mutliple_lines_to_System_out(
	 * ) throws Exception {
	 *   String textWrittenToSystemOut = tapSystemOutNormalized((){@literal ->} {
	 *     System.out.println("some text");
	 *   });
	 *   assertEquals("some text\n", textWrittenToSystemOut);
	 * }
	 * </pre>
	 *
	 * @param throwingRunnable an arbitrary piece of code.
	 * @return text that is written to {@code System.out} by the statement.
	 * @throws Exception any exception thrown by the statement.
	 * @see #tapSystemErr(ThrowingRunnable)
	 * @since 1.0.0
	 */
	public static String tapSystemOutNormalized(ThrowingRunnable throwingRunnable) throws Exception {
		return executeInTappedSystemOut(throwingRunnable)
            .getLinesNormalized();
	}

    /**
     * Get a tap for System out to use with {@link SystemOut#execute}
     * @return the {@link SystemOut} object to use
     */
	public static SystemOut withTapSystemOut() {
	    return new SystemOut();
    }

    /**
     * Get an object which taps both {@link System#err} and {@link System#out} with a
     * shared {@link Output} for asserting against
     * @return the {@link SystemErrAndOut} to use with {@link SystemErrAndOut#execute}
     */
    public static SystemErrAndOut withTapSystemErrAndOut() {
	    return new SystemErrAndOut();
    }

    /**
     * Get an object which taps both {@link System#err} and {@link System#out} with a
     * shared {@link Output} for asserting against
     * @param output the shared target to direct the logging to
     * @return the {@link SystemErrAndOut} to use with {@link SystemErrAndOut#execute}
     */
    public static SystemErrAndOut withSystemErrAndOut(Output output) {
        return new SystemErrAndOut(output);
    }

    /**
     * Tape both {@link System#err} and {@link System#out} and execute the {@link ThrowingRunnable}
     * then return the composite of any text written via either method. Note {@link #withTapSystemErrAndOut()}
     * allows return of the {@link SystemErrAndOut} object which has more flexible methods for
     * retrieving the text written.
     * @param throwingRunnable the code under test
     * @return the {@link String} of the text written to the output - non null
     * @throws Exception on error in the code under test
     */
    public static String tapSystemErrAndOut(ThrowingRunnable throwingRunnable) throws Exception {
        SystemErrAndOut tappedSystem = withTapSystemErrAndOut();
        tappedSystem.execute(throwingRunnable);
        return tappedSystem.getText();
    }

	/**
	 * Executes the statement with the specified environment variables. All
	 * changes to environment variables are reverted after the statement has
	 * been executed.
	 * <pre>
	 * &#064;Test
	 * void execute_code_with_environment_variables() throws Exception {
	 *   {@literal List<String>} values = withEnvironmentVariable("first", "first value")
	 *     .and("second", "second value")
	 *     .and("third", null)
	 *     .execute((){@literal ->} asList(
	 *         System.getenv("first"),
	 *         System.getenv("second"),
	 *         System.getenv("third")
	 *     ));
	 *   assertEquals(
	 *     asList("first value", "second value", null),
	 *     values
	 *   );
	 * }
	 * </pre>
	 * <p>You cannot specify the value of an an environment variable twice. An
	 * {@code IllegalArgumentException} is thrown when you try.
	 * <p><b>Warning:</b> This method uses reflection for modifying internals of the
	 * environment variables map. It fails if your {@code SecurityManager} forbids
	 * such modifications.
	 * @param name the name of the environment variable.
	 * @param value the value of the environment variable.
	 * @return an {@link EnvironmentVariables} instance that can be used to
	 * set more variables and run a statement with the specified environment
	 * variables.
	 * @since 1.0.0
	 * @see EnvironmentVariables#and(String, String)
	 * @see EnvironmentVariables#execute(Callable)
	 * @see EnvironmentVariables#execute(ThrowingRunnable)
	 */
	public static EnvironmentVariables withEnvironmentVariable(String name, String value) {
		return new EnvironmentVariables(singletonMap(name, value));
	}

    /**
     * Create an {@link EnvironmentVariables} object with multiple up front values
     * @param name name of first environment variable
     * @param value value of first environment variable
     * @param values pairs of name/values (must be even number of entries)
     * @return an {@link EnvironmentVariables} object for further use
     */
    public static EnvironmentVariables withEnvironmentVariables(String name, String value, String ... values) {
        return new EnvironmentVariables(name, value, values);
    }

    /**
     * Create a blank {@link EnvironmentVariables} object
     * @return empty {@link EnvironmentVariables} for adding values to
     */
    public static EnvironmentVariables withEnvironmentVariables() {
        return new EnvironmentVariables();
    }

    /**
     * Execute a callable with the test resources around it.
     * @see Resources#execute(Callable, TestResource...)
     * @param callable callable to call
     * @param resources resources to set up around it
     * @param <T> type of return value
     * @return the result of the callable
     * @throws Exception on error in any of the resources or the callable
     */
    public static <T> T execute(Callable<T> callable, TestResource... resources) throws Exception {
        return Resources.execute(callable, resources);
    }

    /**
     * Execute a runnable with the test resources around it.
     * @see Resources#execute(Callable, TestResource...)
     * @param runnable to call
     * @param resources resources to set up around it
     * @throws Exception on error in any of the resources or the runnable
     */
    public static void execute(ThrowingRunnable runnable, TestResource... resources) throws Exception {
        Resources.execute(runnable.asCallable(), resources);
    }

    /**
     * Executes the statement with the provided security manager.
     * <pre>
     * &#064;Test
     * void execute_code_with_specific_SecurityManager() throws Exception {
     *   SecurityManager securityManager = new ASecurityManager();
     *   withSecurityManager(
     *     securityManager,
     *     (){@literal ->} {
	 *       //code under test
	 *       //e.g. the following assertion is met
     *       assertSame(securityManager, System.getSecurityManager())
     *     }
     *   );
     * }
     * </pre>
     * The specified security manager is only present during the test.
     * @param securityManager the security manager that is used while the
     *                        statement is executed.
     * @param throwingRunnable an arbitrary piece of code.
     * @throws Exception any exception thrown by the statement.
     * @since 1.0.0
     */
    public static void withSecurityManager(SecurityManager securityManager,
        ThrowingRunnable throwingRunnable) throws Exception {
        new SecurityManagerStub<>(securityManager)
            .execute(throwingRunnable.asCallable());
    }

	/**
	 * Executes the statement and lets {@code System.in} provide the specified
	 * text during the execution. In addition several Exceptions can be
	 * specified that are thrown when {@code System.in#read} is called.
	 *
	 * <pre>
	 * &#064;Test
	 * void Scanner_reads_text_from_System_in(
	 * ) throws Exception {
	 *   withTextFromSystemIn("first line", "second line")
	 *     .execute((){@literal ->} {
	 *       Scanner scanner = new Scanner(System.in);
	 *       scanner.nextLine();
	 *       assertEquals("first line", scanner.nextLine());
	 *     });
	 * }
	 * </pre>
	 *
	 * <h3>Throwing Exceptions</h3>
	 * <p>You can also simulate a {@code System.in} that throws an
	 * {@code IOException} or {@code RuntimeException}. Use
	 *
	 * <pre>
	 * &#064;Test
	 * void System_in_throws_IOException(
	 * ) throws Exception {
	 *   withTextFromSystemIn()
	 *     .andExceptionThrownOnInputEnd(new IOException())
	 *     .execute((){@literal ->} {
	 *       assertThrownBy(
	 *         IOException.class,
	 *         (){@literal ->} new Scanner(System.in).readLine())
	 *       );
	 *     )};
	 * }
	 *
	 * &#064;Test
	 * void System_in_throws_RuntimeException(
	 * ) throws Exception {
	 *   withTextFromSystemIn()
	 *    .andExceptionThrownOnInputEnd(new RuntimeException())
	 *    .execute((){@literal ->} {
	 *       assertThrownBy(
	 *         RuntimeException.class,
	 *         (){@literal ->} new Scanner(System.in).readLine())
	 *       );
	 *     )};
	 * }
	 * </pre>
	 * <p>If you provide text as parameters of {@code withTextFromSystemIn(...)}
	 * in addition then the exception is thrown after the text has been read
	 * from {@code System.in}.
	 * @param lines the lines that are available from {@code System.in}.
	 * @return an {@link SystemIn} instance that is used to execute a
	 * statement with its {@link SystemIn#execute(ThrowingRunnable) execute}
	 * method. In addition it can be used to specify an exception that is thrown
	 * after the text is read.
	 * @since 1.0.0
	 * @see SystemIn#execute(ThrowingRunnable)
	 * @see SystemIn#andExceptionThrownOnInputEnd(IOException)
	 * @see SystemIn#andExceptionThrownOnInputEnd(RuntimeException)
	 */
    public static SystemIn withTextFromSystemIn(String... lines) {
    	return new SystemIn(new LinesAltStream(lines));
	}

}
