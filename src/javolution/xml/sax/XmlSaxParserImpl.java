/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2005 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.xml.sax;

import j2me.lang.CharSequence;
import j2me.nio.ByteBuffer;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import javolution.lang.Reusable;
import javolution.xml.pull.XmlPullParser;
import javolution.xml.pull.XmlPullParserException;
import javolution.xml.pull.XmlPullParserImpl;

import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXParseException;

/**
 * <p> This class provides a real-time SAX2-like XML parser; this parser is
 *     <i>extremely</i> fast and <b>does not create temporary objects</b>
 *     (no garbage generated and no GC interruption).</p>
 *     
 * <p> The parser input source can be either a {@link #parse(Reader) Reader},
 *     an {@link #parse(InputStream) InputStream} or even a {@link 
 *     #parse(ByteBuffer) ByteBuffer} (e.g. <code>MappedByteBuffer</code>).</p>
 *     
 * <p> The parser is implemented as a SAX2 wrapper around the real-time  
 *     {@link XmlPullParserImpl} and share the same characteristics.</p>
 *
 * <p><i> Note: This parser is a <b>SAX2-like</b> parser with the
 *        <code>java.lang.String</code> type replaced by the more generic 
 *       <code>j2me.lang.CharSequence</code> in {@link ContentHandler},
 *       {@link Attributes} interfaces and {@link DefaultHandler} base classes.
 *       If a standard SAX2 or JAXP parser is required, you may consider using
 *       the wrapping class {@link XMLReaderImpl}. Fast but not as fast as 
 *       <code>java.lang.String</code> instances are dynamically allocated
 *       while parsing.</i></p>
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 3.2, April 2, 2005
 */
public class XmlSaxParserImpl implements Reusable {

    /**
     * Holds the default handler instance.
     */
    private static DefaultHandler DEFAULT_HANDLER = new DefaultHandler();

    /**
     * Holds the content handler.
     */
    private ContentHandler _contentHandler;

    /**
     * Holds the error handler.
     */
    private ErrorHandler _errorHandler;

    /**
     * Holds the pull parser used for parsing.
     */
    private final XmlPullParserImpl _pullParser = new XmlPullParserImpl();

    /**
     * Holds the document locator.
     */
    private final LocatorImpl _locator = new LocatorImpl();

    /**
     * Default constructor.
     */
    public XmlSaxParserImpl() {
        // Sets default handlers.
        setContentHandler(DEFAULT_HANDLER);
        setErrorHandler(DEFAULT_HANDLER);
    }

    /**
     * Allows an application to register a real-time content event handler.
     *
     * <p> If the application does not register a content handler, all
     *     content events reported by the SAX parser will be silently
     *     ignored.</p>
     *
     * <p> Applications may register a new or different handler in the
     *     middle of a parse, and the SAX parser must begin using the new
     *     handler immediately.</p>
     *
     * @param  handler the real-time content handler.
     * @throws NullPointerException if the handler argument is null.
     * @see    #getContentHandler
     */
    public void setContentHandler(ContentHandler handler) {
        if (handler != null) {
            _contentHandler = handler;
        } else {
            throw new NullPointerException();
        }
    }

    /**
     * Returns the current real-time content handler.
     *
     * @return the current real-time content handler, or <code>null</code>
     *         if none has been registered.
     * @see    #setContentHandler
     */
    public ContentHandler getContentHandler() {
        return (_contentHandler == DEFAULT_HANDLER) ? null : _contentHandler;
    }

    /**
     * Allows an application to register an error event handler.
     *
     * <p> If the application does not register an error handler, all
     *     error events reported by the SAX parser will be silently
     *     ignored; however, normal processing may not continue.  It is
     *     highly recommended that all SAX applications implement an
     *     error handler to avoid unexpected bugs.</p>
     *
     * <p> Applications may register a new or different handler in the
     *     middle of a parse, and the SAX parser must begin using the new
     *     handler immediately.</p>
     *
     * @param  handler the error handler.
     * @throws NullPointerException if the handler argument is null.
     * @see    #getErrorHandler
     */
    public void setErrorHandler(ErrorHandler handler) {
        if (handler != null) {
            _errorHandler = handler;
        } else {
            throw new NullPointerException();
        }
    }

    /**
     * Returns the current error handler.
     *
     * @return the current error handler, or <code>null</code> if none
     *         has been registered.
     * @see    #setErrorHandler
     */
    public ErrorHandler getErrorHandler() {
        return (_errorHandler == DEFAULT_HANDLER) ? null : _errorHandler;
    }

