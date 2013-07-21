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
 * <p> An object whose value is not subject or susceptible of change or 
 *     variation. Once a class is declared immutable, any subclass must 
 *     ensure immutability as well.</p>
 *     
 * <p> {@link Immutable} objects can safely be used in a multi-threaded 
 *     environment and <b>do not require defensive copying</b>.
 * [code]
 * class Polygon extends Shape implements Immutable<Shape> {
 *     private List<Point2D> vertices;
 *     public Polygon(Immutable<List<Point2D>> vertices) {
 *         this.vertices = vertices.value(); // No defensive copying required.
 *     }
 *     public Polygon value() { return this; }
 * }
 * ...
 * // FastCollection/FastMap can be converted directly to immutable.
 * Polygone triangle = new Polygon(new FastTable<Point2D>().addAll(p1, p2, p3).toImmutable());
 * ...[/code]</p>
 * @see <a href="http://en.wikipedia.org/wiki/Immutable_object">
 *      Wikipedia: Immutable Object<a>    
 * @see javolution.util.FastCollection#toImmutable
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0, July 21, 2013
 */
public interface Immutable<T> {
    
    /**
     * Returns the unmodifiable value.
     */
    T value();

}