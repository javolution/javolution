/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2006 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package _templates.javolution.xml.sax;

import java.io.IOException;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;



import _templates.java.lang.CharSequence;
import _templates.javolution.lang.Reusable;
import _templates.javolution.text.CharArray;
import _templates.javolution.text.Text;

/**
 * <p> This class provides a SAX2-compliant parser wrapping a
 *     {@link _templates.javolution.xml.sax.XMLReaderImpl}. This parser allocates 
 *     <code>java.lang.String</code> instances while parsing in accordance 
 *     with the SAX2 specification. For faster performance (2-5x), the use of 
 *     the SAX2-like {@link _templates.javolution.xml.sax.XMLReaderImpl 
 *     XMLSaxParserImpl} or better{@link _templates.javolution.xml.stream.XMLStreamReader 
 *     XMLStreamReader} is recommended.</p>
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 4.0, June 16, 2005
 * @see <a href="http://www.saxproject.org"> SAX -- Simple API for XML</a> 
 */
public final class SAX2ReaderImpl implements XMLReader, Reusable {

    /**
     * Holds the SAX2 default handler instance.
     */
    private static Sax2DefaultHandler DEFAULT_HANDLER 
        = new Sax2DefaultHandler();

    /**
     * Holds the real-time parser instance associated to this SAX2 parser.
     */
    private final XMLReaderImpl _parser = new XMLReaderImpl();

    /**
     * Holds the content handler proxy.
     */
    private final Proxy _proxy = new Proxy();

    /**
     * Default constructor.
     */
    public SAX2ReaderImpl() {
    }

    // Implements org.xml.sax.XMLReader interface.
    public boolean getFeature(String name) throws SAXNotRecognizedException,
            SAXNotSupportedException {
        return _parser.getFeature(name);
    }

    // Implements org.xml.sax.XMLReader interface.
    public void setFeature(String name, boolean value)
            throws SAXNotRecognizedException, SAXNotSupportedException {
        _parser.setFeature(name, value);
    }

    // Implements org.xml.sax.XMLReader interface.
    public Object getProperty(String name) throws SAXNotRecognizedException,
            SAXNotSupportedException {
        return _parser.getProperty(name);
    }

    // Implements org.xml.sax.XMLReader interface.
    public void setProperty(String name, Object value)
            throws SAXNotRecognizedException, SAXNotSupportedException {
        _parser.setProperty(name, value);
    }

    // Implements org.xml.sax.XMLReader interface.
    public void setEntityResolver(EntityResolver resolver) {
        _parser.setEntityResolver(resolver);
    }

    // Implements org.xml.sax.XMLReader interface.
    public EntityResolver getEntityResolver() {
        return _parser.getEntityResolver();
    }

    // Implements org.xml.sax.XMLReader interface.
    public void setDTDHandler(DTDHandler handler) {
        _parser.setDTDHandler(handler);
    }

    // Implements org.xml.sax.XMLReader interface.
    public DTDHandler getDTDHandler() {
        return _parser.getDTDHandler();
    }

    // Implements org.xml.sax.XMLReader interface.
    public void setContentHandler(ContentHandler handler) {
        if (handler != null) {
            _proxy._sax2Handler = handler;
            _parser.setContentHandler(_proxy);
        } else {
            throw new NullPointerException();
        }
    }

    // Implements org.xml.sax.XMLReader interface.
    public ContentHandler getContentHandler() {
        return (_proxy._sax2Handler == DEFAULT_HANDLER) ? null
                : _proxy._sax2Handler;
    }

    // Implements org.xml.sax.XMLReader interface.
    public void setErrorHandler(ErrorHandler handler) {
        _parser.setErrorHandler(handler);
    }

    // Implements org.xml.sax.XMLReader interface.
    public ErrorHandler getErrorHandler() {
        return _parser.getErrorHandler();
    }

    // Implements org.xml.sax.XMLReader interface.
    public void parse(InputSource input) throws IOException, SAXException {
        try {
           _parser.parse(input);
        } finally {
            _parser.reset();
        }
    }

    // Implements org.xml.sax.XMLReader interface.
    public void parse(String systemId) throws IOException, SAXException {
        try {
            _parser.parse(systemId);
         } finally {
             _parser.reset();
         }
    }

