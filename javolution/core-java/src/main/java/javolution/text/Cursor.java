/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 *
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.text;

/**
 * <p> A parsing cursor over the characters read. Cursor
 *     allows for token iterations over any {@link CharSequence}.
 *     [code]
 *     String str = "this is a test";
 *     Cursor cursor = new Cursor();
 *     for (CharSequence token; (token=cursor.nextToken(str, ' '))!= null;)
 *            System.out.println(token); 
 *     [/code]
 *     Prints the following output:<pre>
 *        this
 *        is
 *        a
 *        test</pre>
 *     Cursors are typically used with {@link TextFormat} instances.
 *     [code]
 *     // Parses decimal number (e.g. "xxx.xxxxxExx" or "NaN")
 *     public Decimal parse(CharSequence csq, Cursor cursor) throws IllegalArgumentException {
 *         TextFormat<LargeInteger> largeIntegerFormat = TextContext.getFormat(LargeInteger.class);
 *         if (cursor.skip("NaN", csq))
 *             return Decimal.NaN;
 *         LargeInteger significand = LargeIntegerFormat.parse(csq, cursor);
 *         LargeInteger fraction = cursor.skip('.', csq) ? largeIntegerFormat.parse(csq, cursor) : LargeInteger.ZERO;
 *         int exponent = cursor.skip(CharSet.valueOf('E', 'e'), csq) ? TypeFormat.parseInt(csq, cursor) : 0;
 *         int fractionDigits = fraction.digitLength();
 *         return Decimal.valueOf(significand.E(fractionDigits).plus(fraction), exponent - fractionDigits);
 *     }
 *     [/code]
 * </p>
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 5.4, November 19, 2009
 */
public class Cursor {

    /**
     * Holds the index.
     */
    private int index;

    /**
     * Default constructor.
     */
    public Cursor() {}

    /**
     * Returns this cursor index.
     *
     * @return the index of the next character to parse.
     */
    public final int getIndex() {
        return index;
    }

    /**
     * Sets the cursor current index.
     *
     * @param i the index of the next character to parse.
     */
    public final void setIndex(int i) {
        index = i;
    }

    /**
     * Indicates if this cursor points to the end of the specified
     * character sequence.
     *
     * @param csq the character sequence iterated by this cursor.
     * @return <code>getIndex() &gt;= csq.length()</code>
     */
    public final boolean atEnd(CharSequence csq) {
        return index >= csq.length();
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
        return index < csq.length() ? csq.charAt(index) == c : false;
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
        return index < csq.length() ? charSet.contains(csq.charAt(index))
                : false;
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
        int i = index;
        int length = csq.length();
        for (int j = 0; j < str.length();) {
            if ((i >= length) || (str.charAt(j++) != csq.charAt(i++)))
                return false;
        }
        return true;
    }

    /**
     * Returns the current character at this cursor position.
     *
     * @param csq the character sequence iterated by this cursor.
     * @return the current character this cursor points to.
     * @throws IndexOutOfBoundsException if {@link #atEnd this.atEnd(csq)}
     */
    public final char currentChar(CharSequence csq) {
        return csq.charAt(index);
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
        return csq.charAt(index++);
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
        int i = index;
        int n = csq.length();
        for (; (i < n) && (csq.charAt(i) == c); i++) {}
        if (i == index)
            return false; // Cursor did not moved.
        index = i;
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
        int i = index;
        int n = csq.length();
        for (; (i < n) && charSet.contains(csq.charAt(i)); i++) {}
        if (i == index)
            return false; // Cursor did not moved.
        index = i;
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
            index++;
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
            index++;
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
            index += str.length();
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
        for (int i = index; i < n; i++) {
            if (csq.charAt(i) != c) {
                int j = i;
                for (; (++j < n) && (csq.charAt(j) != c);) {
                    // Loop until j at the end of sequence or at specified character.
                }
                index = j;
                return csq.subSequence(i, j);
            }
        }
        index = n;
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
        for (int i = index; i < n; i++) {
            if (!charSet.contains(csq.charAt(i))) {
                int j = i;
                for (; (++j < n) && !charSet.contains(csq.charAt(j));) {
                    // Loop until j at the end of sequence or at specified characters.
                }
                index = j;
                return csq.subSequence(i, j);
            }
        }
        index = n;
        return null;
    }

    /**
     * Returns the head of the specified character sequence until   
     * this cursor position.
     *
     * @return the corresponding sub-sequence.
     */
    public final CharSequence head(CharSequence csq) {
        return csq.subSequence(0, index);
    }

    /**
     * Returns the tail of the specified character sequence starting at 
     * this cursor position.
     *
     * @return the corresponding sub-sequence.
     */
    public final CharSequence tail(CharSequence csq) {
        return csq.subSequence(index, csq.length());
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
        index += i;
        return this;
    }

    /**
     * Returns the string representation of this cursor.
     *
     * @return the index value as a string.
     */
    @Override
    public String toString() {
        return "Cursor: " + index;
    }

    /**
     * Indicates if this cursor is equals to the specified object.
     *
     * @return <code>true</code> if the specified object is a cursor
     *         at the same index; <code>false</code> otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (!(obj instanceof Cursor))
            return false;
        return index == ((Cursor) obj).index;
    }

    /**
     * Returns the hash code for this cursor.
     *
     * @return the hash code value for this object
     */
    @Override
    public int hashCode() {
        return index;
    }
}
