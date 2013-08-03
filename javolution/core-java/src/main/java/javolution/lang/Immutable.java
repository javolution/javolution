/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.lang;

/**
 * <p> An object having a {@link #value value} not subject or susceptible of 
 *     change or variation (see {@link ValueType} for examples of immutable).
 *     This interface guarantees that the object returned by {@link #value value()}
 *     is constant, but not that instance of the class itself is constant.
 * [code]
 * class Polygon extends Shape implements Immutable<Shape> { // Here the class and value type are the same.
 *     private List<Point2D> vertices;
 *     public Polygon(Immutable<List<Point2D>> vertices) { // Immutable<List> is not necessarily a List !
 *         this.vertices = vertices.value(); // No defensive copying required (vertices.value() is certified constant).
 *     }
 *     public Polygon value() { return this; } 
 * }[/code]</p>
 * <p> {@link javolution.util.FastCollection FastCollection/FastMap} have 
 *     direct support for immutable.
 * [code]
 * Polygone triangle = new Polygon(new FastTable<Point2D>().addAll(p1, p2, p3).toImmutable());
 * [/code]</p>
 * 
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0, July 21, 2013
 * @param <T> The type of the immutable constant value.
 * @see <a href="http://en.wikipedia.org/wiki/Immutable_object">
 *      Wikipedia: Immutable Object<a>    
 * @see javolution.util.FastCollection#toImmutable
 */
public interface Immutable<T> {
    
    /**
     * Returns the constant value. 
     */
    T value();

}