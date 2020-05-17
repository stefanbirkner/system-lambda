package com.github.stefanbirkner.systemlambda;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static com.github.stefanbirkner.fishbowl.Fishbowl.exceptionThrownBy;
import static com.github.stefanbirkner.systemlambda.SystemLambda.withTextFromSystemIn;
import static java.lang.String.format;
import static java.lang.System.in;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

@DisplayNameGeneration(ReplaceUnderscores.class)
@Timeout(value = 10, unit = SECONDS)
class WithTextFromSystemInTest {
	private static final byte[] DUMMY_ARRAY = new byte[1024];
	private static final int VALID_OFFSET = 2;
	private static final int VALID_READ_LENGTH = 100;
	private static final IOException DUMMY_IO_EXCEPTION = new IOException();
	private static final RuntimeException DUMMY_RUNTIME_EXCEPTION
		= new RuntimeException();

	@BeforeAll
	static void checkArrayConstants() {
		assertThat(VALID_OFFSET).isBetween(0, DUMMY_ARRAY.length);
		assertThat(VALID_READ_LENGTH)
			.isBetween(0, DUMMY_ARRAY.length - VALID_OFFSET);
	}

	@Test
	void provided_text_is_available_from_system_in(
	) throws Exception {
		AtomicReference<String> secondLineCapture = new AtomicReference<>();

		withTextFromSystemIn(
			"first line",
			"second line"
		).execute(() -> {
			Scanner firstScanner = new Scanner(in);
			firstScanner.nextLine();
			Scanner secondScanner = new Scanner(in);
			secondLineCapture.set(secondScanner.nextLine());
		});

		assertThat(secondLineCapture).hasValue("second line");
	}

	@Test
	void no_text_is_available_from_system_in_if_no_text_has_been_provided(
	) throws Exception {
		AtomicInteger charCapture = new AtomicInteger();

		withTextFromSystemIn()
			.execute(() -> {
				charCapture.set(in.read());
			});

		assertThat(charCapture).hasValue(-1);
	}

	@Test
	void system_in_provides_specified_text_and_throws_requested_IOException_afterwards(
	) throws Exception {
		withTextFromSystemIn("arbitrary text")
			.andExceptionThrownOnInputEnd(DUMMY_IO_EXCEPTION)
			.execute(() -> {
				assertSystemInProvidesText(format("arbitrary text%n"));
				Throwable exception = exceptionThrownBy(in::read);
				assertThat(exception).isSameAs(DUMMY_IO_EXCEPTION);
			});
	}

	@Test
	void system_in_throws_requested_IOException_on_first_read_if_no_text_has_been_specified(
	) throws Exception {
		withTextFromSystemIn()
			.andExceptionThrownOnInputEnd(DUMMY_IO_EXCEPTION)
			.execute(() -> {
				Throwable exception = exceptionThrownBy(in::read);
				assertThat(exception).isSameAs(DUMMY_IO_EXCEPTION);
			});
	}

	@Test
	void system_in_provides_specified_text_and_throws_requested_RuntimeException_afterwards(
	) throws Exception {
		withTextFromSystemIn("arbitrary text")
			.andExceptionThrownOnInputEnd(DUMMY_RUNTIME_EXCEPTION)
			.execute(() -> {
				assertSystemInProvidesText(format("arbitrary text%n"));
				Throwable exception = exceptionThrownBy(in::read);
				assertThat(exception).isSameAs(DUMMY_RUNTIME_EXCEPTION);
			});
	}

	@Test
	void system_in_throws_requested_RuntimeException_on_first_read_if_no_text_has_been_specified(
	) throws Exception {
		withTextFromSystemIn()
			.andExceptionThrownOnInputEnd(DUMMY_RUNTIME_EXCEPTION)
			.execute(() -> {
				Throwable exception = exceptionThrownBy(in::read);
				assertThat(exception).isSameAs(DUMMY_RUNTIME_EXCEPTION);
			});
	}

	@Test
	void an_IOException_cannot_be_requested_if_a_RuntimeException_has_already_been_requested() {
		Throwable exception = exceptionThrownBy(() -> {
			withTextFromSystemIn()
				.andExceptionThrownOnInputEnd(DUMMY_RUNTIME_EXCEPTION)
				.andExceptionThrownOnInputEnd(DUMMY_IO_EXCEPTION)
				.execute(() -> {});
		});

		assertThat(exception)
			.hasMessage(
				"You cannot call andExceptionThrownOnInputEnd(IOException)"
					+ " because andExceptionThrownOnInputEnd(RuntimeException) has"
					+ " already been called.");
	}

	@Test
	void a_RuntimeException_cannot_be_requested_if_an_IOException_has_already_been_requested() {
		Throwable exception = exceptionThrownBy(() -> {
			withTextFromSystemIn()
				.andExceptionThrownOnInputEnd(DUMMY_IO_EXCEPTION)
				.andExceptionThrownOnInputEnd(DUMMY_RUNTIME_EXCEPTION)
				.execute(() -> {});
		});

		assertThat(exception)
			.hasMessage(
				"You cannot call andExceptionThrownOnInputEnd(RuntimeException)"
					+ " because andExceptionThrownOnInputEnd(IOException) has"
					+ " already been called.");
	}

