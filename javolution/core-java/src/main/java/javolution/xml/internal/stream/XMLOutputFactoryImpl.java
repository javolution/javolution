/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.xml.internal.stream;

import java.io.OutputStream;
import java.io.Writer;

import javolution.util.FastTable;
import javolution.xml.stream.XMLOutputFactory;
import javolution.xml.stream.XMLStreamException;

/**
 * This default XML Output factory implementation.
 */
public final class XMLOutputFactoryImpl implements XMLOutputFactory {

    // Property setting.
    private Boolean _automaticEmptyElements = Boolean.FALSE;

    // Property setting.
    private String _indentation;

    // Property setting.
    private Boolean _isRepairingNamespaces = Boolean.FALSE;

    // Property setting.
    private String _lineSeparator = "\n";

    // Property setting.
    private Boolean _noEmptyElementTag = Boolean.FALSE;

    // Property setting.
    private String _repairingPrefix = "ns";

    private FastTable<XMLStreamWriterImpl> _recycled = new FastTable<XMLStreamWriterImpl>()
            .shared();

    // Implements XMLOutputFactory abstract method.
    public XMLStreamWriterImpl createXMLStreamWriter(OutputStream stream)
            throws XMLStreamException {
        XMLStreamWriterImpl xmlWriter = newWriter();
        xmlWriter.setOutput(stream);
        return xmlWriter;
    }

    // Implements XMLOutputFactory abstract method.
    public XMLStreamWriterImpl createXMLStreamWriter(OutputStream stream,
            String encoding) throws XMLStreamException {
        if ((encoding == null) || encoding.equals("UTF-8")
                || encoding.equals("utf-8"))
            return createXMLStreamWriter(stream);
        XMLStreamWriterImpl xmlWriter = newWriter();
        xmlWriter.setOutput(stream, encoding);
        return xmlWriter;
    }

    // Implements XMLOutputFactory abstract method.
    public XMLStreamWriterImpl createXMLStreamWriter(Writer writer)
            throws XMLStreamException {
        XMLStreamWriterImpl xmlWriter = newWriter();
        xmlWriter.setOutput(writer);
        return xmlWriter;
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

    /**
     * Recycles the specified writer instance.
     */
    void recycle(XMLStreamWriterImpl xmlWriter) {
        _recycled.addLast(xmlWriter);
    }

    private XMLStreamWriterImpl newWriter() {
        XMLStreamWriterImpl xmlWriter = _recycled.pollLast();
        if (xmlWriter == null) xmlWriter = new XMLStreamWriterImpl(this);
        xmlWriter.setRepairingNamespaces(_isRepairingNamespaces.booleanValue());
        xmlWriter.setRepairingPrefix(_repairingPrefix);
        xmlWriter.setIndentation(_indentation);
        xmlWriter.setLineSeparator(_lineSeparator);
        xmlWriter.setAutomaticEmptyElements(_automaticEmptyElements
                .booleanValue());
        xmlWriter.setNoEmptyElementTag(_noEmptyElementTag.booleanValue());
        return xmlWriter;
    }
    
    @Override
    public XMLOutputFactory clone() {
        try {
            XMLOutputFactoryImpl clone = (XMLOutputFactoryImpl) super.clone();
            clone._recycled = new FastTable<XMLStreamWriterImpl>().shared();
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new Error();// Cannot happen since cloneable.
        }
    }
}
