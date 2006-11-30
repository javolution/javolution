/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2005 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution;

import javolution.context.ConcurrentContext;
import javolution.context.ObjectFactory;
import javolution.context.PoolContext;
import javolution.context.RealtimeObject;
import javolution.context.ConcurrentContext.Logic;
import javolution.lang.MathLib;

/**
 * <p> This class holds {@link javolution.context} benchmark.</p>
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 2.0, November 26, 2004
 */
final class PerfContext extends Javolution implements Runnable {

    Object[] _objects = new Object[1000]; // Larger arrays result in 
                                          // poor heap performance (full GC ?)
    /** 
     * Executes benchmark.
     */
    public void run() {
        println("//////////////////////////////////");
        println("// Package: javolution.context //");
        println("//////////////////////////////////");
        println("");

        setOutputStream(null); // Warming up, to avoid measuring JIT.
        benchmarkConcurrency();
        setOutputStream(System.out);
        benchmarkConcurrency();

        setOutputStream(null); // Warming up, to avoid measuring JIT.
        benchmarkSmallObjects();
        setOutputStream(System.out);
        benchmarkSmallObjects();

        setOutputStream(null); // Warming up, to avoid measuring JIT.
        benchmarkHeapArrays();
        setOutputStream(System.out);
        benchmarkHeapArrays();

        setOutputStream(null); // Warming up, to avoid measuring JIT.
        benchmarkStackArrays();
        setOutputStream(System.out);
        benchmarkStackArrays();
        println("");
    }
    private void benchmarkConcurrency() {
        Adder adder = new Adder();
        for (int i=0; i < N; i++) {
            adder.left[i] = MathLib.random(0, 1000);
            adder.right[i] = MathLib.random(-1000, 0);
        }

        println("-- Concurrent Context --");
        print("Calculates int[] + int[] - Concurrency disabled: ");
        ConcurrentContext.setEnabled(false);
        startTime();
        adder.add(0, N);
        println(endTime(1));
        
        print("Calculates int[] + int[] - Concurrency (" + 
                ConcurrentContext.CONCURRENCY.get() + ") enabled: ");
        ConcurrentContext.setEnabled(false);
        startTime();
        adder.add(0, N);
        println(endTime(1));

        println("");
    }    
    
    final static int N = 1000000;
    private static class Adder {
        long[] left = new long[N];
        long[] right = new long[N];
        long[] sum = new long[N];
        
        public void add(final int start, final int end) {
            if (end - start  < 1000) { 
                for (int i=start; i < end; i++) {
                    sum[i] = left[i] + right[i];
                }
            } else {
                ConcurrentContext.enter();
                try {
                    final int half = (end - start) / 2;
                    ConcurrentContext.execute(new Logic() {
                        public void run(Object[] args) {
                            add(start, start + half);
                        }
                    });
                    ConcurrentContext.execute(new Logic() {
                        public void run(Object[] args) {
                            add(start + half + 1, end);
                        }
                    });
                } finally {
                    ConcurrentContext.exit();
                }
            }
        }   
    }

    private void benchmarkSmallObjects() {
        
        println("-- Heap versus Stack Allocation (Pool-Context) --");
        print("Small object heap creation: ");
        startTime();
        for (int i = 0; i < 10000; i++) {
            for (int j = 0; j < _objects.length;) {
                _objects[j++] = new SmallObject();
            }
            preventCompilerCodeRemoval();
        }
        println(endTime(10000 * _objects.length));

        print("Small object stack creation: ");
        startTime();
        for (int i = 0; i < 10000; i++) {
            PoolContext.enter();
            for (int j = 0; j < _objects.length;) {
                _objects[j++] = SmallObject.FACTORY.object();
            }
            PoolContext.exit();
            preventCompilerCodeRemoval();
        }
        println(endTime(10000 * _objects.length));

    }

    private void benchmarkHeapArrays() {
        print("char[256] heap creation: ");
        startTime();
        for (int i = 0; i < 1000; i++) {
            for (int j = 0; j < _objects.length;) {
                _objects[j++] = new char[256];
            }
            preventCompilerCodeRemoval();
        }
        println(endTime(1000 * _objects.length));

        print("char[512] heap creation: ");
        startTime();
        for (int i = 0; i < 1000; i++) {
            for (int j = 0; j < _objects.length;) {
                _objects[j++] = new char[512];
            }
            preventCompilerCodeRemoval();
        }
        println(endTime(1000 * _objects.length));

    }

    private void benchmarkStackArrays() {
        print("char[256] stack creation: ");
        startTime();
        for (int i = 0; i < 1000; i++) {
            PoolContext.enter();
            for (int j = 0; j < _objects.length;) {
                _objects[j++] = CHAR256_FACTORY.object();
            }
            PoolContext.exit();
            preventCompilerCodeRemoval();
        }
        println(endTime(1000 * _objects.length));

        print("char[512] stack creation: ");
        startTime();
        for (int i = 0; i < 1000; i++) {
            PoolContext.enter();
            for (int j = 0; j < _objects.length;) {
                _objects[j++] = CHAR512_FACTORY.object();
            }
            PoolContext.exit();
            preventCompilerCodeRemoval();
        }
        println(endTime(1000 * _objects.length));
    }

    private void preventCompilerCodeRemoval() {
        if (_objects[MathLib.random(0, _objects.length - 1)] == null)
            throw new Error(); // Test to avoid compiler code removal.
    }

    private static final class SmallObject extends RealtimeObject {
        long longValue;

        int intValue;

        SmallObject refValue;

        static final Factory FACTORY = new Factory() {
            public Object create() {
                return new SmallObject();
            }
        };
    }

    private static final ObjectFactory CHAR256_FACTORY = new ObjectFactory() {
        public Object create() {
            return new char[256];
        }
    };

    private static final ObjectFactory CHAR512_FACTORY = new ObjectFactory() {
        public Object create() {
            return new char[512];
        }
    };
}