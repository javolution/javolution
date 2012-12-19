/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.context;

import java.lang.reflect.Field;
import javolution.lang.Configurable;

/**
 * <p> This class represents a local parameter. The default value of 
 *     a local parameter is {@link Configurable}. Its local value is
 *     given by the {@link LocalContext#valueOf} method and can be locally
 *     {@link LocalContext#override overriden}.</p>
 * 
 * <p> The parameter default values is either the value specified at creation
 *     or a configuration value read from system properties. For example the
 *     java option <code>-Djavolution.context.ConcurrentContext#CONCURRENCY=0</code>
 *     disables concurrency.</p>
 * 
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0, December 12, 2012
 */
public abstract class LocalParameter<T> implements Configurable {

    /**
     * Holds the general permission to override a local parameter 
     * (action <code>"override"</code>).
     */
    public static final SecurityPermission<LocalParameter> OVERRIDE_PERMISSION 
            = new SecurityPermission(LocalParameter.class, "override");
    
    // Private members
    private T defaultValue;
    private final SecurityPermission<LocalParameter> configurePermission;
    private final SecurityPermission<LocalParameter> overridePermission;
    private final String name;

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
        name = LocalParameter.nameOf(this);
        if (name != null) { // Reads system properties for default value.
            try {
                String configuration = System.getProperty(name);
                configure(configuration);   
            } catch (Throwable e) {
                LogContext.error(e);
            }            
        }        
    }
    
    /**
     * Configures the default value of this local parameter.
     * 
     * @param configuration the textual representation of the new default value. 
     * @throws SecurityException if {@link #getConfigurePermission} is not 
     *         granted.
     */
    public abstract void configure(CharSequence configuration);

    /**
     * Returns the local value of this parameter.
     * 
     * @return <code>LocalContext.valueOf(this)</code>
     */
    public T get() {
        return LocalContext.valueOf(this);
    }

    /**
     * Returns the name of this local parameter.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns this local parameter default value.
     */
    public T getDefault() {
        return defaultValue;
    }

    /**
     * This methods checks if this parameter default value can be changed.
     * Subclasses may override this method to raise an exception if this 
     * parameter can never be configured (but only locally overriden).
     * 
     * @throws SecurityException if 
     *         <code>SecurityPermission(LocalParameter.class, "configure", this)</code>
     *         is not granted.
     */
    public void checkConfigurePermission() throws SecurityException {
        SecurityContext.check(configurePermission);
    }

    /**
     * This methods checks if this parameter can be locally overriden.
     * Subclasses may override this method to raise an exception if this 
     * parameter can never be overriden (but only configured).
     * 
     * @throws SecurityException if 
     *         <code>SecurityPermission(LocalParameter.class, "override", this)</code>
     *         is not granted.
     */
    public void checkOverridePermission() throws SecurityException {
        SecurityContext.check(overridePermission);
    }

    /**
     * Returns the name of this local parameter.
     * @see #getName() 
     */
    @Override
    public String toString() {
        return name;
    }

    /**
     * Sets the default value of the configurable local parameter.
     * 
     * @param newDefault the new default value.
     * @throws SecurityException if {@link #checkConfigurePermission() raises 
     *         an exception.
     */
    protected void setDefault(T newDefault) {
        checkConfigurePermission();
        defaultValue = newDefault;
    }

  // Returns the full name of this local parameter (complete field name)
    private static String nameOf(LocalParameter<?> param) {
        Class<?> paramClass = param.getClass();
        Class<?> enclosingClass = paramClass.getEnclosingClass();
        Field[] fields = enclosingClass.getDeclaredFields();
        String name = null;
        for (Field field : fields) {
            if (field.getDeclaringClass().equals(paramClass)) {
                name = field.getName();
                break;
            }
        }
        return name;        
    }
}
