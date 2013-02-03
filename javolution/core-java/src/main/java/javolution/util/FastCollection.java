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
import javolution.text.TextFormat;

/**
 * <p> A {@link javolution.annotation.StackSafe stack-safe}, 
 *     time-deterministics and closure-ready collections.</p>
 * 
 * <p> Fast collections views can be chained. The following illustrates
 *     how to build a concurrently modifiable collection using a lexical 
 *     comparator for element comparison.
 *     [code]
 *     FastTable<CharSequence> names 
 *        = new FastTable<CharSequence>().usingComparator(FastComparator.LEXICAL).shared();
 *     [/code]
 * <p> Shared collections can be iterated/modified concurrently using closures 
 *     (no concurrent modification exception possible). 
 *     [code]
 *     final TextBuilder txt = new TextBuilder();
 *     names.doWhile(new Predicate<CharSequence>() { // Ok even if names (shared) is concurrently modified.
 *         public Boolean evaluate(CharSequence csq) {
 *              if (txt.size() != 0) tmp.append(", ");
 *              txt.append(csq);
 *              return true;
 *         }
 *     });[/code]</p>
 * <p> This class methods are all thread-safe if the fast collection is 
 *     {@link #shared} (default implementation based on closure).</p>
 * 
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0.0, December 12, 2012
 */
