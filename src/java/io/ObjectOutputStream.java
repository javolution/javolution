/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2004 - The Javolution Team (http://javolution.org/)
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */

package java.io;
/**
 *  Classes provided for serialization support.
 */
public class ObjectOutputStream
    extends OutputStream implements ObjectOutput {
	
    // For sub-classes reimplementing this class.
	protected ObjectOutputStream() throws IOException, SecurityException {
    }

    public ObjectOutputStream(OutputStream out) throws IOException {
        throw new UnsupportedOperationException();
    }
    public void useProtocolVersion(int version) throws IOException {
        throw new UnsupportedOperationException();
    }
    public final void writeObject(Object obj) throws IOException {
        throw new UnsupportedOperationException();
    }
    protected void writeObjectOverride(Object obj) throws IOException {
        throw new UnsupportedOperationException();
    }
    public void writeUnshared(Object obj) throws IOException {
        throw new UnsupportedOperationException();
    }
    public void defaultWriteObject() throws IOException {
        throw new UnsupportedOperationException();
    }
    public void writeFields() throws IOException {
        throw new UnsupportedOperationException();
    }
    public void reset() throws IOException {
        throw new UnsupportedOperationException();
    }
    public void write(int val) throws IOException {
        throw new UnsupportedOperationException();
    }
    public void write(byte[] buf) throws IOException {
        throw new UnsupportedOperationException();
    }
    public void write(byte[] buf, int off, int len) throws IOException {
        throw new UnsupportedOperationException();
    }
    public void flush() throws IOException {
        throw new UnsupportedOperationException();
    }
    public void close() throws IOException {
        throw new UnsupportedOperationException();
    }
    public void writeBoolean(boolean val) throws IOException {
        throw new UnsupportedOperationException();
    }
    public void writeByte(int val) throws IOException  {
        throw new UnsupportedOperationException();
    }
    public void writeShort(int val)  throws IOException {
        throw new UnsupportedOperationException();
    }
    public void writeChar(int val)  throws IOException {
        throw new UnsupportedOperationException();
    }
    public void writeInt(int val)  throws IOException {
        throw new UnsupportedOperationException();
    }
    public void writeLong(long val)  throws IOException {
        throw new UnsupportedOperationException();
    }
    public void writeFloat(float val) throws IOException {
        throw new UnsupportedOperationException();
    }
    public void writeDouble(double val) throws IOException {
        throw new UnsupportedOperationException();
    }
    public void writeBytes(String str) throws IOException {
        throw new UnsupportedOperationException();
    }
    public void writeChars(String str) throws IOException {
        throw new UnsupportedOperationException();
    }
    public void writeUTF(String str) throws IOException {
        throw new UnsupportedOperationException();
    }
}
