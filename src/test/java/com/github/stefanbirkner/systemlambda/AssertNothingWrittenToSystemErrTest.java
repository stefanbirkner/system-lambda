package com.github.stefanbirkner.systemlambda;

import java.io.PrintStream;
import java.util.Locale;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.github.stefanbirkner.fishbowl.Fishbowl.ignoreException;
import static com.github.stefanbirkner.systemlambda.SystemLambda.assertNothingWrittenToSystemErr;
import static java.lang.System.err;
import static java.lang.System.getProperty;
import static java.util.Locale.CANADA;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

@DisplayNameGeneration(ReplaceUnderscores.class)
class AssertNothingWrittenToSystemErrTest {
	private static final Locale DUMMY_LOCALE = CANADA;

	@Test
	void execution_of_statement_is_not_intercepted_when_it_does_not_write_to_System_err(
	) throws Exception {
		assertNothingWrittenToSystemErr(
			() -> {}
		);
	}

	@Nested
	class throws_AssertionError_when_statement {
		@Test
		void tries_to_append_a_text_to_System_err(
		) {
			Throwable exception = catchThrowable(
				() -> assertNothingWrittenToSystemErr(
					() -> System.err.append("dummy text")
				)
			);
			assertThat(exception)
				.isInstanceOf(AssertionError.class)
				.hasMessage("Tried to write 'd' although this is not allowed.");
		}

		@Test
		void tries_to_append_a_character_to_System_err(
		) {
			Throwable exception = catchThrowable(
				() -> assertNothingWrittenToSystemErr(
					() -> System.err.append('x')
				)
			);
			assertThat(exception)
				.isInstanceOf(AssertionError.class)
				.hasMessage("Tried to write 'x' although this is not allowed.");
		}

		@Test
		void tries_to_append_a_sub_sequence_of_a_text_to_System_err(
		) {
			Throwable exception = catchThrowable(
				() -> assertNothingWrittenToSystemErr(
					() -> System.err.append("dummy text", 2, 3)
				)
			);
			assertThat(exception)
				.isInstanceOf(AssertionError.class)
				.hasMessage("Tried to write 'm' although this is not allowed.");
		}

		@Test
		void calls_System_err_format_with_a_Locale(
		) {
			Throwable exception = catchThrowable(
				() -> assertNothingWrittenToSystemErr(
					() -> System.err.format(
						DUMMY_LOCALE, "%s, %s", "first dummy", "second dummy")
				)
			);
			assertThat(exception)
				.isInstanceOf(AssertionError.class)
				.hasMessage("Tried to write 'f' although this is not allowed.");
		}

		@Test
		void calls_System_err_format_without_a_Locale(
		) {
			Throwable exception = catchThrowable(
				() -> assertNothingWrittenToSystemErr(
					() -> System.err.format(
						"%s, %s", "first dummy", "second dummy")
				)
			);
			assertThat(exception)
				.isInstanceOf(AssertionError.class)
				.hasMessage("Tried to write 'f' although this is not allowed.");
		}

		@Test
		void calls_System_err_print_with_a_boolean(
		) {
			Throwable exception = catchThrowable(
				() -> assertNothingWrittenToSystemErr(
					() -> System.err.print(true)
				)
			);
			assertThat(exception)
				.isInstanceOf(AssertionError.class)
				.hasMessage("Tried to write 't' although this is not allowed.");
		}

		@Test
		void calls_System_err_print_with_a_char(
		) {
			Throwable exception = catchThrowable(
				() -> assertNothingWrittenToSystemErr(
					() -> System.err.print('a')
				)
			);

			assertThat(exception)
				.isInstanceOf(AssertionError.class)
				.hasMessage("Tried to write 'a' although this is not allowed.");
		}

		@Test
		void calls_System_err_print_with_an_array_of_chars(
		) {
			Throwable exception = catchThrowable(
				() -> assertNothingWrittenToSystemErr(
					() -> System.err.print(new char[]{'d', 'u', 'm', 'm', 'y'})
				)
			);

			assertThat(exception)
				.isInstanceOf(AssertionError.class)
				.hasMessage("Tried to write 'd' although this is not allowed.");
		}

