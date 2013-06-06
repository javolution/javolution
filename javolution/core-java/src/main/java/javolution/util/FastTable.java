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
import java.util.Comparator;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.RandomAccess;

import javolution.annotation.RealTime;
import javolution.internal.util.table.FractalTableImpl;
import javolution.internal.util.table.NoDuplicateTableImpl;
import javolution.internal.util.table.ReversedTableImpl;
import javolution.internal.util.table.SharedTableImpl;
import javolution.internal.util.table.SortedTableImpl;
import javolution.internal.util.table.SubTableImpl;
import javolution.internal.util.table.TableIteratorImpl;
import javolution.internal.util.table.UnmodifiableTableImpl;
import javolution.util.function.Predicate;
import javolution.util.service.TableService;

/**
 * <p> A high-performance table (fractal-based) with {@link RealTime real-time} 
 *     behavior (insertion/deletion in <i><b>O(Log(size))</b></i>; smooth capacity 
 *     increase/decrease and minimal memory footprint.</p>
 * <p> The fractal-based implementation ensures that basic operations 
 *     <b>worst</b> execution time is always in <i><b>O(log(size))</b></i> even 
 *     for arbitrary insertions or deletions. The capacity of a fast table 
 *     is automatically adjusted to best fit its size (e.g. when a table is cleared
 *     its memory footprint is minimal).</p>
 *     <img src="doc-files/list-add.png"/>
 *
 * <p> Instances of this class can advantageously replace {@link java.util.ArrayList ArrayList},
 *     {@link java.util.LinkedList LinkedList} or {@link java.util.ArrayDeque ArrayDeque}
 *      in terms of adaptability, space or performance.
 *     Null elements are supported and fast tables can be concurrently iterated over using
 *     their {@link #shared() shared} views. Fast table inherits from all the fast collection
 *     views and also support the {@link #subList subList} view on a portion of the table.</li>
 * [code]
 * FastTable<String> names = ...;
 * names.sort(Comparators.LEXICAL_CASE_INSENSITIVE); // Sorts the names ignoring case.
 * names.subList(0, names.size() / 2).clear(); // Removes the first half of the table.
 * names.filter(str -> str.startsWith("A")).clear(); // Removes all the names starting with "A" (Java 8 notation).
 * [/code]
 * </p>
 *
 * <p> As for any {@link FastCollection fast collection}, iterations are faster
 *     when performed using closures (and the notation is shorter with Java 8).
 *     This is also the preferred mean of iterating over {@link FastTable#shared shared}
 *     tables since <code>ConcurrentModificationException</code> cannot occur ! 
 * [code]
 * FastTable<Person> persons = new FastTable<Person>().shared(); // Thread-safe table.
 * ...
 * Person findWithName(final String name) { 
 *     return persons.filter(new Predicate<Person>() { 
 *         public boolean test(Person person) {
 *             return (person.getName().equals(name));
 *         }
 *     }).reduce(Operators.ANY);
 * }
 * [/code]
 * The code above can be simplified using Java 8.
 * [code]
 * Person findWithName(String name) {
 *     return persons.filter(person -> person.getName().equals(name)).reduce(Operators.ANY);
 * }
 * [/code]
 * </p>
 *  <p> The iteration order over a basic table is the {@link #add insertion} order; 
 *      specialization may have a different order, for example the {@link FastSortedTable} has 
 *      an iteration order based on element order (and faster {@link #contains},
 *      {@link #remove} methods).</p> 
 *
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0.0, December 12, 2012
 */
public class FastTable<E> extends FastCollection<E> implements List<E>,
        Deque<E>, RandomAccess {

      /**
     * Creates an empty table whose capacity increments/decrements smoothly
     * without large resize operations to best fit the table current size.
     */
    public FastTable() {
        super(new FractalTableImpl<E>());
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

    /***************************************************************************
     * Table views.
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
    public FastTable<E> usingComparator(Comparator<? super E> comparator) {
        return null;
    }

    @Override
    public FastTable<E> subList(int fromIndex, int toIndex) {
        if ((fromIndex < 0) || (toIndex > size()) || (fromIndex > toIndex))
            throw new IndexOutOfBoundsException(); // As per List.subList contract.
        return new FastTable<E>(
                new SubTableImpl<E>(service, fromIndex, toIndex));
    }

    /***************************************************************************
     * List operations.
     */

    @Override
    public void add(int index, E element) {
        service.add(index, element);
    }

    @Override
    public boolean addAll(final int index, Collection<? extends E> elements) {
        return subList(index, index).addAll(elements);
    }

    @Override
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

    @SuppressWarnings("unchecked")
    @Override
    public int indexOf(Object element) {
        return service.indexOf((E) element);
    }

    @SuppressWarnings("unchecked")
    @Override
    public final int lastIndexOf(Object element) {
        return service.lastIndexOf((E) element);
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
     * Deque operations.
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
        return reverse().remove(obj);
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
        return reverse().iterator();
    }

    /***************************************************************************
     * Misc.
     */

    /**
     * Sorts this table in place (quick sort). Sorts uses the table 
     * comparator.
     * 
     * @see #usingComparator(java.util.Comparator)
     */
    public void sort() {
        service.sort();
    }

    /**
     * Removes the elements in range <code>[fromIndex..toIndex[</code> from
     * this table.
     *
     * @param fromIndex the beginning index, inclusive.
     * @param toIndex   the ending index, exclusive.
     * @throws IndexOutOfBoundsException if <code>(fromIndex &lt; 0) || 
     *         (toIndex &lt; 0) || (fromIndex &gt; toIndex) || (toIndex &gt; size())</code>
     */
    public void removeRange(int fromIndex, int toIndex) {
        subList(fromIndex, toIndex).clear();
    }
  
    /***************************************************************************
     * For sub-classes.
     */

    /**
     * Creates a fast table backed up by the specified implementation.
     */
    protected FastTable(TableService<E> service) {
        super(service);
    }
    
    @Override
    protected TableService<E> getService() {
        return (TableService<E>) super.getService();
    }
    
    private static final long serialVersionUID = 9153496416654421848L;


}