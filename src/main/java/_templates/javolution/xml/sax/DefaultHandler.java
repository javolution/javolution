/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2006 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package _templates.javolution.xml.sax;

import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import _templates.java.lang.UnsupportedOperationException;
import _templates.javolution.text.CharArray;

/**
 * Default base class for real-time handling of XML events.
 *
 * @author  <a href="mailto:sax@megginson.com">David Megginson</a>
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 3.1, March 11, 2005
 */
public class DefaultHandler implements ContentHandler, ErrorHandler {

    /**
     * Receives notification of a warning. The default behaviour is to take no
     * action.
     *
     * @param  e the warning information encapsulated in a SAX parse exception.
     * @throws org.xml.sax.SAXException any SAX exception.
     */
    public void warning (SAXParseException e) throws SAXException {}

    /**
     * Receives notification of recoverable parser error. The default behaviour
     * is to take no action.
     *
     * @param  e the error information encapsulated in a SAX parse exception.
     * @throws org.xml.sax.SAXException any SAX exception.
     */
    public void error (SAXParseException e) throws SAXException {}

    /**
     * Reports a fatal XML parsing error. The default behaviour is to throw
     * the specified exception.
     *
     * @param  e the error information encapsulated in a SAX parse exception.
     * @throws org.xml.sax.SAXException any SAX exception.
     */
    public void fatalError (SAXParseException e) throws SAXException {
	throw e;
    }

    // Implements ContentHandler
    public void setDocumentLocator(Locator locator) {}

    // Implements ContentHandler
    public void startDocument() throws SAXException {}

    // Implements ContentHandler
    public void endDocument() throws SAXException {}

    // Implements ContentHandler
    public void startPrefixMapping(CharArray prefix, CharArray uri)
        throws SAXException {}

    // Implements ContentHandler
    public void endPrefixMapping(CharArray prefix) throws SAXException {}

    // Implements ContentHandler
    public void startElement(CharArray namespaceURI, CharArray localName,
        CharArray qName, Attributes atts) throws SAXException {}

    // Implements ContentHandler
    public void endElement(CharArray namespaceURI, CharArray localName,
        CharArray qName) throws SAXException {}

    // Implements ContentHandler
    public void characters(char ch[], int start, int length)
        throws SAXException {}

    // Implements ContentHandler
    public void ignorableWhitespace(char ch[], int start, int length)
        throws SAXException {}

    // Implements ContentHandler
    public void processingInstruction(CharArray target, CharArray data)
        throws SAXException {}

    // Implements ContentHandler
    public void skippedEntity(CharArray name) throws SAXException {}

    /**
     * <b> Generates compile-time error if <code>startElement</code> is not
     *     correctly overriden.  This method generates a compile-error
     *     <code>"final method cannot be overridden"</code> if
     *     <code>org.xml.sax.Attributes</code> is used instead of
     *     <code>javolution.xml.sax.Attributes</code> (common mistake).</b>
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
    protected final void startElement(
        CharArray uri, CharArray localName,
        CharArray qName, org.xml.sax.Attributes atts) throws SAXException {
            throw new UnsupportedOperationException();
    }

}