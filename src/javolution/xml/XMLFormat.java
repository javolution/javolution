/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2006 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.xml;

import java.util.Hashtable;
import j2me.lang.CharSequence;
import javolution.Javolution;
import javolution.text.CharArray;
import javolution.text.TextBuilder;
import javolution.text.TypeFormat;
import javolution.xml.sax.Attributes;
import javolution.xml.stream.XMLStreamException;
import javolution.xml.stream.XMLStreamReader;
import javolution.xml.stream.XMLStreamReaderImpl;
import javolution.xml.stream.XMLStreamWriter;
import javolution.xml.stream.XMLStreamWriterImpl;

/**
 * <p> This class represents the format base class for XML serialization and
 *     deserialization.</p>
 *     
 * <p> Application classes typically define a default XML format for their 
 *     instances using static {@link XMLFormat} class members. 
 *     Formats are inherited by sub-classes. For example:[code]
 *     
 *     public abstract class Graphic {
 *         private boolean _isVisible;
 *         private Paint _paint; // null if none.
 *         private Stroke _stroke; // null if none.
 *         private Transform _transform; // null if none.
 *          
 *         // XML format with positional associations (members identified by their position),
 *         // see XML package description for examples of name associations.
 *         private static final XMLFormat<Graphic> XML = new XMLFormat<Graphic>(Graphic.class) {
 *              public void write(Graphic g, OutputElement xml) {
 *                  xml.setAttribute("isVisible", g._isVisible); 
 *                  xml.add(g._paint); // First.
 *                  xml.add(g._stroke); // Second.
 *                  xml.add(g._transform); // Third.
 *              }
 *              public void read(InputElement xml, Graphic g) {
 *                  g._isVisible = xml.getAttribute("isVisible", true);
 *                  g._paint = xml.getNext();
 *                  g._stroke = xml.getNext();
 *                  g._transform = xml.getNext();
 *                  return g;
 *             }
 *         };
 *    }[/code]
 *    
 * <p> Due to the sequential nature of XML serialization/deserialization, 
 *     formatting/parsing of XML attributes should always be performed before 
 *     formatting/parsing of the XML content.</p>
 * 
 * <p> The mapping between classes and XML formats is defined by {@link 
 *     XMLBinding} instances. 
 *     Here is an example of serialization/deserialization:[code]
 *     
 *     // Creates a list holding diverse objects.
 *     List list = new ArrayList();
 *     list.add("John Doe");
 *     list.add(null);
 *     Map map = new FastMap();
 *     map.put("ONE", new Integer(1));
 *     map.put("TWO", new Integer(2));
 *     list.add(map);
 *     
 *     // Creates some aliases to use instead of class names.
 *     XMLBinding binding = new XMLBinding();
 *     binding.setAlias(FastMap.class, "Map");
 *     binding.setAlias(String.class, "String");
 *     binding.setAlias(Integer.class, "Integer");
 *     
 *     // Formats the list to XML .
 *     OutputStream out = new FileOutputStream("C:/list.xml");
 *     XMLObjectWriter writer = new XMLObjectWriter().setOutput(out).setBinding(binding);
 *     writer.write(list, "MyList", ArrayList.class);
 *     writer.close();[/code]
 *     
 *     Here is the output <code>list.xml</code> document produced:[code]
 *     
 *     <MyList>
 *         <String value="John Doe"/>
 *         <Null/>
 *         <Map>
 *             <Key class="String" value="ONE"/>
 *             <Value class="Integer" value="1"/>
 *             <Key class="String" value="TWO"/>
 *             <Value class="Integer" value="2"/>
 *         </Map>
 *     </MyList>[/code]
 *     
 *     The list can be read back with the following code:[code]
 *     
 *     // Reads back to a FastTable instance.
 *     InputStream in = new FileInputStream("C:/list.xml");
 *     XMLObjectReader reader = new XMLObjectReader().setInput(in).setBinding(binding);
 *     FastTable table = reader.read("MyList", FastTable.class); 
 *     reader.close();[/code]
 *     </p>
 * 
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 4.0, September 4, 2006
 */
public abstract class XMLFormat/*<T>*/{

    /**
     * Holds <code>null</code> representation.
     */
    private static final String NULL = "Null";

    /**
     * Holds the class instances.
     */
    static volatile XMLFormat[] _ClassInstances = new XMLFormat[64];

    /**
     * Holds the number of class instances.
     */
    static volatile int _ClassInstancesLength;

    /**
     * Holds the class associated to this format (static instances only).
     */
    final Class _class;

