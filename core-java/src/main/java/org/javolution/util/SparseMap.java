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

import org.javolution.util.SparseArray.EntryNode;
import org.javolution.util.SparseArray.Node;
import org.javolution.util.SparseArray.NullNode;
import org.javolution.util.function.Equality;
import org.javolution.util.function.Order;
import org.javolution.util.internal.map.SortedMapImpl;

/**
 * <p> The default <a href="http://en.wikipedia.org/wiki/Trie">trie-based</a> 
 *     implementation of {@link FastMap}.</p> 
 *     
 * <p> Worst-case execution time when adding new entries is significantly 
 *     better than when using standard hash table since there is
 *     no resize/rehash ever performed.</p> 
 *   
 * <p> Sparse maps are efficient for indexing multi-dimensional information 
 *     such as dictionaries, multi-keys attributes, geographical coordinates,
 *     sparse matrix elements, etc.
 * <pre>{@code
 * // Prefix Maps.
 * SparseMap<String, President> presidents = new SparseMap<>(Order.LEXICAL);
 * presidents.put("John Adams", johnAdams);
 * presidents.put("John Tyler", johnTyler);
 * presidents.put("John Kennedy", johnKennedy);
 * ...
 * presidents.subMap("J", "K").clear(); // Removes all president whose first name starts with "J" ! 
 *     
 * // Sparse Matrix.
 * class RowColumn extends Binary<Index, Index> { ... }
 * SparseMap<RowColumn, E> sparseMatrix = new SparseMap<>(Order.QUADTREE); 
 * sparseMatrix.put(RowColumn.of(2, 44), e);
 * ...
 * }</pre></p>
 * 
 * <p> The memory footprint of the sparse map is automatically adjusted up or 
 *     down based on the map size (minimal when the map is cleared).</p>
 *      
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 7.0, September 13, 2015
 * @see SparseArray
 */
public class SparseMap<K,V> extends FastMap<K,V> {
	
	private static final long serialVersionUID = 0x700L; // Version. 
	private static final Object SUB_MAP = new Object();
	private final Order<? super K> keyOrder; 
    private final Equality<? super V> valueEquality; 
	private Node<K,V> root = NullNode.getInstance(); // Map's values are either instances of V or FastMap<K,V>
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
    public SparseMap(Order<? super K> order) {
    	this(Order.DEFAULT, Equality.DEFAULT);
    }
    
    /**
     * Creates an empty map sorted according to the specified key order and using the specified 
     * equality for values comparisons.
     * 
     * @param keyOrder the key order of the map.
     */
    public SparseMap(Order<? super K> keyOrder, Equality<? super V> valueEquality) {
        this.keyOrder = keyOrder;
        this.valueEquality = valueEquality; 
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
		return valueEquality;
	}
	
