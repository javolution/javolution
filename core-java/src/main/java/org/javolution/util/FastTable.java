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
import static org.javolution.lang.Realtime.Limit.LOG_N;
import static org.javolution.lang.Realtime.Limit.N_LOG_N;
import static org.javolution.lang.Realtime.Limit.N_SQUARE;

import java.util.Collection;
import java.util.Comparator;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.RandomAccess;

import org.javolution.lang.Parallel;
import org.javolution.lang.Realtime;
import org.javolution.util.function.Consumer;
import org.javolution.util.function.Equality;
import org.javolution.util.function.Function;
import org.javolution.util.function.Predicate;
import org.javolution.util.internal.table.AtomicTableImpl;
import org.javolution.util.internal.table.CustomEqualityTableImpl;
import org.javolution.util.internal.table.MappedTableImpl;
import org.javolution.util.internal.table.ParallelTableImpl;
import org.javolution.util.internal.table.QuickSortImpl;
import org.javolution.util.internal.table.ReversedTableImpl;
import org.javolution.util.internal.table.SharedTableImpl;
import org.javolution.util.internal.table.SubTableImpl;
import org.javolution.util.internal.table.TableIteratorImpl;
import org.javolution.util.internal.table.UnmodifiableTableImpl;

/**
 * <p> A high-performance table (fractal-based) with {@link Realtime strict timing constraints}.</p>
 *     
 * <p> Instances of this class can advantageously replace {@link java.util.ArrayList ArrayList},
 *     {@link java.util.LinkedList LinkedList} or {@link java.util.ArrayDeque ArrayDeque}
 *     in terms of adaptability, space or performance. They inherit all the fast collection views
 *     and support the new {@link #subTable subTable} view over a portion of the table.
 * <pre>{@code
 * FastTable<String> names = FastTable.newTable(); 
 * ...
 * names.sort(Order.LEXICAL_CASE_INSENSITIVE); // Sorts the names in place (different from sorted() which returns a sorted view).
 * names.subTable(0, names.size() / 2).clear(); // Removes the first half of the table (see java.util.List.subList specification).
 * names.filter(str -> str.startsWith("A")).clear(); // Removes all the names starting with "A" (Java 8 notation).
 * names.filter(str -> str.startsWith("A")).parallel().clear(); // Same as above but removal performed concurrently.
 * }</pre></p>
 *
 * <p> As for any {@link FastCollection}, iterations can be performed using closures.
 * <pre>{@code
 * FastTable<Person> persons = ...;
 * Person john = persons.filter(new Predicate<Person>() { 
 *         public boolean test(Person person) {
 *             return (person.getName().equals("John"));
 *         }
 *     }).any();
 * }</pre></p>
 * 
 * <p> The notation is shorter with Java 8.
 * <pre>{@code
 * Person john= persons.filter(person -> person.getName().equals("John")).any();
 * }</pre></p>
 * 
 * <p> Fast tables allows for sorting either through the {@link #sort} method or through the {@link #addSorted} method 
 *     keeping the table sorted. For sorted tables, faster access is provided through the {@link #indexOfSorted} 
 *     method.</p> 
 * 
 * @param <E> the type of table element (can be {@code null})
 * 
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 7.0, September 13, 2015
 */
public abstract class FastTable<E> extends FastCollection<E> implements List<E>, Deque<E>, RandomAccess {

	
    private static final long serialVersionUID = 0x700L; // Version.

    /**
     * Default constructor.
     */
    protected FastTable() {
    }
 
    /**
     * Returns a new high-performance table.
     */
    public static <E> FastTable<E> newTable() {
    	return new FractalTable<E>();
    }

    /**
     * Returns a new high-performance table using the specified equality for elements {@link #equality comparisons}
     * (convenience method).
     */
    public static <E> FastTable<E> newTable(final Equality<? super E> equality) {
        return new FractalTable<E>() {
            private static final long serialVersionUID = FastTable.serialVersionUID;  

            @Override
            public Equality<? super E> equality() {
                return equality;
            }
        };
    }


    ////////////////////////////////////////////////////////////////////////////
    // Views.
    //

