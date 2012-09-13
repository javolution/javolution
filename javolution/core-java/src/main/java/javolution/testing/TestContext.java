/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2007 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.testing;

import java.lang.CharSequence;

import javolution.context.Context;
import javolution.context.LogContext;
import javolution.context.ObjectFactory;
import javolution.lang.Configurable;
import javolution.lang.MathLib;
import javolution.text.Text;
import javolution.util.FastTable;


/**
 * <p> This class represents a logging context specialized for testing.</p>
 *
 * <p> A test context is necessary to run a {@link TestSuite} or
 *     {@link TestCase}. The {@link #DEFAULT default} test context sends results
 *     to <code>System.out</code> and errors to <code>System.err</code>.[code]
 *     TestContext.enter(); // Enters default (logs to System.out/System.err)
 *     try {
 *         TestContext.run(testSuite);
 *         TestContext.run(testSuite.tests().get(3)); // Runs specific test case.
 *         ...
 *    } finally {
 *         TestContext.exit(); // Outputs test results statistics.
 *    }[/code] </p>
 * 
 * <p> Users may provide their own test context (or plugin) to output or
 *     show test results in various form (e.g. tabular, IDE integraged).[code]
 *     TestContext tabularLog = new TestContext() { ... } 
 *     TestContext.enter(tabularLog);
 *     try {
 *         TestContext.run(testSuite); // Results sent to spreadsheet.
 *         ...
 *     } finally {
 *         TestContext.exit();
 *     }[/code] </p>  
 *     
 * <p> For automatic regression tests, a {@link #REGRESSION regression}
 *     context is provided which does not perform any logging but raises an
 *     {@link AssertionException} when an assertion fails.[code]
 *     TestContext.enter(TestContext.REGRESSION);
 *     try {
 *         TestContext.run(testSuite); // AssertionError if assert fails.
 *         ...
 *     } finally {
 *         TestContext.exit();
 *     }[/code]</p>
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 5.3, March 8, 2009
 * @see    TestSuite
 */
public abstract class TestContext extends LogContext {

    /**
     * Holds the test context default implementation (the test report is sent to
     * to <code>System.out</code> and test errors are sent to <code>System.err</code>).
     */
    public static final Configurable <Class<? extends TestContext>>  DEFAULT 
            = new Configurable(Default.class) {};
    /**
     * Holds a test context logging test results to the system console.
     */
    public static final Class <? extends LogContext>  CONSOLE = Console.class;
    /**
     * Holds an implementation which does not perform any logging but raises 
     * an {@link AssertionException} when an assertion fails.
     * This implementation can be used for automatic regression tests.
     */
    public static final Class <? extends TestContext>  REGRESSION = Regression.class;

    /**
     * Enters the {@link #DEFAULT} test context.
     */
    public static void enter() {
        Context.enter((Class) DEFAULT.get());
    }

    /**
     * Exits the current test context.
     *
     * @throws ClassCastException if the current context is not a test context.
     */
    public static void exit() {
         Context.exit(TestContext.class);
    }

    /**
     * Executes the specified test suite and logs the results to the
     * current test context.
     *
     * @param testSuite the test suite to be executed.
     * @throws ClassCastException if the current logging context is not a test
     *         context.
     */
    public static void run(TestSuite testSuite) throws Exception {
        TestContext testContext = (TestContext) LogContext.getCurrentLogContext();
        testContext.doRun(testSuite);        
    }

    /**
     * Executes the specified test case and logs the results to the current
     * test context.
     *  
     * @param testCase the test case to be executed.
     * @throws ClassCastException if the current logging context is not a test
     *         context.
     */
    public static void run(TestCase testCase) throws Exception {
        TestContext testContext = (TestContext) LogContext.getCurrentLogContext();
        testContext.doRun(testCase);
    }

