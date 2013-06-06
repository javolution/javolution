/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.internal.context;

import javolution.context.ImmortalContext;

/**
 * Holds the default implementation of ImmortalContext.
 * 
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0, December 12, 2012
 */
public final class ImmortalContextImpl extends ImmortalContext {

    // TODO: Use RTSJ Immortal Memory Memory.
    
    @Override
    protected void executeInContext(Runnable logic) {
        logic.run();
    }

    @Override
    protected ImmortalContext inner() {
        return this;
    }
}
