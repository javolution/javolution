/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2004 - The Javolution Team (http://javolution.org/)
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package java.nio;
import javolution.lang.Appendable;

/**
 *  Class provided for the sole purpose of compiling the Readable interface.
 */
public abstract class CharBuffer extends Buffer implements Comparable,
        Appendable, CharSequence, Readable {

    CharBuffer(int capacity, int limit, int position, int mark) {
        super(capacity, limit, position, mark);
    }
}
