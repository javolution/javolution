/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2004 - The Javolution Team (http://javolution.org/)
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.lang;
import j2me.lang.CharSequence;
import javolution.JavolutionError;
import javolution.realtime.RealtimeObject;

import java.io.IOException;

/**
 * <p> This class represents the base format for text parsing and 
 *     formatting; it uses <code>CharSequence</code> and <code>Appendable</code>
 *     interfaces for greater flexibility.</p>
 * 
 * <p> The current format (used by <code>valueOf(CharSequence)</code> or
 *     <code>toText()</code>) is typically context sensitive. For example:<pre>
 *     public class FooFormat extends Format {
 *         public static final FooFormat DEFAULT = ...; 
 *         private static final LocalContext.Variable CURRENT
 *               = new LocalContext.Variable(DEFAULT);
 *         public static FooFormat getInstance() {  
 *             return (FooFormat) CURRENT.getValue();
 *         }
 *         public static void setInstance(FooFormat format) {
 *             CURRENT.setValue(format);
 *         }
 *     }
 *     ...
 *     public class Foo {
 *         public static Foo valueOf(CharSequence csq) {
 *             return (Foo) FooFormat.getInstance().parse(csq);
 *         }
 *         public Text toText() {
 *             return FooFormat.getInstance().format(this);
 *         } 
 *     }</pre></p> 
 * 
 * <p> For parsing/formatting of primitive types, the {@link TypeFormat}
 *     utility class is recommended.</p>
 * 
 * <p> <i>This class is public domain (not copyrighted).</i></p>
 * 
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 1.0, October 4, 2004
 */
public abstract class TextFormat {

    /**
     * Default constructor.
     */
    protected TextFormat() {
    }
    
    /**
     * Formats an object into the specified <code>Appendable</code>.
     *
     * @param obj the object to format.
     * @param dest the <code>Appendable</code> destination.
     * @return the specified <code>Appendable</code>.
     * @throws IOException if an I/O exception occurs.
     */
    public abstract Appendable format(Object obj, Appendable dest)
            throws IOException;

    /**
     * Parses a portion of the specified <code>CharSequence</code> from the 
     * specified position to produce an object.
     * If parsing succeeds, then the index of the <code>pos</code> argument
     * is updated to the index after the last character used. 
     * 
     * @param csq the <code>CharSequence</code> to parse.
     * @param pos an object holding the parsing index.
     * @return an <code>Object</code> parsed from the character sequence. 
     * @throws IllegalArgumentException if the character sequence contains
     *         an illegal syntax.
     */
    public abstract Object parse(CharSequence csq, ParsePosition pos);

    /**
     * Returns the textual representation of the specified object
     * (convenience method). 
     * 
     * @param obj the object being formated.
     * @return <code>format(obj, TextBuilder.newInstance()).toText()</code> 
     */
    public final Text format(Object obj) {
        try {
            TextBuilder tb = TextBuilder.newInstance();
            format(obj, tb);
            return tb.toText();
        } catch (IOException e) {
            throw new JavolutionError(e);
        }
    }

    /**
     * Parses a whole character sequence from the beginning to 
     * produce an object (convenience method). 
     * 
     * @param csq the <code>CharSequence</code> to parse.
     * @return <code>parse(csq, ParsePosition.newInstance())</code> 
     * @throws IllegalArgumentException if the character sequence contains
     *         an illegal syntax or if the whole sequence has not been 
     *         completely parsed.
     */
    public final Object parse(CharSequence csq) {
        ParsePosition pos = ParsePosition.newInstance();
        Object obj = parse(csq, pos);
        if (pos.index == csq.length()) {
            return obj;
        } else {
            throw new IllegalArgumentException("Parsing of " + csq
                    + " incomplete (terminated at index: " + pos.index + ")");
        }
    }

    /**
     * This inner class represents a {@link TextFormat} parsing index.
     */
    public static final class ParsePosition extends RealtimeObject {

        /**
         * The index of the current parse position.
         */
        public int index;

        /**
         * Holds the parse position factory.
         */
        private static final Factory FACTORY = new Factory() {
            public Object create() {
                return new ParsePosition();
            }
        };

        /**
         * Default constructor.
         */
        private ParsePosition() {
        }

        /**
         * Returns a new/reused parse position with an index 
         * set to <code>0</code>.
         * 
         * @return a parse position object potentially allocated on the stack.
         */
        public static ParsePosition newInstance() {
            ParsePosition pp = (ParsePosition) FACTORY.object();
            pp.index = 0;
            return pp;
        }
    }
}