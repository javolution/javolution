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
 * <p> Annotation indicating when applied to a class that all its methods 
 *     and fields are safe to be used while executing
 *     in a {@link javolution.context.StackContext StackContext}.</p>
 *     
 * <p> Classes with no static fields or with static fields unmodifiable are 
 *     usually {@link StackSafe} (when running on a real-time VM static fields
 *     are being allocated in immortal memory). Lazy initialization of static
 *     fields should be avoided or forced to be executed on the 
 *     {@link javolution.context.HeapContext heap} or in
 *     {@link javolution.context.ImmortalContext immortal} memory.</p>
 *     
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0, December 12, 2012
 * @see javolution.context.StackContext
 */
@Documented
@Inherited
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.CONSTRUCTOR})
@Retention(RetentionPolicy.RUNTIME)
public @interface StackSafe {


}
