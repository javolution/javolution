/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2004 - The Javolution Team (http://javolution.org/)
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.xml;

import java.io.IOException;
import java.io.OutputStream;

import javolution.lang.Reusable;

/**
 * <p> This class represents an object output stream using {@link ObjectWriter}
 *     for object serialization.</p>
 *     
 * <p> Instances of this class embed their own data buffer, wrapping using a
 *     <code>j2me.io.BufferedOutputStream</code> is therefore unnescessary.</p>
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 2.0, December 5, 2004
 * @see     XmlInputStream
 */
public class XmlOutputStream extends OutputStream implements Reusable {

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
    private final ObjectWriter _objectWriter;

    /**
     * Holds the object writer.
     */
    private final OutputStreamProxy _outputStreamProxy;

    /**
     * Creates a xml output stream of default buffer capacity.
     */
    public XmlOutputStream() {
        this(2048);
    }
    
    /**
     * Creates a xml output stream having the specified buffer capacity.
     * 
     * @param capacity the buffer capacity. 
     */
    public XmlOutputStream(int capacity) {
        _objectWriter = new ObjectWriter(capacity);
        _outputStreamProxy = new OutputStreamProxy();
        _objectWriter.setIndent("");
        _objectWriter.setProlog(false);
    }

    /**
     * Sets the underlying output destination for this stream.
     * 
     * @param out the output destination.
     * @return <code>this</code> 
     * @throws Error if this stream is being reused and 
     *         it has not been {@link #close closed} or {@link #clear cleared}.
     */
    public XmlOutputStream setOutputStream(OutputStream out) {
        if (_outputStream != null)
            throw new Error("This stream has not been closed or cleared");
        _outputStream = out;
        return this;
    }

    /**
     * Writes an object to the underlying stream using an {@link ObjectWriter}.
     * 
     * @param obj the object writen using its xml representation. 
     * @throws IOException if an I/O error occurs.
     */
    public void writeObject(Object obj) throws IOException {
        if (_outputStream == null) throw new IOException("Stream closed");
        _objectWriter.write(obj, _outputStreamProxy);
        _outputStream.write(END_XML);
        _outputStream.flush();
    }

    // Implements abstract method.
    public void write(int b) throws IOException {
        if (_outputStream == null) throw new IOException("Stream closed");
        _outputStream.write(b);
    }
    
    // Overrides.
    public void flush() throws IOException {
        if (_outputStream == null) throw new IOException("Stream closed");
        _outputStream.flush();
    }
    
    // Overrides.
    public void write(byte b[], int off, int len) throws IOException {
        if (_outputStream == null) throw new IOException("Stream closed");
        _outputStream.write(b, off, len);
    }
    
    // Overrides.
    public void close() throws IOException {
        if (_outputStream != null) {
            _outputStream.close();
            clear();
        }
    }

    // Implements Reusable interface.
    public void clear() {
        _objectWriter.clear();
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