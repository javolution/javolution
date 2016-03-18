/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util;

import static javolution.lang.Realtime.Limit.CONSTANT;
import static javolution.lang.Realtime.Limit.LINEAR;

import java.util.Iterator;
import java.util.NavigableSet;

import javolution.lang.Realtime;
import javolution.util.function.Equality;
import javolution.util.function.Order;
import javolution.util.function.Predicate;

/**
 * <p> A high-performance ordered set (trie-based) with 
 *     {@link Realtime strict timing constraints}.</p>
 * 
 * <p> In general, fast set methods have a limiting behavior in 
 *     {@link Realtime#Limit#CONSTANT O(1)} (constant) to be compared 
 *     with {@link Realtime#Limit#LOG_N O(log n)} for most sorted sets.</p>
 *     
 * <p> Iterations order over the set elements is typically determined 
 *     by the set {@link #comparator() order} except for specific views 
 *     such as the {@link #linked linked} view for which the iterations 
 *     order is the insertion order.</p> 
 * 
 * <p> Instances of this class can advantageously replace {@code java.util.*} 
 *     sets in terms of adaptability, space or performance. 
 * <pre>{@code
 * FastSet<Foo> hashSet = FastSet.newSet(); // Hash order. 
 * FastSet<Foo> identityHashSet = FastSet.newSet(Order.IDENTITY);
 * FastSet<String> treeSet = FastSet.newSet(Order.LEXICAL); 
 * FastSet<Foo> linkedHashSet = FastSet.newSet(Foo.class).linked(); // Insertion order.
 * FastSet<Foo> concurrentHashSet = FastSet.newSet(Foo.class).shared(); 
 * FastSet<String> concurrentSkipListSet = FastSet.newSet(Order.LEXICAL, String.class).shared();
 * FastSet<Foo> copyOnWriteArraySet = FastSet.newSet(Foo.class).atomic();
 * ...
 * }</pre> </p>
 * 
 * <p> This class inherits all the {@link FastCollection} views and support 
 *     the new {@link #subSet subSet} view over a portion of the set.
 * <pre>{@code
 * FastSet<String> names = FastSet.newSet(Equality.LEXICAL); 
 * ...
 * names.subSet("A", "B").clear(); // Removes the names starting with "A"  (see java.util.SortedSet.subSet specification).
 * names.filter(str -> str.length < 5).clear(); // Removes all short name (Java 8 notation).
 * names.filter(str -> str.length < 5).parallel().clear(); // Same as above but removal performed concurrently.
 * }</pre></p>
 *      
 * @param <E> the type of set element (can be {@code null})
 * 
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 7.0, March 14, 2016
 */
public abstract class FastSet<E> extends FastCollection<E> implements NavigableSet<E> {

    private static final long serialVersionUID = 0x700L; // Version.

    /**
     * Default constructor.
     */
    protected FastSet() {
    }

    /**
     * Returns a new high-performance set sorted arbitrarily (hash-based order).
     */
    public static <E> SparseSet<E> newSet() {
    	return new SparseSet<E>();
    }

    /**
     * Returns a new high-performance set sorted arbitrarily for the specified
     * element type.
     */
    public static <E> SparseSet<E> newSet(Class<E> elementType) {
    	return new SparseSet<E>();
    }

    /**
     * Returns a new high-performance set sorted according to the specified
     * comparator.
     */
    public static <E> SparseSet<E> newSet(Order<? super E> comparator) {
    	return new SparseSet<E>(comparator);
    }

    /**
     * Returns a new high-performance set sorted according to the specified
     * comparator for the specified element type.
     */
    public static <E> SparseSet<E> newSet(Order<? super E> comparator, Class<E> elementType) {
    	return new SparseSet<E>(comparator);
    }

	////////////////////////////////////////////////////////////////////////////
    // Change in time limit behavior.
    //

	@Override
	@Realtime(limit = CONSTANT)
	public abstract boolean add(E element);

	@Override
    @Realtime(limit = CONSTANT)
    public abstract boolean isEmpty();

