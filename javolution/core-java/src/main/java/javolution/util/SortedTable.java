/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util;

import static javolution.lang.Realtime.Limit.LOG_N;
import static javolution.lang.Realtime.Limit.N_LOG_N;

import java.util.Collection;
import java.util.Comparator;

import javolution.lang.Realtime;
import javolution.util.function.Equality;
import javolution.util.function.Order;

/**
 * <p> A specialized version of the fractal-based table with fast 
 *     search capabilities.</p>
 *     
 * <p> Unlike {@link FastSet}, sorted tables allow for elements 
 *     duplications and support simple comparator-based ordering.</p>
 *     
 * <p> Any operation which inserts or sets elements at fixed location
 *     will throw  {@link UnsupportedOperationException}.</p>
 *     
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 7.0, September 13, 2015
 */
public class SortedTable<E> extends FractalTable<E> {


    private static final long serialVersionUID = 0x700L; // Version. 
    /**
     * Returns a new table holding the same elements as the specified 
     * collection (convenience method).
     * 
     * @param <E> Element Type
     * @param that the collection holding the elements to copy.
     * @return the table containing the elements specified in the collection
     */
    public static <E> SortedTable<E> of(Collection<? extends E> that) {
    	SortedTable<E> table = new SortedTable<E>();
    	table.addAll(that);
        return table;
    }
  
    /**
     * Returns a new table holding the same elements as the specified 
     * array (convenience method).
     * 
     * @param <E> Element Type
     * @param elements Elements to place in the table
     * @return the table containing the specified elements
     */
    public static <E> SortedTable<E> of(@SuppressWarnings("unchecked") E... elements) {
    	SortedTable<E> table = new SortedTable<E>();
    	for (E e : elements) table.add(e);
        return table;
    }
    
    private final Comparator<? super E> comparator;

    /**
     * Creates an empty sorted table (fractal-based) keeping its elements 
     * sorted according to their natural order.
     */
    public SortedTable() {
    	this(Order.NATURAL);
    }
 
    /**
     * Creates an empty sorted table (fractal-based) keeping its elements 
     * sorted according to the specified comparator.
     */
    public SortedTable(Comparator<? super E> comparator) {
    	this.comparator = comparator;
    }
    
    /**
     * Inserts this element at its proper location based upon this table
     * order.
     */
 	@Override
    @Realtime(limit = LOG_N)
	public boolean add(E element) {
        int i = locationOf(element);
        add((i < 0) ? -i-1 : i, element);
        return true;
    }

    /**
     * Throws {@link UnsupportedOperationException}
     */
	@Deprecated
	@Override
	public void add(int index, E element) {
		throw new UnsupportedOperationException();
	}

    @Override
    @Realtime(limit = N_LOG_N)
	public boolean addAll(Collection<? extends E> that) {
		return super.addAll(that);
	}

    /**
     * Throws {@link UnsupportedOperationException}
     */
	@Deprecated
	@Override
	public boolean addAll(int index, Collection<? extends E> elements) {
		throw new UnsupportedOperationException();
	}
 
    /**
     * Throws {@link UnsupportedOperationException}
     */
	@Deprecated
	@Override
	public void addFirst(E element) {
		throw new UnsupportedOperationException();
	}
 
    /** 
     * Adds the specified element only if not already present.
     *  
     * @return {@code true} if the element has been added; 
     *         {@code false} otherwise.
     */
    @Realtime(limit = LOG_N)
    public boolean addIfAbsent(E element) {
        int i = locationOf(element);
        if (i >= 0) return false;
        add(-i-1, element);
        return true;
    }

    /**
     * Throws {@link UnsupportedOperationException}
     */
	@Deprecated
	@Override
	public void addLast(E element) {
		throw new UnsupportedOperationException();
	}

	private void append(E element) {
		super.addLast(element);
	}

    @Override
	public SortedTable<E> clone() {
        SortedTable<E> copy = new SortedTable<E>(comparator);
        for (int i=0, n = size(); i < n; i++)
           copy.append(get(i));
        return copy;
	}

    /**
     * Returns the comparator used for ordering.
     */
	public Comparator<? super E> comparator() {
		return comparator;
	}

    @Override
    @Realtime(limit = LOG_N)
	public boolean contains(Object searched) {
     	int i = locationOf(searched);
        return (i >= 0);
 	}

	@Override
    @Realtime(limit = LOG_N)
	public boolean containsAll(Collection<?> that) {
		for (Object e : that) {
			if (!contains(e))
				return false;
		}
		return true;
	}
	
	/**
     * Returns the equality based upon this table {@link #comparator()
     * comparator}.
     */
	@SuppressWarnings("unchecked")
	@Override
	public Equality<? super E> equality() {
		return (comparator instanceof Equality) ?
				(Equality<E>) comparator : new Equality<E>() {

					@Override
					public boolean areEqual(E left, E right) {
						return comparator.compare(left, right) == 0;
					}};
	}

	@Override
    @Realtime(limit = LOG_N)
    public int indexOf(Object element) {
		int i = locationOf(element);
        return (i >= 0) ? i : -1;
    }

	@SuppressWarnings("unchecked")
	@Override
    @Realtime(limit = LOG_N)
    public int lastIndexOf(Object o) {
     	int i = locationOf(o);
        if (i < 0) return -1;
        while ((++i < size()) && (comparator.compare((E) o, get(i)) == 0)) {
        }
        return --i;
    }

    /** 
     * Returns the index of the specified element if present; or a negative 
     * number equals to {@code -n} with {@code n} being the index next to 
     * the "would be" index of the specified element if the specified element 
     * was to be added.
     */
    @Realtime(limit = LOG_N)
    private int locationOf(Object searched) {
        return locationOf(searched, 0, size());
    }

    /** Search location of specified element in the given range */
	@SuppressWarnings("unchecked")
	private int locationOf(Object element, int start, int length) {
        if (length == 0) return -start - 1;
        int half = length >> 1;
        int cmp = comparator.compare((E)element, get(start + half));
        if (cmp == 0) return start + half; // Found.
        if (cmp < 0) return locationOf(element, start, half);
        return locationOf(element, start + half + 1, length - half - 1);
    }

	@Override
    @Realtime(limit = LOG_N)
	public boolean remove(Object searched) {
	    int i = locationOf(searched);
	    if (i < 0) return false;
	    remove(i);
	    return true;
	}

	@Override
    @Realtime(limit = LOG_N)
	public boolean removeFirstOccurrence(Object o) {
		return super.removeFirstOccurrence(o);
	}

	@Override
	@Realtime(limit = LOG_N)
	public boolean removeLastOccurrence(Object o) {
		return super.removeLastOccurrence(o);
	}

    /**
     * Throws {@link UnsupportedOperationException}
     */
	@Deprecated
	@Override
	public E set(int index, E element) {
		throw new UnsupportedOperationException();
	}

    /**
     * Throws {@link UnsupportedOperationException}
     */
	@Deprecated
	@Override
	public void sort(Comparator<? super E> cmp) {
		throw new UnsupportedOperationException();
	}
 
}