    /**
     * Checks the equality of both objects specified.
     *
     * @param expected the expected result (can be <code>null</code>).
     * @param actual the actual result (can be <code>null</code>).
     * @param message the message displayed if assert fails or <code>null</code> if none.
     * @return <code>true</code> if both expected and actual are equal;
     *         <code>false</code> otherwise.
     */
    public static boolean assertEquals(Object expected,
            Object actual, CharSequence message) {
        boolean ok = ((expected == null) && (actual == null)) || ((expected != null) && (expected.equals(actual)));
        message = (ok || (message != null)) ? message : // Provides error message if necessary.
                Text.valueOf(expected).plus(" expected but found ").plus(actual);
        TestContext ctx = (TestContext) LogContext.getCurrentLogContext();
        return ctx.doAssert(ok, message);
    }

    /**
     * Convenience method equivalent to <code>assertEquals(expected, actual, null)</code>.
     * 
     * @param expected the expected result (can be <code>null</code>).
     * @param actual the actual result (can be <code>null</code>).
     */
    public static boolean assertEquals(Object expected, Object actual) {
        return TestContext.assertEquals(expected, actual, null);
    }

    /**
     * Checks that both objects specified refer to the same instance.
     *
     * @param expected the expected result (can be <code>null</code>).
     * @param actual the actual result (can be <code>null</code>).
     * @param message the message displayed if assert fails or <code>null</code> if none.
     * @return <code>true</code> if both expected and actual are the same
     *         object; <code>false</code> otherwise.
     */
    public static boolean assertSame(Object expected, Object actual, CharSequence message) {
        boolean ok = (expected == actual);
        message = (ok || (message != null)) ? message : // Provides error message if necessary.
                Text.valueOf(expected).plus(" expected but found a different instance ").plus(actual);
        TestContext ctx = (TestContext) LogContext.getCurrentLogContext();
        return ctx.doAssert(ok, message);
    }

    /**
     * Convenience method equivalent to <code>assertSame(expected, actual, null)</code>.
     *
     * @param expected the expected result (can be <code>null</code>).
     * @param actual the actual result (can be <code>null</code>).
     * @return <code>true</code> if both expected and actual are the same
     *         object; <code>false</code> otherwise.
     */
    public static boolean assertSame(Object expected, Object actual) {
        return TestContext.assertSame(expected, actual, null);
    }

    /**
     * Convenience method equivalent to <code>assertEquals(true, actual, message)</code>.
     * 
     * @param actual the actual value.
     * @param message the message displayed if assert fails or <code>null</code> if none.
     * @return <code>true</code> if actual is <code>true</code>;
     *         <code>false</code> otherwise.
     */
    public static boolean assertTrue(boolean actual, CharSequence message) {
        return TestContext.assertEquals(Boolean.TRUE, actual ? Boolean.TRUE : Boolean.FALSE, message);
    }

    /**
     * Convenience method equivalent to <code>assertTrue(actual, null)</code>.
     * 
     * @param actual the actual value.
     * @return <code>true</code> if actual is <code>true</code>;
     *         <code>false</code> otherwise.
     */
    public static boolean assertTrue(boolean actual) {
        return TestContext.assertTrue(actual, null);
    }

    /**
     * Convenience method equivalent to <code>assertEquals(false, actual, message)</code>.
     *
     * @param actual the actual value.
     * @param message the message displayed if assert fails or <code>null</code> if none.
     * @return <code>true</code> if actual is <code>false</code>;
     *         <code>false</code> otherwise.
     */
    public static boolean assertFalse(boolean actual, CharSequence message) {
        return TestContext.assertEquals(Boolean.FALSE, actual ? Boolean.TRUE : Boolean.FALSE, message);
    }

    /**
     * Convenience method equivalent to <code>assertFalse(actual, null)</code>.
     *
     * @param actual the actual value.
     * @return <code>true</code> if actual is <code>false</code>;
     *         <code>false</code> otherwise.
     */
    public static boolean assertFalse(boolean actual) {
        return TestContext.assertFalse(actual, null);
    }

