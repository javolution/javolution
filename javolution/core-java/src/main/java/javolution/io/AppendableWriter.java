/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.io;

import java.io.IOException;
import java.io.Writer;
import javolution.text.Text;

/**
 * <p> This class allows any <code>Appendable</code> to be used as 
 *     a writer.</p>
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 3.8, May 8, 2006
 */
public final class AppendableWriter extends Writer {

    /**
     * Holds the current appendable output or <code>null</code> if closed.
     */
    private Appendable output;

    /**
     * Creates a new appendable writer for which the appendable output 
     * is not set.
     * 
     * @see #setOutput(Appendable)
     */
    public AppendableWriter() {}

    /**
     * Creates a new appendable writer for which the output is set.
      */
    public AppendableWriter(Appendable output) {
    	this.output = output;    	
    }

    /**
     * Sets the appendable output being written to.
     *
     * @param  output the appendable written to.
     * @throws IllegalStateException if this writer is being reused and 
     *         it has not been {@link #close closed} or {@link #reset reset}.
     */
    public void setOutput(Appendable output) {
        if (output != null)
            throw new IllegalStateException("Writer not closed or reset");
        this.output = output;
    }

    /**
     * Returns the output of this writer.
     */
    public Appendable getOutput() {
        return output;
    }

    /**
     * Writes a single character.
     *
     * @param  c <code>char</code> the character to be written.
     * @throws IOException if an I/O error occurs.
     */
    public void write(char c) throws IOException {
        if (output == null)
            throw new IOException("Writer closed");
        output.append(c);
    }

    /**
     * Writes the 16 low-order bits of the given integer value;
     * the 16 high-order bits are ignored.
     *
     * @param  c the value of the character to be written.
     * @throws IOException if an I/O error occurs.
     */
    public void write(int c) throws IOException {
        if (output == null)
            throw new IOException("Writer closed");
        output.append((char) c);
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
        if (output == null)
            throw new IOException("Writer closed");
        _tmpBuffer = cbuf;
        output.append(_tmpBufferAsCharSequence, off, off + len);
        _tmpBuffer = null; // Removes temporary references.
    }

    private char[] _tmpBuffer;

    private final CharSequence _tmpBufferAsCharSequence = new CharSequence() {
        public int length() {
            return _tmpBuffer.length;
        }

        public char charAt(int index) {
            return _tmpBuffer[index];
        }

        public CharSequence subSequence(int start, int end) {
            throw new UnsupportedOperationException();
        }
    };

    /**
     * Writes a portion of a string.
     *
     * @param  str a String.
     * @param  off the offset from which to start writing characters.
     * @param  len the number of characters to write.
     * @throws IOException if an I/O error occurs
     */
    public void write(String str, int off, int len) throws IOException {
        if (output == null)
            throw new IOException("Writer closed");
        Object obj = str;
        if (obj instanceof CharSequence) {
            output.append((CharSequence) obj);
        } else {
            output.append(Text.valueOf(str));
        }
    }

    /**
     * Writes the specified character sequence.
     *
     * @param  csq the character sequence.
     * @throws IOException if an I/O error occurs
     */
    public void write(CharSequence csq) throws IOException {
        if (output == null)
            throw new IOException("Writer closed");
        output.append(csq);
    }

    /**
     * Flushes the stream.
     */
    public void flush() {
        // Do nothing (no buffer).
    }

    /**
     * Closes and {@link #reset resets} this writer for reuse.
     */
    public void close() {
        if (output != null) {
            reset();
        }
    }

    public void reset() {
        output = null;
        _tmpBuffer = null;
    }
}