    /**
     * Creates a XML format mapped to the specified class. If the specified 
     * class is <code>null</code> then the format is left unmapped (e.g. 
     * dynamic format used by custom {@link XMLBinding binding} instances).
     * 
     * @param cls the root class/interface to associate to this XML format
     *        or <code>null</code> if this format is not mapped.
     * @throws IllegalArgumentException if the specified class is already 
     *         bound to another format.
     */
    protected XMLFormat(Class/*<T>*/cls) {
        _class = cls;
        if (cls == null)
            return; // Dynamic format.
        synchronized (_ClassToFormat) {
            // Check if statically bounded.
            if (_ClassToFormat.containsKey(cls))
                throw new IllegalArgumentException(
                        "Multiple static binding for class " + cls
                        + " The XMLFormat(Class) constructor should be " +
                                "used solely for static instances.");
            final int length = XMLFormat._ClassInstancesLength;
            final XMLFormat[] formats = XMLFormat._ClassInstances;
            if (length >= formats.length) { // Resizes (ImmortalMemory).
                XMLFormat[] tmp = new XMLFormat[length * 2];
                System.arraycopy(formats, 0, tmp, 0, length);
                XMLFormat._ClassInstances = tmp;
            }
            XMLFormat._ClassInstances[XMLFormat._ClassInstancesLength++] = this;
            _ClassToFormat.put(cls, this);
        }
    }

    private static Hashtable _ClassToFormat = new Hashtable();

    /**
     * Returns the class/interface statically bound to this format or 
     * <code>null</code> if none.
     * 
     * @return the class/interface bound to this format.
     */
    public final Class/*<T>*/getBoundClass() {
        return _class;
    }

    /**
     * Indicates if the object serialized through this format can be referenced
     * to (default <code>true</code>). This method can be overriden to return
     * <code>false</code> if  serialized objects are manipulated "by value".
     *
     * @return <code>true</code> if serialized object may hold a reference;
     *         <code>false</code> otherwise.
     * @see XMLReferenceResolver
     */
    public boolean isReferenceable() {
        return true;
    }

    /**
     * Allocates a new object of the specified class from the specified 
     * XML input element. By default, this method returns an object created 
     * using the public no-arg constructor of the specified class. 
     * XML formats may override this method in order to use private/multi-arg
     * constructors.  
     *
     * @param cls the class of the object to return.
     * @param xml the XML input element.
     * @return the object corresponding to the specified XML element.
     */
    public Object/*{T}*/newInstance(Class/*<T>*/cls, InputElement xml)
            throws XMLStreamException {
        try {
            return cls.newInstance();
        } catch (InstantiationException e) {
            throw new XMLStreamException(e);
        } catch (IllegalAccessException e) {
            throw new XMLStreamException(e);
        }
    }

    /**
     * Formats an object into the specified XML output element.
     *
     * @param obj the object to format.
     * @param xml the <code>XMLElement</code> destination.
     */
    public abstract void write(Object/*{T}*/obj, OutputElement xml)
            throws XMLStreamException;

    /**
     * Parses an XML input element into the specified object. 
     * 
     * @param xml the XML element to parse.
     * @param obj the object created through {@link #newInstance}
     *        and to setup from the specified XML element.
     */
    public abstract void read(InputElement xml, Object/*{T}*/obj)
            throws XMLStreamException;

    /**
     * This class represents an input XML element (unmarshalling).
     */
    protected static final class InputElement {

        /**
         * Holds the stream reader.
         */
        final XMLStreamReaderImpl _reader = new XMLStreamReaderImpl();

        /**
         * Holds the XML binding.
         */
        private XMLBinding _binding;

        /**
         * Holds the reference resolver.
         */
        private XMLReferenceResolver _referenceResolver;

        /**
         * Indicates if the reader is currently positioned on the next element.
         */
        private boolean _isReaderAtNext;

        /**
         * Default constructor.
         */
        InputElement() {
            reset();
        }

        /**
         * Returns the StAX-like stream reader (provides complete control 
         * over the unmarshalling process).
         * 
         * @return the stream reader.
         */
        public XMLStreamReader getStreamReader() {
            return _reader;
        }

        /**
         * Indicates if more nested XML element can be read. This method 
         * positions the {@link #getStreamReader reader} at the start of the
         * next XML element to be read (if any).
         *
         * @return <code>true</code> if there is more XML element to be read; 
         *         <code>false</code> otherwise.
         */
        public boolean hasNext() throws XMLStreamException {
            if (!_isReaderAtNext) {
                _isReaderAtNext = true;
                _reader.nextTag();
            }
            return _reader.getEventType() == XMLStreamReader.START_ELEMENT;
        }

