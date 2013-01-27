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
import javolution.internal.util.CustomComparatorTableImpl;
import javolution.internal.util.FastTableImpl;
import javolution.internal.util.NoDuplicateTableImpl;
import javolution.internal.util.ReverseTableImpl;
import javolution.internal.util.SharedTableImpl;
import javolution.internal.util.SortedTableImpl;
import javolution.internal.util.SubTableImpl;
import javolution.internal.util.TableIteratorImpl;
import javolution.internal.util.UnmodifiableTableImpl;
import javolution.lang.Copyable;
import javolution.lang.Functor;
import javolution.lang.Predicate;

/**
 * <p> A random access collection of ordered/unordered elements with fast
 * insertion/deletion and smooth (time bounded) capacity increase/decrease.
 * The implementation (fractal based) ensures that basic operations <b>worst</b> 
 * execution time is in <i><b>O(log(size))</b></i> even for arbitrary insertions 
 * or deletions. Also, the capacity of a fast table is automatically adjusted to
 * best fit its size (no more than 3/4 of the table capacity is ever wasted).</p>
 * <img src="doc-files/list-add.png"/>
 *
 * <p> Instances of this class can advantageously replace {@link java.util.ArrayList ArrayList},
 * {@link java.util.LinkedList LinkedList}, {@link java.util.ArrayDeque ArrayDeque}
 * and even {@link java.util.TreeSet TreeSet} in terms of adaptability, space or performance.
 * Null elements are supported and fast tables can be concurrently accessed using
 * their {@link #shared() shared} views. The following predefined views are provided.
 * <ol>
 *    <li>{@link #subList} - View on a portion of the table.</li>
 *    <li>{@link #unmodifiable} - View which does not allow for the modification of the table.</li>
 *    <li>{@link #shared} - View allowing concurrent modifications (iterations should be performed using closures).</li>
 *    <li>{@link #sorted} - View for which elements are inserted according to their sorting order.</li>
 *    <li>{@link #usingComparator} - View for which elements comparison and sorting use the specified comparator.</li>
 *    <li>{@link #reverse} - View for which elements are in the reverse order.</li>
 *    <li>{@link #noDuplicate} - View for which elements are not added if already present.</li>
 * </ol>
 * Views can be chained, for example:
 * [code]
 * FastTable<Session> sessions = new FastTable().shared(); // Table which can be concurrently accessed/modified.
 * FastTable<Item> items = new FastTable().sorted().noDuplicate(); // Table of sorted items with no duplicate.
 *     // Sorted tables have faster {@link #indexOf indexOf}, {@link #contains contains} and {@link #remove(java.lang.Object) remove} methods.
 * FastTable<String> names ...
 * names.usingComparator(FastComparator.LEXICAL).reverse().sort(); // Sorts the names in reverse alphabetical order.
 * names.shared().subList(0, mames.size() / 2); // Provides a view over the first half of a shared table.
 * names.shared().subList(mames.size() / 2, names.size()); // Provides a view over the second half of a shared table.
 * names.subList(start, end).shared(); // Provides a concurrently modifiable view over a part of a table (which is different from above).
 * [/code]</p>
 *
 * <p> As for any {@link FastCollection fast collection}, iterations are faster
 * when performed using closures (and the notation will be shorter with JDK 8).
 * They are also the preferred mean of iterating over {@link FastTable#shared shared}
 * tables, there are no concurrent modification exception possible! Closure 
 * based iterations over shared tables use local copies of the table to avoid 
 * blocking concurrent writes and being impacted by concurrent modifications. 
 * [code]
 * FastTable<Person> persons = ...
 * Person john = persons.findFirst(new Predicate<Person>() { // Thread-safe if persons is shared.
 *     public Boolean evaluate(Person person) {
 *         return person.getName().equals("John");
 *     }
 * });
 * [/code]</p>
 *
 * <p> Note: Most of this class methods are final, the actual behavior is defined by
 * the table "plugable" implementation (see {@link FastTable#FastTable(AbstractTable)}.</p>
 *
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0.0, December 12, 2012
 */
