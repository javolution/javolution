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
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javolution.annotation.Format;
import javolution.annotation.RealTime;
import javolution.annotation.RealTime.Limit;
import javolution.annotation.StackSafe;
import javolution.annotation.ThreadSafe;
import javolution.internal.util.collection.CustomComparatorCollectionImpl;
import javolution.internal.util.collection.DistinctCollectionImpl;
import javolution.internal.util.collection.FilteredCollectionImpl;
import javolution.internal.util.collection.MappedCollectionImpl;
import javolution.internal.util.collection.ParallelCollectionImpl;
import javolution.internal.util.collection.ReversedCollectionImpl;
import javolution.internal.util.collection.SharedCollectionImpl;
import javolution.internal.util.collection.SortedCollectionImpl;
import javolution.internal.util.collection.UnmodifiableCollectionImpl;
import javolution.internal.util.comparator.WrapperComparatorImpl;
import javolution.lang.Copyable;
import javolution.text.Cursor;
import javolution.text.TextContext;
import javolution.text.TextFormat;
import javolution.util.function.Consumer;
import javolution.util.function.Function;
import javolution.util.function.Predicate;
import javolution.util.service.CollectionService;
import javolution.util.service.ComparatorService;
import javolution.util.service.OperatorService;

/**
 * <p> A high-performance collection with {@link RealTime real-time} behavior; 
 *     smooth capacity increase/decrease and minimal memory footprint. 
 *     Fast collections support multiple views which can be chained.
 * <ul>
 *    <li>{@link #unmodifiable} - View which does not allow for modification.</li>
 *    <li>{@link #shared} - View allowing for concurrent read/write.</li>
 *    <li>{@link #filter} - View exposing only the elements matching a specified filter.</li>
 *    <li>{@link #map} - View exposing elements through the specified mapping function.</li>
 *    <li>{@link #sorted} - View exposing elements according to their sorting order.</li>
 *    <li>{@link #reverse} - View exposing elements in reverse order.</li>
 *    <li>{@link #distinct} - View exposing each element only once.</li>
 *    <li>{@link #usingComparator} - View using a custom comparator for element equality/comparison.</li>
 *    <li>{@link #parallel} - A {@link #shared() shared} view for which closure iterations can
 *                             be performed concurrently.</li>
 * </ul>
 *    Views are similar to <a href="http://lambdadoc.net/api/java/util/stream/package-summary.html">
 *    Java 8 streams</a> except that views are themselves fast collections and can be used to update
 *    the original collections.
 *    [code]
 *    FastTable<String> names = new FastTable<String>("Oscar Thon", "Eva Poret", "Paul Auchon");
 *    names.subList(0, n).clear(); // Removes the n first names (see java.util.List).
 *    names.distinct().add("Guy Liguili"); // Adds "Guy Liguili" only if not already present.
 *    names.filter(isLong).clear(); // Removes all the persons with long names.
 *    names.parallel().filter(isLong).clear(); // Same as above but performed concurrently.
 *    ...
 *    Predicate<CharSequence> isLong = new Predicate<CharSequence>() { 
 *         public boolean test(CharSequence csq) {
 *             return csq.length() > 16; 
 *         }
 *    });
 *    [/code]
 *    Views can of course be used to perform "stream" oriented filter-map-reduce operations.
 *    [code]
 *    String anyLongName = names.filter(isLong).reduce(Operators.ANY); // Returns any long name.
 *    int nbrChars = names.map(toLength).reduce(Operators.SUM); // Returns the total number of characters (sequentially).
 *    int maxLength = names.parallel().map(toLength).reduce(Operators.MAX); // Finds the longest name in parallel.
 *    ...
 *    Function<CharSequence, Integer> toLength = new Function<CharSequence, Integer>() {
 *         public Integer apply(CharSequence csq) {
 *             return csq.length(); 
 *         }
 *    });
 *    [/code]
 *    Views may override the element comparator used by methods 
 *    such as {@link #contains contains}, {@link #remove remove} or by views 
 *    such as {@link #sorted sorted}, {@link #distinct distinct}.
 *    [code]
 *    // Case insensitive search for "Luc Surieux".
 *    boolean found = names.usingComparator(Comparators.LEXICAL_CASE_INSENSITIVE).contains("Luc Surieux"); 
 *    [/code]
 *    </p>
 *           
 * <p> Fast collections can be iterated over using closures, this is also the
 *     preferred way to iterate over {@link #shared() shared} collections (no concurrent modification exception possible).
 *     If a collection (or a map) is shared, any chained view is also thread-safe.
 *     Analogously, if a collection is {@link #parallel parallel}, any closure-based iteration over 
 *     a chained view is performed concurrently. 
 *     [code]
 *     FastMap<String, Runnable> tasks = new FastMap<String, Runnable>().shared();
 *     ...
 *     boolean completed = tasks.values().doWhile(new Predicate<Runnable>() { // Thread-Safe (even if tasks concurrently modified).
 *         public boolean test(Runnable task) {
 *              try {
 *                  task.run();
 *                  return true; // Continue.
 *              } (Throwable error) {
 *                  return false; // Stop iterating.      
 *              }
 *         }
 *     });
 *     [/code]
 *     With Java 8, closures are greatly simplified using lambda expressions.
 *     For example, the following prints all names in alphabetical order. 
 *     [code]
 *     names.sorted().forEach(str -> System.out.println(str)); 
 *     [/code]
 *     </p>
 * 
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0.0, December 12, 2012
 */
