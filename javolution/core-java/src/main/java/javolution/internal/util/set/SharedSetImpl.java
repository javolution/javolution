/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.internal.util.set;

import java.io.Serializable;

import javolution.internal.util.collection.SharedCollectionImpl;
import javolution.util.service.SetService;

/**
 * A shared view over a set allowing concurrent access and sequential updates.
 */
public class SharedSetImpl<E> extends SharedCollectionImpl<E> implements SetService<E>,
        Serializable {

    public SharedSetImpl(SetService<E> that) {
        super(that);
    }
    
    @Override
    public int size() {
        read.lock();
        try {
            return ((SetService<E>) that).size();
        } finally {
            read.unlock();
        }
    }

    @Override
    public void clear() {
        write.lock();
        try {
            ((SetService<E>) that).clear();
        } finally {
            write.unlock();
        }
    }

    @Override
    public boolean contains(E e) {
        read.lock();
        try {
            return ((SetService<E>) that).contains(e);
        } finally {
            read.unlock();
        }   
    }

    @Override
    public boolean remove(E e) {
        write.lock();
        try {
            return ((SetService<E>) that).remove(e);
        } finally {
            write.unlock();
        }
    }
    
    private static final long serialVersionUID = 8668632118009609808L;
}
