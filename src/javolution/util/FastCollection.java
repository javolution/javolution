/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2005 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util;

import j2me.util.Collection;
import j2me.util.Iterator;
import j2me.util.List;
import j2me.util.NoSuchElementException;
import j2me.util.Set;
import j2me.io.Serializable;
import j2me.lang.IllegalStateException;
import j2me.lang.UnsupportedOperationException;

import javolution.lang.Text;
import javolution.realtime.RealtimeObject;

/**
 * <p> This class represents collections which can quickly be iterated over 
 *     (forward or backward) in a thread-safe manner without creating new 
 *     objects and without using {@link #iterator iterators} . For example:<pre>
 *     static boolean contains(Object obj, FastCollection c) {
 *         for (FastCollection.Record r = c.headRecord(), end = c.tailRecord(); (r = r.getNextRecord()) != end;) {
 *              if (obj.equals(c.valueOf(r))) return true;
 *         }
 *         return false;
 *     }</pre></p>
 *     
 * <p> Iterations are thread-safe as long as the {@link Record record} sequence
 *     iterated over is not structurally modified by another thread 
 *     (objects can safely be append/prepend during iteration but not 
 *     inserted/removed).</p>
 *     
 * <p> Users may provide a read-only view of any {@link FastCollection} 
 *     instance using the {@link #unmodifiable()} method. For example:<pre>
 *     public class Unit { // Immutable and unique.
 *         private final FastSet<Unit> _units = new FastSet<Unit>();
 *         // Read-only view (also thread-safe as units are never "deleted")
 *         public FastCollection<Unit> getInstances() { 
 *             return _units.unmodifiable();
 *         }
 *     }</pre></p>
 *     
 * <p> Finally, {@link FastCollection} may use custom {@link #setValueComparator
 *     comparators} for element equality or ordering if the collection is 
 *     ordered (e.g. <code>FastTree</code>).
 *     
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 3.2, April 1, 2005
 */
