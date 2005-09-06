/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2005 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.xml;

import java.io.IOException;

import org.xml.sax.SAXException;

import j2me.lang.CharSequence;
import j2me.util.NoSuchElementException;
import javolution.lang.Text;
import javolution.lang.TextBuilder;
import javolution.lang.TypeFormat;
import javolution.util.FastComparator;
import javolution.util.FastList;
import javolution.util.FastMap;
import javolution.util.FastTable;
import javolution.xml.pull.XmlPullParser;
import javolution.xml.pull.XmlPullParserException;
import javolution.xml.pull.XmlPullParserImpl;
import javolution.xml.sax.Attributes;
import javolution.xml.sax.ContentHandler;

/**
 * <p> This class represents a XML element. Instances of this class are made
 *     available only during the XML serialization/deserialization process.</p>
 *     
 * <p> During serialization, {@link XmlFormat#format 
 *     XmlFormat.format(XmlElement)} is used to represent the Java objects
 *     into XML.
 *     
 * <p> During deserialization, {@link XmlFormat#format 
 *     XmlFormat.parse(XmlElement)} is used to restore the objects from their
 *     XML representations.</p>
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 3.5, August 29, 2005
 */
public final class XmlElement {

    /**
     * Holds the content handler during serialization.
     */
    ContentHandler _formatHandler;

    /**
     * Holds the pull parser during deserialization.
     */
    final XmlPullParserImpl _parser;

    /**
     * Holds the object class for this xml element.
     */
    private Class _objectClass;

    /**
     * Holds the corresponding object.
     */
    private Object _object;

    /**
     * Holds this xml element name during serialization (null after 
     * the start element is written out).
     */
    private CharSequence _name;

    /**
     * Indicates if the parser is currently positioned on the next element.
     */
    private boolean _isParserAtNext;

    /**
     * Indicates if the parser is on a closure element (e.g. end-tag).
     */
    private boolean _isClosure;

    /**
     * Holds the current depth.
     */
    private int _depth;

    /**
     * Holds this element attributes when formatting. 
     */
    private final FormatAttributes _formatAttributes;

    /**
     * Holds the referenced objects (id-to-object when parsing,
     * object-to-id when formatting).
     */
    private final FastMap _objects = new FastMap();

    /**
     * Holds the id counter.
     */
    private int _idCount;

    /**
     * Creates a new xml element.
     * 
     * @param parser the pull parser or <code>null</code> when formatting.
     */
    XmlElement(XmlPullParserImpl parser) {
        if (parser != null) { // Parsing.
            _parser = parser;
            _formatAttributes = null;
            // Id-To-Object mapping.
            _objects.setKeyComparator(FastComparator.LEXICAL);

        } else { // Formatting.
            _parser = null;
            _formatAttributes = new FormatAttributes();
            // Object-To-Id mapping.
            _objects.setKeyComparator(FastComparator.IDENTITY);
        }

    }

    /**
     * Returns the object corresponding to this xml element; this is the 
     * object which has been {@link XmlFormat#allocate allocated} by  
     * the xml format or using the public no-arg constructor
     * of {@link #objectClass()}. 
     * 
     * @return the (uninitialized) object corresponding to this xml element.
     */
    public/*<T>*/Object/*T*/object() throws XmlException {
        if (_object == null) {
            if (_objectClass == null)
                throw new XmlException(
                        "Object type unknown, cannot use reflection");
            try {
                _object = _objectClass.newInstance();
            } catch (IllegalAccessException e2) {
                throw new XmlException(_objectClass
                        + " default constructor inaccessible");
            } catch (InstantiationException e3) {
                throw new XmlException(_objectClass
                        + " default constructor throws an exception");
            }
        }
        return (Object/*T*/) _object;
    }

