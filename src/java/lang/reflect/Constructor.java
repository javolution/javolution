/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2004 - The Javolution Team (http://javolution.org/)
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package java.lang.reflect;

/**
 *  Class provided for the sole purpose of compiling the Reflection class.
 */
public final class Constructor implements Member {

    public Object newInstance(Object[] initargs) throws InstantiationException,
            IllegalAccessException, IllegalArgumentException,
            InvocationTargetException {
        throw new UnsupportedOperationException();
    }

    public Class getDeclaringClass() {
        throw new UnsupportedOperationException();
    }

    public String getName() {
        throw new UnsupportedOperationException();
    }

    public int getModifiers() {
        throw new UnsupportedOperationException();
    }
}