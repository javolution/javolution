/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;

import javolution.context.LogContext;
import javolution.osgi.internal.OSGiServices;
import javolution.test.Perfometer;

/**
 * Validation and performance tests of FastTable.
 */
public class FastTableTest {
    
    static final boolean INITIALIZE_REALTIME_CLASSES = OSGiServices
            .initializeRealtimeClasses();

    @SuppressWarnings("rawtypes")
    Perfometer<Class<? extends List>> addPerf = new Perfometer<Class<? extends List>>(
            "java.util.List#add(Object)") {
        List<Object> list;

        @SuppressWarnings("unchecked")
        public void initialize() throws Exception {
            list = getInput().newInstance();
        }

        protected void run(boolean measure) {
            Object obj = new Object();
            if (measure) list.add(obj);
        }
    };

    @SuppressWarnings("rawtypes")
    Perfometer<Class<? extends List>> insertPerf = new Perfometer<Class<? extends List>>(
            "java.util.List#add(int, Object)") {
        List<Object> list;
        Random random;

        @SuppressWarnings("unchecked")
        public void initialize() throws Exception {
            list = getInput().newInstance();
            random = new Random(-1);
        }

        protected void run(boolean measure) {
            Object obj = new Object();
            int i = random.nextInt(list.size() + 1);
            if (measure) list.add(i, obj);
        }

        protected void validate() {
            assert list.size() == getNbrOfIterations();
        }
    };

    @SuppressWarnings("rawtypes")
    Perfometer<Class<? extends List>> newPerf = new Perfometer<Class<? extends List>>(
            "new java.util.List()") {
        Class<? extends List> cls;

        protected void initialize() throws Exception {
            cls = getInput();
        }

        protected void run(boolean measure) throws Exception {
            if (measure) cls.newInstance();
        }
    };

    @SuppressWarnings("rawtypes")
    Perfometer<Class<? extends List>> removePerf = new Perfometer<Class<? extends List>>(
            "java.util.List#remove(int)") {
        List<Object> list;
        Random random;

        @SuppressWarnings("unchecked")
        public void initialize() throws Exception {
            list = getInput().newInstance();
            random = new Random(-1);
            for (int i = 0; i < getNbrOfIterations(); i++) {
                list.add(new Object());
            }
        }

        protected void run(boolean measure) {
            int i = random.nextInt(list.size());
            if (measure) list.remove(i);
        }

        protected void validate() {
            assert list.size() == 0;
        }
    };

    private final long ONE_SECOND_IN_NS = 1000 * 1000 * 1000L;
    private Random random = new Random();

    public void testDequeuOperations() {
        Deque<Integer> ft = new FastTable<Integer>();
        Deque<Integer> ad = new ArrayDeque<Integer>();
        for (long start = System.nanoTime(), time = start; time < start + 2
                * ONE_SECOND_IN_NS; time = System.nanoTime()) {
            long seed = random.nextLong();
            Throwable found = anyDequeOperation(seed, ft);
            Throwable expected = anyDequeOperation(seed, ad);
            assertEquals(found, expected);
            assert areEquals(ad, ft) : found.getMessage() + "\nFound:    " + ft
                    + "\nExpected: " + ad;
        }
        LogContext.info("FastTable - Deque Operations Validated!");
    }

    public void testListOperations() {
        List<Integer> ft = new FastTable<Integer>();
        List<Integer> al = new ArrayList<Integer>();
        for (long start = System.nanoTime(), time = start; time < start + 2
                * ONE_SECOND_IN_NS; time = System.nanoTime()) {
            long seed = random.nextLong();
            Throwable found = anyListOperation(seed, ft);
            Throwable expected = anyListOperation(seed, al);
            assertEquals(found, expected);
            assert al.equals(ft) && ft.equals(al) : found.getMessage()
                    + "\nFound:    " + ft + "\nExpected: " + al;
        }
        LogContext.info("FastTable - List Operations Validated!");
    }

    public void testPerformance() {
        int N = 10000;
        newPerf.measure(ArrayList.class, 1).print();
        newPerf.measure(LinkedList.class, 1).print();
        newPerf.measure(FastTable.class, 1).print();
        addPerf.measure(ArrayList.class, N).print();
        addPerf.measure(LinkedList.class, N).print();
        addPerf.measure(FastTable.class, N).print();
        insertPerf.measure(ArrayList.class, N).print();
        insertPerf.measure(LinkedList.class, N).print();
        insertPerf.measure(FastTable.class, N).print();
        removePerf.measure(ArrayList.class, N).print();
        removePerf.measure(LinkedList.class, N).print(); 
        removePerf.measure(FastTable.class, N).print();
    }

