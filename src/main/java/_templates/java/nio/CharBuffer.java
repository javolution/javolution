/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2006 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package _templates.java.nio;
import _templates.java.lang.CharSequence;
import _templates.java.lang.Comparable;
import _templates.java.lang.Readable;
import _templates.javolution.text.Appendable;

/**
 *  Class provided for the sole purpose of compiling the Readable interface.
 */
public abstract class CharBuffer extends Buffer implements Comparable,
        Appendable, CharSequence, Readable {

    CharBuffer(int capacity, int limit, int position, int mark) {
        super(capacity, limit, position, mark);
    }
}
