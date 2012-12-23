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
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import javolution.annotation.Format;
import javolution.annotation.StackSafe;
import javolution.lang.Functor;
import javolution.text.Cursor;
import javolution.text.TextContext;

/**
 * <p> This class represents {@link javolution.annotation.StackSafe stack-safe},
 *     time-deterministics and closure-ready collections.</p>
 * 
 * <p> Unlike standard {@link java.util.collection}, collection elements 
 *     cannot be <code>null</code>.</p> 
 *     
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 5.4.5, March 23, 2010
 */
@StackSafe
@Format(text = FastCollection.TextFormat.class)
public abstract class FastCollection<E> implements Collection<E> {

    /**
     * Holds the default text format for fast collections (parsing not supported).
     */
    public static class TextFormat extends javolution.text.TextFormat<FastCollection> {

        @Override
        public FastCollection parse(CharSequence csq, Cursor cursor) throws IllegalArgumentException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Appendable format(FastCollection fc, Appendable dest) throws IOException {
            dest.append('{');
            for (Iterator it = fc.iterator(); it.hasNext();) {
                Object obj = it.next();
                javolution.text.TextFormat tf = TextContext.getFormat(obj.getClass());
                tf.format(obj, dest);
                if (it.hasNext()) {
                    dest.append(", ");
                }
            }
            return dest.append('}');
        }
    }

    /**
     * Default constructor.  
     */
    protected FastCollection() {
    }

    /**
     * Returns the unmodifiable view associated to this collection. 
     * Attempts to modify the returned collection result in an 
     * {@link UnsupportedOperationException} being thrown. 
     * 
     * @return the unmodifiable view over this collection.
     */
    public FastCollection<E> unmodifiable() {
        return new Unmodifiable();
    }

    /**
     * <p> Returns a thread-safe read-write view of this collection.</p>
     * <p> The default implementation performs synchronization on read and write.
     *     Sub-classes may provide more efficient implementation.</p>
     * <p> Having a shared collection does not mean that modifications made
     *     by one thread are automatically viewed by others thread.
     *     For this to happen the thread must obtain a shared lock, then 
     *     all values cached in its CPU registers or CPU cache are invalidated
     *     and refreshed from main memory. When the thread releases the 
     *     shared lock, all values cached in its CPU registers or CPU cache are
     *     flushed (written) to main memory. 
     *     In a well-designed system, synchronization points should be 
     *     unfrequent.</p>
     * <p> Iterators on {@link #shared} collections are deprecated as the may 
     *     raise {@link ConcurrentModificationException}.  {@link #forEach 
     *     Closures} should be used to iterate over shared collections
     *    (note: all fast collections methods are based on closure).</p> 
     */
    public FastCollection<E> shared() {
        return new Shared();
    }

    /**
     * Returns the value comparator for this collection (default 
     * {@link FastComparator#DEFAULT}).
     *
     * @return the comparator to use for value equality (or ordering if 
     *        the collection is ordered)
     */
    public FastComparator<? super E> getValueComparator() {
        return FastComparator.DEFAULT;
    }

    /***************************************************************************
     * Closures operations.
     */
    
    /**
     * Applies the specified functor to this collection elements; 
     * returns "non-null" evaluated elements.
     */
    public abstract <R> FastCollection<R> forEach(final Functor<E, R> functor);

    /**
     * Returns all the elements matching the specified predicate.
     */
    public FastCollection<E> findAll(final Functor<E, Boolean> predicate) {
        return this.forEach(new Functor<E, E>() {
            public E evaluate(E param) {
                return predicate.evaluate(param) ? param : null;
            }
        });
    }

    /**
     * Returns the first element matching the specified predicate.
     */
    public E findFirst(final Functor<E, Boolean> predicate) {
        final Object[] found = new Object[1];
        this.forEach(new Functor<E, E>() {
            public E evaluate(E param) {
                if ((found[0] == null) && predicate.evaluate(param)) {
                    found[0] = param;
                }
                return null;
            }
        });
        return (E) found[0];
    }

    /**
     * Removes from this collection all the elements matching the specified 
     * predicate.
     * 
     * @return <code>true</code> if this collection changed as a result of 
     *         the call; <code>false</code> otherwise.
     */
    public abstract boolean removeAll(final Functor<E, Boolean> predicate);

    /**
     * Retains from this collection all the elements matching the specified 
     * predicate.
     * 
     * @return <code>true</code> if this collection changed as a result of 
     *         the call; <code>false</code> otherwise.
     */
    public boolean retainAll(final Functor<E, Boolean> functor) {
        return removeAll(new Functor<E, Boolean>() {
            public Boolean evaluate(E param) {
                return !functor.evaluate(param);
            }
        });
    }

