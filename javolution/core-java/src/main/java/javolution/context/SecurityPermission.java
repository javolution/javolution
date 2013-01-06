/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.context;

import javolution.annotation.StackSafe;

/**
 * A security permission associated to a specific class/action/instance. 
 * There are three levels of permission possible, at 
 * the class/category level, at the action level and at the instance level.
 * Any permission granted/revoked at the higer level is explicitly 
 * granted/revoked at the lower level. The order in which the permission 
 * are granted/revoked is important. For example, it is possible to grant 
 * a permission at the class level, then to revoke it at the action or 
 * instance level. In which case, for that class the permission is granted 
 * for all actions/instances except for those actions/instances for which the 
 * permission has been explicitly revoked.
 * 
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0, December 12, 2012
 * @see SecurityContext
 */
@StackSafe(initialization=false)
public class SecurityPermission<T> {

    /**
     * Holds the global permission for anything.
     */
    public static final SecurityPermission<?> ALL = new SecurityPermission(null);

    private final Class<T> category;

    private final String action;

    private final T instance;

    /**
     * Creates a security permission for all actions of the specified category.
     */
    public SecurityPermission(Class<T> category) {
        this(category, null, null);
    }

    /**
     * Creates a security permission for the specified action of the 
     * specified category.
     */
    public SecurityPermission(Class<T> category, String action) {
        this(category, action, null);
    }

    /**
     * Creates a security permission for the specified instance and the 
     * specified action of the specified category.
     */
    public SecurityPermission(Class<T> category, String action, T instance) {
        this.category = category;
        this.action = action;
        this.instance = instance;
    }

    /**
     * Returns the permission category or <code>null</code> for all categories.
     */
    public Class<T> getCategory() {
        return category;
    }

    /**
     * Returns the permission action or <code>null</code> for all actions.
     */
    public String getAction() {
        return action;
    }

    /**
     * Returns the permission instance or <code>null</code> for all instances.
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
    public boolean implies(SecurityPermission<?> that) {
        if (category == null) return true;
        if (!category.isAssignableFrom(that.category)) return false;
        if (action == null) return true;
        if (!action.equals(that.action)) return false;
        if (instance == null) return true;
        if (!instance.equals(that.instance)) return false;
        return true;
    }

    @Override
    public String toString() {
        if (category == null) return "All permissions";
        if (action == null)
            return "Permission for any action on " + category.getName();
        if (instance == null)
            return "Permission for " + action + " on " + category.getName();
        return "Permission for " + action + " on instance " + instance + " of " + category.getName();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof SecurityPermission)) return false;
        SecurityPermission that = (SecurityPermission) obj;
        if ((category == null) && (that.category != null)) return false;
        if ((category != null) && (!category.equals(that.category)))
            return false;
        if ((action == null) && (that.action != null)) return false;
        if ((action != null) && (!action.equals(that.action))) return false;
        if ((instance == null) && (that.instance != null)) return false;
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
