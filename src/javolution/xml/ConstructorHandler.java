/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2005 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.xml;

import j2me.util.List;
import j2me.lang.CharSequence;
import javolution.lang.Reusable;
import javolution.lang.TextBuilder;
import javolution.util.FastList;
import javolution.util.FastMap;
import javolution.xml.sax.Attributes;
import javolution.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * <p> This class handles SAX2 events in order to build objects from 
 *     their xml representation.</p>
 *     
 * <p> For example, the following formats and parses an object without 
 *     intermediate xml document:<pre>
 *          ConstructorHandler ch = new ConstructorHandler();
 *          ObjectWriter.write(obj, ch); 
 *          assert(obj.equals(ch.getRoot()));
 *     </pre></p>
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 2.2, January 8, 2005
 */
public final class ConstructorHandler implements ContentHandler, ErrorHandler, 
		Reusable {

    /**
     * Holds the current nesting level (0 is the document level)
     */
    private int _level;

    /**
     * Holds the document locator.
     */
    private Locator _locator;

    /**
     * Holds the root object.
     */
    private Object _root;

    /**
     * Holds the stack of XML elements (nesting limited to 32).
     */
    private final XmlElement[] _stack = new XmlElement[32];

    /**
     * Holds the identifier value to object mapping (TextBuilder to Object).
     */
    private final FastMap _idToObject = new FastMap();

    /**
     * Holds a pool of TextBuilder instances (for identifiers).
     */
    private FastList _textBuilderPool = new FastList();
    
    /**
     * Default constructor.
     */
    ConstructorHandler() {
        _stack[0] = new XmlElement();
        for (int i = 1; i < _stack.length; i++) {
            _stack[i] = new XmlElement();
            _stack[i]._parent = _stack[i - 1];
        }
    }

    /**
     * Receive an object for locating the origin of SAX document events.
     *
     * @param  locator an object that can return the location of any SAX
     *         document event.
     */
    public void setDocumentLocator(Locator locator) {
        _locator = locator;
    }

    /**
     * Returns the root object.
     * 
     * @return the object corresponding to the root xml element.
     */
    public Object getRoot() {
        return _root;
    }

    /**
     * Receives notification of the beginning of the document.
     *
     * @throws SAXException any SAX exception, possibly wrapping
     *         another exception.
     */
    public void startDocument() throws SAXException {
        _level = 0;
        _root = null;
    }

    /**
     * Receives notification of the end of the document.
     *
     * @throws SAXException any SAX exception, possibly wrapping
     *         another exception.
     */
    public void endDocument() throws SAXException {
        List roots = _stack[0].getContent();
        if (roots.size() > 0) {
            _root = roots.get(0);
        }
        // Clean-up (e.g. if parsing failed)
        for (int i = 0; i <= _level;) {
           _stack[i++].reset();
        }
    }

    /**
     * Receives notification of the start of an element.
     *
     * @param  uri the namespace.
     * @param  localName the local name.
     * @param  qName the raw XML 1.0 name.
     * @param  attributes the attributes.
     * @throws SAXException any SAX exception, possibly wrapping
     *         another exception.
     */
    public void startElement(CharSequence uri, CharSequence localName,
            CharSequence qName, Attributes attributes) throws SAXException {
        _accessId.uri = uri;
        _accessId.localName = localName;
        final XmlElement xml = _stack[++_level];
        final Class objectClass = XmlFormat.classFor(_accessId);
        final XmlFormat xmlFormat = XmlFormat.getInstance(objectClass);
        final int attLength = attributes.getLength();

        // Checks for references.
        if ((attLength == 1) && 
                (attributes.getQName(0).equals(xmlFormat.identifier(true)))) {
            final CharSequence idValue = attributes.getValue(0);
            xml._object = _idToObject.get(idValue);
            if (xml._object == null) {
                    throw new SAXException(
                       "Referenced object (" + idValue + ") not found");
            }
            return; // Reference found.
        }    

        // Reads attributes.
        String idName = xmlFormat.identifier(false);
        for (int i = 0; i < attLength; i++) {
            CharSequence key = attributes.getQName(i);
            CharSequence value = attributes.getValue(i);
            xml._attributes.add(key, value);
            if (key.equals(idName)) { // Identifier.
                xml._idValue = value;
            }
        }

        xml._format = xmlFormat;
        xml._objectClass = objectClass;
        xml._object = xmlFormat.preallocate(xml);
        
        // If preallocated and identifier, then maps id to object.
        if ((xml._object != null) && (xml._idValue != null)) {
            _idToObject.put(newTextBuilder(xml._idValue), xml._object);
        }
    }
    private XmlFormat.Identifier _accessId = new XmlFormat.Identifier();

    /**
     * Receives notification of the end of an element.
     *
     * @param  uri the namespace.
     * @param  localName the local name.
     * @param  qName the raw XML 1.0 name.
     * @throws SAXException any SAX exception, possibly wrapping
     *         another exception.
     */
    public void endElement(CharSequence uri, CharSequence localName,
            CharSequence qName) throws SAXException {
        XmlElement xml = _stack[_level];
        if (xml._format == null) { // Reference.
            if (xml._content.size() != 0) {
                throw new SAXException("Non-empty reference element");
            }
            _stack[--_level]._content.add(xml._object);
        } else {
            Object obj = xml._format.parse(xml);
            if (xml._idValue != null) {
                _idToObject.put(newTextBuilder(xml._idValue), obj);
            }
            _stack[--_level]._content.add(obj);
        }

        // Clears the xml element (for reuse latter).
        xml.reset();
    }

    /**
     * Receives notification of character data.
     *
     * @param ch the characters from the XML document.
     * @param start the start position in the array.
     * @param length the number of characters to read from the array.
     * @throws SAXException any SAX exception, possibly wrapping
     *         another exception.
     */
    public void characters(char ch[], int start, int length)
            throws SAXException {
        for (int i = start + length; i > start;) {
            if (ch[--i] > ' ') {
                CharacterData cd = CharacterData.valueOf(ch, start, length);
                _stack[_level].getContent().add(cd);
                break;
            }
        }
    }

    // Implements ContentHandler
    public void startPrefixMapping(CharSequence prefix, CharSequence uri)
            throws SAXException {
    }

    // Implements ContentHandler
    public void endPrefixMapping(CharSequence prefix) throws SAXException {
    }

    // Implements ContentHandler
    public void ignorableWhitespace(char ch[], int start, int length)
            throws SAXException {
    }

    // Implements ContentHandler
    public void processingInstruction(CharSequence target, CharSequence data)
            throws SAXException {
    }

    // Implements ContentHandler
    public void skippedEntity(CharSequence name) throws SAXException {
    }

    /**
     * Receives notification of a parser warning. The warning is printed to
     * the error stream (System.err)
     *
     * @param e the warning information encoded as an exception.
     * @throws SAXException any SAX exception, possibly wrapping
     *         another exception.
     */
    public void warning(SAXParseException e) throws SAXException {
        System.err.println("XML Parsing Warning: " + e);
    }

    /**
     * Receive notification of a recoverable parser error.
     * This implementation throws a XmlException.
     *
     * @param e the error encoded as an exception.
     * @throws SAXException any SAX exception, possibly wrapping
     *         another exception.
     */
    public void error(SAXParseException e) throws SAXException {
        throw e;
    }

    /**
     * Report a fatal XML parsing error.
     *
     * @param e the error information encoded as an exception.
     * @throws SAXException any SAX exception, possibly wrapping
     *         another exception.
     */
    public void fatalError(SAXParseException e) throws SAXException {
        throw e;
    }

    // Implements Reusable.
    public void reset() {
        _textBuilderPool.addAll(_idToObject.keySet()); // Recycle ids.
        _idToObject.clear();
        _root = null;
    }

    /**
     * Returns a new TextBuilder instance (recycled) initialized 
     * with the specified characters.  
     */
    private TextBuilder newTextBuilder(CharSequence chars) {
        TextBuilder tb = (_textBuilderPool.size() == 0) ?
             new TextBuilder() : (TextBuilder) _textBuilderPool.removeLast();
        tb.reset();
        tb.append(chars);
        return tb;
    }
}