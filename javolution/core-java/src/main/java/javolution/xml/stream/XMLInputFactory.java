/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.xml.stream;

import javolution.internal.xml.stream.XMLInputFactoryImpl;
import java.io.InputStream;
import java.io.Reader;


/**
 * <p> The class represents the factory for getting {@link XMLStreamReader}
 *     intances.
  *     
 * <P> Usage example:[code]
 * 
 *     // Lets read a CharSequence input.
 *     String xml = "...";
 *     CharSequenceReader in = new CharSequenceReader().setInput(xml);

 *     // Creates a factory of readers coalescing adjacent character data.
 *     XMLInputFactory factory = XMLInputFactory.newInstance();
 *     factory.setProperty(XMLInputFactory.IS_COALESCING, true);
 *     
 *     // Creates a new reader.
 *     XMLStreamReader reader = factory.createXMLStreamReader(in);
 *     
 *     // Parses XML.
 *     for (int e=reader.next(); e != XMLStreamConstants.END_DOCUMENT; e = reader.next()) {
 *         switch (e) { // Event.
 *             ...
 *         }
 *     }
 *     reader.close(); // Automatically recycles this writer. 
 *     in.close(); // Underlying input should be closed explicitly.
 *     [/code]</p>
 * 
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 4.0, September 4, 2006
 */
public abstract class XMLInputFactory {


    /**
     * The property that requires the parser to coalesce adjacent character data
     * sections (type: <code>Boolean</code>, default: <code>FALSE</code>)
     */
    public static final String IS_COALESCING = "javolution.xml.stream.isCoalescing";

    /**
     * Property used to specify additional entities to be recognized by the 
     * readers (type: <code>java.util.Map</code>, default: <code>null</code>).
     * For example:[code]
     *     FastMap<String, String> HTML_ENTITIES = new FastMap<String, String>();
     *     HTML_ENTITIES.put("nbsp", " ");
     *     HTML_ENTITIES.put("copy", "©");
     *     HTML_ENTITIES.put("eacute", "é");
     *     ...
     *     XMLInputFactory factory = XMLInputFactory.newInstance();
     *     factory.setProperty(ENTITIES, HTML_ENTITIES);
     * [/code]
     */
    public static final String ENTITIES = "javolution.xml.stream.entities";

    /**
     * Default constructor.
     */
    protected XMLInputFactory() {
    }

    /**
     * Returns a new instance of the input factory
     * implementation which may be configurated by the user 
     * (see {@link #setProperty(String, Object)}).
     * 
     * @return a new factory instance.
     */
    public static XMLInputFactory newInstance() {
        return new XMLInputFactoryImpl();
    }

    /**
     * Returns a XML stream reader for the specified I/O reader.
     * 
     * @param reader the XML data to read from.
     * @throws XMLStreamException
     */
    public abstract XMLStreamReader createXMLStreamReader(Reader reader)
            throws XMLStreamException;

    /**
     * Returns a XML stream reader for the specified input stream 
     * (encoding autodetected).
     * 
     * @param stream the input stream to read from.
     * @throws XMLStreamException
     */
    public abstract XMLStreamReader createXMLStreamReader(InputStream stream)
            throws XMLStreamException;

    /**
     * Returns a XML stream reader for the specified input stream using the
     * specified encoding.
     * 
     * @param stream the input stream to read from.
     * @param encoding the character encoding of the stream.
     * @throws XMLStreamException
     */
    public abstract XMLStreamReader createXMLStreamReader(InputStream stream,
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
    public abstract void setProperty(String name, Object value)
            throws IllegalArgumentException;

    /**
     * Gets the value of a feature/property from the underlying implementation.
     * 
     * @param name the name of the property (may not be null).
     * @return the value of the property.
     * @throws IllegalArgumentException if the property is not supported.
     */
    public abstract Object getProperty(String name)
            throws IllegalArgumentException;

    /**
     * Queries the set of properties that this factory supports.
     * 
     * @param name the name of the property.
     * @return <code>true</code> if the property is supported;
     *         <code>false</code> otherwise.
     */
    public abstract boolean isPropertySupported(String name);
}