    /**
     * Convenience method equivalent to <code>assertEquals(null, actual, message)</code>.
     *
     * @param actual the actual value.
     * @param message the message displayed if assert fails or <code>null</code> if none.
     * @return <code>true</code> if actual is <code>null</code>;
     *         <code>false</code> otherwise.
     */
    public static boolean assertNull(Object actual, CharSequence message) {
        return TestContext.assertEquals(null, actual, message);
    }

    /**
     * Convenience method equivalent to <code>assertNull(actual, null)</code>.
     *
     * @param actual the actual value.
     * @return <code>true</code> if actual is <code>null</code>;
     *         <code>false</code> otherwise.
     */
    public static boolean assertNull(Object actual) {
        return TestContext.assertNull(actual, null);
    }

    /**
     * Convenience method equivalent to <code>assertTrue(actual != null, message)</code>.
     *
     * @param actual the actual value.
     * @param message the message displayed if assert fails or <code>null</code> if none.
     * @return <code>true</code> if actual is not <code>null</code>;
     *         <code>false</code> otherwise.
     */
    public static boolean assertNotNull(Object actual, CharSequence message) {
        boolean ok = (actual != null);
        message = (ok || (message != null)) ? message : // Provides error message if necessary.
                Text.valueOf("Not null expected but found null");
        TestContext ctx = (TestContext) LogContext.getCurrentLogContext();
        return ctx.doAssert(ok, message);
    }

    /**
     * Convenience method equivalent to <code>assertNotNull(actual, null)</code>.
     *
     * @param actual the actual value.
     * @return <code>true</code> if actual is <code>null</code>;
     *         <code>false</code> otherwise.
     */
    public static boolean assertNotNull(Object actual) {
        return TestContext.assertNotNull(actual, null);
    }

    /**
     * Convenience method equivalent to
     * <code>assertEquals(new Integer(expected), new Integer(actual), message)</code>.
     *
     * @param expected the expected result.
     * @param actual the actual result.
     * @param message the message displayed if assert fails or <code>null</code> if none.
     * @return <code>true</code> if both expected and actual are equal;
     *         <code>false</code> otherwise.
     */
    public static boolean assertEquals(int expected, int actual, CharSequence message) {
        boolean ok = (expected == actual);
        message = (ok || (message != null)) ? message : // Provides error message if necessary.
                Text.valueOf(expected + " expected but found " + actual);
        TestContext ctx = (TestContext) LogContext.getCurrentLogContext();
        return ctx.doAssert(ok, message);
    }

    /**
     * Convenience method equivalent to
     * <code>assertEquals(expected, actual, null)</code>.
     *
     * @param expected the expected result.
     * @param actual the actual result.
     * @return <code>true</code> if both expected and actual are equal;
     *         <code>false</code> otherwise.
     */
    public static boolean assertEquals(int expected, int actual) {
        return TestContext.assertEquals(expected, actual, null);
    }

    /**
     * Convenience method equivalent to
     * <code>assertEquals(new Long(expected), new Long(actual))</code>.
     *
     * @param expected the expected result.
     * @param actual the actual result.
     * @param message the message displayed if assert fails or <code>null</code> if none.
     * @return <code>true</code> if both expected and actual are equal;
     *         <code>false</code> otherwise.
     */
    public static boolean assertEquals(long expected, long actual, CharSequence message) {
        boolean ok = (expected == actual);
        message = (ok || (message != null)) ? message : // Provides error message if necessary.
                Text.valueOf(expected + " expected but found " + actual);
        TestContext ctx = (TestContext) LogContext.getCurrentLogContext();
        return ctx.doAssert(ok, message);
    }

    /**
     * Convenience method equivalent to
     * <code>assertEquals(expected, actual, null)</code>.
     *
     * @param expected the expected result.
     * @param actual the actual result.
     * @return <code>true</code> if both expected and actual are equal;
     *         <code>false</code> otherwise.
     */
    public static boolean assertEquals(long expected, long actual) {
        return TestContext.assertEquals(expected, actual, null);
    }

