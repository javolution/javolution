/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2004 - The Javolution Team (http://javolution.org/)
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */

package java.util;

public class IllegalFormatFlagsException extends IllegalFormatException {

    private String _flags;

    public IllegalFormatFlagsException(String f) {
        _flags = f;
    }

    public String getFlags() {
        return _flags;
    }
}