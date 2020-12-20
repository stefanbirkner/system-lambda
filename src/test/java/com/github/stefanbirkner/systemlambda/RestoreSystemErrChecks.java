package com.github.stefanbirkner.systemlambda;

import org.junit.jupiter.api.Test;

import java.io.PrintStream;

import static com.github.stefanbirkner.fishbowl.Fishbowl.ignoreException;
import static org.assertj.core.api.Assertions.assertThat;

class RestoreSystemErrChecks {

	private final MethodUnderTest methodUnderTest;

	RestoreSystemErrChecks(
		MethodUnderTest methodUnderTest
	) {
		this.methodUnderTest = methodUnderTest;
	}

	@Test
	void after_statement_is_executed(
	) throws Exception {
		PrintStream originalErr = System.err;
		methodUnderTest.accept(
			() -> {
			}
		);
		assertThat(System.err).isSameAs(originalErr);
	}

	@Test
	void after_statement_throws_exception() {
		PrintStream originalErr = System.err;
		ignoreException(
			() -> methodUnderTest.accept(
				() -> {
					throw new Exception("some exception");
				}
			)
		);
		assertThat(System.err).isSameAs(originalErr);
	}
}
