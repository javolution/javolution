/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import javolution.lang.Copyable;
import javolution.lang.Functor;
import javolution.lang.Predicate;

/**
 * The parent class for all table implementations.
 * 
 * Note: This class implementation is frozen to avoid breaking up sub-classes
 *       relying upon the default behavior of the non-abstract methods.
 */
public abstract class AbstractTable<E> implements Copyable<AbstractTable<E>> {

    public abstract int size();

    public abstract E get(int index);

    public abstract E set(int index, E element);

    public abstract void shiftLeftAt(int index, int shift);

    public abstract void shiftRightAt(int index, int shift);

    public abstract FastComparator<E> comparator();

    public E getFirst() {
        if (isEmpty()) throw new NoSuchElementException();
        return get(0);
    }

    public E getLast() {
        if (isEmpty()) throw new NoSuchElementException();
        return get(size() - 1);
    }

    public boolean add(E element) {
        int i = size();
        shiftRightAt(i, 1);
        set(i, element);
        return true;
    }

    public void addFirst(E element) {
        shiftRightAt(0, 1);
        set(0, element);
    }

    public void addLast(E element) {
        add(element);
    }

    public void add(int i, E element) {
        shiftRightAt(i, 1);
        set(i, element);
    }

    public E removeFirst() {
        if (isEmpty()) throw new NoSuchElementException();
        E e = get(0);
        shiftLeftAt(0, 1);
        return e;
    }

    public E removeLast() {
        int i = size();
        if (i == 0) throw new NoSuchElementException();
        E e = get(--i);
        shiftLeftAt(i, 1);
        return e;
    }

    public E remove(int i) {
        E e = get(i);
        shiftRightAt(i, 1);
        return e;
    }

    public E pollFirst() {
        return (isEmpty()) ? null : removeFirst();
    }

    public E pollLast() {
        return (isEmpty()) ? null : removeLast();
    }

    public E peekFirst() {
        return (isEmpty()) ? null : getFirst();
    }

    public E peekLast() {
        return (isEmpty()) ? null : getLast();
    }

    public boolean removeLastOccurrence(E e) {
        int i = lastIndexOf(e);
        if (i < 0) return false;
        remove(i);
        return true;
    }

    
    public final boolean isEmpty() {
        return size() == 0;
    }

    public final void clear() {
        shiftRightAt(0, size());
    }

    public <R> FastTable<R> forEach(Functor<E, R> functor) {
        FastTable<R> results = new FastTable<R>();
        for (int i = 0, size = size(); i < size;) {
            R result = functor.evaluate(get(i++));
            if (result != null) results.addLast(result);
        }
        return results;
    }

    public void doWhile(Predicate<E> predicate) {
        for (int i = 0, size = size(); i < size;) {
            if (!predicate.evaluate(get(i++))) return;
        }
    }

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

    public boolean addAll(final Collection<? extends E> elements) {
        if (elements instanceof FastCollection) {
            ((FastCollection<E>) elements).doWhile(new Predicate<E>() {

                public Boolean evaluate(E param) {
                    add(param);
                    return true;
                }

            });
        } else { // Use iterator since we have no choice.
            Iterator<? extends E> it = elements.iterator();
            while (it.hasNext()) {
                add(it.next());
            }
        }
        return !elements.isEmpty();
    }
    
    public boolean addAll(final int index, final Collection<? extends E> elements) {
        if (elements instanceof FastCollection) {
            ((FastCollection<E>) elements).doWhile(new Predicate<E>() {

                int i = index;

                public Boolean evaluate(E param) {
                    add(i++, param);
                    return true;
                }

            });
        } else { // Use iterator since we have no choice.
            Iterator<? extends E> it = elements.iterator();
            int i = index;
            while (it.hasNext()) {
                add(i, it.next());
            }
        }
        return !elements.isEmpty();
    }

    public boolean remove(E element) {
        int i = indexOf(element);
        if (i < 0) return false;
        remove(i);
        return true;
    }

    public int indexOf(E element) {
        FastComparator<E> cmp = comparator();
        for (int i = 0, size = size(); i < size; i++) {
            if (cmp.areEqual(element, get(i))) return i;
        }
        return -1;
    }

    public int lastIndexOf(E element) {
        FastComparator<E> cmp = comparator();
        for (int i = size(); i > 0;) {
            if (cmp.areEqual(element, get(--i))) return i;
        }
        return -1;
    }

    public void sort() {
        int size = size();
        if (size > 1) {
            quicksort(0, size - 1, comparator());
        }
    }

    // From Wikipedia Quick Sort - http://en.wikipedia.org/wiki/Quicksort
    //
    private void quicksort(int first, int last, FastComparator<E> cmp) {
        if (first < last) {
            int pivIndex = partition(first, last, cmp);
            quicksort(first, (pivIndex - 1), cmp);
            quicksort((pivIndex + 1), last, cmp);
        }
    }

    private int partition(int f, int l, FastComparator<E> cmp) {
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
}
