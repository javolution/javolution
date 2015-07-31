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
    public Perfometer(String description) {
        this.description = description;
    }

    /**
     * Returns the average execution time in seconds.
     */
    public BigDecimal getAvgTimeInSeconds() {
        if (times == null) return BigDecimal.ZERO;
        long sum = 0;
        for (long time : times) {
            sum += time;
        }
        
        BigDecimal timeSum = new BigDecimal(sum);
        BigDecimal length = new BigDecimal(times.length);
        
        return timeSum.divide(_1e9).divide(length);
    }

    /**
     * Returns this perfometer description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns this perfometer current inputs.
     */
    public T getInput() {
        return input;
    }

    /**
     * Returns this perfometer current number of iterations performed.
     */
    public int getNbrOfIterations() {
        return (times != null) ? times.length : 0;
    }

    /**
     * Returns the execution times in seconds.
     */
    public BigDecimal[] getTimesInSeconds() {
        if (times == null) return new BigDecimal[0];
        BigDecimal[] timesSec = new BigDecimal[times.length];
        for (int i=0; i < times.length; i++) {
        	BigDecimal time = new BigDecimal(times[i]);
            timesSec[i] = time.divide(_1e9);
        }
        return timesSec;
    }

    /**
     * Measures the execution time with high precision (single iteration).
     * 
     * @param input the test input.
     */
    public Perfometer<T> measure(T input) {
    	return measure(input, 1);
    }
    
    /**
     * Measures the worst case execution time and average execution time.
     * 
     * @param input the test input.
     * @param nbrOfIterations the number of iterations performed on which 
     *        the average will be calculated.
     */
    public Perfometer<T> measure(T input, int nbrOfIterations) {
        if (SKIP.get()) return this; // Skip.
        this.input = input;
        this.times = new long[nbrOfIterations];
        long[] measures = longArray(nbrOfIterations, Long.MAX_VALUE);
        try {
                // Warm Up
                initialize();
                for (int i = 0; i < nbrOfIterations; i++) {
                    run(true);
                }
                
                // Measurement.
                initialize();
                for (int i = 0; i < nbrOfIterations; i++) {
                    long start = System.nanoTime();
                    run(true);
                    long time = System.nanoTime() - start;
                    times[i] = time;
                }
            return this;
        } catch (Exception error) {
            throw new RuntimeException("Perfometer Exception", error);
        }
    }

    /**
     * Outputs the result.
     */
    public void print() {
        if (Perfometer.SKIP.get()) return;
        TextBuilder txt = new TextBuilder();
        txt.append(description).append(" (").append(getNbrOfIterations())
                .append(") for ").append(input).append(": ");
        while (txt.length() < 80)
            txt.append(' ');
        txt.append(getAvgTimeInSeconds().multiply(_1e9).toPlainString()); // Nano-Seconds.
        txt.append(" ns (avg), ");
        txt.append(getWCETinSeconds().multiply(_1e9).toPlainString()); // Nano-Seconds.
        txt.append(" ns (wcet#").append(getWorstCaseNumber()).append(")");
        LogContext.info(txt);
    }

    /**
     * Outputs the measurements in nanoseconds.
     */
    public void printDetails() {
        if (Perfometer.SKIP.get()) return;
        FastTable<Long> measurements = new FastTable<Long>();
        for (long time : times)
            measurements.add(time);
        LogContext.debug(measurements);
    }

    /**
     * Returns the worst case execution time in seconds.
     */
    public BigDecimal getWCETinSeconds() {
        if (times == null) return null;
        long wcet = 0;
        for (long time : times) {
            if (time > wcet) wcet = time;
        }
        return new BigDecimal(wcet).divide(_1e9);
    }

    /**
     * Returns the iteration number having the slowest execution time.
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
     */
    protected abstract void initialize() throws Exception;

    /**
     * Runs the code being benchmarked.
     * 
     * @param measure {@code false} when calibration is performed;
     *        {@code true} otherwise. 
     */
    protected abstract void run(boolean measure) throws Exception;

    /**
     * Validates the final result (after all iterations are completed). 
     */
    protected void validate() {}

    private long[] longArray(int length, long initialValue) {
        long[] array = new long[length];
        for (int i = 0; i < length; i++)
            array[i] = initialValue;
        return array;
    }
}
