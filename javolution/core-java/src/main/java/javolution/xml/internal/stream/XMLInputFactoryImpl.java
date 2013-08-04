/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.xml.internal.stream;

import java.io.InputStream;
import java.io.Reader;
import java.util.Map;

import javolution.util.FastTable;
import javolution.xml.stream.XMLInputFactory;
import javolution.xml.stream.XMLStreamException;

/**
 * The default XML input factory implementation.
 */
public final class XMLInputFactoryImpl implements XMLInputFactory {
    private Map<String, String> _entities = null;
    private FastTable<XMLStreamReaderImpl> _recycled = new FastTable<XMLStreamReaderImpl>()
            .shared();

    // Implements XMLInputFactory abstract method.
    public XMLStreamReaderImpl createXMLStreamReader(InputStream stream)
            throws XMLStreamException {
        XMLStreamReaderImpl xmlReader = newReader();
        xmlReader.setInput(stream);
        return xmlReader;
    }

    // Implements XMLInputFactory abstract method.
    public XMLStreamReaderImpl createXMLStreamReader(InputStream stream,
            String encoding) throws XMLStreamException {
        XMLStreamReaderImpl xmlReader = newReader();
        xmlReader.setInput(stream, encoding);
        return xmlReader;
    }

    // Implements XMLInputFactory abstract method.
    public XMLStreamReaderImpl createXMLStreamReader(Reader reader)
            throws XMLStreamException {
        XMLStreamReaderImpl xmlReader = newReader();
        xmlReader.setInput(reader);
        return xmlReader;
    }

    // Implements XMLInputFactory abstract method.
    public Object getProperty(String name) throws IllegalArgumentException {
        if (name.equals(IS_COALESCING)) {
            return Boolean.TRUE;
        } else if (name.equals(ENTITIES)) {
            return _entities;
        } else {
            throw new IllegalArgumentException("Property: " + name
                    + " not supported");
        }
    }

    // Implements XMLInputFactory abstract method.
    public boolean isPropertySupported(String name) {
        return name.equals(IS_COALESCING) || name.equals(ENTITIES);
    }

    // Implements XMLInputFactory abstract method.
    @SuppressWarnings("unchecked")
    public void setProperty(String name, Object value)
            throws IllegalArgumentException {
        if (name.equals(IS_COALESCING)) {
            // Do nothing, always coalescing.
        } else if (name.equals(ENTITIES)) {
            _entities = (Map<String, String>) value;
        } else {
            throw new IllegalArgumentException("Property: " + name
                    + " not supported");
        }
    }

    /** Recycles the specified instance. */
    void recycle(XMLStreamReaderImpl reader) {
        _recycled.addLast(reader);
    }

    private XMLStreamReaderImpl newReader() {
        XMLStreamReaderImpl xmlReader = _recycled.pollLast();
        if (xmlReader == null) xmlReader = new XMLStreamReaderImpl(this);
        if (_entities != null) {
            xmlReader.setEntities(_entities);
        }
        return xmlReader;
    }
    
    @Override
    public XMLInputFactory clone() {
        try {
            XMLInputFactoryImpl clone = (XMLInputFactoryImpl) super.clone();
            clone._recycled = new FastTable<XMLStreamReaderImpl>().shared();
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new Error();// Cannot happen since cloneable.
        }
    }
}
