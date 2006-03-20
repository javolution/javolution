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
import j2me.text.ParsePosition;
import javolution.realtime.ObjectFactory;

import java.io.IOException;

/**
 * <p> This class represents the base format for text parsing and formatting; 
 *     it supports {@link CharSequence} and {@link javolution.lang.Appendable} 
 *     interfaces for greater flexibility.</p>
 * 
* <p> The default format (used by <code>valueOf(CharSequence)</code>,
 *     <code>toString()</code> or <code>toText()</code>) 
 *     can be {@link javolution.realtime.LocalContext locally scoped}.
 *     For example:[code]
 *     public class Complex extends RealtimeObject {
 *         public static final LocalReference<TextFormat<Complex>> FORMAT 
 *             = new LocalReference<TextFormat<Complex>>(new TextFormat<Complex>() {
 *                 ... // Default format (cartesien form).
 *             });
 *         public Complex valueOf(CharSequence csq) {
 *             return FORMAT.get().parse(csq);
 *         }
 *         public Text toText() {
 *             return FORMAT.get().format(this);
 *         }
 *     }
 *     ...
 *     Matrix<Complex> M = ...;
 *     LocalContext.enter();
 *     try {
 *         Complex.FORMAT.set(POLAR); // Current thread displays complex numbers
 *         System.out.prinln(M);      // using the polar form.
 *     } finally {
 *         LocalContext.exit(); // Current thread reverts to previous format.
 *     }[/code]</p>
 *      
 * <p> For parsing/formatting of primitive types, the {@link TypeFormat}
 *     utility class is recommended.</p>
 * 
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle </a>
 * @version 3.7, January 14, 2006
 */
public abstract class TextFormat/*<T>*/{

    /*
     * Default constructor.
     */
    protected TextFormat() {
    }

    /**
     * Formats the specified object into an <code>Appendable</code> 
     * 
     * @param obj the object to format.
     * @param dest the appendable destination.
     * @return the specified <code>Appendable</code>.
     * @throws IOException if an I/O exception occurs.
     */
    public abstract Appendable format(Object/*T*/obj, Appendable dest)
            throws IOException;

    /**
     * Parses a portion of the specified <code>CharSequence</code> from the
     * specified position to produce an object. If parsing succeeds, then the
     * index of the <code>cursor</code> argument is updated to the index after
     * the last character used. 
     * 
     * @param csq the <code>CharSequence</code> to parse.
     * @param cursor the cursor holding the current parsing index.
     * @return the object parsed from the specified character sub-sequence.
     * @throws RuntimeException if any problem occurs while parsing the 
     *         specified character sequence (e.g. illegal syntax).
     */
    public abstract Object/*T*/parse(CharSequence csq, Cursor cursor);

    /**
     * Formats the specified object to a {@link Text} instance
     * (convenience method).
     * 
     * @param obj the object being formated.
     * @return the text representing the specified object.
     */
    public final Text format(Object/*T*/obj) {
        try {
            TextBuilder tb = TextBuilder.newInstance();
            format(obj, tb);
            return tb.toText();
        } catch (IOException e) {
            throw new Error(); // Should never happen.
        }
    }

    /**
     * Parses a whole character sequence from the beginning to produce an object
     * (convenience method). 
     * 
     * @param csq the whole character sequence to parse.
     * @return the corresponding object.
     * @throws IllegalArgumentException if the specified character sequence 
     *        cannot be fully parsed.
     */
    public final Object/*T*/parse(CharSequence csq) {
        Cursor cursor = Cursor.newInstance();
        try {
           Object/*T*/obj = parse(csq, cursor);
           if (cursor.getIndex() != csq.length())
               throw new IllegalArgumentException("Parsing of " + csq
                      + " incomplete (terminated at index: " + cursor.getIndex()
                    + ")");
           return obj;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("Cannot parse \"" + csq +
                    "\" (" + e.toString() + ")");
        } finally {
           cursor.recycle();
        }
    }

    /**
     * This class represents a parsing cursor.
     */
    public static class Cursor extends ParsePosition {

        /**
         * Holds the cursor factory.
         */
        private static final ObjectFactory FACTORY = new ObjectFactory() {
            public Object create() {
                return new Cursor();
            }
        };

        /**
         * Holds the cursor index.
         */
        private int _index;

        /**
         * Default constructor.
         */
        private Cursor() {
            super(0);
        }

        /**
         * Returns a new cursor instance (possibly recycled).
         * 
         * @return a cursor instance whose index is <code>0</code>.
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
        public final int getIndex() {
            return _index;
        }

        /**
         * Sets the cursor index.
         * 
         * @param i the index of the next character to parse.
         */
        public final void setIndex(int i) {
            _index = i;
        }

        /**
         * Indicates if this cursor has not yet reached the end of the specified 
         * character sequence.
         * 
         * @param csq the character sequence iterated by this cursor.
         * @return <code>this.getIndex() < csq.length()</code>
         */
        public final boolean hasNext(CharSequence csq) {
            return _index < csq.length();
        }

        /**
         * Returns the next character at the cursor position in the specified 
         * character sequence and increments the cursor position by one.
         * 
         * @param csq the character sequence iterated by this cursor.
         * @return the character at the cursor position.
         * @throws IndexOutOfBoundsException if <code>(this.getIndex() &lt; 0) 
         *         || (this.getIndex(() &gt;= csq.length())</code>
         */
        public final char next(CharSequence csq) {
            return csq.charAt(_index++);
        }

        /**
         * Moves this cursor forward until it points to a character 
         * different from the character specified. 
         * 
         * @param c the character to skip.
         * @param csq the character sequence iterated by this cursor.
         * @return <code>true</code> if this cursor points to a character 
         *         different from the ones specified; <code>false</code> 
         *         otherwise (e.g. end of sequence reached). 
         */
        public final boolean skip(char c, CharSequence csq) {
            int length = csq.length();
            while ((_index < length) && (csq.charAt(_index) == c)) {
                _index++;
            }
            return _index < length;
        }

        /**
         * Moves this cursor forward until it points to a character 
         * different from any of the character in the specified set. 
         * For example: [code]
         *  // Reads numbers separated by tabulations or spaces.
         *  FastTable<Integer> numbers = new FastTable<Integer>();
         *  while (cursor.skip(CharSet.SPACE_OR_TAB, csq)) {
         *      numbers.add(TypeFormat.parseInt(csq, cursor));
         *  }[/code]   
         * 
         * @param charSet the character to skip.
         * @param csq the character sequence iterated by this cursor.
         * @return <code>true</code> if this cursor points to a character 
         *         different from the ones specified; <code>false</code> 
         *         otherwise (e.g. end of sequence reached). 
         */
        public final boolean skip(CharSet charSet, CharSequence csq) {
            int length = csq.length();
            while ((_index < length) && (charSet.contains(csq.charAt(_index)))) {
                _index++;
            }
            return _index < length;
        }

        /**
         * Increments the cursor index by one.
         */
        public final void increment() {
            _index++;
        }

        /**
         * Increments the cursor index by the specified value.
         * 
         * @param i the increment value.
         */
        public final void increment(int i) {
            _index += i;
        }

        /**
         * Recycles this cursor for reuse.
         */
        public final void recycle() {
            FACTORY.currentPool().recycle(this);
        }
    }
}