/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.context.internal;

import javolution.context.StackContext;
import javolution.lang.Copyable;
import javolution.util.function.Function;

/**
 * Holds the default implementation of StackContext.
 */
public final class StackContextImpl extends StackContext {

    // TODO: Use RTSJ Scoped Memory.

    @Override
    protected <P, R extends Copyable<R>> R executeInContext(
            Function<P, R> function, P parameter) {
        return function.apply(parameter);
    }

    @Override
    protected void executeInContext(Runnable logic) {
        logic.run();
    }

    @Override
    protected StackContext inner() {
        return this;
    }

}
