/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p> Annotation specifying the  
 *     <a href="http://en.wikipedia.org/wiki/Worst-case_execution_time">
 *     worst case execution time</a> limit behavior when the input size increases.
 *     For real-time systems that worst case execution time is always bounded.</p>
 *          
 * <p> Analysis tools / compilers may produce warnings if program elements 
 *     use or override elements with incompatible behavioral limits.</p>
 *          
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0, December 12, 2012
 * @see javolution.context.StackContext
 */
@Documented
@Inherited
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.CONSTRUCTOR})
@Retention(RetentionPolicy.RUNTIME)
public @interface RealTime {
    
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
         * Worst case execution time is unknown.
         */
        UNKNOWN,
    }

    
    /**
     * Returns the limit behavior for the worst case execution time.
     * By default, the worst case execution time is assumed constant.
     */
    Limit value() default Limit.CONSTANT;
  
}
