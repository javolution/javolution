/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2006 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.xml;

import java.io.IOException;
import javolution.Javolution;
import javolution.JavolutionError;
import javolution.context.PersistentContext;
import javolution.lang.ClassInitializer;
import javolution.lang.Reflection;
import javolution.lang.Reusable;
import javolution.text.Appendable;
import javolution.text.CharArray;
import javolution.text.Text;
import javolution.util.FastCollection;
import javolution.util.FastComparator;
import javolution.util.FastMap;
import javolution.util.Index;
import javolution.util.FastCollection.Record;
import javolution.util.FastMap.Entry;
import javolution.xml.stream.XMLStreamException;
import javolution.xml.stream.XMLStreamReaderImpl;
import javolution.xml.stream.XMLStreamWriterImpl;
import j2me.lang.CharSequence;
import j2me.util.Collection;
import j2me.util.Iterator;
import j2me.util.Map;

/**
 * <p> This class represents the binding between Java classes and 
 *     their XML representation ({@link XMLFormat}); the binding may be shared
 *     among multiple {@link XMLObjectReader}/ {@link XMLObjectWriter} 
 *     instances (thread-safe).</p>
 *     
 * <p> Custom XML bindings can also be used to alias class names and 
 *     ensure that the XML representation is:<ul>
 *     <li> Impervious to obfuscation.</li>
 *     <li> Unnaffected by any class refactoring.</li>
 *     <li> Can be mapped to multiple implementations. For example:[code]
 *     
 *     // Creates a binding to serialize Swing components into high-level XML
 *     // and deserialize the same XML into SWT components.
 *     XMLBinding swingBinding = new XMLBinding();
 *     swingBinding.setAlias(javax.swing.JButton.class, "Button");
 *     swingBinding.setAlias(javax.swing.JTable.class, "Table");
 *     ...
 *     XMLBinding swtBinding = new XMLBinding();
 *     swtBinding.setAlias(org.eclipse.swt.widgets.Button.class, "Button");
 *     swtBinding.setAlias(org.eclipse.swt.widgets.Table.class, "Table");
 *     ...
 *     
 *     // Writes Swing Desktop to XML.
 *     XMLObjectWriter writer = new XMLObjectWriter().setBinding(swingBinding);
 *     writer.setOutput(new FileOutputStream("C:/desktop.xml"));
 *     writer.write(swingDesktop, "Desktop", SwingDesktop.class);
 *     writer.close();
 *
 *     // Reads back high-level XML to a SWT implementation!    
 *     XMLObjectReader reader = new XMLObjectReader().setXMLBinding(swtBinding);
 *     reader.setInput(new FileInputStream("C:/desktop.xml"));
 *     SWTDesktop swtDesktop = reader.read("Desktop", SWTDesktop.class);
 *     reader.close();
 *     [/code]</li>
 *     </ul></p>        
 *     
 * <p> More advanced bindings can also be created through sub-classing.[code]
 * 
 *     // XML binding using reflection.
 *     public ReflectionBinding extends XMLBinding {
 *         public <T> XMLFormat<T> getFormat(Class<T> cls) {
 *             Field[] fields = clt.getDeclaredFields();
 *             return new XMLReflectionFormat<T>(fields);
 *         }
 *     }
 *     
 *     // XML binding read from DTD input source.
 *     public DTDBinding extends XMLBinding {
 *         public DTDBinding(InputStream dtd) {
 *             ...
 *         }
 *     }
 *     
 *     // XML binding overriding statically bounded formats.
 *     public MyBinding extends XMLBinding {
 *         // Non-static formats use unmapped XMLFormat instances.
 *         XMLFormat<String> _myStringFormat = new XMLFormat<String>(null) {...}
 *         XMLFormat<Collection> _myCollectionFormat = new XMLFormat<Collection>(null) {...}
 *         public <T> XMLFormat<T> getFormat(Class<T> cls) {
 *             if (String.class.equals(cls))
 *                  return _myStringFormat;
 *             if (Collection.class.isAssignableFrom(cls))
 *                  return _myCollectionFormat;
 *             return super.getFormat(cls);
 *         }
 *     }
 *     [/code]
 *      
 * <p> The default XML binding supports all static XML formats 
 *     (static members of the classes being mapped) as well as the 
 *     following types:<ul>
 *        <li><code>java.lang.Object</code> (empty element)</li>
 *        <li><code>java.lang.Class</code></li>
 *        <li><code>java.lang.String</code></li>
 *        <li><code>java.lang.Appendable</code></li>
 *        <li><code>java.util.Collection</code></li>
 *        <li><code>java.util.Map</code></li>
 *        <li><code>java.lang.Object[]</code></li>
 *        <li> all primitive types wrappers (e.g. 
 *            <code>Boolean, Integer ...</code>)</li>
 *        </ul></p>
 *     
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 4.0, April 9, 2007
 */
