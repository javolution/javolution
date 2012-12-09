/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.context;


/**
 * This class represents a security permission associated to a specific 
 * class/interface. There are three levels of permission possible, at 
 * the class level, at the action level or at the instance level.
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
public class SecurityPermission<T> {
    
    public static SecurityPermission<?> ALL = new SecurityPermission(null);
    
    private final Class<T> target;
    private final String action;
    private final T instance;
    
    public SecurityPermission(Class<T> target) {
        this(target, null, null);
    }

    public SecurityPermission(Class<T> target, String action) {
        this(target, action, null);
    }
    
    public SecurityPermission(Class<T> target, String action, T instance) {
        this.target = target;
        this.action = action;
        this.instance = instance;
    }

    @Override
    public String toString() {
        if (target == null) return "All permissions";
        if (action == null) return "Permission for any action on " + target.getName();
        if (instance == null) return "Permission for " + action + " on " + target.getName();
        return "Permission for " + action + " on instance " + instance + " of " + target.getName();
    }
}
