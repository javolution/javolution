/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2004 - The Javolution Team (http://javolution.org/)
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.xml;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.util.List;

import javolution.realtime.RealtimeObject;
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
 *     by {@link javolution.lang.Text Text} instances.</p>
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 1.0, October 4, 2004
 */
public class ObjectReader extends RealtimeObject {

    /**
     * Holds the real-time parser used.
     */
    private final RealtimeParser _parser;

    /**
     * Holds the constructor handler.
     */
    private final ConstructorHandler _handler;

    /**
     * Returns a new object reader (potentially allocated on the stack).
     * 
     * @return a new or recycled object reader.
     */
    public static ObjectReader newInstance() {
        return (ObjectReader) FACTORY.object();
    }

    private static final Factory FACTORY = new Factory() {
        public Object create() {
            return new ObjectReader();
        }

        public void cleanup(Object obj) {
            ObjectReader or = (ObjectReader) obj;
            or._handler._idToObject.clear();
        }
    };

    /**
     * Default constructor.
     */
    public ObjectReader() {
        _parser = new RealtimeParser();
        _handler = new ConstructorHandler();
        _parser.setContentHandler(_handler);
    }

    /**
     * Clears the internal object references maintained by this reader.
     * Objects previously read cannot be refered to, they will have to 
     * be send again.
     */
    public void reset() {
        _handler._idToObject.clear();
    }

    /**
     * Creates an object from its XML representation read from
     * the specified <code>Reader</code>.
     *
     * @param  reader the reader containing the XML representation of the
     *         object being created.
     * @return the object corresponding to the first xml root element.
     * @throws XmlException if the object cannot be created.
     */
    public Object read(Reader reader) throws XmlException {
        try {
            _parser.parse(reader);
            if (getRoots().size() > 0) {
            	return getRoots().get(0);
            } else {
                throw new XmlException("Parsing Incomplete");
            }
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
     * the specified <code>InputStream</code>.
     *
     * @param  in the input stream containing the XML representation of the
     *         object being created.
     * @return the object corresponding to the first xml root element.
     * @throws XmlException if the object cannot be created.
     */
    public Object read(InputStream in) throws XmlException {
        try {
            _parser.parse(in);
            if (getRoots().size() > 0) {
            	return getRoots().get(0);
            } else {
                throw new XmlException("Parsing Incomplete");
            }
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
     * the specified <code>ByteBuffer</code>.
     *
     * @param  byteBuffer the byte buffer containing the XML representation 
     *         of the object being created.
     * @return the object corresponding to the first xml root element.
     * @throws XmlException if the object cannot be created.
     */
    public Object read(ByteBuffer byteBuffer) throws XmlException {
        try {
            _parser.parse(byteBuffer);
            if (getRoots().size() > 0) {
            	return getRoots().get(0);
            } else {
                throw new XmlException("Parsing Incomplete");
            }
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
     * Returns a view of the current root objects for the document being parsed.
     * This method allows for reading more than one xml root element.
     * 
     * @return the current list of root objects.
     */
    public List getRoots() {
    	return _handler.getRoots();
    }
}