    /**
     * Convenience method equivalent to
     * <code>assertEquals(new Double(expected), new Double(actual), message)</code>.
     * 
     * @param expected the expected result.
     * @param actual the actual result.
     * @param message the message displayed if assert fails or <code>null</code> if none.
     * @return <code>true</code> if both expected and actual are equal;
     *         <code>false</code> otherwise.
     * @deprecated  Use {@link #assertEquals(double, double, double, CharSequence)
     *              assertEquals(expected, actual, delta, message)}  instead
     */
    public static boolean assertEquals(double expected, double actual, CharSequence message) {
        boolean ok = (expected == actual) || (Double.isNaN(expected) && Double.isNaN(actual));
        message = (ok || (message != null)) ? message : // Provides error message if necessary.
                Text.valueOf(expected + " expected but found " + actual);
        TestContext ctx = (TestContext) LogContext.getCurrentLogContext();
        return ctx.doAssert(ok, message);
    }

    /**
     * Convenience method equivalent to
     * <code>assertEquals(expected, actual, null)</code>.
     *
     * @param expected the expected result.
     * @param actual the actual result.
     * @return <code>true</code> if both expected and actual are equal;
     *         <code>false</code> otherwise.
     * @deprecated  Use {@link #assertEquals(double, double, double)
     *              assertEquals(expected, actual, delta)}  instead
     */
    public static boolean assertEquals(double expected, double actual) {
        return TestContext.assertEquals(expected, actual, null);
    }

    /**
     * Asserts that two doubles or floats are equal to within a positive delta.
     * If the expected value is infinity then the delta value is ignored.
     * NaN numbers are considered equal.
     *
     * @param expected the expected result.
     * @param actual the actual result.
     * @param delta the maximum delta between expected and actual for which
     *        both numbers are still considered equal.
     * @param message the message displayed if assert fails or <code>null</code> if none.
     * @return <code>true</code> if both expected and actual are approximately
     *         equal; <code>false</code> otherwise.
     */
    public static boolean assertEquals(double expected, double actual, double delta, CharSequence message) {
        boolean ok = (expected == actual) || (Double.isNaN(expected) && Double.isNaN(actual));
        message = (ok || (message != null)) ? message : // Provides error message if necessary.
                Text.valueOf(expected + " expected but found " + actual);
        TestContext ctx = (TestContext) LogContext.getCurrentLogContext();
        return ctx.doAssert(ok, message);
    }

    /**
     * Convenience method equivalent to <code>assertEquals(expected, actual, delta, null)</code>.
     *
     * @param expected the expected result.
     * @param actual the actual result.
     * @param delta the maximum delta between expected and actual for which
     *        both numbers are still considered equal.
     * @return <code>true</code> if both expected and actual are approximately
     *         equal; <code>false</code> otherwise.
     */
    public static boolean assertEquals(double expected, double actual, double delta) {
        return TestContext.assertEquals(expected, actual, delta, null);
    }

    /**
     * Checks the equality of the arrays specified.
     * If expecteds and actuals are <code>null</code>, they are considered equal.
     *
     * @param expected the single dimension array
     *        with expected values (can be <code>null</code>).
     * @param actual the single dimension array
     *        with actual values (can be <code>null</code>).
     * @param message the message displayed if assert fails or <code>null</code> if none.
     * @return <code>true</code> if both expected and actual have equals
     *         elements; <code>false</code> otherwise.
     */
    public static boolean assertArrayEquals(Object[] expected,
            Object[] actual, CharSequence message) {
        TestContext ctx = (TestContext) LogContext.getCurrentLogContext();
        if (expected == actual)
            return ctx.doAssert(true, message);
        if (expected == null)
            return ctx.doAssert(false, message != null ? message : Text.valueOf("Null array expected but found actual array not null"));
        if (actual == null)
            return ctx.doAssert(false, message != null ? message : Text.valueOf("Not null array expected but found actual array null"));
        if ((expected.length != actual.length))
            return ctx.doAssert(false, message != null ? message : Text.valueOf("Array of size " + expected.length + " expected but found array of actual size " + actual.length));
        for (int i = 0; i < expected.length; i++) {
            Object e = expected[i];
            Object a = actual[i];
            if (((e != null) && !e.equals(a)) || (e != a))
                return ctx.doAssert(false, message != null ? message : Text.valueOf("Array element at " + i + ", expected " + e + " but found " + a));
        }
        return ctx.doAssert(true, message);
    }

