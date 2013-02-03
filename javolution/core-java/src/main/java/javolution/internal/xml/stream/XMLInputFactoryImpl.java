/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.internal.xml.stream;

import java.io.InputStream;
import java.io.Reader;
import java.util.Map;
import javolution.xml.stream.XMLInputFactory;
import javolution.xml.stream.XMLStreamException;
import javolution.xml.stream.XMLStreamReader;

/**
 * This class represents the default factory implementation.
 */
public final class XMLInputFactoryImpl extends XMLInputFactory {
    Map<?,?> _entities = null;

    // Implements XMLInputFactory abstract method.
    public XMLStreamReader createXMLStreamReader(Reader reader) throws XMLStreamException {
        XMLStreamReaderImpl xmlReader = newReader();
        xmlReader.setInput(reader);
        return xmlReader;
    }

    // Implements XMLInputFactory abstract method.
    public XMLStreamReader createXMLStreamReader(InputStream stream) throws XMLStreamException {
        XMLStreamReaderImpl xmlReader = newReader();
        xmlReader.setInput(stream);
        return xmlReader;
    }

    // Implements XMLInputFactory abstract method.
    public XMLStreamReader createXMLStreamReader(InputStream stream, String encoding) throws XMLStreamException {
        XMLStreamReaderImpl xmlReader = newReader();
        xmlReader.setInput(stream, encoding);
        return xmlReader;
    }

    // Implements XMLInputFactory abstract method.
    @SuppressWarnings("unchecked")
    public void setProperty(String name, Object value) throws IllegalArgumentException {
        if (name.equals(IS_COALESCING)) {
            // Do nothing, always coalescing.
        } else if (name.equals(ENTITIES)) {
            _entities = (Map<Object,Object>) value;
        } else {
            throw new IllegalArgumentException("Property: " + name + " not supported");
        }
    }

    // Implements XMLInputFactory abstract method.
    public Object getProperty(String name) throws IllegalArgumentException {
        if (name.equals(IS_COALESCING)) {
            return Boolean.TRUE;
        } else if (name.equals(ENTITIES)) {
            return _entities;
        } else {
            throw new IllegalArgumentException("Property: " + name + " not supported");
        }
    }

    // Implements XMLInputFactory abstract method.
    public boolean isPropertySupported(String name) {
        return name.equals(IS_COALESCING) || name.equals(ENTITIES);
    }

    private XMLStreamReaderImpl newReader() {
        XMLStreamReaderImpl xmlReader = new XMLStreamReaderImpl();
        if (_entities != null) {
            xmlReader.setEntities(_entities);
        }
        return xmlReader;
    }
    
}
