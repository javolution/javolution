/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util.internal.set;

import javolution.util.internal.collection.AtomicCollectionImpl;
import javolution.util.service.SetService;

/**
 * An atomic view over a set allowing concurrent access and sequential updates.
 */
public class AtomicSetImpl<E> extends AtomicCollectionImpl<E> implements
        SetService<E> {

    private static final long serialVersionUID = 0x600L; // Version.

    public AtomicSetImpl(SetService<E> target) {
        super(target);
    }

    @SuppressWarnings("unchecked")
    @Override
    public SetService<E>[] split(int n, boolean updateable) { 
        return new SetService[] { this }; // Split not supported.
    }
 }
