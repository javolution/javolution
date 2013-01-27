/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.internal.util;

import java.util.Collection;
import javolution.lang.Functor;
import javolution.lang.Predicate;
import javolution.util.AbstractTable;
import javolution.util.FastComparator;
import javolution.util.FastTable;

/**
 * A shared view over a table allowing concurrent modifications.
 * Closure-based iterations use local table copies to avoid being 
 * impacted by concurrent modifications and not to block concurrent 
 * writes while iterating.
 */
public final class SharedTableImpl<E> extends AbstractTable<E> {

    private final AbstractTable<E> that;

    public SharedTableImpl(AbstractTable<E> that) {
        this.that = that;
    }

    @Override
    public void clear() {
        synchronized (that) {
            that.clear();
        }
    }

    @Override
    public int size() {
        synchronized (that) {
            return that.size();
        }
    }

    @Override
    public E get(int index) {
        synchronized (that) {
            return that.get(index);
        }
    }

    @Override
    public E set(int index, E element) {
        synchronized (that) {
            return that.set(index, element);
        }
    }

    @Override
    public void add(int index, E element) {
        synchronized (that) {
            that.add(index, element);
        }
    }

    @Override
    public E remove(int index) {
        synchronized (that) {
            return that.remove(index);
        }
    }

    @Override
    public SharedTableImpl<E> copy() {
        synchronized (that) {
            return new SharedTableImpl<E>(that.copy());
        }
    }

    //
    // Non-abstract methods should forwards to actual table (unless default implementation is ok).
    //
    @Override
    public E getFirst() {
        synchronized (that) {
            return that.getFirst();
        }
    }

    @Override
    public E getLast() {
        synchronized (that) {
            return that.getLast();
        }
    }

    @Override
    public boolean add(E element) {
        synchronized (that) {
            return that.add(element);
        }
    }

    @Override
    public void addFirst(E element) {
        synchronized (that) {
            that.addFirst(element);
        }
    }

    @Override
    public void addLast(E element) {
        synchronized (that) {
            that.addLast(element);
        }
    }

    @Override
    public E removeFirst() {
        synchronized (that) {
            return that.removeFirst();
        }
    }

    @Override
    public E removeLast() {
        synchronized (that) {
            return that.removeLast();
        }
    }

    @Override
    public E pollFirst() {
        synchronized (that) {
            return that.pollFirst();
        }
    }

    @Override
    public E pollLast() {
        synchronized (that) {
            return that.pollLast();
        }
    }

    @Override
    public E peekFirst() {
        synchronized (that) {
            return that.peekFirst();
        }
    }

    @Override
    public E peekLast() {
        synchronized (that) {
            return that.peekLast();
        }
    }

    @Override
    public <R> FastTable<R> forEach(Functor<E, R> functor) {
        final FastTableImpl copy = new FastTableImpl();
        synchronized (that) {
            that.doWhile(new Predicate<E>() {
                public Boolean evaluate(E param) {
                    copy.addLast(that);
                    return true;
                }
            });
        }
        return copy.forEach(functor);
    }

    @Override
    public void doWhile(Predicate<E> predicate) {
        final FastTableImpl copy = new FastTableImpl();
        synchronized (that) {
            that.doWhile(new Predicate<E>() {
                public Boolean evaluate(E param) {
                    copy.addLast(that);
                    return true;
                }
            });
        }
        copy.doWhile(predicate);
    }

    @Override
    public boolean removeAll(Predicate<E> predicate) {
        synchronized (that) {
            return that.removeAll(predicate);
        }
    }

    @Override
    public boolean addAll(final Collection<? extends E> elements) {
        synchronized (that) {
            return that.addAll(elements);
        }
    }

    @Override
    public boolean addAll(final int index, final Collection<? extends E> elements) {
        synchronized (that) {
            return that.addAll(index, elements);
        }
    }

    @Override
    public boolean remove(E element) {
        synchronized (that) {
            return that.remove(element);
        }
    }

    @Override
    public int indexOf(E element) {
        synchronized (that) {
            return that.indexOf(element);
        }
    }

    @Override
    public int lastIndexOf(E element) {
        synchronized (that) {
            return that.lastIndexOf(element);
        }
    }

    @Override
    public void sort() {
        synchronized (that) {
            that.sort();
        }
    }

    @Override
    public FastComparator<E> comparator() {
        return that.comparator();
    }
}