    /**
     * Convenience method equivalent to <code>assertArrayEquals(expected, actual, null)</code>.
     *
     * @param expected the single dimension array
     *        with expected values (can be <code>null</code>).
     * @param actual the single dimension array
     *        with actual values (can be <code>null</code>).
     */
    public static boolean assertArrayEquals(Object[] expected, Object[] actual) {
        return TestContext.assertArrayEquals(expected, actual, null);
    }

    /**
     * Checks the equality of both arrays specified.
     * If expecteds and actuals are <code>null</code>, they are considered equal.
     *
     * @param expected array (can be <code>null</code>).
     * @param actual array (can be <code>null</code>).
     * @param message the message displayed if assert fails or <code>null</code> if none.
     * @return <code>true</code> if both expected and actual have the same
     *        values; <code>false</code> otherwise.
     */
    public static boolean assertArrayEquals(boolean[] expected,
            boolean[] actual, CharSequence message) {
        TestContext ctx = (TestContext) LogContext.getCurrentLogContext();
        if (expected == actual)
            return ctx.doAssert(true, message);
        if (expected == null)
            return ctx.doAssert(false, message != null ? message : Text.valueOf("Null array expected but found actual array not null"));
        if (actual == null)
            return ctx.doAssert(false, message != null ? message : Text.valueOf("Not null array expected but found actual array null"));
        if ((expected.length != actual.length))
            return ctx.doAssert(false, message != null ? message : Text.valueOf("Array of size " + expected.length + " expected but found array of actual size " + actual.length));
        for (int i = 0; i < expected.length; i++) {
            boolean e = expected[i];
            boolean a = actual[i];
            if (e != a)
                return ctx.doAssert(false, message != null ? message : Text.valueOf("Array element at " + i + ", expected " + e + " but found " + a));
        }
        return ctx.doAssert(true, message);
    }

    /**
     * Convenience method equivalent to <code>assertArrayEquals(expected, actual, null)</code>.
     *
     * @param expected array (can be <code>null</code>).
     * @param actual array (can be <code>null</code>).
     * @return <code>true</code> if both expected and actual have the same
     *        values; <code>false</code> otherwise.
     */
    public static boolean assertArrayEquals(boolean[] expected, boolean[] actual) {
        return TestContext.assertArrayEquals(expected, actual, null);
    }

    /**
     * Checks the equality of both arrays specified.
     * If expecteds and actuals are <code>null</code>, they are considered equal.
     *
     * @param expected array (can be <code>null</code>).
     * @param actual array (can be <code>null</code>).
     * @param message the message displayed if assert fails or <code>null</code> if none.
     * @return <code>true</code> if both expected and actual have the same
     *        values; <code>false</code> otherwise.
     */
    public static boolean assertArrayEquals(int[] expected,
            int[] actual, CharSequence message) {
        TestContext ctx = (TestContext) LogContext.getCurrentLogContext();
        if (expected == actual)
            return ctx.doAssert(true, message);
        if (expected == null)
            return ctx.doAssert(false, message != null ? message : Text.valueOf("Null array expected but found actual array not null"));
        if (actual == null)
            return ctx.doAssert(false, message != null ? message : Text.valueOf("Not null array expected but found actual array null"));
        if ((expected.length != actual.length))
            return ctx.doAssert(false, message != null ? message : Text.valueOf("Array of size " + expected.length + " expected but found array of actual size " + actual.length));
        for (int i = 0; i < expected.length; i++) {
            int e = expected[i];
            int a = actual[i];
            if (e != a)
                return ctx.doAssert(false, message != null ? message : Text.valueOf("Array element at " + i + ", expected " + e + " but found " + a));
        }
        return ctx.doAssert(true, message);
    }

