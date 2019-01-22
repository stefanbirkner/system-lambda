package com.github.stefanbirkner.systemlambda;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.PrintStream;

import static com.github.stefanbirkner.systemlambda.SystemLambda.tapSystemOut;
import static java.lang.System.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

@DisplayNameGeneration(ReplaceUnderscores.class)
class TapSystemOutTest {

	@Test
	void taps_text_that_is_written_to_System_out_by_statement(
	) throws Exception {
		String textWrittenToSystemOut = tapSystemOut(
			() -> out.print("some text")
		);

		assertThat(textWrittenToSystemOut)
			.isEqualTo("some text");
	}

	@Test
	void tapped_text_is_empty_when_statement_does_not_write_to_System_out(
	) throws Exception {
		String textWrittenToSystemOut = tapSystemOut(
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

			tapSystemOut(
				() -> out.print("some text")
			);

			assertThat(out).isSameAs(originalOut);
		}

		@Test
		void after_statement_throws_exception() {
			PrintStream originalOut = out;

			catchThrowable(
				() -> tapSystemOut(
					() -> {
						throw new Exception("some exception");
					}
				)
			);

			assertThat(out).isSameAs(originalOut);
		}
	}
}
