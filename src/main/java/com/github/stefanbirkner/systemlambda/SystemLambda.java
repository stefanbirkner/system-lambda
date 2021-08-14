package com.github.stefanbirkner.systemlambda;

import java.io.*;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.security.Permission;
import java.util.*;
import java.util.concurrent.Callable;

import static java.lang.Class.forName;
import static java.lang.System.*;
import static java.nio.charset.Charset.defaultCharset;
import static java.util.Arrays.stream;
import static java.util.Collections.singletonMap;
import static java.util.stream.Collectors.joining;

/**
 * {@code SystemLambda} is a collection of functions for testing code
 * that uses {@code java.lang.System}.
 *
 * <h2>System.exit</h2>
 * <p>Command-line applications terminate by calling {@code System.exit} with
 * some status code. If you test such an application then the JVM that runs the
 * test exits when the application under test calls {@code System.exit}. You can
 * avoid this with the method
 * {@link #catchSystemExit(Statement) catchSystemExit} which also returns the
 * status code of the {@code System.exit} call.
 *
 * <pre>
 * &#064;Test
 * void application_exits_with_status_42(
 * ) throws Exception {
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
 * void execute_code_with_environment_variables(
 * ) throws Exception {
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
 * {@link #restoreSystemProperties(Statement) restoreSystemProperties}
 * guarantees that after executing the test code each System property has the
 * same value like before. Therefore you can modify System properties inside of
 * the test code without having an impact on other tests.
 * <pre>
 * &#064;Test
 * void execute_code_that_manipulates_system_properties(
 * ) throws Exception {
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
 * {@link #tapSystemErr(Statement) tapSystemErr},
 * {@link #tapSystemErrNormalized(Statement) tapSystemErrNormalized},
 * {@link #tapSystemOut(Statement) tapSystemOut},
 * {@link #tapSystemOutNormalized(Statement) tapSystemOutNormalized},
 * {@link #tapSystemErrAndOut(Statement) tapSystemErrAndOut} and
 * {@link #tapSystemErrAndOutNormalized(Statement) tapSystemErrAndOutNormalized}
 * allow you
 * to tap the text that is written to {@code System.err}/{@code System.out}. The
 * methods with the suffix {@code Normalized} normalize line breaks to
 * {@code \n} so that you can run tests with the same assertions on different
 * operating systems.
 *
 * <pre>
 * &#064;Test
 * void application_writes_text_to_System_err(
 * ) throws Exception {
 *   String text = tapSystemErr((){@literal ->} {
 *     System.err.print("some text");
 *   });
 *   assertEquals(text, "some text");
 * }
 *
 * &#064;Test
 * void application_writes_mutliple_lines_to_System_err(
 * ) throws Exception {
 *   String text = tapSystemErrNormalized((){@literal ->} {
 *     System.err.println("first line");
 *     System.err.println("second line");
 *   });
 *   assertEquals(text, "first line\nsecond line\n");
 * }
 *
 * &#064;Test
 * void application_writes_text_to_System_out(
 * ) throws Exception {
 *   String text = tapSystemOut((){@literal ->} {
 *     System.out.print("some text");
 *   });
 *   assertEquals(text, "some text");
 * }
 *
 * &#064;Test
 * void application_writes_mutliple_lines_to_System_out(
 * ) throws Exception {
 *   String text = tapSystemOutNormalized((){@literal ->} {
 *     System.out.println("first line");
 *     System.out.println("second line");
 *   });
 *   assertEquals(text, "first line\nsecond line\n");
 * }
 *
 * &#064;Test
 * void application_writes_text_to_System_err_and_out(
 * ) throws Exception {
 *   String text = tapSystemErrAndOut((){@literal ->} {
 *     System.err.print("text from err");
 *     System.out.print("text from out");
 *   });
 *   assertEquals("text from errtext from out", text);
 * }
 *
 * &#064;Test
 * void application_writes_mutliple_lines_to_System_err_and_out(
 * ) throws Exception {
 *   String text = tapSystemErrAndOutNormalized((){@literal ->} {
 *     System.err.println("text from err");
 *     System.out.println("text from out");
 *   });
 *   assertEquals("text from err\ntext from out\n", text);
 * }</pre>
 *
 * <p>You can assert that nothing is written to
 * {@code System.err}/{@code System.out} by wrapping code with the function
 * {@link #assertNothingWrittenToSystemErr(Statement)
 * assertNothingWrittenToSystemErr}/{@link #assertNothingWrittenToSystemOut(Statement)
 * assertNothingWrittenToSystemOut}. E.g. the following tests fail:
 * <pre>
 * &#064;Test
 * void fails_because_something_is_written_to_System_err(
 * ) throws Exception {
 *   assertNothingWrittenToSystemErr((){@literal ->} {
 *     System.err.println("some text");
 *   });
 * }
 *
 * &#064;Test
 * void fails_because_something_is_written_to_System_out(
 * ) throws Exception {
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
 * the function {@link #muteSystemErr(Statement)
 * muteSystemErr}/{@link #muteSystemOut(Statement) muteSystemOut}. E.g. the
 * following tests don't write anything to
 * {@code System.err}/{@code System.out}:
 * <pre>
 * &#064;Test
 * void nothing_is_written_to_System_err(
 * ) throws Exception {
 *   muteSystemErr((){@literal ->} {
 *     System.err.println("some text");
 *   });
 * }
 *
 * &#064;Test
 * void nothing_is_written_to_System_out(
 * ) throws Exception {
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
 * <p>For complete test coverage you may also want to simulate {@code System.in}
 * throwing exceptions when the application reads from it. You can specify such
 * an exception (either {@code RuntimeException} or {@code IOException}) after
 * specifying the text. The exception will be thrown by the next {@code read}
 * after the text has been consumed.
 * <pre>
 * &#064;Test
 * void System_in_throws_IOException(
 * ) throws Exception {
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
 * void System_in_throws_RuntimeException(
 * ) throws Exception {
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
 * {@link #withSecurityManager(SecurityManager, Statement) withSecurityManager}
 * lets you specify the {@code SecurityManager} that is returned by
 * {@code System.getSecurityManger()} while your code under test is executed.
 * <pre>
 * &#064;Test
 * void execute_code_with_specific_SecurityManager(
 * ) throws Exception {
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
public class SystemLambda {

	private static final boolean AUTO_FLUSH = true;
	private static final String DEFAULT_ENCODING = defaultCharset().name();

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
	 * @param statement an arbitrary piece of code.
	 * @throws AssertionError if the statements tries to write to
	 *                        {@code System.err}.
	 * @throws Exception any exception thrown by the statement.
	 * @see #assertNothingWrittenToSystemOut(Statement)
	 * @since 1.0.0
	 */
	public static void assertNothingWrittenToSystemErr(
		Statement statement
	) throws Exception {
		executeWithSystemErrReplacement(
			new DisallowWriteStream(),
			statement
		);
	}

	/**
	 * Executes the statement and fails (throws an {@code AssertionError}) if
	 * the statement tries to write to {@code System.out}.
	 * <p>The following test fails
	 * <pre>
	 * &#064;Test
	 * void fails_because_something_is_written_to_System_out(
	 * ) throws Exception {
	 *   assertNothingWrittenToSystemOut((){@literal ->} {
	 *     System.out.println("some text");
	 *   });
	 * }
	 * </pre>
	 * The test fails with the failure "Tried to write 's' to System.out
	 * although this is not allowed."
	 *
	 * @param statement an arbitrary piece of code.
	 * @throws AssertionError if the statements tries to write to
	 *                        {@code System.out}.
	 * @throws Exception any exception thrown by the statement.
	 * @see #assertNothingWrittenToSystemErr(Statement)
	 * @since 1.0.0
	 */
	public static void assertNothingWrittenToSystemOut(
		Statement statement
	) throws Exception {
		executeWithSystemOutReplacement(
			new DisallowWriteStream(),
			statement
		);
	}

	/**
	 * Executes the statement and returns the status code that is provided to
	 * {@code System.exit(int)} within the statement. Additionally it avoids
	 * that the JVM is shut down because of a call to {@code System.exit(int)}.
	 * <pre>
	 *{@literal @Test}
	 * void application_exits_with_status_42(
	 * ) throws Exception {
	 *   int statusCode = catchSystemExit((){@literal ->} {
	 *     System.exit(42);
	 *   });
	 *   assertEquals(42, statusCode);
	 * }
	 * </pre>
	 * @param statement an arbitrary piece of code.
	 * @return the status code provided to {@code System.exit(int)}.
	 * @throws AssertionError if the statement does not call
	 *                        {@code System.exit(int)}.
	 * @throws Exception any exception thrown by the statement.
	 * @since 1.0.0
	 */
	public static int catchSystemExit(
		Statement statement
	) throws Exception {
		NoExitSecurityManager noExitSecurityManager
			= new NoExitSecurityManager(getSecurityManager());
		try {
			withSecurityManager(noExitSecurityManager, statement);
		} catch (CheckExitCalled ignored) {
		}
		return checkSystemExit(noExitSecurityManager);
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
	 * @param statement an arbitrary piece of code.
	 * @throws Exception any exception thrown by the statement.
	 * @see #muteSystemOut(Statement)
	 * @since 1.0.0
	 */
	public static void muteSystemErr(
		Statement statement
	) throws Exception {
		executeWithSystemErrReplacement(
			new NoopStream(),
			statement
		);
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
	 * @param statement an arbitrary piece of code.
	 * @throws Exception any exception thrown by the statement.
	 * @see #muteSystemErr(Statement)
	 * @since 1.0.0
	 */
	public static void muteSystemOut(
		Statement statement
	) throws Exception {
		executeWithSystemOutReplacement(
			new NoopStream(),
			statement
		);
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
	 * @param statement an arbitrary piece of code.
	 * @throws Exception any exception thrown by the statement.
	 * @since 1.0.0
	 */
	public static void restoreSystemProperties(
		Statement statement
	) throws Exception {
		Properties originalProperties = getProperties();
		setProperties(copyOf(originalProperties));
		try {
			statement.execute();
		} finally {
			setProperties(originalProperties);
		}
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
	 * @param statement an arbitrary piece of code.
	 * @return text that is written to {@code System.err} by the statement.
	 * @throws Exception any exception thrown by the statement.
	 * @see #tapSystemOut(Statement)
	 * @see #tapSystemErrAndOut(Statement)
	 * @see #tapSystemErrAndOutNormalized(Statement)
	 * @since 1.0.0
	 */
	public static String tapSystemErr(
		Statement statement
	) throws Exception {
		TapStream tapStream = new TapStream();
		executeWithSystemErrReplacement(
			tapStream,
			statement
		);
		return tapStream.textThatWasWritten();
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
	 * @param statement an arbitrary piece of code.
	 * @return text that is written to {@code System.err} by the statement.
	 * @throws Exception any exception thrown by the statement.
	 * @see #tapSystemOut(Statement)
	 * @see #tapSystemErrAndOut(Statement)
	 * @see #tapSystemErrAndOutNormalized(Statement)
	 * @since 1.0.0
	 */
	public static String tapSystemErrNormalized(
		Statement statement
	) throws Exception {
		return tapSystemErr(statement)
			.replace(lineSeparator(), "\n");
	}

	/**
	 * Executes the statement and returns the text that was written to
	 * {@code System.err} and {@code System.out} by the statement.
	 * <pre>
	 * &#064;Test
	 * void application_writes_text_to_System_err_and_out(
	 * ) throws Exception {
	 *   String text = tapSystemErrAndOut((){@literal ->} {
	 *     System.err.print("text from err");
	 *     System.out.print("text from out");
	 *   });
	 *   assertEquals("text from errtext from out", text);
	 * }
	 * </pre>
	 *
	 * @param statement an arbitrary piece of code.
	 * @return text that is written to {@code System.err} and {@code System.out}
	 * by the statement.
	 * @throws Exception any exception thrown by the statement.
	 * @see #tapSystemErrAndOutNormalized(Statement)
	 * @see #tapSystemErr(Statement)
	 * @see #tapSystemErrNormalized(Statement)
	 * @see #tapSystemOut(Statement)
	 * @see #tapSystemOutNormalized(Statement)
	 * @since 1.2.0
	 */
	public static String tapSystemErrAndOut(
		Statement statement
	) throws Exception {
		TapStream tapStream = new TapStream();
		executeWithSystemErrReplacement(
			tapStream,
			() -> executeWithSystemOutReplacement(
					tapStream,
					statement
			)
		);
		return tapStream.textThatWasWritten();
	}

	/**
	 * Executes the statement and returns the text that was written to
	 * {@code System.err} and {@code System.out} by the statement. New line
	 * characters are replaced with a single {@code \n}.
	 * <pre>
	 * &#064;Test
	 * void application_writes_mutliple_lines_to_System_err_and_out(
	 * ) throws Exception {
	 *   String text = tapSystemErrAndOutNormalized((){@literal ->} {
	 *     System.err.println("text from err");
	 *     System.out.println("text from out");
	 *   });
	 *   assertEquals("text from err\ntext from out\n", text);
	 * }
	 * </pre>
	 *
	 * @param statement an arbitrary piece of code.
	 * @return text that is written to {@code System.err} and {@code System.out}
	 * by the statement.
	 * @throws Exception any exception thrown by the statement.
	 * @see #tapSystemErrAndOut(Statement)
	 * @see #tapSystemErr(Statement)
	 * @see #tapSystemErrNormalized(Statement)
	 * @see #tapSystemOut(Statement)
	 * @see #tapSystemOutNormalized(Statement)
	 * @since 1.2.0
	 */
	public static String tapSystemErrAndOutNormalized(
		Statement statement
	) throws Exception {
		return tapSystemErrAndOut(statement)
			.replace(lineSeparator(), "\n");
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
	 * @param statement an arbitrary piece of code.
	 * @return text that is written to {@code System.out} by the statement.
	 * @throws Exception any exception thrown by the statement.
	 * @see #tapSystemErr(Statement)
	 * @see #tapSystemErrAndOut(Statement)
	 * @see #tapSystemErrAndOutNormalized(Statement)
	 * @since 1.0.0
	 */
	public static String tapSystemOut(
		Statement statement
	) throws Exception {
		TapStream tapStream = new TapStream();
		executeWithSystemOutReplacement(
			tapStream,
			statement
		);
		return tapStream.textThatWasWritten();
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
	 * @param statement an arbitrary piece of code.
	 * @return text that is written to {@code System.out} by the statement.
	 * @throws Exception any exception thrown by the statement.
	 * @see #tapSystemErr(Statement)
	 * @see #tapSystemErrAndOut(Statement)
	 * @see #tapSystemErrAndOutNormalized(Statement)
	 * @since 1.0.0
	 */
	public static String tapSystemOutNormalized(
		Statement statement
	) throws Exception {
		return tapSystemOut(statement)
			.replace(lineSeparator(), "\n");
	}

	/**
	 * Executes the statement with the specified environment variables. All
	 * changes to environment variables are reverted after the statement has
	 * been executed.
	 * <pre>
	 * &#064;Test
	 * void execute_code_with_environment_variables(
	 * ) throws Exception {
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
	 * @return an {@link WithEnvironmentVariables} instance that can be used to
	 * set more variables and run a statement with the specified environment
	 * variables.
	 * @since 1.0.0
	 * @see WithEnvironmentVariables#and(String, String)
	 * @see WithEnvironmentVariables#execute(Callable)
	 * @see WithEnvironmentVariables#execute(Statement)
	 */
	public static WithEnvironmentVariables withEnvironmentVariable(
		String name,
		String value
	) {
		return new WithEnvironmentVariables(
			singletonMap(name, value)
		);
	}

    /**
     * Executes the statement with the provided security manager.
     * <pre>
     * &#064;Test
     * void execute_code_with_specific_SecurityManager(
	 * ) throws Exception {
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
     * @param statement an arbitrary piece of code.
     * @throws Exception any exception thrown by the statement.
     * @since 1.0.0
     */
    public static void withSecurityManager(
        SecurityManager securityManager,
        Statement statement
    ) throws Exception {
        SecurityManager originalSecurityManager = getSecurityManager();
        setSecurityManager(securityManager);
        try {
            statement.execute();
        } finally {
            setSecurityManager(originalSecurityManager);
        }
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
	 * @return an {@link SystemInStub} instance that is used to execute a
	 * statement with its {@link SystemInStub#execute(Statement) execute}
	 * method. In addition it can be used to specify an exception that is thrown
	 * after the text is read.
	 * @since 1.0.0
	 * @see SystemInStub#execute(Statement)
	 * @see SystemInStub#andExceptionThrownOnInputEnd(IOException)
	 * @see SystemInStub#andExceptionThrownOnInputEnd(RuntimeException)
	 */
    public static SystemInStub withTextFromSystemIn(
    	String... lines
	) {
    	String text = stream(lines)
			.map(line -> line + lineSeparator())
			.collect(joining());
    	return new SystemInStub(text);
	}

	private static Properties copyOf(
		Properties source
	) {
		Properties copy = new Properties();
		copy.putAll(source);
		return copy;
	}

	private static void executeWithSystemErrReplacement(
		OutputStream replacementForErr,
		Statement statement
	) throws Exception {
		PrintStream originalStream = err;
		try {
			setErr(wrap(replacementForErr));
			statement.execute();
		} finally {
			setErr(originalStream);
		}
	}

	private static void executeWithSystemOutReplacement(
		OutputStream replacementForOut,
		Statement statement
	) throws Exception {
		PrintStream originalStream = out;
		try {
			setOut(wrap(replacementForOut));
			statement.execute();
		} finally {
			setOut(originalStream);
		}
	}

	private static PrintStream wrap(
		OutputStream outputStream
	) throws UnsupportedEncodingException {
		return new PrintStream(
			outputStream,
			AUTO_FLUSH,
			DEFAULT_ENCODING
		);
	}

	private static class DisallowWriteStream extends OutputStream {
		@Override
		public void write(
			int b
		) {
			throw new AssertionError(
				"Tried to write '"
					+ (char) b
					+ "' although this is not allowed."
			);
		}
	}

	private static class NoopStream extends OutputStream {
		@Override
		public void write(
			int b
		) {
		}
	}

	/**
	 * A stub that defines the text provided by {@code System.in}. The methods
	 * {@link #andExceptionThrownOnInputEnd(IOException)} and
	 * {@link #andExceptionThrownOnInputEnd(RuntimeException)} can be used to
	 * simulate a {@code System.in} that throws an exception.
	 * <p>The specified behaviour of {@code System.in} is applied to an
	 * arbitrary piece of code that is provided to {@link #execute(Statement)}.
	 */
	public static class SystemInStub {
		private IOException ioException;
		private RuntimeException runtimeException;
		private final String text;

		private SystemInStub(
			String text
		) {
			this.text = text;
		}

		/**
		 * Sets an exception that is thrown after the text is read.
		 * @param exception the {@code IOException} to be thrown.
		 * @return the {@code SystemInStub} itself.
		 * @throws IllegalStateException if a {@code RuntimeException} was
		 * already set by
		 * {@link #andExceptionThrownOnInputEnd(RuntimeException)}
		 */
		public SystemInStub andExceptionThrownOnInputEnd(
			IOException exception
		) {
			if (runtimeException != null)
				throw new IllegalStateException("You cannot call"
					+ " andExceptionThrownOnInputEnd(IOException) because"
					+ " andExceptionThrownOnInputEnd(RuntimeException) has"
					+ " already been called.");
			this.ioException = exception;
			return this;
		}

		/**
		 * Sets an exception that is thrown after the text is read.
		 * @param exception the {@code RuntimeException} to be thrown.
		 * @return the {@code SystemInStub} itself.
		 * @throws IllegalStateException if an {@code IOException} was already
		 * set by {@link #andExceptionThrownOnInputEnd(IOException)}
		 */
		public SystemInStub andExceptionThrownOnInputEnd(
			RuntimeException exception
		) {
			if (ioException != null)
				throw new IllegalStateException("You cannot call"
					+ " andExceptionThrownOnInputEnd(RuntimeException) because"
					+ " andExceptionThrownOnInputEnd(IOException) has already"
					+ " been called.");
			this.runtimeException = exception;
			return this;
		}

		/**
		 * Executes the statement and lets {@code System.in} provide the
		 * specified text during the execution. After the text was read it
		 * throws and exception when {@code System.in#read} is called and an
		 * exception was specified by
		 * {@link #andExceptionThrownOnInputEnd(IOException)} or
		 * {@link #andExceptionThrownOnInputEnd(RuntimeException)}.
		 * @param statement an arbitrary piece of code.
		 * @throws Exception any exception thrown by the statement.
		 */
		public void execute(
			Statement statement
		) throws Exception {
			InputStream stubStream = new ReplacementInputStream(
				text, ioException, runtimeException
			);
			InputStream originalIn = System.in;
			try {
				setIn(stubStream);
				statement.execute();
			} finally {
				setIn(originalIn);
			}
		}


		private static class ReplacementInputStream extends InputStream {
			private final StringReader reader;
			private final IOException ioException;
			private final RuntimeException runtimeException;

			ReplacementInputStream(
				String text,
				IOException ioException,
				RuntimeException runtimeException
			) {
				this.reader = new StringReader(text);
				this.ioException = ioException;
				this.runtimeException = runtimeException;
			}

			@Override
			public int read(
			) throws IOException {
				int character = reader.read();
				if (character == -1)
					handleEmptyReader();
				return character;
			}

			private void handleEmptyReader(
			) throws IOException {
				if (ioException != null)
					throw ioException;
				else if (runtimeException != null)
					throw runtimeException;
			}

			@Override
			public int read(
				byte[] buffer,
				int offset,
				int len
			) throws IOException {
				if (buffer == null)
					throw new NullPointerException();
				else if (offset < 0 || len < 0 || len > buffer.length - offset)
					throw new IndexOutOfBoundsException();
				else if (len == 0)
					return 0;
				else
					return readNextLine(buffer, offset, len);
			}

			private int readNextLine(
				byte[] buffer,
				int offset,
				int len
			) throws IOException {
				int c = read();
				if (c == -1)
					return -1;
				buffer[offset] = (byte) c;

				int i = 1;
				for (; (i < len) && !isCompleteLineWritten(buffer, i - 1); ++i) {
					byte read = (byte) read();
					if (read == -1)
						break;
					else
						buffer[offset + i] = read;
				}
				return i;
			}

			private boolean isCompleteLineWritten(
				byte[] buffer,
				int indexLastByteWritten
			) {
				byte[] separator = getProperty("line.separator")
					.getBytes(defaultCharset());
				int indexFirstByteOfSeparator = indexLastByteWritten
					- separator.length + 1;
				return indexFirstByteOfSeparator >= 0
					&& contains(buffer, separator, indexFirstByteOfSeparator);
			}

			private boolean contains(
				byte[] array,
				byte[] pattern,
				int indexStart
			) {
				for (int i = 0; i < pattern.length; ++i)
					if (array[indexStart + i] != pattern[i])
						return false;
				return true;
			}
		}
	}

	private static class TapStream extends OutputStream {
		final ByteArrayOutputStream text = new ByteArrayOutputStream();

		@Override
		public void write(
			int b
		) {
			text.write(b);
		}

		String textThatWasWritten() {
			return text.toString();
		}
	}

	/**
	 * A collection of values for environment variables. New values can be
	 * added by {@link #and(String, String)}. The {@code EnvironmentVariables}
	 * object is then used to execute an arbitrary piece of code with these
	 * environment variables being present.
	 */
	public static final class WithEnvironmentVariables {
		private final Map<String, String> variables;

		private WithEnvironmentVariables(
			Map<String, String> variables
		) {
			this.variables = variables;
		}

		/**
		 * Creates a new {@code WithEnvironmentVariables} object that
		 * additionally stores the value for an additional environment variable.
		 * <p>You cannot specify the value of an environment variable twice. An
		 * {@code IllegalArgumentException} when you try.
		 * @param name the name of the environment variable.
		 * @param value the value of the environment variable.
		 * @return a new {@code WithEnvironmentVariables} object.
		 * @throws IllegalArgumentException when a value for the environment
		 * variable {@code name} is already specified.
		 * @see #withEnvironmentVariable(String, String)
		 * @see #execute(Statement)
		 */
    	public WithEnvironmentVariables and(
    		String name,
			String value
		) {
    		validateNotSet(name, value);
			HashMap<String, String> moreVariables = new HashMap<>(variables);
			moreVariables.put(name, value);
			return new WithEnvironmentVariables(moreVariables);
		}

		private void validateNotSet(
			String name,
			String value
		) {
			if (variables.containsKey(name)) {
				String currentValue = variables.get(name);
				throw new IllegalArgumentException(
					"The environment variable '" + name + "' cannot be set to "
						+ format(value) + " because it was already set to "
						+ format(currentValue) + "."
				);
			}
		}

		private String format(
			String text
		) {
    		if (text == null)
    			return "null";
    		else
    			return "'" + text + "'";
		}

		/**
		 * Executes a {@code Callable} with environment variable values
		 * according to what was set before. It exposes the return value of the
		 * {@code Callable}. All changes to environment variables are reverted
		 * after the {@code Callable} has been executed.
		 * <pre>
		 * &#064;Test
		 * void execute_code_with_environment_variables(
		 * ) throws Exception {
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
		 * <p><b>Warning:</b> This method uses reflection for modifying internals of the
		 * environment variables map. It fails if your {@code SecurityManager} forbids
		 * such modifications.
		 * @param <T> the type of {@code callable}'s result
		 * @param callable an arbitrary piece of code.
		 * @return the return value of {@code callable}.
		 * @throws Exception any exception thrown by the callable.
		 * @since 1.1.0
		 * @see #withEnvironmentVariable(String, String)
		 * @see #and(String, String)
		 * @see #execute(Statement)
		 */
		public <T> T execute(
			Callable<T> callable
		) throws Exception {
			Map<String, String> originalVariables = new HashMap<>(getenv());
			try {
				setEnvironmentVariables();
				return callable.call();
			} finally {
				restoreOriginalVariables(originalVariables);
			}
		}

		/**
		 * Executes a statement with environment variable values according to
		 * what was set before. All changes to environment variables are
		 * reverted after the statement has been executed.
		 * <pre>
		 * &#064;Test
		 * void execute_code_with_environment_variables(
		 * ) throws Exception {
		 *   withEnvironmentVariable("first", "first value")
		 *     .and("second", "second value")
		 *     .and("third", null)
		 *     .execute((){@literal ->} {
		 *       assertEquals(
		 *         "first value",
		 *         System.getenv("first")
		 *       );
		 *       assertEquals(
		 *         "second value",
		 *         System.getenv("second")
		 *       );
		 *       assertNull(
		 *         System.getenv("third")
		 *       );
		 *     });
		 * }
		 * </pre>
		 * <p><b>Warning:</b> This method uses reflection for modifying internals of the
		 * environment variables map. It fails if your {@code SecurityManager} forbids
		 * such modifications.
		 * @param statement an arbitrary piece of code.
		 * @throws Exception any exception thrown by the statement.
		 * @since 1.0.0
		 * @see #withEnvironmentVariable(String, String)
		 * @see WithEnvironmentVariables#and(String, String)
		 * @see #execute(Callable)
		 */
    	public void execute(
    		Statement statement
		) throws Exception {
    		Map<String, String> originalVariables = new HashMap<>(getenv());
    		try {
				setEnvironmentVariables();
				statement.execute();
			} finally {
				restoreOriginalVariables(originalVariables);
			}
		}

		private void setEnvironmentVariables() {
			overrideVariables(
				getEditableMapOfVariables()
			);
			overrideVariables(
				getTheCaseInsensitiveEnvironment()
			);
		}

		private void overrideVariables(
			Map<String, String> existingVariables
		) {
			if (existingVariables != null) //theCaseInsensitiveEnvironment may be null
				variables.forEach(
					(name, value) -> set(existingVariables, name, value)
				);
		}

		private void set(
			Map<String, String> variables,
			String name,
			String value
		) {
			if (value == null)
				variables.remove(name);
			else
				variables.put(name, value);
		}

		void restoreOriginalVariables(
			Map<String, String> originalVariables
		) {
			restoreVariables(
				getEditableMapOfVariables(),
				originalVariables
			);
			restoreVariables(
				getTheCaseInsensitiveEnvironment(),
				originalVariables
			);
		}

		void restoreVariables(
			Map<String, String> variables,
			Map<String, String> originalVariables
		) {
    		if (variables != null) { //theCaseInsensitiveEnvironment may be null
				variables.clear();
				variables.putAll(originalVariables);
			}
		}

		private static Map<String, String> getEditableMapOfVariables() {
			Class<?> classOfMap = getenv().getClass();
			try {
				return getFieldValue(classOfMap, getenv(), "m");
			} catch (IllegalAccessException e) {
				throw new RuntimeException("System Rules cannot access the field"
					+ " 'm' of the map System.getenv().", e);
			} catch (NoSuchFieldException e) {
				throw new RuntimeException("System Rules expects System.getenv() to"
					+ " have a field 'm' but it has not.", e);
			}
		}

		/*
		 * The names of environment variables are case-insensitive in Windows.
		 * Therefore it stores the variables in a TreeMap named
		 * theCaseInsensitiveEnvironment.
		 */
		private static Map<String, String> getTheCaseInsensitiveEnvironment() {
			try {
				Class<?> processEnvironment = forName("java.lang.ProcessEnvironment");
				return getFieldValue(
					processEnvironment, null, "theCaseInsensitiveEnvironment");
			} catch (ClassNotFoundException e) {
				throw new RuntimeException("System Rules expects the existence of"
					+ " the class java.lang.ProcessEnvironment but it does not"
					+ " exist.", e);
			} catch (IllegalAccessException e) {
				throw new RuntimeException("System Rules cannot access the static"
					+ " field 'theCaseInsensitiveEnvironment' of the class"
					+ " java.lang.ProcessEnvironment.", e);
			} catch (NoSuchFieldException e) {
				//this field is only available for Windows
				return null;
			}
		}

		private static Map<String, String> getFieldValue(
			Class<?> klass,
			Object object,
			String name
		) throws NoSuchFieldException, IllegalAccessException {
			Field field = klass.getDeclaredField(name);
			field.setAccessible(true);
			return (Map<String, String>) field.get(object);
		}
	}

	private static int checkSystemExit(
		NoExitSecurityManager securityManager
	) {
		if (securityManager.isCheckExitCalled())
			return securityManager.getStatusOfFirstCheckExitCall();
		else
			throw new AssertionError("System.exit has not been called.");
	}

    private static class CheckExitCalled extends SecurityException {
        private static final long serialVersionUID = 159678654L;
    }

    /**
     * A {@code NoExitSecurityManager} throws a {@link CheckExitCalled}
	 * exception whenever {@link #checkExit(int)} is called. All other method
	 * calls are delegated to the original security manager.
     */
    private static class NoExitSecurityManager extends SecurityManager {
        private final SecurityManager originalSecurityManager;
		private Integer statusOfFirstExitCall = null;

		NoExitSecurityManager(
			SecurityManager originalSecurityManager
		) {
			this.originalSecurityManager = originalSecurityManager;
		}

		@Override
		public void checkExit(
			int status
		) {
			if (statusOfFirstExitCall == null)
				statusOfFirstExitCall = status;
			throw new CheckExitCalled();
		}

		boolean isCheckExitCalled() {
			return statusOfFirstExitCall != null;
		}

		int getStatusOfFirstCheckExitCall() {
			if (isCheckExitCalled())
				return statusOfFirstExitCall;
			else
				throw new IllegalStateException(
					"checkExit(int) has not been called.");
		}

        @Override
        public boolean getInCheck() {
            return (originalSecurityManager != null)
                && originalSecurityManager.getInCheck();
        }

        @Override
        public Object getSecurityContext() {
            return (originalSecurityManager == null) ? super.getSecurityContext()
                : originalSecurityManager.getSecurityContext();
        }

        @Override
        public void checkPermission(
        	Permission perm
		) {
            if (originalSecurityManager != null)
                originalSecurityManager.checkPermission(perm);
        }

        @Override
        public void checkPermission(
        	Permission perm,
			Object context
		) {
            if (originalSecurityManager != null)
                originalSecurityManager.checkPermission(perm, context);
        }

        @Override
        public void checkCreateClassLoader() {
            if (originalSecurityManager != null)
                originalSecurityManager.checkCreateClassLoader();
        }

        @Override
        public void checkAccess(
        	Thread t
		) {
            if (originalSecurityManager != null)
                originalSecurityManager.checkAccess(t);
        }

        @Override
        public void checkAccess(
        	ThreadGroup g
		) {
            if (originalSecurityManager != null)
                originalSecurityManager.checkAccess(g);
        }

        @Override
        public void checkExec(
        	String cmd
		) {
            if (originalSecurityManager != null)
                originalSecurityManager.checkExec(cmd);
        }

        @Override
        public void checkLink(
        	String lib
		) {
            if (originalSecurityManager != null)
                originalSecurityManager.checkLink(lib);
        }

        @Override
        public void checkRead(
        	FileDescriptor fd
		) {
            if (originalSecurityManager != null)
                originalSecurityManager.checkRead(fd);
        }

        @Override
        public void checkRead(
        	String file
		) {
            if (originalSecurityManager != null)
                originalSecurityManager.checkRead(file);
        }

        @Override
        public void checkRead(
        	String file,
			Object context
		) {
            if (originalSecurityManager != null)
                originalSecurityManager.checkRead(file, context);
        }

        @Override
        public void checkWrite(
        	FileDescriptor fd
		) {
            if (originalSecurityManager != null)
                originalSecurityManager.checkWrite(fd);
        }

        @Override
        public void checkWrite(
        	String file
		) {
            if (originalSecurityManager != null)
                originalSecurityManager.checkWrite(file);
        }

        @Override
        public void checkDelete(
        	String file
		) {
            if (originalSecurityManager != null)
                originalSecurityManager.checkDelete(file);
        }

        @Override
        public void checkConnect(
        	String host,
			int port
		) {
            if (originalSecurityManager != null)
                originalSecurityManager.checkConnect(host, port);
        }

        @Override
        public void checkConnect(
        	String host,
			int port,
			Object context
		) {
            if (originalSecurityManager != null)
                originalSecurityManager.checkConnect(host, port, context);
        }

        @Override
        public void checkListen(
        	int port
		) {
            if (originalSecurityManager != null)
                originalSecurityManager.checkListen(port);
        }

        @Override
        public void checkAccept(
        	String host,
			int port
		) {
            if (originalSecurityManager != null)
                originalSecurityManager.checkAccept(host, port);
        }

        @Override
        public void checkMulticast(
        	InetAddress maddr
		) {
            if (originalSecurityManager != null)
                originalSecurityManager.checkMulticast(maddr);
        }

        @Override
        public void checkMulticast(
        	InetAddress maddr,
			byte ttl
		) {
            if (originalSecurityManager != null)
                originalSecurityManager.checkMulticast(maddr, ttl);
        }

        @Override
        public void checkPropertiesAccess() {
            if (originalSecurityManager != null)
                originalSecurityManager.checkPropertiesAccess();
        }

        @Override
        public void checkPropertyAccess(
        	String key
		) {
            if (originalSecurityManager != null)
                originalSecurityManager.checkPropertyAccess(key);
        }

        @Override
        public boolean checkTopLevelWindow(
        	Object window
		) {
            return (originalSecurityManager == null) ? super.checkTopLevelWindow(window)
                : originalSecurityManager.checkTopLevelWindow(window);
        }

        @Override
        public void checkPrintJobAccess() {
            if (originalSecurityManager != null)
                originalSecurityManager.checkPrintJobAccess();
        }

        @Override
        public void checkSystemClipboardAccess() {
            if (originalSecurityManager != null)
                originalSecurityManager.checkSystemClipboardAccess();
        }

        @Override
        public void checkAwtEventQueueAccess() {
            if (originalSecurityManager != null)
                originalSecurityManager.checkAwtEventQueueAccess();
        }

        @Override
        public void checkPackageAccess(
        	String pkg
		) {
            if (originalSecurityManager != null)
                originalSecurityManager.checkPackageAccess(pkg);
        }

        @Override
        public void checkPackageDefinition(
        	String pkg
		) {
            if (originalSecurityManager != null)
                originalSecurityManager.checkPackageDefinition(pkg);
        }

        @Override
        public void checkSetFactory() {
            if (originalSecurityManager != null)
                originalSecurityManager.checkSetFactory();
        }

        @Override
        public void checkMemberAccess(
        	Class<?> clazz,
			int which
		) {
            if (originalSecurityManager != null)
                originalSecurityManager.checkMemberAccess(clazz, which);
        }

        @Override
        public void checkSecurityAccess(
        	String target
		) {
            if (originalSecurityManager != null)
                originalSecurityManager.checkSecurityAccess(target);
        }

        @Override
        public ThreadGroup getThreadGroup() {
            return (originalSecurityManager == null) ? super.getThreadGroup()
                : originalSecurityManager.getThreadGroup();
        }
    }
}
