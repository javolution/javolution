/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util;

import static javolution.lang.Realtime.Limit.CONSTANT;
import static javolution.lang.Realtime.Limit.LINEAR;
import static javolution.lang.Realtime.Limit.N_SQUARE;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;

import javolution.lang.Immutable;
import javolution.lang.Parallelizable;
import javolution.lang.Realtime;
import javolution.text.Cursor;
import javolution.text.DefaultTextFormat;
import javolution.text.TextContext;
import javolution.text.TextFormat;
import javolution.util.function.Consumer;
import javolution.util.function.Equalities;
import javolution.util.function.Equality;
import javolution.util.function.Function;
import javolution.util.function.Predicate;
import javolution.util.function.Reducer;
import javolution.util.function.Reducers;
import javolution.util.internal.collection.AtomicCollectionImpl;
import javolution.util.internal.collection.ComparatorCollectionImpl;
import javolution.util.internal.collection.DistinctCollectionImpl;
import javolution.util.internal.collection.FilteredCollectionImpl;
import javolution.util.internal.collection.MappedCollectionImpl;
import javolution.util.internal.collection.ParallelCollectionImpl;
import javolution.util.internal.collection.ReversedCollectionImpl;
import javolution.util.internal.collection.SequentialCollectionImpl;
import javolution.util.internal.collection.SharedCollectionImpl;
import javolution.util.internal.collection.SortedCollectionImpl;
import javolution.util.internal.collection.UnmodifiableCollectionImpl;
import javolution.util.service.CollectionService;

