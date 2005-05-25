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
import javolution.lang.Text;
import javolution.lang.TextBuilder;
import javolution.util.FastComparator;
import javolution.util.FastMap;
import javolution.util.FastTable;
import javolution.xml.sax.Attributes;
import javolution.xml.sax.AttributesImpl;
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
 * @version 3.2, March 20, 2005
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
     * Holds the stack of XML elements.
     */
    private final FastTable _stack = new FastTable();

    /**
     * Holds the persistent id to object mapping.
     */
    private final FastMap _idToObject = new FastMap()
            .setKeyComparator(FastComparator.LEXICAL);

    /**
     * Default constructor.
     */
    ConstructorHandler() {
        _stack.add(new XmlElement());
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
        List roots = ((XmlElement)_stack.get(0)).getContent();
        if (roots.size() > 0) {
            _root = roots.get(0);
        }
        // Clean-up (e.g. if parsing failed)
        for (int i = 0; i <= _level;) {
			((XmlElement)_stack.get(i++)).reset();
        }
    }

    /**
     * Receives notification of the start of an element.
     *
     * @param  uri the namespace.
     * @param  localName the local name.
     * @param  qName the raw XML 1.0 name.
     * @param  attrs the attributes (instance of AttributesImpl).
     * @throws SAXException any SAX exception, possibly wrapping
     *         another exception.
     */
    public void startElement(CharSequence uri, CharSequence localName,
            CharSequence qName, Attributes attrs) throws SAXException {

		if (++_level >= _stack.size()) {
            XmlElement tmp = (XmlElement) XmlElement.FACTORY.newObject();
            tmp._parent = (XmlElement) _stack.get(_level - 1);
            _stack.add(tmp);
        }
        XmlElement xml = (XmlElement) _stack.get(_level);
  
        // Searches if attribute "j:class" is specified.
        final AttributesImpl attributes = (AttributesImpl) attrs;
        int i = attributes.getIndex("j:class");
        if (i >= 0) { // Class name from attribute.
            _uriLocalName.uri = "http://javolution.org";
            _uriLocalName.localName = attributes.getValue(i);
            xml._name = localName;
        } else { // Class name from element tag.
            _uriLocalName.uri = uri;
            _uriLocalName.localName = localName;
        }
        
        // Retrieves class and associated xml format. 
        final Class objectClass = XmlFormat.classFor(_uriLocalName);
        final XmlFormat xmlFormat = XmlFormat.getInstance(objectClass);
        final int attLength = attributes.getLength();

        // Checks for references.
        if (xmlFormat._idRef != null) {
            int j = attributes.getIndex(xmlFormat._idRef);
            if (j >= 0) { // Holds id reference.
                final CharSequence idValue = attributes.getValue(j);
                xml._object = _idToObject.get(idValue);
                if (xml._object != null) return; // Reference found.
                if (!xmlFormat._idRef.equals(xmlFormat._idName))  
                    throw new SAXException("Referenced object (" + idValue
                            + ") not found");
                // Use the same attribute for identifiers and references.
            }
        }

        // Setup xml element.
        xml._attributes = attributes;
        if (xmlFormat._idName != null) {
            xml._idValue = attributes.getValue(xmlFormat._idName);
        }
        xml._format = xmlFormat;
        xml._objectClass = objectClass;
        xml._object = xmlFormat.preallocate(xml);

        // If preallocated and identifier, then maps id to object.
        if ((xml._object != null) && (xml._idValue != null)) {
            _idToObject.put(newId().append(xml._idValue), xml._object);
        }
    }

    private XmlFormat.UriLocalName _uriLocalName = new XmlFormat.UriLocalName();

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
        XmlElement xml = ((XmlElement)_stack.get(_level));
        if (xml._format != null) { // Not a reference.
            xml._object = xml._format.parse(xml);
            if (xml._idValue != null) {
                _idToObject.put(newId().append(xml._idValue), xml._object);
            }
        }
        
        // Adds to parent.
        if (xml._name == null) { // Anonymous.
			((XmlElement)_stack.get(--_level))._content.add(xml._object);
        } else { // Named child element.
			((XmlElement)_stack.get(--_level)).add(xml._name, xml._object);
        }

        // Clears the xml element (for reuse latter).
        xml.reset();
    }

    /**
     * Receives notification of (non whitespace) character data.
     *
     * @param ch the characters from the XML document.
     * @param start the start position in the array.
     * @param length the number of characters to read from the array.
     * @throws SAXException any SAX exception, possibly wrapping
     *         another exception.
     */
    public void characters(char ch[], int start, int length)
            throws SAXException {
        CharacterData charData = CharacterData.valueOf(
                Text.valueOf(ch, start, length));
        ((XmlElement)_stack.get(_level)).getContent().add(charData);
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
        // Recycles the ids.
        _idPool.addAll(_idToObject.keySet());
        _idToObject.clear();
        _root = null;
    }

    /**
     * Returns a persistent mutable character sequence from a local pool.
     * 
     * @return a new or recycled text builder instance.
     */
    private TextBuilder newId() {
        if (_idPool.isEmpty()) 
            return (TextBuilder) TextBuilder.newInstance().moveHeap();
        TextBuilder tb = (TextBuilder) _idPool.removeLast();
        tb.reset();
        return tb;
    }
    private FastTable _idPool = new FastTable(); // Persistent pool.
}