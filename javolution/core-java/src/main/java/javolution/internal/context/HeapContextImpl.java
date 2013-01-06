/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.internal.context;

import javolution.context.HeapContext;

/**
 * Holds the default implementation of HeapContext.
 * 
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0, December 12, 2012
 */
public final class HeapContextImpl extends HeapContext {

    // TODO: Use RTSJ Heap Memory.
    
    @Override
    protected void executeInContext(Runnable logic) {
        logic.run();
    }

    @Override
    protected HeapContext inner() {
        return this;
    }
}
