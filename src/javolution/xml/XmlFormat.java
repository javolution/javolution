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
import javolution.lang.TypeFormat;
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
 *         protected static final XmlFormat&lt;Graphic&gt; GRAPHIC_XML = new XmlFormat&lt;Graphic&gt;(Graphic.class) {
 *              public void format(Graphic g, XmlElement xml) {
 *                  xml.setAttribute("isVisible", g._isVisible); 
 *                  xml.getContent().add(g._paint); // First.
 *                  xml.getContent().add(g._stroke); // Second.
 *                  xml.getContent().add(g._transform); // Third.
 *              }
 *              public Graphic parse(XmlElement xml) {
 *                  Graphic g = xml.object();
 *                  g._isVisible = xml.getAttribute("isVisible", true);
 *                  g._paint = (Paint) xml.getContent().removeFirst();
 *                  g._stroke = (Stroke) xml.getContent().removeFirst();
 *                  g._transform = (Transform) xml.getContent().removeFirst();
 *                  return g;
 *             }
 *         };
 *    }</pre>
 * <p> A default format is defined for <code>null</code> values 
 *     (<code>&lt;null/&gt;</code>) and the following types:<ul>
 *        <li><code>java.lang.Object</code> (empty tag)</li>
 *        <li><code>java.lang.Class</code></li>
 *        <li><code>java.lang.String</code></li>
 *        <li><code>javolution.lang.Text</code></li>
 *        <li><code>javolution.lang.Appendable</code></li>
 *        <li><code>j2me.util.Collection</code></li>
 *        <li><code>j2me.util.Map</code></li>
 *        <li>and all primitive types wrappers (e.g. 
 *            <code>Boolean, Integer ...</code>)</li>
 *        </ul></p>
 *        
 * <p>Here is an example of serialization/deserialization using predefined 
 *    formats:<pre>
 *     ArrayList names = new ArrayList();
 *     names.add("John Doe");
 *     names.add(null);
 *     names.add("Jean Bon");
 *     ObjectWriter ow = new ObjectWriter();
 *     ow.setNamespace("", "java.lang"); // Default namespace for java.lang.* classes
 *     ow.write(names, new FileOutputStream("C:/names.xml"));</pre>
 *     Here is the output <code>names.xml</code> document produced:<pre>
 *     &lt;j:java.util.ArrayList xmlns:j="http://javolution.org" xmlns="java:java.lang">
 *         &lt;String value="John Doe"/>
 *         &lt;j:null/>
 *         &lt;String value="Jean Bon"/>
 *     &lt;/j:java.util.ArrayList></pre>
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
 *     the {@link #setAlias aliases} of the obfuscated classes. Aliases can also
 *     be used when the application has no control over the xml representation
 *     (e.g. SOAP messages). Then, the aliases holds the mapping between 
 *     the tag name and the associated class.</p>
 * 
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 3.3, March 18, 2005
 */
public abstract class XmlFormat/*<T>*/{

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
     * Holds the object representing <code>null</code> values
     * <p> Note: Applications may change the representation of <code>null</code>
     *     values by changing the class alias of this object. For example:<pre>
     *     XmlFormat.setAlias(XmlFormat.NULL.getClass(), "none");</pre></p> 
     */
    protected static final Null NULL = new Null();

    /**
     * Holds the default XML representation when a more specific format 
     * cannot be found. This representation consists of an empty XML element
     * with no attribute. Objects are deserialized using the class default 
     * constructor (the class being identified by the element's name).
     */
    protected static final XmlFormat OBJECT_XML = new XmlFormat() {
        public void format(Object obj, XmlElement xml) {
            // Do nothing.
        }

        public Object parse(XmlElement xml) {
            return xml.object();
        }
    };

    /**
     * Holds the XML representation for <code>null</code> objects
     * (<code>&lt;null/&gt;</code>).
     */
    private static final XmlFormat NULL_XML = new XmlFormat(NULL.getClass()) {
        public void format(Object obj, XmlElement xml) {
            // Empty tag.
        }

        public Object parse(XmlElement xml) {
            return null;
        }
    };
    static {
        XmlFormat.setAlias(NULL.getClass(), "null");
    }

    /**
     * Holds the default XML representation for <code>java.lang.Class</code>
     * instances. This representation consists of a <code>"name"</code> 
     * attribute holding the class name.
     */
    protected static final XmlFormat CLASS_XML = new XmlFormat(
            "java.lang.Class") {
        public void format(Object obj, XmlElement xml) {
            xml.setAttribute("value", ((Class) obj).getName());
        }

        public Object parse(XmlElement xml) {
            try {
                return Reflection.getClass(xml.getAttribute("name", ""));
            } catch (ClassNotFoundException e) {
                throw new XmlException(e);
            }
        }
    };

    /**
     * Holds the default XML representation for <code>java.lang.String</code>
     * classes. This representation consists of a <code>"value"</code> attribute 
     * holding the string.
     */
    protected static final XmlFormat STRING_XML = new XmlFormat(
            "java.lang.String") {
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
    protected static final XmlFormat TEXT_XML = new XmlFormat(
            "javolution.lang.Text") {
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
    protected static final XmlFormat APPENDABLE_XML = new XmlFormat(
            "javolution.lang.Appendable") {
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
     * Holds the default XML representation for classes implementing 
     * the <code>j2me.util.Collection</code> interface.
     * This representation consists of nested XML elements one for each 
     * element of the collection. The elements' order is defined by
     * the collection iterator order. Collections are deserialized using their
     * default constructor. 
     */
    protected static final XmlFormat COLLECTION_XML = new XmlFormat(
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
    protected static final XmlFormat MAP_XML = new XmlFormat("j2me.util.Map") {

        public void format(Object obj, XmlElement xml) {
            Map map = (Map) obj;
            for (Iterator it = map.entrySet().iterator(); it.hasNext();) {
                Map.Entry entry = (Map.Entry) it.next();
                xml.getContent().addLast(entry.getKey());
                xml.getContent().addLast(entry.getValue());
            }
        }

        public Object parse(XmlElement xml) {
            Map map = (Map) xml.object();
            for (FastList.Node n = xml.getContent().headNode(), end = xml
                    .getContent().tailNode(); (n = n.getNextNode()) != end;) {
                Object key = n.getValue();
                Object value = (n = n.getNextNode()).getValue();
                map.put(key, value);
            }
            return map;
        }
    };

    /**
     * Holds the default XML representation for <code>java.lang.Boolean</code>.
     */
    protected static final XmlFormat BOOLEAN_XML = new XmlFormat(
            "java.lang.Boolean") {
        public void format(Object obj, XmlElement xml) {
            xml.setAttribute("value", ((Boolean) obj).booleanValue());
        }

        public Object parse(XmlElement xml) {
            CharSequence csq = xml.getAttribute("value");
            if (csq == null)
                throw new XmlException("Missing value attribute");
            return new Boolean(TypeFormat.parseBoolean(csq));
        }
    };

    /**
     * Holds the default XML representation for <code>java.lang.Byte</code>.
     */
    protected static final XmlFormat BYTE_XML = new XmlFormat("java.lang.Byte") {
        public void format(Object obj, XmlElement xml) {
            xml.setAttribute("value", ((Byte) obj).byteValue());
        }

        public Object parse(XmlElement xml) {
            CharSequence csq = xml.getAttribute("value");
            if (csq == null)
                throw new XmlException("Missing value attribute");
            return new Byte(TypeFormat.parseByte(csq));
        }
    };

    /**
     * Holds the default XML representation for <code>java.lang.Character</code>.
     */
    protected static final XmlFormat CHARACTER_XML = new XmlFormat(
            "java.lang.Character") {
        public void format(Object obj, XmlElement xml) {
            xml.setAttribute("value", Text.valueOf(((Character) obj)
                    .charValue()));
        }

        public Object parse(XmlElement xml) {
            CharSequence csq = xml.getAttribute("value");
            if ((csq == null) || (csq.length() != 1))
                throw new XmlException("Missing or invalid value attribute");
            return new Character(csq.charAt(0));
        }
    };

    /**
     * Holds the default XML representation for <code>java.lang.Integer</code>.
     */
    protected static final XmlFormat INTEGER_XML = new XmlFormat(
            "java.lang.Integer") {
        public void format(Object obj, XmlElement xml) {
            xml.setAttribute("value", ((Integer) obj).intValue());
        }

        public Object parse(XmlElement xml) {
            CharSequence csq = xml.getAttribute("value");
            if (csq == null)
                throw new XmlException("Missing value attribute");
            return new Integer(TypeFormat.parseInt(csq));
        }
    };

    /**
     * Holds the default XML representation for <code>java.lang.Long</code>.
     */
    protected static final XmlFormat LONG_XML = new XmlFormat("java.lang.Long") {
        public void format(Object obj, XmlElement xml) {
            xml.setAttribute("value", ((Long) obj).longValue());
        }

        public Object parse(XmlElement xml) {
            CharSequence csq = xml.getAttribute("value");
            if (csq == null)
                throw new XmlException("Missing value attribute");
            return new Long(TypeFormat.parseLong(csq));
        }
    };

    /**
     * Holds the default XML representation for <code>java.lang.Short</code>.
     */
    protected static final XmlFormat SHORT_XML = new XmlFormat("java.lang.Short") {
        public void format(Object obj, XmlElement xml) {
            xml.setAttribute("value", ((Short) obj).shortValue());
        }

        public Object parse(XmlElement xml) {
            CharSequence csq = xml.getAttribute("value");
            if (csq == null)
                throw new XmlException("Missing value attribute");
            return new Short(TypeFormat.parseShort(csq));
        }
    };

    /**
     * Holds the default XML representation for <code>java.lang.Float</code>.
     /*@FLOATING_POINT@
     protected static final XmlFormat FLOAT_XML = new XmlFormat("java.lang.Float") {
     public void format(Object obj, XmlElement xml) {
     xml.setAttribute("value", ((Float)obj).floatValue());
     }

     public Object parse(XmlElement xml) {
     CharSequence csq = xml.getAttribute("value");
     if (csq == null) 
     throw new XmlException("Missing value attribute");
     return new Float(TypeFormat.parseFloat(csq));
     }
     };
     /**/

    /**
     * Holds the default XML representation for <code>java.lang.Double</code>.
     /*@FLOATING_POINT@
     protected static final XmlFormat DOUBLE_XML = new XmlFormat("java.lang.Double") {
     public void format(Object obj, XmlElement xml) {
     xml.setAttribute("value", ((Double)obj).doubleValue());
     }

     public Object parse(XmlElement xml) {
     CharSequence csq = xml.getAttribute("value");
     if (csq == null) 
     throw new XmlException("Missing value attribute");
     return new Double(TypeFormat.parseDouble(csq));
     }
     };
     /**/

    /**
     * The class/interface mapped to this format.
     */
    private Class _mappedClass;

    /**
     * Holds the xml format this format overrides.
     */
    private XmlFormat _super;

    /**
     * Holds the name identifier.
     */
    final Text _idName;

    /**
     * Holds the reference identifier.
     */
    final Text _idRef;

    /**
     * Default constructor (used for dynamic mapping).
     */
    protected XmlFormat() {
        _idName = null;
        _idRef = null;
    }

    /**
     * Creates a default XML mapping for the specified class/interface; 
     * this mapping is inherited by sub-classes or implementing classes.
     * 
     * @param clazz the class/interface for which this XML format can be used.
     * @throws IllegalArgumentException if a default mapping already exists 
     *        for the specified class.
     */
    protected XmlFormat(Class/*<T>*/ clazz) {
        mapTo(clazz);
		_idName = _super._idName;
		_idRef = _super._idRef;
    }

    /**
     * Creates a default XML mapping for the specified class/interface; 
     * this mapping is inherited by sub-classes or implementing classes.
     * 
     * @param clazz the class/interface for which this XML format can be used.
     * @param idName the qualified attribute identifier for non-references. 
     * @param idRef the qualified attribute identifier for references.
     * @throws IllegalArgumentException if a default mapping already exists 
     *        for the specified class.
     */
    protected XmlFormat(Class/*<T>*/ clazz, String idName, String idRef) {
        mapTo(clazz);
        _idName = (idName != null) ? Text.valueOf(idName) : null;
        _idRef = (idRef != null) ? Text.valueOf(idRef) : null;
    }

    /**
     * Creates a default XML mapping for the class/interface having the 
     * specified name.
     * 
     * @param className the name of the class/interface for which this 
     *        XML format can be used.
     * @throws IllegalArgumentException if a default mapping already exists 
     *        for the specified class.
     */
    protected XmlFormat(String className) {
        try {
            mapTo(Reflection.getClass(className));
        } catch (ClassNotFoundException e) {
            throw new JavolutionError(e);
        }
 		_idName = _super._idName;
		_idRef = _super._idRef;
    }

    private void mapTo(Class clazz) {
		_mappedClass = clazz;
        synchronized (BASE_CLASS_TO_FORMAT) {
            if (BASE_CLASS_TO_FORMAT.containsKey(clazz)) {
                throw new IllegalArgumentException(
                        "Mapping already exists for " + clazz);
            }
            _super = XmlFormat.getInstance(clazz);
            BASE_CLASS_TO_FORMAT.put(clazz, this);
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
            xmlFormat._super = XmlFormat.getInstance(forClass);
            BASE_CLASS_TO_FORMAT.put(forClass, xmlFormat);
            invalidateClassToFormatMapping();
        }
    }

    /**
     * Returns the format for the specified class/interface.
     * This method looks for the more specialized format; if none is found
     * the default {@link Object} format is returned (empty element).
     * 
     * <p> Note: This method forces the initialization of the specified
     *           class. This to ensure that the class static fields (which 
     *           may hold the most specialized format) are initialized.</p> 
     * 
     * @param mappedClass the class/interface for which the most specialized 
     *        format is returned.
     * @return the format to use for the specified class.
     */
    public static /*<T>*/ XmlFormat/*<T>*/ getInstance(Class/*<T>*/ mappedClass) {

        // Checks look-up.
        Object obj = CLASS_TO_FORMAT.get(mappedClass);
        if (obj != null)
            return (XmlFormat/*<T>*/) obj;

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
        return (XmlFormat/*<T>*/) bestMatchFormat;

    }

    /**
     * Sets the element name (tag name) to be used when serializing instances
     * of the specified class. This method is particularly useful
     * in case of obfuscation to ensure proper/invariant xml formatting 
     * (you don't want to use the obfuscated class name in such case).
     * It is also useful if you have no control over the xml format (e.g.
     * SOAP messages) in order to manually control the mapping between the 
     * element name and the corresponding class (as you cannot rely
     * upon the <code>"j:class"</code> attribute to be set).
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
     * @param  uri class uri namespace (CharSequenceImpl or String).
     * @param  localName the local name (CharSequenceImpl).
     * @throws XmlException if there is no class matching the specified classId.
     */
    static Class classFor(Object uri, CharSequence localName) {
        // Searches current mapping.
        FastList list = (FastList) URI_LOCAL_NAME_TO_CLASS.get(localName);
        if (list != null) {
            for (int i=1; i < list.size(); i += 2) {
                if (uri.equals(list.get(i))) {
                    return (Class) list.get(i-1);
                }
            }
        }
        // Extracts the class name (or alias).
        String uriAsString = uri.toString();
        String localNameAsString = localName.toString();
        String className;
        if ((uriAsString.length() == 0) || uriAsString.equals("http://javolution.org")) {
            className = localNameAsString; // Class name is the local name.
        } else { // Use package prefix.   
            if (uriAsString.startsWith("java:")) {
                className = (uriAsString.length() > 5) ? uriAsString.substring(5) + "."
                        + localNameAsString : localNameAsString;
            } else {
                throw new XmlException("Invalid URI (must use a java scheme)");
            }
        }

        // Finds the class object.
        Class clazz = (Class) NAME_TO_CLASS.get(className);
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
        synchronized (URI_LOCAL_NAME_TO_CLASS) {
            list = (FastList) URI_LOCAL_NAME_TO_CLASS.get(localNameAsString);
            if (list == null) {
                list = (FastList) FastList.newInstance().moveHeap();
                URI_LOCAL_NAME_TO_CLASS.put(localNameAsString, list);
            } 
            list.addLast(clazz);
            list.addLast(uriAsString);
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
     * Returns the format being overriden by this format.
     *
     * @return the format being overriden.
     */
    public final /*<T>*/ XmlFormat/*<T>*/ getSuper() {
        return  (XmlFormat/*<T>*/) _super;
    }

    /**
     * Preallocates the object corresponding to this xml element in order to
     * support circular references. 
     *
     * @param xml the xml elements holding this objects's attribute but 
     *        not its content yet.
     * @return <code>getSuper().preallocate(xml)</code>
     * @see    XmlElement#object()
     */
    public Object/*T*/preallocate(XmlElement xml) {
        return (_super != null) ? (Object/*T*/)_super.preallocate(xml) : null;
    }


    /**
     * <p> Returns the class associated to the specified element name.</p>
     * <p> By default when name association is employed, a <code>"j:class"
     *     </code>attribute holding the class name (or alias) is created.</p>
     * <p> Applications may avoid this extra attribute when the name-to-class
     *     mapping is known at compile time. For example:<pre>
     *  public class Graphic {
     *      private java.awt.Color _color; // Color class is final.
     *      static final XmlFormat&lt;Graphic> GRAPHIC_XML = new XmlFormat&lt;Graphic>(Graphic.class) {
     *          public void format(Graphic g, XmlElement xml) {
     *              xml.add("Color", g._color);
     *          }
     *          public Graphic parse(XmlElement xml) {
     *              Graphic g =  xml.object();
     *              g._color = xml.get("Color");
     *              return g;
     *          }
     *          public Class classFor(CharSequence elementName) {
     *              if (name.equals("Color")) return java.awt.Color;
     *              return getSuper().classFor(name);
     *          }
     *      };
     *  }</pre>
     *
     * @param elementName the element name or tag name for which the associated 
     *       class is known and returned.
     * @return <code>getSuper().classFor(name)</code>
     */
    public Class classFor(CharSequence elementName) {
        return (_super != null) ? _super.classFor(elementName) : null;
    }

    /**
     * Formats an object into the specified {@link XmlElement}.
     *
     * @param obj the object to format.
     * @param xml the <code>XmlElement</code> destination.
     */
    public abstract void format(Object/*T*/obj, XmlElement xml);

    /**
     * Parses the specified {@link XmlElement} to produce an object. 
     * 
     * @param xml the <code>XmlElement</code> to parse.
     * @return an <code>Object</code> parsed from the specified 
     *         <code>XmlElement</code>. 
     * @throws IllegalArgumentException if the character sequence contains
     *         an illegal syntax.
     */
    public abstract Object/*T*/parse(XmlElement xml);

    /**
     * Holds the class for <code>null</code> values. 
     */
    private static class Null {
    };
}