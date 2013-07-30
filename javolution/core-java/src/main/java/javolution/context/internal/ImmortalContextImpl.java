/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.context.internal;

import javolution.context.ImmortalContext;

/**
 * Holds the default implementation of ImmortalContext.
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
