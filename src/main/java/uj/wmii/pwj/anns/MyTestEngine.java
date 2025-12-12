package uj.wmii.pwj.anns;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class MyTestEngine {

    private static final String RESET = "\u001B[0m";
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String CYAN = "\u001B[36m";

    public static void main(String[] args) {
        String className;
        if (args.length < 1) {
            className = "uj.wmii.pwj.anns.MyBeautifulTestSuite";
        } else {
            className = args[0].trim();
        }

        printBanner();
        System.out.println(CYAN + "Loading class: " + className + "..." + RESET);

        MyTestEngine engine = new MyTestEngine(className);
        engine.runTests();
    }

    private final String className;

    public MyTestEngine(String className) {
        this.className = className;
    }

    public void runTests() {
        Object unit = getObject(className);
        if (unit == null) return;

        List<Method> testMethods = getTestMethods(unit);

        System.out.printf("Found %d test methods.\n", testMethods.size());
        System.out.println("-------------------------------------------------------");

        int totalRun = 0;
        int passCount = 0;
        int failCount = 0;
        int errorCount = 0;

        for (Method m : testMethods) {
            MyTest annotation = m.getAnnotation(MyTest.class);
            TestCase[] scenarios = annotation.scenarios();

            if (scenarios.length == 0) {
                // Test bez parametrów
                TestResult result = executeTest(m, unit, null, null);
                totalRun++;
                if (result == TestResult.PASS) passCount++;
                else if (result == TestResult.FAIL) failCount++;
                else errorCount++;
            } else {
                // Test z parametrami (scenariusze)
                System.out.println(CYAN + "Running parameterized test: " + m.getName() + RESET);
                for (TestCase scenario : scenarios) {
                    TestResult result = executeTest(m, unit, scenario.params(), scenario.expected());
                    totalRun++;
                    if (result == TestResult.PASS) passCount++;
                    else if (result == TestResult.FAIL) failCount++;
                    else errorCount++;
                }
            }
        }

        printSummary(totalRun, passCount, failCount, errorCount);
    }

    private TestResult executeTest(Method m, Object unit, String param, String expected) {
        String testName = m.getName() + (param != null ? "(" + param + ")" : "()");
        System.out.print("  Test: " + String.format("%-30s", testName));

        try {
            Object result;

            // Logika wywołania z parametrem lub bez
            if (param != null) {
                // Prosta konwersja typów (obsługa String, int, boolean)
                Class<?>[] paramTypes = m.getParameterTypes();
                Object typedParam = convertType(param, paramTypes[0]);
                result = m.invoke(unit, typedParam);
            } else {
                result = m.invoke(unit);
            }

            // Weryfikacja wyniku
            if (expected != null) {
                String actualString = String.valueOf(result);
                if (expected.equals(actualString)) {
                    System.out.println(GREEN + "[PASS]" + RESET);
                    return TestResult.PASS;
                } else {
                    System.out.println(RED + "[FAIL]" + RESET);
                    System.out.printf(RED + "    -> Expected: '%s', Actual: '%s'\n" + RESET, expected, actualString);
                    return TestResult.FAIL;
                }
            } else {
                // Jeśli nie oczekujemy wyniku (np. void), sam brak wyjątku oznacza sukces
                System.out.println(GREEN + "[PASS]" + RESET);
                return TestResult.PASS;
            }

        } catch (InvocationTargetException e) {
            // Wyjątek rzucony przez samą metodę testową
            System.out.println(YELLOW + "[ERROR]" + RESET);
            System.out.printf(YELLOW + "    -> Exception thrown: %s\n" + RESET, e.getCause().getClass().getSimpleName());
            return TestResult.ERROR;
        } catch (Exception e) {
            // Problemy techniczne (np. zła definicja testu, brak metody)
            System.out.println(RED + "[ENGINE ERROR] " + e.getMessage() + RESET);
            return TestResult.ERROR;
        }
    }

    private Object convertType(String value, Class<?> targetType) {
        if (targetType == String.class) return value;
        if (targetType == int.class || targetType == Integer.class) return Integer.parseInt(value);
        if (targetType == double.class || targetType == Double.class) return Double.parseDouble(value);
        if (targetType == boolean.class || targetType == Boolean.class) return Boolean.parseBoolean(value);
        return value;
    }

    private static List<Method> getTestMethods(Object unit) {
        Method[] methods = unit.getClass().getDeclaredMethods();
        List<Method> list = new ArrayList<>(Arrays.asList(methods));
        list.removeIf(m -> m.getAnnotation(MyTest.class) == null);
        list.sort(Comparator.comparing(Method::getName));
        return list;
    }

    private static Object getObject(String className) {
        try {
            Class<?> unitClass = Class.forName(className);
            return unitClass.getConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            System.out.println(RED + "CRITICAL: Could not instantiate class " + className + RESET);
            e.printStackTrace();
            return null;
        }
    }

    private static void printBanner() {
        System.out.println(CYAN);
        System.out.println("  _______        _     ______             _            ");
        System.out.println(" |__   __|      | |   |  ____|           (_)           ");
        System.out.println("    | | ___  ___| |_  | |__   _ __   __ _ _ _ __   ___ ");
        System.out.println("    | |/ _ \\/ __| __| |  __| | '_ \\ / _` | | '_ \\ / _ \\");
        System.out.println("    | |  __/\\__ \\ |_  | |____| | | | (_| | | | | |  __/");
        System.out.println("    |_|\\___||___/\\__| |______|_| |_|\\__, |_|_| |_|\\___|");
        System.out.println("                                     __/ |             ");
        System.out.println("                                    |___/              ");
        System.out.println(RESET);
    }

    private void printSummary(int total, int pass, int fail, int error) {
        System.out.println("-------------------------------------------------------");
        System.out.println("SUMMARY:");
        System.out.printf("Total Scenarios: %d\n", total);
        System.out.printf(GREEN + "Passed:          %d\n" + RESET, pass);
        System.out.printf(RED + "Failed:          %d\n" + RESET, fail);
        System.out.printf(YELLOW + "Errors:          %d\n" + RESET, error);
        System.out.println("-------------------------------------------------------");
    }
}