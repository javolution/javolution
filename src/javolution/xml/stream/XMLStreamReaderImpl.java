/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2006 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.xml.stream;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import javolution.context.ObjectFactory;
import javolution.io.UTF8StreamReader;
import javolution.lang.Reusable;
import javolution.text.CharArray;
import javolution.xml.sax.Attributes;
import j2me.lang.CharSequence;
import j2me.lang.IllegalStateException;
import j2me.util.Map;
import j2mex.realtime.MemoryArea;

/**
 * <p> This class represents a  {@link javolution.lang.Reusable reusable}
 *     implementation of {@link XMLStreamWriter}.</p>
 *  
 * <p> Except for the types being used ({@link CharArray CharArray}/
 *     {@link CharSequence CharSequence} instead of {@link String}) the 
 *     parsing behavior is about the same as for the standard 
 *     <code>javax.xml.stream.XMLStreamReader</code> (although several times 
 *     faster).</p>
 *     
 * <p> The {@link CharArray CharArray} instances returned by this reader 
 *     supports fast primitive conversions as illustrated below:[code]
 *     
 *     // Creates reader for an input sream with unknown encoding.
 *     XMLStreamReaderImpl xmlReader = new XMLStreamReaderImpl().setInput(inputStream);
 *     
 *     // Parses.
 *     for (int e=xmlReader.next(); e != XMLStreamConstants.END_DOCUMENT; e = xmlReader.next()) {
 *         switch (e) { // Event.
 *             case XMLStreamConstants.START_ELEMENT:
 *             if (xmlReader.getLocalName().equals("Time")) {
 *                  // Reads primitive types (int) attributes directly.
 *                  int hour = xmlReader.getAttributeValue("hour").toInt();
 *                  int minute = xmlReader.getAttributeValue("minute").toInt();
 *                  int second = xmlReader.getAttributeValue("second").toInt();
 *                  ...
 *             }
 *             ...
 *             break;
 *         }         
 *     }
 *     
 *     // Closes reader, it is automatically reset() and can be reused!
 *     xmlReader.close();
 *     [/code]</p>
 *     
 *  <p> This reader returns all contiguous character data in a single
 *      chunk (always coalescing). It is non-validating (DTD is returned 
 *      unparsed). Although, users may define custom entities mapping using 
 *      the {@link #setEntities} method (e.g. after parsing/resolving 
 *      external entities).</p>
 *       
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 4.0, September 4, 2006
 */
public final class XMLStreamReaderImpl implements XMLStreamReader, Reusable {

    /**
     * Holds the textual representation for events.
     */
    static final String[] NAMES_OF_EVENTS = new String[] { "UNDEFINED",
            "START_ELEMENT", "END_ELEMENT", "PROCESSING_INSTRUCTIONS",
            "CHARACTERS", "COMMENT", "SPACE", "START_DOCUMENT", "END_DOCUMENT",
            "ENTITY_REFERENCE", "ATTRIBUTE", "DTD", "CDATA", "NAMESPACE",
            "NOTATION_DECLARATION", "ENTITY_DECLARATION" };

    /**
     * Holds the reader buffer capacity.
     */
    static final int READER_BUFFER_CAPACITY = 4096;

    /**
     * Holds the prolog if any.
     */
    CharArray _prolog;

    /**
     * Holds object factory when factory-produced.
     */
    ObjectFactory _objectFactory;

    /**
     * Holds the current index in the character buffer.
     */
    private int _readIndex;

    /**
     * Number of characters read from reader
     */
    private int _readCount;

    /**
     * Holds the data buffer for CharSequence produced by this parser.
     */
    private char[] _data = new char[READER_BUFFER_CAPACITY * 2];

    /**
     * Holds the current index of the data buffer (_data).
     */
    private int _index;

    /**
     * Holds the current element nesting.
     */
    private int _nesting;

    /**
     * Holds qualified name (include prefix).
     */
    private CharArray _qName;

    /**
     * Holds element prefix separator index.
     */
    private int _prefixSep;

    /**
     * Holds attribute qualified name.
     */
    private CharArray _attrQName;

    /**
     * Holds attribute prefix separator index.
     */
    private int _attrPrefixSep;

    /**
     * Holds attribute value.
     */
    private CharArray _attrValue;

    /**
     * Holds current event type
     */
    private int _eventType = START_DOCUMENT;

    /**
     * Indicates if event type is START_TAG, and tag is empty, i.e. <sometag/>
     */
    private boolean _isEmpty;

    /**
     * Indicates if characters are pending for potential coalescing.
     */
    boolean _charactersPending = false;

    /**
     * Holds the start index for the current state within _data array.
     */
    private int _start;

    /**
     * Holds the parser state.
     */
    private int _state = STATE_DEFAULT;

    /**
     * Holds the current text.
     */
    private CharArray _text;

    /** 
     * Holds the reader input source (<code>null</code> when unused).
     */
    private Reader _reader;

    /**
     * Holds the character buffer used for reading.
     */
    private final char[] _readBuffer = new char[READER_BUFFER_CAPACITY];

    /**
     * Holds the start offset in the character buffer (due to auto detection
     * of encoding).
     */
    private int _startOffset; // Byte Order Mark count.

    /**
     * Holds the location object.
     */
    private final LocationImpl _location = new LocationImpl();

    /**
     * Holds the namespace  stack.
     */
    private final NamespacesImpl _namespaces = new NamespacesImpl();

    /**
     * Holds the current attributes.
     */
    private final AttributesImpl _attributes = new AttributesImpl(_namespaces);

    /**
     * Holds working stack (by nesting level).
     */
    private CharArray[] _elemStack = new CharArray[16];

    /**
     * Holds stream encoding if known.
     */
    private String _encoding;

    /**
     * Holds the entities.
     */
    private final EntitiesImpl _entities = new EntitiesImpl();

    /**
     * Holds the reader for input streams.
     */
    private final UTF8StreamReader _utf8StreamReader = new UTF8StreamReader();

    /** 
     * Default constructor.
     */
    public XMLStreamReaderImpl() {
    }

