/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util;

import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.UnsupportedOperationException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.io.IOException;

import javolution.lang.Realtime;
import javolution.text.Text;
import javolution.xml.XMLSerializable;

/**
 * <p> This class represents collections which can quickly be iterated over 
 *     (forward or backward) and which an be made {@link #shared() thread-safe}
 *     and/or {@link #unmodifiable() unmodifiable}.</p>
 *
 * <p> Fast collections can be iterated over  without creating new objects
 *     and without using {@link #iterator iterators} .
 *     [code]
 *         public boolean search(Object item, FastCollection c) {
 *             for (Record r = c.head(), end = c.tail(); (r = r.getNext()) != end;) {
 *                 if (item.equals(c.valueOf(r))) return true;
 *             }
 *             return false;
 *         }
 *     [/code]</p>
 *     
 * <p> Fast collections are thread-safe when marked {@link #shared shared}
 *     (can be read, iterated over or modified concurrently).
 *     [code]
 *         public class Foo {
 *             private static final Collection<Foo> INSTANCES = new FastTable().shared();
 *             public Foo() {
 *                 INSTANCES.add(this);
 *             }
 *             public static void showInstances() {
 *                 for (Foo foo : INSTANCES) { // Iterations are thread-safe even if new Foo instances are added.
 *                      System.out.println(foo);
 *                 }
 *             }
 *         }[/code]</p>
 *     
 * <p> Users may provide a read-only view of any {@link FastCollection} 
 *     instance using the {@link #unmodifiable()} method (the view is 
 *     thread-safe if the collection is {@link #shared shared}).
 *     [code]
 *         class Foo {
 *             private static final FastTable<Foo> INSTANCES = new FastTable().shared();
 *             Foo() {
 *                INSTANCES.add(this);
 *             }
 *             public static Collection<Foo> getInstances() {
 *                 return INSTANCES.unmodifiable(); // Returns a public unmodifiable view over the shared collection.
 *             }
 *         }[/code]</p>
 *     
 * <p> Finally, {@link FastCollection} may use custom {@link #getValueComparator
 *     comparators} for element equality or ordering if the collection is 
 *     ordered (e.g. <code>FastTree</code>).
 *     
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 5.4.5, March 23, 2010
 */
