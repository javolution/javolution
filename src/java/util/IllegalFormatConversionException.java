/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2004 - The Javolution Team (http://javolution.org/)
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */

package java.util;

public class IllegalFormatConversionException extends IllegalFormatException {
    private char c;

    private Class arg;

    public IllegalFormatConversionException(char c, Class arg) {
        if (arg == null)
            throw new NullPointerException();
        this.c = c;
        this.arg = arg;
    }

    public char getConversion() {
        return c;
    }

    public Class getArgumentClass() {
        return arg;
    }

}