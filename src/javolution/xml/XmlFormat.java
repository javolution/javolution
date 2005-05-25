/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2005 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.xml;

import java.io.IOException;

import j2me.lang.CharSequence;
import j2me.util.Collection;
import j2me.util.Iterator;
import j2me.util.Map;

import javolution.JavolutionError;
import javolution.lang.Appendable;
import javolution.lang.Reflection;
import javolution.lang.Text;
import javolution.util.FastList;
import javolution.util.FastMap;

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
 *         // XML format with positional association (members identified by their position),
 *         // see {@link javolution.xml} for examples of name association.
 *         protected static final XmlFormat GRAPHIC_XML = new XmlFormat(Graphic.class) {
 *              public void format(Object obj, XmlElement xml) {
 *                  Graphic g = (Graphic) obj;
 *                  xml.setAttribute("isVisible", g._isVisible); 
 *                  xml.getContent().add(g._paint); // First.
 *                  xml.getContent().add(g._stroke); // Second.
 *                  xml.getContent().add(g._transform); // Third.
 *              }
 *              public Object parse(XmlElement xml) {
 *                  Graphic g = (Graphic) xml.object();
 *                  g._isVisible = xml.getAttribute("isVisible", true);
 *                  FastNode n = xml.getContent().headNode();
 *                  g._paint = (Paint) (n = n.getNextNode()).getValue(); // First.
 *                  g._stroke = (Stroke) (n = n.getNextNode()).getValue(); // Second.
 *                  g._transform = (Transform) (n = n.getNextNode()).getValue(); // Third.
 *                  return g;
 *             }
 *         };
 *    }</pre>
 * <p> Default formats for: <code>null, java.lang.Object, java.lang.String,
 *     javolution.lang.Text, javolution.lang.Appendable, j2me.util.Collection
 *     </code> and <code>j2me.util.Map</code> are also provided.
 *     Here is an example of serialization/deserialization using
 *     these predefined formats:<pre>
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
 * @version 3.2, March 18, 2005
 */
public abstract class XmlFormat/*<T>*/ {

    /**
     * Holds the base class to format look-up table (no removal allowed)
     */
    private static final FastMap BASE_CLASS_TO_FORMAT = new FastMap();

    /**
     * Hold the updated class to format look-up table (no removal allowed).
     */
    private static final FastMap CLASS_TO_FORMAT = new FastMap();

    /**
     * Holds class to tag name (CharSequence) look-up table 
     * (no removal allowed).
     */
    private static final FastMap CLASS_TO_NAME = new FastMap();

    /**
     * Holds the tag name (CharSequence) to class look-up table 
     * (no removal allowed).
     */
    private static final FastMap NAME_TO_CLASS = new FastMap();