    /**
     * Convenience method equivalent to <code>assertArrayEquals(expected, value, null)</code>.
     *
     * @param expected array (can be <code>null</code>).
     * @param actual array (can be <code>null</code>).
     * @return <code>true</code> if both expected and actual have the same
     *        values; <code>false</code> otherwise.
     */
    public static boolean assertArrayEquals(int[] expected, int[] actual) {
        return TestContext.assertArrayEquals(expected, actual, null);
    }

    /**
     * Checks the equality of both arrays specified.
     * If expecteds and actuals are <code>null</code>, they are considered equal.
     *
     * @param expected array (can be <code>null</code>).
     * @param actual array (can be <code>null</code>).
     * @param message the message displayed if assert fails or <code>null</code> if none.
     * @return <code>true</code> if both expected and actual have the same
     *        values; <code>false</code> otherwise.
     */
    public static boolean assertArrayEquals(long[] expected,
            long[] actual, CharSequence message) {
        TestContext ctx = (TestContext) LogContext.getCurrentLogContext();
        if (expected == actual)
            return ctx.doAssert(true, message);
        if (expected == null)
            return ctx.doAssert(false, message != null ? message : Text.valueOf("Null array expected but found actual array not null"));
        if (actual == null)
            return ctx.doAssert(false, message != null ? message : Text.valueOf("Not null array expected but found actual array null"));
        if ((expected.length != actual.length))
            return ctx.doAssert(false, message != null ? message : Text.valueOf("Array of size " + expected.length + " expected but found array of actual size " + actual.length));
        for (int i = 0; i < expected.length; i++) {
            long e = expected[i];
            long a = actual[i];
            if (e != a)
                return ctx.doAssert(false, message != null ? message : Text.valueOf("Array element at " + i + ", expected " + e + " but found " + a));
        }
        return ctx.doAssert(true, message);
    }

    /**
     * Convenience method equivalent to <code>assertArrayEquals(expected, value, null)</code>.
     *
     * @param expected array (can be <code>null</code>).
     * @param actual array (can be <code>null</code>).
     * @return <code>true</code> if both expected and actual have the same
     *        values; <code>false</code> otherwise.
     */
    public static boolean assertArrayEquals(long[] expected, long[] actual) {
        return TestContext.assertArrayEquals(expected, actual, null);
    }

    /**
     * Checks the equality of both arrays specified.
     * If expecteds and actuals are <code>null</code>, they are considered equal.
     *
     * @param expected array (can be <code>null</code>).
     * @param actual array (can be <code>null</code>).
     * @param delta the maximum delta between expected and actual for which
     *        both numbers are still considered equal.
     * @param message the message displayed if assert fails or <code>null</code> if none.
     * @return <code>true</code> if both expected and actual have approximately
     *        the same values; <code>false</code> otherwise.
     */
    public static boolean assertArrayEquals(double[] expected,
            double[] actual, double delta, CharSequence message) {
        TestContext ctx = (TestContext) LogContext.getCurrentLogContext();
        if (expected == actual)
            return ctx.doAssert(true, message);
        if (expected == null)
            return ctx.doAssert(false, message != null ? message : Text.valueOf("Null array expected but found actual array not null"));
        if (actual == null)
            return ctx.doAssert(false, message != null ? message : Text.valueOf("Not null array expected but found actual array null"));
        if ((expected.length != actual.length))
            return ctx.doAssert(false, message != null ? message : Text.valueOf("Array of size " + expected.length + " expected but found array of actual size " + actual.length));
        for (int i = 0; i < expected.length; i++) {
            double e = expected[i];
            double a = actual[i];
            if (MathLib.abs(e - a) > delta)
                return ctx.doAssert(false, message != null ? message : Text.valueOf("Array element at " + i + ", expected " + e + " but found " + a));
        }
        return ctx.doAssert(true, message);
    }

