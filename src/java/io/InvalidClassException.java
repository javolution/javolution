/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2004 - The Javolution Team (http://javolution.org/)
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package java.io;

public class InvalidClassException extends ObjectStreamException {

    public InvalidClassException(String reason) {
        super(reason);
    }

    public InvalidClassException(String cname, String reason) {
        super(reason);
    }
}