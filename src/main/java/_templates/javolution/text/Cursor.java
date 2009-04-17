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
import _templates.javolution.lang.Reusable;

/**
 * <p> This class represents a parsing cursor over characters. Cursor
 *     allows for token iterations over any {@link CharSequence}.[code]
 *       CharSequence csq = "this is a test";
 *       for (Cursor cursor = new Cursor(); cursor.skip(CharSet.WHITESPACES, csq);) {
 *           System.out.println(cursor.nextToken(csq, CharSet.WHITESPACES));
 *       }[/code]
 *     Prints the following output:<pre>
 *        this
 *        is
 *        a
 *        test</pre>
 *     Cursors are typically used with {@link TextFormat} instances.[code]
 *        public Font parse(CharSequence csq, Cursor cursor) {
 *            CharSequence fontName = cursor.nextToken(csq, CharSet.WHITESPACE);
 *            return Font.decode(fontName.toString());
 *        }[/code]
 * </p>
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 5.3, February 15, 2006
 */
public class Cursor extends ParsePosition implements Reusable {

    /**
     * Default constructor.
     */
    public Cursor() {
        super(0);
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
     * Indicates if this cursor points to a valid character in the specified
     * character sequence.
     *
     * @param csq the character sequence iterated by this cursor.
     * @return <code>true</code> if this cursor points to a valid character
     *         position; <code>false</code> otherwise.
     */
    public final boolean hasNext(CharSequence csq) {
        return getIndex() < csq.length();
    }

    /**
     * Returns the next character at this cursor position. The cursor
     * position is incremented by one.
     *
     * @param csq the character sequence iterated by this cursor.
     * @return this cursor points to a valid character;
     *         <code>false</code> otherwise.
     * @throws IndexOutOfBoundsException if {@link #hasNext !hasNext(csq)}
     */
    public final char next(CharSequence csq) {
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
     * @return <code>true</code> if this cursor points to a character
     *         different from the ones specified; <code>false</code>
     *         otherwise (end of sequence reached).
     */
    public final boolean skip(char c, CharSequence csq) {
        int n = csq.length();
        for (int i=getIndex(); i < n; i++) {
            if (csq.charAt(i) != c) {
                setIndex(i);
                return true;
            }
        }
        setIndex(n);
        return false;
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
        int n = csq.length();
        for (int i=getIndex(); i < n; i++) {
            if (!charSet.contains(csq.charAt(i))) {
                setIndex(i);
                return true;
            }
        }
        setIndex(n);
        return false;
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
        for (int i=getIndex(); i < n; i++) {
            if (csq.charAt(i) != c) {
                int j=i;
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
        for (int i=getIndex(); i < n; i++) {
            if (!charSet.contains(csq.charAt(i))) {
                int j=i;
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