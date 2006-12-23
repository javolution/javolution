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
import javolution.util.FastTable;
import j2me.lang.Comparable;

/**
 * <p> This class holds {@link javolution.context} benchmark.</p>
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 2.0, November 26, 2004
 */
final class PerfContext extends Javolution implements Runnable {

    Object[] _objects = new Object[1000]; // Larger arrays result in really 
                                          // poor heap performance (full GC ?)
    /** 
     * Executes benchmark.
     */
    public void run() {
        println("//////////////////////////////////");
        println("// Package: javolution.context //");
        println("//////////////////////////////////");
        println("");

        benchmarkConcurrency();
        benchmarkSmallObjects();
        benchmarkHeapArrays();
        benchmarkStackArrays();
        println("");
    }

    private static final int N = 10000;
    private void benchmarkConcurrency() {
 
        println("-- Concurrent Context --");
        print("Quick Sort " + N + " elements - Concurrency disabled: ");
        ConcurrentContext.setEnabled(false);
        for (int i=0; i < 100; i++) {
            FastTable table = randomTable();
            startTime();
            quickSort(table);
            keepBestTime(1);
        }
        println(endTime());

        print("Quick Sort " + N + " elements - Concurrency ("
                + ConcurrentContext.CONCURRENCY.get() + ") enabled: ");
        ConcurrentContext.setEnabled(true);
        for (int i=0; i < 100; i++) {
            FastTable table = randomTable();
            startTime();
            quickSort(table);
            keepBestTime(1);
        }
        println(endTime());

        println("");
    }

    private FastTable randomTable() {
        FastTable table = new FastTable(N);
        for (int i=0; i < N; i++) {
            table.add(new Integer(MathLib.random(Integer.MIN_VALUE, Integer.MAX_VALUE)));
        }
        return table;
    }

    private void quickSort(final FastTable table) {
        final int size = table.size();
        if (size < 100) { 
            table.sort();
        } else {
            final FastTable t1 = FastTable.newInstance();
            final FastTable t2 = FastTable.newInstance();
            ConcurrentContext.enter();
            try {
                ConcurrentContext.execute(new Logic() {
                    public void run() {
                        t1.addAll(table.subList(0, size / 2));
                        quickSort(t1);
                    }
                });
                ConcurrentContext.execute(new Logic() {
                    public void run() {
                        t2.addAll(table.subList(size / 2, size));
                        quickSort(t2);
                    }
                });
            } finally {
                ConcurrentContext.exit();
            }
            // Merges results.
            for (int i=0, i1=0, i2=0; i < size; i++) {
                 if (i1 >= t1.size()) {
                     table.set(i, t2.get(i2++));
                 } else if (i2 >= t2.size()) {
                     table.set(i, t1.get(i1++));
                 } else {
                     Comparable o1 = (Comparable) t1.get(i1);
                     Comparable o2 = (Comparable) t2.get(i2);
                     if (o1.compareTo(o2) < 0) {
                         table.set(i, o1);
                         i1++;
                     } else {
                         table.set(i, o2);
                         i2++;
                     }
                 }
            }
            FastTable.recycle(t1);
            FastTable.recycle(t2);
        }
    }
 
    private void benchmarkSmallObjects() {

        println("-- Heap versus Stack Allocation (Pool-Context) --");
        print("Small object heap creation: ");
        
        for (int i = 0; i < 100000; i++) {
            startTime();
            for (int j = 0; j < _objects.length;) {
                _objects[j++] = new SmallObject();
            }
            keepBestTime(_objects.length);
        }
        println(endTime());

        print("Small object stack creation: ");
        for (int i = 0; i < 100000; i++) {
            startTime();
            PoolContext.enter();
            for (int j = 0; j < _objects.length;) {
                _objects[j++] = SmallObject.FACTORY.object();
            }
            PoolContext.exit();
            keepBestTime(_objects.length);
        }
        println(endTime());

    }

    private void benchmarkHeapArrays() {
        print("char[256] heap creation: ");
        for (int i = 0; i < 1000; i++) {
            startTime();
            for (int j = 0; j < _objects.length;) {
                _objects[j++] = new char[256];
            }
            keepBestTime(_objects.length);
        }
        println(endTime());

        print("char[512] heap creation: ");
        for (int i = 0; i < 1000; i++) {
            startTime();
            for (int j = 0; j < _objects.length;) {
                _objects[j++] = new char[512];
            }
            keepBestTime(_objects.length);
        }
        println(endTime());

    }

    private void benchmarkStackArrays() {
        print("char[256] stack creation: ");
        for (int i = 0; i < 1000; i++) {
            startTime();
            PoolContext.enter();
            for (int j = 0; j < _objects.length;) {
                _objects[j++] = CHAR256_FACTORY.object();
            }
            PoolContext.exit();
            keepBestTime(_objects.length);
        }
        println(endTime());

        print("char[512] stack creation: ");
        for (int i = 0; i < 1000; i++) {
            startTime();
            PoolContext.enter();
            for (int j = 0; j < _objects.length;) {
                _objects[j++] = CHAR512_FACTORY.object();
            }
            PoolContext.exit();
            keepBestTime(_objects.length);
        }
        println(endTime());
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