    /**
     * Sets the input stream source for this XML stream reader 
     * (encoding retrieved from XML prolog if any).
     *
     * @param  in the input source with unknown encoding.
     */
    public void setInput(InputStream in) throws XMLStreamException {
        setInput(in, detectEncoding(in));
        CharArray prologEncoding = getCharacterEncodingScheme();

        // Checks if necessary to change the reader.
        if ((prologEncoding != null) && !prologEncoding.equals(_encoding)
                && !(isUTF8(prologEncoding) && isUTF8(_encoding))) {
            // Changes reader (keep characters already read).
            int startOffset = _readCount;
            reset();
            _startOffset = startOffset;
            setInput(in, prologEncoding.toString());
        }
    }

    private static boolean isUTF8(Object encoding) {
        return encoding.equals("utf-8") || encoding.equals("UTF-8")
                || encoding.equals("ASCII") || encoding.equals("utf8")
                || encoding.equals("UTF8");
    }

    /**
     * Sets the input stream source and encoding for this XML stream reader.
     *
     * @param in the input source.
     * @param encoding the associated encoding.
     */
    public void setInput(InputStream in, String encoding)
            throws XMLStreamException {
        _encoding = encoding;
        if (isUTF8(encoding)) { // Use our fast UTF-8 Reader.
            setInput(_utf8StreamReader.setInput(in));
        } else {
            try {
                setInput(new InputStreamReader(in, encoding));
            } catch (UnsupportedEncodingException e) {
                throw new XMLStreamException(e);
            }
        }
    }

    /**
     * Sets the reader input source for this XML stream reader. 
     * This method reads the prolog (if any).
     *
     * @param  reader the input source reader.
     * @see    javolution.io.UTF8StreamReader
     * @see    javolution.io.UTF8ByteBufferReader
     * @see    javolution.io.CharSequenceReader
     */
    public void setInput(Reader reader) throws XMLStreamException {
        if (_reader != null)
            throw new IllegalStateException("Reader not closed or reset");
        _reader = reader;
        try { // Reads prolog (if there)
            int readCount = reader.read(_readBuffer, _startOffset,
                    _readBuffer.length - _startOffset);
            _readCount = (readCount >= 0) ? readCount + _startOffset
                    : _startOffset;
            if ((_readCount >= 5) && (_readBuffer[0] == '<')
                    && (_readBuffer[1] == '?') && (_readBuffer[2] == 'x')
                    && (_readBuffer[3] == 'm') && (_readBuffer[4] == 'l')
                    && (_readBuffer[5] == ' ')) { // Prolog detected.
                next(); // Processing instruction.
                _prolog = this.getPIData();
                _index = _prolog.offset() + _prolog.length(); // Keep prolog.
                _eventType = START_DOCUMENT; // Resets to START_DOCUMENT.
            }
        } catch (IOException e) {
            throw new XMLStreamException(e);
        }
    }

    /**
     * Returns the qualified name of the current event.
     * 
     * @return the qualified name.
     * @throws IllegalStateException if this not a START_ELEMENT or END_ELEMENT.
     */
    public CharArray getQName() {
        if ((_eventType != XMLStreamConstants.START_ELEMENT)
                && (_eventType != XMLStreamConstants.END_ELEMENT))
            throw new IllegalStateException(
                    "Not a start element or an end element");
        return _qName;
    }

    /**
     * Returns the current attributes (SAX2-Like).
     *
     * @return returns the number of attributes.
     * @throws IllegalStateException if not a START_ELEMENT.
     */
    public Attributes getAttributes() {
        if (_eventType != XMLStreamConstants.START_ELEMENT)
            throw new IllegalStateException("Not a start element");
        return _attributes;
    }

    /**
     * Defines a custom entities to replacement text mapping for this reader.
     * For example:[code]
     *     FastMap<String, String> HTML_ENTITIES = new FastMap<String, String>();
     *     HTML_ENTITIES.put("nbsp", " ");
     *     HTML_ENTITIES.put("copy", "©");
     *     HTML_ENTITIES.put("eacute", "é");
     *     ...
     *     XMLStreamReaderImpl reader = new XMLStreamReaderImpl();
     *     reader.setEntities(HTML_ENTITIES);
     * [/code]
     * The entities mapping may be changed dynamically (e.g. 
     * after reading the DTD and all external entities references are resolved).
     * 
     * @param entities the entities to replacement texts mapping 
     *        (both must be <code>CharSequence</code> instances).
     */
    public void setEntities(Map entities) {
        _entities.setEntitiesMapping(entities);
    }

    /**
     * Returns the textual representation of this reader current state.
     * 
     * @return the textual representation of the current state.
     */
    public String toString() {
        return "XMLStreamReader - State: " + NAMES_OF_EVENTS[_eventType]
                + ", Location: " + _location.toString();
    }

