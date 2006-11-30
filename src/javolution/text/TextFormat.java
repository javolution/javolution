/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2006 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.text;

import j2me.lang.CharSequence;
import j2me.text.ParsePosition;
import javolution.context.ObjectFactory;

import java.io.IOException;

/**
 * <p> This class represents the base format for text parsing and formatting; 
 *     it supports {@link CharSequence} and {@link javolution.text.Appendable} 
 *     interfaces for greater flexibility.</p>
 * 
 * <p> The default format (used by <code>valueOf(CharSequence)</code>,
 *     <code>toString()</code> or <code>toText()</code>) 
 *     can be {@link javolution.context.LocalContext locally scoped}.
 *     For example:[code]
 *     public class Complex extends RealtimeObject {
 *         public static final LocalContext.Reference<TextFormat<Complex>> FORMAT 
 *             = new LocalContext.Reference<TextFormat<Complex>>(new TextFormat<Complex>() {
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
    public abstract Appendable format(Object/*{T}*/obj, Appendable dest)
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
    public abstract Object/*{T}*/parse(CharSequence csq, Cursor cursor);

    /**
     * Formats the specified object to a {@link Text} instance
     * (convenience method).
     * 
     * @param obj the object being formated.
     * @return the text representing the specified object.
     */
    public final Text format(Object/*{T}*/obj) {
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
    public final Object/*{T}*/parse(CharSequence csq) {
        Cursor cursor = Cursor.newInstance(0, csq.length());
        Object/*{T}*/obj = parse(csq, cursor);
        if (cursor.hasNext())
            throw new IllegalArgumentException("Incomplete Parsing");
        Cursor.recycle(cursor);
        return obj;
    }

    /**
     * This class represents a parsing cursor over a character sequence.
     * A cursor location may start and end at any predefined location within 
     * the character sequence iterated over.
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
         * Holds the start index.
         */
        private int _start;

        /**
         * Holds the end index.
         */
        private int _end;

        /**
         * Default constructor.
         */
        private Cursor() {
            super(0);
        }

        /**
         * Returns a new, preallocated or {@link #recycle recycled} cursor
         * instance (on the stack when executing in a {@link 
         * javolution.context.PoolContext PoolContext}).
         * 
         * @param start the start index.
         * @param end the end index (index after the last character to be read).
         * @return a new or recycled cursor instance.
         */
        public static Cursor newInstance(int start, int end) {
            Cursor cursor = (Cursor) FACTORY.object();
            cursor._start = cursor._index = start;
            cursor._end = end;
            return cursor;
        }

        /**
         * Recycles a cursor {@link #newInstance instance} immediately
         * (on the stack when executing in a {@link 
         * javolution.context.PoolContext PoolContext}).
         * 
         * @param instance the cursor instance being recycled.
         */
        public static void recycle(Cursor instance) {
            FACTORY.recycle(instance);
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
         * Returns this cursor start index.
         * 
         * @return the start index.
         */
        public final int getStartIndex() {
            return _start;
        }

        /**
         * Returns this cursor end index.
         * 
         * @return the end index.
         */
        public final int getEndIndex() {
            return _end;
        }

        /**
         * Sets the cursor index.
         * 
         * @param i the index of the next character to parse.
         * @throws IllegalArgumentException 
         *        if <code>((i < getStartIndex()) || (i > getEndIndex()))</code>
         */
        public final void setIndex(int i) {
            if ((i < _start) || (i > _end))
                throw new IllegalArgumentException();
            _index = i;
        }

        /**
         * Indicates if this cursor has reached the end index.
         * 
         * @return <code>this.getIndex() >= this.getEndIndex()</code>
         */
        public final boolean hasNext() {
            return _index < _end;
        }

        /**
         * Returns the next character at the cursor position in the specified 
         * character sequence and increments the cursor position by one.
         * For example:[code]
         *    for (char c=cursor.next(csq); c != 0; c = cursor.next(csq)) {
         *        ...
         *    }
         *    }[/code]
         * 
         * @param csq the character sequence iterated by this cursor.
         * @return the character at the current cursor position in the
         *         specified character sequence or <code>'&#92;u0000'</code>
         *         if the end index has already been reached.
         */
        public final char next(CharSequence csq) {
            return (_index < _end) ? csq.charAt(_index++) : 0;
        }

        /**
         * Indicates if this cursor points to the specified character 
         * in the specified character sequence.
         *  
         * @param c the character.
         * @param csq the character sequence iterated by this cursor.
         * @return <code>true</code> if the cursor next character is the 
         *         one specified; <code>false</code> otherwise.
         */
        public final boolean at(char c, CharSequence csq) {
            return (_index < _end) && (csq.charAt(_index) == c);
        }

        /**
         * Indicates if this cursor points to one of the specified character.
         *  
         * @param charSet the character set
         * @param csq the character sequence iterated by this cursor.
         * @return <code>true</code> if the cursor next character is one 
         *         of the character contained by the character set; 
         *         <code>false</code> otherwise.
         */
        public final boolean at(CharSet charSet, CharSequence csq) {
            return (_index < _end) && (charSet.contains(csq.charAt(_index)));
        }

        /**
         * Indicates if this cursor points to the specified characters 
         * in the specified character sequence.
         *  
         * @param pattern the characters searched for.
         * @param csq the character sequence iterated by this cursor.
         * @return <code>true</code> if the cursor next character are the 
         *         one specified in the pattern; <code>false</code> otherwise.
         */
        public final boolean at(String pattern, CharSequence csq) {
            return (_index < _end) && (csq.charAt(_index) == pattern.charAt(0)) ? match(
                    pattern, csq)
                    : false;
        }

        private final boolean match(String pattern, CharSequence csq) {
            for (int i = 1, j = _index + 1, n = pattern.length(), m = _end; i < n;) {
                if ((j >= m) || (csq.charAt(j++) != pattern.charAt(i++)))
                    return false;
            }
            return true;
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
            while ((_index < _end) && (csq.charAt(_index) == c)) {
                _index++;
            }
            return _index < _end;
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
            while ((_index < _end) && (charSet.contains(csq.charAt(_index)))) {
                _index++;
            }
            return _index < _end;
        }

        /**
         * Increments the cursor index by one.
         *
         * @return <code>this</code>
         */
        public final Cursor increment() {
            _index++;
            return this;
        }

        /**
         * Increments the cursor index by the specified value.
         * 
         * @param i the increment value.
         * @return <code>this</code>
         */
        public final Cursor increment(int i) {
            _index += i;
            return this;
        }

        /**
         * Returns the string representation of this cursor.
         * 
         * @return the index value as a string.
         */
        public String toString() {
            return String.valueOf(_index);
        }
    }
}