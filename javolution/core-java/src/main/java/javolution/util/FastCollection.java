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
import javolution.internal.util.collection.ComparatorCollectionImpl;
import javolution.internal.util.collection.DistinctCollectionImpl;
import javolution.internal.util.collection.FilteredCollectionImpl;
import javolution.internal.util.collection.MappedCollectionImpl;
import javolution.internal.util.collection.ParallelCollectionImpl;
import javolution.internal.util.collection.ReversedCollectionImpl;
import javolution.internal.util.collection.SequentialCollectionImpl;
import javolution.internal.util.collection.SharedCollectionImpl;
import javolution.internal.util.collection.SortedCollectionImpl;
import javolution.internal.util.collection.UnmodifiableCollectionImpl;
import javolution.internal.util.comparator.WrapperComparatorImpl;
import javolution.lang.Copyable;
import javolution.text.Cursor;
import javolution.text.TextContext;
import javolution.text.TextFormat;
import javolution.util.function.CollectionConsumer;
import javolution.util.function.CollectionOperator;
import javolution.util.function.FullComparator;
import javolution.util.function.Comparators;
import javolution.util.function.Consumer;
import javolution.util.function.Function;
import javolution.util.function.Operators;
import javolution.util.function.Predicate;
import javolution.util.function.Sequential;
import javolution.util.service.CollectionService;

/**
 * <p> A high-performance collection with {@link RealTime real-time} behavior; 
 *     smooth capacity increase/decrease and minimal memory footprint. 
 *     Fast collections support multiple views which can be chained.
 * <ul>
 *    <li>{@link #unmodifiable} - View which does not allow any modification.</li>
 *    <li>{@link #shared} - View allowing concurrent modifications.</li>
 *    <li>{@link #parallel} - A {@link #shared shared} view allowing parallel processing (closure-based).</li>
 *    <li>{@link #sequential} - View disallowing parallel processing.</li>
 *    <li>{@link #filtered} - View exposing the elements matching the specified filter.</li>
 *    <li>{@link #mapped} - View exposing elements through the specified mapping function.</li>
 *    <li>{@link #sorted} - View exposing elements according to the collection sorting order.</li>
 *    <li>{@link #reversed} - View exposing elements in reverse iterative order.</li>
 *    <li>{@link #distinct} - View exposing each element only once.</li>
 *    <li>{@link #comparator} - View using the specified comparator for element equality/order.</li>
 * </ul>
 *    Views are similar to <a href="http://lambdadoc.net/api/java/util/stream/package-summary.html">
 *    Java 8 streams</a> except that views are themselves collections (virtual collections)
 *    and actions on a view can impact the original collection. Collection views are nothing "new" 
 *    since they already existed in the original java.util collection classes (e.g. List.subList(...),
 *    Map.keySet(), Map.values()). Javolution extends to this concept and allows views to be chained 
 *    which addresses the concern of class proliferation (see
 *    <a href="http://cr.openjdk.java.net/~briangoetz/lambda/collections-overview.html">
 *    State of the Lambda: Libraries Edition</a>). 
 *    [code]
 *    FastTable<String> names = new FastTable<String>("Oscar Thon", "Eva Poret", "Paul Auchon");
 *    boolean found = names.comparator(Comparators.LEXICAL_CASE_INSENSITIVE).contains("LUC SURIEUX"); 
 *    names.subList(0, n).clear(); // Removes the n first names (see java.util.List).
 *    names.distinct().add("Guy Liguili"); // Adds "Guy Liguili" only if not already present.
 *    names.filtered(isLong).clear(); // Removes all the persons with long names.
 *    names.parallel().filtered(isLong).clear(); // Same as above but performed concurrently.
 *    ...
 *    Predicate<CharSequence> isLong = new Predicate<CharSequence>() { 
 *         public boolean test(CharSequence csq) {
 *             return csq.length() > 16; 
 *         }
 *    });
 *    [/code]
 *    
 *    Views can of course be used to perform "stream" oriented filter-map-reduce operations with the same benefits:
 *    Parallelism support, excellent memory characteristics (no caching and cost nothing to create), etc.
 *    [code]
 *    String anyLongName = names.filtered(isLong).reduce(Operators.ANY); // Returns any long name.
 *    int nbrChars = names.mapped(toLength).reduce(Operators.SUM); // Returns the total number of characters.
 *    int maxLength = names.parallel().mapped(toLength).reduce(Operators.MAX); // Finds the longest name in parallel.
 *    ...
 *    Function<CharSequence, Integer> toLength = new Function<CharSequence, Integer>() {
 *         public Integer apply(CharSequence csq) {
 *             return csq.length(); 
 *         }
 *    });
 *    
 *    // JDK Class.getEnclosingMethod using Javolution's views and Java 8 (to be compared with the current 20 lines implementation !).
 *    Method matching = new FastTable<Method>(enclosingInfo.getEnclosingClass().getDeclaredMethods())
 *       .filtered(m -> Comparators.STANDARD.areEqual(m.getName(), enclosingInfo.getName())
 *       .filtered(m -> Comparators.ARRAY.areEqual(m.getParameterTypes(), parameterClasses))
 *       .filtered(m -> Comparators.STANDARD.areEqual(m.getReturnType(), returnType))
 *       .reduce(Operators.ANY);
 *    if (matching == null) throw new InternalError("Enclosing method not found");
 *    return matching;
 *    [/code]
 *    </p>
 *           
 * <p> Fast collections can be iterated over using closures, this is also the preferred way 
 *     to iterate over {@link #shared() shared} collections (no concurrent update possible).
 *     If a collection (or a map) is shared, derived views are also thread-safe.
 *     Similarly, if a collection is {@link #parallel parallel}, closure-based iterations 
 *     on derived views can be performed concurrently. 
 *     [code]
 *     FastMap<String, Runnable> tasks = ...
 *     ...
 *     tasks.values().parallel().forEach(new Consumer<Runnable>() { // Executes task concurrently. 
 *         public void accept(Runnable task) {
 *              task.run();
 *         }
 *     });
 *     [/code]
 *     With Java 8, closures are greatly simplified using lambda expressions.
 *     [code]
 *     tasks.values().parallel().forEach(task -> task.run()); // Same as above.
 *     names.sorted().reversed().forEach(str -> System.out.println(str)); // Prints names in reverse alphabetical order. 
 *     [/code]
 *     </p>
 * 
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0.0, December 12, 2012
 */
