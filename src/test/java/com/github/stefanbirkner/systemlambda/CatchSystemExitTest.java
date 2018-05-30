package com.github.stefanbirkner.systemlambda;

import static com.github.stefanbirkner.systemlambda.SystemLambda.catchSystemExit;
import static com.github.stefanbirkner.systemlambda.SystemLambda.withSecurityManager;
import static java.lang.System.exit;
import static java.lang.System.getSecurityManager;
import static java.net.InetAddress.getLocalHost;
import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.*;

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
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

@RunWith(Enclosed.class)
public class CatchSystemExitTest {
	private static final int ARBITRARY_STATUS = 216843;

	@RunWith(Parameterized.class)
	public static class check_system_exit {

		@Parameters(name = "{0}")
		public static List<Object[]> data() {
			return asList(
				new Object[] {
					"with_original_SecurityManager",
					new SecurityManagerMock()
				},
				new Object[] {
					"without_original_SecurityManager",
					null
				}
			);
		}

		@Parameter(0)
		public String name;

		@Parameter(1)
		public SecurityManager securityManager;

		@Test
		public void status_provided_to_System_exit_is_made_available_when_called_in_same_thread(
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

		@Test
		public void status_provided_to_System_exit_is_made_available_when_called_in_another_thread(
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

		@Test
		public void test_fails_if_System_exit_is_not_called(
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

		@Test
		public void after_execution_the_security_manager_is_the_same_as_before(
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

	@RunWith(Parameterized.class)
	public static class security_managers_public_methods {
		@Parameters(name = "{0}")
		public static List<Object[]> data() {
			List<Object[]> methods = new ArrayList<>();
			for (Method method : SecurityManager.class.getMethods())
				if (
					notDeclaredByObjectClass(method)
					&& notChangedByNoExitSecurityManager(method)
				)
					methods.add(new Object[] { testName(method), method });
			return methods;
		}

		@Parameter(0)
		public String testName;

		@Parameter(1)
		public Method method;

		@Test
		public void may_be_called_when_original_security_manager_is_missing(
		) throws Exception {
			withSecurityManager(
				null,
				() -> catchSystemExit(
					() -> {
						method.invoke(getSecurityManager(), dummyArguments());
						//ensure that catchSystemExit does not fail
						exit(ARBITRARY_STATUS);
					}
				)
			);
		}

		@Test
		public void is_delegated_to_original_security_manager_when_it_is_present(
		) throws Exception {
			SecurityManagerMock originalManager = new SecurityManagerMock();
			Object[] arguments = dummyArguments();
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
			assertCallIsDelegated(originalManager, arguments);
		}

		private Object[] dummyArguments(
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
			Object[] arguments
		) {
			Collection<Invocation> invocations = invocationsForMethod(target);
			assertThat(invocations)
				.withFailMessage("Method was not invoked.")
				.isNotEmpty();
			assertThat(argumentsOf(invocations))
				.contains(arguments);
		}

		private Collection<Invocation> invocationsForMethod(
			SecurityManagerMock target
		) {
			return target.invocations
				.stream()
				.filter(this::matchesMethod)
				.collect(toList());
		}

		private boolean matchesMethod(Invocation invocation) {
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

	public static class security_managers_public_non_void_methods {
		private final SecurityManagerMock originalSecurityManager
			= new SecurityManagerMock();

		@Test
		public void getInCheck_is_delegated_to_original_security_manager(
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
		public void security_context_of_original_security_manager_is_provided(
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
		public void checkTopLevelWindow_is_delegated_to_original_security_manager(
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
		public void thread_group_of_original_security_manager_is_provided(
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

	private static boolean notChangedByNoExitSecurityManager(Method method) {
		return !method.getName().equals("checkExit");
	}

	private static boolean notDeclaredByObjectClass(Method method) {
		return !method.getDeclaringClass().equals(Object.class);
	}

	private static String testName(Method method) {
		return method.getName()
			+ "(" + join(method.getParameterTypes()) + ")";
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
}
