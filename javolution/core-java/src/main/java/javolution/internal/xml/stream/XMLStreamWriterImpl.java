/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.internal.xml.stream;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import javolution.context.HeapContext;
import javolution.io.UTF8StreamWriter;
import javolution.text.CharArray;
import javolution.text.TextBuilder;
import javolution.xml.stream.XMLOutputFactory;
import javolution.xml.stream.XMLStreamException;
import javolution.xml.stream.XMLStreamWriter;



/**
 * <p> This class represents an implementation of {@link XMLStreamWriter}.</p>
 *     
 * <p> The <code>writeCharacters</code> methods will escape &amp; , &lt; and 
 *     &gt;. For attribute values, the <code>writeAttribute</code> methods will
 *     escape the above characters plus &quot; and control characters.</p>
 *     
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 4.0, September 4, 2006
 */
public final class XMLStreamWriterImpl implements XMLStreamWriter {

    /** 
     * Holds the length of intermediate buffer.
     */
    private static final int BUFFER_LENGTH = 2048;

    /** 
     * Holds the current nesting level.
     */
    private int _nesting = 0;

    /** 
     * Holds the element qualified name (indexed per nesting level)
     */
    private TextBuilder[] _qNames = new TextBuilder[16];

    /** 
     * Indicates if the current element is open.
     */
    private boolean _isElementOpen;

    /** 
     * Indicates if the current element is an empty element.
     */
    private boolean _isEmptyElement;

    /** 
     * Holds intermediate buffer.
     */
    private final char[] _buffer = new char[BUFFER_LENGTH];

    /** 
     * Holds the namespace stack.
     */
    private final NamespacesImpl _namespaces = new NamespacesImpl();

    /** 
     * Holds the buffer current index.
     */
    private int _index;

    /** 
     * Holds repairing namespace property.
     */
    private boolean _isRepairingNamespaces;

    /** 
     * Holds repairing prefix property.
     */
    private String _repairingPrefix = "ns";

    /** 
     * Holds indentation property.
     */
    private String _indentation;

    /**
     * Holds line separator property.
     */
    private String _lineSeparator = "\n";

    /** 
     * Holds current indentation level.
     */
    private int _indentationLevel;

    /** 
     * Holds automatic empty elements  property.
     */
    private boolean _automaticEmptyElements;

    /** 
     * Holds no empty element tag property.
     */
    private boolean _noEmptyElementTag;

    /** 
     * Holds counter for automatic namespace generation.
     */
    private int _autoNSCount;

    /** 
     * Indicates if the current object written is an attribute value.
     */
    private boolean _isAttributeValue;
    
    ////////////////////////
    // Temporary Settings //
    ////////////////////////

    /** 
     * Holds the writer destination (<code>null</code> when unused).
     */
    private Writer _writer;

    /** 
     * Holds the encoding  (<code>null</code> if N/A).
     */
    private String _encoding;

    /**
     * Holds the defautl writer for output streams.
     */
    private final UTF8StreamWriter _utf8StreamWriter = new UTF8StreamWriter();

    /** 
     * Default constructor.
     */
    public XMLStreamWriterImpl() {
        for (int i = 0; i < _qNames.length;) {
            _qNames[i++] = new TextBuilder();
        }
    }

    /**
     * Sets the output stream destination for this XML stream writer 
     * (UTF-8 encoding).
     *
     * @param out the output source with utf-8 encoding.
     */
    public void setOutput(OutputStream out) throws XMLStreamException {
        _utf8StreamWriter.setOutput(out);
        _encoding = "UTF-8";
        setOutput(_utf8StreamWriter);
    }

    /**
     * Sets the output stream destination and encoding for this XML stream 
     * writer.
     *
     * @param out the output source.
     * @param encoding the associated encoding.
     * @throws XMLStreamException if the specified encoding is not supported.
     */
    public void setOutput(OutputStream out, String encoding)
            throws XMLStreamException {
        if (encoding.equals("UTF-8") || encoding.equals("utf-8")
                || encoding.equals("ASCII")) {
            setOutput(out); // Default encoding.
        } else {
            try {
                _encoding = encoding;
                setOutput(new OutputStreamWriter(out, encoding));
            } catch (UnsupportedEncodingException e) {
                throw new XMLStreamException(e);
            }
        }
    }

