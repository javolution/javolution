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
import j2me.util.NoSuchElementException;
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
 *     comparators} for equality or ordering if the collection is ordered 
 *     (e.g. <code>FastTree</code>).
 *     
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 3.2, April 1, 2005
 */
public abstract class FastCollection/*<E>*/ extends RealtimeObject implements
        Collection/*<E>*/ {

    /**
     * Holds the value comparator.  
     */
    private FastComparator _valueComp = FastComparator.DEFAULT;

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
    public abstract Object/*E*/ valueOf(Record record);

    /**
     * Deletes the specified record from this collection.
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
    public final FastCollection/*<E>*/ unmodifiable() {
        return _unmodifiable;
    }

    /**
     * Returns an iterator over the elements in this collection in proper 
     * sequence.
     *
     * @return an iterator over this collection's elements.
     * @deprecated Applications should use direct {@link Record} iterations
     *             (faster, thread-safe and no object creation).
     */
    public final Iterator/*<E>*/ iterator() {
        FastIterator iterator = new FastIterator();
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
    public FastCollection/*<E>*/ setValueComparator(FastComparator comparator) {
        _valueComp = comparator;
        return this;
    }

    /**
     * Returns the value comparator for this collection (default 
     * {@link FastComparator#DEFAULT}).
     *
     * @return the comparator to use for value equality (or ordering if 
     *        the collection is ordered)
     */
    public FastComparator getValueComparator() {
        return _valueComp;
    }

    /**
     * Appends the specified value to the end of this collection
     * (optional operations).
     * 
     * <p>Note: This default implementation always throws 
     *          <code>UnsupportedOperationException</code>.</p>
     * 
     * @param value the value to be appended to this collection.
     * @return <code>true</code> (as per the general contract of the
     *         <code>Collection.add</code> method).
     * @throws UnsupportedOperationException if not supported.
     */
    public boolean add( Object/*E*/ value) {
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
        for (Record r = headRecord(), end = tailRecord(); (r = r.getNextRecord()) != end;) {
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
        for (Record r = headRecord().getNextRecord(), end = tailRecord(); r != end;) {
            final Record next = r.getNextRecord(); // Save next.
            delete(r);
            r = next;
        }
    }

    /**
     * Indicates if this collection is empty.
     *
     * @return <code>true</code> if this collection contains no value;
     *         <code>false</code> otherwise.
     */
    public boolean isEmpty() {
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
        for (Record r = headRecord(), end = tailRecord(); (r = r.getNextRecord()) != end;) {
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
    public boolean addAll(Collection/*<? extends E>*/ c) {
        if (c instanceof FastCollection)
            return addAll((FastCollection) c);
        boolean modified = false;
        Iterator/*<? extends E>*/ itr = c.iterator();
        int pos = c.size();
        while (--pos >= 0) {
            if (add(itr.next())) {
                modified = true;
            }
        }
        return modified;
    }

    private boolean addAll(FastCollection/*<? extends E>*/ c) {
        boolean modified = false;
        for (Record r = c.headRecord(), end = c.tailRecord(); (r = r.getNextRecord()) != end;) {
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
    public boolean containsAll(Collection/*<?>*/ c) {
        if (c instanceof FastCollection)
            return containsAll((FastCollection) c);
        Iterator/*<?>*/ itr = c.iterator();
        int pos = c.size();
        while (--pos >= 0) {
            if (!contains(itr.next())) {
                return false;
            }
        }
        return true;
    }

    private boolean containsAll(FastCollection/*<?>*/ c) {
        for (Record r = c.headRecord(), end = c.tailRecord(); (r = r.getNextRecord()) != end;) {
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
    public boolean removeAll(Collection/*<?>*/ c) {
        boolean modified = false;
        for (Record r = headRecord().getNextRecord(), end = tailRecord(); r != end;) {
            final Record next = r.getNextRecord(); // Save next.
            if (c.contains(valueOf(r))) {
                delete(r);
                modified = true;
            }
            r = next;
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
    public boolean retainAll(Collection/*<?>*/ c) {
        boolean modified = false;
        for (Record r = headRecord().getNextRecord(), end = tailRecord(); r != end;) {
            final Record next = r.getNextRecord(); // Save next.
            if (!c.contains(valueOf(r))) {
                delete(r);
                modified = true;
            }
            r = next;
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
    public Object[]/*<T> T[]*/ toArray(Object[]/*T[]*/ array) {
        int size = size();
        if (array.length < size)
            throw new UnsupportedOperationException(
                    "Destination array too small");
        if (array.length > size) {
            array[size] = null; // As per Collection contract.
        }
        int i = 0;
        Object[] arrayView = array;
        for (Record r = headRecord(), end = tailRecord(); (r = r.getNextRecord()) != end;) {
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
		for (Record r = headRecord(), end = tailRecord(); (r = r.getNextRecord()) != end;) {
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
     * regardless of the order.
     *
     * @param obj the object to be compared for equality with this collection.
     * @return <code>true</code> if the specified object is equal to this
     *         collection; <code>false</code> otherwise.
     */
    public boolean equals(Object obj) {
        return (obj == this || (obj instanceof Collection
                && ((Collection) obj).size() == size() && containsAll((Collection) obj)));
    }

    /**
     * Returns the hash code for this collection (independent from the 
     * collection order).
     *
     * @return the hash code for this collection.
     */
    public int hashCode() {
        final FastComparator valueComp = this.getValueComparator();
        int hash = 0;
        for (Record r = headRecord(), end = tailRecord(); (r = r.getNextRecord()) != end;) {
            hash += valueComp.hashCodeOf(valueOf(r));
        }
        return hash;
    }

    /**
     * Returns an iterator over the elements in this collection in proper 
     * sequence.
     *
     * @return an iterator over this collection's elements.
     * @deprecated Applications should use direct {@link Record} iterations
     *             (faster, thread-safe and no object creation).
     */
    public final Iterator fastIterator() {
        return iterator();
    }
    
    /**
     * This interface represents the collection records which can directly be
     * iterated over.
     */
    public interface Record/*<E>*/ {

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
    private final class FastIterator implements Iterator/*<E>*/ {

        private Record _current;

        private Record _next;

        private Record _tail;

        private FastIterator() {
        }

        public boolean hasNext() {
            return (_next != _tail);
        }

        public Object/*E*/ next() {
            if (_next == _tail)
                throw new NoSuchElementException();
            _current = _next;
            _next = _next.getNextRecord();
            return valueOf(_current);
        }

        public void remove() {
            if (_current != null) {
                delete(_current);
                _current = null;
            } else {
                throw new IllegalStateException();
            }
        }
    }

    /**
     * This inner class represents an unmodifiable view over the collection.
     */
    private final class Unmodifiable extends FastCollection/*<E>*/ implements
            Serializable {

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

        public FastCollection/*<E>*/ setValueComparator(FastComparator comparator) {
            throw new UnsupportedOperationException("Unmodifiable");
        }

        public boolean add(Object/*E*/ obj) {
            throw new UnsupportedOperationException("Unmodifiable");
        }

        public void delete(Record node) {
            throw new UnsupportedOperationException("Unmodifiable");
        }

        public Object/*E*/ valueOf(Record record) {
            return FastCollection.this.valueOf(record);
        }

        private static final long serialVersionUID = 4048789065711367989L;
    }
}