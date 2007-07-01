/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2005 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.lang;

import javolution.text.Text;

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
 * <p>  Configurable instances have the same textual representation as their 
 *      current values. For example:[code]
 *       public static final Configurable<String> AIRPORT_TABLE
 *            = new Configurable<String>("Airports");
 ...
 *       String sql = "SELECT * FROM " + AIRPORT_TABLE // AIRPORT_TABLE.get() superfluous 
 *           + " WHERE State = '" + state  + "'";[/code]
 *      </p>
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
 *          public static final Configurable<Integer> MAXIMUM_CONCURRENCY 
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
 *  <p> Here is an example of configurable logic for a web application.[code]
 *      public class Configuration extends Configurable.Logic implements ServletContextListener {
 *          public void contextInitialized(ServletContextEvent sce) {
 *              try {
 *                  ServletContext ctx = sce.getServletContext();
 *               
 *                  // Loads properties.
 *                  Properties properties = new Properties();
 *                  properties.load(ctx.getResourceAsStream("WEB-INF/config/configuration.properties"));
 *               
 *                  // Configures the web application from property file.
 *                  String rehash = properties.get("REHASH"); 
 *                  if (rehash != null) { 
 *                      configure(FastComparator.REHASH, TypeFormat.parseBoolean(rehash));
 *                  }
 *                  String concurrency = properties.get("MAXIMUM_CONCURRENCY"); 
 *                  if (concurrency != null) { 
 *                      configure(ConcurrentContext.MAXIMUM_CONCURRENCY, TypeFormat.parseInt(concurrency));
 *                  }                
 *                  String xmlInputFactoryClass  = properties.get("XML_INPUT_FACTORY_CLASS"); 
 *                  if (xmlInputFactoryClass != null) { 
 *                      configure(XMLInputFactory.CLASS, Class.forName(xmlInputFactoryClass));
 *                  }  
 *                  ...              
 *              } catch (Exception ex) {
 *                  LogContext.error(ex);
 *              }
 *          }
 *      }[/code]
 *      This listener is registered in the <code>web.xml</code> file:[code]
 *      <web-app>
 *          <listener>
 *              <listener-class>mypackage.Configuration</listener-class>
 *           </listener>
 *      </web-app>[/code]</p>
 *      
 * <p> Configuration settings are global (affect all threads). For thread-local
 *     environment settings {@link javolution.context.LocalContext.Reference 
 *     LocalContext.Reference} instances are recommended.</p>   
 *       
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 5.1, July 2, 2007
 */
public class Configurable/*<T>*/{

    /**
     * Holds the current value. 
     */
    private Object/*{T}*/_value;

    /**
     * Default constructor. 
     */
    public Configurable(Object/*{T}*/defaultValue) {
        _value = defaultValue;
    }

    /**
     * Returns the current value for this configurable.
     * 
     *  @return the current value.
     */
    public Object/*{T}*/get() {
        return _value;
    }

    /**
     * Returns the string representation of the value of this configurable.
     * 
     * @return <code>String.valueOf(this.get())</code>
     */
    public String toString() {
        return String.valueOf(_value);
    }

    /**
     * Returns the text representation of the value of this configurable.
     * 
     * @return <code>Text.valueOf(this.get())</code>
     */
    public Text toText() {
        return Text.valueOf(_value);
    }

    /**
     * Notifies this configurable that its runtime value has been changed.
     * The default implementation does nothing. 
     */
    protected void notifyChange() {
    }

    /**
     * This class represents a configuration logic capable of setting  
     * {@link Configurable} values. For example:[code]
     * class MyApplication {
     *     private static final Configuration CONFIGURATION = new Configuration();
     *     public static void main(String[] args) {
     *         CONFIGURATION.run();
     *         ...       
     *     }
     *     static class Configuration extends Configurable.Logic implements Runnable {
     *         public void run() {
     *             Properties properties = System.getProperties();
     *             String concurrency = properties.get("MAXIMUM_CONCURRENCY"); 
     *             if (concurrency != null) { 
     *                 configure(ConcurrentContext.MAXIMUM_CONCURRENCY, TypeFormat.parseInt(concurrency));
     *             }                
     *             ...
     *         }
     *    }
     * }[/code]
     */
    public static abstract class Logic {

        /**
         * Sets the run-time value of the specified configurable. If 
         * configurable value is different from the previous one, then  
         * {@link Configurable#notifyChange()} is called.
         * 
         * @param configurable the configurable being configurated.
         * @param value the new run-time value.
         */
        protected final/*<T>*/void configure(Configurable/*<T>*/configurable,
                Object/*{T}*/value) {
            Object previous = configurable._value;
            configurable._value = value;
            boolean change = (value == null) ? previous != null : !value
                    .equals(previous);
            if (change) {
                configurable.notifyChange();
            }
        }
    }

}