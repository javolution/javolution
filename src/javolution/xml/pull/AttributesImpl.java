/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2005 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.xml.pull;

import javolution.lang.Reusable;
import javolution.realtime.ArrayFactory;
import javolution.realtime.ObjectFactory;
import javolution.xml.sax.Attributes;
import j2me.lang.CharSequence;

/**
 * <p> This class represents a list of XML attributes. It implements the 
 *     real-time version of Sax2 {@link Attributes} interface with 
 *     <code>String</code> replaced by <code>CharSequence</code>.</p>
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 3.2, April 2, 2005
 */
final class AttributesImpl implements Attributes, Reusable {

    /**
     * Holds the factory for this class.
     */
    static final ObjectFactory FACTORY = new ObjectFactory() {
        protected Object create() {
            return new AttributesImpl();
        }
    };
    /**
     * Holds the factory for single entries.
     */
    private static final ObjectFactory ENTRY_FACTORY = new ObjectFactory() {
        protected Object create() {
            return new Entry();
        }
    };
    
    /**
     * Holds the factory for the internal entries array.
     */
    private static final ArrayFactory ENTRIES_FACTORY = new ArrayFactory(32) {
        protected Object create(int length) {
            return new Entry[length];
        }
    };
    
    /**
     * Holds number of attributes.
     */
    private int _length;

    /**
     * Holds the attributes entries.
     */
    private Entry[] _entries = (Entry[]) ENTRIES_FACTORY.newObject();

    /**
     * Default constructor.
     */
    public AttributesImpl() {
    }

    /**
     * Returns the number of attributes in the list.
     *
     * @return the number of attributes in the list.
     */
    public int getLength() {
        return _length;
    }

    /**
     * Returns an attribute's namespace URI.
     *
     * @param  index the attribute's index (zero-based).
     * @return the namespace URI, the empty string if none is
     *         available, or null if the index is out of range.
     */
    public CharSequence getURI(int index) {
        if (index >= 0 && index < _length) {
            return _entries[index].uri;
        } else {
            return null;
        }
    }

    /**
     * Returns the attribute's prefix.
     *
     * @param  index the attribute's index (zero-based).
     * @return the attribute's prefix, the empty string if
     *         none is available, or null if the index if out of range.
     */
    public CharSequence getPrefix(int index) {
        if (index >= 0 && index < _length) {
            return _entries[index].prefix;
        } else {
            return null;
        }
    }

    /**
     * Returns an attribute's local name.
     *
     * @param  index the attribute's index (zero-based).
     * @return the attribute's local name, the empty string if
     *         none is available, or null if the index if out of range.
     */
    public CharSequence getLocalName(int index) {
        if (index >= 0 && index < _length) {
            return _entries[index].localName;
        } else {
            return null;
        }
    }

    /**
     * Returns an attribute's qualified (prefixed) name.
     * This method returns a new <code>String</code> instance.
     *
     * @param  index the attribute's index (zero-based).
     * @return <code>&lt;prefix&gt; + ":" + &lt;localName&gt;</code>,
     *         or null if the index is out of bounds.
     */
    public CharSequence getQName(int index) {
        if (index >= 0 && index < _length) {
            return _entries[index].qName;
        } else {
            return null;
        }
    }

    /**
     * Returns an attribute's type by index.
     *
     * @param  index the attribute's index (zero-based).
     * @return the attribute's type, "CDATA" if the type is unknown, or null
     *         if the index is out of bounds.
     */
    public String getType(int index) {
        if (index >= 0 && index < _length) {
            return "CDATA";
        } else {
            return null;
        }
    }

    /**
     * Returns an attribute's value by index.
     *
     * @param  index The attribute's index (zero-based).
     * @return the attribute's value or null if the index is out of bounds.
     */
    public CharSequence getValue(int index) {
        if (index >= 0 && index < _length) {
            return _entries[index].value;
        } else {
            return null;
        }
    }

