/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2007 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package _templates.javolution.xml;

import _templates.java.io.ObjectStreamException;
import _templates.java.lang.CharSequence;
import _templates.javolution.lang.Immutable;
import _templates.javolution.text.CharArray;
import _templates.javolution.text.TextBuilder;
import _templates.javolution.util.FastComparator;
import _templates.javolution.util.FastMap;
import _templates.javolution.xml.stream.XMLStreamException;

/**
 * <p> This class represents unique identifiers for XML elements (tags) or 
 *     attributes (names).</p>
 *     
 * <p> It should be noted that <code>QName.valueOf(null, "name")</code> and 
 *     <code>QName.valueOf("", "name")</code> are distinct; the first one has no 
 *     namespace associated with; whereas the second is associated 
 *     to the root namespace.</p>
 *     
 * <p> {@link QName} have a textual representation ({@link CharSequence}) which 
 *     is either the local name (if no namespace URI) or 
 *     <code>{namespaceURI}localName</code> (otherwise).</p>     
 *     
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 5.3, January 14, 2007
 * @see <a href="http://en.wikipedia.org/wiki/Qname">Wikipedia: QName</a> 
 */
public final class QName implements XMLSerializable, Immutable, CharSequence {

    /**
     * Holds default XML representation.
     */
    static final XMLFormat XML = new XMLFormat(QName.class) {

        public Object newInstance(Class cls, InputElement xml)
                throws XMLStreamException {
            CharSequence namespaceURI = xml.getAttribute("namespaceURI");
            CharSequence localName = xml.getAttribute("localName");
            return QName.valueOf(namespaceURI, localName);
        }

        public void read(InputElement xml, Object obj)
                throws XMLStreamException {
            // Do nothing (attribute already read).
        }

        public void write(Object obj, OutputElement xml)
                throws XMLStreamException {
            QName qName = (QName) obj;
            xml.setAttribute("namespaceURI", qName._namespaceURI);
            xml.setAttribute("localName", qName._localName);
        }
    };

    /**
     * Holds the local name.
     */
    private transient final CharArray _localName;

    /**
     * Holds the namespace URI reference or <code>null</code> if none.
     */
    private transient final CharArray _namespaceURI;

    /**
     * Holds the string representation.
     */
    private final String _toString;

    /**
     * Holds the full name (String) to QName mapping.
     */
    private static final FastMap FULL_NAME_TO_QNAME = new FastMap()
            .setKeyComparator(FastComparator.LEXICAL).setShared(true);

    /**
     * Creates a qualified name having the specified local name and namespace 
     * reference.
     * 
     * @param namespaceURI the URI reference or <code>null</code> if none.
     * @param localName the local name.
     * @param toString the string representation.
     */
    private QName(String namespaceURI, String localName, String toString) {
        _namespaceURI = (namespaceURI == null) ? null : new CharArray(namespaceURI);
        _localName = new CharArray(localName);
        _toString = toString;
    }

    /**
     * Returns the qualified name corresponding to the specified character 
     * sequence representation (may include the "{namespaceURI}" prefix).
     * 
     * @param name the qualified name lexical representation.
     * @see #toString()
     */
    public static QName valueOf(CharSequence name) {
        QName qName = (QName) FULL_NAME_TO_QNAME.get(name);
        return (qName != null) ? qName : QName.createNoNamespace(name.toString());
    }
    
    private static QName createNoNamespace(String name) {    
        String localName = name;
        String namespaceURI = null;
        if (name.length() > 0 && name.charAt(0) == '{') { // Namespace URI.
            int index = name.lastIndexOf('}');
            localName = name.substring(index + 1);
            namespaceURI = name.substring(1, index);
        }
        QName qName = new QName(namespaceURI, localName, name);
        synchronized (FULL_NAME_TO_QNAME) {
            QName tmp = (QName) FULL_NAME_TO_QNAME.putIfAbsent(name, qName);
            return tmp == null ? qName : tmp;
        }
    }

    /**
     * Equivalent to {@link #valueOf(CharSequence)} (for J2ME compatibility).
     * 
     * @param name the qualified name lexical representation.
     * @see #toString()
     */
    public static QName valueOf(String name) {
        QName qName = (QName) FULL_NAME_TO_QNAME.get(name);
        return (qName != null) ? qName : QName.createNoNamespace(name);
    }

    /**
     * Returns the qualified name corresponding to the specified namespace URI 
     * and local name.
     * 
     * @param namespaceURI the URI reference or <code>null</code> if none.
     * @param localName the local name.
     * @see #toString()
     */
    public static QName valueOf(CharSequence namespaceURI, CharSequence localName) {
        if (namespaceURI == null)
            return QName.valueOf(localName);
        TextBuilder tmp = TextBuilder.newInstance();
        try {
            tmp.append('{');
            tmp.append(namespaceURI);
            tmp.append('}');
            tmp.append(localName);
            return QName.valueOf(tmp);
        } finally {
            TextBuilder.recycle(tmp);
        }       
    }

    /**
     * Returns the local part of this qualified name or the full qualified 
     * name if there is no namespace.
     * 
     * @return the local name. 
     */
    public CharSequence getLocalName() {
        return _localName;
    }

    /**
     * Returns the namespace URI of this qualified name or <code>null</code>
     * if none (the local name is then the full qualified name).
     * 
     * @return the URI reference or <code>null</code> 
     */
    public CharSequence getNamespaceURI() {
        return _namespaceURI;
    }

    /**
     * Instances of this class are unique; object's equality can be 
     * replaced object identity (<code>==</code>).
     * 
     * @return <code>this == obj</code>  
     */
    public boolean equals(Object obj) {
        return this == obj;
    }

    /**
     * Returns the <code>String</code> representation of this qualified name.
     * 
     * @return the textual representation.
     */
    public String toString() {
        return _toString;
    }

    /**
     * Returns the hash code for this qualified name.
     *
     * <p> Note: Returns the same hashCode as <code>java.lang.String</code>
     *           (consistent with {@link #equals})</p>
     * @return the hash code value.
     */
    public int hashCode() {
        return _toString.hashCode();
    }

    /**
     * Returns the character at the specified index.
     *
     * @param  index the index of the character starting at <code>0</code>.
     * @return the character at the specified index of this character sequence.
     * @throws IndexOutOfBoundsException  if <code>((index < 0) || 
     *         (index >= length))</code>
     */
    public char charAt(int index) {
        return _toString.charAt(index);
    }

    /**
     * Returns the length of this character sequence.
     *
     * @return the number of characters (16-bits Unicode) composing this
     *         character sequence.
     */
    public int length() {
        return _toString.length();
    }

    /**
     * Returns a new character sequence that is a subsequence of this sequence.
     *
     * @param  start the index of the first character inclusive.
     * @param  end the index of the last character exclusive.
     * @return the character sequence starting at the specified
     *         <code>start</code> position and ending just before the specified
     *         <code>end</code> position.
     * @throws IndexOutOfBoundsException if <code>(start < 0) || (end < 0) ||
     *         (start > end) || (end > this.length())</code>
     */
    public CharSequence subSequence(int start, int end) {
        return _templates.javolution.Javolution.j2meToCharSeq(_toString.substring(start, end));
    }

    //Maintains unicity.
    private Object readResolve() throws ObjectStreamException {
        return QName.valueOf(_toString);
    }
}