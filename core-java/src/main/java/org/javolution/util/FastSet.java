/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.util;

import static org.javolution.lang.Realtime.Limit.CONSTANT;
import static org.javolution.lang.Realtime.Limit.LINEAR;

import java.util.Collection;
import java.util.Iterator;
import java.util.NavigableSet;
import java.util.Set;

import org.javolution.lang.MathLib;
import org.javolution.lang.Parallel;
import org.javolution.lang.Realtime;
import org.javolution.util.function.Consumer;
import org.javolution.util.function.Order;
import org.javolution.util.function.Predicate;
import org.javolution.util.internal.set.AtomicSetImpl;
import org.javolution.util.internal.set.FilteredSetImpl;
import org.javolution.util.internal.set.LinkedSetImpl;
import org.javolution.util.internal.set.ParallelSetImpl;
import org.javolution.util.internal.set.ReversedSetImpl;
import org.javolution.util.internal.set.SharedSetImpl;
import org.javolution.util.internal.set.SubSetImpl;
import org.javolution.util.internal.set.UnmodifiableSetImpl;

/**
 * <p> A high-performance ordered set (trie-based) with {@link Realtime strict timing constraints}.</p>
 * 
 * <p> In general, ordered set methods have a limiting behavior in {@link Realtime#Limit#CONSTANT O(1)} (constant) 
 *     to be compared with {@link Realtime#Limit#LOG_N O(log n)} for most sorted sets.</p>
 *     
 * <p> Iterations order over the set elements is typically determined by the set {@link #comparator() order} except 
 *     for specific views such as the {@link #linked linked} view for which the iterations order is the insertion 
 *     order.</p> 
 * 
 * <p> Instances of this class can advantageously replace {@code java.util.*} sets in terms of adaptability, 
 *     space or performance. 
 * <pre>{@code
 * FastSet<Foo> hashSet = FastSet.newSet(); // Hash order (type implicit). 
 * FastSet<Foo> identityHashSet = FastSet.newSet(Order.IDENTITY); 
 * FastSet<String> treeSet = FastSet.newSet(Order.LEXICAL); 
 * FastSet<Foo> linkedHashSet = FastSet.newSet().linked().downcast(); // Insertion order.
 * FastSet<Foo> concurrentHashSet = FastSet.newSet().shared().downcast(); 
 * FastSet<String> concurrentSkipListSet = FastSet.newSet().shared().downcast();
 * FastSet<Foo> copyOnWriteArraySet = FastSet.newSet().atomic().downcast();
 * ...
 * }</pre> </p>
 * 
 * <p> This class inherits all the {@link FastCollection} views and support the new {@link #subSet subSet} view over
 *     a portion of the set.
 * <pre>{@code
 * FastSet<String> names = FastSet.newSet(Order.LEXICAL); 
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
     * Returns a new high-performance set sorted arbitrarily (hash order).
     */
    public static <E> FastSet<E> newSet() {
    	return new SparseSet<E>();
    }

    /**
     * Returns a new high-performance set sorted according to the specified order.
     */
    public static <E> FastSet<E> newSet(Order<? super E> order) {
    	return new SparseSet<E>(order);
    }

    /**
     * Downcast the parameterized type of a fast set (safe at creation).
     */
    @SuppressWarnings("unchecked")
    public <E1 extends E> FastSet<E1> downcast() {
        return (FastSet<E1>) this; 
    }

    
    ////////////////////////////////////////////////////////////////////////////
    // Change in time limit behavior and parallelization.
    //

    @Override
    @Realtime(limit = CONSTANT)
    public abstract Iterator<E> iterator();

    @Parallel(false)
    @Override
    @Realtime(limit = CONSTANT)
    public abstract boolean contains(Object obj);

    @Parallel(false)
    @Override
    @Realtime(limit = CONSTANT)
    public abstract boolean remove(Object obj);

    @Override
    @Realtime(limit = LINEAR)
    public boolean containsAll(Collection<?> that) {
        return super.containsAll(that);
    }
    
    @Override
    @Realtime(limit = LINEAR)
    public boolean removeAll(Collection<?> that) {
        boolean modified = false;        
        for (Object obj : that)
            if (remove(obj)) modified = true;
        return modified;
    }
    
    @Override
    @Realtime(limit = LINEAR)
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof Set))
             return false;
        @SuppressWarnings("unchecked")
        Set<E> set = (Set<E>) obj;
        return (size() == set.size()) && containsAll(set);
    }    

    @Override
    @Realtime(limit = LINEAR)
    public int hashCode() {
        Iterator<E> itr = this.iterator();
        int hash = 0;
        while (itr.hasNext()) {
            E e = itr.next();
            hash += (e != null) ? e.hashCode() : 0;
        }
        return hash;
    }
    
    @Override
    public FastSet<E> collect() {
         final FastSet<E> reduction = FastSet.newSet(comparator());
         forEach(new Consumer<E>() {
            @Override
            public void accept(E param) {
                synchronized (reduction) {
                    add(param);
                }
            }});
         return reduction;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public FastSet<E>[] trySplit(final int n) {
        // Split into filtered sets with filter based on the element index (hashed to ensure balanced distribution). 
        final Order<? super E> order = comparator();
        FastSet<E>[] split = new FastSet[n];        
        for (int i=0; i < n; i++) {
            final int m = i;
            split[i] = this.filter(new Predicate<E>() {

                @Override
                public boolean test(E param) {
                    int hash = MathLib.hash(order.indexOf(param));
                    return Math.abs(hash) % n == m;
                }});
        }
        return split;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Views.
    //

    @Override
	public FastSet<E> atomic() {
        return new AtomicSetImpl<E>(this);
	}

    @Override
    public FastSet<E> headSet(E toElement) {
        return new SubSetImpl<E>(this, null, null, toElement, false);
    }

    @Override
	public FastSet<E> headSet(E toElement, boolean inclusive) {
        return new SubSetImpl<E>(this, null, null, toElement, inclusive);
	}

    @Override
    public FastSet<E> filter(Predicate<? super E> filter) {
        return new FilteredSetImpl<E>(this, filter);
    }

    @Override
	public FastSet<E> linked() {
		return new LinkedSetImpl<E>(this);
	}
   
    @Override
	public FastSet<E> parallel() {
		return new ParallelSetImpl<E>(this);
	}

    @Override
	public FastSet<E> reversed() {
		return new ReversedSetImpl<E>(this);
    }
    
    @Override
    public FastSet<E> sequential() {
        return this;
    }

    @Override
    public FastSet<E> subSet(E fromElement, E toElement) {
        return new SubSetImpl<E>(this, fromElement, true, toElement, false);
    }

	@Override
	public FastSet<E> subSet(E fromElement, boolean fromInclusive,
			E toElement, boolean toInclusive) {
        return new SubSetImpl<E>(this, fromElement, fromInclusive, toElement, toInclusive);
	}

    @Override
	public FastSet<E> shared() {
		return new SharedSetImpl<E>(this); 
	}

    @Override
    public FastSet<E> tailSet(E fromElement) {
        return new SubSetImpl<E>(this, fromElement, true, null, null);
    }

	@Override
	public FastSet<E> tailSet(E fromElement, boolean inclusive) {
        return new SubSetImpl<E>(this, fromElement, inclusive, null, null);
	}

    @Override
	public FastSet<E> unmodifiable() {
		return new UnmodifiableSetImpl<E>(this);
	}
	
	/** 
     * Equivalent to {@link #reversed()}.
     * @deprecated {@link #reversed()} should be used.
     */
	@Override
	public NavigableSet<E> descendingSet() {
		return reversed();
	}

    ////////////////////////////////////////////////////////////////////////////
    // Misc.
    //
	
    /** Returns an iterator starting from the specified element. */
    public abstract Iterator<E> iterator(E fromElement);
    
    /** Returns a descending iterator starting from the specified element. */
    public abstract Iterator<E> descendingIterator(E fromElement);
    
    @Override
	public final Order<? super E> equality() {
		return comparator();
	}
    
	@Override
	public abstract FastSet<E> clone();
  
    @Override
    public boolean removeIf(Predicate<? super E> filter) {
        final FastSet<E> toRemove = FastSet.newSet();
        // Default equality comparator, assumes that if x.equals(y) then matching.test(x) == matching.

        for (Iterator<E> itr=iterator(); itr.hasNext();) {
            E e = itr.next();
            if (filter.test(e)) toRemove.add(e);
        }                   
        for (Iterator<E> itr=toRemove.iterator(); itr.hasNext();) remove(itr.next());
        return !toRemove.isEmpty();
    }       

    ////////////////////////////////////////////////////////////////////////////
    // SortedSet / NavigableSet Interface.
    //

    @Override
    public E first() {
        return iterator().next();
    }

    @Override
    public E last() {
        return descendingIterator().next();
    }

	@Override
	public abstract Order<? super E> comparator();
	
	@Override
	public E ceiling(E element) {
        Iterator<E> itr = iterator(element);
        return itr.hasNext() ? itr.next() : null;
	}

	@Override
	public abstract Iterator<E> descendingIterator();

	@Override
	public E floor(E element) {
        Iterator<E> itr = descendingIterator(element);
        return itr.hasNext() ? itr.next() : null;
	}
	
	@Override
	public E higher(E element) {
        Iterator<E> itr = iterator(element);
        if (!itr.hasNext()) return null;
        E ceiling = itr.next();
        if (!equality().areEqual(element, ceiling)) return ceiling;
        return itr.hasNext() ? itr.next() : null;	    
	}

	@Override
	public E lower(E element) {
        Iterator<E> itr = descendingIterator(element);
        if (!itr.hasNext()) return null;
        E floor = itr.next();
        if (!equality().areEqual(element, floor)) return floor;
        return itr.hasNext() ? itr.next() : null;       
	}

	@Override
	public E pollFirst() {
	    return (isEmpty()) ? null : first();
	}

	@Override
	public E pollLast() {
        return (isEmpty()) ? null : first();
	}

}