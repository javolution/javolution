/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2004 - The Javolution Team (http://javolution.org/)
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */

package j2me.lang;

public class TypeNotPresentException extends RuntimeException {
    String _typeName;

    public TypeNotPresentException(String typeName, Throwable cause) {
        super("Type " + typeName + " not present");
        _typeName = typeName;
    }

    public String typeName() {
        return _typeName;
    }
}