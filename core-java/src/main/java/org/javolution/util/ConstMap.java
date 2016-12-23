/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2014 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.javolution.annotations.Nullable;
import org.javolution.annotations.ReadOnly;
import org.javolution.lang.Immutable;
import org.javolution.util.function.Equality;
import org.javolution.util.function.Order;
import org.javolution.util.internal.SparseArrayDescendingIteratorImpl;
import org.javolution.util.internal.SparseArrayIteratorImpl;

/**
 * <p> A map for which immutability is guaranteed by construction.
 * 
 * <pre>{@code
 * // Creation from literal entries (hash-based default order)
 * ConstMap<String, Integer> ranking = ConstMap.of("Oscar Thon", 2, "Yvon Tremblay", 5);
 * ConstMap<String, String> multimap = ConstMap.of(MULTI, "John", "Adams", "John", "Kennedy");  
 * 
 * // Creation from existing mapping.
 * ConstMap<String, Integer> winners = ConstMap.of(LEXICAL, ranking.entrySet().filter(e -> e.getValue() <= 3));
 * }</pre></p>
 *
 * <p> This class ensures that calling a method which may modify the map will generate a deprecated warning
 *     at compile time and will raise an exception at run-time.</p>
 *     
 * @param <K> the immutable keys
 * @param <V> the immutable values 
 * 
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 7.0, March 14, 2016
 */
@ReadOnly
public final class ConstMap<K, V> extends FastMap<K, V> implements Immutable {

    private static final long serialVersionUID = 0x700L; // Version.
    private static final String ERROR_MSG = "Constant maps cannot be modified.";
    private static final ConstMap<?,?> EMPTY = new ConstMap<Object, Object>(Order.DEFAULT);

    /**
     * Returns a constant empty set. 
     */
    @SuppressWarnings("unchecked")
    public static <K,V> ConstMap<K,V> empty() {
        return (ConstMap<K,V>) EMPTY;
    }
    
   /**
     * Returns a constant map (default hash-order) holding the same entries as the specified collection. 
     */
    public static <K, V> ConstMap<K, V> of(Collection<? extends Entry<? extends K, ? extends V>> entries) {
        return new ConstMap<K, V>(Order.DEFAULT, entries);
    }
    
    /**
     * Returns a constant map (default hash-order) holding the specified key/value pairs. 
     */
    public static <K, V> ConstMap<K, V> of(Object... keyValuePairs) {
        return new ConstMap<K, V>(Order.DEFAULT, keyValuePairs);
    }
    /**
     * Returns a constant map sorted according to the specified order and holding the same entries
     * as the specified collection. 
     */
    public static <K, V> ConstMap<K, V> of(Order<? super K> keyOrder,
            Collection<? extends Entry<? extends K, ? extends V>> entries) {
        return new ConstMap<K, V>(keyOrder, entries);
    }

    /**
     * Returns a constant map sorted according to the specified order and holding the specified key/value pairs. 
     */
    public static <K, V> ConstMap<K, V> of(Order<? super K> keyOrder, Object... keyValuePairs) {
        return new ConstMap<K, V>(keyOrder, keyValuePairs);
    }

    private final Order<? super K> keyOrder;

    private final SparseArray<Object> array;

    private final int size;

    /** Creates a constant map from the specified entries.*/
    private ConstMap(Order<? super K> keyOrder, Collection<? extends Entry<? extends K, ? extends V>> entries) {
        SparseMap<K, V> sparse = new SparseMap<K, V>(keyOrder);
        for (Entry<? extends K, ? extends V> e : entries)
            sparse.putEntry(new Entry<K, V>(e.getKey(), e.getValue()));
        this.keyOrder = keyOrder;
        this.array = sparse.array;
        this.size = sparse.size();
    }

    /** Creates a constant map holding the specified key/value pairs.*/
    @SuppressWarnings("unchecked")
    private ConstMap(Order<? super K> keyOrder, Object... keyValuePairs) {
        SparseMap<K, V> sparse = new SparseMap<K, V>(keyOrder);
        for (int i = 0; i < keyValuePairs.length;)
            sparse.putEntry(new Entry<K, V>((K) keyValuePairs[i++], (V) keyValuePairs[i++]));
        this.keyOrder = keyOrder;
        this.array = sparse.array;
        this.size = sparse.size();
    }

    /** 
     * Guaranteed to throw an exception and leave the map unmodified.
     * @deprecated Should never be used on immutable map.
     */
    @Override
    public void clear() {
        throw new UnsupportedOperationException(ERROR_MSG);
    }

    @Override
    public ConstMap<K, V> clone() {
        return this;
    }

    @Override
    public Iterator<Entry<K, V>> descendingIterator() {
        return new SparseArrayDescendingIteratorImpl<K, Entry<K, V>>(array) {
            @Override
            public void remove() {
                throw new UnsupportedOperationException(ERROR_MSG);
            }
        };
    }

