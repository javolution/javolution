/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2004 - The Javolution Team (http://javolution.org/)
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution;

/**
 *  Signals that a serious problem (bug ?!) has been detected 
 *  within the library.
 */
public final class JavolutionError extends Error {

    /**
     * Creates an error message with the specified message 
     * and cause.
     * 
     * @param  message the detail message.
     * @param  cause the cause or <code>null</code> if the cause 
     *         is nonexistent or unknown.
     * @throws Error (always) 
     */
    public JavolutionError(String message) {
        super(message);
    }

    /**
     * Creates an error message with the specified message 
     * and cause. The cause stack trace is printed to the
     * current error stream (System.err).
     * 
     * @param  message the detailed message.
     * @param  cause the cause of this error.
     */
    public JavolutionError(String message, Throwable cause) {
        super(message);
        cause.printStackTrace();
    }

    /**
     * Creates an error message with the specified cause 
     * The cause stack trace is printed to the current error
     * stream (System.err).
     * 
     * @param  cause the cause of this error.
     */
    public JavolutionError(Throwable cause) {
        cause.printStackTrace();
    }
}