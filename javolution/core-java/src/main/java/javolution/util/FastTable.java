/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util;

import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.RandomAccess;

import javolution.annotation.RealTime;
import javolution.internal.util.table.FractalTableImpl;
import javolution.internal.util.table.QuickSort;
import javolution.internal.util.table.ReversedTableImpl;
import javolution.internal.util.table.SharedTableImpl;
import javolution.internal.util.table.SubTableImpl;
import javolution.internal.util.table.TableIteratorImpl;
import javolution.internal.util.table.UnmodifiableTableImpl;
import javolution.util.service.ComparatorService;
import javolution.util.service.TableService;

/**
 * <p> A high-performance table (fractal-based) with {@link RealTime real-time} 
 *     behavior (insertion/deletion in <i><b>O(Log(size))</b></i>; smooth capacity 
 *     increase/decrease and minimal memory footprint.</p>
 *     
 * <p> The fractal-based implementation ensures that basic operations 
 *     <b>worst</b> execution time is always in <i><b>O(log(size))</b></i> even 
 *     for arbitrary insertions or deletions. The capacity of a fast table 
 *     is automatically adjusted to best fit its size (e.g. when a table is cleared
 *     its memory footprint is minimal).</p>
 *
 * <p> Instances of this class can advantageously replace {@link java.util.ArrayList ArrayList},
 *     {@link java.util.LinkedList LinkedList} or {@link java.util.ArrayDeque ArrayDeque}
 *     in terms of adaptability, space or performance.
 *     Null elements are supported and fast tables can be concurrently iterated over using
 *     their {@link #shared() shared} views. Fast table inherits from all the fast collection
 *     views and also support the new {@link #subList subList} view over a portion of the table.</li>
 * [code]
 * FastTable<String> names = ...;
 * names.sort(Comparators.LEXICAL_CASE_INSENSITIVE); // Actually sorts the names (different from sorted() which is a sorted view).
 * names.subList(0, names.size() / 2).clear(); // Removes the first half of the table.
 * names.filtered(str -> str.startsWith("A")).clear(); // Removes all the names starting with "A" (Java 8 notation).
 * names.reversed().addLast("Paul Auchon"); // Actually adds first to names.
 * [/code]
 * </p>
 *
 * <p> As for any {@link FastCollection fast collection}, iterations are faster
 *     when performed using closures (and the notation is shorter with Java 8).
 *     This is also the preferred mean of iterating over {@link FastTable#shared shared}
 *     tables since <code>ConcurrentModificationException</code> cannot occur. 
 * [code]
 * FastTable<Person> persons = new FastTable<Person>().shared(); // Thread-safe table.
 * ...
 * Person findWithName(final String name) { 
 *     return persons.filtered(new Predicate<Person>() { 
 *         public boolean test(Person person) {
 *             return (person.getName().equals(name));
 *         }
 *     }).reduce(Operators.ANY);
 * }
 * [/code]
 * The code above can be simplified using Java 8.
 * [code]
 * Person findWithName(String name) {
 *     return persons.filtered(person -> person.getName().equals(name)).reduce(Operators.ANY);
 * }
 * [/code]
 * </p>
 *  <p> The iteration order over the default fast table is the {@link #add insertion} order; 
 *      specialization may have a different order, for example the {@link FastSortedTable} has 
 *      an iteration order based on the element order (and consequently faster 
 *      {@link #contains}, {@link #indexOf} and {@link #remove} methods).</p> 
 *
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0.0, December 12, 2012
 */
