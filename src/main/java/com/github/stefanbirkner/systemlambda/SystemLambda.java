package com.github.stefanbirkner.systemlambda;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Properties;

import static java.lang.System.*;

/**
 * {@code SystemLambda} is a collection of functions for testing code
 * that uses {@code java.lang.System}.
 *
 * <h2>Security Manager</h2>
 *
 * <p>The function
 * {@link #withSecurityManager(SecurityManager, Statement) withSecurityManager}
 * lets you specify which {@code SecurityManager} is returned by
 * {@code System.getSecurityManger()} while your code under test is executed.
 * <pre>
 * &#064;Test
 * void execute_code_with_specific_SecurityManager() {
 *   SecurityManager securityManager = new ASecurityManager();
 *   withSecurityManager(
 *     securityManager,
 *     () -&gt; {
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
 * <p>After the statement {@code withSecurityManager(...)} is executed
 * {@code System.getSecurityManager()} will return the original security manager
 * again.
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
 * void execute_code_that_manipulates_system_properties() {
 *   restoreSystemProperties(
 *     () -&gt; {
 *       System.setProperty("some.property", "some value");
 *       //code under test that reads properties (e.g. "some.property") or
 *       //modifies them.
 *     }
 *   );
 * }
 * </pre>
 *
 * <h2>System.in, System.out and System.err</h2>
 *
 * <p>You can assert that nothing is written to
 * {@code System.err}/{@code System.out} by wrapping code with the function
 * {@link #assertNothingWrittenToSystemErr(Statement)
 * assertNothingWrittenToSystemErr}/{@link #assertNothingWrittenToSystemOut(Statement)
 * assertNothingWrittenToSystemOut}. E.g. the following tests fail:
 * <pre>
 * &#064;Test
 * void fails_because_something_is_written_to_System_err() {
 *   assertNothingWrittenToSystemErr(
 *     () -&gt; {
 *        System.err.println("some text");
 *     }
 *   );
 * }
 *
 * &#064;Test
 * void fails_because_something_is_written_to_System_out() {
 *   assertNothingWrittenToSystemOut(
 *     () -&gt; {
 *        System.out.println("some text");
 *     }
 *   );
 * }
 * </pre>
 */
public class SystemLambda {

	private static final boolean AUTO_FLUSH = true;
	private static final String DEFAULT_ENCODING = Charset.defaultCharset().name();

	/**
	 * Executes the statement and fails (throws an {@code AssertionError}) if
	 * the statement tries to write to {@code System.err}.
	 * <p>The following test fails
	 * <pre>
	 * &#064;Test
	 * public void fails_because_something_is_written_to_System_err() {
	 *   assertNothingWrittenToSystemErr(
	 *     () -&gt; {
	 *       System.err.println("some text");
	 *     }
	 *   );
	 * }
	 * </pre>
	 * The test fails with the failure "Tried to write 's' to System.err
	 * although this is not allowed."
	 *
	 * @param statement an arbitrary piece of code.
	 * @throws Exception any exception thrown by the statement or an
	 *                   {@code AssertionError} if the statement tries to write
	 *                   to {@code System.err}.
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
	 * public void fails_because_something_is_written_to_System_out() {
	 *   assertNothingWrittenToSystemOut(
	 *     () -&gt; {
	 *       System.out.println("some text");
	 *     }
	 *   );
	 * }
	 * </pre>
	 * The test fails with the failure "Tried to write 's' to System.out
	 * although this is not allowed."
	 *
	 * @param statement an arbitrary piece of code.
	 * @throws Exception any exception thrown by the statement or an
	 *                   {@code AssertionError} if the statement tries to write
	 *                   to {@code System.out}.
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
	 * Executes the statement and restores the system properties after the
	 * statement has been executed. This allows you to set or clear system
	 * properties within the statement without affecting other tests.
	 * <pre>
	 * &#064;Test
	 * public void execute_code_that_manipulates_system_properties(
	 * ) throws Exception {
	 *   System.clearProperty("some property");
	 *   System.setProperty("another property", "value before test");
	 *
	 *   restoreSystemProperties(
	 *     () -&gt; {
	 *       System.setProperty("some property", "some value");
	 *       assertEquals(
	 *         "some value",
	 *         System.getProperty("some property")
	 *       );
	 *
	 *       System.clearProperty("another property");
	 *       assertNull(
	 *         System.getProperty("another property")
	 *       );
	 *     }
	 *   );
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

	private static Properties copyOf(Properties source) {
		Properties copy = new Properties();
		copy.putAll(source);
		return copy;
	}

    /**
     * Executes the statement with the provided security manager.
     * <pre>
     * &#064;Test
     * public void execute_code_with_specific_SecurityManager() {
     *   SecurityManager securityManager = new ASecurityManager();
     *   withSecurityManager(
     *     securityManager,
     *     () -&gt; {
     *       assertSame(securityManager, System.getSecurityManager());
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
		public void write(int b) {
			throw new AssertionError(
				"Tried to write '"
					+ (char) b
					+ "' although this is not allowed."
			);
		}
	}
}
