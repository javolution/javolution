/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util;

import javolution.lang.Configurable;
import javolution.util.function.Function;
import javolution.util.function.MultiVariable;

/**
 * Utility class to measure execution time with high precision.
 */
public class Perfometer {

    volatile boolean doPerform = true;

    /**
     * Indicates if the perfometer measurement should be skipped.
     */
    public static final Configurable<Boolean> SKIP = new Configurable<Boolean>() {

        @Override
        protected Boolean getDefault() {
            return true;
        }

        @Override
        public String getName() { // Requires since there are multiple configurable fields.
            return this.getClass().getEnclosingClass().getName() + "#SKIP";
        }
    };

    /**
     * Hold the measure time duration in milliseconds (default 100 ms)
     */
    public static final Configurable<Long> MEASURE_DURATION_MS = new Configurable<Long>() {

        @Override
        protected Long getDefault() {
            return 100L;
        }

        @Override
        public String getName() { // Requires since there are multiple configurable fields.
            return this.getClass().getEnclosingClass().getName() + "#MEASURE_DURATION_MS";
        }

    };

    /**
     * Indicates if the operation to be measured is actually performed.
     */
    public boolean doPerform() {
        return doPerform;
    }

    /**
     * Measure the execution of the specified functor critical operations in 
     * nanosecond.
     * The functor is executed first with {@link #doPerform} set to <code>false</code>,
     * then it is set to <code>true</code>, the execution time is the second execution
     * time minus the first one. Parameters of a functor can be functors themselves 
     * in which case they are evaluated recursively before each measure.
     */
    public long measure(Function<?, ?> functor, Object... params) {
        measure(true, functor, params); // Class initialization.
        System.gc();
        long nopExecutionTime = measure(false, functor, params);
        System.gc();
        long performExecutionTime = measure(true, functor, params);
        return performExecutionTime - nopExecutionTime;
    }

    @SuppressWarnings("unchecked")
    private long measure(boolean doPerform, Function<?, ?> functor,
            Object... params) {
        long startTime = System.nanoTime();
        int count = 0;
        long executionTime;
        long cumulatedTime = 0;
        while (true) {
            Object param = evaluateParams(0, params);
            try {
                this.doPerform = doPerform;
                cumulatedTime -= System.nanoTime();
                ((Function<Object, ?>) functor).apply(param);
            } finally {
                cumulatedTime += System.nanoTime();
                this.doPerform = true;
            }
            count++;
            executionTime = System.nanoTime() - startTime;
            if (executionTime > MEASURE_DURATION_MS.get() * 1000000L)
                break;
        }
        return cumulatedTime / count;
    }

    @SuppressWarnings("unchecked")
    private Object evaluateParams(int i, Object... params) {
        if (i >= params.length)
            return null;
        Object param = params[i];
        Object next = evaluateParams(++i, params);
        if (param instanceof Function) {
            return ((Function<Object, Object>) param).apply(next);
        } else if (next != null) {
            return new MultiVariable<Object, Object>(param, next);
        } else {
            return param;
        }
    }
}
