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
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javolution.annotation.Format;
import javolution.annotation.StackSafe;
import javolution.internal.util.collection.SharedCollectionImpl;
import javolution.internal.util.collection.UnmodifiableCollectionImpl;
import javolution.lang.Copyable;
import javolution.lang.Functor;
import javolution.lang.Immutable;
import javolution.lang.Predicate;
import javolution.text.Cursor;
import javolution.text.TextContext;
import javolution.text.TextFormat;
import javolution.util.service.CollectionService;

/**
 * <p> A closure-ready collection.</p>
 * 
 * <p> Fast collections can be iterated over using closures. If the 
 *     collections is {@link #shared() shared}, the iteration is thread-safe
 *     (no concurrent modification exception possible). 
 *     [code]
 *     FastCollection<CharSequence> longNames 
 *             = names.findAll(new Predicate<CharSequence>() { 
 *         @Override
 *         public Boolean evaluate(CharSequence csq) {
 *             return csq.length() > 16; 
 *         }
 *     });[/code]
 *     </p>
 *     
 * <p> Due to constraint on access to non-final members, final arrays may be 
 *     required to hold primitives variables.
 *     [code]
 *     public void addOrUpdateToken(final Token token) {
 *         final boolean[] found = new boolean[0]; // Needs to be final.
 *         Text name = token.getName().getValue();
 *         tokens.doWhile(new Predicate<Token>() {
 *              public Boolean evaluate(Token token) {
 *                  if (token.getName().getValue().contentEquals(name)) {
 *                      token.setValue(token.getValue());
 *                      return found[0] = true;
 *                  }
 *              }
 *         });
 *         if (!found[0]) tokens.add(token);
 *     }
 *     [/code]
 *    The writing of the code above will be greatly simplified with the
 *    upcoming Java 1.8 (closure support).</p>
 * 
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0.0, December 12, 2012
 */
