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

import org.javolution.util.function.Order;

/**
 * <p> The default <a href="http://en.wikipedia.org/wiki/Trie">trie-based</a> 
 *     implementation of {@link FastSet}.</p> 
 *  
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 7.0, September 13, 2015
 * @see SparseMap
 */
public class SparseSet<E> extends FastSet<E> {
	   
	private static final long serialVersionUID = 0x700L; // Version.
	private static final Object PRESENT = new Object();
	private SparseMap<E,Object> sparse;
	   
	/**
     * Creates an empty set using an arbitrary order (hash based).
     */
    public SparseSet() {
    	this(Order.DEFAULT);
    }
	/**
     * Creates an empty set using the specified order.
     * 
     * @param order the ordering of the map.
     */
    public SparseSet(Order<? super E> order) {
    	sparse = new SparseMap<E, Object>(order);
    }
    
    /** Structural constructor (for cloning) */
    private SparseSet(SparseMap<E, Object> sparse) {
        this.sparse = sparse;	
    }
    
    @Override
    public Iterator<E> iterator() {
        // TODO Auto-generated method stub
        return null;
    }
    @Override
    public boolean isEmpty() {
        // TODO Auto-generated method stub
        return false;
    }
    @Override
    public boolean contains(Object obj) {
        // TODO Auto-generated method stub
        return false;
    }
    @Override
    public boolean remove(Object obj) {
        // TODO Auto-generated method stub
        return false;
    }
    @Override
    public Iterator<E> iterator(E fromElement) {
        // TODO Auto-generated method stub
        return null;
    }
    @Override
    public Iterator<E> descendingIterator(E fromElement) {
        // TODO Auto-generated method stub
        return null;
    }
    @Override
    public FastSet<E> clone() {
        // TODO Auto-generated method stub
        return null;
    }
    @Override
    public Order<? super E> comparator() {
        // TODO Auto-generated method stub
        return null;
    }
    @Override
    public Iterator<E> descendingIterator() {
        // TODO Auto-generated method stub
        return null;
    }
    @Override
    public boolean add(E element) {
        // TODO Auto-generated method stub
        return false;
    }
    @Override
    public int size() {
        // TODO Auto-generated method stub
        return 0;
    }
    @Override
    public void clear() {
        // TODO Auto-generated method stub
        
    }
    
/*    
	@Override
	public E first() {
		return sparse.firstKey();
	}
	
	@Override
	public E last() {
		return sparse.lastKey();
	}
	
	@Override
	public Order<? super E> comparator() {
		return sparse.comparator();
	}
	
    @Realtime(limit = CONSTANT)
	@Override
	public int size() {
		return sparse.size();
	}

    @Realtime(limit = CONSTANT)
	@Override
	public void clear() {
		sparse.clear();
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean contains(Object obj) {
		return sparse.containsKey((E) obj); // Cast has no effect here.
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean remove(Object obj) {
		return sparse.removeEntry((E)obj) != null;
	}

	@Override
	public SparseSet<E> clone() {
		return new SparseSet<E>(sparse.clone());
	}
	
	@Override
	public boolean add(E element) {
		return sparse.put(element, PRESENT) == null;
	}
	@Override
	public boolean isEmpty() {
		return sparse.isEmpty();
	}
	@Override
	public E ceiling(E element) {
		return sparse.ceilingKey(element);
	}
	@Override
	public E floor(E element) {
		return sparse.floorKey(element);
	}
	@Override
	public E higher(E element) {
		return sparse.higherKey(element);
	}
	@Override
	public E lower(E element) {
		return sparse.lowerKey(element);
	}
	@Override
	public E pollFirst() {
		Entry<E,Object> entry = sparse.pollFirstEntry();
		return entry != null ? entry.getKey() : null;
	}
	@Override
	public E pollLast() {
		Entry<E,Object> entry = sparse.pollLastEntry();
		return entry != null ? entry.getKey() : null;
	}
*/
}