    /**
     * Sets the writer output destination for this XML stream writer. 
     *
     * @param  writer the output destination writer.
     * @see    javolution.io.UTF8StreamWriter
     * @see    javolution.io.UTF8ByteBufferWriter
     * @see    javolution.io.AppendableWriter
     */
    public void setOutput(Writer writer) throws XMLStreamException {
        if (_writer != null)
            throw new IllegalStateException("Writer not closed or reset");
        _writer = writer;
    }

    /** 
     * Requires this writer to create a new prefix when a namespace has none
     * (default <code>false</code>).
     * 
     * @param isRepairingNamespaces <code>true</code> if namespaces 
     *        are repaired; <code>false</code> otherwise.
     */
    public void setRepairingNamespaces(boolean isRepairingNamespaces) {
        _isRepairingNamespaces = isRepairingNamespaces;
    }

    /** 
     * Specifies the prefix to be append by a trailing part 
     * (a sequence number) in order to make it unique to be usable as
     * a temporary non-colliding prefix when repairing namespaces
     * (default <code>"ns"</code>).
     * 
     * @param repairingPrefix the prefix root.
     */
    public void setRepairingPrefix(String repairingPrefix) {
        _repairingPrefix = repairingPrefix;
    }

    /** 
     * Specifies the indentation string; non-null indentation 
     * forces the writer to write elements into separate lines
     * (default <code>null</code>).
     * 
     * @param indentation the indentation string.
     */
    public void setIndentation(String indentation) {
        _indentation = indentation;
    }

    /**
     * Specifies the line separator (default <code>"\n"</code>).
     *
     * @param lineSeparator the line separator string.
     */
    public void setLineSeparator(String lineSeparator) {
        _lineSeparator = lineSeparator;
    }

    /** 
     * Requires this writer to automatically output empty elements when a 
     * start element is immediately followed by matching end element
     * (default <code>false</code>).
     * 
     * @param automaticEmptyElements <code>true</code> if start element 
     *        immediately followed by end element results in an empty element
     *        beign written; <code>false</code> otherwise.
     */
    public void setAutomaticEmptyElements(boolean automaticEmptyElements) {
        _automaticEmptyElements = automaticEmptyElements;
    }

    /** 
     * Prevent this writer from using empty element tags
     * (default <code>false</code>).
     * 
     * @param noEmptyElementTag <code>true</code> if empty element tags 
     *        are replaced by start/end elements with no content;
     *        <code>false</code> otherwise.
     */
    public void setNoEmptyElementTag(boolean noEmptyElementTag) {
        _noEmptyElementTag = noEmptyElementTag;
    }

    // Implements reusable.
    public void reset() {
        _automaticEmptyElements = false;
        _autoNSCount = 0;
        _encoding = null;
        _indentation = null;
        _indentationLevel = 0;
        _index = 0;
        _isAttributeValue = false;
        _isElementOpen = false;
        _isEmptyElement = false;
        _isRepairingNamespaces = false;
        _namespaces.reset();
        _nesting = 0;
        _noEmptyElementTag = false;
        _repairingPrefix = "ns";
        _utf8StreamWriter.reset();
        _writer = null;
    }

    // Implements XMLStreamWriter interface.
    public void writeStartElement(CharSequence localName)
            throws XMLStreamException {
        if (localName == null)
            throw new XMLStreamException("Local name cannot be null");
        writeNewElement(null, localName, null);
    }

    // Implements XMLStreamWriter interface.
    public void writeStartElement(CharSequence namespaceURI,
            CharSequence localName) throws XMLStreamException {
        if (localName == null)
            throw new XMLStreamException("Local name cannot be null");
        if (namespaceURI == null)
            throw new XMLStreamException("Namespace URI cannot be null");
        writeNewElement(null, localName, namespaceURI);
    }

