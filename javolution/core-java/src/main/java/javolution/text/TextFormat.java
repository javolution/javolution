/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.text;

import java.io.IOException;

import javolution.lang.Parallelizable;

/**
 * <p> The service for plain text parsing and formatting;
 *     it supports {@link CharSequence} and {@link Appendable} interfaces 
 *     for greater flexibility.</p>
 * 
 * <p> Instances of this class are typically retrieved from the 
 *     current {@link TextContext} (OSGi service or not).
 * [code]
 * @DefaultTextFormat(Complex.Cartesian.class) 
 * public class Complex extends Number {
 *     public static Complex valueOf(CharSequence csq) {
 *         return TextContext.getFormat(Complex.class).parse(csq);
 *     }
 *     public String toString() {
 *         return TextContext.getFormat(Complex.class).format(this);
 *     }
 *     public static class Cartesian extends javolution.text.TextFormat<Complex> { ... }
 *     public static class Polar extends javolution.text.TextFormat<Complex> { ... }
 * }[/code]</p>
 * 
 * <p> Text formats can be locally overridden.
 * [code]
 * TextContext ctx = TextContext.enter();
 * try {
 *      ctx.setFormat(Complex.class, Complex.Polar.class); // No impact on others threads.
 *      System.out.println(complexMatrix); // Displays complex numbers in polar coordinates.
 * } finally {
 *      ctx.exit(); // Reverts to previous cartesian format for complex numbers.
 * }[/code]</p>
 *
 * <p> For parsing/formatting of primitive types, the {@link TypeFormat}
 *     utility class is recommended.</p>
 *     
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle </a>
 * @version 6.0, July 21, 2013
 */
@Parallelizable
public abstract class TextFormat<T> {

    /**
     * Reads a portion of the specified <code>CharSequence</code> from the
     * specified cursor position to produce an object. If parsing succeeds, 
     * then the index of the <code>cursor</code> argument is updated to the 
     * index after the last character used. 
     * 
     * @param csq the character sequence to parse.
     * @param cursor the cursor holding the current parsing index.
     * @return the object parsed.
     * @throws IllegalArgumentException if the syntax of the specified 
     *         character sequence is incorrect.
     * @throws UnsupportedOperationException if parsing is not supported.
     */
    public abstract T parse(CharSequence csq, Cursor cursor);

    /**
     * Formats the specified object into an <code>Appendable</code> 
     * 
     * @param obj the object to format.
     * @param dest the appendable destination.
     * @return the specified <code>Appendable</code>.
     */
    public abstract Appendable format(T obj, Appendable dest)
            throws IOException;

    /**
     * Convenience method to parse the whole character sequence; if there are 
     * unread extraneous characters after parsing then an exception is raised.
     * 
     * @param csq the <code>CharSequence</code> to parse from the first character
     *        to the last.
     * @throws IllegalArgumentException if the syntax of the specified 
     *         character sequence is incorrect or if there are extraneous
     *         characters at the end not parsed.
     */
    public T parse(CharSequence csq) throws IllegalArgumentException {
        Cursor cursor = new Cursor();
        T obj = parse(csq, cursor);
        if (!cursor.atEnd(csq))
            throw new IllegalArgumentException("Extraneous character(s) \""
                    + cursor.tail(csq) + "\"");
        return obj;
    }

    /**
    * Convenience method to format the specified object to a {@link TextBuilder};
    * unlike the abstract format method, this method does not throw {@link IOException}.
    * 
    * @param obj the object to format.
    * @param dest the appendable destination.
    * @return the specified <code>TextBuilder</code>.
    */
    public TextBuilder format(T obj, TextBuilder dest) {
        try {
            this.format(obj, (Appendable) dest);
            return dest;
        } catch (IOException e) {
            throw new Error(e); // Cannot happens.
        }
    }

    /**
     * Convenience method to format the specified object to a {@link String}.
     * 
     * @param obj the object to format.
     * @return the formatting result as a string.
     */
    public String format(T obj) {
        return this.format(obj, new TextBuilder()).toString();

    }
}
