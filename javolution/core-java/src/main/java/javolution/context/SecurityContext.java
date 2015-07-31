/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2007 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.context;

import javolution.osgi.internal.OSGiServices;

/**
 * <p> A high-level security context integrated with OSGi.</p>
 *     
 * <p> When granting/revoking permission the order is important. 
 *     For example, the following code revokes all configurable permissions 
 *     except for setting the concurrency level.
 * {@code
 * SecurityContext ctx = SecurityContext.enter(); 
 * try {
 *     ctx.revoke(Configurable.RECONFIGURE_PERMISSION);
 *     ctx.grant(ConcurrentContext.CONCURRENCY.getReconfigurePermission());
 *     ...
 *     ConcurrentContext.CONCURRENCY.reconfigure(0); // Ok (permission specifically granted).
 *     ...
 *  } finally {
 *     ctx.exit(); // Back to previous security settings. 
 *  }}</p>
 * 
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0, July 21, 2013
 */
public abstract class SecurityContext extends AbstractContext {

    /**
     * A permission associated to a specific class/action/instance. 
     * There are three levels of permission possible, at 
     * the class/category level, at the action level and at the instance level.
     * Any permission granted/revoked at the higher level is explicitly 
     * granted/revoked at the lower level. The order in which the permission 
     * are granted/revoked is important. For example, it is possible to grant 
     * a permission at the class level, then to revoke it at the action or 
     * instance level. In which case, for that class the permission is granted 
     * for all actions/instances except for those actions/instances for which the 
     * permission has been explicitly revoked.
     */
    public static class Permission<T> {

        /**
         * Holds the global permission for anything.
         */
        public static final Permission<Object> ALL = new Permission<Object>(
                null);

        private final Class<? super T> category;

        private final String action;

        private final T instance;

        /**
         * Creates a security permission for all actions of the specified category.
         * @param category Category to create a Permission for
         */
        public Permission(Class<? super T> category) {
            this(category, null, null);
        }

        /**
         * Creates a security permission for the specified action of the 
         * specified category.
         * @param category Category to create a Permission for
         * @param action Action to create a Permission for
         */
        public Permission(Class<? super T> category, String action) {
            this(category, action, null);
        }

        /**
         * Creates a security permission for the specified instance and the 
         * specified action of the specified category.
         * @param category Category to create a Permission for
         * @param action Action to create a Permission for
         * @param instance Instance to create a permission for
         */
        public Permission(Class<? super T> category, String action, T instance) {
            this.category = category;
            this.action = action;
            this.instance = instance;
        }

        /**
         * @return the permission category or <code>null</code> for all categories.
         */
        public Class<? super T> getCategory() {
            return category;
        }

        /**
         * @return the permission action or <code>null</code> for all actions.
         */
        public String getAction() {
            return action;
        }

        /**
         * @return the permission instance or <code>null</code> for all instances.
         */
        public T getInstance() {
            return instance;
        }

        /**
         * Checks if the specified permission is automatically granted/revoked 
         * by 'this' permission being granted/revoked.
         * 
         * @param that the permission to check.
         * @return <code>true</code> if this permission being granted/revoked 
         *         implies that the specified permission is granted/revoked;
         *         <code>false</code> otherwise.
         */
        public boolean implies(Permission<?> that) {
            if (category == null)
                return true;
            if (!category.isAssignableFrom(that.category))
                return false;
            if (action == null)
                return true;
            if (!action.equals(that.action))
                return false;
            if (instance == null)
                return true;
            if (!instance.equals(that.instance))
                return false;
            return true;
        }

        @Override
        public String toString() {
            if (category == null)
                return "All permissions";
            if (action == null)
                return "Permission for any action on " + category.getName();
            if (instance == null)
                return "Permission for " + action + " on " + category.getName();
            return "Permission for " + action + " on instance " + instance
                    + " of " + category.getName();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this)
                return true;
            if (!(obj instanceof Permission))
                return false;
            Permission<?> that = (Permission<?>) obj;
            if ((category == null) && (that.category != null))
                return false;
            if ((category != null) && (!category.equals(that.category)))
                return false;
            if ((action == null) && (that.action != null))
                return false;
            if ((action != null) && (!action.equals(that.action)))
                return false;
            if ((instance == null) && (that.instance != null))
                return false;
            if ((instance != null) && (!instance.equals(that.instance)))
                return false;
            return false;
        }

        @Override
        public int hashCode() {
            return (category != null ? category.hashCode() : 0)
                    + (action != null ? action.hashCode() : 0)
                    + (instance != null ? instance.hashCode() : 0);
        }
    }

    /**
     * Default constructor.
     */
    protected SecurityContext() {}

    /**
     * Enters and returns a new security context instance.
     * 
     * @return the new security context implementation entered. 
     */
    public static SecurityContext enter() {
        return (SecurityContext) currentSecurityContext().enterInner();
    }

    /**
     * Checks if the specified permission is granted. 
     *
     * @param permission the permission to check.
     * @throws SecurityException if the specified permission is not granted.
     */
    public static void check(Permission<?> permission) {
        if (!currentSecurityContext().isGranted(permission))
            throw new SecurityException(permission + " is not granted.");
    }

    /**
     * Indicates if the specified permission is granted.
     *
     * @param permission the permission to check.
     * @return true if permission is granted
     */
    public abstract boolean isGranted(Permission<?> permission);

    /**
     * Grants the specified permission.
     * 
     * @param permission the permission to grant.
     * @param certificate  the certificate used to grant that permission or 
     *        <code>null</code> if none.
     * @throws SecurityException if the specified permission cannot be granted.
     */
    public abstract void grant(Permission<?> permission, Object certificate);

    /**
     * Revokes the specified permission.
     * 
     * @param permission the permission to grant.
     * @param certificate  the certificate used to grant that permission or 
     *        <code>null</code> if none.
     * @throws SecurityException if the specified permission cannot be revoked.
     */
    public abstract void revoke(Permission<?> permission, Object certificate);

    /**
     * Grants the specified permission (convenience method).
     * 
     * @param permission the permission to grant.
     * @throws SecurityException if the specified permission cannot be granted.
     */
    public final void grant(Permission<?> permission) {
        grant(permission, null);
    }

    /**
     * Revokes the specified permission (convenience method).
     * 
     * @param permission the permission to grant.
     * @throws SecurityException if the specified permission cannot be revoked.
     */
    public final void revoke(Permission<?> permission) {
        revoke(permission, null);
    }

    /**
     * @return the current security context. 
     */
    private static SecurityContext currentSecurityContext() {
        SecurityContext ctx = current(SecurityContext.class);
        if (ctx != null)
            return ctx;
        return OSGiServices.getSecurityContext();
    }
}
