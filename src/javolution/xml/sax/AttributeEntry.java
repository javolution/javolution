/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2005 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.xml.sax;

/**
 * This class represents a single attribute entry.
 *
 * @author  <a href="mailto:sax@megginson.com">David Megginson</a>
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 4.5, May 26, 2003
 */
final class AttributeEntry {

    /**
     * Holds the attribute's URI.
     */
    public CharSequenceImpl uri;

    /**
     * Holds the attribute's local name.
     */
    public CharSequenceImpl localName;

    /**
     * Holds the attribute's qualified name (qName = prefix + ":" + localName).
     */
    public CharSequenceImpl qName;

    /**
     * Holds the attribute's value.
     */
    public CharSequenceImpl value;

}
