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
import j2me.lang.IllegalStateException;

import java.util.NoSuchElementException;
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
 * @version 3.6, October 13, 2005
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
     * Holds the corresponding object.
     */
    private Object _object;

    /**
     * Holds the class of the corresponding object.
     */
    private Class _objectClass;

    /**
     * Indicates if the parser is currently positioned on the next element.
     */
    private boolean _isParserAtNext;

    /**
     * Indicates if the parser is on a closure element (e.g. end-tag).
     */
    private boolean _isClosure;

    /**
     * Holds this element attributes when formatting. 
     */
    final FormatAttributes _formatAttributes;

    /**
     * Holds intermediate text builder instance 
     */
    private final TextBuilder _textBuilder = new TextBuilder();

    /**
     * Holds the current element qualified name when formatting.
     */
    private CharSequence _elemName;

    /**
     * Holds the package prefixes (prefix/package String pairs).
     */
    final FastTable _packagePrefixes = new FastTable();

    /**
     * Indicates if cross references are enabled.
     */
    boolean _areReferencesEnabled;

    /**
     * Indicates if references are expanded.
     */
    boolean _expandReferences = false;

    /**
     * Indicates if class identifers are enabled.
     */
    boolean _isClassIdentifierEnabled;

    /**
     * Maps unique identifiers to objects (deserialization).
     */
    private final FastMap _idToObject;

    /**
     * Maps objects to unique identifiers (serialization).
     */
    private final FastMap _objectToId;

    /**
     * Olds stack of formatted object in order to detect circular references.
     */
    private final FastTable _formatStack;

    /**
     * Creates a new xml element.
     * 
     * @param parser the pull parser or <code>null</code> when formatting.
     */
    XmlElement(XmlPullParserImpl parser) {
        if (parser != null) { // Parsing.
            _parser = parser;
            _parseContents = new FastTable();
            _formatAttributes = null;
            _idToObject = new FastMap();
            _objectToId = null;
            _formatStack = null;
        } else { // Formatting.
            _parser = null;
            _parseContents = null;
            _formatAttributes = new FormatAttributes();
            _idToObject = null;
            _objectToId = new FastMap()
                    .setKeyComparator(FastComparator.IDENTITY);
            _formatStack = (FastTable) new FastTable()
                    .setValueComparator(FastComparator.IDENTITY);
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
     * Adds the specified object as an anonymous nested element of unknown type.
     *
     * @param obj the object added as nested element or <code>null</code>.
     */
    public void add(Object obj) {
        try {
            // Adds null value as NULL
            if (obj == null) {
                obj = XmlFormat.NULL;
            }

            // Flushes outer start element if any.
            if (_elemName != null)
                flushStart();

            // Checks for Character Data
            if (obj instanceof CharacterData) {
                CharacterData charData = (CharacterData) obj;
                _formatHandler.characters(charData.chars(), charData.offset(),
                        charData.length());
                return;
            }

            // Checks if content name set.
            Class cls = _objectClass = obj.getClass();
            XmlFormat xmlFormat = XmlFormat.getInstance(cls);
            String defaultName = xmlFormat.defaultName();
            if (defaultName != null) {
                _objectClass = obj.getClass();
                String alias = XmlFormat.aliasFor(_objectClass);
                if (_isClassIdentifierEnabled) {
                    _formatAttributes.addAttribute("j:class",
                            (alias != null) ? toCharSeq(alias)
                                    : toCharSeq(_objectClass.getName()));
                }
                CharSequence elemName = _elemName = toCharSeq(defaultName);
                format(obj, xmlFormat);
                if (_elemName != null)
                    flushStart();
                _formatHandler.endElement(Text.EMPTY, elemName, elemName);
                return;
            }

            // Formats the specified object using classname as element name.
            CharSequence elemName = _elemName = qNameFor(cls);
            format(obj, xmlFormat);
            if (_elemName != null)
                flushStart();
            if (elemName == _textBuilder) { // Mutable, likely lost.
                elemName = qNameFor(cls);
            }
            _formatHandler.endElement(Text.EMPTY, elemName, elemName);

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
     * @param qName the qualified name of the nested element.
     */
    public void add(Object obj, String qName) {
        if (obj == null)
            return;
        try {
            // Flushes outer start element if any.
            if (_elemName != null)
                flushStart();

            // Formats the specified object.
            _objectClass = obj.getClass();
            String alias = XmlFormat.aliasFor(_objectClass);
            if (_isClassIdentifierEnabled) {
                _formatAttributes.addAttribute("j:class",
                        (alias != null) ? toCharSeq(alias)
                                : toCharSeq(_objectClass.getName()));
            }
            CharSequence elemName = _elemName = toCharSeq(qName);
            format(obj, XmlFormat.getInstance(_objectClass));
            if (_elemName != null)
                flushStart();
            _formatHandler.endElement(Text.EMPTY, elemName, elemName);

        } catch (SAXException e) {
            throw new XmlException(e);
        }
    }

    /**
     * Adds the specified object as a named nested element of known type
     * (<code>null</code> objects are ignored). 
     *
     * @param obj the object added as nested element or <code>null</code>.
     * @param qName the qualified name of the nested element.
     * @param xmlFormat the xml format to employ during serialization.
     */
    public void add(Object obj, String qName, XmlFormat xmlFormat) {
        if (obj == null)
            return;
        try {
            // Flushes outer start element if any.
            if (_elemName != null)
                flushStart();

            // Formats the specified object.
            CharSequence elemName = _elemName = toCharSeq(qName);
            format(obj, xmlFormat);
            if (_elemName != null)
                flushStart();
            _formatHandler.endElement(Text.EMPTY, elemName, elemName);

        } catch (SAXException e) {
            throw new XmlException(e);
        }
    }

    /**
     * Creates a new attribute for this xml element.
     * 
     * This method allows for custom attribute formatting. For example:[code]
     *     // Formats the color RGB value in hexadecimal.
     *     xml.newAttribute("color").append(_color.getRGB(), 16);
     *     
     *     // Formats the error using 4 digits.
     *     xml.newAttribute("error").append(error, 4, false, false);[/code]
     *
     * @param  name the attribute name.
     * @return the text builder to hold the attribute value.
     */
    public TextBuilder newAttribute(String name) {
        if ((_formatAttributes == null) || (_elemName == null))
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
        if ((_formatAttributes == null) || (_elemName == null))
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
        if ((_formatAttributes == null) || (_elemName == null))
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

    ////////////////////////
    // Primitive Wrappers //
    ////////////////////////
    /**
     * Sets the specified <code>Boolean</code> attribute.
     * 
     * @param  name the name of the attribute.
     * @param  value the <code>Boolean</code> value for the specified attribute
     *         or <code>null</code> in which case the attribute is not set.
     * @see    #getAttribute(String, Boolean)
     */
    public void setAttribute(String name, Boolean value) {
        if (value == null)
            return;
        newAttribute(name).append(value.booleanValue());
    }

    /**
     * Sets the specified <code>Byte</code> attribute.
     * 
     * @param  name the name of the attribute.
     * @param  value the <code>Byte</code> value for the specified attribute
     *         or <code>null</code> in which case the attribute is not set.
     * @see    #getAttribute(String, Byte)
     */
    public void setAttribute(String name, Byte value) {
        if (value == null)
            return;
        newAttribute(name).append(value.byteValue());
    }

    /**
     * Sets the specified <code>Short</code> attribute.
     *
     * @param  name the name of the attribute.
     * @param  value the <code>Short</code> value for the specified attribute
     *         or <code>null</code> in which case the attribute is not set.
     * @see    #getAttribute(String, Short)
     */
    public void setAttribute(String name, Short value) {
        if (value == null)
            return;
        newAttribute(name).append(value.shortValue());
    }

    /**
     * Sets the specified <code>Integer</code> attribute.
     * 
     * @param  name the name of the attribute.
     * @param  value the <code>Integer</code> value for the specified attribute
     *         or <code>null</code> in which case the attribute is not set.
     * @see    #getAttribute(String, Integer)
     */
    public void setAttribute(String name, Integer value) {
        if (value == null)
            return;
        newAttribute(name).append(value.intValue());
    }

    /**
     * Sets the specified <code>Long</code> attribute.
     *
     * @param  name the name of the attribute.
     * @param  value the <code>Long</code> value for the specified attribute
     *         or <code>null</code> in which case the attribute is not set.
     * @see    #getAttribute(String, Long)
     */
    public void setAttribute(String name, Long value) {
        if (value == null)
            return;
        newAttribute(name).append(value.longValue());
    }

    /**
     * Sets the specified <code>Float</code> attribute.
     *
     * @param  name the name of the attribute.
     * @param  value the <code>Float</code> value for the specified attribute
     *         or <code>null</code> in which case the attribute is not set.
     * @see    #getAttribute(String, Float)
     /*@FLOATING_POINT@
     public void setAttribute(String name, Float value) {
     if (value == null)
     return;
     newAttribute(name).append(value.floatValue());
     }
     /**/

    /**
     * Sets the specified <code>Double</code> attribute.
     *
     * @param  name the name of the attribute.
     * @param  value the <code>Double</code> value for the specified attribute
     *         or <code>null</code> in which case the attribute is not set.
     * @see    #getAttribute(String, Double)
     /*@FLOATING_POINT@
     public void setAttribute(String name, Double value) {
     if (value == null)
     return;
     newAttribute(name).append(value.doubleValue());
     }
     /**/

    /**
     * Removes the specified attribute.
     * 
     * @param  name the name of the attribute to remove.
     */
    public void removeAttribute(String name) {
        if ((_formatAttributes == null) || (_elemName == null))
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
    public XmlPullParser parser() {
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
     * @throws java.util.NoSuchElementException if 
     *         <code>this.hasNext() == false</code>
     */
    public/*<T>*/Object/*T*/getNext() {
        try {
            if (!hasNext())
                throw new NoSuchElementException();
            _isParserAtNext = false; // Move forward.
            if (_parser.getEventType() == XmlPullParser.START_TAG) {

                // Search for the object class.
                CharSequence className = _parser.getSaxAttributes().getValue(
                        "j:class");
                if (className == null) { // Element name as class name.
                    CharSequence uri = _parser.getNamespace();
                    CharSequence name = _parser.getName();
                    _objectClass = ((uri.length() > 0) && (!uri
                            .equals(ObjectWriter.JAVOLUTION_URI))) ? XmlFormat
                            .classFor(uri, name) : XmlFormat.classFor(name);
                } else {
                    _objectClass = XmlFormat.classFor(className);
                }

                XmlFormat xmlFormat = XmlFormat.getInstance(_objectClass);
                _parseContentIndex++;
                Object obj = parse(xmlFormat);
                _parseContentIndex--;
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
                CharSequence name = _parser.getSaxAttributes().getValue(
                        "j:class");
                if (name == null)
                    throw new XmlException("\"j:class\" attribute missing");
                _objectClass = XmlFormat.classFor(name);
                XmlFormat xmlFormat = XmlFormat.getInstance(_objectClass);
                _parseContentIndex++;
                Object obj = parse(xmlFormat);
                _parseContentIndex--;
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
                _parseContentIndex++;
                Object obj = parse(xmlFormat);
                _parseContentIndex--;
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

    ////////////////////////
    // Primitive Wrappers //
    ////////////////////////
    /**
     * Searches for the specified <code>Boolean</code> attribute.
     *
     * @param  name the name of the attribute.
     * @param  defaultValue the value returned if the attribute is not found.
     * @return the <code>Boolean</code> value for the specified attribute or
     *         the default value if the attribute is not found.
     */
    public Boolean getAttribute(String name, Boolean defaultValue) {
        CharSequence value = getAttributes().getValue(name);
        return (value != null) ? new Boolean(TypeFormat.parseBoolean(value))
                : defaultValue;
    }

    /**
     * Searches for the specified <code>Byte</code> attribute.
     *
     * @param  name the name of the attribute.
     * @param  defaultValue the value returned if the attribute is not found.
     * @return the <code>Byte</code> value for the specified attribute or
     *         the default value if the attribute is not found.
     */
    public Byte getAttribute(String name, Byte defaultValue) {
        CharSequence value = getAttributes().getValue(name);
        return (value != null) ? new Byte(TypeFormat.parseByte(value))
                : defaultValue;
    }

    /**
     * Searches for the specified <code>Short</code> attribute.
     *
     * @param  name the name of the attribute.
     * @param  defaultValue the value returned if the attribute is not found.
     * @return the <code>Short</code> value for the specified attribute or
     *         the default value if the attribute is not found.
     */
    public Short getAttribute(String name, Short defaultValue) {
        CharSequence value = getAttributes().getValue(name);
        return (value != null) ? new Short(TypeFormat.parseShort(value))
                : defaultValue;
    }

    /**
     * Searches for the specified <code>Integer</code> attribute.
     *
     * @param  name the name of the attribute.
     * @param  defaultValue the value returned if the attribute is not found.
     * @return the <code>Integer</code> value for the specified attribute or
     *         the default value if the attribute is not found.
     */
    public Integer getAttribute(String name, Integer defaultValue) {
        CharSequence value = getAttributes().getValue(name);
        return (value != null) ? new Integer(TypeFormat.parseInt(value))
                : defaultValue;
    }

    /**
     * Searches for the specified <code>Long</code> attribute.
     *
     * @param  name the name of the attribute.
     * @param  defaultValue the value returned if the attribute is not found.
     * @return the <code>Long</code> value for the specified attribute or
     *         the default value if the attribute is not found.
     */
    public Long getAttribute(String name, Long defaultValue) {
        CharSequence value = getAttributes().getValue(name);
        return (value != null) ? new Long(TypeFormat.parseLong(value))
                : defaultValue;
    }

    /**
     * Searches for the specified <code>Float</code> attribute.
     *
     * @param  name the name of the attribute.
     * @param  defaultValue the value returned if the attribute is not found.
     * @return the <code>Float</code> value for the specified attribute or
     *         the default value if the attribute is not found.
     /*@FLOATING_POINT@
     public Float getAttribute(String name, Float defaultValue) {
     CharSequence value = getAttributes().getValue(name);
     return (value != null) ? new Float( TypeFormat.parseFloat(value) ) : defaultValue;
     }
     /**/

    /**
     * Searches for the specified <code>Double</code> attribute.
     *
     * @param  name the name of the attribute.
     * @param  defaultValue the value returned if the attribute is not found.
     * @return the <code>Double</code> value for the specified attribute or
     *         the default value if the attribute is not found.
     /*@FLOATING_POINT@
     public Double getAttribute(String name, Double defaultValue) {
     CharSequence value = getAttributes().getValue(name);
     return (value != null) ? new Double( TypeFormat.parseDouble(value) ) : defaultValue;
     }
     /**/

    /**
     * Flushes the start element with the its attributes.
     */
    void reset() {
        _elemName = null;
        _isClosure = false;
        _object = null;
        _objectClass = null;
        _textBuilder.reset();

        if (_parser != null) { // Parsing.
            _parser.reset();
            for (int i = 0; i < _parseContents.size(); i++) {
                FastList list = (FastList) _parseContents.get(i);
                list.reset();
            }
            _parseContentIndex = 0;
            _isParserAtNext = false;
            _idToObject.clear();
        } else { // Formatting.
            _formatAttributes.reset();
            _formatContent.reset();
            _formatHandler = null;
            _objectToId.clear();
            _formatStack.clear();
        }

    }

    /**
     * Flushes the current start element.
     */
    private void flushStart() throws SAXException {
        _formatHandler.startElement(Text.EMPTY, _elemName, _elemName,
                _formatAttributes);
        _formatAttributes.reset();
        _elemName = null;
    }

    /**
     * Moves the parser to the next valid token.
     * 
     * @return the current event.
     */
    private int nextToken() {
        try {
            while (true) {
                int event = _parser.nextToken();
                switch (event) {
                case XmlPullParser.START_TAG:
                case XmlPullParser.CDSECT:
                    _isClosure = false;
                    return event;
                case XmlPullParser.END_TAG:
                case XmlPullParser.END_DOCUMENT:
                    _isClosure = true;
                    return event;
                case XmlPullParser.TEXT:
                    if (!_parser.isWhitespace()) {
                        _isClosure = false;
                        return event;
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
        if (_elemName == null)
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
            throw new XmlException("Incomplete read error (nested "
                    + _parser.getQName() + " has not been read)");
        } else {
            throw new XmlException("Incomplete read error (character data"
                    + "has not been read)");
        }
    }

    /**
     * Returns the qualified name for the specified class.
     * 
     * @param cls the class for which the qualified name is returned.
     */
    private CharSequence qNameFor(Class cls) {
        String alias = XmlFormat.aliasFor(cls);
        String className = (alias != null) ? alias : cls.getName();

        // Searches package prefixes.
        String matchPfx = null;
        String matchPkg = "";
        for (int i = 0; i < _packagePrefixes.size();) {
            String pfx = (String) _packagePrefixes.get(i++);
            String pkg = (String) _packagePrefixes.get(i++);
            if (className.startsWith(pkg)
                    && (pkg.length() >= matchPkg.length())
                    && ((alias == null) || (pfx.equals("j")))) {
                // For aliases only javolution uri can be used.
                matchPfx = pfx;
                matchPkg = pkg;
            }
        }
        if (matchPfx != null) {
            _textBuilder.reset();
            if (matchPfx.length() > 0) {
                _textBuilder.append(matchPfx).append(':');
            }
            if (matchPkg.length() > 0) {
                _textBuilder.append(className, matchPkg.length() + 1, className
                        .length());
            } else {
                _textBuilder.append(className);
            }
            return _textBuilder;
        }
        return toCharSeq(className);
    }

    /**
     * Converts a String to a CharSequence (for J2ME compatibility)
     * 
     * @param str the string to convert.
     * @return the corresponding CharSequence instance.
     */
    private static CharSequence toCharSeq(Object str) {
        if (str instanceof CharSequence)
            return (CharSequence) str;
        return Text.valueOf((String) str);
    }

    /**
     * Parses this xml element using the specified format.
     * 
     * @param xmlFormat the xml format to use.
     * @return the parsed object.
     */
    private Object parse(XmlFormat xmlFormat) {
        if (_areReferencesEnabled && (xmlFormat.identifier(false) != null)) {
            // Checks if reference.
            CharSequence ref = getAttribute(xmlFormat.identifier(true));
            if (ref != null) { // Reference.
                Object obj = _idToObject.get(ref);
                if (obj == null)
                    throw new IllegalStateException("Reference " + ref
                            + " not found.");
                // Flushes/ignores content (if references have been expanded).
                int depth = _parser.getDepth();
                while ((nextToken() != XmlPullParser.END_TAG)
                        && (_parser.getDepth() <= depth)) {
                }
                _isParserAtNext = true;
                return obj;
            }

            // Not a reference.
            _object = null;
            Object obj = _object = xmlFormat.allocate(this);
            CharSequence id = getAttribute(xmlFormat.identifier(false));
            if (id != null) { // Identifier found.
                if (obj != null) { // Stores reference now in case of circularity.
                    _idToObject.put(Text.valueOf(id), obj);
                    if (xmlFormat.parse(this) != obj)
                        throw new XmlException(
                                "Parse should return xml.object() when allocate(xml) != null");
                    return obj;
                } else {
                    obj = xmlFormat.parse(this);
                    _idToObject.put(Text.valueOf(id), obj);
                    return obj;
                }
            } else { // Identifier not present, disables references for this object and its content.
                _areReferencesEnabled = false;
                obj = xmlFormat.parse(this);
                _areReferencesEnabled = true;
                return obj;
            }
        } else { // References disabled.
            _object = null;
            Object obj = _object = xmlFormat.allocate(this);
            if (obj != null) {
                if (xmlFormat.parse(this) != obj)
                    throw new XmlException(
                            "Parse should return xml.object() when allocate(xml) != null");
                return obj;
            } else {
                return xmlFormat.parse(this);
            }
        }
    }

    /**
     * Format the specified object to this xml element using the specified 
     * format.
     * 
     * @param obj the object to format.
     * @param xmlFormat the xml format to use.
     */
    private void format(Object obj, XmlFormat xmlFormat) {
        if (_areReferencesEnabled && (xmlFormat.identifier(false) != null)) {
            // Checks if reference exists.
            Text ref = (Text) _objectToId.get(obj);
            if (ref != null) { // Reference.
                setAttribute(xmlFormat.identifier(true), ref);
            } else { // New identifier.   
                Text id = Text.valueOf(_objectToId.size());
                _objectToId.put(obj, id);
                setAttribute(xmlFormat.identifier(false), id);
            }
            if ((ref == null)
                    || (_expandReferences && !_formatStack.contains(obj))) {
                _formatStack.addLast(obj);
                xmlFormat.format(obj, this);
                _formatStack.removeLast();
            }
            return;
        }
        xmlFormat.format(obj, this);
    }

    /**
     * @deprecated Formats should use {@link #getNext()} and {@link #hasNext()}
     *             to parse anonymous content and {@link #add(Object)} 
     *             to serialize anonymous content.
     */
    public FastList getContent() {
        if (_parser != null) { // Parsing.
            while (_parseContents.size() <= _parseContentIndex) {
                _parseContents.add(new FastList());
            }
            FastList content = (FastList) _parseContents
                    .get(_parseContentIndex);
            while (hasNext()) {
                content.add(getNext());
            }
            return content;
        } else { // Formatting.
            return _formatContent;
        }
    }

    private final FastList _formatContent = new FastList() {
        public void addLast(Object obj) { // TBD: Put back final in FastList
            XmlElement.this.add(obj);
        }
    };

    private final FastTable _parseContents;

    private int _parseContentIndex;

}