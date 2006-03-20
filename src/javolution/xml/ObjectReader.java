/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2005 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.xml;

import j2me.nio.ByteBuffer;

import java.io.InputStream;
import java.io.Reader;

import javolution.lang.Reusable;
import javolution.xml.pull.XmlPullParser;
import javolution.xml.pull.XmlPullParserException;
import javolution.xml.pull.XmlPullParserImpl;

/**
 * <p> This class restores objects which have been serialized in XML
 *     format using an {@link ObjectWriter}.</p>
 *     
 * <p> When the XML document is parsed, each elements are recursively
 *     processed and Java objects are created using the {@link XmlFormat}
 *     of the class identified by the name of the XML element. 
 *     The final object constructed (and returned) is always the root element
 *     of the XML input source.</p>
 *     
 * <p> Non-blank character data of the XML document are represented 
 *     by {@link CharacterData} instances.</p>
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 3.6, October 13, 2005
 */
public class ObjectReader /*<T>*/ implements Reusable {

    /**
     * Hold the xml element used when parsing.
     */
    private final XmlElement _xml;

    /**
     * Default constructor.
     */
    public ObjectReader() {
        _xml = new XmlElement(new XmlPullParserImpl());
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
    public Object/*T*/ read(Reader reader) throws XmlException {
        _xml._parser.setInput(reader);
        return (Object/*T*/) parse();
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
    public Object/*T*/ read(InputStream in) throws XmlException {
        _xml._parser.setInput(in);
        return (Object/*T*/) parse();
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
    public Object/*T*/ read(ByteBuffer byteBuffer) throws XmlException {
        _xml._parser.setInput(byteBuffer);
        return (Object/*T*/) parse();
    }

    private Object parse() throws XmlException {
        try {
            _xml._parser.setFeature(_xml._parser.FEATURE_IGNORE_WHITESPACE, true);
            _xml._areReferencesEnabled = true; // Enabled by default.
            Object obj = _xml.getNext();
            if (_xml.hasNext() || (_xml._parser.getEventType() != XmlPullParser.END_DOCUMENT))
                throw new XmlException("End Document Event Expected");
            return obj;

        } catch (XmlPullParserException e1) {
            throw new XmlException(e1);
        } finally {
            reset();
        }
    }

    /**
     * Resets this object reader; objects previously read cannot be refered to,
     * they will have to be send again.
     */
    public void reset() {
        _xml.reset();
    }
}