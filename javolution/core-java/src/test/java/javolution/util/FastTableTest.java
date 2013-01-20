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
import java.util.List;
import javolution.context.LogContext;
import javolution.lang.Functor;
import javolution.lang.MultiVariable;

public class FastTableTest {

    private static final int MAX_SIZE = 250;
    
    private Performeter performeter = new Performeter();

    // Functor creating an array list.
    Functor<Void, List<Index>> arrayListCreation = new Functor<Void, List<Index>>() {
        public List<Index> evaluate(Void param) {
            return performeter.isNOP() ? null : new ArrayList<Index>();
        }
    };
    // Functor creating a fast table.
    Functor<Void, List<Index>> fastTableCreation = new Functor<Void, List<Index>>() {
        public List<Index> evaluate(Void param) {
            return performeter.isNOP() ? null : new FastTable<Index>();
        }
    };
    // Functor adding n indices to a list.
    Functor<MultiVariable<Index, List<Index>>, Void> addToList = new Functor<MultiVariable<Index, List<Index>>, Void>() {
        public Void evaluate(MultiVariable<Index, List<Index>> param) {
            List<Index> list = param.getRight();
            for (Index i = Index.ZERO; i != param.getLeft(); i = i.next()) {
                if (!performeter.isNOP()) list.add(i);
            }
            return null;
        }
    };


    public void testCreation() {
        long ns = performeter.measure(fastTableCreation);
        long alns = performeter.measure(arrayListCreation);
        LogContext.info("FastTable Creation: ", ns, " ns (", alns, " ns for ArrayList)");
    }


    public void testListAdd() {
        for (int i = 16; i <= MAX_SIZE; i *= 2) {
            // Validation.
            List<Index> al= new ArrayList();
            addToList.evaluate(new MultiVariable(Index.valueOf(i), al));
            List<Index> ft = new FastTable();
            addToList.evaluate(new MultiVariable(Index.valueOf(i), ft));
            assert (al.equals(ft));

            long ns = performeter.measure(addToList, Index.valueOf(i), fastTableCreation);
            long alns = performeter.measure(addToList, Index.valueOf(i), arrayListCreation);
            LogContext.info("FastTable Add ", i, " elements: ", ns, " ns (", alns, " ns for ArrayList)");
        }
    }

}
