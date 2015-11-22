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
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javolution.context.ConcurrentContext;
import javolution.lang.Parallelizable;
import javolution.lang.Realtime;
import javolution.text.Cursor;
import javolution.text.DefaultTextFormat;
import javolution.text.TextContext;
import javolution.text.TextFormat;
import javolution.util.function.Consumer;
import javolution.util.function.Equality;
import javolution.util.function.Function;
import javolution.util.function.BinaryOperator;
import javolution.util.function.Order;
import javolution.util.function.Predicate;
import javolution.util.internal.collection.AtomicCollectionImpl;
import javolution.util.internal.collection.CustomEqualityCollectionImpl;
import javolution.util.internal.collection.DistinctCollectionImpl;
import javolution.util.internal.collection.FilteredCollectionImpl;
import javolution.util.internal.collection.LinkedCollectionImpl;
import javolution.util.internal.collection.MappedCollectionImpl;
import javolution.util.internal.collection.ParallelCollectionImpl;
import javolution.util.internal.collection.ReversedCollectionImpl;
import javolution.util.internal.collection.SharedCollectionImpl;
import javolution.util.internal.collection.SortedCollectionImpl;
import javolution.util.internal.collection.UnmodifiableCollectionImpl;

/**
 * <p> A high-performance collection with documented 
 *     {@link Realtime real-time} behavior.</p>
 * 
 * <p> Instances of this class support numerous views which can be chained:
 * <ul>
 * <li>{@link #atomic} - Thread-safe view for which all reads are mutex-free and
 * collection updates (e.g. {@link #addAll addAll}, {@link #removeIf removeIf})
 * are atomic.</li>
 * <li>{@link #shared} - Thread-safe view based on <a href=
 * "http://en.wikipedia.org/wiki/Readers%E2%80%93writer_lock">readers-writer
 * locks</a>.</li>
 * <li>{@link #unmodifiable} - View which does not allow for modifications.</li>
 * <li>{@link #filter filter(Predicate)} - View exposing only the elements
 * matching the specified filter.</li>
 * <li>{@link #map map(Function)} - View exposing elements through the specified
 * mapping function.</li>
 * <li>{@link #sorted(Comparator)} - View exposing elements sorted according to
 * the specified comparator.</li>
 * <li>{@link #reversed} - View exposing elements in the reverse iterative
 * order.</li>
 * <li>{@link #distinct} - View exposing each element only once.</li>
 * <li>{@link #linked} - View exposing each element sorted based on their 
 *     [@link {@link #add insertion} order.</li>
 * <li>{@link #using(Equality)} - View using the specified comparator to test
 * for equality (e.g. {@link #contains}, {@link #remove}, {@link #distinct},
 * ...)</li>
 * <li>{@link #parallel} - View allowing parallel processing of bulk operations
 * (e.g. {@link #forEach}, {@link #removeIf}, {@link #reduce},
 * {@link #removeAll}, ...)</li>
 * </ul>
 * </p>
 * <p> In general, the chaining order does matter!
 * <pre>{@code 
 * FastCollection<String> elements ...;
 *      
 * // Parallel processing.
 * elements.using(Equality.IDENTITY).parallel().removeAll(ConstantTable.of("Eva Poré"));
 *      
 * // Sequential, using(Equality) is not a parallel view.  
 * elements.parallel().using(Equality.IDENTITY).removeAll(ConstantTable.of("Eva Poré"));
 * 
 * // Thread-safe view.
 * elements.distinct().shared();
 * 
 * // Not a thread-safe view.
 * elements.shared().distinct();
 * }</pre></p>
 * 
 * <p> It should be noted that {@link #unmodifiable Unmodifiable} views <b>are not
 *     immutable</b>;constant/immutable collections can only be obtained through
 *     class specializations (e.g. {@link ConstantTable}, {@link ConstantSet}...)
 * <pre>{@code
 * // Immutable collection.
 * ConstantTable<String> winners = ConstantTable.of("John Deuff", "Otto Graf", "Sim Kamil").using(Equality.LEXICAL_CASE_INSENSITIVE);
 * }</pre></p>
 * 
 * <p> Views are similar to <a
 *     href="http://lambdadoc.net/api/java/util/stream/package-summary.html">
 *     Java 8 streams</a> except that views are themselves collections and 
 *     support actions which may impact the original collection. 
 *     Collection views are nothing "new" since they already existed in the 
 *     original java.util collection classes (e.g. List.subList(...), 
 *     Map.keySet(), Map.values()). Javolution extends to this concept and 
 *     allows views to be chained in order to address the issue of class
 *     proliferation.
 * <pre>{@code
 * FastTable<String> names = ...;
 * names.subTable(0, n).clear(); // Removes the n first names (see java.util.List.subList).
 * names.distinct().add("Guy Liguili"); // Adds "Guy Liguili" only if not already present.
 * names.filter(s -> s.length > 16).clear(); // Removes all the persons with long names.
 * names.filter(s -> s.length > 16).parallel().clear(); // Same as above but performed concurrently !
 * ...
 * }</pre></p>
 * 
 * <p> Views can of course be used to perform "stream" oriented filter-map-reduce
 *     operations with the same benefits: Parallelism support, excellent memory
 *     characteristics (no caching, cost nothing to create), etc.
 * <pre>{@code 
 * String anyLongName = names.filter(s -> s.length > 16).any(); // Returns any long name.
 * int maxLength = names.map(s -> s.length).parallel().max(); // Finds the maximum length in parallel.
 * int sumLength = names.map(s -> s.length).parallel().reduce((x,y)-> x + y); // Calculates the sum in parallel.
 * 
 * // JDK Class.getEnclosingMethod using Javolution's views and Java 8 (to be compared with the current 20 lines implementation !).
 * Method matching = FastTable.of(enclosingInfo.getEnclosingClass().getDeclaredMethods())
 *     .filter(m -> Objects.equals(m.getName(), enclosingInfo.getName())
 *     .filter(m -> Arrays.equals(m.getParameterTypes(), parameterClasses))
 *     .filter(m -> Objects.equals(m.getReturnType(), returnType)).reduce(Operators.ANY); 
 * if (matching == null) throw new InternalError("Enclosing method not found");
 * return matching;
 * }</pre></p>
 * 
 * <p> If the collection is {@link #parallel parallel}, the default implementation
 *     uses {@link ConcurrentContext} to dispatch parallel processing. Sequential
 *     views ({@link #sequential sequential} and {@link #sorted sorted}) always
 *     perform closure operations in sequential order (same order as iterators).
 * <pre>{@code
 * FastTable<Runnable> tasks = ...;
 * tasks.parallel().forEach(task -> task.run()); // Execute concurrently each task and wait for their completion to continue.
 * names.sorted().reversed().forEach(str -> System.out.println(str)); // Prints names in reverse alphabetical order (even if names is parallel())
 * }</pre></p>
 * 
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 7.0, September 13, 2015
 */
