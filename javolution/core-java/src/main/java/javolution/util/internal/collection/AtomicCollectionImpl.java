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

/**
 * An atomic view over a collection (copy-on-write).
 */
public class AtomicCollectionImpl<E> extends CollectionView<E> {

    private static final long serialVersionUID = 0x600L; // Version.

    protected Object lock;
    protected volatile CollectionService<E> targetCopy; // The copy used by readers.
    protected transient Thread updatingThread; // The thread executing an update.

    public AtomicCollectionImpl(CollectionService<E> target) {
        this(target, new Object());
    }

    public AtomicCollectionImpl(CollectionService<E> target, Object lock) {
        super(target);
        this.lock = lock;
        this.targetCopy = cloneTarget();
    }

    @Override
    public boolean add(E element) {
        synchronized (lock) {
            boolean changed = target().add(element);
            if (changed && !updateInProgress())
                targetCopy = cloneTarget();
            return changed;
        }
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        synchronized (lock) {
            boolean changed = target().addAll(c);
            if (changed && !updateInProgress())
                targetCopy = cloneTarget();
            return changed;
        }
    }

    @Override
    public void clear() {
        synchronized (lock) {
            clear();
            if (!updateInProgress()) {
                targetCopy = cloneTarget();
            }
        }
    }

    @Override
    public AtomicCollectionImpl<E> clone() {
        synchronized (lock) { // Necessary since a copy of target is performed.
            AtomicCollectionImpl<E> copy = (AtomicCollectionImpl<E>) super
                    .clone();
            copy.updatingThread = null;
            copy.lock = new Object(); // No need to share the same lock.
            return copy;
        }
    }

    @Override
    public boolean contains(Object o) {
        return targetCopy.contains(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return targetCopy.containsAll(c);
    }

    @Override
    public boolean equals(Object o) {
        return targetCopy.equals(o);
    }

    @Override
    public int hashCode() {
        return targetCopy.hashCode();
    }
    
    @Override
    public boolean isEmpty() {
        return targetCopy.isEmpty();
    }

    @Override
    public Iterator<E> iterator() { 
        // If not within an update scope, the iterator is read-only.
        return updateInProgress() ? target().iterator() : 
            new UnmodifiableCollectionImpl<E>(targetCopy).iterator();
    }

    @Override
    public boolean remove(Object o) {
        synchronized (lock) {
            boolean changed = target().remove(o);
            if (changed && !updateInProgress())
                targetCopy = cloneTarget();
            return changed;
        }
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        synchronized (lock) {
            boolean changed = target().removeAll(c);
            if (changed && !updateInProgress())
                targetCopy = cloneTarget();
            return changed;
        }
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        synchronized (lock) {
            boolean changed = target().retainAll(c);
            if (changed && !updateInProgress())
                targetCopy = cloneTarget();
            return changed;
        }
    }

    @Override
    public int size() {
        return targetCopy.size();
    }

    /** 
     * The default implementation shares the lock between sub-collections, which 
     * prevents concurrent closure-based removal. Sub-classes may override
     * this method to avoid such limitation (e.g. {@code AtomicTableImpl}).
     */
    @SuppressWarnings("unchecked")
    @Override
    public AtomicCollectionImpl<E>[] subViews(int n) {
        CollectionService<E>[] tmp;
        synchronized (lock) {
            tmp = target().subViews(n); // We need sub-views of the real target
        }
        AtomicCollectionImpl<E>[] result = new AtomicCollectionImpl[tmp.length];
        for (int i = 0; i < tmp.length; i++) {
            result[i] = new AtomicCollectionImpl<E>(tmp[i], lock); // Same lock.
        }
        return result;
    }

    @Override
    public Object[] toArray() {
        return targetCopy.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return targetCopy.toArray(a);
    }

    @Override
    public void update(Consumer<Collection<E>> action, CollectionService<E> view) {
        synchronized (lock) {
            updatingThread = Thread.currentThread(); // Update in progress.
            try {
                target().update(action, view); // No copy performed.
            } finally {
                updatingThread = null;
                targetCopy = cloneTarget(); // One single copy !
            }
        }
    }
 
 
    /** Indicates if the current thread is doing an atomic update. */
    protected final boolean updateInProgress() {
        return updatingThread == Thread.currentThread();
    }

}
