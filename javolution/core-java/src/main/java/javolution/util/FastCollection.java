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
import java.util.List;
import java.util.Set;
import javolution.annotation.Format;
import javolution.annotation.StackSafe;
import javolution.lang.Copyable;
import javolution.lang.Functor;
import javolution.lang.Immutable;
import javolution.text.Cursor;
import javolution.text.TextContext;

/**
 * <p> This abstract class represents {@link javolution.annotation.StackSafe 
 *     stack-safe}, time-deterministics and closure-ready collections.</p>
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
 *             return addIfAbsent(); // Avoids duplicate.
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
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 5.4.5, March 23, 2010
 */
@StackSafe
@Format(text = FastCollection.TextFormat.class)
public abstract class FastCollection<E> implements Collection<E>, Copyable<FastCollection<E>> {

    /**
     * Default constructor.  
     */
    protected FastCollection() {
    }

    /**
     * Returns an unmodifiable/{@link Immutable} view of this collection. 
     * Attempts to modify the returned collection result in an 
     * {@link UnsupportedOperationException} being thrown. 
     * 
     * @return the unmodifiable view over this collection.
     */
    public Unmodifiable unmodifiable() {
        return new Unmodifiable();
    }

    /**
     * <p> Returns a thread-safe read-write view of this collection.</p>
     * <p> The default implementation performs synchronization on read/write.
     *     Sub-classes may provide more efficient implementations.</p>
     * <p> Having a shared collection does not mean that modifications made
     *     by one thread are automatically viewed by others thread.
     *     For this to happen the thread must obtain a shared lock, then 
     *     all elements cached in its CPU registers or CPU cache are invalidated
     *     and refreshed from main memory. When the thread releases the 
     *     shared lock, all elements cached in its CPU registers or CPU cache are
     *     flushed (written) to main memory. In a well-designed system, 
     *     synchronization points should only occur when required.</p>
     * <p> Iterators on {@link #shared} collections are deprecated as the may 
     *     raise {@link ConcurrentModificationException}.  {@link #forEach 
     *     Closures} should be used to iterate over shared collections
     *    (Note: All fast collection methods use closures to iterate).</p> 
     */
    public Shared shared() {
        return new Shared();
    }

