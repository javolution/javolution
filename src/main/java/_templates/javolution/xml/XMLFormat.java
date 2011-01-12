/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2006 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package _templates.javolution.xml;

import _templates.java.lang.CharSequence;
import _templates.javolution.lang.Reflection;
import _templates.javolution.text.CharArray;
import _templates.javolution.text.Text;
import _templates.javolution.text.TextBuilder;
import _templates.javolution.text.TextFormat;
import _templates.javolution.xml.sax.Attributes;
import _templates.javolution.xml.stream.XMLStreamException;
import _templates.javolution.xml.stream.XMLStreamReader;
import _templates.javolution.xml.stream.XMLStreamReaderImpl;
import _templates.javolution.xml.stream.XMLStreamWriter;
import _templates.javolution.xml.stream.XMLStreamWriterImpl;

/**
 * <p> This class represents the format base class for XML serialization and
 *     deserialization.</p>
 *     
 * <p> Application classes typically define a default XML format for their 
 *     instances using protected static {@link XMLFormat} class members.
 *     Formats are inherited by sub-classes. For example:[code]
 *     
 *     public abstract class Graphic implements XMLSerializable {
 *         private boolean _isVisible;
 *         private Paint _paint; // null if none.
 *         private Stroke _stroke; // null if none.
 *         private Transform _transform; // null if none.
 *          
 *         // XML format with positional associations (members identified by their position),
 *         // see XML package description for examples of name associations.
 *         protected static final XMLFormat<Graphic> GRAPHIC_XML = new XMLFormat<Graphic>(Graphic.class) {
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
 * <p> The mapping between classes and XML formats can be overriden
 *     through {@link XMLBinding} instances.
 *     Here is an example of serialization/deserialization:[code]
 *     
 *     // Creates a list holding diverse objects.
 *     List list = new ArrayList();
 *     list.add("John Doe");
 *     list.add(null);
 *     Map map = new FastMap();
 *     map.put("ONE", 1);
 *     map.put("TWO", 2);
 *     list.add(map);
 *     
 *     // Use of custom binding.
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
 * <p> <i>Note:</i> Any type for which a text format is 
 *    {@link TextFormat#getInstance known} can be represented as 
 *    a XML attribute.</p>
 * 
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 5.4, December 1, 2009
 */
public abstract class XMLFormat/*<T>*/ {

    /**
     * Holds <code>null</code> representation.
     */
    private static final String NULL = "Null";

    /**
     * Holds the class associated to this format (static instances)
     * or <code>null</code> if format is unbound.
     */
    private final Class/*<T>*/ _class;

    /**
     * Defines the default XML format bound to the specified class.
     * If the specified class is <code>null</code> then the format is unbound
     * (unbound formats are used by custom {@link XMLBinding binding} instances).
     * The static binding is unique and can only be overriden by custom
     * {@link XMLBinding}. For example:[code]
     *    // Overrides default binding for java.util.Collection.
     *    class MyBinding extends XMLBinding {
     *        XMLFormat<Collection> collectionXML = new XMLFormat<Collection>(null) { ... }; // Unbound.
     *        public XMLFormat getFormat(Class cls) {
     *            if (Collection.isAssignableFrom(cls)) {
     *                return collectionXML; // Overrides default XML format.
     *            } else {
     *                return super.getFormat(cls);
     *            }
     *        }
     *    }[/code]
     * 
     * @param forClass the root class/interface to associate to this XML format
     *        or <code>null</code> if this format is not bound.
     * @throws IllegalArgumentException if a XMLFormat is already bound to 
     *         the specified class.
     */
    protected XMLFormat(Class/*<T>*/ forClass) {
        _class = forClass;
        if (forClass == null)
            return; // Dynamic format.
        Reflection.getInstance().setField(this, forClass, XMLFormat.class);
    }

    /**
     * <p> Returns the default format for the specified class/interface.
     *     If there no direct mapping for the specified class, the mapping
     *     for the specified class interfaces is searched, if none is found
     *     the mapping for the parents classes is searched, if still none is
     *     found the format for <code>java.lang.Object</code> is returned.</p>
     *
     * <p> A default xml format exists for the following predefined types:
     *     <code><ul>
     *       <li>java.lang.Object</li>
     *       <li>java.util.Collection</li>
     *       <li>java.util.Map</li>
     *    </ul></code>
     *    The default XML representation (java.lang.Object) consists of the
     *    of a "value" attribute holding its textual representation
     *    (see {@link TextFormat#getInstance}).</p>
     *
     * @return the class/interface bound to this format.
     */
    public static /*<T>*/ XMLFormat/*<T>*/ getInstance(Class/*<? extends T>*/ forClass) {
        XMLFormat objectFormat = XMLBinding.OBJECT_XML; // Also forces initialization or XMLBinding.
        XMLFormat xmlFormat = (XMLFormat) Reflection.getInstance().getField(forClass, XMLFormat.class, true);
        return (xmlFormat != null) ? xmlFormat : objectFormat;
    }

