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
public class ObjectInputStream extends InputStream implements
        ObjectInput {

    // For sub-classes reimplementing this class.
	protected ObjectInputStream() throws IOException, SecurityException {
    }

    public ObjectInputStream(InputStream in) throws IOException {
        throw new UnsupportedOperationException();
    }
    
    public final Object readObject() throws IOException, ClassNotFoundException {
        throw new UnsupportedOperationException();
    }

    public Object readUnshared() throws IOException, ClassNotFoundException {
        throw new UnsupportedOperationException();
    }

    public void defaultReadObject() throws IOException, ClassNotFoundException {
        throw new UnsupportedOperationException();
    }

    public void registerValidation(ObjectInputValidation obj, int prio)
            throws NotActiveException, InvalidObjectException {
        throw new UnsupportedOperationException();
    }

    public int read() throws IOException {
        throw new UnsupportedOperationException();
    }

    public int read(byte[] buf, int off, int len) throws IOException {
        throw new UnsupportedOperationException();
    }

    public int available() throws IOException {
        throw new UnsupportedOperationException();
    }

    public void close() throws IOException {
        throw new UnsupportedOperationException();
    }

    public boolean readBoolean() throws IOException {
        throw new UnsupportedOperationException();
    }

    public byte readByte() throws IOException {
        throw new UnsupportedOperationException();
    }

    public int readUnsignedByte() throws IOException {
        throw new UnsupportedOperationException();
    }

    public char readChar() throws IOException {
        throw new UnsupportedOperationException();
    }

    public short readShort() throws IOException {
        throw new UnsupportedOperationException();
    }

    public int readUnsignedShort() throws IOException {
        throw new UnsupportedOperationException();
    }

    public int readInt() throws IOException {
        throw new UnsupportedOperationException();
    }

    public long readLong() throws IOException {
        throw new UnsupportedOperationException();
    }

    public float readFloat() throws IOException {
        throw new UnsupportedOperationException();
    }

    public double readDouble() throws IOException {
        throw new UnsupportedOperationException();
    }

    public void readFully(byte[] buf) throws IOException {
        throw new UnsupportedOperationException();
    }

    public void readFully(byte[] buf, int off, int len) throws IOException {
        throw new UnsupportedOperationException();
    }

    public int skipBytes(int len) throws IOException {
        throw new UnsupportedOperationException();
    }

    public String readLine() throws IOException {
        throw new UnsupportedOperationException();
    }

    public String readUTF() throws IOException {
        throw new UnsupportedOperationException();
    }
}