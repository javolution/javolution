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
import java.io.OutputStream;
import java.io.Writer;

import javolution.xml.internal.stream.XMLStreamWriterImpl;
import javolution.xml.stream.XMLStreamException;
import javolution.xml.stream.XMLStreamWriter;

/**
 * <p> This class takes an object and formats it to XML; the resulting 
 *     XML can be deserialized using a {@link XMLObjectReader}.</p>
 *     
 * <p> When an object is formatted, the {@link XMLFormat} of the 
 *     object's class as identified by the {@link XMLBinding} is used to
 *     write its XML representation.</p>
 *     
 * <p> Multiple objects can be written to the same XML output.
 *     For example:[code]
 *     XMLObjectWriter writer = XMLObjectWriter.newInstance(outputStream);
 *     while (true)) {
 *         Message message = ...
 *         writer.write(message, "Message", Message.class);
 *     }
 *     writer.close(); // The underlying stream is closed.
 *     [/code]</p>
 *     
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 4.0, September 4, 2006
 */
public class XMLObjectWriter {

    /**
     * Hold the xml element used when formatting.
     */
    private final XMLFormat.OutputElement _xml = new XMLFormat.OutputElement();

    /**
     * Holds writer if any.
     */
    private Writer _writer;

    /**
     * Holds input stream if any.
     */
    private OutputStream _outputStream;

    /**
     * Default constructor.
     */
    public XMLObjectWriter() {}

    /**
     * Returns a XML object writer (potentially recycled) having the specified
     * output stream as output.
     * 
     * @param out the output stream.
     */
    public static XMLObjectWriter newInstance(OutputStream out)
            throws XMLStreamException {
        XMLObjectWriter writer = new XMLObjectWriter();
        writer.setOutput(out);
        return writer;
    }

    /**
     * Returns a XML object writer (potentially recycled) having the specified
     * output stream/encoding as output.
     * 
     * @param out the output stream.
     * @param encoding the output stream encoding.
     */
    public static XMLObjectWriter newInstance(OutputStream out, String encoding)
            throws XMLStreamException {
        XMLObjectWriter writer = new XMLObjectWriter();
        writer.setOutput(out, encoding);
        return writer;
    }

    /**
     * Returns a XML object writer (potentially recycled) having the specified
     * writer as output.
     * 
     * @param out the writer output.
     */
    public static XMLObjectWriter newInstance(Writer out)
            throws XMLStreamException {
        XMLObjectWriter writer = new XMLObjectWriter();
        writer.setOutput(out);
        return writer;
    }

    /**
     * Returns the stream writer used by this object writer (it can be used 
     * to write prolog, write namespaces, etc). The stream writer is setup to 
     * automatically repair namespaces and to automatically output empty 
     * elements when a start element is immediately followed by matching end
     * element. 
     * 
     * @return the stream writer.
     */
    public XMLStreamWriter getStreamWriter() {
        return _xml._writer;
    }

    /**
     * Sets the output stream for this XML object writer.
     * 
     * @param  out the output stream destination.
     * @return <code>this</code>
     * @see    XMLStreamWriterImpl#setOutput(OutputStream)
     */
    public XMLObjectWriter setOutput(OutputStream out)
            throws XMLStreamException {
        if ((_outputStream != null) || (_writer != null))
            throw new IllegalStateException("Writer not closed or reset");
        _xml._writer.setOutput(out);
        _outputStream = out;
        _xml._writer.writeStartDocument();
        return this;
    }

    /**
     * Sets the output stream and encoding for this XML object writer.
     * 
     * @param  out the output stream destination.
     * @param  encoding the stream encoding.
     * @return <code>this</code>
     * @see    XMLStreamWriterImpl#setOutput(OutputStream, String)
     */
    public XMLObjectWriter setOutput(OutputStream out, String encoding)
            throws XMLStreamException {
        if ((_outputStream != null) || (_writer != null))
            throw new IllegalStateException("Writer not closed or reset");
        _xml._writer.setOutput(out, encoding);
        _outputStream = out;
        _xml._writer.writeStartDocument();
        return this;
    }

    /**
     * Sets the output writer for this XML object writer.
     * 
     * @param  out the writer destination.
     * @return <code>this</code>
     * @see    XMLStreamWriterImpl#setOutput(Writer)
     */
    public XMLObjectWriter setOutput(Writer out) throws XMLStreamException {
        if ((_outputStream != null) || (_writer != null))
            throw new IllegalStateException("Writer not closed or reset");
        _xml._writer.setOutput(out);
        _writer = out;
        _xml._writer.writeStartDocument();
        return this;
    }

