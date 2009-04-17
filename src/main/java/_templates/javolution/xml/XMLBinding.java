/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2006 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package _templates.javolution.xml;

import java.io.IOException;

import _templates.java.lang.CharSequence;
import _templates.java.util.Collection;
import _templates.java.util.Iterator;
import _templates.java.util.Map;
import _templates.javolution.Javolution;
import _templates.javolution.JavolutionError;
import _templates.javolution.context.PersistentContext;
import _templates.javolution.lang.ClassInitializer;
import _templates.javolution.lang.Reflection;
import _templates.javolution.lang.Reusable;
import _templates.javolution.text.Appendable;
import _templates.javolution.text.CharArray;
import _templates.javolution.text.Text;
import _templates.javolution.util.FastCollection;
import _templates.javolution.util.FastComparator;
import _templates.javolution.util.FastMap;
import _templates.javolution.util.Index;
import _templates.javolution.util.FastCollection.Record;
import _templates.javolution.util.FastMap.Entry;
import _templates.javolution.xml.stream.XMLStreamException;
import _templates.javolution.xml.stream.XMLStreamReader;
import _templates.javolution.xml.stream.XMLStreamWriter;



/**
 * <p> This class represents the binding between Java classes and 
 *     their XML representation ({@link XMLFormat}).</p>
 *     
 * <p> Custom XML bindings can also be used to alias class names and 
 *     ensure that the XML representation is:<ul>
 *     <li> Impervious to obfuscation.</li>
 *     <li> Unaffected by any class refactoring.</li>
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
 *         protected <T> XMLFormat<T> getFormat(Class<T> cls) {
 *             Field[] fields = cls.getDeclaredFields();
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
 *         protected <T> XMLFormat<T> getFormat(Class<T> cls) {
 *             if (String.class.equals(cls))
 *                  return _myStringFormat;
 *             if (Collection.class.isAssignableFrom(cls))
 *                  return _myCollectionFormat;
 *             return super.getFormat(cls);
 *         }
 *     }
 *     [/code]
 *      
 * <p> The default XML binding implementation supports all static XML formats 
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
 * @version 5.3, April 9, 2007
 */
public class XMLBinding implements Reusable, XMLSerializable {

    /**
     * Holds the default XML representation of this binding.
     */
    static final XMLFormat XML = new XMLFormat(XMLBinding.class) {

        public void read(InputElement xml, Object obj)
                throws XMLStreamException {
            XMLBinding binding = (XMLBinding) obj;
            binding._classAttribute = (QName) xml.get("classAttribute",
                    QName.class);
            binding._classToAlias.putAll((FastMap) xml.get("aliases",
                    FastMap.class));
            binding._aliasToClass.putAll(binding._classToAlias.reverse());
        }

        public void write(Object obj, OutputElement xml)
                throws XMLStreamException {
            XMLBinding binding = (XMLBinding) obj;
            xml.add(binding._classAttribute, "classAttribute", QName.class);
            xml.add(binding._classToAlias, "aliases", FastMap.class);
        }
    };

    /**
     * Holds the static mapping format.
     */
    private static FastMap STATIC_MAPPING = new FastMap().setShared(true);

    /**
     * Holds the default instance used by readers/writers (thread-safe).
     */
    static final XMLBinding DEFAULT = new XMLBinding();

    /**
     * Holds the class attribute.
     */
    private QName _classAttribute = QName.valueOf("class");

    /**
     * Holds the class to alias (QName) mapping.
     */
    private final FastMap _classToAlias = new FastMap();

    /**
     * Holds the alias (QName) to class mapping.
     */
    private final FastMap _aliasToClass = new FastMap();

    /**
     * Default constructor.
     */
    public XMLBinding() {
    }

    /**
     * Sets the qualified alias for the specified class.
     * 
     * @param cls the class being aliased.
     * @param qName the qualified name.
     */
    public void setAlias(Class cls, QName qName) {
        _classToAlias.put(cls, qName);
        _aliasToClass.put(qName, cls);
    }

    /**
     * Convenient method equivalent to {@link #setAlias(Class, QName) 
     * setAlias(cls, QName.valueOf(alias))}.
     * 
     * @param cls the class being aliased.
     * @param alias the alias for the specified class.
     */
    public final void setAlias(Class cls, String alias) {
        setAlias(cls, QName.valueOf(alias));
    }

