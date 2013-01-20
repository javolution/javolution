/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import javolution.context.LogContext;
import javolution.lang.Functor;
import javolution.lang.MultiVariable;

public class FastTableTest {

    private Performeter performeter = new Performeter();
    private Random random = new Random();
    // Functor creating a fast table.
    Functor<Void, List<Index>> fastTableCreation = new Functor<Void, List<Index>>() {
        public List<Index> evaluate(Void param) {
            return performeter.doPerform() ? new FastTable<Index>() : null;
        }
    };
    // Functor creating an array list.
    Functor<Void, List<Index>> arrayListCreation = new Functor<Void, List<Index>>() {
        public List<Index> evaluate(Void param) {
            return performeter.doPerform() ? new ArrayList<Index>() : null;
        }
    };
    // Functor creating a linked list.
    Functor<Void, List<Index>> linkedListCreation = new Functor<Void, List<Index>>() {
        public List<Index> evaluate(Void param) {
            return performeter.doPerform() ? new LinkedList<Index>() : null;
        }
    };
    // Functor adding n indices to a list.
    Functor<MultiVariable<Index, List<Index>>, Void> addToList = new Functor<MultiVariable<Index, List<Index>>, Void>() {
        public Void evaluate(MultiVariable<Index, List<Index>> param) {
            List<Index> list = param.getRight();
            for (Index i = Index.ZERO; i != param.getLeft(); i = i.next()) {
                if (performeter.doPerform()) list.add(i);
            }
            return null;
        }
    };
    // Functor inserting n elements at random to the specified list.
    Functor<MultiVariable<Index, List<Index>>, Void> insertToList = new Functor<MultiVariable<Index, List<Index>>, Void>() {
        public Void evaluate(MultiVariable<Index, List<Index>> param) {
            List<Index> list = param.getRight();
            for (Index i = Index.ZERO; i != param.getLeft(); i = i.next()) {
                int j = random.nextInt(list.size() + 1);
                if (performeter.doPerform()) list.add(j, i);
            }
            return null;
        }
    };

    public void testCreation() {
        long ns = performeter.measure(fastTableCreation);
        long alns = performeter.measure(arrayListCreation);
        long llns = performeter.measure(linkedListCreation);
        LogContext.info("Creation (empty): ", ns, " ns (",
                alns, " ns for ArrayList, ", llns, " ns for LinkedList)");
    }

    public void testAddToList() {
        for (int i = 16; i <= 1024 * 1024; i *= 16) {
            // Validation.
            List<Index> al = new ArrayList();
            addToList.evaluate(new MultiVariable(Index.valueOf(i), al));
            List<Index> ft = new FastTable();
            addToList.evaluate(new MultiVariable(Index.valueOf(i), ft));
            assert (al.equals(ft));

            long ns = performeter.measure(addToList, Index.valueOf(i), fastTableCreation);
            long alns = performeter.measure(addToList, Index.valueOf(i), arrayListCreation);
            long llns = performeter.measure(addToList, Index.valueOf(i), linkedListCreation);
            LogContext.info("Add ", i, " elements: ", nano(ns), " (",
                    nano(alns), " for ArrayList, ", nano(llns), " for LinkedList)");
        }
    }

    public void testInsertToList() {
        for (int i = 16; i <= 1024 * 64; i *= 16) {
            // Validation.
            List<Index> al = new ArrayList();
            long seed = random.nextLong();
            random.setSeed(seed);
            insertToList.evaluate(new MultiVariable(Index.valueOf(i), al));
            List<Index> ft = new FastTable();
            random.setSeed(seed);
            insertToList.evaluate(new MultiVariable(Index.valueOf(i), ft));
            assert (al.equals(ft));

            long ns = performeter.measure(insertToList, Index.valueOf(i), fastTableCreation);
            long alns = performeter.measure(insertToList, Index.valueOf(i), arrayListCreation);
            long llns = performeter.measure(insertToList, Index.valueOf(i), linkedListCreation);
            LogContext.info("Insert ", i, " elements at random: ", nano(ns), " (",
                    nano(alns), " for ArrayList, ", nano(llns), " for LinkedList)");
        }
    }
    private static String nano(long ns) {
        String str = String.valueOf(ns);
        int l = str.length();
        if (l > 3) str = str.substring(0, l-3) + ',' + str.substring(l-3, l++);
        if (l > 7) str = str.substring(0, l-7) + ',' + str.substring(l-7, l++);
        return str + " ns";
    }  
}
