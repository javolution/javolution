/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2005 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util;

import j2me.io.ObjectInputStream;
import j2me.io.ObjectOutputStream;
import j2me.io.Serializable;
import j2me.lang.IllegalStateException;
import j2me.util.Collection;
import j2me.util.Iterator;
import j2me.util.Map;
import j2me.util.NoSuchElementException;
import j2me.util.Set;

import java.io.IOException;
import java.io.PrintStream;

import javolution.Configuration;
import javolution.lang.Reusable;
import javolution.lang.Text;
import javolution.lang.TextBuilder;
import javolution.realtime.ObjectFactory;
import javolution.realtime.Realtime;
import javolution.realtime.RealtimeObject;

/**
 * <p> This class represents a <code>Map</code> collection with real-time
 *     behavior (smooth capacity increase with no rehashing).</p>
 *     
 * <p> {@link FastMap} has a predictable iteration order, which is the order in
 *     which keys are inserted into the map (similar to 
 *     <code>java.util.LinkedHashMap</code> collection class).</p>
 * 
 * <p> Instances of this class can be allocated from the current thread stack 
 *     using the {@link #newInstance} factory method (e.g. for throw-away maps
 *     to avoid the creation cost). Collection views (values, keys and 
 *     entries) are instances of {@link FastCollection} and can be iterated 
 *     over using (non thread-safe) iterators:
 *     {@link #fastValueIterator() fastValueIterator()}, 
 *     {@link #fastKeyIterator() fastKeyIterator()} and  
 *     {@link #fastEntryIterator() fastEntryIterator()}.</p>
 * 
 * <p> {@link FastMap} may use custom key comparators; the default comparator is
 *     either {@link FastComparator#DEFAULT} or {@link FastComparator#REHASH}
 *      based upon the current <a href=
 *     "{@docRoot}/overview-summary.html#configuration">Javolution 
 *     Configuration</a>.</p>
 *     
 * <p> Custom key comparators are extremely useful for value retrieval when
 *     map's keys and argument keys are not of the same class, such as 
 *     {@link String} and {@link javolution.lang.Text Text} 
 *     ({@link FastComparator#LEXICAL} key comparator) or for identity maps 
 *     ({@link FastComparator#IDENTITY} key comparator).
 *     For example:<pre>
 *     FastMap identityMap = new FastMap().setKeyComparator(FastComparator.IDENTITY);
 *     </pre></p>
 * 
 * <p> To be fully {@link Reusable reusable}, {@link FastMap} maintains an 
 *     internal pool of <code>Map.Entry</code> objects. When an entry is removed
 *     from the map, it is automatically restored to the pool.</p>
 *     
 * <p> {@link FastMap} supports concurrent read without synchronization
 *     if the map keys are never removed (e.g. look-up table). Structural 
 *     modifications (entries being added/removed) should always be 
 *     synchronized.</p>
 *     
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle </a>
 * @version 3.0, February 20, 2005
 */
