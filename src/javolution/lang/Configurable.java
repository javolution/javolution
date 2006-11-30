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
 *  <p> This class centralizes run-time configuration parameters and 
 *      allows for custom (plug-in) configuration logic.</p>
 *  
 *  <p> Unlike system properties (or any static mapping), configuration 
 *      parameters may not be known until run-time or may change dynamically.
 *      For example, they may depend upon the current run-time platform, 
 *      the number of cpus, etc. Configuration parameters may also be retrieved
 *      from external resources such as databases, XML files, 
 *      external servers, system properties, etc.</p>
 *      
 *  <p> Regardless of the user configuration constraints, application classes
 *      need only to create static instances of this class and can be 
 *      {@link #notifyChange() notified} of any change. A 
 *      <code>"public static final"</code> modifier is recommended for 
 *      users to identify configurable parameters. For example:[code]
 *  ...
 *  class FastComparator  {     
 *      public static final Configurable<Boolean> REHASH_SYSTEM_HASHCODE 
 *            = new Configurable<Boolean>(isPoorSystemHash()); // Test system hashcode. 
 *  ...
 *  class ConcurrentContext {
 *      public static final Configurable<Integer> MAX_CONCURRENCY 
 *          = new Configurable<Integer>(Runtime.getRuntime().availableProcessors() - 1);
 *               // No algorithm parallelization on single-processor machines.
 *  ...
 *  class XMLInputFactory {    
 *      public static final Configurable<Class> CLASS 
 *           = new Configurable<Class>(XMLInputFactory.Default.class); 
 *  ...
 *  [/code]</p>
 *      
 *  <p> Configuration can only be performed through a configuration 
 *     {@link Logic logic} typically performed at start-up. For example:[code]
 *      private static final Runnable CONFIGURATION = new Configurable.Logic() {
 *          public void run() {
 *              configure(ConcurrentContext.MAX_CONCURRENCY, 0); // No concurrency.
 *              configure(XMLInputFactory.CLASS, MyXMLInputFactory.class);                     
 *          }
 *      };
 *           
 *      public static main(String[] ...) {
 *          CONFIGURATION.run();   
 *      }[/code]
 *      Reconfiguration is allowed at run-time as configurable can be 
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