        /**
         * Returns the next object whose type is identified by the local name
         * and URI of the current XML element (see {@link XMLBinding}).
         *
         * @return the next nested object which can be <code>null</code>.
         * @throws XMLStreamException if <code>hasNext() == false</code>.
         */
        public/*<T>*/Object/*{T}*/getNext() throws XMLStreamException {
            if (!hasNext()) // Asserts isReaderAtNext == true
                throw new XMLStreamException("No more element to read", _reader
                        .getLocation());

            // Checks for null.
            if (_reader.getLocalName().equals(NULL)) {
                if (_reader.next() != XMLStreamReader.END_ELEMENT)
                    throw new XMLStreamException("Non Empty Null Element");
                _isReaderAtNext = false;
                return null;
            }

            // Checks if reference.
            if (_referenceResolver != null) {
                Object obj = _referenceResolver.readReference(this);
                if (obj != null) {
                    if (_reader.next() != XMLStreamReader.END_ELEMENT)
                        throw new XMLStreamException("Non Empty Reference Element");
                    _isReaderAtNext = false;
                    return (Object/*{T}*/) obj;
                }
            }

            // Retrieves object's class.
            Class cls;
            try {
                cls = _binding.getClass(_reader.getLocalName(), _reader
                        .getNamespaceURI());
            } catch (ClassNotFoundException e) {
                throw new XMLStreamException(e);
            }

            return (Object/*{T}*/) get(cls);
        }

        /**
         * Returns the object whose type is identified by a XML class attribute
         * only if the XML element has the specified local name.
         *
         * @param name the local name of the next element.
         * @return the next nested object or <code>null</code>.
         */
        public/*<T>*/Object/*{T}*/get(String name) throws XMLStreamException {
            if (!hasNext()// Asserts isReaderAtNext == true
                    || !_reader.getLocalName().equals(name))
                return null;

            // Checks if reference.
            if (_referenceResolver != null) {
                Object obj = _referenceResolver.readReference(this);
                if (obj != null) {
                    if (_reader.next() != XMLStreamReader.END_ELEMENT)
                        throw new XMLStreamException("Non Empty Reference Element");
                    _isReaderAtNext = false;
                    return (Object/*{T}*/) obj;
                }
            }

            // Retrieves object's class from class attribute.
            Class cls = _binding.readClassAttribute(_reader);

            return (Object/*{T}*/) get(cls);
        }

        /**
         * Returns the object whose type is identified by a XML class attribute
         * only if the XML element has the specified local name and URI.
         *
         * @param localName the local name.
         * @param uri the namespace URI or <code>null</code>.
         * @return the next nested object or <code>null</code>.
         */
        public/*<T>*/Object/*{T}*/get(String localName, String uri)
                throws XMLStreamException {
            if (uri == null)
                return (Object/*{T}*/) get(localName);

            if (!hasNext()// Asserts isReaderAtNext == true
                    || !_reader.getLocalName().equals(localName)
                    || !_reader.getNamespaceURI().equals(uri))
                return null;

            // Checks if reference.
            if (_referenceResolver != null) {
                Object obj = _referenceResolver.readReference(this);
                if (obj != null) {
                    if (_reader.next() != XMLStreamReader.END_ELEMENT)
                        throw new XMLStreamException("Non Empty Reference Element");
                    _isReaderAtNext = false;
                    return (Object/*{T}*/) obj;
                }
            }

            // Retrieves object's class from class attribute.
            Class cls = _binding.readClassAttribute(_reader);

            return (Object/*{T}*/) get(cls);
        }

        /**
         * Returns the object of specified type only if the XML element has the
         * specified local name.
         *      
         * @param name the local name of the element to match.
         * @param cls the class identifying the format of the object to return.
         * @return the next nested object or <code>null</code>.
         */
        public/*<T>*/Object/*{T}*/get(String name, Class/*<T>*/cls) 
                throws XMLStreamException {
            if (!hasNext()// Asserts isReaderAtNext == true
                    || !_reader.getLocalName().equals(name))
                return null;

            // Checks if reference.
            if (_referenceResolver != null) {
                Object obj = _referenceResolver.readReference(this);
                if (obj != null) {
                    if (_reader.next() != XMLStreamReader.END_ELEMENT)
                        throw new XMLStreamException("Non Empty Reference Element");
                    _isReaderAtNext = false;
                    return (Object/*{T}*/) obj;
                }
            }

            return (Object/*{T}*/) get(cls);
        }

