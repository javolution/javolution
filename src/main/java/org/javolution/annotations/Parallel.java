/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates if the actual processing of a method can be performed concurrently using multiple threads.
 *     
 * ```java
 * public abstract class FastCollection<E> {
 * 
 *    ​@Parallel
 *    public void forEach(Consumer<? super E> consumer) { ... }
 *    
 *    ​@Parallel
 *    public boolean removeIf(Predicate<? super E> filter) { ... }
 *    
 *    ​@Parallel
 *    public E reduce(BinaryOperator<E> operator) { ... }
 *    
 *    ​@Parallel
 *    public boolean contains(Object obj) { ... }
 * }
 * ```
 *  
 * @author  <jean-marie@dautelle.com>
 * @version 7.0, July 21, 2013
 */
@Documented
@Inherited
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Parallel {

    /**
     * Indicates if this element may perform its processing in parallel (default {@code true}).
     */
    boolean value() default true;

    /**
     * Provides additional information (default {@code ""}).
     */
    String comment() default "";

}
