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

import org.javolution.annotations.Realtime;

/**
 * A function which does not take any argument and returns instances  of a particular class.
 *           
 * @param <T> The type of result this supplier returns.
 * 
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0, July 21, 2013
 */
public interface Supplier<T> extends java.util.function.Supplier<T> {

    /**
     * A supplier returning {@code null}.
     */
    @Realtime(limit = CONSTANT)
    public static final Supplier<Object> NULL = new Supplier<Object>() {

        @Override
        public Object get() {
            return null;
        }
    };

    /**
     * @return an object.
     */
    T get();

}