/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util.internal.set;

import javolution.util.internal.collection.UnmodifiableCollectionImpl;
import javolution.util.service.SetService;

/**
 * An unmodifiable view over a set.
 */
public class UnmodifiableSetImpl<E> extends UnmodifiableCollectionImpl<E>
        implements SetService<E> {

    private static final long serialVersionUID = 0x600L; // Version.

    public UnmodifiableSetImpl(SetService<E> target) {
        super(target);
    }

    @Override
    public SetService<E> threadSafe() {
        return this;
    }
}