public abstract class FastCollection/*<E>*/extends RealtimeObject implements
        Collection/*<E>*/{

    /**
     * Holds the value comparator.  
     */
    private FastComparator _valueComp = FastComparator.DIRECT;

    /**
     * Holds the value comparator.  
     */
    private final Unmodifiable _unmodifiable;

    /**
     * Default constructor.  
     */
    protected FastCollection() {
        _unmodifiable = new Unmodifiable();
    }

    /**
     * Constructor for the Unmodifiable sub-class.
     * 
     * @param unmodifiable the unmodifiable collection.
     */
    private FastCollection(Unmodifiable unmodifiable) {
        _unmodifiable = unmodifiable;
    }

    /**
     * Returns the number of values in this collection. 
     *
     * @return the number of values.
     */
    public abstract int size();

    /**
     * Returns the head record of this collection; it is the record such as 
     * <code>headRecord().getNextRecord()</code> holds the first collection value.
     * 
     * @return the head record.
     */
    public abstract Record headRecord();

    /**
     * Returns the tail record of this collection; it is the record such as
     * <code>tailRecord().getPreviousRecord()</code> holds the last collection value.
     * 
     * @return the tail record.
     */
    public abstract Record tailRecord();

    /**
     * Returns the collection value for the specified record.
     *
     * @param record the record whose current value is returned.
     * @return the current value.
     */
    public abstract Object/*E*/valueOf(Record record);

    /**
     * Deletes the specified record from this collection.
     * 
     * <p> Implementation must ensure that removing a record from the 
     *     collection does not affect in any way the records preceding 
     *     the record being removed (it might affect the next records though,
     *     e.g. in a list collection, the indices of the subsequent records
     *     will change).</p>   
     *
     * @param record the record to be removed.
     * @throws UnsupportedOperationException if not supported.
     */
    public abstract void delete(Record record);

    /**
     * Returns the unmodifiable view associated to this collection.
     * Attempts to modify the returned collection result in an 
     * {@link UnsupportedOperationException} being thrown.
     *
     * @return an unmodifiable view.
     */
    public final FastCollection/*<E>*/unmodifiable() {
        return _unmodifiable;
    }

    /**
     * Returns an iterator over the elements in this collection 
     * (allocated on the stack when executed in a 
     * {@link javolution.realtime.PoolContext PoolContext}).
     *
     * @return an iterator over this collection's elements.
     */
    public Iterator/*<E>*/iterator() {
        FastIterator iterator = (FastIterator) FastIterator.FACTORY.object();
        iterator._collection = this;
        iterator._next = this.headRecord().getNextRecord();
        iterator._tail = this.tailRecord();
        return iterator;
    }

    /**
     * Sets the comparator to use for value equality or ordering if the 
     * collection is ordered (e.g. <code>FastTree</code>).
     *
     * @param comparator the value comparator.
     * @return <code>this</code>
     */
    public FastCollection/*<E>*/setValueComparator(FastComparator comparator) {
        _valueComp = comparator;
        return this;
    }

    /**
     * Returns the value comparator for this collection (default 
     * {@link FastComparator#DIRECT}).
     *
     * @return the comparator to use for value equality (or ordering if 
     *        the collection is ordered)
     */
    public FastComparator getValueComparator() {
        return _valueComp;
    }

    /**
     * Appends the specified value to the end of this collection
     * (optional operation).
     * 
     * <p>Note: This default implementation always throws 
     *          <code>UnsupportedOperationException</code>.</p>
     * 
     * @param value the value to be appended to this collection.
     * @return <code>true</code> (as per the general contract of the
     *         <code>Collection.add</code> method).
     * @throws UnsupportedOperationException if not supported.
     */
    public boolean add(Object/*E*/value) {
        throw new UnsupportedOperationException();
    }

    /**
     * Removes the first occurrence in this collection of the specified value
     * (optional operation).
     *
     * @param value the value to be removed from this collection.
     * @return <code>true</code> if this collection contained the specified
     *         value; <code>false</code> otherwise.
     * @throws UnsupportedOperationException if not supported.
     */
    public boolean remove(Object value) {
        final FastComparator valueComp = this.getValueComparator();
        for (Record r = headRecord(), end = tailRecord(); (r = r
                .getNextRecord()) != end;) {
            if (valueComp.areEqual(value, valueOf(r))) {
                delete(r);
                return true;
            }
        }
        return false;
    }

    /**
     * Removes all of the values from this collection (optional operation).
     *
     * @throws UnsupportedOperationException if not supported.
     */
    public void clear() {
        // Removes last record until empty.
        for (Record head = headRecord(), r = tailRecord().getPreviousRecord(); r != head; r = r
                .getPreviousRecord()) {
            delete(r);
        }
    }

    /**
     * Indicates if this collection is empty.
     *
     * @return <code>true</code> if this collection contains no value;
     *         <code>false</code> otherwise.
     */
    public final boolean isEmpty() {
        return size() == 0;
    }

    /**
     * Indicates if this collection contains the specified value.
     *
     * @param value the value whose presence in this collection 
     *        is to be tested.
     * @return <code>true</code> if this collection contains the specified
     *         value;<code>false</code> otherwise.
     */
    public boolean contains(Object value) {
        final FastComparator valueComp = this.getValueComparator();
        for (Record r = headRecord(), end = tailRecord(); (r = r
                .getNextRecord()) != end;) {
            if (valueComp.areEqual(value, valueOf(r)))
                return true;
        }
        return false;
    }

    /**
     * Appends all of the values in the specified collection to the end of
     * this collection, in the order that they are returned by the specified
     * collection's iterator or the node order if the specified collection
     * is a {@link FastCollection}.
     *
     * @param c collection whose values are to be added to this collection.
     * @return <code>true</code> if this collection changed as a result of 
     *         the call; <code>false</code> otherwise.
     */
    public boolean addAll(Collection/*<? extends E>*/c) {
        if (c instanceof FastCollection)
            return addAll((FastCollection) c);
        boolean modified = false;
        Iterator/*<? extends E>*/itr = c.iterator();
        int pos = c.size();
        while (--pos >= 0) {
            if (add(itr.next())) {
                modified = true;
            }
        }
        return modified;
    }

    private boolean addAll(FastCollection/*<? extends E>*/c) {
        boolean modified = false;
        for (Record r = c.headRecord(), end = c.tailRecord(); (r = r
                .getNextRecord()) != end;) {
            if (this.add(c.valueOf(r))) {
                modified = true;
            }
        }
        return modified;
    }

    /**
     * Indicates if this collection contains all of the values of the
     * specified collection.
     *
     * @param  c collection to be checked for containment in this collection.
     * @return <code>true</code> if this collection contains all of the values
     *         of the specified collection; <code>false</code> otherwise.
     */
    public boolean containsAll(Collection/*<?>*/c) {
        if (c instanceof FastCollection)
            return containsAll((FastCollection) c);
        Iterator/*<?>*/itr = c.iterator();
        int pos = c.size();
        while (--pos >= 0) {
            if (!contains(itr.next())) {
                return false;
            }
        }
        return true;
    }

    private boolean containsAll(FastCollection/*<?>*/c) {
        for (Record r = c.headRecord(), end = c.tailRecord(); (r = r
                .getNextRecord()) != end;) {
            if (!contains(c.valueOf(r))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Removes from this collection all the values that are contained in the
     * specified collection.
     *
     * @param c collection that defines which values will be removed from
     *          this collection.
     * @return <code>true</code> if this collection changed as a result of 
     *         the call; <code>false</code> otherwise.
     */
    public boolean removeAll(Collection/*<?>*/c) {
        boolean modified = false;
        // Iterates from the tail and removes the record if present in c. 
        for (Record head = headRecord(), r = tailRecord().getPreviousRecord(), previous;
                r != head; r = previous) {
            previous = r.getPreviousRecord(); // Saves previous.
            if (c.contains(valueOf(r))) {
                delete(r);
                modified = true;
            }
        }
        return modified;
    }

    /**
     * Retains only the values in this collection that are contained in the
     * specified collection.
     *
     * @param c collection that defines which values this set will retain.
     * @return <code>true</code> if this collection changed as a result of 
     *         the call; <code>false</code> otherwise.
     */
    public boolean retainAll(Collection/*<?>*/c) {
        boolean modified = false;
        // Iterates from the tail and remove the record if not present in c. 
        for (Record head = headRecord(), r = tailRecord().getPreviousRecord(), previous;
                 r != head; r = previous) {
            previous = r.getPreviousRecord(); // Saves previous.
            if (!c.contains(valueOf(r))) {
                delete(r);
                modified = true;
            }
        }
        return modified;
    }

    /**
     * Returns a new array allocated on the heap containing all of the values 
     * in this collection in proper sequence.
     * <p> Note: To avoid heap allocation {@link #toArray(Object[])} is 
     *           recommended.</p> 
     * @return <code>toArray(new Object[size()])</code>
     */
    public Object[] toArray() {
        return toArray(new Object[size()]);
    }

    /**
     * Fills the specified array with the values of this collection in 
     * the proper sequence.
     *  
     * <p> Note: Unlike standard Collection, this method does not try to resize
     *           the array using reflection (which might not be supported) if 
     *           the array is too small. UnsupportedOperationException is raised 
     *           if the specified array is too small for this collection.</p>
     *
     * @param  array the array into which the values of this collection
     *         are to be stored.
     * @return the specified array.
     * @throws UnsupportedOperationException if <pre>array.length < size()</pre> 
     */
    public Object[]/*<T> T[]*/toArray(Object[]/*T[]*/array) {
        int size = size();
        if (array.length < size)
            throw new UnsupportedOperationException(
                    "Destination array too small");
        if (array.length > size) {
            array[size] = null; // As per Collection contract.
        }
        int i = 0;
        Object[] arrayView = array;
        for (Record r = headRecord(), end = tailRecord(); (r = r
                .getNextRecord()) != end;) {
            arrayView[i++] = valueOf(r);
        }
        return array;
    }

    /**
     * Returns the textual representation of this collection.
     * 
     * @return this collection textual representation.
     */
    public Text toText() {
        final Text sep = Text.valueOf(", ");
        Text text = Text.valueOf('[');
        for (Record r = headRecord(), end = tailRecord(); (r = r
                .getNextRecord()) != end;) {
            text = text.concat(Text.valueOf(valueOf(r)));
            if (r.getNextRecord() != end) {
                text = text.concat(sep);
            }
        }
        return text.concat(Text.valueOf(']'));
    }

    /**
     * Compares the specified object with this collection for equality.  Returns
     * <code>true</code> if and only both collection contains the same values
     * regardless of the order; unless this collection is a list instance 
     * in which case both collection must be list with the same order. 
     *
     * @param obj the object to be compared for equality with this collection.
     * @return <code>true</code> if the specified object is equal to this
     *         collection; <code>false</code> otherwise.
     */
    public boolean equals(Object obj) {
        if (this instanceof List)
            return equalsList(obj);
        return (obj == this || (obj instanceof Collection
                && ((Collection) obj).size() == size() && containsAll((Collection) obj)));
    }

    private boolean equalsList(Object obj) {
        final FastComparator comp = this.getValueComparator();
        if (obj == this)
            return true;
        if (obj instanceof List) {
            List/*<?>*/list = (List) obj;
            if (this.size() != list.size())
                return false;
            Record r1 = this.headRecord();
            Iterator/*<?>*/i2 = list.iterator();
            for (int i = this.size(); i-- != 0;) {
                r1 = r1.getNextRecord();
                Object o1 = this.valueOf(r1);
                Object o2 = i2.next();
                if (!comp.areEqual(o1, o2))
                    return false;
            }
            return true;
        }
        return false;
    }

    /**
     * Returns the hash code for this collection (independent from the 
     * collection order; unless this collection is a list instance).
     *
     * @return the hash code for this collection.
     */
    public int hashCode() {
        if (this instanceof List)
            return hashCodeList();
        final FastComparator valueComp = this.getValueComparator();
        int hash = 0;
        for (Record r = headRecord(), end = tailRecord(); (r = r
                .getNextRecord()) != end;) {
            hash += valueComp.hashCodeOf(valueOf(r));
        }
        return hash;
    }

    private int hashCodeList() {
        final FastComparator comp = this.getValueComparator();
        int h = 1;
        for (Record r = headRecord(), end = tailRecord(); (r = r
                .getNextRecord()) != end;) {
            h = 31 * h + comp.hashCodeOf(valueOf(r));
        }
        return h;

    }

    /**
     * This interface represents the collection records which can directly be
     * iterated over.
     */
    public interface Record/*<E>*/{

        /**
         * Returns the record before this one.
         * 
         * @return the previous record.
         */
        public Record getPreviousRecord();

        /**
         * Returns the record after this one.
         * 
         * @return the next record.
         */
        public Record getNextRecord();

    }

    /**
     * This inner class implements a collection iterator.
     */
    private static final class FastIterator/*<E>*/extends RealtimeObject
            implements Iterator/*<E>*/{

        private static final Factory FACTORY = new Factory() {
            protected Object create() {
                return new FastIterator();
            }

            protected void cleanup(Object obj) {
                FastIterator i = (FastIterator) obj;
                i._collection = null;
                i._current = null;
                i._next = null;
                i._tail = null;
            }
        };

        private FastCollection/*<E>*/_collection;

        private Record _current;

        private Record _next;

        private Record _tail;

        private FastIterator() {
        }

        public boolean hasNext() {
            return (_next != _tail);
        }

        public Object/*E*/next() {
            if (_next == _tail)
                throw new NoSuchElementException();
            _current = _next;
            _next = _next.getNextRecord();
            return _collection.valueOf(_current);
        }

        public void remove() {
            if (_current != null) {
                // Uses the previous record (not affected by the remove)
                // to set the next record.
                final Record previous = _current.getPreviousRecord();
                _collection.delete(_current);
                _current = null;
                _next = previous.getNextRecord();
            } else {
                throw new IllegalStateException();
            }
        }
    }

    /**
     * This inner class represents an unmodifiable view over the collection.
     */
    private final class Unmodifiable extends FastCollection/*<E>*/implements
            Serializable, Set/*<E>*/ { // Allows to be used for unmodifiable set view.

        public Unmodifiable() {
            super(null);
        }

        public int size() {
            return FastCollection.this.size();
        }

        public Record headRecord() {
            return FastCollection.this.headRecord();
        }

        public Record tailRecord() {
            return FastCollection.this.tailRecord();
        }

        public FastComparator getValueComparator() {
            return FastCollection.this.getValueComparator();
        }

        public FastCollection/*<E>*/setValueComparator(
                FastComparator comparator) {
            throw new UnsupportedOperationException("Unmodifiable");
        }

        public boolean add(Object/*E*/obj) {
            throw new UnsupportedOperationException("Unmodifiable");
        }

        public void delete(Record node) {
            throw new UnsupportedOperationException("Unmodifiable");
        }

        public Object/*E*/valueOf(Record record) {
            return FastCollection.this.valueOf(record);
        }

        private static final long serialVersionUID = 4048789065711367989L;
    }
}