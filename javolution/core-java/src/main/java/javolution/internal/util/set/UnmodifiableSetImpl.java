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

import javolution.internal.util.collection.UnmodifiableCollectionImpl;
import javolution.util.service.SetService;

/**
 * An unmodifiable view over a set.
 */
public class UnmodifiableSetImpl<E> extends UnmodifiableCollectionImpl<E> implements SetService<E>,
        Serializable {

    public UnmodifiableSetImpl(SetService<E> that) {
        super(that);
    }

    @Override
    public int size() {
        return ((SetService<E>)that).size();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("Unmodifiable");
    }

    @Override
    public boolean contains(E e) {
        return ((SetService<E>)that).contains(e);
    }

    @Override
    public boolean remove(E e) {
        throw new UnsupportedOperationException("Unmodifiable");
    }
     
    private static final long serialVersionUID = 5049669192830982105L;
}
