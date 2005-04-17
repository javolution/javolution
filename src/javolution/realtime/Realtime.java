/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2005 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.realtime;
import javolution.lang.Text;

/**
 * <p> This interface identifies classes whose instances can be moved to 
 *     different {@link ObjectSpace ObjectSpace} for higher
 *     performance and higher predictability.</p>
 *     
 * <p> Real-time objects may contain references (direct or indirect) to
 *     other real-time objects. Whether these objects being referenced 
 *     have to be moved along when a real-time object is moved to a different 
 *     object space depends whether or not the referenced objects are heap 
 *     allocated. Heap allocated objects (such as intrinsic data structures) are
 *     moved implicitly with the object; but non-heap objects (external objects)
 *     might have to be moved explicitly. For example:<pre>
 *     public class Person extends RealtimeObject  { // Implements Realtime.
 *         private FastList children = new FastList(); // Also Realtime.
 *         ...
 *         public boolean move(ObjectSpace os) { 
 *             if (super.move(os)) { // Propagates to external references.
 *                 // The children list is intrinsic (heap allocated) but 
 *                 // it contains references to external persons (the children).
 *                 for (Iterator i=children.fastIterator(); i.hasNext();) {
 *                      ((Person)i.next()).move(os);
 *                 }
 *                 return true;
 *             }
 *             return false;
 *         }
 *     }</pre></p> 
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 3.0, February 16, 2005
 */
public interface Realtime {

    /**
     * Moves this real-time object to the specified object space.
     * 
     * @param os the object space to move this real-time object to.
     * @return <code>true</code> if this move has to be propagated to 
     *         non-heap external references; <code>false</code> otherwise.
     */
    boolean move(ObjectSpace os);

    /**
     * Returns the textual representation of this real-time object
     * (equivalent to <code>toString</code> except that the returned value
     * can be allocated from the local context space).
     * 
     * @return this object's textual representation.
     */
    Text toText();

    /**
     * This inner class represents an object space destination.
     * Applications may create their own object space. Here are few examples
     * of possible spaces with their defining characteristics.<UL>
     * <LI><B>AGING:</B> Objects are accessible by all; recycling is 
     *     done when the objects are old enough.<BR>Constraint: 
     *     It assumes that old objects become obsolete and are not referenced 
     *     anymore.
     * <LI><B>FLIP:</B> Objects are accessible by all; recycling 
     *     is done when the object space is flipped.<BR>Constraint:
     *     Objects from previous period (before flip) are discarded.</LI>
     * </UL> 
     */
    public static class ObjectSpace {
        
        /**
         * Identifies the {@link HeapContext heap} space;
         * objects are accessible by all; they are indirectly recycled 
         * through garbage collection (default space).
         */
        public static final ObjectSpace HEAP = new ObjectSpace();
    
        /**
         * Identifies the {@link PoolContext stack} space;
         * objects are accessible by the current thread only; 
         * they are recycled when the current thread 
         * {@link PoolContext#exit exits} the {@link PoolContext} scope
         * where the object has been {@link ObjectFactory factory} produced.
         */
        public static final ObjectSpace LOCAL = new ObjectSpace();
    
        /**
         * Identifies the {@link Context#getOuter() outer} object space; 
         * it is the object space after {@link PoolContext#exit exiting} 
         * the current {@link PoolContext} (typically the {@link #HEAP}
         * or another {@link #LOCAL} space).
         */
        public static final ObjectSpace OUTER = new ObjectSpace();
    
        /**
         * Identifies the hold space; objects are accessible by all; 
         * they are moved back to their original space when not held
         * anymore (internal counter). 
         */
        public static final ObjectSpace HOLD = new ObjectSpace();
    
        /**
         * Default constructor (allows extension).
         */
        public ObjectSpace() {}
    
    }    
}