    private Throwable anyDequeOperation(long seed, Deque<Integer> deque) {
        random.setSeed(seed);
        int operation = random.nextInt(20);
        String test = "N/A";
        try {
            switch (operation) {
                case 1: {
                    test = "Test add(E)";
                    deque.add(random.nextInt());
                    break;
                }
                case 2: {
                    test = "Test addFirst(E)";
                    deque.addFirst(random.nextInt());
                    break;
                }
                case 3: {
                    test = "Test addLast(E)";
                    deque.addLast(random.nextInt());
                    break;
                }
                case 4: {
                    test = "Test removeFirst(E)";
                    deque.removeFirst();
                    break;
                }
                case 5: {
                    test = "Test removeLast(E)";
                    deque.removeLast();
                    break;
                }
                case 6: {
                    test = "Test peekFirst/peekLast/element/pop/push/pollFirst/pollLast";
                    deque.push(random.nextInt());
                    deque.addFirst(deque.peekLast());
                    deque.addLast(deque.peekFirst());
                    deque.add(deque.element());
                    deque.addFirst(deque.pop());
                    deque.push(random.nextInt());
                    deque.addLast(deque.pollFirst());
                    deque.addFirst(deque.pollLast());
                    break;
                }
                case 7: {
                    test = "Test descendingIterator";
                    int obj = random.nextInt();
                    for (Iterator<Integer> i = deque.descendingIterator(); i
                            .hasNext(); i.next()) {
                        if (random.nextInt(deque.size()) == 0) obj = i.next();
                    }
                    deque.add(obj);
                    break;
                }
                default:
                    if (deque.size() > 100000) deque.clear();
                    deque.add(random.nextInt());
            }
        } catch (Throwable error) {
            return error;
        }
        return new Throwable(test);
    }

    private Throwable anyListOperation(long seed, List<Integer> list) {
        random.setSeed(seed);
        int operation = random.nextInt(20);
        String test = "N/A";
        try {
            switch (operation) {
                case 1: {
                    test = "Test add(int, E)";
                    int i = random.nextInt(list.size() + 1);
                    list.add(i, random.nextInt());
                    break;
                }
                case 2: {
                    test = "Test remove(int)";
                    int i = random.nextInt(list.size());
                    list.remove(i);
                    break;
                }
                case 3: {
                    test = "Test add(E)";
                    list.add(random.nextInt());
                    break;
                }
                case 4: {
                    test = "Test contains(Object)";
                    int r = random.nextInt();
                    int i = random.nextInt(list.size() + 1);
                    list.add(i, r);
                    list.add(list.contains(r) ? 1 : 0);
                    break;
                }
                case 5: {
                    test = "Test indexOf/lastIndexOf";
                    int r = random.nextInt();
                    list.add(random.nextInt(list.size() + 1), r);
                    list.add(random.nextInt(list.size() + 1), r);
                    int first = list.indexOf(r);
                    int last = list.lastIndexOf(r);
                    list.add(first);
                    list.add(last);
                    break;
                }
                case 6: {
                    test = "Test subList/addAll";
                    int s = list.size();
                    int i = random.nextInt(s);
                    int j = random.nextInt(s);
                    if (i > j) break; // ArrayList throw IllegalArgumentException instead of
                    // IndexOutOfBoundsException (which is incorrect as per List.subList contract).
                    list.addAll(list.subList(i, j));
                    break;
                }
                case 7: {
                    test = "Test subList/clear";
                    int s = list.size();
                    int i = random.nextInt(s);
                    int j = random.nextInt(s);
                    if (i > j) break;
                    list.subList(i, j).clear();
                    break;
                }
                case 8: {
                    test = "Test subList/containsAll";
                    int s = list.size();
                    int i = random.nextInt(s);
                    int j = random.nextInt(s);
                    if (i > j) break;
                    boolean containsAll = list.containsAll(list.subList(i, j));
                    list.add(containsAll ? 1 : 0);
                    break;
                }
                case 9: {
                    test = "Test iterator";
                    int j = 0;
                    for (ListIterator<Integer> i = list.listIterator(); i
                            .hasNext(); i.next()) {
                        if (random.nextInt(list.size()) == 0) j = i.next();
                        if (random.nextInt(list.size()) == 0) j = i.previous();
                        if (random.nextInt(list.size()) == 0) i.remove();
                    }
                    list.add(j);
                    break;
                }
                default:
                    if (list.size() > 100000) list.clear();
                    list.add(random.nextInt());
            }
        } catch (Throwable error) {
            return error;
        }
        return new Throwable(test);
    }

    private boolean areEquals(Deque<?> left, Deque<?> right) {
        if (left.size() != right.size()) return false;
        for (Iterator<?> il = left.iterator(), ir = right.iterator(); il
                .hasNext();) {
            if (!il.next().equals(ir.next())) return false;
        }
        return true;
    }

    private void assertEquals(Throwable found, Throwable expected) {
        if (found.getClass().equals(expected.getClass())) return;
        found.printStackTrace(System.err);
        expected.printStackTrace(System.err);
        assert false : "Exception mismatch found: " + found + ", expected: "
                + expected;
    }
}
