/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util;

import javolution.context.LocalContext;
import javolution.context.LocalParameter;
import javolution.text.TypeFormat;
import javolution.util.function.Function;
import javolution.util.function.MultiVariable;

/**
 * Utility class to measure execution time with high precision.
 */
public class Perfometer {

    volatile boolean doPerform = true;

    /**
     * Hold the measure time duration in nanosecond.
     */
    public static final LocalParameter<Long> MEASURE_DURATION_NS = new LocalParameter<Long>(
            1000 * 1000 * 1000L) {
        @Override
        public void configure(CharSequence configuration) {
            this.setDefaultValue(TypeFormat.parseLong(configuration));
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
    public long measure(Function<?,?> functor, Object... params) {
        measure(true, functor, params); // Class initialization.
        System.gc();
        long nopExecutionTime = measure(false, functor, params);
        System.gc();
        long performExecutionTime = measure(true, functor, params);
        return performExecutionTime - nopExecutionTime;
    }

    @SuppressWarnings("unchecked")
    private long measure(boolean doPerform, Function<?,?> functor, Object... params) {
        long startTime = System.nanoTime();
        int count = 0;
        long executionTime;
        long cumulatedTime = 0;
        while (true) {
            Object param = evaluateParams(0, params);
            try {
                this.doPerform = doPerform;
                cumulatedTime -= System.nanoTime();
                ((Function<Object,?>)functor).evaluate(param);
            } finally {
                cumulatedTime += System.nanoTime();
                this.doPerform = true;
            }
            count++;
            executionTime = System.nanoTime() - startTime;
            if (executionTime > LocalContext.getLocalValue(MEASURE_DURATION_NS))
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
            return ((Function<Object,Object>) param).evaluate(next);
        } else if (next != null) {
            return new MultiVariable<Object,Object>(param, next);
        } else {
            return param;
        }
    }
}
