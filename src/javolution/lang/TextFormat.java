/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2005 - Javolution (http://javolution.org/)
 * All rights reserved.
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
 * <p> This class represents the base format for text parsing and formatting; 
 *     it uses <code>CharSequence</code> and <code>Appendable</code> interfaces
 *     for greater flexibility.</p>
 * 
 * <p> Changes to the current format (used by <code>valueOf(CharSequence)</code>,
 *     <code>toString()</code> or <code>toText()</code>) 
 *     can be {@link javolution.realtime.LocalContext locally scoped}.
 *     For example: <pre>
 *     public class FooFormat extends Format {
 *         static final FooFormat DEFAULT = ...; // Global default.
 *         static final LocalContext.Variable CURRENT  = new LocalContext.Variable(DEFAULT);
 *          
 *         public static FooFormat getInstance() {  
 *             return (FooFormat) CURRENT.getValue();
 *         }
 *          
 *         public static void setInstance(FooFormat format) {
 *             CURRENT.setValue(format);
 *         }
 *     }
 *     ...
 *     LocalContext.enter();
 *     try {
 *         FooFormat.setInstance(myFormat);
 *         System.out.println(foo); // Current thread displays foo using myFormat. 
 *     } finally {
 *         LocalContext.exit(); // Current thread reverts to previous format.
 *     }</pre></p>
 *      
 * <p> For parsing/formatting of primitive types, the {@link TypeFormat}
 *     utility class is recommended (fast with no intermediate 
 *     object creation).</p>
 * 
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle </a>
 * @version 2.2, January 24, 2005
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
     * specified position to produce an object. If parsing succeeds, then the
     * index of the <code>pos</code> argument is updated to the index after
     * the last character used.
     * 
     * @param csq the <code>CharSequence</code> to parse.
     * @param pos an object holding the parsing index.
     * @return an <code>Object</code> parsed from the character sequence.
     * @throws IllegalArgumentException if the character sequence contains 
     *         an illegal syntax.
     */
    public abstract Object parse(CharSequence csq, Cursor pos);

    /**
     * Returns the textual representation of the specified object (convenience
     * method).
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
     * Parses a whole character sequence from the beginning to produce an object
     * (convenience method). A temporary cursor is allocated from the "stack"
     * when executing in a {@link javolution.realtime.PoolContext PoolContext} 
     * and {@link Cursor#recycle recycled} to the "stack" after usage.
     * 
     * @param csq the <code>CharSequence</code> to parse.
     * @return <code>parse(csq, cursor)</code>
     * @throws IllegalArgumentException if the character sequence contains an
     *         illegal syntax or if the whole sequence has not been completely
     *         parsed.
     */
    public final Object parse(CharSequence csq) {
        Cursor cursor = Cursor.newInstance();
        try {
            Object obj = parse(csq, cursor);
            if (cursor.getIndex() == csq.length()) {
                return obj;
            } else {
                throw new IllegalArgumentException("Parsing of " + csq
                        + " incomplete (terminated at index: "
                        + cursor.getIndex() + ")");
            }
        } finally {
            cursor.recycle();
        }
    }

    /**
     * This class represents the parsing cursor. In case of parsing error, 
     * the cursor should be set to the location where the error occured.
     */
    public static final class Cursor extends RealtimeObject {
        
        /**
         * Holds the cursor factory.
         */
        private static final Factory FACTORY = new Factory() {
            public Object create() {
                return new Cursor();
            }
        };

        /**
         * Holds the cursor index.
         */
        private int _index;

        /**
         * Returns a temporary cursor allocated from the "stack" when executing 
         * in a {@link javolution.realtime.PoolContext PoolContext} and 
         * {@link #recycle recyclable} to the "stack".
         * 
         * @return a {@link javolution.realtime.ObjectFactory factory} produced 
         *         cursor instance.
         */
        public static Cursor newInstance() {
            Cursor cursor = (Cursor) FACTORY.object();
            cursor._index = 0;
            return cursor;
        }

        /**
         * Returns this cursor index.
         * 
         * @return the index of the next character to parse.
         */
        public int getIndex() {
            return _index;
        }

        /**
         * Sets the cursor index.
         * 
         * @param i the index of the next character to parse.
         */
        public void setIndex(int i) {
            _index = i;
        }

        /**
         * Increments the cursor index by the specified value.
         * 
         * @param i the increment value.
         */
        public void increment(int i) {
            _index += i;
        }
        
        /**
         * Recycles this cursor after usage.
         */
        public void recycle() {
            super.recycle();
        }
    }
}