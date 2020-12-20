package com.github.stefanbirkner.systemlambda;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.github.stefanbirkner.systemlambda.SystemLambda.tapSystemOut;
import static java.lang.System.*;
import static org.assertj.core.api.Assertions.assertThat;

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
	class System_out_is_same_as_before
		extends RestoreSystemOutChecks
	{
		System_out_is_same_as_before() {
			super(SystemLambda::tapSystemOut);
		}
	}
}
