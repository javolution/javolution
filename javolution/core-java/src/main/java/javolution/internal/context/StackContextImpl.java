/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.internal.context;

import javolution.context.StackContext;
import javolution.lang.Copyable;
import javolution.lang.Functor;

/**
 * Holds the default implementation of StackContext.
 * 
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0, December 12, 2012
 */
public final class StackContextImpl extends StackContext {

    // TODO: Use RTSJ Scoped Memory.
    
    @Override
    protected <P, R extends Copyable<R>> R executeInContext(Functor<P, R> function, P parameter) {
        return function.evaluate(parameter);
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
