/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2005 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package _templates.javolution.lang;

import _templates.javolution.context.LogContext;
import _templates.javolution.context.SecurityContext;
import _templates.javolution.text.Text;
import _templates.javolution.text.TextFormat;
import _templates.javolution.util.FastTable;
import _templates.javolution.xml.XMLBinding;
import _templates.javolution.xml.XMLFormat;
import _templates.javolution.xml.XMLObjectReader;
import _templates.javolution.xml.stream.XMLStreamException;
import java.io.InputStream;
import java.util.Enumeration;

/**
 *  <p> This class facilitates separation of concerns between the configuration
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
 *          ...
 *      }[/code]
 *      With the following (using this class):[code]
 *      class Document {
 *          public static final Configurable<Font> DEFAULT_FONT
 *              = new Configurable<Font>(new Font("Arial", Font.BOLD, 18));
 *          ...
 *      }[/code]
 *      Not only the second example is cleaner, but the actual configuration
 *      data can come from anywhere, for example from the OSGI Configuration
 *      Admin package (<code>org.osgi.service.cm</code>).
 *      Low level code does not need to know.</p>
 *
 * <p>  Configurable instances have the same textual representation as their
 *      current values. For example:[code]
 *       public static final Configurable<String> AIRPORT_TABLE
 *            = new Configurable<String>("Airports");
 *       ...
 *       String sql = "SELECT * FROM " + AIRPORT_TABLE
 *           // AIRPORT_TABLE.get() is superfluous
 *           + " WHERE State = '" + state  + "'";[/code]
 *      </p>
 *
 *  <p> Unlike system properties (or any static mapping), configuration
 *      parameters may not be known until run-time or may change dynamically.
 *      They may depend upon the current run-time platform,
 *      the number of cpus, etc. Configuration parameters may also be retrieved
 *      from external resources such as databases, XML files,
 *      external servers, system properties, etc.[code]
 *      public abstract class FastComparator<T> implements Comparator<T>, Serializable  {
 *          public static final Configurable<Boolean> REHASH_SYSTEM_HASHCODE
 *              = new Configurable<Boolean>(isPoorSystemHash()); // Test system hashcode.
 *      ...
 *      public abstract class ConcurrentContext extends Context {
 *          public static final Configurable<Integer> MAXIMUM_CONCURRENCY
 *              = new Configurable<Integer>(Runtime.getRuntime().availableProcessors() - 1) {};
 *                  // No algorithm parallelization on single-processor machines.
 *     ...
 *     public abstract class XMLInputFactory {
 *          public static final Configurable<Class<? extends XMLInputFactory>> CLASS
 *              = new Configurable<Class<? extends XMLInputFactory>>(XMLInputFactory.Default.class);
 *                  // Default class implementation is a private class.
 *     ...
 *     [/code]</p>
 *
 *  <p> Dynamic {@link #configure configuration} is allowed/disallowed based
 *      upon the current {SecurityContext}. Configurables are automatically
 *      {@link Configurable#notifyChange notified} of
 *      any changes in their configuration values.</p>
 *
 * <p>  Unlike system properties, configurable can be
 *      used in applets or unsigned webstart applications.</p>
 *
 *  <p> Here is an example of configuration of a web application from 
 *      a property file:[code]
 *      public class Configuration implements ServletContextListener {
 *          public void contextInitialized(ServletContextEvent sce) {
 *              try {
 *                  ServletContext ctx = sce.getServletContext();
 *               
 *                  // Loads properties.
 *                  Properties properties = new Properties();
 *                  properties.load(ctx.getResourceAsStream("WEB-INF/config/configuration.properties"));
 *               
 *                  // Reads properties superceeding default values.
 *                  Configurable.read(properties);
 *                  
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
 *      </web-app>[/code]
 *      The property file contains the full names of the configurable static
 *      fields and the textual representation of their new values:[code]
 *      # File configuration.properties
 *      javolution.util.FastComparator#REHASH_SYSTEM_HASHCODE = true
 *      javolution.context.ConcurrentContext#MAXIMUM_CONCURRENCY = 0
 *      javolution.xml.stream.XMLInputFactory#CLASS = com.foo.bar.XMLInputFactoryImpl
 *      [/code]</p>
 *      
 *  <p> Here is an example of reconfiguration from a xml file:[code]
 *      FileInputStream xml = new FileInputStream("D:/configuration.xml");
 *      Configurable.read(xml);[/code]
 *      and the configuration file:[code]
 *      <?xml version="1.0" encoding="UTF-8" ?>
 *      <Configuration>
 *          <Configurable name="javolution.util.FastComparator#REHASH_SYSTEM_HASHCODE">
 *              <Value class="java.lang.Boolean" value="true"/>
 *          </Configurable>
 *          <Configurable name="javolution.context.ConcurrentContext#MAXIMUM_CONCURRENCY">
 *               <Value class="java.lang.Integer" value="0"/>
 *          </Configurable>
 *          <Configurable name="javolution.xml.stream.XMLInputFactory#CLASS">
 *               <Value class="java.lang.Class" value="com.foo.MyXMLInputFactory"/>
 *          </Configurable>
 *      </Configuration>[/code]</p>
 *       
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 5.5, April 20, 2010
 */