public class FastTable<E> extends FastCollection<E> implements
        List<E>, Deque<E>, RandomAccess, Copyable<FastTable<E>> {

    /**
     * The actual implementation.
     */
    private final AbstractTable<E> impl;

    /**
     * Creates an empty table whose capacity increments/decrements smoothly
     * without large resize operations to best fit the table current size.
     */
    public FastTable() {
        impl = new FastTableImpl();
    }

    /**
     * Creates a fast table backed up by the specified implementation.
     */
    protected FastTable(AbstractTable<E> impl) {
        this.impl = impl;
    }

    @Override
    public FastTable<E> unmodifiable() {
        return new FastTable(new UnmodifiableTableImpl<E>(impl));
    }

    /**
     * <p> Returns a concurrent read-write view of this collection.</p>
     * <p> Iterators on {@link #shared} collections are deprecated as the may 
     *     raise {@link ConcurrentModificationException} and should be 
     *     replaced by closure-based iterations (e.g. {@link #doWhile doWhile}).
     *     Closure-based iterations use local snapshot copies of the table 
     *     to avoid being impacted by concurrent modifications and not to block
     *     concurrent writes while iterating.</p>
     */
    public FastTable<E> shared() {
        return new FastTable(new SharedTableImpl<E>(impl));
    }

    @Override
    public FastTable<E> usingComparator(FastComparator<E> comp) {
        return new FastTable(new CustomComparatorTableImpl<E>(impl, comp));
    }

    /**
     * <p> Returns a view that keeps this table (initially empty) sorted. 
     * Having a sorted table improved significantly the
     * performance of the methods {@link #indexOf(java.lang.Object) indexOf},
     * {@link #contains contains} and {@link #remove(java.lang.Object) remove}.</p>
     * <p> Using this view, inserting elements at specific positions
     * will raise {@link UnsupportedOperationException}.
     *
     * @see #sort()
     * @throws UnsupportedOperationException if this table is not empty.
     */
    public FastTable<E> sorted() {
        if (!isEmpty()) throw new UnsupportedOperationException(
                    "Sorted view requires the table to be initially empty");
        return new FastTable(new SortedTableImpl<E>(impl));
    }

    /**
     * <p> Returns a view for which the elements are in reverse order.
     * Iterating on this view (iterator or closure) will start from the
     * last element and finish by the first.</p>
     */
    public FastTable<E> reverse() {
        return new FastTable(new ReverseTableImpl<E>(impl));
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
        return new FastTable(new NoDuplicateTableImpl<E>(impl));
    }

    /**
     * Returns this table comparator.
     */
    @Override
    public FastComparator<E> comparator() {
        return impl.comparator();
    }

    @Override
    public final <R> FastTable<R> forEach(Functor<E, R> functor) {
        return impl.forEach(functor);
    }

    @Override
    public final void doWhile(Predicate<E> predicate) {
        impl.doWhile(predicate);
    }

    @Override
    public final boolean removeAll(Predicate<E> predicate) {
        return impl.removeAll(predicate);
    }

    @Override
    public ListIterator<E> iterator() {
        return new TableIteratorImpl(impl, 0);
    }

    /**
     * Adds the specified element to this table; although the default 
     * implementation appends the element to the end it is not forced to
     * ({@link #addLast} should be used for that purpose).
     *
     * @param element the element to be added to this table.
     * @return <code>true</code> if the element has been added;
     *         <code>false</code> otherwise.
     */
    @Override
    public final boolean add(E element) {
        return impl.add(element);
    }

    /**
     * Removes the first occurrence in this collection of the specified element.
     *
     * @param element the element to be removed from this collection.
     * @return <code>true</code> if this collection contained the specified
     *         element; <code>false</code> otherwise.
     * @throws UnsupportedOperationException if the collection is not modifiable.
     */
    @Override
    public final boolean remove(Object element) {
        return impl.remove((E) element);
    }

    /**
     * Indicates if this collection contains the specified element.
     *
     * @param element the element whose presence in this collection
     *                is to be tested.
     * @return <code>true</code> if this collection contains the specified
     *         element;<code>false</code> otherwise.
     */
    @Override
    public final boolean contains(Object element) {
        return impl.contains((E) element);
    }

    @Override
    public final void clear() {
        impl.clear();
    }

    @Override
    public final int size() {
        return impl.size();
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
    public final void removeRange(int fromIndex, int toIndex) {
        this.subList(fromIndex, toIndex).clear();
    }

    /**
     * Sorts this table in place (quick sort).
     */
    public final void sort() {
        impl.sort();
    }

    /**
     * Returns a deep copy of this object.
     *
     * @return an object identical to this object but possibly allocated
     *         in a different memory space.
     * @see Copyable
     */
    @Override
    public FastTable<E> copy() {
        return new FastTable(impl.copy());
    }

    //                      //////////////////
    //                      // List Methods //
    //                      //////////////////
    /**
     * Inserts the specified element at the specified position in this table.
     * Shifts the element currently at that position
     * (if any) and any subsequent elements to the right (adds one to their
     * indices).
     *
     * @param index   the index at which the specified element is to be inserted.
     * @param element the element to be inserted.
     * @throws IndexOutOfBoundsException if <code>(index &lt; 0) || (index &gt; size())</code>
     */
    @Override
    public final void add(int index, E element) {
        impl.add(index, element);
    }

    /**
     * Appends all of the elements in the specified collection to the end of
     * this collection, in the order that they are returned by {@link #doWhile}
     * or the collection's iterator (if the specified collection is not
     * a fast collection).
     *
     * @param that collection whose elements are to be added to this collection.
     * @return <code>true</code> if this collection changed as a result of
     *         the call; <code>false</code> otherwise.
     */
    @Override
    public final boolean addAll(final Collection<? extends E> that) {
        return impl.addAll(that);
    }

    /**
     * Inserts all of the elements in the specified collection into this
     * table at the specified position. Shifts the element currently at that
     * position and any subsequent elements to the right
     * (increases their indices).
     *
     * @param index    the index at which to insert first element from the specified
     *                 collection.
     * @param elements the elements to be inserted into this list.
     * @return <code>true</code> if this list changed as a result of the call;
     *         <code>false</code> otherwise.
     * @throws IndexOutOfBoundsException if <code>(index &lt; 0) || (index &gt; size())</code>
     */
    @Override
    public final boolean addAll(final int index, final Collection<? extends E> elements) {
        return impl.addAll(index, elements);
    }

    /**
     * Removes the element at the specified position from this table.
     * Shifts any subsequent elements to the left (subtracts one
     * from their indices). Returns the element that was removed from the
     * table.
     *
     * @param index the index of the element to removed.
     * @return the element previously at the specified position.
     * @throws IndexOutOfBoundsException if <code>(index &lt; 0) || (index &gt;= size())</code>
     */
    @Override
    public final E remove(int index) {
        return impl.remove(index);
    }

    /**
     * Returns the element at the specified index.
     *
     * @param index index of element to return.
     * @return the element at the specified position in this list.
     * @throws IndexOutOfBoundsException if <code>(index &lt; 0) || (index &gt;= size())</code>
     */
    @Override
    public final E get(int index) {
        return impl.get(index);
    }

    /**
     * Replaces the element at the specified position in this table with the
     * specified element.
     *
     * @param index   index of element to replace.
     * @param element element to be stored at the specified position.
     * @return previous element.
     * @throws IndexOutOfBoundsException if <code>(index &lt; 0) || (index &gt;= size())</code>
     */
    @Override
    public final E set(int index, E element) {
        return impl.set(index, element);
    }

    /**
     * Returns the index in this table of the first occurrence of the specified
     * element, or -1 if this table does not contain this element.
     *
     * @param element the element to search for.
     * @return the index in this table of the first occurrence of the specified
     *         element, or -1 if this table does not contain this element.
     */
    @Override
    public final int indexOf(Object element) {
        return impl.indexOf((E) element);
    }

    /**
     * Returns the index in this table of the last occurrence of the specified
     * element, or -1 if this table does not contain this element.
     *
     * @param element the element to search for.
     * @return the index in this table of the last occurrence of the specified
     *         element, or -1 if this table does not contain this element.
     */
    @Override
    public final int lastIndexOf(Object element) {
        return impl.lastIndexOf((E) element);
    }

    /**
     * Returns a list iterator over the elements in this list.
     *
     * @return an iterator over this list values.
     */
    @Override
    public ListIterator<E> listIterator() {
        return listIterator(0);
    }

    /**
     * Returns a list iterator from the specified position.
     *
     * @param index the index of first value to be returned from the
     *              list iterator (by a call to the <code>next</code> method).
     * @return a list iterator of the elements in this table
     *         starting at the specified position in this list.
     * @throws IndexOutOfBoundsException if the index is out of range
     *         <code>(index &lt; 0) || (index &gt; size())[/code]
     */
    @Override
    public ListIterator<E> listIterator(int index) {
        if ((index < 0) || (index > size()))
            throw new IndexOutOfBoundsException("index: " + index + ", size: " + size());
        return new TableIteratorImpl(impl, index);
    }

    /**
     * Returns a view of the portion of this table between the specified
     * indexes.
     *
     * @param fromIndex low endpoint (inclusive) of the subList.
     * @param toIndex   high endpoint (exclusive) of the subList.
     * @return a view of the specified range within this list.
     *
     * @throws IndexOutOfBoundsException if <code>(fromIndex &lt; 0) ||
     *        (toIndex &gt; size || fromIndex &gt; toIndex)</code>
     */
    @Override
    public FastTable<E> subList(int fromIndex, int toIndex) {
        if ((fromIndex < 0) || (toIndex > size()) || (fromIndex > toIndex))
            throw new IndexOutOfBoundsException(); // As per List.subList contract.
        return new FastTable(new SubTableImpl(impl, fromIndex, toIndex));
    }

    //                      ///////////////////
    //                      // Deque Methods //
    //                      ///////////////////
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
    public final boolean offerFirst(E e) {
        addFirst(e);
        return true;
    }

    @Override
    public final boolean offerLast(E e) {
        addLast(e);
        return true;
    }

    @Override
    public final boolean removeFirstOccurrence(Object obj) {
        return remove(obj);
    }

    @Override
    public final boolean removeLastOccurrence(Object obj) {
        return reverse().remove(obj);
    }

    @Override
    public final boolean offer(E e) {
         return offerLast(e);
    }

    @Override
    public final E remove() {
        return removeFirst();
    }

    @Override
    public final E poll() {
        return pollFirst();
    }

    @Override
    public final E element() {
        return getFirst();
    }

    @Override
    public final E peek() {
       return peekFirst();
    }

    @Override
    public final void push(E e) {
        addFirst(e);
    }

    @Override
    public final E pop() {
        return removeFirst();
    }

    @Override
    public Iterator<E> descendingIterator() {
        return reverse().iterator();
    }
}