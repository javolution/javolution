/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.text;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * <p> Specifies the default text format of a class (for parsing/formatting). 
 *     The default format is typically used by the {@link Object#toString()}
 *     method and can be locally overridden in the scope of a   
 *     {@link javolution.text.TextContext TextContext}.</p>
 *     
 * [code]
 * @DefaultTextFormat(Complex.Cartesian.class) 
 * public class Complex {
 *     public String toString() { // Uses the default format unless locally overridden.
 *         return TextContext.toString(this);
 *     }
 *     public static Complex valueOf(CharSequence csq) {
 *         return TextContext.parse(csq, Complex.class);
 *     }
 *     public static class Cartesian extends TextFormat<Complex> { ... }
 *     public static class Polar extends TextFormat<Complex> { ... }
 * }
 * ...
 * TextContext ctx = TextContext.enter(); // Enters a local textual context.
 * try {
 *     ctx.setFormat(Complex.class, new Complex.Polar()); // Configure the local context.
 *     System.out.println(complexMatrix); // Displays complex numbers in polar coordinates.
 * } finally {
 *     ctx.exit(); // Exits local context (reverts to previous Cartesian format).
 * }[/code]
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0, July 21, 2013
 */
@Documented
@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DefaultTextFormat {

    /**
     * Returns the default text format of the annotated class.
     */
    Class<? extends TextFormat<?>> value();

}
