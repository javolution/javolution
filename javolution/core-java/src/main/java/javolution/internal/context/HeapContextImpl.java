/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.internal.context;

import javolution.context.AllocatorContext;
import javolution.context.HeapContext;
import javolution.lang.Copyable;

/**
 * Holds the default implementation of HeapContext.
 * 
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0, December 12, 2012
 */
public final class HeapContextImpl extends HeapContext {

    @Override
    protected void execute(Runnable logic) {
        logic.run();
    }

    @Override
    protected <T extends Copyable> T copy(T copyable) {
        return copyable;
    }

    @Override
    protected AllocatorContext shared() {
        return this;
    }
    
}
