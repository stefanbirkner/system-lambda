package com.github.stefanbirkner.systemlambda;

import static java.lang.System.getSecurityManager;
import static java.lang.System.setSecurityManager;

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
 */
public class SystemLambda {
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
