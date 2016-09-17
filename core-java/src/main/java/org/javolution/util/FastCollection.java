/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.util;

import static org.javolution.lang.Realtime.Limit.CONSTANT;
import static org.javolution.lang.Realtime.Limit.LINEAR;
import static org.javolution.lang.Realtime.Limit.N_SQUARE;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.javolution.context.ConcurrentContext;
import org.javolution.lang.Parallel;
import org.javolution.lang.Realtime;
import org.javolution.text.Cursor;
import org.javolution.text.DefaultTextFormat;
import org.javolution.text.TextContext;
import org.javolution.text.TextFormat;
import org.javolution.util.function.BinaryOperator;
import org.javolution.util.function.Consumer;
import org.javolution.util.function.Equality;
import org.javolution.util.function.Function;
import org.javolution.util.function.Order;
import org.javolution.util.function.Predicate;
import org.javolution.util.internal.collection.AtomicCollectionImpl;
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
 * <p> A high-performance collection with {@link Realtime strict timing constraints}.</p>
 * 
 * <p> Instances of this class support numerous views which can be chained:
 * <ul>
 * <li>{@link #parallel} - View allowing parallel processing of {@link Parallel} operations.</li>
 * <li>{@link #sequential} - View disallowing parallel processing of {@link Parallel} operations.</li>
 * <li>{@link #unmodifiable} - View which does not allow for modifications.</li>
 * <li>{@link #shared} - Thread-safe view based on <a href= "http://en.wikipedia.org/wiki/Readers%E2%80%93writer_lock">
 *                       readers-writer locks</a>.</li>
 * <li>{@link #atomic} - Thread-safe view for which all reads are mutex-free and collection updates 
 *                       (e.g. {@link #addAll addAll}, {@link #removeIf removeIf}, ...) are atomic.</li>
 * <li>{@link #filter filter(Predicate)} - View exposing only the elements matching the specified filter and 
 *                                         preventing elements not matching the specified filter to be added.</li>
 * <li>{@link #map map(Function)} - View exposing elements through the specified mapping function.</li>
 * <li>{@link #sorted(Comparator)} - View exposing elements sorted according to the specified comparator.</li>
 * <li>{@link #reversed} - View exposing elements in the reverse iterative order.</li>
 * <li>{@link #distinct} - View exposing each element only once.</li>
 * <li>{@link #linked} - View exposing each element based on its {@link #add insertion} order.</li>
 * <li>{@link #equality(Equality)} - View using the specified comparator to test for element equality 
 *                                   (e.g. {@link #contains}, {@link #remove}, {@link #distinct}, ...)</li>
 * </ul></p>
 * 
 * <p> In general, the chaining order does matter !
 * <pre>{@code 
 * FastCollection<String> names ...;
 *      
 * names.filter(s -> s.startsWith("X")).parallel().clear(); // Parallel removal of names starting with "X"
 * names.parallel().filter(s -> s.startsWith("X")).clear(); // Sequential removal of names starting with "X"
 * 
 * FastCollection<String> atomic = names.sorted().atomic(); 
 * FastCollection<String> nonAtomic = names.atomic().sorted();
 * 
 * FastCollection<String> threadSafe = names.linked().shared(); // 
 * FastCollection<String> threadUnsafe = names.shared().linked(); 
 *  
 * }</pre></p>
 * 
 * <p> It should be noted that {@link #unmodifiable Unmodifiable} views <b>are not immutable</b>; 
 *     constant/immutable collections (or maps) can only be obtained through class specializations (e.g. 
 *     {@link ConstantTable}, {@link ConstantSet}, {@link ConstantMap}, ...)
 * <pre>{@code
 * 
 * // Constant collections from literal elements.
 * ConstantSet<String> winners = ConstantSet.of("John Deuff", "Otto Graf", "Sim Kamil");
 * 
 * // Constant collections from existing collections.
 * ConstantSet<String> caseInsensitiveWinners = ConstantSet.of(Order.LEXICAL_CASE_INSENSITIVE, winners);
 *         
 * }</pre></p>
 * 
 * <p> Views are similar to <a href="http://lambdadoc.net/api/java/util/stream/package-summary.html">
 *     Java 8 streams</a> except that views are themselves collections and actions on the view will impact 
 *     the original collection. Collection views are nothing "new" since they already existed in the original 
 *     java.util collection classes (e.g. List.subList(...), Map.keySet(), Map.values()). Javolution extends to 
 *     this concept and allows views to be chained in order to address the issue of class proliferation.
 * <pre>{@code
 * FastTable<String> names = FastTable.newTable();
 * names.addAll("Sim Ilicuir", "Pat Ibulair");
 * names.subTable(0, n).clear(); // Removes the n first names (see java.util.List.subList).
 * names.distinct().add("Guy Liguili"); // Adds "Guy Liguili" only if not already present.
 * names.filter(s -> s.length > 16).clear(); // Removes all the persons with long names.
 * names.sorted().reversed().forEach(str -> System.out.println(str)); // Prints names in reverse alphabetical order.
 * tasks.parallel().forEach(task -> task.run()); // Execute concurrently the tasks and wait for their completion.
 * ...
 * }</pre></p>
 * 
 * <p> Views can of course be used to perform "stream" oriented filter-map-reduce operations with the same benefits:
 *     Parallelism support, excellent memory characteristics (no caching, cost nothing to create), etc.
 * <pre>{@code 
 * String anyFound = names.filter(s -> s.length > 16).any(); // Sequential search (returns the first found).
 * String anyFound = names.filter(s -> s.length > 16).parallel().any(); // Parallel search.
 * FastCollection<String> allFound = names.filter(s -> s.length > 16).collect(); // Sequential reduction.
 * FastCollection<String> allFound = names.filter(s -> s.length > 16).parallel().collect(); // Parallel reduction.
 * 
 * int maxLength = names.map(s -> s.length).parallel().max(); // Finds the maximum length in parallel.
 * int sumLength = names.map(s -> s.length).parallel().reduce((x,y)-> x + y); // Calculates the sum in parallel.
 * 
 * // JDK Class.getEnclosingMethod using Javolution's views and Java 8.
 * Method matching = ConstantTable.of(enclosingInfo.getEnclosingClass().getDeclaredMethods())
 *     .filter(m -> Objects.equals(m.getName(), enclosingInfo.getName())
 *     .filter(m -> Arrays.equals(m.getParameterTypes(), parameterClasses))
 *     .filter(m -> Objects.equals(m.getReturnType(), returnType)).any(); 
 * if (matching == null) throw new InternalError("Enclosing method not found");
 * return matching;
 * }</pre></p>
 * 
 * @param <E> the type of collection element (can be {@code null})
 * 
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 7.0, September 13, 2015
 */
@Realtime
@DefaultTextFormat(FastCollection.Format.class)
public abstract class FastCollection<E> implements Collection<E>, Serializable, Cloneable {
    private static final long serialVersionUID = 0x610L; // Version.

    /**
     * Default constructor.
     */
    protected FastCollection() {
    }

    // //////////////////////////////////////////////////////////////////////////
    // Views.
    //

    /**
     * Returns an atomic view over this collection. All operations that write or access multiple elements 
     * in the collection (such as {@code addAll(), retainAll()}) are atomic. All read operations are mutex-free.
     */
    public FastCollection<E> atomic() {
        return new AtomicCollectionImpl<E>(this);
    }

    /**
     * Returns a view allowing {@link Parallel parallel} operations to be performed {@link ConcurrentContext 
     * concurrently}. 
     */
    public FastCollection<E> parallel() {
        return new ParallelCollectionImpl<E>(this);
    }

    /**
     * Returns a view exposing elements in reversed iterative order.
     */
    public FastCollection<E> reversed() {
        return new ReversedCollectionImpl<E>(this);
    }

    /**
     * Returns a thread-safe view over this collection. The shared view allows for concurrent read as long as 
     * there is no writer. The default implementation is based on <a href=
     * "http://en.wikipedia.org/wiki/Readers%E2%80%93writer_lock">readers-writers locks</a> giving priority to writers.
     */
    public FastCollection<E> shared() {
        return new SharedCollectionImpl<E>(this);
    }

    /**
     * Returns an unmodifiable view over this collection. Any attempt to modify the collection through this view will
     * result into a {@link java.lang.UnsupportedOperationException} being raised.
     */
    public FastCollection<E> unmodifiable() {
        return new UnmodifiableCollectionImpl<E>(this);
    }

    /**
     * Returns a view exposing only the elements matching the specified filter. Adding elements not matching the 
     * specified filter has no effect. If this collection is initially empty, using a filtered view
     * ensures that this collection has only elements satisfying the specified filter predicate.
     */
    public FastCollection<E> filter(Predicate<? super E> filter) {
        return new FilteredCollectionImpl<E>(this, filter);
    }

    /**
     * Returns a view exposing elements through the specified mapping function.
     * The returned view does not allow new elements to be added.
     */
    public <R> FastCollection<R> map(Function<? super E, ? extends R> function) {
        return new MappedCollectionImpl<E, R>(this, function);
    }

    /**
     * Returns a view disallowing {@link Parallel parallel} operations to be performed
     * {@link ConcurrentContext concurrently}. 
     */
    public FastCollection<E> sequential() {
        return this;
    }

    /**
     * Returns an ordered view exposing its elements sorted according to the specified comparator.
     */
    public FastCollection<E> sorted(Comparator<? super E> comparator) {
        return new SortedCollectionImpl<E>(this, comparator);
    }
    /**
     * Returns an ordered view exposing elements sorted according to the elements natural order (
     * the collection elements must implement the {@link Comparable} interface).
     */
    @SuppressWarnings("unchecked")
    public FastCollection<E> sorted() {
        return sorted((Comparator<? super E>) NATURAL);
    }

    /**
     * Returns a view exposing only distinct elements. It does not iterate twice over the  {@link #equality() same} 
     * elements. Adding elements already present has no effect. If this collection is initially empty, 
     * using a distinct view to add new elements ensures that this collection has no duplicate element.
     */
    public FastCollection<E> distinct() {
        return new DistinctCollectionImpl<E>(this);
    }

    /**
     * Returns an ordered view keeping track of the insertion order and exposing elements in that order 
     * (first added, first to iterate). This view can be useful for compatibility with Java linked collections
     * (e.g. {@code LinkedHashSet}). Any element not added through this view is ignored while iterating.
     */
    public FastCollection<E> linked() {
        return new LinkedCollectionImpl<E>(this);
    }

    /**
     * Returns a view using the specified equality comparator for element equality comparisons.
     * 
     * @param equality the equality to use for element comparisons.
     * @return a view using the specified custom equality.
     * @see #contains
     * @see #containsAll
     * @see #remove
     * @see #removeAll
     * @see #retainAll
     * @see #distinct
     */
    public FastCollection<E> equality(Equality<? super E> equality) {
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
    public void forEach(Consumer<? super E> consumer) {
        for (E e : this)
            consumer.accept(e);
    }

    /**
     * Iterates partially or fully over this collection until the specified predicate is verified.
     * For non-parallel collections, the iterative order is the same as for {@link #iterator}.
     * 
     * @param matching the predicate to be verified.
     * @return {@code true} if there is an element matching the specified predicate; {@code false} otherwise.
     */
    @Parallel
    @Realtime(limit = LINEAR)
    public boolean until(Predicate<? super E> matching) {
        for (E e : this) {
            if (matching.test(e))
                return true;
        }
        return false;
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
        Iterator<E> itr = iterator();
        if (!itr.hasNext())
            return null;
        E accumulator = itr.next();
        while (itr.hasNext())
            accumulator = operator.apply(accumulator, itr.next());
        return accumulator;
    }

    /**
     * Removes from this collection all the elements matching the specified functional predicate.
     * The removal order is implementation dependent (usually what goes the fastest).
     * 
     * <p> To support parallel processing, the following property should be always verified:
     *     if {@code x.equals(y)} then {@code (filter.test(x) == filter.test(y))}.</p>
     *   
     * @param filter a predicate returning {@code true} for elements to be removed.
     * @return {@code true} if at least one element has been removed; {@code false} otherwise.
     */
    @Parallel
    @Realtime(limit = LINEAR)
    public abstract boolean removeIf(Predicate<? super E> filter);

    ////////////////////////////////////////////////////////////////////////////
    // Common Reductions.
    //

    /**
     * Returns an element from this collection or {@code null} if this collection is empty (convenience method).
     */
    @Parallel
    @Realtime(limit = LINEAR)
    public E any() {
        final AtomicReference<E> found = new AtomicReference<E>(null);
        until(new Predicate<E>() { // Parallel.
            @Override
            public boolean test(E param) {
                found.set(param);
                return true;
            }
        });
        return found.get();
    }

    /**
     * Returns the elements of this collection through reduction (convenience method).The type of the collection 
     * (e.g. List or Set) and its equality / comparator  should be the same as for this collection.
     */
    @Parallel
    @Realtime(limit = LINEAR)
    public FastCollection<E> collect() { // Overridden by FastTable / FastSet to return the proper type.
        final FastTable<E> reduction = FastTable.newTable(equality());
        forEach(new Consumer<E>() { // Parallel.
            @Override
            public void accept(E param) {
                synchronized (reduction) {
                    add(param);
                }
            }
        });
        return reduction;
    }

    /**
     * Returns {@code reduce((x,y) -> compare(x, y) > 0 ? x : y)} (convenience method).
     */
    @Parallel
    @Realtime(limit = LINEAR)
    @SuppressWarnings("unchecked")
    public E max() {
        return reduce((BinaryOperator<E>) MAX);
    }

    /**
     * Returns {@code reduce((x,y) -> compare(x, y) < 0 ? x : y)} (convenience method).
     */
    @Parallel
    @Realtime(limit = LINEAR)
    @SuppressWarnings("unchecked")
    public E min() {
        return reduce((BinaryOperator<E>) MIN);
    }

    // //////////////////////////////////////////////////////////////////////////
    // Collection operations.
    //

    /** Returns an iterator overs this collection; this iterator may not support element removal;
     *  for safe element removal the method {@link #removeIf} should be used. */
    @Override
    @Realtime(limit = LINEAR, comment="Usually constant except for sorted collections.")
    public abstract Iterator<E> iterator();

    /** Adds the specified element to this collection. */
    @Override
    @Realtime(limit = CONSTANT, comment = "Adding an element should not depend on the collection size")
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
        final Equality<? super E> equality = equality();
        return until(new Predicate<E>() { // Parallel.
            @Override
            @SuppressWarnings("unchecked")
            public boolean test(E param) {
                return equality.areEqual((E) searched, param);
            }
        });
    }

    /** Removes a single instance of the specified element from this collection testing for element equality 
     *  using this collection {@link #equality}. */
    @Parallel
    @Override
    @Realtime(limit = LINEAR, comment = "Could search the whole collection.")
    public boolean remove(final Object searched) {
        final Equality<? super E> equality = equality();
        final AtomicBoolean removed = new AtomicBoolean(false);
        return removeIf(new Predicate<E>() { // Parallel.
            @Override
            @SuppressWarnings("unchecked")
            public boolean test(E param) {
                return !removed.get() && equality.areEqual((E) searched, param) && !removed.compareAndSet(false, true);
            }
        });
    }

    /** Adds all the elements of the specified collection to this collection.*/
    @Override
    @Realtime(limit = LINEAR)
    public boolean addAll(Collection<? extends E> that) {
        boolean changed = false;
        for (E e : that)
            if (add(e))
                changed = true;
        return changed;
    }

    /** Adds all the elements specified  to this collection (convenience method).*/
    @Realtime(limit = LINEAR)
    public boolean addAll(E... elements) {
        boolean changed = false;
        for (E e : elements)
            if (add(e))
                changed = true;
        return changed;
    }

    /** Indicates if this collection contains all the specified elements testing for element equality 
     *  using this collection {@link #equality}. */
    @Parallel
    @Override
    @Realtime(limit = N_SQUARE)
    public boolean containsAll(Collection<?> that) {
        for (Object obj : that)
            if (!contains(obj)) // Parallel.
                return false;
        return true;
    }

    /** Removes all the specified elements from this collection testing for element equality 
     *  using this collection {@link #equality}.*/
    @Parallel
    @Override
    @Realtime(limit = N_SQUARE)
    public boolean removeAll(Collection<?> that) {
        @SuppressWarnings("unchecked")
        Equality<Object> cmp = (Equality<Object>) equality();
        final FastCollection<Object> toRemove = (cmp instanceof Order) ? FastSet.newSet((Order<Object>) cmp)
                : FastTable.newTable(cmp);
        toRemove.addAll(that);
        return removeIf(new Predicate<E>() { // Parallel.
            @Override
            public boolean test(E param) {
                return toRemove.contains(param);
            }
        });
    }

    /** Removes all the elements except those in the specified collection testing for element equality
     *  using this collection {@link #equality}. */
    @Parallel
    @Override
    @Realtime(limit = N_SQUARE)
    public boolean retainAll(final Collection<?> that) {
        @SuppressWarnings("unchecked")
        Equality<Object> cmp = (Equality<Object>) equality();
        final FastCollection<Object> toKeep = (cmp instanceof Order) ? FastSet.newSet((Order<Object>) cmp)
                : FastTable.newTable(cmp);
        toKeep.addAll(that);
        return removeIf(new Predicate<E>() { // Parallel.
            @Override
            public boolean test(E param) {
                return !toKeep.contains(param);
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
        while (it.hasNext()) {
            result[i++] = (T) it.next();
        }
        if (result.length > size) {
            result[size] = null; // As per Collection contract.
        }
        return result;
    }

    // //////////////////////////////////////////////////////////////////////////
    // Object operations.
    //


    /**
     * Compares the specified object with this collection for equality. This method follows the 
     * {@link Collection#equals(Object)} specification regardless of this collection {@link #equality equality}
     * (this method should be overridden by {@link List} and {@link Set} sub-classes).
     * 
     * @param obj the object to be compared for equality with this collection
     * @return <code>true</code> if this collection is considered equals to the one specified; 
     *         <code>false</code> otherwise.
     */
    @Override
    @Realtime(limit = LINEAR)
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    /** 
     * Returns the hash code of this collection. This method follows the {@link Collection#hashCode()} 
     * specification and should be overridden by {@link List} and {@link Set} sub-classes.
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
        return TextContext.getFormat(FastCollection.class).format(this);
    }

    // //////////////////////////////////////////////////////////////////////////
    // Misc.
    //

    /** Returns a copy of this collection; updates of the copy should not impact the original. */
    @Realtime(limit = LINEAR)
    public abstract FastCollection<E> clone();

    /** Returns the element equality for this collection. */
    public abstract Equality<? super E> equality();

    /**
     * Returns multiple read-only views over this collection (to support {@link #parallel} processing). 
     * How this collection splits (or does not split) is collection dependent (for example {@link #distinct}
     * views do not split). There is no guarantee over the iterative order of the sub-views which may 
     * be different from this collection iterative order.
     * 
     * <p> Any attempt to modify this collection through its sub-views will result 
     *     in a {@link UnsupportedOperationException} being thrown.</p>
     * 
     * @param n the desired number of independent views.
     * @return the sub-views (array of length in range [1..n])
     * @throws IllegalArgumentException if {@code n <= 0} 
     */
    public abstract FastCollection<E>[] trySplit(int n);

    /**
     * Default text format for fast collections (parsing not supported).
     */
    public static class Format extends TextFormat<FastCollection<?>> {

        @Override
        public FastCollection<Object> parse(CharSequence csq, Cursor cursor) throws IllegalArgumentException {
            throw new UnsupportedOperationException();
        }

        @Override
        public Appendable format(FastCollection<?> that, final Appendable dest) throws IOException {
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

    /** Natural comparator for comparable instances. */
    private static final Comparator<Comparable<Object>> NATURAL = new Comparator<Comparable<Object>>() {
        @Override
        public int compare(Comparable<Object> left, Comparable<Object> right) {
            return left != null ? left.compareTo((Comparable<Object>) right)
                    : right != null ? -right.compareTo(left) : 0;
        }

    };

    /** Max operator for comparable instances. */
    private static final BinaryOperator<Comparable<Object>> MAX = new BinaryOperator<Comparable<Object>>() {
        @Override
        public Comparable<Object> apply(Comparable<Object> left, Comparable<Object> right) {
            return NATURAL.compare(left, right) > 0 ? left : right;
        }
    };

    /** Min operator for comparable instances. */
    private static final BinaryOperator<Comparable<Object>> MIN = new BinaryOperator<Comparable<Object>>() {
        @Override
        public Comparable<Object> apply(Comparable<Object> left, Comparable<Object> right) {
            return NATURAL.compare(left, right) < 0 ? left : right;
        }
    };
}