    /**
     * Returns the Java(tm) class corresponding to this XML element;
     * the class is identified by the tag name of this xml element or the <code>
     * "j:class" attribute when present. 
     *
     * @return this XML element's corresponding class.
     */
    public Class objectClass() {
        return _objectClass;
    }
    /**
     * Returns the referenced objects (local to this xml document).
     * The map returned is either an ID-TO-OBJECT (parsing) 
     * or an OBJECT-TO-ID (formatting) mapping.
     * 
     * @return the referenced objects.
     */
    public FastMap objects() {
        return _objects;
    }

    /**
     * Returns a new unique id (local to this xml document).
     * 
     * @return the a new reference id.
     */
    public int newId() {
        return ++_idCount;
    }

    ///////////////////
    // Serialization //
    ///////////////////

    /**
     * Returns the content handler used during serialization (typically
     * a {@link javolution.xml.sax.WriterHandler WriterHandler}).
     *
     * @return the content handler receiving the SAX-2 events.
     */
    public ContentHandler formatter() {
        return _formatHandler;
    }

    /**
     * Adds the specified object as an anonymous nested element of unknown
     * type.
     *
     * @param obj the object added as nested element or <code>null</code>.
     */
    public void add(Object obj) {
        try {
            if (obj == null) {
                add(XmlFormat.NULL);
                return;
            }
            flushStartElement();

            // Checks for Character Data
            if (obj instanceof CharacterData) {
                CharacterData charData = (CharacterData) obj;
                _formatHandler.characters(charData.chars(), charData.offset(),
                        charData.length());
                return;
            }

            // Formats the specified object.
            _objectClass = obj.getClass();
            XmlFormat xmlFormat = XmlFormat.getInstance(_objectClass);
            CharSequence tagName = _name = toCharSeq(XmlFormat
                    .aliasFor(_objectClass));
            _depth++;
            xmlFormat.format(obj, this);
            flushStartElement();
            flushContent();
            _formatHandler.endElement(Text.EMPTY, tagName, tagName);
            _depth--;

        } catch (SAXException e) {
            throw new XmlException(e);
        }
    }

    /**
     * Adds the specified object as a named nested element of unknown type
     * (<code>null</code> objects are ignored). The nested xml element
     * will contain a "j:class" attribute identifying the object type.
     *
     * @param obj the object added as nested element or <code>null</code>.
     * @param name the nested element name.
     */
    public void add(Object obj, String name) {
        if (obj == null)
            return;
        try {
            flushStartElement();

            // Formats the specified object.
            _objectClass = obj.getClass();
            XmlFormat xmlFormat = XmlFormat.getInstance(_objectClass);
            CharSequence tagName = _name = toCharSeq(name);
            CharSequence alias = toCharSeq(XmlFormat.aliasFor(_objectClass));
            _formatAttributes.addAttribute("j:class", alias);
            _depth++;
            xmlFormat.format(obj, this);
            flushStartElement();
            flushContent();
            _formatHandler.endElement(Text.EMPTY, tagName, tagName);
            _depth--;

        } catch (SAXException e) {
            throw new XmlException(e);
        }
    }

    /**
     * Adds the specified object as a named nested element of known type
     * (<code>null</code> objects are ignored). 
     *
     * @param obj the object added as nested element or <code>null</code>.
     * @param name the nested element name.
     * @param xmlFormat the xml format to employ during serialization.
     */
    public void add(Object obj, String name, XmlFormat xmlFormat) {
        if (obj == null)
            return;
        try {
            flushStartElement();

            // Formats the specified object.
            CharSequence tagName = _name = toCharSeq(name);
            _depth++;
            xmlFormat.format(obj, this);
            flushStartElement();
            flushContent();
            _formatHandler.endElement(Text.EMPTY, tagName, tagName);
            _depth--;

        } catch (SAXException e) {
            throw new XmlException(e);
        }
    }

