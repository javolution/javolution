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
 *     has been {@link #newInstance created}.</p>
 *     
 * <p> Stack references are automatically cleared based upon their 
 *     reachability level like any <code>java.lang.ref.Reference</code>.
 *     In other words, stack references are automatically cleared when exiting
 *     the {@link PoolContext} where they have been factory produced.</p>
 *     
 * <p> Stack references are typically used by functions having more than one
 *     return value to avoid creating new objects on the heap. For example:<pre>
 *     // Returns both the position and its status.
 *     public Coordinates getPosition(Reference&lt;Status&gt; status) {
 *         ...
 *     }
 *     ...
 *     StackReference&lt;Status&gt; status = StackReference.newInstance(); // On the stack.
 *     Coordinates position = getPosition(status);
 *     if (status.get() == ACCURATE) ...</pre> 
 *     See also {@link ConcurrentContext} for examples of 
 *     {@link StackReference} usage.</p>
 *          
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 3.4, July 3, 2005
 */
public final class StackReference/*<T>*/extends RealtimeObject implements
        Reference/*<T>*/{

    /**
     * Holds the factory.
     */
    private static final Factory FACTORY = new Factory() {
        protected Object create() {
            return new StackReference();
        }

        protected void cleanup(Object obj) {
            ((StackReference) obj)._value = null;
        }
    };

    /**
     * Holds the reference value.
     */
    private Object/*T*/_value;

    /**
     * Default constructor (private, instances should be created using 
     * factories).
     */
    private StackReference() {
    }

    /**
     * Returns a new stack reference instance allocated on the current stack
     * when executing in {@link PoolContext}.
     * 
     * @return a local reference object.
     */
    public static/*<T>*/StackReference /*<T>*/newInstance() {
        return (StackReference) FACTORY.object();
    }

    // Implements Reference interface.
    public Object/*T*/get() {
        return _value;
    }

    // Implements Reference interface.
    public void set(Object/*T*/value) {
        _value = value;
    }
}