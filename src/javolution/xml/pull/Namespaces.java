/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2005 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.xml.pull;

import javolution.lang.PersistentReference;

/**
 * This class represents the namespaces stack when parsing.
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 3.2, April 2, 2005
 */
final class Namespaces {

    /**
     * Holds the configurable nominal size to avoid resizing. 
     */
    private static final PersistentReference SIZE = new PersistentReference(
            "javolution.xml.pull.Namespaces#SIZE", new Integer(64));

    /**
     * Holds the number of namespace per depth level.
     */
    private int[] _nspCounts = new int[((Integer) SIZE.get()).intValue()];

    /**
     * Holds the current number being mapped.
     */
    private int _mapCount;

    /**
     * Holds the namespace mapping [uri, prefix] pairs.
     */
    private CharSequenceImpl[] _namespaces = new CharSequenceImpl[((Integer) SIZE
            .get()).intValue()];

    /**
     * Holds the current depth.
     */
    private int _depth;

    /**
     * Holds the default namespace URI.
     */
    private CharSequenceImpl _default = CharSequenceImpl.EMPTY;

    /**
     * Default constructor.
     */
    public Namespaces() {
    }

    /**
     * Returns the default namespace.
     */
    public CharSequenceImpl getDefault() {
        return _default;
    }

    /**
     * Returns the numbers of elements in the namespace stack for the given
     * depth.
     * 
     * @param depth the element depth.
     */
    public int getNamespaceCount(int depth) {
        if (depth > _depth)
            return _nspCounts[_depth];
        return _nspCounts[depth];
    }

    /**
     * Returns the namespace prefix at the specified position.
     * 
     * @param pos the position in the namespace stack.
     * @return the namespace prefix.
     */
    public CharSequenceImpl getNamespacePrefix(int pos) {
        return _namespaces[pos << 1];
    }

    /**
     * Returns the namespace uri at the specified position.
     * 
     * @param pos the position in the namespace stack.
     * @return the namespace uri.
     */
    public CharSequenceImpl getNamespaceUri(int pos) {
        return _namespaces[(pos << 1) + 1];
    }

    /**
     * Returns the namespace for the specified prefix or the default 
     * namespace is the prefix is <code>null</code>.
     * 
     * @param prefix the prefix to search for or <code>null</code>.
     * @return the associated namespace uri.
     */
    public CharSequenceImpl getNamespaceUri(String prefix) {
        if (prefix == null)
            return _default;
        for (int i = _nspCounts[_depth] + _mapCount; i > 0;) {
            CharSequenceImpl pfx = _namespaces[--i << 1];
            if ((pfx != null) && pfx.equals(prefix))
                return _namespaces[(i << 1) + 1];
        }
        if (XML_PREFIX.equals(prefix))
            return XML_URI;
        if (XMLNS_PREFIX.equals(prefix))
            return XMLNS_URI;
        return null;
    }

    private static final CharSequenceImpl XML_PREFIX = new CharSequenceImpl(
            "xml");

    private static final CharSequenceImpl XML_URI = new CharSequenceImpl(
            "http://www.w3.org/XML/1998/namespace");

    private static final CharSequenceImpl XMLNS_PREFIX = new CharSequenceImpl(
            "xmlns");

    private static final CharSequenceImpl XMLNS_URI = new CharSequenceImpl(
            "http://www.w3.org/2000/xmlns/");

    /**
     * Returns the namespace for the specified prefix (different from 
     * <code>null</code>).
     * 
     * @param prefix the prefix to search for.
     * @return the associated namespace uri.
     */
    CharSequenceImpl getNamespaceUri(CharSequenceImpl prefix) {
        for (int i = _nspCounts[_depth] + _mapCount; i > 0;) {
            CharSequenceImpl pfx = _namespaces[--i << 1];
            if ((pfx != null) && pfx.equals(prefix))
                return _namespaces[(i << 1) + 1];
        }
        if (XML_PREFIX.equals(prefix))
            return XML_URI;
        if (XMLNS_PREFIX.equals(prefix))
            return XMLNS_URI;
        return null;
    }

    /**
     * Adds the specified mapping to the current mapping buffer.
     *
     * @param  prefix the prefix to be mapped or <code>null</code> to 
     *         map the defaut namespace.
     * @param  uri the associated uri.
     * @throws SAXException any SAX exception, possibly wrapping another
     *         exception.
     */
    public void map(CharSequenceImpl prefix, CharSequenceImpl uri) {
        final int i = (_nspCounts[_depth] + _mapCount++) << 1;
        if (i + 1 >= _namespaces.length) resize();
        _namespaces[i] = prefix;
        _namespaces[i + 1] = uri;
        if (prefix == null) { // Maps default namespace.
            _default = uri;
        }
    }

    /**
     * Flushes the current mapping buffer (equivalent to push() then pop()).
     */
    public void flush() {
        if (_mapCount != 0) {
            push();
            pop();
        }
    }

    /**
     * Pushes the current namespaces.
     */
    public void push() {
        if (++_depth >= _nspCounts.length) resize();
        _nspCounts[_depth] = _nspCounts[_depth - 1] + _mapCount;
        _mapCount = 0;
    }

    /**
     * Pops the current namespaces.
     */
    public void pop() {
        _mapCount = 0;
        final int oldCount = _nspCounts[_depth];
        final int newCount = _nspCounts[--_depth];
        for (int i = oldCount; i > newCount;) {
            if (_namespaces[--i << 1] == null) { // Unmaps default namespace.
                _default = CharSequenceImpl.EMPTY;
                for (int j = i; j > 0;) { // Searches current default.
                    if (_namespaces[--j << 1] == null) {
                        _default = _namespaces[(j << 1) + 1];
                        break;
                    }
                }
            }
        }
    }

    /**
     * Resets this {@link Namespaces} for reuse. 
     */
    public void reset() {
        _depth = 0;
        _nspCounts[0] = 0;
        _default = CharSequenceImpl.EMPTY;
    }

    /**
     * Resizes internal arrays.
     */
    private void resize() {
        final int size = _nspCounts.length; // = _namepaces.length;
        int[] tmp0 = new int[size * 2];
        System.arraycopy(_nspCounts, 0, tmp0, 0, size);
        _nspCounts = tmp0;
        CharSequenceImpl[] tmp1 = new CharSequenceImpl[size * 2];
        System.arraycopy(_namespaces, 0, tmp1, 0, size);
        _namespaces = tmp1;
        SIZE.setMinimum(new Integer(_namespaces.length));
    }
}