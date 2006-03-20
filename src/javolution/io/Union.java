/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2005 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.io;

/**
 * <p> This class represents a <code>C/C++ union</code>; it works in the same
 *     way as {@link Struct} (sub-class) except that all members are mapped
 *     to the same location in memory.</p>
 * <p> Here is an example of C union: [code]
 *     union Number {
 *         int   asInt;
 *         float asFloat;
 *         char  asString[12];
 *     };[/code]
 *     And its Java equivalent:[code]
 *     public class Number extends Union {
 *         Signed32   asInt    = new Signed32();
 *         Float32    asFloat  = new Float32();
 *         Utf8String asString = new Utf8String(12);
 *     }[/code]
 *     As for any {@link Struct}, fields are directly accessible:[code]
 *     Number num = new Number();
 *     num.asInt.set(23);
 *     num.asString.set("23"); // Null terminated (C compatible)
 *     float f = num.asFloat.get();[/code]
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

    /**
     * Returns <code>true</code>.
     * 
     * @return <code>true</code>
     */
    public final boolean isUnion() {
        return true;
    }
}