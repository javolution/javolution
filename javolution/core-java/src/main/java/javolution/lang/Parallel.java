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
 * <p> Indicates if the processing of an element can be performed
 *     concurrently using multiple threads.
 *     
 * <pre>{@code
 * {@literal@}Parallel
 * public abstract class FastCollection<E> {
 * 
 *    {@literal@}Parallel
 *    public void forEach(Consumer<? super E> consumer) { ... }

 *    {@literal@}Parallel
 *    public boolean removeIf(Predicate<? super E> filter) { ... }
 *    
 *    {@literal@}Parallel
 *    public E reduce(BinaryOperator<E> operator) { ... }
 *    
* }}</pre></p>
 *  
 * <p> Classes with no internal fields or Immutable are usually parallelizable and mutex-free.</p>
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 7.0, July 21, 2013
 */
@Documented
@Inherited
@Target({ ElementType.TYPE, ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface Parallel {

    /**
     * Indicates if this element may perform its processing in parallel 
     * (default {@code true}).
     */
    boolean value() default true;

    /**
     * Provides additional information (default {@code ""}).
     */
    String comment() default "";

}
