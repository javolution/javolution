/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2007 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution;

import j2me.lang.CharSequence;
import j2me.util.ArrayList;
import j2me.util.Iterator;
import j2me.util.Collection;
import j2me.util.Map;
import javolution.lang.Configurable;
import javolution.testing.TestCase;
import javolution.testing.TestContext;
import javolution.testing.TestSuite;
import javolution.text.TextBuilder;
import javolution.util.FastList;
import javolution.util.FastMap;
import javolution.util.FastSet;
import javolution.util.FastTable;
import javolution.util.Index;
import javolution.util.FastMap.Entry;
import javolution.util.FastList.Node;

/**
 * <p> This class holds {@link javolution.util} benchmark.</p>
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 5.2, August 19, 2007
 */
public final class UtilTestSuite extends TestSuite {

    public static final Configurable/*<Integer>*/
    SIZE = new Configurable(new Integer(256));

    private static final int PADDING = 60;

    public void run() {
        int size = ((Integer) SIZE.get()).intValue();
        Index.setMinimumRange(0, size); // Preallocates indexes.

        TestContext.info("----------------------------------------------");
        TestContext.info("-- Test Suite for javolution.util.* classes --");
        TestContext.info("----------------------------------------------");
        TestContext.info("Collections/Maps of " + size
                + " elements (configurable \"javolution.UtilTestSuite#SIZE\")");
        TestContext.info("");

        TestContext.info(" - Add elements to collection -");
        boolean reuse = false;
        TestContext.test(new CollectionAdd(FastTable.class, size, reuse));
        /*@JVM-1.4+@ TestContext.test(new CollectionAdd(java.util.ArrayList.class, size, reuse)); /**/
        TestContext.test(new CollectionAdd(FastList.class, size, reuse));
        /*@JVM-1.4+@ TestContext.test(new CollectionAdd(java.util.LinkedList.class, size, reuse)); /**/
        TestContext.test(new CollectionAdd(FastSet.class, size, reuse));
        /*@JVM-1.4+@ TestContext.test(new CollectionAdd(java.util.HashSet.class, size, reuse)); /**/
        reuse = true;
        TestContext.test(new CollectionAdd(FastTable.class, size, reuse));
        /*@JVM-1.4+@ TestContext.test(new CollectionAdd(java.util.ArrayList.class, size, reuse)); /**/
        TestContext.test(new CollectionAdd(FastList.class, size, reuse));
        /*@JVM-1.4+@ TestContext.test(new CollectionAdd(java.util.LinkedList.class, size, reuse)); /**/
        TestContext.test(new CollectionAdd(FastSet.class, size, reuse));
        /*@JVM-1.4+@ TestContext.test(new CollectionAdd(java.util.HashSet.class, size, reuse)); /**/
        TestContext.info("");

        TestContext.info(" - Iterate over collections -");
        TestContext.test(new CollectionIteration(FastTable.class, size));
        /*@JVM-1.4+@ TestContext.test(new CollectionIteration(java.util.ArrayList.class, size)); /**/
        TestContext.test(new CollectionIteration(FastList.class, size));
        /*@JVM-1.4+@ TestContext.test(new CollectionIteration(java.util.LinkedList.class, size)); /**/
        TestContext.test(new CollectionIteration(FastSet.class, size));
        /*@JVM-1.4+@ TestContext.test(new CollectionIteration(java.util.HashSet.class, size)); /**/
        TestContext.info("");

        TestContext.info(" - Put new key/value pairs to map instance -");
        reuse = false;
        TestContext.test(new MapPut(FastMap.class, size, reuse));
        /*@JVM-1.4+@ TestContext.test(new MapPut(java.util.HashMap.class, size, reuse)); /**/
        /*@JVM-1.4+@ TestContext.test(new MapPut(java.util.LinkedHashMap.class, size, reuse)); /**/
        TestContext.test(new MapPut(SharedFastMap.class, size, reuse));
        /*@JVM-1.5+@ TestContext.test(new MapPut(java.util.concurrent.ConcurrentHashMap.class, size, reuse)); /**/
        reuse = true;
        TestContext.test(new MapPut(FastMap.class, size, reuse));
        /*@JVM-1.4+@ TestContext.test(new MapPut(java.util.HashMap.class, size, reuse)); /**/
        /*@JVM-1.4+@ TestContext.test(new MapPut(java.util.LinkedHashMap.class, size, reuse)); /**/
        TestContext.info("");

        TestContext.info(" - Retrieves map value from key - ");
        TestContext.test(new MapGet(FastMap.class, size));
        /*@JVM-1.4+@ TestContext.test(new MapGet(java.util.HashMap.class, size)); /**/
        /*@JVM-1.4+@ TestContext.test(new MapGet(java.util.LinkedHashMap.class, size)); /**/
        TestContext.test(new MapGet(SharedFastMap.class, size));
        /*@JVM-1.5+@ TestContext.test(new MapGet(java.util.concurrent.ConcurrentHashMap.class, size)); /**/
        TestContext.info("");

        TestContext.info(" - Iterates over map entries - ");
        TestContext.test(new MapIteration(FastMap.class, size));
        /*@JVM-1.4+@ TestContext.test(new MapIteration(java.util.HashMap.class, size)); /**/
        /*@JVM-1.4+@ TestContext.test(new MapIteration(java.util.LinkedHashMap.class, size)); /**/
        TestContext.test(new MapIteration(SharedFastMap.class, size));
        /*@JVM-1.5+@ TestContext.test(new MapIteration(java.util.concurrent.ConcurrentHashMap.class, size)); /**/
        TestContext.info("");

        TestContext
                .info(" - Direct collection/map iterations (no iterator) - ");
        TestContext.test(new FastTableDirectIteration(size));
        TestContext.test(new ArrayListDirectIteration(size));
        TestContext.test(new FastListDirectIteration(size));
        TestContext.test(new FastMapDirectIteration(size));
        TestContext.info("");

    }