        /**
         * Returns the object of specified type only if the 
         * XML element has the specified local name and namespace URI.
         *      
         * @param localName the local name.
         * @param uri the namespace URI or <code>null</code>.
         * @param cls the class identifying the format of the object to return.
         * @return the next nested object or <code>null</code>.
         */
        public/*<T>*/Object/*{T}*/get(String localName, String uri,
                Class/*<T>*/cls) throws XMLStreamException {
            if (uri == null)
                return get(localName, cls);

            if (!hasNext()// Asserts isReaderAtNext == true
                    || !_reader.getLocalName().equals(localName)
                    || !_reader.getNamespaceURI().equals(uri))
                return null;

            // Checks if reference.
            if (_referenceResolver != null) {
                Object obj = _referenceResolver.readReference(this);
                if (obj != null) {
                    if (_reader.next() != XMLStreamReader.END_ELEMENT)
                        throw new XMLStreamException("Non Empty Reference Element");
                    _isReaderAtNext = false;
                    return (Object/*{T}*/) obj;
                }
            }

            return (Object/*{T}*/) get(cls);
        }

        // Builds object of specified class.
        private Object get(Class cls) throws XMLStreamException {

            // Retrieves format.
            XMLFormat xmlFormat = _binding.getFormat(cls);

            // Creates object.
            _isReaderAtNext = false; // Makes attributes accessible.
            Object obj = xmlFormat.newInstance(cls, this);

            // Adds reference (before reading to support circular reference).
            if (_referenceResolver != null) {
                _referenceResolver.createReference(obj, this);
            }

            // Parses xml.
            xmlFormat.read(this, obj);
            if (hasNext()) // Asserts _isReaderAtNext == true
                throw new XMLStreamException("Incomplete element reading",
                        _reader.getLocation());
            _isReaderAtNext = false; // Skips end element.
            return obj;
        }

        /**
         * Returns the content of a text-only element (equivalent to 
         * {@link javolution.xml.stream.XMLStreamReader#getElementText 
         * getStreamReader().getElementText()}).
         *
         * @return the element text content or an empty sequence if none.
         */
        public CharArray getText() throws XMLStreamException {
            CharArray txt = _reader.getElementText();
            _isReaderAtNext = true; // End element is next.
            return txt;
        }

        /**
         * Returns the attributes for this XML input element.
         *
         * @return the attributes mapping.
         */
        public Attributes getAttributes() throws XMLStreamException {
            if (_isReaderAtNext)
                throw new XMLStreamException(
                        "Attributes should be read before content");
            return _reader.getAttributes();
        }

        /**
         * Searches for the attribute having the specified name.
         *
         * @param  name the name of the attribute.
         * @return the value for the specified attribute or <code>null</code>
         *         if the attribute is not found.
         */
        public CharArray getAttribute(String name) throws XMLStreamException {
            if (_isReaderAtNext)
                throw new XMLStreamException(
                        "Attributes should be read before reading content");
            return _reader.getAttributeValue(null, toCsq(name));
        }

        /**
         * Returns the specified <code>String</code> attribute.
         *
         * @param  name the name of the attribute.
         * @param  defaultValue a default value.
         * @return the value for the specified attribute or
         *         the <code>defaultValue</code> if the attribute is not found.
         */
        public String getAttribute(String name, String defaultValue)
                throws XMLStreamException {
            CharArray value = getAttribute(name);
            return (value != null) ? value.toString() : defaultValue;
        }

        /**
         * Returns the specified <code>boolean</code> attribute.
         *
         * @param  name the name of the attribute searched for.
         * @param  defaultValue the value returned if the attribute is not found.
         * @return the <code>boolean</code> value for the specified attribute or
         *         the default value if the attribute is not found.
         */
        public boolean getAttribute(String name, boolean defaultValue)
                throws XMLStreamException {
            CharArray value = getAttribute(name);
            return (value != null) ? value.toBoolean() : defaultValue;
        }

        /**
         * Returns the specified <code>int</code> attribute. This method handles
         * string formats that are used to represent octal and hexadecimal numbers.
         *
         * @param  name the name of the attribute searched for.
         * @param  defaultValue the value returned if the attribute is not found.
         * @return the <code>int</code> value for the specified attribute or
         *         the default value if the attribute is not found.
         */
        public int getAttribute(String name, int defaultValue)
                throws XMLStreamException {
            CharArray value = getAttribute(name);
            return (value != null) ? value.toInt() : defaultValue;
        }

