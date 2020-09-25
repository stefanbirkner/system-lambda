package com.github.stefanbirkner.systemlambda;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.github.stefanbirkner.systemlambda.SystemLambda.tapSystemErr;
import static java.lang.System.err;
import static org.assertj.core.api.Assertions.assertThat;

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
	class System_err_is_same_as_before
			extends RestoreSystemErrChecks
	{
		System_err_is_same_as_before() {
			super(SystemLambda::tapSystemErr);
		}
	}
}
