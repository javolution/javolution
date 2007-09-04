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
import javolution.context.Context;
import javolution.context.LogContext;
import javolution.context.ObjectFactory;
import javolution.lang.Configurable;
import javolution.lang.MathLib;
import javolution.text.TextBuilder;

/**
 * <p> This class represents a {@link TestContext test context} specialized 
 *     for measuring execution time.</p>
 * 
 * <p> {@link TimeContext} implementations may perform assertions based upon the
 *     execution time. For example:[code] 
 *     class MyTestCase extends TestCase() {
 *          ...
 *          public void validate() {
 *              long ns = TimeContext.getAverageTime("ns");
 *              TimeContext.assertTrue(ns < 100); // Error if execution time is more than 100 ns. 
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
	public static final Class/*<TimeContext>*/REGRESSION = Regression.CLASS;

	/**
	 * Holds the minimum duration of each test case execution (default 2000 ms).
	 * The larger the number the more accurate is the average time result; but
	 * the longer it takes to run the test.
	 */
	public static final Configurable/*<Integer>*/
	   TEST_DURATION_MS = new Configurable(new Integer(1000));

	/**
	 * Holds the time context default implementation (by default logs 
	 * average and minimum execution time to <code>System.out</code>).
	 */
	public static final Configurable/*<Class<? extends TimeContext>>*/
	   DEFAULT = new Configurable(Default.CLASS);

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
	 * 
	 * @return the time context being entered.
	 */
	public static TimeContext enter() {
		return (TimeContext) Context.enter((Class) DEFAULT.get());
	}

	/**
	 * Exits the current time context.
	 * 
	 * @return the time context being exited.
	 * @throws ClassCastException if the context is not a stack context.
	 */
	public static/*TimeContext*/Context exit() {
		return (TimeContext) Context.exit();
	}

	/**
	 * Returns the minimum execution time of the latest execution performed or
	 * <code>-1</code> if the current context is not a time context.
	 * 
	 * @param unit one of <code>"s", "ms", "us", "ns", "ps"</code>
	 * @return the minimum execution time stated in the specified unit.
	 */
	public static long getMinimumTime(String unit) {
		LogContext ctx = (LogContext) LogContext.getCurrent();
		if (ctx instanceof TimeContext) {
			return TimeContext.picosecondTo(unit, ((TimeContext) ctx)
					.getMinimumTimeInPicoSeconds());
		} else {
			return -1;
		}
	}

	/**
	 * Returns the average execution time of the latest execution performed or
	 * <code>-1</code> if the current context is not a time context.
	 * 
	 * @param unit one of <code>"s", "ms", "us", "ns", "ps"</code>
	 * @return the average execution time stated in the specified unit.
	 */
	public static long getAverageTime(String unit) {
		LogContext ctx = (LogContext) LogContext.getCurrent();
		if (ctx instanceof TimeContext) {
			return TimeContext.picosecondTo(unit, ((TimeContext) ctx)
					.getAverageTimeInPicoSeconds());
		} else {
			return -1;
		}
	}

	/**
	 * Returns the maximum execution time of the latest execution performed or
	 * <code>-1</code> if the current context is not a time context.
	 * 
	 * @param unit one of <code>"s", "ms", "us", "ns", "ps"</code>
	 * @return the maximum execution time stated in the specified unit.
	 */
	public static long getMaximumTime(String unit) {
		LogContext ctx = (LogContext) LogContext.getCurrent();
		if (ctx instanceof TimeContext) {
			return TimeContext.picosecondTo(unit, ((TimeContext) ctx)
					.getMaximumTimeInPicoSeconds());
		} else {
			return -1;
		}
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

	// Overrides.
	public void doTest(TestCase testCase) {
		_testCase = testCase;
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
		long maximumDurationPs 
		   = ((Integer) TEST_DURATION_MS.get()).intValue() * 1000000000L;
		do {
			testCase.prepare(); // Prepare
			try {
				long start = TimeContext.nanoTime();
				testCase.execute(); // Execute.
				long duration = (TimeContext.nanoTime() - start) * 1000; // Picoseconds
				int count = testCase.count();
				totalCount += count;
				totalDuration += duration;
				long singleExecutionDuration = duration / count;
				if (singleExecutionDuration < _minimumPs) {
					_minimumPs = singleExecutionDuration;
				}
				if (singleExecutionDuration > _maximumPs) {
					_maximumPs = singleExecutionDuration;
				}
				if (totalDuration >= maximumDurationPs) {
					_averagePs = totalDuration / totalCount;
					testCase.validate(); // Validate only at last iteration.
					break;
				}
			} finally {
				testCase.cleanup(); // Cleanup.
			}
		} while (true);
	}
	private TestCase _testCase;

	// Override.
	public boolean doAssertEquals(String message, Object expected, Object actual) {
		if (((expected == null) && (actual != null))
				|| ((expected != null) && (!expected.equals(actual)))) {
			LogContext.error(_testCase.toString());
			throw new AssertionException(message, expected, actual);
		}
		return true;
	}

	// Holds the default implementation.
	private static final class Default extends TimeContext {

		private static final Class CLASS = new Default().getClass();

		// Overrides.
		public void doTest(TestCase testCase) {
			super.doTest(testCase);
			TextBuilder tb = TextBuilder.newInstance();
			tb.append("[test] ");
		    tb.append(testCase.toString());
			tb.append(": ");
			Default.appendTime(this.getAverageTimeInPicoSeconds(), tb);
			tb.append(" (minimum ");
			Default.appendTime(this.getMinimumTimeInPicoSeconds(), tb);
			tb.append(")");
            tb.println();
			TextBuilder.recycle(tb);
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

        public boolean isInfoLogged() {
            return true;
        }

        public void logInfo(CharSequence message) {
            System.out.print("[info] ");
            System.out.println(message);
        }

        public boolean isWarningLogged() {
            return true;
        }

        public void logWarning(CharSequence message) {
            System.out.print("[warning] ");
            System.out.println(message);
        }

        public boolean isErrorLogged() {
            return true;
        }

        public void logError(Throwable error, CharSequence message) {
            System.out.print("[error] ");
            if (error != null) {
                System.out.print(error.getClass().getName());
                System.out.print(" - ");
            }
            String description = (message != null) ? message.toString()
                    : (error != null) ? error.getMessage() : "";
            System.out.println(description);
            if (error != null) {
                error.printStackTrace();
            }
        }

	}

	private static long nanoTime() {
		/*@JVM-1.5+@
	    if (true) return System.nanoTime(); 
        /**/
	    return System.currentTimeMillis() * 1000000;
	}

	// TestContext implementation with no output (just validation).
	private static final class Regression extends TimeContext {

		private static final Class CLASS = new Regression().getClass();

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
				return new Default();
			}
		}, Default.CLASS);
		ObjectFactory.setInstance(new ObjectFactory() {
			protected Object create() {
				return new Regression();
			}
		}, Regression.CLASS);
	}

}