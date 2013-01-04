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

/**
 * Holds the default implementation of StackContext.
 * 
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0, December 12, 2012
 */
public final class StackContextImpl extends StackContext {

   @Override
    protected void executeInContext(Runnable logic) {
        this.enterScope();
        try {
            logic.run();
        } finally {
            this.exit();
        }
    }

    @Override
    protected StackContext inner() {
        return this;
    }

}