public class XMLBinding implements Reusable, XMLSerializable {

    /**
     * Holds the static mapping format.
     */
    private static FastMap STATIC_MAPPING = new FastMap().setShared(true);

    /**
     * Holds the default instance used by readers/writers.
     */
    static final XMLBinding DEFAULT = new XMLBinding();

    /**
     * Holds the XML representation of this binding (class/alias mapping 
     * and class attribute values).
     */
    static final XMLFormat XML = new XMLFormat(Javolution
            .j2meGetClass("javolution.xml.XMLBinding")) {

        public void read(InputElement xml, Object obj)
                throws XMLStreamException {
            XMLBinding binding = (XMLBinding) obj;
            binding._classAttributeName = xml.getAttribute(
                    "classAttributeName", (String) null);
            binding._classAttributeURI = xml.getAttribute("classAttributeURI",
                    (String) null);
            FastMap fm = binding._aliasToClass = (FastMap) xml.get(
                    "AliasToClass", FastMap.class);
            // Builds reverse mapping automatically.
            for (Entry e = fm.head(), t = fm.tail(); (e = (Entry) e.getNext()) != t;) {
                binding._classToAlias.put(e.getValue(), e.getKey());
            }
        }

        public void write(Object obj, OutputElement xml)
                throws XMLStreamException {
            XMLBinding binding = (XMLBinding) obj;
            xml.setAttribute("classAttributeName", binding._classAttributeName);
            xml.setAttribute("classAttributeURI", binding._classAttributeURI);
            xml.add(binding._aliasToClass, "AliasToClass", FastMap.class);
        }
    };

    /**
     * Holds the local name of the class attribute.
     */
    private String _classAttributeName = "class";

    /**
     * Holds the URI of the class attribute if any.
     */
    private String _classAttributeURI = null;

    /**
     * Holds the class to name mapping.
     */
    private FastMap _classToAlias = new FastMap();

    /**
     * Holds the name to class mapping.
     */
    private FastMap _aliasToClass = new FastMap();

    /**
     * Default constructor.
     */
    public XMLBinding() {
    }

    /**
     * Sets the alias of the specified class. Classes may have multiple 
     * aliases but any given alias maps to a single class.
     * 
     * @param cls the class being aliased.
     * @param alias the alias for the specified class.
     */
    public void setAlias(Class cls, String alias) {
        Class prevForAlias = (Class) _aliasToClass.put(alias, cls);
        // Removes any previous class to given alias mapping.
        if (prevForAlias != null) {
            _classToAlias.put(prevForAlias, null);
        }
        _classToAlias.put(cls, alias);
    }

    /**
     * Sets the name of the attribute holding the classname/alias
     * (by default<code>"class"</code>).
     * If the local name is <code>null</code> then the class attribute
     * is never read/written (which may prevent unmarshalling).
     * 
     * @param name the local name of the attribute or <code>null</code>.
     */
    public void setClassAttribute(String name) {
        _classAttributeName = name;
        _classAttributeURI = null;
    }

    /**
     * Sets the local name and namespace URI of the attribute holding the 
     * classname/alias (by default<code>"class"</code> and no namespace URI).
     * If the local name is <code>null</code> then the class attribute
     * is never read/written (which may prevent unmarshalling).
     * 
     * @param localName the local name of the attribute or <code>null</code>.
     * @param uri the URI of the attribute or <code>null</code> if the 
     *        class attribute has no namespace URI.
     */
    public void setClassAttribute(String localName, String uri) {
        _classAttributeName = localName;
        _classAttributeURI = uri;
    }

