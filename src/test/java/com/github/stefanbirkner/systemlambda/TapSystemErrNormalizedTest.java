package com.github.stefanbirkner.systemlambda;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.PrintStream;

import static com.github.stefanbirkner.systemlambda.SystemLambda.tapSystemErrNormalized;
import static java.lang.System.err;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

@DisplayNameGeneration(ReplaceUnderscores.class)
class TapSystemErrNormalizedTest {

	@Test
	void taps_text_that_is_written_to_System_err_by_statement_has_only_slash_n_for_new_line(
	) throws Exception {
		String textWrittenToSystemErr = tapSystemErrNormalized(
			() -> err.println("some text")
		);

		assertThat(textWrittenToSystemErr)
			.isEqualTo("some text\n");
	}

	@Test
	void tapped_text_is_empty_when_statement_does_not_write_to_System_err(
	) throws Exception {
		String textWrittenToSystemErr = tapSystemErrNormalized(
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

			tapSystemErrNormalized(
				() -> err.println("some text")
			);

			assertThat(err).isSameAs(originalErr);
		}

		@Test
		void after_statement_throws_exception() {
			PrintStream originalErr = err;

			catchThrowable(
				() -> tapSystemErrNormalized(
					() -> {
						throw new Exception("some exception");
					}
				)
			);

			assertThat(err).isSameAs(originalErr);
		}
	}
}
