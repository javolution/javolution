/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2006 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.context;
import javolution.text.Text;

/**
 * <p> This interface identifies classes whose instances can be moved to 
 *     different {@link ObjectSpace ObjectSpace} for higher
 *     performance and higher predictability.</p>
 *     
 * <p> Real-time objects may contain references to external real-time objects.
 *     These referenced objects might have to be moved along when the real-time
 *     object is moved. For example:[code]
 *     public class Person extends RealtimeObject  { // Implements Realtime.
 *         private Person _mother; // External.
 *         private Person _father; // External.
 *         private final FastList<Person> _children = new FastList<Person>(); // Internal list.  
 *         ...
 *         public boolean move(ObjectSpace os) { 
 *             if (super.move(os)) { // Propagates to referenced objects.
 *                 _mother.move(os);
 *                 _father.move(os);
 *                 // The children list itself is part of this object
 *                 // and does not require any particular action, but it 
 *                 // references external objects which need to be moved along.
 *                 for (Person child : _children) child.move(os);
 *                 return true; // For sub-classes to do their part.
 *             }
 *             return false;
 *         }
 *     }[/code]</p> 
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 3.7, January 1, 2006
 */
public interface Realtime {

    /**
     * Moves this real-time object to the specified object space.
     * 
     * @param os the object space to move this real-time object to.
     * @return <code>true</code> if the move has to be propagated to 
     *         external real-time references; <code>false</code> otherwise.
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
     * This class represents an object space which determinates when object 
     * recycling occurs. <ul>
     * <li>{@link #HEAP}: Associated memory recycled through garbage collection
     *                    when the object is not reachable (default).</li>
     * <li>{@link #STACK}: Recycled when the current thread exits the scope of 
     *                     the {@link PoolContext} where the object has been 
     *                     "factory produced".</li>
     * <li>{@link #HOLD}: Not recycled, moved back to its original space when 
     *                    internal preserve count drops to zero.</li>
     * </li>
     * Applications may create their own object space. Here are few examples
     * of possible spaces with their defining characteristics.<ul>
     * <li><b>AGING:</b> Recycling done when the objects are old enough.<br>
     *                   <b>Constraint:</b> It assumes that old objects become 
     *                   obsolete and are not referenced anymore.</li>
     * <li><b>FLIP:</b> Recycling done when the object space is flipped.<br>
     *                  <b>Constraint:</b> Objects from previous period 
     *                  (before flip) are discarded.</li>
     * </ul> 
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
        public static final ObjectSpace STACK = new ObjectSpace();
    
        /**
         * Identifies the current {@link Context#getOuter() outer} object space; 
         * it is the object space after {@link PoolContext#exit exiting} 
         * the current {@link PoolContext} (typically the {@link #HEAP}
         * or another {@link #STACK} space).
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