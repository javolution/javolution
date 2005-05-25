/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2005 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.xml;

import javolution.lang.Text;
import javolution.realtime.RealtimeObject;
import javolution.util.FastComparator;
import j2me.io.Serializable;
import j2me.lang.CharSequence;

/**
 * <p> This class represents a text that is not markup and 
 *     constitutes the "Character Data" of a XML document.</p>
 *     
 * <p> During deserialization, instances of this class are generated for 
 *     character data containing at least one non-whitespace character.</p>
 *     
 * <p> During serialization, instances of this class are written in a 
 *     "CDATA" section (<code>&lt;![CDATA[...]]&gt;</code>).</p>
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 3.3, May 13, 2005
 */
public final class CharacterData extends RealtimeObject implements
        Serializable, CharSequence {

    /**
     * Holds the object factory.
     */
    private static final Factory FACTORY = new Factory() {
        protected Object create() {
            return new CharacterData();
        }
        protected void cleanup(Object obj) {
            CharacterData charData = (CharacterData) obj;
            charData._csq = null;
        }
    };

    /**
     * Holds the character sequence being wrapped.
     */
    private CharSequence _csq;

    /**
     * Default constructor.
     */
    private CharacterData() {
    }

    /**
     * Returns the character data wrapping the specified character sequence.
     * 
     * @param csq the character sequence being wrapped.
     * @return a new, preallocated or recycled instance.
     */
    public static CharacterData valueOf(CharSequence csq) {
        CharacterData charData = (CharacterData) FACTORY.object();
        charData._csq = csq;
        return charData;
    }

    /**
     * Returns the length of this character data.
     *
     * @return the number of characters.
     */
    public int length() {
        return _csq.length();
    }

    /**
     * Returns the character at the specified index.
     *
     * @param  index the index of the character.
     * @return the character at the specified index.
     * @throws IndexOutOfBoundsException if index is negative, or index 
     *         is equal or greater than <code>this.length()</code>.
     */
    public char charAt(int index) {
        return _csq.charAt(index);
    }

    /**
     * Returns a subsequence of this character data.
     *
     * @param  start the index of the first character inclusive.
     * @param  end the index of the last character exclusive.
     * @return a character data subsequence of this one.
     * @throws IndexOutOfBoundsException if <code>(start < 0) || (end < 0) ||
     *         (start > end) || (end > this.length())</code>
     */
    public CharSequence subSequence(int start, int end) {
        return CharacterData.valueOf(_csq.subSequence(start, end));
    }

    /**
     * Compares this character data against the specified object for equality.
     * Returns <code>true</code> if the specified object are both character 
     * data having the same character content. 
     * 
     * @param  obj the object to compare with or <code>null</code>.
     * @return <code>true</code> if that is a character data with the same 
     *         character content as this one; <code>false</code> otherwise.
     */
    public final boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof CharacterData))
            return false;
        final CharacterData that = (CharacterData) obj;
        return FastComparator.LEXICAL.areEqual(this._csq, that._csq);
    }

    /**
     * Returns the hash code for this character data.
     *
     * @return the hash code value.
     */
    public final int hashCode() {
        return FastComparator.LEXICAL.hashCodeOf(this._csq);
    }

    /**
     * Copies the characters from this character data into the destination
     * character array.
     *
     * @param start the index of the first character to copy.
     * @param end the index after the last character to copy.
     * @param dest the destination array.
     * @param destPos the start offset in the destination array.
     * @throws IndexOutOfBoundsException if <code>(start < 0) || (end < 0) ||
     *         (start > end) || (end > this.length())</code>
     */
    public void getChars(int start, int end, char dest[], int destPos) {
        if ((end > _csq.length()) || (end < start))
            throw new IndexOutOfBoundsException();
        for (int i = start, j = destPos; i < end;) {
            dest[j++] = _csq.charAt(i++);
        }
    }

    // Overrides.
    public Text toText() {
        return Text.valueOf(_csq);
    }

}