    /**
     * Returns the XML format for the specified class/interface.
     * The default implementation returns the most 
     * specialized static format compatible with the specified class.
     * 
     * @param cls the class for which the XML format is returned.
     * @return the XML format for the specified class. 
     */
    public/*<T>*/XMLFormat/*<T>*/getFormat(Class/*<T>*/cls) {
        Object xmlFormat = STATIC_MAPPING.get(cls);
        return (xmlFormat != null) ? (XMLFormat) xmlFormat : searchFormat(cls);
    }

    private XMLFormat searchFormat(Class cls) {
        // First initialize class to ensure static format creation.
        ClassInitializer.initialize(cls);

        // Then search best match.
        XMLFormat bestMatchFormat = null;
        for (int i = 0, j = XMLFormat._ClassInstancesLength; i < j; i++) {
            XMLFormat xmlFormat = XMLFormat._ClassInstances[i];
            if (xmlFormat._class.isAssignableFrom(cls)) { // Compatible.
                if ((bestMatchFormat == null)
                        || (bestMatchFormat._class
                                .isAssignableFrom(xmlFormat._class))) {
                    bestMatchFormat = xmlFormat;
                }
            }
        }
        if (bestMatchFormat == null)
            throw new JavolutionError("Cannot find format for " + cls);

        STATIC_MAPPING.put(cls, bestMatchFormat);
        return bestMatchFormat;
    }

    /**
     * Returns the name identifying the specified class (value of 
     * {@link #setClassAttribute class attribute} during marshalling).  
     * The default implementation returns the class alias (if any) or 
     * <code>cls.getName()</code>. 
     * 
     * @param cls the class for which a name identifier is returned.
     * @return the alias or name for the class.
     */
    protected String getName(Class cls) {
        String alias = (String) _classToAlias.get(cls);
        return (alias != null) ? alias : cls.getName();
    }

    /**
     * Returns the class identified by the specified name (value of 
     * {@link #setClassAttribute class attribute} during unmarshalling).
     * The default implementation returns an aliased class or 
     * <code>Class.forName(name.toString())</code>.
     * 
     * @param name the class name identifier.
     * @return the class for the specified name.
     * @throws ClassNotFoundException 
     */
    protected Class getClass(CharArray name) throws ClassNotFoundException {
        Class cls = (Class) _aliasToClass.get(name);
        return (cls != null) ? cls : Reflection.getClass(name);
    }

    /**
     * Returns the local name identifying the specified class (the element local 
     * name during marshalling). The default implementation returns 
     * <code>this.getName(cls)</code>.
     * 
     * @param cls the class for which the local name is returned.
     * @return the local name of the specified class.
     */
    protected String getLocalName(Class cls) {
        return this.getName(cls);
    }

    /**
     * Returns the URI identifying the specified class (the element namespace
     * URI during marshalling). The default implementation returns 
     * <code>null</code> (no namespace URI).
     * 
     * @param cls the class for which the namespace URI is returned.
     * @return the URI for the specified class or <code>null</code> if none.
     */
    protected String getURI(Class cls) {
        return null;
    }

    /**
     * Returns the class identified by the specified local name and URI
     * (the element local name and URI during unmarshalling). The default 
     * implementation returns <code>getClass(localName)</code>.
     * 
     * @param localName the class local name identifier.
     * @param uri the class URI identifier (can be <code>null</code>).
     * @return the corresponding class.
     * @throws ClassNotFoundException 
     */
    protected Class getClass(CharArray localName, CharArray uri)
            throws ClassNotFoundException {
        return getClass(localName);
    }

    // Implements Reusable.
    public void reset() {
        _aliasToClass.reset();
        _classToAlias.reset();
    }

    /**
     * Reads the class from the class attribute of current XML element.
     * 
     * @param reader the reader to be used.
     * @return the corresponding class.
     * @throws XMLStreamException 
     */
    final Class readClassAttribute(XMLStreamReaderImpl reader)
            throws XMLStreamException {
        if (_classAttributeName == null)
            throw new XMLStreamException(
                    "Binding has no class attribute defined, cannot retrieve class");
        CharArray className = reader.getAttributeValue(
                toCsq(_classAttributeURI), toCsq(_classAttributeName));
        if (className == null)
            throw new XMLStreamException(
                    "Cannot retrieve class (class attribute not found)");
        try {
            return getClass(className);
        } catch (ClassNotFoundException e) {
            throw new XMLStreamException(e);
        }
    }

