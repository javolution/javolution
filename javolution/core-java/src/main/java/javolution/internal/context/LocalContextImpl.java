/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.internal.context;

import javolution.context.LocalContext;
import javolution.context.LocalParameter;

/**
 * Holds the default implementation of LocalContext.
 * 
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0, December 12, 2012
 */
public final class LocalContextImpl extends LocalContext {

    @Override
    protected LocalContext inner() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public <T> void set(LocalParameter<T> param, T localValue) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected <T> T get(LocalParameter<T> param) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
