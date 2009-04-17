/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2006 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package _templates.javolution.xml.sax;
import _templates.java.lang.CharSequence;
import _templates.javolution.text.CharArray;

/**
 * <p> This interface represents a list of XML attributes.</p>
 * 
 * <p> It is a more efficient version of <code>org.xml.sax.Attributes</code>
 *     with  {@link CharArray CharArray}/{@link CharSequence CharSequence} 
 *     instead of the <code>String</code> to avoid forcing dynamic object 
 *     allocations.</p>
 *
 * @author  <a href="mailto:sax@megginson.com">David Megginson</a>
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 4.0, June 16, 2006
 */
public interface Attributes {

    /**
     * Returns the number of attributes in this list of attributes.
     *
     * @return the number of attributes.
     */
    int getLength();

    /**
     * Looks up an attribute's Namespace URI by index.
     *
     * @param  index the attribute index (zero-based).
     * @return the Namespace URI, or an empty character sequence if none is
     *         available, or <code>null</code> if the index is out of range.
     * @see    #getLength
     */
    CharArray getURI(int index);

    /**
     * Looks up an attribute's local name by index.
     *
     * @param  index the attribute index (zero-based).
     * @return the local name, or an empty character sequence if Namespace
     *         processing is  not being performed, or <code>null</code> if
     *         the index is out of range.
     * @see    #getLength
     */
    CharArray getLocalName(int index);


    /**
     * Looks up an attribute's XML 1.0 qualified name by index.
     *
     * @param  index the attribute index (zero-based).
     * @return the XML 1.0 qualified name, or an empty character sequence if
     *         none is available, or <code>null</code> if the index is out
     *         of range.
     * @see    #getLength
     */
    CharArray getQName(int index);

    /**
     * Looks up an attribute's type by index.
     *
     * <p> The attribute type is one of the strings "CDATA", "ID",
     *    "IDREF", "IDREFS", "NMTOKEN", "NMTOKENS", "ENTITY", "ENTITIES",
     *    or "NOTATION" (always in upper case).</p>
     *
     * <p> If the parser has not read a declaration for the attribute,
     *     or if the parser does not report attribute types, then it must
     *     return the value "CDATA" as stated in the XML 1.0 Recommentation
     *     (clause 3.3.3, "Attribute-TextBuilder Normalization").</p>
     *
     * <p> For an enumerated attribute that is not a notation, the
     *     parser will report the type as "NMTOKEN".</p>
     *
     * @param  index the attribute index (zero-based).
     * @return the attribute's type as a string, or null if the
     *         index is out of range.
     * @see    #getLength
     */
    CharArray getType(int index);

    /**
     * Looks up an attribute's value by index.
     *
     * <p> If the attribute value is a list of tokens (IDREFS,
     *     ENTITIES, or NMTOKENS), the tokens will be concatenated
     *     into a single string with each token separated by a
     *     single space.</p>
     *
     * @param  index the attribute index (zero-based).
     * @return the attribute's value as a character sequence,
     *         <code>null</code> if the index is out of range.
     * @see    #getLength
     */
    CharArray getValue(int index);

    /**
     * Looks up the index of an attribute by namespace name (convenience 
     * method).
     * This method returns the index of the attribute whose uri/localName 
     * have the same character content as the specified uri/localName.
     *
     * @param  uri the Namespace URI, or an empty character sequence if
     *         the name has no Namespace URI.
     * @param  localName the attribute's local name.
     * @return the index of the attribute, or <code>-1</code> if it does not
     *         appear in the list.
     */
    int getIndex(CharSequence uri, CharSequence localName);

    /**
     * Looks up the index of an attribute by XML 1.0 qualified name 
     * (convenience method). This method returns the index of the attribute 
     * whose name has the same character content as the specified qName.
     *
     * @param  qName the qualified (prefixed) name.
     * @return the index of the attribute, or <code>-1</code> if it does not
     *         appear in the list.
     */
    int getIndex(CharSequence qName);

    /**
     * Looks up an attribute's type by Namespace name (convenience method).
     * This method returns the type of the attribute whose uri/localName 
     * have the same character content as the specified uri/localName.
     *
     * @param  uri the Namespace URI, or an empty string if the
     *         name has no Namespace URI.
     * @param  localName the local name of the attribute.
     * @return the attribute type as a string, or null if the attribute is not
     *         in the list or if Namespace processing is not being performed.
     */
    CharArray getType(CharSequence uri, CharSequence localName);

    /**
     * Looks up an attribute's type by XML 1.0 qualified name.
     * This method returns the type of the attribute whose qName 
     * has the same character content as the specified qName.
     *
     * @param  qName The XML 1.0 qualified name.
     * @return the attribute type as a string, or null if the attribute is not
     *         in the list or if qualified names are not available.
     */
    CharArray getType(CharSequence qName);

    /**
     * Looks up an attribute's value by Namespace name (convenience method).
     * This method returns the value of the attribute whose uri/localName 
     * have the same character content as the specified uri/localName.
     *
     * @param  uri the Namespace URI, or the empty string if the name has no 
     *         Namespace URI.
     * @param  localName the local name of the attribute.
     * @return the attribute value as a character sequence, or <code>null</code>
     *         if the attribute is not in the list.
     */
    CharArray getValue(CharSequence uri, CharSequence localName);

    /**
     * Looks up an attribute's value by XML 1.0 qualified name (convenience 
     * method). This method returns the value of the attribute whose qName 
     * has the same character content as the specified qName.
     *
     * @param  qName The XML 1.0 qualified name.
     * @return the attribute value as a character sequence, or <code>null</code>
     *         if the attribute is not in the list or if qualified names
     *         are not available.
     */
    CharArray getValue(CharSequence qName);
}