/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util.internal.collection;

import java.util.Collection;
import java.util.Iterator;

import javolution.util.function.Consumer;
import javolution.util.function.Equality;
import javolution.util.service.CollectionService;

/**
 * A shared view over a collection (reads-write locks). 
 */
public class SharedCollectionImpl<E> extends CollectionView<E> {

    /** Thread-Safe Iterator. */
    private class IteratorImpl implements Iterator<E> { // Thread-Safe.
        private E next;
        private final Iterator<E> targetIterator;

        public IteratorImpl() {
            lock.readLock.lock();
            try {
                targetIterator = cloneTarget().iterator(); // Copy.
            } finally {
                lock.readLock.unlock();
            }
        }

        @Override
        public boolean hasNext() {
            return targetIterator.hasNext();
        }

        @Override
        public E next() {
            next = targetIterator.next();
            return next;
        }

        @Override
        public void remove() {
            if (next == null) throw new IllegalStateException();
            SharedCollectionImpl.this.remove(next);
            next = null;
        }
    }

    private static final long serialVersionUID = 0x600L; // Version.
    protected ReadWriteLockImpl lock;

    public SharedCollectionImpl(CollectionService<E> target) {
        this(target, new ReadWriteLockImpl());
    }

    public SharedCollectionImpl(CollectionService<E> target,
            ReadWriteLockImpl lock) {
        super(target);
        this.lock = lock;
    }

    @Override
    public boolean add(E element) {
        lock.writeLock.lock();
        try {
            return target().add(element);
        } finally {
            lock.writeLock.unlock();
        }
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        lock.writeLock.lock();
        try {
            return target().addAll(c);
        } finally {
            lock.writeLock.unlock();
        }
    }

    @Override
    public void clear() {
        lock.writeLock.lock();
        try {
            target().clear();
        } finally {
            lock.writeLock.unlock();
        }
    }

    @Override
    public SharedCollectionImpl<E> clone() {
        lock.readLock.lock();
        try {
            SharedCollectionImpl<E> copy = (SharedCollectionImpl<E>) super
                    .clone();
            copy.lock = new ReadWriteLockImpl(); // No need to share the same lock.
            return copy;
        } finally {
            lock.readLock.unlock();
        }
    }

    @Override
    public Equality<? super E> comparator() {
        return target().comparator();
    }

    @Override
    public boolean contains(Object o) {
        lock.readLock.lock();
        try {
            return target().contains(o);
        } finally {
            lock.readLock.unlock();
        }
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        lock.readLock.lock();
        try {
            return target().containsAll(c);
        } finally {
            lock.readLock.unlock();
        }
    }

    @Override
    public boolean equals(Object o) {
        lock.readLock.lock();
        try {
            return target().equals(o);
        } finally {
            lock.readLock.unlock();
        }
    }

    @Override
    public int hashCode() {
        lock.readLock.lock();
        try {
            return target().hashCode();
        } finally {
            lock.readLock.unlock();
        }
    }

    @Override
    public boolean isEmpty() {
        lock.readLock.lock();
        try {
            return target().isEmpty();
        } finally {
            lock.readLock.unlock();
        }
    }

    @Override
    public Iterator<E> iterator() {
        return new IteratorImpl();
    }

    @Override
    public void perform(Consumer<CollectionService<E>> action,
            CollectionService<E> view) {
        lock.readLock.lock();
        try {
            target().perform(action, view);
        } finally {
            lock.readLock.unlock();
        }
    }

    @Override
    public boolean remove(Object o) {
        lock.writeLock.lock();
        try {
            return target().remove(o);
        } finally {
            lock.writeLock.unlock();
        }
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        lock.writeLock.lock();
        try {
            return target().removeAll(c);
        } finally {
            lock.writeLock.unlock();
        }
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        lock.writeLock.lock();
        try {
            return target().retainAll(c);
        } finally {
            lock.writeLock.unlock();
        }
    }

    @Override
    public int size() {
        lock.readLock.lock();
        try {
            return target().size();
        } finally {
            lock.readLock.unlock();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public CollectionService<E>[] split(int n) { // Shares the same locks.
        CollectionService<E>[] tmp;
        lock.readLock.lock();
        try {
            tmp = target().split(n);
        } finally {
            lock.readLock.unlock();
        }
        CollectionService<E>[] result = new CollectionService[tmp.length];
        for (int i = 0; i < tmp.length; i++) {
            result[i] = new SharedCollectionImpl<E>(tmp[i], lock);
        }
        return result;
    }

    @Override
    public CollectionService<E> threadSafe() {
        return this;
    }

    @Override
    public Object[] toArray() {
        lock.readLock.lock();
        try {
            return target().toArray();
        } finally {
            lock.readLock.unlock();
        }
    }

    @Override
    public <T> T[] toArray(T[] a) {
        lock.readLock.lock();
        try {
            return target().toArray(a);
        } finally {
            lock.readLock.unlock();
        }
    }

    /** Returns a clone copy of target. */
    protected CollectionService<E> cloneTarget() {
        try {
            return target().clone();
        } catch (CloneNotSupportedException e) {
            throw new Error("Cannot happen since target is Cloneable.");
        }
    }

}
