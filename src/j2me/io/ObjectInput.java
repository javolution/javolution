/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2004 - The Javolution Team (http://javolution.org/)
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package j2me.io;
import java.io.IOException;
import j2me.io.DataInput;

public interface ObjectInput extends DataInput {
    Object readObject() throws ClassNotFoundException, IOException;

    int read() throws IOException;

    int read(byte b[]) throws IOException;

    int read(byte b[], int off, int len) throws IOException;

    long skip(long n) throws IOException;

    int available() throws IOException;

    void close() throws IOException;
}