    // Implements XMLStreamWriter interface.
    public void writeStartElement(CharSequence prefix, CharSequence localName,
            CharSequence namespaceURI) throws XMLStreamException {
        if (localName == null)
            throw new XMLStreamException("Local name cannot be null");
        if (namespaceURI == null)
            throw new XMLStreamException("Namespace URI cannot be null");
        if (prefix == null)
            throw new XMLStreamException("Prefix cannot be null");
        writeNewElement(prefix, localName, namespaceURI);
    }

    // Implements XMLStreamWriter interface.
    public void writeEmptyElement(CharSequence localName)
            throws XMLStreamException {
        writeStartElement(localName);
        _isEmptyElement = true;
    }

    // Implements XMLStreamWriter interface.
    public void writeEmptyElement(CharSequence namespaceURI,
            CharSequence localName) throws XMLStreamException {
        writeStartElement(namespaceURI, localName);
        _isEmptyElement = true;
    }

    // Implements XMLStreamWriter interface.
    public void writeEmptyElement(CharSequence prefix, CharSequence localName,
            CharSequence namespaceURI) throws XMLStreamException {
        writeStartElement(prefix, localName, namespaceURI);
        _isEmptyElement = true;
    }

    // Implements XMLStreamWriter interface.
    public void writeEndElement() throws XMLStreamException {
        if (_isElementOpen) { // Empty element.
            if (_isEmptyElement) { // Closes the empty element tag.
                closeOpenTag();
            } else { // Start element open.
                if (_automaticEmptyElements) { // Do as if empty element written. 		    
                    _isEmptyElement = true;
                    closeOpenTag();
                    return;
                } else { // Closes the start element tag.
                    closeOpenTag();
                }
            }
        }
        if ((_indentation != null) && (_indentationLevel != _nesting - 1)) {
            // Do not indent if no change in indentation level
            // to avoid interfering with text only elements.
            writeNoEscape(_lineSeparator);
            for (int i = 1; i < _nesting; i++) {
                writeNoEscape(_indentation);
            }
        }

        write('<');
        write('/');
        writeNoEscape(_qNames[_nesting--]);
        write('>');
        _namespaces.pop();
    }

    // Implements XMLStreamWriter interface.
    public void writeEndDocument() throws XMLStreamException {
        if (_isElementOpen)
            closeOpenTag();
        while (_nesting > 0) { // Implicits closing of all elements.
            writeEndElement();
        }
        flush(); // Not mandatory but safer.
    }

    // Implements XMLStreamWriter interface.
    public void close() throws XMLStreamException {
        if (_writer != null) {
            if (_nesting != 0) { // Closes all elements.
                writeEndDocument();
            }
            flush();
        }
        reset(); // Explicit reset.
     }

    // Implements XMLStreamWriter interface.
    public void flush() throws XMLStreamException {
        flushBuffer();
        try {
            _writer.flush();
        } catch (IOException e) {
            throw new XMLStreamException(e);
        }
    }

    // Implements XMLStreamWriter interface.
    public void writeAttribute(CharSequence localName, CharSequence value)
            throws XMLStreamException {
        if (localName == null)
            throw new XMLStreamException("Local name cannot be null");
        if (value == null)
            throw new XMLStreamException("Value cannot be null");
        writeAttributeOrNamespace(null, null, localName, value);
    }

    // Implements XMLStreamWriter interface.
    public void writeAttribute(CharSequence namespaceURI,
            CharSequence localName, CharSequence value)
            throws XMLStreamException {
        if (localName == null)
            throw new XMLStreamException("Local name cannot be null");
        if (value == null)
            throw new XMLStreamException("Value cannot be null");
        if (namespaceURI == null)
            throw new XMLStreamException("Namespace URI cannot be null");
        writeAttributeOrNamespace(null, namespaceURI, localName, value);
    }

    // Implements XMLStreamWriter interface.
    public void writeAttribute(CharSequence prefix, CharSequence namespaceURI,
            CharSequence localName, CharSequence value)
            throws XMLStreamException {
        if (localName == null)
            throw new XMLStreamException("Local name cannot be null");
        if (value == null)
            throw new XMLStreamException("Value cannot be null");
        if (namespaceURI == null)
            throw new XMLStreamException("Namespace URI cannot be null");
        if (prefix == null)
            throw new XMLStreamException("Prefix cannot be null");
        writeAttributeOrNamespace(prefix, namespaceURI, localName, value);
    }

