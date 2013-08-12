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
import javolution.util.service.CollectionService;
import javolution.util.service.TableService;

/**
 * A shared view over a collection (reads-write locks). 
 */
public class SharedCollectionImpl<E> extends CollectionView<E> {

    private static final long serialVersionUID = 0x600L; // Version.

    protected ReadWriteLockImpl lock;
    protected transient Thread updatingThread; // The thread executing an update.

    public SharedCollectionImpl(CollectionService<E> target) {
        this(target, new ReadWriteLockImpl());        
    }

    public SharedCollectionImpl(CollectionService<E> target, ReadWriteLockImpl lock) {
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
        lock.readLock.lock(); // Necessary since a copy of target is performed.
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
        return updateInProgress() ? target().iterator() : 
            new UnmodifiableCollectionImpl<E>(cloneTarget()).iterator();
    }

    @Override
    public void perform(Consumer<Collection<E>> action,
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

    /** 
     * The default implementation shares the lock between sub-collections, which 
     * prevents concurrent closure-based removal. Sub-classes may override
     * this method to avoid such limitation (e.g. {@code SharedTableImpl}).
     */
    @SuppressWarnings("unchecked")
    @Override
    public SharedCollectionImpl<E>[] subViews(int n) {
        CollectionService<E>[] tmp;
        lock.readLock.lock();
        try {
            tmp = target().subViews(n);
        } finally {
            lock.readLock.unlock();
        }
        SharedCollectionImpl<E>[] result = new SharedCollectionImpl[tmp.length];
        for (int i = 0; i < tmp.length; i++) {
            result[i] = new SharedCollectionImpl<E>(tmp[i], lock); // Same lock.
        }
        return result;
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

    @Override
    public void update(Consumer<Collection<E>> action, CollectionService<E> view) {
        lock.writeLock.lock();
        try {
            updatingThread = Thread.currentThread();
            target().update(action, view);
        } finally {
            lock.writeLock.unlock();
            updatingThread = null;
        }
    }

    /** Returns a clone copy of target. */
    protected TableService<E> cloneTarget() {
        lock.readLock.lock();
        try {
            return (TableService<E>) super.cloneTarget();
        } finally {
            lock.readLock.unlock();
        }
    }

    /** Indicates if the current thread is doing an atomic update. */
    protected final boolean updateInProgress() {
        return updatingThread == Thread.currentThread();
    }    
}
