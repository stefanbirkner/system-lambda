package com.github.stefanbirkner.systemlambda;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.PrintStream;

import static com.github.stefanbirkner.systemlambda.SystemLambda.tapSystemOutNormalized;
import static java.lang.System.out;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

@DisplayNameGeneration(ReplaceUnderscores.class)
class TapSystemOutNormalizedTest {

	@Test
	void taps_text_that_is_written_to_System_out_by_statement_has_only_slash_n_for_new_line(
	) throws Exception {
		String textWrittenToSystemOut = tapSystemOutNormalized(
			() -> out.println("some text")
		);

		assertThat(textWrittenToSystemOut)
			.isEqualTo("some text\n");
	}

	@Test
	void tapped_text_is_empty_when_statement_does_not_write_to_System_out(
	) throws Exception {
		String textWrittenToSystemOut = tapSystemOutNormalized(
			() -> {}
		);

		assertThat(textWrittenToSystemOut)
			.isEqualTo("");
	}

	@Nested
	class System_out_is_same_as_before {
		@Test
		void after_statement_is_executed(
		) throws Exception {
			PrintStream originalOut = out;

			tapSystemOutNormalized(
				() -> out.println("some text")
			);

			assertThat(out).isSameAs(originalOut);
		}

		@Test
		void after_statement_throws_exception() {
			PrintStream originalOut = out;

			catchThrowable(
				() -> tapSystemOutNormalized(
					() -> {
						throw new Exception("some exception");
					}
				)
			);

			assertThat(out).isSameAs(originalOut);
		}
	}
}
