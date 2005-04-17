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
import j2me.util.RandomAccess;
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

    private static final int MAX_COLLECTION_SIZE = 100000;

    private final Object[] _objects = new Object[MAX_COLLECTION_SIZE];

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
        println("(new)      : The collection is created (using the new keyword), populated, then discarded (throw-away collections).");
        println("(recycled) : The collection is cleared, populated, then reused (static collections or throw-away collections in PoolContext).");
        println("(indirect) : Iteration through iterators.");
        println("(direct)   : Explicit iteration when applicable (e.g. ArrayList.get(i++) or Record.getNext() for FastCollection).");
        println("");

        // Creates objects collection.
        for (int i = 0; i < MAX_COLLECTION_SIZE; i++) {
            _objects[i] = new Object();
        }

        println("-- FastList versus ArrayList/LinkedList --");
        benchmarkList(new Reflection.Constructor() {
            protected Object allocate(Object[] args) {
                return new FastList();
            }
        });
        if (ARRAY_LIST_CONSTRUCTOR != null) {
            benchmarkList(ARRAY_LIST_CONSTRUCTOR);
        }
        if (LINKED_LIST_CONSTRUCTOR != null) {
            benchmarkList(LINKED_LIST_CONSTRUCTOR);
        }
        println("");

        println("-- FasMap versus HashMap/LinkedMap  --");
        benchmarkMap(new Reflection.Constructor() {
            protected Object allocate(Object[] args) {
                return new FastMap();
            }
        });
        if (HASH_MAP_CONSTRUCTOR != null) {
            benchmarkMap(HASH_MAP_CONSTRUCTOR);
        }
        if (LINKED_HASH_MAP_CONSTRUCTOR != null) {
            benchmarkMap(LINKED_HASH_MAP_CONSTRUCTOR);
        }
        println("");

        println("-- FastSet versus HashSet/LinkedHashSet --");
        benchmarkSet(new Reflection.Constructor() {
            protected Object allocate(Object[] args) {
                return new FastSet();
            }
        });
        if (HASH_SET_CONSTRUCTOR != null) {
            benchmarkSet(HASH_SET_CONSTRUCTOR);
        }
        if (LINKED_HASH_SET_CONSTRUCTOR != null) {
            benchmarkSet(LINKED_HASH_SET_CONSTRUCTOR);
        }
        println("");

    }

    private void benchmarkList(Reflection.Constructor listConstructor) {
        List list = (List) listConstructor.newInstance(); // Ensures class initialization.
        String listName = list.getClass().getName();
        println(listName);

        for (int size = 10; size <= MAX_COLLECTION_SIZE; size *= 10) {
            final int nbrIterations = 10 * MAX_COLLECTION_SIZE / size;
            print("    Size: " + size);

            print(", add (new): ");
            startTime();
            for (int j = 0; j < nbrIterations; j++) {
                list = (List) listConstructor.newInstance();
                for (int i = 0; i < size;) {
                    list.add(_objects[i++]);
                }
            }
            print(endTime(nbrIterations * size));

            print(", add (recycled): ");
            startTime();
            for (int j = 0; j < nbrIterations; j++) {
                list.clear();
                for (int i = 0; i < size;) {
                    list.add(_objects[i++]);
                }
            }
            print(endTime(nbrIterations * size));

            print(", iteration (indirect): ");
            startTime();
            for (int j = 0; j < nbrIterations * 10; j++) {
                for (Iterator i = list.iterator(); i.hasNext();) {
                    if (i.next() == list)
                        throw new Error();
                }
            }
            print(endTime(nbrIterations * size * 10));

            if (list instanceof FastList) {
                print(", iteration (direct): ");
                FastList fl = (FastList) list;
                startTime();
                for (int j = 0; j < nbrIterations * 10; j++) {
                    for (FastList.Node n = fl.headNode(), end = fl.tailNode(); (n = n.getNextNode()) != end;) {
                        if (n.getValue() == list)
                            throw new Error();
                    }
                }
                print(endTime(nbrIterations * size * 10));
            } else if (list instanceof RandomAccess) {
                print(", iteration (direct): ");
                startTime();
                for (int j = 0; j < nbrIterations * 10; j++) {
                    for (int i = list.size(); --i > 0;) {
                        if (list.get(i) == list)
                            throw new Error();
                    }
                }
                print(endTime(nbrIterations * size * 10));
            }

            println("");
        }
        println("");
    }

    private void benchmarkMap(Reflection.Constructor mapConstructor) {
        Map map = (Map) mapConstructor.newInstance(); // Ensures class initialization.
        String mapName = map.getClass().getName();
        println(mapName);

        for (int size = 10; size <= MAX_COLLECTION_SIZE; size *= 10) {
            final int nbrIterations = 10 * MAX_COLLECTION_SIZE / size;
            print("    Size: " + size);

            print(", put (new): ");
            startTime();
            for (int j = 0; j < nbrIterations; j++) {
                map = (Map) mapConstructor.newInstance();
                for (int i = 0; i < size;) {
                    map.put(_objects[i++], null);
                }
            }
            print(endTime(nbrIterations * size));

            print(", put (recycled): ");
            startTime();
            for (int j = 0; j < nbrIterations; j++) {
                map.clear();
                for (int i = 0; i < size;) {
                    map.put(_objects[i++], null);
                }
            }
            print(endTime(nbrIterations * size));

            print(", get: ");
            startTime();
            for (int j = 0; j < nbrIterations; j++) {
                for (int i = 0; i < size;) {
                    if (map.get(_objects[i++]) == map)
                        throw new Error();
                }
            }
            print(endTime(nbrIterations * size));

            print(", iteration (indirect): ");
            startTime();
            for (int j = 0; j < nbrIterations * 10; j++) {
                for (Iterator i = map.entrySet().iterator(); i.hasNext();) {
                    if (i.next() == map)
                        throw new Error();
                }
            }
            print(endTime(nbrIterations * size * 10));

            if (map instanceof FastMap) {
                FastMap fm = (FastMap) map;
                print(", iteration (direct): ");
                startTime();
                for (int j = 0; j < nbrIterations * 10; j++) {
                    for (FastMap.Entry e = fm.headEntry(), end = fm.tailEntry(); (e = e
                            .getNextEntry()) != end;) {
                        if (e.getValue() == map)
                            throw new Error();
                    }
                }
                print(endTime(nbrIterations * size * 10));
            }

            println("");
        }
        println("");

    }

    private void benchmarkSet(Reflection.Constructor setConstructor) {
        Set set = (Set) setConstructor.newInstance(); // Ensures class initialization.
        String setName = set.getClass().getName();
        println(setName);

        for (int size = 10; size <= MAX_COLLECTION_SIZE; size *= 10) {
            final int nbrIterations = 10 * MAX_COLLECTION_SIZE / size;
            print("    Size: " + size);

            print(", add (new): ");
            startTime();
            for (int j = 0; j < nbrIterations; j++) {
                set = (Set) setConstructor.newInstance();
                for (int i = 0; i < size;) {
                    set.add(_objects[i++]);
                }
            }
            print(endTime(nbrIterations * size));

            print(", add (recycled): ");
            startTime();
            for (int j = 0; j < nbrIterations; j++) {
                set.clear();
                for (int i = 0; i < size;) {
                    set.add(_objects[i++]);
                }
            }
            print(endTime(nbrIterations * size));

            print(", contain: ");
            startTime();
            for (int j = 0; j < nbrIterations; j++) {
                for (int i = 0; i < size;) {
                    if (!set.contains(_objects[i++]))
                        throw new Error();
                }
            }
            print(endTime(nbrIterations * size));

            print(", iteration (indirect): ");
            startTime();
            for (int j = 0; j < nbrIterations * 10; j++) {
                for (Iterator i = set.iterator(); i.hasNext();) {
                    if (i.next() == set)
                        throw new Error();
                }
            }
            print(endTime(nbrIterations * size * 10));

            if (set instanceof FastSet) {
                FastSet fs = (FastSet) set;
                print(", iteration (direct): ");
                startTime();
                for (int j = 0; j < nbrIterations * 10; j++) {
                    for (FastSet.Record r = fs.headRecord(), end = fs.tailRecord(); (r = r
                            .getNextRecord()) != end;) {
                        if (fs.valueOf(r) == set)
                            throw new Error();
                    }
                }
                print(endTime(nbrIterations * size * 10));
            }

            println("");
        }
        println("");

    }

}
