/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2005 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util;

import java.io.IOException;
import java.util.NoSuchElementException;

import j2me.io.ObjectInputStream;
import j2me.io.ObjectOutputStream;
import j2me.lang.IllegalStateException;
import j2me.lang.UnsupportedOperationException;
import j2me.util.Collection;
import j2me.util.Iterator;
import j2me.util.List;
import j2me.util.ListIterator;
import j2me.util.RandomAccess;
import j2mex.realtime.MemoryArea;
import javolution.context.PersistentContext;
import javolution.context.RealtimeObject;
import javolution.lang.Reusable;

/**
 * <p> This class represents a random access collection with real-time behavior; 
 *     smooth capacity increase (no array resize/copy ever) and no memory 
 *     allocation as long as the collection size does not exceed its initial
 *     capacity.</p>
 *     <img src="doc-files/list-add.png"/>
 *     
 * <p> This class has the following advantages over the widely used 
 *     <code>java.util.ArrayList</code>:<ul>
 *     <li> Faster when the capacity is unknown (default constructor) as no 
 *          array resize/copy is ever performed.</li>
 *     <li> No large array allocation (for large collections multi-dimensional
 *          arrays are employed). Does not stress the garbage collector with
 *          large chunk of memory to allocate (likely to trigger a
 *          full garbage collection due to memory fragmentation).</li>
 *     <li> Support concurrent access/iteration without synchronization if the 
 *          collection values are not removed/inserted (Ref. 
 *          {@link javolution.util} discussion).</li>
 *     </ul></p>
 *     
 *  <p> Iterations over the {@link FastTable} values are faster when
 *      performed using the {@link #get} method rather than using collection
 *      records or iterators:[code]
 *     for (int i = 0, n = table.size(); i < n; i++) {
 *          table.get(i);
 *     }[/code]</p>
 *     
 *  <p> {@link FastTable} supports {@link #sort sorting} in place (quick sort) 
 *      using the {@link FastCollection#getValueComparator() value comparator}
 *      for the table (no object or array allocation when sorting).</p> 
 * 
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 4.2, December 18, 2006
 */
