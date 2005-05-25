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
import java.io.IOException;
import java.io.OutputStream;

import javolution.lang.Reusable;

/**
 * <p> This class represents an object output stream using {@link ObjectWriter}
 *     for object serialization.</p>
 *     
 * <p> Instances of this class embed their own data buffer, wrapping using a
 *     <code>java.io.BufferedOutputStream</code> is therefore unnescessary.</p>
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 2.2, January 8, 2005
 * @see     XmlInputStream
 */
public class XmlOutputStream/*<T>*/ extends OutputStream implements Reusable {

    /**
     * Holds the byte indicating the end of a XML document transmission,
     * acceptable values are <code>0xFE</code> and <code>0xFF</code>
     * as they do not occur in a UTF-8 encoded text.
     */
    static final byte END_XML  = (byte) 0xFE;

    /**
     * Holds the output stream.
     */
    private OutputStream _outputStream;

    /**
     * Holds the object writer.
     */
    private final ObjectWriter/*<T>*/ _objectWriter = new ObjectWriter/*<T>*/();

    /**
     * Holds the object writer.
     */
    private final OutputStreamProxy _outputStreamProxy = new OutputStreamProxy();

    /**
     * Default constructor.
     */
    public XmlOutputStream() {
    }
    
    /**
     * Sets the underlying output destination for this stream.
     * 
     * @param out the output destination.
     * @return <code>this</code> 
     * @throws IllegalStateException if this stream is being reused and 
     *         it has not been {@link #close closed} or {@link #reset reset}.
     */
    public XmlOutputStream setOutputStream(OutputStream out) {
        if (_outputStream != null)
            throw new IllegalStateException("Stream not closed or reset");
        _outputStream = out;
        return this;
    }

    /**
     * Writes an object to the underlying stream using an {@link ObjectWriter}.
     * 
     * @param obj the object writen using its xml representation. 
     * @throws IOException if an I/O error occurs.
     */
    public void writeObject(Object/*T*/ obj) throws IOException {
        if (_outputStream == null) throw new IOException("Stream closed");
        _objectWriter.write(obj, _outputStreamProxy);
        _outputStream.write(END_XML);
        _outputStream.flush();
    }

    // Implements abstract method.
    
    /**
     * Writes the specified byte to this output stream
     * 
     * @param b the byte. 
     * @throws IOException if an I/O error occurs.
     */
    public void write(int b) throws IOException {
        if (_outputStream == null) throw new IOException("Stream closed");
        _outputStream.write(b);
    }
    
    /**
     * Flushes this output stream and forces any buffered output bytes 
     * to be written out.
     *  
     * @throws IOException if an I/O error occurs.
     */
    public void flush() throws IOException {
        if (_outputStream == null) throw new IOException("Stream closed");
        _outputStream.flush();
    }
    
    /**
     * Writes the specified number of bytes from the specified byte array 
     * starting at the specified offset to this output stream. 
     * 
     * @param b the data.
     * @param off the start offset in the data.
     * @param len the number of bytes to write. 
     * @throws IOException if an I/O error occurs.
     */
    public void write(byte b[], int off, int len) throws IOException {
        if (_outputStream == null) throw new IOException("Stream closed");
        _outputStream.write(b, off, len);
    }
    
    /**
     * Closes and {@link #reset resets} this stream for reuse.
     *
     * @throws IOException if an I/O error occurs.
     */
    public void close() throws IOException {
        if (_outputStream != null) {
            _outputStream.close();
            reset();
        }
    }

    // Implements Reusable interface.
    public void reset() {
        _objectWriter.reset();
        _outputStream = null;
    }

    /**
     * This inner class represents an output stream proxy for which the 
     * {@link #close()} command has no effect.
     */
    private final class OutputStreamProxy extends OutputStream {
        public void flush() throws IOException {
            _outputStream.flush();
        }
        public void write(byte b[], int off, int len) throws IOException {
            _outputStream.write(b, off, len);
        }
        public void write(int b) throws IOException {
            _outputStream.write(b);
        }
        public void close() throws IOException {
            // Do nothing.
        }
    }
}