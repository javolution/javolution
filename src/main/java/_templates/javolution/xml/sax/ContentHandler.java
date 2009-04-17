/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2006 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package _templates.javolution.xml.sax;


import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import _templates.java.lang.CharSequence;
import _templates.javolution.text.CharArray;

/**
 * <p> Receives notification of the logical content of a document.</p>
 * 
 * <p> It is a more efficient version of <code>org.xml.sax.ContentHandler</code>
 *     with  {@link CharArray CharArray}/{@link CharSequence CharSequence} 
 *     instead of the <code>String</code> to avoid forcing dynamic object 
 *     allocations.</p>
 *
 * @author  <a href="mailto:sax@megginson.com">David Megginson</a>
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 4.0, June 16, 2006
 */
public interface ContentHandler {

    /**
     * Receives an object for locating the origin of SAX document events.
     *
     * @param  locator the document locator.
     */
    void setDocumentLocator(Locator locator);

    /**
     * Receives notification of the beginning of a document.
     *
     * @throws org.xml.sax.SAXException any SAX exception.
     */
    void startDocument() throws SAXException;

    /**
     * Receives notification of the end of a document.
     *
     * @throws org.xml.sax.SAXException any SAX exception.
     */
    void endDocument() throws SAXException;

    /**
     * Begins the scope of a prefix-URI Namespace mapping.
     *
     * @param  prefix the Namespace prefix being declared.
     * @param  uri the namespace URI the prefix is mapped to.
     * @throws org.xml.sax.SAXException any SAX exception.
     */
    void startPrefixMapping(CharArray prefix, CharArray uri)
        throws SAXException;

    /**
     * Ends the scope of a prefix-URI mapping.
     *
     * @param  prefix the prefix that was being mapping.
     * @throws org.xml.sax.SAXException any SAX exception.
     */
    void endPrefixMapping(CharArray prefix) throws SAXException;

    /**
     * Receives notification of the beginning of an element.
     *
     * @param  uri the namespace URI, or an empty character sequence if the
     *         element has no Namespace URI or if namespace processing is not
     *         being performed.
     * @param  localName the local name (without prefix), or an empty character
     *         sequence if namespace processing is not being performed.
     * @param  qName the qualified name (with prefix), or an empty character
     *         sequence if qualified names are not available.
     * @param  atts the attributes attached to the element.  If there are no
     *         attributes, it shall be an empty {@link Attributes} object.
     * @throws org.xml.sax.SAXException any SAX exception.
     */
    void startElement(CharArray uri, CharArray localName,
                      CharArray qName, Attributes atts) throws SAXException;

    /**
     * Receives notification of the end of an element.
     *
     * @param  uri the namespace URI, or an empty character sequence if the
     *         element has no Namespace URI or if namespace processing is not
     *         being performed.
     * @param  localName the local name (without prefix), or an empty character
     *         sequence if namespace processing is not being performed.
     * @param  qName the qualified XML 1.0 name (with prefix), or an empty
     *         character sequence if qualified names are not available.
     * @throws org.xml.sax.SAXException any SAX exception.
     */
    void endElement (CharArray uri, CharArray localName,
        CharArray qName) throws SAXException;

    /**
     * Receives notification of character data.
     *
     * @param  ch the characters from the XML document.
     * @param  start the start position in the array.
     * @param  length the number of characters to read from the array.
     * @throws org.xml.sax.SAXException any SAX exception.
     */
    void characters(char ch[], int start, int length) throws SAXException;

    /**
     * Receives notification of ignorable whitespace in element content.
     *
     * @param  ch the characters from the XML document.
     * @param  start the start position in the array.
     * @param  length the number of characters to read from the array.
     * @throws org.xml.sax.SAXException any SAX exception.
     */
    void ignorableWhitespace(char ch[], int start, int length)
        throws SAXException;

    /**
     * Receives notification of a processing instruction.
     *
     * @param  target the processing instruction target.
     * @param  data the processing instruction data, or null if
     *         none was supplied.  The data does not include any
     *         whitespace separating it from the target.
     * @throws org.xml.sax.SAXException any SAX exception.
     */
    void processingInstruction(CharArray target, CharArray data)
        throws SAXException;

    /**
     * Receives notification of a skipped entity.
     *
     * @param name the name of the skipped entity.  If it is a
     *        parameter entity, the name will begin with '%', and if
     *        it is the external DTD subset, it will be the character sequence
     *        "[dtd]".
     * @throws org.xml.sax.SAXException any SAX exception.
     */
    void skippedEntity(CharArray name) throws SAXException;

}