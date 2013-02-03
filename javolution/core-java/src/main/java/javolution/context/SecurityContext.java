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
 * <p> A high-level security context integrated with OSGi.</p>
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
 *         ctx.grant(ConcurrentContext.CONCURRENCY.getOverridePermission(), adminCertificate);
 *         ...
 *         ConcurrentContext.CONCURRENCY.configure("0"); // Ok (disables concurrency).
 *         ...
 *     } finally {
 *         ctx.exit(); // Back to previous security settings. 
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
    public static final Configurable<Boolean> WAIT_FOR_SERVICE = new Configurable<Boolean>(false) {

        @Override
        public void configure(CharSequence configuration) {
            setDefaultValue(TypeFormat.parseBoolean(configuration));
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
    public static SecurityContext enter() {
        return SecurityContext.current().inner().enterScope();
    }

    /**
     * Checks if the specified permission is granted. 
     *
     * @param permission the permission to check.
     * @throws SecurityException if the specified permission is not granted.
     */
    public static void check(SecurityPermission<?> permission) {
        if (!SecurityContext.current().isGranted(permission))
            throw new SecurityException(permission + " is not granted.");
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
    public abstract void grant(SecurityPermission<?> permission, Object certificate);

    /**
     * Revokes the specified permission.
     * 
     * @param permission the permission to grant.
     * @param certificate  the certificate used to grant that permission or 
     *        <code>null</code> if none.
     * @throws SecurityException if the specified permission cannot be revoked.
     */
    public abstract void revoke(SecurityPermission<?> permission, Object certificate);

    /**
     * Grants the specified permission (convenience method).
     * 
     * @param permission the permission to grant.
     * @throws SecurityException if the specified permission cannot be granted.
     */
    public final void grant(SecurityPermission<?> permission) {
        grant(permission, null);
    }

    /**
     * Revokes the specified permission (convenience method).
     * 
     * @param permission the permission to grant.
     * @throws SecurityException if the specified permission cannot be revoked.
     */
    public final void revoke(SecurityPermission<?> permission) {
        revoke(permission, null);
    }

    /**
     * Returns the current security context. 
     */
    protected static SecurityContext current() {
        SecurityContext ctx = AbstractContext.current(SecurityContext.class);
        if (ctx != null) return ctx;
        return SECURITY_CONTEXT_TRACKER.getService(WAIT_FOR_SERVICE.getDefaultValue());
    }

}