    // Implements XMLStreamReader Interface.
    public int next() throws XMLStreamException {

        // Clears previous state.
        if (_eventType == START_ELEMENT) {
            _attributes.reset();
            if (_isEmpty) { // Previous empty tag, generates END_TAG automatically.
                _isEmpty = false;
                return _eventType = END_ELEMENT;
            }
        } else if (_eventType == END_ELEMENT) {
            _namespaces.pop();
            CharArray startElem = _elemStack[_nesting--];
            _start = _index = startElem.offset();
            while (_seqs[--_seqsIndex] != startElem) { // Recycles CharArray instances.
            }
        }
        // Reader loop.
        while (true) {

            // Main character reading block.
            if ((_readIndex >= _readCount) && isEndOfStream())
                return _eventType; // END_DOCUMENT or CHARACTERS.
            char c = _readBuffer[_readIndex++];
            if (c <= '&')
                c = (c == '&') ? replaceEntity()
                        : (c < ' ') ? handleEndOfLine(c) : c;
            _data[_index++] = c;

            // Main processing.
            //
            switch (_state) {

            case STATE_DEFAULT:
                _start = _index - 1; // Resets start position.
                _state = (c == '<') ? STATE_MARKUP : STATE_CHARACTERS;
                break;

            case STATE_CHARACTERS:
                while (true) { // Read characters data all at once.

                    if (c == '<') {
                        int length = _index - _start - 1;
                        if (_charactersPending) {
                            _text.setArray(_data, _text.offset(), _text
                                    .length()
                                    + length); // Coalescing.
                        } else {
                            _text = newSeq(_start, length);
                            _charactersPending = true;
                        }
                        _start = _index - 1; // Keeps '<' as part of markup.
                        _state = STATE_MARKUP;
                        break;

                    }

                    // Local character reading block.
                    if ((_readIndex >= _readCount) && isEndOfStream())
                        return _eventType;
                    c = _readBuffer[_readIndex++];
                    if (c <= '&')
                        c = (c == '&') ? replaceEntity()
                                : (c < ' ') ? handleEndOfLine(c) : c;
                    _data[_index++] = c;
                }
                break;

            case STATE_CDATA:
                while (true) { // Reads CDATA all at once.

                    if ((c == '>') && (_index - _start >= 3)
                            && (_data[_index - 2] == ']')
                            && (_data[_index - 3] == ']')) {
                        _index -= 3;
                        int length = _index - _start;
                        if (length > 0) { // Not empty.
                            if (_charactersPending) {
                                _text.setArray(_data, _text.offset(), _text
                                        .length()
                                        + length); // Coalescing.
                            } else {
                                _text = newSeq(_start, length);
                                _charactersPending = true;
                            }
                        }
                        _state = STATE_DEFAULT;
                        break;
                    }

                    // Local character reading block.
                    if (_readIndex >= _readCount)
                        reloadBuffer();
                    c = _readBuffer[_readIndex++];
                    if (c < ' ')
                        c = handleEndOfLine(c);
                    _data[_index++] = c;
                }
                break;

            case STATE_DTD:
                if (c == '>') {
                    _text = newSeq(_start, _index - _start);
                    _index = _start; // Do not keep DTD.
                    _state = STATE_DEFAULT;
                    return _eventType = DTD;
                } else if (c == '[') {
                    _state = STATE_DTD_INTERNAL;
                }
                break;

            case STATE_DTD_INTERNAL:
                if (c == ']') {
                    _state = STATE_DTD;
                }
                break;

            case STATE_MARKUP: // Starts with '<'
                if (_index - _start == 2) {
                    if (c == '/') {
                        _start = _index = _index - 2;
                        _state = STATE_CLOSE_TAGxREAD_ELEM_NAME;
                        _prefixSep = -1;
                        if (_charactersPending) { // Flush characters event.
                            _charactersPending = false;
                            return _eventType = CHARACTERS;
                        }
                    } else if (c == '?') {
                        _start = _index = _index - 2;
                        _state = STATE_PI;
                        if (_charactersPending) { // Flush characters event.
                            _charactersPending = false;
                            return _eventType = CHARACTERS;
                        }
                    } else if (c != '!') { // Element tag (first letter).
                        _data[_start] = c;
                        _index = _start + 1;
                        _state = STATE_OPEN_TAGxREAD_ELEM_NAME;
                        _prefixSep = -1;
                        if (_charactersPending) { // Flush character event.
                            _charactersPending = false;
                            return _eventType = CHARACTERS;
                        }
                    }
                } else if ((_index - _start == 4) && (_data[_start + 1] == '!')
                        && (_data[_start + 2] == '-')
                        && (_data[_start + 3] == '-')) {
                    _start = _index = _index - 4; // Removes <!--
                    _state = STATE_COMMENT;
                    if (_charactersPending) { // Flush character event.
                        _charactersPending = false;
                        return _eventType = CHARACTERS;
                    }

                } else if ((_index - _start == 9) && (_data[_start + 1] == '!')
                        && (_data[_start + 2] == '[')
                        && (_data[_start + 3] == 'C')
                        && (_data[_start + 4] == 'D')
                        && (_data[_start + 5] == 'A')
                        && (_data[_start + 6] == 'T')
                        && (_data[_start + 7] == 'A')
                        && (_data[_start + 8] == '[')) {
                    _start = _index = _index - 9; // Do not keep <![CDATA[
                    _state = STATE_CDATA;

                } else if ((_index - _start == 9) && (_data[_start + 1] == '!')
                        && (_data[_start + 2] == 'D')
                        && (_data[_start + 3] == 'O')
                        && (_data[_start + 4] == 'C')
                        && (_data[_start + 5] == 'T')
                        && (_data[_start + 6] == 'Y')
                        && (_data[_start + 7] == 'P')
                        && (_data[_start + 8] == 'E')) {
                    // Keeps <!DOCTYPE as part of DTD.
                    _state = STATE_DTD;
                } else {
                    // Ignores, e.g. <!ELEMENT <!ENTITY...
                }
                break;

            case STATE_COMMENT:
                while (true) { // Read comment all at once.

                    if ((c == '>') && (_index - _start >= 3)
                            && (_data[_index - 2] == '-')
                            && (_data[_index - 3] == '-')) {
                        _index -= 3; // Removes -->
                        _text = newSeq(_start, _index - _start);
                        _state = STATE_DEFAULT;
                        _index = _start; // Do not keep comments.
                        return _eventType = COMMENT;
                    }

                    // Local character reading block.
                    if (_readIndex >= _readCount)
                        reloadBuffer();
                    c = _readBuffer[_readIndex++];
                    if (c < ' ')
                        c = handleEndOfLine(c);
                    _data[_index++] = c;
                }

            case STATE_PI:
                if ((c == '>') && (_index - _start >= 2)
                        && (_data[_index - 2] == '?')) {
                    _index -= 2; // Removes ?>
                    _text = newSeq(_start, _index - _start);
                    _state = STATE_DEFAULT;
                    _index = _start; // Do not keep processing instructions.
                    return _eventType = PROCESSING_INSTRUCTION;
                }
                break;

            // OPEN_TAG:
            case STATE_OPEN_TAGxREAD_ELEM_NAME:
                while (true) { // Read element name all at once.

                    if (c < '@') { // Else avoid multiple checks.
                        if (c == '>') {
                            _qName = newSeq(_start, --_index - _start);
                            _state = STATE_DEFAULT;
                            processStartTag();
                            _isEmpty = false;
                            return _eventType = START_ELEMENT;
                        } else if (c == '/') {
                            _qName = newSeq(_start, --_index - _start);
                            _start = _index;
                            _state = STATE_OPEN_TAGxEMPTY_TAG;
                            break;
                        } else if (c == ':') {
                            _prefixSep = _index - 1;
                        } else if (c <= ' ') {
                            _qName = newSeq(_start, --_index - _start);
                            _state = STATE_OPEN_TAGxELEM_NAME_READ;
                            break;
                        }
                    }

                    if (_readIndex >= _readCount)
                        reloadBuffer();
                    c = _data[_index++] = _readBuffer[_readIndex++];
                }
                break;

            case STATE_OPEN_TAGxELEM_NAME_READ:
                if (c == '>') {
                    --_index;
                    _state = STATE_DEFAULT;
                    processStartTag();
                    _isEmpty = false;
                    return _eventType = START_ELEMENT;
                } else if (c == '/') {
                    _state = STATE_OPEN_TAGxEMPTY_TAG;
                } else if (c > ' ') {
                    _start = _index - 1; // Includes current character.
                    _attrPrefixSep = -1;
                    _state = STATE_OPEN_TAGxREAD_ATTR_NAME;
                }
                break;

            case STATE_OPEN_TAGxREAD_ATTR_NAME:
                while (true) { // Read attribute name all at once.

                    if (c < '@') { // Else avoid multiple checks.
                        if (c <= ' ') {
                            _attrQName = newSeq(_start, --_index - _start);
                            _state = STATE_OPEN_TAGxATTR_NAME_READ;
                            break;
                        } else if (c == '=') {
                            _attrQName = newSeq(_start, --_index - _start);
                            _state = STATE_OPEN_TAGxEQUAL_READ;
                            break;
                        } else if (c == ':') {
                            _attrPrefixSep = _index - 1;
                        }
                    }

                    if (_readIndex >= _readCount)
                        reloadBuffer();
                    _data[_index++] = c = _readBuffer[_readIndex++];
                }
                break;

            case STATE_OPEN_TAGxATTR_NAME_READ:
                if (c == '=') {
                    --_index;
                    _state = STATE_OPEN_TAGxEQUAL_READ;
                } else if (c > ' ') {
                    throw new XMLStreamException("'=' expected", _location);
                }
                break;

            case STATE_OPEN_TAGxEQUAL_READ:
                if (c == '\'') {
                    _start = --_index;
                    _state = STATE_OPEN_TAGxREAD_ATTR_VALUE_SIMPLE_QUOTE;
                } else if (c == '\"') {
                    _start = --_index;
                    _state = STATE_OPEN_TAGxREAD_ATTR_VALUE_DOUBLE_QUOTE;
                } else if (c > ' ') {
                    throw new XMLStreamException("Quotes expected", _location);
                }
                break;

            case STATE_OPEN_TAGxREAD_ATTR_VALUE_SIMPLE_QUOTE:
                while (true) { // Read attribute value all at once.

                    if (c == '\'') {
                        _attrValue = newSeq(_start, --_index - _start);
                        processAttribute();
                        _state = STATE_OPEN_TAGxELEM_NAME_READ;
                        break;
                    }

                    // Local character reading block.
                    if (_readIndex >= _readCount)
                        reloadBuffer();
                    c = _readBuffer[_readIndex++];
                    if (c == '&')
                        c = replaceEntity();
                    _data[_index++] = c;
                }
                break;

            case STATE_OPEN_TAGxREAD_ATTR_VALUE_DOUBLE_QUOTE:
                while (true) { // Read attribute value all at once.

                    if (c == '\"') {
                        _attrValue = newSeq(_start, --_index - _start);
                        processAttribute();
                        _state = STATE_OPEN_TAGxELEM_NAME_READ;
                        break;
                    }

                    // Local character reading block.
                    if (_readIndex >= _readCount)
                        reloadBuffer();
                    c = _readBuffer[_readIndex++];
                    if (c == '&')
                        c = replaceEntity();
                    _data[_index++] = c;
                }
                break;

            case STATE_OPEN_TAGxEMPTY_TAG:
                if (c == '>') {
                    --_index;
                    _state = STATE_DEFAULT;
                    processStartTag();
                    _isEmpty = true;
                    return _eventType = START_ELEMENT;
                } else {
                    throw new XMLStreamException("'>' expected", _location);
                }

                // CLOSE_TAG:
            case STATE_CLOSE_TAGxREAD_ELEM_NAME:
                while (true) { // Element name can be read all at once.

                    if (c < '@') { // Else avoid multiple checks.
                        if (c == '>') {
                            _qName = newSeq(_start, --_index - _start);
                            _state = STATE_DEFAULT;
                            processEndTag();
                            return _eventType = END_ELEMENT;
                        } else if (c == ':') {
                            _prefixSep = _index - 1;
                        } else if (c <= ' ') {
                            _qName = newSeq(_start, --_index - _start);
                            _state = STATE_CLOSE_TAGxELEM_NAME_READ;
                            break;
                        }
                    }

                    if (_readIndex >= _readCount)
                        reloadBuffer();
                    c = _data[_index++] = _readBuffer[_readIndex++];
                }
                break;

            case STATE_CLOSE_TAGxELEM_NAME_READ:
                if (c == '>') {
                    --_index;
                    _state = STATE_DEFAULT;
                    processEndTag();
                    return _eventType = END_ELEMENT;
                } else if (c > ' ') {
                    throw new XMLStreamException("'>' expected", _location);
                }
                break;

            default:
                throw new XMLStreamException("State unknown: " + _state,
                        _location);
            }
        }
    }

