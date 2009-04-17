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
import _templates.javolution.text.CharArray;

/**
 * <p> This interface is similar to 
 *     <code>javax.xml.stream.XMLStreamReader</code>; but it does not forces
 *     dynamic allocation when parsing  (its methods returns 
 *     {@link CharArray CharArray} instances instead  of {@link String}).</p>
 *     
 * <p> Except for the speed (faster) and its real-time characteristics  
 *     the usage/behavior is about the same as its StAX counterpart.</p>
 *     
 * <p> The {@link CharArray CharArray} instances returned by this reader 
 *     supports fast primitive conversions as illustrated below:[code]
 *     
 *     // Creates a new reader (potentially recycled).
 *     XMLInputFactory factory = XMLInputFactory.newInstance();
 *     XMLStreamReader reader = factory.createXMLStreamReader(inputStream);
 *     
 *     while (reader.getEventType() != XMLStreamConstants.END_DOCUMENT) {
 *         switch (reader.next()) {
 *             case XMLStreamConstants.START_ELEMENT:
 *             if (reader.getLocalName().equals("Time")) {
 *                  // Reads primitive types (int) attributes directly (no memory allocation).
 *                  time.hour = reader.getAttributeValue(null, "hour").toInt();
 *                  time.minute = reader.getAttributeValue(null, "minute").toInt();
 *                  time.second = reader.getAttributeValue(null, "second").toInt();
 *             }
 *             ...
 *             break;
 *         }         
 *     }
 *     
 *     reader.close(); // Recycles the reader.
 *     inputStream.close(); // Underlying stream has to be closed explicitly.
 *     [/code] 
 *     
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 4.0, September 4, 2006
 */
public interface XMLStreamReader extends XMLStreamConstants {

    /**
     * Gets the value of a feature/property from the underlying implementation
     * 
     * @param name the name of the property.
     * @return the value of the property.
     */
    public Object getProperty(String name) throws IllegalArgumentException;

    /**
     * Gets next parsing event - contiguous character data is returned into a
     * single chunk.
     * 
     * By default entity references must be expanded and reported transparently
     * to the application. An exception will be thrown if an entity reference
     * cannot be expanded. If element content is empty (i.e. content is "") then
     * no CHARACTERS event will be reported.
     * 
     * <p>
     * Given the following XML:<br>
     * &lt;foo>&lt;!--description-->content
     * text&lt;![CDATA[&lt;greeting>Hello&lt;/greeting>]]>other content&lt;/foo><br>
     * The behavior of calling next() when being on foo will be:<br>
     * 1- the comment (COMMENT)<br>
     * 2- then the characters section (CHARACTERS)<br>
     * 3- then the CDATA section (another CHARACTERS)<br>
     * 4- then the next characters section (another CHARACTERS)<br>
     * 5- then the END_ELEMENT<br>
     * 
     * <p>
     * <b>NOTE:</b> empty element (such as &lt;tag/>) will be reported with two
     * separate events: START_ELEMENT, END_ELEMENT - This preserves parsing
     * equivalency of empty element to &lt;tag>&lt;/tag>.
     * 
     * This method will throw an IllegalStateException if it is called after
     * hasNext() returns false.
     * 
     * @return the integer code corresponding to the current parse event
     * @throws NoSuchElementException if this is called when hasNext() 
     *         returns false
     * @throws XMLStreamException if there is an error processing the 
     *         underlying XML source
     */
    public int next() throws XMLStreamException;

    /**
     * Tests if the current event is of the given type and if the namespace and
     * name match the current namespace and name of the current event. If the
     * namespaceURI is null it is not checked for equality, if the localName is
     * null it is not checked for equality.
     * 
     * @param type the event type.
     * @param namespaceURI the uri of the event, may be null.
     * @param localName the localName of the event, may be null.
     * @throws XMLStreamException if the required values are not matched.
     */
    public void require(int type, CharSequence namespaceURI,
            CharSequence localName) throws XMLStreamException;

