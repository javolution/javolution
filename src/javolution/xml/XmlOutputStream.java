/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2004 - The Javolution Team (http://javolution.org/)
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.xml;

import javolution.Javolution;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

/**
 * <p> This class represents a stream serializer using {@link XmlFormat} 
 *     for the serialization of Java(tm) objects over open connection.</p>
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 2.0, December 5, 2004
 * @see     XmlInputStream
 */
public class XmlOutputStream extends ObjectOutputStream {

    /**
     * Holds the output stream.
     */
    private OutputStream _outputStream;

    /**
     * Holds the object writer.
     */
    private ObjectWriter _objectWriter = new ObjectWriter();

    /**
     * Holds the minimum number of separator bytes between two xml objects.
     * This allows the reader to work using block read without missing
     * objects.
     */
    static final int SEPARATOR_SIZE  = 256;

    /**
     * Holds the separator bytes (UTF-8 spaces).
     */
    static final byte[] SEPARATOR  = new byte[SEPARATOR_SIZE];
    static {
    	for (int i=0; i < SEPARATOR_SIZE;) {
    		SEPARATOR[i++] = (byte) ' ';
    	}
    }

    /**
     * Default constructor.
     */
    private XmlOutputStream() throws IOException, SecurityException {
    	_objectWriter.setIndent("");
    	_objectWriter.setProlog(false);
    }
    
    /**
     * Returns an XmlOutputStream writing to the specified output stream.
     *
     * @param out the output stream to write to.
     */
    public static XmlOutputStream newInstance(OutputStream out)  {
    	try {
    		XmlOutputStream xos = new XmlOutputStream();
    	    xos._outputStream = out;
    	    return xos;
    	} catch (Throwable e) {
    		throw new Javolution.InternalError(e);
    	}
    }

    /**
     * Overrides the default (@link #writeObject} method.
     *
     * @param  obj the object to serialize using its xml format.
     * @throws IOException propagates error from underlying stream
     */
    protected void writeObjectOverride(Object obj) throws IOException {
         _objectWriter.write(obj, _outputStream, true); // Keep open.
         _outputStream.write(SEPARATOR);
    }

    /**
     * Flushes the underlying stream.
     * 
     * @throws IOException propagates error from underlying stream.
     */
    public void flush() throws IOException {
         _outputStream.flush();
    }

    /**
     * Closes this stream and the underlaying stream.
     * 
     * @throws IOException propagates error from underlying stream.
     */
    public void close() throws IOException {
         _outputStream.flush();
         _outputStream.close();
    }
    /**
     * Resets the object writer for this stream.
     * 
     * @see ObjectWriter#reset
     */
    public void reset() {
        _objectWriter.reset();
    }

}