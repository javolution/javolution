/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2004 - The Javolution Team (http://javolution.org/)
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javolution.realtime.PoolContext;
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

    /** 
     * Executes benchmark.
     */
    public void run() throws Javolution.InternalError {
        // Creates objects collection.
        for (int i = 0; i < COLLECTION_SIZE; i++) {
            _objects[i] = new Object();
        }
        
        println("-- HashMap/LinkedMap versus FastMap --");
        benchmarkMap("HashMap", Reflection
                .getConstructor("java.util.HashMap(int)"));
        benchmarkMap("LinkedHashMap", Reflection
                .getConstructor("java.util.LinkedHashMap(int)"));
        benchmarkMap("FastMap", new Reflection.Constructor() {
            public Object allocate(Object[] args) {
                return FastMap.newInstance(((Integer) args[0]).intValue());
            }
        });
        println("");

        println("-- HashSet/LinkedHashSet/TreeSet versus FastSet --");
        benchmarkSet("HashSet", Reflection
                .getConstructor("java.util.HashSet(int)"));
        benchmarkSet("LinkedHashSet", Reflection
                .getConstructor("java.util.LinkedHashSet(int)"));
        benchmarkSet("FastSet", new Reflection.Constructor() {
            public Object allocate(Object[] args) {
                return FastSet.newInstance(((Integer) args[0]).intValue());
            }
        });
        println("");

        println("-- ArrayList/LinkedList versus FastList --");
        benchmarkList("ArrayList", Reflection
                .getConstructor("java.util.ArrayList()"));
        benchmarkList("LinkedList", Reflection
                .getConstructor("java.util.LinkedList()"));
        benchmarkList("FastList", new Reflection.Constructor() {
            public Object allocate(Object[] args) {
                return FastList.newInstance();
            }
        });
        println("");
    }
    
    private void benchmarkMap(String mapClassName,
            Reflection.Constructor mapConstructor) {
        Map map = null;
        if (mapConstructor != null) {
            println(mapClassName);

            print("    Creates/populates map of " + COLLECTION_SIZE
                    + " entries: ");
            startTime();
            for (int i = 0; i < 10000000 / COLLECTION_SIZE; i++) {
                PoolContext.enter();
                map = (Map) mapConstructor.newInstance(new Integer(
                        COLLECTION_SIZE));
                for (int j = 0; j < COLLECTION_SIZE; j++) {
                    map.put(_objects[j], _objects[j]);
                }
                PoolContext.exit();
            }
            endTime(10000000 / COLLECTION_SIZE);
            
            print("    Access (get): ");
            map = (Map) mapConstructor
                    .newInstance(new Integer(COLLECTION_SIZE));
            for (int j = 0; j < COLLECTION_SIZE; j++) {
                map.put(_objects[j], _objects[j]);
            }
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
                print("    Statistics: ");
                ((FastMap) map).printStatistics(System.out);
            }
        }
    }

    private void benchmarkSet(String setClassName,
            Reflection.Constructor setConstructor) {
        Set set = null;
        if (setConstructor != null) {
            println(setClassName);

            print("    Creates/populates set of " + COLLECTION_SIZE
                    + " elements: ");
            startTime();
            for (int i = 0; i < 10000000 / COLLECTION_SIZE; i++) {
                PoolContext.enter();
                set = (Set) setConstructor.newInstance(new Integer(
                        COLLECTION_SIZE));
                for (int j = 0; j < COLLECTION_SIZE; j++) {
                    set.add(_objects[j]);
                }
                PoolContext.exit();
            }
            endTime(10000000 / COLLECTION_SIZE);
            
            print("    Access (contains): ");
            set = (Set) setConstructor
                    .newInstance(new Integer(COLLECTION_SIZE));
            for (int j = 0; j < COLLECTION_SIZE; j++) {
                set.add(_objects[j]);
            }
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
    }

    private void benchmarkList(String listClassName,
            Reflection.Constructor listConstructor) {
        List list = null;
        if (listConstructor != null) {
            println(listClassName);

            print("    Creates new list and appends " + COLLECTION_SIZE
                    + " elements: ");
            startTime();
            for (int i = 0; i < 10000000 / COLLECTION_SIZE; i++) {
                PoolContext.enter();
                list = (List) listConstructor.newInstance();
                for (int j = 0; j < COLLECTION_SIZE; j++) {
                    list.add(_objects[j]);
                }
                PoolContext.exit();
            }
            endTime(10000000 / COLLECTION_SIZE);

            print("    Iterates through all list elements: ");
            startTime();
            list = (List) listConstructor.newInstance();
            for (int j = 0; j < COLLECTION_SIZE; j++) {
                list.add(_objects[j]);
            }
            for (int i = 0; i < 10000000 / COLLECTION_SIZE; i++) {
                for (Iterator it = list.iterator(); it.hasNext();) {
                    it.next();
                }
            }
            endTime(10000000 / COLLECTION_SIZE);
        }
    }
}
