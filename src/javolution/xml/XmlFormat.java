/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2005 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.xml;

import j2me.lang.CharSequence;
import j2me.util.Collection;
import j2me.util.Iterator;
import j2me.util.Map;

import javolution.JavolutionError;
import javolution.util.FastMap;
import javolution.util.Reflection;
import javolution.lang.Text;
import javolution.realtime.ObjectFactory;

/**
 * <p> This class represents the format base class for XML serialization and
 *     deserialization.</p>
 * <p> Application classes may provide a default XML format using static class
 *     members. For example:<pre>
 *     public abstract class Graphic {
 *         private boolean _isVisible;
 *         private Paint _paint; // null if none.
 *         private Stroke _stroke; // null if none.
 *         private Transform _transform; // null if none.
 *          
 *         // XML format with type-based associations for child elements 
 *         // (see {@link javolution.xml} for examples of positional associations).
 *         protected static final XmlFormat GRAPHIC_XML = new XmlFormat(Graphic.class) {
 *              public void format(Object obj, XmlElement xml) {
 *                  Graphic g = (Graphic) obj;
 *                  xml.setAttribute("isVisible", g._isVisible); 
 *                  if (g._paint != null) xml.getContent().add(g._paint);
 *                  if (g._stroke != null) xml.getContent().add(g._stroke);
 *                  if (g._transform != null) xml.getContent().add(g._transform);
 *              }
 *              public Object parse(XmlElement xml) {
 *                  Graphic g = (Graphic) xml.object();
 *                  g._isVisible = xml.getAttribute("isVisible", true);
 *                  for (Iterator i=xml.getContent().fastIterator(); i.hasNext();) {
 *                      Object obj = i.next();
 *                      if (obj instanceof Paint) {
 *                          g._paint = (Paint) obj;
 *                      } else if (obj instanceof Stroke) {
 *                          g._stroke = (Stroke) obj;
 *                      } else if (obj instanceof Transform) {
 *                          g._transform = (Transform) obj;
 *                      }
 *                  }
 *                  return g;
 *             }
 *         };
 *    }</pre>
 * <p> Default formats for: <code>null, java.lang.Object, java.lang.String,
 *     j2me.util.Collection</code> and <code>j2me.util.Map</code> are also
 *     provided. Here is an example of serialization/deserialization using these
 *     predefined formats:<pre>
 *     ArrayList names = new ArrayList();
 *     names.add("John Doe");
 *     names.add("Oscar Thon");
 *     names.add("Jean Bon");
 *     ObjectWriter ow = new ObjectWriter();
 *     ow.setNamespace("", "java.lang"); // Default namespace for java.lang.* classes
 *     ow.write(names, new FileOutputStream("C:/names.xml"));</pre>
 *     Here is the <code>names.xml</code> document produced:<pre>
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
 *     XmlFormat xmlFormat = new XmlFormat() {
 *          public void format(Object obj, XmlElement xml) {
 *              ArrayList list = (ArrayList) obj;
 *              xml.getContent().addAll(list); // Adds list's elements as nested objects.
 *          }
 *          public Object parse(XmlElement xml) {
 *              // Avoids resizing of ArrayList instances.
 *              ArrayList list = (xml.objectClass() == ArrayList.class) ?
 *                  new ArrayList(xml.getContent().size()) : (ArrayList) xml.object(); 
 *              list.addAll(xml.getContent());
 *              return list;
 *          }
 *     };
 *     XmlFormat.setInstance(xmlFormat, java.util.ArrayList.class);
 *     </pre></p>   
 * <p> Finally, xml formats can be made impervious to obfuscation by setting 
 *     the {@link #setAlias alias} of the obfuscated classes. Aliases can also 
 *     be used for tag name customization (by default the tag name is the 
 *     full class name of the object serialized).</p>
 * 
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 3.0, February 16, 2005
 */
public abstract class XmlFormat {

    /**
     * Holds the base class to format look-up table (no removal allowed)
     */
    private static final FastMap BASE_CLASS_TO_FORMAT = new FastMap();

    /**
     * Hold the updated class to format look-up table (no removal allowed).
     */
    private static final FastMap CLASS_TO_FORMAT = new FastMap();

    /**
     * Holds class to tag name (string) look-up table (no removal allowed).
     */
    private static final FastMap CLASS_TO_NAME = new FastMap();

    /**
     * Holds the tag name (string) to class look-up table (no removal allowed).
     */
    private static final FastMap NAME_TO_CLASS = new FastMap();

    /**
     * Holds uri-name to class look-up table (no removal allowed) 
     */
    private static final FastMap ID_TO_CLASS = new FastMap();

    /**
     * Holds the object representing <code>null</code> values.
     */
    static final Null NULL = new Null();

    private static class Null {
    };

