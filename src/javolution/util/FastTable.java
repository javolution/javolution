/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2005 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util;

import java.util.NoSuchElementException;

import j2me.io.Serializable;
import j2me.lang.IllegalStateException;
import j2me.lang.UnsupportedOperationException;
import j2me.util.Collection;
import j2me.util.Iterator;
import j2me.util.List;
import j2me.util.ListIterator;
import j2me.util.RandomAccess;
import javolution.lang.Reusable;
import javolution.realtime.ObjectFactory;
import javolution.realtime.RealtimeObject;

/**
 * <p> This class represents a random access collection with real-time behavior; 
 *     smooth capacity increase (no array resize/copy ever) and no memory 
 *     allocation as long as the collection size does not exceed its initial
 *     capacity.</p>
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
 *          {@link javolution.util} discussion). To keep access/iterations 
 *          unsynchronized, users might want to consider replacing
 *          the whole collection instead of clearing it for example.</li>
 *     </ul></p>
 *     
 *  <p> Iterations over the {@link FastTable} values are faster when
 *      performed using the {@link #get} method rather than using collection
 *      records or iterators:<pre>
 *     for (int i = 0, n = table.size(); i < n; i++) {
 *          table.get(i);
 *     }</pre></p>
 *     
 * <p><i> Implementation Note: To avoid expensive resize/copy operations
 *        {@link FastTable} used multi-dimensional arrays internally. This 
 *        has also for effect to reduce memory fragmentation (and garbage 
 *        collection time) because the allocated objects are always small.
 *        Performance comparison  with <code>ArrayList</code>
 *         <a href="http://javolution.org/doc/benchmark.html">shows</a>
 *        almost identical {@link #get(int) access} time and up to 2x 
 *        improvement when {@link #add adding} values to a new collection.
 *        </i></p>
 * 
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 3.4, July 4, 2005
 */
