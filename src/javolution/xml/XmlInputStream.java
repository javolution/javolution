/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2005 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.xml;

import j2me.lang.IllegalStateException;
import j2me.io.CharConversionException;
import javolution.lang.Reusable;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

/**
 * <p> This class represents an object input stream using {@link ObjectReader}
 *     for object deserialization.</p>
 *     
 * <p> Instances of this class embed their own data buffer, wrapping using 
 *     a <code>java.io.BufferedInputStream</code> is therefore unnescessary.</p>
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 2.2, January 8, 2005
 * @see     XmlOutputStream
 */
public class XmlInputStream extends InputStream implements Reusable {

    /**
     * Holds the object reader.
     */
    private final ObjectReader _objectReader = new ObjectReader();

    /**
     * Holds the xml reader used for parsing.
     */
    private final XmlReader _xmlReader = new XmlReader();

    /**
     * Default constructor.
     */
    public XmlInputStream() {
    }
    
    /**
     * Sets the underlying input source for this stream.
     * 
     * @param in the input source.
     * @return <code>this</code> 
     * @throws IllegalStateException if this stream is being reused and 
     *         it has not been {@link #close closed} or {@link #reset reset}.
     */
    public XmlInputStream setInputStream(InputStream in) {
        if (_xmlReader._inputStream != null)
            throw new IllegalStateException("Stream not closed or reset");
        _xmlReader._inputStream = in;
        return this;
    }

    /**
     * Reads an object from the underlying stream using an {@link ObjectReader}.
     * 
     * @return the object read from its xml representation. 
     * @throws IOException if an I/O error occurs.
     */
    public Object readObject() throws IOException {
        try {
            return _objectReader.read(_xmlReader);
        } finally {
           _xmlReader.resume();
        }
    }

    /**
     * Closes and {@link #reset resets} this stream for reuse.
     *
     * @throws IOException if an I/O error occurs.
     */
    public void close() throws IOException {
        if (_xmlReader._inputStream != null) {
            _xmlReader._inputStream.close();
            reset();
        }
    }
    
    /**
     * Reads the next byte of data from the input stream.
     * 
     * @return the next byte of data, or -1 if the end of the stream is reached.
     * @throws IOException if an I/O error occurs.
     */
    public int read() throws IOException {
        if (_xmlReader._start < _xmlReader._end) {
            return _xmlReader._bytes[_xmlReader._start++];
        } else { // Use the reader buffer.
            return _xmlReader.fillBuffer() ?
                 _xmlReader._bytes[_xmlReader._start++] : -1;
        }
    }   
    
    /**
     * Reads up to len bytes of data from the input stream into an array of 
     * bytes.
     * 
     * @param b the buffer into which the data is read.
     * @param off the start offset in array b at which the data is written.
     * @param len the maximum number of bytes to read. 
     * @return  the total number of bytes read into the buffer, or -1 if there 
     *          is no more data because the end of the stream has been reached.
     * @throws IOException if an I/O error occurs.
     */
    public int read(byte b[], int off, int len) throws IOException {
        int rem = _xmlReader._end - _xmlReader._start;
        if (rem == 0) { // Buffer empty, go straight to the source.
            return _xmlReader._inputStream.read(b, off, len);
        }
        // Returns bytes from buffer.
        int count = (len < rem) ? len : rem;
        System.arraycopy(_xmlReader._bytes, _xmlReader._start, b, off, count);
        _xmlReader._start += count;
        return count;
    }
    
    // Implements Reusable interface.
    public void reset() {
        _objectReader.reset();
        _xmlReader.reset();
    }
    
    /**
     * This inner class represents a custom reader for reading the xml 
     * data when parsing. It stops reading when the 
     * {@link XmlOutputStream#END_XML} byte is encountered, it then returns
     * the code <code>-1</code> (end of stream) until {@link #resume resumed}. 
     */
    private static final class XmlReader extends Reader implements Reusable {
        private InputStream _inputStream;
        private int _code;
        private int _moreBytes;
        private int _start;
        private int _end;
        private final byte[] _bytes = new byte[4096];
        private boolean _isHalted;
        

