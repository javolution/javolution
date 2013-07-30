/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.xml.stream;

import java.io.OutputStream;
import java.io.Writer;

import javolution.lang.Copyable;
import javolution.lang.Parallelizable;

/**
 * <p> The OSGi factory service to create {@link XMLStreamWriter} instances.
 *     For each bundle, a distinct factory instance is returned and can be 
 *     individually configured.
 * [code]
 * import javolution.xml.stream.*;
 * public class Activator implements BundleActivator { 
 *     public void start(BundleContext bc) throws Exception {
 *     
 *         // Configures factory. 
 *         ServiceTracker<XMLOutputFactory, XMLOutputFactory> tracker 
 *             = new ServiceTracker<>(bc, XMLOutputFactory.class, null);
 *         tracker.open();
 *         tracker.getService().setProperty(XMLOutputFactory.INDENTATION, "/t"); // Use tab for indentations.
 *         
 *         // Instantiates a new writer.
 *         TextBuilder xml = new TextBuilder();
 *         AppendableWriter out = new AppendableWriter(xml);
 *         XMLStreamWriter writer = tracker.getService().createXMLStreamWriter(out);
 *     
 *         // Formats to XML.
 *         writer.writeStartDocument("1.0");
 *         writer.writeCharacters("\n");
 *         writer.writeStartElement("ns1", "sample", "http://www.e.com/ns1");
 *         writer.writeNamespace("ns1", "http://www.e.com/ns1");
 *         writer.writeEndElement();
 *         writer.writeEndDocument();
 *         
 *         // Closes the writer which may be recycled back to the factory.
 *         writer.close();
 *     
 *         // Displays the formatted output.
 *         System.out.println(xml);
 *     }
 *  [/code]</p>
 *  
 * <p> Bundles requiring multiple factories or stack-allocated 
 *     instances may create new factories through {@link Copyable copy}.</p>
 *     
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0 December 12, 2012
 */
@Parallelizable(comment="Factory configuration should be performed sequentially.")
public interface XMLOutputFactory extends Copyable<XMLOutputFactory>{

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
     * Returns a XML stream writer to the specified i/o writer.
     * 
     * @param writer the writer to write to.
     * @return a xml stream writer possibly recycled.
     * @throws XMLStreamException
     */
    XMLStreamWriter createXMLStreamWriter(Writer writer)
            throws XMLStreamException;

    /**
     * Returns a XML stream writer to the specified output stream (UTF-8
     * encoding).
     * 
     * @param stream the stream to write to.
     * @return a xml stream writer possibly recycled.
     * @throws XMLStreamException
     */
    XMLStreamWriter createXMLStreamWriter(OutputStream stream)
            throws XMLStreamException;

    /**
     * Returns a XML stream writer to the specified output stream using the
     * specified encoding.
     * 
     * @param stream the stream to write to.
     * @param encoding the encoding to use.
     * @return a xml stream writer possibly recycled.
     * @throws XMLStreamException
     */
    XMLStreamWriter createXMLStreamWriter(OutputStream stream, String encoding)
            throws XMLStreamException;

    /**
     * Allows the user to set specific features/properties on the underlying
     * implementation.
     * 
     * @param name the name of the property.
     * @param value  the value of the property.
     * @throws IllegalArgumentException if the property is not supported.
     */
    void setProperty(String name, Object value) throws IllegalArgumentException;

    /**
     * Gets a feature/property on the underlying implementation.
     * 
     * @param name the name of the property
     * @return the value of the property
     * @throws IllegalArgumentException if the property is not supported.
     */
    Object getProperty(String name) throws IllegalArgumentException;

    /**
     * Queries the set of properties that this factory supports.
     * 
     * @param name the name of the property (may not be null).
     * @return <code>true</code> if the property is supported;
     *         <code>false</code> otherwise.
     */
    boolean isPropertySupported(String name);

}