/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util.internal.table.sorted;

import java.util.Comparator;

import javolution.util.function.Equalities;
import javolution.util.function.Equality;
import javolution.util.internal.comparator.WrapperComparatorImpl;
import javolution.util.internal.table.FastTableImpl;

/**
 * The default {@link javolution.util.FastSortedTable FastSortedTable} implementation.
 */
public class FastSortedTableImpl<E> extends SortedTableView<E>  {

    private static final long serialVersionUID = 0x600L; // Version.
    private final FastTableImpl<E> table = new FastTableImpl<E>(Equalities.STANDARD);
    private final Comparator<? super E> comparator;

	public FastSortedTableImpl(Comparator<? super E> comparator) {
    	super(null);
    	this.comparator = comparator;    	
    }

    @Override
	public void add(int index, E element) {
		table.add(index, element);
	}

    @Override
	public void clear() {
		table.clear();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Equality<? super E> comparator() {
		return (Equality<? super E>) ((comparator instanceof Equality) ? comparator : 
        	new  WrapperComparatorImpl<E>(comparator));
	}

	@Override
	public E get(int index) {
		return table.get(index);
	}

	@Override
    public int positionOf(E element) {
        return positionOf(element, 0, size());
    }

	private int positionOf(E element, int start, int length) {
        if (length == 0) return -start - 1;
        int half = length >> 1;
        int cmp = comparator.compare(element, get(start + half));
        if (cmp == 0) return start + half; // Found.
        if (cmp < 0) return positionOf(element, start, half);
        return positionOf(element, start + half + 1, length - half - 1);
    }

	@Override
	public E remove(int index) {
		return table.remove(index);
	}

	@Override
	public E set(int index, E element) {
		return table.set(index, element);
    }

	@Override
	public int size() {
		return table.size();
	}

}
