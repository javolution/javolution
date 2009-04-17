/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2006 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package _templates.javolution.xml.stream;

import _templates.java.lang.CharSequence;
import _templates.java.util.Iterator;
import _templates.javax.realtime.MemoryArea;
import _templates.javolution.lang.Reusable;
import _templates.javolution.text.CharArray;
import _templates.javolution.util.FastList;

/**
 * This class represents the namespaces stack while parsing.
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 3.2, April 2, 2005
 */
final class NamespacesImpl implements Reusable, NamespaceContext {

    /** 
     * Holds the number of predefined namespaces.
     */
    static final int NBR_PREDEFINED_NAMESPACES = 3;

    /** 
     * Holds useful CharArray instances (non-static to avoid potential 
     * inter-thread corruption).
     */
    final CharArray _nullNsURI = new CharArray(""); // No namespace URI.

    final CharArray _defaultNsPrefix = new CharArray("");

    final CharArray _xml = new CharArray("xml");

    final CharArray _xmlURI = new CharArray(
            "http://www.w3.org/XML/1998/namespace");

    final CharArray _xmlns = new CharArray("xmlns");

    final CharArray _xmlnsURI = new CharArray("http://www.w3.org/2000/xmlns/");

    /** 
     * Holds the current nesting level.
     */
    private int _nesting = 0;

    /** 
     * Holds the currently mapped prefixes.
     */
    CharArray[] _prefixes = new CharArray[16];

    /** 
     * Holds the currently mapped namespaces.
     */
    CharArray[] _namespaces = new CharArray[_prefixes.length];

    /** 
     * Indicates if the prefix has to been written (when writing).
     */
    boolean[] _prefixesWritten = new boolean[_prefixes.length];

    /** 
     * Holds the number of prefix/namespace association per nesting level.
     */
    int[] _namespacesCount = new int[16];

    /** 
     * Holds the default namespace.
     */
    CharArray _defaultNamespace = _nullNsURI;

    /** 
     * Holds the default namespace index.
     */
    int _defaultNamespaceIndex;

    /**
     * Default constructor.
     */
    public NamespacesImpl() {
        _prefixes[0] = _defaultNsPrefix;
        _namespaces[0] = _nullNsURI;
        _prefixes[1] = _xml;
        _namespaces[1] = _xmlURI;
        _prefixes[2] = _xmlns;
        _namespaces[2] = _xmlnsURI;
        _namespacesCount[0] = NBR_PREDEFINED_NAMESPACES;
    }

    // Implements NamespaceContext
    public CharArray getNamespaceURI(CharSequence prefix) {
        if (prefix == null) 
            throw new IllegalArgumentException("null prefix not allowed");
        return getNamespaceURINullAllowed(prefix);
    }
    CharArray getNamespaceURINullAllowed(CharSequence prefix) {
        if ((prefix == null) || (prefix.length() == 0))
            return _defaultNamespace;
        final int count = _namespacesCount[_nesting];
        for (int i = count; --i >= 0;) {
            if (_prefixes[i].equals(prefix))
                return _namespaces[i];
        }
        return null; // Not bound.
    }

    // Implements NamespaceContext
    public CharArray getPrefix(CharSequence uri) {
        if (uri == null) 
            throw new IllegalArgumentException("null namespace URI not allowed");
        return _defaultNamespace.equals(uri) ? _defaultNsPrefix : getPrefix(
                uri, _namespacesCount[_nesting]);
    }

    CharArray getPrefix(CharSequence uri, int count) {
        for (int i = count; --i >= 0;) {
            CharArray prefix = _prefixes[i];
            CharArray namespace = _namespaces[i];
            if (namespace.equals(uri)) { // Find matching uri.
                // Checks that the prefix has not been overwriten after being set.
                boolean isPrefixOverwritten = false;
                for (int j = i + 1; j < count; j++) {
                    if (prefix.equals(_prefixes[j])) {
                        isPrefixOverwritten = true;
                        break;
                    }
                }
                if (!isPrefixOverwritten)
                    return prefix;
            }
        }
        return null; // Not bound.
    }

    // Implements NamespaceContext
    public Iterator getPrefixes(CharSequence namespaceURI) {
        FastList prefixes = new FastList();
        for (int i = _namespacesCount[_nesting]; --i >= 0;) {
            if (_namespaces[i].equals(namespaceURI)) {
                prefixes.add(_prefixes[i]);
            }
        }
        return prefixes.iterator();
    }

    // Null values are not allowed.
    void setPrefix(CharArray prefix, CharArray uri) {
        int index = _namespacesCount[_nesting];
        _prefixes[index] = prefix;
        _namespaces[index] = uri;
        if (prefix.length() == 0) { // The default namespace is set.
            _defaultNamespaceIndex = index;
            _defaultNamespace = uri;
        }
        if (++_namespacesCount[_nesting] >= _prefixes.length)
            resizePrefixStack();
    }