    /**
     * Reads the content of a text-only element, an exception is thrown if this
     * is not a text-only element. Regardless of the value of
     * javax.xml.stream.isCoalescing this method always returns coalesced
     * content. <br />
     * Precondition: the current event is START_ELEMENT. <br />
     * Postcondition: the current event is the corresponding END_ELEMENT.
     * 
     * <br />
     * The method does the following (implementations are free to optimized but
     * must do equivalent processing):
     * 
     * <pre>
     * if (getEventType() != XMLStreamConstants.START_ELEMENT) {
     * 	throw new XMLStreamException(
     * 			&quot;parser must be on START_ELEMENT to read next text&quot;, getLocation());
     * }
     * int eventType = next();
     * StringBuffer content = new StringBuffer();
     * while (eventType != XMLStreamConstants.END_ELEMENT) {
     * 	if (eventType == XMLStreamConstants.CHARACTERS
     * 			|| eventType == XMLStreamConstants.CDATA
     * 			|| eventType == XMLStreamConstants.SPACE
     * 			|| eventType == XMLStreamConstants.ENTITY_REFERENCE) {
     * 		buf.append(getText());
     * 	} else if (eventType == XMLStreamConstants.PROCESSING_INSTRUCTION
     * 			|| eventType == XMLStreamConstants.COMMENT) {
     * 		// skipping
     * 	} else if (eventType == XMLStreamConstants.END_DOCUMENT) {
     * 		throw new XMLStreamException(
     * 				&quot;unexpected end of document when reading element text content&quot;,
     * 				this);
     * 	} else if (eventType == XMLStreamConstants.START_ELEMENT) {
     * 		throw new XMLStreamException(
     * 				&quot;element text content may not contain START_ELEMENT&quot;,
     * 				getLocation());
     * 	} else {
     * 		throw new XMLStreamException(&quot;Unexpected event type &quot; + eventType,
     * 				getLocation());
     * 	}
     * 	eventType = next();
     * }
     * return buf.toString();
     * </pre>
     * 
     * @throws XMLStreamException if the current event is not a START_ELEMENT 
     *         or if a non text element is encountered.
     */
    public CharArray getElementText() throws XMLStreamException;

    /**
     * Skips any white space (isWhiteSpace() returns true), COMMENT, or
     * PROCESSING_INSTRUCTION, until a START_ELEMENT or END_ELEMENT is reached.
     * If other than white space characters, COMMENT, PROCESSING_INSTRUCTION,
     * START_ELEMENT, END_ELEMENT are encountered, an exception is thrown. This
     * method should be used when processing element-only content seperated by
     * white space.
     * 
     * <br />
     * Precondition: none <br />
     * Postcondition: the current event is START_ELEMENT or END_ELEMENT and
     * cursor may have moved over any whitespace event.
     * 
     * <br />
     * Essentially it does the following (implementations are free to optimized
     * but must do equivalent processing):
     * 
     * <pre>
     *   int eventType = next();
     *   while((eventType == XMLStreamConstants.CHARACTERS &amp;&amp; isWhiteSpace()) // skip whitespace
     *   || (eventType == XMLStreamConstants.CDATA &amp;&amp; isWhiteSpace()) 
     *   // skip whitespace
     *   || eventType == XMLStreamConstants.SPACE
     *   || eventType == XMLStreamConstants.PROCESSING_INSTRUCTION
     *   || eventType == XMLStreamConstants.COMMENT
     *   ) {
     *   eventType = next();
     *   }
     *   if (eventType != XMLStreamConstants.START_ELEMENT &amp;&amp; eventType != XMLStreamConstants.END_ELEMENT) {
     *   throw new String XMLStreamException(&quot;expected start or end tag&quot;, getLocation());
     *   }
     *   return eventType;
     * </pre>
     * 
     * @return the event type of the element read (START_ELEMENT or END_ELEMENT)
     * @throws XMLStreamException if the current event is not white space,
     *             PROCESSING_INSTRUCTION, START_ELEMENT or END_ELEMENT
     * @throws NoSuchElementException if this is called when hasNext() 
     *         returns false
     */
    public int nextTag() throws XMLStreamException;

    /**
     * Returns true if there are more parsing events and false if there are no
     * more events. This method will return false if the current state of the
     * XMLStreamReader is END_DOCUMENT.
     * 
     * @return true if there are more events, false otherwise.
     * @throws XMLStreamException if there is a fatal error detecting the next
     *         state.
     */
    public boolean hasNext() throws XMLStreamException;

