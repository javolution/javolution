/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util;

import static javolution.lang.RealTime.Limit.CONSTANT;
import static javolution.lang.RealTime.Limit.LINEAR;
import static javolution.lang.RealTime.Limit.LOG_N;
import static javolution.lang.RealTime.Limit.N_LOG_N;
import static javolution.lang.RealTime.Limit.N_SQUARE;

import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.RandomAccess;

import javolution.internal.util.table.FastTableImpl;
import javolution.internal.util.table.QuickSort;
import javolution.internal.util.table.ReversedTableImpl;
import javolution.internal.util.table.SharedTableImpl;
import javolution.internal.util.table.SubTableImpl;
import javolution.internal.util.table.TableIteratorImpl;
import javolution.internal.util.table.UnmodifiableTableImpl;
import javolution.lang.RealTime;
import javolution.util.function.Comparators;
import javolution.util.function.EqualityComparator;
import javolution.util.function.Predicate;
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
 *     {@code null} elements are supported and fast tables can be concurrently iterated over using
 *     their {@link #shared() shared} views. Fast tables inherit from all the fast collection
 *     views and also support the new {@link #subList subList} view over a portion of the table.</li>
 * [code]
 * FastTable<String> names = ...;
 * names.sort(Comparators.LEXICAL_CASE_INSENSITIVE); // Actually sorts the names (different from sorted() which is a sorted view).
 * names.subList(0, names.size() / 2).clear(); // Removes the first half of the table.
 * names.filtered(str -> str.startsWith("A")).clear(); // Removes all the names starting with "A" (Java 8 notation).
 * names.parallel().filtered(str -> str.startsWith("A")).clear(); // Same as above but performed concurrently.
 * [/code]
 * </p>
 *
 * <p> As for any {@link FastCollection fast collection}, iterations are faster
 *     when performed using closures (and the notation is shorter with Java 8).
 *     This is also the preferred mean of iterating over {@link FastTable#shared shared}
 *     tables since {@link ConcurrentModificationException} cannot occur. 
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
 *  <p> FastTable iteration order is the {@link #add insertion} order; specialization may 
 *      have a different order, for example the iteration order of {@link FastSortedTable} 
 *      is based on the table element order.</p> 
 *
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0.0, December 12, 2012
 */
public class FastTable<E> extends FastCollection<E> implements List<E>,
        Deque<E>, RandomAccess {

    private static final long serialVersionUID = 0x600L; // Version.

    /**
     * Holds the actual service implementation.
     */
    private final TableService<E> service;

    /**
     * Creates an empty table whose capacity increments/decrements smoothly
     * without large resize operations to best fit the table current size.
     */
    public FastTable() {
        this(Comparators.STANDARD);
    }

    /**
     * Creates an empty table using the specified comparator for element 
     * equality.
    */
    public FastTable(EqualityComparator<? super E> comparator) {
        service = new FastTableImpl<E>(comparator);
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
    protected FastTable(TableService<E> service) {
        this.service = service;
    }

    /***************************************************************************
    * Views.
    */

    @Override
    public FastTable<E> unmodifiable() {
        return new FastTable<E>(new UnmodifiableTableImpl<E>(service));
    }

    @Override
    public FastTable<E> shared() {
        return new FastTable<E>(new SharedTableImpl<E>(service));
    }

    @Override
    public FastTable<E> reversed() {
        return new FastTable<E>(new ReversedTableImpl<E>(service));
    }

    @Override
    public FastTable<E> subList(int fromIndex, int toIndex) {
        if ((fromIndex < 0) || (toIndex > size()) || (fromIndex > toIndex))
            throw new IndexOutOfBoundsException(); // As per List.subList contract.
        return new FastTable<E>(
                new SubTableImpl<E>(service, fromIndex, toIndex));
    }

    /***************************************************************************
     * Collection operations.
     */

    @Override
    @RealTime(limit = CONSTANT)
    public int size() {
        return service.size();
    }

    @Override
    @RealTime(limit = CONSTANT)
    public void clear() {
        service.clear();
    }

    /***************************************************************************
     * List operations.
     */

    @Override
    @RealTime(limit = LOG_N)
    public void add(int index, E element) {
        service.add(index, element);
    }

    @Override
    @RealTime(limit = N_LOG_N)
    public boolean addAll(final int index, Collection<? extends E> elements) {
        return subList(index, index).addAll(elements);
    }

    @Override
    @RealTime(limit = LOG_N)
    public E remove(int index) {
        return service.remove(index);
    }

    @Override
    public E get(int index) {
        return service.get(index);
    }

    @Override
    public E set(int index, E element) {
        return service.set(index, element);
    }

    @Override
    @RealTime(limit = LINEAR)
    public int indexOf(final Object element) {
        final EqualityComparator<? super E> cmp = comparator();
        final int[] count = new int[] { -1 };
        boolean notFound = sequential().doWhile(new Predicate<E>() {

            @Override
            @SuppressWarnings("unchecked")
            public boolean test(E param) {
                count[0]++;
                return !cmp.areEqual((E) element, param);
            }
        });
        return notFound ? -1 : count[0];
    }

    @Override
    @RealTime(limit = LINEAR)
    public int lastIndexOf(final Object element) {
        final EqualityComparator<? super E> cmp = comparator();
        final int[] count = new int[] { -1 };
        boolean notFound = reversed().sequential().doWhile(new Predicate<E>() {

            @Override
            @SuppressWarnings("unchecked")
            public boolean test(E param) {
                if (count[0] < 0)
                    count[0] = size();
                count[0]--;
                return !cmp.areEqual((E) element, param);
            }
        });
        return notFound ? -1 : count[0];
    }

    @Override
    public Iterator<E> iterator() {
        return listIterator();
    }

    @Override
    public ListIterator<E> listIterator() {
        return new TableIteratorImpl<E>(service, 0);
    }

    @Override
    public ListIterator<E> listIterator(int index) {
        if ((index < 0) || (index > size()))
            throw new IndexOutOfBoundsException("index: " + index + ", size: "
                    + size());
        return new TableIteratorImpl<E>(service, index);
    }

    /***************************************************************************
     * Deque operations (atomic when the table is shared).
     */

    @Override
    public E getFirst() {
        return service.getFirst();
    }

    @Override
    public E getLast() {
        return service.getLast();
    }

    @Override
    public void addFirst(E element) {
        service.addFirst(element);
    }

    @Override
    public void addLast(E element) {
        service.addLast(element);
    }

    @Override
    public E removeFirst() {
        return service.removeFirst();
    }

    @Override
    public E removeLast() {
        return service.removeLast();
    }

    @Override
    public E pollFirst() {
        return service.pollFirst();
    }

    @Override
    public E pollLast() {
        return service.pollLast();
    }

    @Override
    public E peekFirst() {
        return service.peekFirst();
    }

    @Override
    public E peekLast() {
        return service.peekLast();
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
    @RealTime(limit = N_SQUARE)
    public void sort() {
        atomic(new Runnable() {
            @Override
            public void run() {
                QuickSort<E> qs = new QuickSort<E>(service,
                        service.comparator());
                qs.sort();
            }
        });
    }

    @Override
    protected TableService<E> service() {
        return service;
    }

}