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
import java.util.NoSuchElementException;
import java.util.RandomAccess;
import javolution.lang.Copyable;
import javolution.lang.Functor;
import javolution.lang.Immutable;
import javolution.lang.MathLib;
import javolution.lang.Predicate;

/**
 * <p> A random access collection of ordered/unordered elements with fast 
 *     insertion/deletion and smooth capacity increase (or decrease). 
 *     The capacity of a fast table is automatically adjusted to best fit
 *     its size (memory footprint minimization).</p>
 * 
 * <p> Instances of this class can advantageously replace {@link java.util.ArrayList ArrayList},
 *     {@link java.util.LinkedList LinkedList}, {@link java.util.Deque Deque} 
 *      and {@link java.util.TreeSet TreeSet} in terms of adaptability, space or performance.</p>
 *     <img src="doc-files/list-add.png"/>
 *     
 *  <p> As for any {@link FastCollection fast collection} iterations are faster
 *      when performed using closures (and the notation is shorter with JDK 8).
 *      [code]
 *      FastTable<Person> persons = ...
 *      Person john = persons.findFirst(new Predicate<Person>() {
 *          public Boolean evaluate(Person person) {
 *              return person.getName().equals("John");
 *          }
 *      });
 *      [/code]</p>
 *     p> Fast table supports {@link #sort sorting} in place (quick sort) 
 *      using the table {@link FastCollection#comparator() comparator}.
 *      If the table {@link FastCollection#isOrdered is ordered}, the 
 *      the {@link #add add} method keeps the collection ordered and the 
 *      implementation guaranteed log(n) time cost for the basic
 *      operations such as {@link #indexOf indexOf}, {@link #contains},
 *      and {@link #remove}.</p>
 * 
 * <p>  Finally, fast table provides a {@link FastTable#shared shared} view 
 *      which can be iterated over using closures and modified concurrently.</p>  
 * 
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0.0, December 12, 2012
 */
