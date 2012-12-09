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

import javolution.context.AbstractContext;
import javolution.context.LogContext;
import javolution.context.ObjectFactory;
import javolution.lang.Configurable;
import javolution.lang.MathLib;
import javolution.text.Text;
import javolution.text.TextBuilder;


/**
 * <p> This class represents a {@link TestContext test context} specialized
 *     for measuring execution time.</p>
 * 
 * <p> {@link TimeContext} implementations may perform assertions based upon the
 *     execution time. For example:[code] 
 *     class MyTestCase extends TestCase() {
 *          ...
 *          protected void validate() {
 *              long ns = TimeContext.getAverageTime("ns");
 *              TestContext.assertTrue(ns < 100); // Error if execution time is more than 100 ns.
 *              ... 
 *          }
 *     }[/code]</p>
 * 
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 5.2, August 5, 2007
 */
public abstract class TimeContext extends TestContext {

    /**
     * Holds an implementation which does not perform any logging but raises an
     * {@link AssertionException} when an assertion fails, including any timing
     * assertion.
     */
    public static final Class <? extends TimeContext>  REGRESSION = Regression.class;
    /**
     * Holds the minimum duration of each test execution (default 1000 ms).
     * The larger the number the more accurate is the average time result; but
     * the longer it takes to run the tests.
     */
    public static final Configurable <Integer>  TEST_DURATION_MS 
            = new Configurable(new Integer(1000)) {};
    /**
     * Holds the time context default implementation (by default logs 
     * average and minimum execution time to <code>System.out</code>).
     */
    public static final Configurable <Class<? extends TimeContext>>  DEFAULT 
            = new Configurable(Default.class) {};
 
    /**
     * Holds the minimum execution time in picoseconds.
     */
    private long _minimumPs;
    /**
     * Holds the average execution time in picoseconds.
     */
    private long _averagePs;
    /**
     * Holds the maximum execution time in picoseconds.
     */
    private long _maximumPs;

    /**
     * Enters the {@link #DEFAULT} time context.
     */
    public static void enter() {
         AbstractContext.enter((Class) DEFAULT.get());
    }

    /**
     * Exits the current time context.
     *
     * @throws ClassCastException if the context is not a time context.
     */
    public static void exit() {
         AbstractContext.exit(TimeContext.class);
    }

    /**
     * Returns the minimum execution time of the latest execution performed or
     * <code>-1</code> if the current context is not a time context.
     * 
     * @param unit one of <code>"s", "ms", "us", "ns", "ps"</code>
     * @return the minimum execution time stated in the specified unit.
     */
    public static long getMinimumTime(String unit) {
        LogContext ctx = (LogContext) LogContext.getCurrentLogContext();
        if (ctx instanceof TimeContext)
            return TimeContext.picosecondTo(unit, ((TimeContext) ctx).getMinimumTimeInPicoSeconds());
        else
            return -1;
    }

    /**
     * Returns the average execution time of the latest execution performed or
     * <code>-1</code> if the current context is not a time context.
     * 
     * @param unit one of <code>"s", "ms", "us", "ns", "ps"</code>
     * @return the average execution time stated in the specified unit.
     */
    public static long getAverageTime(String unit) {
        LogContext ctx = (LogContext) LogContext.getCurrentLogContext();
        if (ctx instanceof TimeContext)
            return TimeContext.picosecondTo(unit, ((TimeContext) ctx).getAverageTimeInPicoSeconds());
        else
            return -1;
    }

    /**
     * Returns the maximum execution time of the latest execution performed or
     * <code>-1</code> if the current context is not a time context.
     * 
     * @param unit one of <code>"s", "ms", "us", "ns", "ps"</code>
     * @return the maximum execution time stated in the specified unit.
     */
    public static long getMaximumTime(String unit) {
        LogContext ctx = (LogContext) LogContext.getCurrentLogContext();
        if (ctx instanceof TimeContext)
            return TimeContext.picosecondTo(unit, ((TimeContext) ctx).getMaximumTimeInPicoSeconds());
        else
            return -1;
    }

    private static long picosecondTo(String unit, long picoseconds) {
        if (unit.equals("ps"))
            return picoseconds;
        if (unit.equals("ns"))
            return picoseconds / 1000;
        if (unit.equals("us"))
            return picoseconds / 1000000;
        if (unit.equals("ms"))
            return picoseconds / 1000000000;
        if (unit.equals("s"))
            return picoseconds / 1000000000000L;
        throw new IllegalArgumentException("Unit " + unit + " not recognized");
    }

    /**
     * Returns the minimum execution time of the latest execution stated in
     * pico-seconds.
     * 
     * @return the time in pico-seconds.
     */
    public long getMinimumTimeInPicoSeconds() {
        return _minimumPs;
    }