        /**
         * Returns the specified <code>long</code> attribute. This method handles
         * string formats that are used to represent octal and hexadecimal numbers.
         *
         * @param  name the name of the attribute searched for.
         * @param  defaultValue the value returned if the attribute is not found.
         * @return the <code>long</code> value for the specified attribute or
         *         the default value if the attribute is not found.
         */
        public long getAttribute(String name, long defaultValue)
                throws XMLStreamException {
            CharArray value = getAttribute(name);
            return (value != null) ? value.toLong() : defaultValue;
        }

        /**
         * Returns the specified <code>float</code> attribute.
         *
         * @param  name the name of the attribute searched for.
         * @param  defaultValue the value returned if the attribute is not found.
         * @return the <code>float</code> value for the specified attribute or
         *         the default value if the attribute is not found.
         /*@JVM-1.1+@
         public float getAttribute(String name, float defaultValue) throws XMLStreamException {
         CharArray value = getAttribute(name);
         return (value != null) ? value.toFloat() : defaultValue;
         }
         /**/

        /**
         * Returns the specified <code>double</code> attribute.
         *
         * @param  name the name of the attribute searched for.
         * @param  defaultValue the value returned if the attribute is not found.
         * @return the <code>double</code> value for the specified attribute or
         *         the default value if the attribute is not found.
         /*@JVM-1.1+@
         public double getAttribute(String name, double defaultValue) throws XMLStreamException {
         CharArray value = getAttribute(name);
         return (value != null) ? value.toDouble() : defaultValue;
         }
         /**/

        ////////////////////////
        // Primitive Wrappers //
        ////////////////////////
        /**
         * Searches for the specified <code>Boolean</code> attribute.
         *
         * @param  name the name of the attribute.
         * @param  defaultValue the value returned if the attribute is not found.
         * @return the <code>Boolean</code> value for the specified attribute or
         *         the default value if the attribute is not found.
         */
        public Boolean getAttribute(String name, Boolean defaultValue)
                throws XMLStreamException {
            CharArray value = getAttribute(name);
            return (value != null) ? value.toBoolean() ? TRUE : FALSE
                    : defaultValue;
        }

        private static final Boolean TRUE = new Boolean(true);

        private static final Boolean FALSE = new Boolean(false);

        /**
         * Searches for the specified <code>Byte</code> attribute.
         *
         * @param  name the name of the attribute.
         * @param  defaultValue the value returned if the attribute is not found.
         * @return the <code>Byte</code> value for the specified attribute or
         *         the default value if the attribute is not found.
         */
        public Byte getAttribute(String name, Byte defaultValue)
                throws XMLStreamException {
            CharArray value = getAttribute(name);
            return (value != null) ? new Byte(TypeFormat.parseByte(value))
                    : defaultValue;
        }

        /**
         * Searches for the specified <code>Short</code> attribute.
         *
         * @param  name the name of the attribute.
         * @param  defaultValue the value returned if the attribute is not found.
         * @return the <code>Short</code> value for the specified attribute or
         *         the default value if the attribute is not found.
         */
        public Short getAttribute(String name, Short defaultValue)
                throws XMLStreamException {
            CharArray value = getAttribute(name);
            return (value != null) ? new Short(TypeFormat.parseShort(value))
                    : defaultValue;
        }

        /**
         * Searches for the specified <code>Integer</code> attribute.
         *
         * @param  name the name of the attribute.
         * @param  defaultValue the value returned if the attribute is not found.
         * @return the <code>Integer</code> value for the specified attribute or
         *         the default value if the attribute is not found.
         */
        public Integer getAttribute(String name, Integer defaultValue)
                throws XMLStreamException {
            CharArray value = getAttribute(name);
            return (value != null) ? new Integer(value.toInt()) : defaultValue;
        }

        /**
         * Searches for the specified <code>Long</code> attribute.
         *
         * @param  name the name of the attribute.
         * @param  defaultValue the value returned if the attribute is not found.
         * @return the <code>Long</code> value for the specified attribute or
         *         the default value if the attribute is not found.
         */
        public Long getAttribute(String name, Long defaultValue)
                throws XMLStreamException {
            CharArray value = getAttribute(name);
            return (value != null) ? new Long(value.toLong()) : defaultValue;
        }

        /**
         * Searches for the specified <code>Float</code> attribute.
         *
         * @param  name the name of the attribute.
         * @param  defaultValue the value returned if the attribute is not found.
         * @return the <code>Float</code> value for the specified attribute or
         *         the default value if the attribute is not found.
         /*@JVM-1.1+@
         public Float getAttribute(String name, Float defaultValue) throws XMLStreamException {
         CharArray value = getAttribute(name);
         return (value != null) ? new Float(value.toFloat()) : defaultValue;
         }
         /**/