    // Implements XMLStreamWriter interface.
    public void writeNamespace(CharSequence prefix, CharSequence namespaceURI)
            throws XMLStreamException {
        if ((prefix == null) || (prefix.length() == 0)
                || _namespaces._xmlns.equals(prefix)) {
            prefix = _namespaces._defaultNsPrefix;
        }
        if (!_isElementOpen) // Check now as the actual writting is queued.
            throw new IllegalStateException("No open start element");
        _namespaces.setPrefix(prefix,
                (namespaceURI == null) ? _namespaces._nullNsURI : namespaceURI,
                true);
    }

    // Implements XMLStreamWriter interface.
    public void writeDefaultNamespace(CharSequence namespaceURI)
            throws XMLStreamException {
        writeNamespace(_namespaces._defaultNsPrefix, namespaceURI);
    }

    // Implements XMLStreamWriter interface.
    public void writeComment(CharSequence data) throws XMLStreamException {
        if (_isElementOpen)
            closeOpenTag();
        writeNoEscape("<!--");
        if (data != null) { // null values allowed.
            writeNoEscape(data);
        }
        writeNoEscape("-->");
    }

    // Implements XMLStreamWriter interface.
    public void writeProcessingInstruction(CharSequence target)
            throws XMLStreamException {
        writeProcessingInstruction(target, _noChar);
    }

    private final CharArray _noChar = new CharArray("");

    // Implements XMLStreamWriter interface.
    public void writeProcessingInstruction(CharSequence target,
            CharSequence data) throws XMLStreamException {
        if (target == null)
            throw new XMLStreamException("Target cannot be null");
        if (data == null)
            throw new XMLStreamException("Data cannot be null");
        if (_isElementOpen)
            closeOpenTag();
        writeNoEscape("<?");
        writeNoEscape(target);
        write(' ');
        writeNoEscape(data);
        writeNoEscape(" ?>");
    }

    // Implements XMLStreamWriter interface.
    public void writeCData(CharSequence data) throws XMLStreamException {
        if (data == null)
            throw new XMLStreamException("Data cannot be null");
        if (_isElementOpen)
            closeOpenTag();
        writeNoEscape("<![CDATA[");
        writeNoEscape(data);
        writeNoEscape("]]>");
    }

    // Implements XMLStreamWriter interface.
    public void writeDTD(CharSequence dtd) throws XMLStreamException {
        if (dtd == null)
            throw new XMLStreamException("DTD cannot be null");
        if (_nesting > 0)
            throw new XMLStreamException(
                    "DOCTYPE declaration (DTD) when not in document root (prolog)");
        writeNoEscape(dtd);
    }

    // Implements XMLStreamWriter interface.
    public void writeEntityRef(CharSequence name) throws XMLStreamException {
        write('&');
        writeNoEscape(name);
        write(';');
    }

    // Implements XMLStreamWriter interface.
    public void writeStartDocument() throws XMLStreamException {
        writeStartDocument(null, null);
    }

    // Implements XMLStreamWriter interface.
    public void writeStartDocument(CharSequence version)
            throws XMLStreamException {
        writeStartDocument(null, version);
    }

    // Implements XMLStreamWriter interface.
    public void writeStartDocument(CharSequence encoding, CharSequence version)
            throws XMLStreamException {
        if (_nesting > 0)
            throw new XMLStreamException("Not in document root");
        writeNoEscape("<?xml version=\"");
        if (version != null) {
            writeNoEscape(version);
            write('"');
        } else { // Default to 1.0
            writeNoEscape("1.0\"");
        }
        if (encoding != null) {
            writeNoEscape(" encoding=\"");
            writeNoEscape(encoding);
            write('"');
        } else if (_encoding != null) { // Use init encoding (if any).
            writeNoEscape(" encoding=\"");
            writeNoEscape(_encoding);
            write('"');
        }
        writeNoEscape(" ?>");
    }

