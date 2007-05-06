/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2006 - Javolution (http://javolution.org/)
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
final class PerfUtil extends Javolution implements Runnable {

    private static final int MAX_COLLECTION_SIZE = 10000;

    private static final int ITERATIONS = 100;

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
        println("(recycled) : The collection is cleared, populated, then reused (static collections or throw-away collections in StackContext).");
        println("");

        // Creates objects collection.
        for (int i = 0; i < MAX_COLLECTION_SIZE; i++) {
            _objects[i] = new Object();
        }

        println("-- FastTable versus ArrayList -- ");
        benchmarkFastTable();
        benchmarkArrayList();

        println("-- FastList versus LinkedList -- ");
        benchmarkFastList();
        benchmarkLinkedList();
        println("");

        println("-- FastMap versus HashMap  --");
        benchmarkFastMap();
        benchmarkHashMap();
        benchmarkLinkedHashMap();
        println("");

        println("-- FastMap.setShared(true) versus ConcurrentHashMap  --");
        benchmarkSharedFastMap();
        /*@JVM-1.5+@
         benchmarkConcurrentHashMap();
         /**/
        println("");

        println("-- FastSet versus HashSet --");
        benchmarkFastSet();
        benchmarkHashSet();
        benchmarkLinkedHashSet();
        println("");

    }

    private void benchmarkFastTable() {
        FastTable list = new FastTable();
        println(list.getClass());

        for (int size = 10; size <= MAX_COLLECTION_SIZE; size *= 10) {
            final int iterations = 10 * MAX_COLLECTION_SIZE / size;
            print("    Size: " + size);

            print(", add (new): ");
            for (int n = 0; n < ITERATIONS; n++) {
                startTime();
                for (int j = 0; j < iterations; j++) {
                    list = new FastTable();
                    for (int i = 0; i < size;) {
                        list.add(_objects[i++]);
                    }
                }
                keepBestTime(size * iterations);
            }
            print(endTime());

            print(", add (recycled): ");
            for (int n = 0; n < ITERATIONS; n++) {
                startTime();
                for (int j = 0; j < iterations; j++) {
                    list.clear();
                    for (int i = 0; i < size;) {
                        list.add(_objects[i++]);
                    }
                }
                keepBestTime(size * iterations);
            }
            print(endTime());

            print(", iteration (iterator): ");
            for (int n = 0; n < ITERATIONS; n++) {
                startTime();
                for (int j = 0; j < iterations; j++) {
                    for (Iterator i = list.iterator(); i.hasNext();) {
                        if (i.next() == list)
                            throw new Error();
                    }
                }
                keepBestTime(size * iterations);
            }
            print(endTime());

            if (list instanceof RandomAccess) {
                print(", get(int): ");
                for (int n = 0; n < ITERATIONS; n++) {
                    startTime();
                    for (int j = 0; j < iterations; j++) {
                        for (int i = list.size(); --i > 0;) {
                            if (list.get(i) == list)
                                throw new Error();
                        }
                    }
                    keepBestTime(size * iterations);
                }
                print(endTime());
            }

            println("");
        }
        println("");
    }

    private void benchmarkFastList() {
        FastList list = new FastList();
        println(list.getClass());

        for (int size = 10; size <= MAX_COLLECTION_SIZE; size *= 10) {
            final int iterations = 10 * MAX_COLLECTION_SIZE / size;
            print("    Size: " + size);

            print(", add (new): ");
            for (int n = 0; n < ITERATIONS; n++) {
                startTime();
                for (int j = 0; j < iterations; j++) {
                    list = new FastList();
                    for (int i = 0; i < size;) {
                        list.add(_objects[i++]);
                    }
                }
                keepBestTime(size * iterations);
            }
            print(endTime());

            print(", add (recycled): ");
            for (int n = 0; n < ITERATIONS; n++) {
                startTime();
                for (int j = 0; j < iterations; j++) {
                    list.clear();
                    for (int i = 0; i < size;) {
                        list.add(_objects[i++]);
                    }
                }
                keepBestTime(size * iterations);
            }
            print(endTime());

            print(", iteration (iterator): ");
            for (int n = 0; n < ITERATIONS; n++) {
                startTime();
                for (int j = 0; j < iterations; j++) {
                    for (Iterator i = list.iterator(); i.hasNext();) {
                        if (i.next() == list)
                            throw new Error();
                    }
                }
                keepBestTime(size * iterations);
            }
            print(endTime());

            print(", iteration (node): ");
            FastList fl = (FastList) list;
            for (int n = 0; n < ITERATIONS; n++) {
                startTime();
                for (int j = 0; j < iterations; j++) {
                    for (FastList.Node nn = (FastList.Node) /*CAST UNNECESSARY WITH JDK1.5*/fl
                            .head(), end = (FastList.Node) /*CAST UNNECESSARY WITH JDK1.5*/fl
                            .tail(); (nn = (FastList.Node) /*CAST UNNECESSARY WITH JDK1.5*/nn
                            .getNext()) != end;) {
                        if (nn.getValue() == list)
                            throw new Error();
                    }
                }
                keepBestTime(size * iterations);
            }
            print(endTime());

            if (list instanceof RandomAccess) {
                print(", get(int): ");
                for (int n = 0; n < ITERATIONS; n++) {
                    startTime();
                    for (int j = 0; j < iterations; j++) {
                        for (int i = list.size(); --i > 0;) {
                            if (list.get(i) == list)
                                throw new Error();
                        }
                    }
                    keepBestTime(size * iterations);
                }
                print(endTime());
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

        for (int size = 10; size <= MAX_COLLECTION_SIZE; size *= 10) {
            final int iterations = 10 * MAX_COLLECTION_SIZE / size;
            print("    Size: " + size);

            print(", add (new): ");
            for (int n = 0; n < ITERATIONS; n++) {
                startTime();
                for (int j = 0; j < iterations; j++) {
                    list = new ArrayList();
                    for (int i = 0; i < size;) {
                        list.add(_objects[i++]);
                    }
                }
                keepBestTime(size * iterations);
            }
            print(endTime());

            print(", add (recycled): ");
            for (int n = 0; n < ITERATIONS; n++) {
                startTime();
                for (int j = 0; j < iterations; j++) {
                    list.clear();
                    for (int i = 0; i < size;) {
                        list.add(_objects[i++]);
                    }
                }
                keepBestTime(size * iterations);
            }
            print(endTime());

            print(", iteration (iterator): ");
            for (int n = 0; n < ITERATIONS; n++) {
                startTime();
                for (int j = 0; j < iterations; j++) {
                    for (Iterator i = list.iterator(); i.hasNext();) {
                        if (i.next() == list)
                            throw new Error();
                    }
                }
                keepBestTime(size * iterations);
            }
            print(endTime());

            if (list instanceof RandomAccess) {
                print(", get(int): ");
                for (int n = 0; n < ITERATIONS; n++) {
                    startTime();
                    for (int j = 0; j < iterations; j++) {
                        for (int i = list.size(); --i > 0;) {
                            if (list.get(i) == list)
                                throw new Error();
                        }
                    }
                    keepBestTime(size * iterations);
                }
                print(endTime());
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

        for (int size = 10; size <= MAX_COLLECTION_SIZE; size *= 10) {
            final int iterations = 10 * MAX_COLLECTION_SIZE / size;
            print("    Size: " + size);

            print(", add (new): ");
            for (int n = 0; n < ITERATIONS; n++) {
                startTime();
                for (int j = 0; j < iterations; j++) {
                    list = new LinkedList();
                    for (int i = 0; i < size;) {
                        list.add(_objects[i++]);
                    }
                }
                keepBestTime(size * iterations);
            }
            print(endTime());

            print(", add (recycled): ");
            for (int n = 0; n < ITERATIONS; n++) {
                startTime();
                for (int j = 0; j < iterations; j++) {
                    list.clear();
                    for (int i = 0; i < size;) {
                        list.add(_objects[i++]);
                    }
                }
                keepBestTime(size * iterations);
            }
            print(endTime());

            print(", iteration (iterator): ");
            for (int n = 0; n < ITERATIONS; n++) {
                startTime();
                for (int j = 0; j < iterations; j++) {
                    for (Iterator i = list.iterator(); i.hasNext();) {
                        if (i.next() == list)
                            throw new Error();
                    }
                }
                keepBestTime(size * iterations);
            }
            print(endTime());

            if (list instanceof RandomAccess) {
                print(", get(int): ");
                for (int n = 0; n < ITERATIONS; n++) {
                    startTime();
                    for (int j = 0; j < iterations; j++) {
                        for (int i = list.size(); --i > 0;) {
                            if (list.get(i) == list)
                                throw new Error();
                        }
                    }
                    keepBestTime(size * iterations);
                }
                print(endTime());
            }

            println("");
        }
        println("");
    }

    private void benchmarkFastMap() {
        FastMap map = new FastMap();
        println(map.getClass());

        for (int size = 10; size <= MAX_COLLECTION_SIZE; size *= 10) {
            final int iterations = 10 * MAX_COLLECTION_SIZE / size;
            print("    Size: " + size);

            print(", put (new): ");
            for (int n = 0; n < ITERATIONS; n++) {
                startTime();
                for (int j = 0; j < iterations; j++) {
                    map = new FastMap();
                    for (int i = 0; i < size;) {
                        map.put(_objects[i++], "");
                    }
                }
                keepBestTime(size * iterations);
            }
            print(endTime());

            print(", put (recycled): ");
            for (int n = 0; n < ITERATIONS; n++) {
                startTime();
                for (int j = 0; j < iterations; j++) {
                    map.clear();
                    for (int i = 0; i < size;) {
                        map.put(_objects[i++], "");
                    }
                }
                keepBestTime(size * iterations);
            }
            print(endTime());

            print(", get: ");
            for (int n = 0; n < ITERATIONS; n++) {
                startTime();
                for (int j = 0; j < iterations; j++) {
                    for (int i = 0; i < size;) {
                        if (map.get(_objects[i++]) != "")
                            throw new Error();
                    }
                }
                keepBestTime(size * iterations);
            }
            print(endTime());

            print(", iteration (iterator): ");
            for (int n = 0; n < ITERATIONS; n++) {
                startTime();
                for (int j = 0; j < iterations; j++) {
                    for (Iterator i = map.entrySet().iterator(); i.hasNext();) {
                        if (i.next() == map)
                            throw new Error();
                    }
                }
                keepBestTime(size * iterations);
            }
            print(endTime());

            print(", iteration (entry): ");
            for (int n = 0; n < ITERATIONS; n++) {
                startTime();
                for (int j = 0; j < iterations; j++) {
                    for (FastMap.Entry e = map.head(), end = map.tail(); (e = (FastMap.Entry) /*CAST UNNECESSARY WITH JDK1.5*/e
                            .getNext()) != end;) {
                        if (e.getValue() == map)
                            throw new Error();
                    }
                }
                keepBestTime(size * iterations);
            }
            print(endTime());

            println("");
        }
        println("");

    }

    private void benchmarkHashMap() {
        HashMap map = new HashMap();
        if (!map.getClass().getName().equals("java.util.HashMap"))
            return; // J2ME Target.
        println(map.getClass());

        for (int size = 10; size <= MAX_COLLECTION_SIZE; size *= 10) {
            final int iterations = 10 * MAX_COLLECTION_SIZE / size;
            print("    Size: " + size);

            print(", put (new): ");
            for (int n = 0; n < ITERATIONS; n++) {
                startTime();
                for (int j = 0; j < iterations; j++) {
                    map = new HashMap();
                    for (int i = 0; i < size;) {
                        map.put(_objects[i++], "");
                    }
                }
                keepBestTime(size * iterations);
            }
            print(endTime());

            print(", put (recycled): ");
            for (int n = 0; n < ITERATIONS; n++) {
                startTime();
                for (int j = 0; j < iterations; j++) {
                    map.clear();
                    for (int i = 0; i < size;) {
                        map.put(_objects[i++], "");
                    }
                }
                keepBestTime(size * iterations);
            }
            print(endTime());

            print(", get: ");
            for (int n = 0; n < ITERATIONS; n++) {
                startTime();
                for (int j = 0; j < iterations; j++) {
                    for (int i = 0; i < size;) {
                        if (map.get(_objects[i++]) != "")
                            throw new Error();
                    }
                }
                keepBestTime(size * iterations);
            }
            print(endTime());

            print(", iteration (iterator): ");
            for (int n = 0; n < ITERATIONS; n++) {
                startTime();
                for (int j = 0; j < iterations; j++) {
                    for (Iterator i = map.entrySet().iterator(); i.hasNext();) {
                        if (i.next() == map)
                            throw new Error();
                    }
                }
                keepBestTime(size * iterations);
            }
            print(endTime());

            println("");
        }
        println("");
    }

    private void benchmarkSharedFastMap() {
        FastMap map = new FastMap().setShared(true);
        println("Shared FastMap");

        for (int size = 10; size <= MAX_COLLECTION_SIZE; size *= 10) {
            final int iterations = 10 * MAX_COLLECTION_SIZE / size;
            print("    Size: " + size);

            print(", put (new): ");
            for (int n = 0; n < ITERATIONS; n++) {
                startTime();
                for (int j = 0; j < iterations; j++) {
                    map = new FastMap().setShared(true);
                    for (int i = 0; i < size;) {
                        map.put(_objects[i++], "");
                    }
                }
                keepBestTime(size * iterations);
            }
            print(endTime());

            print(", put (recycled): ");
            for (int n = 0; n < ITERATIONS; n++) {
                startTime();
                for (int j = 0; j < iterations; j++) {
                    map.clear();
                    for (int i = 0; i < size;) {
                        map.put(_objects[i++], "");
                    }
                }
                keepBestTime(size * iterations);
            }
            print(endTime());

            print(", get: ");
            for (int n = 0; n < ITERATIONS; n++) {
                startTime();
                for (int j = 0; j < iterations; j++) {
                    for (int i = 0; i < size;) {
                        if (map.get(_objects[i++]) != "")
                            throw new Error();
                    }
                }
                keepBestTime(size * iterations);
            }
            print(endTime());

            print(", iteration (iterator): ");
            for (int n = 0; n < ITERATIONS; n++) {
                startTime();
                for (int j = 0; j < iterations; j++) {
                    for (Iterator i = map.entrySet().iterator(); i.hasNext();) {
                        if (i.next() == map)
                            throw new Error();
                    }
                }
                keepBestTime(size * iterations);
            }
            print(endTime());

            print(", iteration (entry): ");
            for (int n = 0; n < ITERATIONS; n++) {
                startTime();
                for (int j = 0; j < iterations; j++) {
                    for (FastMap.Entry e = map.head(), end = map.tail(); (e = (FastMap.Entry) /*CAST UNNECESSARY WITH JDK1.5*/e
                            .getNext()) != end;) {
                        if (e.getValue() == map)
                            throw new Error();
                    }
                }
                keepBestTime(size * iterations);
            }
            print(endTime());

            println("");
        }
        println("");

    }

    /*@JVM-1.5+@
     private void benchmarkConcurrentHashMap() {
     java.util.concurrent.ConcurrentHashMap map = new java.util.concurrent.ConcurrentHashMap();
     println(map.getClass());

     for (int size = 10;  size <= MAX_COLLECTION_SIZE; size *= 10) {
     final int iterations = 10 * MAX_COLLECTION_SIZE / size;
     print("    Size: " + size);

     print(", put (new): ");
     for (int n=0; n < 10; n++) { startTime();
     for (int j = 0; j < iterations; j++) {
     map = new java.util.concurrent.ConcurrentHashMap();
     for (int i = 0; i < size;) {
     map.put(_objects[i++], "");
     }
     }
     keepBestTime(size * iterations);
     } print(endTime());

     print(", put (recycled): ");
     for (int n=0; n < 10; n++) { startTime();
     for (int j = 0; j < iterations; j++) {
     map.clear();
     for (int i = 0; i < size;) {
     map.put(_objects[i++], "");
     }
     }
     keepBestTime(size * iterations);
     } print(endTime());

     print(", get: ");
     for (int n=0; n < 10; n++) { startTime();
     for (int j = 0; j < iterations; j++) {
     for (int i = 0; i < size;) {
     if (map.get(_objects[i++]) != "")
     throw new Error();
     }
     }
     keepBestTime(size * iterations);
     } print(endTime());

     print(", iteration (iterator): ");
     for (int n=0; n < 10; n++) { startTime();
     for (int j = 0; j < iterations; j++) {
     for (Iterator i = map.entrySet().iterator(); i.hasNext();) {
     if (i.next() == map)
     throw new Error();
     }
     }
     keepBestTime(size * iterations);
     } print(endTime());

     println("");
     }
     println("");
     }
     /**/

    private void benchmarkLinkedHashMap() {
        LinkedHashMap map = new LinkedHashMap();
        if (!map.getClass().getName().equals("java.util.LinkedHashMap"))
            return; // J2ME Target.
        println(map.getClass());

        for (int size = 10; size <= MAX_COLLECTION_SIZE; size *= 10) {
            final int iterations = 10 * MAX_COLLECTION_SIZE / size;
            print("    Size: " + size);

            print(", put (new): ");
            for (int n = 0; n < ITERATIONS; n++) {
                startTime();
                for (int j = 0; j < iterations; j++) {
                    map = new LinkedHashMap();
                    for (int i = 0; i < size;) {
                        map.put(_objects[i++], "");
                    }
                }
                keepBestTime(size * iterations);
            }
            print(endTime());

            print(", put (recycled): ");
            for (int n = 0; n < ITERATIONS; n++) {
                startTime();
                for (int j = 0; j < iterations; j++) {
                    map.clear();
                    for (int i = 0; i < size;) {
                        map.put(_objects[i++], "");
                    }
                }
                keepBestTime(size * iterations);
            }
            print(endTime());

            print(", get: ");
            for (int n = 0; n < ITERATIONS; n++) {
                startTime();
                for (int j = 0; j < iterations; j++) {
                    for (int i = 0; i < size;) {
                        if (map.get(_objects[i++]) != "")
                            throw new Error();
                    }
                }
                keepBestTime(size * iterations);
            }
            print(endTime());

            print(", iteration (iterator): ");
            for (int n = 0; n < ITERATIONS; n++) {
                startTime();
                for (int j = 0; j < iterations; j++) {
                    for (Iterator i = map.entrySet().iterator(); i.hasNext();) {
                        if (i.next() == map)
                            throw new Error();
                    }
                }
                keepBestTime(size * iterations);
            }
            print(endTime());

            println("");
        }
        println("");
    }

    private void benchmarkFastSet() {
        FastSet set = new FastSet();
        println(set.getClass());

        for (int size = 10; size <= MAX_COLLECTION_SIZE; size *= 10) {
            final int iterations = 10 * MAX_COLLECTION_SIZE / size;
            print("    Size: " + size);

            print(", add (new): ");
            for (int n = 0; n < ITERATIONS; n++) {
                startTime();
                for (int j = 0; j < iterations; j++) {
                    set = new FastSet();
                    for (int i = 0; i < size;) {
                        set.add(_objects[i++]);
                    }
                }
                keepBestTime(size * iterations);
            }
            print(endTime());

            print(", add (recycled): ");
            for (int n = 0; n < ITERATIONS; n++) {
                startTime();
                for (int j = 0; j < iterations; j++) {
                    set.clear();
                    for (int i = 0; i < size;) {
                        set.add(_objects[i++]);
                    }
                }
                keepBestTime(size * iterations);
            }
            print(endTime());

            print(", contain: ");
            for (int n = 0; n < ITERATIONS; n++) {
                startTime();
                for (int j = 0; j < iterations; j++) {
                    for (int i = 0; i < size;) {
                        if (!set.contains(_objects[i++]))
                            throw new Error();
                    }
                }
                keepBestTime(size * iterations);
            }
            print(endTime());

            print(", iteration (iterator): ");
            for (int n = 0; n < ITERATIONS; n++) {
                startTime();
                for (int j = 0; j < iterations; j++) {
                    for (Iterator i = set.iterator(); i.hasNext();) {
                        if (i.next() == set)
                            throw new Error();
                    }
                }
                keepBestTime(size * iterations);
            }
            print(endTime());

            print(", iteration (record): ");
            for (int n = 0; n < ITERATIONS; n++) {
                startTime();
                for (int j = 0; j < iterations; j++) {
                    for (FastSet.Record r = set.head(), end = set.tail(); (r = r
                            .getNext()) != end;) {
                        if (set.valueOf(r) == set)
                            throw new Error();
                    }
                }
                keepBestTime(size * iterations);
            }
            print(endTime());

            println("");
        }
        println("");

    }

    private void benchmarkHashSet() {
        HashSet set = new HashSet();
        if (!set.getClass().getName().equals("java.util.HashSet"))
            return; // J2ME Target.
        println(set.getClass());

        for (int size = 10; size <= MAX_COLLECTION_SIZE; size *= 10) {
            final int iterations = 10 * MAX_COLLECTION_SIZE / size;
            print("    Size: " + size);

            print(", add (new): ");
            for (int n = 0; n < ITERATIONS; n++) {
                startTime();
                for (int j = 0; j < iterations; j++) {
                    set = new HashSet();
                    for (int i = 0; i < size;) {
                        set.add(_objects[i++]);
                    }
                }
                keepBestTime(size * iterations);
            }
            print(endTime());

            print(", add (recycled): ");
            for (int n = 0; n < ITERATIONS; n++) {
                startTime();
                for (int j = 0; j < iterations; j++) {
                    set.clear();
                    for (int i = 0; i < size;) {
                        set.add(_objects[i++]);
                    }
                }
                keepBestTime(size * iterations);
            }
            print(endTime());

            print(", contain: ");
            for (int n = 0; n < ITERATIONS; n++) {
                startTime();
                for (int j = 0; j < iterations; j++) {
                    for (int i = 0; i < size;) {
                        if (!set.contains(_objects[i++]))
                            throw new Error();
                    }
                }
                keepBestTime(size * iterations);
            }
            print(endTime());

            print(", iteration (iterator): ");
            for (int n = 0; n < ITERATIONS; n++) {
                startTime();
                for (int j = 0; j < iterations; j++) {
                    for (Iterator i = set.iterator(); i.hasNext();) {
                        if (i.next() == set)
                            throw new Error();
                    }
                }
                keepBestTime(size * iterations);
            }
            print(endTime());

            println("");
        }
        println("");

    }

    private void benchmarkLinkedHashSet() {
        LinkedHashSet set = new LinkedHashSet();
        if (!set.getClass().getName().equals("java.util.LinkedHashSet"))
            return; // J2ME Target.
        println(set.getClass());

        for (int size = 10; size <= MAX_COLLECTION_SIZE; size *= 10) {
            final int iterations = 10 * MAX_COLLECTION_SIZE / size;
            print("    Size: " + size);

            print(", add (new): ");
            for (int n = 0; n < ITERATIONS; n++) {
                startTime();
                for (int j = 0; j < iterations; j++) {
                    set = new LinkedHashSet();
                    for (int i = 0; i < size;) {
                        set.add(_objects[i++]);
                    }
                }
                keepBestTime(size * iterations);
            }
            print(endTime());

            print(", add (recycled): ");
            for (int n = 0; n < ITERATIONS; n++) {
                startTime();
                for (int j = 0; j < iterations; j++) {
                    set.clear();
                    for (int i = 0; i < size;) {
                        set.add(_objects[i++]);
                    }
                }
                keepBestTime(size * iterations);
            }
            print(endTime());

            print(", contain: ");
            for (int n = 0; n < ITERATIONS; n++) {
                startTime();
                for (int j = 0; j < iterations; j++) {
                    for (int i = 0; i < size;) {
                        if (!set.contains(_objects[i++]))
                            throw new Error();
                    }
                }
                keepBestTime(size * iterations);
            }
            print(endTime());

            print(", iteration (iterator): ");
            for (int n = 0; n < ITERATIONS; n++) {
                startTime();
                for (int j = 0; j < iterations; j++) {
                    for (Iterator i = set.iterator(); i.hasNext();) {
                        if (i.next() == set)
                            throw new Error();
                    }
                }
                keepBestTime(size * iterations);
            }
            print(endTime());

            println("");
        }
        println("");

    }
}
