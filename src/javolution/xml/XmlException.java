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
    }

    /**
     * Constructs a <code>XmlException</code> with the specified cause.
     *
     * @param  cause the cause.
     */
    public XmlException(Throwable cause) {
        super("Constructor exception caused by " + cause.toString());
    }

    private static final long serialVersionUID = 3762814870390716212L;
}