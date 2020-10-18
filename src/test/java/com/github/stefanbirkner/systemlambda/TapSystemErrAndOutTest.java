package com.github.stefanbirkner.systemlambda;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.github.stefanbirkner.systemlambda.SystemLambda.tapSystemErr;
import static com.github.stefanbirkner.systemlambda.SystemLambda.tapSystemErrAndOut;
import static java.lang.System.err;
import static java.lang.System.out;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayNameGeneration(ReplaceUnderscores.class)
class TapSystemErrAndOutTest {

	@Test
	void taps_text_that_is_written_to_System_err_and_out_by_statement(
	) throws Exception {
		String textWrittenToSystemErrAndOut = tapSystemErrAndOut(
			() -> {
				err.print("word1 ");
				out.print("word2 ");
				err.print("word3 ");
				out.print("word4 ");
			}
		);

		assertThat(textWrittenToSystemErrAndOut)
			.isEqualTo("word1 word2 word3 word4 ");
	}

	@Test
	void tapped_text_is_empty_when_statement_does_not_write_to_System_err_nor_out(
	) throws Exception {
		String textWrittenToSystemErrAndOut = tapSystemErrAndOut(
			() -> {}
		);

		assertThat(textWrittenToSystemErrAndOut)
			.isEqualTo("");
	}

	@Nested
	class System_err_is_same_as_before
			extends RestoreSystemErrChecks
	{
		System_err_is_same_as_before() {
			super(SystemLambda::tapSystemErrAndOut);
		}
	}

	@Nested
	class System_out_is_same_as_before
			extends RestoreSystemOutChecks
	{
		System_out_is_same_as_before() {
			super(SystemLambda::tapSystemErrAndOut);
		}
	}
}
