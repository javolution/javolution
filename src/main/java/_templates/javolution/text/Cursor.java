/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2009 - Javolution (http://javolution.org/)
 * All rights reserved.
 *
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package _templates.javolution.text;

import _templates.java.lang.CharSequence;
import _templates.java.text.ParsePosition;
import _templates.javolution.context.ObjectFactory;
import _templates.javolution.lang.Reusable;

/**
 * <p> This class represents a parsing cursor over characters. Cursor
 *     allows for token iterations over any {@link CharSequence}.
 *     [code]
 *     CharSequence csq = "this is a test";
 *     Cursor cursor = Cursor.newInstance();
 *     try {
 *        for (CharSequence token; (token=cursor.nextToken(csq, ' '))!= null;)
 *            System.out.println(token); 
 *     } finally {
 *         Cursor.recycle(cursor);
 *     }
 *     [/code]
 *     Prints the following output:<pre>
 *        this
 *        is
 *        a
 *        test</pre>
 *     Cursors are typically used with {@link TextFormat} instances.
 *     [code]
 *     // Parses decimal number (e.g. "xxx.xxxxxExx" or "NaN")
 *     public Decimal parse(CharSequence csq, Cursor cursor) {
 *         if (cursor.skip("NaN", csq))
 *             return Decimal.NaN;
 *         LargeInteger significand = LargeInteger.TEXT_FORMAT.parse(csq, cursor);
 *         LargeInteger fraction = cursor.skip('.', csq) ? LargeInteger.TEXT_FORMAT.parse(csq, cursor) : LargeInteger.ZERO;
 *         int exponent = cursor.skip(CharSet.valueOf('E', 'e'), csq) ? TypeFormat.parseInt(csq, 10, cursor) : 0;
 *         int fractionDigits = fraction.digitLength();
 *         return Decimal.valueOf(significand.E(fractionDigits).plus(fraction), exponent - fractionDigits);
 *     }
 *     [/code]
 * </p>
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 5.4, November 19, 2009
 */
public class Cursor extends ParsePosition implements Reusable {

    /**
     * Default constructor.
     */
    public Cursor() {
        super(0);
    }

    /**
     * Returns a factory produced instance which can be {@link #recycle recycled}
     * after usage.
     *
     * @return a recyclable instance.
     */
    public static Cursor newInstance() {
        return (Cursor) FACTORY.object();
    }
    private static final ObjectFactory FACTORY = new ObjectFactory() {

        protected Object create() {
            return new Cursor();
        }
    };

    /**
     * Recycles the specified factory {@link #newInstance() produced} cursor.
     *
     * @param cursor the cursor to recycle.
     */
    public static void recycle(Cursor cursor) {
        FACTORY.recycle(cursor);
    }

    /**
     * Returns this cursor index.
     *
     * @return the index of the next character to parse.
     */
    public final int getIndex() {
        return super.getIndex();
    }

    /**
     * Sets the cursor current index.
     *
     * @param i the index of the next character to parse.
     */
    public void setIndex(int i) {
        super.setIndex(i);
    }

    /**
     * Indicates if this cursor points to the end of the specified
     * character sequence.
     *
     * @param csq the character sequence iterated by this cursor.
     * @return <code>getIndex() &gt;= csq.length()</code>
     */
    public final boolean atEnd(CharSequence csq) {
        return getIndex() >= csq.length();
    }

    /**
     * Indicates if this cursor points to the specified character in the
     * specified character sequence.
     *
     * @param c the character to test.
     * @param csq the character sequence iterated by this cursor.
     * @return <code>csq.charAt(this.getIndex()) == c</code>
     */
    public final boolean at(char c, CharSequence csq) {
        int i = getIndex();
        return i < csq.length() ? csq.charAt(i) == c : false;
    }

    /**
     * Indicates if this cursor points to any of the specified character in the
     * specified character sequence.
     *
     * @param charSet any of the character to test.
     * @param csq the character sequence iterated by this cursor.
     * @return <code>csq.charAt(this.getIndex()) == c</code>
     */
    public final boolean at(CharSet charSet, CharSequence csq) {
        int i = getIndex();
        return i < csq.length() ? charSet.contains(csq.charAt(i)) : false;
    }

    /**
     * Indicates if this cursor points to the specified characters in the
     * specified sequence.
     *
     * @param str the characters to test.
     * @param csq the character sequence iterated by this cursor.
     * @return <code>true</code> if this cursor points to the specified
     *         characters; <code>false</code> otherwise.
     */
    public final boolean at(String str, CharSequence csq) {
        int i = getIndex();
        int length = csq.length();
        for (int j = 0; j < str.length();) {
            if ((i >= length) || (str.charAt(j++) != csq.charAt(i++)))
                return false;
        }
        return true;
    }

    /**
     * Returns the next character at this cursor position.The cursor
     * position is incremented by one.
     *
     * @param csq the character sequence iterated by this cursor.
     * @return the next character this cursor points to.
     * @throws IndexOutOfBoundsException if {@link #atEnd this.atEnd(csq)}
     */
    public final char nextChar(CharSequence csq) {
        int i = getIndex();
        setIndex(i + 1);
        return csq.charAt(i);
    }

    /**
     * Moves this cursor forward until it points to a character
     * different from the specified character.
     *
     * @param c the character to skip.
     * @param csq the character sequence iterated by this cursor.
     * @return <code>true</code> if this cursor has skipped at least one 
     *         character;<code>false</code> otherwise (e.g. end of sequence
     *         reached).
     */
    public final boolean skipAny(char c, CharSequence csq) {
        int i = getIndex();
        int n = csq.length();
        for (; (i < n) && (csq.charAt(i) == c); i++) {
        }
        if (i == getIndex())
            return false; // Cursor did not moved.
        setIndex(i);
        return true;
    }

    /**
     * Moves this cursor forward until it points to a character
     * different from any of the character in the specified set.
     * For example: [code]
     *  // Reads numbers separated by tabulations or spaces.
     *  FastTable<Integer> numbers = new FastTable<Integer>();
     *  while (cursor.skipAny(CharSet.SPACE_OR_TAB, csq)) {
     *      numbers.add(TypeFormat.parseInt(csq, cursor));
     *  }[/code]
     *
     * @param charSet the character to skip.
     * @param csq the character sequence iterated by this cursor.
     * @return <code>true</code> if this cursor has skipped at least one
     *         character;<code>false</code> otherwise (e.g. end of sequence
     *         reached).
     */
    public final boolean skipAny(CharSet charSet, CharSequence csq) {
        int i = getIndex();
        int n = csq.length();
        for (; (i < n) && charSet.contains(csq.charAt(i)); i++) {
        }
        if (i == getIndex())
            return false; // Cursor did not moved.
        setIndex(i);
        return true;
    }

    /**
     * Moves this cursor forward only if at the specified character.
     * This method is equivalent to:
     * [code]
     *     if (at(c, csq))
     *          increment();
     * [/code]
     *
     * @param c the character to skip.
     * @param csq the character sequence iterated by this cursor.
     * @return <code>true</code> if this cursor has skipped the specified
     *         character;<code>false</code> otherwise.
     */
    public final boolean skip(char c, CharSequence csq) {
        if (this.at(c, csq)) {
            this.increment();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Moves this cursor forward only if at any of the specified character.
     * This method is equivalent to:
     * [code]
     *     if (at(charSet, csq))
     *          increment();
     * [/code]
     *
     * @param charSet holding the characters to skip.
     * @param csq the character sequence iterated by this cursor.
     * @return <code>true</code> if this cursor has skipped any the specified
     *         character;<code>false</code> otherwise.
     */
    public final boolean skip(CharSet charSet, CharSequence csq) {
        if (this.at(charSet, csq)) {
            this.increment();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Moves this cursor forward only if at the specified string.
     * This method is equivalent to:
     * [code]
     *     if (at(str, csq))
     *          increment(str.length());
     * [/code]
     *
     * @param str the string to skip.
     * @param csq the character sequence iterated by this cursor.
     * @return <code>true</code> if this cursor has skipped the specified
     *        string;<code>false</code> otherwise (e.g. end of sequence
     *         reached).
     */
    public final boolean skip(String str, CharSequence csq) {
        if (this.at(str, csq)) {
            this.increment(str.length());
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns the subsequence from the specified cursor position not holding
     * the specified character. For example:[code]
     *    CharSequence csq = "This is a test";
     *    for (CharSequence token; (token=cursor.nextToken(csq, ' '))!= null;) {
     *        System.out.println(token); // Prints one word at a time.
     *    }[/code]
     *
     * @param csq the character sequence iterated by this cursor.
     * @param c the character being skipped.
     * @return the subsequence not holding the specified character or
     *         <code>null</code> if none.
     */
    public final CharSequence nextToken(CharSequence csq, char c) {
        int n = csq.length();
        for (int i = getIndex(); i < n; i++) {
            if (csq.charAt(i) != c) {
                int j = i;
                for (; (++j < n) && (csq.charAt(j) != c);) {
                    // Loop until j at the end of sequence or at specified character.
                }
                setIndex(j);
                return csq.subSequence(i, j);
            }
        }
        setIndex(n);
        return null;
    }

    /**
     * Returns the subsequence from the specified cursor position not holding
     * any of the characters specified. For example:[code]
     *    CharSequence csq = "This is a test";
     *    for (CharSequence token; (token=cursor.nextToken(csq, CharSet.WHITESPACE))!= null;) {
     *        System.out.println(token); // Prints one word at a time.
     *    }[/code]
     *
     * @param csq the character sequence iterated by this cursor.
     * @param charSet the characters being skipped.
     * @return the subsequence not holding the specified character or
     *         <code>null</code> if none.
     */
    public final CharSequence nextToken(CharSequence csq, CharSet charSet) {
        int n = csq.length();
        for (int i = getIndex(); i < n; i++) {
            if (!charSet.contains(csq.charAt(i))) {
                int j = i;
                for (; (++j < n) && !charSet.contains(csq.charAt(j));) {
                    // Loop until j at the end of sequence or at specified characters.
                }
                setIndex(j);
                return csq.subSequence(i, j);
            }
        }
        setIndex(n);
        return null;
    }

    /**
     * Increments the cursor index by one.
     *
     * @return <code>this</code>
     */
    public final Cursor increment() {
        return increment(1);
    }

    /**
     * Increments the cursor index by the specified value.
     *
     * @param i the increment value.
     * @return <code>this</code>
     */
    public final Cursor increment(int i) {
        setIndex(getIndex() + i);
        return this;
    }

    /**
     * Returns the string representation of this cursor.
     *
     * @return the index value as a string.
     */
    public String toString() {
        return "Index: " + getIndex();
    }

    /**
     * Indicates if this cursor is equals to the specified object.
     *
     * @return <code>true</code> if the specified object is a cursor
     *         at the same index; <code>false</code> otherwise.
     */
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (!(obj instanceof Cursor))
            return false;
        return getIndex() == ((Cursor) obj).getIndex();
    }

    /**
     * Returns the hash code for this cursor.
     *
     * @return the hash code value for this object
     */
    public int hashCode() {
        return getIndex();
    }

    /**
     * Resets this cursor instance.
     *
     * @see Reusable
     */
    public void reset() {
        super.setIndex(0);
        super.setErrorIndex(-1);
    }
}