    /**
     * Convenience method equivalent to
     * <code>assertArrayEquals(expected, actual, delta, null)</code>.
     *
     * @param expected array (can be <code>null</code>).
     * @param actual array (can be <code>null</code>).
     * @param delta the maximum delta between expected and actual for which
     *        both numbers are still considered equal.
     * @return <code>true</code> if both expected and actual have the same
     *        values; <code>false</code> otherwise.
     */
    public static boolean assertArrayEquals(double[] expected, double[] actual, double delta) {
        return TestContext.assertArrayEquals(expected, actual, delta, null);
    }

    /**
     * Checks that the specified logic raises an instance of the specified
     * exception.
     *
     * @param exceptionClass the type of exception expected.
     * @param logic the logic supposed to produce the desired exception.
     * @param message the message displayed if assert fails or <code>null</code> if none.
     * @return <code>true</code> if the specified logic raises an exception of
     *        specified type; <code>false</code> otherwise.
     */
    public static boolean assertException(Class exceptionClass, Runnable logic, CharSequence message) {
        Throwable exception = null;
        try {
            logic.run();
        } catch (Throwable e) {
            exception = e;
        }
        TestContext ctx = (TestContext) LogContext.getCurrentLogContext();
        if (exception == null)
            return ctx.doAssert(false, message != null ? message : Text.valueOf("Expected exception instance of ").plus(exceptionClass.getName()).plus(" but no exception has been raised"));
        boolean ok = exceptionClass.isInstance(exception);
        message = (ok || (message != null)) ? message : // Provides error message if necessary.
                Text.valueOf("Expected instance of ").plus(exceptionClass.getName()).plus(
                " but actual exception is instance of ").plus(exception.getClass().getName());
        return ctx.doAssert(ok, message);
    }

    /**
     * Convenience method equivalent to 
     * <code>assertException(exceptionClass, logic, null)</code>.
     */
    public static boolean assertException(Class exceptionClass, Runnable logic) {
        return TestContext.assertException(exceptionClass, logic, null);
    }

    /**
     * Convenience method equivalent to <code>assertTrue(false, message)</code>.
     */
    public static boolean fail(CharSequence message) {
        return TestContext.assertTrue(false, message);
    }

    /**
     * Convenience method equivalent to <code>fail(null)</code>.
     */
    public static boolean fail() {
        return TestContext.fail(null);
    }

    /**
     * Executes the specified test suite and logs the results.
     * The default implementation runs all the test cases.
     *
     * @param testSuite the test suite to be executed.
     * @see #doRun(javolution.testing.TestCase)
     */
    protected void doRun(TestSuite testSuite) throws Exception {
        testSuite.setUp();
        try {
            FastTable tests = testSuite._tests;
            for (int i = 0; i < tests.size(); i++) {
                doRun((TestCase) tests.get(i));
            }
        } finally {
            testSuite.tearDown();
        }
    }

    /**
     * Executes the specified test case and logs the results.
     * If the test case is not marked {@link TestCase#isIgnored() ignored},
     * the default implementation runs {@link TestCase#setUp() setUp},
     * {@link TestCase#execute() execute}, {@link TestCase#setUp() validate}
     * and {@link TestCase#setUp() tearDown} in sequence.
     * 
     * @param testCase the test case being executed if not marked ignored.
     */
    protected void doRun(TestCase testCase) throws Exception {
        if (testCase.isIgnored())
            return;
        testCase.setUp();
        try {
            testCase.execute();
            testCase.validate();
        } finally {
            testCase.tearDown();
        }
    }

