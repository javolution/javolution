/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2005 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.xml.sax;

import org.xml.sax.SAXException;

/**
 * This class represents the namespace context when parsing.
 *
 * @author  <a href="mailto:sax@megginson.com">David Megginson</a>
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0, January 25, 2004
 */
final class Namespace {

    /**
     * Holds the stack of pairs: [prefix, uri] or [element, null].
     * The pair [element, null] is on top of the stack after the element
     * start tag is read.
     */
    private CharSequenceImpl[] _stack = new CharSequenceImpl[64];

    /**
     * Holds the stack current length.
     */
    private int _length;

    /**
     * Holds the default namespace URI.
     */
    private CharSequenceImpl _default;

    /**
     * Holds the content handler to notify when prefix are mapped/unmapped.
     */
    private ContentHandler _contentHandler;

    /**
     * Default constructor.
     */
    public Namespace() {
        reset();
    }

    /**
     * Sets the local namespace context as of the specified element.
     *
     * @param  element the element (being copied).
     */
    public void push(CharSequenceImpl element) {
        if (_stack.length - _length < 2) {
            ensureCapacity(_stack.length * 2);
        }
        _stack[_length++] = element;
        _stack[_length++] = null; // Marks as element.
    }

    /**
     * Terminates the local element namespace context.
     *
     * @return the local element namespace context being terminated.
     * @throws SAXException any SAX exception, possibly wrapping another
     *         exception.
     */
    public CharSequenceImpl pop() throws SAXException {
        _length -= 2;
        CharSequenceImpl topElement = _stack[_length];
        for (; _length >= 2; _length -= 2) {
            CharSequenceImpl chars = _stack[_length-1];
            if (chars == null) { // Found enclosing element, done.
                return topElement;
            }
            CharSequenceImpl prefix = _stack[_length-2];
            _contentHandler.endPrefixMapping(prefix);
            if (prefix.length == 0) { // Unmapped default namespace.
                _default = getUri(CharSequenceImpl.EMPTY);
            }
        }
        return topElement;
    }

    /**
     * Maps the specified prefix to the specified URI.
     *
     * @param  prefix the prefix to be mapped (by reference).
     * @param  uri the associated uri.
     * @throws SAXException any SAX exception, possibly wrapping another
     *         exception.
     */
    public void map(CharSequenceImpl prefix, CharSequenceImpl uri)
            throws SAXException {
        if (_stack.length - _length < 2) {
            ensureCapacity(_stack.length * 2);
        }
        _stack[_length++] = prefix;
        _stack[_length++] = uri;
        _contentHandler.startPrefixMapping(prefix, uri);
        if (prefix.length == 0) {  // Maps default namespace.
            _default = uri;
        }
    }

    /**
     * Returns the URI mapped to the specified prefix.
     *
     * @param  prefix the prefix being searched.
     * @return the associated uri or <code>null</code> if the prefix is not
     *         mapped.
     */
    public CharSequenceImpl getUri(CharSequenceImpl prefix) {
        for (int i=_length-1; i > 0; i-=2) {
            CharSequenceImpl uri = _stack[i];
            if (uri != null) { // Not an element.
                CharSequenceImpl pfx = _stack[i-1];
                if (pfx.equals(prefix)) {
                    return uri;
                }
            }
        }
        return null;
    }

    /**
     * Returns the default namespace (uri mapped to prefix <code>""</code>).
     *
     * @return the default namespace uri or <code>""</code> (default).
     */
    public CharSequenceImpl getDefault() {
        return _default;
    }

    /**
     * Sets the content handler to notify when prefixes are mapped/unmapped.
     *
     * @param  handler the content handler.
     */
    public void setContentHandler(ContentHandler handler) {
        _contentHandler = handler;
    }

    /**
     * Resets this {@link Namespace} for reuse. 
     * The namespace xml is mapped to "http://www.w3.org/XML/1998/namespace".
     * The namespace xmlns is mapped to "http://www.w3.org/2000/xmlns/".
     */
    public void reset() {
        // Sets default namespace.
        _stack[0] = CharSequenceImpl.EMPTY;
        _stack[1] = CharSequenceImpl.EMPTY;
        _default = CharSequenceImpl.EMPTY;

        // Maps xmlns prefix.
        _stack[2] = XMLNS_PREFIX;
        _stack[3] = XMLNS_URI;

        // Maps xml prefix.
        _stack[4] = XML_PREFIX;
        _stack[5] = XML_URI;

        _length = 6;

        // Ensures than too many pop returns an empty element.
        push(CharSequenceImpl.EMPTY);
    }
    private static final CharSequenceImpl XML_PREFIX
        = new CharSequenceImpl("xml");
    private static final CharSequenceImpl XML_URI
        = new CharSequenceImpl("http://www.w3.org/XML/1998/namespace");
    private static final CharSequenceImpl XMLNS_PREFIX
        = new CharSequenceImpl("xmlns");
    private static final CharSequenceImpl XMLNS_URI
        = new CharSequenceImpl("http://www.w3.org/2000/xmlns/");
    

    /**
     * Ensures the specified capacity.
     *
     * @param  capacity the stack capacity.
     */
    public void ensureCapacity(int capacity) {
        CharSequenceImpl[] tmp = new CharSequenceImpl[capacity];
        System.arraycopy(_stack, 0, tmp, 0, _stack.length);
        _stack = tmp;
    }

}