    /**
     * Sets the XML binding to use with this object writer.
     * 
     * @param binding the XML binding to use.
     * @return <code>this</code>
     */
    public XMLObjectWriter setBinding(XMLBinding binding) {
        _xml.setBinding(binding);
        return this;
    }

    /**
     * Sets the indentation to be used by this writer (no indentation 
     * by default).
     * 
     * @param indentation the indentation string.
     * @return <code>this</code>
     */
    public XMLObjectWriter setIndentation(String indentation) {
        _xml._writer.setIndentation(indentation);
        return this;
    }

    /**
     * Sets the XML reference resolver to use with this object writer 
     * (the same reference resolver can be used accross multiple writers).
     * 
     * @param referenceResolver the XML reference resolver.
     * @return <code>this</code>
     */
    public XMLObjectWriter setReferenceResolver(
            XMLReferenceResolver referenceResolver) {
        _xml.setReferenceResolver(referenceResolver);
        return this;
    }

    /**
     * Writes the specified object as an anonymous nested element of 
     * unknown type. This result in the actual type of the object being
     * identified by the element name.
     *
     * @param obj the object written as nested element or <code>null</code>.
     * @see   XMLFormat.OutputElement#add(Object)
     */
    public void write(Object obj) throws XMLStreamException {
        _xml.add(obj);
    }

    /**
     * Writes the specified object as a named nested element of unknown type
     * (<code>null</code> objects are ignored). The nested XML element
     * may contain a class attribute identifying the object type.
     *
     * @param obj the object added as nested element or <code>null</code>.
     * @param name the name of the nested element.
     * @see   XMLFormat.OutputElement#add(Object, String)
     */
    public void write(Object obj, String name) throws XMLStreamException {
        _xml.add(obj, name);
    }

    /**
     * Writes the specified object as a fully qualified nested element of 
     * unknown type (<code>null</code> objects are ignored). 
     * The nested XML element may contain a class attribute identifying
     * the object type.
     *
     * @param obj the object added as nested element or <code>null</code>.
     * @param localName the local name of the nested element.
     * @param uri the namespace URI of the nested element.
     * @see   XMLFormat.OutputElement#add(Object, String, String)
     */
    public void write(Object obj, String localName, String uri)
            throws XMLStreamException {
        _xml.add(obj, localName, uri);
    }

    /**
     * Writes the specified object as a named nested element of actual type
     * known (<code>null</code> objects are ignored). 
     *
     * @param obj the object added as nested element or <code>null</code>.
     * @param name the name of the nested element.
     * @param cls the non-abstract class identifying the XML format to use.
     * @see   XMLFormat.OutputElement#add(Object, String, Class)
     */
    public <T> void write(T obj, String name, Class<T> cls)
            throws XMLStreamException {
        _xml.add(obj, name, cls);
    }

    /**
     * Writes the specified object as a fully qualified nested element of
     *  actual type known (<code>null</code> objects are ignored). 
     *
     * @param obj the object added as nested element or <code>null</code>.
     * @param localName the local name of the nested element.
     * @param uri the namespace URI of the nested element.
     * @param cls the class identifying the XML format to use.
     * @see   XMLFormat.OutputElement#add(Object, String, String, Class)
     */
    public <T> void write(T obj, String localName, String uri, Class<T> cls)
            throws XMLStreamException {
        _xml.add(obj, localName, uri, cls);
    }

    /**
     * Flushes the output stream of this writer (automatically done 
     * when {@link #close() closing}).
     */
    public void flush() throws XMLStreamException {
        _xml._writer.flush();
    }

    /**
     * Ends document writting, closes this writer and its underlying 
     * output then {@link #reset reset} this Writer for potential reuse.
     */
    public void close() throws XMLStreamException {
        try {
            if (_outputStream != null) {
                _xml._writer.writeEndDocument();
                _xml._writer.close();
                _outputStream.close();
                reset();
            } else if (_writer != null) {
                _xml._writer.writeEndDocument();
                _xml._writer.close();
                _writer.close();
                reset();
            }

        } catch (IOException e) {
            throw new XMLStreamException(e);
        }
    }

    /**
     * Resets this object writer for reuse.
     */
    public void reset() {
        _xml.reset();
        _outputStream = null;
        _writer = null;
    }
}