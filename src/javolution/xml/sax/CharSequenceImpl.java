/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2004 - The Javolution Team (http://javolution.org/)
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.xml.sax;

import javolution.lang.Text;

/**
 * This class represents the <code>CharSequence</code> generated while
 * parsing XML document; parsers may reuse instances of this class
 * to avoid dynamic memory allocation.
 *
 * @author  <a href="mailto:sax@megginson.com">David Megginson</a>
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0, January 25, 2004
 */
final class CharSequenceImpl implements CharSequence, Comparable {

    /**
     * Holds the character data.
     */
    char[] data = new char[0];

    /**
     * Holds the index of the first character.
     */
    int first;

    /**
     * Holds the length of char sequence.
     */
    int length;

    /**
     * Holds an empty character sequence.
     */
    static final CharSequenceImpl EMPTY = new CharSequenceImpl();

    /**
     * Default constructor.
     */
    CharSequenceImpl() {
    }

    /**
     * Creates a character sequence from the specified String.
     * 
     * @param string the String.
     */
    CharSequenceImpl(String string) {
        data = string.toCharArray();
        first = 0;
        length = string.length();
    }

    /**
     * Returns the length of this character sequence.
     *
     * @return the number of characters (16-bits Unicode) composing this
     *         character sequence.
     */
    public int length() {
        return length;
    }

    /**
     * Returns the character at the specified index.
     *
     * @param  index the index of the character starting at <code>0</code>.
     * @return the character at the specified index of this character sequence.
     * @throws IndexOutOfBoundsException  if the <code>index</code> is not
     *         in the <code>[0, length() - 1]</code> range.
     */
    public char charAt(int index) {
        if ((index >= 0) && (index < length)) {
            return data[first + index];
        } else {
            throw new IndexOutOfBoundsException("index: " + index);
        }
    }

    /**
     * Returns a new character sequence that is a subsequence of this sequence.
     *
     * @param  start the index of the first character inclusive.
     * @param  end the index of the last character exclusive.
     * @return the character sequence starting at the specified
     *         <code>start</code> position and ending just before the specified
     *         <code>end</code> position.
     * @throws IndexOutOfBoundsException  if the <code>start</code> index
     *         is not in the <code>[0, length()]</code> range.
     * @throws IndexOutOfBoundsException  if the <code>end</code> index
     *         is not in the <code>[start, length()]</code> range.
     */
    public CharSequence subSequence(int start, int end) {
        CharSequenceImpl chars = new CharSequenceImpl();
        chars.data = data;
        chars.first = first + start;
        chars.length = end - start;
        return chars;
    }

    /**
     * Returns the <code>String<code> corresponding to this character
     * sequence. The <code>String</code> returned is always allocated on the
     * heap and can safely be referenced elsewhere.
     *
     * @return the <code>java.lang.String</code> for this character sequence.
     */
    public String toString() {
        return new String(data, first, length);
    }

    /**
     * Returns the hash code for this {@link CharSequenceImpl}.
     *
     * <p> Note: Returns the same hashCode as <code>java.lang.String</code>
     *           (consistent with {@link #equals})</p>
     *
     * @return the hash code value.
     */
    public int hashCode() {
        int h = 0;
        for (int i = 0, j = first; i < length; i++) {
            h = 31 * h + data[j++];
        }
        return h;
    }

    /**
     * Compares this character sequence against the specified object.
     *
     * @param  that the object to compare with.
     * @return <code>true</code> if both objects are <code>CharSequence</code>
     *         and they represent the same sequence;
     *         <code>false</code> otherwise.
     */
    public boolean equals(Object that) {
        if (that instanceof CharSequence) {
            return equals((CharSequence) that);
        } else {
            return false;
        }
    }

    /**
     * Compares this character sequence against the specified character
     * sequence.
     *
     * <p> Note: Unfortunately, due to the current (JDK 1.4.1) implementation
     *          of <code>java.lang.String</code> and <code>
     *          java.lang.StringBuffer</code>, this method is not symmetric.</p>
     *
     * @param  chars the character sequence to compare with.
     * @return <code>true</code> if both objects represent the same sequence;
     *         <code>false</code> otherwise.
     */
    public boolean equals(CharSequence chars) {
        if (length != chars.length()) {
            return false;
        } else {
            for (int i = 0, j = first; i < length; i++) {
                if (data[j++] != chars.charAt(i)) {
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * Compares this character sequence against the specified
     * {@link CharSequenceImpl}.
     *
     * @param  chars the character sequence to compare with.
     * @return <code>true</code> if both objects represent the same sequence;
     *         <code>false</code> otherwise.
     */
    public boolean equals(CharSequenceImpl chars) {
        if (this.length == chars.length) {
            final char[] charsData = chars.data;
            final int end = first + length;
            for (int i = first, j = chars.first; i < end;) {
                if (data[i++] != charsData[j++]) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * Compares this {@link CharSequenceImpl} with the specified character
     * sequence lexicographically.
     *
     * @param   seq the character sequence to be compared.
     * @return  <code>{@link Text.COMPARATOR}.compare(this, seq)</code>
     * @throws  ClassCastException if the specifed object is not a
     *          <code>CharSequence</code>.
     */
    public int compareTo(Object seq) {
        return Text.COMPARATOR.compare(this, seq);
    }
}