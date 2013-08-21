/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util.function;


/**
 * An object which can be divided in distinct parts and on which the same 
 * action may be performed on the parts rather than the whole. 
 *  
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0, July 21, 2013
 */
public interface Splittable<T> {

    /** 
     * Executes a read-only action on the specified part of this object.
     *       
     * @param action the read-only action.
     * @param part this object or a part of it.
     * @throws UnsupportedOperationException if the action tries to update the 
     *         specified part.
     */
    void perform(Consumer<T> action, T part);

    /** 
     * Returns {@code n} distinct parts of this object. 
     * This method may return an array of size less than {@code n}
     * (e.g. an array of size one if this object cannot split).
     *   
     * @param n the number of parts.
     * @param threadsafe {@code true} if the returned parts can be updated 
     *        concurrently;  {@code false} otherwise. 
     * @return the distinct parts (or views) for this object.
     * @throws IllegalArgumentException if {@code n < 1}
     */
    T[] split(int n, boolean threadsafe);

    /** 
     * Executes an update action on the specified part of this object. 
     * Any change to the part is reflected in the whole (this object). 
     *       
     * @param action the action authorized to update this object part.
     * @param part this object or a part of it.
     */
    void update(Consumer<T> action, T part);

}