    // Used only by XMLStreamWriter (converts CharSequence to CharArray).
    // Null values are not allowed.
    void setPrefix(final CharSequence prefix, CharSequence uri,
            boolean isWritten) {
        final int index = _namespacesCount[_nesting];
        _prefixesWritten[index] = isWritten;
        final int prefixLength = prefix.length();
        CharArray prefixTmp = _prefixesTmp[index]; 
        if ((prefixTmp == null)
                || (prefixTmp.array().length < prefixLength)) {
            MemoryArea.getMemoryArea(this).executeInArea(new Runnable() {
                public void run() {
                    _prefixesTmp[index] = new CharArray().setArray(new char[prefixLength + 32], 0, 0);
                }
            });
            prefixTmp = _prefixesTmp[index];
        }
        for (int i = 0; i < prefixLength; i++) {
            prefixTmp.array()[i] = prefix.charAt(i);
        }
        prefixTmp.setArray(prefixTmp.array(), 0, prefixLength);

        final int uriLength = uri.length();
        CharArray namespaceTmp = _namespacesTmp[index]; 
        if ((namespaceTmp == null)
                || (namespaceTmp.array().length < uriLength)) {
            MemoryArea.getMemoryArea(this).executeInArea(new Runnable() {
                public void run() {
                    _namespacesTmp[index] = new CharArray().setArray(new char[uriLength + 32], 0, 0);
                }
            });
            namespaceTmp = _namespacesTmp[index];
        }
        for (int i = 0; i < uriLength; i++) {
            namespaceTmp.array()[i] = uri.charAt(i);
        }
        namespaceTmp.setArray(namespaceTmp.array(), 0, uriLength);
        
        // Sets the prefix using CharArray instances.
        setPrefix(prefixTmp, namespaceTmp);
    }

    private CharArray[] _prefixesTmp = new CharArray[_prefixes.length];

    private CharArray[] _namespacesTmp = new CharArray[_prefixes.length];

    void pop() {
        if (_namespacesCount[--_nesting] <= _defaultNamespaceIndex) {
            searchDefaultNamespace();
        }
    }

    private void searchDefaultNamespace() {
        int count = _namespacesCount[_nesting];
        for (int i = count; --i >= 0;) {
            if (_prefixes[i].length() == 0) {
                _defaultNamespaceIndex = i;
                return;
            }
        }
        throw new Error("Cannot find default namespace");
    }

    void push() {
        _nesting++;
        if (_nesting >= _namespacesCount.length) {
            resizeNamespacesCount();
        }
        _namespacesCount[_nesting] = _namespacesCount[_nesting - 1];
    }

    public void reset() {
        _defaultNamespace = _nullNsURI;
        _defaultNamespaceIndex = 0;
        _namespacesCount[0] = NBR_PREDEFINED_NAMESPACES;
        _nesting = 0;
    }

    private void resizeNamespacesCount() {
        MemoryArea.getMemoryArea(this).executeInArea(new Runnable() {
            public void run() {
                final int oldLength = _namespacesCount.length;
                final int newLength = oldLength * 2;

                // Resizes namespaces counts.
                int[] tmp = new int[newLength];
                System.arraycopy(_namespacesCount, 0, tmp, 0, oldLength);
                _namespacesCount = tmp;
            }
        });
    }

    // Resizes prefix mapping  stack.
    private void resizePrefixStack() {
        MemoryArea.getMemoryArea(this).executeInArea(new Runnable() {
            public void run() {
                final int oldLength = _prefixes.length;
                final int newLength = oldLength * 2;

                // Resizes prefixes.
                CharArray[] tmp0 = new CharArray[newLength];
                System.arraycopy(_prefixes, 0, tmp0, 0, oldLength);
                _prefixes = tmp0;

                // Resizes namespaces uri.
                CharArray[] tmp1 = new CharArray[newLength];
                System.arraycopy(_namespaces, 0, tmp1, 0, oldLength);
                _namespaces = tmp1;

                // Resizes prefix sets.
                boolean[] tmp2 = new boolean[newLength];
                System.arraycopy(_prefixesWritten, 0, tmp2, 0, oldLength);
                _prefixesWritten = tmp2;

                // Resizes temporary prefix (CharSequence to CharArray conversion).
                CharArray[] tmp3 = new CharArray[newLength];
                System.arraycopy(_prefixesTmp, 0, tmp3, 0, oldLength);
                _prefixesTmp = tmp3;

                // Resizes temporary namespaces (CharSequence to CharArray conversion).
                CharArray[] tmp4 = new CharArray[newLength];
                System.arraycopy(_namespacesTmp, 0, tmp4, 0, oldLength);
                _namespacesTmp = tmp4;

            }
        });
    }


}