    /**
     * Sets the qualified name of the attribute holding the 
     * class identifier. If the local name is <code>null</code> the class 
     * attribute is never read/written (which may prevent unmarshalling).
     * 
     * @param classAttribute the qualified name of the class attribute or 
     *        <code>null<code>.
     */
    public void setClassAttribute(QName classAttribute) {
        _classAttribute = classAttribute;
    }

    /**
     * Convenience method equivalent to {@link #setClassAttribute(QName)
     * setClassAttribute(QName.valueOf(name))}.
     * 
     * @param name the name of the class attribute or <code>null<code>.
     */
    public final void setClassAttribute(String name) {
        setClassAttribute(name == null ? null : QName.valueOf(name));
    }

    /**
     * Returns the XML format for the specified class/interface.
     * The default implementation returns the most 
     * specialized static format compatible with the specified class.
     * 
     * @param cls the class for which the XML format is returned.
     * @return the XML format for the specified class. 
     */
    protected/*<T>*/XMLFormat/*<T>*/getFormat(Class/*<T>*/cls) {
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
     * Reads the class corresponding to the current XML element.
     * 
     * This method is called by {@link XMLFormat.InputElement#getNext()} 
     * {@link XMLFormat.InputElement#get(String)} and
     * {@link XMLFormat.InputElement#get(String, String)} to retrieve the 
     * Java class corresponding to the current XML element.
     * 
     * If <code>useAttributes</code> is set, the default implementation 
     * reads the class name from the class attribute; otherwise the class 
     * name (or alias) is read from the current element qualified name.
     * 
     * @param reader the XML stream reader.
     * @param useAttributes indicates if the element's attributes should be 
     *        used to identify the class (e.g. when the element name is 
     *        specified by the user then attributes have to be used).
     * @return the corresponding class.
     * @throws XMLStreamException 
     */
    protected Class readClass(XMLStreamReader reader, boolean useAttributes)
            throws XMLStreamException {
        QName className;
        if (useAttributes) {
            if (_classAttribute == null)
                throw new XMLStreamException(
                        "Binding has no class attribute defined, cannot retrieve class");
            className = QName.valueOf(reader.getAttributeValue(_classAttribute
                    .getNamespaceURI(), _classAttribute.getLocalName()));
            if (className == null)
                throw new XMLStreamException(
                        "Cannot retrieve class (class attribute not found)");
        } else {
            className = QName.valueOf(reader.getNamespaceURI(), reader
                    .getLocalName());
        }
        try {
            Class cls = (Class) _aliasToClass.get(className);
            if (cls != null)
                return cls;
            cls = Reflection.getClass(className.getLocalName());
            _aliasToClass.put(className, cls);
            return cls;
        } catch (ClassNotFoundException e) {
            throw new XMLStreamException("Class: " + className + " not found");
        }
    }

    /**
     * Writes the specified class to the current XML element attributes or to 
     * a new element if the element attributes cannot be used.
     * 
     * This method is called by 
     * {@link XMLFormat.OutputElement#add(Object)} and 
     * {@link XMLFormat.OutputElement#add(Object, String)} and 
     * {@link XMLFormat.OutputElement#add(Object, String, String)} to 
     * identify the Java class corresponding to the XML element.
     * 
     * 
     * @param cls the class to be written.
     * @param writer the XML stream writer.
     * @param useAttributes indicates if the element's attributes should be 
     *        used to identify the class (e.g. when the element name is 
     *        specified by the user then attributes have to be used).
     * @throws XMLStreamException 
     */
    protected void writeClass(Class cls, XMLStreamWriter writer,
            boolean useAttributes) throws XMLStreamException {
        QName qName = (QName) _classToAlias.get(cls);
        String name = qName != null ? qName.toString() : cls.getName();
        if (useAttributes) {
            if (_classAttribute == null)
                return;
            if (_classAttribute.getNamespaceURI() == null) {
                writer.writeAttribute(_classAttribute.getLocalName(),
                        Javolution.j2meToCharSeq(name));
            } else {
                writer.writeAttribute(_classAttribute.getNamespaceURI(),
                        _classAttribute.getLocalName(), Javolution
                                .j2meToCharSeq(name));
            }
        } else {
            if (qName != null) {
                writer.writeStartElement(qName.getNamespaceURI(), qName
                        .getLocalName());
            } else {
                writer.writeStartElement(Javolution.j2meToCharSeq(name));
            }
        }
    }

    // Implements Reusable.
    public void reset() {
        _classAttribute = QName.valueOf("class");
        _aliasToClass.reset();
        _classToAlias.reset();
    }

    /**
     * Holds the static XML format for <code>Object.class</code> instances
     * (default format when a more specialized format does not exist).
     * The XML representation consists of an empty element with no attribute.
     */
    static final XMLFormat/*<Object>*/OBJECT_XML = new XMLFormat(Object.class) {

        public void read(_templates.javolution.xml.XMLFormat.InputElement xml, Object obj) {
            // Do nothing.
        }

        public void write(Object obj, _templates.javolution.xml.XMLFormat.OutputElement xml) {
            // Do nothing
        }
    };

    /**
     * Holds the default XML representation for <code>java.lang.Class</code>
     * instances. This representation consists of a <code>"name"</code> 
     * attribute holding the class name.
     */
    static final XMLFormat/*<Class>*/CLASS_XML = new XMLFormat(Class.class) {

        public boolean isReferenceable() {
            return false; // Always by value (immutable). 
        }

        public Object newInstance(Class cls,
                _templates.javolution.xml.XMLFormat.InputElement xml)
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

        public void read(_templates.javolution.xml.XMLFormat.InputElement xml, Object obj)
                throws XMLStreamException {
            // Do nothing.            
        }

        public void write(Object obj, _templates.javolution.xml.XMLFormat.OutputElement xml)
                throws XMLStreamException {
            xml.setAttribute("name", ((Class) obj).getName());
        }
    };

    /**
     * Holds the default XML representation for <code>java.lang.String</code>
     * instances. This representation consists of a <code>"value"</code> 
     * attribute holding the string.
     */
    static final XMLFormat/*<String>*/STRING_XML = new XMLFormat(String.class) {

        public Object newInstance(Class cls,
                _templates.javolution.xml.XMLFormat.InputElement xml)
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
     * Holds the default XML representation for {@link Appendable}
     * instances. This representation consists of a <code>"value"</code> attribute
     * holding the characters.
     */
    static final XMLFormat/*<Appendable>*/APPENDABLE_XML = new XMLFormat(
            Appendable.class) {

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
            _templates.java.util.Collection.class) {

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
    static final XMLFormat/*<Map>*/MAP_XML = new XMLFormat(_templates.java.util.Map.class) {

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
    /*<Object[]>*/
    /*@JVM-1.4+@
             OBJECT_ARRAY_XML = new XMLFormat(
             new Object[0].getClass()) {
                 public Object newInstance(Class cls, javolution.xml.XMLFormat.InputElement xml) throws XMLStreamException {
                     Class componentType = cls.getComponentType();
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
            Boolean.class) {

        public boolean isReferenceable() {
            return false; // Always by value (immutable). 
        }

        public Object newInstance(Class cls,
                _templates.javolution.xml.XMLFormat.InputElement xml)
                throws XMLStreamException {
            return xml.getAttribute("value", false) ? Boolean.TRUE
                    : Boolean.FALSE;
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
    static final XMLFormat/*<Byte>*/BYTE_XML = new XMLFormat(Byte.class) {

        public boolean isReferenceable() {
            return false; // Always by value (immutable). 
        }

        public Object newInstance(Class cls,
                _templates.javolution.xml.XMLFormat.InputElement xml)
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
            Character.class) {

        public boolean isReferenceable() {
            return false; // Always by value (immutable). 
        }

        public Object newInstance(Class cls,
                _templates.javolution.xml.XMLFormat.InputElement xml)
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
    static final XMLFormat/*<Short>*/SHORT_XML = new XMLFormat(Short.class) {

        public boolean isReferenceable() {
            return false; // Always by value (immutable). 
        }

        public Object newInstance(Class cls,
                _templates.javolution.xml.XMLFormat.InputElement xml)
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
            Integer.class) {

        public boolean isReferenceable() {
            return false; // Always by value (immutable). 
        }

        public Object newInstance(Class cls,
                _templates.javolution.xml.XMLFormat.InputElement xml)
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
    static final XMLFormat/*<Long>*/LONG_XML = new XMLFormat(Long.class) {

        public boolean isReferenceable() {
            return false; // Always by value (immutable). 
        }

        public Object newInstance(Class cls,
                _templates.javolution.xml.XMLFormat.InputElement xml)
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
     */
    static final XMLFormat/*<Float>*/
    FLOAT_XML = new XMLFormat(Float.class) {

        public boolean isReferenceable() {
            return false; // Always by value (immutable). 
        }

        public Object newInstance(Class cls,
                _templates.javolution.xml.XMLFormat.InputElement xml)
                throws XMLStreamException {
            return new Float(xml.getAttribute("value", 0f));
        }

        public void read(InputElement xml, Object obj)
                throws XMLStreamException {
            // Do nothing.
        }

        public void write(Object obj, OutputElement xml)
                throws XMLStreamException {
            xml.setAttribute("value", ((Float) obj).floatValue());
        }
    };

    /**
     * Holds the default XML representation for <code>java.lang.Double</code>.
     */
    static final XMLFormat/*<Double>*/
    DOUBLE_XML = new XMLFormat(Double.class) {
        public boolean isReferenceable() {
            return false; // Always by value (immutable). 
        }

        public Object newInstance(Class cls,
                _templates.javolution.xml.XMLFormat.InputElement xml)
                throws XMLStreamException {
            return new Double(xml.getAttribute("value", 0.0));
        }

        public void read(InputElement xml, Object obj)
                throws XMLStreamException {
            // Do nothing.
        }

        public void write(Object obj, OutputElement xml)
                throws XMLStreamException {
            xml.setAttribute("value", ((Double) obj).doubleValue());
        }
    };

    ////////////////////////////////////////////////////////////////////////////
    // JAVOLUTION XML FORMAT (HERE TO AVOID LOADING XML FRAMEWORK IF NOT USED)//
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Holds the default XML representation for Text instances. 
     * This representation consists of a <code>"value"</code> attribute 
     * holding the characters.
     */
    static final XMLFormat/*<Text>*/TEXT_XML = new XMLFormat(
            _templates.javolution.text.Text.class) {

        public Object newInstance(Class cls,
                _templates.javolution.xml.XMLFormat.InputElement xml)
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
    static final XMLFormat/*<FastMap>*/FASTMAP_XML = new XMLFormat(
            FastMap.class) {

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
    static final XMLFormat/*<FastCollection>*/FASTCOLLECTION_XML = new XMLFormat(
            _templates.javolution.util.FastCollection.class) {

        public void read(InputElement xml, Object obj)
                throws XMLStreamException {
            FastCollection fc = (FastCollection) obj;
            while (xml.hasNext()) {
                fc.add(xml.getNext());
            }
        }

        public void write(Object obj, OutputElement xml)
                throws XMLStreamException {
            FastCollection fc = (FastCollection) obj;
            for (Record r = fc.head(), end = fc.tail(); (r = r.getNext()) != end;) {
                xml.add(fc.valueOf(r));
            }
        }
    };

    /**
     * Holds the default XML representation for FastComparator instances
     * (format ensures unicity of predefined comparator).
     */
    static final XMLFormat/*<FastComparator>*/FASTCOMPARATOR_XML = new XMLFormat(
            _templates.javolution.util.FastComparator.class) {

        public Object newInstance(Class cls,
                _templates.javolution.xml.XMLFormat.InputElement xml)
                throws XMLStreamException {
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
            if (cls == FastComparator.STRING.getClass())
                return FastComparator.STRING;
            return super.newInstance(cls, xml);
        }

        public void read(InputElement xml, Object obj)
                throws XMLStreamException {
            // Do nothing.
        }

        public void write(Object obj, OutputElement xml)
                throws XMLStreamException {
            // Do nothing.
        }
    };

    /**
     * Holds the default XML representation for indexes.
     * This presentation consists of a <code>"value"</code> attribute 
     * holding the index <code>int</code> value.
     */
    static final XMLFormat/*<Index>*/INDEX_XML = new XMLFormat(Index.class) {

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
            PersistentContext.class) {
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

}