    /**
     * Writes the class to the class attribute of the current XML element.
     * 
     * @param writer the writer to be used.
     * @param cls the class being written.
     * @throws XMLStreamException 
     */
    final void writeClassAttribute(XMLStreamWriterImpl writer, Class cls)
            throws XMLStreamException {

        if (_classAttributeName != null) {
            String value = getName(cls);
            if (_classAttributeURI == null) {
                writer.writeAttribute(toCsq(_classAttributeName), toCsq(value));
            } else {
                writer.writeAttribute(toCsq(_classAttributeURI),
                        toCsq(_classAttributeName), toCsq(value));
            }
        }
    }

    /**
     * Holds the static XML format for <code>Object.class</code> instances
     * (default format when a more specialized format does not exist).
     * The XML representation consists of an empty element with no attribute.
     */
    static final XMLFormat/*<Object>*/OBJECT_XML = new XMLFormat(new Object()
            .getClass()) {

        public void read(javolution.xml.XMLFormat.InputElement xml, Object obj) {
            // Do nothing.
        }

        public void write(Object obj, javolution.xml.XMLFormat.OutputElement xml) {
            // Do nothing
        }
    };

    /**
     * Holds the default XML representation for <code>java.lang.Class</code>
     * instances. This representation consists of a <code>"name"</code> 
     * attribute holding the class name.
     */
    static final XMLFormat/*<Class>*/CLASS_XML = new XMLFormat("".getClass()
            .getClass()) {

        public boolean isReferenceable() {
            return false; // Always by value (immutable). 
        }

        public Object newInstance(Class cls,
                javolution.xml.XMLFormat.InputElement xml)
                throws XMLStreamException {
            CharArray name = xml.getAttribute("name");
            if (name == null)
                throw new XMLStreamException("Attribute 'name' missing");
            Class clazz;
            try {
                clazz = Reflection.getClass(name);
            } catch (ClassNotFoundException e) {
                throw new XMLStreamException(e);
            }
            return clazz;
        }

        public void read(javolution.xml.XMLFormat.InputElement xml, Object obj)
                throws XMLStreamException {
            // Do nothing.            
        }

        public void write(Object obj, javolution.xml.XMLFormat.OutputElement xml)
                throws XMLStreamException {
            xml.setAttribute("name", ((Class) obj).getName());
        }
    };

    /**
     * Holds the default XML representation for <code>java.lang.String</code>
     * instances. This representation consists of a <code>"value"</code> 
     * attribute holding the string.
     */
    static final XMLFormat/*<String>*/STRING_XML = new XMLFormat("".getClass()) {

        public Object newInstance(Class cls,
                javolution.xml.XMLFormat.InputElement xml)
                throws XMLStreamException {
            return xml.getAttribute("value", "");
        }

        public void read(InputElement xml, Object obj)
                throws XMLStreamException {
            // Do nothing.            
        }

        public void write(Object obj, OutputElement xml)
                throws XMLStreamException {
            xml.setAttribute("value", (String) obj);
        }
    };

    /**
     * Holds the default XML representation for <code>java.lang.Appendable</code>
     * instances. This representation consists of a <code>"value"</code> attribute 
     * holding the characters.
     */
    static final XMLFormat/*<Appendable>*/APPENDABLE_XML = new XMLFormat(
            Javolution.j2meGetClass("javolution.text.Appendable")) {

        public void read(InputElement xml, Object obj)
                throws XMLStreamException {
            CharSequence csq = xml.getAttribute("value");
            if (csq != null) {
                try {
                    ((Appendable) obj).append(csq);
                } catch (IOException e) {
                    throw new XMLStreamException(e);
                }
            }
        }

        public void write(Object obj, OutputElement xml)
                throws XMLStreamException {
            if (obj instanceof CharSequence) {
                xml.setAttribute("value", (CharSequence) obj);
            } else {
                xml.setAttribute("value", obj.toString());
            }
        }
    };

