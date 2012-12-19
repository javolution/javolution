/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.internal.context;

import javolution.context.SecurityContext;
import javolution.context.SecurityPermission;

/**
 * Holds the default implementation of SecurityContext.
 * 
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0, December 12, 2012
 */
public final class SecurityContextImpl extends SecurityContext {

    @Override
    public boolean isGranted(SecurityPermission<?> permission) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void grant(SecurityPermission permission, Object certificate) throws SecurityException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void revoke(SecurityPermission permission, Object certificate) throws SecurityException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected SecurityContext inner() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
