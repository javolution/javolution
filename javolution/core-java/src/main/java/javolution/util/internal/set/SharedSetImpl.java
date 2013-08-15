/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util.internal.set;

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

    @Override
    public SetService<E> threadSafe() {
        return this;
    }
}
