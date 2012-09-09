/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2006 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.xml.stream;

import java.io.OutputStream;
import java.io.Writer;

import javolution.context.ObjectFactory;
import javolution.lang.Configurable;


/**
 * <p> The class represents the factory for getting {@link XMLStreamWriter}
 *     intances.</p>
 * 
 * <p> The {@link #newInstance() default implementation} automatically 
 *     {@link ObjectFactory#recycle recycles} any writer which has been 
 *     {@link XMLStreamWriter#close() closed}.</p>
 *     
 * <P> Usage example:[code]
 *
 *     // Lets format to an appendable.
 *     TextBuilder xml = new TextBuilder();
 *     AppendableWriter out = new AppendableWriter(xml);
 *     
 *     // Creates a factory producing writers using tab indentation.
 *     XMLOutpuFactory factory = XMLOutputFactory.newInstance();
 *     factory.setProperty(XMLOutputFactory.INDENTATION, "/t");
 *     
 *     // Creates a new writer (potentially recycled).
 *     XMLStreamWriter writer = factory.createXMLStreamReader(out);
 *     
 *     // Formats to XML.
 *     writer.writeStartDocument();
 *     writer.writeStartElement(...); 
 *     ...
 *     writer.close(); // Automatically recycles this writer. 
 *     out.close(); // Underlying output should be closed explicitly.
 *     
 *     // Displays the formatted output.
 *     System.out.println(xml);
 *     [/code]</p>
 *     
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 4.0, September 4, 2006
 */
public abstract class XMLOutputFactory {

    /**
     * Holds the XMLOutputFactory implementation (configurable).
     */
    public static final Configurable <Class<? extends XMLOutputFactory>> CLASS
        = new Configurable <Class<? extends XMLOutputFactory>> (Default.class) {};

    /**
     * Property used to set prefix defaulting on the output side
     * (type: <code>Boolean</code>, default: <code>FALSE</code>).
     */
    public static final String IS_REPAIRING_NAMESPACES = "javolution.xml.stream.isRepairingNamespaces";

    /**
     * Property used to specify the prefix to be appended by a trailing
     * part (a sequence number) in order to make it unique to be usable as
     * a temporary non-colliding prefix when repairing namespaces
     * (type: <code>String</code>, default: <code>"ns"</code>).
     */
    public final static String REPAIRING_PREFIX = "javolution.xml.stream.repairingPrefix";

    /**
     * Property used to specify an indentation string; non-null indentation 
     * forces the writer to write elements into separate lines
     * (type: <code>String</code>, default: <code>null</code>).
     */
    public static final String INDENTATION = "javolution.xml.stream.indentation";

     /**
     * Property used to specify the new line characters
     * (type: <code>String</code>, default: <code>"\n"</code>).
     */
    public static final String LINE_SEPARATOR = "javolution.xml.stream.lineSeparator";

    /**
     * Property indicating if the stream writers are allowed to automatically 
     * output empty elements when a start element is immediately followed by
     * matching end element
     * (type: <code>Boolean</code>, default: <code>FALSE</code>).
     */
    public final static String AUTOMATIC_EMPTY_ELEMENTS = "javolution.xml.stream.automaticEmptyElements";

    /**
     * Property indicating if the stream writers are not allowed to use 
     * empty element tags 
     * (type: <code>Boolean</code>, default: <code>FALSE</code>).
     * When set, this property forces the use of start/end element tag 
     * (e.g. i.e. "&lt;empty /&gt;" replaced by  "&lt;empty&gt;&lt;/empty&gt;"),
     * This property takes precedence over {@link #AUTOMATIC_EMPTY_ELEMENTS}.
     */
    public final static String NO_EMPTY_ELEMENT_TAG = "javolution.xml.stream.noEmptyElementTag";

    /**
     * Default constructor.
     */
    protected XMLOutputFactory() {
    }


    /**
     * Returns a new instance of the {@link #CLASS} output factory
     * implementation which may be configurated by the user 
     * (see {@link #setProperty(String, Object)}). The output factory
     * instance is allocated through {@link ObjectFactory#getInstance(Class)}.
     * 
     * @return a new factory instance.
     */
    public static XMLOutputFactory newInstance() {
        Class cls = (Class) CLASS.get();
        return (XMLOutputFactory) ObjectFactory.getInstance(cls).object();
    }

    /**
     * Returns a XML stream writer to the specified i/o writer.
     * 
     * @param writer the writer to write to.
     * @throws XMLStreamException
     */
    public abstract XMLStreamWriter createXMLStreamWriter(Writer writer)
            throws XMLStreamException;

    /**
     * Returns a XML stream writer to the specified output stream (UTF-8
     * encoding).
     * 
     * @param stream the stream to write to.
     * @throws XMLStreamException
     */
    public abstract XMLStreamWriter createXMLStreamWriter(OutputStream stream)
            throws XMLStreamException;

    /**
     * Returns a XML stream writer to the specified output stream using the
     * specified encoding.
     * 
     * @param stream the stream to write to.
     * @param encoding the encoding to use.
     * @throws XMLStreamException
     */
    public abstract XMLStreamWriter createXMLStreamWriter(OutputStream stream,
            String encoding) throws XMLStreamException;

    /**
     * Allows the user to set specific features/properties on the underlying
     * implementation.
     * 
     * @param name the name of the property.
     * @param value  the value of the property.
     * @throws IllegalArgumentException if the property is not supported.
     */
    public abstract void setProperty(String name, Object value)
            throws IllegalArgumentException;

