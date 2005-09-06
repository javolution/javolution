/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2005 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.xml;

/**
 * <p> Signals that a problem of some sort has occurred when 
 *     serializing/deserializaing an object from its XML representation.</p>
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 1.0, October 4, 2004
 */
public class XmlException extends RuntimeException {

    /**
     * Holds cause if any.
     */
    private Throwable _cause;

    /**
     * Constructs a <code>XmlException</code> with  no detail message.
     */
    public XmlException() {
        super();
    }

    /**
     * Constructs a <code>XmlException</code> with the specified detail
     * message.
     *
     * @param   message the detail message.
     */
    public XmlException(String message) {
        super(message);
    }

    /**
     * Constructs a <code>XmlException</code> with the specified detail
     * message and cause.
     *
     * @param  message the detail message.
     * @param  cause the cause.
     */
    public XmlException(String message, Throwable cause) {
        super(message + " caused by " + cause);
        _cause = cause;
    }

    /**
     * Constructs a <code>XmlException</code> with the specified cause.
     *
     * @param  cause the cause.
     */
    public XmlException(Throwable cause) {
        super("XML exception caused by " + cause);
        _cause = cause;
    }

    /**
     * Returns the original cause of this xml exception.
     *
     * @return  the cause of this exception or <code>null</code> if the
     *          cause is nonexistent or unknown.
     */
    public Throwable getCause() {
        return _cause;
    }

    private static final long serialVersionUID = 1L;
}