/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.xml;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import javolution.lang.Configurable;
import javolution.lang.Reflection;
import javolution.lang.Reusable;
import javolution.text.CharArray;
import javolution.text.TextBuilder;
import javolution.text.TextFormat;
import javolution.util.FastList;
import javolution.util.FastMap;
import javolution.util.FastSet;
import javolution.util.FastTable;
import javolution.xml.XMLFormat.InputElement;
import javolution.xml.XMLFormat.OutputElement;
import javolution.xml.stream.XMLStreamException;
import javolution.xml.stream.XMLStreamReader;
import javolution.xml.stream.XMLStreamWriter;

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
 *         protected XMLFormat getFormat(Class forClass) {
 *             Field[] fields = forClass.getDeclaredFields();
 *             return new XMLReflectionFormat(fields);
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
 *     // XML binding overriding default formats.
 *     public MyBinding extends XMLBinding {
 *         // Non-static formats use unmapped XMLFormat instances.
 *         XMLFormat<String> myStringFormat = new XMLFormat<String>(null) {...}
 *         XMLFormat<Collection> myCollectionFormat = new XMLFormat<Collection>(null) {...}
 *         protected XMLFormat getFormat(Class forClass) throws XMLStreamException {
 *             if (String.class.equals(forClass))
 *                  return myStringFormat;
 *             if (Collection.class.isAssignableFrom(forClass))
 *                  return myCollectionFormat;
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
 * @version 5.4, December 1, 2009
 */
public class XMLBinding implements Reusable, XMLSerializable {

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
     * The default implementation returns the {@link XMLFormat#getInstance}
     * for the specified class.
     * 
     * @param forClass the class for which the XML format is returned.
     * @return the XML format for the specified class (never <code>null</code>).
     * @throws XMLStreamException if there is no format for the
     *         specified class.
     */
    protected XMLFormat getFormat(Class forClass) throws XMLStreamException  {
         return XMLFormat.getInstance(forClass);
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
        QName classQName;
        if (useAttributes) {
            if (_classAttribute == null)
                throw new XMLStreamException(
                        "Binding has no class attribute defined, cannot retrieve class");
            classQName = QName.valueOf(reader.getAttributeValue(_classAttribute
                    .getNamespaceURI(), _classAttribute.getLocalName()));
            if (classQName == null)
                throw new XMLStreamException(
                        "Cannot retrieve class (class attribute not found)");
        } else {
            classQName = QName.valueOf(reader.getNamespaceURI(), reader
                    .getLocalName());
        }

        // Searches aliases with namespace URI.
        Class cls = (Class) _aliasToClass.get(classQName);
        if (cls != null)
            return cls;

        // Searches aliases without namespace URI.
        cls = (Class) _aliasToClass.get(QName.valueOf(classQName.getLocalName()));
        if (cls != null)
            return cls;

        // Finally convert the qualified name to a class (ignoring namespace URI).
        cls = Reflection.getInstance().getClass(classQName.getLocalName());
        if (cls == null)
            throw new XMLStreamException("Class " + classQName.getLocalName() + 
                    " not found (see javolution.lang.Reflection to support additional class loader)");
        _aliasToClass.put(classQName, cls);
        return cls;
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
                        QName.j2meToCharSeq(name));
            } else {
                writer.writeAttribute(_classAttribute.getNamespaceURI(),
                        _classAttribute.getLocalName(), QName.j2meToCharSeq(name));
            }
        } else {
            if (qName != null) {
            	if(qName.getNamespaceURI() == null) {
            		writer.writeStartElement(qName.getLocalName());
            	} else {
            		writer.writeStartElement(qName.getNamespaceURI(), qName.getLocalName());
            	}
            } else {
                writer.writeStartElement(QName.j2meToCharSeq(name));
            }
        }
    }

    // Implements Reusable.
    public void reset() {
        _classAttribute = QName.valueOf("class");
        _aliasToClass.reset();
        _classToAlias.reset();
    }

    //////////////////////////////////////////////////
    // PREDEFINED FORMATS (LOADED ONLY IF REQUIRED) //
    //////////////////////////////////////////////////

    /**
     * Holds the static XML format for <code>java.lang.Object.class</code> instances.
     * The XML representation consists of the text representation of the object
     * as a "value" attribute.
     */
    static final XMLFormat OBJECT_XML = new XMLFormat(Object.class) {
      public boolean isReferenceable() {
            return false; // Always by value (immutable).
        }

        public Object newInstance(Class cls,
                javolution.xml.XMLFormat.InputElement xml)
                throws XMLStreamException {
            TextFormat format = TextFormat.getInstance(cls);
            if (!format.isParsingSupported())
                throw new XMLStreamException("No XMLFormat or TextFormat (with parsing supported) for instances of " + cls);
            CharArray value = xml.getAttribute("value");
            if (value == null) throw new XMLStreamException("Missing value attribute (to be able to parse the instance of " + cls + ")");
            return format.parse(value);
        }

        public void read(InputElement xml, Object obj)
                throws XMLStreamException {
            // Do nothing.
        }

        public void write(Object obj, OutputElement xml)
                throws XMLStreamException {
            TextBuilder tmp = TextBuilder.newInstance();
            try {
                TextFormat.getInstance(obj.getClass()).format(obj, tmp);
                xml.setAttribute("value", tmp);
            } finally {
                TextBuilder.recycle(tmp);
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
    static final XMLFormat COLLECTION_XML = new XMLFormat(
            java.util.Collection.class) {

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
    static final XMLFormat MAP_XML = new XMLFormat(java.util.Map.class) {

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

}