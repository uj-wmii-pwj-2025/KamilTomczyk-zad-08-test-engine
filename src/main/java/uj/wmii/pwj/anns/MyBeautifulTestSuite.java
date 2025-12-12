package uj.wmii.pwj.anns;

public class MyBeautifulTestSuite {

    @MyTest
    public void simpleTest() {
        System.out.println("      ...executing simple logic...");
    }

    @MyTest(scenarios = {
            @TestCase(params = "1", expected = "2"),  // 1 + 1 = 2 (PASS)
            @TestCase(params = "5", expected = "6"),  // 5 + 1 = 6 (PASS)
            @TestCase(params = "10", expected = "99") // 10 + 1 != 99 (FAIL)
    })
    public int addOne(int number) {
        return number + 1;
    }


    @MyTest(scenarios = {
            @TestCase(params = "java", expected = "JAVA"),
            @TestCase(params = "test", expected = "TEST")
    })
    public String toUpperCase(String input) {
        return input.toUpperCase();
    }


    @MyTest
    public void crashTest() {
        throw new RuntimeException("Something went wrong inside the test!");
    }


    public void notATest() {
        System.out.println("I should not run.");
    }
}