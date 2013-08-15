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
import static javolution.lang.Realtime.Limit.LOG_N;
import static javolution.lang.Realtime.Limit.N_LOG_N;
import static javolution.lang.Realtime.Limit.N_SQUARE;

import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.RandomAccess;

import javolution.lang.Realtime;
import javolution.util.function.Consumer;
import javolution.util.function.Equalities;
import javolution.util.function.Equality;
import javolution.util.internal.table.AtomicTableImpl;
import javolution.util.internal.table.FastTableImpl;
import javolution.util.internal.table.QuickSort;
import javolution.util.internal.table.ReversedTableImpl;
import javolution.util.internal.table.SharedTableImpl;
import javolution.util.internal.table.SubTableImpl;
import javolution.util.internal.table.UnmodifiableTableImpl;
import javolution.util.service.TableService;

/**
 * <p> A high-performance table (fractal-based) with {@link Realtime real-time} 
 *     behavior; smooth capacity increase/decrease and minimal memory footprint.</p>
 *     
 * <p> The fractal-based implementation ensures that add/insertion/deletion operations 
 *     <b>worst</b> execution time is always in less than <i><b>O(log(size))</b></i> (for  
 *     comparison {@code ArrayList.add} is in <i><b>O(size)</b></i> due to resize).
 *     The capacity of a fast table is automatically adjusted to best fit its size 
 *     (e.g. when a table is cleared its memory footprint is minimal).</p>
 *
 * <p> Instances of this class can advantageously replace {@link java.util.ArrayList ArrayList},
 *     {@link java.util.LinkedList LinkedList} or {@link java.util.ArrayDeque ArrayDeque}
 *     in terms of adaptability, space or performance.
 *     Fast tables can be concurrently iterated / modified through their {@link #shared() shared}/{@link #atomic() atomic} 
 *     views. They inherit all the fast collection views and support the {@link #subTable subTable} view over a portion of the table.</p>
 * [code]
 * FastTable<String> names = new FastTable<String>().addAll("John Deuff", "Otto Graf", "Sim Kamil");
 * names.sort(Equalities.LEXICAL_CASE_INSENSITIVE); // Sorts the names in place (different from sorted() which returns a sorted view).
 * names.subTable(0, names.size() / 2).clear(); // Removes the first half of the table (see java.util.List.subList specification).
 * names.filtered(str -> str.startsWith("A")).clear(); // Removes all the names starting with "A" (Java 8 notation).
 * names.filtered(str -> str.startsWith("A")).parallel().clear(); // Same as above but performed concurrently and atomically !
 * [/code]
 *
 * <p> As for any {@link FastCollection fast collection}, iterations can be 
 *     performed using closures and the notation is shorter with Java 8.
 * [code]
 * FastTable<Person> persons = ...
 * Person findWithName(final String name) { 
 *     return persons.filtered(new Predicate<Person>() { 
 *         public boolean test(Person person) {
 *             return (person.getName().equals(name));
 *         }
 *     }).any(Person.class);
 * }
 * [/code]</p>
 * <p> The same code using Java 8.
 * [code]
 * Person findWithName(String name) {
 *     return persons.filtered(person -> person.getName().equals(name)).any(Person.class);
 * }
 * [/code]
 * </p>
 *  <p> FastTable iteration order is the {@link #add insertion} order; specialization may 
 *      have a different order, for example the iteration order of {@link FastSortedTable} 
 *      is based on the table sorting order.</p> 
 *
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0, July 21, 2013
 */
