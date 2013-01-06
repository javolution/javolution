/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.xml.sax;


import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import javolution.text.CharArray;
import javolution.xml.stream.XMLStreamConstants;
import javolution.xml.stream.XMLStreamException;
import javolution.xml.stream.XMLStreamReaderImpl;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

/**
 * <p> This class provides a real-time SAX2-like XML parser; this parser is
 *     <i>extremely</i> fast and <b>does not create temporary objects</b>
 *     (no garbage generated and no GC interruption).</p>
 *     
 * <p> The parser is implemented as a SAX2 wrapper around  
 *     {@link XMLStreamReaderImpl} and share the same characteristics.</p>
 *
 * <p><i> Note: This parser is a <b>SAX2-like</b> parser with the
 *        <code>java.lang.String</code> type replaced by 
 *        {@link CharArray}/{@link CharSequence} in the {@link ContentHandler},
 *       {@link Attributes} interfaces and {@link DefaultHandler} base class.
 *       If a standard SAX2 or JAXP parser is required, you may consider using
 *       the wrapping class {@link SAX2ReaderImpl}. Fast but not as fast as 
 *       <code>java.lang.String</code> instances are dynamically allocated
 *       while parsing.</i></p>
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 4.0, June 16, 2006
 */
public class XMLReaderImpl implements XMLReader {

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
     * Holds reusable StAX reader.
     */
    private final XMLStreamReaderImpl _xmlReader = new XMLStreamReaderImpl();

    /**
     * Default constructor.
     */
    public XMLReaderImpl() {
        // Sets default handlers.
        setContentHandler(DEFAULT_HANDLER);
        setErrorHandler(DEFAULT_HANDLER);
    }

    /**
     * Parses an XML document from the specified input stream 
     * (encoding retrieved from input source and the XML prolog if any).
     *
     * @param in the input stream with unknown encoding.
     * @throws org.xml.sax.SAXException any SAX exception, possibly
     *         wrapping another exception.
     * @throws IOException an IO exception from the parser,
     *         possibly from a byte stream or character stream
     *         supplied by the application.
     */
    public void parse(InputStream in) throws IOException, SAXException {
        try {
            _xmlReader.setInput(in);
            parseAll();
        } catch (XMLStreamException e) {
            if (e.getNestedException() instanceof IOException) 
                throw (IOException)e.getNestedException();
            throw new SAXException(e.getMessage());
        } finally {
            _xmlReader.reset();
        }
    }
    
    /**
     * Parses an XML document from the specified input stream and encoding.
     *
     * @param in the input stream.
     * @param encoding the input stream encoding.
     * @throws org.xml.sax.SAXException any SAX exception, possibly
     *         wrapping another exception.
     * @throws IOException an IO exception from the parser,
     *         possibly from a byte stream or character stream
     *         supplied by the application.
     */
    public void parse(InputStream in, String encoding) throws IOException, SAXException {
        try {
            _xmlReader.setInput(in, encoding);
            parseAll();
        } catch (XMLStreamException e) {
            if (e.getNestedException() instanceof IOException) 
                throw (IOException)e.getNestedException();
            throw new SAXException(e.getMessage());
        } finally {
            _xmlReader.reset();
        }
    }
    
    /**
     * Parses an XML document using the specified reader.
     *
     * @param  reader the document reader.
     * @throws SAXException any SAX exception, possibly wrapping another
     *         exception.
     * @throws IOException an IO exception from the parser, possibly from
     *         a byte stream or character stream supplied by the application.
     * @see    javolution.io.UTF8StreamReader
     * @see    javolution.io.UTF8ByteBufferReader
     * @see    javolution.io.CharSequenceReader
     */
    public void parse(Reader reader) throws IOException, SAXException {
        try {
            _xmlReader.setInput(reader);
            parseAll();
        } catch (XMLStreamException e) {
            if (e.getNestedException() instanceof IOException) 
                throw (IOException)e.getNestedException();
            throw new SAXException(e.getMessage());
        } finally {
            _xmlReader.reset();
        }
    }

    // Implements XMLReader interface.
    public void parse(InputSource input) throws IOException, SAXException {
        Reader reader = input.getCharacterStream();
        if (reader != null) {
            parse(reader);
        } else {
            InputStream inStream = input.getByteStream();
            if (inStream != null) {
                parse(inStream, input.getEncoding());
            } else {
                parse(input.getSystemId());
            }
        }
    }

    // Implements XMLReader interface.
    public void parse(String systemId) throws IOException, SAXException {
        InputStream inStream;
        try {
            URL url = new URL(systemId);
            inStream = url.openStream();
        } catch (Exception urlException) { // Try as filename.
            try {
                inStream = new FileInputStream(systemId);
            } catch (Exception fileException) {
                throw new UnsupportedOperationException("Cannot parse "
                        + systemId);
            }
        }
        parse(inStream);
    }