        /**
         * Resumes reading after an {@link XmlOutputStream#END_UTF8} byte 
         * is encountered. 
         */
        public void resume() {
            _isHalted = false;
        }
        
        /**
         * Fills buffer.
         * 
         * @return <code>true</code> if at least one more byte has been read;
         *         <code>false</code> otherwise.
         * @throws IOException if an I/O error occurs.
         */
        public boolean fillBuffer() throws IOException {
            if (_inputStream == null) throw new IOException("Stream closed");
            _start = 0;
            _end = _inputStream.read(_bytes, 0, _bytes.length);
            return _end > 0;
        }
        
        // Implements abstract method.
        public int read(char[] cbuf, int off, int len) throws IOException {
            if (_isHalted) {
                return -1;
            } else if (_start >= _end) { 
                if (!fillBuffer()) {
                    return - 1;
                }
            }
            final int off_plus_len = off + len;
            for (int i = off; i < off_plus_len;) {
                // assert(_start < _end)
                byte b = _bytes[_start];
                if ((b >= 0) && (++_start < _end)) {
                    cbuf[i++] = (char) b; // Most common case.
                } else if (b < 0) {
                    if (b == XmlOutputStream.END_XML) {
                        ++_start;
                        _isHalted = true;
                        return i - off;
                    } else if (i < off_plus_len - 1) { // Up to two 'char' can be read.
                        int code = read2();
                        if (code < 0x10000) {
                            cbuf[i++] = (char) code;
                        } else if (code <= 0x10ffff) { // Surrogates.
                            cbuf[i++] = (char) (((code - 0x10000) >> 10) + 0xd800);
                            cbuf[i++] = (char) (((code - 0x10000) & 0x3ff) + 0xdc00);
                        } else {
                            throw new CharConversionException(
                                    "Cannot convert U+"
                                            + Integer.toHexString(code)
                                            + " to char (code greater than U+10FFFF)");
                        }
                        if (_start < _end) {
                            continue;
                        }
                    }
                    return i - off;
                } else { // End of buffer (_start >= _end).
                    cbuf[i++] = (char) b;
                    return i - off;
                }
            }
            return len;
        }

        // Implements abstract method.
        public void close() throws IOException {
            // Do nothing (we don't want the object reader to close
            // the input stream).
        }

        // Reads one full character, blocks if necessary.
        private int read2() throws IOException {
            if (_start < _end) {
                byte b = _bytes[_start++];

                // Decodes UTF-8.
                if ((b >= 0) && (_moreBytes == 0)) {
                    // 0xxxxxxx
                    return b;
                } else if (((b & 0xc0) == 0x80) && (_moreBytes != 0)) {
                    // 10xxxxxx (continuation byte)
                    _code = (_code << 6) | (b & 0x3f); // Adds 6 bits to code.
                    if (--_moreBytes == 0) {
                        return _code;
                    } else {
                        return read2();
                    }
                } else if (((b & 0xe0) == 0xc0) && (_moreBytes == 0)) {
                    // 110xxxxx
                    _code = b & 0x1f;
                    _moreBytes = 1;
                    return read2();
                } else if (((b & 0xf0) == 0xe0) && (_moreBytes == 0)) {
                    // 1110xxxx
                    _code = b & 0x0f;
                    _moreBytes = 2;
                    return read2();
                } else if (((b & 0xf8) == 0xf0) && (_moreBytes == 0)) {
                    // 11110xxx
                    _code = b & 0x07;
                    _moreBytes = 3;
                    return read2();
                } else {
                    throw new CharConversionException("Invalid UTF-8 Encoding");
                }
            } else { // No more bytes in buffer.
                if (fillBuffer()) {
                    return read2(); // Continues.
                } else { // Done.
                    if (_moreBytes == 0) {
                        return -1;
                    } else { // Incomplete sequence.
                        throw new CharConversionException(
                                "Unexpected end of stream");
                    }
                }
            }
        }

        // Implements Reusable interface.
        public void reset() {
            _code = 0;
            _end = 0;
            _inputStream = null;
            _moreBytes = 0;
            _start = 0;
        }
    }
}