/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2004 - The Javolution Team (http://javolution.org/)
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util;

import java.util.Collection;
import java.util.Iterator;

import javolution.lang.Text;
import javolution.lang.TextBuilder;
import javolution.realtime.RealtimeObject;

/**
 * <p> This class represents the fast collection base class; instances of 
 *     this class embed their own {@link #fastIterator fast iterators} 
 *     over the collection elements (avoids dynamic allocation of 
 *     iterator).</p>
 * 
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 1.0, October 4, 2004
 */
public abstract class FastCollection extends RealtimeObject implements
        Collection {

    /**
     * Default constructor.  
     */
    protected FastCollection() {
    }

    /**
     * Returns the iterator instance associated to this collection.
     * This iterator is positioned at the beginning of the collection.
     * Multiple threads using this iterator must synchronize on the collection
     * itself. 
     *
     * @return the single reusable iterator instance for this collection.
     */
    public abstract Iterator fastIterator();

    /**
     * Returns an iterator over the elements in this collection in proper 
     * sequence (allocated from the "stack" when executing in a
     * {@link javolution.realtime.PoolContext PoolContext}).
     *
     * @return a real-time iterator over this collection's elements.
     */
    public abstract Iterator iterator();

    /**
     * Returns the number of elements in this collection. 
     *
     * @return the number of elements in this collection.
     */
    public abstract int size();

    /**
     * Appends the specified element to the end of this collection
     * (optional operations).
     * 
     * <p>Note: This default implementation always throws 
     *          <code>UnsupportedOperationException</code>.</p>
     * 
     * @param element the element to be appended to this collection.
     * @return <code>true</code> (as per the general contract of the
     *         <code>Collection.add</code> method).
     * @throws UnsupportedOperationException if not supported.
     */
    public boolean add(Object element) {
        throw new UnsupportedOperationException();
    }

    /**
     * Removes the first occurrence in this collection of the specified element
     * (optional operation).
     *
     * @param element the element to be removed from this collection.
     * @return <code>true</code> if this collection contained the specified
     *         element; <code>false</code> otherwise.
     * @throws UnsupportedOperationException if not supported.
     */
    public boolean remove(Object element) {
        if (element != null) {
            Iterator itr = iterator();
            int pos = size();
            while (--pos >= 0) {
                if (element.equals(itr.next())) {
                    itr.remove();
                    return true;
                }
            }
            return false;
        } else {
            return removeNull();
        }
    }

    private boolean removeNull() {
        Iterator itr = iterator();
        int pos = size();
        while (--pos >= 0) {
            if (itr.next() == null) {
                itr.remove();
                return true;
            }
        }
        return false;
    }

    /**
     * Removes all of the elements from this collection (optional operation).
     *
     * @throws UnsupportedOperationException if not supported.
     */
    public void clear() {
        Iterator itr = iterator();
        int pos = size();
        while (--pos >= 0) {
            itr.next();
            itr.remove();
        }
    }

    /**
     * Indicates if this collection is empty.
     *
     * @return <code>true</code> if this collection contains no elements;
     *         <code>false</code> otherwise.
     */
    public boolean isEmpty() {
        return size() == 0;
    }

    /**
     * Indicates if this collection contains the specified element.
     *
     * @param element the element whose presence in this collection 
     *        is to be tested.
     * @return <code>true</code> if this collection contains the specified
     *         element;<code>false</code> otherwise.
     */
    public boolean contains(Object element) {
        if (element != null) {
            Iterator itr = iterator();
            int pos = size();
            while (--pos >= 0) {
                if (element.equals(itr.next())) {
                    return true;
                }
            }
            return false;
        } else {
            return containsNull();
        }
    }

    private boolean containsNull() {
        Iterator itr = iterator();
        int pos = size();
        while (--pos >= 0) {
            if (itr.next() == null) {
                return true;
            }
        }
        return false;
    }

    /**
     * Appends all of the elements in the specified collection to the end of
     * this collection, in the order that they are returned by the specified
     * collection's iterator.
     *
     * @param c collection whose elements are to be added to this collection.
     * @return <code>true</code> if this collection changed as a result of 
     *         the call; <code>false</code> otherwise.
     */
    public boolean addAll(Collection c) {
        boolean modified = false;
        Iterator itr = c.iterator();
        int pos = c.size();
        while (--pos >= 0) {
            if (add(itr.next())) {
                modified = true;
            }
        }
        return modified;
    }

    /**
     * Indicates if this collection contains all of the elements of the
     * specified collection.
     *
     * @param  c collection to be checked for containment in this collection.
     * @return <code>true</code> if this collection contains all of the elements
     *         of the specified collection; <code>false</code> otherwise.
     */
    public boolean containsAll(Collection c) {
        Iterator itr = c.iterator();
        int pos = c.size();
        while (--pos >= 0) {
            if (!contains(itr.next())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Removes from this collection all the elements that are contained in the
     * specified collection.
     *
     * @param c collection that defines which elements will be removed from
     *          this collection.
     * @return <code>true</code> if this collection changed as a result of 
     *         the call; <code>false</code> otherwise.
     */
    public boolean removeAll(Collection c) {
        boolean modified = false;
        Iterator itr = iterator();
        int pos = size();
        while (--pos >= 0)
            if (c.contains(itr.next())) {
                itr.remove();
                modified = true;
            }
        return modified;
    }

    /**
     * Retains only the elements in this collection that are contained in the
     * specified collection.
     *
     * @param c collection that defines which elements this set will retain.
     * @return <code>true</code> if this collection changed as a result of 
     *         the call; <code>false</code> otherwise.
     */
    public boolean retainAll(Collection c) {
        boolean modified = false;
        Iterator itr = iterator();
        int pos = size();
        while (--pos >= 0)
            if (!c.contains(itr.next())) {
                itr.remove();
                modified = true;
            }
        return modified;
    }

    /**
     * Returns a new array allocated on the heap containing all of the elements 
     * in this collection in proper sequence.
     * <p> Note: To avoid heap allocation {@link #toArray(Object[])} is 
     *           recommended.</p> 
     * @return <code>toArray(new Object[size()])</code>
     */
    public Object[] toArray() {
        return toArray(new Object[size()]);
    }

    /**
     * Fills the specified array with the elements of this collection in 
     * the proper sequence. 
     *
     * @param  array the array into which the elements of this collection
     *         are to be stored.
     * @return the specified array.
     * @throws UnsupportedOperationException if <pre>array.length < size()</pre> 
     */
    public Object[] toArray(Object[] array) {
        int size = size();
        if (array.length < size) {
            throw new UnsupportedOperationException(
                    "Destination array too small");
        } else if (array.length > size) {
            array[size] = null; // As per Collection contract.
        }
        Iterator itr = iterator();
        for (int pos = 0; pos < size; pos++) {
            array[pos] = itr.next();
        }
        return array;
    }

    /**
     * Returns the textual representation of this collection.
     * 
     * @return this collection textual representation.
     */
    public Text toText() {
        TextBuilder tb = TextBuilder.newInstance();
        tb.append('[');
        Iterator itr = this.iterator();
        int pos = this.size();
        while (--pos >= 0) {
            tb.append(itr.next());
            if (pos != 0) {
                tb.append(", ");
            }
        }
        tb.append(']');
        return tb.toText();
    }

    /**
     * Compares the specified object with this collection for equality.  Returns
     * <code>true</code> if and only both collection contains the same elements
     * regardless of the order.
     *
     * @param obj the object to be compared for equality with this collection.
     * @return <code>true</code> if the specified object is equal to this
     *         collection; <code>false</code> otherwise.
     */
    public boolean equals(Object obj) {
        return (obj == this || (obj instanceof Collection
                && ((Collection) obj).size() == size() && 
                    containsAll((Collection) obj)));
    }

    /**
     * Returns the hash code for this collection (independent from the 
     * collection order).
     *
     * @return the hash code for this collection.
     */
    public int hashCode() {
        Iterator itr = iterator();
        int hash = 0;
        int pos = size();
        while (--pos >= 0) {
            Object element = itr.next();
            hash += element == null ? 0 : element.hashCode();
        }
        return hash;
    }
}