    // Implements XMLStreamWriter interface.
    public void writeCharacters(CharSequence text) throws XMLStreamException {
        if (_isElementOpen)
            closeOpenTag();
        if (text == null)
            return;
        writeEscape(text);
    }

    // Implements XMLStreamWriter interface.
    public void writeCharacters(char[] text, int start, int length)
            throws XMLStreamException {
        _tmpCharArray.setArray(text, start, length);
        writeCharacters(_tmpCharArray);
    }

    private final CharArray _tmpCharArray = new CharArray();

    // Implements XMLStreamWriter interface.
    public CharSequence getPrefix(CharSequence uri) throws XMLStreamException {
        return _namespaces.getPrefix(uri);
    }

    // Implements XMLStreamWriter interface.
    public void setPrefix(CharSequence prefix, CharSequence uri)
            throws XMLStreamException {
        _namespaces.setPrefix(prefix, (uri == null) ? _namespaces._nullNsURI
                : uri, false);
    }

    // Implements XMLStreamWriter interface.
    public void setDefaultNamespace(CharSequence uri) throws XMLStreamException {
        setPrefix(_namespaces._defaultNsPrefix, uri);
    }

    // Implements XMLStreamWriter interface.
    public Object getProperty(String name) throws IllegalArgumentException {
        if (name.equals(XMLOutputFactory.IS_REPAIRING_NAMESPACES)) {
            return new Boolean(_isRepairingNamespaces);
        } else if (name.equals(XMLOutputFactory.REPAIRING_PREFIX)) {
            return _repairingPrefix;
        } else if (name.equals(XMLOutputFactory.AUTOMATIC_EMPTY_ELEMENTS)) {
            return new Boolean(_automaticEmptyElements);
        } else if (name.equals(XMLOutputFactory.NO_EMPTY_ELEMENT_TAG)) {
            return new Boolean(_noEmptyElementTag);
        } else if (name.equals(XMLOutputFactory.INDENTATION)) {
            return _indentation;
        } else if (name.equals(XMLOutputFactory.LINE_SEPARATOR)) {
            return _lineSeparator;
        } else {
            throw new IllegalArgumentException("Property: " + name
                    + " not supported");
        }
    }

    // Writes a new start or empty element.
    private void writeNewElement(CharSequence prefix, CharSequence localName,
            CharSequence namespaceURI) throws XMLStreamException {

        // Close any open element and gets ready to write a new one.
        if (_isElementOpen)
            closeOpenTag();
        if (_indentation != null) {
            writeNoEscape(_lineSeparator);
            _indentationLevel = _nesting;
            for (int i = 0; i < _indentationLevel; i++) {
                writeNoEscape(_indentation);
            }
        }
        write('<');
        _isElementOpen = true;

        // Enters a new local scope.
        if (++_nesting >= _qNames.length)
            resizeElemStack();
        _namespaces.push();

        // Constructs qName.
        TextBuilder qName = _qNames[_nesting].clear();

        // Writes prefix if any.
        if ((namespaceURI != null)
                && (!_namespaces._defaultNamespace.equals(namespaceURI))) {
            if (_isRepairingNamespaces) { // Repairs prefix.
                prefix = getRepairedPrefix(prefix, namespaceURI);
            } else if (prefix == null) { // Retrieves prefix.
                prefix = getPrefix(namespaceURI);
                if (prefix == null)
                    throw new XMLStreamException("URI: " + namespaceURI
                            + " not bound and repairing namespaces disabled");
            }
            if (prefix.length() > 0) {
                qName.append(prefix);
                qName.append(':');
            }
        }
        qName.append(localName);
        writeNoEscape(qName);
    }

    // Writes a new attribute.
    private void writeAttributeOrNamespace(CharSequence prefix,
            CharSequence namespaceURI, CharSequence localName,
            CharSequence value) throws XMLStreamException {
        if (!_isElementOpen)
            throw new IllegalStateException("No open start element");
        write(' ');

        // Writes prefix if any.
        if ((namespaceURI != null)
                && (!_namespaces._defaultNamespace.equals(namespaceURI))) {
            if (_isRepairingNamespaces) { // Repairs prefix if current prefix is not correct.
                prefix = getRepairedPrefix(prefix, namespaceURI);
            } else if (prefix == null) {
                prefix = getPrefix(namespaceURI);
                if (prefix == null)
                    throw new XMLStreamException("URI: " + namespaceURI
                            + " not bound and repairing namespaces disabled");
            }
            if (prefix.length() > 0) {
                writeNoEscape(prefix);
                write(':');
            }
        }

        writeNoEscape(localName);
        write('=');
        write('"');
        _isAttributeValue = true;
        writeEscape(value);
        _isAttributeValue = false;
        write('"');
    }

