/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.util.function;

import java.io.Serializable;

import org.javolution.annotations.ReadOnly;
import org.javolution.lang.Immutable;

/**
 * A function (functional interface) associating an unsigned 32-bits index value to an object.
 * 
 * @param <T> the type of objects that may be compared for equality.
 * 
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 7.0 September 13, 2015
 */
@ReadOnly
public interface Indexer<T> extends Immutable, Serializable {


    /**
     * Returns the index (unsigned 32-bits value) of the specified object.
     * If the specified object is {@code null}, returns {@code 0}.
     * 
     * @param obj the object for which the index is calculated.
     * @return the corresponding index (unsigned).
     * @throws NullPointerException if the specified object is {@code null}
     */
    int indexOf(T obj);

}