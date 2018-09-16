/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.util;

import static org.javolution.annotations.Realtime.Limit.CONSTANT;
import static org.javolution.annotations.Realtime.Limit.LINEAR;
import static org.javolution.annotations.Realtime.Limit.N_SQUARE;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;

import org.javolution.annotations.Parallel;
import org.javolution.annotations.Realtime;
import org.javolution.context.ConcurrentContext;
import org.javolution.text.Cursor;
import org.javolution.text.DefaultTextFormat;
import org.javolution.text.TextContext;
import org.javolution.text.TextFormat;
import org.javolution.util.function.BinaryOperator;
import org.javolution.util.function.Consumer;
import org.javolution.util.function.Equality;
import org.javolution.util.function.Function;
import org.javolution.util.function.Predicate;
import org.javolution.util.internal.collection.AtomicCollectionImpl;
import org.javolution.util.internal.collection.ConcatCollectionImpl;
import org.javolution.util.internal.collection.CustomEqualityCollectionImpl;
import org.javolution.util.internal.collection.DistinctCollectionImpl;
import org.javolution.util.internal.collection.FilteredCollectionImpl;
import org.javolution.util.internal.collection.LinkedCollectionImpl;
import org.javolution.util.internal.collection.MappedCollectionImpl;
import org.javolution.util.internal.collection.ParallelCollectionImpl;
import org.javolution.util.internal.collection.ReversedCollectionImpl;
import org.javolution.util.internal.collection.SharedCollectionImpl;
import org.javolution.util.internal.collection.SortedCollectionImpl;
import org.javolution.util.internal.collection.UnmodifiableCollectionImpl;

/**
 * High-performance collection with {@link Realtime strict timing constraints}.
 * 
 * This class implements most of the {@link java.util.stream.Stream} functions and can be used directly 
 * to perform sequential or parallel aggregate operations.
 * 
 * Instance of this class may use custom element comparators instead of the default object equality 
 * when comparing elements. This affects the behavior of the contains, remove, containsAll, equals, and 
 * hashCode methods. The {@link java.util.Collection} contract is guaranteed to hold only for collections
 * using {@link Equality#STANDARD} for {@link #equality() elements comparisons}.
 * 
 * @param <E> the type of collection element ({@code null} values allowed)
 * 
 * @author <jean-marie@dautelle.com>
 * @version 7.0, March 31st, 2017
 */
@Realtime
@DefaultTextFormat(AbstractCollection.Format.class)
public abstract class AbstractCollection<E> implements Collection<E>, Serializable, Cloneable {

    private static final long serialVersionUID = 0x700L; // Version.
        
    /** Returns this collection with the specified elements added (convenience method). */
    public AbstractCollection<E> with(@SuppressWarnings("unchecked") E... elements) {
        addAll(elements);
        return this;
    }

    // //////////////////////////////////////////////////////////////////////////
    // Views.
    //

    /**
     * Returns a view whose elements are the elements of this collection followed by the elements of the 
     * specified collection. New elements are added to this collection; others collection operations are 
     * typically performed on both views (e.g. {@link #removeIf(Predicate)}).  
     */
    public AbstractCollection<E> concat(AbstractCollection<? extends E> that) {
        return new ConcatCollectionImpl<E>(this, that);
    }

    /**
     * Returns an atomic view over this collection. All operations that write or access multiple elements 
     * in the collection (such as {@code addAll(), retainAll()}) are atomic. All read operations are mutex-free.
     */
    public AbstractCollection<E> atomic() {
        return new AtomicCollectionImpl<E>(this);
    }

    /**
     * Returns a view allowing {@link Parallel parallel} operations to be performed {@link ConcurrentContext 
     * concurrently}. 
     */
    public AbstractCollection<E> parallel() {
        return new ParallelCollectionImpl<E>(this);
    }

    /**
     * Returns a view exposing elements in reversed iterative order.
     */
    public AbstractCollection<E> reversed() {
        return new ReversedCollectionImpl<E>(this);
    }

    /**
     * Returns a thread-safe view over this collection. The shared view allows for concurrent read as long as 
     * there is no writer. The default implementation is based on <a href=
     * "http://en.wikipedia.org/wiki/Readers%E2%80%93writer_lock">readers-writers locks</a> giving priority to writers.
     */
    public AbstractCollection<E> shared() {
        return new SharedCollectionImpl<E>(this);
    }

