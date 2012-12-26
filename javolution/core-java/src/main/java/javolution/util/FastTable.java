/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.RandomAccess;

/**
 * <p> This class represents a random access collection with fast 
 *     insertion/deletion and smooth capacity increase.</p>
 * 
 * <p> Instances of this class can advantageously replace {@link ArrayList}
 *     and {@link LinkedList} both in term of space and performance.</p>
 *     <img src="doc-files/list-add.png"/>
 *     
 *  <p> As for any {@link FastCollection} iterations are faster when performed
 *      using closures (and the notation will be shorter with JDK 8).
 *      [code]
 *      FastTable<Person> persons = ...
 *      Person john = persons.findFirst(new Functor<Person, Boolean>() {
 *          public Boolean evaluate(Person person) {
 *               return person.getName().equals("John");
 *          }
 *      });
 *      [/code]</p>
 *     
 *  <p> {@link FastTable} supports {@link #sort sorting} in place (quick sort) 
 *      using the {@link FastCollection#getComparator() comparator}
 *      for the table (no object or array allocation when sorting). 
 *      {@link FastCollection#isOrdered Ordered} sub-classes ensure that the 
 *      {@link #add} method inserts elements while keeping the collection
 *      ordered (unlike {@link #addLast} which always adds to the end).</p>
 * 
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 5.4.5, August 20, 2007
 */
