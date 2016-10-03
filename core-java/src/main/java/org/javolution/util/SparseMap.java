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

import org.javolution.util.function.Equality;
import org.javolution.util.function.Order;
import org.javolution.util.internal.map.TrieNodeImpl;
import org.javolution.util.internal.map.TrieNodeImpl.EntryNode;
import org.javolution.util.internal.map.UnorderedMapImpl;

/**
 * <p> The default <a href="http://en.wikipedia.org/wiki/Trie">trie-based</a> implementation of {@link FastMap}.</p> 
 *     
 * <p> The trie-based structure allows for extremely fast (constant time) access/insertion/deletion.</p>
 * 
 * <p> The memory footprint of the array is automatically adjusted up or down in constant time 
 *     (minimal when the map is cleared).</p>
 * 
 * <p> Sparse maps are efficient for indexing multi-dimensional information such as dictionaries, multi-keys attributes, geographical coordinates,
 *     sparse matrix elements, etc.
 * <pre>{@code
 * // Prefix Maps.
 * SparseMap<String, President> presidents = new SparseMap<>(Order.LEXICAL);
 * presidents.put("John Adams", johnAdams);
 * presidents.put("John Tyler", johnTyler);
 * presidents.put("John Kennedy", johnKennedy);
 * ...
 * presidents.subMap("J", "K").clear(); // Removes all president whose first name starts with "J" ! 
 * presidents.filter(str -> str.startWith("John "); // Map holding presidents with "John" as first name.
 * presidents.values().filter(p -> p.birth < 1900).parallel().clear(); // Concurrent removal of presidents born before 1900.
 *     
 * // Sparse Array.
 * SparseArray<Index, E> sparseArray = new SparseArray(Order.INDEX);
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
	private TrieNodeImpl<Object,Object> root = TrieNodeImpl.empty(); // Hold either Entry<K,V> or Entry<SUB_MAP, FastMap> 
	private int size;
	
	/**
     * Creates an empty map sorted arbitrarily (hash based).
     */
    public SparseMap() {
    	this(Order.DEFAULT);
    }
    
	/**
     * Creates an empty map sorted according to the specified order.
     * 
     * @param keyOrder the key order of the map.
     */
    public SparseMap(Order<? super K> keyOrder) {
        this.keyOrder = keyOrder;
    }        
        
	@Override
	public int size() {
		return size;
	}
	
	@Override
	public Order<? super K> comparator() {
		return keyOrder;
	}
	
	@Override
	public Equality<? super V> valuesEquality() {
		return Equality.DEFAULT;
	}
	
	@Override
	public SparseMap<K, V> clone() {
		SparseMap<K,V> copy = new SparseMap<K,V>(keyOrder);
		copy.root = root.clone();
		copy.size = size;
		return copy;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Entry<K, V> getEntry(K key) {
		EntryNode<?,?> entry = root.getEntry(keyOrder.indexOf(key));
        if (entry == null) return null;
        if (entry.getKey() == TrieNodeImpl.SUB_MAP)    
             return ((FastMap<K,V>)entry.getValue()).getEntry(key); 
        return keyOrder.areEqual((K)entry.getKey(), key) ? (Entry<K, V>)entry : null;
    }		

	@SuppressWarnings("unchecked")
	@Override
	public V put(K key, V value) {
		int i = keyOrder.indexOf(key);
		EntryNode<Object, Object> entry = root.entry(i);
		if (entry.getKey() == TrieNodeImpl.SUB_MAP) {
			FastMap<K,V> subMap = (FastMap<K,V>)entry.getValue();
			int previousSize = subMap.size();
			V previousValue = subMap.put(key, value);
			if (subMap.size() > previousSize) size++;
			return previousValue;			
		}
		if (entry == TrieNodeImpl.UPSIZE) { // Resizes.
			root = root.upsize(i);
			entry = root.entry(i);
		}		
		if (entry.getKey() == EntryNode.NOT_INITIALIZED) { // New entry.
			entry.init(key,value);
			size++;
			return null;
		} 
		// Existing entry.
		if (keyOrder.areEqual((K)entry.getKey(), key))
			return (V) entry.setValue(value);
		// Collision.
        Order<? super K> subOrder = keyOrder.subOrder(key);
        FastMap<K,V> subMap = (subOrder != null) ? 
		         new SparseMap<K,V>(subOrder) : 
		        	 new UnorderedMapImpl<K,V>(keyOrder);
	    subMap.put((K)entry.getKey(), (V)entry.getValue());
	    entry.init(TrieNodeImpl.SUB_MAP, subMap);
	    size++;
	    return subMap.put(key, value);
    }
	
	@SuppressWarnings("unchecked")
	@Override
	public Entry<K,V> removeEntry(K key) {
		int i = keyOrder.indexOf((K)key);
		EntryNode<Object,Object> entry = root.getEntry(i);
        if (entry == null) return null;
        if (entry.getKey() == TrieNodeImpl.SUB_MAP) {
        	Entry<K,V> previousEntry = ((FastMap<K,V>)entry.getValue()).removeEntry(key);
        	if (previousEntry != null) size--;
        	return previousEntry;
        }
        if (!keyOrder.areEqual((K)entry.getKey(), key)) return null;
        Object tmp = root.removeEntry(i);
        if (tmp == TrieNodeImpl.DOWNSIZE) {
        	root = root.downsize(i);
        } 
        size--;
        return (Entry<K, V>) entry;
	}
	
	@Override
	public void clear() {
		root = TrieNodeImpl.empty();
		size = 0;
	}

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public Iterator<Entry<K, V>> iterator() {
        return new TrieNodeImpl.NodeIterator<K,V>(this, root);
    }

    @Override
    public Iterator<Entry<K, V>> descendingIterator() {
        return new TrieNodeImpl.DescendingNodeIterator<K,V>(this, root);
    }

    @Override
    public Iterator<Entry<K, V>> iterator(K fromKey) {
        return new TrieNodeImpl.NodeIterator<K,V>(this, root, fromKey, keyOrder.indexOf(fromKey));
    }

    @Override
    public Iterator<Entry<K, V>> descendingIterator(K fromKey) {
        return new TrieNodeImpl.DescendingNodeIterator<K,V>(this, root, fromKey, keyOrder.indexOf(fromKey));
    }
    
   
}