    @Override
	public FastTable<E> atomic() {
		return new AtomicTableImpl<E>(this);
	}

    @Override
  	public FastTable<E> equality(Equality<? super E> equality) {
  		return new CustomEqualityTableImpl<E>(this, equality);
  	}

    public <R> FastTable<R> map(Function<? super E, ? extends R> function) {
        return new MappedTableImpl<E, R>(this, function);
    }
    
    @Override
    public FastTable<E> parallel() {
        return new ParallelTableImpl<E>(this);
    }
    
    @Override
	public FastTable<E> reversed() {
		return new ReversedTableImpl<E>(this);
	}

    @Override
    public FastTable<E> sequential() {
        return this;
    }

    @Override
	public FastTable<E> shared() {
		return new SharedTableImpl<E>(this);
	}
    
   /**
     * Returns a view over a portion of the table (equivalent to {@link java.util.List#subList(int, int)}).
     * 
     * @param fromIndex Starting index for a subtable
     * @param toIndex Ending index for a subtable
     * @return Subtable representing the specified range of the parent FastTable
     * @throws IndexOutOfBoundsException - if {@code (fromIndex < 0 || toIndex > size || fromIndex > toIndex)}
     */
    public FastTable<E> subTable(int fromIndex, int toIndex) {
        if ((fromIndex < 0) || (toIndex > size()) || (fromIndex > toIndex)) 
        	 throw new IndexOutOfBoundsException(
                "fromIndex: " + fromIndex + ", toIndex: " + toIndex + ", size(): "
                        + size()); // As per List.subList contract.
        return new SubTableImpl<E>(this, fromIndex, toIndex);
    }

    @Override
	public FastTable<E> unmodifiable() {
		return new UnmodifiableTableImpl<E>(this);
	}

    ////////////////////////////////////////////////////////////////////////////
    // Change in time limit behavior.
    //

	@Override
    @Realtime(limit = LINEAR, comment="For sorted tables indexOfSorted should be used (LOG_N).")
    public boolean contains(Object searched) {
		return indexOf(searched) >= 0;
	}
	
    @Parallel(false)
    @Override
    @Realtime(limit = CONSTANT)
    public boolean isEmpty() {
    	return size() == 0;
    }
    
	@Override
    @Realtime(limit = LINEAR, comment="For sorted tables removeSorted should be used (LOG_N).")
    public boolean remove(Object searched) {
		int i = indexOf(searched);
		if (i < 0) return false;
		remove(i);
		return true;
	}

    @Parallel(false)
	@Override
    @Realtime(limit = CONSTANT)
    public abstract int size();