@StackSafe
@Format(text = FastCollection.PlainText.class)
@RealTime
public abstract class FastCollection<E> implements Collection<E>,
        Copyable<FastCollection<E>>, Serializable {

    private static final long serialVersionUID = 6474700510998033882L;

    /**
     * Default constructor.
     */
    protected FastCollection() {}

    /**
      * Returns the service implementation of this collection.
      */
    public abstract CollectionService<E> service();

    /***************************************************************************
     * Collection views.
     */

    /**
     * Returns an unmodifiable view over this collection. Any attempt to 
     * modify the collection through this view will result into 
     * a {@link java.lang.UnsupportedOperationException} being raised.
     */
    public FastCollection<E> unmodifiable() {
        return new UnmodifiableCollectionImpl<E>(service());
    }

    /**
     * Returns a thread-safe view over this collection allowing 
     * concurrent read without blocking and concurrent write possibly 
     * blocking. All updates performed by the specified action on this 
     * collection are atomic as far as this collection's readers are concerned.
     * To perform complex actions on a shared collection in an atomic manner, 
     * the {@link #atomic atomic} method should be used (on the shared view
     * or on any view derived from the shared view).
     * 
     *  @see #atomic(Runnable)
     */
    public FastCollection<E> shared() {
        return new SharedCollectionImpl<E>(service());
    }

    /** 
     * Returns a view over this collection using the specified comparator 
     * service for element equality or sorting.
     * 
     * @see #contains(java.lang.Object) 
     * @see #remove(java.lang.Object) 
     * @see #sorted() 
     * @see #distinct() 
     */
    public FastCollection<E> comparator(FullComparator<? super E> cmp) {
        return new ComparatorCollectionImpl<E>(service(), cmp);
    }

    /** 
     * Returns a view over this collection using the specified functional 
     * comparator for element equality or sorting.
     * 
     * @see #comparator(javolution.util.function.FullComparator) 
     */
    public FastCollection<E> comparator(Comparator<? super E> cmp) {
        return comparator(new WrapperComparatorImpl<E>(cmp));
    }

    /** 
     * Returns a view exposing only the elements matching the specified 
     * filter.
     */
    public FastCollection<E> filtered(Predicate<? super E> filter) {
        return new FilteredCollectionImpl<E>(service(), filter);
    }

    /** 
     * Returns a view exposing elements through the specified mapping function.
     * The view does not allows new elements to be added.
     */
    public <R> FastCollection<R> mapped(
            Function<? super E, ? extends R> function) {
        return new MappedCollectionImpl<E, R>(service(), function);
    }

    /** 
     * Returns a view exposing element sorted according to this collection 
     * {@link #comparator() comparator}. 
     * The {@link #comparator(java.util.Comparator) comparator view} can be used
     * to sort elements using any user-defined comparator.
     */
    public FastCollection<E> sorted() {
        return new SortedCollectionImpl<E>(service());
    }

    /** 
     * Returns a view exposing elements in reverse iterative order.
     */
    public FastCollection<E> reversed() {
        return new ReversedCollectionImpl<E>(service());
    }

    /** 
     * Returns a view exposing only distinct elements (it does not iterate twice 
     * over the {@link #comparator() same} elements). Adding elements already 
     * in the collection through this view has no effect (if this collection is 
     * initially empty, using the distinct view prevents element duplication).   
     */
    public FastCollection<E> distinct() {
        return new DistinctCollectionImpl<E>(service());
    }

    /** 
     * Returns a parallel/{@link #shared() shared} view of this collection. 
     * Using this view, closure-based iteration can be performed concurrently. 
     * The default implementation is based upon 
     * {@link javolution.context.ConcurrentContext ConcurrentContext}.
     * 
     * <p> Note: Some view may prevent parallel processing over their 
     *           collection target. It is therefore preferable for 
     *           parallel views to be the last view before processing. 
     *           For example {@code collection.unmodifiable().parallel()}
     *           is preferred to {@code collection.parallel().unmodifiable()}.</p>
     */
    public FastCollection<E> parallel() {
        return new ParallelCollectionImpl<E>(service());
    }

    /** 
     * Returns a sequential view of this collection. Using this view, 
     * all closure-based iterations are performed in sequence.
     */
    public FastCollection<E> sequential() {
        return new SequentialCollectionImpl<E>(service());
    }

    /***************************************************************************
     * Closure operations.
     */

    /** 
     * Iterates over this collection elements applying the specified consumer 
     * service. For {@link #parallel parallel} collections, iterations can be
     * performed concurrently if the specified consumer is not 
     * {@link Sequential Sequential}. Iterations may not fully complete if the 
     * consumer has {@link CollectionConsumer.Controller#terminate terminated}.
     * Also, the consumer service may {@link CollectionConsumer.Controller#remove
     * remove} the elements iterated over.
     * 
     * @param consumer the service applied to this collection elements.
     */
    @RealTime(Limit.LINEAR)
    public void forEach(CollectionConsumer<? super E> consumer) {
        service().forEach(consumer);
    }

    /** 
     * Iterates over all this collection elements applying the specified 
     * functional consumer. Iterations are performed concurrently if the 
     * collection is {@link #parallel() parallel} and the specified consumer 
     * is not {@link Sequential Sequential}.
     * 
     * @param consumer the functional consumer applied to the collection elements.
     */
    @RealTime(Limit.LINEAR)
    public void forEach(final Consumer<? super E> consumer) {
        (consumer instanceof Sequential ? sequential().service() : service())
                .forEach(new CollectionConsumer<E>() {

                    @Override
                    public void accept(E e, Controller controller) {
                        consumer.accept(e); // Controller is ignored.
                    }

                });
    }

    /**
     * Removes from this collection all the elements matching the specified
     * functional predicate. Removal are performed concurrently if the 
     * collection is {@link #parallel() parallel} and the specified filter 
     * is not {@link Sequential Sequential}.
     * 
     * @param filter a predicate returning {@code true} for elements to be removed.
     * @return {@code true} if at least one element has been removed;
     *         {@code false} otherwise.
     */
    @RealTime(Limit.LINEAR)
    public boolean removeIf(final Predicate<? super E> filter) {
        final boolean[] anyRemoved = new boolean[1];
        (filter instanceof Sequential ? sequential() : this)
                .forEach(new CollectionConsumer<E>() {

                    @Override
                    public void accept(E e, Controller controller) {
                        if (filter.test(e)) {
                            controller.remove();
                            anyRemoved[0] = true;
                        }
                    }

                });
        return anyRemoved[0];
    }

    /** 
     * Performs a reduction of the elements of this collection using the 
     * specified operator (convenience method). This may not involve iterating 
     * over all the collection elements, for example the comparators:
     * {@link Comparators#ANY ANY}, {@link Comparators#AND AND}, 
     * {@link Comparators#OR OR} may stop iterating early. 
     * Reductions are performed concurrently if the collection is 
     * {@link #parallel() parallel} in which case the operator should 
     * be marked {@link ThreadSafe thread-safe}. All predefined 
     * {@link Operators operators} are thread-safe. 
     *    
     * @param operator the operator.
     * @return {@code operator.apply(this.getService())}
     */
    @SuppressWarnings("unchecked")
    @RealTime(Limit.LINEAR)
    public E reduce(CollectionOperator<? super E> operator) {
        return ((CollectionOperator<E>) operator).apply(service());
    }

    /***************************************************************************
     * Collection operations.
     */

    /**
     * Adds the specified element to this collection.
     * 
     * @param element the element to be added to this collection.
     * @return {@code true} if the collection is modified as a result of 
     *         this call; {@code false} otherwise.
     * @throws UnsupportedOperationException if the collection is not modifiable.
     */
    public boolean add(E element) {
        return service().add(element);
    }

    /**
     * Indicates if this collection is empty (convenience method).
     * 
     * @return {@code size() == 0}
     */
    @RealTime(Limit.LINEAR)
    public boolean isEmpty() {
        return size() == 0;
    }

    /**
     * Returns the number of elements in this collection (convenience method).
     * 
     * @return {@code this.map(TO_ONE).reduce(Operators.SUM)}
     */
    @RealTime(Limit.LINEAR)
    public int size() {
        return this.mapped(TO_ONE).reduce(Operators.SUM);
    }

    private static final Function<Object, Integer> TO_ONE = new Function<Object, Integer>() {
        Integer ONE = 1;

        @Override
        public Integer apply(Object param) {
            return ONE;
        }

    };

    /**
     * Removes all of the elements from this collection (convenience method).
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
     * The search can be performed in parallel if the collection is 
     * {@link #parallel parallel}.
     *
     * @param element the element whose presence in this collection 
     *        is to be tested.
     * @return {@code true} if this collection contains the specified
     *         element;{@code false} otherwise.
     */
    @SuppressWarnings("unchecked")
    @RealTime(Limit.LINEAR)
    public boolean contains(final Object element) {
        final boolean[] found = new boolean[1];
        service().forEach(new CollectionConsumer<E>() {
            final FullComparator<? super E> cmp = service().comparator();

            @Override
            public void accept(E e, Controller controller) {
                if (cmp.areEqual((E) element, e)) {
                    found[0] = true;
                    controller.terminate();
                }
            }

        });
        return found[0];
    }

    /**
     * Removes a single instance {@link #comparator() equals} to the specified
     * element from this collection. Even for {@link #parallel parallel} 
     * collections, no more than one instance is removed.
     *
     * @param element the element to be removed from this collection.
     * @return {@code true} if this collection contained the specified
     *         element; {@code false} otherwise.
     * @throws UnsupportedOperationException if the collection is not modifiable.
     */
    @SuppressWarnings("unchecked")
    @RealTime(Limit.LINEAR)
    public boolean remove(final Object element) {
        final boolean[] found = new boolean[1];
        service().forEach(new CollectionConsumer<E>() {
            final FullComparator<? super E> cmp = service().comparator();

            @Override
            public void accept(E param, Controller controller) {
                if (cmp.areEqual((E) element, param)) {
                    synchronized (this) { // Ensures removal unicity.
                        if (!found[0]) {
                            controller.remove();
                        }
                    }
                    controller.terminate();
                }
            }
        });
        return found[0];
    }

    /** 
     * Returns an iterator over this collection. For 
     * {@link #shared shared} / {@link #parallel parallel}
     * collections, closures should be used instead of iterators.
     */
    public Iterator<E> iterator() {
        return service().iterator();
    }

    @Override
    @SuppressWarnings("unchecked")
    @RealTime(Limit.LINEAR)
    public boolean addAll(Collection<? extends E> that) {
        if (that instanceof FastCollection)
            return addAll((FastCollection<E>) that);
        boolean modified = false;
        for (E e : that) {
            if (add(e)) {
                modified = true;
            }
        }
        return modified;
    }

    private boolean addAll(FastCollection<E> that) {
        final boolean[] modified = new boolean[1];
        // Adds sequentially to keep the original order.
        that.sequential().forEach(new CollectionConsumer<E>() {
            @Override
            public void accept(E e, Controller controller) {
                if (add(e)) {
                    modified[0] = true;
                }
            }
        });
        return modified[0];
    }

    @Override
    @SuppressWarnings("unchecked")
    @RealTime(Limit.N_SQUARE)
    public boolean containsAll(Collection<?> that) {
        if (that instanceof FastCollection)
            return containsAll((FastCollection<E>) that);
        for (Object e : that) {
            if (!contains(e))
                return false;
        }
        return true;
    }

    private boolean containsAll(FastCollection<E> that) {
        final boolean[] missing = new boolean[1];
        that.service().forEach(new CollectionConsumer<E>() {

            @Override
            public void accept(E e, Controller controller) {
                if (!contains(e)) {
                    missing[0] = true;
                    controller.terminate();
                }
            }
        });
        return !missing[0];
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
    public <T> T[] toArray(final T[] array) {
        final int size = size();
        final T[] result = (size <= array.length) ? array
                : (T[]) java.lang.reflect.Array.newInstance(array.getClass()
                        .getComponentType(), size);
        // Keep the collection order by using the sequential view.
        sequential().forEach(new CollectionConsumer<E>() {
            int i;

            @Override
            public void accept(E e, Controller controller) {
                if (i >= result.length)
                    return; // Concurrent add.
                result[i++] = (T) e;
            }
        });
        if (result.length > size) {
            result[size] = null; // As per Collection contract.
        }
        return result;
    }

    /***************************************************************************
     * Misc.
     */

    /** 
     * Executes the specified action on this collection in an atomic manner. As 
     * far as readers of this collection are concerned, either they see the full
     * result of the action executed or nothing.
     * If this collection is not {@link #shared shared} or {@link #parallel 
     * parallel} the specified action is executed directly.
     * 
     * @param action the action to be executed in an atomic manner.
     */
    public void atomic(Runnable action) {
        service().atomic(action);
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
            return (this.size() == that.size()) && containsAll(that);
        } else if (this instanceof List) {
            if (!(obj instanceof List))
                return false;
            List<E> that = (List<E>) obj;
            return Comparators.ARRAY.areEqual(this.toArray(), that.toArray());
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
        if (this instanceof Set) {
            Function<E, Integer> toHash = new Function<E, Integer>() {

                @Override
                public Integer apply(E param) {
                    return (param == null) ? 0 : param.hashCode();
                }
            };
            return mapped(toHash).reduce(Operators.SUM); // Can be performed in parallel.            
        } else if (this instanceof List) {
            final int[] hash = new int[1];
            sequential().forEach(new CollectionConsumer<E>() {

                @Override
                public void accept(E e, Controller controller) {
                    hash[0] = 31 * hash[0] + ((e == null) ? 0 : e.hashCode());
                }
            });
            return hash[0];
        } else {
            return super.hashCode();
        }
    }

    /**
     * Returns a copy of this collection holding a copies of its elements.
     * For lists and sets the copy is {@link #equals} to the original 
     * (the copy of a list is a list and the copy of a set is a set).
     * The iterative order is kept by this operation.
     */
    @Override
    @RealTime(Limit.LINEAR)
    public FastCollection<E> copy() {
        if (this instanceof Set) {
            final FastSet<E> set = new FastSet<E>(service().comparator());
            sequential().forEach(new CollectionConsumer<E>() {
                @SuppressWarnings("unchecked")
                @Override
                public void accept(E e, Controller controller) {
                    E copy = (e instanceof Copyable) ? ((Copyable<E>) e).copy()
                            : e;
                    set.add(copy);
                }
            });
            return set;
        } else { // Returns a list.
            final FastTable<E> list = new FastTable<E>();
            sequential().forEach(new CollectionConsumer<E>() {
                @SuppressWarnings("unchecked")
                @Override
                public void accept(E e, Controller controller) {
                    E copy = (e instanceof Copyable) ? ((Copyable<E>) e).copy()
                            : e;
                    list.add(copy);
                }
            });
            return list;
        }
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
        public Appendable format(FastCollection<Object> that,
                final Appendable dest) throws IOException {
            dest.append('[');
            that.sequential().forEach(new CollectionConsumer<Object>() {
                boolean isFirst = true;

                @Override
                public void accept(Object obj, Controller controller) {
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
                    } catch (IOException error) {
                        throw new RuntimeException(error);
                    }
                }

            });
            return dest.append(']');
        }
    }

}