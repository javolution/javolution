/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util.internal.table;

import java.util.Collection;
import java.util.Iterator;
import java.util.ListIterator;

import javolution.util.internal.collection.AtomicCollectionImpl;
import javolution.util.service.TableService;

/**
 * An atomic view over a table.
 */
public class AtomicTableImpl<E> extends AtomicCollectionImpl<E> implements
        TableService<E> {

    private static final long serialVersionUID = 0x600L; // Version.

    public AtomicTableImpl(TableService<E> target) {
        super(target);
    }

    @Override
    public void add(int index, E element) {
        synchronized (lock) {
            target().add(index, element);
            if (!updateInProgress()) targetCopy = cloneTarget();
        }
    }

    @Override
    public void addFirst(E element) {
        synchronized (lock) {
            target().addFirst(element);
            if (!updateInProgress()) targetCopy = cloneTarget();
        }
    }

    @Override
    public void addLast(E element) {
        synchronized (lock) {
            target().addLast(element);
            if (!updateInProgress()) targetCopy = cloneTarget();
        }
    }

    @Override
    public E get(int index) {
        return targetCopy().get(index);
    }

    @Override
    public E getFirst() {
        return targetCopy().getFirst();
    }

    @Override
    public E getLast() {
        return targetCopy().getLast();
    }

    @Override
    public int indexOf(Object element) {
        return targetCopy().indexOf(element);
    }

    @Override
    public int lastIndexOf(Object element) {
        return targetCopy().lastIndexOf(element);
    }

    @Override
    public E peekFirst() {
        return targetCopy().peekFirst();
    }

    @Override
    public E peekLast() {
        return targetCopy().peekLast();
    }

    @Override
    public E pollFirst() {
        synchronized (lock) {
            E e = target().pollFirst();
            if ((e != null) && !updateInProgress())
                targetCopy = cloneTarget();
            return e;
        }
    }

    @Override
    public E pollLast() {
        synchronized (lock) {
            E e = target().pollLast();
            if ((e != null) && !updateInProgress())
                targetCopy = cloneTarget();
            return e;
        }
    }

    @Override
    public E remove(int index) {
        synchronized (lock) {
            E e = target().remove(index);
            if (!updateInProgress())
                targetCopy = cloneTarget();
            return e;
        }
    }

    @Override
    public E removeFirst() {
        synchronized (lock) {
            E e = target().removeFirst();
            if (!updateInProgress())
                targetCopy = cloneTarget();
            return e;
        }
    }

    @Override
    public E removeLast() {
        synchronized (lock) {
            E e = target().removeLast();
            if (!updateInProgress())
                targetCopy = cloneTarget();
            return e;
        }
    }

    @Override
    public E set(int index, E element) {
        synchronized (lock) {
            E e = target().set(index, element);
            if (!updateInProgress())
                targetCopy = cloneTarget();
            return e;
        }
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        synchronized (lock) {
            boolean changed = target().addAll(index, c);
            if (changed && !updateInProgress())
                targetCopy = cloneTarget();
            return changed;
        }
    }

    @Override
    public ListIterator<E> listIterator() {
        return listIterator(0);
    }

    @Override
    public ListIterator<E> listIterator(int index) {
        return updateInProgress() ? target().listIterator()
                : new UnmodifiableTableImpl<E>(targetCopy()).listIterator(index);
    }


    @Override
    public TableService<E> subList(int fromIndex, int toIndex) {
        return updateInProgress() ? target().subList(fromIndex, toIndex)
                : new UnmodifiableTableImpl<E>(targetCopy()).subList(fromIndex,
                        toIndex);
    }

    /** Returns the actual target */
    protected TableService<E> target() {
        return (TableService<E>) super.target();
    }

    @Override
    public boolean offerFirst(E e) {
        synchronized (lock) {
            boolean changed = target().offerFirst(e);
            if (changed && !updateInProgress())
                targetCopy = cloneTarget();
            return changed;
        } 
    }

    @Override
    public boolean offerLast(E e) {
        synchronized (lock) {
            boolean changed = target().offerLast(e);
            if (changed && !updateInProgress())
                targetCopy = cloneTarget();
            return changed;
        } 
    }

    @Override
    public boolean removeFirstOccurrence(Object o) {
        synchronized (lock) {
            boolean changed = target().removeFirstOccurrence(o);
            if (changed && !updateInProgress())
                targetCopy = cloneTarget();
            return changed;
        } 
    }

    @Override
    public boolean removeLastOccurrence(Object o) {
        synchronized (lock) {
            boolean changed = target().removeLastOccurrence(o);
            if (changed && !updateInProgress())
                targetCopy = cloneTarget();
            return changed;
        } 
    }

    @Override
    public boolean offer(E e) {
        return offerLast(e);
    }

    @Override
    public E remove() {
        return removeFirst();
    }

    @Override
    public E poll() {
        return pollFirst();
    }

    @Override
    public E element() {
        return getFirst();
    }

    @Override
    public E peek() {
        return peekFirst(); 
    }

    @Override
    public void push(E e) {
        addFirst(e); 
    }

    @Override
    public E pop() {
        return removeFirst();
    }

    @Override
    public Iterator<E> descendingIterator() {
        return updateInProgress() ? target().descendingIterator()
                : new UnmodifiableTableImpl<E>(targetCopy()).descendingIterator();
    }
    
    /** TableService view over targetCopy */
    protected TableService<E> targetCopy() {
        return (TableService<E>)targetCopy;
    }
 }