@StackSafe
@Format(text = FastCollection.PlainText.class)
@RealTime
public class FastCollection<E> implements Collection<E>,
        Copyable<FastCollection<E>>, Serializable {

    /**
     * Holds the actual collection implementation.
     */
    private final CollectionService<E> impl;

    /**
     * Creates a collection backed up by the specified service implementation.
     */
    protected FastCollection(CollectionService<E> service) {
        this.impl = service;
    }

    /**
     * Returns this collection service implementation.
     */
    protected CollectionService<E> getService() {
        return impl;
    }

    /***************************************************************************
     * Collection views.
     */

    /**
     * Returns an unmodifiable view of this collection. 
     * Attempts to modify the returned collection result in an 
     * {@link UnsupportedOperationException} being thrown.
     */
    public FastCollection<E> unmodifiable() {
        return new FastCollection<E>(new UnmodifiableCollectionImpl<E>(impl));          
    }
   
    /**
     * Returns a thread safe read-write view of this collection.
     * Any view derived from a shared view is itself thread-safe.
     * The semantic of a shared view allows for concurrent read / iterations 
     * without blocking. The preferred way to iterate through a shared 
     * collection is using closure (e.g. {@link #doWhile doWhile}, 
     * {@link #removeIf removeIf}, {@link #forEach forEach}) as no concurrent 
     * modification can possibly occur while iterating.
     */
    public FastCollection<E> shared() {
        return new FastCollection<E>(new SharedCollectionImpl<E>(impl));
    }
    
    /** 
     * Returns a view over this collection overriding this collection 
     * comparator. Any modification of the returned view is reflected 
     * in this collection and vice-versa. The returned view uses the specified 
     * comparator for methods such as {@link #contains contains}, 
     * {@link #remove remove} as well as for views such as 
     * {@link #sorted sorted}, {@link #distinct distinct}.
     */
    @SuppressWarnings("unchecked")
    public FastCollection<E> usingComparator(Comparator<? super E> cmp) {
        ComparatorService<E> comparator = (cmp instanceof ComparatorService) ?
                (ComparatorService<E>)cmp :  new WrapperComparatorImpl<E>(cmp);
        return new FastCollection<E>(new CustomComparatorCollectionImpl<E>(impl, comparator));
    }
    
    /** 
     * Returns a view exposing only the elements matching the specified 
     * filter. Any modification of the returned view is reflected in this
     * collection and vice-versa. Adding/removing elements to a filtered 
     * view for which the predicate is not verified has no effect 
     * (add/remove returns <code>false</code>).
     */
    public FastCollection<E> filter(Predicate<? super E> filter) {
        return new FastCollection<E>(new FilteredCollectionImpl<E>(impl, filter));
    }  
    
    /** 
     * Returns a view exposing elements through the specified mapping function.
     * Any modification in the returned view is reflected in this
     * collection and vice-versa. The mapped view does not support adding 
     * new elements (although removing elements is authorized).
     */
    public <R> FastCollection<R> map(Function<? super E, ? extends R> function) {
        return new FastCollection<R>(new MappedCollectionImpl<E, R>(impl, function));     
    }

    /** 
     * Returns a view exposing elements ordered according to this collection 
     * {@link #comparator() comparator}. Adding/removing elements
     * to a sorted view is authorized and will impact the view being sorted.
     */
    public FastCollection<E> sorted() {
        return new FastCollection<E>(new SortedCollectionImpl<E>(impl));
    }

    /** 
     * Returns a view exposing elements in reverse iterative order.
     * Adding/removing elements to a reversed view is authorized and 
     * will directly impact the original collection.
     */
    public FastCollection<E> reverse() {
        return new FastCollection<E>(new ReversedCollectionImpl<E>(impl));
    }

    /** 
     * Returns a view exposing only distinct elements (it does not iterate twice 
     * over the {@link #comparator() same} elements). Any modification of the 
     * returned view is reflected in this collection and vice-versa. 
     * Adding/removing elements to the returned view is authorized and 
     * directly impacts this collection. Adding elements already in the 
     * collection has no effect (if this collection is initially empty, using
     * the distinct view prevents element duplication).   
     */
    public FastCollection<E> distinct() {
        return new FastCollection<E>(new DistinctCollectionImpl<E>(impl));
    }
 
    /** 
     * Returns a parallel view of this collection. Using this view, closures
     * operations are performed concurrently. The number of concurrent sub-collections
     * is typically equals to the local {@link 
     * javolution.context.ConcurrentContext#CONCURRENCY concurrency}.
     */
    public FastCollection<E> parallel() {
        return new FastCollection<E>(new ParallelCollectionImpl<E>(impl));
    }
        
    /***************************************************************************
     * Closure operations.
     */

    /** 
     * Iterates this collection elements. For {@link #parallel parallel} 
     * collections iterations may be performed concurrently on sub-collections.
     * Iterations will stop (including parallel ones) if the specified predicate
     * evaluation returns {@code false} at least once.
     * 
     * @param doContinue a predicate returning {@code false} to stop iterating.
     * @return {@code true} if iterations have been performed without any stopping;
     *         {@code false} if at least one predicate evaluation returned {@code false}  
     */
    @RealTime(Limit.LINEAR)
    public boolean doWhile(Predicate<? super E> doContinue) {
        return impl.doWhile(doContinue);
    }

    /** 
     * Removes from this collection all the elements matching the specified 
     * predicate  
     * 
     * @param filter a predicate returning {@code true} for elements to be removed.
     * @return {@code true} if the collection has changed due to this call;
     *         {@code false} otherwise.
     */
    @RealTime(Limit.LINEAR)
    public boolean removeIf(Predicate<? super E> filter) {
        return impl.removeIf(filter);
    }

    /** 
     * Iterates all the elements of this collection and apply the specified 
     * consumer operation (convenience method).
     */
    @RealTime(Limit.LINEAR)
    public void forEach(final Consumer<? super E> consumer) {
        doWhile(new Predicate<E>() {

            @Override
            public boolean test(E param) {
                consumer.accept(param);
                return true;
            }
            
        });
    }

    /** 
     * Performs a reduction of the elements of this collection using the 
     * specified operator (convenience method). This may not involve iterating 
     * over all the collection elements, for example the comparators:
     * {@link Comparators#ANY ANY}, {@link Comparators#AND AND}, 
     * {@link Comparators#OR OR} may stop iterating early. 
     * Reductions are performed concurrently if the collection is 
     * {@link #parallel() parallel}. 
     *    
     * @param operator the {@link ThreadSafe thread-safe} operator.
     * @return {@code operator.apply(this.getService())}
     */
    @SuppressWarnings("unchecked")
    @RealTime(Limit.LINEAR)
    public E reduce(OperatorService<? super E> operator) {
        return ((OperatorService<E>)operator).apply(impl);
    }

    /***************************************************************************
     * Collection operations.
     */

    /**
     * Adds the specified element; although the implementation may append the 
     * element to the end of this collection it is not forced to do so 
     * (e.g. if the collection is ordered).
     * 
     * @param element the element to be added to this collection.
     * @return {@code true} if the collection is modified as a result of 
     *         this call; {@code false} otherwise.
     * @throws UnsupportedOperationException if the collection is not modifiable.
     */
    public boolean add(E element) {
        return impl.add(element);
    }

    /**
     * Indicates if this collection is empty.
     * 
     * @return {@code size() == 0}
     */
    @RealTime(Limit.LINEAR)
    public boolean isEmpty() {
        return size() == 0;
    }
 
    /**
     * Returns the number of elements in this collection. The default 
     * implementation counts the number of element (in parallel 
     * if the collection is {@link #parallel parallel}).
     * 
     * @return {@code this.map(TO_ONE).reduce(Operators.SUM)}
     */
    @RealTime(Limit.LINEAR)
    public int size() {
        return this.map(TO_ONE).reduce(Operators.SUM);
    }
    
    private static final Function<Object, Integer> TO_ONE 
        = new Function<Object, Integer>() {
            Integer ONE = 1;
            @Override
            public Integer apply(Object param) {
                return ONE;
            }
      
    };

    /**
     * Removes all of the elements from this collection.
     * The default implementation removes all the elements iteratively 
     * (in parallel if the collection is {@link #parallel parallel}).
     *
     * @throws UnsupportedOperationException if the collection is not modifiable.
     */
    @RealTime(Limit.LINEAR)
    public void clear() {
        removeIf(new Predicate<E>() {
            @Override
            public boolean test(E param) {
                return true;
            }
        });
    }
    
    /**
     * Indicates if this collection contains the specified element. 
     * The default implementation iterates over all the elements (in parallel 
     * if the collection is {@link #parallel parallel}) in order to find an element
     * {@link #comparator() equals} to the one specified.
     *
     * @param element the element whose presence in this collection 
     *        is to be tested.
     * @return {@code true} if this collection contains the specified
     *         element;{@code false} otherwise.
     */
    @SuppressWarnings("unchecked")
    @RealTime(Limit.LINEAR)
    public boolean contains(final Object element) {
        final ComparatorService<? super E> cmp = this.comparator();
        final boolean[] found = new boolean[1];
        doWhile(new Predicate<E>() {

            @Override
            public boolean test(E param) {
                if (cmp.areEqual((E)element, param)) {
                    found[0] = true;
                    return false;
                }
                return true;
            }
            
        });
        return found[0];
    }
    

    /**
     * Removes a single instance {@link #comparator() equals} to the specified
     * element from this collection, if it is present. Even if the removal 
     * is performed concurrently ({@link #parallel parallel} collections), 
     * only one instance is removed. 
     *
     * @param element the element to be removed from this collection.
     * @return  {@code true}  if this collection contained the specified
     *         element;  {@code false} otherwise.
     * @throws UnsupportedOperationException if the collection is not modifiable.
     */
    @SuppressWarnings("unchecked")
    @RealTime(Limit.LINEAR)
    public boolean remove(final Object element) {
        final ComparatorService<? super E> cmp = this.comparator();
        final boolean[] found = new boolean[1];
        removeIf(new Predicate<E>() {
            @Override
            public boolean test(E param) {
                if (found[0]) return false; // Shortcut.
                if (cmp.areEqual((E)element, param)) {
                    synchronized (this) {
                        if (found[0]) return false; // One instance already removed.
                        found[0] = true;
                    }
                    return true;
                }
                return false;
            }
        });
        return found[0];
    }

    /** 
     * Returns an iterator over this collection. For {@link #shared shared} 
     * collections, closure (e.g. {@link #doWhile}) should be used instead of
     * iterators (or Java 1.5 simplified loop) whenever possible.
     */
    public Iterator<E> iterator() {
        return impl.iterator();
    }

    @Override
    @SuppressWarnings("unchecked")
    @RealTime(Limit.LINEAR)
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
            public boolean test(E param) {
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
    @RealTime(Limit.N_SQUARE)
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
        boolean containsAll = that.doWhile(new Predicate<E>() {
            public boolean test(E param) {
                if (!contains(param)) { return false; // Breaks.
                }
                return true;
            }
        });
        return containsAll;
    }

    @Override
    @RealTime(Limit.N_SQUARE)
    public boolean removeAll(final Collection<?> that) {
        return removeIf(new Predicate<E>() {
            public boolean test(E param) {
                return that.contains(param);
            }
        });
    }

    @Override
    @RealTime(Limit.N_SQUARE)
    public boolean retainAll(final Collection<?> that) {
        return removeIf(new Predicate<E>() {
            public boolean test(E param) {
                return !that.contains(param);
            }
        });
    }

    @Override
    @RealTime(Limit.LINEAR)
    public Object[] toArray() {
        return toArray(new Object[size()]);
    }

    @Override
    @SuppressWarnings("unchecked")
    @RealTime(Limit.LINEAR)
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

            public boolean test(E param) {
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
    @RealTime(Limit.LINEAR)
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
                public boolean test(E param) {
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
    @RealTime(Limit.LINEAR)
    public int hashCode() {
        final int[] hash = new int[1];
        if (this instanceof Set) {
            this.doWhile(new Predicate<E>() {
                public boolean test(E param) {
                    hash[0] += ((param == null) ? 0 : param.hashCode());
                    return true;
                }
            });
            return hash[0];
        } else if (this instanceof List) {
            hash[0] = 1;
            this.doWhile(new Predicate<E>() {
                public boolean test(E param) {
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

    /***************************************************************************
     * Misc.
     */

    /**
     * Returns the comparator used by this collection for element equality
     * or comparison (if the collection is sorted).
     */
    public ComparatorService<? super E> comparator() {
        return impl.comparator();
    }

    @Override
    @RealTime(Limit.LINEAR)
    public FastCollection<E> copy() {
        final FastTable<E> table = new FastTable<E>();
        this.doWhile(new Predicate<E>() {
            @SuppressWarnings("unchecked")
            public boolean test(E param) {
                table.add((param instanceof Copyable) ? ((Copyable<E>) param)
                        .copy() : param);
                return true;
            }
        });
        return table;
    }

    @Override
    @RealTime(Limit.LINEAR)
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
                public boolean test(Object obj) {
                    try {
                        if (!isFirst) {
                            dest.append(", ");
                        } else {
                            isFirst = false;
                        }
                        if (obj != null) {
                            javolution.text.TextFormat<Object> tf = TextContext
                                    .getFormat(obj.getClass());
                            tf.format(obj, dest);
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

    private static final long serialVersionUID = 5965713679404252279L;
 }