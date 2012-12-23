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
 * <p> Annotation indicating that a class can be initialized and its methods 
 *     used safely when new objects are allocated on the stack.</p>
 * 
 * <p> Classes with no static field and whose only dependencies are stack-safe
 *     classes are themself stack-safe. A class with static fields can be 
 *     stack-safe only if it can ensure that its static fields cannot leak 
 *     stack-allocated objects. In other words all its static fields 
 *     are created/updated using the {@link javolution.context.HeapContext 
 *     HeapContext}.</p>
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0, December 12, 2012
 */
@Documented
@Inherited
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.CONSTRUCTOR})
@Retention(RetentionPolicy.RUNTIME)
public @interface StackSafe {
}