    /**
     * Creates a new attribute for this xml element.
     * 
     * This method allows for custom attribute formatting. For example:<pre>
     *     // Formats the color RGB value in hexadecimal.
     *     xml.newAttribute("color").append(_color.getRGB(), 16);
     *     
     *     // Formats the error using 4 digits.
     *     xml.newAttribute("error").append(error, 4, false, false);</pre>
     *
     * @param  name the attribute name.
     * @return the text builder to hold the attribute value.
     */
    public TextBuilder newAttribute(String name) {
        if ((_formatAttributes == null) || (_name == null))
            attributeSettingError();
        return _formatAttributes.newAttribute(name);
    }

    /**
     * Sets the specified <code>CharSequence</code> attribute
     * (<code>null</code> values are ignored).
     *
     * @param  name the attribute name.
     * @param  value the attribute value or <code>null</code>.
     */
    public void setAttribute(String name, CharSequence value) {
        if ((_formatAttributes == null) || (_name == null))
            attributeSettingError();
        if (value == null)
            return;
        _formatAttributes.addAttribute(name, value);
    }

    /**
     * Sets the specified <code>String</code> attribute
     * (<code>null</code> values are ignored).
     *
     * @param  name the attribute name.
     * @param  value the attribute value.
     */
    public void setAttribute(String name, String value) {
        if ((_formatAttributes == null) || (_name == null))
            attributeSettingError();
        if (value == null)
            return;
        CharSequence csqValue = toCharSeq(value);
        _formatAttributes.addAttribute(name, csqValue);
    }

    /**
     * Sets the specified <code>boolean</code> attribute.
     * 
     * @param  name the attribute name.
     * @param  value the <code>boolean</code> value for the specified attribute.
     * @see    #getAttribute(String, boolean)
     */
    public void setAttribute(String name, boolean value) {
        newAttribute(name).append(value);
    }

    /**
     * Sets the specified <code>int</code> attribute.
     * 
     * @param  name the attribute name.
     * @param  value the <code>int</code> value for the specified attribute.
     * @see    #getAttribute(String, int)
     */
    public void setAttribute(String name, int value) {
        newAttribute(name).append(value);
    }

    /**
     * Sets the specified <code>long</code> attribute.
     * 
     * @param  name the attribute name.
     * @param  value the <code>long</code> value for the specified attribute.
     * @see    #getAttribute(String, long)
     */
    public void setAttribute(String name, long value) {
        newAttribute(name).append(value);
    }

    /**
     * Sets the specified <code>float</code> attribute.
     * 
     * @param  name the attribute name.
     * @param  value the <code>float</code> value for the specified attribute.
     * @see    #getAttribute(String, float)
     /*@FLOATING_POINT@
     public void setAttribute(String name, float value) {
     newAttribute(name).append(value);
     }
     /**/

    /**
     * Sets the specified <code>double</code> attribute.
     * 
     * @param  name the attribute name.
     * @param  value the <code>double</code> value for the specified attribute.
     * @see    #getAttribute(String, double)
     /*@FLOATING_POINT@
     public void setAttribute(String name, double value) {
     newAttribute(name).append(value);
     }
     /**/

    /**
     * Removes the specified attribute.
     * 
     * @param  name the name of the attribute to remove.
     */
    public void removeAttribute(String name) {
        if ((_formatAttributes == null) || (_name == null))
            attributeSettingError();
        int index = _formatAttributes.getIndex(name);
        if (index >= 0) {
            _formatAttributes.remove(index);
        }
    }

    /////////////////////
    // Deserialization //
    /////////////////////
    /**
     * Returns the pull parser used during deserialization.
     *
     * @return the pull parser.
     */
    protected XmlPullParser parser() {
        return _parser;
    }

    /**
     * Indicates if more nested elements can be read.
     *
     * @return <code>true</code> if more nested elements can be read; 
     *         <code>false</code> otherwise.
     */
    public boolean hasNext() {
        if (!_isParserAtNext) {
            nextToken();
            _isParserAtNext = true;
        }
        return !_isClosure;
    }

