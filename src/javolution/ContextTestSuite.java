/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2007 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution;

import j2me.lang.CharSequence;
import javolution.context.ArrayFactory;
import javolution.context.ConcurrentContext;
import javolution.context.LocalContext;
import javolution.context.ObjectFactory;
import javolution.context.StackContext;
import javolution.lang.MathLib;
import javolution.testing.TestCase;
import javolution.testing.TestSuite;
import javolution.testing.TestContext;
import javolution.text.TextBuilder;
import javolution.util.FastTable;
import javolution.util.Index;

/**
 * <p> This class holds {@link javolution.context} benchmark.</p>
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 5.2, August 19, 2007
 */
public final class ContextTestSuite extends TestSuite {

    public void run() {
        TestContext.info("-------------------------------------------------");
        TestContext.info("-- Test Suite for package javolution.context.* --");
        TestContext.info("-------------------------------------------------");
        final int defaultConcurrency = ConcurrentContext.getConcurrency();
        TestContext.test(new Concurrency(10000, 0));
        TestContext.test(new Concurrency(10000, defaultConcurrency));
        TestContext.info("");
        TestContext.test(new SmallObjectAllocation(1000, false));
        TestContext.test(new SmallObjectAllocation(1000, true));
        TestContext.info("");
        TestContext.test(new ArrayAllocation(4096, false));
        TestContext.test(new ArrayAllocation(4096, true));
        TestContext.info("");
    }

    public static class Concurrency extends TestCase {
        final int _size;

        final int _concurrency;

        FastTable _table;

        public Concurrency(int size, int concurrency) {
            _size = size;
            _concurrency = concurrency;
        }

        public CharSequence getDescription() {
            return TextBuilder.newInstance().append(
                    "ConcurrentContext - Quick Sort (size: ").append(_size)
                    .append(", concurrency: ").append(_concurrency).append(")");
        }

        public void prepare() {
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
            TestContext.assertEquals("Size", new Integer(_size), new Integer(
                    _table.size()));
            for (int i = 0; i < _size - 1; i++) {
                int i1 = ((Index) _table.get(i)).intValue();
                int i2 = ((Index) _table.get(i + 1)).intValue();
                if (!TestContext.assertTrue("Natural Order", i1 <= i2))
                    break;
            }
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

    public static class SmallObjectAllocation extends TestCase {
        int _size;

        boolean _useStack;

        XYZ c0, c1;

        public SmallObjectAllocation(int size, boolean useStack) {
            _size = size;
            _useStack = useStack;
        }

        public CharSequence getDescription() {
            return TextBuilder.newInstance().append(
                    (!_useStack) ? "HeapContext (default) - Create "
                            : "StackContext            Create ").append(_size)
                    .append(" small objects");
        }

        public void prepare() {
            c0 = new XYZ(0.0, 0.0, 0.0);
            c1 = new XYZ(-1.0, 1.0, 0.0);
            StackContext.enter(); // Allows stack allocations.
        }

        public void execute() {
            if (_useStack) {
                for (int i = 0; i < _size; i++) { // Fibonacci sequence.
                    XYZ cn = c1.plusStack(c0);
                    c0 = c1;
                    c1 = cn;
                }
            } else {
                for (int i = 0; i < _size; i++) { // Fibonacci sequence.
                    XYZ cn = c1.plusHeap(c0);
                    c0 = c1;
                    c1 = cn;
                }
            }
        }

        public int count() {
            return _size;
        }

        public void cleanup() {
            StackContext.exit();
        }

        public void validate() {
            TestContext.assertTrue(c0.x == -c0.y);
        }

        private static final class XYZ {
            static ObjectFactory FACTORY = new ObjectFactory() {
                protected Object create() {
                    return new XYZ();
                }
            };

            double x, y, z;

            private XYZ() {
            }

            public XYZ(double x, double y, double z) {
                this.x = x;
                this.y = y;
                this.z = z;
            }

            public static XYZ valueOf(double x, double y, double z) {
                XYZ c = (XYZ) FACTORY.object();
                c.x = x;
                c.y = y;
                c.z = z;
                return c;
            }

            public XYZ plusStack(XYZ that) {
                return XYZ.valueOf(this.x + that.x, this.y + that.y, this.z
                        + that.z);
            }

            public XYZ plusHeap(XYZ that) {
                return new XYZ(this.x + that.x, this.y + that.y, this.z
                        + that.z);
            }
        }
    }

    public static class ArrayAllocation extends TestCase {
        int _size;

        boolean _recycle;

        char[] _array;

        public ArrayAllocation(int size, boolean recycle) {
            _size = size;
            _recycle = recycle;
        }

        public CharSequence getDescription() {
            if (_recycle) {
                return TextBuilder.newInstance().append(
                        "Recycled - ArrayFactory.CHARS_FACTORY.array(").append(
                        _size).append(")");
            } else {
                return TextBuilder.newInstance().append(
                        "HeapContext (default)            - new char[").append(
                        _size).append("]");
            }
        }

        public void execute() {
            if (_recycle) {
                _array = (char[]) ArrayFactory.CHARS_FACTORY.array(_size);
                ArrayFactory.CHARS_FACTORY.recycle(_array);
            } else {
                _array = new char[_size];
            }
        }

        public void validate() {
            TestContext.assertTrue(_array.length >= _size);
        }
    }
}