package com.github.stefanbirkner.systemlambda;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static com.github.stefanbirkner.systemlambda.SystemLambda.tapSystemOut;
import static com.github.stefanbirkner.systemlambda.SystemLambda.tapSystemOutToCollection;
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
	void taps_text_that_is_written_to_System_out_by_statement_to_collection(
	) throws Exception {
		Collection<String> linesWrittenToSystemOut = tapSystemOutToCollection(
				() -> { out.println("some text"); out.println("more text"); }
		);

		assertThat(linesWrittenToSystemOut)
				.containsExactly("some text", "more text");
	}

	@Test
	void tapped_text_is_empty_when_statement_does_not_write_to_System_out(
	) throws Exception {
		String textWrittenToSystemOut = tapSystemOut(
			() -> {}
		);

		assertThat(textWrittenToSystemOut)
			.isEmpty();
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
