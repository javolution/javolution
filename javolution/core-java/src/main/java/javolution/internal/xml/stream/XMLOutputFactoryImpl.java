/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.internal.xml.stream;

import java.io.OutputStream;
import java.io.Writer;
import javolution.xml.stream.XMLOutputFactory;
import javolution.xml.stream.XMLStreamException;
import javolution.xml.stream.XMLStreamWriter;

/**
 * This class represents the default implementation.
 */
public final class XMLOutputFactoryImpl extends XMLOutputFactory {
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
        XMLStreamWriterImpl xmlWriter = new XMLStreamWriterImpl();
        xmlWriter.setRepairingNamespaces(_isRepairingNamespaces.booleanValue());
        xmlWriter.setRepairingPrefix(_repairingPrefix);
        xmlWriter.setIndentation(_indentation);
        xmlWriter.setLineSeparator(_lineSeparator);
        xmlWriter.setAutomaticEmptyElements(_automaticEmptyElements
                .booleanValue());
        xmlWriter.setNoEmptyElementTag(_noEmptyElementTag.booleanValue());
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
                || name.equals(INDENTATION) || name.equals(LINE_SEPARATOR);
    }

}
