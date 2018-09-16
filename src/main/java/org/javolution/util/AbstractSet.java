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
import java.util.Set;
import java.util.SortedSet;

import org.javolution.annotations.Nullable;
import org.javolution.annotations.Parallel;
import org.javolution.annotations.ReadOnly;
import org.javolution.annotations.Realtime;
import org.javolution.lang.MathLib;
import org.javolution.util.function.Equality;
import org.javolution.util.function.Order;
import org.javolution.util.function.Predicate;
import org.javolution.util.internal.set.AtomicSetImpl;
import org.javolution.util.internal.set.FilteredSetImpl;
import org.javolution.util.internal.set.LinkedSetImpl;
import org.javolution.util.internal.set.MultiSetImpl;
import org.javolution.util.internal.set.SharedSetImpl;
import org.javolution.util.internal.set.SubSetImpl;
import org.javolution.util.internal.set.UnmodifiableSetImpl;

/**
 * High-performance ordered set / multiset with {@link Realtime strict timing constraints}.
 * 
 * A multiset (sometimes called a bag) is a generalization of the concept of a set that, unlike a standard set, 
 * allows multiple instances of the multiset's elements. 
 * 
 * Instances of this class may use custom element comparators instead of the default object equality 
 * when comparing elements. This affects the behavior of the contains, remove, containsAll, equals, and 
 * hashCode methods. The {@link java.util.Set} contract is guaranteed to hold only for sets
 * using {@link Equality#STANDARD} for {@link #equality() elements comparisons}.
 *      
 * @param <E> the type of set elements ({@code null} values are not supported)
 * 
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 7.0, March 14, 2016
 * @see <a href="https://en.wikipedia.org/wiki/Multiset">Wikipedia: Multiset</a>
 */
public abstract class AbstractSet<E> extends AbstractCollection<E> implements SortedSet<E> {
    
    private static final long serialVersionUID = 0x700L; // Version.

