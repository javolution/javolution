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
import javolution.util.FastCollection;
import javolution.util.FastComparator;
import javolution.util.FastMap;
import javolution.util.FastTable;
import javolution.util.LocalMap;

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
 *                  xml.add(g._paint); // First.
 *                  xml.add(g._stroke); // Second.
 *                  xml.add(g._transform); // Third.
 *              }
 *              public Graphic parse(XmlElement xml) {
 *                  Graphic g = xml.object();
 *                  g._isVisible = xml.getAttribute("isVisible", true);
 *                  g._paint = xml.getNext();
 *                  g._stroke = xml.getNext();
 *                  g._transform = xml.getNext();
 *                  return g;
 *             }
 *         };
 *    }</pre>
 *    
 * <p> Due to the sequential nature of xml serialization/deserialization, 
 *     formatting/parsing of xml attributes should always be performed before 
 *     formatting/parsing of the xml content.</p>
 * 
 * <p> Xml formats can dynamically associated. For example:<pre>
 * 
 *     // XML Conversion (different formats for reading and writing).
 *     XmlFormat&lt;Foo&gt; readFormat = new XmlFormat&lt;Foo&gt;() {...};
 *     XmlFormat&lt;Foo&gt; writeFormat = new XmlFormat&lt;Foo&gt;() {...};
 *     LocalContext.enter();
 *     try {  // Local context to avoid impacting other threads.
 *        XmlFormat.setFormat(Foo.class, readFormat);
 *        Foo foo = new ObjectReader&lt;Foo&gt;().read(in);
 *        XmlFormat.setFormat(Foo.class, writeFormat);
 *        new ObjectWriter&lt;Foo&gt;().write(foo, out);
 *     } finally {
 *        LocalContext.exit();
 *     }
 *     
 *     // Temporary change of String format during formatting.
 *     public void format(List list, XmlElement xml) {
 *         LocalContext.enter();
 *         try {  // Always safer to scope the change.
 *             XmlFormat.setFormat(String.class, MY_STRING_FORMAT);
 *             for (Object elem : list)  {
 *                 xml.add(elem);
 *             }
 *        } finally {
 *            LocalContext.exit(); 
 *        }      
 *     }</pre></p>
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
 * <p> Finally, xml formats can be made impervious to obfuscation by 
 *     setting local {@link #setAlias aliases} for the obfuscated classes.</p>
 * 
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 3.5, August 29, 2005
 */
public abstract class XmlFormat/*<T>*/{

    /**
     * Holds the default formats.
     */
    private static final FastMap DEFAULT_INSTANCES = new FastMap().setShared(true);

    /**
     * Holds the dynamic class-to-format override.
     */
    private static final LocalMap DYNAMIC_INSTANCES = new LocalMap();

    /**
     * Holds the local look-up.
     */
    private static final LocalMap CLASS_TO_FORMAT = new LocalMap();

    /**
     * Holds the class-to-alias mapping.
     */
    private static final LocalMap CLASS_TO_ALIAS = new LocalMap();

    /**
     * Holds the class-name to class mapping.
     */
    private static FastMap CLASS_NAME_TO_CLASS = new FastMap().setShared(true);

    /**
     * Holds the local-name/uri to class mapping (localName to uri/class pairs).
     */
    private static FastMap LOCAL_NAME_TO_URI_CLASS = new FastMap().setShared(true);

    /**
     * Holds the alias-to-class mapping.
     */
    private static final LocalMap ALIAS_TO_CLASS = new LocalMap();

    /**
     * Holds the default XML representation when a more specific format 
     * cannot be found. This representation consists of an empty XML element
     * with no attribute. Objects are deserialized using the class public
     * no-arg constructor (the class being identified by the element's name).
     */
    public static final XmlFormat/*<Object>*/DEFAULT_XML = new XmlFormat() {
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
     * (<code>&lt;null/&gt;</code>).
     */
    static final XmlFormat NULL_XML = new XmlFormat(NULL.getClass()) {
        public void format(Object obj, XmlElement xml) {
            // Empty tag.
        }

        public Object parse(XmlElement xml) {
            return null;
        }
    };
    static {
        CLASS_TO_ALIAS.putDefault(NULL.getClass(), "null");
        ALIAS_TO_CLASS.putDefault("null", NULL.getClass());
    }

    /**
     * Holds the default XML representation for <code>java.lang.Class</code>
     * instances. This representation consists of a <code>"name"</code> 
     * attribute holding the class name.
     */
    public static final XmlFormat/*<Class>*/CLASS_XML = new XmlFormat(
            "java.lang.Class") {
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
    public static final XmlFormat/*<String>*/STRING_XML = new XmlFormat(
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
    public static final XmlFormat/*<Text>*/TEXT_XML = new XmlFormat(
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
    public static final XmlFormat/*<Appendable>*/APPENDABLE_XML = new XmlFormat(
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
     * Holds the default XML representation for FastComparator.
     */
    static final XmlFormat FAST_COMPARATOR_XML = new XmlFormat(
            "javolution.util.FastComparator") {

        public Object allocate(XmlElement xml) {
            if (xml.objectClass() == FastComparator.DEFAULT.getClass())
                return FastComparator.DEFAULT;
            if (xml.objectClass() == FastComparator.DIRECT.getClass())
                return FastComparator.DIRECT;
            if (xml.objectClass() == FastComparator.IDENTITY.getClass())
                return FastComparator.IDENTITY;
            if (xml.objectClass() == FastComparator.LEXICAL.getClass())
                return FastComparator.LEXICAL;
            if (xml.objectClass() == FastComparator.REHASH.getClass())
                return FastComparator.REHASH;
            return null;
        }

        public void format(Object obj, XmlElement xml) {
            // Nothing to do.
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
    public static final XmlFormat/*<Collection>*/COLLECTION_XML = new XmlFormat(
            "j2me.util.Collection") {
        public void format(Object obj, XmlElement xml) {
            final Collection collection = (Collection) obj;

            // Special processing for FastCollection.
            if (collection instanceof FastCollection) {
                FastComparator valueComparator = ((FastCollection) collection)
                        .getValueComparator();
                if (valueComparator != FastComparator.DEFAULT) {
                    xml.add(valueComparator, "ValueComparator");
                }
            }

            for (Iterator i = collection.iterator(); i.hasNext();) {
                xml.add(i.next());
            }
        }

        public Object parse(XmlElement xml) {
            final Collection collection = (Collection) xml.object();

            // Special processing for FastCollection.
            if (collection instanceof FastCollection) {
                FastComparator valueComparator = (FastComparator) xml
                        .get("ValueComparator");
                if (valueComparator != null) {
                    ((FastCollection) collection)
                            .setValueComparator(valueComparator);
                }
            }

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
     * The elements' order is defined by the map's entries iterator order. 
     * Maps are deserialized using their default constructor.
     */
    public static final XmlFormat/*<Map>*/MAP_XML = new XmlFormat(
            "j2me.util.Map") {

        public void format(Object obj, XmlElement xml) {
            final Map map = (Map) obj;

            // Special processing for FastMap.
            if (map instanceof FastMap) {
                FastComparator keyComparator = ((FastMap) map)
                        .getKeyComparator();
                if (keyComparator != FastComparator.DEFAULT) {
                    xml.add(keyComparator, "KeyComparator");
                }

                FastComparator valueComparator = ((FastMap) map)
                        .getValueComparator();
                if (valueComparator != FastComparator.DEFAULT) {
                    xml.add(valueComparator, "ValueComparator");
                }
            }

            for (Iterator it = map.entrySet().iterator(); it.hasNext();) {
                Map.Entry entry = (Map.Entry) it.next();
                xml.add(entry.getKey(), "Key");
                xml.add(entry.getValue(), "Value");
            }
        }

        public Object parse(XmlElement xml) {
            final Map map = (Map) xml.object();

            // Special processing for FastMap.
            if (map instanceof FastMap) {
                FastComparator keyComparator = (FastComparator) xml
                        .get("KeyComparator");
                if (keyComparator != null) {
                    ((FastMap) map).setKeyComparator(keyComparator);
                }
                FastComparator valueComparator = (FastComparator) xml
                        .get("ValueComparator");
                if (valueComparator != null) {
                    ((FastMap) map).setValueComparator(valueComparator);
                }
            }

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
    public static final XmlFormat/*<Boolean>*/BOOLEAN_XML = new XmlFormat(
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
    public static final XmlFormat/*<Byte>*/BYTE_XML = new XmlFormat(
            "java.lang.Byte") {
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
    public static final XmlFormat/*<Character>*/CHARACTER_XML = new XmlFormat(
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
    public static final XmlFormat/*<Integer>*/INTEGER_XML = new XmlFormat(
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
    public static final XmlFormat/*<Long>*/LONG_XML = new XmlFormat(
            "java.lang.Long") {
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
    public static final XmlFormat/*<Short>*/SHORT_XML = new XmlFormat(
            "java.lang.Short") {
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
     public static final XmlFormat
     /**/
    /*<Float>*//*@FLOATING_POINT@
     FLOAT_XML = new XmlFormat("java.lang.Float") {
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
     public static final XmlFormat
     /**/
    /*<Double>*//*@FLOATING_POINT@
     DOUBLE_XML = new XmlFormat("java.lang.Double") {
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
     * Holds root class (for deprecated support).
     */
    private Class _rootClass;

    /**
     * Creates a dynamic XML format which can be 
     * {@link javolution.realtime.LocalContext locally} mapped to any class 
     * (see {@link #setFormat}).
     */
    protected XmlFormat() {
    }

    /**
     * Creates a default XML format for instances of the specified 
     * class/interface.
     * 
     * @param rootClass the root class/interface for this XML format.
     * @throws j2me.lang.IllegalStateException if the specified class is 
     *         already mapped to another format.
     */
    protected XmlFormat(Class/*<T>*/rootClass) {
        _rootClass = rootClass;
        if (DEFAULT_INSTANCES.put(rootClass, this) != null) {
            throw new IllegalStateException("XmlFormat already mapped to "
                    + rootClass);
        }
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
        this(classForName(className));
    }

    private static Class classForName(String className) {
        try {
            return Reflection.getClass(className);
        } catch (ClassNotFoundException e) {
            throw new JavolutionError(e);
        }
    }

    /**
     * Specifies the format associated to the specified class 
     * ({@link LocalMap local association}).
     *  
     * @param forClass the class/interface being locally mapped.
     * @param that the format to be associated to the specified class.
     */
    public static void setFormat(Class forClass, XmlFormat that) {
        DYNAMIC_INSTANCES.put(forClass, that);
        CLASS_TO_FORMAT.clear();
        that._rootClass = forClass;
    }

    /**
     * Returns the format for the specified class/interface.
     * This method searches for the more specialized format (default or 
     * dynamic); if none is found {@link #DEFAULT_XML} format is returned.
     * 
     * <p> Note: This method forces the initialization of the specified
     *           class. This to ensure that the class static fields (which 
     *           may hold the most specialized format) are initialized.</p> 
     * 
     * @param forClass the class/interface for which the most specialized 
     *        format is returned.
     * @return the format to use for the specified class.
     */
    public static/*<T>*/XmlFormat/*<T>*/getInstance(Class/*<T>*/forClass) {
        Object xmlFormat = CLASS_TO_FORMAT.get(forClass);
        return (xmlFormat != null) ? (XmlFormat/*<T>*/) xmlFormat
                : (XmlFormat/*<T>*/) searchInstanceFor(forClass, false);
    }

    /**
     * Sets the {@link javolution.realtime.LocalContext local} alias to be used 
     * instead of the class name for the specified class (element tag name or 
     * "j:class" attribute).
     * This method is particularly useful in case of obfuscation to ensure 
     * proper/invariant xml formatting (you don't want to use the obfuscated 
     * class name in such case). 
     * 
     * @param forClass the class for which the specified alias should be used.
     * @param alias the name to use for the specified class.
     * @see   LocalMap
     */
    public static void setAlias(Class forClass, String alias) {
        CLASS_TO_ALIAS.put(forClass, alias);
        ALIAS_TO_CLASS.put(alias, forClass);
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

        try { // Ensures that the class is initialized.
            Reflection.getClass(clazz.getName());
        } catch (ClassNotFoundException e) {
            // Ignores (hopefully the class has already been initialized).
        }

        XmlFormat bestMatchFormat = null;
        Class bestMatchClass = null;

        // Searches dynamic mapping.
        FastCollection dynamicEntries = (FastCollection) DYNAMIC_INSTANCES
                .entrySet();
        for (FastCollection.Record r = dynamicEntries.head(), end = dynamicEntries
                .tail(); (r = r.getNext()) != end;) {
            Map.Entry e = (Map.Entry) r;
            Class cls = (Class) e.getKey();
            if ((cls == clazz) && forceInherited)
                continue;
            if (cls.isAssignableFrom(clazz)) { // Compatible.
                if ((bestMatchClass == null)
                        || (bestMatchClass.isAssignableFrom(cls))) {
                    bestMatchClass = cls;
                    bestMatchFormat = (XmlFormat) e.getValue();
                }
            }
        }

        // Searches default mapping.
        for (FastMap.Entry e = DEFAULT_INSTANCES.head(), end = DEFAULT_INSTANCES
                .tail(); (e = (FastMap.Entry) /*CAST UNNECESSARY WITH JDK1.5*/e
                .getNext()) != end;) {
            Class cl = (Class) e.getKey();
            if ((cl == clazz) && forceInherited)
                continue;
            Class cls = (Class) e.getKey();
            if ((cls == clazz) && forceInherited)
                continue;
            if (cls.isAssignableFrom(clazz)) { // Compatible.
                if ((bestMatchClass == null)
                        || ((bestMatchClass != cls) && bestMatchClass
                                .isAssignableFrom(cls))) {
                    bestMatchClass = cls;
                    bestMatchFormat = (XmlFormat) e.getValue();
                }
            }
        }

        // If none found, use default object format.
        if (bestMatchFormat == null) {
            bestMatchFormat = DEFAULT_XML;
        }

        // Updates local look-up (when not inherited format)
        if (!forceInherited) {
            CLASS_TO_FORMAT.put(clazz, bestMatchFormat);
        }
        return bestMatchFormat;
    }

    /**
     * Returns the name or alias for the specified class.
     *
     * @param cls the class for which the name or alias is returned.
     * @return the class name or alias. 
     */
    static String aliasFor(Class cls) {
        return (String) CLASS_TO_ALIAS.get(cls);
    }

    /**
     * Returns the class for the specified name or alias.
     *
     * @param name the class name or alias.
     * @return the corresponding class. 
     */
    static Class classFor(CharSequence name) {
        Class cls = (Class) CLASS_NAME_TO_CLASS.get(name);
        return (cls != null) ? cls : searchClassFor(name);
    }

    private static Class searchClassFor(CharSequence name) {
        Class cls = (Class) ALIAS_TO_CLASS.get(name);
        if (cls != null)
            return cls;
        try {
            String className = name.toString();
            cls = Reflection.getClass(className);
            CLASS_NAME_TO_CLASS.put(className, cls);
            return cls;
        } catch (ClassNotFoundException e) {
            throw new XmlException(e);
        }
    }

    /**
     * Returns the class for the specified uri/local name.
     * 
     * @param uri the namespace uri.
     * @param localName the local name.
     * @return the corresponding class. 
     */
    static Class classFor(CharSequence uri, CharSequence localName) {
        FastTable uriClassTable = (FastTable) LOCAL_NAME_TO_URI_CLASS
                .get(localName);
        if (uriClassTable != null) {
            for (int i = 0; i < uriClassTable.size(); i += 2) {
                if (uri.equals(uriClassTable.get(i)))
                    return (Class) uriClassTable.get(i + 1);
            }
        }
        return searchClassFor(uri, localName);
    }

    private static Class searchClassFor(CharSequence uri, CharSequence localName) {
        String uriString = uri.toString();
        String localNameString = localName.toString();
        if (!uriString.startsWith("java:"))
            throw new XmlException("Invalid package uri (" + uriString
                    + "), the package uri should start with \"java:\"");
        String className = uriString.substring(5)
                + (uriString.length() > 5 ? "." : "") + localNameString;

        try {
            Class cls = Reflection.getClass(className);
            FastTable uriClassTable = (FastTable) LOCAL_NAME_TO_URI_CLASS
                    .get(localName);
            if (uriClassTable == null) {
                uriClassTable = new FastTable();
                LOCAL_NAME_TO_URI_CLASS.put(localNameString, uriClassTable);
            }
            uriClassTable.add(uriString);
            uriClassTable.add(cls);
            return cls;
        } catch (ClassNotFoundException e) {
            throw new XmlException(e);
        }
    }

    /**
     * Holds the target class for <code>null</code> values. 
     */
    private static class Null {
    };

    /**
     * @deprecated Replaced by {@link #setFormat}.
     */
    public static/*<T>*/void setInstance(XmlFormat/*<T>*/that,
            Class/*<T>*/rootClass) {
        XmlFormat.setFormat(rootClass, that);
    }

    /**
     * @deprecated the XmlFormat instance should be stated explicitly
     *             (xml formats might have multiple parents).
     */
    public final/*<T>*/XmlFormat/*<T>*/getSuper() {
        return searchInstanceFor(_rootClass, true);
    }

}