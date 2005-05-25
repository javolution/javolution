/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2005 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution;

import j2me.util.ArrayList;
import j2me.util.HashMap;
import j2me.util.HashSet;
import j2me.util.Iterator;
import j2me.util.LinkedHashMap;
import j2me.util.LinkedHashSet;
import j2me.util.LinkedList;
import j2me.util.RandomAccess;

import javolution.util.FastList;
import javolution.util.FastMap;
import javolution.util.FastSet;
import javolution.util.FastTable;

/**
 * <p> This class holds {@link javolution.util} benchmark.</p>
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 2.0, November 26, 2004
 */
final class Perf_Util extends Javolution implements Runnable {

    private static final int MAX_COLLECTION_SIZE = 10000;

    private final Object[] _objects = new Object[MAX_COLLECTION_SIZE];

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
        println("");

        // Creates objects collection.
        for (int i = 0; i < MAX_COLLECTION_SIZE; i++) {
            _objects[i] = new Object();
        }

        println("-- FastTable versus ArrayList -- ");
        setOutputStream(null); // Warming up, to avoid measuring JIT.
        benchmarkFastTable();
        setOutputStream(System.out);
        benchmarkFastTable();
        setOutputStream(null); // Warming up, to avoid measuring JIT.
        benchmarkArrayList();
        setOutputStream(System.out);
        benchmarkArrayList();

        println("-- FastList versus LinkedList -- ");
        setOutputStream(null); // Warming up, to avoid measuring JIT.
        benchmarkFastList();
        setOutputStream(System.out);
        benchmarkFastList();
        setOutputStream(null); // Warming up, to avoid measuring JIT.
        benchmarkLinkedList();
        setOutputStream(System.out);
        benchmarkLinkedList();
        println("");

        println("-- FastMap versus HashMap  --");
        setOutputStream(null); // Warming up, to avoid measuring JIT.
        benchmarkFastMap();
        setOutputStream(System.out);
        benchmarkFastMap();
        setOutputStream(null); // Warming up, to avoid measuring JIT.
        benchmarkHashMap();
        setOutputStream(System.out);
        benchmarkHashMap();
        setOutputStream(null); // Warming up, to avoid measuring JIT.
        benchmarkLinkedHashMap();
        setOutputStream(System.out);
        benchmarkLinkedHashMap();
        println("");

