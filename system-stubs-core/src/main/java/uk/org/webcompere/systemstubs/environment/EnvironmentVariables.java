package uk.org.webcompere.systemstubs.environment;

import uk.org.webcompere.systemstubs.ThrowingRunnable;
import uk.org.webcompere.systemstubs.SystemStubs;
import uk.org.webcompere.systemstubs.resource.Resources;
import uk.org.webcompere.systemstubs.resource.SingularTestResource;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import static java.lang.Class.forName;
import static java.lang.System.getenv;
import static java.util.Collections.emptyMap;

/**
 * A collection of values for environment variables. New values can be
 * added by {@link #and(String, String)}. The {@code EnvironmentVariables}
 * object is then used to execute an arbitrary piece of code with these
 * environment variables being present.
 */
public class EnvironmentVariables extends SingularTestResource {
    protected final Map<String, String> variables;
    private Map<String, String> originalEnvironment = null;

    /**
     * Default constructor with an empty set of environment variables. Use {@link #set(String, String)} to
     * provide some, mutating this, or {@link #and(String, String)} to fork a fresh object with desired
     * environment variable settings.
     */
    public EnvironmentVariables() {
        this(emptyMap());
    }

    /**
     * Construct with an initial set of variables as name value pairs.
     * @param name first environment variable's name
     * @param value first environment variable's value
     * @param others must be of even-numbered length. Name/value pairs of the other values to
     *               apply to the environment when this object is active
     */
    public EnvironmentVariables(String name, String value, String ... others) {
        if (others.length % 2 != 0) {
            throw new IllegalArgumentException("Must provide even number of parameters");
        }
        variables = new HashMap<>();
        variables.put(name, value);
        for (int i = 0; i < others.length; i += 2) {
            variables.put(others[i], others[i + 1]);
        }
    }

    /**
     * Construct with an initial map of variables as name value pairs
     */
    public EnvironmentVariables(Map<String, String> variables) {
        this.variables = new HashMap<>(variables);
    }

    /**
     * <em>Immutable setter:</em> creates a new {@code WithEnvironmentVariables} object that
     * additionally stores the value for an additional environment variable.
     * <p>You cannot specify the value of an environment variable twice. An
     * {@code IllegalArgumentException} when you try.
     * @param name the name of the environment variable.
     * @param value the value of the environment variable.
     * @return a new {@code WithEnvironmentVariables} object.
     * @throws IllegalArgumentException when a value for the environment
     * variable {@code name} is already specified.
     * @see SystemStubs#withEnvironmentVariable(String, String)
     * @see #execute(ThrowingRunnable)
     */
    public EnvironmentVariables and(String name, String value) {
        validateNotSet(name, value);
        return new EnvironmentVariables(variables).set(name, value);
    }

    /**
     * <em>Mutable setter:</em> applies the change to the stored environment variables
     * and applies to the environment too if currently active.
     * @param name name of variable to set
     * @param value value to set
     * @return this for fluent calling
     */
    public EnvironmentVariables set(String name, String value) {
        variables.put(name, value);

        if (isActive()) {
            setEnvironmentVariables();
        }
        return this;
    }

    public Map<String, String> getVariables() {
        return variables;
    }

    private void validateNotSet(String name, String value) {
        if (variables.containsKey(name)) {
            String currentValue = variables.get(name);
            throw new IllegalArgumentException(
                "The environment variable '" + name + "' cannot be set to "
                    + format(value) + " because it was already set to "
                    + format(currentValue) + "."
            );
        }
    }

    private String format(String text) {
        if (text == null) {
            return "null";
        } else {
            return "'" + text + "'";
        }
    }

    /**
     * Executes a {@code Callable} with environment variable values
     * according to what was set before. It exposes the return value of the
     * {@code Callable}. All changes to environment variables are reverted
     * after the {@code Callable} has been executed.
     * <pre>
     * &#064;Test
     * void execute_code_with_environment_variables(
     * ) throws Exception {
     *   {@literal List<String>} values = withEnvironmentVariable("first", "first value")
     *     .and("second", "second value")
     *     .and("third", null)
     *     .execute((){@literal ->} asList(
     *         System.getenv("first"),
     *         System.getenv("second"),
     *         System.getenv("third")
     *     ));
     *   assertEquals(
     *     asList("first value", "second value", null),
     *     values
     *   );
     * }
     * </pre>
     * <p><b>Warning:</b> This method uses reflection for modifying internals of the
     * environment variables map. It fails if your {@code SecurityManager} forbids
     * such modifications.
     * @param <T> the type of {@code callable}'s result
     * @param callable an arbitrary piece of code.
     * @return the return value of {@code callable}.
     * @throws Exception any exception thrown by the callable.
     * @since 1.1.0
     * @see SystemStubs#withEnvironmentVariable(String, String)
     * @see #and(String, String)
     * @see #execute(ThrowingRunnable)
     */
    @Override
    public <T> T execute(Callable<T> callable) throws Exception {
        return super.execute(callable);
    }

