/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2005 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.realtime;

import javolution.realtime.RealtimeObject.Factory;

/**
 * <p> This class encapsulates a reference to some other object, the 
 *     reachability level of a real-time reference is the scope of the 
 *     {@link PoolContext} in which it has been {@link #newInstance created}.
 *     </p>
 *     
 * <p> Real-time reference are automatically cleared based upon their 
 *     reachability level like any <code>java.lang.ref.Reference</code>.
 *     </p>
 *
 * <p> Direct instances of this class may be used to implement simple object
 *     references; derived subclasses may provide more sophisticated 
 *     {@link #clear clearing} actions (e.g. release of system resources).</p>
 *     
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 3.0, February 20, 2004
 */
public class RealtimeReference {

    /**
     * Holds the reference factory.
     */
    private static final Factory FACTORY = new Factory() {

        public Object create() {
            return new RealtimeReference();
        }

        public void cleanup(Object obj) {
            ((RealtimeReference) obj)._referent = null;
        }
    };

    /**
     * Holds the referent.
     */
    private Object _referent;

    /**
     * Default constructor.
     */
    public RealtimeReference() {
    }

    /**
     * Returns a reference allocated from the stack when executing in a
     * {@link javolution.realtime.PoolContext PoolContext}.
     *
     * @return a new, preallocated or recycled reference instance.
     */
    public static RealtimeReference newInstance() {
        return (RealtimeReference) FACTORY.object();
    }

    /**
     * Returns this reference object's referent. If this reference object has
     * been cleared, either by the program or due to the {@link PoolContext#exit
     * exit} of the {@link PoolContext} scope (for stack allocated references),
     * then this method returns <code>null</code>.
     * 
     * @return the object to which this reference refers, or <code>null</code>
     *         if this reference object has been cleared.
     */
    public Object get() {
        return _referent;
    }

    /**
     * Sets this reference object's referent.
     * 
     * @param  referent the object to which this reference refers.
     * @return <code>this</code>
     */
    public RealtimeReference set(Object referent) {
        _referent = referent;
        return this;
    }

    /**
     * Clears this reference object.
     */
    public void clear() {
        _referent = null;
    }

}