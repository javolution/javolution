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
import javolution.context.SecurityPermission;

/**
 *  <p> This interface identifies the element which are configurable in your 
 *      code without presupposing how this configuration can be done.
 *      This class facilitates separation of concerns between the configuration
 *      logic and the application code.</p>

 *  <p> Does your class need to know or has to assume that the configuration is
 *      coming from system properties ??</p>
 *
 *  <p> The response is obviously NO!</p>
 *
 *  <p> Let's compare the following examples:[code]
 *      class Document {
 *          private static final Font DEFAULT_FONT
 *              = Font.decode(System.getProperty("DEFAULT_FONT") != null ?
 *                  System.getProperty("DEFAULT_FONT") : "Arial-BOLD-18");
 *      }[/code]
 *      With the following (LocalParameter implements Configurable):[code]
 *      class Document {
 *          public static final LocalParameter<Font> FONT
 *                  = new LocalParameter<Font>(new Font("Arial", Font.BOLD, 18)) {
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
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0, December 12, 2012
 * @see     javolution.osgi.ConfigurableService
 */
public interface Configurable {

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
     * Configures this configurable from the specified textual configuration.
     * Decoding and meaning of the configuration may vary according to the
     * class implementing the configurable interface.
     * 
     * @param configuration the configuration text.
     * @throws SecurityException if the permission to configure this configurable is not granted.
     * @throws IllegalArgumentException if the specified configuration if not valid.
     */
    public void configure(CharSequence configuration) throws SecurityException, IllegalArgumentException;
        
}
