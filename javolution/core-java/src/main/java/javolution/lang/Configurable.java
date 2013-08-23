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
import javolution.text.DefaultTextFormat;
import javolution.text.TextContext;

/**
 * <p> An element which is configurable without presupposing how the
 *     configuration is done.</p>
 *     
 * <p> Does your class need to know or has to assume that the configuration is
 *      coming from system properties ??</p>
 *      
 * <p> The response is obviously NO !</p>
 *
 * <p> Let's compare the following examples:
 * [code]
 * class Document {
 *     private static final Font FONT
 *         = Font.decode(System.getProperty("pkg.Document#FONT") != null ?
 *             System.getProperty("FONT") : "Arial-BOLD-18");
 * }[/code]</p>
 * 
 * <p>With the following:
 * [code]
 * class Document {
 *     public static final Configurable<Font> FONT = new Configurable<Font>() {
 *         @Override
 *         protected Font getDefault() { 
 *             new Font("Arial", Font.BOLD, 18);
 *         }
 *     };
 * }[/code]</p>
 *  
 * <p> Not only the second example is cleaner, but the actual configuration
 *     data can come from anywhere, from system properties (default), 
 *     OSGi Configuration Admin service, another bundle, etc. 
 *     Low level code does not need to know.</p>
 *      
 * <p> Configurables may perform any logic upon initialization or 
 *     update. Users are notified of configuration events through 
 *     the OSGi {@link Configurable.Listener} service.
 * [code]
 * class Index {
 *     // Holds the number of unique preallocated instances (default {@code 1024}).  
 *     public static final Configurable<Integer> UNIQUE = new Configurable<Integer>() {
 *         @Override
 *         protected Integer getDefault() { 
 *             return 1024;
 *         }
 *         @Override
 *         protected Integer initialized(Integer value) {
 *             return MathLib.min(value, 65536); // Hard-limiting
 *         }
 *         @Override
 *         protected Integer reconfigured(Integer oldCount, Integer newCount) {
 *             throw new UnsupportedOperationException("Unicity reconfiguration not supported."); 
 *         }               
 *     }
 * }[/code]</p>
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
         * Receives notification that a configurable has been initialized..
         * 
         * @param configurable the configurable instance being initialized.
         * @param value the initial value.
         */
        <T> void configurableInitialized(Configurable<T> configurable, T value);

        /**
         * Receives notification that a configurable has been updated.
         * 
         * @param configurable the configurable instance being updated.
         * @param oldValue the previous value.
         * @param newValue the updated value.
         */
        <T> void configurableReconfigured(Configurable<T> configurable,
                T oldValue, T newValue);

    }

    /**
     * Holds the general permission to reconfigure configurable instances
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
     * Holds the configurable name.
     */
    private String name;

    /**
     * Holds the reconfigure permission.
     */
    private final Permission<Configurable<T>> reconfigurePermission;

    /**
     * Holds the configurable value.
     */
    private volatile T value;

    /**
     * Creates a new configurable. If a system property exist for this 
     * configurable's {@link #getName() name}, then 
     * the {@link #parse parsed} value of the property supersedes the 
     * {@link #getDefault() default} value of this configurable. 
     * For example, running the JVM with
     * the option {@code -Djavolution.context.ConcurrentContext#CONCURRENCY=0} 
     * disables concurrency support.
     */
    public Configurable() {
        reconfigurePermission = new Permission<Configurable<T>>(
                Configurable.class, "reconfigure", this);
        String name = getName();
        T defaultValue = getDefault();
        if (name != null) {
            try { // Checks system properties.
                String property = System.getProperty(name);
                if (property != null) {
                    defaultValue = parse(property); // Supersedes.
                    LogContext.debug(name, ", System Properties Value: ",
                            defaultValue);
                }
            } catch (SecurityException securityError) {
                // Ok, current runtime does not allow system properties access.
            }
        }
        this.name = name;
        this.value = initialized(defaultValue);
        Object[] listeners = OSGiServices.getConfigurableListeners();
        for (Object listener : listeners) {
            ((Listener) listener).configurableInitialized(this, this.value);
        }
    }

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
     * impervious to obfuscation or if the enclosing class defines multiple 
     * configurable fields.
     * 
     * @throws UnsupportedOperationException if the enclosing class has
     *         multiple configurable static fields.
     */
    public String getName() {
        if (name != null)
            return name; // Already set.
        Class<?> thisClass = this.getClass();
        Class<?> enclosingClass = thisClass.getEnclosingClass();
        String fieldName = null;
        for (Field field : enclosingClass.getFields()) {
            if (field.getType().isAssignableFrom(thisClass)) {
                if (fieldName != null) // Indistinguishable field types.
                    throw new UnsupportedOperationException(
                       "Multiple configurables static fields in the same class" +
                       "requires the Configurable.getName() method to be overriden.");
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
     * Reconfigures this instance with the specified value if authorized 
     * by the {@link SecurityContext}. This method returns the actual new 
     * value which may be different from the requested new value 
     * (see {@link #reconfigured(Object, Object)}).
     *    
     * @param newValue the requested new value.
     * @return the actual new value.
     * @throws SecurityException if the permission to reconfigure this 
     *         configurable is not granted.
     * @throws UnsupportedOperationException if this configurable does not 
     *         support dynamic reconfiguration.
     */
    public T reconfigure(T newValue) {
        SecurityContext.check(reconfigurePermission);
        synchronized (this) {
            T oldValue = this.value;
            this.value = reconfigured(oldValue, newValue);
            Object[] listeners = OSGiServices.getConfigurableListeners();
            for (Object listener : listeners) {
                ((Listener) listener).configurableReconfigured(this, oldValue,
                        this.value);
            }
            return this.value;
        }
    }

    /**
     * Returns this configurable default value (always different from 
     * {@code null}).
     */
    protected abstract T getDefault();

    /**
     * This methods is called when the configurable is initialized.
     * Developers may override this method to perform 
     * any initialization logic (e.g. input validation).
     * 
     * @param value the requested value for this configurable.
     * @return the actual value of this configurable. 
     */
    protected T initialized(T value) {
        return value;
    }

    /**
     * Parses the specified text to return the corresponding value. 
     * This method is used to initialize this configurable from 
     * system properties. The default implementation uses the 
     * {@link TextContext} to retrieve the text format 
     * (based on the {@link DefaultTextFormat} class annotation). 
     */
    @SuppressWarnings("unchecked")
    protected T parse(String str) {
        Class<? extends T> type = (Class<? extends T>) getDefault().getClass();
        return TextContext.getFormat(type).parse(str);
    }

    /**
     * This methods is called when the configurable is reconfigured.
     * Developers may override this method to perform 
     * any reconfiguration logic (e.g. hard limiting values).
     * 
     * @param oldValue the old value.
     * @param newValue the requested new value.
     * @return the actual new value of this configurable. 
     * @throws UnsupportedOperationException if this configurable does not 
     *         support dynamic reconfiguration.
     */
    protected T reconfigured(T oldValue, T newValue) {
        return newValue;
    }
}