    public static class CollectionAdd extends TestCase {

        private final Class _class;

        private final int _size;

        private final boolean _reuse;

        private Collection _collection;

        public CollectionAdd(Class clazz, int size, boolean reuse) {
            _class = clazz;
            _size = size;
            _reuse = reuse;
        }

        public CharSequence getDescription() {
            return TextBuilder.newInstance().append(
                    _reuse ? "Recycled " : "New ").append(_class.getName())
                    .append(".add(element)").toText().padRight(PADDING);
        }

        public void prepare() {
            if (_reuse && _collection != null) {
                _collection.clear();
            } else {
                _collection = (Collection) UtilTestSuite.newInstanceOf(_class);
            }
        }

        public void execute() {
            for (int i = 0; i < _size;) {
                _collection.add(Index.valueOf(i++));
            }
        }

        public int count() {
            return _size;
        }

        public void validate() {
            TestContext.assertTrue(_collection.size() == _size);
            for (int i = 0; i < _size;) {
                if (!TestContext.assertTrue(_collection.contains(Index
                        .valueOf(i++))))
                    break;
            }
        }
    }

    public static class CollectionIteration extends TestCase {

        private final Class _class;

        private final int _size;

        private final Collection _collection;

        private int _count;

        public CollectionIteration(Class clazz, int size) {
            _class = clazz;
            _size = size;
            _collection = (Collection) UtilTestSuite.newInstanceOf(clazz);
            for (int i = 0; i < _size;) {
                _collection.add(Index.valueOf(i++));
            }
        }

        public CharSequence getDescription() {
            return TextBuilder.newInstance().append("Iterates over ").append(
                    _class.getName()).toText().padRight(PADDING);
        }

        public void execute() {
            _count = 0;
            Iterator i = _collection.iterator();
            while (i.hasNext()) {
                i.next();
                _count++;
            }
        }

