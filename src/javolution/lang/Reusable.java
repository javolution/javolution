/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2004 - The Javolution Team (http://javolution.org/)
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.lang;


/**
 * <p> This interfaces identifies mutable objects capable of being used again
 *     or repeatedly without incurring dynamic memory allocation.</p>
 * <p> Instances of this class can safely reside in permanent memory (e.g.<code>
 *     static</code>) or be an integral part of a higher level component.</p>
 * <p> Once {@link #clear cleared}, reusable objects behave as if they were
 *     brand-new.</p>    
 * 
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 2.0, November 26, 2004
 */
public interface Reusable {

    /**
     * Clears all internal data and external references maintained by 
     * this object.
     */
    void clear();

}