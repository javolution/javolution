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
import j2me.lang.UnsupportedOperationException;
import j2me.util.Collection;
import j2me.util.Iterator;
import j2me.util.Map;
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
import javolution.util.FastCollection.Record;

/**
 * <p> This class represents a hash map with real-time behavior; 
 *     smooth capacity increase and no rehashing ever performed.</p>
 *     
 * <p> {@link FastMap} has a predictable iteration order, which is the order in
 *     which keys are inserted into the map (similar to 
 *     <code>java.util.LinkedHashMap</code> collection class).</p>
 *     
 * <p> {@link FastMap.Entry} can quickly be iterated over (forward or backward)
 *     without using iterators. For example:<pre>
 *     FastMap map = new FastMap&lt;String, Thread&gt;();
 *     for (FastMap.Entry&lt;String, Thread&gt; e = map.headEntry(), end = map.tailEntry(); (e = e.getNextEntry()) != end;) {
 *          String key = e.getKey(); // No typecast necessary.
 *          Thread value = e.getValue(); // No typecast necessary.
 *     }</pre>
 *     Collection views {@link #values values}, {@link #keySet() keys}
 *      and {@link #entrySet() entries} are all instances of 
 *     {@link FastCollection} and also support direct iterations.</p>
 * 
 * <p> {@link FastMap} may use custom key comparators; the default comparator is
 *     either {@link FastComparator#DIRECT DIRECT} or 
 *     {@link FastComparator#REHASH REHASH} based upon the current <a href=
 *     "{@docRoot}/overview-summary.html#configuration">Javolution 
 *     Configuration</a>. Users may explicitly set the key comparator to 
 *     {@link FastComparator#DIRECT DIRECT} for optimum performance
 *     when the hash codes are well distributed for all run-time platforms
 *     (e.g. calculated hash codes).</p>
 *     
 * <p> Custom key comparators are extremely useful for value retrieval when
 *     map's keys and argument keys are not of the same class, such as 
 *     {@link String} and {@link javolution.lang.Text Text} 
 *     ({@link FastComparator#LEXICAL LEXICAL}) or for identity maps 
 *     ({@link FastComparator#IDENTITY IDENTITY}).
 *     For example:<pre>
 *     FastMap identityMap = new FastMap().setKeyComparator(FastComparator.IDENTITY);
 *     </pre></p>
 * 
 * <p> {@link FastMap} are fully {@link Reusable reusable}; they maintains 
 *     internal pools of <code>Map.Entry</code> objects. When an entry is 
 *     removed from the map, it is automatically restored to the pool.</p>
 *     
 * <p> Finally, {@link FastMap} supports concurrent {@link #get read} without 
 *     synchronization if the map keys are never removed (e.g. look-up table).
 *     The structural modifications themselves (entries being added/removed)
 *     should always be synchronized.</p>
 *     
 * <p> <b>Hint:</b> Instead of removing the map keys, applications may associate
 *                  keys with <code>null</code> values and keep concurrent 
 *                  access unsynchronized!</p>
 *     
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle </a>
 * @version 3.3, June 24, 2005
 */