        public int count() {
            return _count;
        }

        public void validate() {
            TestContext.assertTrue(_count == _size);
        }
    }

    public static class MapPut extends TestCase {

        private final Class _class;

        private final int _size;

        private final boolean _reuse;

        private Map _map;

        public MapPut(Class clazz, int size, boolean reuse) {
            _class = clazz;
            _size = size;
            _reuse = reuse;
        }

        public CharSequence getDescription() {
            return TextBuilder.newInstance().append(
                    _reuse ? "Recycled " : "New ").append(_class.getName())
                    .append(".put(key, value)").toText().padRight(PADDING);
        }

        public void prepare() {
            if (_reuse && _map != null) {
                _map.clear();
            } else {
                _map = (Map) UtilTestSuite.newInstanceOf(_class);
            }
        }

        public void execute() {
            for (int i = 0; i < _size;) {
                _map.put(Index.valueOf(i++), "");
            }
        }

        public int count() {
            return _size;
        }

        public void validate() {
            TestContext.assertTrue(_map.size() == _size);
            for (int i = 0; i < _size;) {
                if (!TestContext.assertTrue(_map
                        .containsKey(Index.valueOf(i++))))
                    break;
            }
        }
    }

    public static class MapGet extends TestCase {

        private final Class _class;

        private final int _size;

        private Map _map;

        private Object _last;

        public MapGet(Class clazz, int size) {
            _class = clazz;
            _size = size;
            _map = (Map) UtilTestSuite.newInstanceOf(clazz);
            for (int i = 0; i < _size;) {
                _map.put(Index.valueOf(i), Index.valueOf(++i));
            }
        }

        public CharSequence getDescription() {
            return TextBuilder.newInstance().append(_class.getName()).append(
                    ".get(key):").toText().padRight(PADDING);
        }

        public void execute() {
            for (int i = 0; i < _size;) {
                _last = _map.get(Index.valueOf(i++));
            }
        }

        public int count() {
            return _size;
        }

        public void validate() {
            for (int i = 0; i < _size; i++) {
                if (!TestContext.assertEquals(Index.valueOf(i + 1), _map
                        .get(Index.valueOf(i))))
                    break;
            }
            TestContext.assertEquals(Index.valueOf(_size), _last);
        }

    }

    public static class MapIteration extends TestCase {

        private final Class _class;

        private final int _size;

        private final Map _map;

        private Object _last;

        public MapIteration(Class clazz, int size) {
            _class = clazz;
            _size = size;
            _map = (Map) UtilTestSuite.newInstanceOf(clazz);
            for (int i = 0; i < _size;) {
                _map.put(Index.valueOf(i), Index.valueOf(i++));
            }
        }

        public CharSequence getDescription() {
            return TextBuilder.newInstance().append("Iterates over ").append(
                    _class.getName()).toText().padRight(PADDING);
        }

        public void execute() {
            Iterator i = _map.entrySet().iterator();
            while (i.hasNext()) {
                _last = i.next();
            }
        }

        public int count() {
            return _size;
        }

        public void validate() {
            Map.Entry entry = (Map.Entry) _last;
            TestContext.assertTrue(entry.getKey() == entry.getValue());
        }
    }

    public static class ArrayListDirectIteration extends TestCase {

        private final int _size;

        private final ArrayList _list;

        public ArrayListDirectIteration(int size) {
            _size = size;
            _list = new ArrayList();
            for (int i = 0; i < _size; i++) {
                _list.add(Index.valueOf(i));
            }
        }

        public CharSequence getDescription() {
            return TextBuilder.newInstance().append(
                    "java.util.ArrayList.get(i)").toText().padRight(PADDING);
        }

        public void execute() {
            Object obj = Index.valueOf(_size - 1);
            for (int i = 0; i < _size; i++) {
                if (_list.get(i) == obj) return; // Last one.                
            }
            throw new Error();
        }

