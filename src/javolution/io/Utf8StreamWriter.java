/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2004 - The Javolution Team (http://javolution.org/)
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.io;
import java.io.CharConversionException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

/**
 * <p> This class represents an UTF-8 stream writer.</p>
 *
 * <p> This writer supports surrogate <code>char</code> pairs (representing
 *     characters in the range [U+10000 .. U+10FFFF]). It can also be used
 *     to write characters from their unicodes (31 bits) directly
 *     (ref. {@link #write(int)}).</p>
 *
 * <p> Instances of this class can be reused for different output streams
 *     and can be part of a higher level component (e.g. serializer) in order
 *     to avoid dynamic buffer allocation when the destination output changes.
 *     Also wrapping using a <code>java.io.BufferedWriter</code> is unnescessary
 *     as instances of this class embed their own data buffers.</p>
 * 
 * <p> Note: This writer is unsynchronized and always produces well-formed
 *           UTF-8 sequences.</p>
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 1.0, October 4, 2004
 * @see     Utf8StreamReader
 */
public final class Utf8StreamWriter extends Writer {

    /**
     * Holds the current output stream or <code>null</code> if closed.
     */
    private OutputStream _outStream;

    /**
     * Holds the bytes' buffer.
     */
    private final byte[] _bytes;

    /**
     * Holds the bytes buffer index.
     */
    private int _index;

    /**
     * Default constructor.
     */
    public Utf8StreamWriter() {
        this(2048);
    }

    /**
     * Creates a {@link Utf8StreamWriter} of specified buffer size.
     *
     * @param  bufferSize the buffer size in bytes.
     */
    public Utf8StreamWriter(int bufferSize) {
         _bytes = new byte[bufferSize];
    }

    /**
     * Sets the output stream to use for writing until this writer is closed.
     * For example:<pre>
     *     Writer writer = new Utf8StreamWriter().setOutputStream(outStream);
     * </pre> is equivalent but writes faster than <pre>
     *     Writer writer = new java.io.OutputStreamWriter(outStream, "UTF-8");
     * </pre>
     *
     * @param  outStream the output stream.
     * @return this UTF-8 writer.
     * @see    #close
     */
    public Utf8StreamWriter setOutputStream(OutputStream outStream) {
        _outStream = outStream;
        return this;
    }

    /**
     * Writes a single character. This method supports 16-bits
     * character surrogates.
     *
     * @param  c <code>char</code> the character to be written (possibly
     *        a surrogate).
     * @throws IOException if an I/O error occurs.
     */
    public void write(char c) throws IOException {
        if ((c < 0xd800) || (c > 0xdfff)) {
            write((int)c);
        } else if (c < 0xdc00) { // High surrogate.
            _highSurrogate = c;
        } else { // Low surrogate.
            int code = ((_highSurrogate - 0xd800) << 10) +
                (c - 0xdc00) + 0x10000;
            write(code);
        }
    }
    private char _highSurrogate;

    /**
     * Writes a character given its 31-bits Unicode.
     *
     * @param  code the 31 bits Unicode of the character to be written.
     * @throws IOException if an I/O error occurs.
     */
    public void write(int code) throws IOException {
        if ((code & 0xffffff80) == 0) {
            _bytes[_index] = (byte) code;
            if (++_index >= _bytes.length) {
                flushBuffer();
            }
        } else { // Writes more than one byte.
            write2(code);
        }
    }
    private void write2(int c) throws IOException {
        if ((c & 0xfffff800) == 0) { // 2 bytes.
            _bytes[_index] = (byte) (0xc0 | (c >> 6));
            if (++_index >= _bytes.length) {
                flushBuffer();
            }
            _bytes[_index] = (byte) (0x80 | (c & 0x3f));
            if (++_index >= _bytes.length) {
                flushBuffer();
            }
        } else if ((c & 0xffff0000) == 0) { // 3 bytes.
            _bytes[_index] = (byte) (0xe0 | (c >> 12));
            if (++_index >= _bytes.length) {
                flushBuffer();
            }
            _bytes[_index] = (byte) (0x80 | ((c >> 6) & 0x3f));
            if (++_index >= _bytes.length) {
                flushBuffer();
            }
            _bytes[_index] = (byte) (0x80 | (c & 0x3f));
            if (++_index >= _bytes.length) {
                flushBuffer();
            }
        } else if ((c & 0xff200000) == 0) { // 4 bytes.
            _bytes[_index] = (byte) (0xf0 | (c >> 18));
            if (++_index >= _bytes.length) {
                flushBuffer();
            }
            _bytes[_index] = (byte) (0x80 | ((c >> 12) & 0x3f));
            if (++_index >= _bytes.length) {
                flushBuffer();
            }
            _bytes[_index] = (byte) (0x80 | ((c >> 6) & 0x3f));
            if (++_index >= _bytes.length) {
                flushBuffer();
            }
            _bytes[_index] = (byte) (0x80 | (c & 0x3f));
            if (++_index >= _bytes.length) {
                flushBuffer();
            }
        } else if ((c & 0xf4000000) == 0) { // 5 bytes.
            _bytes[_index] = (byte) (0xf8 | (c >> 24));
            if (++_index >= _bytes.length) {
                flushBuffer();
            }
            _bytes[_index] = (byte) (0x80 | ((c >> 18) & 0x3f));
            if (++_index >= _bytes.length) {
                flushBuffer();
            }
            _bytes[_index] = (byte) (0x80 | ((c >> 12) & 0x3f));
            if (++_index >= _bytes.length) {
                flushBuffer();
            }
            _bytes[_index] = (byte) (0x80 | ((c >> 6) & 0x3f));
            if (++_index >= _bytes.length) {
                flushBuffer();
            }
            _bytes[_index] = (byte) (0x80 | (c & 0x3f));
            if (++_index >= _bytes.length) {
                flushBuffer();
            }
        } else if ((c & 0x80000000) == 0) { // 6 bytes.
            _bytes[_index] = (byte) (0xfc | (c >> 30));
            if (++_index >= _bytes.length) {
                flushBuffer();
            }
            _bytes[_index] = (byte) (0x80 | ((c >> 24) & 0x3f));
            if (++_index >= _bytes.length) {
                flushBuffer();
            }
            _bytes[_index] = (byte) (0x80 | ((c >> 18) & 0x3f));
            if (++_index >= _bytes.length) {
                flushBuffer();
            }
            _bytes[_index] = (byte) (0x80 | ((c >> 12) & 0x3F));
            if (++_index >= _bytes.length) {
                flushBuffer();
            }
            _bytes[_index] = (byte) (0x80 | ((c >> 6) & 0x3F));
            if (++_index >= _bytes.length) {
                flushBuffer();
            }
            _bytes[_index] = (byte) (0x80 | (c & 0x3F));
            if (++_index >= _bytes.length) {
                flushBuffer();
            }
        } else {
            throw new CharConversionException(
                "Illegal character U+" + Integer.toHexString(c));
        }
    }

    /**
     * Writes a portion of an array of characters.
     *
     * @param  cbuf the array of characters.
     * @param  off the offset from which to start writing characters.
     * @param  len the number of characters to write.
     * @throws IOException if an I/O error occurs.
     */
    public void write(char cbuf[], int off, int len) throws IOException {
        final int off_plus_len = off + len;
        for (int i=off; i < off_plus_len;) {
            char c = cbuf[i++];
            if (c < 0x80) {
                _bytes[_index] = (byte) c;
                if (++_index >= _bytes.length) {
                    flushBuffer();
                }
            } else {
                write(c);
            }
        }
    }

    /**
     * Writes a portion of a string.
     *
     * @param  str a String.
     * @param  off the offset from which to start writing characters.
     * @param  len the number of characters to write.
     * @throws IOException if an I/O error occurs
     */
    public void write(String str, int off, int len) throws IOException {
        final int off_plus_len = off + len;
        for (int i=off; i < off_plus_len;) {
            char c = str.charAt(i++);
            if (c < 0x80) {
                _bytes[_index] = (byte) c;
                if (++_index >= _bytes.length) {
                    flushBuffer();
                }
            } else {
                write(c);
            }
        }
    }

    /**
     * Writes the specified character sequence.
     *
     * @param  csq the character sequence.
     * @throws IOException if an I/O error occurs
     */
    public void write(CharSequence csq) throws IOException {
        final int length = csq.length();
        for (int i=0; i < length; ) {
            char c = csq.charAt(i++);
            if (c < 0x80) {
                _bytes[_index] = (byte) c;
                if (++_index >= _bytes.length) {
                    flushBuffer();
                }
            } else {
                write(c);
            }
        }
    }

    /**
     * Flushes the stream.  If the stream has saved any characters from the
     * various write() methods in a buffer, write them immediately to their
     * intended destination.  Then, if that destination is another character or
     * byte stream, flush it.  Thus one flush() invocation will flush all the
     * buffers in a chain of Writers and OutputStreams.
     *
     * @throws IOException if an I/O error occurs.
     */
    public void flush() throws IOException {
        flushBuffer();
        _outStream.flush();
    }

    /**
     * Closes the stream, flushing it first.  Once a stream has been closed,
     * further write() or flush() invocations will cause an IOException to be
     * thrown.  Closing a previously-closed stream, however, has no effect.
     *
     * @exception  IOException  If an I/O error occurs
     */
    public void close() throws IOException {
        if (_outStream != null) {
            flushBuffer();
            _outStream.close();
            _outStream = null;
        }
    }

    /**
     * Flushes the internal bytes buffer.
     *
     * @throws IOException if an I/O error occurs
     */
    private void flushBuffer() throws IOException {
        if (_outStream != null) {
            _outStream.write(_bytes, 0, _index);
            _index = 0;
        } else {
            throw new IOException("Stream closed");
        }
    }

}