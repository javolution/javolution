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

import org.javolution.lang.Constant;
import org.javolution.util.function.Equality;
import org.javolution.util.function.Order;
import org.javolution.util.internal.collection.ReadOnlyIteratorImpl;

/**
 * <p> A map for which immutability is guaranteed by construction.
 * 
 * <pre>{@code
 * // From literal entries (hash-based default order)
 * ConstantMap<String, Integer> ranking = ConstantMap.of("Oscar Thon", 2, "Yvon Tremblay", 5);
 * 
 * // From existing mapping.
 * ConstantMap<String, Integer> winners = ConstantMap.of(LEXICAL, ranking.entrySet().filter(e -> e.getValue() <= 3));
 * }</pre></p>
 *
 * <p> This class ensures that calling a method which may modify the map will generate a warning
 *     (deprecated) at compile time and will raise an exception at run-time.</p>
 *     
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 7.0, March 14, 2016
 */
@Constant
public final class ConstantMap<K,V> extends FastMap<K,V> {
    
    private static final long serialVersionUID = 0x700L; // Version.
    private static final String ERROR_MSG = "Constant maps cannot be modified.";

	/**
     * Returns a constant map (default hash-order) holding the specified key/value pairs. 
     */
	public static <K,V> ConstantMap<K,V> of(K firstKey, V firstValue, Object...others) {
    	return ConstantMap.of(Order.DEFAULT, firstKey, firstValue, others);
    }
	
    /**
     * Returns a constant map sorted according to the specified order and holding the specified key/value pairs. 
     */
    public static <K,V> ConstantMap<K,V> of(Order<? super K> keyOrder, K firstKey, V firstValue, Object...others) {
        return ConstantMap.of(Order.DEFAULT, Equality.DEFAULT, firstKey, firstValue, others);
    }
    
    /**
     * Returns a constant map sorted according to the specified order, using the specified equality for values 
     * comparisons and holding the specified key/value pairs. 
     */
    @SuppressWarnings("unchecked")
    public static <K,V> ConstantMap<K,V> of(Order<? super K> keyOrder, Equality<? super V> valuesEquality, 
            K firstKey, V firstValue, Object...others) {
        SparseMap<K,V> sparse = new SparseMap<K,V>(keyOrder, valuesEquality);
        sparse.put(firstKey, firstValue);
        for (int i=0; i < others.length; i++) 
            sparse.put((K)others[i], (V)others[++i]);
        return new ConstantMap<K,V>(sparse);
    }
    
    /**
     * Returns a constant map (default hash-order) holding the same entries as the specified collection. 
     */
    public static <K,V> ConstantMap<K,V> of(Collection<? extends Entry<? extends K, ? extends V>> entries) {
        return ConstantMap.of(Order.DEFAULT, entries);
    }
    
    /**
     * Returns a constant map sorted according to the specified order and holding the same entries
     * as the specified collection. 
     */
    public static <K,V> ConstantMap<K,V> of(Order<? super K> keyOrder, 
            Collection<? extends Entry<? extends K, ? extends V>> entries) {
        return ConstantMap.of(Order.DEFAULT, Equality.DEFAULT, entries);
    }
    
    /**
     * Returns a constant map sorted according to the specified order, using the specified equality for values 
     * comparisons and holding the same entries as the specified collection. 
     */
    public static <K,V> ConstantMap<K,V> of(Order<? super K> keyOrder, Equality<? super V> valuesEquality, 
            Collection<? extends Entry<? extends K, ? extends V>> entries) {
        SparseMap<K,V> sparse = new SparseMap<K,V>(keyOrder, valuesEquality);
        for (Entry<? extends K, ? extends V> e : entries)
            sparse.put(e.getKey(), e.getValue());
        return new ConstantMap<K,V>(sparse);
    }
    
    
    /** Holds the mapping (sparse). */
    private final SparseMap<K,V> sparse;

    /** Private Constructor.*/
    private ConstantMap(SparseMap<K,V> sparse) {
        this.sparse = sparse;
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
    public V put(K key, V value) {
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

    /** 
     * Guaranteed to throw an exception and leave the map unmodified.
     * @deprecated Should never be used on immutable map.
     */
    @Override
    public void putAll(K key, V value, Object...others) {
        throw new UnsupportedOperationException(ERROR_MSG);
    }

    /** 
     * Guaranteed to throw an exception and leave the map unmodified.
     * @deprecated Should never be used on immutable map.
     */
    @Override
    public void clear() {
        throw new UnsupportedOperationException(ERROR_MSG);
    }

    /** 
     * Guaranteed to throw an exception and leave the map unmodified.
     * @deprecated Should never be used on immutable map.
     */
    @Override
    public V putIfAbsent(K key, V value) {
        throw new UnsupportedOperationException(ERROR_MSG);
    }

    /** 
     * Guaranteed to throw an exception and leave the map unmodified.
     * @deprecated Should never be used on immutable map.
     */
    @Override
    public boolean remove(Object key, Object value) {
        throw new UnsupportedOperationException(ERROR_MSG);
    }

    /** 
     * Guaranteed to throw an exception and leave the map unmodified.
     * @deprecated Should never be used on immutable map.
     */
    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        throw new UnsupportedOperationException(ERROR_MSG);
    }

    /** 
     * Guaranteed to throw an exception and leave the map unmodified.
     * @deprecated Should never be used on immutable map.
     */
    @Override
    public V replace(K key, V value) {
        throw new UnsupportedOperationException(ERROR_MSG);
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

    @Override
    public Iterator<Entry<K,V>> iterator() {
        return ReadOnlyIteratorImpl.of(sparse.iterator());
    }
    
    @Override
    public Iterator<Entry<K,V>> descendingIterator() {
        return ReadOnlyIteratorImpl.of(sparse.descendingIterator());
    }
        
    @Override
    public Iterator<Entry<K,V>> iterator(K fromKey) {
        return ReadOnlyIteratorImpl.of(sparse.iterator(fromKey));
    }
    
    @Override
    public Iterator<Entry<K,V>> descendingIterator(K fromKey) {
        return ReadOnlyIteratorImpl.of(sparse.descendingIterator(fromKey));
    }
    
    /** 
     * Guaranteed to throw an exception and leave the map unmodified.
     * @deprecated Should never be used on immutable map.
     */
    public Entry<K,V> putEntry(K key, V value) {
        throw new UnsupportedOperationException(ERROR_MSG);
    }

    /** 
     * Guaranteed to throw an exception and leave the map unmodified.
     * @deprecated Should never be used on immutable map.
     */
    public Entry<K,V> removeEntry(K key) {
        throw new UnsupportedOperationException(ERROR_MSG);
    }

    @Override
    public Entry<K, V> getEntry(K key) {       
        return sparse.getEntry(key);
    }

    @Override
    public Equality<? super V> valuesEquality() {
        return sparse.valuesEquality();
    }

    @Override
    public FastMap<K, V> clone() {
        return this;
    }

    @Override
    public Order<? super K> comparator() { // Immutable
        return sparse.comparator();
    }

    @Override
    public int size() {
        return sparse.size();
    }

    @Override
    public boolean isEmpty() {
        return sparse.isEmpty();
    }

}