    /**
     * Returns an unmodifiable view over this collection. Any attempt to modify the collection through this view will
     * result into a {@link java.lang.UnsupportedOperationException} being raised.
     */
    public AbstractCollection<E> unmodifiable() {
        return new UnmodifiableCollectionImpl<E>(this);
    }

    /**
     * Returns a view exposing only the elements matching the specified filter. Adding elements not matching the 
     * specified filter has no effect. If this collection is initially empty, using a filtered view
     * ensures that this collection has only elements satisfying the specified filter predicate.
     */
    public AbstractCollection<E> filter(Predicate<? super E> filter) {
        return new FilteredCollectionImpl<E>(this, filter);
    }

    /**
     * Returns a view exposing elements through the specified mapping function.
     * The returned view does not allow new elements to be added.
     */
    public <R> AbstractCollection<R> map(Function<? super E, ? extends R> function) {
        return new MappedCollectionImpl<E, R>(this, function);
    }

    /**
     * Returns a view disallowing {@link Parallel parallel} operations to be performed
     * {@link ConcurrentContext concurrently}. This method returns {@code this} since 
     * collections are sequential by default. This method is overridden by parallel views.
     */
    public AbstractCollection<E> sequential() {
        return this;
    }

    /**
     * Returns an ordered view exposing its elements sorted according to the specified comparator.
     */
    public AbstractCollection<E> sorted(Comparator<? super E> comparator) {
        return new SortedCollectionImpl<E>(this, comparator);
    }

    /**
     * Returns on ordered view exposing its element sorted according to their natural ordering (convenience 
     * method). The elements should implement the {@link Comparable} interface.
     *  
     * @return {@code sorted((x,y) -> ((Comparable<E>)x).compareTo(y))} 
     */
    public AbstractCollection<E> sorted() {
        return sorted(new Comparator<E>() {

            @SuppressWarnings("unchecked")
            @Override
            public int compare(E left, E right) {
                return ((Comparable<E>) left).compareTo(right);
            }});
    }

    /**
     * Returns a view exposing only distinct elements as identified by this collection equality. 
     * Adding elements already present has no effect. If this collection is initially empty, 
     * using a distinct view to add new elements ensures that this collection has no duplicate element.
     */
    public AbstractCollection<E> distinct() {
        return new DistinctCollectionImpl<E>(this);
    }

    /**
     * Returns an ordered view keeping track of the insertion order and exposing elements in that order 
     * (first added, first to iterate). This view can be useful for compatibility with Java linked collections
     * (e.g. {@code LinkedHashSet}). Any element not added through this view is ignored while iterating.
     */
    public AbstractCollection<E> linked() {
        return new LinkedCollectionImpl<E>(this);
    }

