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
 * <p> This interface identifies classes whose instances can be allocated from
 *     and moved to different {@link ObjectSpace ObjectSpace} for higher
 *     performance and higher predictability.</p>
 * <p> For example, classes implementing this interface can be allocated 
 *     on the local "stack" to avoid generating garbage.</p>
 * <p> {@link Realtime} objects follow specific rules with regard to external
 *     references. These rules can be summarize by the following table:
 *     <TABLE border="1" summary="Real-Time Accessibility Rules.">
 *          <CAPTION><EM>Real-Time Accessibility Rules</EM></CAPTION>
 *          <TR>
 *            <TD>Reference\Object</TD>
 *            <TD>{@link ObjectSpace#HEAP Heap}</TD>
 *            <TD>{@link ObjectSpace#LOCAL Local}</TD>
 *            <TD>{@link ObjectSpace#OUTER Outer}</TD>
 *            <TD>{@link ObjectSpace#HOLD Hold}</TD>
 *          </TR>
 *          <TR>
 *            <TD>{@link ObjectSpace#HEAP Heap}</TD>
 *            <TD>Y</TD><TD>Y</TD><TD>Y</TD><TD>Y</TD>
 *          </TR>
 *          <TR>
 *            <TD>{@link ObjectSpace#LOCAL Local}</TD>
 *            <TD>N</TD><TD>Y</TD><TD>N</TD><TD>Y</TD>
 *          </TR>
 *          <TR>
 *            <TD>{@link ObjectSpace#OUTER Outer}</TD>
 *            <TD>N</TD><TD>Y</TD><TD>Y</TD><TD>Y</TD>
 *          </TR>
 *          <TR>
 *            <TD>{@link ObjectSpace#HOLD Hold}</TD>
 *            <TD>N</TD><TD>Y</TD><TD>N</TD><TD>Y</TD>
 *          </TR>
 *       </TABLE>
 *     To ensure that these rules are respected, {@link Realtime real-time} 
 *     objects may have to override the {@link #move} method when referring 
 *     to non-heap objects. For example:<pre>
 *     public class Foo implements Realtime {
 *         private Realtime externalReference; // Might not be on the heap.
 *         public boolean move(ObjectSpace space) {
 *             if (super.move(space)) { // Propagates.
 *                 externalReference.move(os);
 *             }
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
     * of possible {@link ObjectSpace} with their defining characteristics.<UL>
     * <LI><CODE>SHARED:</CODE> Objects are accessible by all; recycling 
     *     is done when the internal sharing counters drop to zero
     *     (Constraint: Circular references are not allowed).</LI>
     * <LI><CODE>AGING:</CODE> Objects are accessible by all; recycling is 
     *     done when the objects is old enough (Constraint: It assumes that
     *     old objects become obsolete and are not referenced anymore).
     * </UL> 
     */
    public static class ObjectSpace {
        
        /**
         * Identifies the {@link HeapContext heap} space;
         * objects are recycled through garbage collection.
         */
        public static final ObjectSpace HEAP = new ObjectSpace();
    
        /**
         * Identifies the {@link PoolContext stack} space;
         * objects are recycled when the current thread 
         * {@link PoolContext#exit exits} its current {@link PoolContext}.
         */
        public static final ObjectSpace LOCAL = new ObjectSpace();
    
        /**
         * Identifies the {@link Context#getOuter() outer} object space; 
         * it is the object space after {@link PoolContext#exit exiting} 
         * the current {@link PoolContext}.
         */
        public static final ObjectSpace OUTER = new ObjectSpace();
    
        /**
         * Identifies the {@link PoolContext hold} space; similar to the 
         * {@link #LOCAL} space except that objects are not recycled upon
         * the next {@link PoolContext#exit} but moved back to the 
         * {@link #LOCAL} space instead.
         */
        public static final ObjectSpace HOLD = new ObjectSpace();
    
        /**
         * Default constructor (allows extension).
         */
        public ObjectSpace() {}
    
    }    
}