    /**
     * Returns the average execution time of the latest execution stated in
     * pico-seconds.
     * 
     * @return the time in pico-seconds.
     */
    public long getAverageTimeInPicoSeconds() {
        return _averagePs;
    }

    /**
     * Returns the maximmum execution time of the latest execution stated in
     * pico-seconds.
     * 
     * @return the time in pico-seconds.
     */
    public long getMaximumTimeInPicoSeconds() {
        return _maximumPs;
    }

    /**
     * Benchmarks the specified test case and logs the results.
     *
     * @param testCase the test case being executed if not marked ignored.
     */
    protected void doRun(TestCase testCase) throws Exception {
        if (testCase.isIgnored())
            return;
        System.gc();
        try {
            Thread.sleep(200); // For GC to run.
        } catch (InterruptedException e) {
        }
        _minimumPs = Long.MAX_VALUE;
        _maximumPs = 0;
        _averagePs = 0;
        long totalCount = 0;
        long totalDuration = 0;
        long maximumDurationPs = ((Integer) TEST_DURATION_MS.get()).intValue() * 1000000000L;
        do {
            testCase.setUp(); // Prepare
            try {
                long start = TimeContext.nanoTime();
                testCase.execute(); // Execute.
                long duration = (TimeContext.nanoTime() - start) * 1000; // Picoseconds
                int count = testCase.count();
                totalCount += count;
                totalDuration += duration;
                long singleExecutionDuration = duration / count;
                if (singleExecutionDuration < _minimumPs)
                    _minimumPs = singleExecutionDuration;
                if (singleExecutionDuration > _maximumPs)
                    _maximumPs = singleExecutionDuration;
                if (totalDuration >= maximumDurationPs) {
                    _averagePs = totalDuration / totalCount;
                    testCase.validate(); // Validate only at last iteration.
                    break;
                }
            } finally {
                testCase.tearDown(); // Cleanup.
            }
        } while (true);
    }

    // Holds the default implementation.
    private static final class Default extends TimeContext {

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

        protected void doRun(TestCase testCase) {
            if (testCase.isIgnored()) {
                logWarning(Text.valueOf("Ignore ").plus(testCase.getName()));
                _ignoredCount++;
                return;
            }
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
            TextBuilder tmp = TextBuilder.newInstance();
            try { // Formats time report.
                tmp.append(testCase.getName());
                tmp.setLength(40, ' ');
                tmp.append(" - Average: ");
                Default.appendTime(this.getAverageTimeInPicoSeconds(), tmp);
                tmp.append(", Minimum: ");
                Default.appendTime(this.getMinimumTimeInPicoSeconds(), tmp);
                tmp.append(", Maximum: ");
                Default.appendTime(this.getMaximumTimeInPicoSeconds(), tmp);
                logMessage("time", tmp);
            } finally {
                TextBuilder.recycle(tmp);
            }

        }

        private static TextBuilder appendTime(long picoseconds, TextBuilder tb) {
            long divisor;
            String unit;
            if (picoseconds > 1000 * 1000 * 1000 * 1000L) { // 1 s
                unit = " s";
                divisor = 1000 * 1000 * 1000 * 1000L;
            } else if (picoseconds > 1000 * 1000 * 1000L) {
                unit = " ms";
                divisor = 1000 * 1000 * 1000L;
            } else if (picoseconds > 1000 * 1000L) {
                unit = " us";
                divisor = 1000 * 1000L;
            } else if (picoseconds > 1000L) {
                unit = " ns";
                divisor = 1000L;
            } else {
                unit = " ps";
                divisor = 1L;
            }
            long value = picoseconds / divisor;
            tb.append(value);
            int fracDigits = 3 - MathLib.digitLength(value); // 3 digits
            // precision
            if (fracDigits > 0)
                tb.append(".");
            for (int i = 0, j = 10; i < fracDigits; i++, j *= 10) {
                tb.append((picoseconds * j / divisor) % 10);
            }
            return tb.append(unit);
        }
    }

    private static long nanoTime() {
        /**/
        if (true) return System.nanoTime(); 
        /**/
        return System.currentTimeMillis() * 1000000;
    }

    // TestContext implementation with no output (just validation).
    private static final class Regression extends TimeContext {

        protected boolean doAssert(boolean value, CharSequence message) {
            if (!value)
                throw new AssertionException(message.toString());
            return value;
        }

        protected void logMessage(String category, CharSequence message) {
            // Do nothing.
        }

        public boolean isLogged(String category) {
            return false;
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
                return new Regression();
            }
        }, Regression.class);
    }
}