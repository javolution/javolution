/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.lang;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p> Indicates if an element has a bounded  
 *     <a href="http://en.wikipedia.org/wiki/Worst-case_execution_time">
 *     worst-case execution time</a>. The {@link #limit limit} behavior
 *     of the execution time with the input size may be specified (if no limit 
 *     specified the worst case execution time is assumed to be constant).</p>
 *     
 * [code]
 * public class Equalities {
 *     @Realtime(limit = UNKNOWN)
 *     public static final Equality<Object> STANDARD = new StandardComparatorImpl<Object>();
 *     
 *     @Realtime(limit = CONSTANT)
 *     public static final Equality<Object> IDENTITY = new IdentityComparatorImpl<Object>();
 *     
 *     @Realtime(limit = LINEAR)
 *     public static final Equality<Object> ARRAY = new ArrayComparatorImpl();
 *     
 *     @Realtime(limit = LINEAR)
 *     public static final Equality<CharSequence> LEXICAL = new LexicalComparatorImpl();
 * }[/code]      
 *     
 * <p> Analysis tools / compilers may produce warnings if program elements 
 *     use or override elements with incompatible real-time characteristics.</p>
 *     
 * <p> Note: For multi-cores systems, if a real-time element is {@link  Parallelizable}
 *     but not {@link  Parallelizable#mutexFree() mutex-free}, response 
 *     time even for high priority threads may be unbounded due to 
 *     <a href="http://en.wikipedia.org/wiki/Priority_inversion">priority 
 *     inversion</a>. This is no longer the case when running on real-time 
 *     VMs due to their support for priority inheritance.</p>
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
     * (default {@link Limit#CONSTANT}).
     */
    Limit limit() default Limit.CONSTANT;

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