public class FastTable<E> extends FastCollection<E> implements List<E>,
        Deque<E>, RandomAccess {

    private static final long serialVersionUID = 8176661943244396559L;
 
    /**
     * Actual service implementation.
     */
    private final TableService<E> impl;
    
    /**
     * Creates an empty table whose capacity increments/decrements smoothly
     * without large resize operations to best fit the table current size.
     */
    public FastTable() {
        impl = new FractalTableImpl<E>();
    }

    /**
     * Creates a table having the specified initial elements.
     */
    public FastTable(E... elements) {
        this();     
        for (E e : elements) {
            add(e);
        }
    }

    /**
     * Creates a fast table backed up by the specified service implementation.
     */
    public FastTable(TableService<E> service) {
        this.impl = service;
    }
    
    @Override
    public TableService<E> service() {
        return impl;
    }

    /***************************************************************************
     * Table views.
     */

    @Override
    public FastTable<E> unmodifiable() {
        return new FastTable<E>(new UnmodifiableTableImpl<E>(impl));
    }

    @Override
    public FastTable<E> shared() {
        return new FastTable<E>(new SharedTableImpl<E>(impl));
    }

    @Override
    public FastTable<E> reversed() {
        return new FastTable<E>(new ReversedTableImpl<E>(impl));
    }

    @Override
    public FastTable<E> subList(int fromIndex, int toIndex) {
        if ((fromIndex < 0) || (toIndex > size()) || (fromIndex > toIndex))
            throw new IndexOutOfBoundsException(); // As per List.subList contract.
        return new FastTable<E>(
                new SubTableImpl<E>(impl, fromIndex, toIndex));
    }


    /***************************************************************************
     * Collection operations.
     */
    
    // Methods calling directly the service implementation are marked final
    // (overriding should be done through service specialization).
    
    @Override
    public final int size() {
        return impl.size();
    }
    
    @Override
    public final void clear() {
        impl.clear();
    }
 
    /***************************************************************************
     * List operations.
     */

    @Override
    public final void add(int index, E element) {
        impl.add(index, element);
    }

    @Override
    public boolean addAll(final int index, Collection<? extends E> elements) {
        return subList(index, index).addAll(elements);
    }

    @Override
    public final E remove(int index) {
        return impl.remove(index);
    }

    @Override
    public final E get(int index) {
        return impl.get(index);
    }

    @Override
    public final E set(int index, E element) {
        return impl.set(index, element);
    }

    @SuppressWarnings("unchecked")
    @Override
    public int indexOf(final Object element) {
        final int[] index = new int[] { -1 };
        atomicRead(new Runnable() { // Prevents concurrent writes when table shared.
            ComparatorService<? super E> cmp = impl.comparator();
            @Override
            public void run() {
                for (int i=0, n = size(); i < n; i++) {
                    if (cmp.areEqual((E) element, impl.get(i))) {
                        index[0] = i;
                        break;
                    }
                }
            }
        });
        return index[0];
    }

    @Override
    public int lastIndexOf(final Object element) {
        final int[] index = new int[1];
        atomicRead(new Runnable() { // Prevents concurrent writes when table shared.
            @Override
            public void run() {
                index[0] = size() - 1 - reversed().indexOf(element); 
            }
        });
        return index[0];
    }

    @Override
    public Iterator<E> iterator() {
        return listIterator();
    }

    @Override
    public ListIterator<E> listIterator() {
        return new TableIteratorImpl<E>(impl, 0);
    }

    @Override
    public ListIterator<E> listIterator(int index) {
        if ((index < 0) || (index > size()))
            throw new IndexOutOfBoundsException("index: " + index + ", size: "
                    + size());
        return new TableIteratorImpl<E>(impl, index);
    }

    /***************************************************************************
     * Deque operations (atomic when the table is shared).
     */

    @Override
    public final E getFirst() {
        return impl.getFirst();
    }

    @Override
    public final E getLast() {
        return impl.getLast();
    }

    @Override
    public final void addFirst(E element) {
        impl.addFirst(element);
    }

    @Override
    public final void addLast(E element) {
        impl.addLast(element);
    }

    @Override
    public final E removeFirst() {
        return impl.removeFirst();
    }

    @Override
    public final E removeLast() {
        return impl.removeLast();
    }

    @Override
    public final E pollFirst() {
        return impl.pollFirst();
    }

    @Override
    public final E pollLast() {
        return impl.pollLast();
    }

    @Override
    public final E peekFirst() {
        return impl.peekFirst();
    }

    @Override
    public final E peekLast() {
        return impl.peekLast();
    }
    
    @Override
    public boolean offerFirst(E e) {
        addFirst(e);
        return true;
    }

    @Override
    public boolean offerLast(E e) {
        addLast(e);
        return true;
    }

    @Override
    public boolean removeFirstOccurrence(Object obj) {
        return remove(obj);
    }

    @Override
    public boolean removeLastOccurrence(Object obj) {
        return reversed().removeFirstOccurrence(obj);
    }

    @Override
    public boolean offer(E e) {
        return offerLast(e);
    }

    @Override
    public E remove() {
        return removeFirst();
    }

    @Override
    public E poll() {
        return pollFirst();
    }

    @Override
    public E element() {
        return getFirst();
    }

    @Override
    public E peek() {
        return peekFirst();
    }

    @Override
    public void push(E e) {
        addFirst(e);
    }

    @Override
    public E pop() {
        return removeFirst();
    }

    @Override
    public Iterator<E> descendingIterator() {
        return reversed().iterator();
    }

    /***************************************************************************
     * Misc.
     */

    /**
     * Sorts this table in place (quick sort).
     */
    public void sort() {
        atomicWrite(new Runnable() {
            @Override
            public void run() {
                QuickSort<E> qs = new QuickSort<E>(impl, impl.comparator());
                qs.sort();              
            }
        });
    }

}