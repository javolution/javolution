/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2004 - The Javolution Team (http://javolution.org/)
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.realtime;
import javolution.lang.Text;

/**
 * <p> This interface identifies classes whose instances can be allocated from
 *     and moved to different {@link ContextSpace ContextSpace} for higher
 *     performance and higher predictability.</p>
 * <p> For example, classes implementing this interface can be allocated 
 *     on the local stack to avoid generating garbage.</p>
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 1.0, October 4, 2004
 */
public interface Realtime {

    /**
     * Moves this real-time object as well as its real-time associations
     * to the specified destination context space.
     * 
     * <p><i> Sub-classes adding new real-time variable members should override
     *        this method to move these new members as well.</i></p> 
     * 
     * @param cs the context space to move this real-time object to.
     */
    void move(ContextSpace cs);

    /**
     * Returns the textual representation of this real-time object
     * (equivalent to <code>toString</code> except that the returned value
     * can be allocated from the local context space).
     * 
     * @return this object's textual representation.
     */
    Text toText();

    /**
     * This inner class represents a context space destination. 
     */
    public static class ContextSpace {
        
        /**
         * Identifies the {@link HeapContext heap context}.
         */
        public static final ContextSpace HEAP = new ContextSpace();
    
        /**
         * Identifies the {@link PoolContext local pool context}.
         */
        public static final ContextSpace LOCAL = new ContextSpace();
    
        /**
         * Identifies the {@link Context#getOuter() outer} pool context.
         */
        public static final ContextSpace OUTER = new ContextSpace();
    
        /**
         * Identifies a shared context space (usable by all threads).
         */
        public static final ContextSpace SHARED = new ContextSpace();
    
        /**
         * Default constructor (allows extension).
         */
        public ContextSpace() {}
    
    }    
}