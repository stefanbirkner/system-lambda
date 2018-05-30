package com.github.stefanbirkner.systemlambda;

import static com.github.stefanbirkner.systemlambda.SystemLambda.catchSystemExit;
import static com.github.stefanbirkner.systemlambda.SystemLambda.withSecurityManager;
import static java.lang.System.exit;
import static java.lang.System.getSecurityManager;
import static java.net.InetAddress.getLocalHost;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import com.github.stefanbirkner.systemlambda.SecurityManagerMock.Invocation;

import java.io.FileDescriptor;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.AllPermission;
import java.security.Permission;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

@DisplayNameGeneration(ReplaceUnderscores.class)
class CatchSystemExitTest {
	private static final int ARBITRARY_STATUS = 216843;

	@Nested
	class check_system_exit {
		@ParameterizedTest(name = "{0}")
		@ArgumentsSource(SecurityManagers.class)
		void status_provided_to_System_exit_is_made_available_when_called_in_same_thread(
			String description,
			SecurityManager securityManager
		) throws Exception {
			withSecurityManager(
				securityManager,
				() -> {
					int status = catchSystemExit(
						() -> exit(ARBITRARY_STATUS)
					);
					assertThat(status).isEqualTo(ARBITRARY_STATUS);
				}
			);
		}

		@ParameterizedTest(name = "{0}")
		@ArgumentsSource(SecurityManagers.class)
		void status_provided_to_System_exit_is_made_available_when_called_in_another_thread(
			String description,
			SecurityManager securityManager
		) throws Exception {
			withSecurityManager(
				securityManager,
				() -> {
					int status = catchSystemExit(
						() -> {
							Thread thread = new Thread(
								() -> exit(ARBITRARY_STATUS)
							);
							thread.start();
							thread.join();
						}
					);
					assertThat(status).isEqualTo(ARBITRARY_STATUS);
				}
			);
		}

		@ParameterizedTest(name = "{0}")
		@ArgumentsSource(SecurityManagers.class)
		void test_fails_if_System_exit_is_not_called(
			String description,
			SecurityManager securityManager
		) throws Exception {
			withSecurityManager(
				securityManager,
				() ->
					assertThatThrownBy(
                    	() -> catchSystemExit(() -> {})
                	)
                    .isInstanceOf(AssertionError.class)
                    .hasMessage("System.exit has not been called.")
			);
		}

		@ParameterizedTest(name = "{0}")
		@ArgumentsSource(SecurityManagers.class)
		void after_execution_the_security_manager_is_the_same_as_before(
			String description,
			SecurityManager securityManager
		) throws Exception {
			AtomicReference<SecurityManager> managerAfterExecution
				= new AtomicReference<>();
			withSecurityManager(
				securityManager,
				() -> {
					catchSystemExit(() -> exit(ARBITRARY_STATUS));
					managerAfterExecution.set(getSecurityManager());
				}
			);
			assertThat(managerAfterExecution).hasValue(securityManager);
		}
	}

	@Nested
	class security_managers_public_methods {
		@ParameterizedTest(name = "{0}")
		@ArgumentsSource(SecurityManagerPublicMethods.class)
		void may_be_called_when_original_security_manager_is_missing(
			String description,
			Method method
		) throws Exception {
			withSecurityManager(
				null,
				() -> catchSystemExit(
					() -> {
						method.invoke(
							getSecurityManager(),
							dummyArguments(method)
						);
						//ensure that catchSystemExit does not fail
						exit(ARBITRARY_STATUS);
					}
				)
			);
		}

		@ParameterizedTest(name = "{0}")
		@ArgumentsSource(SecurityManagerPublicMethods.class)
		void is_delegated_to_original_security_manager_when_it_is_present(
			String testName,
			Method method
		) throws Exception {
			SecurityManagerMock originalManager = new SecurityManagerMock();
			Object[] arguments = dummyArguments(method);
			withSecurityManager(
				originalManager,
				() -> catchSystemExit(
					() -> {
						method.invoke(getSecurityManager(), arguments);
						//ensure that catchSystemExit does not fail
						exit(ARBITRARY_STATUS);
					}
				)
			);
			assertCallIsDelegated(originalManager, method, arguments);
		}

		private Object[] dummyArguments(
			Method method
		) throws UnknownHostException {
			Class<?>[] parameterTypes = method.getParameterTypes();
			Object[] args = new Object[parameterTypes.length];
			for (int i = 0; i < args.length; ++i)
				args[i] = dummy(parameterTypes[i]);
			return args;
		}

		private Object dummy(
			Class<?> type
		) throws UnknownHostException {
			if (type.getName().equals("int"))
				return new Random().nextInt();
			else if (type.getName().equals("byte"))
				return (byte) new Random().nextInt();
			else if (type.equals(String.class))
				return randomUUID().toString();
			else if (type.equals(Class.class))
				return String.class;
			else if (type.equals(FileDescriptor.class))
				return new FileDescriptor();
			else if (type.equals(InetAddress.class))
				return getLocalHost();
			else if (type.equals(Object.class))
				return new Object();
			else if (type.equals(Permission.class))
				return new AllPermission();
			else if (type.equals(Thread.class))
				return new Thread();
			else if (type.equals(ThreadGroup.class))
				return new ThreadGroup("arbitrary-thread-group");
			else
				throw new IllegalArgumentException(type + " not supported.");
		}

