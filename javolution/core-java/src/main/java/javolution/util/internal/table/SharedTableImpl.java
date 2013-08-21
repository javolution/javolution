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

import javolution.util.internal.ReadWriteLockImpl;
import javolution.util.internal.collection.SharedCollectionImpl;
import javolution.util.service.TableService;

/**
 * A shared view over a table allowing concurrent access and sequential updates.
 */
public class SharedTableImpl<E> extends SharedCollectionImpl<E> implements
        TableService<E> {

    private static final long serialVersionUID = 0x600L; // Version.

    public SharedTableImpl(TableService<E> target) {
        super(target);
    }

    public SharedTableImpl(TableService<E> target, ReadWriteLockImpl lock) {
        super(target, lock);
    }

    @Override
    public void add(int index, E element) {
        lock.writeLock.lock();
        try {
            target().add(index, element);
        } finally {
            lock.writeLock.unlock();
        }
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        lock.writeLock.lock();
        try {
            return target().addAll(index, c);
        } finally {
            lock.writeLock.unlock();
        }
    }

    @Override
    public void addFirst(E element) {
        lock.writeLock.lock();
        try {
            target().addFirst(element);
        } finally {
            lock.writeLock.unlock();
        }
    }

    @Override
    public void addLast(E element) {
        lock.writeLock.lock();
        try {
            target().addLast(element);
        } finally {
            lock.writeLock.unlock();
        }
    }

    @Override
    public Iterator<E> descendingIterator() {
        return new ReversedTableImpl<E>(this).iterator(); // View on this.
    }

    @Override
    public E element() {
        return getFirst();
    }

    @Override
    public E get(int index) {
        lock.readLock.lock();
        try {
            return target().get(index);
        } finally {
            lock.readLock.unlock();
        }
    }

    @Override
    public E getFirst() {
        lock.readLock.lock();
        try {
            return target().getFirst();
        } finally {
            lock.readLock.unlock();
        }
    }

    @Override
    public E getLast() {
        lock.readLock.lock();
        try {
            return target().getLast();
        } finally {
            lock.readLock.unlock();
        }
    }

    @Override
    public int indexOf(Object element) {
        lock.readLock.lock();
        try {
            return target().indexOf(element);
        } finally {
            lock.readLock.unlock();
        }
    }

    @Override
    public ListIterator<E> iterator() {
        return target().listIterator(0);
    }

    @Override
    public int lastIndexOf(Object element) {
        lock.readLock.lock();
        try {
            return target().lastIndexOf(element);
        } finally {
            lock.readLock.unlock();
        }
    }

    @Override
    public ListIterator<E> listIterator() {
        return target().listIterator(0);
    }

    @Override
    public ListIterator<E> listIterator(int index) {
        return new TableIteratorImpl<E>(this, index); // View on this.
    }

    @Override
    public boolean offer(E e) {
        return offerLast(e);
    }

    @Override
    public boolean offerFirst(E e) {
        lock.writeLock.lock();
        try {
            return target().offerFirst(e);
        } finally {
            lock.writeLock.unlock();
        }
    }

    @Override
    public boolean offerLast(E e) {
        lock.writeLock.lock();
        try {
            return target().offerLast(e);
        } finally {
            lock.writeLock.unlock();
        }
    }

    @Override
    public E peek() {
        return peekFirst();
    }

    @Override
    public E peekFirst() {
        lock.readLock.lock();
        try {
            return target().peekFirst();
        } finally {
            lock.readLock.unlock();
        }
    }

    @Override
    public E peekLast() {
        lock.readLock.lock();
        try {
            return target().peekLast();
        } finally {
            lock.readLock.unlock();
        }
    }

    @Override
    public E poll() {
        return pollFirst();
    }

    @Override
    public E pollFirst() {
        lock.writeLock.lock();
        try {
            return target().pollFirst();
        } finally {
            lock.writeLock.unlock();
        }
    }

    @Override
    public E pollLast() {
        lock.writeLock.lock();
        try {
            return target().pollLast();
        } finally {
            lock.writeLock.unlock();
        }
    }

    @Override
    public E pop() {
        return removeFirst();
    }

    @Override
    public void push(E e) {
        addFirst(e);
    }

    @Override
    public E remove() {
        return removeFirst();
    }

    @Override
    public E remove(int index) {
        lock.writeLock.lock();
        try {
            return target().remove(index);
        } finally {
            lock.writeLock.unlock();
        }
    }

    @Override
    public E removeFirst() {
        lock.writeLock.lock();
        try {
            return target().removeFirst();
        } finally {
            lock.writeLock.unlock();
        }
    }

    @Override
    public boolean removeFirstOccurrence(Object o) {
        lock.writeLock.lock();
        try {
            return target().removeFirstOccurrence(o);
        } finally {
            lock.writeLock.unlock();
        }
    }

    @Override
    public E removeLast() {
        lock.writeLock.lock();
        try {
            return target().removeLast();
        } finally {
            lock.writeLock.unlock();
        }
    }

    @Override
    public boolean removeLastOccurrence(Object o) {
        lock.writeLock.lock();
        try {
            return target().removeLastOccurrence(o);
        } finally {
            lock.writeLock.unlock();
        }
    }

    @Override
    public E set(int index, E element) {
        lock.writeLock.lock();
        try {
            return target().set(index, element);
        } finally {
            lock.writeLock.unlock();
        }
    }
    @Override
    public TableService<E>[] split(int n, boolean updateable) {
        return SubTableImpl.splitOf(this, n, false); // Sub-views over this.
    }

    @Override
    public TableService<E> subList(int fromIndex, int toIndex) {
        return new SubTableImpl<E>(this, fromIndex, toIndex); // View on this.
    }

    /** Returns the actual target */
    @Override
    protected TableService<E> target() {
        return (TableService<E>) super.target();
    }

}