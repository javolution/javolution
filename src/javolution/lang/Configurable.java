/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2005 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.lang;

/**
 *  <p> This class facilitates separation of concerns between the configuration
 *      logic and the application code.</p>
 *  
 *  <p> Does your class need to know or has to assume that the configuration is
 *      coming from system properties ??</p>
 *      
 *  <p> The response is obviously NO!</p>
 *  
 *  <p> Let's compare the following examples:[code]
 *      class Document {
 *          private static final Font DEFAULT_FONT
 *              = Font.decode(System.getProperty("DEFAULT_FONT") != null ? System.getProperty("DEFAULT_FONT") : "Arial-BOLD-18");
 *          ...
 *      }[/code]
 *      With the following (using this class):[code]
 *      class Document {
 *          public static final Configurable<Font> DEFAULT_FONT = new Configurable<Font>(new Font("Arial", Font.BOLD, 18));
 *          ...
 *      }[/code]
 *      Not only the second example is cleaner, but the actual configuration 
 *      data can come from anywhere (even remotely). Low level code does not 
 *      need to know.</p>
 *  
 *  <p> Furthermore, with the second example the configurable data is 
 *      automatically documented in the JavaDoc (public). Still only instances 
 *      of {@link Logic} may set this data. There is no chance
 *      for the user to modify the configuration by accident.</p>
 *  
 *  <p> Unlike system properties (or any static mapping), configuration 
 *      parameters may not be known until run-time or may change dynamically.
 *      They may depend upon the current run-time platform, 
 *      the number of cpus, etc. Configuration parameters may also be retrieved
 *      from external resources such as databases, XML files, 
 *      external servers, system properties, etc.[code]
 *      class FastComparator  {     
 *          public static final Configurable<Boolean> REHASH_SYSTEM_HASHCODE 
 *              = new Configurable<Boolean>(isPoorSystemHash()); // Test system hashcode. 
 *      ...
 *      class ConcurrentContext {
 *          public static final Configurable<Integer> MAX_CONCURRENCY 
 *              = new Configurable<Integer>(Runtime.getRuntime().availableProcessors() - 1);
 *                  // No algorithm parallelization on single-processor machines.
 *     ...
 *     class XMLInputFactory {    
 *          public static final Configurable<Class> CLASS 
 *              = new Configurable<Class>(XMLInputFactory.Default.class);
 *                  // Default class implementation is a private class. 
 *     ...
 *     [/code]</p>
 *      
 *  <p> Reconfiguration is allowed at run-time as configurable can be 
 *      {@link Configurable#notifyChange() notified} of changes in their
 *      configuration values. Unlike system properties, configurable can be 
 *      used in applets or unsigned webstart applications.</p>
 *      
 * <p> Configuration settings are global (affect all threads). For thread-local
 *     environment settings {@link javolution.context.LocalContext.Reference 
 *     LocalContext.Reference} instances are recommended.</p>   
 *       
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 4.0, September ,4 2006
 */
public class Configurable/*<T>*/ {

    /**
     * Holds the current value. 
     */
    private Object/*{T}*/ _value;
    
    /**
     * Default constructor. 
     */
    public Configurable(Object/*{T}*/ defaultValue) {
        _value = defaultValue;
    }

    /**
     * Returns the current value for this configurable.
     * 
     *  @return the current value.
     */
    public Object/*{T}*/ get() {
        return _value;
    }
        
    /**
     * Notifies this configurable that its runtime value has been changed.
     * The default implementation does nothing. 
     */
    protected void notifyChange() {
    }
        
    /**
     * This class represents a configuration logic which may be called 
     * at any time (configurating is always thread-safe).
     */
    public static abstract class Logic implements Runnable  {
        
        /**
         * Sets the run-time value of the specified configurable.
         * 
         * @param configurable the configurable being configurated.
         * @param value the new run-time value.
         */
        protected final /*<T>*/ void configure(Configurable/*<T>*/ configurable, Object/*{T}*/ value) {
            configurable._value = value;
            configurable.notifyChange();
        }
    }

}