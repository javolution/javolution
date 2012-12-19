/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.internal.context;

import javolution.context.ConcurrentContext;

/**
 * Holds the default implementation of ConcurrentContext.
 * 
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0, December 12, 2012
 */
public final class ConcurrentContextImpl extends ConcurrentContext {

    @Override
    public void execute(Runnable logic) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected ConcurrentContext inner() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
