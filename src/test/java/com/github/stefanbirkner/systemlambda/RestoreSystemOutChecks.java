package com.github.stefanbirkner.systemlambda;

import org.junit.jupiter.api.Test;

import java.io.PrintStream;

import static com.github.stefanbirkner.fishbowl.Fishbowl.ignoreException;
import static org.assertj.core.api.Assertions.assertThat;

class RestoreSystemOutChecks {

	private final MethodUnderTest methodUnderTest;

	RestoreSystemOutChecks(
		MethodUnderTest methodUnderTest
	) {
		this.methodUnderTest = methodUnderTest;
	}

	@Test
	void after_statement_is_executed(
	) throws Exception {
		PrintStream originalOut = System.out;
		methodUnderTest.accept(
			() -> {
			}
		);
		assertThat(System.out).isSameAs(originalOut);
	}

	@Test
	void after_statement_throws_exception() {
		PrintStream originalOut = System.out;
		ignoreException(
			() -> methodUnderTest.accept(
				() -> {
					throw new Exception("some exception");
				}
			)
		);
		assertThat(System.out).isSameAs(originalOut);
	}
}
