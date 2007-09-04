/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2007 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.testing;

import j2me.lang.CharSequence;
import javolution.context.LogContext;
import javolution.context.ObjectFactory;
import javolution.text.TextBuilder;

/**
 * <p> This class represents a logging context specialized for testing.</p>
 * 
 * <p> Custom implementations may output results in varied form (e.g. tabular) 
 *     and/or perform all kind of measurements (e.g. {@link TimeContext timing},
 *     memory usage, etc.) For example:[code]
 *     TestContext tabularLog = new TestContext() { ... } // Outputs to spreadsheet.
 *     TestContext.enter(tabularLog);
 *     try {
 *         TestContext.run(new myTestSuite());
 *         ...
 *     } finally {
 *         TestContext.exit();
 *     }[/code] </p>  
 *     
 * <p> For automatic regression tests, developers may use the 
 *     {@link #REGRESSION} implementation which does not perform any 
 *     logging but raises an {@link AssertionException} when an assertion fails. 
 *     For example:[code]
 *     TestContext.enter(TestContext.REGRESSION);
 *     try {
 *         TestContext.run(new myTestSuite()); // AssertionError is one assertion fails.
 *         ...
 *     } finally {
 *         TestContext.exit();
 *     }[/code] </p>  
 *             
 * 
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 5.2, August 5, 2007
 * @see    TestCase
 */
public abstract class TestContext extends LogContext {

    /**
     * Holds an implementation which does not perform any logging but raises 
     * an {@link AssertionException} when an assertion fails.
     * This implementation can be used for automatic regression tests.
     */
    public static final Class/*<TestContext>*/REGRESSION = Regression.CLASS;

    /**
     * Runs an individual test case (possibly multiple times) and logs the
     * results. If the current logging context is a test context then 
     * {@link TestContext#doTest(TestCase)} is called; otherwise the name of 
     * test case is logged (info), the test case is excuted once   
     * and the test results are written to the current logging context.
     *  
     * @param testCase the test case being executed.
     */
    public static void test(TestCase testCase) {
        LogContext ctx = (LogContext) LogContext.getCurrent();
        if (ctx instanceof TestContext) {
            ((TestContext)ctx).doTest(testCase);
        } else { // Not a test context.
            if (ctx.isInfoLogged()) {
                ctx.logInfo(testCase.toString());
            }
            testCase.prepare();
            try {
                testCase.execute();
                testCase.validate();
            } finally {
                testCase.cleanup();
            }
        }
    }

    /**
     * Checks the equality of both objects specified. If the current logging 
     * context is a test context then 
     * {@link TestContext#doAssertEquals(String, Object, Object)} is called; 
     * otherwise an error message is logged only if the assert fails.
     * 
     * @param message the message displayed if assert fails (can be <code>null</code>)
     * @param expected the expected result (can be <code>null</code>).
     * @param actual the actual result (can be <code>null</code>).
     * @return <code>true</code> if both expected and actual are equal;
     *         <code>false</code> otherwise.
     */
    public static boolean assertEquals(String message, Object expected,
            Object actual) {
        LogContext ctx = (LogContext) LogContext.getCurrent();
        if (ctx instanceof TestContext) {
            return ((TestContext)ctx).doAssertEquals(message, expected, actual);
        } else { // Not a test context.
            if (((expected == null) && (actual != null))
                    || ((expected != null) && (!expected.equals(actual)))) {
                if (ctx.isErrorLogged()) {
                    TextBuilder tmp = TextBuilder.newInstance();
                    if (message != null) {
                        tmp.append(message).append(": " );
                    }
                    tmp.append(expected);
                    tmp.append(" expected but found ");
                    tmp.append(actual);
                    ctx.logError(null, tmp);
                    TextBuilder.recycle(tmp);
                }
                return false;
            } 
            return true;
        }
    }

    /**
     * Convenience method equivalent to <code>assertEquals(null, expected, value)</code>.
     * 
     * @param expected the expected result (can be <code>null</code>).
     * @param actual the actual result (can be <code>null</code>).
     */
    public static boolean assertEquals(Object expected, Object actual) {
        return TestContext.assertEquals(null, expected, actual);
    }

    /**
     * Convenience method equivalent to <code>assertEquals(message, true, value)</code>.
     * 
     * @param message the message displayed if assert fails (can be <code>null</code>)
     * @param actual the actual value.
     * @return <code>true</code> if actual is <code>true</code>;
     *         <code>false</code> otherwise.
     */
    public static boolean assertTrue(String message, boolean actual) {
        return TestContext.assertEquals(message, TRUE, actual ? TRUE : FALSE);
    }
    private static final Boolean TRUE = new Boolean(true);
    private static final Boolean FALSE = new Boolean(false);
    
    /**
     * Convenience method equivalent to <code>assertEquals(null, true, value)</code>.
     * 
     * @param actual the actual value.
     * @return <code>true</code> if actual is <code>true</code>;
     *         <code>false</code> otherwise.
     */
    public static boolean assertTrue(boolean actual) {
        return TestContext.assertEquals(null, TRUE, actual ? TRUE : FALSE);
    }
    
    /**
     * Executes the specified test case and logs the results.
     * 
     * @param testCase the test case being executed.
     */
    public abstract void doTest(TestCase testCase);

    /**
     * Asserts that two objects are equal.
     * 
     * @param message the message displayed if assert fails (can be <code>null</code>)
     * @param expected the expected result (can be <code>null</code>).
     * @param actual the actual result (can be <code>null</code>).
     * @return <code>true</code> if both expected and actual are equal;
     *         <code>false</code> otherwise.
     */
    public abstract boolean doAssertEquals(String message, Object expected,
            Object actual);        
   
    
    // TestContext implementation with no output (just validation).
    private static final class Regression extends TestContext {
        
        private static final Class CLASS = new Regression().getClass();

        // Overrides.
        public void doTest(TestCase testCase) {
            testCase.prepare();
            try {
                testCase.execute();
                testCase.validate();
            } finally {
                testCase.cleanup();
            }
        }

        // Overrides.
        public boolean doAssertEquals(String message, Object expected, Object actual) {
            if (((expected == null) && (actual != null))
                    || ((expected != null) && (!expected.equals(actual)))) 
                throw new AssertionException(message, expected, actual);  
            return true;
        }

        public boolean isErrorLogged() {
            return false;
        }

        public boolean isInfoLogged() {
            return false;
        }

        public boolean isWarningLogged() {
            return false;
        }

        public void logError(Throwable error, CharSequence message) {
        }

        public void logInfo(CharSequence message) {
        }

        public void logWarning(CharSequence message) {
        }        
    }
    

    // Allows instances of private classes to be factory produced. 
    static {
        ObjectFactory.setInstance(new ObjectFactory() {
            protected Object create() {
                return new Regression();
            } }, Regression.CLASS);
    }        
    
}