public class FastTable/*<E>*/extends FastCollection/*<E>*/implements
        List/*<E>*/, Reusable, RandomAccess {

    /**
     * Holds the factory for this fast table.
     */
    private static final Factory FACTORY = new Factory() {
        public Object create() {
            return new FastTable();
        }

        public void cleanup(Object obj) {
            ((FastTable) obj).reset();
        }
    };

    //
    // Holds the arrays. The array sizes are adjusted to ensures that
    // no more than 4 time the required space is ever allocated.
    //
    // elems[1<<D3][1<<D2][1<<D1][1<<D0]
    // with get(i) = elems[(i>>R3)&M3][(i>>R2)&M2][(i>>R1)&M1][(i>>R0)&M0]
    // 

    private static final int D0 = 5;

    private static final int M0 = (1 << D0) - 1;

    private static final int C0 = 1 << D0; // capacity chars0

    private static final int D1 = D0 + 2;

    private static final int R1 = D0;

    private static final int M1 = (1 << D1) - 1;

    private static final int C1 = 1 << (D0 + D1); // capacity elems1

    private static final int D2 = D1 + 2;

    private static final int R2 = D0 + D1;

    private static final int M2 = (1 << D2) - 1;

    private static final int C2 = 1 << (D0 + D1 + D2); // capacity elems2

    private static final int D3 = D2 + 2;

    private static final int R3 = D0 + D1 + D2;

    // new Object[1<<7][1<<5], 12 bits (4096)
    private transient Object/*{E}*/[][] _elems1;

    // new Object[1<<9][1<<7][1<<5], 21 bits (2097152)
    private transient Object/*{E}*/[][][] _elems2;

    // new Object[1<<11][1<<9][1<<7][1<<5], 32 bits
    private transient Object/*{E}*/[][][][] _elems3;

    /**
     * Holds the current capacity. 
     */
    private transient int _capacity = C0;

    /**
     * Holds the current size.
     */
    private transient int _size;

    /**
     * Holds the value comparator.
     */
    private transient FastComparator/*<? super E>*/ _valueComparator = FastComparator.DEFAULT;

    /**
     * Creates a table of small initial capacity.
     */
    public FastTable() {
        _elems1 = (Object/*{E}*/[][]) new Object[1][];
        _elems1[0] = (Object/*{E}*/[]) new Object[C0];
    }

    /**
     * Creates a persistent table associated to the specified unique identifier
     * (convenience method).
     * 
     * @param id the unique identifier for this map.
     * @throws IllegalArgumentException if the identifier is not unique.
     * @see javolution.context.PersistentContext.Reference
     */
    public FastTable(String id) {
        this();
        new PersistentContext.Reference(id, this) {
            protected void notifyChange() {
                FastTable.this.clear();
                FastTable.this.addAll((FastList) this.get());
            }
        };
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
        while (capacity > _capacity) {
            increaseCapacity();
        }
    }

    /**
     * Creates a table containing the specified values, in the order they
     * are returned by the collection's iterator.
     *
     * @param values the values to be placed into this table.
     */
    public FastTable(Collection/*<? extends E>*/values) {
        this(values.size());
        addAll(values);
    }

    /**
     * Returns a new, preallocated or {@link #recycle recycled} table instance
     * (on the stack when executing in a {@link javolution.context.PoolContext
     * PoolContext}).
     *
     * @return a new, preallocated or recycled table instance.
     */
    public static/*<E>*/FastTable/*<E>*/newInstance() {
        return (FastTable/*<E>*/) FACTORY.object();
    }

    /**
     * Recycles a table {@link #newInstance() instance} immediately
     * (on the stack when executing in a {@link javolution.context.PoolContext
     * PoolContext}). 
     */
    public static void recycle(FastTable instance) {
        FACTORY.recycle(instance);
    }

    /**
     * Returns the current capacity of this table.
     *
     * @return this table's capacity.
     */
    protected final int getCapacity() {
        return _capacity;
    }

    /**
     * Returns the element at the specified index.
     *
     * @param index index of value to return.
     * @return the value at the specified position in this list.
     * @throws IndexOutOfBoundsException if <code>(index < 0) || 
     *         (index >= size())</code>
     */
    public final Object/*{E}*/get(int index) { // Short to be inlined.
        if (((index >> R2) == 0) && (index < _size))
            return _elems1[(index >> R1)][index & M0];
        return get2(index);
    }

    private final Object/*{E}*/get2(int index) {
        if ((index < 0) || (index >= _size))
            throw new IndexOutOfBoundsException("index: " + index);
        return (index < C2) ? _elems2[(index >> R2)][(index >> R1) & M1][index
                & M0]
                : _elems3[(index >> R3)][(index >> R2) & M2][(index >> R1) & M1][index
                        & M0];
    }

    /**
     * Replaces the value at the specified position in this table with the
     * specified value.
     *
     * @param index index of value to replace.
     * @param value value to be stored at the specified position.
     * @throws IndexOutOfBoundsException if <code>(index < 0) || 
     *         (index >= size())</code>
     */
    public final Object/*{E}*/set(int index, Object/*{E}*/value) {
        if ((index < 0) || (index >= _size))
            throw new IndexOutOfBoundsException("index: " + index);
        final Object/*{E}*/[] elems = (index < C1) ? _elems1[(index >> R1)]
                : (index < C2) ? _elems2[(index >> R2)][(index >> R1) & M1]
                        : _elems3[(index >> R3)][(index >> R2) & M2][(index >> R1)
                                & M1];
        final Object/*{E}*/oldValue = elems[index & M0];
        elems[index & M0] = value;
        return oldValue;
    }

    /**
     * Appends the specified value to the end of this table.
     *
     * @param value the value to be appended to this table.
     * @return <code>true</code> (as per the general contract of the
     *         <code>Collection.add</code> method).
     */
    public final boolean add(Object/*{E}*/value) {
        final int i = _size;
        if (i >= _capacity) {
            increaseCapacity();
        }
        final Object/*{E}*/[] elems = (i < C1) ? _elems1[(i >> R1)]
                : (i < C2) ? _elems2[(i >> R2)][(i >> R1) & M1]
                        : _elems3[(i >> R3)][(i >> R2) & M2][(i >> R1) & M1];
        elems[i & M0] = value;
        // The size increment is always performed last (the compiler will not 
        // reorder due to possible IndexOutOfBoundsException above).
        _size++;
        return true;
    }

    /**
     * Returns the first value of this table.
     *
     * @return this table first value.
     * @throws NoSuchElementException if this table is empty.
     */
    public final Object/*{E}*/getFirst() {
        if (_size == 0)
            throw new NoSuchElementException();
        return _elems1[0][0];
    }
    
    /**
     * Returns the last value of this table.
     *
     * @return this table last value.
     * @throws NoSuchElementException if this table is empty.
     */
    public final Object/*{E}*/getLast() {
        if (_size == 0)
            throw new NoSuchElementException();
        final int i = _size - 1;
        final Object/*{E}*/[] elems = (i < C1) ? _elems1[(i >> R1)]
                : (i < C2) ? _elems2[(i >> R2)][(i >> R1) & M1]
                        : _elems3[(i >> R3)][(i >> R2) & M2][(i >> R1) & M1];
        return elems[i & M0];
    }

    /**
     * Appends the specified value to the end of this table <i>(fast)</i>.
     * 
     * @param value the value to be added.
     */
    public final void addLast(Object/*{E}*/value) {
        add(value);
    }

    /**
     * Removes and returns the last value of this table <i>(fast)</i>.
     *
     * @return this table's last value before this call.
     * @throws NoSuchElementException if this table is empty.
     */
    public final Object/*{E}*/removeLast() {
        if (_size == 0)
            throw new NoSuchElementException();
        final int i = --_size;
        final Object/*{E}*/[] elems = (i < C1) ? _elems1[(i >> R1)]
                : (i < C2) ? _elems2[(i >> R2)][(i >> R1) & M1]
                        : _elems3[(i >> R3)][(i >> R2) & M2][(i >> R1) & M1];
        final Object/*{E}*/oldValue = elems[i & M0];
        elems[i & M0] = null;
        return oldValue;
    }

    // Overrides.
    public final void clear() {
        final int size = _size;
        _size = 0;
        final int blockSize = Math.min(size, C0);
        for (int i = 0; i < size; i += C0) {
            final Object/*{E}*/[] elems = (i < C1) ? _elems1[(i >> R1)]
                    : (i < C2) ? _elems2[(i >> R2)][(i >> R1) & M1]
                            : _elems3[(i >> R3)][(i >> R2) & M2][(i >> R1) & M1];
            System.arraycopy(NULL_BLOCK, 0, elems, 0, blockSize);
        }
    }

    private static final Object[] NULL_BLOCK = (Object[]) new Object[C0];    

    /**
     * Inserts all of the values in the specified collection into this
     * table at the specified position. Shifts the value currently at that
     * position (if any) and any subsequent values to the right 
     * (increases their indices). 
     *
     * @param index the index at which to insert first value from the specified
     *        collection.
     * @param values the values to be inserted into this list.
     * @return <code>true</code> if this list changed as a result of the call;
     *         <code>false</code> otherwise.
     * @throws IndexOutOfBoundsException if <code>(index < 0) || 
     *         (index > size())</code>
     */
    public final boolean addAll(int index, Collection/*<? extends E>*/values) {
        if ((index < 0) || (index > _size))
            throw new IndexOutOfBoundsException("index: " + index);
        final int shift = values.size();
        final int prevSize = _size;
        final int newSize = prevSize + shift;
        while (newSize >= _capacity) {
            increaseCapacity();
        }
        _size = newSize; // Set here to avoid index error.
        // Shift values after index (TBD: Optimize).
        for (int i = prevSize; --i >= index;) {
            this.set(i + shift, this.get(i));
        }
        Iterator/*<? extends E>*/valuesIterator = values.iterator();
        for (int i = index, n = index + shift; i < n; i++) {
            this.set(i, valuesIterator.next());
        }
        return shift != 0;
    }

    /**
     * Inserts the specified value at the specified position in this table.
     * Shifts the value currently at that position
     * (if any) and any subsequent values to the right (adds one to their
     * indices).
     *
     * @param index the index at which the specified value is to be inserted.
     * @param value the value to be inserted.
     * @throws IndexOutOfBoundsException if <code>(index < 0) || 
     *         (index > size())</code>
     */
    public final void add(int index, Object/*{E}*/value) {
        if ((index < 0) || (index > _size))
            throw new IndexOutOfBoundsException("index: " + index);
        final int prevSize = _size;
        final int newSize = prevSize + 1;
        if (newSize >= _capacity) {
            increaseCapacity();
        }
        _size = newSize;
        for (int i = index, n = newSize; i < n;) {
            value = this.set(i++, value);
        }
    }

    /**
     * Removes the value at the specified position from this table.
     * Shifts any subsequent values to the left (subtracts one
     * from their indices). Returns the value that was removed from the
     * table.
     *
     * @param index the index of the value to removed.
     * @return the value previously at the specified position.
     * @throws IndexOutOfBoundsException if <code>(index < 0) || 
     *         (index >= size())</code>
     */
    public final Object/*{E}*/remove(int index) {
        if ((index < 0) || (index >= _size))
            throw new IndexOutOfBoundsException("index: " + index);
        final int lastIndex = _size - 1;
        Object/*{E}*/obj = this.get(lastIndex);
        for (int i = lastIndex; --i >= index;) {
            obj = this.set(i, obj);
        }
        this.set(lastIndex, null); // For GC to do its work.
        _size = lastIndex;
        return obj;
    }

    /**
     * Removes the values between <code>[fromIndex..toIndex[<code> from
     * this table.
     * 
     * @param fromIndex the beginning index, inclusive.
     * @param toIndex the ending index, exclusive.
     * @throws IndexOutOfBoundsException if <code>(fromIndex < 0) || (toIndex < 0) 
     *         || (fromIndex > toIndex) || (toIndex > this.size())</code>
     */
    public final void removeRange(int fromIndex, int toIndex) {
        final int prevSize = _size;
        if ((fromIndex < 0) || (toIndex < 0) || (fromIndex > toIndex)
                || (toIndex > prevSize))
            throw new IndexOutOfBoundsException();
        for (int i = toIndex, j = fromIndex; i < prevSize;) {
            this.set(j++, this.get(i++));
        }
        final int newSize = prevSize - toIndex + fromIndex;
        for (int i = newSize; i < prevSize;) {
            this.set(i++, null); // For GC to do its work.
        }
        _size = newSize;
    }

    /**
     * Returns the index in this table of the first occurrence of the specified
     * value, or -1 if this table does not contain this value.
     *
     * @param value the value to search for.
     * @return the index in this table of the first occurrence of the specified
     *         value, or -1 if this table does not contain this value.
     */
    public final int indexOf(Object value) {
        final FastComparator comp = this.getValueComparator();
        for (int i = -1; ++i < _size;) {
            if (comp.areEqual(value, get(i)))
                return i;
        }
        return -1;
    }

    /**
     * Returns the index in this table of the last occurrence of the specified
     * value, or -1 if this table does not contain this value.
     *
     * @param value the value to search for.
     * @return the index in this table of the last occurrence of the specified
     *         value, or -1 if this table does not contain this value.
     */
    public final int lastIndexOf(Object value) {
        final FastComparator comp = this.getValueComparator();
        for (int i = _size; --i >= 0;) {
            if (comp.areEqual(value, get(i)))
                return i;
        }
        return -1;
    }

    /**
     * Returns an iterator over the elements in this list 
     * (allocated on the stack when executed in a 
     * {@link javolution.context.PoolContext PoolContext}).
     *
     * @return an iterator over this list values.
     */
    public Iterator/*<E>*/iterator() {
        FastTableIterator i = (FastTableIterator) FastTableIterator.FACTORY
                .object();
        i._table = this;
        i._start = 0;
        i._end = this._size;
        i._nextIndex = 0;
        i._currentIndex = -1;
        return i;
    }

    /**
     * Returns a list iterator over the elements in this list 
     * (allocated on the stack when executed in a 
     * {@link javolution.context.PoolContext PoolContext}).
     *
     * @return an iterator over this list values.
     */
    public ListIterator/*<E>*/listIterator() {
        FastTableIterator i = (FastTableIterator) FastTableIterator.FACTORY
                .object();
        i._table = this;
        i._start = 0;
        i._end = this._size;
        i._nextIndex = 0;
        i._currentIndex = -1;
        return i;
    }

    /**
     * Returns a list iterator from the specified position
     * (allocated on the stack when executed in a 
     * {@link javolution.context.PoolContext PoolContext}).
     * The list iterator being returned does not support insertion/deletion.
     * 
     * @param index the index of first value to be returned from the
     *        list iterator (by a call to the <code>next</code> method).
     * @return a list iterator of the values in this table
     *         starting at the specified position in this list.
     * @throws IndexOutOfBoundsException if the index is out of range 
     *         [code](index < 0 || index > size())[/code]
     */
    public ListIterator/*<E>*/listIterator(int index) {
        if ((index >= 0) && (index <= _size)) {
            FastTableIterator i = (FastTableIterator) FastTableIterator.FACTORY
                    .object();
            i._table = this;
            i._start = 0;
            i._end = this._size;
            i._nextIndex = index;
            i._currentIndex = -1;
            return i;
        } else {
            throw new IndexOutOfBoundsException("index: " + index
                    + " for table of size: " + _size);
        }
    }

    /**
     * Returns a view of the portion of this list between the specified
     * indexes (instance of {@link FastList} allocated from the "stack" when
     * executing in a {@link javolution.context.PoolContext PoolContext}).
     * If the specified indexes are equal, the returned list is empty. 
     * The returned list is backed by this list, so non-structural changes in
     * the returned list are reflected in this list, and vice-versa. 
     *
     * This method eliminates the need for explicit range operations (of
     * the sort that commonly exist for arrays). Any operation that expects
     * a list can be used as a range operation by passing a subList view
     * instead of a whole list.  For example, the following idiom
     * removes a range of values from a list: [code]
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
    public final List/*<E>*/subList(int fromIndex, int toIndex) {
        if ((fromIndex < 0) || (toIndex > _size) || (fromIndex > toIndex))
            throw new IndexOutOfBoundsException("fromIndex: " + fromIndex
                    + ", toIndex: " + toIndex + " for list of size: " + _size);
        SubTable st = (SubTable) SubTable.FACTORY.object();
        st._table = this;
        st._offset = fromIndex;
        st._size = toIndex - fromIndex;
        return st;
    }

    /**
     * Reduces the capacity of this table to the current size (minimize 
     * storage space).
     */
    public final void trimToSize() {
        while (_capacity > _size + C0) {
            decreaseCapacity();
        }
    }

    /**
     * Sorts this table in place (quick sort) using this table 
     * {@link FastCollection#getValueComparator() value comparator}.
     */
    public final void sort() {
        if (_size > 1) {
            quicksort(0, _size - 1, this.getValueComparator());
        }
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
        Object/*{E}*/ piv = this.get(f);
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
                Object/*{E}*/ temp = get(up);
                set(up, get(down));
                set(down, temp);
            }
        } while (down > up);
        set(f, get(down));
        set(down, piv);
        return down;
    }


    /**
     * Sets the comparator to use for value equality.
     *
     * @param comparator the value comparator.
     * @return <code>this</code>
     */
    public FastTable/*<E>*/ setValueComparator(FastComparator/*<? super E>*/ comparator) {
        _valueComparator = comparator;
        return this;
    }
    
    // Overrides.
    public FastComparator/*<? super E>*/ getValueComparator() {
        return _valueComparator;
    }
    
    // Implements FastCollection abstract method.
    public final int size() {
        return _size;
    }

    // Implements FastCollection abstract method.
    public final Record head() {
        return Index.valueOf(-1);
    }

    // Implements FastCollection abstract method.
    public final Record tail() {
        return Index.valueOf(_size);
    }

    // Implements FastCollection abstract method.
    public final Object/*{E}*/valueOf(Record record) {
        return get(((Index) record).intValue());
    }

    // Implements FastCollection abstract method.
    public final void delete(Record record) {
        remove(((Index) record).intValue());
    }

    // Overrides  to return a list (JDK1.5+).
    public Collection/*List<E>*/unmodifiable() {
        return (Collection/*List<E>*/) super.unmodifiable();
    }

    // Requires special handling during de-serialization process.
    private void readObject(ObjectInputStream stream) throws IOException,
            ClassNotFoundException {
        // Default setup.
        _capacity = C0;
        _elems1 = (Object/*{E}*/[][]) new Object[1][];
        _elems1[0] = (Object/*{E}*/[]) new Object[C0];
        
        setValueComparator((FastComparator) stream.readObject());
        final int size = stream.readInt();
        for (int i = 0; i < size; i++) {
            addLast((Object/*{E}*/) stream.readObject());
        }
    }

    // Requires special handling during serialization process.
    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.writeObject(getValueComparator());
        final int size = _size;
        stream.writeInt(size);
        for (int i = 0; i < size; i++) {
            stream.writeObject(get(i));
        }
    }

    /**
     * Increases this table capacity.
     */
    protected void increaseCapacity() {
        MemoryArea.getMemoryArea(this).executeInArea(new Runnable() {
            public void run() {
                final int c = _capacity;
                _capacity += C0;
                if (c < C1) {
                    if (_elems1.length == 1) { // Replaces the original table.
                        Object/*{E}*/[][] tmp = (Object/*{E}*/[][]) new Object[1 << D1][];
                        tmp[0] = _elems1[0];
                        _elems1 = tmp;
                    }
                    _elems1[(c >> R1)] = (Object/*{E}*/[]) new Object[1 << D0];

                } else if (c < C2) {
                    if (_elems2 == null) {
                        _elems2 = (Object/*{E}*/[][][]) new Object[1 << D2][][];
                    }
                    if (_elems2[(c >> R2)] == null) {
                        _elems2[(c >> R2)] = (Object/*{E}*/[][]) new Object[1 << D1][];
                    }
                    _elems2[(c >> R2)][(c >> R1) & M1] = (Object/*{E}*/[]) new Object[1 << D0];

                } else {
                    if (_elems3 == null) {
                        _elems3 = (Object/*{E}*/[][][][]) new Object[D3][][][];
                    }
                    if (_elems3[(c >> R3)] == null) {
                        _elems3[(c >> R3)] = (Object/*{E}*/[][][]) new Object[D2][][];
                    }
                    if (_elems3[(c >> R3)][(c >> R2) & M2] == null) {
                        _elems3[(c >> R3)][(c >> R2) & M2] = (Object/*{E}*/[][]) new Object[D1][];
                    }
                    _elems3[(c >> R3)][(c >> R2) & M2][(c >> R1) & M1] = (Object/*{E}*/[]) new Object[D0];
                }
            }
        });
    }

    /**
     * Decreases this table capacity.
     */
    protected void decreaseCapacity() {
        if (_size >= _capacity - C0) return;
        final int c = _capacity;
        _capacity -= C0;
        if (c < C1) {
            _elems1[(c >> R1)] = null;
            _elems2 = null;
            _elems3 = null;
        } else if (c < C2) {
            _elems2[(c >> R2)][(c >> R1) & M1] = null;
            _elems3 = null;
        } else {
            _elems3[(c >> R3)][(c >> R2) & M2][(c >> R1) & M1] = null;
        }
    }

    /**
     * This inner class implements a fast table iterator.
     */
    private static final class FastTableIterator extends RealtimeObject
            implements ListIterator {

        private static final Factory FACTORY = new Factory() {
            protected Object create() {
                return new FastTableIterator();
            }

            protected void cleanup(Object obj) {
                FastTableIterator i = (FastTableIterator) obj;
                i._table = null;
            }
        };

        private FastTable _table;

        private int _currentIndex;

        private int _start; // Inclusive.

        private int _end; // Exclusive.

        private int _nextIndex;

        public boolean hasNext() {
            return (_nextIndex != _end);
        }

        public Object next() {
            if (_nextIndex == _end)
                throw new NoSuchElementException();
            return _table.get(_currentIndex = _nextIndex++);
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
            return _table.get(_currentIndex = --_nextIndex);
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

    /**
     * This inner class implements a sub-table.
     */
    private static final class SubTable extends FastCollection implements List,
            RandomAccess {

        private static final Factory FACTORY = new Factory() {
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

        public int size() {
            return _size;
        }

        public Record head() {
            return Index.valueOf(-1);
        }

        public Record tail() {
            return Index.valueOf(_size);
        }

        public Object valueOf(Record record) {
            return _table.get(((Index) record).intValue() + _offset);
        }

        public void delete(Record record) {
            throw new UnsupportedOperationException(
                    "Deletion not supported, thread-safe collections.");
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
                FastTableIterator i = (FastTableIterator) FastTableIterator.FACTORY
                        .object();
                i._table = _table;
                i._start = _offset;
                i._end = _offset + _size;
                i._nextIndex = index + _offset;
                return i;
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
            SubTable st = (SubTable) SubTable.FACTORY.object();
            st._table = _table;
            st._offset = _offset + fromIndex;
            st._size = toIndex - fromIndex;
            return st;
        }
    }

    private static final long serialVersionUID = 1L;
}