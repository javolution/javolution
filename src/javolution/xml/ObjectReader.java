/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2004 - The Javolution Team (http://javolution.org/)
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.xml;

import j2me.nio.ByteBuffer;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import javolution.lang.Reusable;
import javolution.xml.sax.RealtimeParser;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * <p> This class restores objects which have been serialized in XML
 *     format using an {@link ObjectWriter}.</p>
 * <p> When the XML document is parsed, each elements are recursively
 *     processed and Java objects are created using the {@link XmlFormat}
 *     of the class identified by the name of the XML element. 
 *     The final object constructed (and returned) is always the root element
 *     of the XML input source.</p>
 * <p> Processing instructions are ignored, but namespaces may be used to
 *     specify package names (java addressing scheme).</p>
 * <p> Non-blank character data of the XML document are represented 
 *     by {@link CharacterData} instances.</p>
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 2.0, December 9, 2004
 */
public class ObjectReader implements Reusable {

    /**
     * Holds the real-time parser used.
     */
    private final RealtimeParser _parser;

    /**
     * Holds the constructor handler.
     */
    private final ConstructorHandler _handler;

    /**
     * Creates an object reader of default capacity.
     */
    public ObjectReader() {
        this(2048);
    }
    
    /**
     * Creates an object reader having the specified buffer capacity.
     * 
     * @param capacity the buffer capacity in characters. 
     */
    public ObjectReader(int capacity) {
        _parser = new RealtimeParser(capacity);
        _handler = new ConstructorHandler();
        _parser.setContentHandler(_handler);
    }
    
    /**
     * Returns a new object reader.
     * 
     * @return <code>new ObjectReader()</code>
     * @deprecated replaced by {@link #ObjectReader()} or 
     *             {@link #ObjectReader(int)}
     */
    public static ObjectReader newInstance() {
        return new ObjectReader();
    }

    /**
     * Clears all internal data maintained by this reader. Objects previously
     * read cannot be refered to, they will have to be send again.
     */
    public void clear() {
        _handler.clear();
        _parser.clear();
        _parser.setContentHandler(_handler);
    }

    /**
     * Creates an object from its XML representation read from
     * the specified <code>Reader</code>. This method reads until the  
     * end of stream; to read multiple objects over a persistent connection
     * {@link XmlInputStream} should be used instead.
     *
     * @param  reader the reader containing the XML representation of the
     *         object being created.
     * @return the object corresponding to the xml root element.
     * @throws XmlException if the object cannot be created.
     */
    public Object read(Reader reader) throws XmlException {
        try {
            _parser.parse(reader);
            return getRootObject();
        } catch (SAXParseException e1) {
            String message;
            message = e1.getMessage() + " (" + "line " + e1.getLineNumber()
                    + ", column " + e1.getColumnNumber() + ")";
            throw new XmlException(message);
        } catch (SAXException e2) {
            throw new XmlException(e2);
        } catch (IOException e3) {
            throw new XmlException(e3);
        }
    }

    /**
     * Creates an object from its XML representation read from
     * the specified <code>InputStream</code>. This method reads until the  
     * end of stream; to read multiple objects over a persistent connection
     * {@link XmlInputStream} should be used instead.
     *
     * @param  in the input stream containing the XML representation of the
     *         object being created.
     * @return the object corresponding to the xml root element.
     * @throws XmlException if the object cannot be created.
     */
    public Object read(InputStream in) throws XmlException {
        try {
            _parser.parse(in);
            return getRootObject();
        } catch (SAXParseException e1) {
            String message;
            message = e1.getMessage() + " (" + "line " + e1.getLineNumber()
                    + ", column " + e1.getColumnNumber() + ")";
            throw new XmlException(message);
        } catch (SAXException e2) {
            throw new XmlException(e2);
        } catch (IOException e3) {
            throw new XmlException(e3);
        }
    }

    /**
     * Creates an object from its XML representation read from
     * the specified <code>ByteBuffer</code>. This method reads from 
     * the current buffer position up to the buffer's limit.
     *
     * @param  byteBuffer the byte buffer containing the XML representation 
     *         of the object being created.
     * @return the object corresponding to the xml root element.
     * @throws XmlException if the object cannot be created.
     */
    public Object read(ByteBuffer byteBuffer) throws XmlException {
        try {
            _parser.parse(byteBuffer);
            return getRootObject();
        } catch (SAXParseException e1) {
            String message;
            message = e1.getMessage() + " (" + "line " + e1.getLineNumber()
                    + ", column " + e1.getColumnNumber() + ")";
            throw new XmlException(message);
        } catch (SAXException e2) {
            throw new XmlException(e2);
        } catch (IOException e3) {
            throw new XmlException(e3);
        }
    }

    /**
     * Returns the object corresponding to the xml root element.
     * 
     * @return the object being deserialized.
     * @throws XmlException if the object cannot be created.
     */
    private Object getRootObject() throws XmlException {
        if (_handler.getRoots().size() == 1) {
            return _handler.getRoots().get(0);
        } else if (_handler.getRoots().size() < 1) {
            throw new XmlException("Parsing Incomplete");
        } else  {
            throw new XmlException("More than one root element.");
        }
    }
}