public abstract class FastCollection <E>  implements
        Collection <E> , XMLSerializable, Realtime {

    /**
     * Default constructor.  
     */
    protected FastCollection() {
    }

    /**
     * Returns the number of values in this collection. 
     *
     * @return the number of values.
     */
    public abstract int size();

    /**
     * Returns the head record of this collection; it is the record such as 
     * <code>head().getNext()</code> holds the first collection value.
     * 
     * @return the head record.
     */
    public abstract Record head();

    /**
     * Returns the tail record of this collection; it is the record such as
     * <code>tail().getPrevious()</code> holds the last collection value.
     * 
     * @return the tail record.
     */
    public abstract Record tail();

    /**
     * Returns the collection value for the specified record.
     *
     * @param record the record whose current value is returned.
     * @return the current value.
     */
    public abstract  E  valueOf(Record record);

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
     * @return the unmodifiable view over this collection.
     */
    public Collection <E>  unmodifiable() {
        return new Unmodifiable();
    }

    /**
     * <p> Returns a thread-safe read-write view of this collection.</p>
     * <p> The default implementation performs synchronization on read and write.
     *     Sub-classes may provide more efficient implementation (e.g.
     *     only synchronizing on writes modifying the internal data structure).</p>
     * <p> Having a shared collection does not mean that modifications made
     *     by onethread are automatically viewed by others thread. Which in practice
     *     is not an issue. In a well-behaved system, threads need to synchronize
     *     only at predetermined synchronization points (the fewer the better).</p>
     *
     * @return a thread-safe collection.
     */
    public Collection <E>  shared() {
        return new Shared();
    }

    /**
     * Returns an iterator over the elements in this collection 
     * (allocated on the stack when executed in a 
     * {@link javolution.context.StackContext StackContext}).
     *
     * @return an iterator over this collection's elements.
     */
    public Iterator <E>  iterator() {
        return FastIterator.valueOf(this);
    }

    /**
     * Returns the value comparator for this collection (default 
     * {@link FastComparator#DEFAULT}).
     *
     * @return the comparator to use for value equality (or ordering if 
     *        the collection is ordered)
     */
    public FastComparator <? super E>  getValueComparator() {
        return FastComparator.DEFAULT;
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
    public boolean add( E  value) {
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
        for (Record r = head(), end = tail(); (r = r.getNext()) != end;) {
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
        for (Record head = head(), r = tail().getPrevious(); r != head; r = r.getPrevious()) {
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
        for (Record r = head(), end = tail(); (r = r.getNext()) != end;) {
            if (valueComp.areEqual(value, valueOf(r)))
                return true;
        }
        return false;
    }

    /**
     * Appends all of the values in the specified collection to the end of
     * this collection, in the order that they are returned by the specified
     * collection's iterator.
     *
     * @param c collection whose values are to be added to this collection.
     * @return <code>true</code> if this collection changed as a result of 
     *         the call; <code>false</code> otherwise.
     */
    public boolean addAll(Collection <? extends E>  c) {
        boolean modified = false;
        Iterator <? extends E>  itr = c.iterator();
        while (itr.hasNext()) {
            if (add(itr.next())) {
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
    public boolean containsAll(Collection <?>  c) {
        Iterator <?>  itr = c.iterator();
        while (itr.hasNext()) {
            if (!contains(itr.next()))
                return false;
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
    public boolean removeAll(Collection <?>  c) {
        boolean modified = false;
        // Iterates from the tail and remove the record if present in c. 
        for (Record head = head(), r = tail().getPrevious(), previous; r != head; r = previous) {
            previous = r.getPrevious(); // Saves previous.
            if (FastCollection.contains(c, valueOf(r), this.getValueComparator())) {
                delete(r);
                modified = true;
            }
        }
        return modified;
    }

    private static boolean contains(Collection c, Object obj, FastComparator cmp) {
        if ((c instanceof FastCollection)
                && ((FastCollection) c).getValueComparator().equals(cmp))
            return c.contains(obj); // Direct is ok (same value comparator). 
        Iterator <?>  itr = c.iterator();
        while (itr.hasNext()) {
            if (cmp.areEqual(obj, itr.next()))
                return true;
        }
        return false;
    }

    /**
     * Retains only the values in this collection that are contained in the
     * specified collection.
     *
     * @param c collection that defines which values this set will retain.
     * @return <code>true</code> if this collection changed as a result of 
     *         the call; <code>false</code> otherwise.
     */
    public boolean retainAll(Collection <?>  c) {
        boolean modified = false;
        // Iterates from the tail and remove the record if not present in c. 
        for (Record head = head(), r = tail().getPrevious(), previous; r != head; r = previous) {
            previous = r.getPrevious(); // Saves previous.
            if (!FastCollection.contains(c, valueOf(r), this.getValueComparator())) {
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
     * @throws UnsupportedOperationException if <code>array.length < size()</code> 
     */
    public  <T> T [] toArray( T [] array) {
        int size = size();
        if (array.length < size)
            throw new UnsupportedOperationException(
                    "Destination array too small");
        if (array.length > size) {
            array[size] = null; // As per Collection contract.
        }
        int i = 0;
        Object[] arrayView = array;
        for (Record r = head(), end = tail(); (r = r.getNext()) != end;) {
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
        // We use Text concatenation instead of TextBuilder to avoid copying 
        // the text representation of the record values (unknown length).
        Text text = Text.valueOf("{");
        for (Record r = head(), end = tail(); (r = r.getNext()) != end;) {
            text = text.plus(valueOf(r));
            if (r.getNext() != end) {
                text = text.plus(", ");
            }
        }
        return text.plus("}");
    }

    /**
     * Returns the <code>String</code> representation of this 
     * {@link FastCollection}.
     *
     * @return <code>toText().toString()</code>
     */
    public final String toString() {
        return toText().toString();
    }

    /**
     * Compares the specified object with this collection for equality.
     * The default behavior is to consider two collections equal if they 
     * hold the same values and have the same iterative order if any of 
     * the collections is an ordered collection ({@link List} instances).
     * Equality comparisons are performed using this collection 
     * {@link #getValueComparator value comparator}.
     *
     * @param obj the object to be compared for equality with this collection
     * @return <code>true</code> if the specified object is a collection with
     *         the same content and iteration order when necessary; 
     *         <code>false</code> otherwise.
     */
    public boolean equals(Object obj) {
        if (this instanceof List)
            return (obj instanceof List) ? equalsOrder((List) obj) : false;
        if (obj instanceof List)
            return false; // 'this' is not a list but obj is!
        if (!(obj instanceof Collection))
            return false; // Can only compare collections.
        Collection that = (Collection) obj;
        return (this == that) || ((this.size() == that.size()) && containsAll(that));
    }

    private boolean equalsOrder(List that) {
        if (that == this)
            return true;
        if (this.size() != that.size())
            return false;
        Iterator thatIterator = that.iterator();
        final FastComparator comp = this.getValueComparator();
        for (Record r = head(), end = tail(); (r = r.getNext()) != end;) {
            Object o1 = valueOf(r);
            Object o2 = thatIterator.next();
            if (!comp.areEqual(o1, o2))
                return false;
        }
        return true;
    }

    /**
     * Returns the hash code for this collection. For non-ordered collection
     * the hashcode of this collection is the sum of the hashcode of its 
     * values.

     * @return the hash code for this collection.
     */
    public int hashCode() {
        if (this instanceof List)
            return hashCodeList();
        final FastComparator valueComp = this.getValueComparator();
        int hash = 0;
        for (Record r = head(), end = tail(); (r = r.getNext()) != end;) {
            hash += valueComp.hashCodeOf(valueOf(r));
        }
        return hash;
    }

    private int hashCodeList() {
        final FastComparator comp = this.getValueComparator();
        int h = 1;
        for (Record r = head(), end = tail(); (r = r.getNext()) != end;) {
            h = 31 * h + comp.hashCodeOf(valueOf(r));
        }
        return h;
    }

    /**
     * This interface represents the collection records which can directly be
     * iterated over.
     */
    public interface Record {

        /**
         * Returns the record before this one.
         * 
         * @return the previous record.
         */
        public Record getPrevious();

        /**
         * Returns the record after this one.
         * 
         * @return the next record.
         */
        public Record getNext();
    }

    /**
     * This inner class represents an unmodifiable view over the collection.
     */
    class Unmodifiable  extends FastCollection implements List, Set {

        // Implements abstract method.
        public int size() {
            return FastCollection.this.size();
        }

        // Implements abstract method.
        public Record head() {
            return FastCollection.this.head();
        }

        // Implements abstract method.
        public Record tail() {
            return FastCollection.this.tail();
        }

        // Implements abstract method.
        public Object valueOf(Record record) {
            return FastCollection.this.valueOf(record);
        }

        // Forwards...
        public boolean contains(Object value) {
            return (FastCollection.this).contains(value);
        }

        // Forwards...
        public boolean containsAll(Collection c) {
            return (FastCollection.this).containsAll(c);
        }

        // Forwards...
        public FastComparator getValueComparator() {
            return FastCollection.this.getValueComparator();
        }

        // Disallows...
        public boolean add(Object obj) {
            throw new UnsupportedOperationException("Unmodifiable");
        }

        // Disallows...
        public void delete(Record node) {
            throw new UnsupportedOperationException("Unmodifiable");
        }

        //////////////////////////////////////////
        // List interface supplementary methods //
        //////////////////////////////////////////
        public boolean addAll(int index, Collection c) {
            throw new UnsupportedOperationException("Unmodifiable");
        }

        public Object get(int index) {
            return ((List) FastCollection.this).get(index);
        }

        public Object set(int index, Object element) {
            throw new UnsupportedOperationException("Unmodifiable");
        }

        public void add(int index, Object element) {
            throw new UnsupportedOperationException("Unmodifiable");
        }

        public Object remove(int index) {
            throw new UnsupportedOperationException("Unmodifiable");
        }

        public int indexOf(Object o) {
            return ((List) FastCollection.this).indexOf(o);
        }

        public int lastIndexOf(Object o) {
            return ((List) FastCollection.this).lastIndexOf(o);
        }

        public ListIterator listIterator() {
            throw new UnsupportedOperationException(
                    "List iterator not supported for unmodifiable collection");
        }

        public ListIterator listIterator(int index) {
            throw new UnsupportedOperationException(
                    "List iterator not supported for unmodifiable collection");
        }

        public List subList(int fromIndex, int toIndex) {
            throw new UnsupportedOperationException(
                    "Sub-List not supported for unmodifiable collection");
        }
    }

    /**
     * This inner class represents a thread safe view (read-write) over the
     * collection.
     */
    private class Shared implements Collection , Serializable {

        public synchronized int size() {
            return FastCollection.this.size();
        }

        public synchronized boolean isEmpty() {
            return FastCollection.this.isEmpty();
        }

        public synchronized boolean contains(Object o) {
            return FastCollection.this.contains(o);
        }

        public synchronized Object[] toArray() {
            return FastCollection.this.toArray();
        }

        public synchronized Object[] toArray(Object[] a) {
            return FastCollection.this.toArray(a);
        }

        public synchronized Iterator iterator() {
            if (FastCollection.this instanceof List) 
                return new ListArrayIterator(FastCollection.this.toArray());
            return new CollectionArrayIterator(FastCollection.this.toArray());
        }

        public synchronized boolean add(Object  e) {
            return ((FastCollection)FastCollection.this).add(e);
        }

        public synchronized boolean remove(Object o) {
            return FastCollection.this.remove(o);
        }

        public synchronized boolean containsAll(Collection c) {
            return FastCollection.this.containsAll(c);
        }

        public synchronized boolean addAll(Collection c) {
            return FastCollection.this.addAll(c);
        }

        public synchronized boolean removeAll(Collection c) {
            return FastCollection.this.removeAll(c);
        }

        public synchronized boolean retainAll(Collection c) {
            return FastCollection.this.retainAll(c);
        }

        public synchronized void clear() {
            FastCollection.this.clear();
        }

        public synchronized String toString() {
            return FastCollection.this.toString();
        }

        private synchronized void writeObject(ObjectOutputStream s) throws IOException {
            s.defaultWriteObject();
        }

        private class ListArrayIterator implements Iterator {

            private final Object[] _elements;

            private int _index;

            private int _removed;

            public ListArrayIterator(Object[] elements) {
                _elements = elements;
            }

            public boolean hasNext() {
                return _index < _elements.length;
            }

            public Object next() {
                return _elements[_index++];
            }

            public void remove() {
                if (_index == 0)
                    throw new java.lang.IllegalStateException();
                Object removed = _elements[_index - 1];
                if (removed == NULL) // Double removed.
                    throw new java.lang.IllegalStateException();
                _elements[_index - 1] = NULL;
                _removed++;
                synchronized (FastCollection.Shared.this) {
                    ((List) FastCollection.this).remove(_index - _removed);
                }
            }
        }

        private class CollectionArrayIterator implements Iterator {

            private final Object[] _elements;

            private int _index;

            private Object _next;

            public CollectionArrayIterator(Object[] elements) {
                _elements = elements;
            }

            public boolean hasNext() {
                return _index < _elements.length;
            }

            public Object next() {
                return _next = _elements[_index++];
            }

            public void remove() {
                if (_next == null)
                    throw new java.lang.IllegalStateException();
                FastCollection.Shared.this.remove(_next);
                _next = null;
            }
        }
    }
    private static final Object NULL = new Object();
}