public class Configurable/*<T>*/ {

    /**
     * Holds the current value (never null).
     */
    private Object/*{T}*/ _value;

    /**
     * Holds the default value (never null).
     */
    private final Object/*{T}*/ _default;

    /**
     * Holds the class where this configurable is defined.
     */
    private final Class _container;

    /**
     * Creates a new configurable having the specified default value.
     *
     * @param defaultValue the default value.
     * @throws IllegalArgumentException if <code>defaultValue</code> is
     *         <code>null</code>.
     */
    public Configurable(Object/*{T}*/ defaultValue) {
        if (defaultValue == null)
            throw new IllegalArgumentException("Default value cannot be null");
        _default = defaultValue;
        _value = defaultValue;
        _container = Configurable.findContainer();
    }

    private static Class findContainer() {
        /* @JVM-1.4+@
        try {
        StackTraceElement[] stack = new Throwable().getStackTrace();
        String className = stack[2].getClassName();
        int sep = className.indexOf("$");
        if (sep >= 0) { // If inner class, remove suffix.
        className = className.substring(0, sep);
        }
        return Class.forName(className); // We use the caller class loader (and avoid dependency to Reflection utility).
        } catch (Throwable error) {
        LogContext.error(error);
        }
        /**/
        return null;
    }

    /**
     * Returns the current value for this configurable.
     * 
     * @return the current value (always different from <code>null</code>).
     */
    public Object/*{T}*/ get() {
        return _value;
    }

    /**
     * Returns the default value for this configurable.
     * 
     * @return the default value (always different from <code>null</code>).
     */
    public Object/*{T}*/ getDefault() {
        return _default;
    }

    /**
     * Returns the container class of this configurable (the class
     * where this configurable is defined as a <code>public static</code> field.
     *
     * @return the container class or <code>null</code> if unknown (e.g. J2ME).
     */
    public Class getContainer() {
        return _container;
    }

    /**
     * Returns the field name of this configurable (for example <code>
     * "javolution.context.ConcurrentContext#MAXIMUM_CONCURRENCY"</code>)
     * for {@link javolution.context.ConcurrentContext#MAXIMUM_CONCURRENCY}.
     *
     *  @return this configurable name or <code>null</code> if the name
     *          of this configurable is unknown (e.g. J2ME).
     */
    public String getName() {
        if (_container == null)
            return null;

        /* @JVM-1.4+@
        try {
        java.lang.reflect.Field[] fields = _container.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
        java.lang.reflect.Field field = fields[i];
        if (java.lang.reflect.Modifier.isPublic(field.getModifiers()) && field.get(null) == this)
        return _container.getName() + '#' + field.getName();
        }
        } catch (Throwable error) {
        LogContext.error(error);
        }
        /**/
        return null;
    }

