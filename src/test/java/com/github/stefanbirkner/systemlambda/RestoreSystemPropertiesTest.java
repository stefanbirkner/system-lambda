package com.github.stefanbirkner.systemlambda;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.github.stefanbirkner.fishbowl.Fishbowl.ignoreException;
import static com.github.stefanbirkner.systemlambda.SystemLambda.restoreSystemProperties;
import static java.lang.System.clearProperty;
import static java.lang.System.getProperty;
import static java.lang.System.setProperty;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayNameGeneration(ReplaceUnderscores.class)
class RestoreSystemPropertiesTest {
	@Test
	void statement_is_executed(
	) throws Exception {
		StatementMock statementMock = new StatementMock();

		restoreSystemProperties(
			statementMock
		);

		assertThat(statementMock.hasBeenEvaluated).isTrue();
	}

	@Nested
	class properties_have_the_same_values_as_before {
		@Test
		void after_statement_is_executed(
		) throws Exception {
			setProperty("some property", "some value");
			setProperty("another property", "some value");

			restoreSystemProperties(
				() -> {
					clearProperty("some property");
					setProperty("another property", "another value");
				}
			);

			assertThat(getProperty("some property"))
				.isEqualTo("some value");
			assertThat(getProperty("another property"))
				.isEqualTo("some value");
		}

		@Test
		void after_statement_throws_exception(
		) {
			setProperty("some property", "some value");
			setProperty("another property", "some value");

			ignoreException(() ->
				restoreSystemProperties(
					() -> {
						clearProperty("some property");
						setProperty("another property", "another value");
						throw new RuntimeException();
					}
				)
			);

			assertThat(getProperty("some property"))
				.isEqualTo("some value");
			assertThat(getProperty("another property"))
				.isEqualTo("some value");
		}
	}

	@Nested
	class property_that_does_not_exist_before_the_statement_is_executed {
		@Test
		void does_not_exist_afterwards(
		) throws Exception {
			clearProperty("some property");

			restoreSystemProperties(
				() -> setProperty("some property", "some value")
			);

			assertThat(getProperty("some property"))
				.isNull();
		}

		@Test
		void does_not_exist_after_statement_throws_exception(
		) {
			clearProperty("some property");

			ignoreException(() ->
				restoreSystemProperties(
					() -> {
						setProperty("some property", "some value");
						throw new RuntimeException();
					}
				)
			);

			assertThat(getProperty("some property"))
				.isNull();
		}
	}

	@Test
	void at_start_of_the_statement_execution_properties_are_equal_to_the_original_properties(
	) throws Exception {
		AtomicReference<Properties> propertiesAtStartOfExecution
			= new AtomicReference<>();
		//ensure at least one property is set
		setProperty("some property", "some value");
		Properties originalProperties = System.getProperties();

		restoreSystemProperties(
			() -> propertiesAtStartOfExecution.set(System.getProperties())
		);

		assertThat(propertiesAtStartOfExecution.get())
			.isEqualTo(originalProperties);
	}
}
