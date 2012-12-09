/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2007 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.context;

import javolution.internal.context.SecurityContextImpl;
import javolution.internal.osgi.JavolutionActivator;

/**
 * <p> This class represents a high-level security context (low level 
 *     security being addressed by the system security manager). It defines 
 *     whether or not {@link SecurityPermission} are granted or not.</p>
 *     
 * <p> Outside of any context scope, there is no security context and the only 
 *     permission granted is to enter a security context! Therefore, before 
 *     executing any code which requires permissions to be granted, it is 
 *     necessary to enter a security context and to grant the appropriate 
 *     permissions.
 *     [code]
 *     SecurityContext.enter();
 *     try {
 *         SecurityContext.grant(SecurityPermission.ALL);
 *         ...
 *     } finally {
 *         SecurityContext.exit(); // Back to previous security settings. 
 *     }
 *     [/code]
 * 
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0, December 12, 2012
 */
public abstract class SecurityContext extends AbstractContext<SecurityContext> {

    /**
     * Holds the permission to enter a new security context.
     */
    public static final SecurityPermission<SecurityContext> ENTER_PERMISSION = new SecurityPermission(SecurityContext.class, "enter");

    /**
     * Defines the factory producing {@link SecurityContext} implementations.
     */
    public interface Factory {

        /**
         * Returns a new instance of the security context.
         */
        SecurityContext newSecurityContext();
    }

    /**
     * Default constructor.
     */
    protected SecurityContext() {
    }

    /**
     * Checks if the specified permission is granted.
     *
     * @return the current security context.
     * @throws SecurityException if the permission is not granted.
     */
    public static void check(SecurityPermission permission) throws SecurityException {
        SecurityContext ctx = AbstractContext.current(SecurityContext.class);
        if ((ctx == null) && (permission.equals(ENTER_PERMISSION))) return; // Ok.
        if (ctx == null) throw new SecurityException(
                    "There is no SecurityContext!"
                    + " Make sure to enter a SecurityContext in order to grant "
                    + permission);
        if (!ctx.isGranted(permission))
            throw new SecurityException(permission + " is not granted.");
    }

    /**
     * Grants the specified permission.
     * 
     * @throws SecurityException if the permission cannot be granted or if there 
     *         is no SecurityContext outer scope.
     */
    public static void grant(SecurityPermission permission) throws SecurityException {
        SecurityContext ctx = AbstractContext.current(SecurityContext.class);
        if (ctx == null)
            throw new SecurityException("Not executing in a SecurityContext scope");
        ctx.doGrant(permission);
    }

    /**
     * Revokes the specified permission.
     * 
     * @throws SecurityException if the permission cannot be revoked or if there
     *         is no SecurityContext outer scope.
     */
    public static void revoke(SecurityPermission permission) throws SecurityException {
        SecurityContext ctx = AbstractContext.current(SecurityContext.class);
        if (ctx == null)
            throw new SecurityException("Not executing in a SecurityContext scope");
        ctx.doRevoke(permission);
    }

    /**
     * Enters a new security context instance.
     * 
     * @throws SecurityException if the {@link SecurityContext#ENTER_PERMISSION
     *         permission} to enter a security context is not granted.
     */
    public static void enter() throws SecurityException {
        SecurityContext.Factory factory = JavolutionActivator.getSecurityContextFactory();
        SecurityContext ctx = (factory != null) ? factory.newSecurityContext()
                : new SecurityContextImpl();
        ctx.enterScope();
    }

    /**
     * Exits the current security context.
     *
     * @throws ClassCastException if the current context is not a security context.
     */
    public static void exit() {
        ((SecurityContext) AbstractContext.current()).exitScope();
    }

    /**
     * Indicates if the specified permission is granted.
     *
     * @return the current security context.
     */
    public abstract boolean isGranted(SecurityPermission<?> permission);

    /**
     * Grants the specified permission.
     * 
     * @throws SecurityException if the permission cannot be granted.
     */
    public abstract void doGrant(SecurityPermission<?> permission) throws SecurityException;

    /**
     * Revokes the specified permission.
     * 
     * @throws SecurityException if the permission cannot be revoked.
     */
    public abstract void doRevoke(SecurityPermission<?> permission) throws SecurityException;

    /**
     * Overrides the parent method {@link AbstractContext#enterScope() } 
     * to check that {@link SecurityContext#ENTER_PERMISSION} is granted.
     * Revoking this permission prevents the application from overriding the 
     * current security policy with a new one.
     * 
     * @throws SecurityException if the permission to enter a security context
     *         is not granted.
     */
    @Override
    protected void enterScope() throws IllegalStateException, SecurityException {
        SecurityContext.check(SecurityContext.ENTER_PERMISSION);
        super.enterScope();
    }
}
