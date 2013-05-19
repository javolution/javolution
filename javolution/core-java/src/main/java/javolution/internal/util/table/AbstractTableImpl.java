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

import javolution.lang.MathLib;
import javolution.util.function.Predicate;
import javolution.util.service.ComparatorService;
import javolution.util.service.TableService;

/**
 * Parent class to facilitate implementations of custom tables/views.
 * 
 * Note: This class implementation is frozen to avoid breaking up sub-classes
 *       relying upon the behavior of its non-abstract methods.
 *       
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0.0, December 12, 2012
 */
public abstract class AbstractTableImpl<E> implements TableService<E>, Serializable {
      
    //
    // Default implementation.
    //
        
    final boolean addDefault(E element) {
        addLast(element);
        return true;
    }

    final E getFirstDefault() {
        if (size() == 0)
            emptyError();
        return get(0);
    }

    final E getLastDefault() {
        if (size() == 0)
            emptyError();
        return get(size() - 1);
    }
    
    final void addFirstDefault(E element) {
        add(0, element);
    }

     final void addLastDefault(E element) {
        add(size(), element);
    }

    final E removeFirstDefault() {
        E e = getFirst();
        remove(0);
        return e;
    }

    final E removeLastDefault() {
        E e = getLast();
        remove(size() - 1);
        return e;
    }

    final E pollFirstDefault() {
        return (size() == 0) ? null : removeFirst();
    }

    final E pollLastDefault() {
        return (size() == 0) ? null : removeLast();
    }

    final E peekFirstDefault() {
        return (size() == 0) ? null : getFirst();
    }

    final E peekLastDefault() {
        return (size() == 0) ? null : getLast();
    }

    final boolean doWhileDefault(Predicate<? super E> predicate) {
        for (int i = 0, size = size(); i < size;) {
            if (!predicate.apply(get(i++)))
                return false;
        }
        return true;
    }

    final boolean removeAllDefault(Predicate<? super E> predicate) {
        boolean modified = false;
        for (int i = size(); i > 0;) {
            if (predicate.apply(get(--i))) {
                remove(i);
                modified = true;
            }
        }
        return modified;
    }

    final boolean containsDefault(E element) {
        return (indexOf(element) < 0) ? false : true;
    }

    final boolean removeDefault(E element) {
        int i = indexOf(element);
        if (i < 0)
            return false;
        remove(i);
        return true;
    }

     final int indexOfDefault(E element) {
        ComparatorService<? super E> cmp = getComparator();
        for (int i = 0, size = size(); i < size; i++) {
            if (cmp.areEqual(element, get(i)))
                return i;
        }
        return -1;
    }

     final int lastIndexOfDefault(E element) {
        ComparatorService<? super E> cmp = getComparator();
        for (int i = size(); i > 0;) {
            if (cmp.areEqual(element, get(--i)))
                return i;
        }
        return -1;
    }

    final void sortDefault() {
        int size = size();
        if (size > 1) {
            quicksort(0, size - 1, getComparator());
        }
    }

    final Iterator<E> iteratorDefault() {
        return new TableIteratorImpl<E>(this, 0);
    }

    @SuppressWarnings("unchecked")
    final TableService<E>[] trySplitDefault(int n) {
        int size = size();
        int length = MathLib.min(n, size);
        if (length < 2)  return null; // No split.
        TableService<E>[] subTables = new TableService[length];
        int div = size / length;
        int start = 0;
        for (int i=0; i < length - 1; i++) {
            subTables[i] = new SubTableImpl<E>(this, start, start + div);
            start += div;
        }
        subTables[length-1] = new SubTableImpl<E>(this, start, size - start);
        return subTables;
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
    private void quicksort(int first, int last, ComparatorService<? super E> cmp) {
        if (first < last) {
            int pivIndex = partition(first, last, cmp);
            quicksort(first, (pivIndex - 1), cmp);
            quicksort((pivIndex + 1), last, cmp);
        }
    }

    private int partition(int f, int l, ComparatorService<? super E> cmp) {
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

    private static final long serialVersionUID = -3556502691724909437L;
  
}
