/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2005 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package _templates.java.io;
import java.io.IOException;

import _templates.java.io.DataOutput;

public interface ObjectOutput extends DataOutput {
    void writeObject(Object obj) throws IOException;

    void write(int b) throws IOException;

    void write(byte b[]) throws IOException;

    void write(byte b[], int off, int len) throws IOException;

    void flush() throws IOException;

    void close() throws IOException;
}