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
import java.io.Serializable;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javolution.annotation.Format;
import javolution.annotation.StackSafe;
import javolution.lang.Functor;
import javolution.lang.Immutable;
import javolution.lang.Predicate;
import javolution.text.Cursor;
import javolution.text.TextContext;

/**
 * <p> A {@link javolution.annotation.StackSafe stack-safe}, 
 *     time-deterministics and closure-ready collections.</p>
 * 
 * <p> Whereas Java current evolution leads to more and more classes being parts 
 *     of the standard library; Javolution approach is quite the opposite. 
 *     It aims to provide only the quintessential classes from which all 
 *     others can be derived. For example, the following illustrates how  
 *     a {@link FastTable} can advantageously replace a 
 *     {@link java.util.TreeSet} both in terms of space and performance.
 *     [code]
 *     class EmployeeSet extends FastTable<Employee> {
 *         public boolean add(Employee e) {
 *             return addIfAbsent(e); // Avoids duplicate.
 *         }
 *         public boolean isOrdered() {
 *             return true; // Keeps elements ordered. 
 *         }
 *         public FastComparator<Employee> comparator() {
 *             return employeeComparator; // E.g. comparison based on names.
 *         }
 *      }
 *      [/code]</p>
 * 
 * <p> All fast collections classes are {@link StackSafe Stack-Safe}.</p>
 * 
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0.0, December 12, 2012
 */
@StackSafe
@Format(text = FastCollection.TextFormat.class)
public abstract class FastCollection<E> implements Collection<E>, Serializable {

    /**
     * Default constructor.  
     */
    protected FastCollection() {
    }

    /**
     * <p> Returns an unmodifiable/{@link Immutable} view of this collection. 
     *     Attempts to modify the returned collection result in an 
     *     {@link UnsupportedOperationException} being thrown.</p> 
     * <p> If this collection is a {@link List} the instance returned is also 
     *     a list. If this collection is a {@link Set} the instance returned 
     *     is also a set.</p>
     * 
     * @return an unmodifiable view over this collection.
     */
    public abstract FastCollection<E> unmodifiable();

    /**
     * <p> Returns a thread-safe read-write view of this collection.</p>
     * <p> Having a shared collection does not mean that modifications made
     *     by one thread are automatically viewed by others thread.
     *     For this to happen the thread must obtain a shared lock, then 
     *     all elements cached in its CPU registers or CPU cache are invalidated
     *     and refreshed from main memory. When the thread releases the 
     *     shared lock, all elements cached in its CPU registers or CPU cache are
     *     flushed (written) to main memory. In a well-designed system, 
     *     synchronization points should only occur when required.</p>
     * <p> Iterators on {@link #shared} collections are deprecated as the may 
     *     raise {@link ConcurrentModificationException}.  {@link #doWhile 
     *     Closures} should be used to iterate over shared collections
     *    (Note: All fast collection methods use closures to iterate).</p> 
     * <p> If this collection is a {@link List} the instance returned is also 
     *     a list. If this collection is a {@link Set} the instance returned 
     *     is also a set.</p>
     */
    public abstract FastCollection<E> shared();

    /**
     * Returns the element comparator for this collection (default 
     * {@link FastComparator#DEFAULT} FastComparator.DEFAULT). 
     * If this method is overriden, it is possible that elements considered
     * distinct using the default equality comparator, would appear to be 
     * equals as far as this collection is concerned. For example, a 
     * {@link FastComparator#LEXICAL lexical comparator} considers that two 
     * {@link CharSequence} are equals if they hold the same characters 
     * regardless of the {@link CharSequence} implementation.
     * A direct consequences is that fast collections equality/hashcode 
     * is linked to the collection comparator and two collections can be 
     * considered equals only if they use the same comparators.
     *
     * @return the comparator to use for element equality (or ordering if 
     *        the collection is ordered).
     * @see #equals
     * @see #hashCode()
     */
    public FastComparator<E> comparator() {
        return (FastComparator<E>) FastComparator.DEFAULT;
    }    

    /**
     * Indicates if this collecion is ordered (default <code>false</code>). 
     * Sub-classes for which this method returns <code>true</code>
     * must ensure that the {@link #add} method keeps the collection ordered.
     */
    public boolean isOrdered() {
        return false;
    }