		@Test
		void calls_System_err_print_with_a_double(
		) {
			Throwable exception = catchThrowable(
				() -> assertNothingWrittenToSystemErr(
					() -> System.err.print(1d)
				)
			);

			assertThat(exception)
				.isInstanceOf(AssertionError.class)
				.hasMessage("Tried to write '1' although this is not allowed.");
		}

		@Test
		void calls_System_err_print_with_a_float(
		) {
			Throwable exception = catchThrowable(
				() -> assertNothingWrittenToSystemErr(
					() -> System.err.print(1f)
				)
			);

			assertThat(exception)
				.isInstanceOf(AssertionError.class)
				.hasMessage("Tried to write '1' although this is not allowed.");
		}

		@Test
		void calls_System_err_print_with_an_int(
		) {
			Throwable exception = catchThrowable(
				() -> assertNothingWrittenToSystemErr(
					() -> System.err.print(1)
				)
			);

			assertThat(exception)
				.isInstanceOf(AssertionError.class)
				.hasMessage("Tried to write '1' although this is not allowed.");
		}

		@Test
		void calls_System_err_print_with_a_long(
		) {
			Throwable exception = catchThrowable(
				() -> assertNothingWrittenToSystemErr(
					() -> System.err.print(1L)
				)
			);

			assertThat(exception)
				.isInstanceOf(AssertionError.class)
				.hasMessage("Tried to write '1' although this is not allowed.");
		}

		@Test
		void calls_System_err_print_with_an_object(
		) {
			Throwable exception = catchThrowable(
				() -> assertNothingWrittenToSystemErr(
					() -> System.err.print(new Object())
				)
			);

			assertThat(exception)
				.isInstanceOf(AssertionError.class)
				.hasMessage("Tried to write 'j' although this is not allowed.");
		}

		@Test
		void calls_System_err_print_with_a_string(
		) {
			Throwable exception = catchThrowable(
				() -> assertNothingWrittenToSystemErr(
					() -> System.err.print("dummy")
				)
			);

			assertThat(exception)
				.isInstanceOf(AssertionError.class)
				.hasMessage("Tried to write 'd' although this is not allowed.");
		}

		@Test
		void calls_System_err_printf_with_a_localized_formatted_text(
		) {
			Throwable exception = catchThrowable(
				() -> assertNothingWrittenToSystemErr(
					() -> System.err.printf(
						DUMMY_LOCALE, "%s, %s", "first dummy", "second dummy"
					)
				)
			);

			assertThat(exception)
				.isInstanceOf(AssertionError.class)
				.hasMessage("Tried to write 'f' although this is not allowed.");
		}

		@Test
		void calls_System_err_printf_with_a_formatted_text(
		) {
			Throwable exception = catchThrowable(
				() -> assertNothingWrittenToSystemErr(
					() -> System.err.printf(
						"%s, %s", "first dummy", "second dummy"
					)
				)
			);

			assertThat(exception)
				.isInstanceOf(AssertionError.class)
				.hasMessage("Tried to write 'f' although this is not allowed.");
		}

		@Test
		void calls_println_on_System_err(
		) {
			Throwable exception = catchThrowable(
				() -> assertNothingWrittenToSystemErr(
					() -> System.err.println()
				)
			);

			assertThat(exception)
				.isInstanceOf(AssertionError.class)
				.hasMessage(
					"Tried to write '"
						+ getProperty("line.separator").substring(0, 1)
						+ "' although this is not allowed."
				);
		}

		@Test
		void calls_System_err_println_with_a_boolean(
		) {
			Throwable exception = catchThrowable(
				() -> assertNothingWrittenToSystemErr(
					() -> System.err.println(true)
				)
			);

			assertThat(exception)
				.isInstanceOf(AssertionError.class)
				.hasMessage("Tried to write 't' although this is not allowed.");
		}

