/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2004 - The Javolution Team (http://javolution.org/)
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.io;




/**
 * <p> This class represents a <code>C/C++ union</code>; it works in the same
 *     way as {@link Struct} (sub-class) except that all members are mapped
 *     to the same location in memory.</p>
 * <p> Here is an example of C union: <pre>
 *     union Number {
 *         int   asInt;
 *         float asFloat;
 *         char  asString[12];
 *     };</pre>
 *     And its Java equivalent:<pre>
 *     public class Number extends Union {
 *         Signed32   asInt    = new Signed32();
 *         Float32    asFloat  = new Float32();
 *         Utf8String asString = new Utf8String(12);
 *     }</pre>
 *     As for any {@link Struct}, fields are directly accessible:<pre>
 *     Number num = new Number();
 *     num.asInt.set(23);
 *     num.asString.set("23"); // Null terminated (C compatible)
 *     float f = num.asFloat.get();</pre>
 * 
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 1.0, October 4, 2004
 */
public abstract class Union extends Struct {

    /**
     * Default constructor.
     */
    public Union() {
    }

    ///////////////////////////////
    // No method, tagging class. //
    ///////////////////////////////
}