		private void assertCallIsDelegated(
			SecurityManagerMock target,
			Method method,
			Object[] arguments
		) {
			Collection<Invocation> invocations = invocationsForMethod(
				target,
				method
			);
			assertThat(invocations)
				.withFailMessage("Method was not invoked.")
				.isNotEmpty();
			assertThat(argumentsOf(invocations))
				.contains(arguments);
		}

		private Collection<Invocation> invocationsForMethod(
			SecurityManagerMock target,
			Method method
		) {
			return target.invocations
				.stream()
				.filter(invocation -> matchesMethod(invocation, method))
				.collect(toList());
		}

		private boolean matchesMethod(
			Invocation invocation,
			Method method
		) {
			return Objects.equals(
					method.getName(),
					invocation.methodName
				)
				&& Arrays.equals(
					method.getParameterTypes(),
					invocation.parameterTypes
				);
		}

		private Stream<Object[]> argumentsOf(Collection<Invocation> invocations) {
			return invocations.stream().map(invocation -> invocation.arguments);
		}
	}

	@Nested
	class security_managers_public_non_void_methods {
		private final SecurityManagerMock originalSecurityManager
			= new SecurityManagerMock();

		@Test
		void getInCheck_is_delegated_to_original_security_manager(
		) throws Exception {
			originalSecurityManager.inCheck = true;
			AtomicBoolean inCheck = new AtomicBoolean();
			withSecurityManager(
				originalSecurityManager,
				() -> catchSystemExit(
					() -> {
						inCheck.set(getSecurityManager().getInCheck());
						//ensure that catchSystemExit does not fail
						exit(ARBITRARY_STATUS);
					}
				)
			);
			assertThat(inCheck).isTrue();
		}

		@Test
		void security_context_of_original_security_manager_is_provided(
		) throws Exception {
			Object context = new Object();
			originalSecurityManager.securityContext = context;
			AtomicReference<Object> contextDuringExecution = new AtomicReference<>();
			withSecurityManager(
				originalSecurityManager,
				() -> catchSystemExit(
					() -> {
						contextDuringExecution.set(
							getSecurityManager().getSecurityContext()
						);
						//ensure that catchSystemExit does not fail
						exit(ARBITRARY_STATUS);
					}
				)
			);
			assertThat(contextDuringExecution).hasValue(context);
		}

		@Test
		void checkTopLevelWindow_is_delegated_to_original_security_manager(
		) throws Exception {
			originalSecurityManager.topLevelWindow = true;
			Object window = new Object();
			AtomicBoolean check = new AtomicBoolean();
			withSecurityManager(
				originalSecurityManager,
				() -> catchSystemExit(
					() -> {
						check.set(
							getSecurityManager().checkTopLevelWindow(window)
						);
						//ensure that catchSystemExit does not fail
						exit(ARBITRARY_STATUS);
					}
				)
			);
			assertThat(check).isTrue();
			assertThat(originalSecurityManager.windowOfCheckTopLevelWindowCall)
				.isSameAs(window);
		}

		@Test
		void thread_group_of_original_security_manager_is_provided(
		) throws Exception {
			ThreadGroup threadGroup = new ThreadGroup("dummy name");
			originalSecurityManager.threadGroup = threadGroup;
			AtomicReference<Object> threadGroupDuringExecution = new AtomicReference<>();
			withSecurityManager(
				originalSecurityManager,
				() -> catchSystemExit(
					() -> {
						threadGroupDuringExecution.set(
							getSecurityManager().getThreadGroup());
						//ensure that catchSystemExit does not fail
						exit(ARBITRARY_STATUS);
					}
				)
			);
			assertThat(threadGroupDuringExecution).hasValue(threadGroup);
		}
	}

	private static String join(Class<?>[] types) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < types.length; i++) {
			if (i != 0)
				sb.append(",");
			sb.append(types[i].getSimpleName());
		}
		return sb.toString();
	}

	private static class SecurityManagers implements ArgumentsProvider {
		@Override
		public Stream<? extends Arguments> provideArguments(
			ExtensionContext extensionContext
		)  {
			return Stream.of(
				arguments(
					"with original SecurityManager",
					new SecurityManagerMock()
				),
				arguments(
					"without original SecurityManager",
					null
				)
			);
		}
	}

	private static class SecurityManagerPublicMethods
		implements ArgumentsProvider
	{
		@Override
		public Stream<? extends Arguments> provideArguments(
			ExtensionContext extensionContext
		)  {
			return Arrays.stream(SecurityManager.class.getMethods())
				.filter(this::notDeclaredByObjectClass)
				.filter(this::notChangedByNoExitSecurityManager)
				.map(method -> arguments(testName(method), method));
		}

		private boolean notDeclaredByObjectClass(Method method) {
			return !method.getDeclaringClass().equals(Object.class);
		}

		private boolean notChangedByNoExitSecurityManager(Method method) {
			return !method.getName().equals("checkExit");
		}

		private String testName(Method method) {
			return method.getName()
				+ "(" + join(method.getParameterTypes()) + ")";
		}
	}
}