    /**
     * Returns the class/interface statically bound to this format or 
     * <code>null</code> if none.
     * 
     * @return the class/interface bound to this format.
     */
    public final Class/*<T>*/ getBoundClass() {
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
    public Object/*{T}*/ newInstance(Class/*<T>*/ cls, InputElement xml)
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
    public abstract void write(Object/*{T}*/ obj, OutputElement xml)
            throws XMLStreamException;

    /**
     * Parses an XML input element into the specified object. 
     * 
     * @param xml the XML element to parse.
     * @param obj the object created through {@link #newInstance}
     *        and to setup from the specified XML element.
     */
    public abstract void read(InputElement xml, Object/*{T}*/ obj)
            throws XMLStreamException;

    /**
     * Returns textual information about this format.
     *
     * @return this format textual information.
     */
    public String toString() {
        Class boundClass = getBoundClass();
        return (boundClass != null)
                ? "Default XMLFormat for " + boundClass.getName()
                : "Dynamic XMLtFormat (" + this.hashCode() + ")";
    }

    /**
     * This class represents an input XML element (unmarshalling).
     */
    public static final class InputElement {

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
         * and URI of the current XML element.
         *
         * @return the next nested object which can be <code>null</code>.
         * @throws XMLStreamException if <code>hasNext() == false</code>.
         */
        public/*<T>*/ Object/*{T}*/ getNext() throws XMLStreamException {
            if (!hasNext()) // Asserts isReaderAtNext == true
                throw new XMLStreamException("No more element to read", _reader.getLocation());

            // Checks for null.
            if (_reader.getLocalName().equals(NULL)) {
                if (_reader.next() != XMLStreamReader.END_ELEMENT)
                    throw new XMLStreamException("Non Empty Null Element");
                _isReaderAtNext = false;
                return null;
            }

            Object ref = readReference();
            if (ref != null)
                return (Object/*{T}*/) ref;

            // Retrieves object's class from element tag.
            Class cls = _binding.readClass(_reader, false);
            return (Object/*{T}*/) readInstanceOf(cls);
        }

        /**
         * Returns the object whose type is identified by a XML class attribute
         * only if the XML element has the specified local name.
         *
         * @param name the local name of the next element.
         * @return the next nested object or <code>null</code>.
         */
        public/*<T>*/ Object/*{T}*/ get(String name) throws XMLStreamException {
            if (!hasNext()// Asserts isReaderAtNext == true
                    || !_reader.getLocalName().equals(name))
                return null;

            Object ref = readReference();
            if (ref != null)
                return (Object/*{T}*/) ref;

            // Retrieves object's class from class attribute.
            Class cls = _binding.readClass(_reader, true);
            return (Object/*{T}*/) readInstanceOf(cls);
        }

        /**
         * Returns the object whose type is identified by a XML class attribute
         * only if the XML element has the specified local name and URI.
         *
         * @param localName the local name.
         * @param uri the namespace URI or <code>null</code>.
         * @return the next nested object or <code>null</code>.
         */
        public/*<T>*/ Object/*{T}*/ get(String localName, String uri)
                throws XMLStreamException {
            if (uri == null)
                return (Object/*{T}*/) get(localName);

            if (!hasNext()// Asserts isReaderAtNext == true
                    || !_reader.getLocalName().equals(localName) || !_reader.getNamespaceURI().equals(uri))
                return null;

            Object ref = readReference();
            if (ref != null)
                return (Object/*{T}*/) ref;

            // Retrieves object's class from class attribute.
            Class cls = _binding.readClass(_reader, true);
            return (Object/*{T}*/) readInstanceOf(cls);
        }

        /**
         * Returns the object of specified type only if the XML element has the
         * specified local name. 
         *      
         * @param name the local name of the element to match.
         * @param cls the class identifying the format of the object to return.
         * @return the next nested object or <code>null</code>.
         */
        public/*<T>*/ Object/*{T}*/ get(String name, Class/*<T>*/ cls)
                throws XMLStreamException {
            if (!hasNext()// Asserts isReaderAtNext == true
                    || !_reader.getLocalName().equals(name))
                return null;

            Object ref = readReference();
            if (ref != null)
                return (Object/*{T}*/) ref;

            return (Object/*{T}*/) readInstanceOf(cls);
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
        public/*<T>*/ Object/*{T}*/ get(String localName, String uri,
                Class/*<T>*/ cls) throws XMLStreamException {
            if (uri == null)
                return get(localName, cls);

            if (!hasNext()// Asserts isReaderAtNext == true
                    || !_reader.getLocalName().equals(localName) || !_reader.getNamespaceURI().equals(uri))
                return null;

            Object ref = readReference();
            if (ref != null)
                return (Object/*{T}*/) ref;

            return (Object/*{T}*/) readInstanceOf(cls);
        }

        // Returns the referenced object if any.
        private Object readReference() throws XMLStreamException {
            if (_referenceResolver == null)
                return null;
            Object ref = _referenceResolver.readReference(this);
            if (ref == null)
                return null;
            if (_reader.next() != XMLStreamReader.END_ELEMENT)
                throw new XMLStreamException("Non Empty Reference Element");
            _isReaderAtNext = false;
            return ref;
        }

        // Builds object of specified class.
        private Object readInstanceOf(Class cls) throws XMLStreamException {

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
         * {@link _templates.javolution.xml.stream.XMLStreamReader#getElementText 
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
         * Returns the specified <code>char</code> attribute.
         *
         * @param  name the name of the attribute searched for.
         * @param  defaultValue the value returned if the attribute is not found.
         * @return the <code>char</code> value for the specified attribute or
         *         the default value if the attribute is not found.
         */
        public char getAttribute(String name, char defaultValue)
                throws XMLStreamException {
            CharArray value = getAttribute(name);
            if (value == null)
                return defaultValue;
            if (value.length() != 1)
                throw new XMLStreamException(
                        "Single character expected (read '" + value + "')");
            return value.charAt(0);
        }

        /**
         * Returns the specified <code>byte</code> attribute. This method handles
         * string formats that are used to represent octal and hexadecimal numbers.
         *
         * @param  name the name of the attribute searched for.
         * @param  defaultValue the value returned if the attribute is not found.
         * @return the <code>byte</code> value for the specified attribute or
         *         the default value if the attribute is not found.
         */
        public byte getAttribute(String name, byte defaultValue)
                throws XMLStreamException {
            CharArray value = getAttribute(name);
            return (value != null) ? (byte) value.toInt() : defaultValue;
        }

        /**
         * Returns the specified <code>short</code> attribute. This method handles
         * string formats that are used to represent octal and hexadecimal numbers.
         *
         * @param  name the name of the attribute searched for.
         * @param  defaultValue the value returned if the attribute is not found.
         * @return the <code>short</code> value for the specified attribute or
         *         the default value if the attribute is not found.
         */
        public short getAttribute(String name, short defaultValue)
                throws XMLStreamException {
            CharArray value = getAttribute(name);
            return (value != null) ? (short) value.toInt() : defaultValue;
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
         */
        public float getAttribute(String name, float defaultValue)
                throws XMLStreamException {
            CharArray value = getAttribute(name);
            return (value != null) ? value.toFloat() : defaultValue;
        }

        /**
         * Returns the specified <code>double</code> attribute.
         *
         * @param  name the name of the attribute searched for.
         * @param  defaultValue the value returned if the attribute is not found.
         * @return the <code>double</code> value for the specified attribute or
         *         the default value if the attribute is not found.
         */
        public double getAttribute(String name, double defaultValue)
                throws XMLStreamException {
            CharArray value = getAttribute(name);
            return (value != null) ? value.toDouble() : defaultValue;
        }

        /**
         * Returns the attribute of same type as the specified
         * default value. The default value 
         * {@link _templates.javolution.text.TextFormat#getInstance TextFormat} is
         * used to parse the attribute value.
         *
         * @param  name the name of the attribute.
         * @param  defaultValue the value returned if the attribute is not found.
         * @return the parse value for the specified attribute or
         *         the default value if the attribute is not found.
         */
        public/*<T>*/ Object/*{T}*/ getAttribute(String name,
                Object/*{T}*/ defaultValue) throws XMLStreamException {
            CharArray value = getAttribute(name);
            if (value == null)
                return defaultValue;
            // Parses attribute value.
            Class type = defaultValue.getClass();
            TextFormat format = TextFormat.getInstance(type);
            if (!format.isParsingSupported())
                throw new XMLStreamException("No TextFormat instance for " + type);
            return (Object/*{T}*/) format.parse(value);
        }

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
    public static final class OutputElement {

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
            _binding.writeClass(obj.getClass(), _writer, false);

            // Checks if reference written.
            XMLFormat xmlFormat = _binding.getFormat(cls);
            if (xmlFormat.isReferenceable() && writeReference(obj))
                return;

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
            _binding.writeClass(cls, _writer, true);

            // Checks if reference written.
            XMLFormat xmlFormat = _binding.getFormat(cls);
            if (xmlFormat.isReferenceable() && writeReference(obj))
                return;

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
            _binding.writeClass(cls, _writer, true);

            // Checks if reference written.
            XMLFormat xmlFormat = _binding.getFormat(cls);
            if (xmlFormat.isReferenceable() && writeReference(obj))
                return;

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
        public/*<T>*/ void add(Object/*{T}*/ obj, String name, Class/*<T>*/ cls)
                throws XMLStreamException {
            if (obj == null)
                return;

            // Writes start element.
            _writer.writeStartElement(toCsq(name));

            // Checks if reference written.
            XMLFormat xmlFormat = _binding.getFormat(cls);
            if (xmlFormat.isReferenceable() && writeReference(obj))
                return;

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
        public/*<T>*/ void add(Object/*{T}*/ obj, String localName, String uri,
                Class/*<T>*/ cls) throws XMLStreamException {
            if (obj == null)
                return;

            // Writes start element.
            _writer.writeStartElement(toCsq(uri), toCsq(localName));

            // Checks if reference written.
            XMLFormat xmlFormat = _binding.getFormat(cls);
            if (xmlFormat.isReferenceable() && writeReference(obj))
                return;

            xmlFormat.write(obj, this);
            _writer.writeEndElement();
        }

        // Returns true if reference written.
        private boolean writeReference(Object obj) throws XMLStreamException {
            if ((_referenceResolver == null) || !_referenceResolver.writeReference(obj, this))
                return false;
            _writer.writeEndElement();
            return true; // Reference written.
        }

        /**
         * Adds the content of a text-only element (equivalent to {@link 
         * _templates.javolution.xml.stream.XMLStreamWriter#writeCharacters(CharSequence) 
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
            _writer.writeAttribute(toCsq(name), toCsq(value));
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
         * Sets the specified <code>char</code> attribute.
         * 
         * @param  name the attribute name.
         * @param  value the <code>char</code> value for the specified attribute.
         */
        public void setAttribute(String name, char value)
                throws XMLStreamException {
            setAttribute(name, (TextBuilder) _tmpTextBuilder.clear().append(
                    value));
        }

        /**
         * Sets the specified <code>byte</code> attribute.
         * 
         * @param  name the attribute name.
         * @param  value the <code>byte</code> value for the specified attribute.
         */
        public void setAttribute(String name, byte value)
                throws XMLStreamException {
            setAttribute(name, _tmpTextBuilder.clear().append(value));
        }

        /**
         * Sets the specified <code>short</code> attribute.
         * 
         * @param  name the attribute name.
         * @param  value the <code>short</code> value for the specified attribute.
         */
        public void setAttribute(String name, short value)
                throws XMLStreamException {
            setAttribute(name, _tmpTextBuilder.clear().append(value));
        }

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
         */
        public void setAttribute(String name, float value)
                throws XMLStreamException {
            setAttribute(name, _tmpTextBuilder.clear().append(value));
        }

        /**
         * Sets the specified <code>double</code> attribute.
         * 
         * @param  name the attribute name.
         * @param  value the <code>double</code> value for the specified attribute.
         */
        public void setAttribute(String name, double value)
                throws XMLStreamException {
            setAttribute(name, _tmpTextBuilder.clear().append(value));
        }

        /**
         * Sets the specified attribute using its associated 
         * {@link _templates.javolution.text.TextFormat#getInstance TextFormat}.
         * 
         * @param  name the name of the attribute.
         * @param  value the <code>Boolean</code> value for the specified attribute
         *         or <code>null</code> in which case the attribute is not set.
         */
        public void setAttribute(String name, Object value)
                throws XMLStreamException {
            if (value == null)
                return;
            Class type = value.getClass();
            TextFormat format = TextFormat.getInstance(type);
            setAttribute(name, (TextBuilder) format.format(value,
                    _tmpTextBuilder.clear()));
        }

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

    // For J2ME Compatibility.
    private static CharSequence toCsq(Object str) {
        /*@JVM-1.4+@
        if (true) return (CharSequence) str;
        /**/
        return str == null ? null : Text.valueOf(str);
    }
}