    /**
     * Holds the default XML representation for <code>java.util.Collection</code>
     * instances. This representation consists of nested XML elements one for
     * each element of the collection. The elements' order is defined by
     * the collection iterator order. Collections are deserialized using their
     * default constructor. 
     */
    static final XMLFormat/*<Collection>*/COLLECTION_XML = new XMLFormat(
            Javolution.j2meGetClass("j2me.util.Collection")) {

        public void read(InputElement xml, Object obj)
                throws XMLStreamException {
            Collection collection = (Collection) obj;
            while (xml.hasNext()) {
                collection.add(xml.getNext());
            }
        }

        public void write(Object obj, OutputElement xml)
                throws XMLStreamException {
            Collection collection = (Collection) obj;
            for (Iterator i = collection.iterator(); i.hasNext();) {
                xml.add(i.next());
            }
        }
    };

    /**
     * Holds the default XML representation for <code>java.util.Map</code>
     * instances. This representation consists of key/value pair as nested 
     * XML elements. For example:[code]
     * <javolution.util.FastMap>
     *     <Key class="java.lang.String" value="ONE"/>
     *     <Value class="java.lang.Integer" value="1"/>
     *     <Key class="java.lang.String" value="TWO"/>
     *     <Value class="java.lang.Integer" value="2"/>
     *     <Key class="java.lang.String" value="THREE"/>
     *     <Value class="java.lang.Integer" value="3"/>
     * </javolution.util.FastMap>[/code]
     * 
     * The elements' order is defined by the map's entries iterator order. 
     * Maps are deserialized using their default constructor.
     */
    static final XMLFormat/*<Map>*/MAP_XML = new XMLFormat(Javolution
            .j2meGetClass("j2me.util.Map")) {

        public void read(InputElement xml, Object obj)
                throws XMLStreamException {
            final Map map = (Map) obj;
            while (xml.hasNext()) {
                Object key = xml.get("Key");
                Object value = xml.get("Value");
                map.put(key, value);
            }
        }

        public void write(Object obj, OutputElement xml)
                throws XMLStreamException {
            final Map map = (Map) obj;
            for (Iterator it = map.entrySet().iterator(); it.hasNext();) {
                Map.Entry entry = (Map.Entry) it.next();
                xml.add(entry.getKey(), "Key");
                xml.add(entry.getValue(), "Value");
            }
        }
    };

    /**
     * Holds the default XML representation for <code>java.lang.Object[]</code>
     * instances. This representation consists of nested XML elements one for
     * each element of the array.
     /*@JVM-1.4+@
     static final XMLFormat
     /**/
    /*<Object[]>*//*@JVM-1.4+@
     OBJECT_ARRAY_XML = new XMLFormat(
     new Object[0].getClass()) {
     
     public Object newInstance(Class cls, javolution.xml.XMLFormat.InputElement xml) throws XMLStreamException {
     Class componentType;
     try {
     componentType = Reflection.getClass(xml.getAttribute("componentType"));
     } catch (ClassNotFoundException e) {
     throw new XMLStreamException(e);
     }
     int length = xml.getAttribute("length", 0);
     return java.lang.reflect.Array.newInstance(componentType, length);
     }

     public void read(InputElement xml, Object obj) throws XMLStreamException {
     Object[] array = (Object[]) obj;
     for (int i=0; i < array.length; i++) {
     array[i] = xml.getNext();
     }
     }

     public void write(Object obj, OutputElement xml) throws XMLStreamException {
     Object[] array = (Object[]) obj;
     xml.setAttribute("componentType", array.getClass().getComponentType().getName());
     xml.setAttribute("length", array.length);
     for (int i=0; i < array.length; i++) {
     xml.add(array[i]);
     }
     }
     };
     /**/

