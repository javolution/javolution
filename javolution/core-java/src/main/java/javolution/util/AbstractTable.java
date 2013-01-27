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
 *       relying upon the behavior of its non-abstract methods.
 */
public abstract class AbstractTable<E> implements Copyable<AbstractTable<E>> {

    /** See {@link FastTable#size() } */
    public abstract int size();

    /** See {@link FastTable#get(int)  } */
    public abstract E get(int index);

    /** See {@link FastTable#set(int, java.lang.Object)  } */
    public abstract E set(int index, E element);

    /** See {@link FastTable#add(int, java.lang.Object) } */
    public abstract void add(int index, E element);

    /** See {@link FastTable#remove(int) } */
    public abstract E remove(int index);

    //
    // Non-Abstract methods.
    //
    /** See {@link FastTable#clear() } */
    public void clear() {
        removeAll(new Predicate<E>() {
            public Boolean evaluate(E param) {
                return true;
            }
        });
    }

    /** See {@link FastTable#getFirst() } */
    public E getFirst() {
        if (size() == 0) emptyError();
        return get(0);
    }

    /** See {@link FastTable#getLast() } */
    public E getLast() {
        if (size() == 0) emptyError();
        return get(size() - 1);
    }

    /** See {@link FastTable#add(java.lang.Object) } */
    public boolean add(E element) {
        add(size(), element);
        return true;
    }

    /** See {@link FastTable#addFirst(java.lang.Object) } */
    public void addFirst(E element) {
        add(0, element);
    }

    /** See {@link FastTable#addLast(java.lang.Object) } */
    public void addLast(E element) {
        add(size(), element);
    }

    /** See {@link FastTable#removeFirst() } */
    public E removeFirst() {
        E e = getFirst();
        remove(0);
        return e;
    }

    /** See {@link FastTable#removeLast() } */
    public E removeLast() {
        E e = getLast();
        remove(size() - 1);
        return e;
    }

    /** See {@link FastTable#pollFirst() } */
    public E pollFirst() {
        return (size() == 0) ? null : removeFirst();
    }

    /** See {@link FastTable#pollLast() } */
    public E pollLast() {
        return (size() == 0) ? null : removeLast();
    }

    /** See {@link FastTable#peekFirst() } */
    public E peekFirst() {
        return (size() == 0) ? null : getFirst();
    }

    /** See {@link FastTable#peekLast() } */
    public E peekLast() {
        return (size() == 0) ? null : getLast();
    }

    /** See {@link FastTable#forEach(javolution.lang.Functor) } */
    public <R> FastTable<R> forEach(Functor<E, R> functor) {
        FastTable<R> results = new FastTable<R>();
        for (int i = 0, size = size(); i < size;) {
            R result = functor.evaluate(get(i++));
            if (result != null) results.addLast(result);
        }
        return results;
    }

    /** See {@link FastTable#doWhile(javolution.lang.Predicate) } */
    public void doWhile(Predicate<E> predicate) {
        for (int i = 0, size = size(); i < size;) {
            if (!predicate.evaluate(get(i++))) return;
        }
    }

    /** See {@link FastTable#removeAll(javolution.lang.Predicate) } */
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

    /** See {@link FastTable#addAll(java.util.Collection) } */
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

    /** See {@link FastTable#addAll(int, java.util.Collection) } */
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
                add(i++, it.next());
            }
        }
        return !elements.isEmpty();
    }

    /** See {@link FastTable#contains(java.lang.Object) } */
    public boolean contains(E element) {
        int i = indexOf(element);
        return (i < 0) ? false : true;
    }

    /** See {@link FastTable#remove(java.lang.Object) } */
    public boolean remove(E element) {
        int i = indexOf(element);
        if (i < 0) return false;
        remove(i);
        return true;
    }

    /** See {@link FastTable#size() } */
    public int indexOf(E element) {
        FastComparator<E> cmp = comparator();
        for (int i = 0, size = size(); i < size; i++) {
            if (cmp.areEqual(element, get(i))) return i;
        }
        return -1;
    }

    /** See {@link FastTable#lastIndexOf(java.lang.Object) } */
    public int lastIndexOf(E element) {
        FastComparator<E> cmp = comparator();
        for (int i = size(); i > 0;) {
            if (cmp.areEqual(element, get(--i))) return i;
        }
        return -1;
    }

    /** See {@link FastTable#sort() } */
    public void sort() {
        int size = size();
        if (size > 1) {
            quicksort(0, size - 1, comparator());
        }
    }

    /** See {@link FastTable#comparator() } */
    public FastComparator<E> comparator() {
        return (FastComparator<E>) FastComparator.DEFAULT;
    }

    /** Throws NoSuchElementException */
    protected void emptyError() {
        throw new NoSuchElementException("Empty Table");
    }

    /** Throws IndexOutOfBoundsException */
    protected void indexError(int index) {
        throw new IndexOutOfBoundsException(
                "index: " + index + ", size: " + size());
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
