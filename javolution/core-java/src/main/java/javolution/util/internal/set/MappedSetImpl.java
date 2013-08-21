/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util.internal.set;

import javolution.util.function.Function;
import javolution.util.internal.collection.MappedCollectionImpl;
import javolution.util.service.SetService;

/**
 * A mapped view over a set.
 */
public abstract class MappedSetImpl<E, R> extends MappedCollectionImpl<E, R>
        implements SetService<R> {

    private static final long serialVersionUID = 0x600L; // Version.

    public MappedSetImpl(SetService<E> target,
            Function<? super E, ? extends R> function) {
        super(target, function);
    }

    @Override
    public abstract boolean add(R r);

    @Override
    public abstract boolean contains(Object r);

    @Override
    public abstract boolean remove(Object r);


    @SuppressWarnings("unchecked")
    @Override
    public SetService<R>[] split(int n, boolean updateable) {
        return new SetService[] { this }; // Split not supported.
    }    
}