@StackSafe(initialization = false)
@Format(text = FastCollection.PlainText.class)
public abstract class FastCollection<E> implements 
       Collection<E>, Copyable<FastCollection<E>>, Serializable {

    /**
     * Default constructor.
     */
    protected FastCollection() {}

    /**
     * Returns the collection service backing up this collection.
     */
    protected abstract CollectionService<E> getService();


    /***************************************************************************
     * Collection views.
     */

    /**
     * <p> Returns an unmodifiable/{@link Immutable} view of this collection. 
     *     Attempts to modify the returned collection result in an 
     *     {@link UnsupportedOperationException} being thrown.</p> 
     */
    public FastCollection<E> unmodifiable() {
        return new FastCollection<E>() {
            CollectionService<E> service = new UnmodifiableCollectionImpl<E>(FastCollection.this.getService());

            @Override
            protected CollectionService<E> getService() {
                return service;
            }
            
            private static final long serialVersionUID = -2037354762526261781L;
        };
     
    }
    
    /**
     * <p> Returns a concurrent read-write view of this collection.</p>
     * <p> Iterators on {@link #shared} collections are deprecated as the may 
     *     raise {@link ConcurrentModificationException}.  {@link #doWhile 
     *     Closures} should be used to iterate over shared collections.
     *     Closure-based {@link #doWhile(Predicate)} iterations over shared 
     *     collections allow for concurrent read (read-write lock).</p> 
     */
    public FastCollection<E> shared() {
        return new FastCollection<E>() {
            CollectionService<E> service 
               = new SharedCollectionImpl<E>(FastCollection.this.getService(), new ReentrantReadWriteLock());

            @Override
            protected CollectionService<E> getService() {
                return service;
            }

            private static final long serialVersionUID = -2037354762526261781L;
        };
     
    }
    

    /***************************************************************************
     * Closure operations.
     */  

    /**
     * Iterates this collection elements until the specified predicate 
     * returns <code>false</code>.
     */
    public void doWhile(Predicate<E> predicate) {
        getService().doWhile(predicate);
    }

    /**
     * Removes from this collection all the elements matching the specified 
     * predicate.
     * 
     * @return <code>true</code> if this collection changed as a result of 
     *         the call; <code>false</code> otherwise.
     */
    public boolean removeAll(Predicate<E> predicate) {
         return getService().removeAll(predicate);
    }

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
     * Applies the specified functor to this collection elements; returns
     * all the results of these evaluations different from <code>null</code>.
     */
    public <R> FastCollection<R> forEach(final Functor<E, R> functor) {
        final FastTable<R> result = new FastTable<R>();
        doWhile(new Predicate<E>() {
            @Override
            public Boolean evaluate(E param) {
                R r = functor.evaluate(param);
                if (r != null)
                    result.add(r);
                return true;
            }
        });
        return result;
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

 
    /***************************************************************************
     * Collection operations.
     */

    /**
     * Returns the number of element in this collection. 
     */
    public int size() {
        return getService().size();
    }

    /**
     * Adds the specified element; although the implementation may append the 
     * element to the end of the collection it is not forced to do so 
     * (e.g. if the collection is ordered).
     * 
     * @param element the element to be added to this collection.
     * @return <code>true</code> (as per the general contract of the
     *         <code>Collection.add</code> method).
     * @throws UnsupportedOperationException if the collection is not modifiable.
     */
    public boolean add(E element) {
        return getService().add(element);
    }

    /**
     * Removes the first occurrence in this collection of the specified element.
     *
     * @param element the element to be removed from this collection.
     * @return <code>true</code> if this collection contained the specified
     *         element; <code>false</code> otherwise.
     * @throws UnsupportedOperationException if the collection is not modifiable.
     */
    @SuppressWarnings("unchecked")
    public boolean remove(Object element) {
        return getService().remove((E)element);
    }

    /**
     * Removes all of the elements from this collection (optional operation).
     *
     * @throws UnsupportedOperationException if not supported.
     */
    public void clear() {
        getService().clear();
    }

    /**
     * Indicates if this collection is empty.
     *
     * @return <code>true</code> if this collection contains no element;
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
    @SuppressWarnings("unchecked")
    public boolean contains(final Object element) {
        return getService().contains((E)element);
    }
    

    @Override
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
        final boolean[] modified = new boolean[] { false };
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

    @Override
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
        final boolean[] containsAll = new boolean[] { true };
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

    @Override
    public boolean removeAll(final Collection<?> that) {
        return removeAll(new Predicate<E>() {
            public Boolean evaluate(E param) {
                return that.contains(param);
            }
        });
    }

    @Override
    public boolean retainAll(final Collection<?> that) {
        return retainAll(new Predicate<E>() {
            public Boolean evaluate(E param) {
                return that.contains(param);
            }
        });
    }

    @Override
    public Object[] toArray() {
        return toArray(new Object[size()]);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(final T[] array) { // Support concurrent modifications if Shared.
        final T[][] result = (T[][]) new Object[1][];
        final int[] size = new int[1];
        doWhile(new Predicate<E>() { // Synchronized if Shared instance.
            int i;

            { // Instance initializer.
                size[0] = size();
                result[0] = (size[0] <= array.length) ? array
                        : (T[]) java.lang.reflect.Array.newInstance(array
                                .getClass().getComponentType(), size[0]);
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
     * Compares the specified object with this collection for equality.
     * If this collection is a set, returns <code>true</code> if the specified
     * object is also a set, the two sets have the same size and the specified 
     * set contains all the element of this set. If this collection is a list, 
     * returns <code>true</code> if and
     * only if the specified object is also a list, both lists have the same 
     * size, and all corresponding pairs of elements in
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
        if (this == obj)
            return true;
        if (this instanceof Set) {
            if (!(obj instanceof Set))
                return false;
            Set<E> that = (Set<E>) obj;
            if (this.size() != that.size())
                return false;
            return that.containsAll(this);
        } else if (this instanceof List) {
            final List<E> that = (List<E>) obj;
            if (this.size() != that.size())
                return false;
            final boolean[] areEqual = new boolean[] { true };
            this.doWhile(new Predicate<E>() {
                Iterator<E> it = that.iterator();

                @Override
                public Boolean evaluate(E param) {
                    if (it.hasNext()
                            && ((param == null) ? it.next() == null : param
                                    .equals(it.next()))) { return true; }
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
                    hash[0] = 31 * hash[0]
                            + ((param == null) ? 0 : param.hashCode());
                    return true;
                }
            });
            return hash[0];
        } else {
            return super.hashCode();
        }
    }

    /** 
     * Returns an iterator over this collection. For shared collection 
     * closure (e.g. {@link #doWhile(Predicate)}) should be used instead o
     * iterators (or Java 1.5 simplified loop)
     */
    public Iterator<E> iterator() {
        return getService().iterator();
    }      
    
    /***************************************************************************
     * Misc.
     */

    @Override
    public FastCollection<E> copy() {
        final FastTable<E> table = new FastTable<E>();
        this.doWhile(new Predicate<E>() {
            @SuppressWarnings("unchecked")
            public Boolean evaluate(E param) {
                table.add((param instanceof Copyable) ? ((Copyable<E>)param).copy() : param);
                return false;
            }
        });
        return table;
    }


    @Override
    public String toString() {
        return TextContext.getFormat(FastCollection.class).format(this);
    }

    /**
     * Holds the default text format for fast collections (parsing not supported).
     */
    public static class PlainText extends TextFormat<FastCollection<Object>> {

        @Override
        public FastCollection<Object> parse(CharSequence csq, Cursor cursor)
                throws IllegalArgumentException {
            throw new UnsupportedOperationException(
                    "Parsing of generic FastCollection not supported");
        }

        @Override
        public Appendable format(final FastCollection<Object> fc,
                final Appendable dest) throws IOException {
            dest.append('[');
            fc.doWhile(new Predicate<Object>() {
                boolean isFirst = true;

                @Override
                public Boolean evaluate(Object param) {
                    try {
                        if (!isFirst) {
                            dest.append(", ");
                        } else {
                            isFirst = false;
                        }
                        if (param != null) {
                            javolution.text.TextFormat<Object> tf = TextContext
                                    .getFormat(param.getClass());
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

    private static final long serialVersionUID = 586810942305431721L;
}