    // Closes the current element (scope if empty element).
    private void closeOpenTag() throws XMLStreamException {

        // Writes namespaces now.
        writeNamespaces();

        // Closes the tag.
        _isElementOpen = false;
        if (_isEmptyElement) {
            if (_noEmptyElementTag) {
                write('<');
                write('/');
                writeNoEscape(_qNames[_nesting]);
                write('>');
            } else { // Use empty element tag.
                write('/');
                write('>');
            }
            _nesting--;
            _namespaces.pop();
            _isEmptyElement = false;
        } else {
            write('>');
        }
    }

    // Writes all namespaces, these include namespaces set but 
    // not written in outer scope.
    private void writeNamespaces() throws XMLStreamException {
        int i0 = (_nesting > 1) ? _namespaces._namespacesCount[_nesting - 2]
                : NamespacesImpl.NBR_PREDEFINED_NAMESPACES;
        int i1 = _namespaces._namespacesCount[_nesting - 1];
        int i2 = _namespaces._namespacesCount[_nesting];
        for (int i = i0; i < i2; i++) {
            if (((_isRepairingNamespaces && (i < i1) && !_namespaces._prefixesWritten[i]))
                    || ((i >= i1) && _namespaces._prefixesWritten[i])) { // Write namespace.

                // In repairing mode, removes redondancy.
                if (_isRepairingNamespaces) {
                    CharArray prefix = _namespaces.getPrefix(
                            _namespaces._namespaces[i], i);
                    if (_namespaces._prefixes[i].equals(prefix))
                        continue; // Not necessary.
                } // Direct mode, just write them as requested (no check).

                // Writes namespace.
                if (_namespaces._prefixes[i].length() == 0) { // Default namespace.
                    writeAttributeOrNamespace(null, null, _namespaces._xmlns,
                            _namespaces._namespaces[i]);
                } else {
                    writeAttributeOrNamespace(_namespaces._xmlns,
                            _namespaces._xmlnsURI, _namespaces._prefixes[i],
                            _namespaces._namespaces[i]);
                }
            }
        }
    }

    // Returns the prefix for the specified namespace.
    private CharSequence getRepairedPrefix(CharSequence prefix,
            CharSequence namespaceURI) throws XMLStreamException {
        CharArray prefixForURI = _namespaces.getPrefix(namespaceURI);
        if ((prefixForURI != null)
                && ((prefix == null) || prefixForURI.equals(prefix)))
            return prefixForURI; // No repair needed.
        if ((prefix == null) || (prefix.length() == 0)) { // Creates new prefix.
            prefix = _autoPrefix.clear().append(_repairingPrefix).append(
                    _autoNSCount++);
        }
        _namespaces.setPrefix(prefix, namespaceURI, true); // Map to namespace URI.
        return prefix;
    }

    private final TextBuilder _autoPrefix = new TextBuilder();

    // Resizes element stack  (same memory area as the writer).
    private void resizeElemStack() {
        HeapContext.execute(new Runnable() {
            public void run() {
                final int oldLength = _qNames.length;
                final int newLength = oldLength * 2;

                // Resizes elements qNames stack.
                TextBuilder[] tmp = new TextBuilder[newLength];
                System.arraycopy(_qNames, 0, tmp, 0, oldLength);
                _qNames = tmp;
                for (int i = oldLength; i < newLength; i++) {
                    _qNames[i] = new TextBuilder();
                }
            }
        });
    }

    // Writes methods.
    //

    private final void writeNoEscape(String str) throws XMLStreamException {
        write(str, 0, str.length(), false);
    }

    private final void writeNoEscape(TextBuilder tb) throws XMLStreamException {
        write(tb, 0, tb.length(), false);
    }

