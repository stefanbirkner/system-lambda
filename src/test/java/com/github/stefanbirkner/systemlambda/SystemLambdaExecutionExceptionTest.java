package com.github.stefanbirkner.systemlambda;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SystemLambdaExecutionExceptionTest {

	@Test
	void shouldReturnCause() {
		Throwable cause = new Exception("cause");

		Throwable exception = new SystemLambdaExecutionException(cause);

		assertThat(exception.getCause()).isEqualTo(cause);
	}
}
