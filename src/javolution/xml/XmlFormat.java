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
import j2me.lang.IllegalStateException;
import j2me.util.Collection;
import j2me.util.Iterator;
import j2me.util.Map;

import javolution.JavolutionError;
import javolution.lang.Appendable;
import javolution.lang.Reflection;
import javolution.lang.Text;
import javolution.lang.TypeFormat;
import javolution.util.FastMap;

/**
 * <p> This class represents the format base class for XML serialization and
 *     deserialization.</p>
 *     
 * <p> Application classes typically define a XML format for their instances 
 *     using static {@link XmlFormat} class members. 
 *     The format is inherited by sub-classes. For example:<pre>
 *     public abstract class Graphic {
 *         private boolean _isVisible;
 *         private Paint _paint; // null if none.
 *         private Stroke _stroke; // null if none.
 *         private Transform _transform; // null if none.
 *          
 *         // XML format with positional associations (members identified by their position),
 *         // see {@link javolution.xml} for examples of name associations.
 *         public static final XmlFormat&lt;Graphic&gt; GRAPHIC_XML = new XmlFormat&lt;Graphic&gt;(Graphic.class) {
 *              public void format(Graphic g, XmlElement xml) {
 *                  xml.setAttribute("isVisible", g._isVisible); 
 *                  getSuper().format(g, xml); 
 *                  xml.add(g._paint); // First.
 *                  xml.add(g._stroke); // Second.
 *                  xml.add(g._transform); // Third.
 *              }
 *              public Graphic parse(XmlElement xml) {
 *                  g._isVisible = xml.getAttribute("isVisible", true);
 *                  Graphic g = (Graphic) getSuper().parse(xml);
 *                  g._paint = (Paint) xml.getNext();
 *                  g._stroke = (Stroke) xml.getNext();
 *                  g._transform = (Transform) xml.getNext();
 *                  return g;
 *             }
 *         };
 *    }</pre>
 *    
 * <p> Due to the sequential nature of xml serialization/deserialization, 
 *     formatting/parsing of xml attributes should always be performed before 
 *     formatting/parsing of the xml content.</p>
 * 
 * <p> Xml formats can also be dynamically associated. For example:<pre>
 * 
 *     // XML Conversion (different formats for reading and writing).
 *     XmlFormat&lt;Foo&gt; readFormat = new XmlFormat&lt;Foo&gt;() {...};
 *     XmlFormat&lt;Foo&gt; writeFormat = new XmlFormat&lt;Foo&gt;() {...};
 *     XmlFormat.setInstance(readFormat, Foo.class);
 *     Foo foo = new ObjectReader&lt;Foo&gt;().read(in);
 *     XmlFormat.setInstance(writeFormat, Foo.class);
 *     new ObjectWriter&lt;Foo&gt;().write(foo, out);</pre></p>
 *     
 * <p> A default format is defined for <code>null</code> values 
 *     (<code>&lt;null/&gt;</code>) and the following types:<ul>
 *        <li><code>java.lang.Object</code> (default)</li>
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
 *     List list = new ArrayList();
 *     list.add("John Doe");
 *     list.add(null);
 *     Map map = new FastMap();
 *     map.put("ONE", new Integer(1));
 *     map.put("TWO", new Integer(2));
 *     list.add(map);
 *     ObjectWriter ow = new ObjectWriter();
 *     ow.write(list, new FileOutputStream("C:/list.xml"));</pre>
 *     Here is the output <code>list.xml</code> document produced:<pre>
 *     &lt;java.util.ArrayList xmlns:j="http://javolution.org">
 *         &lt;java.lang.String value="John Doe"/>
 *         &lt;null/>
 *         &lt;javolution.util.FastMap>
 *             &lt;Key j:class="java.lang.String" value="ONE"/>
 *             &lt;Value j:class="java.lang.Integer" value="1"/>
 *             &lt;Key j:class="java.lang.String" value="TWO"/>
 *             &lt;Value j:class="java.lang.Integer" value="2"/>
 *         &lt;/javolution.util.FastMap></pre>
 *     The list can be read back with the following code:<pre>
 *     ObjectReader or = new ObjectReader();
 *     List list = (List) or.read(new FileInputStream("C:/list.xml"));
 *     </pre></p>
 *     
 * <p> Finally, xml formats can be made impervious to obfuscation by setting 
 *     the {@link #setAlias aliases} of the obfuscated classes at start-up.</p>
 * 
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 3.5, August 29, 2005
 */