    /**
     * Returns the object corresponding to the next nested element.
     *
     * @return the next nested object.
     * @throws j2me.util.NoSuchElementException if 
     *         <code>this.hasNext() == false</code>
     */
    public/*<T>*/Object/*T*/getNext() {
        try {
            if (!hasNext())
                throw new NoSuchElementException();
            _isParserAtNext = false; // Move forward.
            if (_parser.getEventType() == XmlPullParser.START_TAG) {
                CharSequence qName = _parser.getQName();
                _objectClass = XmlFormat.classFor(qName);
                XmlFormat xmlFormat = XmlFormat.getInstance(_objectClass);
                _depth++;
                _object = xmlFormat.allocate(this);
                Object obj = xmlFormat.parse(this);
                _depth--;
                if (hasNext()) 
                    incompleteReadError();
                _isParserAtNext = false; // Move forward.
                return (Object/*T*/) obj;
            } else { // Character Data.
                CharacterData cdata = CharacterData.valueOf(_parser.getText());
                return (Object/*T*/) cdata;
            }
        } catch (XmlPullParserException e) {
            throw new XmlException(e);
        }
    }

    /**
     * Returns the object corresponding to the next nested element only 
     * if it has the specified qualified name.
     *
     * @param qName the nested element qualified name required.
     * @return the next content object or <code>null</code> if the qName 
     *         does not match.
     * @throws XmlException if the specified object has no "j:class" attribute
     *         identifying the object type.
     */
    public/*<T>*/Object/*T*/get(String qName) {
        try {
            if (hasNext()
                    && (_parser.getEventType() == XmlPullParser.START_TAG)
                    && _parser.getQName().equals(qName)) {
                _isParserAtNext = false; // Move forward.
                CharSequence alias = _parser.getSaxAttributes().getValue(
                        "j:class");
                if (alias == null)
                    throw new XmlException("j:class attribute missing");
                _objectClass = XmlFormat.classFor(alias);
                XmlFormat xmlFormat = XmlFormat.getInstance(_objectClass);
                _depth++;
                _object = xmlFormat.allocate(this);
                Object obj = xmlFormat.parse(this);
                _depth--;
                if (hasNext()) 
                    incompleteReadError();
                _isParserAtNext = false; // Move forward.
                return (Object/*T*/) obj;
            }
            return null;
        } catch (XmlPullParserException e) {
            throw new XmlException(e);
        }
    }

    /**
     * Returns the object corresponding to the next nested element only 
     * if it has the specified qualified name; this method use the specified 
     * xml format for parsing the object.
     * Returns the object parse using the specified format corresponding to 
     * the next nested element only if it has the specified qualified name.
     *
     * @param qName the nested element qualified name required.
     * @param xmlFormat the format used for parsing.
     * @return the next content object or <code>null</code> if the qName 
     *         does not match.
     * @throws XmlException if the specified format requires 
     *        {@link #objectClass()} to be set (ref. {@link #object()}).
     */
    public/*<T>*/Object/*T*/get(String qName, XmlFormat xmlFormat) {
        try {
            if (hasNext()
                    && (_parser.getEventType() == XmlPullParser.START_TAG)
                    && _parser.getQName().equals(qName)) {
                _isParserAtNext = false; // Move forward.
                _depth++;
                _object = xmlFormat.allocate(this);
                Object obj = xmlFormat.parse(this);
                _depth--;
                if (hasNext()) 
                    incompleteReadError();
                _isParserAtNext = false; // Move forward.
                return (Object/*T*/) obj;
            }
            return null;
        } catch (XmlPullParserException e) {
            throw new XmlException(e);
        }
    }

    /**
     * Returns the attributes for this xml element (parsing or formatting).
     *
     * @return the attributes mapping.
     */
    public Attributes getAttributes() {
        if (_formatAttributes != null)
            return _formatAttributes;
        if (_isParserAtNext)
            throw new XmlException(
                    "Attributes should be read before any content");
        return _parser.getSaxAttributes();
    }