    // Defines parsing states (keep values close together to avoid lookup).
    private static final int STATE_DEFAULT = 0;

    private static final int STATE_CHARACTERS = 1;

    private static final int STATE_MARKUP = 2;

    private static final int STATE_COMMENT = 3;

    private static final int STATE_PI = 4;

    private static final int STATE_CDATA = 5;

    private static final int STATE_OPEN_TAGxREAD_ELEM_NAME = 6;

    private static final int STATE_OPEN_TAGxELEM_NAME_READ = 7;

    private static final int STATE_OPEN_TAGxREAD_ATTR_NAME = 8;

    private static final int STATE_OPEN_TAGxATTR_NAME_READ = 9;

    private static final int STATE_OPEN_TAGxEQUAL_READ = 10;

    private static final int STATE_OPEN_TAGxREAD_ATTR_VALUE_SIMPLE_QUOTE = 11;

    private static final int STATE_OPEN_TAGxREAD_ATTR_VALUE_DOUBLE_QUOTE = 12;

    private static final int STATE_OPEN_TAGxEMPTY_TAG = 13;

    private static final int STATE_CLOSE_TAGxREAD_ELEM_NAME = 14;

    private static final int STATE_CLOSE_TAGxELEM_NAME_READ = 15;

    private static final int STATE_DTD = 16;

