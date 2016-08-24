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
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.RandomAccess;

import org.javolution.lang.Realtime;
import org.javolution.util.function.Equality;
import org.javolution.util.internal.table.AtomicTableImpl;
import org.javolution.util.internal.table.CustomEqualityTableImpl;
import org.javolution.util.internal.table.QuickSortImpl;
import org.javolution.util.internal.table.ReversedTableImpl;
import org.javolution.util.internal.table.SharedTableImpl;
import org.javolution.util.internal.table.SubTableImpl;
import org.javolution.util.internal.table.TableIteratorImpl;
import org.javolution.util.internal.table.UnmodifiableTableImpl;

/**
 * <p> A high-performance table (fractal-based) with {@link Realtime strict 
 *     timing constraints}.</p>
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
 * <p> As for any {@link FastCollection}, iterations can be 
 *     performed using closures.
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
 * <p> Fast tables allows for sorting either through the {@link #sort} method
 *     or through the {@link #addSorted} method keeping the table sorted.
 *     For sorted tables, faster access is provided through 
 *     the {@link #indexOfSorted} method.</p> 
 * 
 * @param <E> the type of table element (can be {@code null})
 * 
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 7.0, September 13, 2015
 */
public abstract class FastTable<E> extends FastCollection<E> implements List<E>,
        Deque<E>, RandomAccess {

	
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
     * Returns a new high-performance table using the specified equality
     * comparator for its elements.
     */
    public static <E> FastTable<E> newTable(Equality<? super E> equality) {
    	return new FractalTable<E>().equality(equality);
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

    @Override
	public FastTable<E> parallel() {
		return null;
	}

   @Override
	public FastTable<E> reversed() {
		return new ReversedTableImpl<E>(this);
	}

   @Override
	public FastTable<E> shared() {
		return new SharedTableImpl<E>(this);
	}
    
    /**
     * Returns a view over a portion of the table (equivalent to 
     * {@link java.util.List#subList(int, int)}).
     * @param fromIndex Starting index for a subtable
     * @param toIndex Ending index for a subtable
     * @return Subtable representing the specified range of the parent FastTable
     * @throws IndexOutOfBoundsException - if {@code (fromIndex < 0 ||
     *         toIndex > size || fromIndex > toIndex)}

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
    @Realtime(limit = CONSTANT)
    public abstract boolean add(E element);

	@Override
    @Realtime(limit = CONSTANT)
    public abstract void clear();

	@Override
    @Realtime(limit = LINEAR, comment="For sorted tables indexOfSorted should be used.")
    public boolean contains(Object searched) {
		return indexOf(searched) >= 0;
	}
	
    @Override
    @Realtime(limit = CONSTANT)
    public boolean isEmpty() {
    	return size() == 0;
    }
    
	@Override
    @Realtime(limit = LINEAR)
    public boolean remove(Object searched) {
		int i = indexOf(searched);
		if (i < 0) return false;
		remove(i);
		return true;
	}

	@Override
    @Realtime(limit = CONSTANT)
    public abstract int size();
		
    ////////////////////////////////////////////////////////////////////////////
    // List Interface.
    //

    /** 
     * Inserts the specified element at the specified position in this table. 
     */
    @Override
    @Realtime(limit = LOG_N)
    public abstract void add(int index, E element);

    /** Inserts all of the elements in the specified collection into this table
     *  at the specified position. */
    @Override
    @Realtime(limit = N_LOG_N)
    public boolean addAll(int index, Collection<? extends E> elements) {
        return subTable(index, index).addAll(elements);
    }

    /** Removes the element at the specified position in this table. */
    @Override
    @Realtime(limit = LOG_N)
    public abstract E remove(int index);

    /** Returns the element at the specified position in this table. */
    @Override
    @Realtime(limit = CONSTANT)
    public abstract E get(int index);

    /** 
     * Replaces the element at the specified position in this table with the specified element.
     *  
     */
    @Override
    @Realtime(limit = CONSTANT)
    public abstract E set(int index, E element);

    /**
     *  Returns the index of the first occurrence of the specified object 
     *  in this table, or -1 if this table does not contain the element.
     *  
     *  <p> Note: For sorted tables, the method {@link #indexOfSorted} has 
     *      better limit behavior (in O(Log(n)).</p> 
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

    /** Returns the index of the last occurrence of the specified element in this table,
     * or -1 if this table does not contain the element. */
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

    /** Returns an iterator over the elements in this table. */
    @Override
    @Realtime(limit = CONSTANT)
	public Iterator<E> iterator() {
		return new Iterator<E>() {
			int nextIndex = 0;
			int size = size();

			@Override
			public boolean hasNext() {
				return nextIndex < size;
			}

			@Override
			public E next() {
				if (nextIndex >= size)
					throw new NoSuchElementException();
				return get(nextIndex++);
			}};
	}

    /** Returns a list iterator over the elements in this table. */
    @Override
    @Realtime(limit = CONSTANT)
    public ListIterator<E> listIterator() {
        return listIterator(0);
    }

    /** Returns a list iterator over the elements in this table, starting 
     * at the specified position in the table. */
    @Override
    @Realtime(limit = CONSTANT)
    public ListIterator<E> listIterator(int index) {
    	return new TableIteratorImpl<E>(this, index, size());
    }

    ////////////////////////////////////////////////////////////////////////////
    // Deque Interface.
    //

    /** Inserts the specified element at the front of this table. */
    @Override
    @Realtime(limit = CONSTANT)
    public void addFirst(E element) {
        add(0, element);
    }

    /** Inserts the specified element at the end of this table. */
    @Override
    @Realtime(limit = CONSTANT)
    public void addLast(E element) {
        add(size(), element);
    }

    /** Retrieves, but does not remove, the first element of this table. */
    @Override
    @Realtime(limit = CONSTANT)
    public E getFirst() {
        if (isEmpty()) emptyError();
        return get(0);
    }

    /** Retrieves, but does not remove, the last element of this table. */
    @Override
    @Realtime(limit = CONSTANT)
    public E getLast() {
        if (isEmpty()) emptyError();
        return get(size() - 1);
    }

    /** Retrieves, but does not remove, the first element of this table, 
     * or returns null if this table is empty. */
    @Override
    @Realtime(limit = CONSTANT)
    public E peekFirst() {
        return (isEmpty()) ? null : getFirst();
    }

    /** Retrieves, but does not remove, the last element of this table,
     *  or returns null if this table is empty. */
    @Override
    @Realtime(limit = CONSTANT)
    public E peekLast() {
        return isEmpty() ? null : getLast();
    }

    /** Retrieves and removes the first element of this table, 
     * or returns null if this table is empty. */
    @Override
    @Realtime(limit = CONSTANT)
    public E pollFirst() {
        return isEmpty() ? null : removeFirst();
    }

    /** Retrieves and removes the last element of this table, 
     * or returns null if this table is empty. */
    @Override
    @Realtime(limit = CONSTANT)
    public E pollLast() {
        return isEmpty() ? null : removeLast();
    }

    /** Retrieves and removes the last element of this table, 
     * or returns null if this table is empty. */
    @Override
    @Realtime(limit = CONSTANT)
    public E removeFirst() {
        if (isEmpty()) emptyError();
        return remove(0);
    }

    /** Retrieves and removes the last element of this table. */
    @Override
    @Realtime(limit = CONSTANT)
    public E removeLast() {
        if (isEmpty()) emptyError();
        return remove(size() - 1);
    }

    /** Inserts the specified element at the front of this table. */
    @Override
    @Realtime(limit = CONSTANT)
    public final boolean offerFirst(E e) {
        addFirst(e);
        return true;
    }

    /** Inserts the specified element at the end of this table. */
    @Override
    @Realtime(limit = CONSTANT)
    public final boolean offerLast(E e) {
        addLast(e);
        return true;
    }

    /** Removes the first occurrence of the specified element from this table. */
    @Override
    @Realtime(limit = LINEAR, comment="LOG_N for sorted tables.")
    public boolean removeFirstOccurrence(Object o) {
        int i = indexOf(o);
        if (i < 0) return false;
        remove(i);
        return true;
    }

    /** Removes the last occurrence of the specified element from this table. */
    @Override
    @Realtime(limit = LINEAR, comment="LOG_N for sorted tables.")
    public boolean removeLastOccurrence(Object o) {
        int i = lastIndexOf(o);
        if (i < 0) return false;
        remove(i);
        return true;
    }

    /** Inserts the specified element into the queue represented by this table. */
    @Override
    @Realtime(limit = CONSTANT)
    public final boolean offer(E e) {
        return offerLast(e);
    }

    /** Retrieves and removes the head of the queue represented by this table. */
    @Override
    @Realtime(limit = CONSTANT)
    public final E remove() {
        return removeFirst();
    }

    /** Retrieves and removes the head of the queue represented by this table. */
    @Override
    @Realtime(limit = CONSTANT)
    public final E poll() {
        return pollFirst();
    }

    /** Retrieves, but does not remove, the head of the queue represented by this table. */
    @Override
    @Realtime(limit = CONSTANT)
    public final E element() {
        return getFirst();
    }

    /** Retrieves, but does not remove, the head of the queue represented by this table. */
    @Override
    @Realtime(limit = CONSTANT)
    public final E peek() {
        return peekFirst();
    }

    /** Pushes an element onto the stack represented by this table. */
    @Override
    @Realtime(limit = CONSTANT)
    public final void push(E e) {
        addFirst(e);
    }

    /** Pops an element from the stack represented by this table. */
    @Override
    @Realtime(limit = CONSTANT)
    public final E pop() {
        return removeFirst();
    }

    /** Returns an iterator over the elements in this table in reverse sequential order. */
    @Override
    @Realtime(limit = CONSTANT)
    public Iterator<E> descendingIterator() {
        return this.reversed().iterator();
    }

    ////////////////////////////////////////////////////////////////////////////
    // Misc.
    //

	@Override
	public FastTable<E> all() {
		return (FastTable<E>) super.all();
	}

	@Override
	public ConstantTable<E> constant() {
		int size = size();
		@SuppressWarnings("unchecked")
		E[] elements = (E[]) new Object[size];
		for (int i=0; i < size; i++)
			elements[i] = get(i); 
		return new ConstantTable<E>(elements, equality());
	}
	
	@Override
    public FastTable<E> addAll(E first, @SuppressWarnings("unchecked") E... others) {
		super.addAll(first, others);
		return this;
	}

	/**
     * Assuming the table is sorted according to the specified comparator;
     * inserts the specified element at the proper index and returns that index.
     */
    @Realtime(limit = LOG_N)
	public int addSorted(E element, Comparator<? super E> comparator) {
    	int i = indexOfSorted(element, comparator);
    	if (i < 0) i = -(i+1);
    	add(i, element);
    	return i;
    }

    /**
     * Assuming the table is sorted according to the specified comparator;
     * returns the index of the specified element or a negative number 
     * {@code n} such as {@code -(n+1)} would be the insertion index of 
     * the element keeping this table sorted.
     */
    @Realtime(limit = LOG_N)
	public int indexOfSorted(Object obj, Comparator<? super E> comparator) {
        return indexOfSorted(obj, comparator, 0, size());
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

    /**
     * Replaced by  {@link #subTable(int, int)}. The term "List" for an 
     * interface with random access is disturbing !
     */
    @Override
    @Deprecated
    public final FastTable<E> subList(int fromIndex, int toIndex) {
        return subTable(fromIndex, toIndex);
    }

    /** Throws NoSuchElementException */
    protected void emptyError() {
        throw new NoSuchElementException("Empty Table");
    }

    /** Throws IndexOutOfBoundsException */
    protected void indexError(int index) {
        throw new IndexOutOfBoundsException("index: " + index + ", size: "
                + size());
    }
 
    /** In sorted table find the real or "would be" index of the specified
     *  element in the given range */
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