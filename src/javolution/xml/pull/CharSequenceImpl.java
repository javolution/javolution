/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2005 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.xml.pull;

import j2me.lang.CharSequence;
import j2me.lang.Comparable;
import javolution.realtime.ObjectFactory;
import javolution.util.FastComparator;

/**
 * This class represents the <code>CharSequence</code> generated while
 * parsing XML document; parsers may reuse instances of this class
 * to avoid dynamic memory allocation.
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 3.2, March 21, 2005
 */
final class CharSequenceImpl implements CharSequence, Comparable {

    /**
     * Holds the associated factory (for subSequence).
     */
    private static final ObjectFactory FACTORY = new ObjectFactory() {
        protected Object create() {
            return new CharSequenceImpl();
        }
    };

    /**
     * Holds an empty character sequence.
     */
    static final CharSequenceImpl EMPTY = new CharSequenceImpl("");

    /**
     * Holds the character data.
     */
    char[] data;

    /**
     * Holds the index of the first character.
     */
    int offset;

    /**
     * Holds the length of char sequence.
     */
    int length;

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
        offset = 0;
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
     * @throws IndexOutOfBoundsException  if <code>((index < 0) || 
     *         (index >= length))</code>
     */
    public char charAt(int index) {
        if ((index < 0) || (index >= length))
            throw new IndexOutOfBoundsException("index: " + index);
        return data[offset + index];
    }

    /**
     * Returns a new character sequence that is a subsequence of this sequence.
     *
     * @param  start the index of the first character inclusive.
     * @param  end the index of the last character exclusive.
     * @return the character sequence starting at the specified
     *         <code>start</code> position and ending just before the specified
     *         <code>end</code> position.
     * @throws IndexOutOfBoundsException if <code>(start < 0) || (end < 0) ||
     *         (start > end) || (end > this.length())</code>
     */
    public CharSequence subSequence(int start, int end) {
        if ((start < 0) || (end < 0) ||
                 (start > end) || (end > this.length())) 
            throw new IndexOutOfBoundsException();
        CharSequenceImpl chars = (CharSequenceImpl) FACTORY.object();
        chars.data = data;
        chars.offset = offset + start;
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
        return new String(data, offset, length);
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
        for (int i = 0, j = offset; i < length; i++) {
            h = 31 * h + data[j++];
        }
        return h;
    }

    /**
     * Compares this character sequence against the specified object
     * (<code>String</code> or <code>CharSequence</code>).
     *
     * @param  that the object to compare with.
     * @return <code>true</code> if both objects represent the same sequence;
     *         <code>false</code> otherwise.
     */
    public boolean equals(Object that) {
        if (that instanceof CharSequenceImpl) {
            return equals((CharSequenceImpl) that);
        } else if (that instanceof String) { // J2ME: String not a CharSequence.
            return equals((String) that);
        } else if (that instanceof CharSequence) {
            return equals((CharSequence) that);
        } else {
            return false;
        }
    }

    /**
     * Compares this character sequence against the specified
     * {@link CharSequenceImpl}.
     *
     * @param  that the character sequence to compare with.
     * @return <code>true</code> if both objects represent the same sequence;
     *         <code>false</code> otherwise.
     */
    public boolean equals(CharSequenceImpl that) {
        if (that == null)
            return false;
        if (this.length != that.length)
            return false;
        final char[] thatData = that.data;
        final int end = offset + length;
        for (int i = offset, j = that.offset; i < end;) {
            if (data[i++] != thatData[j++])
                return false;
        }
        return true;
    }

    /**
     * Compares this character sequence against the specified String.
     * 
     * @param  chars the character sequence to compare with.
     * @return <code>true</code> if both objects represent the same sequence;
     *         <code>false</code> otherwise.
     */
    public boolean equals(String str) {
        if (str == null)
            return false;
        if (length != str.length())
            return false;
        for (int i = 0, j = offset; i < length;) {
            if (data[j++] != str.charAt(i++))
                return false;
        }
        return true;
    }

    /**
     * Compares this character sequence against the specified character
     * sequence.
     * 
     * @param  chars the character sequence to compare with.
     * @return <code>true</code> if both objects represent the same sequence;
     *         <code>false</code> otherwise.
     */
    public boolean equals(CharSequence chars) {
        if (chars == null)
            return false;
        if (this.length != chars.length())
            return false;
        for (int i = 0, j = offset; i < length;) {
            if (data[j++] != chars.charAt(i++))
                return false;

        }
        return true;
    }

    /**
     * Compares this {@link CharSequenceImpl} with the specified character
     * sequence lexicographically.
     *
     * @param   seq the character sequence to be compared.
     * @return  <code>{@link FastComparator#LEXICAL}.compare(this, seq)</code>
     * @throws  ClassCastException if the specifed object is not a
     *          <code>CharSequence</code>.
     */
    public int compareTo(Object seq) {
        return FastComparator.LEXICAL.compare(this, seq);
    }
}