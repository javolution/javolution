/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2007 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.testing;

/**
 * This class represents an exception which might be raised when 
 * a testing assertion fails (see {@link TestContext#REGRESSION}).
 * 
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 5.2, August 5, 2007
 */
public class AssertionException extends RuntimeException {

    /**
     * Holds the associated message if any.
     */
    private String _message;

    /**
     * Holds the expected value.
     */
    private Object _expected;

    /**
     * Holds the actual value.
     */
    private Object _actual;

    /**
     * Creates an exception with the specified parameters.
     * 
     * @param message the associated message or <code>null</code>
     * @param expected the expected value
     * @param actual the actual value
     */
    public AssertionException(String message, Object expected, Object actual) {
        _message = message;
        _expected = expected;
        _actual = actual;    
    }

    /**
     * Returns the assertion message if any.
     * 
     * @return the assertion message or <code>null</code>
     */
    public String getMessage() {
        return _message;
    }

    /**
     * Returns the expected value.
     * 
     * @return the assertion expected value.
     */
    public Object getExpected() {
        return _expected;
    }
    
    /**
     * Returns the actual value.
     * 
     * @return the assertion actual value.
     */
    public Object getActual() {
        return _actual;
    }

    /**
     * Returns the textual representation of this exception.
     * 
     * @return the string representation of the exception.
     */
    public String toString() {
        return _message != null ? _message + ": " + _expected
                + " expected but found " + _actual : _expected
                + " expected but found " + _actual;
    }

    private static final long serialVersionUID = 1L;
}
