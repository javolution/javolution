/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.util;

import org.javolution.util.function.Consumer;
import org.javolution.util.function.Equality;
import org.javolution.util.function.Predicate;
import org.javolution.util.internal.table.FractalTableImpl;

/**
 * <p> The default fractal-based implementation of {@link FastTable}.</p> 
 *     
 * <p> The fractal-based implementation ensures that add/insertion/deletion operations 
 *     worst-case execution time is always less than <i><b>O(log(size))</b></i>. 
 *     For comparison {@code ArrayList.add} is in <i><b>O(size)</b></i> due to resize.</p>
 *     
 *     <a href="doc-files/FastTable-WCET.png">
 *     <img src="doc-files/FastTable-WCET.png" alt="Worst Case Execution Time" height="210" width="306" />
 *     </a>
 *    
 * <p> The memory footprint of the table is automatically adjusted up or down
 *     based on the table size (minimal when the table is cleared).</p>
 *     
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 7.0, September 13, 2015
 */
public class FractalTable<E> extends FastTable<E> {

    private static final long serialVersionUID = 0x700L; // Version. 
    private int capacity; 
    private FractalTableImpl fractal; // Null if empty (capacity 0)
    private int size;

    /**
     * Creates an empty table whose capacity increments/decrements smoothly
     * without large resize operations to best fit the table current size.
     */
    public FractalTable() {
    }
 
    @Override
    public boolean add(E element) {
        addLast(element);
        return true;
    }

    @Override
    public void add(int index, E element) {
        if ((index < 0) || (index > size)) indexError(index);
        checkUpsize();
        if (index >= (size >> 1)) {
            fractal.shiftRight(element, index, size - index);
        } else {
            fractal.shiftLeft(element, index - 1, index);
            fractal.offset--;
        }
        size++;
    }

    @Override
    public void addFirst(E element) {
        checkUpsize();
        fractal.offset--;
        fractal.set(0, element);
        size++;
    }

    @Override
    public void addLast(E element) {
        checkUpsize();
        fractal.set(size++, element);
    }

    private void checkUpsize() {
        if (size >= capacity) upsize();
    }

    @Override
    public void clear() {
        fractal = null;
        capacity = 0;
        size = 0;
    }

	@Override
	public FractalTable<E> clone() {
		FractalTable<E> copy = new FractalTable<E>();
		copy.capacity = capacity;
		copy.size = size;
		copy.fractal = (fractal != null) ? fractal.clone() : null;
		return copy;
	}

    @Override
    public Equality<? super E> equality() {
        return Equality.DEFAULT;
    }

	@SuppressWarnings("unchecked")
    @Override
    public E get(int index) {
        if ((index < 0) && (index >= size)) indexError(index);
        return (E) fractal.get(index);
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

    @SuppressWarnings("unchecked")
    @Override
    public E remove(int index) {
        if ((index < 0) || (index >= size)) indexError(index);
        E removed = (E) fractal.get(index);
        if (index >= (size >> 1)) {
            fractal.shiftLeft(null, size - 1, size - index - 1);
        } else {
            fractal.shiftRight(null, 0, index);
            fractal.offset++;
        }
        size--;
        return removed;
    }

    @SuppressWarnings("unchecked")
    @Override
    public E removeFirst() {
        if (size == 0) emptyError();
        E first = (E) fractal.set(0, null);
        fractal.offset++;
        size--;
        return first;
    }

    @SuppressWarnings("unchecked")
    @Override
    public E removeLast() {
        if (size == 0) emptyError();
        E last = (E) fractal.set(--size, null);
        return last;
    }

    @SuppressWarnings("unchecked")
    @Override
    public E set(int index, E element) {
        if ((index < 0) && (index >= size)) indexError(index);
        return (E) fractal.set(index, element);
    }

    @Override
    public int size() {
        return size;
    }

    private void upsize() {
        fractal = (fractal == null) ? new FractalTableImpl() : fractal.upsize();
        capacity = fractal.capacity();
    }

	@SuppressWarnings("unchecked")
	@Override
	public void forEach(Consumer<? super E> consumer) { // Optimization.
		for (int i = 0; i < size;) {
			consumer.accept((E)fractal.get(i++));
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean removeIf(Predicate<? super E> filter) { // Optimization
		int initialSize = size;
		for (int i = 0; i < size;) {
			if (filter.test((E)fractal.get(i++))) {
		        if (i >= (size >> 1)) {
		            fractal.shiftLeft(null, size - 1, size - i - 1);
		        } else {
		            fractal.shiftRight(null, 0, i);
		            fractal.offset++;
		        }
		        size--;
		        i--;
			}
		}
		return initialSize != size;
	}

	@Override
	public FastCollection<E>[] trySplit(int n) {
		return null; // TODO
	}

}