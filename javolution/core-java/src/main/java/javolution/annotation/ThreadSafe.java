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
 * <p> Annotation indicating that a class (and any of its instances) can be used 
 *     by multiple threads concurrently.</p>
 * 
 * <p> Classes with no internal fields or immutable are typically thread-safe
 *     without locking. For others, if there is a possibility of thread blocking 
 *     due to internal locks, the {@link #useLock() useLock} attribute 
 *     should be set to <code>true</code>.</p> 
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0, December 12, 2012
 */
@Documented
@Inherited
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.CLASS)
public @interface ThreadSafe {

    /**
     * Indicates if there is a possibility that calling a method of the 
     * annotated class results in thread blocking due to internal locks 
     * (default <code>false</code>)
     */
    boolean useLock() default false;    

}