    /**
     * Holds the default XML representation for <code>java.lang.Boolean</code>.
     */
    static final XMLFormat/*<Boolean>*/BOOLEAN_XML = new XMLFormat(
            new Boolean(true).getClass()) {

        public boolean isReferenceable() {
            return false; // Always by value (immutable). 
        }

        public Object newInstance(Class cls,
                javolution.xml.XMLFormat.InputElement xml)
                throws XMLStreamException {
            return new Boolean(xml.getAttribute("value", false));
        }

        public void read(InputElement xml, Object obj)
                throws XMLStreamException {
            // Do nothing.
        }

        public void write(Object obj, OutputElement xml)
                throws XMLStreamException {
            xml.setAttribute("value", ((Boolean) obj).booleanValue());
        }
    };

    /**
     * Holds the default XML representation for <code>java.lang.Byte</code>.
     */
    static final XMLFormat/*<Byte>*/BYTE_XML = new XMLFormat(
            new Byte((byte) 0).getClass()) {

        public boolean isReferenceable() {
            return false; // Always by value (immutable). 
        }

        public Object newInstance(Class cls,
                javolution.xml.XMLFormat.InputElement xml)
                throws XMLStreamException {
            return new Byte((byte) xml.getAttribute("value", 0));
        }

        public void read(InputElement xml, Object obj)
                throws XMLStreamException {
            // Do nothing.
        }

        public void write(Object obj, OutputElement xml)
                throws XMLStreamException {
            xml.setAttribute("value", ((Byte) obj).byteValue());
        }
    };

    /**
     * Holds the default XML representation for <code>java.lang.Character</code>.
     */
    static final XMLFormat/*<Character>*/CHARACTER_XML = new XMLFormat(
            new Character(' ').getClass()) {

        public boolean isReferenceable() {
            return false; // Always by value (immutable). 
        }

        public Object newInstance(Class cls,
                javolution.xml.XMLFormat.InputElement xml)
                throws XMLStreamException {
            CharSequence csq = xml.getAttribute("value");
            if ((csq == null) || (csq.length() != 1))
                throw new XMLStreamException(
                        "Missing or invalid value attribute");
            return new Character(csq.charAt(0));
        }

        public void read(InputElement xml, Object obj)
                throws XMLStreamException {
            // Do nothing.
        }

        public void write(Object obj, OutputElement xml)
                throws XMLStreamException {
            xml.setAttribute("value", Text.valueOf(((Character) obj)
                    .charValue()));
        }
    };

    /**
     * Holds the default XML representation for <code>java.lang.Short</code>.
     */
    static final XMLFormat/*<Short>*/SHORT_XML = new XMLFormat(new Short(
            (short) 0).getClass()) {

        public boolean isReferenceable() {
            return false; // Always by value (immutable). 
        }

        public Object newInstance(Class cls,
                javolution.xml.XMLFormat.InputElement xml)
                throws XMLStreamException {
            return new Short((short) xml.getAttribute("value", 0));
        }

        public void read(InputElement xml, Object obj)
                throws XMLStreamException {
            // Do nothing.
        }

        public void write(Object obj, OutputElement xml)
                throws XMLStreamException {
            xml.setAttribute("value", ((Short) obj).shortValue());
        }
    };

    /**
     * Holds the default XML representation for <code>java.lang.Integer</code>.
     */
    static final XMLFormat/*<Integer>*/INTEGER_XML = new XMLFormat(
            new Integer(0).getClass()) {

        public boolean isReferenceable() {
            return false; // Always by value (immutable). 
        }

        public Object newInstance(Class cls,
                javolution.xml.XMLFormat.InputElement xml)
                throws XMLStreamException {
            return new Integer(xml.getAttribute("value", 0));
        }

        public void read(InputElement xml, Object obj)
                throws XMLStreamException {
            // Do nothing.
        }

        public void write(Object obj, OutputElement xml)
                throws XMLStreamException {
            xml.setAttribute("value", ((Integer) obj).intValue());
        }
    };

    /**
     * Holds the default XML representation for <code>java.lang.Long</code>.
     */
    static final XMLFormat/*<Long>*/LONG_XML = new XMLFormat(new Long(0)
            .getClass()) {

        public boolean isReferenceable() {
            return false; // Always by value (immutable). 
        }

        public Object newInstance(Class cls,
                javolution.xml.XMLFormat.InputElement xml)
                throws XMLStreamException {
            return new Long(xml.getAttribute("value", 0L));
        }

        public void read(InputElement xml, Object obj)
                throws XMLStreamException {
            // Do nothing.
        }

        public void write(Object obj, OutputElement xml)
                throws XMLStreamException {
            xml.setAttribute("value", ((Long) obj).longValue());
        }
    };