    /***************************************************************************
     * Closures operations.
     */
    /**
     * Applies the specified functor to this collection elements; returns the
     * results of the evaluations different from <code>null</code>.
     */
    public abstract <R> FastCollection<R> forEach(final Functor<E, R> functor);

    /**
     * Iterates this collection elements until the specified predicate 
     * returns <code>false</code>.
     */
    public abstract void doWhile(final Predicate<E> predicate);

    /**
     * Removes from this collection all the elements matching the specified 
     * predicate.
     * 
     * @return <code>true</code> if this collection changed as a result of 
     *         the call; <code>false</code> otherwise.
     */
    public abstract boolean removeAll(final Predicate<E> predicate);

    /**
     * Retains from this collection all the elements matching the specified 
     * predicate.
     * 
     * @return <code>true</code> if this collection changed as a result of 
     *         the call; <code>false</code> otherwise.
     */
    public boolean retainAll(final Predicate<E> predicate) {
        return removeAll(new Predicate<E>() {

            public Boolean evaluate(E param) {
                return !predicate.evaluate(param);
            }

        });
    }

    /**
     * Returns all the elements different from <code>null</code> matching 
     * the specified predicate.
     */
    public FastCollection<E> findAll(final Predicate<E> predicate) {
        return forEach(new Functor<E, E>() {

            public E evaluate(E param) {
                return predicate.evaluate(param) ? param : null;
            }

        });
    }

    /**
     * Returns the first element matching the specified predicate.
     */
    public E findFirst(final Predicate<E> predicate) {
        final Object[] found = new Object[1];
        doWhile(new Predicate<E>() {

            public Boolean evaluate(E param) {
                if (predicate.evaluate(param)) {
                    found[0] = param;
                    return false; // Exits.
                }
                return true;
            }

        });
        return (E) found[0];
    }

    /***************************************************************************
     * Collections operations.
     */
    /**
     * Returns an iterator overs this collection element (for backward
     * compatiblity with <code>java.util.Collection</code>).
     * Fast collection operations do not use iterators but {@link #doWhile 
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
        this.doWhile(new Predicate<E>() {

            public Boolean evaluate(E param) {
                count[0]++;
                return true;
            }

        });
        return count[0];
    }

    /**
     * Adds the specified element; if the collection {@link #isOrdered is ordered}
     * the ordering of the collection is maintained after the element is added.
     * 
     * <p>Note: This default implementation throws 
     *          <code>UnsupportedOperationException</code>.</p>
     * 
     * @param element the element to be added to this collection.
     * @return <code>true</code> (as per the general contract of the
     *         <code>Collection.add</code> method).
     * @throws UnsupportedOperationException if the collection is not modifiable.
     */
    public boolean add(final E element) {
        throw new UnsupportedOperationException();
    }

    /**
     * Removes the first occurrence in this collection of the specified element.
     *
     * @param element the element to be removed from this collection.
     * @return <code>true</code> if this collection contained the specified
     *         element; <code>false</code> otherwise.
     * @throws UnsupportedOperationException if the collection is not modifiable.
     */
    public boolean remove(final Object element) {
        final FastComparator comp = comparator();
        final boolean[] found = new boolean[]{false};
        return removeAll(new Predicate<E>() {

            public Boolean evaluate(E param) {
                if (!found[0] && comp.areEqual(element, param)) {
                    found[0] = true;
                    return true;
                }
                return false;
            }

        });
    }

    /**
     * Removes all of the elements from this collection (optional operation).
     *
     * @throws UnsupportedOperationException if not supported.
     */
    public void clear() {
        removeAll(new Predicate<E>() {

            public Boolean evaluate(E param) {
                return true;
            }

        });
    }