    private static final int STATE_DTD_INTERNAL = 17;

    /**
     * Reloads data buffer.
     * 
     * @param detectEndOfStream indicates 
     * @return <code>true</code> if the buffer has been reloaded;
     *         <code>false</code> if the end of stream has being reached
     *         and the event type (CHARACTERS or END_DOCUMENT) has been set.
     */
    private void reloadBuffer() throws XMLStreamException {
        _location._column += _readIndex;
        _location._charactersRead += _readIndex;
        _readIndex = 0;
        try {
            _readCount = _reader.read(_readBuffer, 0, _readBuffer.length);
            if ((_readCount <= 0)
                    && ((_nesting != 0) || ((_state != STATE_DEFAULT) && (_state != STATE_CHARACTERS))))
                throw new XMLStreamException("Unexpected end of document",
                        _location);
        } catch (IOException e) {
            throw new XMLStreamException(e);
        }
        while ((_index + _readCount) >= _data.length) { // Potential overflow.
            increaseDataBuffer();
        }
    }

    /**
     * Detects end of stream.
     * 
     * @return <code>true</code> if end of stream has being reached
     *         and the event type (CHARACTERS or END_DOCUMENT) has been set;
     *         <code>false</code> otherwise.
     */
    private boolean isEndOfStream() throws XMLStreamException {
        if (_readIndex >= _readCount)
            reloadBuffer();
        if (_readCount <= 0) {
            if (_eventType == END_DOCUMENT)
                throw new XMLStreamException(
                        "End document has already been reached");
            if (_state == STATE_CHARACTERS) { // Flushes trailing characters.
                int length = _index - _start - 1;
                if (_charactersPending) {
                    _text.setArray(_data, _text.offset(), _text.length()
                            + length); // Coalescing.
                } else {
                    _text = newSeq(_start, length);
                }
                _eventType = CHARACTERS;
                _state = STATE_DEFAULT;
            } else {
                _eventType = END_DOCUMENT;
                _state = STATE_DEFAULT;
            }
            return true;
        }
        return false;
    }

    /**
     * Handles end of line as per XML Spec. 2.11
     * 
     * @param c the potential end of line character.
     * @return the replacement character for end of line.
     */
    private char handleEndOfLine(char c) throws XMLStreamException {
        if (c == 0xD) { // Replaces #xD with #xA
            // Unless next char is #xA, then skip,
            // #xD#xA will be replaced by #xA
            if (_readIndex >= _readCount)
                reloadBuffer();
            if ((_readIndex < _readCount) && (_readBuffer[_readIndex] == 0xA))
                _readIndex++; // Skips 0xD
            c = (char) 0xA;
        }
        if (c == 0xA) {
            _location._line++;
            _location._column = -_readIndex; // column = 0
        } else if (c == 0x0) {
            throw new XMLStreamException("Illegal XML character U+0000",
                    _location);
        }
        return c;
    }

    /**
     * Replaces an entity if the current state allows it.
     * 
     * @return the next character after the text replacement or '&' if no
     *         replacement took place.
     */
    private char replaceEntity() throws XMLStreamException {
        if ((_state == STATE_COMMENT) || (_state == STATE_PI)
                || (_state == STATE_CDATA))
            return '&'; // (&2.4)

        int start = _index; // Index of first replacement character.
        _data[_index++] = '&';
        while (true) {
            if (_readIndex >= _readCount)
                reloadBuffer();
            char c = _data[_index++] = _readBuffer[_readIndex++];
            if (c == ';')
                break;
            if (c <= ' ')
                throw new XMLStreamException("';' expected", _location);
        }
        // Ensures that the replacement string holds in the data buffer.
        while (start + _entities.getMaxLength() >= _data.length)
            increaseDataBuffer();

        // Replaces the entity.
        int length = _entities.replaceEntity(_data, start, _index - start);

        // Returns the next character after entity unless ampersand.
        _index = start + length;

        // Local character reading block.
        if (_readIndex >= _readCount)
            reloadBuffer();
        char c = _readBuffer[_readIndex++];
        return (c == '&') ? (c = replaceEntity()) : c;
    }

    /**
     * Processes the attribute just read.
     */
    private void processAttribute() throws XMLStreamException {
        if (_attrPrefixSep < 0) { // No prefix.
            if (isXMLNS(_attrQName)) { // Sets default namespace.
                _namespaces.setPrefix(_namespaces._defaultNsPrefix, _attrValue);
            } else {
                _attributes.addAttribute(_attrQName, null, _attrQName,
                        _attrValue);
            }
        } else { // Prefix.
            final int offset = _attrQName.offset();
            final int length = _attrQName.length();

            CharArray prefix = newSeq(offset, _attrPrefixSep - offset);

            CharArray localName = newSeq(_attrPrefixSep + 1, offset + length
                    - _attrPrefixSep - 1);

            if (isXMLNS(prefix)) { // Namespace association.
                _namespaces.setPrefix(localName, _attrValue);
            } else {
                _attributes.addAttribute(localName, prefix, _attrQName,
                        _attrValue);
            }
        }
    }

    private static boolean isXMLNS(CharArray chars) {
        return (chars.length() == 5) && (chars.charAt(0) == 'x')
                && (chars.charAt(1) == 'm') && (chars.charAt(2) == 'l')
                && (chars.charAt(3) == 'n') && (chars.charAt(4) == 's');
    }

    private void processEndTag() throws XMLStreamException {
        if (!_qName.equals(_elemStack[_nesting]))
            throw new XMLStreamException("Unexpected end tag for " + _qName,
                    _location);
    }

