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
import java.util.Random;
import javolution.context.LogContext;
import javolution.lang.Functor;
import javolution.lang.MultiVariable;

/**
 * Validation and performance tests of FastTable.
 */
public class FastTableTest {

    private static final long ONE_SECOND_IN_NS = 1000 * 1000 * 1000L;

    private Perfometer perfometer = new Perfometer();

    private Random random = new Random();

    /** Functor creating a fast table. */
    Functor<Void, List<Index>> newFastTable = new Functor<Void, List<Index>>() {
        public List<Index> evaluate(Void param) {
            return perfometer.doPerform() ? new FastTable<Index>() : null;
        }
    };

    /** Functor creating an array list.*/
    Functor<Void, List<Index>> newArrayList = new Functor<Void, List<Index>>() {
        public List<Index> evaluate(Void param) {
            return perfometer.doPerform() ? new ArrayList<Index>() : null;
        }
    };

    /** Functor creating a linked list. */
    Functor<Void, List<Index>> newLinkedList = new Functor<Void, List<Index>>() {
        public List<Index> evaluate(Void param) {
            return perfometer.doPerform() ? new LinkedList<Index>() : null;
        }
    };

    /** Functor adding n elements to a list and returning that list. */
    Functor<MultiVariable<Integer, List<Index>>, List<Index>> addToList = new Functor<MultiVariable<Integer, List<Index>>, List<Index>>() {
        public List<Index> evaluate(MultiVariable<Integer, List<Index>> param) {
            int n = param.getLeft();
            List<Index> list = param.getRight();
            for (int i = 0; i < n; i++) {
                Index obj = Index.valueOf(i);
                if (perfometer.doPerform()) list.add(obj);
            }
            return list;
        }
    };

    /** Functor inserting n elements to a list and returning that list. */
    Functor<MultiVariable<Integer, List<Index>>, List<Index>> insertToList = new Functor<MultiVariable<Integer, List<Index>>, List<Index>>() {
        public List<Index> evaluate(MultiVariable<Integer, List<Index>> param) {
            int n = param.getLeft();
            List<Index> list = param.getRight();
            for (int i = 0; i < n; i++) {
                Index obj = Index.valueOf(i);
                int j = random.nextInt(list.size() + 1);
                if (perfometer.doPerform()) list.add(j, obj);
            }
            return list;
        }
    };

    /** Functor removing n elements from a list and returning that list. */
    Functor<MultiVariable<Integer, List<Index>>, List<Index>> removeFromList = new Functor<MultiVariable<Integer, List<Index>>, List<Index>>() {
        public List<Index> evaluate(MultiVariable<Integer, List<Index>> param) {
            int n = param.getLeft();
            List<Index> list = param.getRight();
            for (int i = 0; i < n; i++) {
                int j = random.nextInt(list.size());
                if (perfometer.doPerform()) list.remove(j);
            }
            return list;
        }
    };

    /** Functor inserting n elements to a list and returning that list. */
    Functor<MultiVariable<Integer, List<Index>>, List<Index>> addFirstToList = new Functor<MultiVariable<Integer, List<Index>>, List<Index>>() {
        public List<Index> evaluate(MultiVariable<Integer, List<Index>> param) {
            int n = param.getLeft();
            List<Index> list = param.getRight();
            for (int i = 0; i < n; i++) {
                Index obj = Index.valueOf(i);
                if (perfometer.doPerform()) list.add(0, obj);
            }
            return list;
        }
    };

    public void ttestCreation() {
        long ns = perfometer.measure(newFastTable);
        long alns = perfometer.measure(newArrayList);
        long llns = perfometer.measure(newLinkedList);
        LogContext.info("Creation (empty): ", ns, " ns (",
                alns, " ns for ArrayList, ", llns, " ns for LinkedList)");
    }

    public void ttestAddToList() {
        for (int i = 16; i <= 1024 * 256; i *= 4) {
            long ns = perfometer.measure(addToList, i, newFastTable);
            long alns = perfometer.measure(addToList, i, newArrayList);
            long llns = perfometer.measure(addToList, i, newLinkedList);
            LogContext.info("Add ", i, " elements: ", nano(ns), " (",
                    nano(alns), " for ArrayList, ", nano(llns), " for LinkedList)");
        }
    }

    public void ttestInsertToList() {
        for (int i = 16; i <= 1024 * 256; i *= 4) {
            long ns = perfometer.measure(insertToList, i, newFastTable);
            long alns = perfometer.measure(insertToList, i, newArrayList);
            long llns = (i < 1024 * 64) ? perfometer.measure(insertToList, i, newLinkedList) : -1;
            LogContext.info("Insert (at random position) ", i, " elements: ", nano(ns), " (",
                    nano(alns), " for ArrayList, ", nano(llns), " for LinkedList)");
        }
    }