    /**
     * Parses an XML document from the specified input stream (UTF-8 encoding).
     *
     * @param in the input stream with UTF-8 encoding.
     * @throws org.xml.sax.SAXException any SAX exception, possibly
     *         wrapping another exception.
     * @throws IOException an IO exception from the parser,
     *         possibly from a byte stream or character stream
     *         supplied by the application.
     * @see    javolution.io.Utf8StreamReader
     */
    public void parse(InputStream in) throws IOException, SAXException {
        _pullParser.setInput(in);
        parseAll();
    }

    /**
     * Parses an XML document from the specified <code>ByteBuffer</code>
     * (UTF-8 encoding).
     *
     * @param  byteBuffer the byte buffer with UTF-8 encoding.
     * @throws org.xml.sax.SAXException any SAX exception, possibly
     *         wrapping another exception.
     * @throws IOException an IO exception from the parser,
     *         possibly from a byte stream or character stream
     *         supplied by the application.
     * @see    javolution.io.Utf8ByteBufferReader
     */
    public void parse(ByteBuffer byteBuffer) throws IOException, SAXException {
        _pullParser.setInput(byteBuffer);
        parseAll();
    }

    /**
     * Parses an XML document using the specified reader.
     *
     * @param  reader the document reader.
     * @throws SAXException any SAX exception, possibly wrapping another
     *         exception.
     * @throws IOException an IO exception from the parser, possibly from
     *         a byte stream or character stream supplied by the application.
     */
    public void parse(Reader reader) throws IOException, SAXException {
        _pullParser.setInput(reader);
        parseAll();
    }

    /**
     * Looks up the value of a feature.
     *
     *  <p> Recognizes <code>http://xml.org/sax/features/namespaces</code>
     *      and the  <code>http://xml.org/sax/features/namespace-prefixes</code>
     *      feature names.</p>
     *
     * @param  name the feature name, which is a fully-qualified URI.
     * @return the current state of the feature (true or false).
     * @throws org.xml.sax.SAXNotRecognizedException when the XMLReader does
     *         not recognize the feature name.
     * @throws org.xml.sax.SAXNotSupportedException when the XMLReader
     *         recognizes the feature name but cannot determine its value
     *         at this time.
     * @see #setFeature
     */
    public boolean getFeature(String name) throws SAXNotRecognizedException,
            SAXNotSupportedException {
        if (name.equals("http://xml.org/sax/features/namespaces")) {
            return true;
        } else if (name
                .equals("http://xml.org/sax/features/namespace-prefixes")) {
            return true;
        } else {
            throw new SAXNotRecognizedException("Feature " + name
                    + " not recognized");
        }
    }

    /**
     * Sets the state of a feature.
     *
     *  <p> Recognizes <code>http://xml.org/sax/features/namespaces</code>
     *      and the  <code>http://xml.org/sax/features/namespace-prefixes</code>
     *      feature names.</p>
     *
     * @param  name the feature name, which is a fully-qualified URI.
     * @param  value the requested state of the feature (true or false).
     * @throws org.xml.sax.SAXNotRecognizedException when the XMLReader does not
     *         recognize the feature name.
     * @throws org.xml.sax.SAXNotSupportedException when the XMLReader
     *         recognizes the feature name but cannot set the requested value.
     * @see    #getFeature
     */
    public void setFeature(String name, boolean value)
            throws SAXNotRecognizedException, SAXNotSupportedException {
        if (name.equals("http://xml.org/sax/features/namespaces")
                || name
                        .equals("http://xml.org/sax/features/namespace-prefixes")) {
            return; // Ignores, these features are always set.
        } else {
            throw new SAXNotRecognizedException("Feature " + name
                    + " not recognized");
        }
    }

    /**
     * Looks up the value of a property.  
     *
     * @param  name the property name, which is a fully-qualified URI.
     * @return the current value of the property.
     * @throws org.xml.sax.SAXNotRecognizedException when the
     *         XMLReader does not recognize the property name.
     * @throws org.xml.sax.SAXNotSupportedException when the
     *         XMLReader recognizes the property name but
     *         cannot determine its value at this time.
     * @see    #setProperty
     */
    public Object getProperty(String name) throws SAXNotRecognizedException,
            SAXNotSupportedException {
        throw new SAXNotRecognizedException("Property " + name
                + " not recognized");
    }

    /**
     * Sets the value of a property. 
     * 
     * @param  name the property name, which is a fully-qualified URI.
     * @param  value the requested value for the property.
     * @throws org.xml.sax.SAXNotRecognizedException when the
     *         XMLReader does not recognize the property name.
     * @throws org.xml.sax.SAXNotSupportedException when the
     *         XMLReader recognizes the property name but
     *         cannot set the requested value.
     */
    public void setProperty(String name, Object value)
            throws SAXNotRecognizedException, SAXNotSupportedException {
        throw new SAXNotRecognizedException("Property " + name
                + " not recognized");
    }

    /**
     * Allows an application to register an entity resolver (ignored by this
     * parser).
     *
     * @param resolver the entity resolver.
     */
    public void setEntityResolver(EntityResolver resolver) {
        _entityResolver = resolver;
    }

