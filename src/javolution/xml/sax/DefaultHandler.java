/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2004 - The Javolution Team (http://javolution.org/)
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.xml.sax;
import j2me.lang.CharSequence;
import j2me.lang.UnsupportedOperationException;
import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Default base class for real-time handling of XML events.
 *
 * @author  <a href="mailto:sax@megginson.com">David Megginson</a>
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 4.5, May 26, 2003
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
    public void startPrefixMapping(CharSequence prefix, CharSequence uri)
        throws SAXException {}

    // Implements ContentHandler
    public void endPrefixMapping(CharSequence prefix) throws SAXException {}

    // Implements ContentHandler
    public void startElement(CharSequence namespaceURI, CharSequence localName,
        CharSequence qName, Attributes atts) throws SAXException {}

    // Implements ContentHandler
    public void endElement(CharSequence namespaceURI, CharSequence localName,
        CharSequence qName) throws SAXException {}

    // Implements ContentHandler
    public void characters(char ch[], int start, int length)
        throws SAXException {}

    // Implements ContentHandler
    public void ignorableWhitespace(char ch[], int start, int length)
        throws SAXException {}

    // Implements ContentHandler
    public void processingInstruction(CharSequence target, CharSequence data)
        throws SAXException {}

    // Implements ContentHandler
    public void skippedEntity(CharSequence name) throws SAXException {}

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
        CharSequence uri, CharSequence localName,
        CharSequence qName, org.xml.sax.Attributes atts) throws SAXException {
            throw new UnsupportedOperationException();
    }

}