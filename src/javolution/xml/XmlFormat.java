/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2004 - The Javolution Team (http://javolution.org/)
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.xml;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import javolution.util.FastMap;
import javolution.util.Reflection;
import javolution.realtime.LocalContext.Variable;

/**
 * <p> This class represents the format base class for XML serialization and
 *     deserialization.</p>
 * <p> Application classes may provide a default XML format using static class
 *     members. For example:<pre>
 *     public class Foo {
 *         // Default xml format for Foo and its sub-classes.
 *         protected static final XmlFormat FOO_XML = new XmlFormat(Foo.class) {
 *              public void format(Object obj, XmlElement xml) {
 *                  Foo foo = (Foo) obj;
 *                  xml.setAttribute("xxx", foo.xxx); // Sets attribute. 
 *                  xml.add(...); // Adds nested elements..
 *              }
 *              public Object parse(XmlElement xml) {
 *                  Foo foo = (Foo) xml.object();
 *                  // Sets-up foo from xml attributes and elements.
 *                  foo.xxx = xml.getAttribute("xxx"); 
 *                  ... 
 *                  return foo;
 *             }
 *         };
 *  }</pre>
 * <p> Default formats for: {@link #OBJECT_XML java.lang.Object}, 
 *     {@link #STRING_XML java.lang.String},
 *     {@link #COLLECTION_XML java.util.Collection} and 
 *     {@link #MAP_XML java.util.Map} are also provided.
 *     Here is an example of serialization/deserialization using these
 *     predefined formats:<pre>
 *     ArrayList names = new ArrayList();
 *     names.add("John Doe");
 *     names.add("Oscar Thon");
 *     names.add("Jean Bon");
 *     ObjectWriter ow = new ObjectWriter();
 *     ow.setNamespace("", "java.lang"); // Default namespace for java.lang classes
 *     ow.write(names, new FileOutputStream("C:/names.xml"));</pre>
 *     Here is the <code>names.xml</code> document produced:<pre>
 *     &lt;?xml version="1.0" encoding="UTF-8"?&gt;
 *     &lt;root:java.util.ArrayList xmlns:root="java:" xmlns="java:java.lang"&gt;
 *       &lt;String value="John Doe"/&gt;
 *       &lt;String value="Oscar Thon"/&gt;
 *       &lt;String value="Jean Bon"/&gt;
 *     &lt;/root:java.util.ArrayList&gt;</pre>
 *     The list can be read back with the following code:<pre>
 *     ObjectReader or = new ObjectReader();
 *     ArrayList names = (ArrayList) or.read(new FileInputStream("C:/names.xml"));
 *     </pre></p>
 * <p> Formats can also be dynamically modified. For example:<pre>
 *     // Changes XML mapping for java.util.ArrayList 
 *     XmlFormat listXml = new XmlFormat() {
 *          public void format(Object obj, XmlElement xml) {
 *              ArrayList arraylist = (List) obj;
 *              xml.addAll(list); // Adds list's elements as nested objects.
 *          }
 *          public Object parse(XmlElement xml) {
 *              // Avoids resizing of ArrayList instances.
 *              ArrayList list = (xml.objectClass() == ArrayList.class) ?
 *                  new ArrayList(xml.size()) : xml.object(); 
 *              list.addAll(xml);
 *              return list;
 *          }
 *     };
 *     XmlFormat.setInstance(listXml, java.util.ArrayList.class); // Local setting.
 *     </pre></p>   
 * 
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 1.0, October 4, 2004
 */
public abstract class XmlFormat {

    /**
     * Holds the default mapping (classes to instances).
     */
    private static final FastMap DEFAULTS = new FastMap(64);

    /**
     * Holds the local mapping (classes to local variables).
     */
    private static final FastMap LOCALS = new FastMap(64);

    /**
     * Caches the current class to format mapping.
     */
    private static final FastMap CLASS_TO_FORMAT = new FastMap();

    /**
     * Holds the default XML representation when a more specific format 
     * cannot be found. This representation consists of an empty XML element
     * with no attribute. Objects are deserialized using the class default 
     * constructor (the class being identified by the element's name).
     */
    public static final XmlFormat OBJECT_XML = new XmlFormat() {
        public void format(Object obj, XmlElement xml) {
            // Do nothing.
        }

        public Object parse(XmlElement xml) {
            return xml.object();
        }
    };

    /**
     * Holds the default XML representation for classes implementing 
     * the <code>java.util.Collection</code> interface.
     * This representation consists of nested XML elements one for each 
     * element of the collection. The elements' order is defined by
     * the collection iterator order. Collections are deserialized using their
     * default constructor. 
     */
    public static final XmlFormat COLLECTION_XML = new XmlFormat(
            Collection.class) {
        public void format(Object obj, XmlElement xml) {
            xml.addAll((Collection) obj);
        }

        public Object parse(XmlElement xml) {
            Collection collection = (Collection) xml.object();
            collection.addAll(xml);
            return collection;
        }
    };

    /**
     * Holds the default XML representation for classes implementing 
     * the <code>java.util.Map</code> interface.
     * This representation consists of key/value pair as nested XML elements.
     * The elements' order is defined by the map 's entries iterator order. 
     * Maps are deserialized using their default constructor.
     */
    public static final XmlFormat MAP_XML = new XmlFormat(Map.class) {

        public void format(Object obj, XmlElement xml) {
            Map map = (Map) obj;
            for (Iterator it = map.entrySet().iterator(); it.hasNext();) {
                Map.Entry entry = (Map.Entry) it.next();
                xml.add(entry.getKey());
                xml.add(entry.getValue());
            }
        }

        public Object parse(XmlElement xml) {
            Map map = (Map) xml.object();
            for (Iterator it = xml.iterator(); it.hasNext();) {
                Object key = it.next();
                Object value = it.next();
                map.put(key, value);
            }
            return map;
        }
    };

    /**
     * Holds the default XML representation for <code>java.lang.String</code>
     * classes. This representation consists of a <code>"value"</code> attribute 
     * holding the character sequence.
     */
    public static final XmlFormat STRING_XML = new XmlFormat(String.class) {
        public void format(Object obj, XmlElement xml) {
            xml.setAttribute("value", (String) obj);
        }
        public Object parse(XmlElement xml) {
            return xml.getAttribute("value", "");
        }
    };

    /**
     * Default constructor (used for local mapping).
     */
    protected XmlFormat() {
    }

    /**
     * Creates a default XML mapping for the specified class/interface; 
     * this mapping is inherited by sub-classes or implementing classes.
     * 
     * @param mappedClass the class or interface for which this 
     *        XML format can be used.
     * @throws IllegalArgumentException if a default mapping already exists 
     *        for the specified class.
     */
    protected XmlFormat(Class mappedClass) {
        synchronized (XmlFormat.class) {
            if (DEFAULTS.containsKey(mappedClass)) {
                throw new IllegalArgumentException(
                        "Default mapping already exists for " + mappedClass);
            }
            DEFAULTS.put(mappedClass, this);
            CLASS_TO_FORMAT.clear(); // Clears cache.
        }
    }

    /**
     * Sets a {@link javolution.realtime.LocalContext local} format for the 
     * specified class/interface.
     * 
     * @param xmlFormat the XML format for the specified class/interface.
     * @param mappedClass the associated class/interface.
     */
    public static void setInstance(XmlFormat xmlFormat, Class mappedClass) {
        synchronized (XmlFormat.class) {
            Variable classFormat = (Variable) LOCALS.get(mappedClass);
            if (classFormat == null) { // No local mapping for this class.
                classFormat = new Variable();
                LOCALS.put(mappedClass, classFormat);
            }
            classFormat.setValue(xmlFormat);
            CLASS_TO_FORMAT.clear(); // Clears cache.
        }
    }

    /**
     * Returns the format for the specified class. This method looks for
     * a compatible format from the local formats first; if none is found 
     * the default formats are searched; when no compatible format 
     * exists the {@link #OBJECT_XML} format is returned.
     * 
     * <p> Note: This method attempts to initialize the specified class
     *           the first time a xml format is searched for the specified 
     *           class. This to ensure that the class static fields (which 
     *           may hold the class specific format) are initialized.</p> 
     * 
     * @param mappedClass the class or the superclass 
     *        for which a compatible format is returned.
     * @return the format to use for the specified class.
     */
    public static XmlFormat getInstance(Class mappedClass) {
        synchronized (XmlFormat.class) {

            // Checks cache first.
            Object obj = CLASS_TO_FORMAT.get(mappedClass);
            if (obj != null) {
                return (XmlFormat) obj;
            }

            // Ensures that the class is initialized.
            try {
                Reflection.getClass(mappedClass.getName());
            } catch (ClassNotFoundException e) {
                // Ignores (hopefully the class has already been initialized).
            }

            // Searches local formats first.
            Class bestMatchClass = null;
            for (Iterator i = LOCALS.keySet().iterator(); i.hasNext();) {
                Class cl = (Class) i.next();
                if (cl.isAssignableFrom(mappedClass)) { // Compatible.
                    if ((bestMatchClass == null)
                            || (bestMatchClass.isAssignableFrom(cl))) {
                        bestMatchClass = cl;
                    }
                }
            }
            if (bestMatchClass != null) { // Found local compatible format.
                Variable classFormat = (Variable) LOCALS.get(bestMatchClass);
                XmlFormat xmlFormat = (XmlFormat) classFormat.getValue();
                CLASS_TO_FORMAT.put(mappedClass, xmlFormat);
                return xmlFormat;
            }

            // No local compatible format found, looks for default formats.
            for (Iterator i = DEFAULTS.keySet().iterator(); i.hasNext();) {
                Class cl = (Class) i.next();
                if (cl.isAssignableFrom(mappedClass)) { // Compatible.
                    if ((bestMatchClass == null)
                            || (bestMatchClass.isAssignableFrom(cl))) {
                        bestMatchClass = cl;
                    }
                }
            }
            if (bestMatchClass != null) { // Found local compatible format.
                XmlFormat xmlFormat = (XmlFormat) DEFAULTS.get(bestMatchClass);
                CLASS_TO_FORMAT.put(mappedClass, xmlFormat);
                return xmlFormat;
            } else {
                return OBJECT_XML;
            }
        }
    }

    /**
     * Preallocates the object corresponding to this xml element in order to
     * support circular references. The default implementation returns 
     * <code>null</code> no preallocation.
     *
     * @param xml the xml elements holding this objects's attribute but 
     *        not its content yet.
     * @return a new instance for the specified xml element.
     */
    public Object preallocate(XmlElement xml) {
        return null;
    }

    /**
     * Returns the name of the identifier attribute (default <code>null</code>,
     * no identifer). When overriden, this method should return two distinct
     * identifier for reference and non-reference. 
     * The value of the non-reference identifier if not set by  
     * {@link #format} is generated automatically (counter).
     * 
     * @param isReference <code>true</code> to return the identifier for 
     *        an object reference; <code>false</code> to return the 
     *        identifier for the current object.
     * @return the identifier (object or reference) or <code>null</code>.
     */
    public String identifier(boolean isReference) {
        return null;
    }

    /**
     * Formats an object into the specified {@link XmlElement}.
     *
     * @param obj the object to format.
     * @param xml the <code>XmlElement</code> destination.
     */
    public abstract void format(Object obj, XmlElement xml);

    /**
     * Parses the specified {@link XmlElement} to produce an object. 
     * 
     * @param xml the <code>XmlElement</code> to parse.
     * @return an <code>Object</code> parsed from the specified 
     *         <code>XmlElement</code>. 
     * @throws IllegalArgumentException if the character sequence contains
     *         an illegal syntax.
     */
    public abstract Object parse(XmlElement xml);

}