/**
 * <p> A closure-based collection supporting numerous views which can be chained.
 * <ul>
 *    <li>{@link #atomic} - Thread-safe view for which all reads are mutex-free 
 *    and collection updates (including {@link #addAll addAll}, {@link #removeIf removeIf}} are atomic.</li>
 *    <li>{@link #shared} - Thread-safe view using allowing concurrent reads based 
 *    on mutex (<a href="http://en.wikipedia.org/wiki/Readers%E2%80%93writer_lock">
 *    readers-writer locks).</li>
 *    <li>{@link #parallel} - A view allowing parallel processing including {@link #update updates}.</li>
 *    <li>{@link #sequential} - View disallowing parallel processing.</li>
 *    <li>{@link #unmodifiable} - View which does not allow any modification.</li>
 *    <li>{@link #filtered filtered(filter)} - View exposing only the elements matching the specified filter.</li>
 *    <li>{@link #mapped mapped(function)} - View exposing elements through the specified mapping function.</li>
 *    <li>{@link #comparator(Comparator) comparator(comparator)} - View using the specified comparator for element equality and order.</li>
 *    <li>{@link #sorted sorted()} - View exposing elements sorted according to the collection comparator.</li>
 *    <li>{@link #reversed} - View exposing elements in the reverse iterative order.</li>
 *    <li>{@link #distinct} - View exposing each element only once.</li>
 * </ul></p>
 * 
 * <p> Unmodifiable collections are not always immutable. An {@link javolution.lang.Immutable immutable}. 
 *     reference (or const reference) can only be {@link #toImmutable() obtained} when the originator  
 *     guarantees that the collection source will not be modified even by himself 
 *     (the value of the immutable reference being an {@link #unmodifiable unmodifiable} collection).
 * [code]
 * Immutable<List<String>> winners 
 *     = new FastTable<String>().addAll("John Deuff", "Otto Graf", "Sim Kamil").toImmutable();
 *     // Immutability is guaranteed, no reference left on the collection source.
 * [/code]</p>
 * 
 * <p> Atomic collections use <a href="http://en.wikipedia.org/wiki/Copy-on-write">Copy-On-Write</a> 
 *     optimizations in order to provide mutex-free read access. Only writes operations are mutually 
 *     exclusive. Collections can be optimized to not require the full copy during write operations
 *     (e.g. immutable parts don't need to be copied). Still, when multiple updates are performed,
 *     it is beneficial to group them into one single {@link #update update} operation.
 * [code]
 * FastTable<String> tokens = new FastTable<String>().atomic();
 * ...
 * // Replace null with "" in tokens. If tokens is atomic the update is atomic.
 * // If tokens is parallel, the update is also performed concurrently !
 * tokens.update(new Consumer<List<String>>() {  
 *     public void accept(List<String> view) {
 *         for (int i=0, n = view.size(); i < n; i++)
 *             if (view.get(i) == null) view.set(i, "");
 *         }
 *     }
 * });[/code]</p>
 * <p> The same code using closure (Java 8).
 * [code]
 *  tokens.update((List<String> view) -> {
 *      for (int i = 0, n = view.size(); i < n; i++) {
 *          if (view.get(i) == null) view.set(i, "");
 *      }
 *  });[/code]</p>
 * 
 * <p> Views are similar to <a href="http://lambdadoc.net/api/java/util/stream/package-summary.html">
 *     Java 8 streams</a> except that views are themselves collections (virtual collections)
 *     and actions on a view can impact the original collection. Collection views are nothing "new" 
 *     since they already existed in the original java.util collection classes (e.g. List.subList(...),
 *     Map.keySet(), Map.values()). Javolution extends to this concept and allows views to be chained 
 *     which addresses the concern of class proliferation.</p> 
 * [code]
 * FastTable<String> names = new FastTable<String>().addAll("Oscar Thon", "Eva Poret", "Paul Auchon");
 * boolean found = names.comparator(Equalities.LEXICAL_CASE_INSENSITIVE).contains("LUC SURIEUX"); 
 * names.subTable(0, n).clear(); // Removes the n first names (see java.util.List.subList).
 * names.distinct().add("Guy Liguili"); // Adds "Guy Liguili" only if not already present.
 * names.filtered(isLong).clear(); // Removes all the persons with long names.
 * names.filtered(isLong).parallel().clear(); // Same as above but performed concurrently !
 * ...
 * Predicate<CharSequence> isLong = new Predicate<CharSequence>() { 
 *     public boolean test(CharSequence csq) {
 *         return csq.length() > 16; 
 *     }
 * });[/code]</p>
 *    
 * <p> Views can of course be used to perform "stream" oriented filter-map-reduce operations with the same benefits:
 *     Parallelism support, excellent memory characteristics (no caching and cost nothing to create), etc.
 * [code]
 * String anyLongName = names.filtered(isLong).any(String.class); // Returns any long name.
 * int nbrChars = names.mapped(toLength).reduce(Reducers.sum()); // Returns the total number of characters.
 * int maxLength = names.mapped(toLength).parallel().max(); // Finds the longest name in parallel.
 * ...
 * Function<CharSequence, Integer> toLength = new Function<CharSequence, Integer>() {
 *    public Integer apply(CharSequence csq) {
 *        return csq.length(); 
 *    }
 * });
 *    
 * // JDK Class.getEnclosingMethod using Javolution's views and Java 8 (to be compared with the current 20 lines implementation !).
 * Method matching = new FastTable<Method>().addAll(enclosingInfo.getEnclosingClass().getDeclaredMethods())
 *     .filtered(m -> Equalities.STANDARD.areEqual(m.getName(), enclosingInfo.getName())
 *     .filtered(m -> Equalities.ARRAY.areEqual(m.getParameterTypes(), parameterClasses))
 *     .filtered(m -> Equalities.STANDARD.areEqual(m.getReturnType(), returnType))
 *     .any(Method.class);
 * if (matching == null) throw new InternalError("Enclosing method not found");
 * return matching;[/code]</p>
 *           
 * <p> If a collection (or a map) is shared, derived views are also thread-safe.
 *     Similarly, if a collection is {@link #parallel parallel}, closure-based iterations 
 *     on derived views are performed concurrently.
 * [code]
 * FastTable<Person> persons = new FastTable<Person>().parallel();
 * ...
 * // Since persons is parallel, the search is done concurrently.
 * Person john = persons.filtered(new Predicate<Person>() { 
 *     public boolean test(Person person) {
 *         return person.getName().equals("John");
 *     }
 * }).any(Person.class);[/code]</p>
 * 
 * <p> With Java 8, closures are greatly simplified using lambda expressions.
 * [code]
 * Person john = persons.filtered(person -> person.getName().equals("John")).any(Person.class); // Same as above.
 * tasks.parallel().forEach(task -> task.run());
 * names.sorted().reversed().forEach(str -> System.out.println(str)); // Prints names in reverse alphabetical order. 
 * [/code]</p>
 *     
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0, July 21, 2013
 */
