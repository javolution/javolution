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
import javolution.lang.TextBuilder;
import javolution.lang.TypeFormat;
import javolution.realtime.ObjectFactory;
import javolution.util.FastComparator;
import javolution.util.FastList;
import javolution.util.FastMap;
import javolution.xml.sax.Attributes;

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
 * @version 3.2, March 18, 2005
 */
public final class XmlElement {

    /**
     * Holds the associate factory (for preallocation). 
     */
    static final ObjectFactory FACTORY = new ObjectFactory() {
        protected Object create() {
            return new XmlElement();
        }
    };

    /**
     * Holds the object class corresponding to this xml element.
     */
    Class _objectClass;

    /**
     * Holds the parent xml element.
     */
    XmlElement _parent;

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
     * Holds this element attributes when parsing. 
     */
    Attributes _parseAttributes;

    /**
     * Holds the name for this xml element if any.
     */
    CharSequence _name;

    /**
     * Holds the anoymous elements content.
     */
    final FastList _content = new FastList();

    /**
     * Holds the named child elements.
     */
    final FastMap _nameToChild
        = new FastMap().setKeyComparator(FastComparator.LEXICAL); 

    /**
     * Holds the pool of element names (TextBuilder) available.
     */
    private final FastList _namesAvail = new FastList(); 

    /**
     * Holds the pool of element names (TextBuilder) currently used.
     */
    private final FastList _namesUsed = new FastList(); 

    /**
     * Holds this element attributes when formatting. 
     */
    private final FormatAttributes _formatAttributes = new FormatAttributes();

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
     * Returns the list of anonymous objects associated to this xml element
     * (positional association). During serialization these objects are 
     * represented as child elements with their class name (or alias) for
     * element name. During deserialization this list is constructed from 
     * deserialization of all anonymous child elements.
     *
     * @return the list of anonymous objects content of this xml element.
     */
    public FastList getContent() {
        return _content;
    }

    ///////////////////
    // Serialization //
    ///////////////////
        
    /**
     * Adds a named element corresponding to the specified object
     * (<code>null</code> objects are ignored). Named elements are 
     * always serialized before anonymous one (see {@link #getContent}).
     *
     * @param name the tag name of the nested xml element corresponding 
     *        to the object being added.
     * @param obj the object added as child element or <code>null</code>.
     */
    public void add(String name, Object obj) {
        if (obj == null) return;
        _nameToChild.put(name, obj);
    }

    /**
     * Sets the specified <code>CharSequence</code> attribute
     * (<code>null</code> values are ignored).
     *
     * @param  name the attribute name.
     * @param  value the attribute value or <code>null</code>.
     */
    public void setAttribute(String name, CharSequence value) {
        if (value == null)
            return;
        _formatAttributes.add(name, value);
    }

    /**
     * Sets the specified <code>String</code> attribute
     * (<code>null</code> values are ignored).
     *
     * @param  name the attribute name.
     * @param  value the attribute value.
     */
    public void setAttribute(String name, String value) {
        if (value == null)
            return;
        _formatAttributes.add(name, value);
    }
    

    /**
     * Sets the specified <code>boolean</code> attribute.
     *
     * @param  name the name of the attribute.
     * @param  value the <code>boolean</code> value for the specified attribute.
     * @see    #getAttribute(String, boolean)
     */
    public void setAttribute(String name, boolean value) {
        _formatAttributes.newAttribute(name).append(value);
    }

    /**
     * Sets the specified <code>int</code> attribute.
     *
     * @param  name the name of the attribute.
     * @param  value the <code>int</code> value for the specified attribute.
     * @see    #getAttribute(String, int)
     */
    public void setAttribute(String name, int value) {
        _formatAttributes.newAttribute(name).append(value);
    }

    /**
     * Sets the specified <code>long</code> attribute.
     *
     * @param  name the name of the attribute.
     * @param  value the <code>long</code> value for the specified attribute.
     * @see    #getAttribute(String, long)
     */
    public void setAttribute(String name, long value) {
        _formatAttributes.newAttribute(name).append(value);
    }

    /**
     * Sets the specified <code>float</code> attribute.
     *
     * @param  name the name of the attribute.
     * @param  value the <code>float</code> value for the specified attribute.
     * @see    #getAttribute(String, float)
     /*@FLOATING_POINT@
     public void setAttribute(String name, float value) {
     _formatAttributes.newAttribute(name).append(value);
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
     _formatAttributes.newAttribute(name).append(value);
     }
     /**/

    /////////////////////
    // Deserialization //
    /////////////////////

    /**
     * Adds a child element having the specified element name (mutable). 
     *
     * @param  name the name for the object added.
     * @param  obj the object added as child element.
     */
    void add(CharSequence name, Object obj) {
        TextBuilder tb = _namesAvail.size() > 0 ? 
                (TextBuilder) _namesAvail.removeLast() : 
                    (TextBuilder)TextBuilder.newInstance().moveHeap();
        _namesUsed.addLast(tb);
        tb.reset();
        tb.append(name);
        _nameToChild.put(tb, obj);
    }

    /**
     * Returns the parent container xml element.
     *
     * @return the parent xml element or <code>null</code> if root element.
     */
    public XmlElement getParent() {
        return _parent;
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
     * Returns the object corresponding to the child element having the 
     * specified element name (tag name).
     *
     * @param name the name of the child element.
     * @return the object corresponding to the child element.
     */
    public Object get(String name) {
        return _nameToChild.get(name);
    }

    /**
     * Returns the attributes for this xml element.
     *
     * @return the attributes mapping.
     */
    public Attributes getAttributes() {
        // When parsing, returns the parsing attributes; 
        // otherwise returns formatting attributes.
        return (_parseAttributes != null) ? _parseAttributes : 
            _formatAttributes;
    }

    /**
     * Searches for the attribute having the specified name.
     *
     * @param  name the name of the attribute.
     * @return the value for the specified attribute or <code>null</code>
     *         if the attribute is not found.
     */
    public CharSequence getAttribute(String name) {
        final Attributes attributes = getAttributes();
        final int length = attributes.getLength();
        for (int i=0; i < length; i++) {
            CharSequence attName = attributes.getQName(i);
            if (attName.equals(name)) 
                return attributes.getValue(i);
        }
        return null;
    }

    /**
     * Indicates if the specified attribute is present.
     *
     * @param  name the name of the attribute.
     * @return <code>true</code> if this xml element contains the specified
     *         attribute; <code>false</code> otherwise.
     */
    public boolean isAttribute(String name) {
        return getAttribute(name) != null;
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
     * Resets this XML element for reuse.
     */
    void reset() {
        _object = null;
        _format = null;
        _objectClass = null;
        _idValue = null;
        _name = null;
        _parseAttributes = null;
        _content.clear();
        _nameToChild.clear();
        _namesAvail.addAll(_namesUsed);
        _namesUsed.clear();
        _formatAttributes.reset();
    }
}