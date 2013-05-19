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
import javolution.lang.Permission;
import javolution.util.FastTable;

/**
 * Holds the default implementation of SecurityContext.
 * 
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0, December 12, 2012
 */
public final class SecurityContextImpl extends SecurityContext {
    
    private FastTable<Action> actions = new FastTable<Action>(); 

    @Override
    public boolean isGranted(Permission<?> permission) {
        boolean isGranted = true;
        for (Action a  : actions) {
            if (a.permission.implies(permission)) isGranted = a.grant;
        }
        return isGranted;
    }

    @Override
    public void grant(Permission<?> permission, Object certificate) throws SecurityException {
        Action a = new Action();
        a.grant = true;
        a.permission = permission;
        actions.add(a);
    }

    @Override
    public void revoke(Permission<?> permission, Object certificate) throws SecurityException {
        Action a = new Action();
        a.grant = false;
        a.permission = permission;
        actions.add(a);
    }

    @Override
    protected SecurityContext inner() {
        SecurityContextImpl ctx = new SecurityContextImpl();
        ctx.actions.addAll(actions);
        return ctx;
    }

    // Represents the grant/revoke action performed. 
    private static class Action {
        boolean grant; // Else revoke.
        Permission<?> permission;
    }
}
