/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2009 - Javolution (http://javolution.org/)
 * All rights reserved.
 *
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package _templates.javolution.testing;

import _templates.javolution.context.Context;
import _templates.java.lang.CharSequence;
import _templates.javolution.text.Text;
import _templates.javolution.lang.Reflection;

/**
 * <p> This class represents a test context forwarding events to the
 *     JUnit framework (e.g. asserts). Its purpose is to facilitate
 *     test integration with JUnit. For example:[code]
 *     public class JavolutionTest extends junit.framework.TestSuite {
 *         public static junit.framework.Test suite() {
 *             JavolutionTest suite = new JavolutionTest();
 *             for (TestCase test : new TypeFormatTestSuite().tests()) {
 *                suite.addTest(new JUnitTestCase(test));
 *             }
 *             ... // Adds more test cases from Javolution test suites.
 *             return suite;
 *         }
 *         public static class JUnitTestCase extends junit.framework.TestCase {
 *             private final TestCase _test;
 *             public JUnitTestCase(TestCase test) {
 *                 _test = test;
 *             }
 *             public String getName() {
 *                 return _test.getName();
 *             }
 *             protected void runTest() throws Exception {
 *                 JUnitContext.enter();
 *                 try {
 *                     JUnitContext.run(_test);
 *                 } finally {
 *                    JUnitContext.exit();
 *                 }
 *             }
 *         }
 *     }[/code]</p>
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 5.3, March 21, 2009
 */
public class JUnitContext extends TestContext {

    /**
     * Enters a JUnit test context. This context raises a
     * <code>junit.framework.AssertionFailedError</code> if an assert
     * fails.
     */
    public static void enter() {
        Context.enter(JUnitContext.class);
    }

    /**
     * Exits the current JUnit test context.
     *
     * @throws ClassCastException if the current context is not a JUnit context.
     */
    public static void exit() {
         Context.exit(JUnitContext.class);
    }

    protected void doRun(TestSuite testSuite) throws Exception {
       logMessage("test", Text.valueOf("---------------------------------------------------"));
        logMessage("test", Text.valueOf("Executes Test Suite: ").plus(testSuite.getName()));
        logMessage("test", Text.valueOf(""));
        super.doRun(testSuite);
    }

    protected void doRun(TestCase testCase) throws Exception {
        if (testCase.isIgnored()) {
            logWarning(Text.valueOf("Ignore ").plus(testCase.getName()));
            return;
        }
        logMessage("test", Text.valueOf(testCase.getName()));
        super.doRun(testCase);
    }

    protected boolean doAssert(boolean value, CharSequence message) {
        if (!value) {
            super.doAssert(value, message); // Logs error.
            if (JUNIT_ERROR_CONSTRUCTOR != null) {
                RuntimeException junitError
                        = (RuntimeException) JUNIT_ERROR_CONSTRUCTOR.newInstance(message.toString());
                throw junitError;
            } else {
                throw new AssertionException(message.toString());
            }
        }
        return true;
    }
    private static Reflection.Constructor JUNIT_ERROR_CONSTRUCTOR 
            = Reflection.getInstance().getConstructor("junit.framework.AssertionFailedError(String)");


    protected void logMessage(String category, CharSequence message) {
        if (category.equals("error")) {
            System.err.print("[");
            System.err.print(category);
            System.err.print("] ");
            System.err.println(message);
            System.err.flush();
        } else {
            System.out.print("[");
            System.out.print(category);
            System.out.print("] ");
            System.out.println(message);
            System.out.flush();
        }
    }
}