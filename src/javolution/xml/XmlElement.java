/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2004 - The Javolution Team (http://javolution.org/)
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.xml;

import j2me.lang.CharSequence;
import javolution.lang.Enum;
import javolution.lang.Text;
import javolution.lang.TypeFormat;
import javolution.util.FastList;
import javolution.util.FastMap;

/**
 * <p> This class represents a XML element. Instances of this class are made
 *     available only during the XML serialization/deserialization process.</p>
 * <p> During serialization, {@link XmlFormat#format} is used to represent
 *     the Java objects into XML.
 * <p> During deserialization, {@link XmlFormat#parse} is used to restore
 *     the objects from their XML representations.</p>
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 1.0, October 4, 2004
 */
public final class XmlElement extends FastList {

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
     * Holds the element id (if any).
     */
    CharSequence _id;

    /**
     * Holds the attributes (CharSequence/String to CharSequence).
     */
    final FastMap _attributes = new FastMap()
            .setKeyComparator(FastMap.KeyComparator.CHAR_SEQUENCE);

    /**
     * Holds the parent of this element.
     */
    XmlElement _parent;

    /**
     * Default constructor.
     */
    XmlElement() {
    }

    /**
     * Returns the default object corresponding to this xml element. 
     * If the object has been {@link XmlFormat#preallocate preallocated}
     * then the preallocated instance is returned; otherwise an object
     * created using the public constructor of the class corresponding to
     * this xml element is returned.
     * 
     * @return the default object for this xml element.
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

    ///////////////////
    // Serialization //
    ///////////////////

    /**
     * Sets the specified <code>CharSequence</code> attribute (method 
     * used by {@link ConstructorHandler}).
     *
     * @param  name the attributes' name.
     * @param  value the attributes' value.
     */
    void setAttribute(CharSequence name, CharSequence value) {
        _attributes.put(name, value);
    }

    /**
     * Sets the specified attribute (generic).
     *
     * @param  name the attributes' name.
     * @param  value the attributes' value.
     */
    public void setAttribute(String name, CharSequence value) {
        _attributes.put(name, value);
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
            _attributes.put(name, (CharSequence)objValue);
        } else {
            _attributes.put(name, Text.valueOf(value));
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
        _attributes.put(name, Text.valueOf(value));
    }

    /**
     * Sets the specified <code>int</code> attribute.
     *
     * @param  name the name of the attribute.
     * @param  value the <code>int</code> value for the specified attribute.
     * @see    #getAttribute(String, int)
     */
    public void setAttribute(String name, int value) {
        _attributes.put(name, Text.valueOf(value));
    }

    /**
     * Sets the specified <code>long</code> attribute.
     *
     * @param  name the name of the attribute.
     * @param  value the <code>long</code> value for the specified attribute.
     * @see    #getAttribute(String, long)
     */
    public void setAttribute(String name, long value) {
        _attributes.put(name, Text.valueOf(value));
    }

    /**
     * Sets the specified <code>float</code> attribute.
     *
     * @param  name the name of the attribute.
     * @param  value the <code>float</code> value for the specified attribute.
     * @see    #getAttribute(String, float)
    /*@FLOATING_POINT@
    public void setAttribute(String name, float value) {
        _attributes.put(name, Text.valueOf(value));
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
        _attributes.put(name, Text.valueOf(value));
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
     * Returns the class of the object corresponding to this XML element.
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
    public FastMap getAttributes() {
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
        return _attributes.containsKey(name);
    }

    /**
     * Searches for the specified attribute (generic).
     *
     * @param  name the name of the attribute.
     * @return the value of the specified attribute or <code>null</code>
     *         if there is no mapping for the specified attribute.
     */
    public CharSequence getAttribute(String name) {
        return (CharSequence) _attributes.get(name);
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
        Object value = _attributes.get(name);
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
        this._object = null;
        this._format = null;
        this._objectClass = null;
        this._id = null;
        _attributes.clear();
        this.clear(); // Content.
    }
}