    private void processStartTag() throws XMLStreamException {
        if (++_nesting >= _elemStack.length) {
            increaseStack();
        }
        _elemStack[_nesting] = _qName;
        _namespaces.push();
    }

    // Implements Reusable.
    public void reset() {
        // Resets all members (alphabetically ordered).
        _attributes.reset();
        _attrPrefixSep = 0;
        _attrQName = null;
        _attrValue = null;
        _attrQName = null;
        _charactersPending = false;
        _encoding = null;
        _entities.reset();
        _eventType = START_DOCUMENT;
        _index = 0;
        _isEmpty = false;
        _location.reset();
        _namespaces.reset();
        _objectFactory = null;
        _prolog = null;
        _readCount = 0;
        _reader = null;
        _nesting = 0;
        _readIndex = 0;
        _seqsIndex = 0;
        _start = 0;
        _startOffset = 0;
        _state = STATE_DEFAULT;
        _utf8StreamReader.reset();
    }

    // Returns a new character sequence from the pool.
    private CharArray newSeq(int offset, int length) {
        CharArray seq = (_seqsIndex < _seqsCapacity) ? _seqs[_seqsIndex++]
                : newSeq2();
        return seq.setArray(_data, offset, length);
    }

    private CharArray newSeq2() {
        MemoryArea.getMemoryArea(this).executeInArea(_createSeqLogic);
        return _seqs[_seqsIndex++];
    }

    private final Runnable _createSeqLogic = new Runnable() {
        public void run() {
            if (_seqsCapacity >= _seqs.length) { // Resizes.
                CharArray[] tmp = new CharArray[_seqs.length * 2];
                System.arraycopy(_seqs, 0, tmp, 0, _seqs.length);
                _seqs = tmp;
            }
            CharArray seq = new CharArray();
            _seqs[_seqsCapacity++] = seq;
        }
    };

    private CharArray[] _seqs = new CharArray[256];

    private int _seqsIndex;

    private int _seqsCapacity;

    // Increases internal data buffer capacity.
    private void increaseDataBuffer() {
        // Note: The character data at any nesting level is discarded 
        //       only when moving to outer nesting level (due to coalescing).
        //       This accumulation may cause resize of the data buffer if
        //       numerous elements at the same nesting level are separated by 
        //       spaces or indentation.
        MemoryArea.getMemoryArea(this).executeInArea(new Runnable() {
            public void run() {
                char[] tmp = new char[_data.length * 2];
                javolution.context.LogContext.info(new CharArray(
                        "XMLStreamReaderImpl: Data buffer increased to "
                                + tmp.length));
                System.arraycopy(_data, 0, tmp, 0, _data.length);
                _data = tmp;
            }
        });
    }

    // Increases statck.
    private void increaseStack() {
        MemoryArea.getMemoryArea(this).executeInArea(new Runnable() {
            public void run() {
                CharArray[] tmp = new CharArray[_elemStack.length * 2];
                javolution.context.LogContext.info(new CharArray(
                        "XMLStreamReaderImpl: CharArray stack increased to "
                                + tmp.length));
                System.arraycopy(_elemStack, 0, tmp, 0, _elemStack.length);
                _elemStack = tmp;
            }
        });
    }

    /**
     * This inner class represents the parser location.
     */
    private final class LocationImpl implements Location, Reusable {

        int _column;

        int _line;

        int _charactersRead;

        public int getLineNumber() {
            return _line + 1;
        }

        public int getColumnNumber() {
            return _column + _readIndex;
        }

        public int getCharacterOffset() {
            return _charactersRead + _readIndex;
        }

        public String getPublicId() {
            return null; // Not available.
        }

        public String getSystemId() {
            return null; // Not available.
        }

        public String toString() {
            return "Line " + getLineNumber() + ", Column " + getColumnNumber();
        }

        public void reset() {
            _line = 0;
            _column = 0;
            _charactersRead = 0;
        }
    }

    //////////////////////////////////////////
    // Implements XMLStreamReader Interface //
    //////////////////////////////////////////

    // Implements XMLStreamReader Interface.
    public void require(int type, CharSequence namespaceURI,
            CharSequence localName) throws XMLStreamException {
        if (_eventType != type)
            throw new XMLStreamException("Expected event: "
                    + NAMES_OF_EVENTS[type] + ", found event: "
                    + NAMES_OF_EVENTS[_eventType]);
        if ((namespaceURI != null) && !getNamespaceURI().equals(namespaceURI))
            throw new XMLStreamException("Expected namespace URI: "
                    + namespaceURI + ", found: " + getNamespaceURI());
        if ((localName != null) && !getLocalName().equals(localName))
            throw new XMLStreamException("Expected local name: " + localName
                    + ", found: " + getLocalName());
    }

    // Implements XMLStreamReader Interface.
    public CharArray getElementText() throws XMLStreamException {
        // Derived from interface specification code.
        if (getEventType() != XMLStreamConstants.START_ELEMENT) {
            throw new XMLStreamException(
                    "Parser must be on START_ELEMENT to read next text",
                    getLocation());
        }
        CharArray text = null;
        int eventType = next();
        while (eventType != XMLStreamConstants.END_ELEMENT) {
            if (eventType == XMLStreamConstants.CHARACTERS) {
                if (text == null) {
                    text = getText();
                } else { // Merge (adjacent text, comments and PI are not kept).
                    text.setArray(_data, text.offset(), text.length()
                            + getText().length());
                }
            } else if (eventType == XMLStreamConstants.PROCESSING_INSTRUCTION
                    || eventType == XMLStreamConstants.COMMENT) {
                // Skips (not kept).
            } else if (eventType == XMLStreamConstants.END_DOCUMENT) {
                throw new XMLStreamException(
                        "Unexpected end of document when reading element text content",
                        getLocation());
            } else if (eventType == XMLStreamConstants.START_ELEMENT) {
                throw new XMLStreamException(
                        "Element text content may not contain START_ELEMENT",
                        getLocation());
            } else {
                throw new XMLStreamException("Unexpected event type "
                        + NAMES_OF_EVENTS[eventType], getLocation());
            }
            eventType = next();
        }
        return (text != null) ? text : newSeq(0, 0);
    }