    @Override
    protected void doSetup() {
        originalEnvironment = new HashMap<>(getenv());
        setEnvironmentVariables();
    }

    @Override
    protected void doTeardown() {
        restoreOriginalVariables(originalEnvironment);
    }

    /**
     * Executes a statement with environment variable values according to
     * what was set before. All changes to environment variables are
     * reverted after the statement has been executed.
     * <pre>
     * &#064;Test
     * void execute_code_with_environment_variables(
     * ) throws Exception {
     *   withEnvironmentVariable("first", "first value")
     *     .and("second", "second value")
     *     .and("third", null)
     *     .execute((){@literal ->} {
     *       assertEquals(
     *         "first value",
     *         System.getenv("first")
     *       );
     *       assertEquals(
     *         "second value",
     *         System.getenv("second")
     *       );
     *       assertNull(
     *         System.getenv("third")
     *       );
     *     });
     * }
     * </pre>
     * <p><b>Warning:</b> This method uses reflection for modifying internals of the
     * environment variables map. It fails if your {@code SecurityManager} forbids
     * such modifications.
     * @param throwingRunnable an arbitrary piece of code.
     * @throws Exception any exception thrown by the statement.
     * @since 1.0.0
     * @see SystemStubs#withEnvironmentVariable(String, String)
     * @see EnvironmentVariables#and(String, String)
     * @see #execute(Callable)
     */
    public void execute(ThrowingRunnable throwingRunnable) throws Exception {
        execute(throwingRunnable.asCallable());
    }

    private void setEnvironmentVariables() {
        overrideVariables(
            getEditableMapOfVariables()
        );
        overrideVariables(
            getTheCaseInsensitiveEnvironment()
        );
    }

    private void overrideVariables(Map<String, String> existingVariables) {
        //theCaseInsensitiveEnvironment may be null
        if (existingVariables != null) {
            variables.forEach(
                (name, value) -> setOrRemoveInMap(existingVariables, name, value)
            );
        }
    }

    private void setOrRemoveInMap(Map<String, String> variables,
                                  String name,
                                  String value) {
        if (value == null) {
            variables.remove(name);
        } else {
            variables.put(name, value);
        }
    }

    void restoreOriginalVariables(
        Map<String, String> originalVariables
    ) {
        restoreVariables(
            getEditableMapOfVariables(),
            originalVariables
        );
        restoreVariables(
            getTheCaseInsensitiveEnvironment(),
            originalVariables
        );
    }

    void restoreVariables(
        Map<String, String> variables,
        Map<String, String> originalVariables
    ) {
        if (variables != null) { //theCaseInsensitiveEnvironment may be null
            variables.clear();
            variables.putAll(originalVariables);
        }
    }

    private static Map<String, String> getEditableMapOfVariables() {
        Class<?> classOfMap = getenv().getClass();
        try {
            return getFieldValue(classOfMap, getenv(), "m");
        } catch (IllegalAccessException e) {
            throw new RuntimeException("System Rules cannot access the field"
                + " 'm' of the map System.getenv().", e);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("System Rules expects System.getenv() to"
                + " have a field 'm' but it has not.", e);
        }
    }

    /**
     * The names of environment variables are case-insensitive in Windows.
     * Therefore it stores the variables in a TreeMap named
     * theCaseInsensitiveEnvironment.
     */
    private static Map<String, String> getTheCaseInsensitiveEnvironment() {
        try {
            Class<?> processEnvironment = forName("java.lang.ProcessEnvironment");
            return getFieldValue(
                processEnvironment, null, "theCaseInsensitiveEnvironment");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("System Rules expects the existence of"
                + " the class java.lang.ProcessEnvironment but it does not"
                + " exist.", e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("System Rules cannot access the static"
                + " field 'theCaseInsensitiveEnvironment' of the class"
                + " java.lang.ProcessEnvironment.", e);
        } catch (NoSuchFieldException e) {
            //this field is only available for Windows
            return null;
        }
    }

    private static Map<String, String> getFieldValue(
        Class<?> klass,
        Object object,
        String name
    ) throws NoSuchFieldException, IllegalAccessException {
        Field field = klass.getDeclaredField(name);
        field.setAccessible(true);
        return (Map<String, String>) field.get(object);
    }
}