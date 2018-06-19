package com.github.stefanbirkner.systemlambda;

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
 */
public class SystemLambda {
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
}