@Realtime
@DefaultTextFormat(FastCollection.Format.class)
public abstract class FastCollection<E> implements Collection<E>, Serializable {

    private static final long serialVersionUID = 0x600L; // Version.

    /**
     * Default constructor.
     */
    protected FastCollection() {}

    ////////////////////////////////////////////////////////////////////////////
    // Views.
    //

    /**
     * Returns an atomic view over this collection. All operations that write 
     * or access multiple elements in the collection (such as addAll(), 
     * retainAll()) are atomic. 
     * Iterators on atomic collections are <b>thread-safe</b> 
     * (no {@link ConcurrentModificationException} possible).
     */
    @Parallelizable(mutexFree = true, comment = "Except for write operations, all read operations are mutex-free.")
    public FastCollection<E> atomic() {
        return new AtomicCollectionImpl<E>(service());
    }

    /**
     * Returns a thread-safe view over this collection. The shared view
     * allows for concurrent read as long as there is no writer. 
     * The default implementation is based on <a href=
     * "http://en.wikipedia.org/wiki/Readers%E2%80%93writer_lock">
     * readers-writers locks</a> giving priority to writers. 
     * Iterators on shared collections are <b>thread-safe</b> 
     * (no {@link ConcurrentModificationException} possible).
     */
    @Parallelizable(mutexFree = false, comment = "Use multiple-readers/single-writer lock.")
    public FastCollection<E> shared() {
        return new SharedCollectionImpl<E>(service());
    }

    /** 
     * Returns a parallel collection. Closure-based operations are 
     * performed {@link javolution.context.ConcurrentContext in parallel} 
     * on {@link CollectionService#split sub-views} of this collection.
     * The number of parallel views is equals to  
     * {@link javolution.context.ConcurrentContext#getConcurrency() 
     * concurrency}{@code + 1}.
     * 
     * @see #perform(Consumer)
     * @see #update(Consumer)
     * @see #forEach(Consumer)
     * @see #removeIf(Predicate)
     * @see #reduce(Reducer)
     */
    public FastCollection<E> parallel() {
        return new ParallelCollectionImpl<E>(service());
    }

    /** 
     * Returns a sequential view of this collection. Using this view, 
     * all closure-based iterations are performed sequentially.
     */
    public FastCollection<E> sequential() {
        return new SequentialCollectionImpl<E>(service());
    }

    /**
     * Returns an unmodifiable view over this collection. Any attempt to 
     * modify the collection through this view will result into 
     * a {@link java.lang.UnsupportedOperationException} being raised.
     */
    public FastCollection<E> unmodifiable() {
        return new UnmodifiableCollectionImpl<E>(service());
    }

    /** 
     * Returns a view exposing only the elements matching the specified 
     * filter.  Adding elements not matching the specified filter has 
     * no effect. If this collection is initially empty, using a filtered
     * view to add new elements ensure that this collection has only elements
     * satisfying the filter predicate.
     */
    public FastCollection<E> filtered(Predicate<? super E> filter) {
        return new FilteredCollectionImpl<E>(service(), filter);
    }

    /** 
     * Returns a view exposing elements through the specified mapping function.
     * The returned view does not allow new elements to be added.
     */
    public <R> FastCollection<R> mapped(
            Function<? super E, ? extends R> function) {
        return new MappedCollectionImpl<E, R>(service(), function);
    }

    /** 
     * Returns a view using the specified comparator for element equality 
     * and sorting.
     * 
     * @see #contains(Object)
     * @see #remove(Object)
     * @see #sorted()
     * @see #distinct() 
     */
    public FastCollection<E> comparator(Comparator<? super E> cmp) {
        return new ComparatorCollectionImpl<E>(service(), comparator());
    }

    /** 
     * Returns a view exposing elements sorted according to this 
     * collection {@link #comparator()}.
     * 
     * @see #comparator()
     * @see #comparator(Comparator)
     */
    public FastCollection<E> sorted() {
        return new SortedCollectionImpl<E>(service());
    }