	@Override
	public SparseMap<K, V> clone() {
		SparseMap<K,V> copy = new SparseMap<K,V>(keyOrder, valueEquality);
		copy.root = root != null ? root.clone() : null;
		copy.size = size;
		return copy;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Entry<K, V> getEntry(K key) {
		EntryNode<K,V> entry = root.getEntry(keyOrder.indexOf(key));
        if (entry == null) return null;
        if (entry.key == SUB_MAP)    
             return ((FastMap<K,V>)entry.value).getEntry(key); 
        return keyOrder.areEqual(entry.key, key) ? entry : null;
    }		

	@SuppressWarnings("unchecked")
	@Override
	public V put(K key, V value) {
		int i = keyOrder.indexOf(key);
		EntryNode<K,V> entry = root.entry(i);
		if (entry.key == SUB_MAP) {
			FastMap<K,V> subMap = (FastMap<K,V>)entry.value;
			int previousSize = subMap.size();
			V previousValue = subMap.put(key, value);
			if (subMap.size() > previousSize) size++;
			return previousValue;			
		}
		if (entry == SparseArray.UPSIZE) { // Resizes.
			root = root.upsize(i);
			entry = root.entry(i);
		}		
		if (entry.key == EntryNode.NOT_INITIALIZED) { // New entry.
			entry.key = key;
			entry.value = value;
			size++;
			return null;
		} 
		// Existing entry.
		if (keyOrder.areEqual(entry.key, key))
			return entry.setValueBypass(value);
		// Collision.
        Order<? super K> subOrder = keyOrder.subOrder(key);
        FastMap<K,V> subMap = (subOrder != null) ? 
		         new SparseMap<K,V>(subOrder) : 
		        	 new SortedMapImpl<K,V>(keyOrder);
	    subMap.put(entry.key, entry.value);
	    entry.key = (K) SUB_MAP; // Cast has no effect.
	    entry.value = (V) subMap; // Cast has no effect.
	    size++;
	    return subMap.put(key, value);
    }
	
	@SuppressWarnings("unchecked")
	@Override
	public Entry<K,V> removeEntry(K key) {
		int i = keyOrder.indexOf((K)key);
		EntryNode<K,V> entry = root.getEntry(i);
        if (entry == null) return null;
        if (entry.key == SUB_MAP) {
        	Entry<K,V> previousEntry = ((FastMap<K,V>)entry.value).removeEntry(key);
        	if (previousEntry != null) size--;
        	return previousEntry;
        }
        if (!keyOrder.areEqual(entry.getKey(), key)) return null;
        Object tmp = root.removeEntry(i);
        if (tmp == SparseArray.DOWNSIZE) {
        	root = root.downsize(i);
        } else if (tmp == SparseArray.DELETE) {
        	root = NullNode.getInstance();
        }
        size--;
        return (Entry<K, V>) entry;
	}
	
	@Override
	public void clear() {
		root = NullNode.getInstance();
		size = 0;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Entry<K, V> firstEntry() {
		EntryNode<K,V> entry = root.ceilingEntry(0);
		if ((entry != null) && (entry.key == SUB_MAP))
			return ((FastMap<K,V>)entry.value).firstEntry();
		return entry;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Entry<K, V> lastEntry() {
		EntryNode<K,V> entry = root.floorEntry(-1);
		if ((entry != null) && (entry.key == SUB_MAP))
			return ((FastMap<K,V>)entry.value).lastEntry();
		return entry;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Entry<K, V> higherEntry(K key) {
		int i = keyOrder.indexOf(key);
		EntryNode<K,V> entry = root.ceilingEntry(i);
		if (entry == null) return null;
		if (entry.key == SUB_MAP) {
			Entry<K,V> subMapEntry = ((FastMap<K,V>)entry.value).higherEntry(key);
			if (subMapEntry != null) return subMapEntry;
		} else {
			if (keyOrder.compare(entry.key, key) > 0) return entry;
		}
		if (entry.index == -1) return null;
		entry = root.ceilingEntry(i+1);
		if ((entry != null) && (entry.key == SUB_MAP)) 
			return ((FastMap<K,V>)entry.value).firstEntry();
		return entry;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Entry<K, V> lowerEntry(K key) {
		int i = keyOrder.indexOf(key);
		EntryNode<K,V> entry = root.floorEntry(i);
		if (entry == null) return null;
		if (entry.key == SUB_MAP) {
			Entry<K,V> subMapEntry = ((FastMap<K,V>)entry.value).lowerEntry(key);
			if (subMapEntry != null) return subMapEntry;
		} else {
			if (keyOrder.compare(entry.key, key) < 0) return entry;
		}
		if (entry.index == 0) return null;
		entry = root.floorEntry(i-1);
		if ((entry != null) && (entry.key == SUB_MAP)) 
			return ((FastMap<K,V>)entry.value).lastEntry();
		return entry;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Entry<K, V> ceilingEntry(K key) {
		int i = keyOrder.indexOf(key);
		EntryNode<K,V> entry = root.ceilingEntry(i);
		if (entry == null) return null;
		if (entry.key == SUB_MAP) {
			Entry<K,V> subMapEntry = ((FastMap<K,V>)entry.value).ceilingEntry(key);
			if (subMapEntry != null) return subMapEntry;
		} else {
			if (keyOrder.compare(entry.key, key) >= 0) return entry;
		}
		if (entry.index == -1) return null;
		entry = root.ceilingEntry(i+1);
		if ((entry != null) && (entry.key == SUB_MAP)) 
			return ((FastMap<K,V>)entry.value).firstEntry();
		return entry;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Entry<K, V> floorEntry(K key) {
		int i = keyOrder.indexOf(key);
		EntryNode<K,V> entry = root.floorEntry(i);
		if (entry == null) return null;
		if (entry.key == SUB_MAP) {
			Entry<K,V> subMapEntry = ((FastMap<K,V>)entry.value).floorEntry(key);
			if (subMapEntry != null) return subMapEntry;
		} else {
			if (keyOrder.compare(entry.key, key) <= 0) return entry;
		}
		if (entry.index == 0) return null;
		entry = root.floorEntry(i-1);
		if ((entry != null) && (entry.key == SUB_MAP)) 
			return ((FastMap<K,V>)entry.value).lastEntry();
		return entry;
	}

    @Override
    public boolean isEmpty() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Iterator<org.javolution.util.FastMap.Entry<K, V>> iterator() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Iterator<org.javolution.util.FastMap.Entry<K, V>> descendingIterator() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Iterator<org.javolution.util.FastMap.Entry<K, V>> iterator(K fromKey) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Iterator<org.javolution.util.FastMap.Entry<K, V>> descendingIterator(K fromKey) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public org.javolution.util.FastMap.Entry<K, V> putEntry(K key, V value) {
        // TODO Auto-generated method stub
        return null;
    }

}