        public int count() {
            return _size;
        }

        public void validate() {
            for (int i = 0; i < _size; i++) {
                if (!TestContext.assertEquals(Index.valueOf(i), _list.get(i)))
                    break; // No need to continue.
            }
        }
    }

    public static class FastTableDirectIteration extends TestCase {

        private final int _size;

        private final FastTable _list;

        public FastTableDirectIteration(int size) {
            _size = size;
            _list = new FastTable();
            for (int i = 0; i < _size; i++) {
                _list.add(Index.valueOf(i));
            }
        }

        public CharSequence getDescription() {
            return TextBuilder.newInstance().append(
                    "javolution.util.FastTable.get(i)").toText()
                    .padRight(PADDING);
        }

        public void execute() {
            Object obj = Index.valueOf(_size - 1);
            for (int i = 0; i < _size; i++) {
                if (_list.get(i) == obj) return; // Last one.                
            }
            throw new Error();
        }

        public int count() {
            return _size;
        }

        public void validate() {
            for (int i = 0; i < _size; i++) {
                if (!TestContext.assertEquals(Index.valueOf(i), _list.get(i)))
                    break; // No need to continue.
            }
        }
    }

    public static class FastListDirectIteration extends TestCase {

        private final int _size;

        private final FastList _list;

        public FastListDirectIteration(int size) {
            _size = size;
            _list = new FastList();
            for (int i = 0; i < _size; i++) {
                _list.add(Index.valueOf(i));
            }
        }

        public CharSequence getDescription() {
            return TextBuilder.newInstance().append(
                    "javolution.util.FastList.Node.getNext()").toText()
                    .padRight(PADDING);
        }

        public void execute() {
            Object obj = Index.valueOf(_size - 1);
            for (Node n = (Node) _list.head(), m = (Node) _list.tail(); (n = (Node) n
                    .getNext()) != m;) {
                if (n.getValue() == obj) return; // Last one.
            }
            throw new Error();
        }

        public int count() {
            return _size;
        }

        public void validate() {
            int count = 0;
            for (Node n = (Node) _list.head(), m = (Node) _list.tail(); (n = (Node) n
                    .getNext()) != m;) {
                if (!TestContext.assertEquals(Index.valueOf(count), n
                        .getValue()))
                    break;
                count++;
            }
            TestContext.assertTrue(count == _size);
        }
    }

    public static class FastMapDirectIteration extends TestCase {

        private final int _size;

        private final FastMap _map;

        public FastMapDirectIteration(int size) {
            _size = size;
            _map = new FastMap();
            for (int i = 0; i < _size; i++) {
                _map.put(Index.valueOf(i), Index.valueOf(i));
            }
        }

        public CharSequence getDescription() {
            return TextBuilder.newInstance().append(
                    "javolution.util.FastMap.Entry.getNext()").toText()
                    .padRight(PADDING);
        }

        public void execute() {
            Object obj = Index.valueOf(_size - 1);      
            for (Entry e = _map.head(), n = _map.tail(); (e = (Entry) e
                    .getNext()) != n;) {
                if (e.getKey() == obj) return; // Last one.
            }
            throw new Error();
        }

        public int count() {
            return _size;
        }

        public void validate() {
            int count = 0;
            for (Entry e = _map.head(), n = _map.tail(); (e = (Entry) e
                    .getNext()) != n;) {
                if (!TestContext.assertEquals(Index.valueOf(count), e
                        .getValue()))
                    break;
                count++;
            }
            TestContext.assertTrue(count == _size);
        }
    }

    private static Object newInstanceOf(Class clazz) {
        try {
            return clazz.newInstance();
        } catch (InstantiationException e) {
            throw new JavolutionError(e);
        } catch (IllegalAccessException e) {
            throw new JavolutionError(e);
        }
    }

    private static final class SharedFastMap extends FastMap {
        SharedFastMap() {
            setShared(true);
        }
    }
}