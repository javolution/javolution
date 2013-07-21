/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.lang;

import javolution.context.SecurityContext;

/**
 *  <p> An element which is configurable without presupposing how the
 *      configuration is done.</p>

 *  <p> Does your class need to know or has to assume that the configuration is
 *      coming from system properties ??</p>
 *
 *  <p> The response is obviously NO!</p>
 *
 *  <p> Let's compare the following examples:
 *  [code]
 *  class Document {
 *      private static final Font FONT
 *          = Font.decode(System.getProperty("FONT") != null ?
 *              System.getProperty("FONT") : "Arial-BOLD-18");
 *  }[/code]
 *  
 *      With the following:
 *  
 *  [code]
 *  class Document {
 *      public static final Configurable<Font> FONT 
 *          = new Configurable<Font>(new Font("Arial", Font.BOLD, 18));
 *  }[/code]
 *  
 *      Not only the second example is cleaner, but the actual configuration
 *      data can come from anywhere, for example during bundle activation,
 *      from the system properties, etc. Low level code does not need to know.</p>
 *  *       
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0, July 21, 2013
 * @see     javolution.osgi.ConfigurableService
 */
@RealTime
public class Configurable<T> {

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
     * Creates a configurable having the specified value.
     */
    public Configurable(T value) {
        reconfigurePermission = new Permission<Configurable<T>>(
                Configurable.class, "reconfigure", this);
        this.value = value;
    }

    /**
     * Returns this configurable value.
     */
    public T get() {
        return value;
    }

    /**
     * Returns the permission to configure this instance.
     */
    public Permission<Configurable<T>> getReconfigurePermission() {
        return reconfigurePermission;
    }

    /**
     * Reconfigures this instance with the specified new value.
     * This method should be overridden if reconfiguration triggers 
     * more processing than just setting the configurable value.
     * 
     * @param newValue the new value.
     * @throws SecurityException if the permission to reconfigure this 
     *         configurable is not granted.
     */
    public void reconfigure(T newValue) {
        SecurityContext.check(reconfigurePermission);
        this.value = newValue;
    }

}
