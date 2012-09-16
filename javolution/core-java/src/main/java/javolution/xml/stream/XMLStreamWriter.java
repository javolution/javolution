/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2006 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.xml.stream;

import java.lang.CharSequence;

/**
 * <p> This interface is similar to 
 *     <code>javax.xml.stream.XMLStreamWriter</code>; but it does not forces 
 *     dynamic allocation when formatting (any {@link CharSequence CharSequence}
 *     can be used instead of {@link String}).</p>
 *     
 * <p> Except for the speed (faster) and the added flexibility, the 
 *     usage/behavior is about the same as its StAX counterpart.</p>
 *     
 * <p> This writer does not require creating new <code>String</code> objects 
 *     during XML formatting. Attributes values can be held by a single/reusable
 *     {@link javolution.text.TextBuilder TextBuilder}  
 *     (or <code>StringBuilder</code>) instance to avoid adverse effects 
 *     on memory footprint (heap), garbage collection and performance.
 *     For example:[code]
 *     
 *     // Creates a new writer (potentially recycled).
 *     XMLOutputFactory factory = XMLOutputFactory.newInstance();
 *     XMLStreamWriter writer = factory.createXMLStreamWriter(outputStream);
 *     
 *     TextBuilder tmp = new TextBuilder();
 *     writer.writeStartDocument();
 *     ...
 *     writer.writeStartElement("Time"); 
 *     // Writes primitive types (int) attributes (no memory allocation).
 *     writer.writeAttribute("hour", tmp.clear().append(time.hour);
 *     writer.writeAttribute("minute", tmp.clear().append(time.minute);
 *     writer.writeAttribute("second", tmp.clear().append(time.second);
 *     writer.writeEndElement();
 *     ...
 *     
 *     writer.close(); // Recycles this writer.
 *     outputStream.close(); // Underlying stream has to be closed explicitly.
 *     [/code]</p>
 *          
 * <p> Note: As always, <code>null</code> parameters are not allowed unless 
 *     explicitly authorized.</p>
 *     
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 4.0, June 16, 2006
 */
public interface XMLStreamWriter {

    /**
     * Writes a start tag to the output. All writeStartElement methods open a
     * new scope in the internal namespace context. Writing the corresponding
     * EndElement causes the scope to be closed.
     * 
     * @param localName local name of the tag.
     * @throws XMLStreamException
     */
    public void writeStartElement(CharSequence localName)
            throws XMLStreamException;

    /**
     * Writes a start tag to the output.
     * 
     * @param namespaceURI the namespaceURI of the prefix to use.
     * @param localName local name of the tag.
     * @throws XMLStreamException if the namespace URI has not been bound 
     *         to a prefix and this writer does not {@link 
     *         XMLOutputFactory#IS_REPAIRING_NAMESPACES repair namespaces}.
     */
    public void writeStartElement(CharSequence namespaceURI,
            CharSequence localName) throws XMLStreamException;

    /**
     * Writes a start tag to the output.
     * 
     * @param localName local name of the tag.
     * @param prefix the prefix of the tag.
     * @param namespaceURI the uri to bind the prefix to.
     * @throws XMLStreamException if the namespace URI has not been bound 
     *         to a prefix and this writer does not {@link 
     *         XMLOutputFactory#IS_REPAIRING_NAMESPACES repair namespaces}.
     */
    public void writeStartElement(CharSequence prefix, CharSequence localName,
            CharSequence namespaceURI) throws XMLStreamException;

    /**
     * Writes an empty element tag to the output.
     * 
     * @param namespaceURI the uri to bind the tag to.
     * @param localName local name of the tag.
     * @throws XMLStreamException if the namespace URI has not been bound 
     *         to a prefix and this writer does not {@link 
     *         XMLOutputFactory#IS_REPAIRING_NAMESPACES repair namespaces}.
     */
    public void writeEmptyElement(CharSequence namespaceURI,
            CharSequence localName) throws XMLStreamException;

