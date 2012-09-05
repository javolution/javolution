/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2007 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution;

import org.javolution.context.ArrayFactory;
import org.javolution.context.ConcurrentContext;
import org.javolution.context.LocalContext;
import org.javolution.context.ObjectFactory;
import org.javolution.context.StackContext;
import org.javolution.lang.MathLib;
import org.javolution.testing.TestCase;
import org.javolution.testing.TestContext;
import org.javolution.testing.TestSuite;
import org.javolution.util.FastTable;
import org.javolution.util.Index;

/**
 * <p> This class holds the test cases for the {@link javolution.context 
 *     context} classes.</p>
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 5.3, March 19, 2009
 */
public final class ContextTestSuite extends TestSuite {

    public ContextTestSuite() {
        final int defaultConcurrency = ConcurrentContext.getConcurrency();
        addTest(new Concurrency(10000, 0)); // Test with concurrency disabled
        addTest(new Concurrency(10000, defaultConcurrency));
        addTest(new SmallObjectAllocation(false));
        addTest(new SmallObjectAllocation(true));
        addTest(new ArrayRecycling(4096, false));
        addTest(new ArrayRecycling(4096, true));
    }

    class Concurrency extends TestCase {

        final int _size;

        final int _concurrency;

        FastTable _table;

        public Concurrency(int size, int concurrency) {
            _size = size;
            _concurrency = concurrency;

        }

        public String getName() {
            return "ConcurrentContext (" + _concurrency + ") Quick-Sort (" + _size + " elements)";
        }

        public void setUp() {
            _table = new FastTable(_size);
            for (int i = 0; i < _size; i++) {
                _table.add(Index.valueOf(MathLib.random(0, _size)));
            }
        }

        public void execute() {
            LocalContext.enter();
            try {
                ConcurrentContext.setConcurrency(_concurrency);
                quickSort(_table);
            } finally {
                LocalContext.exit();
            }
        }

        public void validate() {
            TestContext.assertEquals(_size, _table.size());
            for (int i = 0; i < _size - 1; i++) {
                int i1 = ((Index) _table.get(i)).intValue();
                int i2 = ((Index) _table.get(i + 1)).intValue();
                if (!TestContext.assertTrue(i1 <= i2))
                    break;
            }
        }

        private void quickSort(final FastTable table) {
            final int size = table.size();
            if (size < 100)
                table.sort();
            else {
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
                    if (i1 >= t1.size())
                        table.set(i, t2.get(i2++));
                    else if (i2 >= t2.size())
                        table.set(i, t1.get(i1++));
                    else {
                        Index o1 = (Index) t1.get(i1);
                        Index o2 = (Index) t2.get(i2);
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
    }

    class SmallObjectAllocation extends TestCase {

        final int N = 1000;

        boolean _useStack;

        XYZ c0, c1;

        public SmallObjectAllocation(boolean useStack) {
            _useStack = useStack;
        }

        public String getName() {
            return (!_useStack ? "HeapContext (default)" : "StackContext")
                    + ", small object creation";
        }

        public void setUp() {
            c0 = new XYZ(0, 0, 0);
            c1 = new XYZ(-1, 1, 0);
            StackContext.enter(); // Allows stack allocations.
        }

        public void execute() {
            if (_useStack)
                for (int i = 0; i < N; i++) { // Fibonacci sequence.
                    XYZ cn = c1.plusStack(c0);
                    c0 = c1;
                    c1 = cn;
                }
            else
                for (int i = 0; i < N; i++) { // Fibonacci sequence.
                    XYZ cn = c1.plusHeap(c0);
                    c0 = c1;
                    c1 = cn;
                }
        }

        public int count() {
            return N;
        }

        public void tearDown() {
            StackContext.exit();
        }

        public void validate() {
            TestContext.assertTrue(c0.x == -c0.y);
        }
    }

    class ArrayRecycling extends TestCase {

        int _size;

        boolean _recycle;

        char[] _array;

        public ArrayRecycling(int size, boolean recycle) {
            _size = size;
            _recycle = recycle;
        }

        public String getName() {
            return "HeapContext, char[" + _size + "] "
                    + (_recycle ? "recycled" : "allocated");
        }

        public void execute() {
            if (_recycle) {
                _array = (char[]) ArrayFactory.CHARS_FACTORY.array(_size);
                ArrayFactory.CHARS_FACTORY.recycle(_array);
            } else
                _array = new char[_size];
        }

        public void validate() {
            TestContext.assertTrue(_array.length >= _size);
        }
    }

    // Utility classes.
    private static final class XYZ {

        static ObjectFactory FACTORY = new ObjectFactory() {

            protected Object create() {
                return new XYZ();
            }
        };

        long x, y, z;

        private XYZ() {
        }

        public XYZ(long x, long y, long z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public static XYZ valueOf(long x, long y, long z) {
            XYZ c = (XYZ) FACTORY.object();
            c.x = x;
            c.y = y;
            c.z = z;
            return c;
        }

        public XYZ plusStack(XYZ that) {
            return XYZ.valueOf(this.x + that.x, this.y + that.y, this.z + that.z);
        }

        public XYZ plusHeap(XYZ that) {
            return new XYZ(this.x + that.x, this.y + that.y, this.z + that.z);
        }
    }
}
