/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.util;

import org.javolution.util.function.Equality;

/**
 * <p> The default fractal-based implementation of {@link FastTable} based upon high-performance 
 *     {@link FractalArray}.</p> 
 *     
 * <p> The fractal-based implementation ensures that element add/insertion/deletion operations 
 *     execution time is almost independent of the table size. For comparison,
 *     {@code ArrayList.add} execution time is up to <i><b>O(size)</b></i> due to potential resize.</p>
 *     
 *     <a href="doc-files/FastTable-WCET.png">
 *     <img src="doc-files/FastTable-WCET.png" alt="Worst Case Execution Time" height="210" width="306" />
 *     </a>
 *     
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 7.0, September 13, 2015
 */
public class FractalTable<E> extends FastTable<E> {

    private static final long serialVersionUID = 0x700L; // Version. 
    private final Equality<? super E> equality;
    private FractalArray<E> array = FractalArray.empty();
    private int size;

    /**
     * Creates an empty fractal table.
     */
    public FractalTable() {
        this(Equality.DEFAULT);
    }
 
    /**
     * Creates an empty fractal table using the specified equality for elements equality comparisons.
     */
    public FractalTable(Equality<? super E> equality) {
        this.equality = equality;
    }
 

   @Override
    public boolean add(E element) {
        addLast(element);
        return true;
    }

    @Override
    public void add(int index, E element) {
        if ((index < 0) || (index > size)) indexError(index);
        array = array.shiftRight(element, index, size - index);
        size++;
    }

    @Override
    public void addFirst(E element) {
        array = array.shiftRight(element, 0, size);
        size++;
    }

    @Override
    public void addLast(E element) {
        array = array.set(size++, element);
    }

    @Override
    public void clear() {
        array = FractalArray.empty();
        size = 0;
    }

	@Override
	public FractalTable<E> clone() {
	    FractalTable<E> copy = (FractalTable<E>) super.clone();
		copy.array = array.clone();
		return copy;
	}

    @Override
    public Equality<? super E> equality() {
        return equality;
    }

    @Override
    public E get(int index) {
        if ((index < 0) && (index >= size)) indexError(index);
        return (E) array.get(index);
    }

    @Override
    public E getFirst() {
        if (size == 0) emptyError();
        return get(0);
    }

    @Override
    public E getLast() {
        if (size == 0) emptyError();
        return get(size - 1);
    }

    @Override
	public boolean isEmpty() {
		return size == 0;
	}

    @Override
    public E remove(int index) {
        if ((index < 0) || (index >= size)) indexError(index);
        E removed = (E) array.get(index);
        array = array.shiftLeft(null, --size, size - index);
        return removed;
    }

    @Override
    public E removeFirst() {
        if (size == 0) emptyError();
        E first = array.get(0);
        array = array.shiftLeft(null, --size, size);
        return first;
    }

    @Override
    public E removeLast() {
        if (size == 0) emptyError();
        E last = array.get(--size);
        array = array.set(size, null);
        return last;
    }

    @Override
    public E set(int index, E element) {
        if ((index < 0) && (index >= size)) indexError(index);
        E previous = array.get(index);
        array = array.set(index, element);
        return previous;
    }

    @Override
    public int size() {
        return size;
    }
 
}