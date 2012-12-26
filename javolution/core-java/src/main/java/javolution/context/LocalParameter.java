/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.context;

import javolution.lang.Configurable;

/**
 * <p> This class represents a local parameter. The default value of 
 *     a local parameter is {@link Configurable configurable}. Its local value
 *     is given by the {@link LocalContext#valueOf} method and can be locally
 *     {@link LocalContext#setLocalValue overriden}.</p>
 * 
 * <p> The parameter default values is either the value specified at creation
 *     or a configuration value read from system properties. For example the
 *     java option <code>-Djavolution.context.ConcurrentContext#CONCURRENCY=0</code>
 *     disables concurrency.</p>
 * 
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0, December 12, 2012
 */
public abstract class LocalParameter<T> extends Configurable<T> {

    /**
     * Holds the general permission to override a local parameter 
     * (action <code>"override"</code>).
     * @see LocalContext#setLocalValue
     */
    public static final SecurityPermission<LocalParameter> OVERRIDE_PERMISSION = new SecurityPermission(LocalParameter.class, "override");

    /**
     * Holds this instance override permission.
     */
    private final SecurityPermission<LocalParameter> overridePermission;

    /**
     * Creates a local parameter having the specified default value (configurable).
     * 
     * @param defaultValue 
     */
    protected LocalParameter(T defaultValue) {
        super(defaultValue);
        overridePermission = new SecurityPermission(LocalParameter.class, "override", this);
    }

    /**
     * Returns the permission to override this instance.
     * 
     * @see LocalContext#setLocalValue
     */
    public SecurityPermission<LocalParameter> getOverridePermission() {
        return overridePermission;
    }

}