	//this is default behaviour of an InputStream according to its JavaDoc
	@Test
	void system_in_throws_NullPointerException_when_read_is_called_with_null_array(
	) throws Exception {
		withTextFromSystemIn("arbitrary text")
			.execute(() -> {
				Throwable exception = catchThrowable(
					() -> in.read(null)
				);
				assertThat(exception)
					.isInstanceOf(NullPointerException.class);
			});
	}

	//this is default behaviour of an InputStream according to its JavaDoc
	@Test
	void system_in_throws_IndexOutOfBoundsException_when_read_is_called_with_negative_offset(
	) throws Exception {
		withTextFromSystemIn("arbitrary text")
			.execute(() -> {
				Throwable exception = catchThrowable(
					() -> System.in.read(DUMMY_ARRAY, -1, VALID_READ_LENGTH)
				);
				assertThat(exception)
					.isInstanceOf(IndexOutOfBoundsException.class);
			});
	}

	//this is default behaviour of an InputStream according to its JavaDoc
	@Test
	void system_in_throws_IndexOutOfBoundsException_when_read_is_called_with_negative_length(
	) throws Exception {
		withTextFromSystemIn("arbitrary text")
			.execute(() -> {
				Throwable exception = catchThrowable(
					() -> System.in.read(DUMMY_ARRAY, VALID_OFFSET, -1)
				);
				assertThat(exception)
					.isInstanceOf(IndexOutOfBoundsException.class);
			});
	}

	//this is default behaviour of an InputStream according to its JavaDoc
	@Test
	void system_in_throws_IndexOutOfBoundsException_when_read_is_called_with_oversized_length(
	) throws Exception {
		withTextFromSystemIn("arbitrary text")
			.execute(() -> {
				Throwable exception = catchThrowable(() -> {
					int oversizedLength = DUMMY_ARRAY.length - VALID_OFFSET + 1;
					System.in.read(DUMMY_ARRAY, VALID_OFFSET, oversizedLength);
				});
				assertThat(exception)
					.isInstanceOf(IndexOutOfBoundsException.class);
			});
	}

	@Test
	void system_in_reads_zero_bytes_even_if_mock_should_throw_IOException_on_input_end(
	) throws Exception {
		withTextFromSystemIn()
			.andExceptionThrownOnInputEnd(DUMMY_IO_EXCEPTION)
			.execute(() -> {
				int numBytesRead = System.in.read(DUMMY_ARRAY, VALID_OFFSET, 0);
				assertThat(numBytesRead).isZero();
			});
	}

	@Test
	void system_in_reads_zero_bytes_even_if_mock_should_throw_RuntimeException_on_input_end(
	) throws Exception {
		withTextFromSystemIn()
			.andExceptionThrownOnInputEnd(DUMMY_RUNTIME_EXCEPTION)
			.execute(() -> {
				int numBytesRead = System.in.read(DUMMY_ARRAY, VALID_OFFSET, 0);
				assertThat(numBytesRead).isZero();
			});
	}

	@Test
	void system_in_read_bytes_throws_specified_IOException_on_input_end(
	) throws Exception {
		withTextFromSystemIn()
			.andExceptionThrownOnInputEnd(DUMMY_IO_EXCEPTION)
			.execute(() -> {
				Throwable exception = catchThrowable(
					() -> System.in.read(
						DUMMY_ARRAY, VALID_OFFSET, VALID_READ_LENGTH
					)
				);
				assertThat(exception)
					.isSameAs(DUMMY_IO_EXCEPTION);
			});
	}

	@Test
	void system_in_read_bytes_throws_specified_RuntimeException_on_input_end(
	) throws Exception {
		AtomicReference<Throwable> exceptionCapture = new AtomicReference<>();

		withTextFromSystemIn()
			.andExceptionThrownOnInputEnd(DUMMY_RUNTIME_EXCEPTION)
			.execute(() -> {
				Throwable exception = catchThrowable(
					() -> System.in.read(
						DUMMY_ARRAY, VALID_OFFSET, VALID_READ_LENGTH
					)
				);
				exceptionCapture.set(exception);
			});

		assertThat(exceptionCapture)
			.hasValue(DUMMY_RUNTIME_EXCEPTION);
	}


	@Nested
	class System_in_is_same_as_before {
		@Test
		void after_statement_is_executed(
		) throws Exception {
			InputStream originalSystemIn = System.in;

			withTextFromSystemIn("arbitrary text")
				.execute(() -> {});

			assertThat(System.in).isSameAs(originalSystemIn);
		}

		@Test
		void after_statement_throws_exception(
		) {
			InputStream originalSystemIn = System.in;

			catchThrowable(
				() -> withTextFromSystemIn("arbitrary text")
					.execute(
						() -> {
							throw new Exception("some exception");
						}
					)
			);

			assertThat(System.in).isSameAs(originalSystemIn);
		}
	}

	private static void assertSystemInProvidesText(
		String text
	) throws IOException {
		for (char c : text.toCharArray())
			assertThat((char) System.in.read()).isSameAs(c);
	}
}