    /**
     * Asserts the specified value is <code>true</code>.
     * The default implementation logs an error message including the code
     * location of the assert if the assert checks fails. For example:[code]
     * [error] NaN expected but found Infinity
     *         at javolution.TextTestSuite$DoubleParseDouble.validate(TextTestSuite.java:389)
     * [/code]
     * 
     * @param value the boolean value to be tested.
     * @param message the message to be displayed if assert fails (can be <code>null</code>).
     * @return the specified value.
     */
    protected boolean doAssert(boolean value, CharSequence message) {
        if (value)
            return true;
        /**/
        Throwable error = new Error(); // To get stack trace.
        StackTraceElement[] trace = error.getStackTrace();
        javolution.text.TextBuilder tmp = javolution.text.TextBuilder.newInstance();
        try {
            tmp.append(message);
            for (int i = 1; i < trace.length; i++) {
                if (trace[i].getMethodName().equals("validate")) {
                    tmp.append("\n\tat ");
                    tmp.append(trace[i]);
                    break;
                }
            }
            logError(null, tmp);
        } finally {
            javolution.text.TextBuilder.recycle(tmp);
        }
        /**/
        return false;
    }

    // TestContext default implementation.
    private static class Default extends TestContext {

        private int _passedCount;
        private int _failedCount;
        private int _ignoredCount;
        private boolean _isPassed;

        protected void enterAction() {
            _passedCount = _failedCount = _ignoredCount = 0;
        }

        protected void exitAction() {
            logMessage("test", Text.valueOf("---------------------------------------------------"));
            logMessage("test", Text.valueOf("SUMMARY - PASSED: " + _passedCount + ", FAILED: " + _failedCount + ", IGNORED: " + _ignoredCount));
        }

        protected void doRun(TestSuite testSuite) throws Exception {
            logMessage("test", Text.valueOf("---------------------------------------------------"));
            logMessage("test", Text.valueOf("Executes Test Suite: ").plus(testSuite.getName()));
            logMessage("test", Text.valueOf(""));
            super.doRun(testSuite);
        }

        protected void doRun(TestCase testCase) {
            if (testCase.isIgnored()) {
                logWarning(Text.valueOf("Ignore ").plus(testCase.getName()));
                _ignoredCount++;
                return;
            }
            logMessage("test", Text.valueOf(testCase.getName()));
            _isPassed = true;
            try {
                super.doRun(testCase);
            } catch (Throwable error) {
                _isPassed = false;
                logError(error, null);
            } finally { // Updates statistics.
                if (_isPassed)
                    _passedCount++;
                else
                    _failedCount++;
            }
        }

        protected boolean doAssert(boolean value, CharSequence message) {
            if (!value) {
                _isPassed = false;
                return super.doAssert(value, message); // Logs error.
            }
            return value;
        }

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

    // TestContext for regression tests.
    private static class Console extends Default {
        /*@JVM-1.6+@
        final java.io.PrintWriter writer;
        Console() {
        java.io.Console console = System.console();
        writer = console != null ? console.writer() : null;
        }

        @Override
        protected void logMessage(String category, CharSequence message) {
        if (writer == null) {
        super.logMessage(category, message);
        } else {
        writer.print("[");
        writer.print(category);
        writer.print("] ");
        writer.println(message);
        }
        }
        /**/
    }

    // TestContext for regression tests.
    private static class Regression extends TestContext {

        protected boolean doAssert(boolean value, CharSequence message) {
            if (!value)
                throw new AssertionException(message.toString());
            return value;
        }

        protected boolean isLogged(String category) {
            return false;
        }

        protected void logMessage(String category, CharSequence message) {
            // Do nothing.
        }
    }

// Allows instances of private classes to be factory produced.
    static {

        ObjectFactory.setInstance(new ObjectFactory() {

            protected Object create() {
                return new Default();
            }
        }, Default.class);
        ObjectFactory.setInstance(new ObjectFactory() {

            protected Object create() {
                return new Console();
            }
        }, CONSOLE);
        ObjectFactory.setInstance(
                new ObjectFactory() {

                    protected Object create() {
                        return new Regression();
                    }
                },
                Regression.class);
    }
}