    /**
     * Frees any resources associated with this Reader. This method does not
     * close the underlying input source.
     * 
     * @throws XMLStreamException if there are errors freeing associated
     *         resources
     */
    public void close() throws XMLStreamException;

    /**
     * Returns the uri for the given prefix. The uri returned depends on the
     * current state of the processor.
     * 
     * <p>
     * <strong>NOTE:</strong>The 'xml' prefix is bound as defined in <a
     * href="http://www.w3.org/TR/REC-xml-names/#ns-using">Namespaces in XML</a>
     * specification to "http://www.w3.org/XML/1998/namespace".
     * 
     * <p>
     * <strong>NOTE:</strong> The 'xmlns' prefix must be resolved to following
     * namespace <a
     * href="http://www.w3.org/2000/xmlns/">http://www.w3.org/2000/xmlns/</a>
     * 
     * @param prefix the prefix to lookup.
     * @return the uri bound to the given prefix or <code>null</code> if it is
     *         not bound
     */
    public CharArray getNamespaceURI(CharSequence prefix);

    /**
     * Indicates if the cursor points to a start tag.
     * 
     * @return <code>true</code> if the cursor points to a start tag;
     *         <code>false</code> otherwise.
     */
    public boolean isStartElement();

    /**
     * Indicates if the cursor points to an end tag.
     * 
     * @return <code>true</code> if the cursor points to a end tag;
     *         <code>false</code> otherwise.
     */
    public boolean isEndElement();

    /**
     * Indicates if the cursor points to character data.
     * 
     * @return <code>true</code> if the cursor points to character data;
     *         <code>false</code> otherwise.
     */
    public boolean isCharacters();

    /**
     * Indicates if the cursor points to character data that consists
     * of all whitespace.
     * 
     * @return <code>true</code> if the cursor points to whitespaces;
     *         <code>false</code> otherwise.
     */
    public boolean isWhiteSpace();

    /**
     * Returns the normalized attribute value of the attribute with the
     * namespace and localName. 
     * 
     * @param namespaceURI the namespace of the attribute or <code>null</code>.
     * @param localName the local name of the attribute.
     * @return returns the value of the attribute or <code>null</code>.
     * @throws IllegalStateException if not a START_ELEMENT or ATTRIBUTE.
     */
    public CharArray getAttributeValue(CharSequence namespaceURI,
            CharSequence localName);

    /**
     * Returns the count of attributes on this START_ELEMENT, this method is
     * only valid on a START_ELEMENT or ATTRIBUTE. This count excludes namespace
     * definitions. Attribute indices are zero-based.
     * 
     * @return returns the number of attributes.
     * @throws IllegalStateException if not a START_ELEMENT or ATTRIBUTE.
     */
    public int getAttributeCount();

    /**
     * Returns the namespace of the attribute at the provided index
     * 
     * @param index the position of the attribute.
     * @return the namespace URI or <code>null</code> if no prefix. 
     * @throws IllegalStateException if not a START_ELEMENT or ATTRIBUTE.
     */
    public CharArray getAttributeNamespace(int index);

    /**
     * Returns the localName of the attribute at the provided index.
     * 
     * @param index the position of the attribute.
     * @return the localName of the attribute.
     * @throws IllegalStateException if not a START_ELEMENT or ATTRIBUTE.
     */
    public CharArray getAttributeLocalName(int index);

    /**
     * Returns the prefix of this attribute at the provided index
     * 
     * @param index the position of the attribute.
     * @return the prefix of the attribute or <code>null</code> if no prefix.
     * @throws IllegalStateException if not a START_ELEMENT or ATTRIBUTE.
     */
    public CharArray getAttributePrefix(int index);

    /**
     * Returns the XML type of the attribute at the provided index.
     * 
     * @param index the position of the attribute
     * @return the XML type of the attribute.
     * @throws IllegalStateException if not a START_ELEMENT or ATTRIBUTE.
     */
    public CharArray getAttributeType(int index);

    /**
     * Returns the value of the attribute at the index.
     * 
     * @param index the position of the attribute.
     * @return the attribute value.
     * @throws IllegalStateException if not a START_ELEMENT or ATTRIBUTE.
     */
    public CharArray getAttributeValue(int index);