public abstract class XmlFormat/*<T>*/{

    /**
     * Holds the xml format instance (base class to xml format mapping,
     * no removal allowed).
     */
    private static final FastMap INSTANCES = new FastMap();

    /**
     * Holds the class to xml format mapping (no removal allowed).
     */
    private static final FastMap CLASS_TO_FORMAT = new FastMap();

    /**
     * Holds class to alias (String) look-up table (no removal allowed).
     */
    private static final FastMap CLASS_TO_ALIAS = new FastMap();

    /**
     * Holds the alias (String) to class look-up table (no removal allowed).
     */
    private static final FastMap ALIAS_TO_CLASS = new FastMap();

    /**
     * Holds the default XML representation when a more specific format 
     * cannot be found. This representation consists of an empty XML element
     * with no attribute. Objects are deserialized using the class default 
     * constructor (the class being identified by the element's name).
     */
    public static final XmlFormat DEFAULT_XML = new XmlFormat() {
        public void format(Object obj, XmlElement xml) {
            // Do nothing.
        }

        public Object parse(XmlElement xml) {
            return xml.object();
        }
    };

    /**
     * Holds the <code>null</code> object.
     */
    static final Null NULL = new Null();

    /**
     * Holds the XML representation for <code>null</code> objects
     * (<code>&lt;null/&gt;</code>). Applications may change the 
     * representation of <code>null</code> values by aliasing the  
     * {@link #getTargetClass() target class} of this format. For example:
     * <pre>XmlFormat.setAlias(NULL_XML.getTargetClass(), "none");</pre>
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
        XmlFormat.setAlias(NULL_XML.getTargetClass(), "null");
    }

    /**
     * Holds the default XML representation for <code>java.lang.Class</code>
     * instances. This representation consists of a <code>"name"</code> 
     * attribute holding the class name.
     */
    public static final XmlFormat CLASS_XML = new XmlFormat("java.lang.Class") {
        public void format(Object obj, XmlElement xml) {
            xml.setAttribute("name", ((Class) obj).getName());
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
    public static final XmlFormat TEXT_XML = new XmlFormat(
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
    public static final XmlFormat APPENDABLE_XML = new XmlFormat(
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
    public static final XmlFormat COLLECTION_XML = new XmlFormat(
            "j2me.util.Collection") {
        public void format(Object obj, XmlElement xml) {
            for (Iterator i = ((Collection) obj).iterator(); i.hasNext();) {
                xml.add(i.next());
            }
        }

        public Object parse(XmlElement xml) {
            Collection collection = (Collection) xml.object();
            while (xml.hasNext()) {
                collection.add(xml.getNext());
            }
            return collection;
        }
    };

    /**
     * Holds the default XML representation for classes implementing 
     * the <code>j2me.util.Map</code> interface.
     * This representation consists of key/value pair as nested XML elements.
     * For example:<pre>
     * &lt;javolution.util.FastMap>
     *     &lt;Key j:class="java.lang.String" value="ONE"/>
     *     &lt;Value j:class="java.lang.Integer" value="1"/>
     *     &lt;Key j:class="java.lang.String" value="TWO"/>
     *     &lt;Value j:class="java.lang.Integer" value="2"/>
     *     &lt;Key j:class="java.lang.String" value="THREE"/>
     *     &lt;Value j:class="java.lang.Integer" value="3"/>
     * &lt;/javolution.util.FastMap></pre>
     * 
     * (elements' order is defined by the map 's entries iterator order. 
     * Maps are deserialized using their default constructor.
     */
    public static final XmlFormat MAP_XML = new XmlFormat("j2me.util.Map") {

        public void format(Object obj, XmlElement xml) {
            Map map = (Map) obj;
            for (Iterator it = map.entrySet().iterator(); it.hasNext();) {
                Map.Entry entry = (Map.Entry) it.next();
                xml.add(entry.getKey(), "Key");
                xml.add(entry.getValue(), "Value");
            }
        }

        public Object parse(XmlElement xml) {
            Map map = (Map) xml.object();
            while (xml.hasNext()) {
                Object key = xml.get("Key");
                Object value = xml.get("Value");
                map.put(key, value);
            }
            return map;
        }
    };

    /**
     * Holds the default XML representation for <code>java.lang.Boolean</code>.
     */
    public static final XmlFormat BOOLEAN_XML = new XmlFormat(
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
    public static final XmlFormat BYTE_XML = new XmlFormat("java.lang.Byte") {
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
    public static final XmlFormat CHARACTER_XML = new XmlFormat(
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
    public static final XmlFormat INTEGER_XML = new XmlFormat(
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
    public static final XmlFormat LONG_XML = new XmlFormat("java.lang.Long") {
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
    public static final XmlFormat SHORT_XML = new XmlFormat("java.lang.Short") {
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
     public static final XmlFormat FLOAT_XML = new XmlFormat("java.lang.Float") {
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
     public static final XmlFormat DOUBLE_XML = new XmlFormat("java.lang.Double") {
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
     * The class/interface directly mapped to this format.
     */
    private Class _targetClass;

    /**
     * Holds the xml format that this format overrides or <code>null<code>
     * if unknown.
     */
    private XmlFormat _super;

    /**
     * Creates a XML format not mapped to any class (dynamic mapping).
     */
    protected XmlFormat() {
    }

    /**
     * Creates a default XML format for instances of the specified 
     * class/interface.
     * 
     * @param targetClass the target class/interface for this XML format.
     * @throws j2me.lang.IllegalStateException if the specified class is 
     *         already mapped to another format.
     */
    protected XmlFormat(Class/*<T>*/targetClass) {
        if (INSTANCES.containsKey(targetClass))
            throw new IllegalStateException("XmlFormat already mapped to "
                    + targetClass);
        setInstance(this, targetClass);
    }

    /**
     * Creates a default XML format for instances of the class/interface 
     * identified by the specified <code>String</code>.
     * 
     * @param className the name of the target class/interface for this 
     *        XML format.
     * @throws j2me.lang.IllegalStateException if the specified class is 
     *         already mapped to another format.
     */
    protected XmlFormat(String className) {
        Class clazz;
        try {
            clazz = Reflection.getClass(className);
        } catch (ClassNotFoundException e) {
            throw new JavolutionError(e);
        }
        if (INSTANCES.containsKey(clazz))
            throw new IllegalStateException("XmlFormat already mapped to "
                    + clazz);
        setInstance(this, clazz);
    }

    /**
     * Sets the format for the specified class/interface. Any previous 
     * format associated to the specified class/interface is removed
     * (including default formats if any).
     * 
     * @param xmlFormat the XML format for the specified class/interface
     *        or <code>null</code> to unset the current mapping.
     * @param targetClass the new target class for the specified format.
     */
    public static/*<T>*/void setInstance(XmlFormat/*<T>*/xmlFormat,
            Class/*<T>*/targetClass) {
        synchronized (INSTANCES) {
            if (xmlFormat != null) {
                if (xmlFormat._targetClass != null) {
                    INSTANCES.put(xmlFormat._targetClass, null);
                }
                xmlFormat._targetClass = targetClass;
            }
            INSTANCES.put(targetClass, xmlFormat);
            invalidateClassToFormatMapping();
        }
    }

    /**
     * Returns the format for the specified class/interface.
     * This method looks for the more specialized format; if none is found
     * {@link #DEFAULT_XML} format is returned.
     * 
     * <p> Note: This method forces the initialization of the specified
     *           class. This to ensure that the class static fields (which 
     *           may hold the most specialized format) are initialized.</p> 
     * 
     * @param clazz the class/interface for which the most specialized 
     *        format is returned.
     * @return the format to use for the specified class.
     */
    public static/*<T>*/XmlFormat/*<T>*/getInstance(Class/*<T>*/clazz) {
        Object obj = CLASS_TO_FORMAT.get(clazz);
        if (obj != null)
            return (XmlFormat/*<T>*/) obj;
        return searchInstanceFor(clazz, false);
    }

    /**
     * Sets the alias to be used instead of the class name for the specified 
     * class (element tag name or j:class attribute). This method is 
     * particularly useful in case of obfuscation to ensure proper/invariant 
     * xml formatting (you don't want to use the obfuscated class name in 
     * such case). Aliases should be set prior to using the xml 
     * serialization/deserialization facility (at start-up for example). 
     * 
     * @param forClass the class for which the specified alias should be used.
     * @param alias the name to use for the specified class.
     */
    public static void setAlias(Class forClass, String alias) {
        synchronized (CLASS_TO_ALIAS) {
            CLASS_TO_ALIAS.put(forClass, alias);
            ALIAS_TO_CLASS.put(alias, forClass);
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
    static String aliasFor(Class clazz) {
        String name = (String) CLASS_TO_ALIAS.get(clazz);
        if (name != null)
            return name;
        name = clazz.getName();
        synchronized (CLASS_TO_ALIAS) {
            CLASS_TO_ALIAS.put(clazz, name);
            ALIAS_TO_CLASS.put(name, clazz);
        }
        return name;
    }

    /**
     * Returns the class for the specified identifier.
     *
     * @param  alias the class name or alias.
     * @throws XmlException if there is no class matching the specified classId.
     */
    static Class classFor(CharSequence alias) {
        Class clazz = (Class) ALIAS_TO_CLASS.get(alias);
        if (clazz != null)
            return clazz;
        String className = alias.toString();
        try {
            clazz = Reflection.getClass(className);
        } catch (ClassNotFoundException e) {
            throw new XmlException(e);
        }
        synchronized (CLASS_TO_ALIAS) {
            CLASS_TO_ALIAS.put(clazz, className);
            ALIAS_TO_CLASS.put(className, clazz);
        }
        return clazz;
    }

    /**
     * Returns the format being overriden by this format.
     *
     * @return the format being overriden.
     */
    public final/*<T>*/XmlFormat/*<T>*/getSuper() {
        if (_super != null)
            return _super;
        if (this == DEFAULT_XML)
            return null;
        _super = searchInstanceFor(this._targetClass, true);
        return _super;
    }

    /**
     * Returns the base class/interface for which this format is employed.
     *
     * @return the target class for this format.
     */
    public final Class/*<T>*/getTargetClass() {
        return (Class/*<T>*/) _targetClass;
    }

    /**
     * Allocates a new object corresponding to this xml element.
     * By default, the {@link XmlElement#object} method returns an object 
     * created using the deserialized class public no-arg constructor.
     * Xml formats may perform custom allocations by overriding this method.  
     *
     * @param xml the xml elements.
     * @return the object corresponding to the specified xml element.
     */
    public Object/*T*/allocate(XmlElement xml) {
        return null;
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
     * Searches the best matching format for the specified class.
     *
     * @param clazz the class for which the format is searched. 
     * @param forceInherited <code>true</code> if directly mapped format are 
     *        excluded; <code>false</code> otherwise. 
     * @return the best matching format.
     */
    private static XmlFormat searchInstanceFor(Class clazz,
            boolean forceInherited) {
        // Ensures that the class is initialized.
        try {
            Reflection.getClass(clazz.getName());
        } catch (ClassNotFoundException e) {
            // Ignores (hopefully the class has already been initialized).
        }
        XmlFormat bestMatchFormat = null;
        Class bestMatchClass = null;
        for (FastMap.Entry e = INSTANCES.headEntry(), end = INSTANCES
                .tailEntry(); (e = e.getNextEntry()) != end;) {
            Class cl = (Class) e.getKey();
            if ((cl == clazz) && forceInherited)
                continue;
            if (cl.isAssignableFrom(clazz)) { // Compatible.
                if ((bestMatchClass == null)
                        || (bestMatchClass.isAssignableFrom(cl))) {
                    // Class more specialized that bestMatchClass.
                    XmlFormat xmlFormat = (XmlFormat) e.getValue();
                    bestMatchClass = cl;
                    bestMatchFormat = xmlFormat;
                }
            }
        }

        // If none found, use default object format.
        if (bestMatchFormat == null) {
            bestMatchFormat = DEFAULT_XML;
        }
        // Updates direct look-up (when not inherited format)
        if (!forceInherited) {
            synchronized (CLASS_TO_FORMAT) {
                CLASS_TO_FORMAT.put(clazz, bestMatchFormat);
            }
        }
        return bestMatchFormat;
    }

    /**
     * Invalidates the current class to format mapping (including 
     * (@link #getSuper()} values). 
     */
    private static void invalidateClassToFormatMapping() {
        synchronized (INSTANCES) {
            for (FastMap.Entry e = INSTANCES.headEntry(), end = INSTANCES
                    .tailEntry(); (e = e.getNextEntry()) != end;) {
                XmlFormat xmlFormat = (XmlFormat) e.getValue();
                xmlFormat._super = null;
            }
        }
        // Look-up table cannot be cleared (to avoid synchronized read),
        // each entry value is set to null.
        synchronized (CLASS_TO_FORMAT) {
            for (FastMap.Entry e = CLASS_TO_FORMAT.headEntry(), end = CLASS_TO_FORMAT
                    .tailEntry(); (e = e.getNextEntry()) != end;) {
                e.setValue(null);
            }
        }
    }

    /**
     * Holds the target class for <code>null</code> values. 
     */
    private static class Null {
    };
}