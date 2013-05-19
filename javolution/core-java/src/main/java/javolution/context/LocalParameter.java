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
import javolution.lang.Configurable;
import javolution.lang.Permission;

/**
 * <p> A local parameter with {@link Configurable configurable} default value. 
 *     The current value can locally {@link LocalContext#override 
 *     overridden} when running in the scope of a {@link LocalContext}.</p>
 * 
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0, December 12, 2012
 */
@StackSafe
public class LocalParameter<T> {

    /**
     * Holds the general permission to override local parameters (action <code>
     * "override"</code>).
     */
    public static final Permission<LocalParameter<?>> OVERRIDE_PERMISSION 
        = new Permission<LocalParameter<?>>(
              LocalParameter.class, "override");

    /**
     * Holds the default value.
     */
    private final Configurable<T> defaultValue;

    /**
     * Holds this instance override permission.
     */
    private final Permission<LocalParameter<T>> overridePermission;

    /**
     * Creates a local parameter having the specified default value
     * (configurable).
     */
    public LocalParameter(T defaultValue) {
        this.defaultValue = new Configurable<T>(defaultValue);
        this.overridePermission = new Permission<LocalParameter<T>>(
                LocalParameter.class, "override", this);
    }

    /**
     * Returns the permission to override the current value of this instance.
     * 
     * @see LocalContext#override
     */
    public Permission<LocalParameter<T>> getOverridePermission() {
        return overridePermission;
    }

    /**
     * Returns this local parameter current value (the default value if not 
     * {@link LocalContext#override overridden}).
     */
    public T get() {
        LocalContext ctx = AbstractContext.current(LocalContext.class);
        return (ctx != null) ? ctx.getLocalValueInContext(this) : getDefault().get();
    }

    /**
     * Returns this local parameter default value.
     * 
     * @see LocalContext#setLocalValue
     */
    public Configurable<T> getDefault() {
        return defaultValue;
    }
}
