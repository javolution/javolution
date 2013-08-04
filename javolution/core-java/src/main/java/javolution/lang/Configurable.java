/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.lang;

import java.lang.reflect.Field;

import javolution.context.LogContext;
import javolution.context.SecurityContext;
import javolution.context.SecurityContext.Permission;
import javolution.osgi.internal.OSGiServices;

/**
 *  <p> An element which is configurable without presupposing how the
 *      configuration is done.</p>

 *  <p> Does your class need to know or has to assume that the configuration is
 *      coming from system properties ??</p>
 *
 *  <p> The response is obviously NO !</p>
 *
 *  <p> Let's compare the following examples:
 *  [code]
 *  class Document {
 *      private static final Font FONT
 *          = Font.decode(System.getProperty("pkg.Document#FONT") != null ?
 *              System.getProperty("FONT") : "Arial-BOLD-18");
 *  }[/code]</p>
 *  <p>With the following:
 *  [code]
 *  class Document {
 *      public static final Configurable<Font> FONT 
 *          = new Configurable<Font>(new Font("Arial", Font.BOLD, 18)) {
 *             protected Font parse(String str) {
 *                 return Font.decode(str);
 *             }
 *      };
 *  }[/code]</p>
 *  
 *  <p> Not only the second example is cleaner, but the actual configuration
 *      data can come from anywhere, for example from system properties 
 *      (default value), OSGi Configuration Admin service, 
 *      another bundle, etc. Low level code does not need to know.</p>
 *      
 *  <p> Users can be notified of configuration changes through the OSGi 
 *      {@link Configurable.Listener} service.</p>    
 *        
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0, July 21, 2013
 */
public abstract class Configurable<T> {

    /**
     * Services to be published by any one interested in being informed of 
     * configurable changes.
     */
    public interface Listener {

        /**
         * Receives notification that a configurable has been updated.
         * 
         * @param configurable the configurable instance being updated.
         * @param oldValue the old value.
         * @param newValue the new value.
         */
        <T> void configurableChanged(Configurable<T> configurable, T oldValue,
                T newValue);

    }

    /**
     * Holds the general permission to reconfigure configurables values
     * (action <code>"reconfigure"</code>).
     * Whether or not that permission is granted depends on the current 
     * {@link SecurityContext}. It is possible that the general permission 
     * to reconfigure a configurable is granted but revoked for a specific 
     * instance. Also, the general permission to reconfigure a configurable 
     * may be revoked but granted only for a specific instance.
     */
    public static Permission<Configurable<?>> RECONFIGURE_PERMISSION = new Permission<Configurable<?>>(
            Configurable.class, "reconfigure");

    /**
     * Holds the reconfigure permission.
     */
    private final Permission<Configurable<T>> reconfigurePermission;

    /**
     * Holds the configurable value.
     */
    private T value;

    /**
     * Creates a configurable having the specified value. If a system 
     * property exist for this configurable's {@link #getName() name}, the 
     * the {@link #parse parsed} value of the property supersedes the 
     * value specified. For example running the JVM with the option 
     * {@code -Djavolution.context.ConcurrentContext#CONCURRENCY=0} 
     * disables concurrency support.
     */
    public Configurable(T value) {
        reconfigurePermission = new Permission<Configurable<T>>(
                Configurable.class, "reconfigure", this);
        this.value = value;
        String name = getName();
        if (name == null)
            return; // Anonymous.
        try { // Check system properties.
            String property = System.getProperty(getName());
            if (property != null) {
                LogContext.info(name, " = ", property);
                this.value = parse(property);
            }
        } catch (SecurityException securityError) {
            // Current runtime does not allow system properties access.
        }
    }

    /**
     * Parses the specified text to return the corresponding value. 
     * This method is used when the configurable value is read from 
     * system properties.
     */
    protected abstract T parse(String str);

    /**
     * Returns this configurable value.
     */
    public T get() {
        return value;
    }

    /**
     * Returns this configurable name. By convention, the name of the 
     * configurable is the name of the static field holding the
     * configurable (e.g. "javolution.context.ConcurrentContext#CONCURRENCY").
     * This method should be overridden if the enclosing class needs to be 
     * impervious to obfuscation or if the enclosing class has multiple 
     * configurable fields.
     * 
     * @throws UnsupportedOperationException if the enclosing class has
     *         multiple configurable static fields.
     */
    public String getName() {
        Class<?> enclosingClass = this.getClass().getEnclosingClass();
        String fieldName = null;
        for (Field field : enclosingClass.getFields()) {
            if (Configurable.class.isAssignableFrom(field.getType())) {                
                if (fieldName != null)
                    throw new UnsupportedOperationException(
                            "Multiple configurable fields: "
                                    + fieldName
                                    + ", " 
                                    + field.getName()
                                    + ". Configurable.getName() should be overriden.");
                fieldName = field.getName();
            }
        }
        return (fieldName != null) ? enclosingClass.getName() + "#" + fieldName
                : null;
    }

    /**
     * Returns the permission to configure this instance.
     */
    public Permission<Configurable<T>> getReconfigurePermission() {
        return reconfigurePermission;
    }

    /**
     * Reconfigures this instance with the specified new value if authorized 
     * by the {@link SecurityContext}.
     * 
     * @param newValue the new value.
     * @throws SecurityException if the permission to reconfigure this 
     *         configurable is not granted.
     */
    public void reconfigure(T newValue) {
        SecurityContext.check(reconfigurePermission);
        synchronized (this) {
            T oldValue = this.value;
            this.value = newValue;
            Object[] listeners = OSGiServices.getConfigurableListeners();
            for (Object listener : listeners) {
                ((Listener) listener).configurableChanged(this, oldValue,
                        newValue);
            }
        }
    }

}
