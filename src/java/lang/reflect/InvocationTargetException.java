/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2004 - The Javolution Team (http://javolution.org/)
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package java.lang.reflect;

public class InvocationTargetException extends Exception {
    
    private static final long serialVersionUID = 4085088731926701167L;

    private Throwable _target;

    protected InvocationTargetException() {
    }

    public InvocationTargetException(Throwable target) {
        _target = target;
    }

    public InvocationTargetException(Throwable target, String s) {
        super(s);
        _target = target;
    }

    public Throwable getTargetException() {
        return _target;
    }

    public Throwable getCause() {
        return _target;
    }
}