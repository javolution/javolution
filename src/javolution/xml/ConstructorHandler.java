/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2004 - The Javolution Team (http://javolution.org/)
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
import javolution.util.FastMap;
import javolution.xml.sax.Attributes;
import javolution.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * <p> This class handles SAX2 events during the construction process.</p>
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 1.0, October 4, 2004
 * @see     ObjectReader
 */
final class ConstructorHandler implements ContentHandler, ErrorHandler, Reusable {

    /**
     * Holds the current nesting level (0 is the document level)
     */
    private int _level;

    /**
     * Holds the document locator.
     */
    private Locator _locator;

    /**
     * Holds the stack of XML elements (nesting limited to 64).
     */
    private final XmlElement[] _stack = new XmlElement[64];

    /**
     * Holds the identifier value to object mapping.
     */
    private final FastMap _idToObject = new FastMap()
        .setKeyComparator(FastMap.KeyComparator.CHAR_SEQUENCE);

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
     * Returns the root objects (typically one).
     * 
     * @param  the root objects.
     */
    public List getRoots() {
        return _stack[0];
    }

    /**
     * Receives notification of the beginning of the document.
     *
     * @throws SAXException any SAX exception, possibly wrapping
     *         another exception.
     */
    public void startDocument() throws SAXException {
        _level = 0;
        _stack[0].reset();
    }

    /**
     * Receives notification of the end of the document.
     *
     * @throws SAXException any SAX exception, possibly wrapping
     *         another exception.
     */
    public void endDocument() throws SAXException {
        // Clean-up (e.g. if parsing failed)
        for (int i = 1; i <= _level; i++) {
            _stack[i].reset();
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
        final XmlElement xml = _stack[++_level];
        final Class objectClass = classFor(uri, localName);
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
            xml.setAttribute(key, value);
            if (key.equals(idName)) { // Identifier.
                xml._id = value;
            }
        }

        xml._format = xmlFormat;
        xml._objectClass = objectClass;
        xml._object = xmlFormat.preallocate(xml);
        
        // If preallocated and identifier, then maps id to object.
        if ((xml._object != null) && (xml._id != null)) {
            _idToObject.put(Text.valueOf(xml._id), xml._object);
        }
    }

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
            if (xml.size() != 0) {
                throw new SAXException("Non-empty reference element");
            }
            _stack[--_level].add(xml._object);
        } else {
            Object obj = xml._format.parse(xml);
            if (xml._id != null) {
                _idToObject.put(Text.valueOf(xml._id), obj);
            }
            _stack[--_level].add(obj);
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
                _stack[_level].add(cd);
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

    /**
     * Returns the class for the specified Java URI and local name.
     *
     * @param  uri the package name.
     * @param  localName the relative name of the class.
     * @return the corresponding class.
     * @throws SAXException invalid URI (must use a java scheme).
     */
    private Class classFor(CharSequence uri, CharSequence localName)
            throws SAXException {
        // Searches current mapping.
        _accessKey._uri = uri;
        _accessKey._localName = localName;
        Class cl = (Class) _keyToClass.get(_accessKey);
        if (cl != null) {
            return cl;
        }
        // Adds new mapping.
        Key key = new Key();
        key._uri = Text.valueOf(uri).intern();
        key._localName = Text.valueOf(localName).intern();

        // Build class.
        String className;
        if (uri.length() == 0) {
            className = localName.toString();
        } else if ((uri.length() >= 5) && (uri.charAt(0) == 'j')
                && (uri.charAt(1) == 'a') && (uri.charAt(2) == 'v')
                && (uri.charAt(3) == 'a') && (uri.charAt(4) == ':')) {
            TextBuilder tb = TextBuilder.newInstance();
            if (uri.length() > 5) {
                tb.append(uri.subSequence(5, uri.length()));
                tb.append('.');
            }
            tb.append(localName);
            className = tb.toString();
        } else {
            throw new SAXParseException("Invalid URI (must use a java scheme)",
                    _locator);
        }

        // Maps uri/localName to class.
        try {
            cl = XmlFormat.classFor(className);
            _keyToClass.put(key, cl);
            return cl;
        } catch (ClassNotFoundException e) {
            throw new SAXParseException("Class " + className + " not found",
                    _locator);
        }
    }

    private static final class Key {
        CharSequence _uri;

        CharSequence _localName;

        public boolean equals(Object obj) {
            Key that = (Key) obj;
            return (that._uri.equals(this._uri) && that._localName
                    .equals(this._localName));
        }

        public int hashCode() {
            return _localName.hashCode();
        }
    }

    private final FastMap _keyToClass = new FastMap();

    private final Key _accessKey = new Key();

    // Implements Reusable.
    public void clear() {
        // We do not need to clear the key to class mapping as 
        // this mapping is assumed persistent.
        _idToObject.clear();
        getRoots().clear();
    }
}