/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2005 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.xml.sax;
import j2me.lang.CharSequence;

/**
 * <p> This interface represents a list of XML attributes.</p>
 * 
 * <p> It is a more generic version of <code>org.xml.sax.Attributes</code> with
 *     <code>String</code> returned values replaced by the more versatile 
 *     <code>CharSequence</code>.</p>
 *     
 * <p> Note: To parse primitive types attributes (e.g. int, long, double), the
 *           use of the {@link javolution.lang.TypeFormat} class is
 *           recommended (faster and avoids memory allocation).</p>
 *
 * @author  <a href="mailto:sax@megginson.com">David Megginson</a>
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 3.5, September 2, 2005
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
    CharSequence getURI(int index);

    /**
     * Looks up an attribute's local name by index.
     *
     * @param  index the attribute index (zero-based).
     * @return the local name, or an empty character sequence if Namespace
     *         processing is  not being performed, or <code>null</code> if
     *         the index is out of range.
     * @see    #getLength
     */
    CharSequence getLocalName(int index);


    /**
     * Looks up an attribute's XML 1.0 qualified name by index.
     *
     * @param  index the attribute index (zero-based).
     * @return the XML 1.0 qualified name, or an empty character sequence if
     *         none is available, or <code>null</code> if the index is out
     *         of range.
     * @see    #getLength
     */
    CharSequence getQName(int index);

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
    String getType(int index);

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
    CharSequence getValue(int index);

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
    int getIndex(String uri, String localName);

    /**
     * Looks up the index of an attribute by XML 1.0 qualified name 
     * (convenience method). This method returns the index of the attribute 
     * whose name has the same character content as the specified qName.
     *
     * @param  qName the qualified (prefixed) name.
     * @return the index of the attribute, or <code>-1</code> if it does not
     *         appear in the list.
     */
    int getIndex(String qName);

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
    String getType(String uri, String localName);

    /**
     * Looks up an attribute's type by XML 1.0 qualified name.
     * This method returns the type of the attribute whose qName 
     * has the same character content as the specified qName.
     *
     * @param  qName The XML 1.0 qualified name.
     * @return the attribute type as a string, or null if the attribute is not
     *         in the list or if qualified names are not available.
     */
    String getType(String qName);

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
    CharSequence getValue(String uri, String localName);

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
    CharSequence getValue(String qName);
}