    /**
     * Holds the XML representation for <code>null</code> objects
     * (<code>&lt;Null/&gt;</code>).
     */
    public static final XmlFormat NULL_XML = new XmlFormat(NULL.getClass()) {
        public void format(Object obj, XmlElement xml) {
            // Empty tag.
        }

        public Object parse(XmlElement xml) {
            return null;
        }
    };
    static {
        XmlFormat.setAlias(NULL.getClass(), "Null");
    }

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
     * the <code>j2me.util.Collection</code> interface.
     * This representation consists of nested XML elements one for each 
     * element of the collection. The elements' order is defined by
     * the collection iterator order. Collections are deserialized using their
     * default constructor. 
     */
    public static final XmlFormat COLLECTION_XML = new XmlFormat(
            "j2me.util.Collection") {
        public void format(Object obj, XmlElement xml) {
            xml.getContent().addAll((Collection) obj);
        }

        public Object parse(XmlElement xml) {
            Collection collection = (Collection) xml.object();
            collection.addAll(xml.getContent());
            return collection;
        }
    };

    /**
     * Holds the default XML representation for classes implementing 
     * the <code>j2me.util.Map</code> interface.
     * This representation consists of key/value pair as nested XML elements.
     * The elements' order is defined by the map 's entries iterator order. 
     * Maps are deserialized using their default constructor.
     */
    public static final XmlFormat MAP_XML = new XmlFormat("j2me.util.Map") {

        public void format(Object obj, XmlElement xml) {
            Map map = (Map) obj;
            for (Iterator it = map.entrySet().iterator(); it.hasNext();) {
                Map.Entry entry = (Map.Entry) it.next();
                xml.getContent().add(entry.getKey());
                xml.getContent().add(entry.getValue());
            }
        }

        public Object parse(XmlElement xml) {
            Map map = (Map) xml.object();
            for (Iterator it = xml.getContent().fastIterator(); it.hasNext();) {
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
    public static final XmlFormat STRING_XML = new XmlFormat("".getClass()) {
        public void format(Object obj, XmlElement xml) {
            xml.setAttribute("value", (String) obj);
        }

        public Object parse(XmlElement xml) {
            return xml.getAttribute("value", "");
        }
    };

    /**
     * The class/interface mapped to this format.
     */
    private Class _mappedClass;

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
        _mappedClass = mappedClass;
        synchronized (BASE_CLASS_TO_FORMAT) {
            if (BASE_CLASS_TO_FORMAT.containsKey(_mappedClass)) {
                throw new IllegalArgumentException(
                        "Mapping already exists for " + _mappedClass);
            }
            BASE_CLASS_TO_FORMAT.put(_mappedClass, this);
            invalidateClassToFormatMapping();
        }
    }

    /**
     * Creates a default XML mapping for the class/interface having the 
     * specified name.
     * 
     * @param className the name class or interface for which this 
     *        XML format can be used.
     * @throws IllegalArgumentException if a default mapping already exists 
     *        for the specified class.
     */
    protected XmlFormat(String className) {
        try {
            _mappedClass = Reflection.getClass(className);
        } catch (ClassNotFoundException e) {
            throw new JavolutionError(e);
        }
        synchronized (BASE_CLASS_TO_FORMAT) {
            if (BASE_CLASS_TO_FORMAT.containsKey(_mappedClass)) {
                throw new IllegalArgumentException(
                        "Mapping already exists for " + _mappedClass);
            }
            BASE_CLASS_TO_FORMAT.put(_mappedClass, this);
            invalidateClassToFormatMapping();
        }
    }

    /**
     * Returns the class or interface associated to this xml format.
     * 
     * @return the class/interface for this format.
     */
    public Class getMappedClass() {
        return _mappedClass;
    }

    /**
     * Sets the formats for the specified class/interface.
     * 
     * @param xmlFormat the XML format for the specified class/interface
     *        or <code>null</code> to unset the current mapping.
     * @param forClass the associated class/interface.
     */
    public static void setInstance(XmlFormat xmlFormat, Class forClass) {
        xmlFormat._mappedClass = forClass;
        synchronized (BASE_CLASS_TO_FORMAT) {
            BASE_CLASS_TO_FORMAT.put(forClass, xmlFormat);
            invalidateClassToFormatMapping();
        }
    }

    /**
     * Returns the format for the specified class/interface.
     * This method looks for the more specialized format; if none found
     * {@link #OBJECT_XML} is returned.
     * 
     * <p> Note: This method forces the initialization of the specified
     *           class. This to ensure that the class static fields (which 
     *           may hold the most specialized format) are initialized.</p> 
     * 
     * @param mappedClass the class/interface for which the most specialized 
     *        format is returned.
     * @return the format to use for the specified class.
     */
    public static XmlFormat getInstance(Class mappedClass) {

        // Checks look-up.
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
        Class bestMatchClass = null;
        XmlFormat bestMatchFormat = null;
        synchronized (BASE_CLASS_TO_FORMAT) {
            // Searches best match.
            for (Iterator i = BASE_CLASS_TO_FORMAT.fastKeyIterator(); i
                    .hasNext();) {
                Class clazz = (Class) i.next();
                if (clazz.isAssignableFrom(mappedClass)) { // Compatible.
                    if ((bestMatchClass == null)
                            || (bestMatchClass.isAssignableFrom(clazz))) {
                        // clazz more specialized that bestMatchClass.
                        XmlFormat xmlFormat = (XmlFormat) BASE_CLASS_TO_FORMAT
                                .get(clazz);
                        if (xmlFormat != null) {
                            bestMatchClass = clazz;
                            bestMatchFormat = xmlFormat;
                        }
                    }
                }
            }

            // If none found, use default object format.
            if (bestMatchFormat == null) {
                bestMatchFormat = OBJECT_XML;
            }
            // Updates look-up.
            synchronized (CLASS_TO_FORMAT) {
                CLASS_TO_FORMAT.put(mappedClass, bestMatchFormat);
            }
            return bestMatchFormat;
        }
    }

    /**
     * Sets the alias to use for tag name when serializing direct instances of  
     * specified class. This method is particularly useful
     * in case of obfuscation to ensure proper/invariant xml formatting 
     * (you don't want to use the obfuscated class name in such case). 
     * Aliases can also be used to customize the tag names in the xml document
     * (e.g. to use different/shorter names than the full class name).
     * 
     * @param forClass the class for which the specified alias should be used.
     * @param alias the tag name to use for the specified class.
     */
    public static void setAlias(Class forClass, String alias) {
        synchronized (CLASS_TO_NAME) {
            CLASS_TO_NAME.put(forClass, alias);
            NAME_TO_CLASS.put(alias, forClass);
        }
    }

    /**
     * Returns the name to be used for the serialization of the specified 
     * class. If no alias has been set this method returns the full class name.
     * 
     * @param clazz the class for which the name to use is returned.
     * @return the name of the element identifying instances of the 
     *        specified class during serialization.
     */
    static String nameFor(Class clazz) {
        String name = (String) CLASS_TO_NAME.get(clazz);
        if (name == null) {
            name = clazz.getName();
            synchronized (CLASS_TO_NAME) {
                CLASS_TO_NAME.put(clazz, name);
                NAME_TO_CLASS.put(name, clazz);
            }
        }
        return name;
    }

    /**
     * Returns the class for the specified identifier (URI and local name).
     *
     * @param  id the identifier.
     * @return the corresponding class.
     * @throws XmlException if there is no class matching the specified id.
     */
    static Class classFor(Identifier id) {
        // Searches current mapping.
        Class clazz = (Class) ID_TO_CLASS.get(id);
        if (clazz != null) {
            return clazz;
        }

        // Extracts the class name (or alias).
        String name;
        Text uri = Text.valueOf(id.uri).intern();
        Text localName = Text.valueOf(id.localName).intern();
        if (uri.length() == 0) {
            name = localName.toString();
        } else if ((uri.length() >= 5) && (uri.charAt(0) == 'j')
                && (uri.charAt(1) == 'a') && (uri.charAt(2) == 'v')
                && (uri.charAt(3) == 'a') && (uri.charAt(4) == ':')) {
            if (uri.length() > 5) {
                name = uri.subtext(5, uri.length()) + "." + localName;
            } else { // "java:" 
                name = localName.toString();
            }
        } else {
            throw new XmlException("Invalid URI (must use a java scheme)");
        }

        // Finds the class object.
        clazz = (Class) NAME_TO_CLASS.get(name);
        if (clazz == null) {
            try {
                clazz = Reflection.getClass(name);
            } catch (ClassNotFoundException e) {
                throw new XmlException(e);
            }
            synchronized (CLASS_TO_NAME) {
                CLASS_TO_NAME.put(clazz, name);
                NAME_TO_CLASS.put(name, clazz);
            }
        }

        // Adds new id-class mapping.
        Identifier newId = (Identifier) Identifier.FACTORY.heapPool().next();
        newId.uri = uri;
        newId.localName = localName;
        synchronized (ID_TO_CLASS) {
            ID_TO_CLASS.put(newId, clazz);
        }
        return clazz;

    }

    static final class Identifier {

        // Use factory to allow for pre-allocation.
        static final ObjectFactory FACTORY = new ObjectFactory() {
            protected Object create() {
                return new Identifier();
            }
        };

        CharSequence uri;

        CharSequence localName;

        public boolean equals(Object obj) {
            Identifier that = (Identifier) obj;
            return (that.uri.equals(this.uri) && that.localName
                    .equals(this.localName));
        }

        public int hashCode() {
            return localName.hashCode();
        }
    }

    /**
     * Invalidates the current class to format mapping. 
     * Look-up table cannot be cleared (to avoid synchronized read),
     * each entry value is set to null.
     */
    private static void invalidateClassToFormatMapping() {
        synchronized (CLASS_TO_FORMAT) {
            for (Iterator i = CLASS_TO_FORMAT.fastKeyIterator(); i.hasNext();) {
                CLASS_TO_FORMAT.put(i.next(), null);
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
     * @see    XmlElement#object()
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