    @Override
    public AbstractSet<E> with(@SuppressWarnings("unchecked") E... elements) {
        addAll(elements);
        return this;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Views.
    //

    /** 
     * Returns a view allowing multiple instances of the same element to be added to this set (multiset).
     */
    @Realtime(limit = CONSTANT)
    public AbstractSet<E> multi() {
        return new MultiSetImpl<E>(this);
    }
     
    /** 
     * Returns a view of the portion of this set whose elements range from {@code fromElement} to {@code toElement}.
     * 
     * @param fromElement the lower order limit.
     * @param fromInclusive indicates if elements {@link #equality equals} to {@code fromElement} are included.
     * @param toElement the higher order limit.  
     * @param toInclusive indicates if elements {@link #equality equals} to {@code toElement} are included.
     */
    @Realtime(limit = CONSTANT)
    public AbstractSet<E> subSet(@Nullable E fromElement, boolean fromInclusive, @Nullable E toElement, 
            boolean toInclusive) {
        return new SubSetImpl<E>(this, fromElement, fromInclusive, toElement, toInclusive);
    }
     
    /** Returns a view holding only the elements equals to the one specified (multiset). */
    @Realtime(limit = CONSTANT)
    public AbstractSet<E> subSet(E element) {
        return new SubSetImpl<E>(this, element, true, element, true);
    }
     
    @Override
    @Realtime(limit = CONSTANT)
    public AbstractSet<E> subSet(E fromElement, E toElement) {
        return subSet(fromElement, true, toElement, false);
    }

   @Override
    @Realtime(limit = CONSTANT)
    public AbstractSet<E> atomic() {
        return new AtomicSetImpl<E>(this);
    }

    @Override
    @Realtime(limit = CONSTANT)
    public AbstractSet<E> headSet(E toElement) {
        return subSet( null, false, toElement, false);
    }

    @Override
    @Realtime(limit = CONSTANT)
    public AbstractSet<E> filter(Predicate<? super E> filter) {
        return new FilteredSetImpl<E>(this, filter);
    }

    @Override
    @Realtime(limit = CONSTANT)
    public AbstractSet<E> linked() {
        return new LinkedSetImpl<E>(this);
    }
    
    @Override
    @Realtime(limit = CONSTANT)
    public AbstractSet<E> shared() {
        return new SharedSetImpl<E>(this); 
    }

    @Override
    @Realtime(limit = CONSTANT)
    public AbstractSet<E> tailSet(E fromElement) {
        return subSet(fromElement, true, null, false);
    }

    @Override
    @Realtime(limit = CONSTANT)
    public @ReadOnly AbstractSet<E> unmodifiable() {
        return new UnmodifiableSetImpl<E>(this);
    }
   
    @Override
    @Realtime(limit = CONSTANT)
    public boolean add(E element) {
        return add(element, false); // By default we don't allow duplicate.
    }
    
    /** 
     * Adds the specified element allowing multiple instances if {@code allowDuplicate} parameter is set.   
     * Allowing duplicate is usually faster since there is no check if the element is already presents.
     * 
     * @param element the element to be added.
     * @param allowDuplicate indicates if multiple instances are supported.
     */
    @Realtime(limit = CONSTANT)
    public abstract boolean add(E element, boolean allowDuplicate);
    
    @SuppressWarnings("unchecked")
    @Parallel(false)
    @Override
    @Realtime(limit = CONSTANT)
    public boolean contains(Object element) {
        return getAny((E)element) != null;
    }
  
    @SuppressWarnings("unchecked")
    @Parallel(false)
    @Override
    @Realtime(limit = LINEAR, comment="Linear removal time for linked sets")
    public boolean remove(Object element) {
        return removeAny((E)element) != null;
    }

    @Override
    @Realtime(limit = LINEAR)
    public abstract boolean removeIf(Predicate<? super E> filter);

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Change in Time Behavior.
    
    @Override
    @Realtime(limit = LINEAR)
    public boolean containsAll(Collection<?> that) {
        return super.containsAll(that);
    }
        
    /**
     * Compares the specified object with this set / multiset for equality. 
     * 
     * Two sets / multisets are considered equal if they have the same size and each element of one set is present
     * in the other set with the same number of occurrences (multiset).
     */
    @Override
    @Realtime(limit = LINEAR)
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof Set)) return false;
        @SuppressWarnings("unchecked")
        Set<E> that = (Set<E>)obj;
        if (that.size() != size()) return false;
        if (!(that instanceof AbstractSet)) return that.equals(this); 
        AbstractSet<E> thatMulti = (AbstractSet<E>) that;
        FastSet<E> done = new FastSet<E>(order()); // To avoid repeating cardinality checks.
        for (E e : this) { 
            if (done.contains(e)) continue; // Already done.
            done.add(e);
            if (subSet(e).size() != thatMulti.subSet(e).size()) return false; // Checks cardinality.
        }
        return true;
    }

    @Override
    @Realtime(limit = LINEAR)
    public int hashCode() { // See Set contract.
        Order<? super E> order = order();
        int hash = 0;
        for (E e : this) 
            if (e != null) hash += order.indexOf(e);
        return hash;
    }
    
    /**  
     * Splits into filtered sets with filter based on the system identity hash code of the set element
     * (to ensure balanced distribution).
     */
    @Override
    @SuppressWarnings("unchecked")
    @Realtime(limit = CONSTANT)
    public AbstractSet<E>[] trySplit(final int n) {
        AbstractSet<E>[] split = new AbstractSet[n];        
        for (int i=0; i < n; i++) {
            final int m = i;
            split[i] = this.filter(new Predicate<E>() {
                @Override
                public boolean test(E param) {
                    int hash = MathLib.hash(System.identityHashCode(param));
                    return Math.abs(hash) % n == m;
                }}).unmodifiable();
        }
        return split;
    }


    ////////////////////////////////////////////////////////////////////////////
    // Misc.
    //
	    
    /** 
     * Returns any element equals to the specified element (or {@code null} if none).
     */
    @Realtime(limit = CONSTANT)
    public abstract E getAny(E element);
    
    /** 
     * Removes and returns any element equals to the specified element (or {@code null} if none).
     */
    @Realtime(limit = CONSTANT)
    public abstract E removeAny(E element);
    
    /** 
     * Returns this set order.
     */
    @Realtime(limit = CONSTANT)
    public abstract Order<? super E> order();
 
    /**
     * Returns an iterator over the elements of this set higher or equal to the specified element.
     * The iteration order is implementation dependent (e.g. insertion order for linked set).
     * 
     * @param low the lower point (inclusive) or {@code null} to start from the first element. 
     */
    @Realtime(limit = LINEAR, comment="For shared sets a copy of this set may be performed")
    public abstract FastIterator<E> iterator(@Nullable E low);
 
    /**
     * Returns a descending iterator over the elements of this set lower than the specified element.
     * The iteration order is implementation dependent (e.g. insertion order for linked set).
     * 
     * @param high the higher point (inclusive) or {@code null} to start from the last element. 
     */
    @Realtime(limit = LINEAR, comment="For shared sets a copy of this set may be performed")
    public abstract FastIterator<E> descendingIterator(@Nullable E high);
         
    /**
     * Returns an iterator over this set, the iteration order is implementation dependent 
     * (e.g. insertion order for linked set).
     *  
     * @return {@code iterator(null)}
     */
    @Override
    @Realtime(limit = LINEAR, comment="For shared sets a copy of this set may be performed")
    public FastIterator<E> iterator() {
        return iterator(null);
    }
 
    /**
     * Returns a descending iterator over this set, the iteration order is implementation dependent 
     * (e.g. reversed insertion order for linked set). 
     *  
     * @return {@code descendingIterator(null)}
     */
    @Override
    @Realtime(limit = LINEAR, comment="For shared sets a copy of this set may be performed")
    public FastIterator<E> descendingIterator() {
        return descendingIterator(null); 
    }
         
    @Override
    @Realtime(limit = LINEAR)
	public AbstractSet<E> clone() {
	    return (AbstractSet<E>) super.clone();
	}
  
    @Override
    @Realtime(limit = CONSTANT)
    public Equality<? super E> equality() {
        return order();
    }


   ////////////////////////////////////////////////////////////////////////////
    // SortedSet Interface.
    //

    @Override
    @Realtime(limit = CONSTANT)
    public Comparator<? super E> comparator() {
        return order();
    }

    @Override
    @Realtime(limit = LINEAR, comment="Filtered sets may iterate the whole collection")
    public E first() {
        return iterator().next();
    }

    @Override
    @Realtime(limit = LINEAR, comment="Filtered sets may iterate the whole collection")
    public E last() {
        return descendingIterator().next();
    }

   
}