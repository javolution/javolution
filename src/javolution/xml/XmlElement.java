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
import javolution.lang.Text;
import javolution.lang.TextBuilder;
import javolution.lang.TypeFormat;
import javolution.realtime.ObjectFactory;
import javolution.util.FastComparator;
import javolution.util.FastList;
import javolution.util.FastMap;
import javolution.util.FastTable;
import javolution.xml.sax.Attributes;
import javolution.xml.sax.AttributesImpl;

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
     * Holds this element attributes when parsing/formatting. 
     */
    AttributesImpl _attributes = new AttributesImpl();

    /**
     * Holds the name for this xml element if any.
     */
    CharSequence _name;

    /**
     * Holds the anonymous elements content.
     */
    final FastList _content = new FastList();

    /**
     * Holds the named child elements.
     */
    final FastMap _nameToChild
        = new FastMap().setKeyComparator(FastComparator.LEXICAL); 

    /**
     * Holds a pool of text builder instances.
     */
    private FastTable _pool = new FastTable();
    
    /**
     * Holds the index of the first free instance in the pool.
     */
    private int _poolIndex;
    
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
     * Returns a new text builder to hold the specified attribute.
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
        TextBuilder value = newTextBuilder();
        setAttribute(name, value);
        return value;
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
        CharSequence csqName = toCharSeq(name);
        _attributes.addAttribute(Text.EMPTY, csqName, Text.EMPTY, csqName, "CDATA", value);
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
        CharSequence csqName = toCharSeq(name);
        CharSequence csqValue = toCharSeq(value);
        _attributes.addAttribute(Text.EMPTY, csqName, Text.EMPTY, csqName, "CDATA", csqValue);
    }

    /**
     * Sets the specified <code>boolean</code> attribute.
     * 
     * @param  name the name of the attribute.
     * @param  value the <code>boolean</code> value for the specified attribute.
     * @see    #getAttribute(String, boolean)
     */
    public void setAttribute(String name, boolean value) {
        newAttribute(name).append(value);
    }

    /**
     * Sets the specified <code>int</code> attribute.
     * 
     * @param  name the name of the attribute.
     * @param  value the <code>int</code> value for the specified attribute.
     * @see    #getAttribute(String, int)
     */
    public void setAttribute(String name, int value) {
        newAttribute(name).append(value);
    }

    /**
     * Sets the specified <code>long</code> attribute.
     * 
     * @param  name the name of the attribute.
     * @param  value the <code>long</code> value for the specified attribute.
     * @see    #getAttribute(String, long)
     */
    public void setAttribute(String name, long value) {
        newAttribute(name).append(value);
    }

    /**
     * Sets the specified <code>float</code> attribute.
     * 
     * @param  name the name of the attribute.
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
     * @param  name the name of the attribute.
     * @param  value the <code>double</code> value for the specified attribute.
     * @see    #getAttribute(String, double)
     /*@FLOATING_POINT@
     public void setAttribute(String name, double value) {
        newAttribute(name).append(value);
     }
     /**/

     /**
      * Sets the specified <code>Byte</code> attribute.
      * 
      * @param  name the name of the attribute.
      * @param  value the <code>Byte</code> value for the specified attribute.
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
      * @param  value the <code>Short</code> value for the specified attribute.
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
      * @param  value the <code>Integer</code> value for the specified attribute.
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
      * @param  value the <code>Long</code> value for the specified attribute.
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
      * @param  value the <code>Float</code> value for the specified attribute.
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
      * @param  value the <code>Double</code> value for the specified attribute.
      * @see    #getAttribute(String, Double)
     /*@FLOATING_POINT@
     public void setAttribute(String name, Double value) {
         if (value == null)
             return;
         newAttribute(name).append(value.doubleValue());
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
        TextBuilder tb = newTextBuilder();
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
        return _attributes;
    }

    /**
     * Searches for the attribute having the specified name.
     *
     * @param  name the qualified name of the attribute (qName).
     * @return the value for the specified attribute or <code>null</code>
     *         if the attribute is not found.
     */
    public CharSequence getAttribute(String name) {
        return _attributes.getValue(name);
    }

    /**
     * Indicates if the specified attribute is present.
     *
     * @param  name the qualified name of the attribute (qName).
     * @return <code>true</code> if this xml element contains the specified
     *         attribute; <code>false</code> otherwise.
     */
    public boolean isAttribute(String name) {
        return _attributes.getIndex(name) >= 0;
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
        CharSequence value = _attributes.getValue(name);
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
        CharSequence value = _attributes.getValue(name);
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
        CharSequence value = _attributes.getValue(name);
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
        CharSequence value = _attributes.getValue(name);
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
        CharSequence value = _attributes.getValue(name);
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
        CharSequence value = _attributes.getValue(name);
        return (value != null) ? TypeFormat.parseDouble(value) : defaultValue;
    }
     /**/
     
    /**
     * Searches for the specified <code>Byte</code> attribute.
     *
     * @param  name the name of the attribute.
     * @param  defaultValue the value returned if the attribute is not found.
     * @return the <code>Byte</code> value for the specified attribute or
     *         the default value if the attribute is not found.
     */
    public Byte getAttribute(String name, Byte defaultValue) {
        CharSequence value = _attributes.getValue(name);
        return (value != null) ? new Byte(TypeFormat.parseByte(value)) : defaultValue;
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
        CharSequence value = _attributes.getValue(name);
        return (value != null) ? new Short( TypeFormat.parseShort(value) ) : defaultValue;
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
        CharSequence value = _attributes.getValue(name);
        return (value != null) ? new Integer( TypeFormat.parseInt(value) ) : defaultValue;
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
        CharSequence value = _attributes.getValue(name);
        return (value != null) ? new Long( TypeFormat.parseLong(value) ) : defaultValue;
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
        CharSequence value = _attributes.getValue(name);
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
        CharSequence value = _attributes.getValue(name);
        return (value != null) ? new Double( TypeFormat.parseDouble(value) ) : defaultValue;
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
        _content.clear();
        _nameToChild.clear();
        _attributes.reset();
        _poolIndex = 0;
    }

    /**
     * Converts a String to CharSequence (for J2ME compatibility)
     * 
     * @param str the String to convert.
     * @return the corresponding CharSequence instance.
     */
    private CharSequence toCharSeq(Object str) {
        if (str instanceof CharSequence) 
            return (CharSequence) str;
        // Copies the string to a TextBuilder from the pool (J2ME).
        if (_poolIndex >= _pool.size()) {
            _pool.addLast(TextBuilder.newInstance());
        }
        TextBuilder tb = (TextBuilder) _pool.get(_poolIndex++);
        tb.reset();
        tb.append(str);
        return tb;
    }

    /**
     * Returns a text builder instance from the internal pool.
     */
    private TextBuilder newTextBuilder() {
        if (_poolIndex >= _pool.size()) {
            _pool.addLast(TextBuilder.newInstance().moveHeap());
        }
        TextBuilder tb = (TextBuilder) _pool.get(_poolIndex++);
        tb.reset();
        return tb;
    }

    /**
     * Indicates if the current object is already referenced in the 
     * element hierarchy (parents).
     */
    boolean isRecursion() {
        for (XmlElement xml = _parent; xml != null; xml = xml._parent) {
            if (xml._object == _object) return true;
        }
        return false;
    }
}