public class FastMap/*<K,V>*/extends RealtimeObject implements Map/*<K,V>*/,
        Reusable, Serializable {

    /**
     * Holds table higher index rotation. 
     */
    private static final int R0 = 5;

    /**
     * Holds the table lower index mask. 
     */
    private static final int M0 = (1 << R0) - 1;

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
     * Use double array to avoid large arrays allocation. 
     */
    private volatile transient Entry/*<K,V>*/[][] _entries;

    /**
     * Holds the head entry to which the first entry attaches.
     * The head entry never changes (entries always added last).
     */
    private transient Entry/*<K,V>*/_head = new Entry();

    /**
     * Holds the tail entry to which the last entry attaches.
     * The tail entry changes as entries are added/removed.
     */
    private transient Entry/*<K,V>*/_tail = new Entry();

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
     * Holds the unmodifiable view.
     */
    private transient Map/*<K,V>*/_unmodifiable = new Unmodifiable();

    /**
     * Holds a reference to a map having the old entries when resizing.
     */
    private transient FastMap/*<K,V>*/_oldEntries;

    /**
     * Holds the key comparator.
     */
    private transient FastComparator _keyComp = Configuration
            .isPoorSystemHash() ? FastComparator.REHASH : null;

    /**
     * Creates a fast map of small initial capacity.
     */
    public FastMap() {
        this(new Entry[1][1 << R0]);
    }

    /**
     * Creates a map of specified initial capacity; unless the map size 
     * reaches the specified capacity, operations on this map will not allocate
     * memory (no lazy object creation).
     * 
     * @param capacity the initial capacity.
     */
    public FastMap(int capacity) {
        int tableLength = 1 << R0;
        while (tableLength < capacity) {
            tableLength <<= 1;
        }
        _entries = (Entry/*<K,V>*/[][]) new FastMap.Entry[tableLength >> R0][];
        for (int i = 0; i < _entries.length;) {
            _entries[i++] = (Entry/*<K,V>*/[]) new FastMap.Entry[1 << R0];
        }
        _head._next = _tail;
        _tail._previous = _head;
        Entry/*<K,V>*/previous = _tail;
        for (int i = 0; i < capacity; i += 4) { // Four entries at a time.
            Entry/*<K,V>*/newEntry = (Entry/*<K,V>*/) Entry.FACTORY
                    .newObject();
            newEntry._previous = previous;
            previous._next = newEntry;
            previous = newEntry._next._next._next;
        }
    }

    /**
     * Creates a map containing the specified entries, in the order they
     * are returned by the map iterator.
     *
     * @param map the map whose entries are to be placed into this map.
     */
    public FastMap(Map/*<? extends K, ? extends V>*/map) {
        this(map.size());
        putAll(map);
    }

    /**
     * Creates a fast map having the specified entry table.
     * 
     * @param entries the entry table.
     */
    private FastMap(Entry/*<K,V>*/[][] entries) {
        _entries = entries;
        _head._next = _tail;
        _tail._previous = _head;
    }

    /**
     * Returns a map allocated from the stack when executing in a 
     * {@link javolution.realtime.PoolContext PoolContext}).
     * 
     * @return a new, pre-allocated or recycled map instance.
     */
    public static/*<K,V>*/FastMap/*<K,V>*/newInstance() {
        return (FastMap/*<K,V>*/) FACTORY.object();
    }

    /**
     * Returns the head entry of this map.
     *
     * @return the entry such as <code>headEntry().getNextEntry()</code> holds 
     *         the first map entry.
     */
    public final Entry/*<K,V>*/headEntry() {
        return _head;
    }

    /**
     * Returns the tail entry of this map.
     *
     * @return the entry such as <code>tailEntry().getPreviousEntry()</code>
     *         holds the last map entry.
     */
    public final Entry/*<K,V>*/tailEntry() {
        return _tail;
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
     * Indicates if this map contains no key-value mappings.
     * 
     * @return <code>true</code> if this map contains no key-value mappings;
     *         <code>false</code> otherwise.
     */
    public final boolean isEmpty() {
        return _head._next == _tail;
    }

    /**
     * Indicates if this map contains a mapping for the specified key.
     * 
     * @param key the key whose presence in this map is to be tested.
     * @return <code>true</code> if this map contains a mapping for the
     *         specified key; <code>false</code> otherwise.
     * @throws NullPointerException if the key is <code>null</code>.
     */
    public final boolean containsKey(Object key) {
        final int keyHash = (_keyComp == null) ? key.hashCode() : _keyComp
                .hashCodeOf(key);
        Entry/*<K,V>*/entry = _entries[(keyHash >> R0) & (_entries.length - 1)][keyHash
                & M0];
        while (entry != null) {
            if ((key == entry._key)
                    || ((entry._keyHash == keyHash) && ((_keyComp == null) ? key
                            .equals(entry._key)
                            : _keyComp.areEqual(key, entry._key)))) {
                return true;
            }
            entry = entry._beside;
        }
        return (_oldEntries != null) ? _oldEntries.containsKey(key) : false;
    }

    /**
     * Indicates if this map associates one or more keys to the specified value.
     * 
     * @param value the value whose presence in this map is to be tested.
     * @return <code>true</code> if this map maps one or more keys to the
     *         specified value.
     * @throws NullPointerException if the key is <code>null</code>.
     */
    public final boolean containsValue(Object value) {
        return _values.contains(value);
    }

    /**
     * Returns the value to which this map associates the specified key.
     * 
     * @param key the key whose associated value is to be returned.
     * @return the value to which this map maps the specified key, or
     *         <code>null</code> if there is no mapping for the key.
     * @throws NullPointerException if key is <code>null</code>.
     */
    public final Object/*V*/get(Object key) {
        final int keyHash = (_keyComp == null) ? key.hashCode() : _keyComp
                .hashCodeOf(key);
        Entry/*<K,V>*/entry = _entries[(keyHash >> R0) & (_entries.length - 1)][keyHash
                & M0];
        while (entry != null) {
            if ((key == entry._key)
                    || ((entry._keyHash == keyHash) && ((_keyComp == null) ? key
                            .equals(entry._key)
                            : _keyComp.areEqual(key, entry._key)))) {
                return entry._value;
            }
            entry = entry._beside;
        }
        return (_oldEntries != null) ? _oldEntries.get(key) : null;
    }

    /**
     * Returns the entry with the specified key.
     * 
     * @param key the key whose associated entry is to be returned.
     * @return the entry for the specified key or <code>null</code> if none.
     */
    public final Entry/*<K,V>*/getEntry(Object key) {
        final int keyHash = (_keyComp == null) ? key.hashCode() : _keyComp
                .hashCodeOf(key);
        Entry/*<K,V>*/entry = _entries[(keyHash >> R0) & (_entries.length - 1)][keyHash
                & M0];
        while (entry != null) {
            if ((key == entry._key)
                    || ((entry._keyHash == keyHash) && ((_keyComp == null) ? key
                            .equals(entry._key)
                            : _keyComp.areEqual(key, entry._key)))) {
                return entry;
            }
            entry = entry._beside;
        }
        return (_oldEntries != null) ? _oldEntries.getEntry(key) : null;
    }

    /**
     * Associates the specified value with the specified key in this map.
     * If this ma previously contained a mapping for this key, the old value
     * is replaced.
     * 
     * @param key the key with which the specified value is to be associated.
     * @param value the value to be associated with the specified key.
     * @return the previous value associated with specified key, or
     *         <code>null</code> if there was no mapping for key. A
     *         <code>null</code> return can also indicate that the map
     *         previously associated <code>null</code> with the specified key.
     * @throws NullPointerException if the key is <code>null</code>.
     */
    public final Object/*V*/put(Object/*K*/key, Object/*V*/value) {
        final int keyHash = (_keyComp == null) ? key.hashCode() : _keyComp
                .hashCodeOf(key);
        Entry/*<K,V>*/entry = _entries[(keyHash >> R0) & (_entries.length - 1)][keyHash
                & M0];
        while (entry != null) {
            if ((key == entry._key)
                    || ((entry._keyHash == keyHash) && ((_keyComp == null) ? key
                            .equals(entry._key)
                            : _keyComp.areEqual(key, entry._key)))) {
                Object/*V*/prevValue = entry._value;
                entry._value = value;
                return prevValue;
            }
            entry = entry._beside;
        }
        // No mapping in current map, checks old one.
        if (_oldEntries != null) {
            // For safe unsynchronized access we don't remove old key.
            if (_oldEntries.containsKey(key)) {
                return _oldEntries.put(key, value);
            }
        }

        // The key is not mapped.
        addEntry(keyHash, key, value);
        return null;
    }

    /**
     * Copies all of the mappings from the specified map to this map.
     * 
     * @param map the mappings to be stored in this map.
     * @throws NullPointerException the specified map is <code>null</code>,
     *         or the specified map contains <code>null</code> keys.
     */
    public final void putAll(Map/*<? extends K, ? extends V>*/map) {
        if (map instanceof FastMap) { // Optimization.
            FastMap/*<? extends K, ? extends V>*/fm = (FastMap/*<? extends K, ? extends V>*/) map;
            for (Entry/*<? extends K, ? extends V>*/e = fm._head, end = fm._tail; (e = e._next) != end;) {
                put(e._key, e._value);
            }
        } else {
            for (Iterator i = map.entrySet().iterator(); i.hasNext();) {
                Map.Entry/*<? extends K, ? extends V>*/e = (Map.Entry/*<? extends K, ? extends V>*/) i
                        .next();
                put(e.getKey(), e.getValue());
            }
        }
    }

    /**
     * Removes the mapping for this key from this map if present.
     * 
     * @param key the key whose mapping is to be removed from the map.
     * @return previous value associated with specified key, or
     *         <code>null</code> if there was no mapping for key. A
     *         <code>null</code> return can also indicate that the map
     *         previously associated <code>null</code> with the specified key.
     * @throws NullPointerException if the key is <code>null</code>.
     */
    public final Object/*V*/remove(Object key) {
        final int keyHash = (_keyComp == null) ? key.hashCode() : _keyComp
                .hashCodeOf(key);
        Entry/*<K,V>*/entry = _entries[(keyHash >> R0) & (_entries.length - 1)][keyHash
                & M0];
        while (entry != null) {
            if ((key == entry._key)
                    || ((entry._keyHash == keyHash) && ((_keyComp == null) ? key
                            .equals(entry._key)
                            : _keyComp.areEqual(key, entry._key)))) {
                Object/*V*/prevValue = entry._value;
                removeEntry(entry);
                return prevValue;
            }
            entry = entry._beside;
        }
        // No mapping in current map.
        if ((_oldEntries != null) && _oldEntries.containsKey(key)) {
            _size--;
            _oldEntries._tail = _tail; // Specifies the tail for entry storage.
            return _oldEntries.remove(key);
        }
        return null;
    }

    /**
     * Sets the key comparator for this fast map.
     * 
     * @param keyComparator the key comparator.
     * @return <code>this</code>
     */
    public FastMap/*<K,V>*/setKeyComparator(FastComparator keyComparator) {
        _keyComp = (keyComparator == FastComparator.DIRECT) ? null
                : keyComparator;
        return this;
    }

    /**
     * Sets the value comparator for this map.
     * 
     * @param valueComparator the value comparator.
     * @return <code>this</code>
     */
    public FastMap/*<K,V>*/setValueComparator(FastComparator valueComparator) {
        _values.setValueComparator(valueComparator);
        return this;
    }

    /**
     * Removes the specified entry from the map.
     * 
     * @param entry the entry to be removed.
     */
    public final void removeEntry(Entry/*<K,V>*/entry) {
        // Updates size.
        _size--;

        // Clears value and key.
        entry._key = null;
        entry._value = null;

        // Detaches from map.
        entry.detach();

        // Re-inserts next tail.
        final Entry next = _tail._next;
        entry._previous = _tail;
        entry._next = next;
        _tail._next = entry;
        if (next != null) {
            next._previous = entry;
        }
    }

    /**
     * Removes all mappings from this map.
     */
    public final void clear() {
        // Clears all keys, values and buckets linked lists.
        for (Entry/*<K,V>*/e = _head, end = _tail; (e = e._next) != end;) {
            e._key = null;
            e._value = null;
            final Entry/*<K,V>*/[][] table = e._table;
            table[(e._keyHash >> R0) & (table.length - 1)][e._keyHash & M0] = null;
        }
        _tail = _head._next;
        _size = 0;

        // Discards old entries.
        _oldEntries = null;
    }

    /**
     * Compares the specified object with this map for equality.
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
            Map/*<?,?>*/that = (Map) obj;
            if (this.size() == that.size()) {
                final Set thatEntrySet = that.entrySet();
                for (Entry e = _head, end = _tail; (e = e._next) != end;) {
                    if (!thatEntrySet.contains(e)) {
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
     * Returns the hash code value for this map.
     * 
     * @return the hash code value for this map.
     */
    public int hashCode() {
        int code = 0;
        for (Entry e = _head, end = _tail; (e = e._next) != end;) {
            code += e.hashCode();
        }
        return code;
    }

    /**
     * Returns the textual representation of this map.
     * 
     * @return the textual representation of the entry set.
     */
    public Text toText() {
        return _entrySet.toText();
    }

    /**
     * Prints the current statistics on this map.
     * This method may help identify poorly defined hash functions.
     * An average collision of less than <code>50%</code> is typically 
     * acceptable.
     *  
     * @param out the stream to use for output (e.g. <code>System.out</code>)
     */
    public void printStatistics(PrintStream out) {
        int maxOccupancy = 0;
        int totalCollisions = 0;
        int size = 0;
        for (int i = 0; i < _entries.length; i++) {
            for (int j = 0; j < _entries[i].length; j++) {
                Entry entry = _entries[i][j];
                int occupancy = 0;
                while (entry != null) {
                    occupancy++;
                    if (occupancy > maxOccupancy) {
                        maxOccupancy = occupancy;
                    }
                    if (occupancy > 1) {
                        totalCollisions++;
                    }
                    entry = entry._beside;
                    size++;
                }
            }
        }
        TextBuilder percentCollisions = TextBuilder.newInstance();
        if (size != 0) {
            percentCollisions.append((100 * totalCollisions) / size);
            percentCollisions.append('%');
        } else {
            percentCollisions.append("N/A");
        }
        synchronized (out) {
            out.print("SIZE: " + size);
            out.print(", TABLE LENGTH: " + _entries.length * _entries[0].length);
            out.print(", AVG COLLISIONS: " + percentCollisions);
            out.print(", MAX SLOT OCCUPANCY: " + maxOccupancy);
            out.print(", KEY COMPARATOR: "
                    + ((_keyComp == null) ? FastComparator.DIRECT : _keyComp));
            out.println();
            if (_oldEntries != null) {
                out.print(" + ");
                _oldEntries.printStatistics(out);
            }
        }
    }

    /**
     * Returns a {@link FastCollection} view of the values contained in this
     * map. The collection is backed by the map, so changes to the
     * map are reflected in the collection, and vice-versa. The collection 
     * supports element removal, which removes the corresponding mapping from
     * this map, via the <code>Iterator.remove</code>, 
     * <code>Collection.remove</code>, <code>removeAll</code>,
     * <code>retainAll</code> and <code>clear</code> operations. 
     * It does not support the <code>add</code> or <code>addAll</code> 
     * operations.
     * 
     * @return a collection view of the values contained in this map 
     *         (instance of {@link FastCollection}).
     */
    public final Collection/*<V>*/values() {
        return _values;
    }

    private final class Values extends FastCollection/*<V>*/{

        public int size() {
            return _size;
        }

        public void clear() {
            FastMap.this.clear();
        }

        public Record/*<FastMap.Entry<K,V>>*/headRecord() {
            return FastMap.this._head;
        }

        public Record/*<FastMap.Entry<K,V>>*/tailRecord() {
            return FastMap.this._tail;
        }

        public Object/*V*/valueOf(Record record) {
            return ((Entry/*<K,V>*/) record)._value;
        }

        public void delete(Record record) {
            FastMap.this.removeEntry((Entry/*<K,V>*/) record);
        }
    }

    /**
     * Returns a {@link FastCollection} view of the mappings contained in this
     * map. Each element in the returned collection is a 
     * <code>FastMap.Entry</code>. The collection is backed by the map, so
     * changes to the map are reflected in the collection, and vice-versa. The
     * collection supports element removal, which removes the corresponding
     * mapping from this map, via the <code>Iterator.remove</code>,
     * <code>Collection.remove</code>,<code>removeAll</code>,
     * <code>retainAll</code>, and <code>clear</code> operations. It does
     * not support the <code>add</code> or <code>addAll</code> operations.
     * 
     * @return a collection view of the mappings contained in this map
     *         (instance of {@link FastCollection}).
     */
    public final Set/*<Map.Entry<K,V>>*/entrySet() {
        return _entrySet;
    }

    private final class EntrySet extends FastCollection
    /*<Map.Entry<K,V>>*/implements Set/*<Map.Entry<K,V>>*/{

        public int size() {
            return _size;
        }

        public void clear() {
            FastMap.this.clear();
        }

        public boolean contains(Object obj) { // Optimization.
            if (obj instanceof Map.Entry) {
                Map.Entry/*<?,?>*/entry = (Entry) obj;
                Entry/*<K,V>*/mapEntry = getEntry(entry.getKey());
                return entry.equals(mapEntry);
            } else {
                return false;
            }
        }

        public Text toText() {
            Text text = Text.valueOf('[');
            final Text equ = Text.valueOf('=');
            final Text sep = Text.valueOf(", ");
            for (Entry/*<K,V>*/e = _head, end = _tail; (e = e._next) != end;) {
                text = text.concat(Text.valueOf(e._key)).concat(equ).concat(
                        Text.valueOf(e._value));
                if (e._next != end) {
                    text = text.concat(sep);
                }
            }
            return text.concat(Text.valueOf(']'));
        }

        public Record/*<FastMap.Entry<K,V>>*/headRecord() {
            return _head;
        }

        public Record/*<FastMap.Entry<K,V>>*/tailRecord() {
            return _tail;
        }

        public Object/*Map.Entry<K,V>*/valueOf(Record record) {
            return (Map.Entry/*<K,V>*/) record;
        }

        public void delete(Record record) {
            FastMap.this.removeEntry((Entry/*<K,V>*/) record);
        }
    }

    /**
     * Returns a {@link FastCollection} view of the keys contained in this 
     * map. The set is backed by the map, so changes to the map are reflected
     * in the set, and vice-versa. The set supports element removal, which 
     * removes the corresponding mapping from this map, via the 
     * <code>Iterator.remove</code>, <code>Collection.remove</code>,<code>removeAll<f/code>,
     * <code>retainAll</code>, and <code>clear</code> operations. It does
     * not support the <code>add</code> or <code>addAll</code> operations.
     * 
     * @return a set view of the keys contained in this map
     *         (instance of {@link FastCollection}).
     */
    public final Set/*<K>*/keySet() {
        return _keySet;
    }

    private final class KeySet extends FastCollection/*<K>*/implements Set/*<K>*/{

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

        public Record/*<FastMap.Entry<K,V>>*/headRecord() {
            return FastMap.this._head;
        }

        public Record/*<FastMap.Entry<K,V>>*/tailRecord() {
            return FastMap.this._tail;
        }

        public Object/*K*/valueOf(Record record) {
            return ((Entry/*<K,V>*/) record)._key;
        }

        public void delete(Record record) {
            FastMap.this.removeEntry((Entry/*<K,V>*/) record);
        }
    }

    /**
     * Returns the unmodifiable view associated to this map.
     * Attempts to modify the returned map or to directly access its  
     * (modifiable) map entries (e.g. <code>unmodifiable().entrySet()</code>)
     * result in an {@link UnsupportedOperationException} being thrown.
     * Unmodifiable {@link FastCollection} views of this map keys and values
     * are nonetheless obtainable (e.g. <code>unmodifiable().keySet(), 
     * <code>unmodifiable().values()</code>). 
     *  
     * @return an unmodifiable view of this map.
     */
    public final Map/*<K,V>*/unmodifiable() {
        return _unmodifiable;
    }

    /**
     * Adds a new entry for the specified key and value.
     * 
     * @param hash the hash of the key, generated with {@link #keyHash}.
     * @param key the entry's key.
     * @param value the entry's value.
     */
    private void addEntry(int hash, Object/*K*/key, Object/*V*/value) {
        // Updates size.
        if ((_size++ >> R0) >= _entries.length) { // Check if entry table too small. 
            increaseEntryTable();
        }

        Entry newTail = _tail._next;
        if (newTail == null) {
            newTail = _tail._next = (Entry) Entry.FACTORY.newObject();
            newTail._previous = _tail;
        }
        // Setups entry parameters.
        _tail._key = key;
        _tail._value = value;
        _tail._keyHash = hash;
        _tail._table = _entries;

        // Connects to bucket.
        Entry[] tmp = _entries[(hash >> R0) & (_entries.length - 1)];
        if (tmp == NULL_BLOCK) {
            _entries[(hash >> R0) & (_entries.length - 1)] = tmp = (Entry[]) ENTRY_BLOCK
                    .newObject();
        }
        Entry beside = tmp[hash & M0];
        _tail._beside = beside;
        tmp[hash & M0] = _tail; // Volatile.

        // Moves tail forward.
        _tail = newTail;
    }

    // Increases the table size, the table length is multiplied by 8.
    // It still ensures that no more half memory space is unused 
    // (most space is being taken by the entries objects themselves).
    private void increaseEntryTable() {
        if (_entries.length >= (1 << 27))
            return; // Cannot increase.
        final int newLength = _entries.length << 3;
        FastMap/*<K,V>*/tmp;
        if (newLength <= (1 << 3)) { //                
            tmp = (FastMap/*<K,V>*/) FACTORY_3.newObject(); // 256
        } else if (newLength <= (1 << 6)) {
            tmp = (FastMap/*<K,V>*/) FACTORY_6.newObject(); // 2048
        } else if (newLength <= (1 << 9)) {
            tmp = (FastMap/*<K,V>*/) FACTORY_9.newObject(); // 16,384
        } else if (newLength <= (1 << 12)) {
            tmp = (FastMap/*<K,V>*/) FACTORY_12.newObject(); // 131,072
        } else if (newLength <= (1 << 15)) {
            tmp = (FastMap/*<K,V>*/) FACTORY_15.newObject(); // 1,048,576
        } else if (newLength <= (1 << 18)) {
            tmp = (FastMap/*<K,V>*/) FACTORY_18.newObject();
        } else if (newLength <= (1 << 21)) {
            tmp = (FastMap/*<K,V>*/) FACTORY_21.newObject();
        } else if (newLength <= (1 << 24)) {
            tmp = (FastMap/*<K,V>*/) FACTORY_24.newObject();
        } else {
            tmp = (FastMap/*<K,V>*/) FACTORY_27.newObject();
        }
        for (int i = 0; i < tmp._entries.length;) {
            tmp._entries[i++] = NULL_BLOCK;
        }
        // Swaps entries.
        Entry[][] newEntries = tmp._entries;
        tmp._entries = _entries;
        _entries = newEntries;

        // Setup the oldEntries map (used only for hash access).
        tmp._oldEntries = _oldEntries;
        tmp._keyComp = _keyComp;
        tmp._head = null;
        tmp._tail = null;
        tmp._size = -1;

        // Done. We have now a much larger entry table. 
        // Still, we keep reference to the old entries through oldEntries
        // until the map is cleared.
        _oldEntries = tmp;
    }

    private static final Entry[] NULL_BLOCK = new Entry[1 << R0];

    private static ObjectFactory ENTRY_BLOCK = new ObjectFactory() {
        public Object create() {
            return new Entry[1 << R0];
        }
    };

    private static ObjectFactory FACTORY_3 = new ObjectFactory() {
        public Object create() {
            return new FastMap(new Entry[1 << 3][]);
        }
    };

    private static ObjectFactory FACTORY_6 = new ObjectFactory() {
        public Object create() {
            return new FastMap(new Entry[1 << 6][]);
        }
    };

    private static ObjectFactory FACTORY_9 = new ObjectFactory() {
        public Object create() {
            return new FastMap(new Entry[1 << 9][]);
        }
    };

    private static ObjectFactory FACTORY_12 = new ObjectFactory() {
        public Object create() {
            return new FastMap(new Entry[1 << 12][]);
        }
    };

    private static ObjectFactory FACTORY_15 = new ObjectFactory() {
        public Object create() {
            return new FastMap(new Entry[1 << 15][]);
        }
    };

    private static ObjectFactory FACTORY_18 = new ObjectFactory() {
        public Object create() {
            return new FastMap(new Entry[1 << 18][]);
        }
    };

    private static ObjectFactory FACTORY_21 = new ObjectFactory() {
        public Object create() {
            return new FastMap(new Entry[1 << 21][]);
        }
    };

    private static ObjectFactory FACTORY_24 = new ObjectFactory() {
        public Object create() {
            return new FastMap(new Entry[1 << 24][]);
        }
    };

    private static ObjectFactory FACTORY_27 = new ObjectFactory() {
        public Object create() {
            return new FastMap(new Entry[1 << 27][]);
        }
    };

    // Overrides.
    public boolean move(ObjectSpace os) {
        if (super.move(os)) {
            for (Entry e = _head, end = _tail; (e = e._next) != end;) {
                if (e._key instanceof Realtime) {
                    ((Realtime) e._key).move(os);
                }
                if (e._value instanceof Realtime) {
                    ((Realtime) e._value).move(os);
                }
            }
            return true;
        }
        return false;
    }

    // Implements Reusable.
    public void reset() {
        clear();
        _keyComp = Configuration.isPoorSystemHash() ? FastComparator.REHASH
                : null;
        setValueComparator(FastComparator.DIRECT);
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
        final int size = stream.readInt();
        final int entriesLength = stream.readInt();

        // Initializes transient fields.
        _entries = (Entry/*<K,V>*/[][]) new FastMap.Entry[entriesLength][];
        for (int i = 0; i < _entries.length;) {
            _entries[i++] = (Entry/*<K,V>*/[]) new FastMap.Entry[1 << R0];
        }
        _head = new Entry();
        _tail = new Entry();
        _head._next = _tail;
        _tail._previous = _head;
        _values = new Values();
        _entrySet = new EntrySet();
        _keySet = new KeySet();
        _unmodifiable = new Unmodifiable();

        // Sets key comparator and value comparator (force rehash when necessary). 
        _keyComp = (FastComparator) stream.readObject();
        if ((_keyComp == null) && (Configuration.isPoorSystemHash())) {
            _keyComp = FastComparator.REHASH;
        }
        setValueComparator((FastComparator) stream.readObject());

        // Reads data.
        for (int i = 0; i < size; i++) {
            Object/*K*/key = (Object/*K*/) stream.readObject();
            Object/*V*/value = (Object/*V*/) stream.readObject();
            addEntry((_keyComp == null) ? key.hashCode() : _keyComp
                    .hashCodeOf(key), key, value);
        }
    }

    /**
     * Requires special handling during serialization process.
     *
     * @param  stream the object output stream.
     * @throws IOException if an I/O error occurs.
     */
    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.writeInt(_size);
        stream.writeInt(_entries.length);
        stream.writeObject(_keyComp);
        stream.writeObject(_values.getValueComparator());
        for (Entry e = _head, end = _tail; (e = e._next) != end;) {
            stream.writeObject(e._key);
            stream.writeObject(e._value);
        }
    }

    /**
     * This class represents a {@link FastMap} entry.
     */
    public static final class Entry/*<K,V>*/implements Map.Entry/*<K,V>*/,
            Record/*<FastMap.Entry<K,V>>*/{

        /**
         * Holds the entry factory (to allow for pre-allocation).
         */
        private static ObjectFactory FACTORY = new ObjectFactory() {
            // Creates 4 entries at a time.
            protected Object create() {
                Entry e0 = new Entry();

                Entry e1 = new Entry();
                e1._previous = e0;
                e0._next = e1;

                Entry e2 = new Entry();
                e2._previous = e1;
                e1._next = e2;

                Entry e3 = new Entry();
                e3._previous = e2;
                e2._next = e3;

                return e0;
            }
        };

        /**
         * Holds the next node.
         */
        private Entry/*<K,V>*/_next;

        /**
         * Holds the previous node.
         */
        private Entry/*<K,V>*/_previous;

        /**
         * Holds the entry key.
         */
        private Object/*K*/_key;

        /**
         * Holds the entry value.
         */
        private Object/*V*/_value;

        /**
         * Holds the next entry in the same bucket.
         */
        private Entry/*<K,V>*/_beside;

        /**
         * Holds the hash table this entry belongs to.
         */
        private Entry/*<K,V>*/[][] _table;

        /**
         * Holds the key hash code.
         */
        private int _keyHash;

        /**
         * Default constructor.
         */
        private Entry() {
        }

        /**
         * Returns the entry after this one.
         * 
         * @return the next entry.
         */
        public final Entry/*<K,V>*/getNextEntry() {
            return _next;
        }

        /**
         * Returns the entry before this one.
         * 
         * @return the previous entry.
         */
        public final Entry/*<K,V>*/getPreviousEntry() {
            return _previous;
        }

        /**
         * Returns the key for this entry.
         * 
         * @return the entry key.
         */
        public final Object/*K*/getKey() {
            return _key;
        }

        /**
         * Returns the value for this entry.
         * 
         * @return the entry value.
         */
        public final Object/*V*/getValue() {
            return _value;
        }

        /**
         * Sets the value for this entry.
         * 
         * @param value the new value.
         * @return the previous value.
         */
        public final Object/*V*/setValue(Object/*V*/value) {
            Object/*V*/old = _value;
            _value = value;
            return old;
        }

        /**
         * Indicates if this entry is considered equals to the specified entry
         * (using default value and key equality comparator to ensure symetry).
         * 
         * @param that the object to test for equality.
         * @return <code>true<code> if both entry have equal keys and values.
         *         <code>false<code> otherwise.
         */
        public boolean equals(Object that) {
            if (that instanceof Map.Entry) {
                Map.Entry entry = (Map.Entry) that;
                return _key.equals(entry.getKey())
                        && ((_value != null) ? _value.equals(entry.getValue())
                                : (entry.getValue() == null));
            } else {
                return false;
            }
        }

        /**
         * Returns the hash code for this entry.
         * 
         * @return this entry hash code.
         */
        public int hashCode() {
            return _key.hashCode() ^ ((_value != null) ? _value.hashCode() : 0);
        }

        // Implements Record interface.
        public final Record/*<FastMap.Entry<K,V>>*/getPreviousRecord() {
            return _previous;
        }

        // Implements Record interface.
        public final Record/*<FastMap.Entry<K,V>>*/getNextRecord() {
            return _next;
        }

        /**
         * Detaches this entry from the entry table and list.
         */
        private final void detach() {
            // Removes from list.
            _previous._next = _next;
            _next._previous = _previous;

            // Removes from bucket.
            final int index = (_keyHash >> R0) & (_table.length - 1);
            final Entry/*<K,V>*/beside = _beside;
            Entry/*<K,V>*/previous = _table[index][_keyHash & M0];
            if (previous == this) { // First in bucket.
                _table[index][_keyHash & M0] = beside;
            } else {
                while (previous._beside != this) {
                    previous = previous._beside;
                }
                previous._beside = beside;
            }
        }

    }

    /**
     * This class represents an read-only view over a {@link FastMap}.
     */
    private final class Unmodifiable extends RealtimeObject implements
            Map/*<K,V>*/, Serializable {

        public boolean equals(Object obj) {
            return FastMap.this.equals(obj);
        }

        public int hashCode() {
            return FastMap.this.hashCode();
        }

        public Text toText() {
            return FastMap.this.toText();
        }

        public int size() {
            return FastMap.this.size();
        }

        public boolean isEmpty() {
            return FastMap.this.isEmpty();
        }

        public boolean containsKey(Object key) {
            return FastMap.this.containsKey(key);
        }

        public boolean containsValue(Object value) {
            return FastMap.this.containsValue(value);
        }

        public Object/*V*/get(Object key) {
            return FastMap.this.get(key);
        }

        public Object/*V*/put(Object/*K*/key, Object/*V*/value) {
            throw new UnsupportedOperationException("Unmodifiable map");
        }

        public Object/*V*/remove(Object key) {
            throw new UnsupportedOperationException("Unmodifiable map");
        }

        public void putAll(Map/*<? extends K, ? extends V>*/map) {
            throw new UnsupportedOperationException("Unmodifiable map");
        }

        public void clear() {
            throw new UnsupportedOperationException("Unmodifiable map");
        }

        public Set/*<K>*/keySet() {
            return (Set/*<K>*/) FastMap.this._keySet.unmodifiable();
        }

        public Collection/*<V>*/values() {
            return FastMap.this._values.unmodifiable();
        }

        public Set/*<Map.Entry<K,V>>*/entrySet() {
            throw new UnsupportedOperationException(
                    "Direct view over unmodifiable map entries is not supported "
                            + " (to prevent access to Entry.setValue(Object) method). "
                            + "To iterate over unmodifiable map entries, applications may "
                            + "use the keySet() and values() fast collection views "
                            + "in conjonction.");
        }

    }
}