    @Override
    public Iterator<Entry<K, V>> descendingIterator(K fromKey) {
        return new SparseArrayDescendingIteratorImpl<K, Entry<K, V>>(array, fromKey, keyOrder, true) {
            @Override
            public void remove() {
                throw new UnsupportedOperationException(ERROR_MSG);
            }
        };
    }

    @SuppressWarnings("unchecked")
    @Override
    public Entry<K, V> getEntry(K key) {
        Object obj = array.get(keyOrder.indexOf(key));
        if (obj instanceof FastMap)
            return (Entry<K, V>) ((FastMap<K, V>) obj).getEntry(key);
        Entry<K, V> entry = (Entry<K, V>) obj;
        return (entry != null) && keyOrder.areEqual(entry.getKey(), key) ? entry : null;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public Iterator<Entry<K, V>> iterator() {
        return new SparseArrayIteratorImpl<K, Entry<K, V>>(array) {
            @Override
            public void remove() {
                throw new UnsupportedOperationException(ERROR_MSG);
            }
        };
    }

    @Override
    public Iterator<Entry<K, V>> iterator(K fromKey) {
        return new SparseArrayIteratorImpl<K, Entry<K, V>>(array, fromKey, keyOrder, true) {
            @Override
            public void remove() {
                throw new UnsupportedOperationException(ERROR_MSG);
            }
        };
    }

    @Override
    public Order<? super K> keyOrder() {
        return keyOrder;
    }

    /** 
     * Guaranteed to throw an exception and leave the map unmodified.
     * @deprecated Should never be used on immutable map.
     */
    @Override
    public Entry<K, V> pollFirstEntry() {
        throw new UnsupportedOperationException(ERROR_MSG);
    }

    /** 
     * Guaranteed to throw an exception and leave the map unmodified.
     * @deprecated Should never be used on immutable map.
     */
    @Override
    public Entry<K, V> pollLastEntry() {
        throw new UnsupportedOperationException(ERROR_MSG);
    }

    /** 
     * Guaranteed to throw an exception and leave the map unmodified.
     * @deprecated Should never be used on immutable map.
     */
    @Override
    public V put(K key, @Nullable V value) {
        throw new UnsupportedOperationException(ERROR_MSG);
    }

    /** 
     * Guaranteed to throw an exception and leave the map unmodified.
     * @deprecated Should never be used on immutable map.
     */
    @Override
    public void putAll(Object... keyValuePairs) {
        throw new UnsupportedOperationException(ERROR_MSG);
    }

    /** 
     * Guaranteed to throw an exception and leave the map unmodified.
     * @deprecated Should never be used on immutable map.
     */
    @Override
    public void putAll(Map<? extends K, ? extends V> that) {
        throw new UnsupportedOperationException(ERROR_MSG);
    }

    @Override
    public Entry<K, V> putEntry(Entry<? extends K, ? extends V> entry) {
        throw new UnsupportedOperationException(ERROR_MSG);
    }

    /** 
     * Guaranteed to throw an exception and leave the map unmodified.
     * @deprecated Should never be used on immutable map.
     */
    @Override
    public V putIfAbsent(K key, @Nullable V value) {
        throw new UnsupportedOperationException(ERROR_MSG);
    }

    /** 
     * Guaranteed to throw an exception and leave the map unmodified.
     * @deprecated Should never be used on immutable map.
     */
    @Override
    public V remove(Object key) {
        throw new UnsupportedOperationException(ERROR_MSG);
    }

    /** 
     * Guaranteed to throw an exception and leave the map unmodified.
     * @deprecated Should never be used on immutable map.
     */
    @Override
    public boolean remove(Object key, @Nullable Object value) {
        throw new UnsupportedOperationException(ERROR_MSG);
    }

    /** 
     * Guaranteed to throw an exception and leave the map unmodified.
     * @deprecated Should never be used on immutable map.
     */
    @Override
    public Entry<K, V> removeEntry(K key) {
        throw new UnsupportedOperationException(ERROR_MSG);
    }

    /** 
     * Guaranteed to throw an exception and leave the map unmodified.
     * @deprecated Should never be used on immutable map.
     */
    @Override
    public V replace(K key, @Nullable V value) {
        throw new UnsupportedOperationException(ERROR_MSG);
    }

    /** 
     * Guaranteed to throw an exception and leave the map unmodified.
     * @deprecated Should never be used on immutable map.
     */
    @Override
    public boolean replace(K key, @Nullable V oldValue, @Nullable V newValue) {
        throw new UnsupportedOperationException(ERROR_MSG);
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public Equality<? super V> valuesEquality() {
        return Equality.DEFAULT;
    }

}