    /**
     * Indicates if this attribute was created by default.
     * 
     * @param index the position of the attribute.
     * @return <code>true</code> if this is a default attribute;
     *         <code>false</code> otherwise.
     * @throws IllegalStateException if not a START_ELEMENT or ATTRIBUTE.
     */
    public boolean isAttributeSpecified(int index);

    /**
     * Returns the count of namespaces declared on this START_ELEMENT or
     * END_ELEMENT. This method is only valid on a START_ELEMENT, END_ELEMENT or
     * NAMESPACE. On an END_ELEMENT the count is of the namespaces that are
     * about to go out of scope. This is the equivalent of the information
     * reported by SAX callback for an end element event.
     * 
     * @return returns the number of namespace declarations on this specific
     *         element.
     * @throws IllegalStateException if not a START_ELEMENT or END_ELEMENT.
     */
    public int getNamespaceCount();

    /**
     * Returns the prefix for the namespace declared at the index. 
     * 
     * @param index the position of the namespace declaration.
     * @return returns the namespace prefix or <code>null</code> if no prefix.
     * @throws IllegalStateException if this is not a START_ELEMENT, 
     *         END_ELEMENT or NAMESPACE.
     */
    public CharArray getNamespacePrefix(int index);

    /**
     * Returns the URI for the namespace declared at the index.
     * 
     * @param index the position of the namespace declaration.
     * @return returns the namespace uri or <code>null</code> if no prefix.
     * @throws IllegalStateException if this is not a START_ELEMENT, 
     *         END_ELEMENT or NAMESPACE.
     */
    public CharArray getNamespaceURI(int index);

    /**
     * Returns a read only namespace context for the current position.
     * 
     * @return return a namespace context
     */
    public NamespaceContext getNamespaceContext();

    /**
     * Returns an integer code that indicates the type of the event the cursor
     * is pointing to.
     * 
     * @return the event type.
     */
    public int getEventType();

    /**
     * Returns the current value of the parse event as a string, this returns
     * the string value of a CHARACTERS event, returns the value of a COMMENT,
     * the replacement value for an ENTITY_REFERENCE, the string value of a
     * CDATA section, the string value for a SPACE event, or the String value of
     * the internal subset of the DTD. If an ENTITY_REFERENCE has been resolved,
     * any character data will be reported as CHARACTERS events.
     * 
     * @return the current text or <code>null</code>
     * @throws IllegalStateException if this state is not a valid text state.
     */
    public CharArray getText();

    /**
     * Returns an array which contains the characters from this event. This
     * array should be treated as read-only and transient. I.e. the array will
     * contain the text characters until the XMLStreamReader moves on to the
     * next event. Attempts to hold onto the character array beyond that time or
     * modify the contents of the array are breaches of the contract for this
     * interface.
     * 
     * @return the current text or an empty array.
     * @throws IllegalStateException if this state is not a valid text state.
     */
    public char[] getTextCharacters();

    /**
     * Gets the the text associated with a CHARACTERS, SPACE or CDATA event.
     * Text starting a "sourceStart" is copied into "target" starting at
     * "targetStart". Up to "length" characters are copied. The number of
     * characters actually copied is returned.
     * 
     * The "sourceStart" argument must be greater or equal to 0 and less than or
     * equal to the number of characters associated with the event. Usually, one
     * requests text starting at a "sourceStart" of 0. If the number of
     * characters actually copied is less than the "length", then there is no
     * more text. Otherwise, subsequent calls need to be made until all text has
     * been retrieved. For example:
     * 
     * <code>
     * int length = 1024;
     * char[] myBuffer = new char[ length ];
     * 
     * for ( int sourceStart = 0 ; ; sourceStart += length )
     * {
     *    int nCopied = stream.getTextCharacters( sourceStart, myBuffer, 0, length );
     *
     *   if (nCopied < length)
     *       break;
     * }
     * </code> XMLStreamException may be thrown
     * if there are any XML errors in the underlying source. The "targetStart"
     * argument must be greater than or equal to 0 and less than the length of
     * "target", Length must be greater than 0 and "targetStart + length" must
     * be less than or equal to length of "target".
     * 
     * @param sourceStart the index of te first character in the source array 
     *        to copy
     * @param target the destination array
     * @param targetStart the start offset in the target array
     * @param length the number of characters to copy
     * @return the number of characters actually copied
     * @throws XMLStreamException if the XML source is not well-formed.
     * @throws IndexOutOfBoundsException
     *             if targetStart < 0 or > than the length of target
     * @throws IndexOutOfBoundsException
     *             if length < 0 or targetStart + length > length of target
     * @throws UnsupportedOperationException if this method is not supported.
     */
    public int getTextCharacters(int sourceStart, char[] target,
            int targetStart, int length) throws XMLStreamException;