    private final void writeNoEscape(CharSequence csq)
            throws XMLStreamException {
        write(csq, 0, csq.length(), false);
    }

    private final void writeEscape(CharSequence csq)
            throws XMLStreamException {
        write(csq, 0, csq.length(), true);
    }

    private final void write(Object csq, int start, int length,
            boolean escapeMarkup) throws XMLStreamException {
        if (_index + length <= BUFFER_LENGTH) { // Enough buffer space.
            if (csq instanceof String) {
                ((String) csq).getChars(start, start + length, _buffer, _index);
            } else if (csq instanceof javolution.text.Text) {
                ((javolution.text.Text) csq).getChars(start, start
                        + length, _buffer, _index);
            } else if (csq instanceof javolution.text.TextBuilder) {
                ((javolution.text.TextBuilder) csq).getChars(start, start
                        + length, _buffer, _index);
            } else if (csq instanceof javolution.text.CharArray) {
                ((javolution.text.CharArray) csq).getChars(start, start
                        + length, _buffer, _index);
            } else {
                getChars((CharSequence) csq, start, start + length, _buffer,
                        _index);
            }
            if (escapeMarkup) {
                int end = _index + length;
                for (int i = _index; i < end; i++) {
                    char c = _buffer[i];
                    if ((c >= '?') || !isEscaped(c))
                        continue;
                    // Found character to escape.
                    _index = i;
                    flushBuffer();
                    writeDirectEscapedCharacters(_buffer, i, end);
                    return; // Done (buffer is empty).
                }
            }
            _index += length;

        } else { // Not enough remaining space.
            if (length <= BUFFER_LENGTH) { // Enough space if buffer emptied.
                flushBuffer();
                write(csq, start, length, escapeMarkup);
            } else {
                int half = length >> 1;
                write(csq, start, half, escapeMarkup);
                write(csq, start + half, length - half, escapeMarkup);
            }
        }
    }

    private static void getChars(CharSequence csq, int start, int end,
            char dest[], int destPos) {
        for (int i = start, j = destPos; i < end;) {
            dest[j++] = csq.charAt(i++);
        }
    }

    // The buffer must have been flushed prior to calling this method.
    private final void writeDirectEscapedCharacters(char[] chars, int start, int end)
            throws XMLStreamException {
        try {
            int blockStart = start;
            for (int i = start; i < end;) {
                char c = chars[i++];
                if ((c >= '?') || !isEscaped(c))
                    continue;
                // Flush already read characters (excluding escaped one).
                int blockLength = i - blockStart - 1;
                if (blockLength > 0) {
                    _writer.write(_buffer, blockStart, blockLength);
                }
                blockStart = i;                
                switch (c) {
                case '<':
                    _writer.write("&lt;");
                    break;
                case '>':
                    _writer.write("&gt;");
                    break;
                case '\'': 
                   _writer.write("&apos;");
                   break;
                case '"':
                    _writer.write("&quot;");
                    break;
                case '&':
                    _writer.write("&amp;");
                    break;
                default:
                    _writer.write("&#");
                    _writer.write((char) ('0' + c / 10));
                    _writer.write((char) ('0' + c % 10));
                    _writer.write(';');
                }
            }
            // Flush the current block.
            int blockLength = end - blockStart;
            if (blockLength > 0) {
                _writer.write(_buffer, blockStart, blockLength);
            }                                   
        } catch (IOException e) {
            throw new XMLStreamException(e);
        }
    }

    private boolean isEscaped(char c) {
        return ((c < ' ') && _isAttributeValue) ||
            (c == '"' && _isAttributeValue) ||
            (c == '<') || (c == '>') || (c == '&');
    }
    
    private final void write(char c) throws XMLStreamException {
        if (_index == BUFFER_LENGTH) {
            flushBuffer();
        }
        _buffer[_index++] = c;
    }

    private void flushBuffer() throws XMLStreamException {
        try {
            _writer.write(_buffer, 0, _index);
        } catch (IOException e) {
            throw new XMLStreamException(e);
        } finally {
            _index = 0;
        }
    }

}