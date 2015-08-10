/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 *
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.tools;

import java.math.BigDecimal;

import javolution.context.LogContext;
import javolution.lang.Configurable;
import javolution.lang.MathLib;
import javolution.text.TextBuilder;
import javolution.util.FastTable;

/**
 * <p> Utility class to measure the worst case execution time and average
 *     execution time with high precision. Here an example measuring the
 *     worst case execution time of {@link java.util.List#add(int, Object)}
 *     for diverse list implementations.</p>
 * [code]
 * Perfometer<Class<? extends List>> insertPerf = new Perfometer<>("java.util.List#add(int, Object)") {
 *     List<Object> list;
 *     Random random;
 *     protected void initialize() throws Exception {
 *          list = getInput().newInstance();
 *          random = new Random(-1); // Use seed to ensure same execution path.
 *     }
 *     protected void run(boolean measure) {
 *          Object obj = new Object();
 *          int i = random.nextInt(list.size() + 1);
 *          if (measure) list.add(i, obj);
 *     }
 *     protected void validate() { // Optional.
 *         assert list.size() == getNbrOfIterations();
 *     }
 * }
 * ...
 * public void testExecutionTime() {
 *     insertPerf.measure(java.util.ArrayList.class, 10000).print();
 *     insertPerf.measure(java.util.LinkedList.class, 10000).print();
 *     insertPerf.measure(javolution.util.FastTable.class, 10000).print();
 * }
 * ...
 * > [INFO] java.util.List#add(int, Object) (10000) for java.util.ArrayList:       590.21450 ns (avg), 8443.0000 ns (wcet#9369)
 * > [INFO] java.util.List#add(int, Object) (10000) for java.util.LinkedList:      4849.8313 ns (avg), 26536.000 ns (wcet#9863)
 * > [INFO] java.util.List#add(int, Object) (10000) for javolution.util.FastTable: 217.26300 ns (avg), 534.00000 ns (wcet#8864)
 * [/code]
 *
 * @param <T> the perfometer input type.
 */
public abstract class Perfometer<T> {

	private final BigDecimal _1e9 = new BigDecimal(1e9);

	/**
	 * Hold the measurement duration in milliseconds (default 1000 ms).
	 */
	public static final Configurable<Integer> DURATION_MS = new Configurable<Integer>() {

		@Override
		public String getName() { // Requires since there are multiple configurable fields.
			return this.getClass().getEnclosingClass().getName()
					+ "#DURATION_MS";
		}

		@Override
		protected Integer getDefault() {
			return 1000000000;
		}

	};
	/**
	 * Indicates if perfometer measurements should be skipped (
	 * e.g. {@code -Djavolution.test.Perfometer#SKIP=true} to skip
	 * performance measurements).
	 * When skipped, {@link #measure} and {@link #print} don't do anything.
	 */
	public static final Configurable<Boolean> SKIP = new Configurable<Boolean>() {

		@Override
		public String getName() { // Requires since there are multiple configurable fields.
			return this.getClass().getEnclosingClass().getName() + "#SKIP";
		}

		@Override
		protected Boolean getDefault() {
			return false;
		}
	};

	private final String description;
	private T input;
	private long[] times; // Nano-Seconds.

	/**
	 * Creates a perfometer having the specified description.
	 *
	 * @param description the description of the code being measured.
	 */
	public Perfometer(final String description) {
		this.description = description;
	}

	/**
	 * @return the average execution time in seconds.
	 */
	public BigDecimal getAvgTimeInSeconds() {
		if (times == null) return BigDecimal.ZERO;
		long sum = 0;
		for (int i = 0; i < times.length; i++) {
			final long time = times[i];

			//LogContext.info("getAvgTimeInSeconds: Time "+i+" = "+times[i]);

			sum += time;
		}

		final BigDecimal timeSum = new BigDecimal(sum);
		final BigDecimal length = new BigDecimal(times.length);

		return timeSum.divide(_1e9).divide(length);
	}

	/**
	 * @return this perfometer description.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @return this perfometer current inputs.
	 */
	public T getInput() {
		return input;
	}

	/**
	 * @return this perfometer current number of iterations performed.
	 */
	public int getNbrOfIterations() {
		return (times != null) ? times.length : 0;
	}

	/**
	 * @return the execution times in seconds.
	 */
	public BigDecimal[] getTimesInSeconds() {
		if (times == null) return new BigDecimal[0];
		final BigDecimal[] timesSec = new BigDecimal[times.length];
		for (int i=0; i < times.length; i++) {
			final BigDecimal time = new BigDecimal(times[i]);
			timesSec[i] = time.divide(_1e9);
		}
		return timesSec;
	}

	/**
	 * Measures the execution time with high precision (single iteration).
	 *
	 * @param input the test input.
	 * @return Reference to this Performeter
	 */
	public Perfometer<T> measure(final T input) {
		return measure(input, 1);
	}

