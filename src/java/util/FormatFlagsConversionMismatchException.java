/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2004 - The Javolution Team (http://javolution.org/)
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */

package java.util;

public class FormatFlagsConversionMismatchException extends
        IllegalFormatException {
    private String f;

    private char c;

    public FormatFlagsConversionMismatchException(String f, char c) {
        this.f = f;
        this.c = c;
    }

    public String getFlags() {
        return f;
    }

    public char getConversion() {
        return c;
    }

}