    /**
     * Indicates if this collection is empty.
     *
     * @return <code>true</code> if this collection contains no element;
     *         <code>false</code> otherwise.
     */
    public final boolean isEmpty() {
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
    public boolean contains(final Object element) {
        final FastComparator comp = comparator();
        final boolean[] found = new boolean[]{false};
        this.doWhile(new Predicate<E>() {

            public Boolean evaluate(E param) {
                if (comp.areEqual(element, param)) {
                    found[0] = true;
                    return false; // Exits.
                }
                return true;
            }

        });
        return found[0];
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
        final boolean[] modified = new boolean[]{false};
        that.doWhile(new Predicate<E>() {

            public Boolean evaluate(E param) {
                if (add(param)) {
                    modified[0] = true;
                }
                return true;
            }

        });
        return modified[0];
    }

    /**
     * Indicates if this collection contains all of the elements of the
     * specified collection.
     *
     * @param  that collection to be checked for containment in this collection.
     * @return <code>true</code> if this collection contains all of the elements
     *         of the specified collection; <code>false</code> otherwise.
     */
    public boolean containsAll(final Collection<?> that) {
        if (that instanceof FastCollection)
            return containsAllFast((FastCollection) that);
        for (Object e : that) {
            if (!contains(e))
                return false;
        }
        return true;
    }

    private boolean containsAllFast(final FastCollection<E> that) {
        final boolean[] containsAll = new boolean[]{true};
        that.doWhile(new Predicate<E>() {

            public Boolean evaluate(E param) {
                if (!FastCollection.this.contains(param)) {
                    containsAll[0] = false;
                    return false; // Exits.
                }
                return true;
            }

        });
        return containsAll[0];
    }

    /**
     * Removes from this collection all the elements that are contained in the
     * specified collection.
     *
     * @param that collection that defines which elements will be removed from
     *          this collection.
     * @return <code>true</code> if this collection changed as a result of 
     *         the call; <code>false</code> otherwise.
     */
    public boolean removeAll(final Collection<?> that) {
        return this.removeAll(new Predicate<E>() {

            public Boolean evaluate(E param) {
                return that.contains(param);
            }

        });
    }

    /**
     * Retains only the elements in this collection that are contained in the
     * specified collection.
     *
     * @param that collection that defines which elements this set will retain.
     * @return <code>true</code> if this collection changed as a result of 
     *         the call; <code>false</code> otherwise.
     */
    public boolean retainAll(final Collection<?> that) {
        return this.retainAll(new Predicate<E>() {

            public Boolean evaluate(E param) {
                return that.contains(param);
            }

        });
    }

    /**
     * Returns a new array allocated on the heap containing all of the elements 
     * in this collection in proper sequence.
     * 
     * <p> Note: To avoid heap allocation {@link #toArray(Object[])} is 
     *           recommended.</p> 
     * @return <code>toArray(new Object[size()])</code>
     */
    public Object[] toArray() {
        return toArray(new Object[size()]);
    }

    /**
     * Fills the specified array with the elements of this collection in 
     * the proper sequence. If the collection fits in the specified array,
     * it is returned therein. Otherwise, a new array is allocated with 
     * the runtime type of the specified array and the size of this collection.
     * If this collection fits in the specified array with room to spare 
     * (i.e., the array has more elements than this collection), the element in
     * the array immediately following the end of the collection is set to
     * <code>null</code>. 
     *  
     * @param  array the array into which the elements of this collection
     *         are to be stored if it has enough capacity.
     * @return an array containing this collections elements.
     * @throws IndexOutOfBoundsException  if <code>array.length < size()</code> 
     */
    public <T> T[] toArray(final T[] array) { // Support concurrent modifications if Shared.
        final T[][] result = (T[][]) new Object[1][];
        final int[] size = new int[1];
        doWhile(new Predicate<E>() { // Synchronized if Shared instance.

            int i;

            { // Instance initializer.
                size[0] = size();
                result[0] = (size[0] <= array.length) ? array
                        : (T[]) java.lang.reflect.Array
                        .newInstance(array.getClass().getComponentType(), size[0]);
            }

            public Boolean evaluate(E param) {
                result[0][i++] = (T) param;
                return true;
            }

        });
        if (result[0].length > size[0]) {
            result[0][size[0]] = null; // As per Collection contract.
        }
        return result[0];
    }

    /**
     * Returns the <code>String</code> representation of this 
     * {@link FastCollection}.
     *
     * @return <code>TextContext.getFormat(FastCollection.class).format(this)</code>
     */
    @Override
    public String toString() {
        return TextContext.getFormat(FastCollection.class).format(this);
    }

    /**
     * Compares the specified object with this collection for equality.
     * If this collection is a set, returns <code>true</code> if the specified
     * object is also a set, the two sets have the same size, they   
     * use the same comparator and every member of the specified set
     * is contained in this set using that common comparator.
     * If this collection is a list, returns <code>true</code> if and only 
     * if the specified object is also a list, both lists have the same size,
     * they use the same comparator and all corresponding pairs of elements in
     * the two lists are <i>equal</i> using their common comparator.
     * If this collection is neither a list, nor a set, this method returns 
     * the default object equality (<code>this == obj</code>).
     *
     * @param obj the object to be compared for equality with this collection
     * @return <code>true</code> if both collection are considered equals;
     *        <code>false</code> otherwise. 
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        FastComparator thatComparator = (obj instanceof FastCollection)
                ? ((FastCollection) obj).comparator() : FastComparator.DEFAULT;
        if (!this.comparator().equals(thatComparator)) return false;
        if (this instanceof Set) {
            if (!(obj instanceof Set)) return false;
            Set that = (Set) obj;
            if (this.size() != that.size()) return false;
            return (this.containsAll(that));
        } else if (this instanceof List) {
            final List that = (List) obj;
            if (this.size() != that.size()) return false;
            final boolean[] areEqual = new boolean[]{true};
            final FastComparator<E> comp = this.comparator();
            this.doWhile(new Predicate<E>() {

                Iterator<E> it = that.iterator();

                public Boolean evaluate(E param) {
                    if (it.hasNext() && comp.areEqual(param, it.next())) {
                        return true;
                    }
                    areEqual[0] = false;
                    return false; // Exits.
                }

            });
            return areEqual[0];
        } else {
            return false;
        }
    }

    /**
     * Returns the hash code for this collection. 
     * The hash code of a set is defined to be the sum of the hash codes of 
     * the elements in the set, where the hash code of a <code>null</code> 
     * element is defined to be zero. 
     * The hash code of a list is defined to be the result of the following 
     * calculation:
     * <pre>
     *  int hashCode = 1;
     *  for (E e : list)
     *      hashCode = 31*hashCode + comparator().hashCodeOf(e);
     * </pre>
     * If this collection is neither a list, nor a set the default object 
     * hashcode is returned.
     */
    @Override
    public int hashCode() {
        final int[] hash = new int[1];
        final FastComparator<E> comp = comparator();
        if (this instanceof Set) {
            this.doWhile(new Predicate<E>() {

                public Boolean evaluate(E param) {
                    hash[0] += comp.hashCodeOf(param);
                    return true;
                }

            });
            return hash[0];
        } else if (this instanceof List) {
            hash[0] = 1;
            this.doWhile(new Predicate<E>() {

                public Boolean evaluate(E param) {
                    hash[0] = 31 * hash[0] + comp.hashCodeOf(param);
                    return true;
                }

            });
            return hash[0];
        } else {
            return super.hashCode();
        }
    }

    /**
     * Holds the default text format for fast collections (parsing not supported).
     */
    public static class TextFormat extends javolution.text.TextFormat<FastCollection> {

        @Override
        public FastCollection parse(CharSequence csq, Cursor cursor) throws IllegalArgumentException {
            throw new UnsupportedOperationException("Parsing Of Generic FastCollection Not supported");
        }

        @Override
        public Appendable format(FastCollection fc, final Appendable dest) throws IOException {
            dest.append('{');
            fc.doWhile(new  Predicate<Object>() {

                boolean isFirst = true;

                public Boolean evaluate(Object param) {
                    try {
                        if (!isFirst) {
                            dest.append(", ");
                        } else {
                            isFirst = false;
                        }
                        if (param != null) {
                            javolution.text.TextFormat tf = TextContext.getFormat(param.getClass());
                            tf.format(param, dest);
                        } else {
                            dest.append("null");
                        }
                        return true;
                    } catch (IOException error) {
                        throw new RuntimeException(error);
                    }
                }

            });
            return dest.append('}');
        }

    }

}