    // Implements XMLStreamReader Interface.
    public Object getProperty(String name) throws IllegalArgumentException {
        if (name.equals(XMLInputFactory.IS_COALESCING)) {
            return new Boolean(true);
        } else if (name.equals(XMLInputFactory.ENTITIES)) {
            return _entities.getEntitiesMapping();
        } else {
            throw new IllegalArgumentException("Property: " + name
                    + " not supported");
        }
    }

    // Implements XMLStreamReader Interface.
    public void close() throws XMLStreamException {
        if (_objectFactory != null) {
            _objectFactory.recycle(this); // Automatic reset.
        } else {
            reset();
        }
    }

    public int getAttributeCount() {
        if (_eventType != XMLStreamConstants.START_ELEMENT)
            throw illegalState("Not a start element");
        return _attributes.getLength();
    }

    public CharArray getAttributeLocalName(int index) {
        if (_eventType != XMLStreamConstants.START_ELEMENT)
            throw illegalState("Not a start element");
        return _attributes.getLocalName(index);
    }

    public CharArray getAttributeNamespace(int index) {
        if (_eventType != XMLStreamConstants.START_ELEMENT)
            throw illegalState("Not a start element");
        CharArray prefix = _attributes.getPrefix(index);
        return (prefix == null) ? null : _namespaces.getNamespaceURI(prefix);
    }

    public CharArray getAttributePrefix(int index) {
        if (_eventType != XMLStreamConstants.START_ELEMENT)
            throw illegalState("Not a start element");
        return _attributes.getPrefix(index);
    }

    public CharArray getAttributeType(int index) {
        if (_eventType != XMLStreamConstants.START_ELEMENT)
            throw illegalState("Not a start element");
        return _attributes.getType(index);
    }

    public CharArray getAttributeValue(CharSequence uri, CharSequence localName) {
        if (_eventType != XMLStreamConstants.START_ELEMENT)
            throw illegalState("Not a start element");
        return (uri == null) ? _attributes.getValue(localName) : _attributes
                .getValue(uri, localName);
    }

    public CharArray getAttributeValue(int index) {
        if (_eventType != XMLStreamConstants.START_ELEMENT)
            throw illegalState("Not a start element");
        return _attributes.getValue(index);
    }

    public CharArray getCharacterEncodingScheme() {
        return readPrologAttribute(ENCODING);
    }

    private static final CharArray ENCODING = new CharArray("encoding");

    public String getEncoding() {
        return _encoding;
    }

    public int getEventType() {
        return _eventType;
    }

    public CharArray getLocalName() {
        if ((_eventType != XMLStreamConstants.START_ELEMENT)
                && (_eventType != XMLStreamConstants.END_ELEMENT))
            throw illegalState("Not a start or end element");
        if (_prefixSep < 0)
            return _qName;
        CharArray localName = newSeq(_prefixSep + 1, _qName.offset()
                + _qName.length() - _prefixSep - 1);
        return localName;
    }

    public Location getLocation() {
        return _location;
    }

    public int getNamespaceCount() {
        if ((_eventType != XMLStreamConstants.START_ELEMENT)
                && (_eventType != XMLStreamConstants.END_ELEMENT))
            throw illegalState("Not a start or end element");
        return _namespaces._namespacesCount[_nesting];
    }

    public CharArray getNamespacePrefix(int index) {
        if ((_eventType != XMLStreamConstants.START_ELEMENT)
                && (_eventType != XMLStreamConstants.END_ELEMENT))
            throw illegalState("Not a start or end element");
        return _namespaces._prefixes[index];
    }

    public CharArray getNamespaceURI(CharSequence prefix) {
        if ((_eventType != XMLStreamConstants.START_ELEMENT)
                && (_eventType != XMLStreamConstants.END_ELEMENT))
            throw illegalState("Not a start or end element");
        return _namespaces.getNamespaceURI(prefix);
    }

    public CharArray getNamespaceURI(int index) {
        if ((_eventType != XMLStreamConstants.START_ELEMENT)
                && (_eventType != XMLStreamConstants.END_ELEMENT))
            throw illegalState("Not a start or end element");
        return _namespaces._namespaces[index];
    }

    public NamespaceContext getNamespaceContext() {
        return _namespaces;
    }

    public CharArray getNamespaceURI() {
        if ((_eventType != XMLStreamConstants.START_ELEMENT)
                && (_eventType != XMLStreamConstants.END_ELEMENT))
            throw illegalState("Not a start or end element");
        CharArray prefix = getPrefix();
        return (prefix != null) ? getNamespaceURI(prefix) : null;
    }

    public CharArray getPrefix() {
        if ((_eventType != XMLStreamConstants.START_ELEMENT)
                && (_eventType != XMLStreamConstants.END_ELEMENT))
            throw illegalState("Not a start or end element");
        if (_prefixSep < 0)
            return null;
        int offset = _qName.offset();
        CharArray prefix = newSeq(offset, _prefixSep - offset);
        return prefix;
    }

    public CharArray getPIData() {
        if (_eventType != XMLStreamConstants.PROCESSING_INSTRUCTION)
            throw illegalState("Not a processing instruction");
        int offset = _text.offsetOf(' ') + 1;
        CharArray piData = newSeq(offset, _text.length() - offset);
        return piData;
    }

    public CharArray getPITarget() {
        if (_eventType != XMLStreamConstants.PROCESSING_INSTRUCTION)
            throw illegalState("Not a processing instruction");
        CharArray piTarget = newSeq(_text.offset(), _text.offsetOf(' '));
        return piTarget;
    }

    public CharArray getText() {
        if ((_eventType != XMLStreamConstants.CHARACTERS)
                && (_eventType != XMLStreamConstants.COMMENT)
                && (_eventType != XMLStreamConstants.DTD))
            throw illegalState("Not a text event");
        return _text;
    }

    public char[] getTextCharacters() {
        return getText().array();
    }

    public int getTextCharacters(int sourceStart, char[] target,
            int targetStart, int length) throws XMLStreamException {
        CharArray text = getText();
        int copyLength = Math.min(length, text.length());
        System.arraycopy(text.array(), sourceStart + text.offset(), target,
                targetStart, copyLength);
        return copyLength;
    }

