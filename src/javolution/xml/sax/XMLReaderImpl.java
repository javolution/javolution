/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2004 - The Javolution Team (http://javolution.org/)
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.xml.sax;

import j2me.lang.CharSequence;
import j2me.lang.UnsupportedOperationException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import javolution.util.Reflection;

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

/**
 * <p> This class provides a SAX2-compliant parser wrapping a
 *     {@link RealtimeParser}. This parser allocates 
 *     <code>j2me.lang.String</code> instances while parsing in accordance 
 *     with the SAX2 specification. For faster performance (2-3x), the use of 
 *     the SAX2-like {@link RealtimeParser} (with <code>j2me.lang.String</code>
 *     replaced by <code>j2me.lang.CharSequence</code>) is recommended.</p>
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 4.5, May 26, 2003
 * @see <a href="http://www.saxproject.org"> SAX -- Simple API for XML</a> 
 */
public final class XMLReaderImpl implements XMLReader {

    /**
     * Holds the SAX2 default handler instance.
     */
    private static Sax2DefaultHandler DEFAULT_HANDLER 
        = new Sax2DefaultHandler();

    /**
     * Holds the real-time parser instance associated to this SAX2 parser.
     */
    private final RealtimeParser _parser;

    /**
     * Default constructor.
     */
    public XMLReaderImpl() {
        _parser = new RealtimeParser();
        Proxy proxy = new Proxy(_parser);
        _parser.setContentHandler(proxy);
    }

    // Implements XMLReader
    public boolean getFeature(String name) throws SAXNotRecognizedException,
            SAXNotSupportedException {
        return _parser.getFeature(name);
    }

    // Implements XMLReader
    public void setFeature(String name, boolean value)
            throws SAXNotRecognizedException, SAXNotSupportedException {
        _parser.setFeature(name, value);
    }

    // Implements XMLReader
    public Object getProperty(String name) throws SAXNotRecognizedException,
            SAXNotSupportedException {
        return _parser.getProperty(name);
    }

    // Implements XMLReader
    public void setProperty(String name, Object value)
            throws SAXNotRecognizedException, SAXNotSupportedException {
        _parser.setProperty(name, value);
    }

    // Implements XMLReader
    public void setEntityResolver(EntityResolver resolver) {
        _parser.setEntityResolver(resolver);
    }

    // Implements XMLReader
    public EntityResolver getEntityResolver() {
        return _parser.getEntityResolver();
    }

    // Implements XMLReader
    public void setDTDHandler(DTDHandler handler) {
        _parser.setDTDHandler(handler);
    }

    // Implements XMLReader
    public DTDHandler getDTDHandler() {
        return _parser.getDTDHandler();
    }

    // Implements XMLReader
    public void setContentHandler(ContentHandler handler) {
        if (handler != null) {
            Proxy proxy = (Proxy) _parser.getContentHandler();
            proxy._sax2Handler = handler;
        } else {
            throw new NullPointerException();
        }
    }

    // Implements XMLReader
    public ContentHandler getContentHandler() {
        Proxy proxy = (Proxy) _parser.getContentHandler();
        return (proxy._sax2Handler == DEFAULT_HANDLER) ? null
                : proxy._sax2Handler;
    }

    // Implements XMLReader
    public void setErrorHandler(ErrorHandler handler) {
        _parser.setErrorHandler(handler);
    }

    // Implements XMLReader
    public ErrorHandler getErrorHandler() {
        return _parser.getErrorHandler();
    }

    // Implements XMLReader
    public void parse(InputSource input) throws IOException, SAXException {
        Reader reader = input.getCharacterStream();
        if (reader != null) {
            _parser.parse(reader);
        } else {
            InputStream inStream = input.getByteStream();
            if (inStream != null) {
                String encoding = input.getEncoding();
                if ((encoding == null) || encoding.equals("UTF-8")
                        || encoding.equals("utf-8")) {
                    _parser.parse(inStream);
                } else {
                    reader = new InputStreamReader(inStream, encoding);
                    _parser.parse(reader);
                }
            } else {
                parse(input.getSystemId());
            }
        }
    }

    // Implements XMLReader
    public void parse(String systemId) throws IOException, SAXException {
        InputStream in;
        try {
            Object url = NEW_URL.newInstance(systemId);
            in = (InputStream) OPEN_STREAM.invoke(url);
        } catch (Exception urlException) { // Try as filename.
            try {
                in = (InputStream) NEW_FILE_INPUT_STREAM.newInstance(systemId);
            } catch (Exception fileException) {
                throw new UnsupportedOperationException("Cannot parse "
                        + systemId);
            }
        }
        _parser.parse(in);
    }

    private static final Reflection.Constructor NEW_URL = Reflection
            .getConstructor("java.net.URL(j2me.lang.String)");

    private static final Reflection.Method OPEN_STREAM = Reflection
            .getMethod("java.net.URL.openStream()");

    private static final Reflection.Constructor NEW_FILE_INPUT_STREAM = Reflection
            .getConstructor("j2me.io.FileInputStream(j2me.lang.String)");

    /**
     * This class defines the proxy for content handler and attributes.
     */
    private static final class Proxy implements
            javolution.xml.sax.ContentHandler, Attributes {

        /**
         * Holds the SAX2 content handler to which SAX2 events are forwarded.
         */
        private ContentHandler _sax2Handler = DEFAULT_HANDLER;

        /**
         * Holds the real-time attributes implementation from which attributes
         * values are read.
         */
        private final AttributesImpl _attributes;

        /**
         * Creates a proxy for the specified {@link RealtimeParser}.
         *
         * @param parser the real-time parser.
         */
        public Proxy(RealtimeParser parser) {
            _attributes = parser._attributes;
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
        }

        // Implements ContentHandler
        public void startPrefixMapping(CharSequence prefix, CharSequence uri)
                throws SAXException {
            _sax2Handler.startPrefixMapping(prefix.toString(), uri.toString());
        }

        // Implements ContentHandler
        public void endPrefixMapping(CharSequence prefix) throws SAXException {
            _sax2Handler.endPrefixMapping(prefix.toString());
        }

        // Implements ContentHandler
        public void startElement(CharSequence namespaceURI,
                CharSequence localName, CharSequence qName,
                javolution.xml.sax.Attributes atts) throws SAXException {
            _sax2Handler.startElement(namespaceURI.toString(), localName
                    .toString(), qName.toString(), this);
        }

        // Implements ContentHandler
        public void endElement(CharSequence namespaceURI,
                CharSequence localName, CharSequence qName) throws SAXException {
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
        public void processingInstruction(CharSequence target, CharSequence data)
                throws SAXException {
            _sax2Handler.processingInstruction(target.toString(), data
                    .toString());
        }

        // Implements ContentHandler
        public void skippedEntity(CharSequence name) throws SAXException {
            _sax2Handler.skippedEntity(name.toString());
        }

        // Implements Attributes
        public int getLength() {
            return _attributes.getLength();
        }

        // Implements Attributes
        public String getURI(int index) {
            CharSequence chars = _attributes.getURI(index);
            return (chars != null) ? chars.toString() : null;
        }

        // Implements Attributes
        public String getLocalName(int index) {
            CharSequence chars = _attributes.getLocalName(index);
            return (chars != null) ? chars.toString() : null;
        }

        // Implements Attributes
        public String getQName(int index) {
            CharSequence chars = _attributes.getQName(index);
            return (chars != null) ? chars.toString() : null;
        }

        // Implements Attributes
        public String getType(int index) {
            return _attributes.getType(index);
        }

        // Implements Attributes
        public String getValue(int index) {
            CharSequence chars = _attributes.getValue(index);
            return (chars != null) ? chars.toString() : null;
        }

        // Implements Attributes
        public int getIndex(String uri, String localName) {
            return _attributes.getIndex(uri, localName);
        }

        // Implements Attributes
        public int getIndex(String qName) {
            return _attributes.getIndex(qName);
        }

        // Implements Attributes
        public String getType(String uri, String localName) {
            return _attributes.getType(uri, localName);
        }

        // Implements Attributes
        public String getType(String qName) {
            return _attributes.getType(qName);
        }

        // Implements Attributes
        public String getValue(String uri, String localName) {
            return _attributes.getValue(uri, localName);
        }

        // Implements Attributes
        public String getValue(String qName) {
            return _attributes.getValue(qName);
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
}