    /**
     * Holds the default XML representation for <code>java.lang.Float</code>.
     /*@JVM-1.1+@
     static final XMLFormat
     /**/
    /*<Float>*//*@JVM-1.1+@
     FLOAT_XML = new XMLFormat(new Float(0f).getClass()) {
     
     public boolean isReferenceable() {
     return false; // Always by value (immutable). 
     }
     
     public Object newInstance(Class cls, javolution.xml.XMLFormat.InputElement xml) throws XMLStreamException {
     return new Float(xml.getAttribute("value", 0f));
     }

     public void read(InputElement xml, Object obj) throws XMLStreamException {
     // Do nothing.
     }

     public void write(Object obj, OutputElement xml) throws XMLStreamException {
     xml.setAttribute("value", ((Float) obj).floatValue());
     }
     };
     /**/

    /**
     * Holds the default XML representation for <code>java.lang.Double</code>.
     /*@JVM-1.1+@
     static final XMLFormat
     /**/
    /*<Double>*//*@JVM-1.1+@
     DOUBLE_XML = new XMLFormat(new Double(0.0).getClass()) {
     public boolean isReferenceable() {
     return false; // Always by value (immutable). 
     }
     
     public Object newInstance(Class cls, javolution.xml.XMLFormat.InputElement xml) throws XMLStreamException {
     return new Double(xml.getAttribute("value", 0.0));
     }

     public void read(InputElement xml, Object obj) throws XMLStreamException {
     // Do nothing.
     }

     public void write(Object obj, OutputElement xml) throws XMLStreamException {
     xml.setAttribute("value", ((Double) obj).doubleValue());
     }
     };
     /**/

    ////////////////////////////////////////////////////////////////////////////
    // JAVOLUTION XML FORMAT (HERE TO AVOID LOADING XML FRAMEWORK IF NOT USED)//
    ////////////////////////////////////////////////////////////////////////////
    
    /**
     * Holds the default XML representation for Text instances. 
     * This representation consists of a <code>"value"</code> attribute 
     * holding the characters.
     */
    static final XMLFormat/*<Text>*/TEXT_XML = new XMLFormat(Javolution
            .j2meGetClass("javolution.text.Text")) {

        public Object newInstance(Class cls,
                javolution.xml.XMLFormat.InputElement xml)
                throws XMLStreamException {
            CharSequence csq = xml.getAttribute("value");
            return csq != null ? Text.valueOf(csq) : Text.EMPTY;
        }

        public void read(InputElement xml, Object obj)
                throws XMLStreamException {
            // Do nothing.
        }

        public void write(Object obj, OutputElement xml)
                throws XMLStreamException {
            xml.setAttribute("value", (Text) obj);
        }
    };

    /**
     * Holds the default XML representation for FastMap instances.
     * This representation is identical to {@link XMLBinding#MAP_XML}
     * except that it may include the key/value comparators for the map
     * (if different from {@link FastComparator#DEFAULT}) and the 
     * {@link #isShared() "shared"} attribute.
     */
    static final XMLFormat/*<FastMap>*/ FASTMAP_XML = new XMLFormat(
            new FastMap().getClass()) {

        public void read(InputElement xml, Object obj)
                throws XMLStreamException {
            final FastMap fm = (FastMap) obj;
            fm.setShared(xml.getAttribute("shared", false));
            FastComparator keyComparator = (FastComparator) xml
                    .get("KeyComparator");
            if (keyComparator != null) {
                fm.setKeyComparator(keyComparator);
            }
            FastComparator valueComparator = (FastComparator) xml
                    .get("ValueComparator");
            if (valueComparator != null) {
                fm.setValueComparator(valueComparator);
            }
            while (xml.hasNext()) {
                Object key = xml.get("Key");
                Object value = xml.get("Value");
                fm.put(key, value);
            }
        }

        public void write(Object obj, OutputElement xml)
                throws XMLStreamException {
            final FastMap fm = (FastMap) obj;
            if (fm.isShared()) {
                xml.setAttribute("shared", true);
            }
            if (fm.getKeyComparator() != FastComparator.DEFAULT) {
                xml.add(fm.getKeyComparator(), "KeyComparator");
            }
            if (fm.getValueComparator() != FastComparator.DEFAULT) {
                xml.add(fm.getValueComparator(), "ValueComparator");
            }
            for (Entry e = fm.head(), end = fm.tail(); (e = (Entry) e.getNext()) != end;) {
                xml.add(e.getKey(), "Key");
                xml.add(e.getValue(), "Value");
            }
        }
    };