    /**
     * Gets a feature/property on the underlying implementation.
     * 
     * @param name the name of the property
     * @return the value of the property
     * @throws IllegalArgumentException if the property is not supported.
     */
    public abstract Object getProperty(String name)
            throws IllegalArgumentException;

    /**
     * Queries the set of properties that this factory supports.
     * 
     * @param name the name of the property (may not be null).
     * @return <code>true</code> if the property is supported;
     *         <code>false</code> otherwise.
     */
    public abstract boolean isPropertySupported(String name);

    /**
     * This class represents the default implementation.
     */
    private static final class Default extends XMLOutputFactory {

        // Property setting.
        private Boolean _isRepairingNamespaces = Boolean.FALSE;

        // Property setting.
        private String _repairingPrefix = "ns";

        // Property setting.
        private Boolean _automaticEmptyElements = Boolean.FALSE;

        // Property setting.
        private Boolean _noEmptyElementTag = Boolean.FALSE;

        // Property setting.
        private String _indentation;

        // Property setting.
        private String _lineSeparator = "\n";

        // Implements XMLOutputFactory abstract method.
        public XMLStreamWriter createXMLStreamWriter(Writer writer)
                throws XMLStreamException {
            XMLStreamWriterImpl xmlWriter = newWriter();
            xmlWriter.setOutput(writer);
            return xmlWriter;
        }

        // Implements XMLOutputFactory abstract method.
        public XMLStreamWriter createXMLStreamWriter(OutputStream stream)
                throws XMLStreamException {
            XMLStreamWriterImpl xmlWriter = newWriter();
            xmlWriter.setOutput(stream);
            return xmlWriter;
        }

        // Implements XMLOutputFactory abstract method.
        public XMLStreamWriter createXMLStreamWriter(OutputStream stream,
                String encoding) throws XMLStreamException {
            if ((encoding == null) || encoding.equals("UTF-8")
                    || encoding.equals("utf-8"))
                return createXMLStreamWriter(stream);
            XMLStreamWriterImpl xmlWriter = newWriter();
            xmlWriter.setOutput(stream, encoding);
            return xmlWriter;
        }

        private XMLStreamWriterImpl newWriter() {
            XMLStreamWriterImpl xmlWriter = (XMLStreamWriterImpl) XML_WRITER_FACTORY
                    .object();
            xmlWriter._objectFactory = XML_WRITER_FACTORY;
            xmlWriter.setRepairingNamespaces(_isRepairingNamespaces
                    .booleanValue());
            xmlWriter.setRepairingPrefix(_repairingPrefix);
            xmlWriter.setIndentation(_indentation);
            xmlWriter.setLineSeparator(_lineSeparator);
            xmlWriter.setAutomaticEmptyElements(_automaticEmptyElements
                    .booleanValue());
            xmlWriter.setNoEmptyElementTag(_noEmptyElementTag
                    .booleanValue());
            return xmlWriter;
        }

        // Implements XMLOutputFactory abstract method.
        public void setProperty(String name, Object value)
                throws IllegalArgumentException {
            if (name.equals(IS_REPAIRING_NAMESPACES)) {
                _isRepairingNamespaces = (Boolean) value;
            } else if (name.equals(REPAIRING_PREFIX)) {
                _repairingPrefix = (String) value;
            } else if (name.equals(AUTOMATIC_EMPTY_ELEMENTS)) {
                _automaticEmptyElements = (Boolean) value;
            } else if (name.equals(NO_EMPTY_ELEMENT_TAG)) {
                _noEmptyElementTag = (Boolean) value;
            } else if (name.equals(INDENTATION)) {
                _indentation = (String) value;
            } else if (name.equals(LINE_SEPARATOR)) {
                _lineSeparator = (String) value;
            } else {
                throw new IllegalArgumentException("Property: " + name
                        + " not supported");
            }
        }

        // Implements XMLOutputFactory abstract method.
        public Object getProperty(String name) throws IllegalArgumentException {
            if (name.equals(IS_REPAIRING_NAMESPACES)) {
                return _isRepairingNamespaces;
            } else if (name.equals(REPAIRING_PREFIX)) {
                return _repairingPrefix;
            } else if (name.equals(AUTOMATIC_EMPTY_ELEMENTS)) {
                return _automaticEmptyElements;
            } else if (name.equals(NO_EMPTY_ELEMENT_TAG)) {
                return _noEmptyElementTag;
            } else if (name.equals(INDENTATION)) {
                return _indentation;
            } else if (name.equals(LINE_SEPARATOR)) {
                return _lineSeparator;
            } else {
                throw new IllegalArgumentException("Property: " + name
                        + " not supported");
            }
        }

        // Implements XMLOutputFactory abstract method.
        public boolean isPropertySupported(String name) {
            return name.equals(IS_REPAIRING_NAMESPACES)
                    || name.equals(REPAIRING_PREFIX)
                    || name.equals(AUTOMATIC_EMPTY_ELEMENTS)
                    || name.equals(NO_EMPTY_ELEMENT_TAG)
                    || name.equals(INDENTATION)
                    || name.equals(LINE_SEPARATOR);
        }

    }

    /**
     * Holds a factory producing reusable writer instances.
     */
    private static final ObjectFactory XML_WRITER_FACTORY = new ObjectFactory() {

        protected Object create() {
            return new XMLStreamWriterImpl();
        }

        protected void cleanup(Object obj) {
            ((XMLStreamWriterImpl) obj).reset();
        }

    };

    // Allows instances of private classes to be factory produced. 
    static {
        ObjectFactory.setInstance(new ObjectFactory() {
            protected Object create() {
                return new Default();
            } }, Default.class);
    }
}