/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2004 - The Javolution Team (http://javolution.org/)
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package java.lang;

public interface CharSequence {
    int length();

    char charAt(int index);

    CharSequence subSequence(int start, int end);

    public String toString();
}