    /**
     * Returns a view using the specified equality comparator for element equality comparisons.
     * 
     * @param equality the equality to use for element comparisons.
     * @return a view using the specified custom equality.
     * @see #equality()
     */
    public AbstractCollection<E> equality(Equality<? super E> equality) {
        return new CustomEqualityCollectionImpl<E>(this, equality);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Iterative methods.
    //

    /**
     * Iterates over all this collection elements applying the specified consumer.
     * For non-parallel collections, the iterative order is the same as for {@link #iterator}.
     * 
     * @param consumer the functional consumer applied to the collection elements.
     */
    @Parallel
    @Realtime(limit = LINEAR)
    public void forEach(final Consumer<? super E> consumer) {
        iterator().hasNext(new Predicate<E>() {
            @Override
            public boolean test(E param) {
                consumer.accept(param);
                return false;
            }
        });
    }

    /**
     * Performs a reduction by applying the specified operator over the elements of this collection.
     * 
     * @param operator the binary operator applied to the collection elements.
     * @return the result of the reduction or {@code null} if the collection is empty.
     */
    @Parallel
    @Realtime(limit = LINEAR)
    public E reduce(BinaryOperator<E> operator) {
        Reduction<E> reduction = new Reduction<E>(operator);
        iterator().hasNext(reduction);
        return reduction.accumulator;
    }

    /**
     * Removes from this collection all the elements matching the specified functional predicate.
     * The removal order is implementation dependent (usually what goes the fastest).
     * 
     * @param filter a predicate returning {@code true} for elements to be removed.
     * @return {@code true} if at least one element has been removed; {@code false} otherwise.
     */
    @Parallel
    @Realtime(limit = LINEAR)
    public abstract boolean removeIf(Predicate<? super E> filter);

    ////////////////////////////////////////////////////////////////////////////
    // Common Reductions (parallel).
    //

    /**
     * Returns any element from this collection or {@code null} if this collection is empty.
     */
    @Parallel
    @Realtime(limit = LINEAR)
    public E findAny() {
        FastIterator<E> itr = iterator();
        return itr.hasNext() ? itr.next() : null;
    }

    /** 
     * Returns whether any elements of this collection match the provided predicate.
     */
    @Parallel
    @Realtime(limit = LINEAR)
    public boolean anyMatch(Predicate<? super E> predicate) {
        return iterator().hasNext(predicate);
    }
    
    /** 
     * Returns whether all elements of this collection match the provided predicate (convenience method).
     * 
     * @return {@code !anyMatch(!predicate)}
     */
    @Parallel
    public final boolean allMatch(final Predicate<? super E> predicate) {
        Predicate<? super E> reversedPredicate = new Predicate<E>() { 

            @Override
            public boolean test(E param) {
                return !predicate.test(param);
            }};
         return !anyMatch(reversedPredicate);
    }
    
    /** 
     * Returns whether no elements of this stream match the provided predicate (convenience method).
     * 
     * @return {@code !anyMatch(predicate)}
     */
    @Parallel
    public final boolean noneMatch(Predicate<? super E> predicate) {
        return !anyMatch(predicate);
    }
    
    /** 
     * Returns the smallest element of this collection according to the specified comparator (convenience method). 
     * @return {@code reduce((e1, e2) -> comparator.compare(e1, e2) <= 0 ? e1 : e2)}
     */
    @Parallel
    public final E min(final Comparator<? super E> comparator) {
        return reduce(new BinaryOperator<E>() {
            @Override
            public E apply(E first, E second) {
                return comparator.compare(first, second) <= 0 ? first : second;
            }});
    }

    /** 
     * Returns the largest element of this collection according to the specified comparator (convenience method).
     * 
     * @return {@code reduce((e1, e2) -> comparator.compare(e1, e2) >= 0 ? e1 : e2)}
     */
    @Parallel
    public final E max(final Comparator<? super E> comparator) {
        return reduce(new BinaryOperator<E>() {
            @Override
            public E apply(E first, E second) {
                return comparator.compare(first, second) >= 0 ? first : second;
            }});
    }

   /**
     * Returns the elements of this collection through reduction. 
     */
    @Parallel
    @Realtime(limit = LINEAR)
    public AbstractCollection<E> collect() {
        final FastTable<E> collection = new FastTable<E>();
        iterator().hasNext(new Predicate<E>() {
            @Override
            public boolean test(E param) {
                collection.add(param);
                return false;
            }
        });
        return collection;
    }

    // //////////////////////////////////////////////////////////////////////////
    // Collection operations.
    //

    /** 
     * Returns a closure-enabled / read-only ascending iterator overs this collection elements. 
     * 
     * Iterations on {@link #shared} views are thread-safe, they are performed on a {@link #clone snapshot} of the
     * underlying collection taken when this method is called. 
     */
    @Realtime(limit = LINEAR, comment = "FastCollection.clone() method may be called (e.g. shared views)")
    @Override
    public abstract FastIterator<E> iterator();

    /** 
     * Returns a closure-enabled / read-only descending iterator overs this collection elements. 
     * 
     * Iterations on {@link #shared} views are thread-safe, they are performed on a {@link #clone snapshot} of the
     * underlying collection taken when this method is called. 
     */
    @Realtime(limit = LINEAR, comment = "FastCollection.clone() method may be called (e.g. shared views)")
    public abstract FastIterator<E> descendingIterator();

    /** Adds the specified element to this collection. */
    @Override
    @Realtime(limit = CONSTANT)
    public abstract boolean add(E element);

    /** Indicates if this collection is empty.*/
    @Parallel
    @Override
    @Realtime(limit = LINEAR, comment = "Could iterate the whole collection (e.g. filtered views).")
    public abstract boolean isEmpty();

    /** Returns the size of this collection.*/
    @Parallel
    @Override
    @Realtime(limit = LINEAR, comment = "Could count the elements (e.g. filtered views).")
    public abstract int size();

    /** Removes all elements from this collection.*/
    @Parallel
    @Override
    @Realtime(limit = LINEAR, comment = "Could remove the elements one at a time.")
    public abstract void clear();

    /** Indicates if this collection contains the specified element testing for element equality using this 
     *  collection {@link #equality}. */
    @Parallel
    @Override
    @Realtime(limit = LINEAR, comment = "Could search the whole collection.")
    public boolean contains(final Object searched) {
        final AtomicBoolean found = new AtomicBoolean();
        forEach(new Consumer<E>() { // Parallel
            final Equality<? super E> equality = equality();
            
            @SuppressWarnings("unchecked")
            @Override
            public void accept(E param) {
                if (!found.get() && equality.areEqual((E) searched, param)) found.set(true);
            }});
        return found.get();
    }

    /** Removes a single instance of the specified element from this collection testing for element equality 
     *  using this collection {@link #equality}. */
    @Parallel
    @Override
    @Realtime(limit = LINEAR, comment = "Could search the whole collection.")
    public boolean remove(final Object searched) {
        return removeIf(new Predicate<E>() { // Parallel.
            final Equality<? super E> equality = equality();
            AtomicBoolean removed = new AtomicBoolean();
            @Override
            @SuppressWarnings("unchecked")
            public boolean test(E param) {
                return !removed.get() && equality.areEqual((E) searched, param) && removed.compareAndSet(false,  true); 
            }
        });
    }

    /** Adds all the elements of the specified collection to this collection.*/
    @Override
    @Realtime(limit = LINEAR)
    public boolean addAll(Collection<? extends E> that) {
        boolean changed = false;
        for (E e : that)
            if (add(e)) changed = true;
        return changed;
    }

    /** Adds all the elements specified  to this collection (convenience method).*/
    @Realtime(limit = LINEAR)
    public boolean addAll(@SuppressWarnings("unchecked") E... elements) {
        boolean changed = false;
        for (E e : elements)
            if (add(e)) changed = true;
        return changed;
    }

    /** Indicates if this collection contains all the specified elements testing for element equality 
     *  using this collection {@link #equality}. */
    @Parallel
    @Override
    @Realtime(limit = N_SQUARE, comment="LINEAR if the specified collection is a SparseSet")
    public boolean containsAll(Collection<?> that) {
        for (Object obj : that)
            if (!contains(obj)) return false;  // Parallel
        return true;
    }

    /** Removes all the specified elements from this collection testing for element equality 
     *  using this collection {@link #equality}.*/
    @Parallel
    @Override
    @Realtime(limit = N_SQUARE, comment="LINEAR if the specified collection is a SparseSet")
    public boolean removeAll(final Collection<?> that) {
        return removeIf(new Predicate<E>() { // Parallel.
            @Override
            public boolean test(E param) {
                return that.contains(param);
            }
        });
    }

    /** Removes all the elements except those in the specified collection testing for element equality
     *  using this collection {@link #equality}. */
    @Parallel
    @Override
    @Realtime(limit = N_SQUARE, comment="LINEAR if the specified collection is a SparseSet")
    public boolean retainAll(final Collection<?> that) {
        return removeIf(new Predicate<E>() { // Parallel.
            @Override
            public boolean test(E param) {
                return !that.contains(param);
            }
        });
    }

    /** Returns an array holding this collection elements. */
    @Override
    @Realtime(limit = LINEAR)
    public Object[] toArray() {
        return toArray(EMPTY_ARRAY);
    }

    private final static Object[] EMPTY_ARRAY = new Object[0];

    /** Returns the specified array holding this collection elements if enough capacity. */
    @SuppressWarnings("unchecked")
    @Override
    @Realtime(limit = LINEAR)
    public <T> T[] toArray(final T[] array) {
        final int size = size();
        final T[] result = (size <= array.length) ? array
                : (T[]) java.lang.reflect.Array.newInstance(array.getClass().getComponentType(), size);
        int i = 0;
        Iterator<E> it = iterator();
        while (it.hasNext())
            result[i++] = (T) it.next();
        if (result.length > size) result[size] = null; // As per Collection contract.
        return result;
    }

    // //////////////////////////////////////////////////////////////////////////
    // Object operations.
    //

    /**
     * Compares the specified object with this collection for equality.
     * 
     * Although this collection implements the {@link Collection} interface, it obeys the collection 
     * general contract only for collections using the same element {@link #equality() equality}.
     *  
     * ```java
     * FastTable<String> names1 = new FastTable<String>(LEXICAL_CASE_INSENSITIVE).with("OSCAR THON", "SIM CAMILLE");
     * FastTable<String> names2 = new FastTable<String>(LEXICAL_CASE_INSENSITIVE).with("Oscar Thon", "Sim Camille");
     * assert names1.equals(names2);
     * ``` 
     * This method should be overridden by {@link java.util.List} and {@link java.util.Set} instances. 
     * 
     * @param obj the object to be compared for equality with this collection
     * @return {@code super.equals(obj)}
     */
    @Override
    @Realtime(limit = LINEAR)
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    /** 
     * Returns the hash code of this collection.
     * 
     * This method should be overridden by {@link java.util.List} and {@link java.util.Set} instances. 
     *  
     * @return {@code super.hashCode()}
     */
    @Override
    @Realtime(limit = LINEAR)
    public int hashCode() {
        return super.hashCode(); 
    }

    /** Returns the string representation of this collection using its default {@link TextFormat format}. */
    @Override
    @Realtime(limit = LINEAR)
    public String toString() {
        return TextContext.getFormat(AbstractCollection.class).format(this);
    }

    // //////////////////////////////////////////////////////////////////////////
    // Streams operations.
    //
    
     // //////////////////////////////////////////////////////////////////////////
    // Misc.
    //

    /** Returns the element equality used for element comparison. */
    public abstract Equality<? super E> equality();

    /**
     * Returns sub-views over this collection to support {@link #parallel} processing. 
     * How this collection splits (or does not split) is collection dependent (for example {@link #distinct}
     * views do not split). There is no guarantee over the iterative order of the sub-views which may 
     * be different from this collection iterative order.
     * 
     * Any attempt to modify this collection through its sub-views will result in a 
     * {@link UnsupportedOperationException} being thrown.
     * 
     * @param n the desired number of independent views.
     * @return the unmodifiable sub-views (array of length in range [1..n])
     * @throws IllegalArgumentException if {@code n <= 0} 
     */
    public abstract AbstractCollection<E>[] trySplit(int n);

    /** Returns a copy of this collection; updates of the copy should not impact the original. */
    @Realtime(limit = LINEAR)
    @SuppressWarnings("unchecked")
    public AbstractCollection<E> clone() {
        try {
            return (AbstractCollection<E>) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError("Should not happen since this class is Cloneable !");
        }
    }

    /**
     * Default text format for fast collections (parsing not supported).
     */
    public static class Format extends TextFormat<AbstractCollection<?>> {

        @Override
        public AbstractCollection<Object> parse(CharSequence csq, Cursor cursor) throws IllegalArgumentException {
            throw new UnsupportedOperationException();
        }

        @Override
        public Appendable format(AbstractCollection<?> that, final Appendable dest) throws IOException {
            Iterator<?> i = that.iterator();
            dest.append('[');
            while (i.hasNext()) {
                TextContext.format(i.next(), dest);
                if (i.hasNext()) {
                    dest.append(',').append(' ');
                }
            }
            return dest.append(']');
        }
    }

    /** Reduction predicate. */
    private static final class Reduction<E> implements Predicate<E> {
        final BinaryOperator<E> operator;
        E accumulator;

        private Reduction(BinaryOperator<E> operator) {
            this.operator = operator;
        }

        @Override
        public boolean test(E param) {
            accumulator = (accumulator != null) ? operator.apply(accumulator, param) : param;
            return false;
        }
    }
}