@Realtime
@DefaultTextFormat(FastCollection.Format.class)
public abstract class FastCollection<E> implements Collection<E>, Serializable,
		Cloneable {

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
	 * Returns an atomic view over this collection. All operations that write or
	 * access multiple elements in the collection (such as {@code addAll(),
	 * retainAll()}) are atomic. All read operations are mutex-free.
	 * 
	 * @return the atomic view.
	 */
	@Parallelizable(mutexFree = true, comment = "Except for write operations, all read operations are mutex-free.")
	public FastCollection<E> atomic() {
		return new AtomicCollectionImpl<E>(this);
	}

	/**
	 * Returns a thread-safe view over this collection. The shared view allows
	 * for concurrent read as long as there is no writer. The default
	 * implementation is based on <a href=
	 * "http://en.wikipedia.org/wiki/Readers%E2%80%93writer_lock">
	 * readers-writers locks</a> giving priority to writers.
	 * 
	 * @return the shared view.
	 */
	@Parallelizable(mutexFree = false, comment = "Use multiple-readers/single-writer lock.")
	public FastCollection<E> shared() {
		return new SharedCollectionImpl<E>(this);
	}

	/**
	 * Returns a view allowing bulk collection operations to be performed
	 * {@link javolution.context.ConcurrentContext in parallel} internally using
	 * {@link ConcurrentContext}.
	 * 
	 * <p>
	 * Note: This view is not thread-safe for external threads.
	 * </p>
	 * 
	 * @return the parallel view.
	 * @see #forEach
	 * @see #removeIf
	 * @see #reduce
	 * @see #any
	 * @see #min
	 * @see #max
	 * @see #size
	 * @see #clear
	 * @see #removeAll
	 * @see #retainAll
	 */
	public FastCollection<E> parallel() {
		return new ParallelCollectionImpl<E>(this);
	}

	/**
	 * Returns an unmodifiable view over this collection. Any attempt to modify
	 * the collection through this view will result into a
	 * {@link java.lang.UnsupportedOperationException} being raised.
	 * 
	 * @return the unmodifiable view.
	 */
	public FastCollection<E> unmodifiable() {
		return new UnmodifiableCollectionImpl<E>(this);
	}

	/**
	 * Returns a view exposing only the elements matching the specified filter.
	 * Adding elements not matching the specified filter has no effect. If this
	 * collection is initially empty, using a filtered view to add new elements
	 * ensures that this collection has only elements satisfying the filter
	 * predicate.
	 * 
	 * @param filter
	 *            the filter predicate.
	 * @return the filtered view.
	 */
	public FastCollection<E> filter(Predicate<? super E> filter) {
		return new FilteredCollectionImpl<E>(this, filter);
	}

	/**
	 * Returns a view exposing elements through the specified mapping function.
	 * The returned view does not allow new elements to be added.
	 * 
	 * @param function
	 *            the mapping function.
	 * @return the mapped view.
	 */
	public <R> FastCollection<R> map(Function<? super E, ? extends R> function) {
		return new MappedCollectionImpl<E, R>(this, function);
	}

	/**
	 * Returns a view exposing its elements sorted according to the specified
	 * comparator.
	 * 
	 * @param comparator
	 *            the comparator used for sorting.
	 * @return the sorted view.
	 */
	public FastCollection<E> sorted(Comparator<? super E> comparator) {
		return new SortedCollectionImpl<E>(this, comparator);
	}

	/**
	 * Returns a view exposing elements sorted according to the elements natural
	 * order (convenience method).
	 * 
	 * @return {@code sorted(Equalities.NATURAL)}
	 * @throws ClassCastException
	 *             if this collection's elements do not implement
	 *             {@link Comparable}.
	 * @see #sorted(Comparator)
	 * @see Order#NATURAL
	 */
	public FastCollection<E> sorted() {
		return sorted(Order.NATURAL);
	}

	/**
	 * Returns a view exposing elements in reversed iterative order.
	 * 
	 * @return reversed view.
	 */
	public FastCollection<E> reversed() {
		return new ReversedCollectionImpl<E>(this);
	}

	/**
	 * Returns a view exposing only distinct elements. It does not iterate twice
	 * over the {@link #equality() same} elements even when iterations are
	 * performed in {@link #parallel() in parallel}. Adding elements already
	 * present has no effect. If this collection is initially empty, using a
	 * distinct view to add new elements ensures that this collection has no
	 * duplicate element.
	 * 
	 * @return the view having only distinct elements.
	 */
	public FastCollection<E> distinct() {
		return new DistinctCollectionImpl<E>(this);
	}

	/**
	 * Returns a view keeping track of the insertion order and exposing 
	 * elements in that order (first added, first to iterate).
	 * This view can be useful for compatibility with Java linked collections
	 * (e.g. {@code LinkedHashSet}). Any element not added through this 
	 * view is ignored.
	 * 
	 * @return a view maintaining insertion order.
	 */
	public FastCollection<E> linked() {
		return new LinkedCollectionImpl<E>(this);
	}

	/**
	 * Returns a view using the specified comparator for element equalities.
	 * 
	 * @param equality
	 *            the equality to use for element comparisons.
	 * @return a view using the specified custom equality.
	 * @see #contains
	 * @see #containsAll
	 * @see #remove
	 * @see #removeAll
	 * @see #retainAll
	 * @see #distinct
	 */
	public FastCollection<E> using(Equality<? super E> equality) {
		return new CustomEqualityCollectionImpl<E>(this, equality);
	}

	// //////////////////////////////////////////////////////////////////////////
	// Closure operations.
	//

	/**
	 * Iterates over all this collection elements applying the specified
	 * consumer. Iterations may be performed concurrently if this collection is
	 * {@link #parallel parallel}.
	 * 
	 * @param consumer
	 *            the functional consumer applied to the collection elements.
	 */
	@Realtime(limit = LINEAR)
	public void forEach(Consumer<? super E> consumer) {
		for (Iterator<E> itr = iterator(); itr.hasNext();)
			consumer.accept(itr.next());
	}

	/**
	 * Removes from this collection all the elements matching the specified
	 * functional predicate. Removals may be performed concurrently if this
	 * collection is {@link #parallel parallel}.
	 * 
	 * @param filter
	 *            a predicate returning {@code true} for elements to be removed.
	 * @return {@code true} if at least one element has been removed;
	 *         {@code false} otherwise.
	 */
	@Realtime(limit = LINEAR)
	public boolean removeIf(Predicate<? super E> filter) {
		boolean removed = false;
		for (Iterator<E> itr = iterator(); itr.hasNext();) {
			if (filter.test(itr.next())) {
				removed = true;
				itr.remove();
			}	
		}
		return removed;
	}

	/**
	 * Performs a reduction by applying the specified operator on all the
	 * elements of this collection. Reduction may be performed concurrently if
	 * this collection is {@link #parallel parallel} (in which case the binary
	 * operator needs to be associative). If this collection is empty this
	 * method returns {@code null}.
	 * 
	 * @param operator
	 *            the binary operator applied to the collection elements.
	 * @return the result of the reduction or {@code null} if the collection is
	 *         empty.
	 */
	@Realtime(limit = LINEAR)
	public E reduce(BinaryOperator<E> operator) {
		Iterator<E> itr = iterator();
		E result = null;
		while (itr.hasNext()) {
			E next = itr.next();
			if (next == null) continue;
			result = (result != null) ? 
					operator.apply(result, next) : next;
		}
		return result;
	}

	/**
	 * Returns any {@code non-null} elements through reduction (convenience
	 * method). 
	 * The search may be performed concurrently if this collection
	 * is {@link #parallel parallel}.
	 * 
	 * @return {@code reduce((x,y) -> x == null ? y : x)}
	 */
	@Realtime(limit = LINEAR)
	@SuppressWarnings("unchecked")
	public E any() {
		return reduce((BinaryOperator<E>) ANY);
	}

	private static final BinaryOperator<Object> ANY = new BinaryOperator<Object>() {

		@Override
		public Object apply(Object first, Object second) {
			return first == null ? second : first;
		}
	};

	/**
	 * Returns the greatest element of this collection according to its 
	 * natural order (convenience method).
	 * The search may be performed concurrently if this collection
	 * is {@link #parallel parallel}.
	 * 
	 * @return {@code reduce((x,y) -> Equalities.NATURAL.compare(x, y) > 0 ? x : y)} 
	 * @throws ClassCastException if any element of this collection do not
	 *         implement {@link Comparable}
	 * @see Order#NATURAL
	 */
	@Realtime(limit = LINEAR)
	@SuppressWarnings("unchecked")
	public E max() {
		return reduce((BinaryOperator<E>)MAX);
	}

	private static final BinaryOperator<Object> MAX = new BinaryOperator<Object>() {

		@Override
		public Object apply(Object first, Object second) {
			return Order.NATURAL.compare(first, second) > 0 ? first : second;
		}
	};

	/**
	 * Returns the smallest element of this collection according to its 
	 * natural order (convenience method).
	 * The search may be performed concurrently if this collection
	 * is {@link #parallel parallel}.
	 * 
	 * @return {@code reduce((x,y) -> Equalities.NATURAL.compare(x, y) < 0 ? x : y)} 
	 * @throws ClassCastException if any element of this collection do not
	 *         implement {@link Comparable}
	 * @see Order#NATURAL
	 */
	@Realtime(limit = LINEAR)
	@SuppressWarnings("unchecked")
	public E min() {
		return reduce((BinaryOperator<E>)MIN);
	}

	private static final BinaryOperator<Object> MIN = new BinaryOperator<Object>() {

		@Override
		public Object apply(Object first, Object second) {
			return Order.NATURAL.compare(first, second) < 0 ? first : second;
		}
	};

	// //////////////////////////////////////////////////////////////////////////
	// Collection operations.
	//

	/** Adds the specified element to this collection. */
	@Override
	@Realtime(limit = CONSTANT)
	public abstract boolean add(E element);

	/** Indicates if this collection is empty. */
	@Override
	@Realtime(limit = LINEAR, comment = "Could iterate the whole collection (e.g. filtered view).")
	public boolean isEmpty() {
		return !iterator().hasNext();
	}

	/**
	 * Returns the size of this collection. The size may be calculated
	 * concurrently if this collection is {@link #parallel() parallel} (e.g.
	 * parallel view over filtered collection).
	 * 
	 * @return {@code map(x -> 1).reduce((x,y) -> x + y)}
	 */
	@Override
	@Realtime(limit = LINEAR, comment = "Could count the elements (e.g. filtered view).")
	public int size() {
		// map(x -> 1).reduce((x,y) -> x + y);
		return map(TO_ONE).reduce(SUM);
	}

	private static final Function<Object, Integer> TO_ONE = new Function<Object, Integer>() {

		@Override
		public Integer apply(Object param) {
			return 1;
		}
	};
	private static final BinaryOperator<Integer> SUM = new BinaryOperator<Integer>() {

		@Override
		public Integer apply(Integer first, Integer second) {
			return first + second;
		}
	};

	/**
	 * Removes all elements from this collection. The removal may be performed
	 * concurrently if this collection is {@link #parallel() parallel} (e.g.
	 * parallel view over filtered collection).
	 * 
	 * @return {@code removeIf(Predicate.TRUE)}
	 */
	@Override
	@Realtime(limit = LINEAR, comment = "Could remove the elements one at a time.")
	public void clear() {
		removeIf(Predicate.TRUE);
	}

	/**
	 * Indicates if this collection contains the specified element.
	 */
	@SuppressWarnings("unchecked")
	@Override
	@Realtime(limit = LINEAR, comment = "Could search the whole collection.")
	public boolean contains(Object searched) {
		Equality<? super E> cmp = equality();
		for (Iterator<E> itr = iterator(); itr.hasNext();)
			if (cmp.areEqual((E) searched, itr.next()))
				return true;
		return false;
	}

	/**
	 * Removes a single instance of the specified element from this collection.
	 */
	@SuppressWarnings("unchecked")
	@Override
	@Realtime(limit = LINEAR, comment = "Could search the whole collection.")
	public boolean remove(Object searched) {
		Equality<? super E> cmp = equality();
		for (Iterator<E> itr = iterator(); itr.hasNext();)
			if (cmp.areEqual((E) searched, itr.next())) {
				itr.remove();
				return true;
			}
		return false;
	}

	/**
	 * Returns a fast iterator over this collection's elements. This iterator
	 * allows {@link FastIterator#split splitting}.
	 */
	@Override
	public abstract FastIterator<E> iterator();

	/**
	 * Adds all the elements of the specified collection to this collection.
	 */
	@Override
	@Realtime(limit = LINEAR)
	public boolean addAll(Collection<? extends E> that) {
		boolean changed = false;
		Iterator<? extends E> it = that.iterator();
		while (it.hasNext()) {
			if (add(it.next()))
				changed = true;
		}
		return changed;
	}

	/** Indicates if this collection contains all the specified elements. */
	@Override
	@Realtime(limit = N_SQUARE)
	public boolean containsAll(Collection<?> that) {
		for (Object e : that) {
			if (!contains(e))
				return false;
		}
		return true;
	}

	/**
	 * Removes all the specified element from this collection. The removal may
	 * be performed concurrently if this collection is {@link #parallel()
	 * parallel}.
	 */
	@Override
	@Realtime(limit = N_SQUARE)
	public boolean removeAll(final Collection<?> that) {
		final AtomicBoolean changed = new AtomicBoolean(false);
		removeIf(new Predicate<E>() {

			@Override
			public boolean test(E param) {
				if (that.contains(param)) {
					changed.set(true);
					return true;
				}
				return false;
			}
		});
		return changed.get();
	}

	/**
	 * Removes all the elements except those in the specified collection. The
	 * removal may be performed concurrently if this collection is
	 * {@link #parallel() parallel}.
	 */
	@Override
	@Realtime(limit = N_SQUARE)
	public boolean retainAll(final Collection<?> that) {
		final AtomicBoolean changed = new AtomicBoolean(false);
		removeIf(new Predicate<E>() {

			@Override
			public boolean test(E param) {
				if (!that.contains(param)) {
					changed.set(true);
					return true;
				}
				return false;
			}
		});
		return changed.get();
	}

	/** Returns an array holding this collection elements. */
	@Override
	@Realtime(limit = LINEAR)
	public Object[] toArray() {
		return toArray(EMPTY_ARRAY);
	}

	private final static Object[] EMPTY_ARRAY = new Object[0];

	/**
	 * Returns the specified array holding this collection elements if enough
	 * capacity.
	 */
	@SuppressWarnings("unchecked")
	@Override
	@Realtime(limit = LINEAR)
	public <T> T[] toArray(final T[] array) {
		final int size = size();
		final T[] result = (size <= array.length) ? array
				: (T[]) java.lang.reflect.Array.newInstance(array.getClass()
						.getComponentType(), size);
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
	// Misc.
	//

	/**
	 * Returns the element equality for this collection.
	 * 
	 * @see #using(Equality)
	 */
	@Realtime(limit = CONSTANT)
	public abstract Equality<? super E> equality();

	/**
	 * Returns a copy of this collection; updates of the copy should not impact
	 * the original.
	 */
	@Realtime(limit = LINEAR)
	public abstract FastCollection<E> clone();

	/**
	 * Compares the specified object with this collection for equality. This
	 * method follows the {@link Collection#equals(Object)} specification
	 * regardless of this collection element {@link #equality equality}.
	 * 
	 * @param obj
	 *            the object to be compared for equality with this collection
	 * @return <code>true</code> if this collection is considered equals to the
	 *         one specified; <code>false</code> otherwise.
	 */
	@SuppressWarnings("unchecked")
	@Override
	@Realtime(limit = LINEAR)
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (this instanceof Set) {
			if (!(obj instanceof Set))
				return false;
			Set<E> set = (Set<E>) obj;
			return (size() == set.size()) && containsAll(set);
		} else if (this instanceof List) {
			if (!(obj instanceof List))
				return false;
			List<E> list = (List<E>) obj;
			if (size() != list.size())
				return false; // Short-cut.
			Equality<? super E> cmp = Equality.STANDARD;
			Iterator<E> it1 = this.iterator();
			Iterator<E> it2 = list.iterator();
			while (it1.hasNext()) {
				if (!it2.hasNext())
					return false;
				if (!cmp.areEqual(it1.next(), it2.next()))
					return false;
			}
			if (it2.hasNext())
				return false;
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Returns the hash code of this collection. This method always follows the
	 * {@link Collection#hashCode()} specification..
	 * 
	 * @return this collection hash code.
	 */
	@Override
	@Realtime(limit = LINEAR)
	public int hashCode() {
		Iterator<E> it = this.iterator();
		int hash = 0;
		if (this instanceof Set) {
			while (it.hasNext()) {
				hash += Objects.hashCode(it.next());
			}
		} else if (this instanceof List) {
			while (it.hasNext()) {
				hash += 31 * hash + Objects.hashCode(it.next());
			}
		} else {
			hash = super.hashCode();
		}
		return hash;
	}

	/**
	 * Returns the string representation of this collection using its default
	 * {@link TextFormat format}.
	 * 
	 * @see TextContext
	 */
	@Override
	@Realtime(limit = LINEAR)
	public String toString() {
		return TextContext.getFormat(FastCollection.class).format(this);
	}

	/**
	 * Default text format for fast collections (parsing not supported).
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
}