	/**
	 * Measures the worst case execution time and average execution time.
	 *
	 * @param input the test input.
	 * @param nbrOfIterations the number of iterations performed on which
	 *        the average will be calculated.
	 * @return Reference to this Performeter
	 */
	public Perfometer<T> measure(final T input, final int nbrOfIterations) {
		if (SKIP.get()) return this; // Skip.
		this.input = input;
		this.times = new long[nbrOfIterations];
		final long[] calibrations = longArray(nbrOfIterations, Long.MAX_VALUE);
		final long[] measures = longArray(nbrOfIterations, Long.MAX_VALUE);
		try {
			// Warm Up
			initialize();
			for (int i = 0; i < nbrOfIterations; i++) {
				final long start = System.nanoTime();
				run(false);
				final long time = System.nanoTime() - start;
				calibrations[i] = MathLib.min(calibrations[i], time);
			}

			// Measurement.
			initialize();
			for (int i = 0; i < nbrOfIterations; i++) {
				final long start = System.nanoTime();
				run(true);
				final long time = System.nanoTime() - start;
				measures[i] = MathLib.min(measures[i], time);
			}

			for (int i = 0; i < nbrOfIterations; i++) {
				if(measures[i] > calibrations[i]){
					times[i] = measures[i] - calibrations[i];
				}
				else {
					times[i] = measures[i];
				}
			}

			return this;
		} catch (final Exception error) {
			throw new RuntimeException("Perfometer Exception", error);
		}
	}

	/**
	 * Outputs the result.
	 */
	public void print() {
		if (Perfometer.SKIP.get()) return;
		final TextBuilder txt = new TextBuilder();
		txt.append(description).append(" (").append(getNbrOfIterations())
		.append(") for ").append(input).append(": ");
		while (txt.length() < 80)
			txt.append(' ');
		txt.append(getAvgTimeInSeconds().multiply(_1e9).toPlainString()); // Nano-Seconds.
		txt.append(" ns (avg), ");
		txt.append(getBCETinSeconds().multiply(_1e9).toPlainString()); // Nano-Seconds.
		txt.append(" ns (bcet#").append(getBestCaseNumber()).append("), ");
		txt.append(getWCETinSeconds().multiply(_1e9).toPlainString()); // Nano-Seconds.
		txt.append(" ns (wcet#").append(getWorstCaseNumber()).append(")");
		LogContext.info(txt);
	}

	/**
	 * Outputs the measurements in nanoseconds.
	 */
	public void printDetails() {
		if (Perfometer.SKIP.get()) return;
		final FastTable<Long> measurements = new FastTable<Long>();
		for (final long time : times)
			measurements.add(time);
		LogContext.debug(measurements);
	}

	/**
	 * @return the best case execution time in seconds.
	 */
	public BigDecimal getBCETinSeconds() {
		if (times == null) return null;
		long bcet = Long.MAX_VALUE;
		for (final long time : times) {
			if (time < bcet) bcet = time;
		}
		return new BigDecimal(bcet).divide(_1e9);
	}

	/**
	 * @return the worst case execution time in seconds.
	 */
	public BigDecimal getWCETinSeconds() {
		if (times == null) return null;
		long wcet = 0;
		for (final long time : times) {
			if (time > wcet) wcet = time;
		}
		return new BigDecimal(wcet).divide(_1e9);
	}

	/**
	 * @return the iteration number having the fastest execution time.
	 */
	public int getBestCaseNumber() {
		if (times == null) return -1;
		long bcet = Long.MAX_VALUE;
		int j = -1;
		for (int i=0; i < times.length; i++) {
			if (times[i] < bcet) {
				bcet = times[i];
				j = i;
			}
		}
		return j;
	}

	/**
	 * @return the iteration number having the slowest execution time.
	 */
	public int getWorstCaseNumber() {
		if (times == null) return -1;
		long wcet = 0;
		int j = -1;
		for (int i=0; i < times.length; i++) {
			if (times[i] > wcet) {
				wcet = times[i];
				j = i;
			}
		}
		return j;
	}

	/**
	 * Performs the initialization.
	 * @throws java.lang.Exception if an exception occurs during initialization.
	 */
	protected abstract void initialize() throws Exception;

	/**
	 * Runs the code being benchmarked.
	 *
	 * @param measure {@code false} when calibration is performed;
	 *        {@code true} otherwise.
	 * @throws java.lang.Exception if an exception occurs during execution
	 */
	protected abstract void run(boolean measure) throws Exception;

	/**
	 * Validates the final result (after all iterations are completed).
	 */
	protected void validate() {}

	private long[] longArray(final int length, final long initialValue) {
		final long[] array = new long[length];
		for (int i = 0; i < length; i++)
			array[i] = initialValue;
		return array;
	}
}
