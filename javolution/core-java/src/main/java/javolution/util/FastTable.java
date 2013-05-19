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
 * <p> A random access collection of ordered/unordered elements with fast
 * insertion/deletion and smooth (time bounded) capacity increase/decrease.
 * The default implementation (fractal based) ensures that basic operations 
 * <b>worst</b> execution time is in <i><b>O(log(size))</b></i> even 
 * for arbitrary insertions or deletions. The capacity of a fast table 
 * is automatically adjusted to best fit its size (e.g. when a table is cleared
 * its memory footprint is minimal).</p>
 * <img src="doc-files/list-add.png"/>
 *
 * <p> Instances of this class can advantageously replace {@link java.util.ArrayList ArrayList},
 * {@link java.util.LinkedList LinkedList}, {@link java.util.ArrayDeque ArrayDeque}
 * and even {@link java.util.TreeSet TreeSet} in terms of adaptability, space or performance.
 * Null elements are supported and fast tables can be concurrently accessed using
 * their {@link #shared() shared} views. On top of all the views inherited from FastCollection 
 * (such as {@link #unmodifiable unmodifiable}, {@link #shared}, {@link FastCollection#filtered filtered},
 * {@link FastCollection#mapped mapped} or {@link FastCollection#noDuplicate differeninoDuplicate}), fast tables
 * support the following specific views:
 * <ol>
 *    <li>{@link #subList} - View on a portion of the table.</li>
 *    <li>{@link #sorted} - View keeping its elements sorted (sorted tables have faster 
 *        {@link #indexOf indexOf}, {@link #contains contains} and {@link #remove(java.lang.Object) remove} methods).
 *    .</li>
 *    <li>{@link #reverse} - View for which elements are in the reverse order.</li>
 * </ol>
 * Here are few examples of chaining using table views:
 * [code]
 * FastTable<Session> sessions = new FastTable<Session>().shared(); // Table which can be concurrently accessed/modified.
 * FastTable<Item> items = new FastTable<Item>().sorted().noDuplicate(); // Equivalent to FastSortedSet except it is a list!
 * FastTable<CharSequence> names = new FastTable<CharSequence>.setComparator(FastComparator.LEXICAL); // Use lexical comparator for object equality/comparison.
 * ...
 * names.reverse().sort(); // Sorts the names in reverse alphabetical order.
 * names.shared().subList(0, mames.size() / 2); // Provides a view over the first half of a shared table.
 * names.shared().subList(mames.size() / 2, names.size()); // Provides a view over the second half of a shared table.
 * names.subList(start, end).shared(); // Provides a concurrently modifiable view over a part of a table (which is different from the above).
 * [/code]</p>
 *
 * <p> As for any {@link FastCollection fast collection}, iterations are faster
 * when performed using closures (and the notation is shorter with Java 8).
 * This is also the preferred mean of iterating over {@link FastTable#shared shared}
 * tables since <code>ConcurrentModificationException</code> cannot occur ! 
 * [code]
 * FastTable<Person> persons = new FastTable<Person>().shared();
 * ...
 * Person findWithName(final String name) { // Thread-safe even if persons concurrently modified.
 *     return persons.filtered(new Predicate<Person>() { 
 *         public boolean test(Person person) {
 *             return (person.getName().equals(name));
 *         }
 *     }).peek();
 * }
 * [/code]
 * The code above can be simplified using Java 8.
 * [code]
 * Person findWithName(final String name) { // Thread-safe even if persons concurrently modified.
 *     return persons.filtered(person -> person.getName().equals(name)).peek();
 * }
 * [/code]
 * </p>
 *
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0.0, December 12, 2012
 */
public class FastTable<E> extends FastCollection<E> implements List<E>,
        Deque<E>, RandomAccess {

    /**
     * Holds the actual table service implementation.
     */
    private final TableService<E> service;

    /**
     * Creates an empty table whose capacity increments/decrements smoothly
     * without large resize operations to best fit the table current size.
     */
    public FastTable() {
        service = new FractalTableImpl<E>();
    }

    /**
     * Creates a table backed up by the specified implementation.
     */
    protected FastTable(TableService<E> service) {
        this.service = service;
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

    /**
     * Sorts this table and returns a view that keeps the table sorted. 
     * Having a sorted table improved significantly the
     * performance of the methods {@link #indexOf(java.lang.Object) indexOf},
     * {@link #contains contains} and {@link #remove(java.lang.Object) remove}.
     *
     * @throws UnsupportedOperationException if this table is not empty.
     */
    public FastTable<E> sort() {
        service.sort();
        return new FastTable<E>(new SortedTableImpl<E>(service));
    }

    /**
     * Returns a view for which the elements are in reverse order.
     * Iterating on this view (iterator or closure) will start from the
     * last element and finish by the first.</p>
     */
    public FastTable<E> reverse() {
        return new FastTable<E>(new ReversedTableImpl<E>(service));
    }

    /**
     * <p> Returns a view that keeps this table (initially empty) without 
     *     duplicate; elements are not added if already present.</p>
     * 
     * @throws UnsupportedOperationException if this table is not empty.
     */
    public FastTable<E> noDuplicate() {
        if (!isEmpty())
            throw new UnsupportedOperationException(
                    "No duplicate view requires the tables to be initially empty");
        return new FastTable<E>(new NoDuplicateTableImpl<E>(service));
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
     * Sorts this table in place (quick sort). 
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

    @Override
    protected TableService<E> getService() {
        return service;
    }
    
    private static final long serialVersionUID = 9153496416654421848L;


}