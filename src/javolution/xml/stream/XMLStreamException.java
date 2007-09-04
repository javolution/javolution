/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2005 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.xml.stream;

/**
 * This class represents the base exception for unexpected processing errors.
 * 
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 3.8, May 22, 2006
 */
public class XMLStreamException extends Exception {

    /**
     * Holds the nested exception if any.
     */
    private Throwable _nested;

    /**
     * Holds the location.
     */
    private Location _location;

    /**
     * Default constructor
     */
    public XMLStreamException() {
        super();
    }

    /**
     * Constructs an exception with the assocated message.
     * 
     * @param msg the message to report.
     */
    public XMLStreamException(String msg) {
        super(msg);
    }

    /**
     * Constructs an exception with the assocated nested exception.
     * 
     * @param nested the nested exception.
     */
    public XMLStreamException(Throwable nested) {
        _nested = nested;
    }

    /**
     * Constructs an exception with the assocated message and exception.
     * 
     * @param msg the message to report.
     * @param nested the nested exception.
     */
    public XMLStreamException(String msg, Throwable nested) {
        super(msg);
        _nested = nested;
    }

    /**
     * Constructs an exception with the assocated message, exception and
     * location.
     * 
     * @param msg the message to report.
     * @param location the location.
     * @param nested the nested exception.
     */
    public XMLStreamException(String msg, Location location, Throwable nested) {
        super(msg);
        _nested = nested;
        _location = location;
    }

    /**
     * Constructs an exception with the assocated message, exception and
     * location.
     * 
     * @param msg the message to report
     * @param location the location of the error
     */
    public XMLStreamException(String msg, Location location) {
        super(msg);
        _location = location;
    }

    /**
     * Returns the nested exception.
     * 
     * @return the nested exception
     */
    public Throwable getNestedException() {
        return _nested;
    }

    /**
     * Returns the location of the exception.
     * 
     * @return the location of the exception or <code>null</code> 
     *         if none is available
     */
    public Location getLocation() {
        return _location;
    }

    /**
     * Returns the textual representation of this exception.
     * 
     * @return the string representation of the exception.
     */
    public String toString() {
        String msg = super.toString();
        if (_location != null) {
            msg += " (at line " + _location.getLineNumber() + ", column "
                    + _location.getColumnNumber() + ")";
        }
        if (_nested != null) {
            msg += " caused by " + _nested.toString();
        }
        return msg;
    }

    private static final long serialVersionUID = 1L;
}
