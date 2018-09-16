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
import static org.javolution.annotations.Realtime.Limit.LOG_N;
import static org.javolution.annotations.Realtime.Limit.N_LOG_N;
import static org.javolution.annotations.Realtime.Limit.N_SQUARE;

import java.util.Collection;
import java.util.Comparator;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.RandomAccess;

import org.javolution.annotations.Nullable;
import org.javolution.annotations.Parallel;
import org.javolution.annotations.ReadOnly;
import org.javolution.annotations.Realtime;
import org.javolution.util.function.Equality;
import org.javolution.util.function.Function;
import org.javolution.util.function.Order;
import org.javolution.util.function.Predicate;
import org.javolution.util.internal.table.AtomicTableImpl;
import org.javolution.util.internal.table.CustomEqualityTableImpl;
import org.javolution.util.internal.table.MappedTableImpl;
import org.javolution.util.internal.table.QuickSortImpl;
import org.javolution.util.internal.table.SharedTableImpl;
import org.javolution.util.internal.table.SubTableImpl;
import org.javolution.util.internal.table.UnmodifiableTableImpl;

/**
 * A high-performance table with {@link Realtime strict timing constraints}.
 * 
 * Instances of this class may use custom element comparators instead of the default object equality 
 * when comparing elements. This affects the behavior of the contains, remove, containsAll, equals, and 
 * hashCode methods. The {@link java.util.List} contract is guaranteed to hold only for tables
 * using {@link Equality#STANDARD} for {@link #equality() elements comparisons}.
 * 
 * @param <E> the type of table elements ({@code null} instances are supported)
 * 
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 7.0, September 13, 2015
 */
public abstract class AbstractTable<E> extends AbstractCollection<E> implements List<E>, Deque<E>, RandomAccess {

    private static final long serialVersionUID = 0x700L; // Version.

