/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.util;

import static org.javolution.annotations.Realtime.Limit.CONSTANT;
import static org.javolution.annotations.Realtime.Limit.LINEAR;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentMap;

import org.javolution.annotations.Nullable;
import org.javolution.annotations.ReadOnly;
import org.javolution.annotations.Realtime;
import org.javolution.lang.Immutable;
import org.javolution.util.function.Equality;
import org.javolution.util.function.Order;
import org.javolution.util.function.Predicate;
import org.javolution.util.internal.map.AtomicMapImpl;
import org.javolution.util.internal.map.FilteredMapImpl;
import org.javolution.util.internal.map.KeySetImpl;
import org.javolution.util.internal.map.LinkedMapImpl;

/**
 * High-performance ordered map / multimap with {@link Realtime strict timing constraints}.
 * 
 * A multimap is a generalization of a map or associative array abstract data type in which more than one value may 
 * be associated with and returned for a given key.
 * 
 * From a semantic standpoint, {@link AbstractMap} is considered equivalent to an {@link AbstractSet} 
 * of {@link Entry} elements. Accessing/adding/removing custom entries is allowed through the methods: 
 * {@link  #getEntry}, {@link  #addEntry} and {@link #removeEntry} (or through the {@link #entrySet} view).
 *     
 * Instances of this class may use custom key comparators instead of the default object equality 
 * when comparing keys. This affects the behavior of the containsKey, put, remove, equals, and 
 * hashCode methods (see {@link java.util.IdentityHashMap} for such map in the standard library).
 * The {@link java.util.Map} contract is guaranteed to hold only for maps using {@link Equality#STANDARD} for 
 * {@link #equality() key comparisons}.
 *      
 * @param <K> the type of keys ({@code null} values are not supported)
 * @param <V> the type of values 
 *             
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle </a>
 * @version 7.0, September 13, 2015
 */
@Realtime
public abstract class AbstractMap<K, V> implements ConcurrentMap<K, V>, SortedMap<K,V>, Cloneable, Serializable {

    private static final long serialVersionUID = 0x700L; // Version.

    /** Returns this map with the specified key, value pair added (convenience method). */
    public AbstractMap<K,V> with(K key, V value) {
        put(key, value);
        return this;
    }