    @Override
    @Realtime(limit = CONSTANT)
    public Iterator<E> iterator() {
        return listIterator(0);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Iterative methods (optimization).
    //

	@Parallel
    public void forEach(Consumer<? super E> consumer) {
	    for (int i=0, n=size(); i < n;)
            consumer.accept(get(i++));
    }

    @Parallel
    public boolean until(Predicate<? super E> matching) {
        for (int i=0, n=size(); i < n;)
            if (matching.test(get(i++))) return true;
        return false;
    }

    @Parallel
    public boolean removeIf(Predicate<? super E> filter) {
        int j = 0;
        int n = size();
        for (int i=0; i < n; i++) {
            E e = get(i);
            if (filter.test(e)) continue; // Removed (not copied)
            if (i != j) set(j++, e); 
        }
        for (int i=j; i < n; i++) removeLast();
        return j != n;
    }		
	
    ////////////////////////////////////////////////////////////////////////////
    // List Interface.
    //

    @Override
    @Realtime(limit = LOG_N)
    public abstract void add(int index, E element);

    @Override
    @Realtime(limit = N_LOG_N)
    public boolean addAll(int index, Collection<? extends E> that) {
        return subTable(index, index).addAll(that);
    }

    @Override
    @Realtime(limit = LOG_N)
    public abstract E remove(int index);

    @Override
    public abstract E get(int index);

    @Override
    public abstract E set(int index, E element);

    /**
     *  Returns the index of the first occurrence of the specified object in this table, 
     *  or -1 if this table does not contain the element. This methods uses this table
     *  {@link #equality()} to perform the comparison.
     *  
     *  <p> Note: For sorted tables, the method {@link #indexOfSorted} has better limit behavior (in O(Log(n)).</p>
     */
    @Override
    @Realtime(limit = LINEAR, comment="For sorted tables indexOfSorted should be used.")
    public int indexOf(Object searched) {
        @SuppressWarnings("unchecked")
		Equality<Object> cmp = (Equality<Object>) this.equality();
        for (int i = 0, n = size(); i < n; i++) {
            if (cmp.areEqual(searched, get(i))) return i;
        }
        return -1;
    }

    /** 
     * Returns the index of the last occurrence of the specified element in this table,
     * or -1 if this table does not contain the element. This methods uses this table
     * {@link #equality()} to perform the comparison.
     */
    @Override
    @Realtime(limit = LINEAR)
    public int lastIndexOf(Object element) {
        @SuppressWarnings("unchecked")
		Equality<Object> cmp = (Equality<Object>) this.equality();
        for (int i = size() - 1; i >= 0; i--) {
            if (cmp.areEqual(element, get(i))) return i;
        }
        return -1;
    }

    @Override
    public ListIterator<E> listIterator() {
        return listIterator(0);
    }

    @Override
    public ListIterator<E> listIterator(int index) {
    	return new TableIteratorImpl<E>(this, index, size());
    }

    @Override
    @Realtime(limit = LINEAR)
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof List))
            return false;
        @SuppressWarnings("unchecked")
        List<E> list = (List<E>) obj;
        if (size() != list.size())
            return false; // Short-cut.
        Equality<? super E> cmp = Equality.DEFAULT;
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
    }
    
    @Override
    @Realtime(limit = LINEAR)
    public int hashCode() {
        int hash = 0;
        Iterator<E> itr = iterator();
        while (itr.hasNext()) {
            E e = itr.next();
            hash += 31 * hash + ((e != null) ? e.hashCode() : 0);
        }
        return hash;
    }
    
    @Override
    @Parallel
    public FastTable<E> collect() {
         final FastTable<E> reduction = FastTable.newTable(equality());
         forEach(new Consumer<E>() {
            @Override
            public void accept(E param) {
                synchronized (reduction) {
                    add(param);
                }
            }});
         return reduction;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Deque Interface.
    //

    @Override
    public void addFirst(E element) {
        add(0, element);
    }

    @Override
    public void addLast(E element) {
        add(size(), element);
    }

    @Override
    public E getFirst() {
        if (isEmpty()) emptyError();
        return get(0);
    }

    @Override
    public E getLast() {
        if (isEmpty()) emptyError();
        return get(size() - 1);
    }

    @Override
    public E peekFirst() {
        return (isEmpty()) ? null : getFirst();
    }

    @Override
    public E peekLast() {
        return isEmpty() ? null : getLast();
    }

    @Override
    public E pollFirst() {
        return isEmpty() ? null : removeFirst();
    }

    @Override
    public E pollLast() {
        return isEmpty() ? null : removeLast();
    }

    @Override
    public E removeFirst() {
        if (isEmpty()) emptyError();
        return remove(0);
    }

    @Override
    public E removeLast() {
        if (isEmpty()) emptyError();
        return remove(size() - 1);
    }

    @Override
    public final boolean offerFirst(E e) {
        addFirst(e);
        return true;
    }

    @Override
    public final boolean offerLast(E e) {
        addLast(e);
        return true;
    }

    @Override
    @Realtime(limit = LINEAR, comment="LOG_N for sorted tables.")
    public boolean removeFirstOccurrence(Object o) {
        int i = indexOf(o);
        if (i < 0) return false;
        remove(i);
        return true;
    }

    @Override
    @Realtime(limit = LINEAR, comment="LOG_N for sorted tables.")
    public boolean removeLastOccurrence(Object o) {
        int i = lastIndexOf(o);
        if (i < 0) return false;
        remove(i);
        return true;
    }

    @Override
    public final boolean offer(E e) {
        return offerLast(e);
    }

    @Override
    public final E remove() {
        return removeFirst();
    }

    @Override
    public final E poll() {
        return pollFirst();
    }

    @Override
    public final E element() {
        return getFirst();
    }

    @Override
    public final E peek() {
        return peekFirst();
    }

    @Override
    public final void push(E e) {
        addFirst(e);
    }

    @Override
    public final E pop() {
        return removeFirst();
    }

    @Override
    public Iterator<E> descendingIterator() {
        return this.reversed().iterator();
    }

    ////////////////////////////////////////////////////////////////////////////
    // Misc.
    //
 
    /** 
     * Inserts all of the elements of the specified collection into this table keeping them sorted.
     * 
     * @param elements the elements to add
     * @param cmp the comparator used for sorting
     * @return {@code true} if this collection is modified; {@code false} otherwise
     */
    @Realtime(limit = N_LOG_N)
    public boolean addAllSorted(Collection<? extends E> elements, Comparator<? super E> cmp) {
        for (E e : elements) addSorted(e, cmp);
        return !elements.isEmpty();
    }

    /**
     * Assuming the table is sorted according to the specified comparator; inserts the specified element at the proper 
     * index and returns that index.
     */
    @Realtime(limit = LOG_N)
	public int addSorted(E element, Comparator<? super E> cmp) {
    	int i = indexOfSorted(element, cmp);
    	if (i < 0) i = -(i+1);
    	add(i, element);
    	return i;
    }

    /**
     * Assuming the table is sorted according to the specified comparator; remove the specified element and returns
     * its previous index or a negative number (see {@link #indexOfSorted}) if the element is not present.
     */
    @Realtime(limit = LOG_N)
    public int removeSorted(E element, Comparator<? super E> cmp) {
        int i = indexOfSorted(element, cmp);
        if (i >= 0) remove(i);
        return i;
    }

    /**
     * Assuming the table is sorted according to the specified comparator; returns the index of the specified element 
     * or a negative number {@code n} such as {@code -(n+1)} would be the insertion index of the element keeping 
     * this table sorted.
     */
    @Realtime(limit = LOG_N)
	public int indexOfSorted(Object obj, Comparator<? super E> cmp) {
        return indexOfSorted(obj, cmp, 0, size());
    }

    @Realtime(limit = LINEAR)
	public abstract FastTable<E> clone();
	
    /**
     * Sorts this table in place (quick sort).
     */
    @Realtime(limit = N_SQUARE)
    public void sort(Comparator<? super E> cmp) {
        QuickSortImpl<E> qs = new QuickSortImpl<E>(this, cmp);
        qs.sort();
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public FastTable<E>[] trySplit(int n) {
        // Split into n subTables
        FastTable<E>[] split = new FastTable[n];        
        for (int i=0, from=0, size =size(), incr = size / n, rem = size % n; i < n; i++) {
            int to = from + incr;
            if (rem-- > 0) to++; 
            split[i] = new SubTableImpl<E>(this, from, to, true /* Read-Only */);
            from = to;
        }
        return split;
    }
        
    /**
     * Replaced by {@link #subTable(int, int)}. The term "List" for an interface with random access is disturbing !
     */
    @Override
    @Deprecated
    public final FastTable<E> subList(int fromIndex, int toIndex) {
        return subTable(fromIndex, toIndex);
    }

    /** Throws NoSuchElementException. */
    protected void emptyError() {
        throw new NoSuchElementException("Empty Table");
    }

    /** Throws IndexOutOfBoundsException. */
    protected void indexError(int index) {
        throw new IndexOutOfBoundsException("index: " + index + ", size: "
                + size());
    }
 
    /** In sorted table find the real or "would be" index of the specified element in the given range. */
	private int indexOfSorted(Object obj, Comparator<? super E> cmp, int start, int length) {
        if (length == 0) return -(start+1);
        int half = length >> 1;
        @SuppressWarnings("unchecked")
		int test = cmp.compare((E)obj, get(start + half)); // Cast has no effect.
        if (test == 0) return start + half; // Found.
        return (test < 0) ? indexOfSorted(obj, cmp, start, half) :
             indexOfSorted(obj, cmp, start + half + 1, length - half - 1);
    }

}