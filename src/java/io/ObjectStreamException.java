/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2004 - The Javolution Team (http://javolution.org/)
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */

package java.io;

public abstract class ObjectStreamException extends IOException {
    protected ObjectStreamException(String classname) {
        super(classname);
    }

    protected ObjectStreamException() {
        super();
    }
}