    // Implements XMLReader interface.
    public void setContentHandler(ContentHandler handler) {
        if (handler != null) {
            _contentHandler = handler;
        } else {
            throw new NullPointerException();
        }
    }

    // Implements XMLReader interface.
    public ContentHandler getContentHandler() {
        return (_contentHandler == DEFAULT_HANDLER) ? null : _contentHandler;
    }

    // Implements XMLReader interface.
    public void setErrorHandler(ErrorHandler handler) {
        if (handler != null) {
            _errorHandler = handler;
        } else {
            throw new NullPointerException();
        }
    }

    // Implements XMLReader interface.
    public ErrorHandler getErrorHandler() {
        return (_errorHandler == DEFAULT_HANDLER) ? null : _errorHandler;
    }

    // Implements XMLReader interface.
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

    public Object getProperty(String name) throws SAXNotRecognizedException,
            SAXNotSupportedException {
        throw new SAXNotRecognizedException("Property " + name
                + " not recognized");
    }

    public void setProperty(String name, Object value)
            throws SAXNotRecognizedException, SAXNotSupportedException {
        throw new SAXNotRecognizedException("Property " + name
                + " not recognized");
    }

    public void setEntityResolver(EntityResolver resolver) {
        _entityResolver = resolver;
    }

    private EntityResolver _entityResolver;

    public EntityResolver getEntityResolver() {
        return _entityResolver;
    }

    public void setDTDHandler(DTDHandler handler) {
        _dtdHandler = handler;
    }

    private DTDHandler _dtdHandler;

    public DTDHandler getDTDHandler() {
        return _dtdHandler;
    }

    // Implements Reusable.
    public void reset() {
        setContentHandler(DEFAULT_HANDLER);
        setErrorHandler(DEFAULT_HANDLER);
        _xmlReader.reset();
    }

    /**
     * Parses the whole document using the real-time pull parser.
     * 
     * @throws SAXException any SAX exception, possibly wrapping another
     *         exception.
     * @throws IOException an IO exception from the parser, possibly from
     *         a byte stream or character stream supplied by the application.
     */
    private void parseAll() throws XMLStreamException, SAXException {
        int eventType = _xmlReader.getEventType();
        if (eventType != XMLStreamConstants.START_DOCUMENT)
            throw new SAXException("Currently parsing");
        _contentHandler.startDocument();

        boolean doContinue = true;
        while (doContinue) {
            CharArray uri, localName, qName, prefix, text;
            switch (_xmlReader.next()) {
            case XMLStreamConstants.START_ELEMENT:

                // Start prefix mapping.
                for (int i = 0, count = _xmlReader.getNamespaceCount(); i < count; i++) {
                    prefix = _xmlReader.getNamespacePrefix(i);
                    prefix = (prefix == null) ? NO_CHAR : prefix; // Default namespace is "" 
                    uri = _xmlReader.getNamespaceURI(i);
                    _contentHandler.startPrefixMapping(prefix, uri);
                }

                // Start element.
                uri = _xmlReader.getNamespaceURI();
                uri = (uri == null) ? NO_CHAR : uri;
                localName = _xmlReader.getLocalName();
                qName = _xmlReader.getQName();
                _contentHandler.startElement(uri, localName, qName, _xmlReader
                        .getAttributes());
                break;

            case XMLStreamConstants.END_ELEMENT:

                // End element.
                uri = _xmlReader.getNamespaceURI();
                uri = (uri == null) ? NO_CHAR : uri;
                localName = _xmlReader.getLocalName();
                qName = _xmlReader.getQName();
                _contentHandler.endElement(uri, localName, qName);

                // End prefix mapping.
                for (int i = 0, count = _xmlReader.getNamespaceCount(); i < count; i++) {
                    prefix = _xmlReader.getNamespacePrefix(i);
                    prefix = (prefix == null) ? NO_CHAR : prefix; // Default namespace is "" 
                    _contentHandler.endPrefixMapping(prefix);
                }
                break;

            case XMLStreamConstants.CDATA:
            case XMLStreamConstants.CHARACTERS:
                text = _xmlReader.getText();
                _contentHandler.characters(text.array(), text.offset(), text
                        .length());
                break;

            case XMLStreamConstants.SPACE:
                text = _xmlReader.getText();
                _contentHandler.ignorableWhitespace(text.array(),
                        text.offset(), text.length());
                break;

            case XMLStreamConstants.PROCESSING_INSTRUCTION:
                _contentHandler.processingInstruction(
                        _xmlReader.getPITarget(), _xmlReader.getPIData());
                break;

            case XMLStreamConstants.COMMENT:
                // Ignores.
                break;

            case XMLStreamConstants.END_DOCUMENT:
                doContinue = false;
                _xmlReader.close();
                break;

            }
        }
    }
    
    private static final CharArray NO_CHAR = new CharArray("");
}