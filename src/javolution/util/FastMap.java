/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2006 - Javolution (http://javolution.org/)
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
import j2me.lang.UnsupportedOperationException;
import j2me.util.Collection;
import j2me.util.Iterator;
import j2me.util.Map;
import j2me.util.NoSuchElementException;
import j2me.util.Set;
import j2mex.realtime.MemoryArea;
import java.io.IOException;
import java.io.PrintStream;

import javolution.JavolutionError;
import javolution.context.LogContext;
import javolution.context.ObjectFactory;
import javolution.context.PersistentContext;
import javolution.lang.MathLib;
import javolution.lang.Realtime;
import javolution.lang.Reusable;
import javolution.text.Text;
import javolution.util.FastCollection.Record;
import javolution.xml.XMLSerializable;

/**
 * <p> This class represents a hash map with real-time behavior; 
 *     smooth capacity increase and <i>thread-safe</i> {@link #get access}
 *     without synchronization.</p>
 *     <img src="doc-files/map-put.png"/>
 *     
 * <p> {@link FastMap} has a predictable iteration order, which is the order in
 *     which keys are inserted into the map (similar to 
 *     <code>java.util.LinkedHashMap</code> collection class). Access using
 *     the {@link #get(Object) get(key)} is always thread-safe even when  
 *     entries are removed. If the map is marked {@link #setShared(boolean)
 *     shared} then all operations are thread-safe including iterations over the
 *     map's collections.</p>
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
 *     {@link String} and {@link javolution.text.Text Text} 
 *     ({@link FastComparator#LEXICAL LEXICAL}) or for identity maps 
 *     ({@link FastComparator#IDENTITY IDENTITY}).
 *     For example:[code]
 *     FastMap identityMap = new FastMap().setKeyComparator(FastComparator.IDENTITY);
 *     [/code]</p>
 * 
 * <p> {@link FastMap.Entry} can quickly be iterated over (forward or backward)
 *     without using iterators. For example:[code]
 *     FastMap<String, Thread> map = ...;
 *     for (FastMap.Entry<String, Thread> e = map.head(), end = map.tail(); (e = e.getNext()) != end;) {
 *          String key = e.getKey(); // No typecast necessary.
 *          Thread value = e.getValue(); // No typecast necessary.
 *     }[/code]</p>
 *     
 * <p> Finally, {@link FastMap} are {@link Reusable reusable}; they maintain an 
 *     internal pool of <code>Map.Entry</code> objects. When an entry is removed
 *     from a map, it is automatically restored to its pool (unless the map
 *     is shared in which case the removed entry is candidate for garbage 
 *     collection as it cannot be safely recycled).</p>
 *     
 * <p> <b>Implementation Note:</b> To maintain time-determinism, rehash/resize
 *     is performed only when the map's size is small. For large maps 
 *     (size > 512), the collection is divided into smaller sub-maps.</p>  
 *     
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle </a>
 * @version 5.2, September 3, 2007
 */
public class FastMap/*<K,V>*/implements Map/*<K,V>*/, Reusable,
        XMLSerializable, Realtime {

    // We do a full resize (and rehash) only when the capacity is less than C1.
    // For large maps (size > 512), we dispatch to sub-maps.

    private static final int B0 =  4; // Initial capacity in bits.

    private static final int C0 = 1 << B0; // Initial capacity (16)

    private static final int B1 = 10; // Entries array resize limit in bits.

    private static final int C1 = 1 << B1; // Entries array resize limit (1024).

    private static final int B2 = B1 - B0; // Sub-maps array length in bits.

    private static final int C2 = 1 << B2; // Sub-maps array length (32).

    /**
     * Holds the map's entries. 
     */
    private transient Entry/*<K,V>*/[] _entries;

    /**
     * Holds sub-maps (for large collection). 
     */
    private transient FastMap[] _subMaps;

    /**
     * Indicates if sub-maps are active.
     */
    private boolean _useSubMaps;

    /**
     * The hash shift (for sub-maps to discard bits already taken into account).
     */
    private int _keyShift;

    /**
     * Holds the head entry to which the first entry attaches.
     * The head entry never changes (entries always added last).
     */
    private transient Entry/*<K,V>*/_head;

    /**
     * Holds the tail entry to which the last entry attaches.
     * The tail entry changes as entries are added/removed.
     */
    private transient Entry/*<K,V>*/_tail;

    /**
     * Holds the current size.
     */
    private transient int _size;

    /**
     * Holds the values view.
     */
    private transient Values _values;

    /**
     * Holds the key set view.
     */
    private transient KeySet _keySet;

    /**
     * Holds the entry set view.
     */
    private transient EntrySet _entrySet;

    /**
     * Holds the unmodifiable view.
     */
    private transient Map/*<K,V>*/_unmodifiable;

    /**
     * Holds the key comparator.
     */
    private transient FastComparator _keyComparator = FastComparator.DEFAULT;

    /**
     * Holds the value comparator.
     */
    private transient FastComparator _valueComparator = FastComparator.DEFAULT;

    /**
     * Holds comparator set to <code>null</code> when equivalent to direct.
     */
    private transient FastComparator _keyComp = FastComparator._Rehash ? FastComparator.REHASH
            : null;

    /**
     * Indicates if this map is shared (thread-safe).
     */
    private transient boolean _isShared;

    /**
     * Creates a fast map of small initial capacity.
     */
    public FastMap() {
        this(4);
    }

    /**
     * Creates a persistent map associated to the specified unique identifier
     * (convenience method).
     * 
     * @param id the unique identifier for this map.
     * @throws IllegalArgumentException if the identifier is not unique.
     * @see javolution.context.PersistentContext.Reference
     */
    public FastMap(String id) {
        this();
        new PersistentContext.Reference(id, this) {
            protected void notifyChange() {
                FastMap.this.clear();
                FastMap.this.putAll((FastMap) this.get());
            }
        };
    }

    /**
     * Creates a map of specified initial capacity; unless the map size 
     * reaches the specified capacity, operations on this map will not allocate
     * memory (no lazy object creation).
     * 
     * @param capacity the initial capacity.
     */
    public FastMap(int capacity) {
        setup(capacity);
    }

    private void setup(int capacity) {
        int tableLength = C0;
        while (tableLength < capacity) {
            tableLength <<= 1;
        }
        _entries = (Entry/*<K,V>*/[]) new Entry[tableLength];
        _head = new Entry();
        _tail = new Entry();
        _head._next = _tail;
        _tail._previous = _head;
        Entry previous = _tail;
        for (int i = 0; i++ < capacity;) {
            Entry newEntry = new Entry();
            newEntry._previous = previous;
            previous._next = newEntry;
            previous = newEntry;
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
     * Returns a potentially {@link #recycle recycled} map instance.
     *
     * @return a new, preallocated or recycled map instance.
     */
    public static/*<K,V>*/FastMap/*<K,V>*/newInstance() {
        return (FastMap/*<K,V>*/) FACTORY.object();
    }

    /**
     * Recycles the specified map instance.
     * 
     * @param instance the map instance to recycle.
     */
    public static void recycle(FastMap instance) {
        FACTORY.recycle(instance);
    }

    /**
     * Returns the head entry of this map.
     *
     * @return the entry such as <code>head().getNext()</code> holds 
     *         the first map entry.
     */
    public final Entry/*<K,V>*/head() {
        return _head;
    }

    /**
     * Returns the tail entry of this map.
     *
     * @return the entry such as <code>tail().getPrevious()</code>
     *         holds the last map entry.
     */
    public final Entry/*<K,V>*/tail() {
        return _tail;
    }

    /**
     * Returns the number of key-value mappings in this {@link FastMap}.
     * 
     * <p>Note: If concurrent updates are performed, application should not 
     *          rely upon the size during iterations. Iterators (which  
     *          do not use the size) are safe.</p> 
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
        return getEntry(key) != null;
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
        return values().contains(value);
    }

    /**
     * Returns the value to which this map associates the specified key.
     * This method is always thread-safe regardless whether or not the map 
     * is marked {@link #isShared() shared}.
     * 
     * @param key the key whose associated value is to be returned.
     * @return the value to which this map maps the specified key, or
     *         <code>null</code> if there is no mapping for the key.
     * @throws NullPointerException if key is <code>null</code>.
     */
    public final Object/*{V}*/get(Object key) {
        Entry/*<K,V>*/entry = getEntry(key, (_keyComp == null) ? key
                .hashCode() : _keyComp.hashCodeOf(key));
        return (entry != null) ? entry._value : null;
    }

    /**
     * Returns the entry with the specified key.
     * This method is always thread-safe without synchronization.
     * 
     * @param key the key whose associated entry is to be returned.
     * @return the entry for the specified key or <code>null</code> if none.
     */
    public final Entry/*<K,V>*/getEntry(Object key) {
        return getEntry(key, (_keyComp == null) ? key.hashCode() : _keyComp
                .hashCodeOf(key));
    }

    private final Entry getEntry(Object key, int keyHash) {
        int hash = keyHash >> _keyShift;
        if (_useSubMaps) // Forwards to sub-maps (divide size by C2).
            return _subMaps[hash & (C2 - 1)].getEntry(key, keyHash);
        final Entry[] entries = _entries; // Atomic.
        while (true) {
            Entry entry = entries[hash++ & (entries.length - 1)];
            if (entry == null) return null;
            if ((key == entry._key)
                    || ((keyHash == entry._keyHash) && 
                 (_keyComp == null ? key.equals(entry._key) : _keyComp.areEqual(
                            key, entry._key))))
                return entry;
        }
    }

    /**
     * Associates the specified value with the specified key in this map.
     * If this map previously contained a mapping for this key, the old value
     * is replaced. For {@link #isShared() shared} map, internal synchronization
     * is performed only when new entries are created.
     * 
     * @param key the key with which the specified value is to be associated.
     * @param value the value to be associated with the specified key.
     * @return the previous value associated with specified key, or
     *         <code>null</code> if there was no mapping for key. A
     *         <code>null</code> return can also indicate that the map
     *         previously associated <code>null</code> with the specified key.
     * @throws NullPointerException if the key is <code>null</code>.
     */
    public final Object/*{V}*/put(Object/*{K}*/key, Object/*{V}*/value) {
        final int keyHash = (_keyComp == null) ? key.hashCode() : _keyComp
                .hashCodeOf(key);
        Entry/*<K,V>*/entry = getEntry(key, keyHash);
        if (entry != null) {
            Object/*{V}*/prevValue = entry._value;
            entry._value = value;
            return prevValue;
        }
        if (_isShared) // Synchronization only when entry not found.
            return putShared(key, value, keyHash);
        addEntry(keyHash, key, value);
        return null;
    }

    private synchronized Object/*{V}*/putShared(Object/*{K}*/key,
            Object/*{V}*/value, int keyHash) {
        Entry/*<K,V>*/entry = getEntry(key, keyHash); // To avoid duplicates.
        if (entry == null) { // Most likely.
            addEntry(keyHash, key, value);
            return null;
        }
        Object/*{V}*/prevValue = entry._value;
        entry._value = value;
        return prevValue;
    }

    private void addEntry(int hash, Object/*{K}*/key, Object/*{V}*/value) {
        if (_tail._next == null) {
            createNewEntries();
        }
        final Entry newTail = _tail._next;

        // Setups entry parameters.
        _tail._key = key;
        _tail._value = value;
        _tail._keyHash = hash;

        mapEntry(_tail);

        // Moves tail forward (must be last).
        _tail = newTail;
    }

    private void createNewEntries() { // Increase the number of entries.
        MemoryArea.getMemoryArea(this).executeInArea(new Runnable() {
            public void run() {
                Entry/*<K,V>*/newEntry0 = new Entry/*<K,V>*/();
                newEntry0._previous = _tail;
                _tail._next = newEntry0;

                Entry/*<K,V>*/newEntry1 = new Entry/*<K,V>*/();
                newEntry1._previous = newEntry0;
                newEntry0._next = newEntry1;

                Entry/*<K,V>*/newEntry2 = new Entry/*<K,V>*/();
                newEntry2._previous = newEntry1;
                newEntry1._next = newEntry2;

                Entry/*<K,V>*/newEntry3 = new Entry/*<K,V>*/();
                newEntry3._previous = newEntry2;
                newEntry2._next = newEntry3;
            }
        });
    }

    private void mapEntry(Entry entry) {
        _size++;
        int hash = entry._keyHash >> _keyShift;
        if (_useSubMaps) { // Forwards to sub-maps.
            _subMaps[hash & (C2 - 1)].mapEntry(entry);
            return;
        }
        // Direct entry mapping.
        while (true) {
            final int index = hash++ & (_entries.length - 1);
            Entry e = _entries[index];
            if (e == null) { // Found slot.
                _entries[index] = entry;
                break;
            }
        }
        if (_size > (_entries.length >> 1)) { // Table more than half empty.
            resizeTable();
        }
    }

    private void resizeTable() {
        MemoryArea.getMemoryArea(this).executeInArea(new Runnable() {
            public void run() {
                final int tableLength = _entries.length << 1;
                if (tableLength > C1) { // Lets' try using sub-maps instead.
                    if (_subMaps == null) { // Creates sub-maps.
                        _subMaps = new FastMap[C2];
                        for (int i = 0; i < C2; i++) {
                            FastMap subMap = new FastMap(0);
                            subMap._keyShift = B2 + _keyShift;
                            _subMaps[i] = subMap;
                        }
                    }
                    // Copies the current entries to sub-maps. 
                    // We still keep the entry in the main table (used as cache).
                    boolean wellDistributed = true;
                    for (int i = 0; i < _entries.length;) {
                        Entry entry = _entries[i++];
                        if (entry == null)
                            continue;
                        int hash = entry._keyHash >> _keyShift;
                        FastMap subMap = _subMaps[hash & (C2 - 1)];
                        if (subMap._size >= (_size >> 1)) { // Bad distribution.
                            wellDistributed = false;
                            break;
                        }
                        subMap.mapEntry(entry);
                    }
                    if (wellDistributed) {
                        _useSubMaps = true;
                        return;
                    }
                    LogContext.warning("Uneven hash distribution");
                    _subMaps = null; // Discards sub-maps.
                }
                // Small enough to resize (or uneven hash code distribution).                
                Entry[] newEntries = new Entry[tableLength];
                for (int i = 0; i < _entries.length;) {
                    Entry entry = _entries[i++];
                    if (entry == null)
                        continue;
                    int hash = entry._keyHash >> _keyShift;
                    while (true) {
                        final int index = hash & (tableLength - 1);
                        Entry e = newEntries[index];
                        if (e == null) { // Found slot.
                            newEntries[index] = entry;
                            break;
                        }
                        hash++;
                    }
                }
                _entries = newEntries; // Replaces (atomic).
            }
        });
    }

    /**
     * Copies all of the mappings from the specified map to this map.
     * 
     * @param map the mappings to be stored in this map.
     * @throws NullPointerException the specified map is <code>null</code>,
     *         or the specified map contains <code>null</code> keys.
     */
    public final void putAll(Map/*<? extends K, ? extends V>*/map) {
        for (Iterator i = map.entrySet().iterator(); i.hasNext();) {
            Map.Entry/*<K,V>*/e = (Map.Entry/*<K,V>*/) i.next();
            put(e.getKey(), e.getValue());
        }
    }

    /**
     * Removes the entry for the specified key if present. The entry 
     * is recycled if the map is not marked as {@link #isShared shared};
     * otherwise the entry is candidate for garbage collection.
     * 
     * <p> Note: Shared maps in ImmortalMemory (e.g. static) should not remove
     *           their entries as it could cause a memory leak (ImmortalMemory
     *           is never garbage collected), instead they should set their 
     *           entry values to <code>null</code>.</p> 
     * 
     * @param key the key whose mapping is to be removed from the map.
     * @return previous value associated with specified key, or
     *         <code>null</code> if there was no mapping for key. A
     *         <code>null</code> return can also indicate that the map
     *         previously associated <code>null</code> with the specified key.
     * @throws NullPointerException if the key is <code>null</code>.
     */
    public final Object/*{V}*/remove(Object key) {
        final int keyHash = (_keyComp == null) ? key.hashCode() : _keyComp
                .hashCodeOf(key);
        Entry/*<K,V>*/entry = getEntry(key, keyHash);
        if (entry == null)
            return null;
        if (_isShared) // Synchronization only if entry found.
            return removeShared(key, keyHash);
        Object/*{V}*/prevValue = entry._value;
        removeEntry(entry);
        return prevValue;
    }

    private synchronized Object/*{V}*/removeShared(Object key, int keyHash) {
        Entry/*<K,V>*/entry = getEntry(key, keyHash); // Avoids removing twice.
        if (entry == null)
            return null;
        Object/*{V}*/prevValue = entry._value;
        removeEntry(entry);
        return prevValue;
    }

    private final void removeEntry(Entry entry) {

        // Detaches from list.
        entry._previous._next = entry._next;
        entry._next._previous = entry._previous;

        // Removes from tables.
        unmapEntry(entry);

        if (!_isShared) { // Re-insert at the end (for reuse).
            // Clears value and key (only if not shared).
            entry._key = null;
            entry._value = null;

            final Entry next = _tail._next;
            entry._previous = _tail;
            entry._next = next;
            _tail._next = entry;
            if (next != null) {
                next._previous = entry;
            }
        }
    }

    private void unmapEntry(Entry entry) {
        _size--;
        if (_useSubMaps) { // Forwards to sub-maps.
            _subMaps[(entry._keyHash >> _keyShift) & (C2 - 1)]
                    .unmapEntry(entry);
            return;
        } else { // Fills the slot where the entry actually is .
            pack(FastMap.indexOf(entry, _entries, _keyShift));
        }
    }

    // This is the most difficult part. When clearing the slot we cannot just 
    // set it to null; we have to pack to ensure that entries not in their own
    // slot are still reachable. Interestingly, packing is thread-safe
    // with regard to concurrent "get". In other words, removing an entry 
    // does not break our unsynchronized get (during packing we might have
    // duplicate entries in the table which is ok).
    private void pack(int slot) {
        final int mask = _entries.length - 1;
        int i = slot;
        while (true) {
            Entry entry = _entries[++i & mask];
            if (entry == null) { // Done.
                _entries[slot & mask] = null;
                return;
            }
            int entrySlot = FastMap.slotOf(entry, _entries, _keyShift);
            int distToSlot = (i & mask) - entrySlot; 
            if (distToSlot < 0) // Modulo operation.
                distToSlot += _entries.length;
            if ((i - slot) > distToSlot)
                continue; // Entry's slot below the packing slot.
            // Moves entry to avoid breaking reachability.
            _entries[slot & mask] = entry;
            // Now pack the previous slot of the entry moved.
            slot = i;
        }
    }

    /**
     * <p> Sets the shared status of this map (whether the map is thread-safe 
     *     or not). Shared maps are typically used for lookup table (e.g. static 
     *     instances in ImmortalMemory). They support concurrent access 
     *     (e.g. iterations) without synchronization, the maps updates 
     *     themselves are synchronized internally.</p>
     * <p> Unlike <code>ConcurrentHashMap</code> access to a shared map never 
     *     blocks. Retrieval reflects the map state not older than the last 
     *     time the accessing thread has been synchronized (for multi-processors
     *     systems synchronizing ensures that the CPU internal cache is not 
     *     stale).</p>
     * 
     * @param isShared <code>true</code> if this map is shared and thread-safe;
     *        <code>false</code> otherwise.
     * @return <code>this</code>
     */
    public FastMap/*<K,V>*/setShared(boolean isShared) {
        _isShared = isShared;
        return this;
    }

    /**
     * Indicates if this map supports concurrent operations without 
     * synchronization (default unshared).
     * 
     * @return <code>true</code> if this map is thread-safe; <code>false</code> 
     *         otherwise.
     */
    public boolean isShared() {
        return _isShared;
    }

    /**
     * Sets the key comparator for this fast map.
     * 
     * @param keyComparator the key comparator.
     * @return <code>this</code>
     */
    public FastMap/*<K,V>*/setKeyComparator(
            FastComparator/*<? super K>*/keyComparator) {
        _keyComparator = keyComparator;
        _keyComp = (keyComparator instanceof FastComparator.Default) ? (FastComparator._Rehash ? FastComparator.REHASH
                : null)
                : (keyComparator instanceof FastComparator.Direct) ? null
                        : keyComparator;
        return this;
    }

    /**
     * Returns the key comparator for this fast map.
     * 
     * @return the key comparator.
     */
    public FastComparator/*<? super K>*/getKeyComparator() {
        return _keyComparator;
    }

    /**
     * Sets the value comparator for this map.
     * 
     * @param valueComparator the value comparator.
     * @return <code>this</code>
     */
    public FastMap/*<K,V>*/setValueComparator(
            FastComparator/*<? super V>*/valueComparator) {
        _valueComparator = valueComparator;
        return this;
    }

    /**
     * Returns the value comparator for this fast map.
     * 
     * @return the value comparator.
     */
    public FastComparator/*<? super V>*/getValueComparator() {
        return _valueComparator;
    }

    /**
     * Removes all map's entries. The entries are removed and recycled; 
     * unless this map is {@link #isShared shared} in which case the entries 
     * are candidate for garbage collection.
     * 
     * <p> Note: Shared maps in ImmortalMemory (e.g. static) should not remove
     *           their entries as it could cause a memory leak (ImmortalMemory
     *           is never garbage collected), instead they should set their 
     *           entry values to <code>null</code>.</p> 
     */
    public final void clear() {
        if (_isShared) {
            clearShared();
            return;
        }
        // Clears keys, values and recycle entries.
        for (Entry e = _head, end = _tail; (e = e._next) != end;) {
            e._key = null;
            e._value = null;
        }
        _tail = _head._next; // Reuse linked list of entries.       
       
        clearTables();
    }
    
    private void clearTables() { 
        if (_useSubMaps) {
            for (int i = 0; i < C2;) {
                _subMaps[i++].clearTables();
            }
            _useSubMaps = false;
        }
        // We use brute force.
        for (int i = 0; i < _entries.length;) {
            int count = MathLib.min(_entries.length - i, C1);
            System.arraycopy(NULL_ENTRIES, 0, _entries, i, count);
            i += count;
        }        
        _size = 0;
    }
    private static final Entry[] NULL_ENTRIES = new Entry[C1];
    
    private synchronized void clearShared() {
        // We do not modify the linked list of entries (e.g. key, values) 
        // Concurrent iterations can still proceed unaffected.
        // The linked list fragment is detached from the map and will be 
        // garbage collected once all concurrent iterations complete.
        _head._next = _tail;
        _tail._previous = _head;

        // We also detach the entry table and sub-maps.
        _entries = (Entry/*<K,V>*/[]) new Entry[C0];
        if (_useSubMaps) {
            _useSubMaps = false;
            for (int i = 0; i < C2;) {
                _subMaps[i++] = new FastMap(0);
            }
        }
        _size = 0;
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
            return this.entrySet().equals(that.entrySet());
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
        return Text.valueOf(entrySet());
    }

    /**
     * Returns the <code>String</code> representation of this 
     * {@link FastMap}.
     *
     * @return <code>toText().toString()</code>
     */
    public final String toString() {
        return toText().toString();
    }

    /**
     * Prints the current statistics on this map.
     * This method may help identify poorly defined hash functions.
     * The average distance should be 0 (most of the entries are in their 
     * slots or close). 
     *  
     * @param out the stream to use for output (e.g. <code>System.out</code>)
     */
    public void printStatistics(PrintStream out) {
        long sum = this.getSumDistance();
        int max = this.getMaximumDistance();
        int avgDistance = _size != 0 ? (int) (sum / _size) : 0;
        synchronized (out) {
            out.print("SIZE: " + _size);
            out.print(", TABLE LENGTH: " + _entries.length);
            out.print(", USE SUB-MAPS: " + _useSubMaps);
            out.print(", IS SHARED: " + _isShared);
            out.print(", AVG DISTANCE: " + avgDistance);
            out.print(", MAX DISTANCE: " + max);
            out.println();
        }
    }

    private int getMaximumDistance() {
        int max = 0;
        if (_useSubMaps) {
            for (int i = 0; i < C2; i++) {
                int subMapMax = _subMaps[i].getMaximumDistance();
                max = MathLib.max(max, subMapMax);
            }
            return max;
        }
        for (int i = 0; i < _entries.length; i++) {
            Entry entry = _entries[i];
            if (entry != null) {
                int distanceToSlot = i - FastMap.slotOf(entry, _entries, _keyShift);
                if (distanceToSlot < 0)
                    distanceToSlot += _entries.length;
                if (distanceToSlot > max) {
                    max = distanceToSlot;
                }
            }
        }
        return max;
    }

    private long getSumDistance() {
        long sum = 0;
        if (_useSubMaps) {
            for (int i = 0; i < C2; i++) {
                sum += _subMaps[i].getSumDistance();
            }
            return sum;
        }
        for (int i = 0; i < _entries.length; i++) {
            Entry entry = _entries[i];
            if (entry != null) {
                int distanceToSlot = i - FastMap.slotOf(entry, _entries, _keyShift);
                if (distanceToSlot < 0)
                    distanceToSlot += _entries.length;
                sum += distanceToSlot;
            }
        }
        return sum;
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
        if (_values == null) {
            MemoryArea.getMemoryArea(this).executeInArea(new Runnable() {
                public void run() {
                    _values = new Values();
                }
            });
        }
        return _values;
    }

    private final class Values extends FastCollection {

        public int size() {
            return _size;
        }

        public void clear() {
            FastMap.this.clear();
        }

        public Record head() {
            return FastMap.this._head;
        }

        public Record tail() {
            return FastMap.this._tail;
        }

        public Object valueOf(Record record) {
            return ((Entry) record)._value;
        }

        public void delete(Record record) {
            FastMap.this.remove(((Entry) record).getKey());
        }

        public FastComparator getValueComparator() {
            return _valueComparator;
        }

        public Iterator iterator() {
            return ValueIterator.valueOf(FastMap.this);
        }
    }

    // Value iterator optimization.
    private static final class ValueIterator implements Iterator {

        private static final ObjectFactory FACTORY = new ObjectFactory() {
            protected Object create() {
                return new ValueIterator();
            }

            protected void cleanup(Object obj) {
                ValueIterator iterator = (ValueIterator) obj;
                iterator._map = null;
                iterator._current = null;
                iterator._next = null;
                iterator._tail = null;
            }
        };

        private FastMap _map;

        private Entry _current;

        private Entry _next;

        private Entry _tail;

        public static ValueIterator valueOf(FastMap map) {
            ValueIterator iterator = (ValueIterator) ValueIterator.FACTORY
                    .object();
            iterator._map = map;
            iterator._next = map._head._next;
            iterator._tail = map._tail;
            return iterator;
        }

        private ValueIterator() {
        }

        public boolean hasNext() {
            return (_next != _tail);
        }

        public Object next() {
            if (_next == _tail)
                throw new NoSuchElementException();
            _current = _next;
            _next = _next._next;
            return _current._value;
        }

        public void remove() {
            if (_current != null) {
                _next = _current._next;
                _map.remove(_current._key);
                _current = null;
            } else {
                throw new IllegalStateException();
            }
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
        if (_entrySet == null) {
            MemoryArea.getMemoryArea(this).executeInArea(new Runnable() {
                public void run() {
                    _entrySet = new EntrySet();
                }
            });
        }
        return _entrySet;
    }

    private final class EntrySet extends FastCollection implements Set {

        public int size() {
            return _size;
        }

        public void clear() {
            FastMap.this.clear();
        }

        public boolean contains(Object obj) { // Optimization.
            if (obj instanceof Map.Entry) {
                Map.Entry thatEntry = (Entry) obj;
                Entry thisEntry = getEntry(thatEntry.getKey());
                if (thisEntry == null)
                    return false;
                return _valueComparator.areEqual(thisEntry.getValue(),
                        thatEntry.getValue());
            } else {
                return false;
            }
        }

        public Text toText() {
            Text text = Text.valueOf('[');
            final Text equ = Text.valueOf('=');
            final Text sep = Text.valueOf(", ");
            for (Entry e = _head, end = _tail; (e = e._next) != end;) {
                text = text.concat(Text.valueOf(e._key)).concat(equ).concat(
                        Text.valueOf(e._value));
                if (e._next != end) {
                    text = text.concat(sep);
                }
            }
            return text.concat(Text.valueOf(']'));
        }

        public Record head() {
            return _head;
        }

        public Record tail() {
            return _tail;
        }

        public Object valueOf(Record record) {
            return (Map.Entry) record;
        }

        public void delete(Record record) {
            FastMap.this.remove(((Entry) record).getKey());
        }

        public FastComparator getValueComparator() {
            return _entryComparator;
        }

        private final FastComparator _entryComparator = new FastComparator() {

            public boolean areEqual(Object o1, Object o2) {
                if ((o1 instanceof Map.Entry) && (o2 instanceof Map.Entry)) {
                    Map.Entry e1 = (Map.Entry) o1;
                    Map.Entry e2 = (Map.Entry) o2;
                    return _keyComparator.areEqual(e1.getKey(), e2.getKey())
                            && _valueComparator.areEqual(e1.getValue(), e2
                                    .getValue());
                }
                return (o1 == null) && (o2 == null);
            }

            public int compare(Object o1, Object o2) {
                return _keyComparator.compare(o1, o2);
            }

            public int hashCodeOf(Object obj) {
                Map.Entry entry = (Map.Entry) obj;
                return _keyComparator.hashCodeOf(entry.getKey())
                        + _valueComparator.hashCodeOf(entry.getValue());
            }
        };

        public Iterator iterator() {
            return EntryIterator.valueOf(FastMap.this);
        }
    }

    // Entry iterator optimization.
    private static final class EntryIterator implements Iterator {

        private static final ObjectFactory FACTORY = new ObjectFactory() {
            protected Object create() {
                return new EntryIterator();
            }

            protected void cleanup(Object obj) {
                EntryIterator iterator = (EntryIterator) obj;
                iterator._map = null;
                iterator._current = null;
                iterator._next = null;
                iterator._tail = null;
            }
        };

        private FastMap _map;

        private Entry _current;

        private Entry _next;

        private Entry _tail;

        public static EntryIterator valueOf(FastMap map) {
            EntryIterator iterator = (EntryIterator) EntryIterator.FACTORY
                    .object();
            iterator._map = map;
            iterator._next = map._head._next;
            iterator._tail = map._tail;
            return iterator;
        }

        private EntryIterator() {
        }

        public boolean hasNext() {
            return (_next != _tail);
        }

        public Object next() {
            if (_next == _tail)
                throw new NoSuchElementException();
            _current = _next;
            _next = _next._next;
            return _current;
        }

        public void remove() {
            if (_current != null) {
                _next = _current._next;
                _map.remove(_current._key);
                _current = null;
            } else {
                throw new IllegalStateException();
            }
        }
    }

    /**
     * Returns a {@link FastCollection} view of the keys contained in this 
     * map. The set is backed by the map, so changes to the map are reflected
     * in the set, and vice-versa. The set supports element removal, which 
     * removes the corresponding mapping from this map, via the 
     * <code>Iterator.remove</code>,
     <code>Collection.remove</code>,<code>removeAll<f/code>,
     * <code>retainAll</code>, and <code>clear</code> operations. It does
     * not support the <code>add</code> or <code>addAll</code> operations.
     * 
     * @return a set view of the keys contained in this map
     *         (instance of {@link FastCollection}).
     */
    public final Set/*<K>*/keySet() {
        if (_keySet == null) {
            MemoryArea.getMemoryArea(this).executeInArea(new Runnable() {
                public void run() {
                    _keySet = new KeySet();
                }
            });
        }
        return _keySet;
    }

    private final class KeySet extends FastCollection implements Set {

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

        public Record head() {
            return FastMap.this._head;
        }

        public Record tail() {
            return FastMap.this._tail;
        }

        public Object valueOf(Record record) {
            return ((Entry) record)._key;
        }

        public void delete(Record record) {
            FastMap.this.remove(((Entry) record).getKey());
        }

        public FastComparator getValueComparator() {
            return _keyComparator;
        }

        public Iterator iterator() {
            return KeyIterator.valueOf(FastMap.this);
        }
    }

    // Entry iterator optimization.
    private static final class KeyIterator implements Iterator {

        private static final ObjectFactory FACTORY = new ObjectFactory() {
            protected Object create() {
                return new KeyIterator();
            }

            protected void cleanup(Object obj) {
                KeyIterator iterator = (KeyIterator) obj;
                iterator._map = null;
                iterator._current = null;
                iterator._next = null;
                iterator._tail = null;
            }
        };

        private FastMap _map;

        private Entry _current;

        private Entry _next;

        private Entry _tail;

        public static KeyIterator valueOf(FastMap map) {
            KeyIterator iterator = (KeyIterator) KeyIterator.FACTORY.object();
            iterator._map = map;
            iterator._next = map._head._next;
            iterator._tail = map._tail;
            return iterator;
        }

        private KeyIterator() {
        }

        public boolean hasNext() {
            return (_next != _tail);
        }

        public Object next() {
            if (_next == _tail)
                throw new NoSuchElementException();
            _current = _next;
            _next = _next._next;
            return _current._key;
        }

        public void remove() {
            if (_current != null) {
                _next = _current._next;
                _map.remove(_current._key);
                _current = null;
            } else {
                throw new IllegalStateException();
            }
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
        if (_unmodifiable == null) {
            MemoryArea.getMemoryArea(this).executeInArea(new Runnable() {
                public void run() {
                    _unmodifiable = new Unmodifiable();
                }
            });
        }
        return _unmodifiable;
    }

    // Implements Reusable.
    public void reset() {
        setShared(false); // A shared map can only be reset if no thread use it.
        clear(); // In which case, it is safe to recycle the entries.
        setKeyComparator(FastComparator.DEFAULT);
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
        setKeyComparator((FastComparator) stream.readObject());
        setValueComparator((FastComparator) stream.readObject());
        setShared(stream.readBoolean());
        final int size = stream.readInt();
        setup(size);
        for (int i = 0; i < size; i++) {
            Object/*{K}*/key = (Object/*{K}*/) stream.readObject();
            Object/*{V}*/value = (Object/*{V}*/) stream.readObject();
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
        stream.writeObject(getKeyComparator());
        stream.writeObject(getValueComparator());
        stream.writeBoolean(_isShared);
        stream.writeInt(_size);
        for (Entry e = _head, end = _tail; (e = e._next) != end;) {
            stream.writeObject(e._key);
            stream.writeObject(e._value);
        }
    }

    /**
     * This class represents a {@link FastMap} entry.
     */
    public static final class Entry/*<K,V>*/implements Map.Entry/*<K,V>*/,
            Record {

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
        private Object/*{K}*/_key;

        /**
         * Holds the entry value.
         */
        private Object/*{V}*/_value;

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
        public final Record/*Entry<K,V>*/getNext() {
            return _next;
        }

        /**
         * Returns the entry before this one.
         * 
         * @return the previous entry.
         */
        public final Record/*Entry<K,V>*/getPrevious() {
            return _previous;
        }

        /**
         * Returns the key for this entry.
         * 
         * @return the entry key.
         */
        public final Object/*{K}*/getKey() {
            return _key;
        }

        /**
         * Returns the value for this entry.
         * 
         * @return the entry value.
         */
        public final Object/*{V}*/getValue() {
            return _value;
        }

        /**
         * Sets the value for this entry.
         * 
         * @param value the new value.
         * @return the previous value.
         */
        public final Object/*{V}*/setValue(Object/*{V}*/value) {
            Object/*{V}*/old = _value;
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
                return FastComparator.DEFAULT.areEqual(_key, entry.getKey())
                        && FastComparator.DEFAULT.areEqual(_value, entry
                                .getValue());
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
            return ((_key != null) ? _key.hashCode() : 0)
                    ^ ((_value != null) ? _value.hashCode() : 0);
        }
    }

    /**
     * This class represents an read-only view over a {@link FastMap}.
     */
    private final class Unmodifiable implements Map, Serializable {

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

        public Object get(Object key) {
            return FastMap.this.get(key);
        }

        public Object put(Object key, Object value) {
            throw new UnsupportedOperationException("Unmodifiable map");
        }

        public Object remove(Object key) {
            throw new UnsupportedOperationException("Unmodifiable map");
        }

        public void putAll(Map map) {
            throw new UnsupportedOperationException("Unmodifiable map");
        }

        public void clear() {
            throw new UnsupportedOperationException("Unmodifiable map");
        }

        public Set keySet() {
            return (Set) ((KeySet) FastMap.this.keySet()).unmodifiable();
        }

        public Collection values() {
            return ((Values) FastMap.this.values()).unmodifiable();
        }

        public Set entrySet() {
            throw new UnsupportedOperationException(
                    "Direct view over unmodifiable map entries is not supported "
                            + " (to prevent access to Entry.setValue(Object) method). "
                            + "To iterate over unmodifiable map entries, applications may "
                            + "use the keySet() and values() fast collection views "
                            + "in conjonction.");
        }
    }

    // Holds the map factory.
    private static final ObjectFactory FACTORY = new ObjectFactory() {

        public Object create() {
            return new FastMap();
        }

        public void cleanup(Object obj) {
            ((FastMap) obj).reset();
        }
    };

    // Returns the slot of the specified entry.
    private static int slotOf(Entry entry, Entry[] entries, int keyShift) {
        return (entry._keyHash >> keyShift) & (entries.length - 1);
    }

    // Returns the index of the specified entry. It can be different from
    // its slot due to collision.
    private static int indexOf(Entry entry, Entry[] entries, int keyShift) {
        int index = slotOf(entry, entries, keyShift);
        while (true) {
            Entry e = entries[index];
            if (e == entry) return index;
            if (e == null) throw new JavolutionError( // Entry has been removed!
                    "Concurrent maps should be marked shared");
            index = (index + 1) & (entries.length - 1);
        }
    }

    private static final long serialVersionUID = 1L;
}