public class FastTable<E> extends FastCollection<E> implements
        List<E>, RandomAccess, Copyable<FastTable<E>> {

    // We do a full resize (and copy) only when the capacity is less than C1.
    // For large collections, multi-dimensional arrays are employed.
    private static final int B0 = 2; // Block initial capacity in bits.

    private static final int C0 = 1 << B0; // Block initial capacity (4)

    private static final int B1 = 8; // Block maximum capacity in bits.

    private static final int C1 = 1 << B1; // Block maximum capacity (256).

    private static final int M1 = C1 - 1; // Block Mask.

    /**
     * Holds the block elements.
     */
    private Block<E>[] blocks = new Block[] { new Block(C0) };

    /**
     * Holds the current size.
     */
    private int size;

    /**
     * Holds the current capacity.
     */
    private int capacity;

    /**
     * Creates an empty table whose capacity increments/decrements smoothly 
     * without large resize operations to best fit the table current size.
     */
    public FastTable() {
    }

    /**
     * Creates a table containing the specified elements (in the same order).
     *
     * @param that the elements  to be placed into this table.
     */
    public FastTable(Collection<? extends E> that) {
        addAll(that);
    }

    /**
     * Returns an {@link Unmodifiable}/immutable view of this table.
     * Attempts to modify the table returned will result in an 
     * {@link UnsupportedOperationException} being thrown. 
     */
    public Unmodifiable<E> unmodifiable() {
        return new Unmodifiable<E>(this);
    }

    /**
     * Returns a thread-safe read-write {@link Shared} view of this table.
     * It uses synchronization in order to support 
     * concurrent reads. Iterators methods have been deprecated since they
     * don't prevent concurrent modifications. Closures (e.g. {@link FastTable#doWhile}) 
     * are the preferred mean of iterating over a shared table.
     * The shared view exports most of the {@link Deque} methods provided by this table.
     */
    public Shared<E> shared() {
        return new Shared<E>(this);
    }

    // Implements FastCollection Abstract Method.
    public <R> FastTable<R> forEach(Functor<E, R> functor) {
        return forEach(functor, 0, size);
    }

    final <R> FastTable<R> forEach(Functor<E, R> functor, int start, int length) {
        FastTable<R> results = new FastTable<R>();
        for (int i = start >> B1; (i << B1) < (start + length); i++) {
            Block<E> block = blocks[i];
            for (int j = i << B1, n = Math.min(start + length, j + C1); j < n;) {
                R result = functor.evaluate(block.get(j++));
                if (result != null) results.addLast(result);
            }
        }
        return results;
    }

    // Implements FastCollection Abstract Method.
    public void doWhile(Predicate<E> predicate) {
        doWhile(predicate, 0, size);
    }

    final void doWhile(Predicate<E> predicate, int start, int length) {
        for (int i = start >> B1; (i << B1) < (start + length); i++) {
            Block<E> block = blocks[i];
            for (int j = i << B1, n = Math.min(start + length, j + C1); j < n;) {
                if (!predicate.evaluate(block.get(j++))) return;
            }
        }
    }

    // Implements FastCollection Abstract Method.
    public boolean removeAll(Predicate<E> predicate) {
        return removeAll(predicate, 0, size);
    }

    final boolean removeAll(Predicate<E> predicate, int start, int length) {
        boolean modified = false;
        for (int i = start >> B1; (i << B1) < (start + length); i++) {
            Block<E> block = blocks[i];
            for (int j = i << B1, n = Math.min(start + length, j + C1); j < n;) {
                if (predicate.evaluate(block.get(j++))) {
                    this.remove(--j);
                    modified = true;
                }
            }
        }
        return modified;
    }

    // Implements FastCollection Abstract Method.
    public ListIterator<E> iterator() {
        return new ListIteratorImpl(this, 0);
    }

    /**
     * Either inserts the specified element ({@link FastTable#isOrdered 
     * ordered} table) or appends the element to the end of this table. 
     *
     * @param element the element to be added to this table.
     * @return <code>true</code> (as per the general contract of the
     *         <code>Collection.add</code> method).
     */
    @Override
    public boolean add(E element) {
        if (isOrdered()) {
            add(indexIfOrderedOf(element, 0, size), element);
            return true;
        }
        if (capacity <= size) ensureCapacity(size + 1);
        blocks[size >> B1].set(size++, element);
        return true;
    }

    /**
     * Removes the first occurrence in this collection of the specified element
     * (performs a dichotomic search if the table is ordered).
     *
     * @param element the element to be removed from this collection.
     * @return <code>true</code> if this collection contained the specified
     *         element; <code>false</code> otherwise.
     * @throws UnsupportedOperationException if the collection is not modifiable.
     */
    @Override
    public boolean remove(Object element) {
        return remove((E) element, 0, size);
    }

    final boolean remove(E e, int start, int length) {
        int i = indexOf(e, start, length);
        if (i < 0) return false;
        remove(i);
        return true;
    }

    /**
     * Indicates if this collection contains the specified element (performs
     * a dichotomic search if the table is ordered).
     *
     * @param element the element whose presence in this collection 
     *        is to be tested.
     * @return <code>true</code> if this collection contains the specified
     *         element;<code>false</code> otherwise.
     */
    @Override
    public boolean contains(Object element) {
        return contains((E) element, 0, size);
    }

    final boolean contains(E e, int start, int length) {
        int i = indexOf(e, start, length);
        return (i < 0) ? false : true;
    }

    @Override
    public void clear() {
        removeRange(0, size);
    }

    @Override
    public int size() {
        return size;
    }

    //
    // Convenience/Dequeu Methods 
    //
    /**
     * Adds this element to this table only if not already present.
     * The element is either appended to the end of this table or 
     * inserted if the table is {@link FastTable#isOrdered ordered}.
     *
     * @param element the element to be added to this table if not already there.
     * @return <code>true</code> if the element has been added;
     *         <code>false</code> otherwise.
     */
    public boolean addIfAbsent(E element) {
        if (isOrdered()) {
            int index = indexIfOrderedOf(element, 0, size);
            if (comparator().areEqual(element, blocks[index >> B1].get(index)))
                return false;
            add(index, element);
            return true;
        } else {
            if (this.contains(element)) return false;
            return add(element);
        }
    }

    /**
     * Removes the elements between <code>[fromIndex..toIndex[<code> from
     * this table.
     *
     * @param fromIndex the beginning index, inclusive.
     * @param toIndex the ending index, exclusive.
     * @throws IndexOutOfBoundsException if <code>(fromIndex < 0) || (toIndex < 0) 
     *         || (fromIndex > toIndex) || (toIndex > this.size())</code>
     */
    public void removeRange(int fromIndex, int toIndex) {
        if ((fromIndex < 0) || (toIndex < 0) || (fromIndex > toIndex)
                || (toIndex > size))
            throw new IndexOutOfBoundsException("FastTable removeRange("
                    + fromIndex + ", " + toIndex + ") index out of bounds, size: " + size);
        shiftLeftAfter(fromIndex, toIndex - fromIndex);
        trimToSize();
    }

    /**
     * Returns the first element of this table.
     *
     * @return this table first element.
     * @throws NoSuchElementException if this table is empty.
     */
    public E getFirst() {
        if (size == 0)
            throw new NoSuchElementException();
        return blocks[0].get(0);
    }

    /**
     * Returns the last element of this table.
     *
     * @return this table last element.
     * @throws NoSuchElementException if this table is empty.
     */
    public E getLast() {
        if (size == 0)
            throw new NoSuchElementException();
        return blocks[(size - 1) >> B1].get(size - 1);
    }

    /**
     * Appends the specified element to the beginning of this table.
     * 
     * @param element the element to be added.
     */
    public void addFirst(E element) {
        add(0, element);
    }

    /**
     * Appends the specified element to the end of this table.
     * 
     * @param element the element to be added.
     */
    public void addLast(E element) {
        if (capacity <= size) ensureCapacity(size + 1);
        blocks[size >> B1].set(size++, element);
    }

    /**
     * Removes and returns the first element of this table.
     *
     * @return this table's last element before this call.
     * @throws NoSuchElementException if this table is empty.
     */
    public E removeFirst() {
        if (size == 0) throw new NoSuchElementException();
        return remove(0);
    }

    /**
     * Removes and returns the last element of this table.
     *
     * @return this table's last element before this call.
     * @throws NoSuchElementException if this table is empty.
     */
    public E removeLast() {
        if (size == 0) throw new NoSuchElementException();
        final E previous = blocks[--size >> B1].get(size);
        blocks[size >> B1].set(size, null);
        trimToSize();
        return previous;
    }

    /**
     * Retrieves and removes the first element of this table,
     * or returns <code>null</code> if this table is empty.
     *
     * @return the head of this table, or <code>null</code> if empty.
     */
    public E pollFirst() {
        return (size != 0) ? removeFirst() : null;
    }

    /**
     * Retrieves and removes the last element of this table,
     * or returns <code>null</code> if this table is empty.
     *
     * @return the tail of this table or <code>null</code> if empty.
     */
    public E pollLast() {
        return (size != 0) ? removeLast() : null;
    }

    /**
     * Retrieves, but does not remove, the first element of this table,
     * or returns <code>null</code> if this table is empty.
     *
     * @return the head of this table or <code>null</code> if empty
     */
    public E peekFirst() {
        return (size != 0) ? getFirst() : null;
    }

    /**
     * Retrieves, but does not remove, the last element of this table,
     * or returns <code>null</code> if this table is empty.
     *
     * @return the tail of this table or <code>null</code> if empty
     */
    public E peekLast() {
        return (size != 0) ? getLast() : null;
    }

    /**
     * Sorts this table in place (quick sort) using this table 
     * {@link FastCollection#comparator() comparator}
     * (smallest first).
     * 
     * @return <code>this</code>
     */
    public FastTable<E> sort() {
        if (size > 1) {
            quicksort(0, size - 1, this.comparator());
        }
        return this;
    }

    // From Wikipedia Quick Sort - http://en.wikipedia.org/wiki/Quicksort
    //
    private void quicksort(int first, int last, FastComparator<E> cmp) {
        if (first < last) {
            int pivIndex = partition(first, last, cmp);
            quicksort(first, (pivIndex - 1), cmp);
            quicksort((pivIndex + 1), last, cmp);
        }
    }

    private int partition(int f, int l, FastComparator<E> cmp) {
        int up, down;
        E piv = get(f);
        up = f;
        down = l;
        do {
            while (cmp.compare(get(up), piv) <= 0 && up < l) {
                up++;
            }
            while (cmp.compare(get(down), piv) > 0 && down > f) {
                down--;
            }
            if (up < down) { // Swaps.
                E temp = get(up);
                set(up, get(down));
                set(down, temp);
            }
        } while (down > up);
        set(f, get(down));
        set(down, piv);
        return down;
    }

    /**
     * Returns a deep copy of this object. 
     * 
     * @return an object identical to this object but possibly allocated 
     *         in a different memory space.
     * @see Copyable
     */
    public FastTable<E> copy() {
        final FastComparator<E> comp = comparator();
        final boolean ordered = isOrdered();
        final FastTable<E> newTable = new FastTable() {

            @Override
            public boolean isOrdered() {
                return ordered;
            }

            @Override
            public FastComparator<E> comparator() {
                return comp;
            }

        };
        this.doWhile(new Predicate<E>() {

            public Boolean evaluate(E param) {
                newTable.addLast((param instanceof Copyable)
                        ? ((Copyable<E>) param).copy() : param);
                return true;
            }

        });
        return newTable;
    }

    //
    // List Specifics
    //
    /**
     * Inserts the specified element at the specified position in this table.
     * Shifts the element currently at that position
     * (if any) and any subsequent elements to the right (adds one to their
     * indices).
     *
     * @param index the index at which the specified element is to be inserted.
     * @param element the element to be inserted.
     * @throws IndexOutOfBoundsException if <code>(index < 0) || 
     *         (index > size())</code>
     */
    public void add(int index, E element) {
        if (index > size)
            throw new IndexOutOfBoundsException("index: " + index);
        if (capacity <= size) ensureCapacity(size + 1);
        shiftRightAfter(index, 1);
        blocks[index >> B1].set(index, element);
    }

    /**
     * Inserts all of the elements in the specified collection into this
     * table at the specified position. Shifts the element currently at that
     * position and any subsequent elements to the right 
     * (increases their indices). 
     *
     * @param index the index at which to insert first element from the specified
     *        collection.
     * @param elements the elements to be inserted into this list.
     * @return <code>true</code> if this list changed as a result of the call;
     *         <code>false</code> otherwise.
     * @throws IndexOutOfBoundsException if <code>(index < 0) || 
     *         (index > size())</code>
     */
    public boolean addAll(final int index, final Collection<? extends E> elements) {
        if (index > size)
            throw new IndexOutOfBoundsException("index: " + index);
        int shift = elements.size();
        ensureCapacity(size + shift);
        shiftRightAfter(index, shift);
        if (elements instanceof FastCollection) {
            ((FastCollection<E>) elements).doWhile(new Predicate<E>() {

                int i = index;

                public Boolean evaluate(E param) {
                    blocks[i >> B1].set(i++, param);
                    return true;
                }

            });
        } else { // Use iterator since we have no choice.
            Iterator<? extends E> elementsIterator = elements.iterator();
            for (int i = index, n = index + shift; i < n; i++) {
                blocks[i >> B1].set(i, elementsIterator.next());
            }
        }
        return shift != 0;
    }

    /**
     * Removes the element at the specified position from this table.
     * Shifts any subsequent elements to the left (subtracts one
     * from their indices). Returns the element that was removed from the
     * table.
     *
     * @param index the index of the element to removed.
     * @return the element previously at the specified position.
     * @throws IndexOutOfBoundsException if <code>(index < 0) || 
     *         (index >= size())</code>
     */
    public E remove(int index) {
        final E previous = get(index);
        shiftLeftAfter(index, 1);
        trimToSize();
        return previous;
    }

    /**
     * Returns the element at the specified index.
     *
     * @param index index of element to return.
     * @return the element at the specified position in this list.
     * @throws IndexOutOfBoundsException if <code>(index < 0) || 
     *         (index >= size())</code>
     */
    public E get(int index) {
        if (index >= size)
            throw new IndexOutOfBoundsException();
        return blocks[index >> B1].get(index);
    }

    /**
     * Replaces the element at the specified position in this table with the
     * specified element.
     *
     * @param index index of element to replace.
     * @param element element to be stored at the specified position.
     * @return previous element.
     * @throws IndexOutOfBoundsException if <code>(index < 0) || 
     *         (index >= size())</code>
     */
    public E set(int index, E element) {
        if (index >= size)
            throw new IndexOutOfBoundsException();
        final Block<E> block = blocks[index >> B1];
        final E previous = block.get(index);
        block.set(index, element);
        return previous;
    }

    /**
     * Returns the index in this table of the first occurrence of the specified
     * element, or -1 if this table does not contain this element. If this  
     * table {@link #isOrdered() is ordered} a dichotomic search is performed.
     *
     * @param element the element to search for.
     * @return the index in this table of the first occurrence of the specified
     *         element, or -1 if this table does not contain this element.
     */
    public int indexOf(Object element) {
        return indexOf((E) element, 0, size);
    }

    final int indexOf(E e, int start, int length) {
        final FastComparator<E> comp = comparator();
        if (isOrdered()) {
            int index = indexIfOrderedOf(e, start, length);
            if ((index == start + length) || !comp.areEqual(e, blocks[index >> B1].get(index)))
                return -1;
            return index;
        } else {
            for (int i = start; i < start + length; i++) {
                if (comp.areEqual(e, blocks[i >> B1].get(i))) return i;
            }
            return -1;
        }
    }

    /**
     * Returns the index in this table of the last occurrence of the specified
     * element, or -1 if this table does not contain this element.
     *
     * @param element the element to search for.
     * @return the index in this table of the last occurrence of the specified
     *         element, or -1 if this table does not contain this element.
     */
    public int lastIndexOf(Object element) {
        return lastIndexOf((E) element, 0, size);
    }

    final int lastIndexOf(E e, int start, int length) {
        final FastComparator<E> comp = comparator();
        for (int i = start + length - 1; i >= start; i--) {
            if (comp.areEqual(e, blocks[i >> B1].get(i))) return i;
        }
        return -1;
    }

    /**
     * Returns a list iterator over the elements in this list.
     *
     * @return an iterator over this list values.
     */
    public ListIterator<E> listIterator() {
        return new ListIteratorImpl(this, 0);
    }

    /**
     * Returns a list iterator from the specified position.
     * 
     * @param index the index of first value to be returned from the
     *        list iterator (by a call to the <code>next</code> method).
     * @return a list iterator of the elements in this table
     *         starting at the specified position in this list.
     * @throws IndexOutOfBoundsException if the index is out of range 
     *         [code](index < 0 || index > size())[/code]
     */
    public ListIterator<E> listIterator(int index) {
        return new ListIteratorImpl(this, index);
    }

    /**
     * Returns a view of the portion of this table between the specified
     * indexes.
     * If the specified indexes are equal, the returned list is empty. 
     * The returned list is backed by this list, so non-structural changes in
     * the returned list are reflected in this list, and vice-versa. 
     *
     * This method eliminates the need for explicit range operations (of
     * the sort that commonly exist for arrays). Any operation that expects
     * a list can be used as a range operation by passing a subList view
     * instead of a whole list.  For example, the following idiom
     * removes a range of elements from a list: [code]
     * list.subList(from, to).clear();[/code]
     * Similar idioms may be constructed for <code>indexOf</code> and
     * <code>lastIndexOf</code>, and all of the algorithms in the
     * <code>Collections</code> class can be applied to a subList.
     *
     * The semantics of the list returned by this method become undefined if
     * the backing list (i.e., this list) is <i>structurally modified</i> in
     * any way other than via the returned list (structural modifications are
     * those that change the size of this list, or otherwise perturb it in such
     * a fashion that iterations in progress may yield incorrect results).
     *
     * @param fromIndex low endpoint (inclusive) of the subList.
     * @param toIndex high endpoint (exclusive) of the subList.
     * @return a view of the specified range within this list.
     * 
     * @throws IndexOutOfBoundsException if <code>(fromIndex < 0 ||
     *          toIndex > size || fromIndex > toIndex)</code>
     */
    public SubTable<E> subList(int fromIndex, int toIndex) {
        return new SubTable(this, fromIndex, toIndex - fromIndex);
    }

    //
    // Views Inner Classes 
    //  
    /**
     * A view over a portion of a fast table (shared or not). 
     * It is always possible to get a sub-table view over a shared table. 
     * But shared views over sub-tables are not supported.
     */
    public static final class SubTable<E> extends FastCollection<E> implements List<E>, RandomAccess {

        private final List<E> that;

        private final FastTable<E> thatTable;

        private final Shared<E> thatShared;

        private final int start;

        private int length;

        // Sub-tables can only be based on fast tables or shared tables.
        SubTable(List<E> that, int start, int length) {
            if ((start < 0) || (start + length > that.size()) || (length > 0))
                throw new IndexOutOfBoundsException("fromIndex: " + start
                        + ", toIndex: " + (start + length)
                        + " for list of size: " + that.size());
            this.that = that;
            this.thatTable = (that instanceof FastTable) ? (FastTable) that : null;
            this.thatShared = (that instanceof Shared) ? (Shared) that : null;
            this.start = start;
            this.length = length;
        }

        public Unmodifiable<E> unmodifiable() {
            return new Unmodifiable<E>(this);
        }

        /**
         * Throws UnsupportedOperationException; it is always possible to get 
         * a sub-table view over a shared table, but shared views over 
         * sub-tables are not supported.
         * 
         * @throws UnsupportedOperationException
         */
        public FastCollection<E> shared() {
            throw new UnsupportedOperationException(
                    "It is always possible to get a sub-table view over a shared"
                    + " table. But shared views over sub-tables are not supported.");
        }

        @Override
        public FastComparator<E> comparator() {
            return thatTable != null ? thatTable.comparator() : thatShared.comparator();
        }

        @Override
        public boolean isOrdered() {
            return thatTable != null ? thatTable.isOrdered() : thatShared.isOrdered();
        }

        public <R> FastCollection<R> forEach(Functor<E, R> functor) {
            return thatTable != null ? thatTable.forEach(functor, start, length)
                    : thatShared.forEach(functor, start, length);
        }

        public void doWhile(Predicate<E> predicate) {
            if (thatTable != null) thatTable.doWhile(predicate, start, length);
            else thatShared.doWhile(predicate, start, length);
        }

        public boolean removeAll(Predicate<E> predicate) {
            return thatTable != null ? thatTable.removeAll(predicate, start, length)
                    : thatShared.removeAll(predicate, start, length);
        }

        public ListIterator<E> iterator() {
            return new ListIteratorImpl(this, 0);
        }

        @Override
        public boolean add(E element) {
            that.add(start + length++, element);
            return true;
        }

        @Override
        public boolean remove(Object element) {
            return thatTable != null ? thatTable.remove((E) element, start, length--)
                    : thatShared.remove((E) element, start, length--);
        }

        @Override
        public boolean contains(Object element) {
            return thatTable != null ? thatTable.contains((E) element, start, length)
                    : thatShared.contains((E) element, start, length);
        }

        @Override
        public void clear() {
            if (thatTable != null) thatTable.removeRange(start, start + length);
            else thatShared.removeRange(start, start + length);
            length = 0;
        }

        @Override
        public int size() {
            return length;
        }

        //
        // List Specifics 
        //
        public boolean addAll(int index, Collection<? extends E> c) {
            length += c.size();
            return that.addAll(index + start, c);
        }

        public E get(int index) {
            return that.get(index + start);
        }

        public E set(int index, E element) {
            return that.set(index + start, element);
        }

        public void add(int index, E element) {
            length++;
            that.add(index + start, element);
        }

        public E remove(int index) {
            length--;
            return that.remove(index + start);
        }

        public int indexOf(Object o) {
            return thatTable != null ? thatTable.indexOf((E) o, start, length)
                    : thatShared.indexOf((E) o, start, length);
        }

        public int lastIndexOf(Object o) {
            return thatTable != null ? thatTable.lastIndexOf((E) o, start, length)
                    : thatShared.lastIndexOf((E) o, start, length);
        }

        public ListIterator<E> listIterator() {
            return new ListIteratorImpl(this, 0);
        }

        public ListIterator<E> listIterator(int index) {
            return new ListIteratorImpl(this, index);
        }

        public SubTable<E> subList(int fromIndex, int toIndex) {
            return new SubTable(that, start + fromIndex, toIndex - fromIndex);
        }

    }

    /**
     * An unmodifiable/{@link Immutable immutable} view over a table or 
     * a sub-table. 
     */
    public static final class Unmodifiable<E> extends FastCollection<E> implements List<E>, RandomAccess, Immutable {

        private final FastCollection<E> that;

        private final List<E> thatList;

        Unmodifiable(FastCollection<E> that) {
            this.that = that;
            this.thatList = (List<E>) that;
        }

        public Unmodifiable<E> unmodifiable() {
            return this;
        }

        public Unmodifiable<E> shared() {
            return this; // Immutable instances can be shared.
        }

        @Override
        public FastComparator<E> comparator() {
            return that.comparator();
        }

        @Override
        public boolean isOrdered() {
            return that.isOrdered();
        }

        public <R> FastCollection<R> forEach(Functor<E, R> functor) {
            return that.forEach(functor);
        }

        public void doWhile(Predicate<E> predicate) {
            that.doWhile(predicate);
        }

        public boolean removeAll(Predicate<E> predicate) {
            throw new UnsupportedOperationException("Unmodifiable.");
        }

        public ListIterator<E> iterator() {
            return new ListIteratorImpl(this, 0);
        }

        @Override
        public boolean add(E element) {
            throw new UnsupportedOperationException("Unmodifiable.");
        }

        @Override
        public boolean remove(Object element) {
            throw new UnsupportedOperationException("Unmodifiable.");
        }

        @Override
        public boolean contains(Object element) {
            return that.contains(element);
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException("Unmodifiable.");
        }

        @Override
        public int size() {
            return that.size();
        }

        //
        // List Specifics 
        //
        public boolean addAll(int index, Collection<? extends E> c) {
            throw new UnsupportedOperationException("Unmodifiable.");
        }

        public E get(int index) {
            return thatList.get(index);
        }

        public E set(int index, E element) {
            throw new UnsupportedOperationException("Unmodifiable.");
        }

        public void add(int index, E element) {
            throw new UnsupportedOperationException("Unmodifiable.");
        }

        public E remove(int index) {
            throw new UnsupportedOperationException("Unmodifiable.");
        }

        public int indexOf(Object o) {
            return thatList.indexOf(o);
        }

        public int lastIndexOf(Object o) {
            return thatList.lastIndexOf(o);
        }

        public ListIterator<E> listIterator() {
            return new ListIteratorImpl(this, 0);
        }

        public ListIterator<E> listIterator(int index) {
            return new ListIteratorImpl(this, index);
        }

        public Unmodifiable<E> subList(int fromIndex, int toIndex) {
            if (that instanceof FastTable)
                return ((FastTable) that).subList(fromIndex, toIndex).unmodifiable();
            if (that instanceof SubTable)
                return ((SubTable) that).subList(fromIndex, toIndex).unmodifiable();
            throw new UnsupportedOperationException("SubList of " + that.getClass());
        }

    }

    /**
     * A shared view over a fast table.
     * Iterators methods have been deprecated since they don't prevent 
     * concurrent modifications. Closures (e.g. {@link FastTable#doWhile}) 
     * are the preferred mean of iterating over a shared table.
     */
    public static final class Shared<E> extends FastCollection<E> implements List<E>, RandomAccess {

        final FastTable<E> that;

        Shared(FastTable<E> that) {
            this.that = that;
        }

        public Unmodifiable<E> unmodifiable() {
            return new Unmodifiable(that); // shared().unmodifiable() equivalent to unmodifiable() 
        }

        public Shared<E> shared() {
            return this;
        }

        @Override
        public FastComparator<E> comparator() {
            return that.comparator();
        }

        @Override
        public boolean isOrdered() {
            return that.isOrdered();
        }

        public synchronized <R> FastTable<R>  forEach(Functor<E, R> functor) {
                return that.forEach(functor);
        }

        public synchronized void doWhile(Predicate<E> predicate) {
                that.doWhile(predicate);
        }

        public synchronized boolean removeAll(Predicate<E> predicate) {
                return that.removeAll(predicate);
        }

        /**
         * @deprecated Use of closures recommended to iterate over {@link FastCollection}.
         */
        @Deprecated
        public ListIterator<E> iterator() {
            return new ListIteratorImpl(this, 0);
        }

        @Override
        public synchronized boolean add(E element) {
                return that.add(element);
        }

        @Override
        public synchronized boolean remove(Object element) {
                return that.remove(element);
        }

        @Override
        public synchronized boolean contains(Object element) {
                return that.contains(element);
        }

        @Override
        public synchronized void clear() {
                that.clear();
        }

        @Override
        public synchronized int size() {
                return that.size();
        }

        //
        // List Specifics 
        //
        public synchronized boolean addAll(int index, Collection<? extends E> c) {
                return that.addAll(index, c);
        }

        public synchronized E get(int index) {
                return that.get(index);
        }

        public synchronized E set(int index, E element) {
                return that.set(index, element);
        }

        public synchronized void add(int index, E element) {
                that.add(index, element);
        }

        public synchronized E remove(int index) {
                return that.remove(index);
        }

        public synchronized int indexOf(Object o) {
                return that.indexOf(o);
        }

        public synchronized int lastIndexOf(Object o) {
                return that.lastIndexOf(o);
        }

        /**
         * @deprecated Use of closures recommended to iterate over {@link FastCollection}.
         */
        @Deprecated
        public ListIterator<E> listIterator() {
            return new ListIteratorImpl(this, 0);
        }

        /**
         * @deprecated Use of closures recommended to iterate over {@link FastCollection}.
         */
        @Deprecated
        public ListIterator<E> listIterator(int index) {
            return new ListIteratorImpl(this, index);
        }

        public SubTable<E> subList(int fromIndex, int toIndex) {
            return new SubTable(that, fromIndex, toIndex - fromIndex);
        }

        //
        // Convenient method of FastTable exported here (e.g. Deque)
        //
        /**
         * See {@link FastTable#getFirst() }
         */
        public synchronized E getFirst() {
                return that.getFirst();
        }

        /**
         * See {@link FastTable#getLast() }
         */
        public synchronized E getLast() {
                return that.getLast();
        }

        /**
         * See {@link FastTable#addFirst }
         */
        public synchronized void addFirst(E element) {
                that.addFirst(element);
        }

        /**
         * See {@link FastTable#addLast }
         */
        public synchronized void addLast(E element) {
                that.addLast(element);
        }

        /**
         * See {@link FastTable#removeFirst() }
         */
        public synchronized E removeFirst() {
                return that.removeFirst();
        }

        /**
         * See {@link FastTable#removeLast() }
         */
        public synchronized E removeLast() {
                return that.removeLast();
        }

        /**
         * See {@link FastTable#pollFirst() }
         */
        public synchronized E pollFirst() {
                return that.pollFirst();
        }

        /**
         * See {@link FastTable#pollLast() }
         */
        public synchronized E pollLast() {
                return that.pollLast();
        }

        /**
         * See {@link FastTable#peekFirst() }
         */
        public synchronized E peekFirst() {
                return that.peekFirst();
        }

        /**
         * See {@link FastTable#peekLast() }
         */
        public synchronized E peekLast() {
                return that.peekLast();
        }

        /**
         * See {@link FastTable#removeRange }
         */
        public synchronized void removeRange(int fromIndex, int toIndex) {
                that.removeRange(fromIndex, toIndex);
        }

        //
        // Methods useful for Sub-Tables Views over Shared tables.
        //
        final synchronized <R> FastTable<R> forEach(Functor<E, R> functor, int start, int length) {
                return that.forEach(functor, start, length);
        }

        final synchronized void doWhile(Predicate<E> predicate, int start, int length) {
                that.doWhile(predicate, start, length);
        }

        final synchronized boolean removeAll(Predicate<E> predicate, int start, int length) {
                return that.removeAll(predicate, start, length);
        }

        final synchronized boolean remove(E e, int start, int length) {
                return that.remove(e, start, length);
        }

        final synchronized boolean contains(E e, int start, int length) {
                return that.contains(e, start, length);
        }

        final synchronized int indexOf(E e, int start, int length) {
                return that.indexOf(e, start, length);
        }

        final synchronized int lastIndexOf(E e, int start, int length) {
                return that.lastIndexOf(e, start, length);
        }

    }

    /**
     * This utility class provides an iterator over any list instance with 
     * random access.
     */
    static final class ListIteratorImpl<E> implements ListIterator<E> {

        private final List<E> that;

        private int nextIndex;

        private int currentIndex = -1;

        private int end;

        public ListIteratorImpl(List<E> that, int index) {
            if ((index < 0) || (index > that.size()))
                throw new IndexOutOfBoundsException();
            this.that = that;
            this.nextIndex = index;
            this.end = that.size();
        }

        public boolean hasNext() {
            return (nextIndex < end);
        }

        public E next() {
            if (nextIndex >= end)
                throw new NoSuchElementException();
            currentIndex = nextIndex++;
            return that.get(currentIndex);
        }

        public int nextIndex() {
            return nextIndex;
        }

        public boolean hasPrevious() {
            return nextIndex > 0;
        }

        public E previous() {
            if (nextIndex <= 0)
                throw new NoSuchElementException();
            currentIndex = --nextIndex;
            return that.get(currentIndex);
        }

        public int previousIndex() {
            return nextIndex - 1;
        }

        public void add(E e) {
            that.add(nextIndex++, e);
            end++;
            currentIndex = -1;
        }

        public void set(E e) {
            if (currentIndex >= 0) {
                that.set(currentIndex, e);
            } else {
                throw new IllegalStateException();
            }
        }

        public void remove() {
            if (currentIndex >= 0) {
                that.remove(currentIndex);
                end--;
                if (currentIndex < nextIndex) {
                    nextIndex--;
                }
                currentIndex = -1;
            } else {
                throw new IllegalStateException();
            }
        }

    }

    //
    // Internal Implementation. 
    //    
    private static final class Block<T> {

        int offset; // Index of [0]

        T[] data;
        
        Block(int length) {
            data = (T[]) new Object[length];
        }

        T get(int i) {
            return data[(i + offset) & (data.length - 1)];
        }

        void set(int i, T t) {
            data[(i + offset) & (data.length - 1)] = t;
        }

        // Can only be called on block full.          
        void shiftLeft(int n, Block<T> rightBlock) {
            for (int i = 0; i < n; i++) {
                data[offset++ & M1] = (rightBlock != null) ? rightBlock.get(i) : null;
            }
        }

        // Can only be called on block full.          
        void shiftRight(int n, Block<T> leftBlock) {
            for (int i = 0; i < n; i++) {
                data[--offset & M1] = (leftBlock != null) ? leftBlock.get(M1 - i) : null;
            }
        }

    }
 
    // Updates the size after shift. must ensure capacity before shift.
    void shiftRightAfter(int index, int shift) {
        if ((shift == 0) || (index == size)) return;
        int bi = index >> B1;
        if ((index & M1) == 0) { // Full block shift.
            if (shift <= C1 / 2) {
                for (int i = (size - 1 + shift) >> B1; i > bi; i--) {
                    blocks[i].shiftRight(shift, blocks[i - 1]);
                }
                blocks[bi].shiftRight(shift, null);
                size += shift;
            } else if (shift < C1) { // Optimization (reduces shift to less than C1/2)
                ensureCapacity(size + C1);
                shiftRightAfter(index, C1);
                shiftLeftAfter(index, C1 - shift);
            } else { // Shifts blocks by swapping. 
                int blockShift = shift >> B1;
                int bl = (size - 1) >> B1;
                for (int i = bl; i > bi; i--) {
                    Block<E> tmp = blocks[i + blockShift];
                    blocks[i + blockShift] = blocks[i];
                    blocks[i] = tmp;
                }
                size += blockShift << B1;
                shiftRightAfter(index, shift & M1);
            }
        } else {
            int ir = (bi + 1) << B1;
            ir = size < ir ? size : ir;
            if (size > ir) { // Full block shift of left blocks.
                shiftRightAfter(ir, shift); 
            }
            for (int i = ir - 1; i >= index; i--) {
                set(i + shift, blocks[bi].get(i));
            }
            size += shift;
        }
    }

    // Updates the size after shift. 
    void shiftLeftAfter(int index, int shift) {
        if ((shift == 0) || (index == size)) return;
        int bi = index >> B1;
        if ((index & M1) == 0) { // Full block shift.
            int bl = (size - 1) >> B1;
            if (shift <= C1 / 2) {
                for (int i= bi; i < bl; i++)  {
                    blocks[i].shiftLeft(shift & M1, blocks[i + 1]);
                }
                blocks[bl].shiftLeft(shift, null);
                size -= shift;
            } else if (shift < C1) { // Optimization (reduces shift to less than C1/2)
                ensureCapacity(size + C1 - shift);
                shiftRightAfter(index, C1 - shift);
                shiftLeftAfter(index, C1);
            } else { // Shifts blocks by swapping. 
                int blockShift = shift >> B1;
                blocks[bi] = new Block(C1); // Resets (for GC).
                for (int i=1; i < blockShift; i++) {
                    blocks[bi + i] = null; // See trimToSize.
                    capacity -= C1;
                }
                for (int i = bi + blockShift; i <= bl; i++) {
                    Block<E> tmp = blocks[i - blockShift];
                    blocks[i - blockShift] = blocks[i];
                    blocks[i] = tmp;
                }
                size -= blockShift << B1;
                shiftLeftAfter(index, shift & M1);
            }
        } else {
            int ir = (bi + 1) << B1;
            ir = size < ir ? size : ir;
            for (int i = index; i < ir; i++) {
                blocks[bi].set(i, get(i + shift));
            }   
            if (size > ir) { // Full block shift of left blocks.
                shiftLeftAfter(ir, shift); 
            }
            size += shift;
        }
    }

    final void ensureCapacity(int min) {
        if (capacity >= min) return;
        if (capacity < C1) { // Resizes only the first block.
            while (capacity < MathLib.min(C1, min)) {
                capacity <<= 1;
            } 
            E[] tmp = (E[]) new Object[capacity];
            Block<E> blocks0 = blocks[0];
            int n = blocks0.data.length;
            int start = blocks0.offset;
            int end = start + size;
            if (end > blocks0.data.length) { // Wraps around.
                int length = end - n;
                System.arraycopy(blocks0.data, 0, tmp, size - length, length);
                end = n;
            }
            System.arraycopy(blocks0.data, start, tmp, 0, end - start);
            blocks0.data = tmp;
            blocks0.offset = 0;                      
        }
        // Increments capacity by adding new blocks.
        if ((min >> B1) > blocks.length) { // blocks array too small.
            Block<E>[] tmp = new Block[(min >> B1) * 2];
            System.arraycopy(blocks, 0, tmp, 0, blocks.length);
            blocks = tmp;
        }
        while (capacity < min) {
            capacity += C1;
            blocks[capacity >> B1] = new Block(C1); 
        }
    }

    // Ensures capacity - size < 2 * C1 (at most one full block empty).
    final void trimToSize() {
        while (capacity - size >= 2 * C1) {
            capacity -= C1;
            blocks[capacity >> B1] = null; 
        }
    }
    
    // Returns the "should be" position of the specified element in range [start, end] (ordered)
    final int indexIfOrderedOf(E element, int start, int length) {
        if (length == 0) return start;
        int half = length >> 1;
        return comparator().compare(element, get(start + half)) <= 0
                ? indexIfOrderedOf(element, start, half)
                : indexIfOrderedOf(element, start + half + 1, length - half - 1);
    }

}