		@Test
		void calls_System_err_println_with_a_char(
		) {
			Throwable exception = catchThrowable(
				() -> assertNothingWrittenToSystemErr(
					() -> System.err.println('a')
				)
			);

			assertThat(exception)
				.isInstanceOf(AssertionError.class)
				.hasMessage("Tried to write 'a' although this is not allowed.");
		}

		@Test
		void calls_System_err_println_with_an_array_of_chars(
		) {
			Throwable exception = catchThrowable(
				() -> assertNothingWrittenToSystemErr(
					() -> System.err.println(
						new char[]{'d', 'u', 'm', 'm', 'y'}
					)
				)
			);

			assertThat(exception)
				.isInstanceOf(AssertionError.class)
				.hasMessage("Tried to write 'd' although this is not allowed.");
		}

		@Test
		void calls_System_err_println_with_a_double(
		) {
			Throwable exception = catchThrowable(
				() -> assertNothingWrittenToSystemErr(
					() -> System.err.println(1d)
				)
			);

			assertThat(exception)
				.isInstanceOf(AssertionError.class)
				.hasMessage("Tried to write '1' although this is not allowed.");
		}

		@Test
		void calls_System_err_println_with_a_float(
		) {
			Throwable exception = catchThrowable(
				() -> assertNothingWrittenToSystemErr(
					() -> System.err.println(1f)
				)
			);

			assertThat(exception)
				.isInstanceOf(AssertionError.class)
				.hasMessage("Tried to write '1' although this is not allowed.");
		}

		@Test
		void calls_System_err_println_with_an_int(
		) {
			Throwable exception = catchThrowable(
				() -> assertNothingWrittenToSystemErr(
					() -> System.err.println(1)
				)
			);

			assertThat(exception)
				.isInstanceOf(AssertionError.class)
				.hasMessage("Tried to write '1' although this is not allowed.");
		}

		@Test
		void calls_System_err_println_with_a_long(
		) {
			Throwable exception = catchThrowable(
				() -> assertNothingWrittenToSystemErr(
					() -> System.err.println(1L)
				)
			);

			assertThat(exception)
				.isInstanceOf(AssertionError.class)
				.hasMessage("Tried to write '1' although this is not allowed.");
		}

		@Test
		void calls_System_err_println_with_an_object(
		) {
			Throwable exception = catchThrowable(
				() -> assertNothingWrittenToSystemErr(
					() -> System.err.println(new Object())
				)
			);

			assertThat(exception)
				.isInstanceOf(AssertionError.class)
				.hasMessage("Tried to write 'j' although this is not allowed.");
		}

		@Test
		void calls_System_err_println_with_a_string(
		) {
			Throwable exception = catchThrowable(
				() -> assertNothingWrittenToSystemErr(
					() -> System.err.println("dummy")
				)
			);

			assertThat(exception)
				.isInstanceOf(AssertionError.class)
				.hasMessage("Tried to write 'd' although this is not allowed.");
		}
	}

	@Test
	void exception_thrown_by_statement_is_rethrown(
	) {
		Exception exception = new Exception("some exception");
		Throwable rethrownException = catchThrowable(
			() -> assertNothingWrittenToSystemErr(
				() -> {
					throw exception;
				}
			)
		);
		assertThat(rethrownException).isSameAs(exception);
	}

	@Test
	void statement_is_executed(
	) throws Exception {
		StatementMock statementMock = new StatementMock();
		assertNothingWrittenToSystemErr(
			statementMock
		);
		assertThat(statementMock.hasBeenEvaluated).isTrue();
	}

	@Nested
	class System_err_is_same_as_before {
		@Test
		void after_statement_is_executed(
		) throws Exception {
			PrintStream originalErr = err;
			assertNothingWrittenToSystemErr(
				() -> {
				}
			);
			assertThat(err).isSameAs(originalErr);
		}

		@Test
		void after_statement_throws_exception() {
			PrintStream originalErr = err;
			ignoreException(
				() -> assertNothingWrittenToSystemErr(
					() -> {
						throw new Exception("some exception");
					}
				)
			);
			assertThat(err).isSameAs(originalErr);
		}
	}
}
