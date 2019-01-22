package org.junit.contrib.java.lang.system;

import static org.junit.contrib.java.lang.system.internal.PrintStreamHandler.SYSTEM_ERR;

import org.junit.contrib.java.lang.system.internal.LogPrintStream;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * The {@code SystemErrRule} intercepts the writes to
 * {@code System.err}. It is used to make assertions about the text
 * that is written to {@code System.err} or to mute {@code System.err}.
 *
 * <h2>Assertions</h2>
 *
 * <p>{@code SystemErrRule} may be used for verifying the text that is
 * written to {@code System.err}.
 *
 * <pre>
 * public class SystemErrTest {
 *   &#064;Rule
 *   public final SystemErrRule systemErrRule = new SystemErrRule().enableLog();
 *
 *   &#064;Test
 *   public void test() {
 *     System.err.print("some text");
 *     assertEquals("some text", systemErrRule.getLog());
 *   }
 * }
 * </pre>
 *
 * <p>If your code under test writes the correct new line characters to
 * {@code System.err} then the test output is different at different systems.
 * {@link #getLogWithNormalizedLineSeparator()} provides a log that always uses
 * {@code \n} as line separator. This makes it easy to write appropriate
 * assertions that work on all systems.
 *
 * <pre>
 * public class SystemErrTest {
 *   &#064;Rule
 *   public final SystemErrRule systemErrRule = new SystemErrRule().enableLog();
 *
 *   &#064;Test
 *   public void test() {
 *     System.err.print(String.format("some text%n"));
 *     assertEquals("some text\n", systemErrRule.getLogWithNormalizedLineSeparator());
 *   }
 * }
 * </pre>
 *
 * <p>You don't have to enable logging for every test. It can be enabled for
 * specific tests only.
 *
 * <pre>
 * public class SystemErrTest {
 *   &#064;Rule
 *   public final SystemErrRule systemErrRule = new SystemErrRule();
 *
 *   &#064;Test
 *   public void testWithLogging() {
 *     systemErrRule.enableLog()
 *     System.err.print("some text");
 *     assertEquals("some text", systemErrRule.getLog());
 *   }
 *
 *   &#064;Test
 *   public void testWithoutLogging() {
 *     System.err.print("some text");
 *   }
 * }
 * </pre>
 *
 * <p>If you want to verify parts of the output only then you can clear the log
 * during a test.
 *
 * <pre>
 * public class SystemErrTest {
 *   &#064;Rule
 *   public final SystemErrRule systemErrRule = new SystemErrRule().enableLog();
 *
 *   &#064;Test
 *   public void test() {
 *     System.err.print("uninteresting things");
 *     systemErrRule.clearLog()
 *     System.err.print("interesting things");
 *     assertEquals("interesting things", systemErrRule.getLog());
 *   }
 * }
 * </pre>
 */
public class SystemErrRule implements TestRule {
	private LogPrintStream logPrintStream = new LogPrintStream(SYSTEM_ERR);

	/**
	 * Clears the current log.
	 */
	public void clearLog() {
		logPrintStream.clearLog();
	}

	/**
	 * Returns the text that is written to {@code System.err} since
	 * {@link #enableLog()} (respectively {@link #clearLog()} has been called.
	 *
	 * @return the text that is written to {@code System.err} since
	 * {@link #enableLog} (respectively {@link #clearLog()} has been called.
	 */
	public String getLog() {
		return logPrintStream.getLog();
	}

	/**
	 * Returns the text that is written to {@code System.err} since
	 * {@link #enableLog()} (respectively {@link #clearLog()} has been called.
	 * New line characters are replaced with a single {@code \n}.
	 *
	 * @return the normalized log.
	 */
	public String getLogWithNormalizedLineSeparator() {
		return logPrintStream.getLogWithNormalizedLineSeparator();
	}

	/**
	 * Start logging of everything that is written to {@code System.err}.
	 *
	 * @return the rule itself.
	 */
	public SystemErrRule enableLog() {
		logPrintStream.enableLog();
		return this;
	}

	public Statement apply(Statement base, Description description) {
		return logPrintStream.createStatement(base);
	}
}