    /** Returns this map with the entries from the specified map added (convenience method). */
    public AbstractMap<K,V> with(Map<? extends K, ? extends V> that) {
        putAll(that);
        return this;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Views.
    //
    
     
    /** 
     * Returns a view allowing multiple instances of the same key to be put into this map (multimap).
     */
    @Realtime(limit = CONSTANT)
    public AbstractMap<K,V> multi() {
        return null; // TODO
    }
     
    /** 
     * Returns a view of the portion of this map whose elements range from {@code fromElement} to {@code toElement}.
     */
    @Realtime(limit = CONSTANT)
    public AbstractMap<K,V> subMap(@Nullable K fromKey, boolean fromInclusive, @Nullable K toKey, boolean toInclusive) {
        return null; // TODO
    }
     
    /**
     * Returns an atomic view over this map. All operations that write or access multiple elements in the map 
     * are atomic. All read operations are mutex-free.
     */
    public AbstractMap<K, V> atomic() {
        return new AtomicMapImpl<K, V>(this);
    }

    /**
     * Returns a view exposing only the entries matching the specified filter. Mapping keys not 
     * matching the specified filter has no effect. If this map is initially empty, using a filtered view 
     * ensures that this map has only entries satisfying the specified filter predicate.
     */
    public AbstractMap<K, V> filter(Predicate<? super Entry<K,V>> filter) {
        return new FilteredMapImpl<K, V>(this, filter);
    }

    /**
     * Returns a thread-safe view over this map. The shared view allows for concurrent read as long as 
     * there is no writer. The default implementation is based on <a href=
     * "http://en.wikipedia.org/wiki/Readers%E2%80%93writer_lock"> readers-writers locks</a> giving priority to writers. 
     */
    public AbstractMap<K, V> shared() {
        return null; // TODO
    }

    /**
     * Returns a view keeping track of the insertion order and exposing entries/keys/values in that order 
     * (first added, first to iterate). This view can be useful for compatibility with Java linked collections
     * (e.g. {@code LinkedHashMap}). Elements not {@link #put added} through this view are ignored when iterating.
     */
    public AbstractMap<K, V> linked() {
        return new LinkedMapImpl<K, V>(this);
    }

    /**
     * Returns an unmodifiable view over this map. Any attempt to modify the map through this view will result into a
     * {@link java.lang.UnsupportedOperationException} being raised.
     */
    public @ReadOnly AbstractMap<K, V> unmodifiable() {
        return null; // TODO
    }

    /**
     * Returns a view over this map using the specified equality for map's values.
     */
    public AbstractMap<K, V> valuesEquality(Equality<? super V> equality) {
        return null; // TODO
    }
    
    /** Returns this map's keys (the element order of the set is {@link #keyOrder()}. */
    @Override
    public AbstractSet<K> keySet() {
        return new KeySetImpl<K, V>(this);
    }

    /** Returns this map's values (the element equality of the collection is {@link #valuesEquality()}. */
    @Override
    public AbstractCollection<V> values() {
        return null; // TODO
    }

    /** Returns this map's entries (the element order of the set is based on {@link #keyOrder()} and 
     *  {@link #valuesEquality()}). */
    @Override
    public AbstractSet<Map.Entry<K, V>> entrySet() {
        return null; // TODO
    }

    @Override
    public AbstractMap<K, V> subMap(K fromKey, K toKey) {
        return subMap(fromKey, true, toKey, false);
    }

    @Override
    public AbstractMap<K, V> headMap(K toKey) {
        return subMap(null, false, toKey, false);
    }

    @Override
    public AbstractMap<K, V> tailMap(K fromKey) {
        return subMap(fromKey, true, null, false);
    }


    ////////////////////////////////////////////////////////////////////////////
    // Map Interface.
    //

    @Override
    @Realtime(limit = LINEAR, comment = "May count the number of entries (e.g. filtered views)")
    public abstract int size();

    @Override
    @Realtime(limit = LINEAR, comment = "Could iterate the whole map (e.g. filtered views).")
    public abstract boolean isEmpty();

    @SuppressWarnings("unchecked")
    @Override
    public boolean containsKey(Object key) {
        return getEntry((K) key) != null; // Cast has no effect here.
    }

    @Override
    @Realtime(limit = LINEAR)
    public boolean containsValue(@Nullable Object value) {
        return values().contains(value);
    }

    @Override
    public @Nullable V get(Object key) {
        @SuppressWarnings("unchecked")
        Entry<K, V> entry = getEntry((K) key); // Cast has no effect here.
        return entry != null ? entry.getValue() : null;
    }

    /** 
     * Returns the value at the specified key or the specified default if that value is {@code null} 
     * (convenience method). 
     */
     public final V get(Object key, V defaultIfNull) {
        V value = get(key);
        return value != null ? value : defaultIfNull;
    }

    @Override
    public @Nullable V remove(Object key) {
        @SuppressWarnings("unchecked")
        Entry<K, V> entry = removeEntry((K) key); // Cast has no effect here.
        return entry != null ? entry.getValue() : null;
    }

    @Override
    public @Nullable V put(K key, @Nullable V value) {
        Entry<K, V> entry = getEntry(key);
        if (entry != null) return entry.setValueUnsafe(value);
        addEntry(new Entry<K,V>(key, value));
        return null;
    }

    @Override
    @Realtime(limit = LINEAR)
    public void putAll(Map<? extends K, ? extends V> that) {
       for (java.util.Map.Entry<? extends K, ? extends V> entry : that.entrySet())
            put(entry.getKey(), entry.getValue());
    }

    @Override
    @Realtime(limit = LINEAR, comment = "Views may have to remove entries one at a time (e.g. filtered views)")
    public abstract void clear();

    ////////////////////////////////////////////////////////////////////////////
    // ConcurrentMap Interface.
    //

    @Override
    public V putIfAbsent(K key, @Nullable V value) {
        Entry<K, V> entry = getEntry(key);
        return (entry == null) ? put(key, value) : entry.getValue();
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean remove(Object key, @Nullable Object value) {
        Entry<K, V> entry = getEntry((K) key);
        if ((entry != null) && valuesEquality().areEqual(entry.getValue(), (V) value)) {
            remove(key);
            return true;
        }
        return false;
    }

    @Override
    public boolean replace(K key, @Nullable V oldValue, @Nullable V newValue) {
        Entry<K, V> entry = getEntry(key);
        if ((entry != null) && valuesEquality().areEqual(entry.getValue(), oldValue)) {
            put(entry.getKey(), newValue);
            return true;
        }
        return false;
    }

    @Override
    public V replace(K key, @Nullable V value) {
        Entry<K, V> entry = getEntry(key);
        return (entry != null) ? put(entry.getKey(), value) : null;
    }

    ////////////////////////////////////////////////////////////////////////////
    // SortedMap Interface.
    //

    @Override
    @Realtime(limit = LINEAR, comment="Filtered maps may iterate the whole map")
    public K firstKey() {
        return keySet().first();
    }

    @Override
    @Realtime(limit = LINEAR, comment="Filtered maps may iterate the whole map")
    public K lastKey() {
        return keySet().last();
    }

    ////////////////////////////////////////////////////////////////////////////
    // Misc.
    //

    /** Returns an entry having the specified key (or {@code null} if none).*/
    @Realtime(limit = CONSTANT)
    public abstract @Nullable Entry<K, V> getEntry(K key);

    /** Adds the specified entry to this map (regardless if an entry with the same key is already present).*/
    @Realtime(limit = CONSTANT)
    public abstract @Nullable boolean addEntry(Entry<K, V> entry);

    /** Removes and returns a single entry having the specified key.*/
    @Realtime(limit = LINEAR, comment="Linear removal time for linked maps")
    public abstract @Nullable Entry<K, V> removeEntry(K key);
    
    /** Returns the key order of this map. */
    public abstract Order<? super K> keyOrder();

    /** Returns the value equality of this map. */
    public abstract Equality<? super V> valuesEquality();

    /**
     * Returns an ordered iterator over the entries of this map starting from the specified key.
     * 
     * @param from the starting point (inclusive) or {@code null} to start from the first entry. 
     */
    @Realtime(limit = LINEAR, comment="For shared sets a copy of this set may be performed")
    public abstract FastIterator<Entry<K,V>> iterator(@Nullable K from);
 
    /**
     * Returns an ordered descending iterator over the entries of this map starting from the specified key.
     * 
     * @param from the starting point (inclusive) or {@code null} to start from the last entry. 
     */
    @Realtime(limit = LINEAR, comment="For shared sets a copy of this set may be performed")
    public abstract FastIterator<Entry<K,V>> descendingIterator(@Nullable K from);
         
    /**
     * Returns an iterator over this map, the iteration order is implementation dependent 
     * (e.g. insertion order for linked set). 
     */
    @Realtime(limit = LINEAR, comment="For shared sets a copy of this set may be performed")
    public FastIterator<Entry<K,V>> iterator() {
        return iterator(null);
    }
 
    /**
     * Returns a descending iterator over this map, the iteration order is implementation dependent 
     * (e.g. reversed insertion order for linked set). 
     */
    @Realtime(limit = LINEAR, comment="For shared sets a copy of this set may be performed")
    public FastIterator<Entry<K,V>> descendingIterator() {
        return descendingIterator(null); 
    }
         
    /** Returns a copy of this map; updates of the copy should not impact the original 
     * (the entries are not cloned since immutable).*/
    @Realtime(limit = LINEAR)
    @SuppressWarnings("unchecked")
    public AbstractMap<K, V> clone() {
        try {
            return (AbstractMap<K, V>) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError("Should not happen since this class is Cloneable !");
        }
    }

    /** If the specified object is a map returns {@code entrySet().equals(map.entrySet()). */
    @Override
    @Realtime(limit = LINEAR)
    public boolean equals(@Nullable Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof Map))
            return false;
        Map<?, ?> that = (Map<?, ?>) obj;
        return this.entrySet().equals(that.entrySet());
    }

