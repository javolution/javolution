/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2005 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.xml;

import j2me.lang.CharSequence;
import javolution.lang.Enum;
import javolution.lang.Text;
import javolution.lang.TextBuilder;
import javolution.lang.TypeFormat;
import javolution.util.FastList;
import javolution.xml.sax.Attributes;

/**
 * <p> This class represents a XML element. Instances of this class are made
 *     available only during the XML serialization/deserialization process.</p>
 * <p> During serialization,
 *     {@link XmlFormat#format XmlFormat.format(XmlElement)} is used to 
 *     represent the Java objects into XML.
 * <p> During deserialization, 
 *     {@link XmlFormat#format XmlFormat.parse(XmlElement)} is used to 
 *     restore the objects from their XML representations.</p>
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 2.2, January 8, 2004
 */
public final class XmlElement {

    /**
     * Holds the object class corresponding to this xml element.
     */
    Class _objectClass;

    /**
     * Holds the associated format
     */
    XmlFormat _format;

    /**
     * Holds the corresponding object.
     */
    Object _object;

    /**
     * Holds the id value (if any).
     */
    CharSequence _idValue;

    /**
     * Holds the parent of this element.
     */
    XmlElement _parent;

    /**
     * Holds the attributes.
     */
    final AttributesImpl _attributes = new AttributesImpl();

    /**
     * Holds the content.
     */
    final FastList _content = new FastList();

    /**
     * Default constructor.
     */
    XmlElement() {
    }