    @Override
    public AbstractTable<E> with(@SuppressWarnings("unchecked") E... elements) {
        addAll(elements);
        return this;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Views.
    //

    /**
     * Returns a view over a portion of the table (equivalent to {@link java.util.List#subList(int, int)}).
     * 
     * @param fromIndex low index (inclusive)
     * @param toIndex high index (exclusive)
     * @return a view of the specified range within this table.
     * @throws IndexOutOfBoundsException if {@code (fromIndex < 0) || (toIndex > size) || (fromIndex > toIndex)}
     */
    @Realtime(limit = CONSTANT)
    public AbstractTable<E> subTable(int fromIndex, int toIndex) {
        if ((fromIndex < 0) || (toIndex > size()) || (fromIndex > toIndex))
            throw new IndexOutOfBoundsException(
                    "fromIndex: " + fromIndex + ", toIndex: " + toIndex + ", size(): " + size()); // As per List.subList contract.
        return new SubTableImpl<E>(this, fromIndex, toIndex);
    }

    @Override
    @Realtime(limit = CONSTANT)
    public @ReadOnly AbstractTable<E> unmodifiable() {
        return new UnmodifiableTableImpl<E>(this);
    }

    @Override
    @Realtime(limit = CONSTANT)
    public AbstractTable<E> atomic() {
        return new AtomicTableImpl<E>(this);
    }

    @Override
    @Realtime(limit = CONSTANT)
    public AbstractTable<E> equality(Equality<? super E> equality) {
        return new CustomEqualityTableImpl<E>(this, equality);
    }

    @Override
    @Realtime(limit = CONSTANT)
    public <R> AbstractTable<R> map(Function<? super E, ? extends R> function) {
        return new MappedTableImpl<E, R>(this, function);
    }

    @Override
    @Realtime(limit = CONSTANT)
    public AbstractTable<E> shared() {
        return new SharedTableImpl<E>(this);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Change in time limit behavior.
    //

    @Override
    @Realtime(limit = CONSTANT)
    public abstract void clear();

    @Parallel(false)
    @Override
    @Realtime(limit = CONSTANT)
    public boolean isEmpty() {
        return size() == 0;
    }

    @Parallel(false)
    @Override
    @Realtime(limit = CONSTANT)
    public abstract int size();

    ////////////////////////////////////////////////////////////////////////////
    // Iterative methods.
    //

    @Parallel(false)
    @Realtime(limit = LINEAR)
    public boolean removeIf(Predicate<? super E> filter) {
        int j = 0;
        int n = size();
        for (int i = 0; i < n; i++) {
            E e = get(i);
            if (filter.test(e)) continue; // Removed (not copied)
            if (i != j) set(j, e);
            j++;
        }
        for (int i = j; i < n; i++) removeLast();
        return j != n;
    }

    ////////////////////////////////////////////////////////////////////////////
    // List Interface.
    //

    @Override
    @Realtime(limit = LOG_N)
    public abstract void add(int index, @Nullable E element);

    @Override
    @Realtime(limit = N_LOG_N)
    public boolean addAll(int index, Collection<? extends E> that) {
        return subTable(index, index).addAll(that);
    }

    @Override
    @Realtime(limit = LOG_N)
    public abstract @Nullable E remove(int index);

    @Override
    @Realtime(limit = CONSTANT)
    public abstract @Nullable E get(int index);

    @Override
    @Realtime(limit = CONSTANT)
    public abstract @Nullable E set(int index, @Nullable E element);

    /**
     *  Returns the index of the first occurrence of the specified object in this table, or -1 if this table does not
     *  contain the element. This methods uses this table {@link #equality()} to perform the comparison.
     */
    @Override
    @Realtime(limit = LINEAR)
    public int indexOf(final @Nullable Object searched) {
        FastListIterator<E> itr= listIterator();
        return itr.hasNext(new Predicate<E>() {
            Equality<? super E> equality = equality(); 

            @SuppressWarnings("unchecked")
            @Override
            public boolean test(E param) {
                return equality.areEqual((E)searched, param);
            }}) ? itr.nextIndex() : -1;
    }

    /** 
     * Returns the index of the last occurrence of the specified element in this table, or -1 if this table does 
     * not contain the element. This methods uses this table {@link #equality()} to perform the comparison.
     */
    @Override
    @Realtime(limit = LINEAR)
    public int lastIndexOf(final @Nullable Object searched) {
        FastListIterator<E> itr= listIterator(size());
        return itr.hasPrevious(new Predicate<E>() {
            Equality<? super E> equality = equality(); 

            @SuppressWarnings("unchecked")
            @Override
            public boolean test(E param) {
                return equality.areEqual((E)searched, param);
            }}) ? itr.previousIndex() : -1;
    }

    @Override
    @Realtime(limit = LINEAR, comment="A copy/clone of this table may have to be performed (e.g. shared() views)")
    public FastIterator<E> iterator() {
        return listIterator(0);
    }

    @Override
    @Realtime(limit = LINEAR, comment="A copy/clone of this table may have to be performed (e.g. shared() views)")
    public FastListIterator<E> listIterator() {
        return listIterator(0);
    }

    @Override
    @Realtime(limit = LINEAR, comment="A copy/clone of this table may have to be performed (e.g. shared() views)")
    public abstract FastListIterator<E> listIterator(int index);

    /**
     * Compares the specified object with this table for equality.
     *  
     * Although this table implements the {@link List} interface, it obeys the list general contract only for list 
     * having the same {@link #equality() equality} comparator.
     * 
     * @param obj the object to be compared for equality with this table.
     * @return {@code true} only if the specified object is a list containing the same elements in the same order.
     */
    @Override
    @Realtime(limit = LINEAR)
    public boolean equals(@Nullable Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof List)) return false;
        @SuppressWarnings("unchecked")
        List<E> list = (List<E>) obj;
        if (size() != list.size()) return false; // Short-cut (FastTable.size() in O(1))
        Iterator<E> it1 = this.iterator();
        Iterator<E> it2 = list.iterator();
        Equality<? super E> cmp = equality();
        while (it1.hasNext()) {
            if (!it2.hasNext()) return false;
            if (!cmp.areEqual(it1.next(), it2.next())) return false;
        }
        if (it2.hasNext()) return false;
        return true;
    }