    /***************************************************************************
     * Collections operations.
     */
    /**
     * Returns an iterator overs this collection element (for backward
     * compatiblity with <code>java.util.Collection</code>).
     * Fast collection operations do not use iterators but {@link #forEach 
     * closures} to iterate over the collections elements. Iterators on 
     * {@link #shared} collections are deprecated as the may raise 
     * {@link ConcurrentModificationException}. 
     */
    public abstract Iterator<E> iterator();

    /**
     * Returns the number of element in this collection. 
     */
    public int size() {
        final int[] count = new int[1];
        this.forEach(new Functor<E, E>() {
            public E evaluate(E param) {
                count[0]++;
                return null;
            }
        });
        return count[0];
    }

    /**
     * Appends the specified (non null) value to the end of this collection
     * (optional operation).
     * 
     * <p>Note: This default implementation always throws 
     *          <code>UnsupportedOperationException</code>.</p>
     * 
     * @param value the value to be appended to this collection.
     * @return <code>true</code> (as per the general contract of the
     *         <code>Collection.add</code> method).
     * @throws UnsupportedOperationException if the collection is not modificable
     *         or the specified value is <code>null</code>.
     */
    public boolean add(final E value) {
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
    public boolean remove(final Object value) {
        final FastComparator valueComp = this.getValueComparator();
        final Object[] found = new Object[1];
        return this.removeAll(new Functor<E, Boolean>() {
            public Boolean evaluate(E param) {
                if ((found[0] == null) && valueComp.areEqual(value, param)) {
                    found[0] = param;
                    return true;
                }
                return false;
            }
        });
    }

    /**
     * Removes all of the values from this collection (optional operation).
     *
     * @throws UnsupportedOperationException if not supported.
     */
    public void clear() {
        this.removeAll(new Functor<E, Boolean>() {
            public Boolean evaluate(E param) {
                return true;
            }
        });
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
    public boolean contains(final Object value) {
        final FastComparator valueComp = this.getValueComparator();
        E found = this.findFirst(new Functor<E, Boolean>() {
            public Boolean evaluate(E param) {
                return valueComp.areEqual(value, param);
            }
        });
        return (found != null) ? true : false;
    }

    /**
     * Appends all of the values in the specified collection to the end of
     * this collection, in the order that they are returned by {@link #forEach} 
     * or the collection's iterator (if the specified collection is not 
     * a fast collection).
     *
     * @param that collection whose values are to be added to this collection.
     * @return <code>true</code> if this collection changed as a result of 
     *         the call; <code>false</code> otherwise.
     */
    public boolean addAll(final Collection<? extends E> that) {
        if (that instanceof FastCollection)
            return addAllFast((FastCollection) that);
        boolean modified = false;
        Iterator<? extends E> it = that.iterator();
        while (it.hasNext()) {
            if (add(it.next())) {
                modified = true;
            }
        }
        return modified;
    }

    private boolean addAllFast(FastCollection that) {
        final boolean[] modified = new boolean[1];
        that.forEach(new Functor<E, E>() {
            public E evaluate(E param) {
                if (add(param)) {
                    modified[0] = true;
                }
                return null;
            }
        });
        return modified[0];
    }

    /**
     * Indicates if this collection contains all of the values of the
     * specified collection.
     *
     * @param  that collection to be checked for containment in this collection.
     * @return <code>true</code> if this collection contains all of the values
     *         of the specified collection; <code>false</code> otherwise.
     */
    public boolean containsAll(final Collection<?> that) {
        final boolean[] mismatch = new boolean[1];
        this.forEach(new Functor<E, E>() {
            public E evaluate(E param) {
                if (!mismatch[0] && !that.contains(param)) {
                    mismatch[0] = true;
                }
                return null;
            }
        });
        return !mismatch[0];
    }

    /**
     * Removes from this collection all the values that are contained in the
     * specified collection.
     *
     * @param that collection that defines which values will be removed from
     *          this collection.
     * @return <code>true</code> if this collection changed as a result of 
     *         the call; <code>false</code> otherwise.
     */
    public boolean removeAll(final Collection<?> that) {
        return this.removeAll(new Functor<E, Boolean>() {
            public Boolean evaluate(E param) {
                return that.contains(param);
            }
        });
    }

    /**
     * Retains only the values in this collection that are contained in the
     * specified collection.
     *
     * @param that collection that defines which values this set will retain.
     * @return <code>true</code> if this collection changed as a result of 
     *         the call; <code>false</code> otherwise.
     */
    public boolean retainAll(final Collection<?> that) {
        return this.retainAll(new Functor<E, Boolean>() {
            public Boolean evaluate(E param) {
                return that.contains(param);
            }
        });
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
     *           the array is too small. IndexOutOfBoundsException is raised 
     *           if the specified array is too small for this collection.</p>
     *
     * @param  array the array into which the values of this collection
     *         are to be stored.
     * @return the specified array.
     * @throws UnsupportedOperationException if <code>array.length < size()</code> 
     */
    public <T> T[] toArray(final T[] array) {
        final int[] count = new int[1];
        this.forEach(new Functor<E, E>() {
            public E evaluate(E param) {
                array[count[0]++] = (T) param;
                return null;
            }
        });
        if (array.length > count[0]) {
            array[count[0]] = null; // As per Collection contract.
        }
        return array;
    }

    /**
     * Returns the <code>String</code> representation of this 
     * {@link FastCollection}.
     *
     * @return <code>toText().toString()</code>
     */
    @Override
    public String toString() {
        return TextContext.getFormat(FastCollection.class).format(this);
    }

    /**
     * Compares the specified object with this collection for equality.
     * The default behavior is to consider two collections equal if they 
     * contains the same elements regardless of the order.
     * Sub-classes may require additional constraints (e.g. same order 
     * for list instances).
     *
     * @param obj the object to be compared for equality with this collection
     * @return <code>(this.size() == that.size()) && containsAll(obj)</code> 
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Collection))
            return false; // Can only compare collections.
        Collection that = (Collection) obj;
        return (this == that) || ((this.size() == that.size())
                && containsAll(that));
    }

    /**
     * Returns the hash code for this collection. The hashcode of this 
     * collection is the sum of the hashcode of its values.
     */
    @Override
    public int hashCode() {
        final FastComparator valueComp = this.getValueComparator();
        final int[] hash = new int[1];
        this.forEach(new Functor<E, E>() {
            public E evaluate(E param) {
                hash[0] += valueComp.hashCodeOf(param);
                return null;
            }
        });
        return hash[0];
    }

    /**
     * This inner class represents an unmodifiable view over the collection.
     */
    private class Unmodifiable extends FastCollection<E> {

        @Override
        public <R> FastCollection<R> forEach(Functor<E, R> functor) {
            return FastCollection.this.forEach(functor);
        }

        @Override
        public boolean removeAll(Functor<E, Boolean> functor) {
            throw new UnsupportedOperationException("Unmodifiable.");
        }

        @Override
        public int size() {
            return FastCollection.this.size();
        }

        @Override
        public FastComparator getValueComparator() {
            return FastCollection.this.getValueComparator();
        }

        @Override
        public boolean add(Object obj) {
            throw new UnsupportedOperationException("Unmodifiable");
        }

        @Override
        public Iterator<E> iterator() {
            return new Iterator<E>() {
                Iterator<E> it = FastCollection.this.iterator();

                public boolean hasNext() {
                    return it.hasNext();
                }

                public E next() {
                    return it.next();
                }

                public void remove() {
                    throw new UnsupportedOperationException("Unmodifiable");
                }
            };
        }
    }

    /**
     * This inner class represents a thread safe view (read-write) over the
     * collection.
     */
    private class Shared extends FastCollection<E> {

        int modifications; // Count modifications.

        @Override
        public synchronized <R> FastCollection<R> forEach(Functor<E, R> functor) {
            return FastCollection.this.forEach(functor);
        }

        @Override
        public synchronized boolean removeAll(Functor<E, Boolean> functor) {
            boolean modified = FastCollection.this.removeAll(functor);
            if (modified) modifications++;
            return modified;
        }

        @Override
        public synchronized int size() {
            return FastCollection.this.size();
        }

        @Override
        public synchronized FastComparator getValueComparator() {
            return FastCollection.this.getValueComparator();
        }

        @Override
        public synchronized boolean add(E obj) {
            boolean modified = FastCollection.this.add(obj);
            if (modified) modifications++;
            return modified;
        }

        @Override
        @Deprecated
        public synchronized Iterator<E> iterator() {
            return new Iterator<E>() {
                int initialModifications = modifications;
                Iterator<E> it = FastCollection.this.iterator();

                public boolean hasNext() {
                    synchronized (Shared.this) {
                        if (initialModifications != modifications)
                            throw new ConcurrentModificationException();
                        return it.hasNext();
                    }
                }

                public E next() {
                    synchronized (Shared.this) {
                        if (initialModifications != modifications)
                            throw new ConcurrentModificationException();
                        return it.next();
                    }
                }

                public void remove() {
                    synchronized (Shared.this) {
                        if (initialModifications != modifications)
                            throw new ConcurrentModificationException();
                        initialModifications++; // Allows single iterator modification.
                        modifications++;
                        it.remove();
                    }
                }
            };
        }

        private synchronized void writeObject(ObjectOutputStream s) throws IOException {
            s.defaultWriteObject();
        }
    }
}