public class FastTable/*<E>*/extends FastCollection/*<E>*/implements
        List/*<E>*/, Reusable, RandomAccess, Serializable {

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

    private static final int R0 = 0;

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

    private static final int M3 = (1 << D3) - 1;

    // new Object[1<<7][1<<5], 12 bits (4096)
    private Object/*E*/[][] _elems1;

    // new Object[1<<9][1<<7][1<<5], 21 bits (2097152)
    private Object/*E*/[][][] _elems2;

    // new Object[1<<11][1<<9][1<<7][1<<5], 32 bits
    private Object/*E*/[][][][] _elems3;

    private static final ObjectFactory OBJS0_FACTORY = new ObjectFactory() {
        public Object create() {
            return new Object[1 << D0];
        }
    };

    private static final ObjectFactory OBJS1_FACTORY = new ObjectFactory() {
        public Object create() {
            return new Object[1 << D1][];
        }
    };

    private static final ObjectFactory OBJS2_FACTORY = new ObjectFactory() {
        public Object create() {
            return new Object[1 << D2][][];
        }
    };

    private static final ObjectFactory OBJS3_FACTORY = new ObjectFactory() {
        public Object create() {
            return new Object[1 << D3][][][];
        }
    };

    /**
     * Holds the current capacity. 
     */
    private int _capacity = C0;

    /**
     * Holds the current size.
     */
    private int _size;

    /**
     * Creates a table of small initial capacity.
     */
    public FastTable() {
        _elems1 = (Object/*E*/[][]) new Object[1][];
        _elems1[0] =(Object/*E*/[]) new Object[C0];
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
     * Returns a table allocated from the "stack" when executing in
     * a {@link javolution.realtime.PoolContext PoolContext}).
     *
     * @return a new, preallocated or recycled text builder instance.
     */
    public static/*<E>*/FastTable/*<E>*/newInstance() {
        return (FastTable/*<E>*/) FACTORY.object();
    }

    /**
     * Returns the element at the specified index.
     *
     * @param index index of value to return.
     * @return the value at the specified position in this list.
     * @throws IndexOutOfBoundsException if <code>(index < 0) || 
     *         (index >= size())</code>
     */
    public final Object/*E*/get(int index) { // Short to be inlined.
        if (((index >> R2) == 0) && (index < _size))
            return _elems1[(index >> R1)][index & M0];
        return get2(index);
    }

    private final Object/*E*/get2(int index) {
        if ((index < 0) || (index >= _size))
            throw new IndexOutOfBoundsException("index: " + index);
        if (index < C2)
            return _elems2[(index >> R2)][(index >> R1) & M1][index & M0];
        return _elems3[(index >> R3)][(index >> R2) & M2][(index >> R1) & M1][index
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
    public final Object/*E*/set(int index, Object/*E*/value) {
        if ((index < 0) || (index >= _size))
            throw new IndexOutOfBoundsException("index: " + index);
        final Object/*E*/[] elems = (index < C1) ? _elems1[(index >> R1)]
                : (index < C2) ? _elems2[(index >> R2)][(index >> R1) & M1]
                        : _elems3[(index >> R3)][(index >> R2) & M2][(index >> R1)
                                & M1];
        final Object/*E*/oldValue = elems[index & M0];
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
    public final boolean add(Object/*E*/value) {
        addLast(value);
        return true;
    }

    /**
     * Returns the last value of this table.
     *
     * @return this table last value.
     * @throws NoSuchElementException if this table is empty.
     */
    public final Object/*E*/getLast() {
        if (_size == 0)
            throw new NoSuchElementException();
        return get(_size - 1);
    }

    /**
     * Appends the specified value to the end of this table <i>(fast)</i>.
     * 
     * @param value the value to be added.
     */
    public final void addLast(Object/*E*/value) {
        final int i = _size;
        if (i >= _capacity) {
            increaseCapacity();
        }
        if (i < C1) {
            _elems1[(i >> R1)][i & M0] = value;
        } else if (i < C2) {
            _elems2[(i >> R2)][(i >> R1) & M1][i & M0] = value;
        } else {
            _elems3[(i >> R3)][(i >> R2) & M2][(i >> R1) & M1][i & M0] = value;
        }
        checkpoint(); // Ensures that size is incremented last.
        _size++;
    }

    /**
     * Removes and returns the last value of this table <i>(fast)</i>.
     *
     * @return this table's last value before this call.
     * @throws NoSuchElementException if this list is empty.
     */
    public final Object/*E*/removeLast() {
        if (_size == 0)
            throw new NoSuchElementException();
        final int index = --_size;
        final Object/*E*/[] elems = (index < C1) ? _elems1[(index >> R1)]
                : (index < C2) ? _elems2[(index >> R2)][(index >> R1) & M1]
                        : _elems3[(index >> R3)][(index >> R2) & M2][(index >> R1)
                                & M1];
        final Object/*E*/oldValue = elems[index & M0];
        elems[index & M0] = null;
        return oldValue;
    }

    // Overrides.
    public final void clear() {
        for (int index = 0, end = _size; index < end;) {
            if (index < C1) {
                _elems1[(index >> R1)][index++ & M0] = null;
            } else if (index < C2) {
                _elems2[(index >> R2)][(index >> R1) & M1][index++ & M0] = null;
            } else {
                _elems3[(index >> R3)][(index >> R2) & M2][(index >> R1) & M1][index++
                        & M0] = null;
            }
        }
        _size = 0;
    }

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
    public final void add(int index, Object/*E*/value) {
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
    public final Object/*E*/remove(int index) {
        if ((index < 0) || (index >= _size))
            throw new IndexOutOfBoundsException("index: " + index);
        final int lastIndex = _size - 1;
        Object/*E*/obj = this.get(lastIndex);
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
     * {@link javolution.realtime.PoolContext PoolContext}).
     *
     * @return an iterator over this list values.
     */
    public final Iterator/*<E>*/iterator() {
        FastTableIterator/*<E>*/i = (FastTableIterator/*<E>*/) FastTableIterator.FACTORY
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
     * {@link javolution.realtime.PoolContext PoolContext}).
     *
     * @return an iterator over this list values.
     */
    public final ListIterator/*<E>*/listIterator() {
        FastTableIterator/*<E>*/i = (FastTableIterator/*<E>*/) FastTableIterator.FACTORY
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
     * {@link javolution.realtime.PoolContext PoolContext}).
     * The list iterator being returned does not support insertion/deletion.
     * 
     * @param index the index of first value to be returned from the
     *        list iterator (by a call to the <code>next</code> method).
     * @return a list iterator of the values in this table
     *         starting at the specified position in this list.
     * @throws IndexOutOfBoundsException if the index is out of range (index
     *         &lt; 0 || index &gt; size()).
     */
    public final ListIterator/*<E>*/listIterator(int index) {
        if ((index >= 0) && (index <= _size)) {
            FastTableIterator/*<E>*/i = (FastTableIterator/*<E>*/) FastTableIterator.FACTORY
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
     * executing in a {@link javolution.realtime.PoolContext PoolContext}).
     * If the specified indexes are equal, the returned list is empty. 
     * The returned list is backed by this list, so non-structural changes in
     * the returned list are reflected in this list, and vice-versa. 
     *
     * This method eliminates the need for explicit range operations (of
     * the sort that commonly exist for arrays). Any operation that expects
     * a list can be used as a range operation by passing a subList view
     * instead of a whole list.  For example, the following idiom
     * removes a range of values from a list:
     * <pre>
     *      list.subList(from, to).clear();
     * </pre>
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
     * @throws IndexOutOfBoundsException if <code>(fromIndex &lt; 0 ||
     *          toIndex &gt; size || fromIndex &gt; toIndex)</code>
     */
    public final List/*<E>*/subList(int fromIndex, int toIndex) {
        if ((fromIndex < 0) || (toIndex > _size) || (fromIndex > toIndex))
            throw new IndexOutOfBoundsException("fromIndex: " + fromIndex
                    + ", toIndex: " + toIndex + " for list of size: " + _size);
        SubTable/*<E>*/st = (SubTable/*<E>*/) SubTable.FACTORY.object();
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

    // Implements FastCollection abstract method.
    public final int size() {
        return _size;
    }

    // Implements FastCollection abstract method.
    public final Record headRecord() {
        return Index.MINUS_ONE;
    }

    // Implements FastCollection abstract method.
    public final Record tailRecord() {
        return (Index) Index.COLLECTION.get(_size);
    }

    // Implements FastCollection abstract method.
    public final Object/*E*/valueOf(Record record) {
        return get(((Index) record)._position);
    }

    // Implements FastCollection abstract method.
    public final void delete(Record record) {
        remove(((Index) record)._position);
    }

    // Implements abstract method.
    public void reset() {
        super.setValueComparator(FastComparator.DIRECT);
        clear();
    }

    /**
     * Increases this table capacity.
     */
    private void increaseCapacity() {
        final int c = _capacity;
        _capacity += C0;
        if (c < C1) {
            if (_elems1.length == 1) { // Replaces the original table.
                Object/*E*/[][] tmp = (Object/*E*/[][]) OBJS1_FACTORY
                        .newObject();
                tmp[0] = _elems1[0];
                _elems1 = tmp;
            }
            _elems1[(c >> R1)] = (Object/*E*/[]) OBJS0_FACTORY.newObject();

        } else if (c < C2) {
            if (_elems2 == null) {
                _elems2 = (Object/*E*/[][][]) OBJS2_FACTORY.newObject();
            }
            if (_elems2[(c >> R2)] == null) {
                _elems2[(c >> R2)] = (Object/*E*/[][]) OBJS1_FACTORY
                        .newObject();
            }
            _elems2[(c >> R2)][(c >> R1) & M1] = (Object/*E*/[]) OBJS0_FACTORY
                    .newObject();

        } else {
            if (_elems3 == null) {
                _elems3 = (Object/*E*/[][][][]) OBJS3_FACTORY.newObject();
            }
            if (_elems3[(c >> R3)] == null) {
                _elems3[(c >> R3)] = (Object/*E*/[][][]) OBJS2_FACTORY
                        .newObject();
            }
            if (_elems3[(c >> R3)][(c >> R2) & M2] == null) {
                _elems3[(c >> R3)][(c >> R2) & M2] = (Object/*E*/[][]) OBJS1_FACTORY
                        .newObject();
            }
            _elems3[(c >> R3)][(c >> R2) & M2][(c >> R1) & M1] = (Object/*E*/[]) OBJS0_FACTORY
                    .newObject();
        }
        // Checks if more indices are necessary.
        if ((_capacity >= Index.COLLECTION._size) && (this != Index.COLLECTION)) {
            while (_capacity >= Index.COLLECTION._size) {
                Index.augment();
            }
        }
    }

    /**
     * Decreases this table capacity by C0 (down to C0).
     * 
     * @throws IllegalStateException if <code>(_size >= _capacity - C0)</code>
     */
    private void decreaseCapacity() {
        if (_size >= _capacity - C0)
            throw new IllegalStateException();
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
     * Ensures that the compiler will not reorder previous instructions below
     * this point.
     */
    private static void checkpoint() {
        if (CHECK_POINT)
            throw new Error(); // Reads volatile.
    }

    static volatile boolean CHECK_POINT;

    /**
     * This class represents a {@link FastTable} index; it allows for direct 
     * iteration over the collection.
     */
    public static final class Index implements Record {

        /**
         * Holds the index factory (to allow for pre-allocation).
         */
        private static ObjectFactory FACTORY = new ObjectFactory() {
            protected Object create() {
                return new Index();
            }
        };

        /**
         * Holds the indexes.
         */
        private static final FastTable COLLECTION = new FastTable();

        /**
         * Holds the head record for all tables.
         */
        private static final Index MINUS_ONE = new Index();

        /**
         * Holds the last collection index.
         */
        private static Index CollectionLast = MINUS_ONE;

        static {
            MINUS_ONE._position = -1;
            while (COLLECTION._size <= (1 << D0)) { // Default capacity.
                augment();
            }
        }

        /**
         * Holds the index position.
         */
        private int _position;

        /**
         * Holds the next index.
         */
        private Index _next;

        /**
         * Holds the previous node.
         */
        private Index _previous;

        /**
         * Returns the unique index for the specified position 
         * (creating it as well as all its previous indices if they do not 
         * exist). This method may be called at initialization to create
         * all the indices used by {@link FastTable} instances at start-up.
         * 
         * @param position the position in the table
         * @return the corresponding unique index.
         * @throws IllegalArgumentException if <code>position < -1</code>.
         */
        public static Index getInstance(int position) {
            if (position == -1)
                return MINUS_ONE;
            if (position < -1)
                throw new IllegalArgumentException(
                        "position: Should be greater or equal to -1");
            while (position >= COLLECTION.size()) {
                augment();
            }
            return (Index) Index.COLLECTION.get(position);
        }

        /**
         * Augments index table.
         */
        private static void augment() {
            Index index = (Index) Index.FACTORY.newObject();
            synchronized (COLLECTION) {
                index._position = COLLECTION._size;
                Index.CollectionLast._next = index;
                index._previous = Index.CollectionLast;
                COLLECTION.addLast(index);
                Index.CollectionLast = index;
            }
        }

        // Implements Record interface.
        public final Record getNextRecord() {
            return _next;
        }

        // Implements Record interface.
        public final Record getPreviousRecord() {
            return _previous;
        }
    }

    /**
     * This inner class implements a fast table iterator.
     */
    private static final class FastTableIterator/*<E>*/extends RealtimeObject
            implements ListIterator/*<E>*/{

        private static final Factory FACTORY = new Factory() {
            protected Object create() {
                return new FastTableIterator();
            }

            protected void cleanup(Object obj) {
                FastTableIterator i = (FastTableIterator) obj;
                i._table = null;
            }
        };

        private FastTable/*<E>*/_table;

        private int _currentIndex;

        private int _start; // Inclusive.

        private int _end; // Exclusive.

        private int _nextIndex;

        public boolean hasNext() {
            return (_nextIndex != _end);
        }

        public Object/*E*/next() {
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

        public Object/*E*/previous() {
            if (_nextIndex == _start)
                throw new NoSuchElementException();
            return _table.get(_currentIndex = --_nextIndex);
        }

        public int previousIndex() {
            return _nextIndex - 1;
        }

        public void add(Object/*E*/o) {
            _table.add(_nextIndex++, o);
            _end++;
            _currentIndex = -1;
        }

        public void set(Object/*E*/o) {
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
    private static final class SubTable/*<E>*/extends FastCollection
    /*<E>*/implements List/*<E>*/, RandomAccess, Serializable {

        private static final Factory FACTORY = new Factory() {
            protected Object create() {
                return new SubTable();
            }

            protected void cleanup(Object obj) {
                SubTable st = (SubTable) obj;
                st._table = null;
            }
        };

        private FastTable/*<E>*/_table;

        private int _offset;

        private int _size;

        public int size() {
            return _size;
        }

        public Record headRecord() {
            return Index.MINUS_ONE;
        }

        public Record tailRecord() {
            return (Index) Index.COLLECTION.get(_size);
        }

        public Object/*E*/valueOf(Record record) {
            return _table.get(((Index) record)._position + _offset);
        }

        public void delete(Record record) {
            throw new UnsupportedOperationException(
                    "Deletion not supported, thread-safe collections.");
        }

        public boolean addAll(int index, Collection/*<? extends E>*/values) {
            throw new UnsupportedOperationException(
                    "Insertion not supported, thread-safe collections.");
        }

        public Object/*E*/get(int index) {
            if ((index < 0) || (index >= _size))
                throw new IndexOutOfBoundsException("index: " + index);
            return _table.get(index + _offset);
        }

        public Object/*E*/set(int index, Object/*E*/value) {
            if ((index < 0) || (index >= _size))
                throw new IndexOutOfBoundsException("index: " + index);
            return _table.set(index + _offset, value);
        }

        public void add(int index, Object/*E*/element) {
            throw new UnsupportedOperationException(
                    "Insertion not supported, thread-safe collections.");
        }

        public Object/*E*/remove(int index) {
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

        public ListIterator/*<E>*/listIterator() {
            return listIterator(0);
        }

        public ListIterator/*<E>*/listIterator(int index) {
            if ((index >= 0) && (index <= _size)) {
                FastTableIterator/*<E>*/i = (FastTableIterator/*<E>*/) FastTableIterator.FACTORY
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

        public List/*<E>*/subList(int fromIndex, int toIndex) {
            if ((fromIndex < 0) || (toIndex > _size) || (fromIndex > toIndex))
                throw new IndexOutOfBoundsException("fromIndex: " + fromIndex
                        + ", toIndex: " + toIndex + " for list of size: "
                        + _size);
            SubTable/*<E>*/st = (SubTable/*<E>*/) SubTable.FACTORY.object();
            st._table = _table;
            st._offset = _offset + fromIndex;
            st._size = toIndex - fromIndex;
            return st;
        }

    }
}