    public int getTextLength() {
        return getText().length();
    }

    public int getTextStart() {
        return getText().offset();
    }

    public CharArray getVersion() {
        return readPrologAttribute(VERSION);
    }

    private static final CharArray VERSION = new CharArray("version");

    public boolean isStandalone() {
        CharArray standalone = readPrologAttribute(STANDALONE);
        return (standalone != null) ? standalone.equals("no") : true;
    }

    public boolean standaloneSet() {
        return readPrologAttribute(STANDALONE) != null;
    }

    private static final CharArray STANDALONE = new CharArray("standalone");

    public boolean hasName() {
        return (_eventType == XMLStreamConstants.START_ELEMENT)
                || (_eventType == XMLStreamConstants.END_ELEMENT);
    }

    public boolean hasNext() throws XMLStreamException {
        return _eventType != XMLStreamConstants.END_DOCUMENT;
    }

    public boolean hasText() {
        return ((_eventType == XMLStreamConstants.CHARACTERS)
                || (_eventType == XMLStreamConstants.COMMENT) || (_eventType == XMLStreamConstants.DTD))
                && (_text.length() > 0);
    }

    public boolean isAttributeSpecified(int index) {
        if (_eventType != XMLStreamConstants.START_ELEMENT)
            throw new IllegalStateException("Not a start element");
        return _attributes.getValue(index) != null;
    }

    public boolean isCharacters() {
        return _eventType == XMLStreamConstants.CHARACTERS;
    }

    public boolean isEndElement() {
        return _eventType == XMLStreamConstants.END_ELEMENT;
    }

    public boolean isStartElement() {
        return _eventType == XMLStreamConstants.START_ELEMENT;
    }

    public boolean isWhiteSpace() {
        if (isCharacters()) {
            char[] chars = _text.array();
            for (int i = _text.offset(), end = _text.offset() + _text.length(); i < end;) {
                if (!isWhiteSpace(chars[i++]))
                    return false;
            }
            return true;
        }
        return false;
    }

    // Whitespaces according to XML 1.1 Specification.
    private static boolean isWhiteSpace(char c) {
        return (c == 0x20) || (c == 0x9) || (c == 0xD) || (c == 0xA);
    }

    public int nextTag() throws XMLStreamException {
        int eventType = next();
        while (eventType == XMLStreamConstants.COMMENT
                || eventType == XMLStreamConstants.PROCESSING_INSTRUCTION
                || (eventType == XMLStreamConstants.CHARACTERS && isWhiteSpace())) {
            eventType = next();
        }
        if (eventType != XMLStreamConstants.START_ELEMENT
                && eventType != XMLStreamConstants.END_ELEMENT)
            throw new XMLStreamException("Tag expected (but found "
                    + NAMES_OF_EVENTS[_eventType] + ")");
        return eventType;
    }

    private IllegalStateException illegalState(String msg) {
        return new IllegalStateException(msg + " ("
                + NAMES_OF_EVENTS[_eventType] + ")");
    }

    private String detectEncoding(InputStream input) throws XMLStreamException {
        // Autodetect encoding (see http://en.wikipedia.org/wiki/UTF-16)
        int byte0;
        try {
            byte0 = input.read();
        } catch (IOException e) {
            throw new XMLStreamException(e);
        }
        if (byte0 == -1)
            throw new XMLStreamException("Premature End-Of-File");
        if (byte0 == '<') { // UTF-8 or compatible encoding.
            _readBuffer[_startOffset++] = '<';
            return "UTF-8";
        } else {
            int byte1;
            try {
                byte1 = input.read();
            } catch (IOException e) {
                throw new XMLStreamException(e);
            }
            if (byte1 == -1)
                throw new XMLStreamException("Premature End-Of-File");
            if ((byte0 == 0) && (byte1 == '<')) { // UTF-16 BIG ENDIAN
                _readBuffer[_startOffset++] = '<';
                return "UTF-16BE";
            } else if ((byte0 == '<') && (byte1 == 0)) { // UTF-16 LITTLE ENDIAN
                _readBuffer[_startOffset++] = '<';
                return "UTF-16LE";
            } else if ((byte0 == 0xFF) && (byte1 == 0xFE)) { // BOM for UTF-16 LITTLE ENDIAN
                return "UTF-16";
            } else if ((byte0 == 0xFE) && (byte1 == 0xFF)) { // BOM for UTF-16 BIG ENDIAN
                return "UTF-16";
            } else { // Encoding unknown (or no prolog) assumes UTF-8
                _readBuffer[_startOffset++] = (char) byte0;
                _readBuffer[_startOffset++] = (char) byte1;
                return "UTF-8";
            }
        }
    }

    private final CharArray readPrologAttribute(CharSequence name) {
        if (_prolog == null)
            return null;
        final int READ_EQUAL = 0;
        final int READ_QUOTE = 1;
        final int VALUE_SIMPLE_QUOTE = 2;
        final int VALUE_DOUBLE_QUOTE = 3;

        int i = _prolog.offsetOf(name);
        if (i >= 0) {
            int maxIndex = _prolog.offset() + _prolog.length();
            i += name.length();
            int state = READ_EQUAL;
            int valueOffset = 0;
            while (i < maxIndex) {
                char c = _prolog.array()[i++];
                switch (state) {
                case READ_EQUAL:
                    if (c == '=') {
                        state = READ_QUOTE;
                    }
                    break;
                case READ_QUOTE:
                    if (c == '"') {
                        state = VALUE_DOUBLE_QUOTE;
                        valueOffset = i;
                    } else if (c == '\'') {
                        state = VALUE_SIMPLE_QUOTE;
                        valueOffset = i;
                    }
                    break;
                case VALUE_SIMPLE_QUOTE:
                    if (c == '\'')
                        return newSeq(valueOffset, i - valueOffset - 1);
                    break;
                case VALUE_DOUBLE_QUOTE:
                    if (c == '"')
                        return newSeq(valueOffset, i - valueOffset - 1);
                    break;
                }
            }
        }
        return null;
    }
}