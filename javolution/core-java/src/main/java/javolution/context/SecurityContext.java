/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2007 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.context;

import static javolution.internal.osgi.JavolutionActivator.SECURITY_CONTEXT_TRACKER;
import javolution.lang.Configurable;
import javolution.text.TypeFormat;

/**
 * <p> This class represents the current high-level security context (low level 
 *     security being addressed by the system security manager). It defines 
 *     whether or not {@link SecurityPermission} are granted or not.</p>
 *     
 * <p> To ensure that your OSGi published system security context is always used, 
 *     the configurable parameter {@link #WAIT_FOR_SERVICE} should be set
 *     (especially knowing that the default implementation grants all permissions).
 *     When granting/revoking permission the order is very important. 
 *     For example, the following code revokes all configurable permissions 
 *     except for concurrency settings.
 *     [code]
 *     SecurityContext ctx = SecurityContext.enter(); 
 *     try {
 *         ctx.revoke(Configurable.CONFIGURE_PERMISSION, adminCertificate);
 *         ctx.grant(
 *             new SecurityPermission(Configurable.class, "configure", ConcurrentContext.CONCURRENCY), 
 *             adminCertificate);
 *         ...
 *         ConcurrentContext.CONCURRENCY.configure("0"); // Ok (disables concurrency).
 *         ...
 *     } finally {
 *         SecurityContext.exit(); // Back to previous security settings. 
 *     }
 *     [/code]</p>
 * 
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0, December 12, 2012
 */
public abstract class SecurityContext extends AbstractContext<SecurityContext> {

   /**
     * Indicates whether or not static methods will block for an OSGi published
     * implementation this class (default configuration <code>false</code>).
     */
    public static final Configurable<Boolean> WAIT_FOR_SERVICE = new Configurable(false) {
        @Override
        public void configure(CharSequence configuration) {
            setDefault(TypeFormat.parseBoolean(configuration));
        }
    };
 
    /**
     * Default constructor.
     */
    protected SecurityContext() {
    }
    /**
     * Enters a new security context instance.
     * 
     * @return the new security context implementation entered. 
     */
    public static SecurityContext enter() throws SecurityException {
        SecurityContext ctx = AbstractContext.current(SecurityContext.class);
        if (ctx != null) return ctx.inner().enterScope();
        return SECURITY_CONTEXT_TRACKER.getService(WAIT_FOR_SERVICE.getDefault()).inner().enterScope();
    }

    /**
     * Checks if the specified permission is granted. 
     *
     * @param permission the permission to check.
     * @throws SecurityException if the specified permission is not granted.
     */
    public static void check(SecurityPermission permission) throws SecurityException, IllegalStateException {
        SecurityContext ctx = AbstractContext.current(SecurityContext.class);
        if (ctx != null) {
            ctx = SECURITY_CONTEXT_TRACKER.getService(WAIT_FOR_SERVICE.getDefault());
        }
        if (!ctx.isGranted(permission)) throw new SecurityException(permission + " is not granted.");
    }

    /**
     * Indicates if the specified permission is granted.
     *
     * @param permission the permission to check.
     */
    public abstract boolean isGranted(SecurityPermission<?> permission);

    /**
     * Grants the specified permission.
     * 
     * @param permission the permission to grant.
     * @param certificate  the certificate used to grant that permission or 
     *        <code>null</code> if none.
     * @throws SecurityException if the specified permission cannot be granted.
     */
    public abstract void grant(SecurityPermission permission, Object certificate) throws SecurityException;

    /**
     * Revokes the specified permission.
     * 
     * @param permission the permission to grant.
     * @param certificate  the certificate used to grant that permission or 
     *        <code>null</code> if none.
     * @throws SecurityException if the specified permission cannot be revoked.
     */
    public abstract void revoke(SecurityPermission permission, Object certificate) throws SecurityException;

    /**
     * Exits the scope of this security context; reverts to the security settings 
     * before this context was entered.
     * 
     * @throws IllegalStateException if this context is not the current 
     *         context.
     */
    @Override
    public void exit() throws IllegalStateException {
        super.exit();
    }
}