        /**
         * Searches for the specified <code>Double</code> attribute.
         *
         * @param  name the name of the attribute.
         * @param  defaultValue the value returned if the attribute is not found.
         * @return the <code>Double</code> value for the specified attribute or
         *         the default value if the attribute is not found.
         /*@JVM-1.1+@
         public Double getAttribute(String name, Double defaultValue) throws XMLStreamException {
         CharArray value = getAttribute(name);
         return (value != null) ? new Double(value.toDouble()) : defaultValue;
         }
         /**/

        // Sets XML binding. 
        void setBinding(XMLBinding xmlBinding) {
            _binding = xmlBinding;
        }

        // Sets XML reference resolver. 
        void setReferenceResolver(XMLReferenceResolver xmlReferenceResolver) {
            _referenceResolver = xmlReferenceResolver;
        }

        // Resets for reuse.
        void reset() {
            _binding = XMLBinding.DEFAULT;
            _isReaderAtNext = false;
            _reader.reset();
            _referenceResolver = null;
        }
    }

    /**
     * This class represents an output XML element (marshalling).
     */
    protected static class OutputElement {

        /**
         * Holds the stream writer.
         */
        final XMLStreamWriterImpl _writer = new XMLStreamWriterImpl();

        /**
         * Holds the XML binding.
         */
        private XMLBinding _binding;

        /**
         * Holds the reference resolver.
         */
        private XMLReferenceResolver _referenceResolver;

        /**
         * Default constructor.
         */
        OutputElement() {
            reset();
        }

        /**
         * Returns the StAX-like stream writer (provides complete control over
         * the marshalling process).
         * 
         * @return the stream writer.
         */
        public XMLStreamWriter getStreamWriter() {
            return _writer;
        }

        /**
         * Adds the specified object or <code>null</code> as an anonymous 
         * nested element of unknown type. 
         *
         * @param obj the object added as nested element or <code>null</code>.
         */
        public void add(Object obj) throws XMLStreamException {
            if (obj == null) {
                _writer.writeEmptyElement(toCsq(NULL));
                return;
            }

            // Writes start element.
            Class cls = obj.getClass();
            String localName = _binding.getLocalName(cls);
            String uri = _binding.getURI(cls);
            if (uri == null) {
                _writer.writeStartElement(toCsq(localName));
            } else {
                _writer.writeStartElement(toCsq(uri),
                        toCsq(localName));
            }

            // Check if reference to be written.
            XMLFormat xmlFormat = _binding.getFormat(cls);
            if ((_referenceResolver != null) && xmlFormat.isReferenceable()
                    && _referenceResolver.writeReference(obj, this)) {
                _writer.writeEndElement();
                return; // Reference written.
            }

            xmlFormat.write(obj, this);
            _writer.writeEndElement();
        }

        /**
         * Adds the specified object as a named nested element of unknown type
         * (<code>null</code> objects are ignored).
         * The nested XML element contains a class attribute identifying
         * the object type.
         *
         * @param obj the object added as nested element or <code>null</code>.
         * @param name the name of the nested element.
         */
        public void add(Object obj, String name) throws XMLStreamException {
            if (obj == null)
                return;

            // Writes start element.
            _writer.writeStartElement(toCsq(name));

            // Writes class attribute.
            Class cls = obj.getClass();
            _binding.writeClassAttribute(_writer, cls);

            // Check if reference is to be written.
            XMLFormat xmlFormat = _binding.getFormat(cls);
            if ((_referenceResolver != null) && xmlFormat.isReferenceable()
                    && _referenceResolver.writeReference(obj, this)) {
                _writer.writeEndElement();
                return; // Reference written.
            }

            xmlFormat.write(obj, this);
            _writer.writeEndElement();
        }

        /**
         * Adds the specified object as a fully qualified nested element of 
         * unknown type (<code>null</code> objects are ignored). 
         * The nested XML element contains a class attribute identifying
         * the object type.
         *
         * @param obj the object added as nested element or <code>null</code>.
         * @param localName the local name of the nested element.
         * @param uri the namespace URI of the nested element.
         */
        public void add(Object obj, String localName, String uri)
                throws XMLStreamException {
            if (obj == null)
                return;

            // Writes start element.
            _writer.writeStartElement(toCsq(uri), toCsq(localName));

            // Writes class attribute.
            Class cls = obj.getClass();
            _binding.writeClassAttribute(_writer, cls);

            // Check if reference is to be written.
            XMLFormat xmlFormat = _binding.getFormat(cls);
            if ((_referenceResolver != null) && xmlFormat.isReferenceable()
                    && _referenceResolver.writeReference(obj, this)) {
                _writer.writeEndElement();
                return; // Reference written.
            }

            xmlFormat.write(obj, this);
            _writer.writeEndElement();
        }