    /**
     * Returns the offset into the text character array where the first
     * character (of this text event) is stored.
     * 
     * @throws IllegalStateException if this state is not a valid text state.
     */
    public int getTextStart();

    /**
     * Returns the length of the sequence of characters for this Text event
     * within the text character array.
     * 
     * @throws IllegalStateException if this state is not a valid text state.
     */
    public int getTextLength();

    /**
     * Returns the input encoding if known or <code>null</code> if unknown.
     * 
     * @return the encoding of this instance or null.
     */
    public String getEncoding();

    /**
     * Indicates if the current event has text. The following
     * events have text: CHARACTERS, DTD ,ENTITY_REFERENCE, COMMENT, SPACE.
     * 
     * @return <code>true</code> if the current event as text;
     *         <code>false</code> otherwise.
     */
    public boolean hasText();

    /**
     * Return the current location of the processor. If the Location is unknown
     * the processor should return an implementation of Location that returns -1
     * for the location and null for the publicId and systemId. The location
     * information is only valid until next() is called.
     * 
     * @return the current location.
     */
    public Location getLocation();

    /**
     * Returns the (local) name of the current event. For START_ELEMENT or
     * END_ELEMENT returns the (local) name of the current element. For
     * ENTITY_REFERENCE it returns entity name. The current event must be
     * START_ELEMENT or END_ELEMENT, or ENTITY_REFERENCE.
     * 
     * @return the localName.
     * @throws IllegalStateException if this not a START_ELEMENT, END_ELEMENT 
     *         or ENTITY_REFERENCE
     */
    public CharArray getLocalName();

    /**
     * Indicates if the current event has a name (is a START_ELEMENT or
     * END_ELEMENT).
     * 
     * @return <code>true</code> if the current event has a name;
     *         <code>false</code> otherwise.
     */
    public boolean hasName();

    /**
     * If the current event is a START_ELEMENT or END_ELEMENT this method
     * returns the URI of the current element (URI mapping to the prefix
     * element/attribute has; or if no prefix <code>null</code>).
     * 
     * @return the URI bound to this elements prefix or <code>null</code>.
     * @throws IllegalStateException if not a START_ELEMENT, END_ELEMENT 
     *         or ATTRIBUTE.
     */
    public CharArray getNamespaceURI();

    /**
     * Returns the prefix of the current event or null if the event does not
     * have a prefix.
     * 
     * @return the prefix or <code>null</code>
     * @throws IllegalStateException if not a START_ELEMENT or END_ELEMENT.
     */
    public CharArray getPrefix();

    /**
     * Gets the xml version declared on the xml declaration. 
     * 
     * @return the XML version or <code>null</code>
     */
    public CharArray getVersion();

    /**
     * Gets the standalone declaration from the xml declaration.
     * 
     * @return <code>true</code> if this is standalone; 
     *         <code>false</code> otherwise.
     */
    public boolean isStandalone();

    /**
     * Checks if standalone was set in the document.
     * 
     * @return <code>true</code> if standalone was set; 
     *         <code>false</code> otherwise.
     */
    public boolean standaloneSet();

    /**
     * Returns the character encoding declared on the xml declaration.
     * 
     * @return the encoding declared in the document or <code>null</code>
     */
    public CharArray getCharacterEncodingScheme();

    /**
     * Returns the target of a processing instruction.
     * 
     * @return the target.
     * @throws IllegalStateException  if the current event is not a
     *             {@link XMLStreamConstants#PROCESSING_INSTRUCTION}
     */
    public CharArray getPITarget();

    /**
     * Get the data section of a processing instruction.
     * 
     * @return the data (if processing instruction has any) or
     *         <code>null</code> if the processing instruction only has target.
     * @throws IllegalStateException if the current event is not a
     *             {@link XMLStreamConstants#PROCESSING_INSTRUCTION}
     */
    public CharArray getPIData();
}
