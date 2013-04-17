/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.internal.util.table;

import javolution.lang.Predicate;
import javolution.util.service.ComparatorService;
import javolution.util.service.TableService;

/**
 * A shared view over a table allowing concurrent modifications.
 * Closure-based iterations use local table copies to avoid being 
 * impacted by concurrent modifications and not to block concurrent 
 * writes while iterating.
 */
public final class SharedTableImpl<E> extends AbstractTableImpl<E> {

    private final TableService<E> that;

    public SharedTableImpl(TableService<E> that) {
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
    public void doWhile(Predicate<E> predicate) {
        final FractalTableImpl<E> copy = new FractalTableImpl<E>();
        synchronized (that) {
            that.doWhile(new Predicate<E>() {
                public Boolean evaluate(E param) {
                    copy.addLast(param);
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
    public ComparatorService<E> comparator() {
        return that.comparator();
    }

    private static final long serialVersionUID = 2003570192853175381L;
}