    /** Returns {@code entrySet().*/
    @Override
    @Realtime(limit = LINEAR)
    public int hashCode() {
        return entrySet().hashCode();
    }

    /** Returns {@link #keyOrder keyOrder()}. */
    @Override
    public final Comparator<? super K> comparator() {
        return keyOrder();
    }

    @Override
    @Realtime(limit = LINEAR)
    public String toString() {
        return entrySet().toString();
    }
     
    /** 
     * A map entry whose value can only be modified through the map. This class can be extended in order to add 
     * additional fields to the entry (e.g. timestamps).
     */
    public static class Entry<K, V> implements Map.Entry<K, V>, Serializable, Immutable {

        private static final long serialVersionUID = 0x700L; // Version.
        private final K key;
        private @Nullable V value;

        /** Creates an entry from the specified key/value pair.*/
        public Entry(K key, @Nullable V value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public final boolean equals(Object obj) { // Default object equality as per Map.Entry contract.
            if (!(obj instanceof Map.Entry))
                return false;
            @SuppressWarnings("unchecked")
            Map.Entry<K, V> that = (Map.Entry<K, V>) obj;
            return Order.STANDARD.areEqual(key, that.getKey()) && Order.STANDARD.areEqual(value, that.getValue());
        }

        @Override
        public final K getKey() {
            return key;
        }

        @Override
        public final @Nullable V getValue() {
            return value;
        }

        @Override
        public final int hashCode() { // Default hash as per Map.Entry contract.
            return Order.STANDARD.indexOf(key) ^ Order.STANDARD.indexOf(value);
        }

        /** 
         * Guaranteed to throw an exception and leave the entry unmodified.
         * @deprecated Modification of an entry value should be performed through the map put(key, value) method.
         */
        @Override
        public final @Nullable V setValue(@Nullable V value) {
            throw new UnsupportedOperationException("Entry modification should be performed through the map");
        }    

        @Override
        public String toString() {
            return "(" + key + '=' + value + ')'; // For debug.
        }
 
        /** 
         * Sets the value of this entry; this method should only be called by internal map implementations using 
         * custom entries.
         */
        protected final @Nullable V setValueUnsafe(@Nullable V newValue) {
            V previousValue = value;
            value = newValue;
            return previousValue;
        }    
    }
    
    /** 
     * A set of map entries.
     */
    public static abstract class Entries<K, V> extends AbstractSet<Entry<K,V>>  {
        
    }
    

}