    /**
     * Holds (uri, local name) to class look-up table (no removal allowed) 
     */
    private static final FastMap URI_LOCAL_NAME_TO_CLASS = new FastMap();

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
            for (FastList.Node n =  xml.getContent().headNode(), 
                    end = xml.getContent().tailNode();
                    (n = n.getNextNode()) != end;) {
                Object key = n.getValue();
                Object value = (n = n.getNextNode()).getValue();
                map.put(key, value);
            }
            return map;
        }
    };

    /**
     * Holds the default XML representation for <code>java.lang.String</code>
     * classes. This representation consists of a <code>"value"</code> attribute 
     * holding the string.
     */
    public static final XmlFormat STRING_XML = new XmlFormat("java.lang.String") {
        public void format(Object obj, XmlElement xml) {
            xml.setAttribute("value", (String) obj);
        }

        public Object parse(XmlElement xml) {
            return xml.getAttribute("value", "");
        }
    };

    /**
     * Holds the default XML representation for <code>javolution.lang.Text</code>
     * classes. This representation consists of a <code>"value"</code> attribute 
     * holding the characters.
     */
    public static final XmlFormat TEXT_XML = new XmlFormat("javolution.lang.Text") {
        public void format(Object obj, XmlElement xml) {
            xml.setAttribute("value", (Text) obj);
        }

        public Object parse(XmlElement xml) {
            CharSequence csq = xml.getAttribute("value");
            return csq != null ? Text.valueOf(csq) : Text.EMPTY;
        }
    };

    /**
     * Holds the default XML representation for <code>javolution.lang.Appendable</code>
     * classes. This representation consists of a <code>"value"</code> attribute 
     * holding the characters.
     */
    public static final XmlFormat APPENDABLE_XML = new XmlFormat("javolution.lang.Appendable") {
        public void format(Object obj, XmlElement xml) {
            xml.setAttribute("value", Text.valueOf(obj));
        }

        public Object parse(XmlElement xml) {
            Appendable appendable = (Appendable) xml.object();
            CharSequence csq = xml.getAttribute("value");
            try {
                return csq != null ? appendable.append(csq) : appendable;
            } catch (IOException e) {
                throw new XmlException(e);
            }
        }
    };

    /**
     * The class/interface mapped to this format.
     */
    private Class _mappedClass;

    /**
     * Holds the name identifier.
     */
    final Text _idName;

    /**
     * Holds the reference identifier.
     */
    final Text _idRef;

    /**
     * Default constructor (used for local mapping).
     */
    protected XmlFormat() {
        _idName = null;
        _idRef = null;
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
        this();
        _mappedClass = mappedClass;
        setInstance();
    }
    
    /**
     * Creates a default XML mapping for the specified class/interface; 
     * this mapping is inherited by sub-classes or implementing classes.
     * 
     * @param mappedClass the class or interface for which this 
     *        XML format can be used.
     * @param idName the qualified attribute identifier for non-references. 
     * @param idRef the qualified attribute identifier for references.
     * @throws IllegalArgumentException if a default mapping already exists 
     *        for the specified class.
     */
    protected XmlFormat(Class mappedClass, String idName, String idRef) {
        _idName = (idName != null) ? Text.valueOf(idName) : null;
        _idRef = (idRef != null) ? Text.valueOf(idRef) : null;
        if( mappedClass != null ) {
            _mappedClass = mappedClass;
            setInstance();
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
        this();
        try {
            _mappedClass = Reflection.getClass(className);
        } catch (ClassNotFoundException e) {
            throw new JavolutionError(e);
        }
        setInstance();
    }

    private void setInstance() {
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
        if (obj != null) 
            return (XmlFormat) obj;
        

        // Ensures that the class is initialized.
        try {
            Reflection.getClass(mappedClass.getName());
        } catch (ClassNotFoundException e) {
            // Ignores (hopefully the class has already been initialized).
        }
        Class bestMatchClass = null;
        XmlFormat bestMatchFormat = null;
        // Searches best match.
        for (FastMap.Entry e = BASE_CLASS_TO_FORMAT.headEntry(), end = BASE_CLASS_TO_FORMAT
                .tailEntry(); (e = e.getNextEntry()) != end;) {
            Class clazz = (Class) e.getKey();
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
     * Returns the class for the specified identifier.
     *
     * @param  classId the class uri namespace and local name.
     * @throws XmlException if there is no class matching the specified classId.
     */
    static Class classFor(UriLocalName classId) {
        // Searches current mapping.
        Class clazz = (Class) URI_LOCAL_NAME_TO_CLASS.get(classId);
        if (clazz != null) 
            return clazz;

        // Extracts the class name (or alias).
        String uri = classId.uri.toString();
        String localName = classId.localName.toString();
        String className;
        if ((uri.length() == 0) || uri.equals("http://javolution.org")) {
            className = localName; // Class name is the local name.
        } else { // Use package prefix.   
            if (uri.startsWith("java:")) {
                className = (uri.length() > 5) ? uri.substring(5) + "." + localName
                    : localName;
            } else {
                throw new XmlException("Invalid URI (must use a java scheme)");
           }
        }

        // Finds the class object.
        clazz = (Class) NAME_TO_CLASS.get(className);
        if (clazz == null) {
            try {
                clazz = Reflection.getClass(className);
            } catch (ClassNotFoundException e) {
                throw new XmlException(e);
            }
            synchronized (CLASS_TO_NAME) {
                CLASS_TO_NAME.put(clazz, className);
                NAME_TO_CLASS.put(className, clazz);
            }
        }

        // Adds new id-class mapping.
        UriLocalName mapKey = new UriLocalName();
        mapKey.uri = uri;
        mapKey.localName = localName;
        synchronized (URI_LOCAL_NAME_TO_CLASS) {
            URI_LOCAL_NAME_TO_CLASS.put(mapKey, clazz);
        }
        return clazz;
    }

    /**
     * Invalidates the current class to format mapping. 
     * Look-up table cannot be cleared (to avoid synchronized read),
     * each entry value is set to null.
     */
    private static void invalidateClassToFormatMapping() {
        synchronized (CLASS_TO_FORMAT) {
            for (FastMap.Entry e = CLASS_TO_FORMAT.headEntry(), end = CLASS_TO_FORMAT
                    .tailEntry(); (e = e.getNextEntry()) != end;) {
                e.setValue(null);
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
    public Object/*T*/ preallocate(XmlElement xml) {
        return null;
    }

    /**
     * Formats an object into the specified {@link XmlElement}.
     *
     * @param obj the object to format.
     * @param xml the <code>XmlElement</code> destination.
     */
    public abstract void format(Object/*T*/ obj, XmlElement xml);

    /**
     * Parses the specified {@link XmlElement} to produce an object. 
     * 
     * @param xml the <code>XmlElement</code> to parse.
     * @return an <code>Object</code> parsed from the specified 
     *         <code>XmlElement</code>. 
     * @throws IllegalArgumentException if the character sequence contains
     *         an illegal syntax.
     */
    public abstract Object/*T*/ parse(XmlElement xml);

    /**
     * This class represents a URI / LocalName class identifier.
     */
    static final class UriLocalName {

        Object uri; // String when stored in map.

        Object localName; // String when stored in map.

        public boolean equals(Object obj) {
            UriLocalName that = (UriLocalName) obj;
            return that.localName.equals(this.localName)
                    && that.uri.equals(this.uri);
        }

        public int hashCode() {
            return localName.hashCode();
        }

        public String toString() {
            return "(" + uri.toString() + "," + localName.toString() + ")";
        }
    }

}