        /**
         * Adds the specified object as a named nested element of specified  
         * actual type (<code>null</code> objects are ignored).
         * The nested XML element does not contain any class attribute.
         *
         * @param obj the object added as nested element or <code>null</code>.
         * @param name the name of the nested element.
         * @param cls the class identifying the format of the specified object.
         */
        public/*<T>*/void add(Object/*{T}*/ obj, String name, Class/*<T>*/ cls)
                throws XMLStreamException {
            if (obj == null)
                return;

            // Writes start element.
            _writer.writeStartElement(toCsq(name));

            // Check if reference is to be written.
            XMLFormat xmlFormat = _binding.getFormat(cls);
            if ((_referenceResolver != null) && xmlFormat.isReferenceable()
                    && _referenceResolver.writeReference(obj, this)) {
                _writer.writeEndElement();
                return; // Reference written.
            }

            xmlFormat.write(obj, this);
            _writer.writeEndElement();
        }

        /**
         * Adds the specified object as a fully qualified nested element of
         * specified actual type (<code>null</code> objects are ignored). 
         * The nested XML element does not contain any class attribute.
         *
         * @param obj the object added as nested element or <code>null</code>.
         * @param localName the local name of the nested element.
         * @param uri the namespace URI of the nested element.
         * @param cls the class identifying the format of the specified object.
         */
        public/*<T>*/void add(Object/*{T}*/obj, String localName, String uri,
                Class/*<T>*/cls) throws XMLStreamException {
            if (obj == null)
                return;

            // Writes start element.
            _writer.writeStartElement(toCsq(uri), toCsq(localName));

            // Check if reference is to be written.
            XMLFormat xmlFormat = _binding.getFormat(cls);
            if ((_referenceResolver != null) && xmlFormat.isReferenceable()
                    && _referenceResolver.writeReference(obj, this)) {
                _writer.writeEndElement();
                return; // Reference written.
            }

            xmlFormat.write(obj, this);
            _writer.writeEndElement();
        }

        /**
         * Adds the content of a text-only element (equivalent to {@link 
         * javolution.xml.stream.XMLStreamWriter#writeCharacters(CharSequence) 
         * getStreamWriter().writeCharacters(text)}).
         *
         * @param text the element text content or an empty sequence if none.
         */
        public void addText(CharSequence text) throws XMLStreamException {
            _writer.writeCharacters(text);
        }
        
        /**
         * Equivalent to {@link #addText(CharSequence)} 
         * (for J2ME compatibility).
         *
         * @param text the element text content or an empty sequence if none.
         */
        public void addText(String text) throws XMLStreamException {
            _writer.writeCharacters(toCsq(text));
        }

        /**
         * Sets the specified <code>CharSequence</code> attribute
         * (<code>null</code> values are ignored).
         *
         * @param  name the attribute name.
         * @param  value the attribute value or <code>null</code>.
         */
        public void setAttribute(String name, CharSequence value)
                throws XMLStreamException {
            if (value == null)
                return;
            _writer.writeAttribute(toCsq(name), value);
        }

        /**
         * Sets the specified <code>String</code> attribute
         * (<code>null</code> values are ignored).
         *
         * @param  name the attribute name.
         * @param  value the attribute value.
         */
        public void setAttribute(String name, String value)
                throws XMLStreamException {
            if (value == null)
                return;
            _writer.writeAttribute(toCsq(name),
                    toCsq(value));
        }

        /**
         * Sets the specified <code>boolean</code> attribute.
         * 
         * @param  name the attribute name.
         * @param  value the <code>boolean</code> value for the specified attribute.
         */
        public void setAttribute(String name, boolean value)
                throws XMLStreamException {
            setAttribute(name, _tmpTextBuilder.clear().append(value));
        }

        private TextBuilder _tmpTextBuilder = new TextBuilder();

        /**
         * Sets the specified <code>int</code> attribute.
         * 
         * @param  name the attribute name.
         * @param  value the <code>int</code> value for the specified attribute.
         */
        public void setAttribute(String name, int value)
                throws XMLStreamException {
            setAttribute(name, _tmpTextBuilder.clear().append(value));
        }

        /**
         * Sets the specified <code>long</code> attribute.
         * 
         * @param  name the attribute name.
         * @param  value the <code>long</code> value for the specified attribute.
         */
        public void setAttribute(String name, long value)
                throws XMLStreamException {
            setAttribute(name, _tmpTextBuilder.clear().append(value));
        }

