/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.internal.text;

import javolution.text.TextContext;
import javolution.text.TextFormat;

/**
 * Holds the default implementation of TextContext.
 * 
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0, December 12, 2012
 */
public final class TextContextImpl extends TextContext {

    @Override
    protected <T> TextFormat<T> findFormat(Class<T> cls) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected TextContext inner() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected <T> void setFormat(TextFormat<T> format, Class<T> cls) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