    /**
     * Returns the object corresponding to this xml element; this object has 
     * been either {@link XmlFormat#preallocate(XmlElement) preallocated} 
     * or created using the {@link #objectClass} default constructor.
     * 
     * @return the (uninitialized) object corresponding to this xml element.
     */
    public Object object() throws XmlException {
        if (_object == null) {
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
        return _object;
    }

    /**
     * Returns the content of this xml element; it is the list of objects 
     * to serialize as nested xml elements or the list of objects which have
     * been deserialized from nested xml elements.
     *
     * @return the list of objects content of this xml element (empty if 
     *         no content).
     */
    public FastList getContent() {
        return _content;
    }

    ///////////////////
    // Serialization //
    ///////////////////

    /**
     * Maps the specified attribute name to the specified value.
     *
     * @param  name the attributes' name.
     * @param  value the attributes' value.
     */
    void setAttribute(Object name, CharSequence value) {
        _attributes.add(name, value);
    }

    /**
     * Sets the specified <code>CharSequence</code> attribute.
     *
     * @param  name the attributes' name.
     * @param  value the attributes' value.
     */
    public void setAttribute(String name, CharSequence value) {
        _attributes.add(name, value);
    }

    /**
     * Sets the specified <code>String</code> attribute.
     *
     * @param  name the attributes' name.
     * @param  value the attributes' value.
     */
    public void setAttribute(String name, String value) {
        Object objValue = value;
        if (objValue instanceof CharSequence) {
            _attributes.add(name, (CharSequence)objValue);
        } else {
            _attributes.add(name, Text.valueOf(value));
        }
    }

    /**
     * Sets the specified <code>boolean</code> attribute.
     *
     * @param  name the name of the attribute.
     * @param  value the <code>boolean</code> value for the specified attribute.
     * @see    #getAttribute(String, boolean)
     */
    public void setAttribute(String name, boolean value) {
        _attributes.add(name, TextBuilder.newInstance().append(value));
    }

    /**
     * Sets the specified <code>int</code> attribute.
     *
     * @param  name the name of the attribute.
     * @param  value the <code>int</code> value for the specified attribute.
     * @see    #getAttribute(String, int)
     */
    public void setAttribute(String name, int value) {
        _attributes.add(name, TextBuilder.newInstance().append(value));
    }

    /**
     * Sets the specified <code>long</code> attribute.
     *
     * @param  name the name of the attribute.
     * @param  value the <code>long</code> value for the specified attribute.
     * @see    #getAttribute(String, long)
     */
    public void setAttribute(String name, long value) {
        _attributes.add(name, TextBuilder.newInstance().append(value));
    }

    /**
     * Sets the specified <code>float</code> attribute.
     *
     * @param  name the name of the attribute.
     * @param  value the <code>float</code> value for the specified attribute.
     * @see    #getAttribute(String, float)
    /*@FLOATING_POINT@
    public void setAttribute(String name, float value) {
        _attributes.add(name, TextBuilder.newInstance().append(value));
    }
    /**/

    /**
     * Sets the specified <code>double</code> attribute.
     *
     * @param  name the name of the attribute.
     * @param  value the <code>double</code> value for the specified attribute.
     * @see    #getAttribute(String, double)
    /*@FLOATING_POINT@
    public void setAttribute(String name, double value) {
        _attributes.add(name, TextBuilder.newInstance().append(value));
    }
    /**/

    /**
     * Sets the specified <code>Enum</code> attribute.
     *
     * @param  name the name of the attribute.
     * @param  value the <code>Enum</code> value for the specified attribute.
     * @see    #getAttribute(String, Enum)
     */
    public void setAttribute(String name, Enum value) {
        setAttribute(name, value.name());
    }

    /////////////////////
    // Deserialization //
    /////////////////////

    /**
     * Returns the Java(tm) class corresponding to this XML element; unless
     * {@link XmlFormat#setAlias(Class, String) aliases} are used, this class
     * is identified by the tag name of this xml element. 
     *
     * @return this XML element's corresponding class.
     */
    public Class objectClass() {
        return _objectClass;
    }

    /**
     * Returns the parent of this XML element (container element).
     *
     * @return this XML element's parent or <code>null</code> if this element
     *         is a root element.
     */
    public XmlElement getParent() {
        return _parent;
    }

    /**
     * Returns the attribute name-value mapping; values are instances of 
     * {@link CharSequence}.
     *
     * @return the attributes mapping.
     */
    public Attributes getAttributes() {
        return _attributes;
    }

    /**
     * Indicates if the specified attribute is present.
     *
     * @param  name the name of the attribute.
     * @return <code>true</code> if this xml element contains the specified
     *         attribute; <code>false</code> otherwise.
     */
    public boolean isAttribute(String name) {
        return _attributes.indexOf(name) >= 0;
    }

    /**
     * Searches for the specified attribute (generic).
     *
     * @param  name the name of the attribute.
     * @return the value of the specified attribute or <code>null</code>
     *         if there is no mapping for the specified attribute.
     */
    public CharSequence getAttribute(String name) {
        int index = _attributes.indexOf(name);
        return index >= 0 ? _attributes._values[index] : null;
    }

    /**
     * Searches for the specified <code>String</code> attribute.
     *
     * @param  name the name of the attribute.
     * @param  defaultValue a default value.
     * @return the value for the specified attribute or
     *         the <code>defaultValue</code> if the attribute is not found.
     */
    public String getAttribute(String name, String defaultValue) {
        CharSequence value = getAttribute(name);
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
        CharSequence chars = getAttribute(name);
        return (chars != null) ? TypeFormat.parseBoolean(chars) : defaultValue;
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
        CharSequence chars = getAttribute(name);
        return (chars != null) ? TypeFormat.parseInt(chars) : defaultValue;
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
        CharSequence chars = getAttribute(name);
        return (chars != null) ? TypeFormat.parseLong(chars) : defaultValue;
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
        CharSequence chars = getAttribute(name);
        return (chars != null) ? (float) TypeFormat.parseDouble(chars)
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
        CharSequence chars = getAttribute(name);
        return (chars != null) ? TypeFormat.parseDouble(chars) : defaultValue;
    }
    /**/

    /**
     * Returns the specified <code>Enum</code> attribute.
     *
     * @param  name the name of the attribute searched for.
     * @param  defaultValue the value returned if the attribute is not found.
     * @return the <code>Enum</code> value for the specified attribute or
     *         the default value if the attribute is not found.
     */
    public Enum getAttribute(String name, Enum defaultValue) {
        CharSequence chars = getAttribute(name);
        return (chars != null) ? Enum.valueOf(defaultValue.getClass(), chars
                .toString()) : defaultValue;
    }

    /**
     * Resets this XML element for reuse.
     */
    void reset() {
        _object = null;
        _format = null;
        _objectClass = null;
        _idValue = null;
        _attributes.clear();
        _content.clear();
    }
    
    /**
     * Holds attributes implementation.
     */
    static final class AttributesImpl implements Attributes {
        Object _names[] = new Object[16]; // String or CharSequence.
        CharSequence[] _values = new CharSequence[16];
        int _length = 0;

        public void add(Object qName, CharSequence value) {
            if (_length == _names.length) { // Resizes.
                Object[] tmp0 = new Object[_length * 2];
                System.arraycopy(_names, 0, tmp0, 0, _length);
                _names = tmp0;
                CharSequence[] tmp1 = new CharSequence[_length * 2];
                System.arraycopy(_values, 0, tmp1, 0, _length);
                _values = tmp1;
            }
            _names[_length] = qName;
            _values[_length++] = value;
        }

        public void clear() {
            for (int i=_length; i > 0;) {
                _names[--i] = null;
                _values[i] = null;
            }
            _length = 0;
        }
        
        public int indexOf(Object name) {
            for (int i=_length; i > 0;) {
                if (name.equals(_names[--i]) || _names[i].equals(name)) {
                    return i;
                }
            }
            return -1;
        }

        // Implements Attributes Interface.
        //
        
        public int getLength() {
            return _length;
        }

        public CharSequence getURI(int index) {
            return (index >= 0 && index < _length) ? Text.EMPTY : null;
        }

        public CharSequence getLocalName(int index) {
            return getQName(index);
        }

        public CharSequence getQName(int index) {
            if (index >= 0 && index < _length) {
                Object obj = _names[index];
                return (obj instanceof CharSequence) ? (CharSequence) obj:
                    TextBuilder.newInstance().append(obj);
            } else {
                return null;
            }
        }

        public String getType(int index) {
            return (index >= 0 && index < _length) ? "CDATA" : null;
        }

        public CharSequence getValue(int index) {
            return (index >= 0 && index < _length) ? _values[index] : null;
        }

        public int getIndex(CharSequence uri, CharSequence localName) {
            return uri.length() == 0 ? getIndex(localName) : -1;
        }

        public int getIndex(CharSequence qName) {
            return indexOf(qName);
        }

        public String getType(CharSequence uri, CharSequence localName) {
            return (getIndex(uri, localName) >= 0) ? "CDATA" : null;
        }

        public String getType(CharSequence qName) {
            return (getIndex(qName) >= 0) ? "CDATA" : null;
        }

        public CharSequence getValue(CharSequence uri, CharSequence localName) {
            int index = getIndex(uri, localName);
            return (index >= 0) ? _values[index] : null;
        }

        public CharSequence getValue(CharSequence qName) {
            int index = getIndex(qName);
            return (index >= 0) ? _values[index] : null;
        }
    }
}