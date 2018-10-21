/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.util.function;

import static org.javolution.annotations.Realtime.Limit.CONSTANT;
import static org.javolution.annotations.Realtime.Limit.UNKNOWN;

import java.io.Serializable;

import org.javolution.annotations.Nullable;
import org.javolution.annotations.ReadOnly;
import org.javolution.annotations.Realtime;
import org.javolution.lang.Immutable;

/**
 * A function (functional interface) associating an unsigned 64-bits index value to an object.
 * 
 * @param <T> the type of objects that may be compared for equality.
 * 
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 7.0 September 13, 2015
 */
@ReadOnly
public interface Indexer<T> extends Immutable, Serializable {

    /**
     * The hash indexer (based on {@link Object#hashCode}). 
     */
    @Realtime(limit = UNKNOWN)
    static <T> Indexer<T> hash() {
    	return (obj) -> (obj != null) ? obj.hashCode() : 0;
	}

    /**
     * The hash indexer (based on {@link System#identityHashCode}). 
     */
    @Realtime(limit = CONSTANT)
    static <T> Indexer<T> identityHash() {
    	return (obj) -> System.identityHashCode(obj);
	}

    /**
     * Returns the index (unsigned 64-bits value) for the specified object.
     * 
     * @param obj the object for which the index is calculated.
     * @return the corresponding index (unsigned 64-bits).
     */
    long indexOf(@Nullable T obj);

}