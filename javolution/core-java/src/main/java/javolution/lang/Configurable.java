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
import javolution.context.SecurityPermission;

/**
 *  <p> This class identifies the element which are configurable in your 
 *      code without presupposing how this configuration is done.</p>

 *  <p> Does your class need to know or has to assume that the configuration is
 *      coming from system properties ??</p>
 *
 *  <p> The response is obviously NO!</p>
 *
 *  <p> Let's compare the following examples:
 *      [code]
 *      class Document {
 *          private static final Font DEFAULT_FONT
 *              = Font.decode(System.getProperty("DEFAULT_FONT") != null ?
 *                  System.getProperty("DEFAULT_FONT") : "Arial-BOLD-18");
 *      }[/code]
 *      With the following:
 *      [code]
 *      class Document {
 *          public static final Configurable<Font> DEFAULT_FONT 
 *                  = new Configurable(new Font("Arial", Font.BOLD, 18)) {
 *              @Override
 *              public void configure(CharSequence configuration) {
 *                  setDefault(Font.decode(configuration));
 *              }
 *          };
 *      }[/code]
 *      Not only the second example is cleaner, but the actual configuration
 *      data can come from anywhere, for example from the OSGI Configuration 
 *      Admin package. Low level code does not need to know.</p>
 * 
 * <p> The configuration initial value is either the explicit value specified 
 *     at construction or the configuration (parsed) value from system 
 *     properties (the key is the full name of the class static field).
 *     For example, running with the option 
 *     <code>-Dfoo.bar.Document#DEFAULT_FONT=Courier-BOLD-18</code> sets  
 *     the document default font to courier.</p>
 *       
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0, December 12, 2012
 * @see     javolution.osgi.ConfigurableService
 */
public abstract class Configurable<T> {
    
    /**
     * Holds the general permission to configure a configurable instance
     * (action <code>"configure"</code>).
     * Whether or not that permission is granted depends on the current 
     * {@link SecurityContext}. It is possible that the general permission 
     * to update any configurable is granted but revoked for specific 
     * instances. Also, the general permission to update any configurable 
     * may be revoked but granted only for specific instances.
     */
    public static SecurityPermission<Configurable> CONFIGURE_PERMISSION
         = new SecurityPermission(Configurable.class, "configure");
    
    /**
     * Holds the configurable default value.
     */
    private T defaultValue;
    
    /**
     * Holds the full name of the configurable (name of the class member).
     */
    private final String name;
  
    /**
     * Holds this instance configure permission.
     */
    private final SecurityPermission<Configurable> configurePermission;
    
    /**
     * Creates a configurable having the specified default value.
     * 
     * @param defaultValue 
     */
    protected Configurable(T defaultValue) {
        this.defaultValue = defaultValue;
        configurePermission = new SecurityPermission<Configurable>(
                Configurable.class, "configure", this);
        name = Configurable.nameOf(this);
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
     * Configures this configurable from the specified textual configuration.
     * Decoding and meaning of the configuration may vary according to the
     * configurable instance.
     * 
     * @param configuration the configuration text.
     * @throws SecurityException if the permission to configure this configurable is not granted.
     * @throws IllegalArgumentException if the specified configuration if not valid.
     */
    public abstract void configure(CharSequence configuration) throws SecurityException, IllegalArgumentException;

    /**
     * Returns the name of this configurable (full name of the class static field).
     */
    public String getName() {
        return name;
    }

    /**
     * Returns this configurable default value.
     */
    public T getDefault() {
        return defaultValue;
    }  
    
    /**
     * Returns the permission to configure this instance.
     */
    public SecurityPermission<Configurable> getConfigurePermission() {
        return configurePermission;
    }  
    
    /**
     * Sets the configuration default value.
     * 
     * @param defaultValue the new default value.
     * @throws SecurityException if the permission to configure this configurable is not granted.
     */
    protected void setDefault(T defaultValue) {
        SecurityContext.check(configurePermission);
        this.defaultValue = defaultValue;
    }
    
    // Returns the full name of this configurable (field name)
    private static String nameOf(Configurable<?> param) {
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
