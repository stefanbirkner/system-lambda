package org.junit.contrib.java.lang.system;

import static org.junit.contrib.java.lang.system.internal.PrintStreamHandler.SYSTEM_OUT;

import org.junit.contrib.java.lang.system.internal.LogPrintStream;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * The {@code SystemOutRule} intercepts the writes to
 * {@code System.out}. It is used to make assertions about the text
 * that is written to {@code System.out} or to mute {@code System.out}.
 *
 * <h2>Assertions</h2>
 *
 * <p>{@code SystemOutRule} may be used for verifying the text that is
 * written to {@code System.out}.
 *
 * <pre>
 * public class SystemOutTest {
 *   &#064;Rule
 *   public final SystemOutRule systemOutRule = new SystemOutRule().enableLog();
 *
 *   &#064;Test
 *   public void test() {
 *     System.out.print("some text");
 *     assertEquals("some text", systemOutRule.getLog());
 *   }
 * }
 * </pre>
 *
 * <p>If your code under test writes the correct new line characters to
 * {@code System.out} then the test output is different at different systems.
 * {@link #getLogWithNormalizedLineSeparator()} provides a log that always uses
 * {@code \n} as line separator. This makes it easy to write appropriate
 * assertions that work on all systems.
 *
 * <pre>
 * public class SystemOutTest {
 *   &#064;Rule
 *   public final SystemOutRule systemOutRule = new SystemOutRule().enableLog();
 *
 *   &#064;Test
 *   public void test() {
 *     System.out.print(String.format("some text%n"));
 *     assertEquals("some text\n", systemOutRule.getLogWithNormalizedLineSeparator());
 *   }
 * }
 * </pre>
 *
 * <p>You don't have to enable logging for every test. It can be enabled for
 * specific tests only.
 *
 * <pre>
 * public class SystemOutTest {
 *   &#064;Rule
 *   public final SystemOutRule systemOutRule = new SystemOutRule();
 *
 *   &#064;Test
 *   public void testWithLogging() {
 *     systemOutRule.enableLog()
 *     System.out.print("some text");
 *     assertEquals("some text", systemOutRule.getLog());
 *   }
 *
 *   &#064;Test
 *   public void testWithoutLogging() {
 *     System.out.print("some text");
 *   }
 * }
 * </pre>
 *
 * <p>If you want to verify parts of the output only then you can clear the log
 * during a test.
 *
 * <pre>
 * public class SystemOutTest {
 *   &#064;Rule
 *   public final SystemOutRule systemOutRule = new SystemOutRule().enableLog();
 *
 *   &#064;Test
 *   public void test() {
 *     System.out.print("uninteresting things");
 *     systemOutRule.clearLog()
 *     System.out.print("interesting things");
 *     assertEquals("interesting things", systemOutRule.getLog());
 *   }
 * }
 * </pre>
 */
public class SystemOutRule implements TestRule {
	private LogPrintStream logPrintStream = new LogPrintStream(SYSTEM_OUT);

	/**
	 * Clears the current log.
	 */
	public void clearLog() {
		logPrintStream.clearLog();
	}

	/**
	 * Returns the text that is written to {@code System.out} since
	 * {@link #enableLog()} (respectively {@link #clearLog()} has been called.
	 *
	 * @return the text that is written to {@code System.out} since
	 * {@link #enableLog()} (respectively {@link #clearLog()} has been called.
	 */
	public String getLog() {
		return logPrintStream.getLog();
	}

	/**
	 * Returns the text that is written to {@code System.out} since
	 * {@link #enableLog()} (respectively {@link #clearLog()} has been called.
	 * New line characters are replaced with a single {@code \n}.
	 *
	 * @return the normalized log.
	 */
	public String getLogWithNormalizedLineSeparator() {
		return logPrintStream.getLogWithNormalizedLineSeparator();
	}

	/**
	 * Start logging of everything that is written to {@code System.out}.
	 *
	 * @return the rule itself.
	 */
	public SystemOutRule enableLog() {
		logPrintStream.enableLog();
		return this;
	}

	public Statement apply(Statement base, Description description) {
		return logPrintStream.createStatement(base);
	}
}
