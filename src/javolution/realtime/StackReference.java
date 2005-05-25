/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2005 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.realtime;

import javolution.lang.Reference;

/**
 * <p> This class encapsulates a reference allocated on the current stack
 *     when executing in {@link PoolContext}. The reachability level of a
 *     stack reference is the scope of the {@link PoolContext} in which it
 *     has been {@link #set set}. Instances of this class can only be created 
 *     using {@link ObjectFactory#reference factories}.</p>
 *     
 * <p> Stack references are automatically cleared based upon their 
 *     reachability level like any <code>java.lang.ref.Reference</code>.
 *     In other words, stack references are automatically cleared when exiting
 *     the {@link PoolContext} where they have been factory produced.</p>
 *     
 * <p> Stack references are typically used by methods returning more 
 *     than one result. For example:<pre>
 *         // Calculates time/position pair on the stack (thread-local).
 *         StackReference&lt;Time&gt; timeRef = TIME_FACTORY.reference();
 *         StackReference&lt;Position&gt; posRef = POS_FACTORY.reference();
 *         getTimeAndPosition(timeRef, posRef);
 *         Time time = TIME_REF.get(); 
 *         Position pos = POS_REF.get(); 
 *         ...
 *         public void getTimeAndPosition(Reference&lt;Time&gt; timeRef, Reference&lt;Position&gt; posRef) {
 *             Time time = Time.current();
 *             Position pos = positionAt(time);
 *             timeRef.set(time);
 *             posRef.set(pos);
 *         }
 *         private static final ObjectFactory&lt;Time&gt; TIME_FACTORY = new ObjectFactory&lt;Time&gt;;
 *         private static final ObjectFactory&lt;Position&gt; POS_FACTORY = new ObjectFactory&lt;Position&gt;;
 *     </pre>
 *          
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 3.3, February 20, 2004
 */
public final class StackReference/*<T>*/ extends RealtimeObject implements Reference/*<T>*/ {

    /**
     * Holds the factory.
     */
    static final Factory FACTORY = new Factory() {
        protected Object create() {
            return new StackReference();
        }
    };

    /**
     * Holds the reference value.
     */
    private Object/*T*/ _value;
    
    /**
     * Default constructor (private, instances should be created using 
     * factories).
     */
    private StackReference() {
    }

    // Implements Reference interface.
    public Object/*T*/ get() {
        return _value;
    }

    // Implements Reference interface.
    public void set(Object/*T*/ value) {
        _value = value;    
     }
}