    @Override
    @Realtime(limit = LINEAR)
    public int hashCode() {
        Equality<? super E> cmp = equality();
        if (!(cmp instanceof Order)) return 0; // Rare: Most equalities are instance of Order (e.g. Equality.DEFAULT).
        Order<? super E> order = (Order<? super E>)cmp; 
        int hash = 0;
        for (E e : this) 
            hash += 31 * hash + ((e != null) ? order.indexOf(e) : 0);
        return hash;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Deque Interface.
    //

    @Override
    @Realtime(limit = CONSTANT)
    public void addFirst(@Nullable E element) {
        add(0, element);
    }

    @Override
    @Realtime(limit = CONSTANT)
    public void addLast(@Nullable E element) {
        add(element);
    }

    @Override
    @Realtime(limit = CONSTANT)
    public @Nullable E getFirst() {
        if (isEmpty()) throw new NoSuchElementException();
        return get(0);
    }

    @Override
    @Realtime(limit = CONSTANT)
    public @Nullable E getLast() {
        if (isEmpty()) throw new NoSuchElementException();
        return get(size() - 1);
    }

    @Override
    @Realtime(limit = CONSTANT)
    public @Nullable E peekFirst() {
        return (isEmpty()) ? null : getFirst();
    }

    @Override
    @Realtime(limit = CONSTANT)
    public @Nullable E peekLast() {
        return isEmpty() ? null : getLast();
    }

    @Override
    @Realtime(limit = CONSTANT)
    public @Nullable E pollFirst() {
        return isEmpty() ? null : removeFirst();
    }

    @Override
    @Realtime(limit = CONSTANT)
    public @Nullable E pollLast() {
        return isEmpty() ? null : removeLast();
    }

    @Override
    @Realtime(limit = CONSTANT)
    public @Nullable E removeFirst() {
        if (isEmpty()) throw new NoSuchElementException();
        return remove(0);
    }

    @Override
    @Realtime(limit = CONSTANT)
    public @Nullable E removeLast() {
        if (isEmpty()) throw new NoSuchElementException();
        return remove(size() - 1);
    }

    @Override
    @Realtime(limit = CONSTANT)
    public boolean offerFirst(@Nullable E e) {
        addFirst(e);
        return true;
    }

    @Override
    @Realtime(limit = CONSTANT)
    public boolean offerLast(@Nullable E e) {
        addLast(e);
        return true;
    }

    @Override
    @Realtime(limit = LINEAR)
    public boolean removeFirstOccurrence(@Nullable Object o) {
        int i = indexOf(o);
        if (i < 0) return false;
        remove(i);
        return true;
    }

    @Override
    @Realtime(limit = LINEAR)
    public boolean removeLastOccurrence(@Nullable Object o) {
        int i = lastIndexOf(o);
        if (i < 0) return false;
        remove(i);
        return true;
    }

    @Override
    @Realtime(limit = CONSTANT)
    public boolean offer(@Nullable E e) {
        return offerLast(e);
    }

    @Override
    @Realtime(limit = CONSTANT)
    public @Nullable E remove() {
        return removeFirst();
    }

    @Override
    @Realtime(limit = CONSTANT)
    public @Nullable E poll() {
        return pollFirst();
    }

    @Override
    @Realtime(limit = CONSTANT)
    public @Nullable E element() {
        return getFirst();
    }

    @Override
    @Realtime(limit = CONSTANT)
    public @Nullable E peek() {
        return peekFirst();
    }

    @Override
    @Realtime(limit = CONSTANT)
    public void push(@Nullable E e) {
        addFirst(e);
    }

    @Override
    @Realtime(limit = CONSTANT)
    public @Nullable E pop() {
        return removeFirst();
    }

    @Override
    @Realtime(limit = LINEAR, comment="A copy/clone of this table may have to be performed (e.g. shared() views)")
    public FastIterator<E> descendingIterator() {
        return new FastIterator<E>() {
            FastListIterator<E> itr = listIterator(size());

            @Override
            public boolean hasNext() {
                return itr.hasPrevious();
            }

            @Override
            public boolean hasNext(Predicate<? super E> matching) {
                return itr.hasPrevious(matching);
            }

            @Override
            public E next() {
                return itr.previous();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }};
    }

    ////////////////////////////////////////////////////////////////////////////
    // Misc.
    //

    @Override
    @Realtime(limit = LINEAR)
    public AbstractTable<E> clone() {
        return (AbstractTable<E>) super.clone();
    }

    /**
     * Sorts this table in place (quick sort).
     */
    @Realtime(limit = N_SQUARE)
    public void sort(Comparator<? super E> cmp) {
        QuickSortImpl<E> qs = new QuickSortImpl<E>(this, cmp);
        qs.sort();
    }

    @Override
    @SuppressWarnings("unchecked")
    @Realtime(limit = CONSTANT)
    public AbstractTable<E>[] trySplit(int n) {
        AbstractTable<E>[] split = new AbstractTable[n];
        for (int i = 0, from = 0, size = size(), incr = size / n, rem = size % n; i < n; i++) {
            int to = from + incr;
            if (rem-- > 0) to++;
            split[i] = new SubTableImpl<E>(this, from, to).unmodifiable();
            from = to;
        }
        return split;
    }  
   
  
    /**
     * Replaced by {@link #subTable(int, int)}.
     */
    @Override
    @Deprecated
    public AbstractTable<E> subList(int fromIndex, int toIndex) {
        return subTable(fromIndex, toIndex);
    }

}