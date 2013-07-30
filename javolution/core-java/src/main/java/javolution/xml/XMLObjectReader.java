/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.xml;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import javolution.xml.internal.stream.XMLStreamReaderImpl;
import javolution.xml.stream.XMLStreamException;
import javolution.xml.stream.XMLStreamReader;

/**
 * <p> This class restores objects which have been serialized in XML
 *     format using an {@link XMLObjectWriter}.</p>
 *     
 * <p> When the XML document is parsed, each elements are recursively
 *     processed and Java objects are created using the {@link XMLFormat}
 *     of the class as identified by the {@link XMLBinding}.</p>
 *     
 * <p> Multiple objects can be read from the same XML input.
 *     For example:[code]
 *     XMLObjectReader reader = XMLObjectReader.newInstance(inputStream);
 *     while (reader.hasNext()) {
 *         Message message = reader.read("Message", Message.class);
 *     }
 *     reader.close(); // The underlying stream is closed.
 *     [/code]</p>
 *     
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 4.0, September 4, 2006
 */
public class XMLObjectReader {

    /**
     * Hold the xml element used when parsing.
     */
    private final XMLFormat.InputElement _xml = new XMLFormat.InputElement();

    /**
     * Holds reader if any.
     */
    private Reader _reader;

    /**
     * Holds input stream if any.
     */
    private InputStream _inputStream;

    /**
     * Returns a XML object reader having the specified
     * input stream as input.
     * 
     * @param in the input stream.
     */
    public static XMLObjectReader newInstance(InputStream in)
            throws XMLStreamException {
        XMLObjectReader reader = new XMLObjectReader();
        reader.setInput(in);
        return reader;
    }

    /**
     * Returns a XML object reader (potentially recycled) having the specified
     * input stream/encoding as input.
     * 
     * @param in the input stream.
     * @param encoding the input stream encoding
     */
    public static XMLObjectReader newInstance(InputStream in, String encoding)
            throws XMLStreamException {
        XMLObjectReader reader = new XMLObjectReader();
        reader.setInput(in, encoding);
        return reader;
    }

    /**
     * Returns a XML object reader (potentially recycled) having the specified
     * reader as input.
     * 
     * @param in the reader source.
     */
    public static XMLObjectReader newInstance(Reader in)
            throws XMLStreamException {
        XMLObjectReader reader = new XMLObjectReader();
        reader.setInput(in);
        return reader;
    }

    /**
     * Default constructor.
     */
    public XMLObjectReader() {}

    /**
     * Returns the stream reader being used by this reader (it can be 
     * used to set prefix, read prologs, etc).
     * 
     * @return the stream reader.
     */
    public XMLStreamReader getStreamReader() {
        return _xml._reader;
    }

    /**
     * Sets the input stream source for this XML object reader
     * (encoding retrieved from XML prolog if any).
     * 
     * @param  in the source input stream.
     * @return <code>this</code>
     * @see    XMLStreamReaderImpl#setInput(InputStream)
     */
    public XMLObjectReader setInput(InputStream in) throws XMLStreamException {
        if ((_inputStream != null) || (_reader != null))
            throw new IllegalStateException("Reader not closed or reset");
        _xml._reader.setInput(in);
        _inputStream = in;
        return this;
    }

    /**
     * Sets the input stream source and encoding for this XML object reader.
     * 
     * @param in the input source.
     * @param encoding the associated encoding.
     * @return <code>this</code>
     * @see    XMLStreamReaderImpl#setInput(InputStream, String)
     */
    public XMLObjectReader setInput(InputStream in, String encoding)
            throws XMLStreamException {
        if ((_inputStream != null) || (_reader != null))
            throw new IllegalStateException("Reader not closed or reset");
        _xml._reader.setInput(in, encoding);
        _inputStream = in;
        return this;
    }

