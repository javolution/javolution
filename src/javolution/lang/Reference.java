/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2005 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.lang;

/**
 * <p> This interface represents an object reference, the reachability level 
 *     of a reference varies based on the actual reference implementation.
 *     Here are the reachability levels for some of <i><b>J</b>avolution</i>
 *     references:<ul>
 *     <li> {@link javolution.lang.PersistentReference PersistentReference} : 
 *          Reachable accross multiple program executions.</li>
 *     <li> {@link javolution.realtime.LocalReference LocalReference} : 
 *          Reachable only within the scope of the 
 *          {@link javolution.realtime.LocalContext LocalContext}
 *          where it has been set.</li>
 *     <li> {@link javolution.realtime.StackReference StackReference} :
 *          Reachable only within the scope of the  
 *          {@link javolution.realtime.PoolContext PoolContext}
 *          where it has been created (factory produced).</li>
 *     </ul></p>
 * 
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 3.3, May 10, 2005
 */
public interface Reference/*<T>*/ {

    /**
     * Returns the value this reference referes to.
     *
     * @return the referent or <code>null</code> if not set.
     */
    Object/*T*/ get();

    /**
     * Sets the value this reference referes to.
     *
     * @param value the reference value.
     */
    void set(Object/*T*/ value);

}