    public void ttestRemoveFromList() {
        for (int i = 16; i <= 1024 * 256; i *= 4) {
            long ns = perfometer.measure(removeFromList, i, addToList, i, newFastTable);
            long alns = perfometer.measure(removeFromList, i, addToList, i, newArrayList);
            long llns = (i < 1024 * 64) ? perfometer.measure(removeFromList, i, addToList, i, newLinkedList) : -1;
            LogContext.info("Remove (at random position) ", i, " elements: ", nano(ns), " (",
                    nano(alns), " for ArrayList, ", nano(llns), " for LinkedList)");
        }
    }

    public void ttestAddFirstToList() {
        for (int i = 16; i <= 1024 * 256; i *= 4) {
            long ns = perfometer.measure(addFirstToList, i, newFastTable);
            long alns = perfometer.measure(addFirstToList, i, newArrayList);
            long llns = perfometer.measure(addFirstToList, i, newLinkedList);
            LogContext.info("Add first  ", i, " elements: ", nano(ns), " (",
                    nano(alns), " for ArrayList, ", nano(llns), " for LinkedList)");
        }
    }

    public void testListOperations() {
        List<Integer> ft = new FastTable();
        List<Integer> al = new ArrayList();
        for (long start = System.nanoTime(), time = start;
                time < start + ONE_SECOND_IN_NS; time = System.nanoTime()) {
            long seed = random.nextLong();
            Throwable found = anyListOperation(seed, ft);
            Throwable expected = anyListOperation(seed, al);
            assertEquals(found, expected);
            assert al.equals(ft) && ft.equals(al) :
                    found.getMessage() + "\nFound:    " + ft + "\nExpected: " + al;
        }
        LogContext.info("FastTable - List Operations Validated!");
    }

    public void ttestDequeuOperations() {
        Deque<Integer> ft = new FastTable();
        Deque<Integer> ad = new ArrayDeque();
        for (long start = System.nanoTime(), time = start;
                time < start + ONE_SECOND_IN_NS; time = System.nanoTime()) {
            long seed = random.nextLong();
            Throwable found = anyDequeOperation(seed, ft);
            Throwable expected = anyDequeOperation(seed, ad);
            assertEquals(found, expected);
            assert ad.containsAll(ft) && ft.containsAll(ad) :
                    found.getMessage() + "\nFound:    " + ft + "\nExpected: " + ad;
        }
        LogContext.info("FastTable - Deque Operations Validated!");
    }

    private Throwable anyListOperation(long seed, List<Integer> list) {
        random.setSeed(seed);
        int operation = random.nextInt(12);
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
                case 4: { // Clear
                    test = "Test clear()";
                    list.clear();
                    break;
                }
                case 5: { // Test indexOf/lastIndexOf
                    test = "Test indexOf/lastIndexOf";
                    int obj = random.nextInt();
                    list.add(random.nextInt(list.size() + 1), obj);
                    list.add(random.nextInt(list.size() + 1), obj);
                    int first = list.indexOf(obj);
                    int last = list.lastIndexOf(obj);
                    list.add(first);
                    list.add(last);
                    break;
                }
                case 6: {
                    test = "Test subList/addAll";
                    int s = list.size();
                    int i = random.nextInt(s);
                    int j = random.nextInt(s);
                    if (i > j) break; // ArrayList throw IllegalArgumentException 
                    // (incorrect as per List.subList contract).
                    list.addAll(list.subList(i, j));
                    break;
                }
                default: // Populate unless too big then clear.
                    if (list.size() > 100000) list.clear();
                    list.add(random.nextInt());
            }
        } catch (Throwable error) {
            return error;
        }
        return new Throwable(test);
    }

    private Throwable anyDequeOperation(long seed, Deque<Integer> deque) {
        random.setSeed(seed);
        int operation = random.nextInt(12);
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
                    test = "Test clear";
                    deque.clear();
                    break;
                }
                case 7: {
                    test = "Test descendingIterator";
                    int obj = random.nextInt();
                    for (Iterator<Integer> i = deque.descendingIterator(); i.hasNext(); i.next()) {
                        if (random.nextInt(deque.size()) == 0) obj = i.next();
                    }
                    deque.add(obj);
                    break;
                }
                default: // Populate unless too big then clear.
                    if (deque.size() > 100000) deque.clear();
                    deque.add(random.nextInt());
            }
        } catch (Throwable error) {
            return error;
        }
        System.out.println(test + " size " + deque.size());
        return new Throwable(test);
    }

    /* Utilities */
    private static String nano(long ns) {
        if (ns < 0) return "N/A";
        return commaSeparated(String.valueOf(ns)) + " ns";
    }

    private static String commaSeparated(String str) {
        int length = str.length();
        if (length <= 3) return str;
        return commaSeparated(str.substring(0, length - 3)) + ',' + str.substring(length - 3);
    }

    private static void assertEquals(Throwable found, Throwable expected) {
        if (found.getClass().equals(expected.getClass())) return;
        found.printStackTrace(System.err);
        assert false : "Exception mismatch found: " + found + ", expected: " + expected;
    }
}