    /**
     * Sets the reader input source for this XML stream reader. 
     * 
     * @param  in the source reader.
     * @return <code>this</code>
     * @see    XMLStreamReaderImpl#setInput(Reader)
     */
    public XMLObjectReader setInput(Reader in) throws XMLStreamException {
        if ((_inputStream != null) || (_reader != null))
            throw new IllegalStateException("Reader not closed or reset");
        _xml._reader.setInput(in);
        _reader = in;
        return this;
    }

    /**
     * Sets the XML binding to use with this object reader.
     * 
     * @param binding the XML binding to use.
     * @return <code>this</code>
     */
    public XMLObjectReader setBinding(XMLBinding binding) {
        _xml.setBinding(binding);
        return this;
    }

    /**
     * Sets the XML reference resolver to use with this object reader
     * (the same resolver can be used accross multiple readers).
     * 
     * @param referenceResolver the XML reference resolver.
     * @return <code>this</code>
     */
    public XMLObjectReader setReferenceResolver(
            XMLReferenceResolver referenceResolver) {
        _xml.setReferenceResolver(referenceResolver);
        return this;
    }

    /**
     * Indicates if more elements can be read. This method 
     * positions the reader at the start of the
     * next element to be read (if any).
     *
     * @return <code>true</code> if more element/data to be read; 
     *         <code>false</code> otherwise.
     * @see    XMLFormat.InputElement#hasNext()
     */
    public boolean hasNext() throws XMLStreamException {
        return _xml.hasNext();
    }

    /**
     * Returns the object corresponding to the next element/data.
     *
     * @return the next nested object (can be <code>null</code>)
     * @throws XMLStreamException if <code>hasNext() == false</code>
     * @see    XMLFormat.InputElement#getNext()
     */
    @SuppressWarnings("unchecked")
    public <T> T read() throws XMLStreamException {
        return (T) _xml.getNext();
    }

    /**
     * Returns the object corresponding to the next nested element only 
     * if it has the specified local name. 
     *
     * @param name the local name of the next element.
     * @return the next content object or <code>null</code> if the 
     *         local name does not match.
     * @see    XMLFormat.InputElement#get(String)
     */
    @SuppressWarnings("unchecked")
    public <T> T read(String name) throws XMLStreamException {
        return (T) _xml.get(name);
    }

    /**
     * Returns the object corresponding to the next nested element only 
     * if it has the specified local name and namespace URI.
     *
     * @param localName the local name.
     * @param uri the namespace URI.
     * @return the next content object or <code>null</code> if the 
     *         name/uri does not match.
     * @see    XMLFormat.InputElement#get(String, String)
     */
    @SuppressWarnings("unchecked")
    public <T> T read(String localName, String uri) throws XMLStreamException {
        return (T) _xml.get(localName, uri);
    }

    /**
     * Returns the object corresponding to the next nested element only 
     * if it has the specified local name; the actual object type is identified 
     * by the specified class parameter. 
     *      
     * @param name the name of the element to match.
     * @param cls the non-abstract class identifying the object to return.
     * @return <code>read(name, null, cls)</code>
     */
    public <T> T read(String name, Class<T> cls) throws XMLStreamException {
        return _xml.get(name, cls);
    }

    /**
     * Returns the object corresponding to the next nested element only 
     * if it has the specified local name and namespace URI; the 
     * actual object type is identified by the specified class parameter. 
     *      
     * @param localName the local name.
     * @param uri the namespace URI.
     * @param cls the non-abstract class identifying the object to return.
     * @return the next content object or <code>null</code> if no match.
     */
    public <T> T read(String localName, String uri, Class<T> cls)
            throws XMLStreamException {
        return _xml.get(localName, uri, cls);
    }

    /**
     * Closes this reader and its underlying input then {@link #reset reset}
     * this reader for potential reuse.
     */
    public void close() throws XMLStreamException {
        try {
            if (_inputStream != null) {
                _inputStream.close();
                reset();
            } else if (_reader != null) {
                _reader.close();
                reset();
            }
        } catch (IOException e) {
            throw new XMLStreamException(e);
        }
    }

    /**
     * Resets this object reader for reuse.
     */
    public void reset() {
        _xml.reset();
        _reader = null;
        _inputStream = null;
    }
}