    /**
     * Searches for the attribute having the specified name.
     *
     * @param  name the qualified name of the attribute (qName).
     * @return the value for the specified attribute or <code>null</code>
     *         if the attribute is not found.
     */
    public CharSequence getAttribute(String name) {
        return getAttributes().getValue(name);
    }

    /**
     * Indicates if the specified attribute is present.
     *
     * @param  name the qualified name of the attribute (qName).
     * @return <code>true</code> if this xml element contains the specified
     *         attribute; <code>false</code> otherwise.
     */
    public boolean isAttribute(String name) {
        return getAttributes().getIndex(name) >= 0;
    }

    /**
     * Returns the specified <code>CharSequence</code> attribute.
     *
     * @param  name the name of the attribute.
     * @param  defaultValue a default value.
     * @return the value for the specified attribute or
     *         the <code>defaultValue</code> if the attribute is not found.
     */
    public CharSequence getAttribute(String name, CharSequence defaultValue) {
        CharSequence value = getAttributes().getValue(name);
        return (value != null) ? value : defaultValue;
    }

    /**
     * Returns the specified <code>String</code> attribute.
     *
     * @param  name the name of the attribute.
     * @param  defaultValue a default value.
     * @return the value for the specified attribute or
     *         the <code>defaultValue</code> if the attribute is not found.
     */
    public String getAttribute(String name, String defaultValue) {
        CharSequence value = getAttributes().getValue(name);
        return (value != null) ? value.toString() : defaultValue;
    }

    /**
     * Returns the specified <code>boolean</code> attribute.
     *
     * @param  name the name of the attribute searched for.
     * @param  defaultValue the value returned if the attribute is not found.
     * @return the <code>boolean</code> value for the specified attribute or
     *         the default value if the attribute is not found.
     */
    public boolean getAttribute(String name, boolean defaultValue) {
        CharSequence value = getAttributes().getValue(name);
        return (value != null) ? TypeFormat.parseBoolean(value) : defaultValue;
    }

    /**
     * Returns the specified <code>int</code> attribute. This method handles
     * string formats that are used to represent octal and hexadecimal numbers.
     *
     * @param  name the name of the attribute searched for.
     * @param  defaultValue the value returned if the attribute is not found.
     * @return the <code>int</code> value for the specified attribute or
     *         the default value if the attribute is not found.
     */
    public int getAttribute(String name, int defaultValue) {
        CharSequence value = getAttributes().getValue(name);
        return (value != null) ? TypeFormat.parseInt(value) : defaultValue;
    }

    /**
     * Returns the specified <code>long</code> attribute. This method handles
     * string formats that are used to represent octal and hexadecimal numbers.
     *
     * @param  name the name of the attribute searched for.
     * @param  defaultValue the value returned if the attribute is not found.
     * @return the <code>long</code> value for the specified attribute or
     *         the default value if the attribute is not found.
     */
    public long getAttribute(String name, long defaultValue) {
        CharSequence value = getAttributes().getValue(name);
        return (value != null) ? TypeFormat.parseLong(value) : defaultValue;
    }

    /**
     * Returns the specified <code>float</code> attribute.
     *
     * @param  name the name of the attribute searched for.
     * @param  defaultValue the value returned if the attribute is not found.
     * @return the <code>float</code> value for the specified attribute or
     *         the default value if the attribute is not found.
     /*@FLOATING_POINT@
     public float getAttribute(String name, float defaultValue) {
     CharSequence value = getAttributes().getValue(name);
     return (value != null) ? (float) TypeFormat.parseDouble(value)
     : defaultValue;
     }
     /**/

    /**
     * Returns the specified <code>double</code> attribute.
     *
     * @param  name the name of the attribute searched for.
     * @param  defaultValue the value returned if the attribute is not found.
     * @return the <code>double</code> value for the specified attribute or
     *         the default value if the attribute is not found.
     /*@FLOATING_POINT@
     public double getAttribute(String name, double defaultValue) {
     CharSequence value = getAttributes().getValue(name);
     return (value != null) ? TypeFormat.parseDouble(value) : defaultValue;
     }
     /**/

