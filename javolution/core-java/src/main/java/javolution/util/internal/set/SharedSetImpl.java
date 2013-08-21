/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util.internal.set;

import javolution.util.internal.ReadWriteLockImpl;
import javolution.util.internal.collection.SharedCollectionImpl;
import javolution.util.service.SetService;

/**
 * A shared view over a set allowing concurrent access and sequential updates.
 */
public class SharedSetImpl<E> extends SharedCollectionImpl<E> implements
        SetService<E> {

    private static final long serialVersionUID = 0x600L; // Version.

    public SharedSetImpl(SetService<E> target) {
        super(target);
    }

    public SharedSetImpl(SetService<E> target, ReadWriteLockImpl lock) {
        super(target, lock);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public SetService<E>[] split(int n, boolean updateable) {
        SetService<E>[] tmp;
        lock.readLock.lock();
        try {
            tmp = target().split(n, updateable); 
        } finally {
            lock.readLock.unlock();
        }
        SetService<E>[] result = new SetService[tmp.length];
        for (int i = 0; i < tmp.length; i++) {
            result[i] = new SharedSetImpl<E>(tmp[i], lock); // Shares the same locks.
        }
        return result;
    }
    
    @Override
    protected SetService<E> target() {
        return (SetService<E>) super.target();
    }    
}