    /** 
     * Returns a view exposing elements sorted according to the specified 
     * comparator (convenience method).
     * 
     * @return {@code comparator(cmp).sorted()}
     */
    public FastCollection<E> sorted(Comparator<? super E> cmp) {
        return comparator(cmp).sorted();
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
     * in the collection through this view has no effect. If this collection is 
     * initially empty, using a distinct view to add new elements ensures that
     * this collection has no duplicate.  
     */
    public FastCollection<E> distinct() {
        return new DistinctCollectionImpl<E>(service());
    }

    ////////////////////////////////////////////////////////////////////////////
    // Closure operations.
    //

    /** 
     * Executes the specified read action on this collection.
     * That logic may be performed concurrently (on parallel views) 
     * if this collection is {@link #parallel() parallel}.
     * 
     *    
     * @param action the read-only action.
     * @throws ClassCastException if the action type is not compatible with 
     *         this collection (e.g. action on set and this is a list). 
     * @see #update(Consumer)
     */
    @SuppressWarnings("unchecked")
    @Realtime(limit = LINEAR)
    public void perform(Consumer<? extends Collection<E>> action) {
        service().perform((Consumer<CollectionService<E>>) action, service());
    }

    /** 
     * Executes the specified update action on this collection. 
     * For {@link #atomic() atomic} collections the update is atomic 
     * (either concurrent readers see the full result of the action or
     * nothing).
     * The update may be performed concurrently (through parallel views)
     * if this collection is {@link #parallel() parallel}.
     *    
     * @param action the update action.
     * @throws ClassCastException if the action type is not compatible with 
     *         this collection (e.g. action on a {@link java.util.Set Set} 
     *         and this is a {@link java.util.List List}). 
     * @see #perform(Consumer)
     */
    @SuppressWarnings("unchecked")
    @Realtime(limit = LINEAR)
    public void update(Consumer<? extends Collection<E>> action) {
        service().update((Consumer<CollectionService<E>>) action, service());
    }

    /** 
     * Iterates over all this collection elements applying the specified 
     * consumer (convenience method). Iterations are performed concurrently 
     * if the collection is {@link #parallel() parallel}.
     * 
     * @param consumer the functional consumer applied to the collection elements.
     */
    @Realtime(limit = LINEAR)
    public void forEach(final Consumer<? super E> consumer) {
        perform(new Consumer<Collection<E>>() {
            public void accept(Collection<E> view) {
                Iterator<E> it = view.iterator();
                while (it.hasNext()) {
                    consumer.accept(it.next());
                }
            }
        });
    }

    /**
     * Removes from this collection all the elements matching the specified
     * functional predicate (convenience method). Removals are performed 
     * concurrently if this collection is {@link #parallel() parallel} and 
     * atomically if this collection is {@link #atomic() atomic}.
     * 
     * @param filter a predicate returning {@code true} for elements to be removed.
     * @return {@code true} if at least one element has been removed;
     *         {@code false} otherwise.
     */
    @Realtime(limit = LINEAR)
    public boolean removeIf(final Predicate<? super E> filter) {
        final boolean[] removed = new boolean[1];
        update(new Consumer<Collection<E>>() {
            public void accept(Collection<E> view) {
                Iterator<E> it = view.iterator();
                while (it.hasNext()) {
                    if (filter.test(it.next())) {
                        it.remove(); // Ok mutable iteration.
                        removed[0] = true;
                    }
                }
            }
        });
        return removed[0];
    }

    /** 
     * Performs a reduction of the elements of this collection using the 
     * specified reducer. This may not involve iterating  over all the 
     * collection elements, for example the reducers: {@link Reducers#any},
     * {@link Reducers#and} and {@link Reducers#or} may stop iterating 
     * early. Reduction is performed concurrently if this collection is 
     * {@link #parallel() parallel}. 
     *    
     * @param reducer the collection reducer.
     * @return the reduction result.
     * @see #any(Class)
     * @see #min()
     * @see #max()
     */
    @Realtime(limit = LINEAR)
    public E reduce(Reducer<E> reducer) {
        perform(reducer);
        return reducer.get();
    }

    ////////////////////////////////////////////////////////////////////////////
    // Collection operations.
    //

    /** Adds the specified element to this collection */
    @Override
    @Realtime(limit = LINEAR, comment = "Could iterate the whole collection (e.g. distinct view).")
    public boolean add(E element) {
        return service().add(element);
    }

    /** Indicates if this collection is empty. */
    @Override
    @Realtime(limit = LINEAR, comment = "Could iterate the whole collection (e.g. filtered view).")
    public boolean isEmpty() {
        return iterator().hasNext();
    }

    /** Returns the size of this collection. */
    @Override
    @Realtime(limit = LINEAR, comment = "Could count the elements (e.g. filtered view).")
    public int size() {
        return service().size();
    }

    /** Removes all elements from this collection. */
    @Override
    @Realtime(limit = LINEAR, comment = "Could remove the elements one at a time.")
    public void clear() {
        service().clear();
    }

    /** Indicates if this collection contains the specified element. */
    @Override
    @Realtime(limit = LINEAR, comment = "Could search the whole collection.")
    public boolean contains(Object searched) {
        return service().contains(searched);
    }

    /**  Removes the specified element from this collection.*/
    @Override
    @Realtime(limit = LINEAR, comment = "Could search the whole collection.")
    public boolean remove(Object searched) {
        return service().remove(searched);
    }

    /**
     * Returns an iterator over this collection elements. For 
     * shared/atomic collections the iterator is immune to 
     * concurrent modifications. In other words the elements iterated over
     * may or may not reflect the current state of the collection.
     */
    @Override
    @Realtime(limit = N_SQUARE, comment = "Sorted view iterator require sorting the elements.")
    public Iterator<E> iterator() {
        return service().iterator();
    }

    /** Adds all the specified elements to this collection. */
    @Override
    @Realtime(limit = LINEAR)
    public boolean addAll(final Collection<? extends E> that) {
        return service().addAll(that);
    }

    /** Indicates if this collection contains all the specified elements. */
    @Override
    @Realtime(limit = N_SQUARE)
    public boolean containsAll(Collection<?> that) {
        return service().containsAll(that);
    }

    /** Removes all the specified element from this collection. */
    @Override
    @Realtime(limit = N_SQUARE)
    public boolean removeAll(final Collection<?> that) {
        return service().removeAll(that);
    }

    /** Removes all the elements except those in the specified collection. */
    @Override
    @Realtime(limit = N_SQUARE)
    public boolean retainAll(final Collection<?> that) {
        return service().retainAll(that);
    }

    /** Returns an array holding this collection elements. */
    @Override
    @Realtime(limit = LINEAR)
    public Object[] toArray() {
        return service().toArray();
    }

    /** 
     * Returns the specified array holding this collection elements if 
     * enough capacity. 
     */
    @Override
    @Realtime(limit = LINEAR)
    public <T> T[] toArray(final T[] array) {
        return service().toArray(array);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Misc.
    //

    /**
     * Returns any non-null element of the specified type (convenience method).
     * The search is performed concurrently if this collection is 
     * {@link #parallel() parallel}.
     * 
     * @param type the element type searched for.
     * @return {@code reduce(Reducers.any(type))}
     * @see Reducers#any
     */
    @SuppressWarnings("unchecked")
    @Realtime(limit = LINEAR)
    public <T extends E> T any(Class<T> type) {
        return (T) reduce((Reducer<E>) Reducers.any(type));
    }

    /**
     * Returns the smallest element of this collection using this 
     * collection {@link #comparator() comparator} (convenience method).
     * Returns {@code null} if this collection is empty. 
     * The search is performed concurrently if this collection is 
     * {@link #parallel() parallel}.
     * 
     * @return {@code reduce(Reducers.min(comparator()))}
     * @see Reducers#min
     */
    @Realtime(limit = LINEAR)
    public E min() {
        return reduce((Reducer<E>)Reducers.min(comparator())); // Cast only necessary on JDK 1.6 !
    }

    /**
     * Returns the largest element of this collection using this 
     * collection {@link #comparator() comparator} (convenience method). 
     * Returns {@code null} if this collection is empty. 
     * The search is performed concurrently if this collection is 
     * {@link #parallel() parallel}.
      * 
     * @return {@code reduce(Reducers.max(comparator()))}
     * @see Reducers#max
     */
    @Realtime(limit = LINEAR)
    public E max() {
        return reduce((Reducer<E>)Reducers.max(comparator())); // Cast only necessary on JDK 1.6 !
    }

    /**
     * Returns this collection with the specified element added. 
     * 
     * @param elements the elements to be added.
     * @return {@code this}
     */
    @Realtime(limit = LINEAR)
    public FastCollection<E> addAll(E... elements) {
        for (E e : elements) {
            add(e);
        }
        return this;
    }

    /**
     * Returns this collection with the specified collection's elements added
     * in sequence. 
     */
    @Realtime(limit = LINEAR)
    public FastCollection<E> addAll(FastCollection<? extends E> that) {
        addAll((Collection<? extends E>) that);
        return this;
    }

    /** 
     * Returns the comparator uses by this collection for equality and/or 
     * ordering if this collection is sorted.
     */
    @Realtime(limit = CONSTANT)
    public Equality<? super E> comparator() {
        return service().comparator();
    }

    /** 
     * Returns an immutable reference over this collection. The immutable 
     * value is an {@link #unmodifiable() unmodifiable} view of this collection.
     * The caller must guarantees that the original collection is never going 
     * to be updated (e.g. there is no reference left on the original collection).
     */
    @Realtime(limit = CONSTANT)
    public <T extends Collection<E>> Immutable<T> toImmutable() {
        return new Immutable<T>() {
            @SuppressWarnings("unchecked")
            final T value = (T) unmodifiable();

            @Override
            public T value() {
                return value;
            }
        };
    }

    /**
     * Compares the specified object with this collection for equality.
     * This method follows the {@link Collection#equals(Object)} specification 
     * if this collection {@link #comparator comparator} is 
     * {@link Equalities#STANDARD} (default). Otherwise, only collections
     * using the same comparator can be considered equals.  
     * 
     * @param obj the object to be compared for equality with this collection
     * @return <code>true</code> if both collections are considered equals;
     *        <code>false</code> otherwise. 
     */
    @SuppressWarnings("rawtypes")
	@Override
    @Realtime(limit = LINEAR)
    public boolean equals(Object obj) {
    	if (obj instanceof FastCollection) { // Compare services.
    		return service().equals(((FastCollection)obj).service());
    	}
        return service().equals(obj); // Default Collection equality.
    }

    /**
     * Returns the hash code of this collection.
     * This method follows the {@link Collection#hashCode()} specification 
     * if this collection {@link #comparator comparator} is 
     * {@link Equalities#STANDARD}.
     *    
     * @return this collection hash code. 
     */
    @Override
    @Realtime(limit = LINEAR)
    public int hashCode() {
        return service().hashCode();
    }

    /** 
     * Returns the string representation of this collection using its 
     * default {@link TextFormat format}.
     * 
     * @see TextContext
     */
    @Override
    @Realtime(limit = LINEAR)
    public String toString() {
        return TextContext.getFormat(FastCollection.class).format(this);
    }

    /**
     * Returns the service implementation of this collection (for sub-classes).
     */
    protected abstract CollectionService<E> service();

    /**
     * Returns the service implementation of any fast collection 
     * (for sub-classes).
     */
    protected static <E> CollectionService<E> serviceOf(
            FastCollection<E> collection) {
        return collection.service();
    }

    ////////////////////////////////////////////////////////////////////////////
    // Textual format.
    //

    /**
     * Default text format for fast collections (parsing not supported). 
     * It is the format used when printing standard {@code java.util.Collection} 
     * instances except that elements are written using 
     * their current {@link TextContext TextContext} format.
     */
    @Parallelizable
    public static class Format extends TextFormat<FastCollection<?>> {

        @Override
        public FastCollection<Object> parse(CharSequence csq, Cursor cursor)
                throws IllegalArgumentException {
            throw new UnsupportedOperationException();
        }

        @Override
        public Appendable format(FastCollection<?> that, final Appendable dest)
                throws IOException {
            dest.append('[');
            Class<?> elementType = null;
            TextFormat<Object> format = null;
            for (Object element : that) {
                if (elementType == null) elementType = Void.class; 
                else dest.append(", "); // Not the first.
                if (element == null) {
                    dest.append("null");
                    continue;
                }
                Class<?> cls = element.getClass();
                if (elementType.equals(cls)) {
                    format.format(element, dest);
                    continue;
                }
                elementType = cls;
                format = TextContext.getFormat(cls);
                format.format(element, dest);
            }
            return dest.append(']');
        }

    }

}