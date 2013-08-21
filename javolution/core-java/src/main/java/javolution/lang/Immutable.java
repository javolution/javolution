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
 * <p> An object capable of returning a {@link #value value} not subject or 
 *     susceptible of change or variation. For example, {@code Immutable<List>}
 *     has a {@code List} value which is guaranteed to be constant (not modifiable 
 *     by anybody). Classes implementing this interface do not need themselves to
 *     be unmodifiable. If the value and the class are the same, the 
 *     {@link ValueType} sub-interface can be implemented.  
 * [code]
 * class Polygon extends Shape implements ValueType<Shape> { // extends Immutable<Shape>
 *     private List<Point2D> vertices;
 *     public Polygon(Immutable<List<Point2D>> vertices) { // Immutable<List> has a constant List value.
 *         this.vertices = vertices.value(); // No defensive copying required (vertices.value() is certified constant).
 *     }
 *     public Polygon value() { return this; } // As per ValueType contract.
 * }[/code]</p>
 * <p> {@link javolution.util.FastCollection FastCollection/FastMap} have 
 *     direct support for immutable.
 * [code]
 * Polygon triangle = new Polygon(new FastTable<Point2D>().addAll(p1, p2, p3).toImmutable());[/code]</p>
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
     * Returns the constant value held by this object. 
     */
    T value();

}