    /**
     * Holds the default XML representation for FastCollection instances.
     * This representation is identical to {@link XMLBinding#COLLECTION_XML}.
     */
    static final XMLFormat/*<FastCollection>*/ FASTCOLLECTION_XML = new XMLFormat(
            Javolution.j2meGetClass("javolution.util.FastCollection")) {

        public void read(InputElement xml, Object obj) throws XMLStreamException {
            FastCollection fc = (FastCollection) obj;
            while (xml.hasNext()) {
                fc.add(xml.getNext());
            }
        }

        public void write(Object obj, OutputElement xml) throws XMLStreamException {
            FastCollection fc = (FastCollection) obj;
            for (Record r=fc.head(), end=fc.tail(); (r=r.getNext())!=end;) {
                xml.add(fc.valueOf(r));
            }
        }
    };

    /**
     * Holds the default XML representation for FastComparator instances
     * (format ensures unicity of predefined comparator).
     */
    static final XMLFormat/*<FastComparator>*/ FASTCOMPARATOR_XML = new XMLFormat(
            Javolution.j2meGetClass("javolution.util.FastComparator")) {

        public Object newInstance(Class cls, javolution.xml.XMLFormat.InputElement xml) throws XMLStreamException {
            if (cls == FastComparator.DEFAULT.getClass())
                return FastComparator.DEFAULT;
            if (cls == FastComparator.DIRECT.getClass())
                return FastComparator.DIRECT;
            if (cls == FastComparator.IDENTITY.getClass())
                return FastComparator.IDENTITY;
            if (cls == FastComparator.LEXICAL.getClass())
                return FastComparator.LEXICAL;
            if (cls == FastComparator.REHASH.getClass())
                return FastComparator.REHASH;
            return super.newInstance(cls, xml);
        }

        public void read(InputElement xml, Object obj) throws XMLStreamException {
            // Do nothing.
        }

        public void write(Object obj, OutputElement xml) throws XMLStreamException {
            // Do nothing.
        }
    };

    /**
     * Holds the default XML representation for indexes.
     * This presentation consists of a <code>"value"</code> attribute 
     * holding the index <code>int</code> value.
     */
    static final XMLFormat/*<Index>*/INDEX_XML = new XMLFormat(Index.ZERO
            .getClass()) {

        public boolean isReferenceable() {
            return false; // Always by value (immutable). 
        }

        public Object newInstance(Class cls, InputElement xml)
                throws XMLStreamException {
            return Index.valueOf(xml.getAttribute("value", 0));
        }

        public void read(InputElement xml, Object obj)
                throws XMLStreamException {
            // Do nothing.
        }

        public void write(Object obj, OutputElement xml)
                throws XMLStreamException {
            xml.setAttribute("value", ((Index) obj).intValue());
        }
    };

    /**
     * Holds the XML representation for persistent contexts
     * (holds persistent reference mapping).
     */
    static final XMLFormat/*<PersistentContext>*/PERSISTENT_CONTEXT_XML = new XMLFormat(
            new PersistentContext().getClass()) {
        public void read(InputElement xml, Object obj)
                throws XMLStreamException {
            final PersistentContext ctx = (PersistentContext) obj;
            ctx.getIdToValue().putAll((FastMap) xml.get("References"));
        }

        public void write(Object obj, OutputElement xml)
                throws XMLStreamException {
            final PersistentContext ctx = (PersistentContext) obj;
            xml.add(ctx.getIdToValue(), "References");
        }
    };
    
    private static CharSequence toCsq/**/(Object str) {
        return Javolution.j2meToCharSeq(str);
    }

}