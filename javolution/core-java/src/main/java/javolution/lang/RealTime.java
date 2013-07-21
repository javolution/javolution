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
 *     specified the worst case execution time is assumed to be constant).
 *     Real-time elements should support {@link javolution.context.StackContext 
 *     stack allocations} unless indicated {@link #stackSafe otherwise}.</p>
 *     
 * [code]
 * public class Comparators {
 *     @RealTime(limit = UNKNOWN)
 *     public static final EqualityComparator<Object> STANDARD = new StandardComparatorImpl<Object>();
 *     
 *     @RealTime(limit = CONSTANT)
 *     public static final EqualityComparator<Object> IDENTITY = new IdentityComparatorImpl<Object>();
 *     
 *     @RealTime(limit = LINEAR)
 *     public static final EqualityComparator<Object> ARRAY = new ArrayComparatorImpl();
 *     
 *     @RealTime(limit = LINEAR)
 *     public static final EqualityComparator<CharSequence> LEXICAL = new LexicalComparatorImpl();
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
public @interface RealTime {

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
     * Indicates if this element is safe to be used if object allocation
     * is performed on the {@link javolution.context.StackContext stack}.
     * Classes with no static fields or with static fields unmodifiable are 
     * usually stack-safe (constant static fields are always allocated in 
     * immortal memory by real-time VMs). Lazy initialization should of course
     * be avoided or forced to be executed outside the stack (either on the 
     * {@link javolution.context.HeapContext heap} or in
     * {@link javolution.context.ImmortalContext immortal memory}).</p>
     */
    boolean stackSafe() default true;

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
