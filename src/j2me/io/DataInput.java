/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2004 - The Javolution Team (http://javolution.org/)
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package j2me.io;

import java.io.IOException;

public interface DataInput {

    boolean readBoolean() throws IOException;

    byte readByte() throws IOException;

    char readChar() throws IOException;

    void readFully(byte[] b) throws IOException;

    void readFully(byte[] b, int off, int len) throws IOException;

    int readInt() throws IOException;

    String readLine() throws IOException;

    long readLong() throws IOException;

    short readShort() throws IOException;

    int readUnsignedByte() throws IOException;

    int readUnsignedShort() throws IOException;

    String readUTF() throws IOException;

    int skipBytes(int n) throws IOException;

    /*@FLOATING_POINT@
     
     float readFloat() throws IOException;

     double readDouble() throws IOException;
     
     /**/
}