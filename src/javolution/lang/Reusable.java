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
 * <p> This interfaces identifies mutable objects capable of being used again
 *     or repeatedly without incurring dynamic memory allocation. 
 *     Reusable instances may <b>still</b> resize after creation as long as the
 *     "extension parts" are "factory produced" and moved to the heap space
 *     (to allow for dynamic {@link javolution.realtime.AllocationProfile 
 *     profiling} and eventually pre-allocations of these parts).</p>
 *     
 * <p> Instances of this class can safely reside in permanent memory (e.g.<code>
 *     static</code>) or be an integral part of a higher level component.
 *     Once {@link #reset reset}, reusable objects behave as if they were
 *     brand-new.</p>   
 * 
 * <p> Finally, reusable objects can be allocated on the stack providing that
 *     their {@link javolution.realtime.ObjectFactory factory} cleanup 
 *     method calls the {@link #reset reset} method. For example:<pre>
 *     public class Foo extends RealtimeObject implements Reusable {
 *         static final Factory FACTORY = new Factory(Foo.class) {
 *             public Object create() {
 *                 return new Foo();
 *             }
 *             public void cleanup(Object obj) {
 *                 ((Foo)obj).reset();
 *             }
 *         };
 *         public static Foo newInstance() {
 *             return (Foo) FACTORY.object();
 *         } 
 *         ...
 *     }</pre></p>
 *        
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 3.0, February 6, 2005
 */
public interface Reusable {

    /**
     * Resets the internal state of this object to its default values.
     */
    void reset();

}