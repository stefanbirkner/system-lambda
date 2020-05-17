package com.github.stefanbirkner.systemlambda;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.PrintStream;

import static com.github.stefanbirkner.systemlambda.SystemLambda.tapSystemErr;
import static java.lang.System.err;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

@DisplayNameGeneration(ReplaceUnderscores.class)
class TapSystemErrTest {

	@Test
	void taps_text_that_is_written_to_System_err_by_statement(
	) throws Exception {
		String textWrittenToSystemErr = tapSystemErr(
			() -> err.print("some text")
		);

		assertThat(textWrittenToSystemErr)
			.isEqualTo("some text");
	}

	@Test
	void tapped_text_is_empty_when_statement_does_not_write_to_System_err(
	) throws Exception {
		String textWrittenToSystemErr = tapSystemErr(
			() -> {}
		);

		assertThat(textWrittenToSystemErr)
			.isEqualTo("");
	}

	@Nested
	class System_err_is_same_as_before {
		@Test
		void after_statement_is_executed(
		) throws Exception {
			PrintStream originalErr = err;

			tapSystemErr(
				() -> err.print("some text")
			);

			assertThat(err).isSameAs(originalErr);
		}

		@Test
		void after_statement_throws_exception() {
			PrintStream originalErr = err;

			catchThrowable(
				() -> tapSystemErr(
					() -> {
						throw new Exception("some exception");
					}
				)
			);

			assertThat(err).isSameAs(originalErr);
		}
	}
}
