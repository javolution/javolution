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

import java.util.SortedSet;

import javolution.lang.Realtime;
import javolution.util.function.Order;

/**
 * <p> A high-performance set ({@link SparseMap trie-based}) 
 *     with documented {@link Realtime real-time} behavior.</p>
 * 
 * <p> Iterations order over the set elements  is typically determined 
 *     by the set {@link #order() order} except for {@link #newLinkedSet() 
 *     linked set} for which the iterations order is based on the insertion 
 *     order.</p> 
 * 
 * <p> Instances of this class can advantageously replace {@code java.util.*} 
 *     sets in terms of adaptability, space or performance. 
 * <pre>{@code
 * FastSet<Foo> hashSet = FastSet.newSet(); // Hash order. 
 * FastSet<Foo> identityHashSet = FastSet.newSet(Equality.IDENTITY);
 * FastSet<Foo> linkedHashSet = newLinkedSet(); // Iteration order.
 * FastSet<String> treeSet = FastSet.newSet(Equality.LEXICAL); 
 * FastSet<Foo> concurrentHashSet = new SparseSet<Foo>().shared(); 
 * FastSet<String> concurrentSkipListSet = new SparseSet<String>(Equality.LEXICAL).shared();
 * FastSet<Foo> copyOnWriteArraySet = new SparseSet<Foo>().atomic();
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
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 7.0, September 13, 2013
 */
public abstract class FastSet<E> extends FastCollection<E> implements SortedSet<E> {

    private static final long serialVersionUID = 0x700L; // Version.

    /**
     * Returns a new empty set (based on sparse arrays).
     * 
     * @return {@code new SparseSet<E>()}
     * @see SparseSet
     */
    public static <E> FastSet<E> newSet() {
    	return new SparseSet<E>();
    }
    
    /**
     * Returns a new empty sorted set (based on sparse arrays) 
     * using the specified elements order. 
     * 
     * @return {@code new SparseSet<E>(order)}
     * @see SparseSet
     */
	public static <E> FastSet<E> newSet(Order<? super E> order) {
    	return new SparseSet<E>(order);
    }
  
    /**
     * Returns a new empty sorted set (based on sparse arrays) 
     * using an insertion order. 
     * 
     * @return {@code new LinkedSet<E>()}
     */
    public static <E> FastSet<E> newLinkedSet() {
    	return null;
    }
 
    /**
     * Default constructor.
     */
    protected FastSet() {
    }

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
    
    /** Returns a view of the portion of this set whose elements range
     * from fromElement, inclusive, to toElement, exclusive. */
    @Override
    public FastSet<E> subSet(E fromElement, E toElement) {
    	return null;
    }

    /** Returns a view of the portion of this set whose elements are 
     *  strictly less than toElement. */
    @Override
    public FastSet<E> headSet(E toElement) {
        return null;
    }

    /** Returns a view of the portion of this set whose elements are
     *  greater than or equal to fromElement. */
    @Override
    public FastSet<E> tailSet(E fromElement) {
        return null;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Change in time limit behavior.
    //

    @Override
    @Realtime(limit = CONSTANT)
    public boolean isEmpty() {
        return size() == 0;
    }

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
    // SortedSet Interface.
    //

    /** Returns the first (lowest) element currently in this set. */
    @Override
    public E first() {
    	FastIterator<E> itr = iterator();
    	return itr.hasNext() ? itr.next() : null;
    }

    /** Returns the last (highest) element currently in this set. */
    @Override
    public E last() {
      	FastIterator<E> itr = iterator().reversed();
    	return itr.hasNext() ? itr.next() : null;
    }

    /** Returns the ordering of this set. */
	@Override
	public abstract Order<? super E> comparator();
	
    ////////////////////////////////////////////////////////////////////////////
    // Misc.
    //

	@Override
	public final Order<? super E> equality() {
		return comparator();
	}
	
	@Override
	public abstract FastSet<E> clone();

}