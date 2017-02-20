/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.util;

import java.util.Iterator;

import org.javolution.annotations.Nullable;
import org.javolution.util.function.Equality;
import org.javolution.util.function.Order;
import org.javolution.util.internal.SparseArrayDescendingIteratorImpl;
import org.javolution.util.internal.SparseArrayIteratorImpl;
import org.javolution.util.internal.map.InnerSortedMapImpl;
import org.javolution.util.internal.map.InnerSparseMapImpl;

/**
* <p> A {@link FastMap} implementation based upon high-performance {@link SparseArray}.</p> 
 *     
 * <p> Sparse maps are efficient for indexing multi-dimensional information such as dictionaries, multi-keys attributes,
 *     geographical coordinates, sparse matrix elements, etc.
 * <pre>{@code
 * // Multimap supporting duplicate keys.
 * SparseMap<String, String> presidents = new SparseMap<>(Order.MULTI); 
 * presidents.put("John", "Adams");
 * presidents.put("John", "Tyler");
 * presidents.put("John", "Kennedy");
 * ...
 * presidents.subMap("John").clear(); // Removes all presidents whose first name is "John" ! 
 * presidents.filter(str -> str.startWith("J"); // Map holding presidents whose first name starts with "J".
 * presidents.values().filter(str -> str.length() > 6).parallel().clear(); // Concurrent removal of presidents with long names.
 *     
 * // Sparse Matrix.
 * class RowColumn extends Binary<Index, Index> { ... }
 * SparseMap<RowColumn, E> sparseMatrix = new SparseMap<>(Order.QUADTREE); 
 * sparseMatrix.put(RowColumn.of(2, 44), e);
 * ...
 * }</pre></p>
 * 
 * <p> The memory footprint of the sparse map is automatically adjusted up or down based on the map size 
 *     (minimal when the map is cleared).</p>
 *      
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 7.0, September 13, 2015
 */
public class SparseMap<K,V> extends FastMap<K,V> {
	
	private static final long serialVersionUID = 0x700L; // Version. 
	private final Order<? super K> keyOrder; 
	private int size;
    SparseArray<Object> array; // Holds entries or inner sub-maps when collision.
	
	/**
     * Creates an empty map sorted arbitrarily (hash based).
     */
    public SparseMap() {
    	this(Order.DEFAULT, SparseArray.empty(), 0);
    }
    
	/**
     * Creates an empty map sorted according to the specified order.
     * 
     * @param keyOrder the key order of the map.
     */
    public SparseMap(Order<? super K> keyOrder) {
        this(keyOrder, SparseArray.empty(), 0);
    }        

    /**
     * Creates a sparse map from specified parameters.
     * 
     * @param keyOrder the key order of the map.
     * @param array the sparse array implementation.
     * @param size the map size. 
     */
    protected SparseMap(Order<? super K> keyOrder, SparseArray<Object> array, int size) {
        this.keyOrder = keyOrder;
        this.array = array;
        this.size = size;
    }        

    /** 
     * Returns {@code putEntry(new Entry<K,V>(key, value)).getValue()}. This method may be overridden by 
     * sub-classes to put custom entry types. 
     */
    @Override
    public V put(K key, @Nullable V value) {
        Entry<K,V> previous = putEntry(new Entry<K,V>(key, value));
        return previous != null ? previous.getValue() : null;
    }
        
	@Override
	public int size() {
		return size;
	}
	
	@Override
	public Equality<? super V> valuesEquality() {
		return Equality.DEFAULT;
	}
	
	@Override
	public SparseMap<K, V> clone() {
        SparseMap<K,V> copy = (SparseMap<K,V>) super.clone();
        copy.array = array.clone(); // Also clone inner structures.
        return copy;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Entry<K, V> getEntry(K key) {
	    Object obj = array.get(keyOrder.indexOf(key));
	    if (obj instanceof FastMap) return ((FastMap<K,V>) obj).getEntry(key);
	    Entry<K,V> entry = (Entry<K,V>) obj;
	    return (entry != null) && keyOrder.areEqual(entry.getKey(), key) ? entry : null;
    }		

    @SuppressWarnings("unchecked")
    @Override
    public Entry<K, V> putEntry(Entry<? extends K, ? extends V> entry) {
        K key = entry.getKey();
        int index = keyOrder.indexOf(key);
        Object obj = array.get(index);
        if (obj == null) {
            array = array.set(index, entry);            
        } else if (obj instanceof FastMap) {
            Entry<K, V> previous = ((FastMap<K, V>) obj).putEntry(entry);
            if (previous != null)
                return previous;
        } else { // Entry.
            Entry<K,V> previous = (Entry<K,V>)obj;
            if (keyOrder.areEqual(key, previous.getKey())) { // Replace.
                array = array.set(index, entry);
                return previous;
            } else { // Collision.
                Order<? super K> subOrder = keyOrder.subOrder(key);
                FastMap<K, V> subMap = (subOrder != null) ? new InnerSparseMapImpl<K, V>(subOrder)
                        : new InnerSortedMapImpl<K, V>(keyOrder);
                subMap.putEntry(previous);
                subMap.putEntry(entry);
                array.set(index, subMap);
            }
        }
        size++;
        return null;
    }
    
    @SuppressWarnings("unchecked")
	@Override
	public Entry<K,V> removeEntry(K key) {
        int index = keyOrder.indexOf(key);
        Object obj = array.get(index);
        if (obj instanceof FastMap) {
            FastMap<K,V> subMap = (FastMap<K,V>) obj;
            Entry<K,V> previous = subMap.removeEntry(key);
            if (previous != null) size--;
            if (subMap.size() == 1) array.set(index, subMap.firstEntry()); // No sub-map with single entries allowed.
            return previous;
        } 
        Entry<K,V> previous = (Entry<K,V>) obj;
        if ((previous != null) && keyOrder.areEqual(previous.getKey(), key)) { // Found it.
            array = array.set(index, null);
            size--;
            return previous;
        } else {
            return null;
        }
	}
	
	@Override
	public void clear() {
	    array = SparseArray.empty();
		size = 0;
	}

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public Iterator<Entry<K, V>> iterator() {
        return new SparseArrayIteratorImpl<K, Entry<K,V>>(array) {
            @Override
            public void notifyRemoval(SparseArray<Object> newArray) {
                if (newArray != null) array = newArray;
                size--;                
            }};
    }

    @Override
    public Iterator<Entry<K, V>> descendingIterator() {
        return new SparseArrayDescendingIteratorImpl<K, Entry<K,V>>(array) {
            @Override
            public void notifyRemoval(SparseArray<Object> newArray) {
                if (newArray != null) array = newArray;
                size--;                
            }};
    }

    @Override
    public Iterator<Entry<K, V>> iterator(K fromKey) {        
        return new SparseArrayIteratorImpl<K, Entry<K,V>>(array, fromKey, keyOrder, true) {
            @Override
            public void notifyRemoval(SparseArray<Object> newArray) {
                if (newArray != null) array = newArray;
                size--;                
            }};
    }

    @Override
    public Iterator<Entry<K, V>> descendingIterator(K fromKey) {
        return new SparseArrayDescendingIteratorImpl<K, Entry<K,V>>(array, fromKey, keyOrder, true) {
            @Override
            public void notifyRemoval(SparseArray<Object> newArray) {
                if (newArray != null) array = newArray;
                size--;                
            }};
    }

    @Override
    public Order<? super K> keyOrder() {
        return keyOrder;
    }

   
}