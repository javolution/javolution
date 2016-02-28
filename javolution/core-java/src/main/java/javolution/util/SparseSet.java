/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util;

import java.util.Iterator;

import javolution.util.function.Consumer;
import javolution.util.function.Order;
import javolution.util.function.Predicate;

/**
* <p> A <a href="http://en.wikipedia.org/wiki/Trie">trie-based</a> set 
 *     allowing for quick searches, insertions and deletion.</p> 
 *  
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 7.0, September 13, 2015
 * @see SparseMap
 */
public class SparseSet<E> extends FastSet<E> {
	   
	private static final long serialVersionUID = 0x700L; // Version. 
	   
	/**
     * Creates an empty set using an arbitrary order (hash based).
     */
    public SparseSet() {
    	this(Order.HASH);
    }
	/**
     * Creates an empty set using the specified order.
     * 
     * @param order the ordering of the map.
     */
    public SparseSet(Order<? super E> order) {
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
	public Order<? super E> comparator() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public FastSet<E> clone() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void forEach(Consumer<? super E> consumer) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public boolean removeIf(Predicate<? super E> filter) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public boolean add(E element) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public Iterator<E> iterator() {
		// TODO Auto-generated method stub
		return null;
	}

}