package com.github.stefanbirkner.systemlambda;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.github.stefanbirkner.fishbowl.Fishbowl.ignoreException;
import static com.github.stefanbirkner.systemlambda.SystemLambda.muteSystemErr;
import static java.lang.System.*;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayNameGeneration(ReplaceUnderscores.class)
class MuteSystemErrTest {

	@Test
	void no_text_is_written_to_System_err_by_statement(
	) throws Exception {
		ByteArrayOutputStream captureOutputStream = new ByteArrayOutputStream();
		setErr(new PrintStream(captureOutputStream));
		muteSystemErr(
			() -> err.println("some text")
		);
		assertThat(captureOutputStream)
			.hasToString("");
	}

	@Nested
	class System_err_is_same_as_before {
		@Test
		void after_statement_is_executed(
		) throws Exception {
			PrintStream originalErr = err;
			muteSystemErr(
				() -> {
				}
			);
			assertThat(err).isSameAs(originalErr);
		}

		@Test
		void after_statement_throws_exception() {
			PrintStream originalErr = err;
			ignoreException(
				() -> muteSystemErr(
					() -> {
						throw new Exception("some exception");
					}
				)
			);
			assertThat(err).isSameAs(originalErr);
		}
	}
}
