/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2005 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution;

import javolution.context.ArrayFactory;
import javolution.context.ConcurrentContext;
import javolution.context.ObjectFactory;
import javolution.context.StackContext;
import javolution.lang.MathLib;
import javolution.util.FastComparator;
import javolution.util.FastTable;

/**
 * <p> This class holds {@link javolution.context} benchmark.</p>
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 2.0, November 26, 2004
 */
final class PerfContext extends Javolution implements Runnable {

    Object[] _objects = new Object[1000]; // Larger arrays would result in  
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
        benchmarkArrays();
        println("");
    }

    private static final int N = 10000;

    private void benchmarkConcurrency() {

        println("-- Concurrent Context --");
        print("Quick Sort " + N + " elements - Concurrency disabled: ");
        ConcurrentContext.setConcurrency(0);
        for (int i = 0; i < 1000; i++) {
            FastTable table = randomTable();
            startTime();
            quickSort(table);
            keepBestTime(1);
        }
        println(endTime());

        Integer concurrency = (Integer) ConcurrentContext.MAXIMUM_CONCURRENCY.get();
        print("Quick Sort " + N + " elements - Concurrency ("
                + concurrency + ") enabled: ");
        ConcurrentContext.setConcurrency(concurrency.intValue());
        for (int i = 0; i < 1000; i++) {
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
        for (int i = 0; i < N; i++) {
            table.add(new Integer(MathLib.random(Integer.MIN_VALUE,
                    Integer.MAX_VALUE)));
        }
        return table;
    }

    private void quickSort(final FastTable table) {
        final int size = table.size();
        if (size < 100) {
            table.setValueComparator(INTEGER_COMPARATOR);
            table.sort();
        } else {
            final FastTable t1 = FastTable.newInstance();
            final FastTable t2 = FastTable.newInstance();
            ConcurrentContext.enter();
            try {
                ConcurrentContext.execute(new Runnable() {
                    public void run() {
                        t1.addAll(table.subList(0, size / 2));
                        quickSort(t1);
                    }
                });
                ConcurrentContext.execute(new Runnable() {
                    public void run() {
                        t2.addAll(table.subList(size / 2, size));
                        quickSort(t2);
                    }
                });
            } finally {
                ConcurrentContext.exit();
            }
            // Merges results.
            for (int i = 0, i1 = 0, i2 = 0; i < size; i++) {
                if (i1 >= t1.size()) {
                    table.set(i, t2.get(i2++));
                } else if (i2 >= t2.size()) {
                    table.set(i, t1.get(i1++));
                } else {
                    Integer o1 = (Integer) t1.get(i1);
                    Integer o2 = (Integer) t2.get(i2);
                    if (o1.intValue() < o2.intValue()) {
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

    // For J2ME compability.
    private static final FastComparator INTEGER_COMPARATOR = new FastComparator() {

        public boolean areEqual(Object o1, Object o2) {
            return ((Integer) o1).intValue() == ((Integer) o2).intValue();
        }

        public int compare(Object o1, Object o2) {
            return ((Integer) o2).intValue() - ((Integer) o1).intValue();
        }

        public int hashCodeOf(Object obj) {
            return ((Integer) obj).intValue();
        }
    };

    private void benchmarkSmallObjects() {
        final int N = 1000000;
        final int M = 1000;

        println("-- Heap versus Stack Allocation (StackContext) --");
        print("Small objects (XYZ.plus(XYZ)) (heap): ");
        {
            startTime();
            for (int i = 0; i < N; i++) {
                XYZHeap c0 = new XYZHeap(0.0, 0.0, 0.0);
                XYZHeap c1 = new XYZHeap(-1.0, 1.0, 0.0);
                for (int j = 0; j < M; j++) { // Fibonacci sequence.
                    XYZHeap cn = c1.plus(c0);
                    c0 = c1;
                    c1 = cn;
                }
                if (c0.x != -c0.y)
                    throw new Error();
            }
            keepBestTime(M * N);
            println(endTime());
        }

        print("Small objects (XYZ.plus(XYZ)) (stack): ");
        {
            startTime();
            for (int i = 0; i < N; i++) {
                StackContext.enter();
                XYZStack c0 = XYZStack.valueOf(0.0, 0.0, 0.0);
                XYZStack c1 = XYZStack.valueOf(-1.0, 1.0, 0.0);
                for (int j = 0; j < M; j++) { // Fibonacci sequence.
                    XYZStack cn = c1.plus(c0);
                    c0 = c1;
                    c1 = cn;
                }
                if (c0.x != -c0.y)
                    throw new Error();
                StackContext.exit();
            }
            keepBestTime(M * N);
            println(endTime());
        }

    }

    private void benchmarkArrays() {
        print("char[256] on Heap: ");
        for (int i = 0; i < 1000; i++) {
            startTime();
            for (int j = 0; j < _objects.length;) {
                _objects[j++] = new char[256];
            }
            keepBestTime(_objects.length);
        }
        println(endTime());

        print("char[256] on Stack: ");
        for (int i = 0; i < 1000; i++) {
            startTime();
            StackContext.enter();
            for (int j = 0; j < _objects.length;) {
                _objects[j++] = ArrayFactory.CHARS_FACTORY.array(256);
            }
            StackContext.exit();
            keepBestTime(_objects.length);
        }
        println(endTime());

        print("char[512] on Heap: ");
        for (int i = 0; i < 1000; i++) {
            startTime();
            for (int j = 0; j < _objects.length;) {
                _objects[j++] = new char[512];
            }
            keepBestTime(_objects.length);
        }
        println(endTime());

        print("char[512] on Stack: ");
        for (int i = 0; i < 1000; i++) {
            startTime();
            StackContext.enter();
            for (int j = 0; j < _objects.length;) {
                _objects[j++] = ArrayFactory.CHARS_FACTORY.array(512);
            }
            StackContext.exit();
            keepBestTime(_objects.length);
        }
        println(endTime());
    }

    static final class XYZHeap {
        final double x, y, z;     

        public XYZHeap(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public XYZHeap plus(XYZHeap that) {
            return new XYZHeap(this.x + that.x, this.y + that.y, this.z + that.z);
        }
    }

    static final class XYZStack {
        static ObjectFactory FACTORY = new ObjectFactory() {
            protected Object create() {
                return new XYZStack();
            }
        };

        double x, y, z;

        public static XYZStack valueOf(double x, double y, double z) {
            XYZStack c = (XYZStack) FACTORY.object();
            c.x = x;
            c.y = y;
            c.z = z;
            return c;
        }

        public XYZStack plus(XYZStack that) {
            return valueOf(this.x + that.x, this.y + that.y, this.z + that.z);
        }
    } 
}