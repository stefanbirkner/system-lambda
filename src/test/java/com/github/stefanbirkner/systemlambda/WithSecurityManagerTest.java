package com.github.stefanbirkner.systemlambda;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.github.stefanbirkner.fishbowl.Fishbowl.ignoreException;
import static com.github.stefanbirkner.systemlambda.SystemLambda.withSecurityManager;
import static java.lang.System.getSecurityManager;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayNameGeneration(ReplaceUnderscores.class)
class WithSecurityManagerTest {
	private static final SecurityManager MANAGER = new SecurityManagerMock();

	@Test
	void specified_security_manager_is_present_while_statement_is_executed(
	) throws Exception {
		withSecurityManager(
			MANAGER,
			() -> assertThat(getSecurityManager()).isSameAs(MANAGER)
		);
	}
	
    @Test
    void statement_is_executed() throws Exception {
        StatementMock statementMock = new StatementMock();

        withSecurityManager(
			MANAGER,
			statementMock
		);

        assertThat(statementMock.hasBeenEvaluated).isTrue();
	}

	@Nested
	class the_security_manager_is_the_same_as_before {
		@Test
		void after_statement_is_executed(
		) throws Exception {
			SecurityManager originalManager = getSecurityManager();

			withSecurityManager(
				MANAGER,
				() -> {
				}
			);

			assertThat(getSecurityManager()).isSameAs(originalManager);
		}

		@Test
		void after_statement_throws_exception() {
			SecurityManager originalSecurityManager = getSecurityManager();

			ignoreException(() ->
				withSecurityManager(
					MANAGER,
					() -> {
						throw new RuntimeException();
					}
				)
			);

			assertThat(getSecurityManager()).isSameAs(originalSecurityManager);
		}
	}
}