    /**
     * Returns the element comparator for this collection (default 
     * {@link FastComparator#DEFAULT}). If this method is overriden,
     * it is possible that elements considered distinct using the 
     * default equality comparator, would appear to be equals as far 
     * as this collection is concerned. For example, a lexical comparator 
     * will consider that two {@link CharSequence} are equals if they hold 
     * the same characters regardless of the implementation.
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
        return (FastComparator<E>) FastComparator.getDefault();
    }

    /**
     * Indicates if this collecion is ordered (default <code>false</code>). 
     * Sub-classes for which this method is overloaded to return <code>true</code>
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
    public abstract void doWhile(final Functor<E, Boolean> predicate);

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
    public boolean retainAll(final Functor<E, Boolean> predicate) {
        return removeAll(new Functor<E, Boolean>() {
            public Boolean evaluate(E param) {
                return !predicate.evaluate(param);
            }
        });
    }

    /**
     * Returns all the elements different from <code>null</code> matching 
     * the specified predicate.
     */
    public FastCollection<E> findAll(final Functor<E, Boolean> predicate) {
        return forEach(new Functor<E, E>() {
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
        doWhile(new Functor<E, Boolean>() {
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
        this.doWhile(new Functor<E, Boolean>() {
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
        final boolean[] found = new boolean[1];
        return removeAll(new Functor<E, Boolean>() {
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
        removeAll(new Functor<E, Boolean>() {
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
        final boolean[] found = new boolean[1];
        this.doWhile(new Functor<E, Boolean>() {
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
     * this collection, in the order that they are returned by {@link #forEach} 
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
        final boolean[] modified = new boolean[1];
        that.doWhile(new Functor<E, Boolean>() {
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
        that.doWhile(new Functor<E, Boolean>() {
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
        return this.removeAll(new Functor<E, Boolean>() {
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
        return this.retainAll(new Functor<E, Boolean>() {
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
        doWhile(new Functor<E, Boolean>() { // Synchronized if Shared instance.
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
     * @return <code>toText().toString()</code>
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
     * is contained in this set using the common comparator.
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
        FastComparator<E> thatComparator =  (obj instanceof FastCollection) ? ((FastCollection)obj).comparator() : FastComparator.DEFAULT;
        if (!this.comparator().equals(thatComparator)) return false;
        if (this instanceof Set) {
            if (!(obj instanceof Set)) return false;
            Set that = (Set) obj;
            if (this.size() != that.size()) return false;
            return (this.containsAll(that));
        } else if (this instanceof List) {
            final List that = (List) obj;
            if (this.size() != that.size()) return false;
            final boolean[] distinct = new boolean[1];
            final FastComparator comp = this.comparator();
            this.doWhile(new Functor<E, Boolean>() {
                Iterator<E> it = that.iterator();
                public Boolean evaluate(E param) {
                    if (!it.hasNext() || comp.equals(param, it.next())) {
                        distinct[1] = true;
                        return false;
                    } 
                    return true;
                }
            });
            return hash[0];
        } else {
            return super.hashCode();
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
     *      hashCode = 31*hashCode + (e==null ? 0 : e.hashCode());
     * </pre>
     * If this collection is neither a list, nor a set the default object 
     * hashcode is returned.
     */
    @Override
    public int hashCode() {
        final int[] hash = new int[1];
        if (this instanceof Set) {
            this.doWhile(new Functor<E, Boolean>() {
                public Boolean evaluate(E param) {
                    hash[0] += (param != null) ? param.hashCode() : 0;
                    return true;
                }
            });
            return hash[0];
        } else if (this instanceof List) {
            hash[0] = 1;
            this.doWhile(new Functor<E, Boolean>() {
                public Boolean evaluate(E param) {
                    hash[0] = 31 * hash[0] + ((param != null) ? param.hashCode() : 0);
                    return true;
                }
            });
            return hash[0];
        } else {
            return super.hashCode();
        }
    }

    /**
     * This inner class represents an unmodifiable view over the collection.
     */
    protected class Unmodifiable extends FastCollection<E> implements Immutable {

        @Override
        public FastComparator getComparator() {
            return FastCollection.this.getComparator();
        }

        @Override
        public boolean isOrdered() {
            return FastCollection.this.isOrdered();
        }

        @Override
        public <R> FastCollection<R> forEach(Functor<E, R> functor) {
            return FastCollection.this.forEach(functor);
        }

        @Override
        public void doWhile(Functor<E, Boolean> predicate) {
            FastCollection.this.doWhile(predicate);
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

        public FastCollection<E> copy() {
            return FastCollection.this.copy();
        }
    }

    /**
     * This inner class represents a thread safe view (read-write) over the
     * collection.
     */
    protected class Shared extends FastCollection<E> {

        int modifications; // Count modifications.

        @Override
        public synchronized FastComparator getComparator() {
            return FastCollection.this.getComparator();
        }

        @Override
        public boolean isOrdered() {
            return FastCollection.this.isOrdered();
        }

        @Override
        public synchronized <R> FastCollection<R> forEach(Functor<E, R> functor) {
            return FastCollection.this.forEach(functor);
        }

        @Override
        public void doWhile(Functor<E, Boolean> predicate) {
            FastCollection.this.doWhile(predicate);
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

        public synchronized FastCollection<E> copy() {
            return FastCollection.this.copy();
        }

        private synchronized void writeObject(ObjectOutputStream s) throws IOException {
            s.defaultWriteObject();
        }
    }

    /**
     * Holds the default text format for fast collections (parsing not supported).
     */
    public static class TextFormat extends javolution.text.TextFormat<FastCollection> {

        @Override
        public FastCollection parse(CharSequence csq, Cursor cursor) throws IllegalArgumentException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Appendable format(FastCollection fc, final Appendable dest) throws IOException {
            dest.append('{');
            fc.doWhile(new Functor<Object, Boolean>() {
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
