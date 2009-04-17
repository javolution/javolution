/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2007 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package _templates.javolution.testing;
import _templates.java.lang.CharSequence;

/**
 * This class represents an exception which might be raised when 
 * a testing assertion fails (see {@link TestContext#REGRESSION}).
 * 
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 5.3, February 24, 2009
 */
public class AssertionException extends RuntimeException {


    /**
     * Creates an assertion exception with no detailed message.
     */
    public AssertionException() {
    }

    /**
     * Creates an assertion exception with the specified detailed message.
     * 
     * @param message the exception detailed message.
     */
    public AssertionException(String message) {
        super(message);
    }

    private static final long serialVersionUID = 1L;
}