    private EntityResolver _entityResolver;

    /**
     * Returns the current entity resolver.
     *
     * @return the current entity resolver, or <code>null</code> if none
     *         has been registered.
     * @see    #setEntityResolver
     */
    public EntityResolver getEntityResolver() {
        return _entityResolver;
    }

    /**
     * Allows an application to register a DTD handler (ignored by this parser).
     *
     * @param handler the DTD handler.
     */
    public void setDTDHandler(DTDHandler handler) {
        _dtdHandler = handler;
    }

    private DTDHandler _dtdHandler;

    /**
     * Returns the current DTD handler.
     *
     * @return the current DTD handler, or <code>null</code> if none
     *         has been registered.
     * @see    #setDTDHandler
     */
    public DTDHandler getDTDHandler() {
        return _dtdHandler;
    }

    // Implements Reusable.
    public void reset() {
        setContentHandler(DEFAULT_HANDLER);
        setErrorHandler(DEFAULT_HANDLER);
        _pullParser.reset();
    }

    /**
     * Parses the whole document using the real-time pull parser.
     * 
     * @throws SAXException any SAX exception, possibly wrapping another
     *         exception.
     * @throws IOException an IO exception from the parser, possibly from
     *         a byte stream or character stream supplied by the application.
     */
    private void parseAll() throws IOException, SAXException {
        try {
            int eventType = _pullParser.getEventType();
            if (eventType != XmlPullParser.START_DOCUMENT)
                throw new SAXException("Currently parsing");
            _contentHandler.startDocument();
            int namespaceCount = 0;

            while (true) {
                eventType = _pullParser.nextToken();
                if (eventType == XmlPullParser.START_TAG) {

                    // Start prefix mapping.
                    final int depth = _pullParser.getDepth();
                    final int nsStart = _pullParser
                            .getNamespaceCount(depth - 1);
                    final int nsEnd = _pullParser.getNamespaceCount(depth);
                    for (int i = nsStart; i < nsEnd; i++) {
                        CharSequence prefix = _pullParser.getNamespacePrefix(i);
                        CharSequence uri = _pullParser.getNamespaceUri(i);
                        _contentHandler.startPrefixMapping(prefix, uri);
                    }

                    // Start element.
                    CharSequence localName = _pullParser.getName();
                    CharSequence uri = _pullParser.getNamespace();
                    CharSequence qName = _pullParser.getQName();
                    Attributes atts = _pullParser.getSaxAttributes();
                    _contentHandler.startElement(uri, localName, qName, atts);

                    if (_pullParser.isEmptyElementTag()) { // Empty tag.
                        _contentHandler.endElement(uri, localName, qName);
                        for (int i = nsStart; i < nsEnd; i++) {
                            CharSequence prefix = _pullParser
                                    .getNamespacePrefix(i);
                            _contentHandler.endPrefixMapping(prefix);
                        }
                    }

                } else if (eventType == XmlPullParser.END_TAG) {

                    // End element.
                    CharSequence localName = _pullParser.getName();
                    CharSequence uri = _pullParser.getNamespace();
                    CharSequence qName = _pullParser.getQName();
                    _contentHandler.endElement(uri, localName, qName);

                    // Prefix unmapping.
                    final int depth = _pullParser.getDepth();
                    final int nsStart = _pullParser.getNamespaceCount(depth);
                    final int nsEnd = _pullParser.getNamespaceCount(depth + 1);
                    for (int i = nsStart; i < nsEnd; i++) {
                        CharSequence prefix = _pullParser.getNamespacePrefix(i);
                        _contentHandler.endPrefixMapping(prefix);
                    }

                } else if ((eventType == XmlPullParser.TEXT)
                        || (eventType == XmlPullParser.CDSECT)) {
                    char ch[] = _pullParser.getTextCharacters(_startLength);
                    _contentHandler.characters(ch, _startLength[0],
                            _startLength[1]);

                } else if (eventType == XmlPullParser.END_DOCUMENT) {
                    break;

                } else {
                    // Ignores.
                }
            }
        } catch (XmlPullParserException e) {
            SAXParseException error = new SAXParseException(e.getMessage(),
                    _locator);
            _errorHandler.fatalError(error);

        } finally { // Always executed.
            _contentHandler.endDocument();
            reset();
        }
    }

    int[] _startLength = new int[2];

    /**
     * Inner class implements Locator interface.
     */
    private class LocatorImpl implements Locator {
        public String getPublicId() {
            return null;
        }

        public String getSystemId() {
            return null;
        }

        public int getLineNumber() {
            return _pullParser.getLineNumber();
        }

        public int getColumnNumber() {
            return _pullParser.getColumnNumber();
        }
    }
}