package com.github.stefanbirkner.systemlambda;

import com.github.stefanbirkner.systemlambda.SystemLambda.WithEnvironmentVariables;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static com.github.stefanbirkner.fishbowl.Fishbowl.ignoreException;
import static com.github.stefanbirkner.systemlambda.SystemLambda.withEnvironmentVariable;
import static java.lang.System.getenv;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

@DisplayNameGeneration(ReplaceUnderscores.class)
class WithEnvironmentVariableTest {
	@Test
	void statement_is_executed(
	) throws Exception {
		StatementMock statementMock = new StatementMock();

		withEnvironmentVariable("dummy name", "dummy value")
			.execute(statementMock);

		assertThat(statementMock.hasBeenEvaluated).isTrue();
	}

	@Nested
	class environment_variable_that_is_set_to_some_value {
		@Test
		void is_available_in_the_statement(
		) throws Exception {
			withEnvironmentVariable("dummy name", "dummy value")
				.execute(() ->
					assertThat(getenv("dummy name")).isEqualTo("dummy value")
				);
		}

		@Test
		void is_available_in_the_statement_from_environment_variables_map(
		) throws Exception {
			withEnvironmentVariable("dummy name", "dummy value")
				.execute(() ->
					assertThat(getenv()).containsEntry("dummy name", "dummy value")
				);
		}
	}

	@Test
	void multiple_values_can_be_set_is_available_in_the_statement(
	) throws Exception {
		withEnvironmentVariable("first", "first value")
			.and("second", "second value")
			.execute(() -> {
				assertThat(getenv("first")).isEqualTo("first value");
				assertThat(getenv("second")).isEqualTo("second value");
			});
	}

	@Nested
	class environment_variable_that_is_set_to_null {
		@Test
		void is_null_in_the_statement(
		) throws Exception {
			//we need to set a value because it is null by default
			withEnvironmentVariable("dummy name", randomValue())
				.execute(() ->
					withEnvironmentVariable("dummy name", null)
						.execute(() -> assertThat(getenv("dummy name")).isNull())
				);
		}

		@Test
		void is_not_stored_in_the_environment_variables_map_in_the_statement(
		) throws Exception {
			//we need to set a value because it is null by default
			withEnvironmentVariable("dummy name", randomValue())
				.execute(() ->
					withEnvironmentVariable("dummy name", null)
						.execute(() -> assertThat(getenv()).doesNotContainKey("dummy name"))
				);
		}
	}

	@Test
	void an_environment_variable_cannot_be_set_twice() {
		Throwable exception = catchThrowable(() ->
			withEnvironmentVariable("dummy name", "first value")
				.and("dummy name", "second value")
				.execute(() -> {})
		);

		assertThat(exception)
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage(
				"The environment variable 'dummy name' cannot be set to"
					+ " 'second value' because it was already set to 'first"
					+ " value'."
			);
	}

	@Test
	void when_an_environment_variable_is_set_twice_null_is_not_enclosed_in_single_quotes() {
		Throwable exception = catchThrowable(() ->
			withEnvironmentVariable("dummy name", null)
				.and("dummy name", null)
				.execute(() -> {})
		);

		assertThat(exception)
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage(
				"The environment variable 'dummy name' cannot be set to null"
					+ " because it was already set to null."
			);
	}

	@Test
	void the_and_method_creates_a_new_object_so_that_EnvironmentVariables_object_can_be_reused(
	) throws Exception {
		WithEnvironmentVariables baseSetting = withEnvironmentVariable("first", "first value");
		baseSetting.and("second", "second value")
			.execute(() -> {});

		baseSetting.and("third", "third value")
			.execute(() -> {
				assertThat(getenv("first")).isEqualTo("first value");
				assertThat(getenv("second")).isNull();
				assertThat(getenv("third")).isEqualTo("third value");
			});
	}

	@Nested
	class environment_variables_map_contains_same_values_as_before {
		@Test
		void after_statement_is_executed(
		) throws Exception {
			Map<String, String> originalEnvironmentVariables
				= new HashMap<>(getenv());

			withEnvironmentVariable("dummy name", randomValue())
				.execute(() -> {
				});

			assertThat(getenv()).isEqualTo(originalEnvironmentVariables);
		}

		@Test
		void after_statement_throws_exception() {
			Map<String, String> originalEnvironmentVariables
				= new HashMap<>(getenv());

			ignoreException(
				() -> withEnvironmentVariable("dummy name", randomValue())
					.execute(() -> {
						throw new RuntimeException("dummy exception"); }
					)
			);

			assertThat(getenv()).isEqualTo(originalEnvironmentVariables);
		}
	}

	@Nested
	class environment_variables_are_the_same_as_before {
		@Test
		void after_statement_is_executed(
		) throws Exception {
			String originalValue = getenv("dummy name");

			withEnvironmentVariable("dummy name", randomValue())
				.execute(() -> {
				});

			assertThat(getenv("dummy name")).isEqualTo(originalValue);
		}

		@Test
		void after_statement_throws_exception() {
			String originalValue = getenv("dummy name");

			ignoreException(
				() -> withEnvironmentVariable("dummy name", randomValue())
					.execute(() -> {
							throw new RuntimeException("dummy exception");
						}
					)
			);

			assertThat(getenv("dummy name")).isEqualTo(originalValue);
		}
	}

	private String randomValue() {
		return RandomStringUtils.random(20);
	}
}
