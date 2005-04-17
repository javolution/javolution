/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2005 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.xml;

import javolution.lang.Reusable;
import javolution.lang.Text;
import javolution.lang.TextBuilder;
import javolution.realtime.ArrayFactory;
import javolution.util.FastComparator;
import javolution.xml.sax.Attributes;
import j2me.lang.CharSequence;

/**
 * This class represents the attributes list when formatting.
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 3.2, March 20, 2005
 */
final class FormatAttributes implements Attributes, Reusable {

    /**
     * Holds the factory for the internal names array.
     */
    private static final ArrayFactory NAMES_FACTORY = new ArrayFactory(32) {
        protected Object create(int length) {
            return new String[length];
        }
    };

    /**
     * Holds the factory for the internal values array.
     */
    private static final ArrayFactory VALUES_FACTORY = new ArrayFactory(32) {
        protected Object create(int length) {
            return new CharSequence[length];
        }
    };

    /**
     * Holds the factory for the appendable instances.
     */
    private static final ArrayFactory APPENDABLES_FACTORY = new ArrayFactory(32) {
        protected Object create(int length) {
            return new TextBuilder[length];
        }
    };

    /**
     * Holds the attributes names.
     */
    private String[] _names = (String[]) NAMES_FACTORY.newObject();

    /**
     * Holds the attributes values.
     */
    private CharSequence[] _values = (CharSequence[]) VALUES_FACTORY
            .newObject();

    /**
     * Holds the appendables.
     */
    private TextBuilder[] _appendables = (TextBuilder[]) APPENDABLES_FACTORY
            .newObject();

    /**
     * Holds the number of attributes.
     */
    private int _length;

    /**
     * Holds the current capacity.
     */
    private int _capacity;

    /**
     * Default constructor.
     */
    FormatAttributes() {
    }

    /**
     * Adds the specified name/value attribute.
     * 
     * @param name the attribute name.
     * @param value the attribute value.
     */
    public void add(String name, Object value) {
        if (value instanceof CharSequence) {
            if (_length >= _capacity) {
                increaseCapacity();
            }
            _names[_length] = name;
            _values[_length++] = (CharSequence) value;
        } else {
            newAttribute(name).append(value);
        }
    }

    /**
     * Returns a new attribute having the specified name.
     * 
     * @param name the attribute name.
     * @return the text builder where to store the attribute value.
     */
    public TextBuilder newAttribute(String name) {
        if (_length >= _capacity) {
            increaseCapacity();
        }
        _names[_length] = name;
        _values[_length] = _appendables[_length];
        return _appendables[_length++];
    }

    /**
     * Resets this attributes list for reused.
     */
    public void reset() {
        for (int i = 0; i < _length;) {
            _names[i] = null;
            _values[i] = null;
            _appendables[i++].reset();
        }
        _length = 0;
    }

    // Implements attributes interface.
    public int getLength() {
        return _length;
    }

    // Implements attributes interface.
    public CharSequence getURI(int index) {
        return (index >= 0 && index < _length) ? Text.EMPTY : null;
    }

    // Implements attributes interface.
    public CharSequence getLocalName(int index) {
        return getQName(index);
    }

    // Implements attributes interface.
    public CharSequence getQName(int index) {
        return (index >= 0 && index < _length) ? toCharSeq(_names[index])
                : null;
    }

    // Implements attributes interface.
    public String getType(int index) {
        return (index >= 0 && index < _length) ? "CDATA" : null;
    }

    // Implements attributes interface.
    public CharSequence getValue(int index) {
        return (index >= 0 && index < _length) ? _values[index] : null;
    }

    // Implements attributes interface.
    public int getIndex(CharSequence uri, CharSequence localName) {
        if (uri.length() != 0)
            return -1;
        return getIndex(localName);
    }

    // Implements attributes interface.
    public int getIndex(CharSequence qName) {
        for (int i = 0; i < _length; i++) {
            if (FastComparator.LEXICAL.areEqual(_names[i], qName))
                return i;
        }
        return -1;
    }

    // Implements attributes interface.
    public String getType(CharSequence uri, CharSequence localName) {
        return (getIndex(uri, localName) >= 0) ? "CDATA" : null;
    }

    // Implements attributes interface.
    public String getType(CharSequence qName) {
        return (getIndex(qName) >= 0) ? "CDATA" : null;
    }

    // Implements attributes interface.
    public CharSequence getValue(CharSequence uri, CharSequence localName) {
        int index = getIndex(uri, localName);
        return (index >= 0) ? _values[index] : null;
    }

    // Implements attributes interface.
    public CharSequence getValue(CharSequence qName) {
        int index = getIndex(qName);
        return (index >= 0) ? _values[index] : null;
    }

    // Increases capacity.
    private void increaseCapacity() {
        if (_capacity >= _names.length) {
            NAMES_FACTORY.resize(_names);
        }
        if (_capacity >= _values.length) {
            VALUES_FACTORY.resize(_values);
        }
        if (_capacity >= _appendables.length) {
            APPENDABLES_FACTORY.resize(_appendables);
        }
        _appendables[_capacity++] = (TextBuilder) TextBuilder.newInstance()
                .moveHeap();
    }

    /**
     * Converts a String to CharSequence (for J2ME compatibility)
     * 
     * @param str the String to convert.
     * @return the corresponding CharSequence instance.
     */
    private static CharSequence toCharSeq(Object str) {
        return (str instanceof CharSequence) ? (CharSequence) str : Text
                .valueOf((String) str);
    }

}