public class FastTable<E> extends FastCollection<E> implements List<E>, Deque<E>, RandomAccess {

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
        this(Equalities.STANDARD);
    }

    /**
     * Creates an empty table using the specified comparator for element 
     * equality.
    */
    public FastTable(Equality<? super E> comparator) {
        service = new FastTableImpl<E>(comparator);
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
    public FastTable<E> atomic() {
        return new FastTable<E>(new AtomicTableImpl<E>(service));
    }

    @Override
    public FastTable<E> reversed() {
        return new FastTable<E>(new ReversedTableImpl<E>(service));
    }
    
    @Override
    public FastTable<E> shared() {
        return new FastTable<E>(new SharedTableImpl<E>(service));
    }

    @Override
    public FastTable<E> unmodifiable() {
        return new FastTable<E>(new UnmodifiableTableImpl<E>(service));
    }

    /**
     * Returns a view over a portion of the table (equivalent to 
     * {@link java.util.List#subList(int, int)}).
     */
    public FastTable<E> subTable(int fromIndex, int toIndex) {
        return new FastTable<E>(
                new SubTableImpl<E>(service, fromIndex, toIndex));
    }

    /***************************************************************************
     * Collection operations (here because of the change in annotation).
     */

    @Override
    @Realtime(limit = CONSTANT)
    public boolean isEmpty() {
        return service.isEmpty();
    }

    @Override
    @Realtime(limit = CONSTANT)
    public int size() {
        return service.size();
    }

    @Override
    @Realtime(limit = CONSTANT)
    public void clear() {
        service.clear();
    }

    /***************************************************************************
     * List operations.
     */

    @Override
    @Realtime(limit = LOG_N)
    public void add(int index, E element) {
        service.add(index, element);
    }

    @Override
    @Realtime(limit = N_LOG_N)
    public boolean addAll(final int index, Collection<? extends E> elements) {
        return service.addAll(index, elements);
    }

    @Override
    @Realtime(limit = LOG_N)
    public E remove(int index) {
        return service.remove(index);
    }

    @Override
    @Realtime(limit = CONSTANT)
    public E get(int index) {
        return service.get(index);
    }

    @Override
    @Realtime(limit = CONSTANT)
    public E set(int index, E element) {
        return service.set(index, element);
    }

    @Override
    @Realtime(limit = LINEAR)
    public int indexOf(Object element) {
        return service.indexOf(element);
    }

    @Override
    @Realtime(limit = LINEAR)
    public int lastIndexOf(final Object element) {
        return service.lastIndexOf(element);
    }

    @Override
    @Realtime(limit = CONSTANT)
    public ListIterator<E> listIterator() {
        return service.listIterator();
    }

    @Override
    @Realtime(limit = CONSTANT)
    public ListIterator<E> listIterator(int index) {
        return service.listIterator(index);
    }

    /***************************************************************************
     * Deque operations.
     */

    @Override
    @Realtime(limit = CONSTANT)
    public void addFirst(E element) {
        service.addFirst(element);
    }

    @Override
    @Realtime(limit = CONSTANT)
    public void addLast(E element) {
        service.addLast(element);
    }
    
    @Override
    @Realtime(limit = CONSTANT)
    public E getFirst() {
        return service.getFirst();
    }

    @Override
    @Realtime(limit = CONSTANT)
    public E getLast() {
        return service.getLast();
    }

    @Override
    @Realtime(limit = CONSTANT)
    public E peekFirst() {
        return service.peekFirst();
    }
    
    @Override
    @Realtime(limit = CONSTANT)
    public E peekLast() {
        return service.peekLast();
    }

    @Override
    @Realtime(limit = CONSTANT)
    public E pollFirst() {
        return service.pollFirst();
    }

    @Override
    @Realtime(limit = CONSTANT)
    public E pollLast() {
        return service.pollLast();
    }

    @Override
    @Realtime(limit = CONSTANT)
    public E removeFirst() {
        return service.removeFirst();
    }

    @Override
    @Realtime(limit = CONSTANT)
    public E removeLast() {
        return service.removeLast();
    }

    @Override
    @Realtime(limit = CONSTANT)
    public boolean offerFirst(E e) {
        return service.offerFirst(e);
    }

    @Override
    @Realtime(limit = CONSTANT)
    public boolean offerLast(E e) {
        return service.offerLast(e);
    }

    @Override
    @Realtime(limit = LINEAR)
    public boolean removeFirstOccurrence(Object o) {
        return service.removeFirstOccurrence(o);
    }

    @Override
    @Realtime(limit = LINEAR)
    public boolean removeLastOccurrence(Object o) {
        return service.removeLastOccurrence(o);
    }

    @Override
    @Realtime(limit = CONSTANT)
    public boolean offer(E e) {
        return service.offer(e);
    }

    @Override
    @Realtime(limit = CONSTANT)
    public E remove() {
        return service.remove();
    }

    @Override
    @Realtime(limit = CONSTANT)
    public E poll() {
        return service.poll();
    }

    @Override
    @Realtime(limit = CONSTANT)
    public E element() {
        return service.element();
    }

    @Override
    @Realtime(limit = CONSTANT)
    public E peek() {
        return service.peek();
    }

    @Override
    @Realtime(limit = CONSTANT)
    public void push(E e) {
        service.push(e);
    }

    @Override
    @Realtime(limit = CONSTANT)
    public E pop() {
        return service.pop();
    }

    @Override
    @Realtime(limit = CONSTANT)
    public Iterator<E> descendingIterator() {
        return service.descendingIterator();
    }

    /***************************************************************************
     * Misc.
     */

    /**
     * Sorts this table in place (quick sort).
     */
    @Realtime(limit = N_SQUARE)
    public void sort() {
        update(new Consumer<TableService<E>>() {
            @Override
            public void accept(TableService<E> table) {
                QuickSort<E> qs = new QuickSort<E>(table, table.comparator());
                qs.sort();
            }});
    }

    @Override
    @Realtime(limit = LINEAR)
    public FastTable<E> addAll(E... elements) {
        return (FastTable<E>) super.addAll(elements);
    }

    @Override
    @Realtime(limit = LINEAR)
    public FastTable<E> addAll(FastCollection<? extends E> that) {
        return (FastTable<E>) super.addAll(that);
    }

    /**
     * Replaced by  {@link #subTable(int, int)}. The term "List" for an 
     * interface with random access is disturbing !
     */
    @Override
    @Deprecated
    public FastTable<E> subList(int fromIndex, int toIndex) {
        return subTable(fromIndex, toIndex);
    }

    @Override
    protected TableService<E> service() {
        return service;
    }

}