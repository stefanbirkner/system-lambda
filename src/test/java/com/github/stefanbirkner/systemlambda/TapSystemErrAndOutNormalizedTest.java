package com.github.stefanbirkner.systemlambda;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.github.stefanbirkner.systemlambda.SystemLambda.*;
import static java.lang.System.err;
import static java.lang.System.out;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayNameGeneration(ReplaceUnderscores.class)
class TapSystemErrAndOutNormalizedTest {

	@Test
	void taps_text_that_is_written_to_System_err_and_out_by_statement_has_only_slash_n_for_new_line(
	) throws Exception {
		String textWrittenToSystemErr = tapSystemErrAndOutNormalized(
				() -> {
					err.println("line 1");
					out.println("line 2");
					err.println("line 3");
					out.println("line 4");
				}
		);

		assertThat(textWrittenToSystemErr)
				.isEqualTo("line 1\nline 2\nline 3\nline 4\n");
	}

	@Test
	void tapped_text_is_empty_when_statement_does_not_write_to_System_err_nor_out(
	) throws Exception {
		String textWrittenToSystemErr = tapSystemErrAndOutNormalized(
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
			super(SystemLambda::tapSystemErrAndOutNormalized);
		}
	}

	@Nested
	class System_out_is_same_as_before
			extends RestoreSystemOutChecks
	{
		System_out_is_same_as_before() {
			super(SystemLambda::tapSystemErrAndOutNormalized);
		}
	}
}
