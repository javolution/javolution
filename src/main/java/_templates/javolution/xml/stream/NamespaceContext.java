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
import _templates.javolution.text.CharArray;

/**
 * This interface represents the XML namespace context stack while parsing.
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 4.0, September 12, 2006
 */
public interface NamespaceContext {

    /**
     * Returns the namespace URI bound to a prefix in the current scope
     * or <code>null</code> if the prefix is unbound.
     *
     * @param prefix prefix to look up
     * @return the namespace URI.
     * @throws IllegalArgumentException if <code>prefix</code> is
     *         <code>null</code>
     */
    CharArray getNamespaceURI(CharSequence prefix);

    /**
     * Returns the prefix bound to the namespace URI in the current scope
     * or <code>null</code> if the namespace URI is unbound.
     *
     * @param namespaceURI URI of the namespace to lookup.
     * @return the prefix bound to the namespace URI.
     * @throws IllegalArgumentException if <code>namespaceURI</code> is
     *         <code>null</code>
     */
    CharArray getPrefix(CharSequence namespaceURI);

    /**
     * Returns all prefixes bound to a namespace URI in the current scope
     * (including predefined prefixes).
     *
     * @param namespaceURI URI of Namespace to lookup
     * @return an <code>Iterator</code> over {@link CharArray} prefixes.
     * @throws IllegalArgumentException if <code>namespaceURI</code> is
     *         <code>null</code>
     */
    Iterator getPrefixes(CharSequence namespaceURI);
    
}