    @Override
    @Realtime(limit = CONSTANT)
    public abstract int size();

    @Override
    @Realtime(limit = CONSTANT)
    public abstract void clear();

    @Override
    @Realtime(limit = CONSTANT)
    public abstract boolean contains(Object obj);

    @Override
    @Realtime(limit = CONSTANT)
    public abstract boolean remove(Object obj);

    ////////////////////////////////////////////////////////////////////////////
    // Views.
    //

    @Override
	public FastSet<E> atomic() {
		return null;
	}

    @Override
	public FastSet<E> shared() {
		return null;
	}

    @Override
	public FastSet<E> unmodifiable() {
		return null;
	}
	
    @Override
	public FastSet<E> reversed() {
		return null;
    }
    
	public FastSet<E> filter(Predicate<? super E> filter) {
		return null;
	}

	public FastSet<E> linked() {
		return null;
	}
   
    @Override
    public FastSet<E> subSet(E fromElement, E toElement) {
    	return subSet(fromElement, true, toElement, false);
    }

    @Override
    public FastSet<E> headSet(E toElement) {
        return subSet(first(), true, toElement, false);
    }

    @Override
    public FastSet<E> tailSet(E fromElement) {
        return subSet(fromElement, true, last(), true);
    }

    @Override
	public FastSet<E> headSet(E toElement, boolean inclusive) {
		return subSet(first(), true, toElement, inclusive);
	}

	@Override
	public FastSet<E> tailSet(E fromElement, boolean inclusive) {
		return subSet(fromElement, inclusive, last(), true);
	}

	@Override
	public FastSet<E> subSet(E fromElement, boolean fromInclusive,
			E toElement, boolean toInclusive) {
		return null;
	}

	/** 
     * Equivalent to {@link #reversed()}.
     * @deprecated {@link #reversed()} should be used.
     */
	@Override
    @Realtime(limit = LINEAR)
	public NavigableSet<E> descendingSet() {
		return reversed();
	}

    ////////////////////////////////////////////////////////////////////////////
    // Misc.
    //
	
	/**
	 * Returns an immutable set holding the same elements and having 
	 * the same comparator as this set. 	
     */
	@Override
	@Realtime(limit = LINEAR)
	public ConstantSet<E> constant() {
		SparseSet<E> sparse = new SparseSet<E>(comparator());
		sparse.addAll(this);
		return new ConstantSet<E>(sparse);
	}

	@Override
    public FastSet<E> addAll(E first, @SuppressWarnings("unchecked") E... others) {
		super.addAll(first, others);
		return this;
	}

	@Override
    @Realtime(limit = CONSTANT)
	public Iterator<E> iterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
    @Realtime(limit = CONSTANT)
	public final Equality<? super E> equality() {
		return comparator();
	}
	
	@Override
    @Realtime(limit = LINEAR)
	public FastSet<E> clone() {
		return (FastSet<E>) super.clone();
	}


    ////////////////////////////////////////////////////////////////////////////
    // SortedSet / NavigableSet Interface.
    //

    @Override
    @Realtime(limit = CONSTANT)
    public abstract E first();

    @Override
    @Realtime(limit = CONSTANT)
    public abstract E last();

	@Override
    @Realtime(limit = CONSTANT)
	public abstract Order<? super E> comparator();
	
	@Override
    @Realtime(limit = CONSTANT)
	public abstract E ceiling(E element);

	@Override
    @Realtime(limit = CONSTANT)
	public Iterator<E> descendingIterator() {
		return reversed().iterator();
	}

	@Override
    @Realtime(limit = CONSTANT)
	public abstract E floor(E element);
	
	@Override
	@Realtime(limit = CONSTANT)
	public abstract E higher(E element);

	@Override
	@Realtime(limit = CONSTANT)
	public abstract E lower(E element);

	@Override
	@Realtime(limit = CONSTANT)
	public abstract E pollFirst();

	@Override
	@Realtime(limit = CONSTANT)
	public abstract E pollLast();
}