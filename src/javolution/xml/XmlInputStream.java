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
import java.io.InputStream;
import java.io.ObjectInputStream;

/**
 * <p> This class represents a stream deserializer using {@link XmlFormat}
 *     for the deserialization of Java(tm) objects over open connection.</p>
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 2.0, December 5, 2004
 * @see     XmlOutputStream
 */
public class XmlInputStream extends ObjectInputStream {

    /**
     * Holds the input stream.
     */
    private InputStream _inputStream;

    /**
     * Holds the object reader.
     */
    private ObjectReader _objectReader = new ObjectReader();

    /**
     * Holds the proxy for xml data.
     */
    private Proxy _proxy = new Proxy();

    /**
     * Default constructor.
     */
    private XmlInputStream() throws IOException, SecurityException {
    }
    
    /**
     * Returns an XmlInputStream reading from the specified InputStream.
     *
     * @param in the input stream to read from.
     * @return a XmlInputStream instance for the specified stream.
     */
    public static XmlInputStream newInstance(InputStream in)  {
    	try {
    		XmlInputStream xis = new XmlInputStream();
    	    xis._inputStream = in;
    	    return xis;
    	} catch (Throwable e) {
    		throw new Javolution.InternalError(e);
    	}
    }

    /**
     * Closes the underlying stream.
     * 
     * @throws IOException propagates error from underlying stream.
     */
    public void close() throws IOException {
         _inputStream.close();
    }
    
    /**
     * Overrides the default (@link #readObject} method.
     *
     * @return the object deserialized using its xml format.
     * @throws IOException propagates error from underlying stream.
     */
    protected Object readObjectOverride() throws IOException {
    	return _objectReader.read(_proxy);   	
    }
    
    /**
     * This inner class represents the input stream proxy used
     * when reading objects. 
     */
    private final class Proxy extends InputStream {
        public int read(byte b[], int off, int len) throws IOException {
        	if (_objectReader.getRoots().size() == 0) {
        		// Limit length to avoid missing objects.
        		int maxLen = (len < XmlOutputStream.SEPARATOR_SIZE) ?
        				len : XmlOutputStream.SEPARATOR_SIZE;
           		return _inputStream.read(b, off, maxLen);
        	} else { // Done.
        		return -1;
        	}
        }
        public int read() throws IOException {
        	if (_objectReader.getRoots().size() == 0) {
        		return _inputStream.read();
        	} else { // Done.
        		return -1;
        	}
 		}
		
    }
    
}