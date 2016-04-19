/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.lang;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p> Indicates that an element has strict timing constraints and has a
 *     deterministic time behavior. The {@link #limit limit} indicator indicates 
 *     the evolution of the 
 *     <a href="http://en.wikipedia.org/wiki/Worst-case_execution_time">
 *     worst-case execution time</a> of a method with the size of the instance
 *     and its inputs (or only the size of the inputs for static methods).    
 * <pre>{@code
 * public class FastCollection<E> {
 *     {@literal@}Realtime(limit = LINEAR)
 *     public boolean contains(Object obj) { ... }
 *     
 *     {@literal@}Realtime(limit = N_SQUARE)
 *     public boolean containsAll(Collection<?> that) { ... }
 *     
 *     {@literal@}Realtime(limit = LINEAR, comment = "Could count the elements (e.g. filtered view).")
 *     public abstract int size();
 * }}</pre></p>      
 *     
 * <p> Analysis tools / compilers may produce warnings if program elements 
 *     use or override elements with incompatible real-time characteristics.</p>
 *          
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0, July 21, 2013
 * @see <a href="http://en.wikipedia.org/wiki/Real-time_computing">Real-Time Computing</a>
 */
@Documented
@Inherited
@Target({ ElementType.TYPE, ElementType.FIELD, ElementType.METHOD,
        ElementType.CONSTRUCTOR })
@Retention(RetentionPolicy.RUNTIME)
public @interface Realtime {

    /**
     * Indicates if this element has a bounded worst-case execution time
     * (default {@code true}).
     */
    boolean value() default true;

    /**
     * Returns the limit behavior for the worst-case execution time
     * (default {@link Limit#UNKNOWN}).
     */
    Limit limit() default Limit.UNKNOWN;

    /**
     * Provides additional information (default {@code ""}).
     */
    String comment() default "";

    /**
     * Identifies the limit behavior for the worst case execution time.
     */
    public enum Limit {

        /**
         * The worst case execution time is constant.
         */
        CONSTANT,

        /**
         * The worst case execution time is bounded in <i>O(log(n))</i> 
         * with <i>n</i> characteristic of the current size of the inputs.
         */
        LOG_N,

        /**
         * The worst case execution time is bounded in <i>O(n)</i> 
         * with <i>n</i> characteristic of the current size of the inputs.
         */
        LINEAR,

        /**
         * The worst case execution time is bounded in <i>O(n log(n))</i> 
         * with <i>n</i> characteristic of the current size of the inputs.
         */
        N_LOG_N,

        /**
         * The worst case execution time is bounded in <i>O(n²)</i> 
         * with <i>n</i> characteristic of the current size of the inputs.
         */
        N_SQUARE,

        /**
         * The limit behavior of the worst case execution time is unknown
         * or unspecified.
         */
        UNKNOWN,
    }
}
