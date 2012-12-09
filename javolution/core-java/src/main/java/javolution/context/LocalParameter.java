/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2006 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.context;

import javolution.lang.Configurable;

/**
 * <p> This class represents a local parameter. The default value of 
 *     a local parameter is {@link Configurable}. Its actual value is
 *     given by the {@link LocalContext#valueOf} method and can be 
 *     {@link LocalContext#override overriden}.</p>
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0, December 12, 2012
 */
public abstract class LocalParameter<T> implements Configurable {

    private T defaultValue;
    private final SecurityPermission<LocalParameter> configurePermission;
    private final SecurityPermission<LocalParameter> overridePermission;

    /**
     * Creates a local parameter having the specified default value (configurable).
     * 
     * @param defaultValue 
     */
    public LocalParameter(T defaultValue) {
        this.defaultValue = defaultValue;
        configurePermission = new SecurityPermission<LocalParameter>(
                LocalParameter.class, "configure", this);
        overridePermission = new SecurityPermission<LocalParameter>(
                LocalParameter.class, "override", this);
    }

    /**
     * Returns this local parameter default value.
     */
    public T getDefault() {
        return defaultValue;
    }

    /**
     * Returns the permission to configure the default value of this local 
     * parameter.
     */
    public SecurityPermission<LocalParameter> getConfigurePermission() {
        return configurePermission;
    }

    /**
     * Returns the permission to override the current value of this local 
     * parameter when executing in a {@link LocalContext}.
     */
    public SecurityPermission<LocalParameter> getOverridePermission() {
        return overridePermission;
    }

    /**
     * Sets the default value of the configurable local parameter.
     * 
     * @throws SecurityException if {@link #getConfigurePermission} is not 
     *         granted.
     */
    protected void setDefault(T newDefault) {
        SecurityContext.check(configurePermission);
        defaultValue = newDefault;
    }

    /**
     * Configures the default value of this local parameter.
     * 
     * @param configuration the textual representation of the new default value. 
     * @throws SecurityException if {@link #getConfigurePermission} is not 
     *         granted.
     */
    public abstract void configure(CharSequence configuration);
}
