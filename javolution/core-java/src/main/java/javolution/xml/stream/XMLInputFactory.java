/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.xml.stream;

import java.io.InputStream;
import java.io.Reader;

import javolution.lang.Parallelizable;

/**
 * <p> The OSGi factory service to create {@link XMLStreamReader} instances.
 *     For each bundle, a distinct factory instance is returned and can be 
 *     individually configured (if not enough the factory can be 
 *     {@link #clone cloned}). 
 * {@code
 * import javolution.xml.stream.*;
 * public class Activator implements BundleActivator { 
 *     public void start(BundleContext bc) throws Exception {
 *     
 *         // Configures factory. 
 *         ServiceTracker<XMLInputFactory, XMLInputFactory> tracker 
 *             = new ServiceTracker<>(bc, XMLInputFactory.class, null);
 *         tracker.open();
 *         tracker.getService().setProperty(IS_COALESCING, true);
 *         
 *         // Instantiates a reader.
 *         String xml = "<test>This is a test</test>";
 *         CharSequenceReader in = new CharSequenceReader().setInput(xml);
 *         XMLStreamReader reader = tracker.getService().createXMLStreamReader(in);
 *     
 *         // Parses XML.
 *         while (reader.hasNext()) {
 *              int eventType = reader.next();
 *              if (eventType == XMLStreamConstants.CHARACTERS) {
 *                  System.out.println(reader.getText());
 *              }
 *         }
 *         
 *         // Closes the reader which may be recycled back to the factory.
 *         reader.close();
 *     }
 * }}</p>
 * 
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0 December 12, 2012
 */
@Parallelizable(comment="Factory configuration should be performed sequentially.")
public interface XMLInputFactory extends Cloneable {

    /**
     * The property that requires the parser to coalesce adjacent character data
     * sections.
     */
    public static final String IS_COALESCING = "javolution.xml.stream.isCoalescing";

    /**
     * The property that requires the parser to validate the input data.
     */
    public static final String IS_VALIDATING = "javolution.xml.stream.isValidating";

    /**
     * Property used to specify additional entities to be recognized by the 
     * readers (type: <code>java.util.Map</code>, default: <code>null</code>).
     * For example:[code]
     *     FastMap<String, String> HTML_ENTITIES = new FastMap<String, String>();
     *     HTML_ENTITIES.put("nbsp", " ");
     *     HTML_ENTITIES.put("copy", "©");
     *     HTML_ENTITIES.put("eacute", "é");
     *     ...
     *     XMLInputFactory factory = factoryRef.getService();
     *     factory.setProperty(ENTITIES, HTML_ENTITIES);
     * [/code]
     */
    public static final String ENTITIES = "javolution.xml.stream.entities";

    /**
     * Returns a XML stream reader for the specified I/O reader.
     * 
     * @param reader the XML data to read from.
     * @return a xml stream reader possibly recycled.
     * @throws XMLStreamException if an error occurs creating the stream reader
     */
    XMLStreamReader createXMLStreamReader(Reader reader)
            throws XMLStreamException;

    /**
     * Returns a XML stream reader for the specified input stream 
     * (encoding autodetected).
     * 
     * @param stream the input stream to read from.
     * @return a xml stream reader possibly recycled.
     * @throws XMLStreamException if an error occurs creating the stream reader
     */
    XMLStreamReader createXMLStreamReader(InputStream stream)
            throws XMLStreamException;

    /**
     * Returns a XML stream reader for the specified input stream using the
     * specified encoding.
     * 
     * @param stream the input stream to read from.
     * @param encoding the character encoding of the stream.
     * @return a xml stream reader possibly recycled.
     * @throws XMLStreamException if an error occurs creating the stream reader
     */
    XMLStreamReader createXMLStreamReader(InputStream stream,
            String encoding) throws XMLStreamException;

    /**
     * Allows the user to set specific feature/property on the underlying
     * implementation. The underlying implementation is not required to support
     * every setting of every property in the specification and may use
     * <code>IllegalArgumentException</code> to signal that an unsupported
     * property may not be set with the specified value.
     * 
     * @param name the name of the property.
     * @param value the value of the property
     * @throws IllegalArgumentException if the property is not supported.
     */
    void setProperty(String name, Object value) throws IllegalArgumentException;

    /**
     * Gets the value of a feature/property from the underlying implementation.
     * 
     * @param name the name of the property (may not be null).
     * @return the value of the property.
     * @throws IllegalArgumentException if the property is not supported.
     */
    Object getProperty(String name) throws IllegalArgumentException;

    /**
     * Queries the set of properties that this factory supports.
     * 
     * @param name the name of the property.
     * @return <code>true</code> if the property is supported;
     *         <code>false</code> otherwise.
     */
    boolean isPropertySupported(String name);
    
    /**
     * @return a clone of this factory which can be independently configured.
     */
    XMLInputFactory clone();
 
}