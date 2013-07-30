/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */

package javolution.lang;

/**
 * <p> This interfaces identifies mutable objects capable of being used again
 *     or repeatedly; once {@link #reset reset}, reusable objects behave as if
 *     they were brand-new.</p>
 *     
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 3.7, January 1, 2006
 */
public interface Reusable {

    /**
     * Resets the internal state of this object to its default values.
     */
    void reset();

}