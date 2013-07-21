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
 * <p> Indicates that a class, a method or a field can be used by multiple 
 *     threads concurrently and whether or not it is 
 *     {@link Parallelizable#mutexFree() mutex-free} (not blocking).</p>
 * 
 * [code]
 * public class Operators {
 *    @Parallelizable
 *    public static final CollectionOperator<Object> ANY = new CollectionOperator<Object>() { ... }
 *    
 *    @Parallelizable(mutexFree = false, comment="Internal use of synchronization")
 *    public static final CollectionOperator<Object> MAX = new CollectionOperator<Object>() { ... }
 *    
 *    @Parallelizable(mutexFree = false, comment="Internal use of synchronization")
 *    public static final CollectionOperator<Object> MIN = new CollectionOperator<Object>() { ... }
 *    
 *    @Parallelizable
 *    public static final CollectionOperator<Boolean> AND = new CollectionOperator<Boolean>() { ... }
 *    
 *    @Parallelizable
 *    public static final CollectionOperator<Boolean> OR = new CollectionOperator<Boolean>() { ... }
 *    
 *    @Parallelizable(comment="Internal use of AtomicInteger")
 *    public static final CollectionOperator<Integer> SUM = new CollectionOperator<Integer>() { ... }
 * }[/code]
 *  
 * <p> Classes with no internal fields or {@link javolution.lang.Immutable 
 *     Immutable} are usually parallelizable and mutex-free.</p>
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0, July 21, 2013
 * @see <a href="http://en.wikipedia.org/wiki/Mutual_exclusion">Wikipedia: Mutual Exclusion</a>
 */
@Documented
@Inherited
@Target({ ElementType.TYPE, ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface Parallelizable {

    /**
     * Indicates if this element can safely be used concurrently 
     * (default {@code true}).
     */
    boolean value() default true;

    /**
     * Indicates if this element does not use any form of mutex to 
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