        println("-- FastSet versus HashSet --");
        setOutputStream(null); // Warming up, to avoid measuring JIT.
        benchmarkFastSet();
        setOutputStream(System.out);
        benchmarkFastSet();
        setOutputStream(null); // Warming up, to avoid measuring JIT.
        benchmarkHashSet();
        setOutputStream(System.out);
        benchmarkHashSet();
        setOutputStream(null); // Warming up, to avoid measuring JIT.
        benchmarkLinkedHashSet();
        setOutputStream(System.out);
        benchmarkLinkedHashSet();
        println("");

    }

    private void benchmarkFastTable() {
        FastTable list = new FastTable();
        println(list.getClass());

        for (int size = 10; size <= MAX_COLLECTION_SIZE; size *= 10) {
            final int nbrIterations = 100 * MAX_COLLECTION_SIZE / size;
            print("    Size: " + size);

            print(", add (new): ");
            startTime();
            for (int j = 0; j < nbrIterations; j++) {
                list = new FastTable();
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

            print(", iteration (iterator): ");
            startTime();
            for (int j = 0; j < nbrIterations * 10; j++) {
                for (Iterator i = list.iterator(); i.hasNext();) {
                    if (i.next() == list)
                        throw new Error();
                }
            }
            print(endTime(nbrIterations * size * 10));

            if (list instanceof RandomAccess) {
                print(", get(int): ");
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

    private void benchmarkFastList() {
        FastList list = new FastList();
        println(list.getClass());

        for (int size = 10;  size <= MAX_COLLECTION_SIZE; size *= 10) {
            final int nbrIterations = 100 * MAX_COLLECTION_SIZE / size;
            print("    Size: " + size);

            print(", add (new): ");
            startTime();
            for (int j = 0; j < nbrIterations; j++) {
                list = new FastList();
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

            print(", iteration (iterator): ");
            startTime();
            for (int j = 0; j < nbrIterations * 10; j++) {
                for (Iterator i = list.iterator(); i.hasNext();) {
                    if (i.next() == list)
                        throw new Error();
                }
            }
            print(endTime(nbrIterations * size * 10));

            print(", iteration (node): ");
            FastList fl = (FastList) list;
            startTime();
            for (int j = 0; j < nbrIterations * 10; j++) {
                for (FastList.Node n = fl.headNode(), end = fl.tailNode(); (n = n
                        .getNextNode()) != end;) {
                    if (n.getValue() == list)
                        throw new Error();
                }
            }
            print(endTime(nbrIterations * size * 10));

            if (list instanceof RandomAccess) {
                print(", get(int): ");
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

    private void benchmarkArrayList() {
        ArrayList list = new ArrayList();
        if (!list.getClass().getName().equals("java.util.ArrayList"))
            return; // J2ME Target.
        println(list.getClass());

        for (int size = 10;  size <= MAX_COLLECTION_SIZE; size *= 10) {
            final int nbrIterations = 100 * MAX_COLLECTION_SIZE / size;
            print("    Size: " + size);

            print(", add (new): ");
            startTime();
            for (int j = 0; j < nbrIterations; j++) {
                list = new ArrayList();
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

            print(", iteration (iterator): ");
            startTime();
            for (int j = 0; j < nbrIterations * 10; j++) {
                for (Iterator i = list.iterator(); i.hasNext();) {
                    if (i.next() == list)
                        throw new Error();
                }
            }
            print(endTime(nbrIterations * size * 10));

            if (list instanceof RandomAccess) {
                print(", get(int): ");
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

    private void benchmarkLinkedList() {
        LinkedList list = new LinkedList();
        if (!list.getClass().getName().equals("java.util.LinkedList"))
            return; // J2ME Target.
        println(list.getClass());

        for (int size = 10;  size <= MAX_COLLECTION_SIZE; size *= 10) {
            final int nbrIterations = 100 * MAX_COLLECTION_SIZE / size;
            print("    Size: " + size);

            print(", add (new): ");
            startTime();
            for (int j = 0; j < nbrIterations; j++) {
                list = new LinkedList();
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

            print(", iteration (iterator): ");
            startTime();
            for (int j = 0; j < nbrIterations * 10; j++) {
                for (Iterator i = list.iterator(); i.hasNext();) {
                    if (i.next() == list)
                        throw new Error();
                }
            }
            print(endTime(nbrIterations * size * 10));

            if (list instanceof RandomAccess) {
                print(", get(int): ");
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

    private void benchmarkFastMap() {
        FastMap map = new FastMap();
        println(map.getClass());

        for (int size = 10;  size <= MAX_COLLECTION_SIZE; size *= 10) {
            final int nbrIterations = 100 * MAX_COLLECTION_SIZE / size;
            print("    Size: " + size);

            print(", put (new): ");
            startTime();
            for (int j = 0; j < nbrIterations; j++) {
                map = new FastMap();
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

            print(", iteration (iterator): ");
            startTime();
            for (int j = 0; j < nbrIterations * 10; j++) {
                for (Iterator i = map.entrySet().iterator(); i.hasNext();) {
                    if (i.next() == map)
                        throw new Error();
                }
            }
            print(endTime(nbrIterations * size * 10));

            print(", iteration (entry): ");
            startTime();
            for (int j = 0; j < nbrIterations * 10; j++) {
                for (FastMap.Entry e = map.headEntry(), end = map.tailEntry(); (e = e
                        .getNextEntry()) != end;) {
                    if (e.getValue() == map)
                        throw new Error();
                }
            }
            print(endTime(nbrIterations * size * 10));

            println("");
        }
        println("");

    }

    private void benchmarkHashMap() {
        HashMap map = new HashMap();
        if (!map.getClass().getName().equals("java.util.HashMap"))
            return; // J2ME Target.
        println(map.getClass());

        for (int size = 10;  size <= MAX_COLLECTION_SIZE; size *= 10) {
            final int nbrIterations = 100 * MAX_COLLECTION_SIZE / size;
            print("    Size: " + size);

            print(", put (new): ");
            startTime();
            for (int j = 0; j < nbrIterations; j++) {
                map = new HashMap();
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

            print(", iteration (iterator): ");
            startTime();
            for (int j = 0; j < nbrIterations * 10; j++) {
                for (Iterator i = map.entrySet().iterator(); i.hasNext();) {
                    if (i.next() == map)
                        throw new Error();
                }
            }
            print(endTime(nbrIterations * size * 10));

            println("");
        }
        println("");
    }

    private void benchmarkLinkedHashMap() {
        LinkedHashMap map = new LinkedHashMap();
        if (!map.getClass().getName().equals("java.util.LinkedHashMap"))
            return; // J2ME Target.
        println(map.getClass());

        for (int size = 10;  size <= MAX_COLLECTION_SIZE; size *= 10) {
            final int nbrIterations = 100 * MAX_COLLECTION_SIZE / size;
            print("    Size: " + size);

            print(", put (new): ");
            startTime();
            for (int j = 0; j < nbrIterations; j++) {
                map = new LinkedHashMap();
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

            print(", iteration (iterator): ");
            startTime();
            for (int j = 0; j < nbrIterations * 10; j++) {
                for (Iterator i = map.entrySet().iterator(); i.hasNext();) {
                    if (i.next() == map)
                        throw new Error();
                }
            }
            print(endTime(nbrIterations * size * 10));

            println("");
        }
        println("");
    }

    private void benchmarkFastSet() {
        FastSet set = new FastSet();
        println(set.getClass());

        for (int size = 10;  size <= MAX_COLLECTION_SIZE; size *= 10) {
            final int nbrIterations = 100 * MAX_COLLECTION_SIZE / size;
            print("    Size: " + size);

            print(", add (new): ");
            startTime();
            for (int j = 0; j < nbrIterations; j++) {
                set = new FastSet();
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

            print(", iteration (iterator): ");
            startTime();
            for (int j = 0; j < nbrIterations * 10; j++) {
                for (Iterator i = set.iterator(); i.hasNext();) {
                    if (i.next() == set)
                        throw new Error();
                }
            }
            print(endTime(nbrIterations * size * 10));

            print(", iteration (record): ");
            startTime();
            for (int j = 0; j < nbrIterations * 10; j++) {
                for (FastSet.Record r = set.headRecord(), end = set
                        .tailRecord(); (r = r.getNextRecord()) != end;) {
                    if (set.valueOf(r) == set)
                        throw new Error();
                }
            }
            print(endTime(nbrIterations * size * 10));

            println("");
        }
        println("");

    }

    private void benchmarkHashSet() {
        HashSet set = new HashSet();
        if (!set.getClass().getName().equals("java.util.HashSet"))
            return; // J2ME Target.
        println(set.getClass());

        for (int size = 10;  size <= MAX_COLLECTION_SIZE; size *= 10) {
            final int nbrIterations = 100 * MAX_COLLECTION_SIZE / size;
            print("    Size: " + size);

            print(", add (new): ");
            startTime();
            for (int j = 0; j < nbrIterations; j++) {
                set = new HashSet();
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

            print(", iteration (iterator): ");
            startTime();
            for (int j = 0; j < nbrIterations * 10; j++) {
                for (Iterator i = set.iterator(); i.hasNext();) {
                    if (i.next() == set)
                        throw new Error();
                }
            }
            print(endTime(nbrIterations * size * 10));

            println("");
        }
        println("");

    }

    private void benchmarkLinkedHashSet() {
        LinkedHashSet set = new LinkedHashSet();
        if (!set.getClass().getName().equals("java.util.LinkedHashSet"))
            return; // J2ME Target.
        println(set.getClass());

        for (int size = 10;  size <= MAX_COLLECTION_SIZE; size *= 10) {
            final int nbrIterations = 100 * MAX_COLLECTION_SIZE / size;
            print("    Size: " + size);

            print(", add (new): ");
            startTime();
            for (int j = 0; j < nbrIterations; j++) {
                set = new LinkedHashSet();
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

            print(", iteration (iterator): ");
            startTime();
            for (int j = 0; j < nbrIterations * 10; j++) {
                for (Iterator i = set.iterator(); i.hasNext();) {
                    if (i.next() == set)
                        throw new Error();
                }
            }
            print(endTime(nbrIterations * size * 10));

            println("");
        }
        println("");

    }
}
