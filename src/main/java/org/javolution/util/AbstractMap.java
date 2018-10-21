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
import org.javolution.util.function.Equality;
import org.javolution.util.function.Order;
import org.javolution.util.function.UnaryOperator;
import org.javolution.util.internal.map.AtomicMapImpl;
import org.javolution.util.internal.map.KeySetImpl;
import org.javolution.util.internal.map.LinkedMapImpl;
import org.javolution.util.internal.map.MultiMapImpl;
import org.javolution.util.internal.map.SharedMapImpl;
import org.javolution.util.internal.map.SubMapImpl;
import org.javolution.util.internal.map.UnmodifiableMapImpl;
import org.javolution.util.internal.map.ValuesImpl;

/**
 * High-performance ordered map / multimap with {@link Realtime strict timing constraints}.
 * 
 * A multimap is a generalization of a map or associative array abstract data type in which more than one value may 
 * be associated with and returned for a given key.
 * 
 * From a semantic standpoint, {@link AbstractMap} is considered equivalent to an {@link AbstractSet} 
 * of not directly modifiable {@link Entry} elements (the set order is based upon the entries keys order).
 * Accessing/adding/removing custom entries is allowed through the methods: 
 * {@link  #getEntry}, {@link  #addEntry} and {@link #removeEntry} (or {@code entries().remove(entry)} to 
 * remove a specific mapping).
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

    /** 
     * Returns this map entries. The set supports element removal, which removes the corresponding mapping 
     * from the map (if the exact mapping exists). 
     */
    @Realtime(limit = CONSTANT)
    public abstract AbstractSet<Entry<K, V>> entries();
     
    ////////////////////////////////////////////////////////////////////////////
    // Views.
    //    

    @SuppressWarnings("unchecked")
    @Override
    @Realtime(limit = CONSTANT)
    public final AbstractSet<Map.Entry<K, V>> entrySet() {
        return (AbstractSet<Map.Entry<K, V>>) (Object) entries();
    }
     
    /** 
     * Returns a view for which the {@link #put} method allowing multiple entries having the same key to be put into this map (multimap).
     */
    @Realtime(limit = CONSTANT)
    public AbstractMap<K,V> multi() {
        return new MultiMapImpl<K,V>(this); 
    }
     
    /** 
     * Returns a view of the portion of this map whose elements range from {@code fromElement} to {@code toElement}.
     */
    @Realtime(limit = CONSTANT)
    public AbstractMap<K,V> subMap(@Nullable K fromKey, boolean fromInclusive, @Nullable K toKey, boolean toInclusive) {
        return new SubMapImpl<K,V>(this, fromKey, fromInclusive, toKey, toInclusive);
    }
     
    /**
     * Returns an atomic view over this map. All operations that write or access multiple elements in the map 
     * are atomic. All read operations are mutex-free.
     */
    @Realtime(limit = CONSTANT)
    public AbstractMap<K, V> atomic() {
        return new AtomicMapImpl<K, V>(this);
    }

    /**
     * Returns a thread-safe view over this map. The shared view allows for concurrent read as long as 
     * there is no writer. The default implementation is based on <a href=
     * "http://en.wikipedia.org/wiki/Readers%E2%80%93writer_lock"> readers-writers locks</a> giving priority to writers. 
     */
    @Realtime(limit = CONSTANT)
    public AbstractMap<K, V> shared() {
        return new SharedMapImpl<K,V>(this);
    }

    /**
     * Returns a view keeping track of the insertion order and exposing entries/keys/values in that order 
     * (first added, first to iterate). This view can be useful for compatibility with Java linked collections
     * (e.g. {@code LinkedHashMap}). Elements not {@link #put added} through this view are ignored when iterating.
     */
    @Realtime(limit = CONSTANT)
    public AbstractMap<K, V> linked() {
        return new LinkedMapImpl<K, V>(this);
    }

    /**
     * Returns an unmodifiable view over this map. Attempts to modify the map directly through this view will 
     * result into a {@link java.lang.UnsupportedOperationException} being raised.
     * It should be noted, that by default map entries are not directly modifiable (setValue() throws an exception).
     */
    @Realtime(limit = CONSTANT)
    public @ReadOnly AbstractMap<K, V> unmodifiable() {
        return new UnmodifiableMapImpl<K,V>(this);
    }

    @Override
    @Realtime(limit = CONSTANT)
    public AbstractSet<K> keySet() {
        return new KeySetImpl<K, V>(this);
    }

    @Override
    @Realtime(limit = CONSTANT)
    public AbstractCollection<V> values() {
        return new ValuesImpl<K,V>(entries(), valuesEquality());
    }

    @Override
    @Realtime(limit = CONSTANT)
    public AbstractMap<K, V> subMap(K fromKey, K toKey) {
        return subMap(fromKey, true, toKey, false);
    }

    @Override
    @Realtime(limit = CONSTANT)
    public AbstractMap<K, V> headMap(K toKey) {
        return subMap(null, false, toKey, false);
    }

    @Override
    @Realtime(limit = CONSTANT)
    public AbstractMap<K, V> tailMap(K fromKey) {
        return subMap(fromKey, true, null, false);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Map Interface.
    //

    @Override
    @Realtime(limit = LINEAR, comment = "May count the number of entries (e.g. filtered views)")
    public int size() {
        return entrySet().size();
    }

    @Override
    @Realtime(limit = LINEAR, comment = "Could iterate the whole map (e.g. filtered views).")
    public boolean isEmpty() {
        return entrySet().isEmpty();
    }

    @SuppressWarnings("unchecked")
    @Override
    @Realtime(limit = CONSTANT)
    public boolean containsKey(Object key) {
        return getEntry((K) key) != null; // Cast has no effect here.
    }

    @Override
    @Realtime(limit = LINEAR)
    public boolean containsValue(@Nullable Object value) {
        return values().contains(value);
    }

    @Override
    @Realtime(limit = CONSTANT)
    public @Nullable V get(Object key) {
        @SuppressWarnings("unchecked")
        Entry<K, V> entry = getEntry((K) key); // Cast has no effect here.
        return entry != null ? entry.getValue() : null;
    }

    /** 
     * Returns the value at the specified key or the specified default if that value is {@code null} 
     * (convenience method). 
     */
    @Realtime(limit = CONSTANT)
    public V get(Object key, V defaultIfNull) {
        V value = get(key);
        return value != null ? value : defaultIfNull;
    }

    @Override
    @Realtime(limit = CONSTANT)
    public @Nullable V remove(Object key) {
        @SuppressWarnings("unchecked")
        Entry<K, V> entry = removeEntry((K) key); // Cast has no effect here.
        return entry != null ? entry.getValue() : null;
    }

    @Override
    @Realtime(limit = CONSTANT)
    public @Nullable V put(K key, @Nullable V value) {
        Entry<K, V> entry = getEntry(key);
        if (entry != null) return updateValue(entry, value);
        addEntry(key, value);
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
    public void clear() {
        entrySet().clear();
    }

    ////////////////////////////////////////////////////////////////////////////
    // ConcurrentMap Interface.
    //

    @Override
    @Realtime(limit = CONSTANT)
    public V putIfAbsent(K key, @Nullable V value) {
        Entry<K, V> entry = getEntry(key);
        if (entry != null) return entry.getValue();
        addEntry(key, value);
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    @Realtime(limit = CONSTANT)
    public boolean remove(Object key, @Nullable Object value) {
        Entry<K, V> entry = getEntry((K)key);
        if ((entry != null) && valuesEquality().areEqual(entry.getValue(), (V) value)) return entrySet().remove(entry);
        return false;
    }

    @Override
    @Realtime(limit = CONSTANT)
    public boolean replace(K key, @Nullable V oldValue, @Nullable V newValue) {
        Entry<K, V> entry = getEntry((K)key);
        if ((entry != null) && valuesEquality().areEqual(entry.getValue(), (V) oldValue)) {
            updateValue(entry, newValue);
            return true;
        }
        return false;
    }

    @Override
    @Realtime(limit = CONSTANT)
    public V replace(K key, @Nullable V value) {
        Entry<K, V> entry = getEntry(key);
        return (entry != null) ? updateValue(entry, value) : null;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // SortedMap Interface.
    //

    @Override
    @Realtime(limit = LINEAR, comment="Filtered maps may iterate the whole map")
    public K firstKey() {
        return entrySet().first().getKey();
    }

    @Override
    @Realtime(limit = LINEAR, comment="Filtered maps may iterate the whole map")
    public K lastKey() {
        return entrySet().last().getKey();
    }

    ////////////////////////////////////////////////////////////////////////////
    // Misc.
    //

    
    /** Returns an entry having the specified key (or {@code null} if none).*/
    @Realtime(limit = CONSTANT)
    public abstract @Nullable Entry<K, V> getEntry(K key);

    /** Adds and return the specified entry to this map regardless if an entry with the same key already exists 
     * (faster than using {@link #put} as it does not check for key presence)..*/
    @Realtime(limit = LINEAR, comment="Linear removal time for linked maps")
    public abstract @Nullable Entry<K, V> addEntry(K key, V value);
    
    /** Removes and returns a single entry having the specified key.*/
    @Realtime(limit = LINEAR, comment="Linear removal time for linked maps")
    public abstract @Nullable Entry<K, V> removeEntry(K key);
    
    /** Returns the key order of this map. */
    public abstract Order<? super K> keyOrder();

    /** Returns the value equality of this map. */
    public abstract Equality<? super V> valuesEquality();

    /** 
     * Updates the value of the specified key using the specified operator.
     *  
     * @param key the key of the entry to update.
     * @param update the function returning the new value from the previous value (which can be {@code null}). 
     * @return the previous value.
     */
    @Realtime(limit = CONSTANT)
    public @Nullable V put(K key, UnaryOperator<V> update) {
        Entry<K, V> entry = getEntry(key);
        if (entry != null) return updateValue(entry, update.apply(entry.getValue()));
        put(key, update.apply(null)); 
        return null;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    @Realtime(limit = LINEAR)
    public AbstractMap<K,V> clone() {
        try {
            return (AbstractMap<K,V>) super.clone();
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
    public Comparator<? super K> comparator() {
        return keyOrder();
    }

    @Override
    @Realtime(limit = LINEAR)
    public String toString() {
        return entrySet().toString();
    }
     
    /** Updates the the value for the specified entry and returns the previous value; 
     *  this method should be overridden by unmodifiable maps to throw {@link UnsupportedOperationException}. */
    protected V updateValue(Entry<K,V> entry, V newValue) {
        V previousValue = entry.value;
        entry.value = newValue;
        return previousValue;
    }

    /**
     * Entry whose value can only be modified through a map instance.
     */
    public static class Entry<K, V> implements Map.Entry<K, V>, Serializable {
     
        private static final long serialVersionUID = 0x700L; // Version.
        private final K key;
        private @Nullable V value;
        

        /** Creates an entry from the specified key/value pair.*/
        public Entry(K key, @Nullable V value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public boolean equals(Object obj) { // Default object equality as per Map.Entry contract.
            if (!(obj instanceof Map.Entry))
                return false;
            @SuppressWarnings("unchecked")
            Map.Entry<K, V> that = (Map.Entry<K, V>) obj;
            return Order.standard().areEqual(key, that.getKey()) && Order.standard().areEqual(value, that.getValue());
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
        public int hashCode() { // Default hash as per Map.Entry contract.
            return (int) (Order.standard().indexOf(key) ^ Order.standard().indexOf(value));
        }

        /** 
         * Guaranteed to throw an exception and leave the entry unmodified.
         * @deprecated Modification of an entry value should be performed through the map.
         */
        @Override
        public @Nullable V setValue(@Nullable V value) {
            throw new UnsupportedOperationException("Entry modification should be performed through the map");
        }    

        @Override
        public String toString() {
            return "(" + key + '=' + value + ')'; // For debug.
        }
    }
}