public class FastTable<E> extends FastCollection<E> implements
        List<E>, Deque<E>, RandomAccess {

    
    
    // We do a full resize (and copy) only when the capacity is less than C1.
    // For large collections, multi-dimensional arrays are employed.
    private static final int B0 = 2; // Block initial capacity in bits.
    private static final int C0 = 1 << B0; // Block initial capacity (4)
    private static final int B1 = 8; // Block maximum capacity in bits.
    private static final int C1 = 1 << B1; // Block maximum capacity (256).
    private static final int M1 = C1 - 1; // Block Mask.

    // Element blocks.
    private static class Block<T> {

        int offset; // Allows for fast shift.
        T[] data = (T[]) new Object[C0];

        T get(int i) {
            return data[(i + offset) & M1];
        }

        void set(int i, T t) {
            data[(i + offset) & M1] = t;
        }

        void clear(int start, int end) {
            System.arraycopy(NULL_BLOCK, 0, data,, data.length);
        }
        private static final Object[] NULL_BLOCK = (Object[]) new Object[C1];
    }

    void copy(int srcPos, int srcDest, int length) {
    }
    /**
     * Holds the block elements.
     */
    private transient Block<E>[] blocks = new Block[]{new Block()};
    /**
     * Holds the current size.
     */
    private transient int size;
    /**
     * Holds the current capacity.
     */
    private transient int capacity = C0;

    /**
     * Creates a table of small initial capacity.
     */
    public FastTable() {
    }

    /**
     * Creates a table of specified initial capacity; unless the table size 
     * reaches the specified capacity, operations on this table will not 
     * allocate memory (no lazy object creation).
     * 
     * @param capacity the initial capacity.
     */
    public FastTable(int capacity) {
        this();
        ensureCapacity(capacity);
    }

    /**
     * Creates a table containing the specified elements (in the same order).
     *
     * @param that the elements  to be placed into this table.
     */
    public FastTable(Collection<? extends E> that) {
        this(that.size());
        addAll(that);
    }

    /**
     * Returns the element at the specified index.
     *
     * @param index index of element to return.
     * @return the element at the specified position in this list.
     * @throws IndexOutOfBoundsException if <code>(index < 0) || 
     *         (index >= size())</code>
     */
    public final E get(int index) { // Short to be inlined.
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
    public final E set(int index, E element) {
        if (index >= size)
            throw new IndexOutOfBoundsException();
        final Block<E> block = blocks[index >> B1];
        final E previous = block.get(index);
        block.set(index, element);
        return previous;
    }

    /**
     * For {@link FastCollection#isOrdered ordered} collection inserts the
     * specified element at the proper position to keep the collection ordered; 
     * otherwise (default) appends the specified element to the end of this table.
     *
     * @param element the element to be added to this table.
     * @return <code>true</code> (as per the general contract of the
     *         <code>Collection.add</code> method).
     */
    @Override
    public final boolean add(E element) {
        if (isOrdered()) {  
            addOrdered(element);
        } else {
            addLast(element);
        }
        return true;
    }

    /**
     * Returns the first element of this table.
     *
     * @return this table first element.
     * @throws NoSuchElementException if this table is empty.
     */
    public final E getFirst() {
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
    public final E getLast() {
        if (size == 0)
            throw new NoSuchElementException();
        return get(size - 1);
    }

    /**
     * Appends the specified element to the beginning of this table <i>(fast)</i>.
     * 
     * @param element the element to be added.
     */
    public final void addFirst(E element) {
        add(0, element);
    }

    /**
     * Appends the specified element to the end of this table <i>(fast)</i>.
     * 
     * @param element the element to be added.
     */
    public final void addLast(E element) {
        if (size >= capacity)
            ensureCapacity(size + 1);
        blocks[size >> B1].set(size++, element);
    }

    /**
     * Removes and returns the first element of this table <i>(fast)</i>.
     *
     * @return this table's last element before this call.
     * @throws NoSuchElementException if this table is empty.
     */
    public final E removeFirst() {
        return remove(0);
    }

    /**
     * Removes and returns the last element of this table <i>(fast)</i>.
     *
     * @return this table's last element before this call.
     * @throws NoSuchElementException if this table is empty.
     */
    public final E removeLast() {
        if (size == 0)
            throw new NoSuchElementException();
        size--;
        final Block<E> block = blocks[size >> B1];
        final E previous = block.get(size);
        block.set(size, null);
        return previous;
    }

    @Override
    public final void clear() {
        int n = (size - 1) >> B1;
        for (int i = 0; i <= n; i++) {
            blocks[i].clear();
        }
        size = 0;
    }

    /**
     * Inserts all of the elements in the specified collection into this
     * table at the specified position. Shifts the element currently at that
     * position (if any) and any subsequent elements to the right 
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
    public final boolean addAll(int index, Collection<? extends E> elements) {
        if ((index < 0) || (index > size))
            throw new IndexOutOfBoundsException("index: " + index);
        final int shift = elements.size();
        shiftRight(index, shift);
        Iterator<? extends E> elementsIterator = elements.iterator();
        for (int i = index, n = index + shift; i < n; i++) {
            blocks[i >> B1].set(i, elementsIterator.next());
        }
        size += shift;
        return shift != 0;
    }

    /**
     * Inserts the specified element at the specified position in this table.
     * Shifts the element currently at that position
     * (if any) and any subsequent elements to the right (adds one to their
     * indices).
     *
     * <p>Note: If this method is used concurrent access must be synchronized
     *          (the table is no more thread-safe).</p>
     *
     * @param index the index at which the specified element is to be inserted.
     * @param element the element to be inserted.
     * @throws IndexOutOfBoundsException if <code>(index < 0) || 
     *         (index > size())</code>
     */
    public final void add(int index, E element) {
        if ((index < 0) || (index > size))
            throw new IndexOutOfBoundsException("index: " + index);
        shiftRight(index, 1);
        blocks[index >> B1].set(index, element);
        size++;
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
    public final E remove(int index) {
        final E previous = get(index);
        shiftLeft(index + 1, 1);
        size--;
        blocks[size >> B1].set(size, null);
        return previous;
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
    public final void removeRange(int fromIndex, int toIndex) {
        if ((fromIndex < 0) || (toIndex < 0) || (fromIndex > toIndex)
                || (toIndex > size))
            throw new IndexOutOfBoundsException("FastTable removeRange("
                    + fromIndex + ", " + toIndex + ") index out of bounds, size: " + size);
        final int shift = toIndex - fromIndex;
        shiftLeft(toIndex, shift);
        size -= shift; // No need for volatile, removal are not thread-safe.
        for (int i = size, n = size + shift; i < n; i++) {
            blocks[i >> B1].set(i, null);
        }
    }

    /**
     * Returns the index in this table of the first occurrence of the specified
     * element, or -1 if this table does not contain this element.
     *
     * @param element the element to search for.
     * @return the index in this table of the first occurrence of the specified
     *         element, or -1 if this table does not contain this element.
     */
    public final int indexOf(Object element) {
        final FastComparator comp = this.getComparator();
        for (int i = 0; i < size; i++) {
            if (comp.areEqual(element, blocks[i >> B1].get(i))) return i;
        }
        return -1;
    }

    /**
     * Returns the index in this table of the last occurrence of the specified
     * element, or -1 if this table does not contain this element.
     *
     * @param element the element to search for.
     * @return the index in this table of the last occurrence of the specified
     *         element, or -1 if this table does not contain this element.
     */
    public final int lastIndexOf(Object element) {
        final FastComparator comp = this.getComparator();
        for (int i = size - 1; i >= 0; i--) {
            if (comp.areEqual(element, blocks[i >> B1].get(i))) return i;
        }
        return -1;
    }

    /**
     * Returns an iterator over the elements in this list 
     * (allocated on the stack when executed in a 
     * {@link javolution.context.StackContext StackContext}).
     *
     * @return an iterator over this list elements.
     */
    public Iterator<E> iterator() {
        return FastTableIterator.valueOf(this, 0, 0, size);
    }

    /**
     * Returns a list iterator over the elements in this list 
     * (allocated on the stack when executed in a 
     * {@link javolution.context.StackContext StackContext}).
     *
     * @return an iterator over this list values.
     */
    public ListIterator<E> listIterator() {
        return FastTableIterator.valueOf(this, 0, 0, size);
    }

    /**
     * Returns a list iterator from the specified position
     * (allocated on the stack when executed in a 
     * {@link javolution.context.StackContext StackContext}).
     * The list iterator being returned does not support insertion/deletion.
     * 
     * @param index the index of first value to be returned from the
     *        list iterator (by a call to the <code>next</code> method).
     * @return a list iterator of the elements in this table
     *         starting at the specified position in this list.
     * @throws IndexOutOfBoundsException if the index is out of range 
     *         [code](index < 0 || index > size())[/code]
     */
    public ListIterator<E> listIterator(int index) {
        if ((index < 0) || (index > size))
            throw new IndexOutOfBoundsException();
        return FastTableIterator.valueOf(this, index, 0, size);
    }

    /**
     * Returns a view of the portion of this list between the specified
     * indexes (instance of {@link FastList} allocated from the "stack" when
     * executing in a {@link javolution.context.StackContext StackContext}).
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
     * @throws IndexOutOfBoundsException if [code](fromIndex < 0 ||
     *          toIndex > size || fromIndex > toIndex)[/code]
     */
    public List<E> subList(int fromIndex, int toIndex) {
        if ((fromIndex < 0) || (toIndex > size) || (fromIndex > toIndex))
            throw new IndexOutOfBoundsException("fromIndex: " + fromIndex
                    + ", toIndex: " + toIndex + " for list of size: " + size);
        return SubTable.valueOf(this, fromIndex, toIndex - fromIndex);
    }

    /**
     * Sorts this table in place (quick sort) using this table 
     * {@link FastCollection#getComparator() comparator}
     * (smallest first).
     * 
     * @return <code>this</code>
     */
    public FastTable<E> sort() {
        if (size > 1) {
            quicksort(0, size - 1, this.getComparator());
        }
        return this;
    }
    
    // From Wikipedia Quick Sort - http://en.wikipedia.org/wiki/Quicksort
    //
    private void quicksort(int first, int last, FastComparator cmp) {
        int pivIndex = 0;
        if (first < last) {
            pivIndex = partition(first, last, cmp);
            quicksort(first, (pivIndex - 1), cmp);
            quicksort((pivIndex + 1), last, cmp);
        }
    }

    private int partition(int f, int l, FastComparator cmp) {
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

    @Override
    public final int size() {
        return size;
    }

    @Override
    public final boolean contains(Object element) {
        return indexOf(element) >= 0;
    }

    // Requires special handling during de-serialization process.
    private void readObject(ObjectInputStream stream) throws IOException,
            ClassNotFoundException {
        final int readSize = stream.readInt();
        for (int i = 0; i < readSize; i++) {
            addLast((E) stream.readObject());
        }
    }

    // Requires special handling during serialization process.
    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.writeInt(size);
        for (int i = 0; i < size; i++) {
            stream.writeObject(get(i));
        }
    }

    /**
     * Increases this table capacity.
     */
    private void ensureCapacity(int capacity) {
        if (this.capacity >= capacity) return;


        final Block<E> block = blocks[size >> B1];


        MemoryArea.getMemoryArea(this).executeInArea(new Runnable() {
            public void run() {
                if (_capacity < C1) { // For small capacity, resize.
                    _capacity <<= 1;
                    E[] tmp = (E[]) new Object[_capacity];
                    System.arraycopy(_low, 0, tmp, 0, _size);
                    _low = tmp;
                    _high[0] = tmp;
                } else { // Add a new low block of 1024 elements.
                    int j = _capacity >> B1;
                    if (j >= _high.length) { // Resizes _high.
                        E[][] tmp = (E[][]) new Object[_high.length * 2][];
                        System.arraycopy(_high, 0, tmp, 0, _high.length);
                        _high = tmp;
                    }
                    _high[j] = (E[]) new Object[C1];
                    _capacity += C1;
                }
            }
        });
    }

    /**
     * This inner class implements a sub-table.
     */
    private static final class SubTable extends FastCollection implements List,
            RandomAccess {

        private static final ObjectFactory FACTORY = new ObjectFactory() {
            protected Object create() {
                return new SubTable();
            }

            protected void cleanup(Object obj) {
                SubTable st = (SubTable) obj;
                st._table = null;
            }
        };
        private FastTable _table;
        private int _offset;
        private int _size;

        public static SubTable valueOf(FastTable table, int offset, int size) {
            SubTable subTable = (SubTable) FACTORY.object();
            subTable._table = table;
            subTable._offset = offset;
            subTable._size = size;
            return subTable;
        }

        public int size() {
            return _size;
        }

        public boolean addAll(int index, Collection values) {
            throw new UnsupportedOperationException(
                    "Insertion not supported, thread-safe collections.");
        }

        public Object get(int index) {
            if ((index < 0) || (index >= _size))
                throw new IndexOutOfBoundsException("index: " + index);
            return _table.get(index + _offset);
        }

        public Object set(int index, Object value) {
            if ((index < 0) || (index >= _size))
                throw new IndexOutOfBoundsException("index: " + index);
            return _table.set(index + _offset, value);
        }

        public void add(int index, Object element) {
            throw new UnsupportedOperationException(
                    "Insertion not supported, thread-safe collections.");
        }

        public Object remove(int index) {
            throw new UnsupportedOperationException(
                    "Deletion not supported, thread-safe collections.");
        }

        public int indexOf(Object value) {
            final FastComparator comp = _table.getValueComparator();
            for (int i = -1; ++i < _size;) {
                if (comp.areEqual(value, _table.get(i + _offset)))
                    return i;
            }
            return -1;
        }

        public int lastIndexOf(Object value) {
            final FastComparator comp = _table.getValueComparator();
            for (int i = _size; --i >= 0;) {
                if (comp.areEqual(value, _table.get(i + _offset)))
                    return i;
            }
            return -1;
        }

        public ListIterator listIterator() {
            return listIterator(0);
        }

        public ListIterator listIterator(int index) {
            if ((index >= 0) && (index <= _size)) {
                return FastTableIterator.valueOf(_table, index + _offset,
                        _offset, _offset + _size);
            } else {
                throw new IndexOutOfBoundsException("index: " + index
                        + " for table of size: " + _size);
            }
        }

        public List subList(int fromIndex, int toIndex) {
            if ((fromIndex < 0) || (toIndex > _size) || (fromIndex > toIndex))
                throw new IndexOutOfBoundsException("fromIndex: " + fromIndex
                        + ", toIndex: " + toIndex + " for list of size: "
                        + _size);
            return SubTable.valueOf(_table, _offset + fromIndex, toIndex
                    - fromIndex);
        }
    }

    /**
     * This inner class implements a fast table iterator.
     */
    private static final class FastTableIterator implements ListIterator {

        private static final ObjectFactory FACTORY = new ObjectFactory() {
            protected Object create() {
                return new FastTableIterator();
            }

            protected void cleanup(Object obj) {
                FastTableIterator i = (FastTableIterator) obj;
                i._table = null;
                i._low = null;
                i._high = null;
            }
        };
        private FastTable _table;
        private int _currentIndex;
        private int _start; // Inclusive.
        private int _end; // Exclusive.
        private int _nextIndex;
        private Object[] _low;
        private Object[][] _high;

        public static FastTableIterator valueOf(FastTable table, int nextIndex,
                int start, int end) {
            FastTableIterator iterator = (FastTableIterator) FACTORY.object();
            iterator._table = table;
            iterator._start = start;
            iterator._end = end;
            iterator._nextIndex = nextIndex;
            iterator._low = table._low;
            iterator._high = table._high;
            iterator._currentIndex = -1;
            return iterator;
        }

        public boolean hasNext() {
            return (_nextIndex != _end);
        }

        public Object next() {
            if (_nextIndex == _end)
                throw new NoSuchElementException();
            final int i = _currentIndex = _nextIndex++;
            return i < C1 ? _low[i] : _high[i >> B1][i & M1];
        }

        public int nextIndex() {
            return _nextIndex;
        }

        public boolean hasPrevious() {
            return _nextIndex != _start;
        }

        public Object previous() {
            if (_nextIndex == _start)
                throw new NoSuchElementException();
            final int i = _currentIndex = --_nextIndex;
            return i < C1 ? _low[i] : _high[i >> B1][i & M1];
        }

        public int previousIndex() {
            return _nextIndex - 1;
        }

        public void add(Object o) {
            _table.add(_nextIndex++, o);
            _end++;
            _currentIndex = -1;
        }

        public void set(Object o) {
            if (_currentIndex >= 0) {
                _table.set(_currentIndex, o);
            } else {
                throw new IllegalStateException();
            }
        }

        public void remove() {
            if (_currentIndex >= 0) {
                _table.remove(_currentIndex);
                _end--;
                if (_currentIndex < _nextIndex) {
                    _nextIndex--;
                }
                _currentIndex = -1;
            } else {
                throw new IllegalStateException();
            }
        }
    }

    // Shifts element from the specified index to the right (higher indexes). 
    private void shiftRight(int index, int shift) {
        while (_size + shift >= _capacity) {
            increaseCapacity();
        }
        for (int i = _size; --i >= index;) {
            final int dest = i + shift;
            _high[dest >> B1][dest & M1] = _high[i >> B1][i & M1];
        }
    }

    // Shifts element from the specified index to the left (lower indexes). 
    private void shiftLeft(int index, int shift) {
        for (int i = index; i < _size; i++) {
            final int dest = i - shift;
            _high[dest >> B1][dest & M1] = _high[i >> B1][i & M1];
        }
    }

    // For inlining of default comparator. 
    private static boolean defaultEquals(Object o1, Object o2) {
        return (o1 == null) ? (o2 == null) : (o1 == o2) || o1.equals(o2);
    }
    private static final long serialVersionUID = 1L;
}