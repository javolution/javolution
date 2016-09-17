/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.lang;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p> Indicates that the state of a class instance, a static field, a method return value or a parameter 
 *     will not change ever. For objects tagged {@code Constant}, defensive copy is unnecessary. 
 * <pre>{@code
 * {@literal@}Constant(comment="Immutable")
 * class Polygon extends Shape  { 
 *     private Point2D[] vertices;
 *     public Polygon({@literal@}Constant Point2D... vertices) { 
 *         this.vertices = vertices; // No defensive copying required.
 *     }
 *     {@literal@}Constant 
 *     List<Point2D> getVertices() { 
 *         return ConstantTable.of(vertices); // Unmodifiable array wrapper. 
 *     }
 * }}</pre></p>
 * 
 * <p> The constant annotation is primarily for API documentation purpose but static analyzers can be 
 *     used to detect rules violations. 
 * <pre>{@code
 * Polygon triangle = new Polygon(p1, p2, p3); // Ok, literals are always constant.
 * Point2D[] vertices = new Point2D[] { p1, p2, p3 };
 * triangle = new Polygon(vertices); // vertices object is now tagged as constant.
 * vertices[0] = null; // Rule violation!     
 * }</pre></p>
 *   
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 7.0 September 13, 2015
 */
@Documented
@Inherited
@Target({ ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER,
	       ElementType.LOCAL_VARIABLE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Constant  {

    /**
     * Indicates if this element is constant (default {@code true}).
     * @return true if constant
     */
    boolean value() default true;

    /**
     * Provides additional information (default {@code ""}).
     * @return comment
     */
    String comment() default "";

}