public class FastMap extends RealtimeObject implements Map, Reusable,
        Serializable {

    /**
     * Holds the map factory.
     */
    private static final Factory FACTORY = new Factory() {

        public Object create() {
            return new FastMap();
        }

        public void cleanup(Object obj) {
            ((FastMap) obj).reset();
        }
    };

    /**
     * Holds the map's hash table (volatile for unsynchronized access). 
     */
    private volatile transient EntryImpl[] _entries;

    /**
     * Holds the head entry to which the first entry attaches.
     * The head entry never changes (entries always added last).
     */
    private transient EntryImpl _head = new EntryImpl();

    /**
     * Holds the tail entry to which the last entry attaches.
     * The tail entry changes as entries are added/removed.
     */
    private transient EntryImpl _tail = new EntryImpl();

    /**
     * Holds the current size.
     */
    private transient int _size;

    /**
     * Holds the values view.
     */
    private transient Values _values = new Values();

    /**
     * Holds the key set view.
     */
    private transient KeySet _keySet = new KeySet();

    /**
     * Holds the entry set view.
     */
    private transient EntrySet _entrySet = new EntrySet();

    /**
     * Holds a reference to a map having the old entries when resizing.
     */
    private transient FastMap _oldEntries;

    /**
     * Holds the key comparator.
     */
    private FastComparator _keyComparator = Configuration.isPoorSystemHash() ? FastComparator.REHASH
            : FastComparator.DEFAULT;

    /**
     * Default constructor (default capacity of 16 entries). 
     */
    public FastMap() {
        this(16);
    }

    /**
     * Creates a fast map of specified capacity.
     * 
     * @param capacity the minimum length of the internal hash table.
     */
    public FastMap(int capacity) {
        int tableLength = 16;
        while (tableLength < capacity) {
            tableLength <<= 1;
        }
        _entries = new EntryImpl[tableLength];
        _head._after = _tail;
        _tail._before = _head;
    }

    /**
     * Returns a {@link FastMap} pre-allocated or allocated from the stack
     * when executing in a {@link javolution.realtime.PoolContext PoolContext}).
     * 
     * @return a new, pre-allocated or recycled map instance.
     */
    public static FastMap newInstance() {
        return (FastMap) FACTORY.object();
    }

    /**
     * Sets the key comparator for this fast map.
     * 
     * @param keyComparator the key comparator.
     * @return <code>this</code>
     */
    public FastMap setKeyComparator(FastComparator keyComparator) {
        _keyComparator = keyComparator;
        return this;
    }

    /**
     * Sets the value comparator for this fast map.
     * 
     * @param valueComparator the value comparator.
     * @return <code>this</code>
     */
    public FastMap setValueComparator(FastComparator valueComparator) {
        _values.setElementComparator(valueComparator);
        return this;
    }

    /**
     * Returns the fast iterator instance over the entries of this map
     * (<code>entrySet().fastIterator()</code>).
     *
     * @return the single reusable iterator of this map's {@link #entrySet}.
     * @see    FastCollection#fastIterator()
     */
    public final Iterator fastEntryIterator() {
        return _entrySet.fastIterator();
    }

    /**
     * Returns the fast iterator instance over the key of this map
     * (<code>keySet().fastIterator()</code>).
     *
     * @return the single reusable iterator of this map's {@link #keySet}.
     * @see    FastCollection#fastIterator()
     */
    public final Iterator fastKeyIterator() {
        return _keySet.fastIterator();
    }

    /**
     * Returns the fast iterator instance over the values of this map
     * (<code>values().fastIterator()</code>).
     *
     * @return the single reusable iterator of this map's {@link #values}.
     * @see    FastCollection#fastIterator()
     */
    public final Iterator fastValueIterator() {
        return _values.fastIterator();
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
        final int keyHash = keyHashOf(key);
        EntryImpl entry = _entries[keyHash & (_entries.length - 1)];
        while (entry != null) {
            if ((key == entry._key)
                    || ((entry._keyHash == keyHash) && areKeyEqual(key,
                            entry._key))) {
                return true;
            }
            entry = entry._next;
        }
        return (_oldEntries != null) ? _oldEntries.containsKey(key) : false;
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
        final FastComparator comp = _values.getElementComparator();
        for (EntryImpl entry = _head._after; entry != _tail; entry = entry._after) {
            if (comp.areEqual(value, entry._value)) {
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
        final int keyHash = keyHashOf(key);
        EntryImpl entry = _entries[keyHash & (_entries.length - 1)];
        while (entry != null) {
            if ((key == entry._key)
                    || ((entry._keyHash == keyHash) && areKeyEqual(key,
                            entry._key))) {
                return entry._value;
            }
            entry = entry._next;
        }
        return (_oldEntries != null) ? _oldEntries.get(key) : null;
    }

    /**
     * Returns the entry with the specified key.
     * 
     * @param key the key whose associated entry is to be returned.
     * @return the entry for the specified key or <code>null</code> if none.
     */
    public final Entry getEntry(Object key) {
        final int keyHash = keyHashOf(key);
        EntryImpl entry = _entries[keyHash & (_entries.length - 1)];
        while (entry != null) {
            if ((entry._keyHash == keyHash) && areKeyEqual(key, entry._key)) {
                return entry;
            }
            entry = entry._next;
        }
        return (_oldEntries != null) ? _oldEntries.getEntry(key) : null;
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
        final int keyHash = keyHashOf(key);
        EntryImpl entry = _entries[keyHash & (_entries.length - 1)];
        while (entry != null) {
            if ((key == entry._key)
                    || ((entry._keyHash == keyHash) && areKeyEqual(key,
                            entry._key))) {
                Object prevValue = entry._value;
                entry._value = value;
                return prevValue;
            }
            entry = entry._next;
        }
        // No mapping in current map, checks old one.
        if (_oldEntries != null) {
            // For safe unsynchronized access we don't remove old key.
            if (_oldEntries.containsKey(key)) {
                _oldEntries._tail = _tail; // Updates tail info.
                return _oldEntries.put(key, value);
            }
        }

        // The key is not mapped.
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
        if (map instanceof FastMap) { // Optimization.
            FastMap fm = (FastMap) map;
            for (EntryImpl e = fm._head._after; e != fm._tail; e = e._after) {
                put(e._key, e._value);
            }
        } else {
            for (Iterator i = map.entrySet().iterator(); i.hasNext();) {
                Entry e = (Entry) i.next();
                put(e.getKey(), e.getValue());
            }
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
        final int keyHash = keyHashOf(key);
        EntryImpl entry = _entries[keyHash & (_entries.length - 1)];
        while (entry != null) {
            if ((key == entry._key)
                    || ((entry._keyHash == keyHash) && areKeyEqual(key,
                            entry._key))) {
                Object prevValue = entry._value;
                removeEntry(entry);
                return prevValue;
            }
            entry = entry._next;
        }
        // No mapping in current map.
        if ((_oldEntries != null) &&  _oldEntries.containsKey(key)) {
            _size--;
            _oldEntries._tail = _tail; // Updates tail info.
            return _oldEntries.remove(key);
        }
        return null;
    }

    /**
     * Removes all mappings from this {@link FastMap}.
     */
    public final void clear() {
        // Clears all keys, values and buckets linked lists.
        for (EntryImpl entry = _head._after; entry != _tail; entry = entry._after) {
            entry._key = null;
            entry._value = null;
            if (entry._previous == null) { // First in bucket.
                entry._table[entry._index] = null;
            }
        }
        _tail = _head._after;
        _size = 0;

        // Discards old entries.
        _oldEntries = null;
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
                for (EntryImpl entry = _head._after; entry != _tail; entry = entry._after) {
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
        for (EntryImpl entry = _head._after; entry != _tail; entry = entry._after) {
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
        int size = (_oldEntries != null) ? _size - _oldEntries.size() : _size;
        if (size() != 0) {
            percentCollisions.append((100 * totalCollisions) / size);
            percentCollisions.append('%');
        } else {
            percentCollisions.append("N/A");
        }
        synchronized (out) {
            out.print("SIZE: " + size);
            out.print(", TABLE LENGTH: " + _entries.length);
            out.print(", AVG COLLISIONS: " + percentCollisions);
            out.print(", MAX SLOT OCCUPANCY: " + maxOccupancy);
            out.print(", KEY COMPARATOR: " + _keyComparator);
            out.println();
            if (_oldEntries != null) {
                out.print(" + ");
                _oldEntries.printStatistics(out);
            }
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

    private final class Values extends FastCollection {

        // Implements abstract method.    
        public final Iterator fastIterator() {
            _fastIterator.map = FastMap.this;
            _fastIterator.after = _head._after;
            _fastIterator.end = _tail;
            return _fastIterator;
        }

        private final ValueIterator _fastIterator = new ValueIterator();

        public Iterator iterator() {
            ValueIterator i = (ValueIterator) ValueIterator.FACTORY.object();
            i.map = FastMap.this;
            i.after = _head._after;
            i.end = _tail;
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

    private final class EntrySet extends FastCollection implements Set {

        // Implements abstract method.    
        public final Iterator fastIterator() {
            _fastIterator.map = FastMap.this;
            _fastIterator.after = _head._after;
            _fastIterator.end = _tail;
            return _fastIterator;
        }

        private final EntryIterator _fastIterator = new EntryIterator();

        public Iterator iterator() {
            EntryIterator i = (EntryIterator) EntryIterator.FACTORY.object();
            i.map = FastMap.this;
            i.after = _head._after;
            i.end = _tail;
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
                        && (_values.getElementComparator().areEqual(entry
                                .getValue(), mapEntry._value))) {
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

    private final class KeySet extends FastCollection implements Set {

        // Implements abstract method.    
        public final Iterator fastIterator() {
            _fastIterator.map = FastMap.this;
            _fastIterator.after = _head._after;
            _fastIterator.end = _tail;
            return _fastIterator;
        }

        private final KeyIterator _fastIterator = new KeyIterator();

        public Iterator iterator() {
            KeyIterator i = (KeyIterator) KeyIterator.FACTORY.object();
            i.map = FastMap.this;
            i.after = _head._after;
            i.end = _tail;
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
     * Adds a new entry for the specified key and value.
     * 
     * @param hash the hash of the key, generated with {@link #keyHash}.
     * @param key the entry's key.
     * @param value the entry's value.
     */
    private void addEntry(int hash, Object key, Object value) {
        final EntryImpl entry = _tail;

        // Setups entry parameters.
        final int index = hash & (_entries.length - 1);
        entry._key = key;
        entry._value = value;
        entry._keyHash = hash;
        entry._table = _entries;
        entry._index = index;

        // Connects to bucket.
        EntryImpl next = _entries[index];
        entry._next = next;
        if (next != null) {
            next._previous = entry;
        }
        entry._previous = null;
        _entries[index] = entry; // Volatile.

        // Updates size.
        _size++;
        
        // Moves tail forward.
        _tail = entry._after;
        
        if (_tail == null) { // No tail entry available, creates one.
            _tail = new EntryImpl();
            _tail._before = entry;
            entry._after = _tail;
            if (_size >= _entries.length) {
                increaseCapacity();
            }
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
            entry._table[entry._index] = next;
        }
        if (next != null) {
            next._previous = previous;
        } // Else do nothing, last entry in bucket.

        // Detaches from collection.
        entry._before._after = entry._after;
        entry._after._before = entry._before;

        // Re-inserts before tail.
        final EntryImpl before = entry._before = _tail._before;
        entry._after = _tail;
        before._after = entry;
        _tail._before = entry;
        
        // Moves tail backward.
        _tail = entry; 

        // Updates size.
        _size--;
    }

    private void increaseCapacity() {
        if (_entries.length <= 32) { // For small tables, it's ok to rehash (fast).
            _entries = (EntryImpl[]) ENTRIES_256_FACTORY.heapPool().next();
            for (EntryImpl entry = _head._after; entry != _tail; entry = entry._after) {
                final int index = entry._keyHash & 255;
                entry._table = _entries;
                entry._index = index;

                // Connects to bucket.
                EntryImpl next = _entries[index];
                entry._next = next;
                if (next != null) {
                    next._previous = entry;
                }
                entry._previous = null;
                _entries[index] = entry; 
            }
            return;
        } 
        int newCapacity = _entries.length << 4;
        FastMap oldEntries;
        if (newCapacity <= (1 << 10)) { //                1,024
            oldEntries = (FastMap) FACTORY_10.heapPool().next();
        } else if (newCapacity <= (1 << 14)) { //        16,384
            oldEntries = (FastMap) FACTORY_14.heapPool().next();
        } else if (newCapacity <= (1 << 18)) { //       262,144
            oldEntries = (FastMap) FACTORY_18.heapPool().next();
        } else if (newCapacity <= (1 << 22)) { //     4,194,304
            oldEntries = (FastMap) FACTORY_22.heapPool().next();
        } else if (newCapacity <= (1 << 26)) { //    67,108,864
            oldEntries = (FastMap) FACTORY_26.heapPool().next();
        } else { //                               1,073,741,824 
            oldEntries = (FastMap) FACTORY_30.heapPool().next();
        }
        // Swaps entries.
        EntryImpl[] newEntries = oldEntries._entries;
        oldEntries._entries = _entries;
        _entries = newEntries;

        // Setup the oldEntries map.
        oldEntries._oldEntries = _oldEntries;
        oldEntries._keyComparator = _keyComparator;
        oldEntries._head = _head;
        oldEntries._tail = _tail;
        oldEntries._size = _size;

        // Done. We have now a much larger entry table. 
        // Still, we keep reference to the old entries through oldEntries
        // until the map is cleared.
        _oldEntries = oldEntries;
    }

    private static ObjectFactory ENTRIES_256_FACTORY = new ObjectFactory() {
        public Object create() {
            return new EntryImpl[256];
        }
    };
    
    private static ObjectFactory FACTORY_10 = new ObjectFactory() {
        public Object create() {
            return new FastMap(1 << 10);
        }
    };

    private static ObjectFactory FACTORY_14 = new ObjectFactory() {
        public Object create() {
            return new FastMap(1 << 14);
        }
    };

    private static ObjectFactory FACTORY_18 = new ObjectFactory() {
        public Object create() {
            return new FastMap(1 << 18);
        }
    };

    private static ObjectFactory FACTORY_22 = new ObjectFactory() {
        public Object create() {
            return new FastMap(1 << 22);
        }
    };

    private static ObjectFactory FACTORY_26 = new ObjectFactory() {
        public Object create() {
            return new FastMap(1 << 26);
        }
    };

    private static ObjectFactory FACTORY_30 = new ObjectFactory() {
        public Object create() {
            return new FastMap(1 << 30);
        }
    };

    /**
     * Returns the hash value of the specified key.
     * 
     * @param key the key whose hash value is calculated.
     * @return the key hash value.
     */
    private final int keyHashOf(Object key) {
        return (_keyComparator == FastComparator.DEFAULT) ? key.hashCode()
                : _keyComparator.hashCodeOf(key);
    }

    /**
     * Indicates if two keys are considered equal.
     * 
     * @param key1 the first key.
     * @param key2 the second key.
     * @return <code>true</code> if both keys are considered equal;
     *         <code>false</code> otherwise.
     */
    private final boolean areKeyEqual(Object key1, Object key2) {
        return (_keyComparator == FastComparator.DEFAULT) ? key1.equals(key2)
                : _keyComparator.areEqual(key1, key2);
    }

    // Overrides.
    public boolean move(ObjectSpace os) {
        if (super.move(os)) {
            for (EntryImpl entry = _head._after; entry != _tail; entry = entry._after) {
                if (entry._key instanceof Realtime) {
                    ((Realtime) entry._key).move(os);
                }
                if (entry._value instanceof Realtime) {
                    ((Realtime) entry._value).move(os);
                }
            }
            return true;
        }
        return false;
    }

    // Implements Reusable.
    public void reset() {
        clear();
        setKeyComparator(Configuration.isPoorSystemHash() ?
                FastComparator.REHASH : FastComparator.DEFAULT);
        setValueComparator(FastComparator.DEFAULT);
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
        // Reads non-transient fields (e.g. key comparator).
        stream.defaultReadObject();
        final int size = stream.readInt();
        final int entriesLength = stream.readInt();

        // Do not use default key comparator with poor system hash. 
        if (Configuration.isPoorSystemHash()) {
            if (_keyComparator.getClass() == FastComparator.DEFAULT.getClass()) {
                _keyComparator = FastComparator.REHASH;
            }
        }

        // Initializes transient fields.
        _entries = new EntryImpl[entriesLength];
        _head = new EntryImpl();
        _tail = new EntryImpl();
        _head._after = _tail;
        _tail._before = _head;
        _values = new Values();
        _entrySet = new EntrySet();
        _keySet = new KeySet();

        // Reads data.
        for (int i = 0; i < size; i++) {
            Object key = stream.readObject();
            Object value = stream.readObject();
            addEntry(_keyComparator.hashCodeOf(key), key, value);
        }
    }

    /**
     * Requires special handling during serialization process.
     *
     * @param  stream the object output stream.
     * @throws IOException if an I/O error occurs.
     */
    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.defaultWriteObject();
        stream.writeInt(_size);
        stream.writeInt(_entries.length);
        for (EntryImpl entry = _head._after; entry != _tail; entry = entry._after) {
            stream.writeObject(entry._key);
            stream.writeObject(entry._value);
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
         * Holds the table this entry belongs to.
         */
        private EntryImpl[] _table;

        /**
         * Holds its index in the table.
         */
        private int _index;

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
                return (_keyComparator.areEqual(_key, entry.getKey()))
                        && _values.getElementComparator().areEqual(_value,
                                entry.getValue());
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
            return _keyHash ^ _values.getElementComparator().hashCodeOf(_value);
        }
    }
}