    /**
     * Notifies this configurable that its runtime value is going to be changed.
     * The default implementation does nothing.
     *
     * @param oldValue the previous value.
     * @param newValue the new value.
     * @throws UnsupportedOperationException if dynamic reconfiguration of
     *         this configurable is not allowed (regardless of the security
     *         context).
     */
    protected void notifyChange(Object/*{T}*/ oldValue, Object/*{T}*/ newValue)
            throws _templates.java.lang.UnsupportedOperationException {
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
     * Returns the configurable instance having the specified name.
     * For example:[code]
     *     Configurable cfg = Configurable.getInstance("javolution.context.ConcurrentContext#MAXIMUM_CONCURRENCY")
     * [/code] returns {@link javolution.context.ConcurrentContext#MAXIMUM_CONCURRENCY}.
     *
     * <p><b>Note:</b> OSGI based framework should ensure that class loaders
     *    of configurable instances are known to the {@link Reflection} utility
     *    class.</p>
     * @param  name the name of the configurable to retrieve.
     * @return the corresponding configurable or <code>null</code> if it
     *         cannot be found.
     */
    public static Configurable getInstance(String name) {
        int sep = name.lastIndexOf('#');
        if (sep < 0)
            return null;
        String className = name.substring(0, sep);
        String fieldName = name.substring(sep + 1);
        Class cls = Reflection.getInstance().getClass(className);
        if (cls == null) {
            LogContext.warning("Class " + className + " not found");
            return null;
        }
        /* @JVM-1.4+@
        try {
        Configurable cfg = (Configurable) cls.getDeclaredField(fieldName).get(null);
        if (cfg == null) {
        LogContext.warning("Configurable " + name + " not found");
        }
        return cfg;
        } catch (Exception ex) {
        LogContext.error(ex);
        }
        /**/
        return null;
    }

    /**
     * Sets the run-time value of the specified configurable. If the
     * configurable value is different from the previous one, then
     * {@link #notifyChange} is called. This method
     * raises a <code>SecurityException</code> if the specified
     * configurable cannot be {@link SecurityContext#isConfigurable
     * reconfigured}.
     *
     * @param  cfg the configurable being configured.
     * @param  newValue the new run-time value.
     * @throws IllegalArgumentException if <code>value</code> is
     *         <code>null</code>.
     * @throws SecurityException if the specified configurable cannot
     *         be modified.
     */
    public static /*<T>*/ void configure(Configurable/*<T>*/ cfg,
            Object/*{T}*/ newValue) throws SecurityException {
        if (newValue == null)
            throw new IllegalArgumentException("Default value cannot be null");
        SecurityContext policy = (SecurityContext) SecurityContext.getCurrentSecurityContext();


        if (!policy.isConfigurable(cfg))
            throw new SecurityException(
                    "Configuration disallowed by SecurityContext");
        Object/*{T}*/ oldValue = cfg._value;


        if (!newValue.equals(oldValue)) {
            LogContext.info("Configurable " + cfg.getName() + " set to " + newValue);
            cfg._value = newValue;
            cfg.notifyChange(oldValue, newValue);


        }
    }

    /**
     * Convenience method to read the specified properties and reconfigure
     * accordingly. For example:[code]
     *     // Load configurables from system properties.
     *     Configurable.read(System.getProperties());[/code]
     * Configurables are identified by their field names. The textual
     * representation of their value is defined by
     * {@link _templates.javolution.text.TextFormat#getInstance(Class)}
     * text format}. For example:[code]
     *      javolution.util.FastComparator#REHASH_SYSTEM_HASHCODE = true
     *      javolution.context.ConcurrentContext#MAXIMUM_CONCURRENCY = 0
     *      javolution.xml.stream.XMLInputFactory#CLASS = com.foo.bar.XMLInputFactoryImpl
     * [/code]
     * Conversion of <code>String</code> values to actual object is
     * performed using {@link _templates.javolution.text.TextFormat#getInstance(Class)}.
     *
     * <p><b>Note:</b> OSGI based framework should ensure that class loaders
     *    of configurable instances are known to the {@link Reflection} utility
     *    class.</p>
     *
     * @param properties the properties.
    @JVM-1.4+@
    public static void read(java.util.Properties properties) {
        Enumeration e = properties.keys();
        while (e.hasMoreElements()) {
            String name = (String) e.nextElement();
            String textValue = properties.getProperty(name);
            Configurable cfg = Configurable.getInstance(name);
            if (cfg == null)
                continue;
            // Use the default value to retrieve the configurable type
            // and the associated textual format.
            Class type = cfg.getDefault().getClass();
            TextFormat format = TextFormat.getInstance(type);
            if (!format.isParsingSupported()) {
                LogContext.error("Cannot find suitable TextFormat to parse instances of " + type);
                continue;
            }
            Object newValue = format.parse(Configurable.toCsq(textValue));
            Configurable.configure(cfg, newValue);
        }
    }
    /**/


    /**
     * Convenience method to read configurable values from the specified 
     * XML stream. This method uses
     * <a href="http://javolution.org/target/site/apidocs/javolution/xml/package-summary.html">
     * Javolution XML</a> facility to perform the deserialization.
     * Here is an example of XML configuration file.[code]
     * <?xml version="1.0" encoding="UTF-8" ?>
     * <Configuration>
     *     <Configurable name="javolution.util.FastComparator#REHASH_SYSTEM_HASHCODE">
     *          <Value class="java.lang.Boolean" value="true"/>
     *     </Configurable>
     *     <Configurable name="javolution.context.ConcurrentContext#MAXIMUM_CONCURRENCY">
     *          <Value class="java.lang.Integer" value="0"/>
     *     </Configurable>
     *     <Configurable name="javolution.xml.stream.XMLInputFactory#CLASS">
     *          <Value class="java.lang.Class" value="com.foo.MyXMLInputFactory"/>
     *     </Configurable>
     * </Configuration>[/code]
     * It can be read directly with the following code:[code]
     * FileInputStream xml = new FileInputStream("D:/configuration.xml");
     * Configurable.read(xml);[/code]
     *
     * <p><b>Note:</b> OSGI based framework should ensure that class loaders
     *    of configurable instances are known to the {@link Reflection} utility.
     *    </p>
     *
     * @param inputStream the input stream holding the xml configuration.
     */
    public static void read(InputStream inputStream) {
        try {
            XMLObjectReader reader = XMLObjectReader.newInstance(inputStream);
            XMLBinding binding = new XMLBinding() {
                protected XMLFormat getFormat(Class forClass) throws XMLStreamException  {
                    if (Configurable.class.isAssignableFrom(forClass))
                        return new ConfigurableXMLFormat();
                    return super.getFormat(forClass);
                }
            };
            binding.setAlias(Configurable.class, "Configurable");
            reader.setBinding(binding);
            // Reads and configures.
            reader.read("Configuration", FastTable.class);
        } catch (Exception ex) {
            LogContext.error(ex);
        }
    }

    // Local format for read operation.
    private static class ConfigurableXMLFormat extends XMLFormat {

        ConfigurableXMLFormat() {
            super(null); // Unbounded
        }

        public Object newInstance(Class cls, InputElement xml) throws XMLStreamException {
            return Configurable.getInstance(xml.getAttribute("name", ""));
        }

        public void write(Object c, OutputElement xml) throws XMLStreamException {
            throw new _templates.java.lang.UnsupportedOperationException();
        }

        public void read(InputElement xml, Object c) throws XMLStreamException {
            Object value = xml.get("Value");
            if (value == null)
                return; // Optional value not present.
            Configurable.configure((Configurable) c, value);
        }
    };

    // For J2ME Compatibility.
    private static _templates.java.lang.CharSequence toCsq(Object str) {
        /*@JVM-1.4+@
        if (true) return (CharSequence) str;
        /**/
        return str == null ? null : Text.valueOf(str);

    }
}
