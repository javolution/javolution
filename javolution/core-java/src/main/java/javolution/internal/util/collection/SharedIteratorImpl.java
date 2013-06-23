/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.internal.util.collection;

import java.util.Iterator;
import java.util.concurrent.locks.Lock;

/**
 * An iterator allowing concurrent access and sequential removals.
 */
public final class SharedIteratorImpl<E> implements Iterator<E> {

    private final Iterator<E> target;
    private final Lock read;
    private final Lock write;

    public SharedIteratorImpl(Iterator<E> target, Lock read, Lock write) {
        this.target = target;
        this.read = read;
        this.write = write;
    }

    @Override
    public boolean hasNext() {
        read.lock();
        try {
            return target.hasNext();
        } finally {
            read.unlock();
        }
    }

    @Override
    public E next() {
        read.lock();
        try {
            return target.next();
        } finally {
            read.unlock();
        }
    }

    @Override
    public void remove() {
        write.lock();
        try {
            target.remove();
        } finally {
            write.unlock();
        }
    }

}
