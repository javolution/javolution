/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2005 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution;

import j2me.util.Iterator;
import j2me.util.List;
import j2me.util.Map;
import j2me.util.Set;

import javolution.util.FastList;
import javolution.util.FastMap;
import javolution.util.FastSet;
import javolution.util.Reflection;

/**
 * <p> This class holds {@link javolution.util} benchmark.</p>
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 2.0, November 26, 2004
 */
final class Perf_Util extends Javolution implements Runnable {

    private static final int COLLECTION_SIZE = 200;

    private final Object[] _objects = new Object[COLLECTION_SIZE];

    private static final Reflection.Constructor HASH_MAP_CONSTRUCTOR = Reflection
            .getConstructor("j2me.util.HashMap()");

    private static final Reflection.Constructor LINKED_HASH_MAP_CONSTRUCTOR = Reflection
            .getConstructor("j2me.util.LinkedHashMap()");

    private static final Reflection.Constructor HASH_SET_CONSTRUCTOR = Reflection
            .getConstructor("j2me.util.HashSet()");

    private static final Reflection.Constructor LINKED_HASH_SET_CONSTRUCTOR = Reflection
            .getConstructor("j2me.util.LinkedHashSet()");

    private static final Reflection.Constructor ARRAY_LIST_CONSTRUCTOR = Reflection
            .getConstructor("j2me.util.ArrayList()");

    private static final Reflection.Constructor LINKED_LIST_CONSTRUCTOR = Reflection
            .getConstructor("j2me.util.LinkedList()");

    /** 
     * Executes benchmark.
     */
    public void run() throws JavolutionError {
        println("//////////////////////////////");
        println("// Package: javolution.util //");
        println("//////////////////////////////");
        println("");       
        
        // Creates objects collection.
        for (int i = 0; i < COLLECTION_SIZE; i++) {
            _objects[i] = new Object();
        }

        println("-- HashMap/LinkedMap versus FastMap --");
        if (HASH_MAP_CONSTRUCTOR != null) {
            benchmarkMap((Map) HASH_MAP_CONSTRUCTOR.newInstance());
        }
        if (LINKED_HASH_MAP_CONSTRUCTOR != null) {
            benchmarkMap((Map) LINKED_HASH_MAP_CONSTRUCTOR.newInstance());
        }
        benchmarkMap(new FastMap());
        println("");

        println("-- HashSet/LinkedHashSet/TreeSet versus FastSet --");
        if (HASH_SET_CONSTRUCTOR != null) {
            benchmarkSet((Set) HASH_SET_CONSTRUCTOR.newInstance());
        }
        if (LINKED_HASH_SET_CONSTRUCTOR != null) {
            benchmarkSet((Set) LINKED_HASH_SET_CONSTRUCTOR.newInstance());
        }
        benchmarkSet(new FastSet());
        println("");

        println("-- ArrayList/LinkedList versus FastList --");
        if (ARRAY_LIST_CONSTRUCTOR != null) {
            benchmarkList((List) ARRAY_LIST_CONSTRUCTOR.newInstance());
        }
        if (LINKED_LIST_CONSTRUCTOR != null) {
            benchmarkList((List) LINKED_LIST_CONSTRUCTOR.newInstance());
        }
        benchmarkList(new FastList());
        println("");
    }

    private void benchmarkMap(Map map) {
        println(map.getClass());

        print("    Populates/clears map of " + COLLECTION_SIZE + " entries: ");
        startTime();
        for (int i = 0; i < 10000000 / COLLECTION_SIZE; i++) {
            for (int j = 0; j < COLLECTION_SIZE; j++) {
                map.put(_objects[j], _objects[j]);
            }
            map.clear();
        }
        endTime(10000000 / COLLECTION_SIZE);

        // Populates for subsequent tests.
        for (int j = 0; j < COLLECTION_SIZE; j++) {
            map.put(_objects[j], _objects[j]);
        }

        print("    Access (get): ");
        startTime();
        for (int i = 0; i < 10000000 / COLLECTION_SIZE; i++) {
            for (int j = 0; j < COLLECTION_SIZE; j++) {
                map.get(_objects[j]);
            }
        }
        endTime(10000000);

        print("    Iterates through all map entries: ");
        startTime();
        for (int i = 0; i < 10000000 / COLLECTION_SIZE; i++) {
            for (Iterator it = map.entrySet().iterator(); it.hasNext();) {
                it.next();
            }
        }
        endTime(10000000 / COLLECTION_SIZE);

        if (map instanceof FastMap) {
            print("    ");
            ((FastMap) map).printStatistics(System.out);
        }

    }

    private void benchmarkSet(Set set) {
        println(set.getClass());

        print("    Populates/clears set of " + COLLECTION_SIZE + " elements: ");
        startTime();
        for (int i = 0; i < 10000000 / COLLECTION_SIZE; i++) {
            for (int j = 0; j < COLLECTION_SIZE; j++) {
                set.add(_objects[j]);
            }
            set.clear();
        }
        endTime(10000000 / COLLECTION_SIZE);

        // Populates for subsequent tests.
        for (int j = 0; j < COLLECTION_SIZE; j++) {
            set.add(_objects[j]);
        }

        print("    Access (contains): ");
        startTime();
        for (int i = 0; i < 10000000 / COLLECTION_SIZE; i++) {
            for (int j = 0; j < COLLECTION_SIZE; j++) {
                set.contains(_objects[j]);
            }
        }
        endTime(10000000);

        print("    Iterates through all set elements: ");
        startTime();
        for (int i = 0; i < 10000000 / COLLECTION_SIZE; i++) {
            for (Iterator it = set.iterator(); it.hasNext();) {
                it.next();
            }
        }
        endTime(10000000 / COLLECTION_SIZE);

    }

    private void benchmarkList(List list) {
        println(list.getClass());

        print("    Populates/clears list of " + COLLECTION_SIZE + " elements: ");
        startTime();
        for (int i = 0; i < 10000000 / COLLECTION_SIZE; i++) {
            for (int j = 0; j < COLLECTION_SIZE; j++) {
                list.add(_objects[j]);
            }
            list.clear();
        }
        endTime(10000000 / COLLECTION_SIZE);

        // Populates for subsequent tests.
        for (int j = 0; j < COLLECTION_SIZE; j++) {
            list.add(_objects[j]);
        }

        print("    Iterates through all list elements: ");
        startTime();
        for (int i = 0; i < 10000000 / COLLECTION_SIZE; i++) {
            for (Iterator it = list.iterator(); it.hasNext();) {
                it.next();
            }
        }
        endTime(10000000 / COLLECTION_SIZE);

    }
}
