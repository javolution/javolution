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
 * An operation that accepts a single input argument and returns no result.
 * Unlike most other functional interfaces, Consumers are expected to operate via side-effects.
 * 
 * Note: In future version this interface may derive from {@code Function<P, Void>}.
 *           
 * @param <T> The type of input parameter to accept.
 *           
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0, July 21, 2013
 */
public interface Consumer<T> extends java.util.function.Consumer<T> {

    /**
     * A consumer doing nothing.
     */
    @Realtime(limit = CONSTANT)
    public static final Consumer<Object> DO_NOTHING = new Consumer<Object>() {

        @Override
        public void accept(Object param) {
        }
    };

    /**
     * Accepts an input value.
     * @param param parameter to accept
     */
    void accept(T param);

}