    // Implements Reusable interface.
    public void reset() {
        _parser.reset();        
    }
    /**
     * This class defines the proxy for content handler and attributes.
     */
    private static final class Proxy implements
            _templates.javolution.xml.sax.ContentHandler, Attributes {

        /**
         * Holds the SAX2 content handler to which SAX2 events are forwarded.
         */
        private ContentHandler _sax2Handler = DEFAULT_HANDLER;

        /**
         * Holds the real-time attributes implementation from which attributes
         * values are read.
         */
        private _templates.javolution.xml.sax.Attributes _attributes;

        /**
         * Default constructor.
         */
        public Proxy() {
        }

        // Implements ContentHandler
        public void setDocumentLocator(Locator locator) {
            _sax2Handler.setDocumentLocator(locator);
        }

        // Implements ContentHandler
        public void startDocument() throws SAXException {
            _sax2Handler.startDocument();
        }

        // Implements ContentHandler
        public void endDocument() throws SAXException {
            _sax2Handler.endDocument();
            _sax2Handler = DEFAULT_HANDLER;
        }

        // Implements ContentHandler
        public void startPrefixMapping(CharArray prefix, CharArray uri)
                throws SAXException {
            _sax2Handler.startPrefixMapping(prefix.toString(), uri.toString());
        }

        // Implements ContentHandler
        public void endPrefixMapping(CharArray prefix) throws SAXException {
            _sax2Handler.endPrefixMapping(prefix.toString());
        }

        // Implements ContentHandler
        public void startElement(CharArray namespaceURI,
                CharArray localName, CharArray qName,
                _templates.javolution.xml.sax.Attributes atts) throws SAXException {
            _attributes = atts;
            _sax2Handler.startElement(namespaceURI.toString(), localName
                    .toString(), qName.toString(), this);
        }

        // Implements ContentHandler
        public void endElement(CharArray namespaceURI,
                CharArray localName, CharArray qName) throws SAXException {
            _sax2Handler.endElement(namespaceURI.toString(), localName
                    .toString(), qName.toString());
        }

        // Implements ContentHandler
        public void characters(char ch[], int start, int length)
                throws SAXException {
            _sax2Handler.characters(ch, start, length);
        }

        // Implements ContentHandler
        public void ignorableWhitespace(char ch[], int start, int length)
                throws SAXException {
            _sax2Handler.ignorableWhitespace(ch, start, length);
        }

        // Implements ContentHandler
        public void processingInstruction(CharArray target, CharArray data)
                throws SAXException {
            _sax2Handler.processingInstruction(target.toString(), data
                    .toString());
        }

        // Implements ContentHandler
        public void skippedEntity(CharArray name) throws SAXException {
            _sax2Handler.skippedEntity(name.toString());
        }

        // Implements Attributes
        public int getLength() {
            return (_attributes != null ? _attributes.getLength() : 0);
        }

        // Implements Attributes
        public String getURI(int index) {
            CharSequence chars = (_attributes != null ? _attributes.getURI(index) : null);
            return (chars != null ? chars.toString() : "");
        }

        // Implements Attributes
        public String getLocalName(int index) {
            CharSequence chars = (_attributes != null ? _attributes.getLocalName(index) : null);
            return (chars != null ? chars.toString() : "");
        }

        // Implements Attributes
        public String getQName(int index) {
            CharSequence chars = (_attributes != null ? _attributes.getQName(index) : null);
            return (chars != null ? chars.toString() : "");
        }

        // Implements Attributes
        public String getType(int index) {
            return (_attributes != null ? _attributes.getType(index).toString() : null);
        }

        // Implements Attributes
        public String getValue(int index) {
            CharSequence chars = (_attributes != null ? _attributes.getValue(index) : null);
            return (chars != null ? chars.toString() : null);
        }

        // Implements Attributes
        public int getIndex(String uri, String localName) {
            return (uri != null && localName != null && _attributes != null ? _attributes.getIndex(toCharSequence(uri), toCharSequence(localName)) : -1);
        }

        // Implements Attributes
        public int getIndex(String qName) {
            return (qName != null && _attributes != null ? _attributes.getIndex(toCharSequence(qName)) : -1);
        }

        // Implements Attributes
        public String getType(String uri, String localName) {
            return (uri != null && localName != null && _attributes != null ? _attributes.getType(toCharSequence(uri), toCharSequence(localName)).toString() : null);
        }

        // Implements Attributes
        public String getType(String qName) {
            return (qName != null && _attributes != null ? _attributes.getType(toCharSequence(qName)).toString() : null);
        }

        // Implements Attributes
        public String getValue(String uri, String localName) {
            return (uri != null && localName != null && _attributes != null && _attributes.getValue(toCharSequence(uri), toCharSequence(localName)) != null ? _attributes.getValue(toCharSequence(uri), toCharSequence(localName)).toString() : null);
        }

        // Implements Attributes
        public String getValue(String qName) {
            return (qName != null && _attributes != null ? _attributes.getValue(toCharSequence(qName)).toString() : null);
        }
    }

    private static final class Sax2DefaultHandler implements EntityResolver,
            DTDHandler, ContentHandler, ErrorHandler {

        public InputSource resolveEntity(String publicId, String systemId)
                throws SAXException, IOException {
            return null;
        }

        public void notationDecl(String name, String publicId, String systemId)
                throws SAXException {
        }

        public void unparsedEntityDecl(String name, String publicId,
                String systemId, String notationName) throws SAXException {
        }

        public void setDocumentLocator(Locator locator) {
        }

        public void startDocument() throws SAXException {
        }

        public void endDocument() throws SAXException {
        }

        public void startPrefixMapping(String prefix, String uri)
                throws SAXException {
        }

        public void endPrefixMapping(String prefix) throws SAXException {
        }

        public void startElement(String uri, String localName, String qName,
                Attributes atts) throws SAXException {
        }

        public void endElement(String uri, String localName, String qName)
                throws SAXException {
        }

        public void characters(char[] ch, int start, int length)
                throws SAXException {
        }

        public void ignorableWhitespace(char[] ch, int start, int length)
                throws SAXException {
        }

        public void processingInstruction(String target, String data)
                throws SAXException {
        }

        public void skippedEntity(String name) throws SAXException {
        }

        public void warning(SAXParseException exception) throws SAXException {
        }

        public void error(SAXParseException exception) throws SAXException {
        }

        public void fatalError(SAXParseException exception) throws SAXException {
            throw exception;
        }
    }
    
    private static CharSequence toCharSequence(Object obj) {
        return obj instanceof CharSequence ? (CharSequence)obj : 
            Text.valueOf(obj);
    }

}