    /**
     * Converts a String to a CharSequence (for J2ME compatibility)
     * 
     * @param str the String to convert.
     * @return the corresponding CharSequence instance.
     */
    private CharSequence toCharSeq(Object str) {
        if (str instanceof CharSequence)
            return (CharSequence) str;
        return Text.valueOf((String) str);
    }

    /**
     * Flushes the start element with the its attributes.
     */
    void reset() {
        for (int i = 0; i < _contents.size(); i++) {
            FastList list = (FastList) _contents.get(i);
            list.clear();
        }
        _depth = 0;
        if (_formatAttributes != null) {
            _formatAttributes.reset();
        }
        _formatHandler = null;
        _idCount = 0;
        _isParserAtNext = false;
        _name = null;
        _object = null;
        _objectClass = null;
        _objects.clear();
        if (_parser != null) {
            _parser.reset();
        }
    }

    /**
     * Flushes the start element with the its attributes.
     */
    private void flushStartElement() {
        if (_name != null) { // Container element attributes have to be flushed.
            try {
                _formatHandler.startElement(Text.EMPTY, _name, _name,
                        _formatAttributes);
            } catch (SAXException e) {
                throw new XmlException(e);
            }
            _formatAttributes.reset();
            _name = null;
        }
    }

    /**
     * Flushes any object which has been added to xml content
     * (for backward compatibility).
     */
    private void flushContent() {
        if (_contents.size() > _depth) {
            FastList content = (FastList) _contents.get(_depth);
            for (FastList.Node n = content.headNode(), end = content.tailNode(); (n = n
                    .getNextNode()) != end;) {
                add(n.getValue());
            }
            content.clear();
        }
    }

    /**
     * Moves the parser to the next valid token.
     */
    private void nextToken() {
        try {
            while (true) {
                switch (_parser.nextToken()) {
                case XmlPullParser.START_TAG:
                case XmlPullParser.CDSECT:
                    _isClosure = false;
                    return;
                case XmlPullParser.END_TAG:
                case XmlPullParser.END_DOCUMENT:
                    _isClosure = true;
                    return;
                case XmlPullParser.TEXT:
                    if (!_parser.isWhitespace()) {
                        _isClosure = false;
                        return;
                    }
                    break;
                }
            }
        } catch (XmlPullParserException e) {
            throw new XmlException(e);
        } catch (IOException e) {
            throw new XmlException(e);
        }
    }

    /**
     * Notifies there is a problem setting the attribute.
     */
    private void attributeSettingError() {
        if (_formatAttributes == null)
            throw new XmlException("Attributes cannot be set during parsing");
        if (_name == null)
            throw new XmlException(
                    "Attributes should be set before adding nested elements");
        throw new XmlException();
    }

    /**
     * Notifies that an xml element has not been read fully.
     * 
     * @throws XmlPullParserException 
     */
    private void incompleteReadError() throws XmlPullParserException {
        if (_parser.getEventType() == XmlPullParser.START_TAG) {
            throw new XmlException("Incomplete read error (nested " +
                    _parser.getQName() + " has not been read)");
        } else {    
            throw new XmlException("Incomplete read error (character data" +
                    "has not been read)");
        }
    }

    /**
     * @deprecated Formats should use {@link #getNext()} and {@link #hasNext()}
     *             to parse anonymous content and {@link #add(Object)} 
     *             to serialize anonymous content.
     */
    public FastList getContent() {
        while (_contents.size() <= _depth) {
            _contents.add(new FastList());
        }
        FastList content = (FastList) _contents.get(_depth);
        if (_parser != null) { // Parsing.
            while (hasNext()) {
                content.add(getNext());
            }
        }
        return content;
    }

    private final FastTable _contents = new FastTable();

}