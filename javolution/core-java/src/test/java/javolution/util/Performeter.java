/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util;

import javolution.lang.Functor;
import javolution.lang.MultiVariable;

/**
 * Utility class to measure execution time with high precision.
 */
public class Performeter {

    volatile boolean doPerform = true;
 
    private static long TIME_PER_MEASURE_IN_NS = 1000 * 1000 * 500L; // 500 ms.
    
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
     * in which case they are evaluated.
     */
    public long measure(Functor functor, Object... params) {
        measure(false, functor, params); // Class initialization.
        System.gc();
        long nopExecutionTime = measure(false, functor, params);
        System.gc();
        long totalExecutionTime = measure(true, functor, params);
        return totalExecutionTime - nopExecutionTime;        
    }
    
    private long measure(boolean doPerform, Functor functor, Object... params) {
        long startTime = System.nanoTime();
        int count = 0;
        long executionTime;
        while (true) {
            Object param = evaluateParams(0, params);
            try {
                this.doPerform = doPerform;
                functor.evaluate(param);
            } finally {
                this.doPerform = true;
            }
            count++;
            executionTime = System.nanoTime() - startTime;
            if (executionTime > TIME_PER_MEASURE_IN_NS) break; // One second elapsed.
        }
        return executionTime / count;
    }

    private Object evaluateParams(int i, Object... params) {
        if (i >= params.length) return null;
        Object param = params[i];
        if (param instanceof Functor) {
            return ((Functor) param).evaluate(evaluateParams(++i, params));
        } else {
            if (params.length > i + 1) { // Multi-variable.
                return new MultiVariable(param, evaluateParams(++i, params));
            } else {
                return param; // Last parameter.
            }            
        }
    }
    
}
