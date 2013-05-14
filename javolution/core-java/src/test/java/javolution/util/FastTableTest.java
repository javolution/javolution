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
import javolution.internal.osgi.JavolutionActivator;
import javolution.util.function.Function;
import javolution.util.function.MultiVariable;

/**
 * Validation and performance tests of FastTable.
 */
public class FastTableTest {

    static final boolean INITIALIZE_ALL = JavolutionActivator.initializeAll();

    private static final long ONE_SECOND_IN_NS = 1000 * 1000 * 1000L;

    private Perfometer perfometer = new Perfometer();

    private Random random = new Random();
    
    /** Function creating a fast table. */
    Function<Void, List<Index>> newFastTable = new Function<Void, List<Index>>() {
        public List<Index> apply(Void param) {
            return perfometer.doPerform() ? new FastTable<Index>() : null;
        }
    };

    /** Function creating an array list.*/
    Function<Void, List<Index>> newArrayList = new Function<Void, List<Index>>() {
        public List<Index> apply(Void param) {
            return perfometer.doPerform() ? new ArrayList<Index>() : null;
        }
    };

    /** Function creating a linked list. */
    Function<Void, List<Index>> newLinkedList = new Function<Void, List<Index>>() {
        public List<Index> apply(Void param) {
            return perfometer.doPerform() ? new LinkedList<Index>() : null;
        }
    };

    /** Function adding n elements to a list and returning that list. */
    Function<MultiVariable<Integer, List<Index>>, List<Index>> addToList = new Function<MultiVariable<Integer, List<Index>>, List<Index>>() {
        public List<Index> apply(MultiVariable<Integer, List<Index>> param) {
            int n = param.getLeft();
            List<Index> list = param.getRight();
            for (int i = 0; i < n; i++) {
                Index obj = Index.valueOf(i);
                if (perfometer.doPerform()) list.add(obj);
            }
            return list;
        }
    };

    /** Function inserting n elements to a list and returning that list. */
    Function<MultiVariable<Integer, List<Index>>, List<Index>> insertToList = new Function<MultiVariable<Integer, List<Index>>, List<Index>>() {
        public List<Index> apply(MultiVariable<Integer, List<Index>> param) {
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

    /** Function removing n elements from a list and returning that list. */
    Function<MultiVariable<Integer, List<Index>>, List<Index>> removeFromList = new Function<MultiVariable<Integer, List<Index>>, List<Index>>() {
        public List<Index> apply(MultiVariable<Integer, List<Index>> param) {
            int n = param.getLeft();
            List<Index> list = param.getRight();
            for (int i = 0; i < n; i++) {
                int j = random.nextInt(list.size());
                if (perfometer.doPerform()) list.remove(j);
            }
            return list;
        }
    };

    /** Function inserting n elements to a list and returning that list. */
    Function<MultiVariable<Integer, List<Index>>, List<Index>> addFirstToList = new Function<MultiVariable<Integer, List<Index>>, List<Index>>() {
        public List<Index> apply(MultiVariable<Integer, List<Index>> param) {
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
        List<Integer> ft = new FastTable<Integer>();
        List<Integer> al = new ArrayList<Integer>();
        for (long start = System.nanoTime(), time = start;
                time < start + 2 * ONE_SECOND_IN_NS; time = System.nanoTime()) {
            long seed = random.nextLong();
            Throwable found = anyListOperation(seed, ft);
            Throwable expected = anyListOperation(seed, al);
            assertEquals(found, expected);
            assert al.equals(ft) && ft.equals(al) :
                    found.getMessage() + "\nFound:    " + ft + "\nExpected: " + al;
        }
        LogContext.info("FastTable - List Operations Validated!");
    }

    public void testDequeuOperations() {
        Deque<Integer> ft = new FastTable<Integer>();
        Deque<Integer> ad = new ArrayDeque<Integer>();
        for (long start = System.nanoTime(), time = start;
                time < start + 2 * ONE_SECOND_IN_NS; time = System.nanoTime()) {
            long seed = random.nextLong();
            Throwable found = anyDequeOperation(seed, ft);
            Throwable expected = anyDequeOperation(seed, ad);
            assertEquals(found, expected);
            assert FastTableTest.areEquals(ad, ft) :
                    found.getMessage() + "\nFound:    " + ft + "\nExpected: " + ad;
        }
        LogContext.info("FastTable - Deque Operations Validated!");
    }
    private static boolean areEquals(Deque<?> left, Deque<?> right) {
        if (left.size() != right.size()) return false;
        for (Iterator<?> il = left.iterator(), ir = right.iterator(); il.hasNext();) {
            if (!il.next().equals(ir.next())) return false;
        }
        return true;            
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
                    test = "Test subList.clear" + " " + i + " " + j + " size " + list.size();
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
                    for (ListIterator<Integer> i = list.listIterator(); i.hasNext(); i.next()) {
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
                    for (Iterator<Integer> i = deque.descendingIterator(); i.hasNext(); i.next()) {
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
        expected.printStackTrace(System.err);
        assert false : "Exception mismatch found: " + found + ", expected: " + expected;
    }
}
