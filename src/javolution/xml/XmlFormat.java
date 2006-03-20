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
import j2mex.realtime.MemoryArea;

import javolution.lang.Appendable;
import javolution.lang.ClassInitializer;
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
 *     The format is inherited by sub-classes. For example:[code]
 *     public abstract class Graphic {
 *         private boolean _isVisible;
 *         private Paint _paint; // null if none.
 *         private Stroke _stroke; // null if none.
 *         private Transform _transform; // null if none.
 *          
 *         // XML format with positional associations (members identified by their position),
 *         // see xml package description for examples of name associations.
 *         public static final XmlFormat<Graphic> GRAPHIC_XML = new XmlFormat<Graphic>(Graphic.class) {
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
 *    }[/code]
 *    
 * <p> Due to the sequential nature of xml serialization/deserialization, 
 *     formatting/parsing of xml attributes should always be performed before 
 *     formatting/parsing of the xml content.</p>
 * 
 * <p> Xml formats can dynamically associated. For example:[code]
 * 
 *     // XML Conversion (different formats for reading and writing).
 *     XmlFormat<Foo> readFormat = new XmlFormat<Foo>() {...};
 *     XmlFormat<Foo> writeFormat = new XmlFormat<Foo>() {...};
 *     LocalContext.enter();
 *     try {  // Local context to avoid impacting other threads.
 *        XmlFormat.setFormat(Foo.class, readFormat);
 *        Foo foo = new ObjectReader<Foo>().read(in);
 *        XmlFormat.setFormat(Foo.class, writeFormat);
 *        new ObjectWriter<Foo>().write(foo, out);
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
 *     }[/code]</p>
 *     
 * <p> A default format is defined for <code>null</code> values 
 *     (<code>&lt;null/&gt;</code>) and the following types:<ul>
 *        <li><code>java.lang.Object</code> (default)</li>
 *        <li><code>java.lang.Class</code></li>
 *        <li><code>java.lang.String</code></li>
 *        <li><code>javolution.lang.Text</code></li>
 *        <li><code>java.lang.Appendable</code></li>
 *        <li><code>java.util.Collection</code></li>
 *        <li><code>java.util.Map</code></li>
 *        <li>and all primitive types wrappers (e.g. 
 *            <code>Boolean, Integer ...</code>)</li>
 *        </ul></p>
 *        
 * <p>Here is an example of serialization/deserialization using predefined 
 *    formats:[code]
 *     List list = new ArrayList();
 *     list.add("John Doe");
 *     list.add(null);
 *     Map map = new FastMap();
 *     map.put("ONE", new Integer(1));
 *     map.put("TWO", new Integer(2));
 *     list.add(map);
 *     ObjectWriter ow = new ObjectWriter();
 *     ow.write(list, new FileOutputStream("C:/list.xml"));[/code]
 *     Here is the output <code>list.xml</code> document produced:[code]
 *     <java.util.ArrayList xmlns:j="http://javolution.org">
 *         <java.lang.String value="John Doe"/>
 *         <null/>
 *         <javolution.util.FastMap>
 *             <Key j:class="java.lang.String" value="ONE"/>
 *             <Value j:class="java.lang.Integer" value="1"/>
 *             <Key j:class="java.lang.String" value="TWO"/>
 *             <Value j:class="java.lang.Integer" value="2"/>
 *         </javolution.util.FastMap>[/code]
 *     The list can be read back with the following code:[code]
 *     ObjectReader or = new ObjectReader();
 *     List list = (List) or.read(new FileInputStream("C:/list.xml"));
 *     [/code]</p>
 *     
 * <p> Finally, xml formats can be made impervious to obfuscation by 
 *     setting local {@link #setAlias aliases} for the obfuscated classes.</p>
 * 
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 3.6, October 13, 2005
 */
public abstract class XmlFormat /*<T>*/{

    /**
     * Holds the default formats.
     */
    private static final FastMap DEFAULT_INSTANCES = new FastMap()
            .setShared(true);

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
    public static final XmlFormat /*<Object>*/DEFAULT_XML = new XmlFormat() {
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

        public String identifier(boolean isReference) {
            return null; // Disables references.
        }

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
    public static final XmlFormat /*<Class>*/CLASS_XML = new XmlFormat(
            "java.lang.Class") {
        public void format(Object obj, XmlElement xml) {
            xml.setAttribute("name", ((Class) obj).getName());
        }

        public Object parse(XmlElement xml) {
            Class cls = Reflection.getClass(xml.getAttribute("name", ""));
            if (cls == null)
                throw new XmlException("Class: " + cls + " not found");
            return cls;
        }
    };

    /**
     * Holds the default XML representation for <code>java.lang.String</code>
     * classes. This representation consists of a <code>"value"</code> attribute 
     * holding the string.
     */
    public static final XmlFormat /*<String>*/STRING_XML = new XmlFormat(
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
    public static final XmlFormat /*<Text>*/TEXT_XML = new XmlFormat(
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
     * Holds the default XML representation for <code>java.lang.Appendable</code>
     * classes. This representation consists of a <code>"value"</code> attribute 
     * holding the characters.
     */
    public static final XmlFormat /*<Appendable>*/APPENDABLE_XML = new XmlFormat(
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
     * the <code>java.util.Collection</code> interface.
     * This representation consists of nested XML elements one for each 
     * element of the collection. The elements' order is defined by
     * the collection iterator order. Collections are deserialized using their
     * default constructor. 
     */
    public static final XmlFormat /*<Collection>*/COLLECTION_XML = new XmlFormat(
            "j2me.util.Collection") {
        // Preallocates in case of circular references.
        public Object allocate(XmlElement xml) {
            return xml.object();
        }

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
     * the <code>java.util.Map</code> interface.
     * This representation consists of key/value pair as nested XML elements.
     * For example:[code]
     * <javolution.util.FastMap>
     *     <Key j:class="java.lang.String" value="ONE"/>
     *     <Value j:class="java.lang.Integer" value="1"/>
     *     <Key j:class="java.lang.String" value="TWO"/>
     *     <Value j:class="java.lang.Integer" value="2"/>
     *     <Key j:class="java.lang.String" value="THREE"/>
     *     <Value j:class="java.lang.Integer" value="3"/>
     * </javolution.util.FastMap>[/code]
     * 
     * The elements' order is defined by the map's entries iterator order. 
     * Maps are deserialized using their default constructor.
     */
    public static final XmlFormat /*<Map>*/MAP_XML = new XmlFormat(
            "j2me.util.Map") {

        // Preallocates in case of circular references.
        public Object allocate(XmlElement xml) {
            return xml.object();
        }

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
    public static final XmlFormat /*<Boolean>*/BOOLEAN_XML = new XmlFormat(
            "java.lang.Boolean") {
        public String identifier(boolean isReference) {
            return null; // Always by value.
        }

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
    public static final XmlFormat /*<Byte>*/BYTE_XML = new XmlFormat(
            "java.lang.Byte") {
        public String identifier(boolean isReference) {
            return null; // Always by value.
        }

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
    public static final XmlFormat /*<Character>*/CHARACTER_XML = new XmlFormat(
            "java.lang.Character") {
        public String identifier(boolean isReference) {
            return null; // Always by value.
        }

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
    public static final XmlFormat /*<Integer>*/INTEGER_XML = new XmlFormat(
            "java.lang.Integer") {
        public String identifier(boolean isReference) {
            return null; // Always by value.
        }

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
    public static final XmlFormat /*<Long>*/LONG_XML = new XmlFormat(
            "java.lang.Long") {
        public String identifier(boolean isReference) {
            return null; // Always by value.
        }

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
    public static final XmlFormat /*<Short>*/SHORT_XML = new XmlFormat(
            "java.lang.Short") {
        public String identifier(boolean isReference) {
            return null; // Always by value.
        }

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
     public String identifier(boolean isReference) {
     return null; // Always by value.
     }
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
     public String identifier(boolean isReference) {
     return null; // Always by value.
     }
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
     * @throws java.lang.IllegalStateException if the specified class is 
     *         already mapped to another format.
     */
    protected XmlFormat(Class /*<T>*/rootClass) {
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
     * @throws java.lang.IllegalStateException if the specified class is 
     *         already mapped to another format.
     */
    protected XmlFormat(String className) {
        this(classForName(className));
    }

    private static Class classForName(String className) {
        Class cls = Reflection.getClass(className);
        if (cls == null)
            throw new XmlException("Class: " + cls + " not found");
        return cls;
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
    public static/*<T>*/XmlFormat /*<T>*/getInstance(Class /*<T>*/forClass) {
        Object xmlFormat = CLASS_TO_FORMAT.get(forClass);
        return (xmlFormat != null) ? (XmlFormat /*<T>*/) xmlFormat
                : (XmlFormat /*<T>*/) searchInstanceFor(forClass, false);
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
     * Returns the name to be used when objects associated to this 
     * format are added with no name specified (default <code>null</code>
     * the element name is the object class name).
     *
     * @return the default element name for objects using this format.
     */
    public String defaultName() {
        return null;
    }

    /**
     * Returns the names of the identifiers attributes when cross-reference 
     * is enabled. The default implementation returns 
     *  <code>isReference ? "j:ref" : "j:id"</code>. 
     * Format sub-classes may override this method to use different 
     * attribute names. This method may also return <code>null</code> for 
     * objects exclusively manipulated by value (e.g. immutable objects).
     *
     * @param isReference indicates if the attribute name returned is for
     *        a reference or an identifier.
     * @return the name of the attribute identifier or <code>null</code>
     *         if references should not be used.
     * @see ObjectWriter#setReferencesEnabled(boolean)
     */
    public String identifier(boolean isReference) {
        return isReference ? "j:ref" : "j:id";
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

        ClassInitializer.initialize(clazz); // Forces initialization.
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
     * @param name the class name or alias (CharSequenceImpl).
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
        String className = name.toString();
        cls = Reflection.getClass(className);
        if (cls == null)
            throw new XmlException("Class: " + cls + " not found");
        // Class objects and interned Strings are in ImmortalMemory.
        CLASS_NAME_TO_CLASS.put(intern(className), cls); 
        return cls;
    }

    /**
     * Returns the class for the specified uri/local name.
     * 
     * @param uri the namespace uri (CharSequenceImpl).
     * @param localName the local name (CharSequenceImpl).
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
        final String localNameString = localName.toString();
        if (!uriString.startsWith("java:"))
            throw new XmlException("Invalid package uri (" + uriString
                    + "), the package uri should start with \"java:\"");
        String className = uriString.substring(5)
                + (uriString.length() > 5 ? "." : "") + localNameString;
        Class cls = Reflection.getClass(className);
        if (cls == null)
            throw new XmlException("Class: " + cls + " not found");
        FastTable uriClassTable = (FastTable) LOCAL_NAME_TO_URI_CLASS
                .get(localName);
        if (uriClassTable == null) {
            MemoryArea.getMemoryArea(LOCAL_NAME_TO_URI_CLASS).executeInArea(
                    new Runnable() {
                        public void run() {
                            LOCAL_NAME_TO_URI_CLASS.put(intern(localNameString), new FastTable());
                        }
                    });
            uriClassTable = (FastTable) LOCAL_NAME_TO_URI_CLASS.get(localName);
        }
        // Class objects and interned Strings are in ImmortalMemory.
        uriClassTable.add(intern(uriString));
        uriClassTable.add(cls);
        return cls;
    }

    private static String intern(String str) { // J2ME CLDC.
        return STRING_INTERN != null ? (String) STRING_INTERN.invoke(str) : str;
    }
    private static Reflection.Method STRING_INTERN 
        = Reflection.getMethod("java.lang.String.intern()");
        
    /**
     * Holds the target class for <code>null</code> values. 
     */
    private static class Null {
    };
}