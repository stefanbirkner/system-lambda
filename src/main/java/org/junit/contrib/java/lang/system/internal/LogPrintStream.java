package org.junit.contrib.java.lang.system.internal;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import org.junit.runners.model.Statement;

import static java.lang.System.getProperty;

public class LogPrintStream {
	private final PrintStreamHandler printStreamHandler;
	private final MuteableLogStream muteableLogStream;

	public LogPrintStream(PrintStreamHandler printStreamHandler) {
		this.printStreamHandler = printStreamHandler;
		this.muteableLogStream = new MuteableLogStream(printStreamHandler.getStream());
	}

	public Statement createStatement(final Statement base) {
		return new Statement() {
			@Override
			public void evaluate() throws Throwable {
				printStreamHandler.createRestoreStatement(new Statement() {
					@Override
					public void evaluate() throws Throwable {
						printStreamHandler.replaceCurrentStreamWithOutputStream(muteableLogStream);
						base.evaluate();
					}
				}).evaluate();
			}
		};
	}

	public void clearLog() {
		muteableLogStream.log.reset();
	}

	public void enableLog() {
		muteableLogStream.logMuted = false;
	}

	public String getLog() {
		/* The MuteableLogStream is created with the default encoding
		 * because it writes to System.out or System.err if not muted and
		 * System.out/System.err uses the default encoding. As a result all
		 * other streams receive input that is encoded with the default
		 * encoding.
		 */
		String encoding = getProperty("file.encoding");
		try {
			return muteableLogStream.log.toString(encoding);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	public String getLogWithNormalizedLineSeparator() {
		String lineSeparator = getProperty("line.separator");
		return getLog().replace(lineSeparator, "\n");
	}

	private static class MuteableLogStream extends OutputStream {
		final OutputStream originalStream;
		final ByteArrayOutputStream log = new ByteArrayOutputStream();
		boolean logMuted = true;

		MuteableLogStream(OutputStream originalStream) {
			this.originalStream = originalStream;
		}

		@Override
		public void write(int b) throws IOException {
			originalStream.write(b);
			if (!logMuted)
				log.write(b);
		}

		@Override
		public void flush() throws IOException {
			originalStream.flush();
			//ByteArrayOutputStreams don't have to be closed
		}

		@Override
		public void close() throws IOException {
			originalStream.close();
			//ByteArrayOutputStreams don't have to be closed
		}
	}
}