    /**
     * Looks up an attribute's index by namespace name.
     *
     * <p>In many cases, it will be more efficient to look up the name once and
     * use the index query methods rather than using the name query methods
     * repeatedly.</p>
     *
     * @param  uri the attribute's namespace URI, or the empty string if none
     *         is available.
     * @param  localName the attribute's local name.
     * @return the attribute's index, or -1 if none matches.
     */
    public int getIndex(CharSequence uri, CharSequence localName) {
        for (int i = 0; i < _length; i++) {
            Entry entry = _entries[i];
            if (entry.localName.equals(localName) && entry.uri.equals(uri)) {
                return i;
            }
        }
        return -1;
    }

    // String version for XMLReaderImpl 
    int getIndex(String uri, String localName) {
        for (int i = 0; i < _length; i++) {
            Entry entry = _entries[i];
            if (entry.localName.equals(localName) && entry.uri.equals(uri)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Looks up an attribute's index by qualified (prefixed) name.
     *
     * @param  qName the qualified name.
     * @return the attribute's index, or -1 if none matches.
     */
    public int getIndex(CharSequence qName) {
        for (int i = 0; i < _length; i++) {
            if (_entries[i].qName.equals(qName)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Looks up an attribute's type by namespace-qualified name.
     *
     * @param  uri the namespace URI, or the empty string for a name
     *         with no explicit namespace URI.
     * @param  localName the local name.
     * @return the attribute's type, or null if there is no
     *         matching attribute.
     */
    public String getType(CharSequence uri, CharSequence localName) {
        return (getIndex(uri, localName) >= 0) ? "CDATA" : null;
    }

    /**
     * Looks up an attribute's type by qualified (prefixed) name.
     *
     * @param  qName the qualified name.
     * @return the attribute's type, or null if there is no matching attribute.
     */
    public String getType(CharSequence qName) {
        return (getIndex(qName) >= 0) ? "CDATA" : null;
    }

    /**
     * Looks up an attribute's value by namespace-qualified name.
     *
     * @param  uri The namespace URI, or the empty string for a name
     *         with no explicit namespace URI.
     * @param  localName The local name.
     * @return the attribute's value, or null if there is no matching attribute.
     */
    public CharSequence getValue(CharSequence uri, CharSequence localName) {
        int index = getIndex(uri, localName);
        return (index >= 0) ? _entries[index].value : null;
    }

    /**
     * Looks up an attribute's value by qualified (prefixed) name.
     *
     * @param  qName the qualified name.
     * @return the attribute's value, or null if there is no matching attribute.
     */
    public CharSequence getValue(CharSequence qName) {
        int index = getIndex(qName);
        return (index >= 0) ? _entries[index].value : null;
    }

    /**
     * Add an attribute to the end of the list.
     *
     * <p>For the sake of speed, this method does no checking
     * to see if the attribute is already in the list: that is
     * the responsibility of the application.</p>
     *
     * @param uri The namespace URI, or the empty string if
     *         none is available or namespace processing is not
     *         being performed.
     * @param prefix the name prefix.
     * @param localName the local name, or the empty string if
     *         namespace processing is not being performed.
     * @param qName the qualified (prefixed) name, or the empty string
     *        if qualified names are not available.
     * @param value the attribute value.
     */
    public void add(CharSequenceImpl uri, CharSequenceImpl prefix, 
            CharSequenceImpl localName, CharSequenceImpl qName, 
            CharSequenceImpl value) {
        if (_length >= _entries.length) {
            _entries = (Entry[]) ENTRIES_FACTORY.resize(_entries);
        }
        Entry entry = _entries[_length++];
        if (entry == null) {
            entry = _entries[_length - 1] = (Entry) ENTRY_FACTORY.newObject();
        }
        entry.uri = uri;
        entry.prefix = prefix;
        entry.localName = localName;
        entry.qName = qName;
        entry.value = value;
    }

    // Implements Reusable interface.
    public void reset() {
        _length = 0;
    }

    /**
     * This inner class represents a single attribute entry.
     */
    private static final class Entry {

        /**
         * Holds the attribute's URI.
         */
        private CharSequenceImpl uri;

        /**
         * Holds the attribute's prefix.
         */
        private CharSequenceImpl prefix;

        /**
         * Holds the attribute's local name.
         */
        private CharSequenceImpl localName;

        /**
         * Holds the attribute's qualified name (qName = prefix + ":" + localName).
         */
        private CharSequenceImpl qName;

        /**
         * Holds the attribute's value.
         */
        private CharSequenceImpl value;

    }
}