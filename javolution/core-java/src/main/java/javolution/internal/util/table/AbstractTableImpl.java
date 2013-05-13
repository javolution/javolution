/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.internal.util.table;

import java.io.Serializable;
import java.util.Iterator;
import java.util.NoSuchElementException;

import javolution.util.function.Predicate;
import javolution.util.service.ComparatorService;
import javolution.util.service.TableService;

/**
 * Parent class to facilitate TableService custom implementations.
 * 
 * Note: This class implementation is frozen to avoid breaking up sub-classes
 *       relying upon the behavior of its non-abstract methods.
 *       
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0.0, December 12, 2012
 */
public abstract class AbstractTableImpl<E> implements TableService<E>, Serializable {
    
    @Override
    public abstract int size();

    @Override
    public abstract void clear();

    @Override
    public abstract void add(int index, E element);

    @Override
    public abstract E get(int index);

    @Override
    public abstract E set(int index, E element);

    @Override
    public abstract E remove(int index);
    
    @Override
    public abstract void setComparator(ComparatorService<E> cmp);
    //
    // Methods with default implementation.
    //
    
    
    @Override
    public boolean add(E element) {
        addLast(element);
        return true;
    }

    @Override
    public E getFirst() {
        if (size() == 0)
            emptyError();
        return get(0);
    }

    @Override
    public E getLast() {
        if (size() == 0)
            emptyError();
        return get(size() - 1);
    }

    @Override
    public void addFirst(E element) {
        add(0, element);
    }

    @Override
    public void addLast(E element) {
        add(size(), element);
    }

    @Override
    public E removeFirst() {
        E e = getFirst();
        remove(0);
        return e;
    }

    @Override
    public E removeLast() {
        E e = getLast();
        remove(size() - 1);
        return e;
    }

    @Override
    public E pollFirst() {
        return (size() == 0) ? null : removeFirst();
    }

    @Override
    public E pollLast() {
        return (size() == 0) ? null : removeLast();
    }

    @Override
    public E peekFirst() {
        return (size() == 0) ? null : getFirst();
    }

    @Override
    public E peekLast() {
        return (size() == 0) ? null : getLast();
    }

    @Override
    public void doWhile(Predicate<E> predicate) {
        for (int i = 0, size = size(); i < size;) {
            if (!predicate.evaluate(get(i++)))
                return;
        }
    }

    @Override
    public boolean removeAll(Predicate<E> predicate) {
        boolean modified = false;
        for (int i = size(); i > 0;) {
            if (predicate.evaluate(get(--i))) {
                remove(i);
                modified = true;
            }
        }
        return modified;
    }

    @Override
    public boolean contains(E element) {
        return (indexOf(element) < 0) ? false : true;
    }

    @Override
    public boolean remove(E element) {
        int i = indexOf(element);
        if (i < 0)
            return false;
        remove(i);
        return true;
    }

    @Override
    public int indexOf(E element) {
        ComparatorService<E> cmp = getComparator();
        for (int i = 0, size = size(); i < size; i++) {
            if (cmp.areEqual(element, get(i)))
                return i;
        }
        return -1;
    }

    @Override
    public int lastIndexOf(E element) {
        ComparatorService<E> cmp = getComparator();
        for (int i = size(); i > 0;) {
            if (cmp.areEqual(element, get(--i)))
                return i;
        }
        return -1;
    }

    @Override
    public void sort() {
        int size = size();
        if (size > 1) {
            quicksort(0, size - 1, getComparator());
        }
    }

    @Override
    public Iterator<E> iterator() {
        return new TableIteratorImpl<E>(this, 0);
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

    // From Wikipedia Quick Sort - http://en.wikipedia.org/wiki/Quicksort
    //
    private void quicksort(int first, int last, ComparatorService<E> cmp) {
        if (first < last) {
            int pivIndex = partition(first, last, cmp);
            quicksort(first, (pivIndex - 1), cmp);
            quicksort((pivIndex + 1), last, cmp);
        }
    }

    private int partition(int f, int l, ComparatorService<E> cmp) {
        int up, down;
        E piv = get(f);
        up = f;
        down = l;
        do {
            while (cmp.compare(get(up), piv) <= 0 && up < l) {
                up++;
            }
            while (cmp.compare(get(down), piv) > 0 && down > f) {
                down--;
            }
            if (up < down) { // Swaps.
                E temp = get(up);
                set(up, get(down));
                set(down, temp);
            }
        } while (down > up);
        set(f, get(down));
        set(down, piv);
        return down;
    }
  
    private static final long serialVersionUID = -4148136304080489337L;
}
