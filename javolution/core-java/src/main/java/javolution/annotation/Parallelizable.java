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
 * <p> Indicates that a class, a method or a field can be used by multiple 
 *     threads concurrently and whether or not it is 
 *     {@link RealTime#mutexFree() free of mutex}.</p>
 * 
 * <p> Classes with no internal fields or {@link javolution.lang.Immutable 
 *     immutable} are parallelizable without using 
 *     <a href="http://en.wikipedia.org/wiki/Mutual_exclusion>mutexes</a>.</p>
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0, December 12, 2012
 */
@Documented
@Inherited
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Parallelizable {
    
    /**
     * Indicates if this element can safely be used concurrently 
     * (default {@code true}).
     */
    boolean value() default true; 

    /**
     * Indicates if this element does not use any form of 
     * <a href="http://en.wikipedia.org/wiki/Mutual_exclusion>mutex</a> to 
     * access shared resources (default {@code true}). To avoid 
     * <a href="http://en.wikipedia.org/wiki/Priority_inversion">
     * priority inversion</a> and possibly unbounded response times,
     * a real-time VM (with priority inheritance) is recommended
     * when using {@link RealTime real-time} elements which are not mutex-free.
     */
    boolean mutexFree() default true;
  
    /**
     * Provides additional information (default {@code ""}).
     */
    String comment() default "";
 
}