        /**
         * Sets the specified <code>float</code> attribute.
         * 
         * @param  name the attribute name.
         * @param  value the <code>float</code> value for the specified attribute.
         /*@JVM-1.1+@
         public void setAttribute(String name, float value) throws XMLStreamException {
         setAttribute(name, _tmpTextBuilder.clear().append(value));
         }
         /**/

        /**
         * Sets the specified <code>double</code> attribute.
         * 
         * @param  name the attribute name.
         * @param  value the <code>double</code> value for the specified attribute.
         /*@JVM-1.1+@
         public void setAttribute(String name, double value) throws XMLStreamException {
         setAttribute(name, _tmpTextBuilder.clear().append(value));
         }
         /**/

        ////////////////////////
        // Primitive Wrappers //
        ////////////////////////
        /**
         * Sets the specified <code>Boolean</code> attribute.
         * 
         * @param  name the name of the attribute.
         * @param  value the <code>Boolean</code> value for the specified attribute
         *         or <code>null</code> in which case the attribute is not set.
         */
        public void setAttribute(String name, Boolean value)
                throws XMLStreamException {
            if (value == null)
                return;
            setAttribute(name, value.booleanValue());
        }

        /**
         * Sets the specified <code>Byte</code> attribute.
         * 
         * @param  name the name of the attribute.
         * @param  value the <code>Byte</code> value for the specified attribute
         *         or <code>null</code> in which case the attribute is not set.
         */
        public void setAttribute(String name, Byte value)
                throws XMLStreamException {
            if (value == null)
                return;
            setAttribute(name, value.byteValue());
        }

        /**
         * Sets the specified <code>Short</code> attribute.
         *
         * @param  name the name of the attribute.
         * @param  value the <code>Short</code> value for the specified attribute
         *         or <code>null</code> in which case the attribute is not set.
         */
        public void setAttribute(String name, Short value)
                throws XMLStreamException {
            if (value == null)
                return;
            setAttribute(name, value.shortValue());
        }

        /**
         * Sets the specified <code>Integer</code> attribute.
         * 
         * @param  name the name of the attribute.
         * @param  value the <code>Integer</code> value for the specified attribute
         *         or <code>null</code> in which case the attribute is not set.
         */
        public void setAttribute(String name, Integer value)
                throws XMLStreamException {
            if (value == null)
                return;
            setAttribute(name, value.intValue());
        }

        /**
         * Sets the specified <code>Long</code> attribute.
         *
         * @param  name the name of the attribute.
         * @param  value the <code>Long</code> value for the specified attribute
         *         or <code>null</code> in which case the attribute is not set.
         */
        public void setAttribute(String name, Long value)
                throws XMLStreamException {
            if (value == null)
                return;
            setAttribute(name, value.longValue());
        }

        /**
         * Sets the specified <code>Float</code> attribute.
         *
         * @param  name the name of the attribute.
         * @param  value the <code>Float</code> value for the specified attribute
         *         or <code>null</code> in which case the attribute is not set.
         /*@JVM-1.1+@
         public void setAttribute(String name, Float value) throws XMLStreamException {
         if (value == null)
         return;
         setAttribute(name, value.floatValue());
         }
         /**/

        /**
         * Sets the specified <code>Double</code> attribute.
         *
         * @param  name the name of the attribute.
         * @param  value the <code>Double</code> value for the specified attribute
         *         or <code>null</code> in which case the attribute is not set.
         /*@JVM-1.1+@
         public void setAttribute(String name, Double value) throws XMLStreamException {
         if (value == null)
         return;
         }
         /**/

        // Sets XML binding. 
        void setBinding(XMLBinding xmlBinding) {
            _binding = xmlBinding;
        }

        // Sets XML reference resolver. 
        void setReferenceResolver(XMLReferenceResolver xmlReferenceResolver) {
            _referenceResolver = xmlReferenceResolver;
        }

        // Resets for reuse.
        void reset() {
            _binding = XMLBinding.DEFAULT;
            _writer.reset();
            _writer.setRepairingNamespaces(true);
            _writer.setAutomaticEmptyElements(true);
            _referenceResolver = null;
        }

    }
    
    private static CharSequence toCsq/**/(Object str) {
        return Javolution.j2meToCharSeq(str);
    }

    /**
     * Creates an unmapped XML format.
     * 
     * @deprecated <code>XMLFormat(null) should be used instead.
     */
    protected XMLFormat() {
        this(null);
    }
}