    /**
     * Writes an empty element tag to the output.
     * 
     * @param prefix the prefix of the tag.
     * @param localName local name of the tag.
     * @param namespaceURI the uri to bind the tag to.
     * @throws XMLStreamException if the namespace URI has not been bound 
     *         to a prefix and this writer does not {@link 
     *         XMLOutputFactory#IS_REPAIRING_NAMESPACES repair namespaces}.
     */
    public void writeEmptyElement(CharSequence prefix, CharSequence localName,
            CharSequence namespaceURI) throws XMLStreamException;

    /**
     * Writes an empty element tag to the output.
     * 
     * @param localName local name of the tag.
     * @throws XMLStreamException
     */
    public void writeEmptyElement(CharSequence localName)
            throws XMLStreamException;

    /**
     * Writes an end tag to the output relying on the internal state of the
     * writer to determine the prefix and local name of the event.
     * 
     * @throws XMLStreamException
     */
    public void writeEndElement() throws XMLStreamException;

    /**
     * Closes any start tags and writes corresponding end tags.
     * 
     * @throws XMLStreamException
     */
    public void writeEndDocument() throws XMLStreamException;

    /**
     * Close this writer and free any resources associated with the writer. This
     * must not close the underlying output stream.
     * 
     * @throws XMLStreamException
     */
    public void close() throws XMLStreamException;

    /**
     * Write any cached data to the underlying output mechanism.
     * 
     * @throws XMLStreamException
     */
    public void flush() throws XMLStreamException;

    /**
     * Writes an attribute to the output stream without a prefix.
     * 
     * @param localName the local name of the attribute.
     * @param value the value of the attribute.
     * @throws IllegalStateException if the current state does not allow
     *         attribute writing.
     * @throws XMLStreamException
     */
    public void writeAttribute(CharSequence localName, CharSequence value)
            throws XMLStreamException;

    /**
     * Writes an attribute to the output stream.
     * 
     * @param prefix the prefix for this attribute.
     * @param namespaceURI the uri of the prefix for this attribute
     * @param localName the local name of the attribute.
     * @param value the value of the attribute.
     * @throws IllegalStateException if the current state does not allow 
     *         attribute writing.
     * @throws XMLStreamException if the namespace URI has not been bound 
     *         to a prefix and this writer does not {@link 
     *         XMLOutputFactory#IS_REPAIRING_NAMESPACES repair namespaces}.
     */

    public void writeAttribute(CharSequence prefix, CharSequence namespaceURI,
            CharSequence localName, CharSequence value)
            throws XMLStreamException;

    /**
     * Writes an attribute to the output stream.
     * 
     * @param namespaceURI the uri of the prefix for this attribute.
     * @param localName the local name of the attribute.
     * @param value the value of the attribute.
     * @throws IllegalStateException if the current state does not allow 
     *         attribute writing.
     * @throws XMLStreamException if the namespace URI has not been bound 
     *         to a prefix and this writer does not {@link 
     *         XMLOutputFactory#IS_REPAIRING_NAMESPACES repair namespaces}.
     */
    public void writeAttribute(CharSequence namespaceURI,
            CharSequence localName, CharSequence value)
            throws XMLStreamException;

    /**
     * Writes a namespace to the output stream. If the prefix argument to this
     * method is the empty string, "xmlns", or <code>null</code> this method 
     * will delegate to writeDefaultNamespace.
     * 
     * @param prefix the prefix to bind this namespace to or <code>null</code>
     * @param namespaceURI the uri to bind the prefix.
     * @throws IllegalStateException if the current state does not allow 
     *         namespace writing.
     * @throws XMLStreamException
     */
    public void writeNamespace(CharSequence prefix, CharSequence namespaceURI)
            throws XMLStreamException;

    /**
     * Writes the default namespace to the stream.
     * 
     * @param namespaceURI the uri to bind the default namespace to or 
     *        <code>null</code> (to map the prefix to <code>""</code> URI)
     * @throws IllegalStateException if the current state does not allow 
     *         namespace writing.
     * @throws XMLStreamException
     */
    public void writeDefaultNamespace(CharSequence namespaceURI)
            throws XMLStreamException;

    /**
     * Writes an xml comment with the data enclosed.
     * 
     * @param data the data contained in the comment or <code>null</code>
     * @throws XMLStreamException
     */
    public void writeComment(CharSequence data) throws XMLStreamException;

