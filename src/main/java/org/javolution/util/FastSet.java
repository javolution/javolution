/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.util;

import static org.javolution.annotations.Realtime.Limit.CONSTANT;
import static org.javolution.annotations.Realtime.Limit.LINEAR;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NavigableSet;
import java.util.Set;

import org.javolution.annotations.Parallel;
import org.javolution.annotations.ReadOnly;
import org.javolution.annotations.Realtime;
import org.javolution.lang.MathLib;
import org.javolution.util.function.Consumer;
import org.javolution.util.function.Equality;
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
 * <p> A high-performance ordered set / multiset (trie-based) with {@link Realtime strict timing constraints}.</p>
 * 
 * <p> In general, fast set methods have a limiting behavior in {@link Realtime#Limit#CONSTANT O(1)} (constant) 
 *     to be compared with {@link Realtime#Limit#LOG_N O(log n)} for most sorted sets.</p>
 *     
 * <p> Iterations order over the set elements is typically determined by the set {@link #comparator() comparator} except 
 *     for specific views such as the {@link #linked linked} view for which the iterations order is the insertion 
 *     order.</p> 
 * 
 * <p> Instances of this class can advantageously replace {@code java.util.*} sets in terms of adaptability, 
 *     space or performance. 
 * <pre>{@code
 * import static javolution.util.function.Order.*;
 * 
 * FastSet<Foo> hashSet = FastSet.newSet(); // Arbitrary order (hash order) 
 * FastSet<Foo> identitySet = FastSet.newSet(IDENTITY); 
 * FastSet<Foo> multiset = FastSet.newSet(MULTI); // Arbitrary order (hash order) allowing duplicates.
 * FastSet<String> treeSet = FastSet.newSet(LEXICAL); 
 * FastSet<Foo> linkedHashSet = FastSet.<Foo>newSet().linked(); // Insertion order.
 * FastSet<Foo> linkedIdentitySet = FastSet.<Foo>newSet(IDENTITY).linked(); 
 * FastSet<Foo> concurrentHashSet = FastSet.<Foo>newSet().shared();
 * FastSet<String> concurrentSkipListSet = FastSet.<String>newSet(LEXICAL).shared()
 * FastSet<Foo> copyOnWriteArraySet = FastSet.<Foo>newSet().atomic();
 * FastSet<Foo> concurrentLinkedHashSet = FastSet.<Foo>newSet().linked().shared(); 
 * FastSet<Foo> linkedMultiset = FastSet.<Foo>newSet(MULTI).linked(); // Insertion order allowing duplicates.
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
 * <p> Finally, this class provides full support for multisets (for which the same element may appear more than once).
 *  
 * <pre>{@code
 * // Prime factors of 120 are {2, 2, 2, 3, 5}.
 * ConstSet<Integer> primeFactors120 = ConstSet.of(MULTI, 2, 2, 2, 3, 5); // Arbitrary order allowing duplicates.
 * int twoCount = primeFactors120.subSet(2).size();
 * FastSet<Integer> primeFactors120bis = FastSet.<Integer>newSet(MULTI).linked(); 
 * primeFactors120bis.addAll(5, 2, 3, 2, 2); // Keep insertion order (linked multiset).
 *  
 * System.out.println(twoCount);
 * System.out.println(primeFactors120.equals(primeFactors120bis));
 * System.out.println(primeFactors120bis);
 * 
 * >> 3
 * >> true
 * >> { 5, 2, 3, 2, 2 }
 * }</pre></p>
 * 
 * @param <E> the type of set elements
 * 
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 7.0, March 14, 2016
 * @see <a href="https://en.wikipedia.org/wiki/Multiset">Wikipedia: Multiset</a>
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
     * Adds the specified element unless this set already {@link #contains(Object) contains} the specified element 
     * (always added for multisets).
     */
    @Override
    public abstract boolean add(E element);
    
    @Override
    @Realtime(limit = CONSTANT)
    public abstract Iterator<E> iterator();

    /** 
     * Indicates if this set contains the specified element; for multisets, the method {@link #contains(Object, Equality)}
     * makes more sense (multisets will always return {@code false} since its elements are all considered distinct).
     */
    @Parallel(false)
    @Override
    @Realtime(limit = CONSTANT)
    public abstract boolean contains(Object obj);

    /** Equivalent to {@code !subSet(element, equality).isEmpty()} (convenience method). */
    public boolean contains(E element, Equality<? super E> equality) {
        return !subSet(element, equality).isEmpty();
    }

    /** Removes one element of this set for which {@code order.areEqual(element, obj) == true};
     * for multisets, the method {@link #remove(Object, Equality)} makes more sense. */
    @Parallel(false)
    @Override
    @Realtime(limit = CONSTANT)
    public abstract boolean remove(Object obj);

    /** Equivalent to {@code subSet(element, equality).removeIf(Predicate.TRUE)} (convenience method). */
    public boolean remove(E element, Equality<? super E> equality) {
        return subSet(element, equality).removeIf(Predicate.TRUE);
    }
    
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
    
    @SuppressWarnings("unchecked")
    @Override
    @Realtime(limit = LINEAR)
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof Set))
             return false;
        if (this.size() == ((Set<E>)obj).size()) return false;
        FastSet<E> that = (obj instanceof FastSet) ? (FastSet<E>)obj : ConstSet.of(Order.MULTI,((Set<E>)obj));
        // Two sets are considered equals; if they have the same cardinality for all their distinct elements.
        FastCollection<E> distinct = new FractalTable<E>().distinct();
        distinct.addAll(this);
        for (E e : distinct) {
            int thisCount = this.subSet(e, Equality.DEFAULT).size();
            int thatCount = that.subSet(e, Equality.DEFAULT).size();
            if (thisCount != thatCount) return false;
        }
        return true;
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
         final SparseSet<E> reduction = new SparseSet<E>(order());
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
        final Order<? super E> order = order();
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

    /** Returns all the elements such as {@code (order.compare(matching, toElement) < 0)}. */
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

    /** Equivalent to {@code subSet(element, Equality.DEFAULT)} (convenience method). */
    public FastSet<E> subSet(E element) {
        return subSet(element, Equality.DEFAULT);
    }
   /** 
     * Returns the set/multiset holding all the elements equal to the specified element according to the specified 
     * equality comparator (convenience method). The specified equality comparator should be consistent with 
     * this set order; if {@code equality.areEqual(x,y)} then {@code (order().compare(x,y) == 0)}. */
    public FastSet<E> subSet(final E element, final Equality<? super E> equality) {
        return subSet(element, true, element, true).filter(new Predicate<E>(){
            @Override
            public boolean test(E param) {
                return equality.areEqual(element, param);
            }});
        
    }
 
    /** Returns all the elements {@code matching} such as 
     * {@code (order.compare(fromElement, matching) >= 0) && (order.compare(matching, toElement) < 0)}. */
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

    /** Returns all the elements {@code matching} such as {@code (order.compare(fromElement, matching) <= 0)}. */
    @Override
    public FastSet<E> tailSet(E fromElement) {
        return new SubSetImpl<E>(this, fromElement, true, null, null);
    }

	@Override
	public FastSet<E> tailSet(E fromElement, boolean inclusive) {
        return new SubSetImpl<E>(this, fromElement, inclusive, null, null);
	}

    @Override
	public @ReadOnly FastSet<E> unmodifiable() {
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
	
    /** Returns the order of this set. */
    public abstract Order<? super E> order();

    /** Returns an iterator starting from the specified element. */
    public abstract Iterator<E> iterator(E fromElement);
    
    /** Returns a descending iterator starting from the specified element. */
    public abstract Iterator<E> descendingIterator(E fromElement);
    
    /** Returns {@link #order order()}. */
    @Override
	public final Equality<? super E> equality() {
		return order();
	}
    
	@Override
	public FastSet<E> clone() {
	    return (FastSet<E>) super.clone();
	}
  
    @Override
    public boolean removeIf(Predicate<? super E> filter) {
        boolean removed = false;
        for (Iterator<E> itr=iterator(); itr.hasNext();) {
            E element = itr.next();
            if (filter.test(element)) {
                itr.remove();
                removed = true;
            }
        }
        return removed;
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

    /** Returns {@link #order order()}. */
	@Override
	public final Comparator<? super E> comparator() {
	    return order();
	}
	
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