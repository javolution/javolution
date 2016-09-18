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
import java.util.NoSuchElementException;

import org.javolution.lang.MathLib;
import org.javolution.util.SparseArray.EntryNode;
import org.javolution.util.SparseArray.Node;
import org.javolution.util.SparseArray.NullNode;
import org.javolution.util.function.Equality;
import org.javolution.util.function.Order;
import org.javolution.util.internal.map.UnorderedMapImpl;

/**
 * <p> The default <a href="http://en.wikipedia.org/wiki/Trie">trie-based</a> implementation of {@link FastMap}.</p> 
 *     
 * <p> Worst-case execution time when adding new entries is significantly better than when using standard hash table
 *     since there is no resize/rehash ever performed.</p> 
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
 * presidents.filter(str->str.startWith("John "); // Map holding presidents with "John" as first name.
 * presidents.values().filter(p->p.birth<1900).parallel().clear(); // Concurrent removal of presidents born before 1900.
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
		        	 new UnorderedMapImpl<K,V>(keyOrder);
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

    @Override
    public boolean isEmpty() {
        return size != 0;
    }

    @Override
    public Iterator<Entry<K, V>> iterator() {
        return new NodeIterator();
    }

    @Override
    public Iterator<Entry<K, V>> descendingIterator() {
        return new DescendingNodeIterator();
    }

    @Override
    public Iterator<Entry<K, V>> iterator(K fromKey) {
        return new NodeIterator(fromKey);
    }

    @Override
    public Iterator<Entry<K, V>> descendingIterator(K fromKey) {
        return new DescendingNodeIterator(fromKey);
    }

    /** Iterator over the entries of a sparse map. */
    private final class NodeIterator implements Iterator<Entry<K,V>> {
        private Iterator<Entry<K,V>> subItr; // Set when iterating sub-maps (subItr.hasNext() is always true)
        private EntryNode<K,V> next; 
        private Entry<K,V> current;
        public NodeIterator() {
            next = root.ceilingEntry(0);
            if ((next != null) && (next.key == SUB_MAP)) {
                @SuppressWarnings("unchecked")
                FastMap<K,V> subMap = ((FastMap<K,V>)next.value);
                subItr = subMap.iterator(); // subMap cannot be empty.
            }    
        }
        
        public NodeIterator(K from) {
            int index = keyOrder.indexOf(from);
            next = root.ceilingEntry(index);
            if ((next != null) && (next.key == SUB_MAP)) {
                @SuppressWarnings("unchecked")
                FastMap<K,V> subMap = ((FastMap<K,V>)next.value);
                if (index == next.index) {
                    subItr = subMap.iterator(from);
                    if (!subItr.hasNext()) {
                        subItr = null;
                        next();                    
                    }
                } else { // We can ignore from.
                    subItr = subMap.iterator(); // subMap cannot be empty.
                }
            }    
        }

        @Override
        public boolean hasNext() {
            return (subItr != null) || (next != null);
        }

        @Override
        public Entry<K,V> next() {
            if (subItr != null) {
                current = subItr.next();
                if (subItr.hasNext()) return current;
                subItr = null;
            } else {
                if (next == null) throw new NoSuchElementException();
                current = next;
            }
            // Moves to next node.
            next = (next.index != -1) ?
                    root.ceilingEntry((int) (MathLib.unsigned(next.index)+1)) : null;             
            if ((next != null) && (next.key == SUB_MAP)) {
                @SuppressWarnings("unchecked")
                FastMap<K,V> subMap = ((FastMap<K,V>)next.value);
                subItr = subMap.iterator();
            }
            return current;
        }
        
        @Override
        public void remove() {
            if (current == null) throw new IllegalStateException();
            removeEntry(current.getKey());
            current = null;
       }        
    }
    
    /** Descending iterator overs the entries of a sparse map. */
    private final class DescendingNodeIterator implements Iterator<Entry<K,V>> {
        private Iterator<Entry<K,V>> subItr; // Set when iterating sub-maps (subItr.hasNext() is always true)
        private EntryNode<K,V> next; 
        private Entry<K,V> current = null;
        public DescendingNodeIterator() {
            next = root.floorEntry(-1);
            if ((next != null) && (next.key == SUB_MAP)) {
                @SuppressWarnings("unchecked")
                FastMap<K,V> subMap = ((FastMap<K,V>)next.value);
                subItr = subMap.iterator(); // subMap cannot be empty.
            }    
        }
        public DescendingNodeIterator(K from) {
            int index = keyOrder.indexOf(from);
            next = root.floorEntry(index);
            if ((next != null) && (next.key == SUB_MAP)) {
                @SuppressWarnings("unchecked")
                FastMap<K,V> subMap = ((FastMap<K,V>)next.value);
                if (index == next.index) {
                    subItr = subMap.descendingIterator(from);
                    if (!subItr.hasNext()) {
                        subItr = null;
                        next();                    
                    }
                } else { // We can ignore from.
                    subItr = subMap.descendingIterator(); // subMap cannot be empty.
                }
            }    
        }

        @Override
        public boolean hasNext() {
            return (subItr != null) || (next != null);
        }

        @Override
        public Entry<K,V> next() {
            if (subItr != null) {
                current = subItr.next();
                if (subItr.hasNext()) return current;
                subItr = null;
            } else {
                if (next == null) throw new NoSuchElementException();
                current = next;
            }
            // Moves to next node.
            next = (next.index != 0) ?
                    root.floorEntry((int) (MathLib.unsigned(next.index)-1)) : null;             
            if ((next != null) && (next.key == SUB_MAP)) {
                @SuppressWarnings("unchecked")
                FastMap<K,V> subMap = ((FastMap<K,V>)next.value);
                subItr = subMap.descendingIterator();
            }
            return current;
        }
        
        @Override
        public void remove() {
            if (current == null) throw new IllegalStateException();
            removeEntry(current.getKey());
            current = null;
       }        
    }
    
}