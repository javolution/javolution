/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2004 - The Javolution Team (http://javolution.org/)
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package java.util;

public class IllegalFormatWidthException extends IllegalFormatException {
    int _width;

    public IllegalFormatWidthException(int w) {
        _width = w;
    }

    public int getWidth() {
        return _width;
    }
}