@StackSafe(initialization = false)
@Format(text = FastCollection.PlainText.class)
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
     */
    public abstract FastCollection<E> unmodifiable();

    /**
     * <p> Returns a concurrent read-write view of this collection.</p>
     * <p> Iterators on {@link #shared} collections are deprecated as the may 
     *     raise {@link ConcurrentModificationException}.  {@link #doWhile 
     *     Closures} should be used to iterate over shared collections
     *    (Note: All fast collection methods use closures internally to iterate).</p> 
     * <p> If this collection is a {@link List} the instance returned is also 
     *     a list. If this collection is a {@link Set} the instance returned 
     *     is also a set.</p>
     */
    public abstract FastCollection<E> shared();

    /**
     * <p> Returns a view over this collection using the specified comparator for
     *     element equality and sorting (if supported).</p> 
     * <p> For collection having custom comparators, it is possible that 
     *     elements considered distinct using the default equality 
     *     comparator, would appear to be equals as far as this collection is 
     *     concerned. For example, a {@link FastComparator#LEXICAL lexical 
     *     comparator} considers that two {@link CharSequence} are equals if they
     *     hold the same characters regardless of the {@link CharSequence} 
     *     implementation. On the other hands, for the 
     *     {@link FastComparator#IDENTITY identity} comparator, two elements 
     *     might be considered distinct even if the default object equality 
     *     considers them equals.</p>  
     *
     * @param the comparator to use for element equality (or sorting if 
     *         the collection is sorted).
     * @see #comparator() 
     */
    public abstract FastCollection<E> usingComparator(FastComparator<E> comparator);

    /***************************************************************************
     * Closures operations.
     */
    /**
     * Applies the specified functor to this collection elements; returns
     * all the results of these evaluations different from <code>null</code>.
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
    @SuppressWarnings("unchecked")
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
     * Returns the comparator used by the collection to perform element 
     * comparison (or sorting). 
     */
    @SuppressWarnings("unchecked")
    public FastComparator<E> comparator() {
        return (FastComparator<E>) FastComparator.DEFAULT;
    }

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
     * Adds the specified element.
     * 
     * <p>Note: The default implementation throws 
     *          <code>UnsupportedOperationException</code>.</p>
     * 
     * @param element the element to be added to this collection.
     * @return <code>true</code> (as per the general contract of the
     *         <code>Collection.add</code> method).
     * @throws UnsupportedOperationException if the collection is not modifiable.
     */
    public boolean add(E element) {
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
        final boolean[] found = new boolean[]{false};
        return removeAll(new Predicate<E>() {
            FastComparator<E> cmp = comparator();

            @SuppressWarnings("unchecked")
            public Boolean evaluate(E param) {
                if (!found[0] && (cmp.areEqual((E)element, param))) {
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
        final boolean[] found = new boolean[]{false};
        this.doWhile(new Predicate<E>() {
            FastComparator<E> cmp = comparator();

            @SuppressWarnings("unchecked")
            public Boolean evaluate(E param) {
                if (cmp.areEqual((E)element, param)) {
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
    @SuppressWarnings("unchecked")
    public boolean addAll(final Collection<? extends E> that) {
        if (that instanceof FastCollection)
            return addAllFast((FastCollection<E>) that);
        boolean modified = false;
        Iterator<? extends E> it = that.iterator();
        while (it.hasNext()) {
            if (add(it.next())) {
                modified = true;
            }
        }
        return modified;
    }

    private boolean addAllFast(FastCollection<E> that) {
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
    @SuppressWarnings("unchecked")
    public boolean containsAll(final Collection<?> that) {
        if (that instanceof FastCollection)
            return containsAllFast((FastCollection<E>) that);
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
                if (!contains(param)) {
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
    @SuppressWarnings("unchecked")
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
     * object is also a set, the two sets have the same size and every member 
     * of the specified set is contained in this set using the default object equality.
     * If this collection is a list, returns <code>true</code> if and only 
     * if the specified object is also a list, both lists have the same size,
     * and all corresponding pairs of elements in
     * the two lists are <i>equal</i> using the default object equality.
     * If this collection is neither a list, nor a set, this method returns 
     * the default object equality (<code>this == obj</code>).
     *
     * @param obj the object to be compared for equality with this collection
     * @return <code>true</code> if both collection are considered equals;
     *        <code>false</code> otherwise. 
     */
    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (this instanceof Set) {
            if (!(obj instanceof Set)) return false;
            Set<E> that = (Set<E>) obj;
            if (this.size() != that.size()) return false;
            return (this.usingComparator((FastComparator<E>) FastComparator.DEFAULT).containsAll(that));
        } else if (this instanceof List) {
            final List<E> that = (List<E>) obj;
            if (this.size() != that.size()) return false;
            final boolean[] areEqual = new boolean[]{true};
            this.doWhile(new Predicate<E>() {
                Iterator<E> it = that.iterator();

                public Boolean evaluate(E param) {
                    if (it.hasNext() && ((param == null) ? it.next() == null : param.equals(it.next()))) {
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
     *      hashCode = 31*hashCode + (e == null) ? 0 : e.hashCode();
     * </pre>
     * If this collection is neither a list, nor a set the default object 
     * hashcode is returned.
     */
    @Override
    public int hashCode() {
        final int[] hash = new int[1];
        if (this instanceof Set) {
            this.doWhile(new Predicate<E>() {
                public Boolean evaluate(E param) {
                    hash[0] += ((param == null) ? 0 : param.hashCode());
                    return true;
                }
            });
            return hash[0];
        } else if (this instanceof List) {
            hash[0] = 1;
            this.doWhile(new Predicate<E>() {
                public Boolean evaluate(E param) {
                    hash[0] = 31 * hash[0] + ((param == null) ? 0 : param.hashCode());
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
    public static class PlainText extends TextFormat<FastCollection<Object>> {

        @Override
        public FastCollection<Object> parse(CharSequence csq, Cursor cursor) throws IllegalArgumentException {
            throw new UnsupportedOperationException("Parsing Of Generic FastCollection Not supported");
        }

        @Override
        public Appendable format(final FastCollection<Object> fc, final Appendable dest) throws IOException {
            dest.append('[');
            fc.doWhile(new Predicate<Object>() {
                boolean isFirst = true;

                @SuppressWarnings({ "rawtypes", "unchecked" })
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
            return dest.append(']');
        }
    }

    
    private static final long serialVersionUID = -492488199200216508L;
}
