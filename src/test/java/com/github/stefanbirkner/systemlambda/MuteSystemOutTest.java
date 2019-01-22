package com.github.stefanbirkner.systemlambda;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.github.stefanbirkner.fishbowl.Fishbowl.ignoreException;
import static com.github.stefanbirkner.systemlambda.SystemLambda.muteSystemOut;
import static java.lang.System.out;
import static java.lang.System.setOut;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayNameGeneration(ReplaceUnderscores.class)
class MuteSystemOutTest {

	@Test
	void no_text_is_written_to_System_out_by_statement(
	) throws Exception {
		ByteArrayOutputStream captureOutputStream = new ByteArrayOutputStream();
		setOut(new PrintStream(captureOutputStream));
		muteSystemOut(
			() -> out.println("some text")
		);
		assertThat(captureOutputStream)
			.hasToString("");
	}

	@Nested
	class System_out_is_same_as_before {
		@Test
		void after_statement_is_executed(
		) throws Exception {
			PrintStream originalOut = out;
			muteSystemOut(
				() -> {
				}
			);
			assertThat(out).isSameAs(originalOut);
		}

		@Test
		void after_statement_throws_exception(
		) {
			PrintStream originalOut = out;
			ignoreException(
				() -> muteSystemOut(
					() -> {
						throw new Exception("some exception");
					}
				)
			);
			assertThat(out).isSameAs(originalOut);
		}
	}
}