    /**
     * Writes a processing instruction.
     * 
     * @param target the target of the processing instruction.
     * @throws XMLStreamException
     */
    public void writeProcessingInstruction(CharSequence target)
            throws XMLStreamException;

    /**
     * Writes a processing instruction
     * 
     * @param target the target of the processing instruction.
     * @param data the data contained in the processing instruction.
     * @throws XMLStreamException
     */
    public void writeProcessingInstruction(CharSequence target,
            CharSequence data) throws XMLStreamException;

    /**
     * Writes a CData section.
     * 
     * @param data the data contained in the CData Section.
     * @throws XMLStreamException
     */
    public void writeCData(CharSequence data) throws XMLStreamException;

    /**
     * Writes a DTD section (representing the entire doctypedecl
     * production from the XML 1.0 specification).
     * 
     * @param dtd the DTD to be written.
     * @throws XMLStreamException
     */
    public void writeDTD(CharSequence dtd) throws XMLStreamException;

    /**
     * Writes an entity reference
     * 
     * @param name the name of the entity.
     * @throws XMLStreamException
     */
    public void writeEntityRef(CharSequence name) throws XMLStreamException;

    /**
     * Writes the XML Declaration. Defaults the XML version to 1.0 and the
     * encoding (if any) to the one specified when the instance is created 
         * using {@link XMLOutputFactory}.
     * 
     * @throws XMLStreamException
     */
    public void writeStartDocument() throws XMLStreamException;

    /**
     * Writes the XML Declaration. Default the encoding (if any) to the one 
         * specified when the instance is created using {@link XMLOutputFactory}.
     * 
     * @param version the version of the xml document or <code>null</code>.
     * @throws XMLStreamException
     */
    public void writeStartDocument(CharSequence version)
            throws XMLStreamException;

    /**
     * Writes the XML Declaration. Note that the encoding parameter does not set
     * the actual encoding of the underlying output. That must be set when the
     * instance when the instance is created using {@link XMLOutputFactory}.
     * 
     * @param encoding the encoding of the xml declaration or <code>null</code>.
     * @param version the version of the xml document or <code>null</code>.
     * @throws XMLStreamException
     */
    public void writeStartDocument(CharSequence encoding, CharSequence version)
            throws XMLStreamException;

    /**
     * Writes text to the output.
     * 
     * @param text the value to write or <code>null</code>.
     * @throws XMLStreamException
     */
    public void writeCharacters(CharSequence text) throws XMLStreamException;

    /**
     * Writes text to the output.
     * 
     * @param text the value to write
     * @param start the starting position in the array.
     * @param length the number of characters to write.
     * @throws XMLStreamException
     */
    public void writeCharacters(char[] text, int start, int length)
            throws XMLStreamException;

    /**
     * Gets the prefix the specified uri is bound to.
     * 
         * @param uri namespace URI
     * @return the prefix for the URI or <code>null</code>
     * @throws XMLStreamException
     */
    public CharSequence getPrefix(CharSequence uri) throws XMLStreamException;

    /**
     * Sets the prefix the uri is bound to. This prefix is bound in the scope of
     * the current START_ELEMENT / END_ELEMENT pair. If this method is called
     * before a START_ELEMENT has been written the prefix is bound in the root
     * scope.
     * 
     * @param prefix the prefix to bind to the uri.
     * @param uri the uri to bind to the prefix or <code>null</code>
     * @throws XMLStreamException
     */
    public void setPrefix(CharSequence prefix, CharSequence uri)
            throws XMLStreamException;

    /**
     * Binds a URI to the default namespace. This URI is bound in the scope of
     * the current START_ELEMENT / END_ELEMENT pair. If this method is called
     * before a START_ELEMENT has been written the uri is bound in the root
     * scope.
     * 
     * @param uri the uri to bind to the default namespace or <code>null</code>.
     * @throws XMLStreamException
     */
    public void setDefaultNamespace(CharSequence uri) throws XMLStreamException;

    /**
     * Gets the value of a feature/property from the underlying implementation.
     * 
     * @param name the name of the property.
     * @return the value of the property.
     * @throws IllegalArgumentException if the property is not supported.
     */
    public Object getProperty(String name) throws IllegalArgumentException;

}