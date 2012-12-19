/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.internal.context;

import javolution.context.LogContext;


/**
 * Holds the default implementation of LogContext.
 * 
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0, December 12, 2012
 */
public final class LogContextImpl extends LogContext {

    @Override
    public void attach(Object property, Object propertyValue) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void suppress(Level level) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void log(Level level, Object... objs) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected LogContext inner() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
