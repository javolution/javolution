/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2004 - The Javolution Team (http://javolution.org/)
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util;

import j2me.lang.CharSequence;
import j2me.lang.IllegalStateException;
import j2me.io.ObjectInputStream;
import j2me.io.ObjectOutputStream;
import j2me.io.Serializable;
import j2me.util.Collection;
import j2me.util.Iterator;
import j2me.util.Map;
import j2me.util.NoSuchElementException;
import j2me.util.Set;

import java.io.IOException;
import java.io.PrintStream;

import javolution.lang.Text;
import javolution.lang.TextBuilder;
import javolution.realtime.ArrayPool;
import javolution.realtime.Realtime;
import javolution.realtime.RealtimeObject;

/**
 * <p> This class represents a <code>Map</code> collection with real-time
 *     behavior. Unless the map's size exceeds its current capacity, no dynamic
 *     memory allocation is ever performed and response time is <b>extremely 
 *     fast</b> and <b>consistent</b>.</p>
 * 
 * <p> {@link FastMap} has a predictable iteration order, which is the order in
 *     which keys were inserted into the map (similar to 
 *     <code>j2me.util.LinkedHashMap</code> collection class).</p>
 * 
 * <p> Instances of this class can be allocated from the current thread stack 
 *     using the {@link #newInstance} factory method (e.g. for throw-away maps
 *     to avoid the creation cost). Collection views (values, keys and 
 *     entries) are all instances of {@link FastCollection}.</p>
 * 
 * <p> Instances of this class allocated using the {@link #newInstance} factory
 *     method do not resize automatically (the number of entry may increase but
 *     there is no re-hashing ever performed for enhanced predictability).
 *     Instances created from constructors may increase their capacity based 
 *     on the resizing policy. The resizing policy is customizable by overriding
 *     the {@link #sizeChanged} method. For example, to reduce memory footprint,
 *     the map's capacity could be maintained at ±50% of the current map's size.
 *     </p>
 * 
 * <p> {@link FastMap} assumes that the hashcode values are evenly distributed,
 *     if it is not the case (see {@link #printStatistics}), applications should
 *     use a {@link KeyComparator#UNEVEN_HASH UNEVEN_HASH} key comparator. 
 *     Custom {@link KeyComparator key comparators} are extremely useful for 
 *     value retrieval when map's keys and argument keys 
 *     are not of the same class (such as {@link String} and {@link 
 *     javolution.lang.Text Text}) and for identity maps. For example:<pre>
 *     FastMap identityMap = FastMap.newInstance(16).setKeyComparator(FastMap.KeyComparator.REFERENCE);
 *     </pre></p>
 * 
 * <p> To avoid dynamic memory allocations, {@link FastMap}
 *     maintains an internal pool of <code>Map.Entry</code> objects. 
 *     The initial size of the pool is determined by the map's capacity. 
 *     When an entry is removed from the map, it is automatically restored
 *     to the pool.</p>
 *    
 * <p> This implementation is not synchronized. Multiple threads accessing or
 *     modifying the collection must be synchronized externally.</p>
 * 
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle </a>
 * @version 1.0, October 4, 2004
 */
public class FastMap extends RealtimeObject implements Map, Serializable {

    /**
     * Holds the map's hash table.
     */
    private EntryImpl[] _entries;

    /**
     * Holds the map's current capacity (the number of entries available).
     */
    private int _capacity;

    /**
     * Holds the first pool entry, always different from null.
     * (_poolFirst._before is the last map element)
     */
    private EntryImpl _poolFirst = new EntryImpl();

    /**
     * Holds the first map entry, always different from null.
     * (_mapFirst == _poolFirst when map is empty)
     */
    private EntryImpl _mapFirst = _poolFirst;

    /**
     * Holds the current size.
     */
    private int _size;

    /**
     * Indicates if this map has constant capacity (no automatic resizing).
     */
    private boolean _hasConstantCapacity;

    /**
     * Holds the key comparator (if any).
     */
    private KeyComparator _keyComparator = KeyComparator.DEFAULT;

    /**
     * Creates a {@link FastMap} with default capacity, allocated on the heap.
     */
    public FastMap() {
        this(ArrayPool.MIN_LENGTH);
    }

    /**
     * Creates a {@link FastMap} with the specified capacity, allocated on the
     * heap. Unless the capacity is exceeded, operations on this map do not
     * allocate entries. For optimum performance, the capacity should be of the
     * same order of magnitude or larger than the expected map's size.
     * 
     * @param capacity the number of buckets in the hash table; it also defines
     *        the number of pre-allocated entries.
     */
    public FastMap(int capacity) {
        setCapacity(capacity);
    }

    /**
     * Returns a {@link FastMap} of specified capacity, allocated from the stack
     * when executing in a {@link javolution.realtime.PoolContext PoolContext}).
     * Unless the capacity is exceeded, operations on this map do not allocate
     * new entries and even so the capacity is not increased (no re-hashing) for
     * enhanced predictability.
     * 
     * @param capacity the capacity of the map to return.
     * @return a new or recycled map instance.
     */
    public static FastMap newInstance(int capacity) {
        return (FastMap) FACTORIES[ArrayPool.indexFor(capacity)].object();
    }

    private final static class FastMapFactory extends Factory {

        private final int _capacity;

        private FastMapFactory(int capacity) {
            _capacity = capacity;
        }

        public Object create() {
            FastMap fastMap = new FastMap(_capacity);
            fastMap._hasConstantCapacity = true;
            return fastMap;
        }

        public void cleanup(Object obj) {
            FastMap fastMap = (FastMap) obj;
            fastMap._keyComparator = KeyComparator.DEFAULT;
            fastMap.clear();
        }

    }

    private static final FastMapFactory[] FACTORIES;
    static {
        FACTORIES = new FastMapFactory[28];
        for (int i = FACTORIES.length; i > 0;) {
            FACTORIES[--i] = new FastMapFactory(ArrayPool.MIN_LENGTH << i);
        }
    }

    /**
     * Sets the {@link KeyComparator key comparator} to be used for this 
     * fast map.
     * 
     * @param keyComparator the key comparator.
     * @return <code>this</code>
     */
    public FastMap setKeyComparator(KeyComparator keyComparator) {
        _keyComparator = keyComparator;
        return this;
    }

    /**
     * Returns the fast iterator instance over the entries of this map
     * (<code>entrySet().fastIterator()</code>).
     *
     * @return the single reusable iterator of this map's {@link #entrySet}.
     * @see    FastCollection#fastIterator()
     */
    public Iterator fastIterator() {
        return _entrySet.fastIterator();
    }

    /**
     * Returns the number of key-value mappings in this {@link FastMap}.
     * 
     * @return this map's size.
     */
    public final int size() {
        return _size;
    }

    /**
     * Returns the number of preallocated usable entries. For resizable maps, 
     * the capacity also defines the number of slots in the hash table.
     * As long as the size is less than map's capacity, dynamic memory
     * allocation are not performed.
     * 
     * @return this map's capacity.
     */
    public final int capacity() {
        return _capacity;
    }

    /**
     * Indicates if this {@link FastMap} contains no key-value mappings.
     * 
     * @return <code>true</code> if this map contains no key-value mappings;
     *         <code>false</code> otherwise.
     */
    public final boolean isEmpty() {
        return _size == 0;
    }

    /**
     * Indicates if this {@link FastMap} contains a mapping for the specified
     * key.
     * 
     * @param key the key whose presence in this map is to be tested.
     * @return <code>true</code> if this map contains a mapping for the
     *         specified key; <code>false</code> otherwise.
     * @throws NullPointerException if the key is <code>null</code>.
     */
    public final boolean containsKey(Object key) {
        final int keyHash = _keyComparator.keyHash(key);
        EntryImpl entry = _entries[keyHash & (_entries.length - 1)];
        while (entry != null) {
            if ((key == entry._key)
                    || ((entry._keyHash == keyHash) && _keyComparator
                            .areEquals(key, entry._key))) {
                return true;
            }
            entry = entry._next;
        }
        return false;
    }

    /**
     * Indicates if this {@link FastMap} maps one or more keys to the specified
     * value.
     * 
     * @param value the value whose presence in this map is to be tested.
     * @return <code>true</code> if this map maps one or more keys to the
     *         specified value.
     * @throws NullPointerException if the key is <code>null</code>.
     */
    public final boolean containsValue(Object value) {
        for (EntryImpl entry = _mapFirst; entry != _poolFirst; entry = entry._after) {
            if (value.equals(entry._value)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the value to which this {@link FastMap} maps the specified key.
     * 
     * @param key the key whose associated value is to be returned.
     * @return the value to which this map maps the specified key, or
     *         <code>null</code> if there is no mapping for the key.
     * @throws NullPointerException if key is <code>null</code>.
     */
    public final Object get(Object key) {
        final int keyHash = (_keyComparator == KeyComparator.DEFAULT) ? key
                .hashCode() : _keyComparator.keyHash(key);
        EntryImpl entry = _entries[keyHash & (_entries.length - 1)];
        while (entry != null) {
            if ((key == entry._key)
                    || ((entry._keyHash == keyHash) && _keyComparator
                            .areEquals(key, entry._key))) {
                return entry._value;
            }
            entry = entry._next;
        }
        return null;
    }

    /**
     * Returns the entry with the specified key.
     * 
     * @param key the key whose associated entry is to be returned.
     * @return the entry for the specified key or <code>null</code> if none.
     */
    public final Entry getEntry(Object key) {
        final int keyHash = _keyComparator.keyHash(key);
        EntryImpl entry = _entries[keyHash & (_entries.length - 1)];
        while (entry != null) {
            if ((entry._keyHash == keyHash)
                    && _keyComparator.areEquals(key, entry._key)) {
                return entry;
            }
            entry = entry._next;
        }
        return null;
    }

    /**
     * Associates the specified value with the specified key in this
     * {@link FastMap}. If the {@link FastMap} previously contained a mapping
     * for this key, the old value is replaced.
     * 
     * @param key the key with which the specified value is to be associated.
     * @param value the value to be associated with the specified key.
     * @return the previous value associated with specified key, or
     *         <code>null</code> if there was no mapping for key. A
     *         <code>null</code> return can also indicate that the map
     *         previously associated <code>null</code> with the specified key.
     * @throws NullPointerException if the key is <code>null</code>.
     */
    public final Object put(Object key, Object value) {
        final int keyHash = _keyComparator.keyHash(key);
        EntryImpl entry = _entries[keyHash & (_entries.length - 1)];
        while (entry != null) {
            if ((key == entry._key)
                    || ((entry._keyHash == keyHash) && _keyComparator
                            .areEquals(key, entry._key))) {
                Object prevValue = entry._value;
                entry._value = value;
                return prevValue;
            }
            entry = entry._next;
        }
        // No previous mapping.
        addEntry(keyHash, key, value);
        return null;
    }

    /**
     * Copies all of the mappings from the specified map to this {@link FastMap}.
     * 
     * @param map the mappings to be stored in this map.
     * @throws NullPointerException the specified map is <code>null</code>,
     *         or the specified map contains <code>null</code> keys.
     */
    public final void putAll(Map map) {
        for (Iterator i = map.entrySet().iterator(); i.hasNext();) {
            Entry e = (Entry) i.next();
            put(e.getKey(), e.getValue());
        }
    }

    /**
     * Removes the mapping for this key from this {@link FastMap} if present.
     * 
     * @param key the key whose mapping is to be removed from the map.
     * @return previous value associated with specified key, or
     *         <code>null</code> if there was no mapping for key. A
     *         <code>null</code> return can also indicate that the map
     *         previously associated <code>null</code> with the specified key.
     * @throws NullPointerException if the key is <code>null</code>.
     */
    public final Object remove(Object key) {
        final int keyHash = _keyComparator.keyHash(key);
        EntryImpl entry = _entries[keyHash & (_entries.length - 1)];
        while (entry != null) {
            if ((key == entry._key)
                    || ((entry._keyHash == keyHash) && _keyComparator
                            .areEquals(key, entry._key))) {
                Object prevValue = entry._value;
                removeEntry(entry);
                return prevValue;
            }
            entry = entry._next;
        }
        return null;
    }

    /**
     * Removes all mappings from this {@link FastMap}.
     */
    public final void clear() {
        // Clears all keys, values and buckets linked lists.
        for (EntryImpl entry = _mapFirst; entry != _poolFirst; entry = entry._after) {
            entry._key = null;
            entry._value = null;
            if (entry._previous == null) { // First in bucket.
                _entries[entry._keyHash & (_entries.length - 1)] = null;
            }
        }
        // Recycles all entries.
        if (_size != 0) {
            _poolFirst = _mapFirst;
            _size = 0;
            if (!_hasConstantCapacity) {
                sizeChanged();
            }
        }
    }

    /**
     * Changes the current capacity of this {@link FastMap}. If the capacity is
     * increased, new entries are allocated and added to the pool. If the
     * capacity is decreased, entries from the pool are deallocated (and are
     * garbage collected eventually). The capacity also determined the number of
     * buckets for the hash table.
     * 
     * @param newCapacity the new capacity of this map.
     */
    public final void setCapacity(int newCapacity) {

        // Adjusts the number of entries based upon capacity.
        //
        if (newCapacity > _capacity) { // Capacity increases.
            EntryImpl mapLast = _poolFirst._before;
            for (int i = _capacity; i < newCapacity; i++) {
                EntryImpl entry = new EntryImpl();
                entry._after = _poolFirst;
                _poolFirst._before = entry;
                _poolFirst = entry;
            }
            _capacity = newCapacity;
            if (mapLast != null) {
                mapLast._after = _poolFirst;
                _poolFirst._before = mapLast;
            } else { // Empty map.
                _mapFirst = _poolFirst;
            }

        } else if (newCapacity < _capacity) { // Capacity decreases.
            // Clears cross-entry references (for gc).
            for (EntryImpl entry = _poolFirst; entry != null; entry = entry._after) {
                entry._previous = null;
                entry._next = null;
            }
            EntryImpl mapLast = _poolFirst._before;
            for (int i = newCapacity; i < _capacity; i++) {
                EntryImpl next = _poolFirst._after;
                if (next == null) {
                    break;
                } else { // Disconnects.
                    _poolFirst._after = null;
                    _poolFirst._before = null;
                    _poolFirst = next;
                    _capacity--;
                }
            }
            _poolFirst._before = mapLast;
            if (mapLast != null) {
                mapLast._after = _poolFirst;
            } else { // Empty map.
                _mapFirst = _poolFirst;
            }
        }

        // Sizes the entry table appropriatly (power of 2 >= newCapacity).
        //
        int tableLength = 16;
        while (tableLength < _capacity) {
            tableLength <<= 1;
        }
        // Checks if the hash table has to be re-sized.
        if ((_entries == null) || (_entries.length != tableLength)) {
            _entries = new EntryImpl[tableLength];
            // Repopulates the hash table.
            for (EntryImpl entry = _mapFirst; entry != _poolFirst; entry = entry._after) {
                // Connects to associated bucket.
                final int index = entry._keyHash & (tableLength - 1);
                final EntryImpl next = _entries[index];
                _entries[index] = entry;
                entry._previous = null; // Resets previous.
                entry._next = next;
                if (next != null) {
                    next._previous = entry;
                }
            }
        }
    }

    /**
     * Compares the specified object with this {@link FastMap} for equality.
     * Returns <code>true</code> if the given object is also a map and the two
     * maps represent the same mappings (regardless of collection iteration
     * order).
     * 
     * @param obj the object to be compared for equality with this map.
     * @return <code>true</code> if the specified object is equal to this map;
     *         <code>false</code> otherwise.
     */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof Map) {
            Map that = (Map) obj;
            if (this.size() == that.size()) {
                for (EntryImpl entry = _mapFirst; entry != _poolFirst; entry = entry._after) {
                    if (!that.entrySet().contains(entry)) {
                        return false;
                    }
                }
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * Returns the hash code value for this {@link FastMap}.
     * 
     * @return the hash code value for this map.
     */
    public int hashCode() {
        int code = 0;
        for (EntryImpl entry = _mapFirst; entry != _poolFirst; entry = entry._after) {
            code += entry.hashCode();
        }
        return code;
    }

    /**
     * Returns the textual representation of this {@link FastMap}.
     * 
     * @return the textual representation of the entry set.
     */
    public Text toText() {
        return _entrySet.toText();
    }

    /**
     * Prints the current statistics on this {@link FastMap}.
     * This method may help identify poorly defined hash functions.
     * An average collision of less than <code>50%</code> is typically 
     * acceptable.
     *  
     * @param out the stream to use for output (e.g. <code>System.out</code>)
     */
    public void printStatistics(PrintStream out) {
        int maxOccupancy = 0;
        int totalCollisions = 0;
        for (int i = 0; i < _entries.length; i++) {
            EntryImpl entry = _entries[i];
            int occupancy = 0;
            while (entry != null) {
                occupancy++;
                if (occupancy > maxOccupancy) {
                    maxOccupancy = occupancy;
                }
                if (occupancy > 1) {
                    totalCollisions++;
                }
                entry = entry._next;
            }
        }
        TextBuilder percentCollisions = TextBuilder.newInstance();
        percentCollisions.append(100 * totalCollisions / size());
        percentCollisions.append('%');
        synchronized (out) {
            out.print("SIZE: " + size());
            out.print(", CAPACITY: " + capacity());
            out.print(", AVG COLLISIONS: " + percentCollisions);
            out.print(", MAX SLOT OCCUPANCY: " + maxOccupancy);
            out.println();
        }
    }

    /**
     * Returns a collection view of the values contained in this {@link FastMap}.
     * The collection is backed by the map, so changes to the map are reflected
     * in the collection, and vice-versa. The collection supports element
     * removal, which removes the corresponding mapping from this map, via the
     * <code>Iterator.remove</code>,<code>Collection.remove</code>,
     * <code>removeAll</code>,<code>retainAll</code>, and
     * <code>clear</code> operations. It does not support the <code>add</code>
     * or <code>addAll</code> operations.
     * 
     * @return a collection view of the values contained in this map.
     */
    public final Collection values() {
        return _values;
    }

    private Values _values = new Values();

    private final class Values extends FastCollection {

        // Implements abstract method.    
        public final Iterator fastIterator() {
            _fastIterator.map = FastMap.this;
            _fastIterator.after = _mapFirst;
            _fastIterator.end = _poolFirst;
            return _fastIterator;
        }

        private final ValueIterator _fastIterator = new ValueIterator();

        public Iterator iterator() {
            ValueIterator i = (ValueIterator) ValueIterator.FACTORY.object();
            i.map = FastMap.this;
            i.after = _mapFirst;
            i.end = _poolFirst;
            return i;
        }

        public int size() {
            return _size;
        }

        public boolean contains(Object o) {
            return containsValue(o);
        }

        public void clear() {
            FastMap.this.clear();
        }

    }

    private static final class ValueIterator extends RealtimeObject implements
            Iterator {
        private static final Factory FACTORY = new Factory() {

            public Object create() {
                return new ValueIterator();
            }

            public void cleanup(Object obj) {
                ValueIterator valueIterator = (ValueIterator) obj;
                valueIterator.map = null;
                valueIterator.after = null;
                valueIterator.end = null;
            }

        };

        FastMap map;

        EntryImpl after;

        EntryImpl end;

        public void remove() {
            if (after._before == null)
                throw new IllegalStateException();
            map.removeEntry(after._before);
        }

        public boolean hasNext() {
            return after != end;
        }

        public Object next() {
            if (after == end)
                throw new NoSuchElementException();
            EntryImpl entry = after;
            after = entry._after;
            return entry._value;
        }
    }

    /**
     * Returns a collection view of the mappings contained in this
     * {@link FastMap}. Each element in the returned collection is a
     * <code>Map.Entry</code>. The collection is backed by the map, so
     * changes to the map are reflected in the collection, and vice-versa. The
     * collection supports element removal, which removes the corresponding
     * mapping from this map, via the <code>Iterator.remove</code>,
     * <code>Collection.remove</code>,<code>removeAll</code>,
     * <code>retainAll</code>, and <code>clear</code> operations. It does
     * not support the <code>add</code> or <code>addAll</code> operations.
     * 
     * @return a collection view of the mappings contained in this map.
     */
    public final Set entrySet() {
        return _entrySet;
    }

    private EntrySet _entrySet = new EntrySet();

    private final class EntrySet extends FastCollection implements Set {

        // Implements abstract method.    
        public final Iterator fastIterator() {
            _fastIterator.map = FastMap.this;
            _fastIterator.after = _mapFirst;
            _fastIterator.end = _poolFirst;
            return _fastIterator;
        }

        private final EntryIterator _fastIterator = new EntryIterator();

        public Iterator iterator() {
            EntryIterator i = (EntryIterator) EntryIterator.FACTORY.object();
            i.map = FastMap.this;
            i.after = _mapFirst;
            i.end = _poolFirst;
            return i;
        }

        public int size() {
            return _size;
        }

        public void clear() {
            FastMap.this.clear();
        }

        public boolean contains(Object obj) { // Optimization.
            if (obj instanceof Entry) {
                Entry entry = (Entry) obj;
                Entry mapEntry = getEntry(entry.getKey());
                return entry.equals(mapEntry);
            } else {
                return false;
            }
        }

        public boolean remove(Object obj) { // Optimization.
            if (obj instanceof Entry) {
                Entry entry = (Entry) obj;
                EntryImpl mapEntry = (EntryImpl) getEntry(entry.getKey());
                if ((mapEntry != null)
                        && (entry.getValue()).equals(mapEntry._value)) {
                    removeEntry(mapEntry);
                    return true;
                }
            }
            return false;
        }

        public Text toText() {
            TextBuilder tb = TextBuilder.newInstance();
            tb.append("[");
            Iterator itr = iterator();
            for (int i = size(); i > 0;) {
                EntryImpl entry = (EntryImpl) itr.next();
                tb.append(entry._key);
                tb.append('=');
                tb.append(entry._value);
                if (--i != 0) {
                    tb.append(", ");
                }
            }
            tb.append("]");
            return tb.toText();
        }
    }

    private static final class EntryIterator extends RealtimeObject implements
            Iterator {

        private static final Factory FACTORY = new Factory() {

            public Object create() {
                return new EntryIterator();
            }

            public void cleanup(Object obj) {
                EntryIterator entryIterator = (EntryIterator) obj;
                entryIterator.map = null;
                entryIterator.after = null;
                entryIterator.end = null;
            }
        };

        FastMap map;

        EntryImpl after;

        EntryImpl end;

        public void remove() {
            if (after._before == null)
                throw new IllegalStateException();
            map.removeEntry(after._before);
        }

        public boolean hasNext() {
            return after != end;
        }

        public Object next() {
            if (after == end)
                throw new NoSuchElementException();
            EntryImpl entry = after;
            after = entry._after;
            return entry;
        }
    }

    /**
     * Returns a set view of the keys contained in this {@link FastMap}. The
     * set is backed by the map, so changes to the map are reflected in the set,
     * and vice-versa. The set supports element removal, which removes the
     * corresponding mapping from this map, via the <code>Iterator.remove</code>,
     * <code>Collection.remove</code>,<code>removeAll</code>,
     * <code>retainAll</code>, and <code>clear</code> operations. It does
     * not support the <code>add</code> or <code>addAll</code> operations.
     * 
     * @return a set view of the keys contained in this map.
     */
    public final Set keySet() {
        return _keySet;
    }

    private KeySet _keySet = new KeySet();

    private final class KeySet extends FastCollection implements Set {

        // Implements abstract method.    
        public final Iterator fastIterator() {
            _fastIterator.map = FastMap.this;
            _fastIterator.after = _mapFirst;
            _fastIterator.end = _poolFirst;
            return _fastIterator;
        }

        private final KeyIterator _fastIterator = new KeyIterator();

        public Iterator iterator() {
            KeyIterator i = (KeyIterator) KeyIterator.FACTORY.object();
            i.map = FastMap.this;
            i.after = _mapFirst;
            i.end = _poolFirst;
            return i;
        }

        public int size() {
            return _size;
        }

        public void clear() {
            FastMap.this.clear();
        }

        public boolean contains(Object obj) { // Optimization.
            return FastMap.this.containsKey(obj);
        }

        public boolean remove(Object obj) { // Optimization.
            return FastMap.this.remove(obj) != null;
        }
    }

    private static final class KeyIterator extends RealtimeObject implements
            Iterator {

        private static final Factory FACTORY = new Factory() {

            public Object create() {
                return new KeyIterator();
            }

            public void cleanup(Object obj) {
                KeyIterator keyIterator = (KeyIterator) obj;
                keyIterator.map = null;
                keyIterator.after = null;
                keyIterator.end = null;
            }

        };

        FastMap map;

        EntryImpl after;

        EntryImpl end;

        public void remove() {
            if (after._before != null) {
                map.removeEntry(after._before);
            } else {
                throw new IllegalStateException();
            }
        }

        public boolean hasNext() {
            return after != end;
        }

        public Object next() {
            if (after == end)
                throw new NoSuchElementException();
            EntryImpl entry = after;
            after = entry._after;
            return entry._key;
        }
    }

    /**
     * This methods is called when the size of a {@link FastMap} changes. The
     * default behavior is to double the map's capacity when the map's size
     * exceeds the current map's capacity, unless the map has been created using
     * the {@link #newInstance}factory method.
     * 
     * Sub-class may override this method to implement custom resizing policies
     * or to disable automatic resizing. For example:<pre>
     * Map lowFootprintMap = new FastMap() {
     *     protected sizeChanged() {
     *         // Increases/decreases capacity according to current size.
     *          if (size() > 2 * capacity()) {
     *              setCapacity(2 * capacity());
     *          } else if ((capacity() >= 32) && (2 * size() < capacity()){
     *              setCapacity(capacity() / 2)
     *          }  
     *     }
     * };</pre>
     * 
     * @see #setCapacity
     */
    protected void sizeChanged() {
        if (size() > capacity()) {
            setCapacity(capacity() * 2);
        }
    }

    /**
     * Adds a new entry for the specified key and value.
     * 
     * @param hash the hash of the key, generated with {@link #keyHash}.
     * @param key the entry's key.
     * @param value the entry's value.
     */
    private void addEntry(int hash, Object key, Object value) {
        final EntryImpl entry = _poolFirst;

        // Setups entry parameters.
        entry._keyHash = hash;
        entry._key = key;
        entry._value = value;
        final int index = hash & (_entries.length - 1);

        // Connects to bucket.
        EntryImpl next = _entries[index];
        entry._next = next;
        if (next != null) {
            next._previous = entry;
        }
        entry._previous = null;
        _entries[index] = entry;

        // Moves pool index.
        _poolFirst = _poolFirst._after;
        if (_poolFirst == null) { // Capacity exceeded.
            _poolFirst = new EntryImpl();
            _poolFirst._before = entry;
            entry._after = _poolFirst;
            _capacity++;
        }

        // Updates size.
        _size++;
        if (!_hasConstantCapacity) {
            sizeChanged();
        }
    }

    /**
     * Removes the specified entry from the map.
     * 
     * @param entry the entry to be removed.
     */
    private void removeEntry(EntryImpl entry) {
        // Clears value and key.
        entry._key = null;
        entry._value = null;

        // Removes from bucket.
        final EntryImpl previous = entry._previous;
        final EntryImpl next = entry._next;
        if (previous != null) {
            previous._next = next;
        } else { // First in bucket.
            _entries[entry._keyHash & (_entries.length - 1)] = next;
        }
        if (next != null) {
            next._previous = previous;
        } // Else do nothing, last entry in bucket.

        // Removes from collection.
        if (entry._after == _poolFirst) { // Last in collection.
            _poolFirst = entry;
        } else {
            // Detaches.
            final EntryImpl before = entry._before;
            final EntryImpl after = entry._after;
            if (before != null) {
                before._after = after;
            } else { // First in collection.
                _mapFirst = after;
            }
            // after != null; cannot be last in collection.
            after._before = before;

            // Inserts in pool.
            final EntryImpl last = _poolFirst._before;
            entry._after = _poolFirst;
            entry._before = last;
            _poolFirst._before = entry;
            last._after = entry;
            _poolFirst = entry;
        }

        // Updates size.
        _size--;
        if (!_hasConstantCapacity) {
            sizeChanged();
        }
    }

    /**
     * Requires special handling during de-serialization process.
     *
     * @param  stream the object input stream.
     * @throws IOException if an I/O error occurs.
     * @throws ClassNotFoundException if the class for the object de-serialized
     *         is not found.
     */
    private void readObject(ObjectInputStream stream) throws IOException,
            ClassNotFoundException {
        // Initializes fields.
        _poolFirst = new EntryImpl();
        _mapFirst = _poolFirst;
        _values = new Values();
        _entrySet = new EntrySet();
        _keySet = new KeySet();

        // Reads data.
        int capacity = stream.readInt();
        setCapacity(capacity);
        int size = stream.readInt();
        for (int i = 0; i < size; i++) {
            Object key = stream.readObject();
            Object value = stream.readObject();
            addEntry(_keyComparator.keyHash(key), key, value);
        }
    }

    /**
     * Requires special handling during serialization process.
     *
     * @param  stream the object output stream.
     * @throws IOException if an I/O error occurs.
     */
    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.writeInt(_capacity);
        stream.writeInt(_size);
        for (EntryImpl entry = _mapFirst; entry != _poolFirst; entry = entry._after) {
            stream.writeObject(entry._key);
            stream.writeObject(entry._value);
        }
    }

    // Overrides.
    public void move(ContextSpace cs) {
        super.move(cs);
        for (EntryImpl entry = _mapFirst; entry != _poolFirst; entry = entry._after) {
            if (entry._key instanceof Realtime) {
                ((Realtime) entry._key).move(cs);
            }
            if (entry._value instanceof Realtime) {
                ((Realtime) entry._value).move(cs);
            }
        }
    }

    /**
     * This class represents a {@link FastMap} entry.
     */
    private final class EntryImpl implements Entry {

        /**
         * Holds the entry key (null when in pool).
         */
        private Object _key;

        /**
         * Holds the entry value (null when in pool).
         */
        private Object _value;

        /**
         * Holds the key hash code.
         */
        private int _keyHash;

        /**
         * Holds the previous entry in the same bucket (null when in pool).
         */
        private EntryImpl _previous;

        /**
         * Holds the next entry in the same bucket (null when in pool).
         */
        private EntryImpl _next;

        /**
         * Holds the entry added before this entry (null when in pool).
         */
        private EntryImpl _before;

        /**
         * Holds the entry added after this entry or the next available entry
         * when in pool.
         */
        private EntryImpl _after;

        /**
         * Returns the key for this entry.
         * 
         * @return the entry's key.
         */
        public Object getKey() {
            return _key;
        }

        /**
         * Returns the value for this entry.
         * 
         * @return the entry's value.
         */
        public Object getValue() {
            return _value;
        }

        /**
         * Sets the value for this entry.
         * 
         * @param value the new value.
         * @return the previous value.
         */
        public Object setValue(Object value) {
            Object old = _value;
            _value = value;
            return old;
        }

        /**
         * Indicates if this entry is considered equals to the specified entry.
         * 
         * @param that the object to test for equality.
         * @return <code>true<code> if both entry are considered equal;
         *         <code>false<code> otherwise.
         */
        public boolean equals(Object that) {
            if (that instanceof Entry) {
                Entry entry = (Entry) that;
                return (_keyComparator.areEquals(_key, entry.getKey()))
                        && ((_value != null) ? _value.equals(entry.getValue())
                                : (entry.getValue() == null));
            } else {
                return false;
            }
        }

        /**
         * Returns the hash code for this entry.
         * 
         * @return this entry's hash code.
         */
        public int hashCode() {
            return _keyHash ^ ((_value != null) ? _value.hashCode() : 0);
        }
    }

    /**
     * This class represents a {@link FastMap} custom key comparator
     * (such as for identity maps, value retrieval using keys of different class
     * that the map's keys, etc).
     */
    public interface KeyComparator {

        /**
         * Holds the default key comparator, it assumes that key hash codes are 
         * evenly distributed (otherwise {@link #UNEVEN_HASH} should be used
         * instead); two keys k1 and k2 are considered equal if and only if 
         * <code>k1.equals(k2)</code>.
         */
        public static final KeyComparator DEFAULT = new KeyComparator() {
            public int keyHash(Object key) {
                return key.hashCode();
            }

            public boolean areEquals(Object getKey, Object mapKey) {
                return getKey.equals(mapKey);
            }
        };

        /**
         * Holds the key comparator for identity maps; two keys k1 and k2 are 
         * considered equal if and only if <code>(k1 == k2)</code>.
         */
        public static final KeyComparator REFERENCE = new KeyComparator() {
            public int keyHash(Object key) {
                return System.identityHashCode(key);
            }

            public boolean areEquals(Object getKey, Object mapKey) {
                return getKey == mapKey;
            }
        };

        /**
         * Holds the key comparator for object with uneven hash distribution
         * (default comparator for non-Sun VM).
         */
        public static final KeyComparator UNEVEN_HASH = new KeyComparator() {
            public int keyHash(Object key) {
                // The formula being used is identical to the formula 
                // used by <code>j2me.util.HashMap</code> to ensures similar
                // behavior for ill-conditioned hashcode keys. 
                int h = key.hashCode();
                h += ~(h << 9);
                h ^= (h >>> 14);
                h += (h << 4);
                return h ^ (h >>> 10);
            }

            public boolean areEquals(Object getKey, Object mapKey) {
                return getKey.equals(mapKey);
            }
        };

        /**
         * Holds the key comparator for {@link CharSequence} keys 
         * (two keys are considered equals if and only if they represents 
         * the same character sequence). It is assumed that identical character 
         * sequences have the same hash code (regardless of their actual class).
         * 
         * <p>Note: This comparator supports <code>String</code> even so
         *         <code>String</code> were not a <code>CharSequence</code>
         *         prior to JDK1.4.</p>
         */
        public static final KeyComparator CHAR_SEQUENCE = new KeyComparator() {
            public int keyHash(Object key) {
                return key.hashCode();
            }

            public boolean areEquals(Object getKey, Object mapKey) {
                if (getKey instanceof String) {
                    if (mapKey instanceof String) {
                        return getKey.equals(mapKey);
                    } else {
                        String str = (String) getKey;
                        CharSequence chars = (CharSequence) mapKey;
                        final int length = str.length();
                        if (length == chars.length()) {
                            for (int i = 0; i < length;) {
                                if (chars.charAt(i) != str.charAt(i++)) {
                                    return false;
                                }
                            }
                            return true;
                        }
                        return false;
                    }
                } else {
                    if (mapKey instanceof String) {
                        return areEquals(mapKey, getKey);
                    } else {
                        CharSequence getChars = (CharSequence) getKey;
                        CharSequence mapChars = (CharSequence) mapKey;
                        final int length = mapChars.length();
                        if (length == getChars.length()) {
                            for (int i = 0; i < length;) {
                                if (mapChars.charAt(i) != getChars.charAt(i++)) {
                                    return false;
                                }
                            }
                            return true;
                        }
                        return false;
                    }
                }
            }
        };

        /**
         * Returns the hash code for the specified key.
         * 
         * @param key the key to return the hashcode for.
         * @return the hashcode for the specified key.
         */
        int keyHash(Object key);

        /**
         * Indicates if the specified keys can be considered equals.
         * 
         * @param getKey the accessor key.
         * @param mapKey the map key.
         * @return <code>true</code> if both key are considered equals;
         *         <code>false</code> otherwise. 
         */
        boolean areEquals(Object getKey, Object mapKey);
    }

    private static final long serialVersionUID = 3258412815931684401L;
}