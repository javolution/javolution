/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2005 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.xml;

import j2me.lang.CharSequence;
import j2me.io.Serializable;
import javolution.lang.TextBuilder;
import javolution.realtime.RealtimeObject;
import javolution.util.FastComparator;

/**
 * <p> This class represents the text that is not markup and constitutes
 *     the "Character Data" of a XML document.</p>
 *     
 * <p> During deserialization, instances of this class are generated for 
 *     character data containing at least one non-whitespace character.</p>
 *     
 * <p> During serialization, instances of this class are written in a 
 *     "CDATA" section (<code>&lt;![CDATA[...]]&gt;</code>).</p>
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 2.2, January 8, 2005
 */
public final class CharacterData extends RealtimeObject 
        implements CharSequence, Serializable {

    /**
     * Holds the object factory.
     */
    private static final Factory FACTORY = new Factory() {
        public Object create() {
            return new CharacterData();
        }
    };
    
    /**
     * Holds the text.
     */
    private TextBuilder _text = new TextBuilder();

    /**
     * Returns the character data for the specified string.
     *
     * @param  str the <code>String</code> source.
     * @return the corresponding character data instance.
     */
    public static CharacterData valueOf(String str) {
        CharacterData cd = (CharacterData) FACTORY.object();
        cd._text.reset();
        cd._text.append(str);
        return cd;
    }

    /**
     * Returns the character data for the specified character sequence.
     *
     * @param  csq the <code>CharSequence</code> source.
     * @return the corresponding character data instance.
     */
    public static CharacterData valueOf(CharSequence csq) {
        CharacterData cd = (CharacterData) FACTORY.object();
        cd._text.reset();
        cd._text.append(csq);
        return cd;
    }

    /**
     * Returns the character data that contains the characters from the specified 
     * subarray of characters.
     *
     * @param data the source of the characters.
     * @param offset the index of the first character in the data soure.
     * @param length the length of the subarray.
     * @return the corresponding character data instance.
     */
    public static CharacterData valueOf(char[] data, int offset, int length) {
        CharacterData cd = (CharacterData) FACTORY.object();
        cd._text.reset();
        for (int i=0; i < length;) {
            cd._text.append(data[offset + i++]);
        }
        return cd;
    }

    /**
     * Returns an intrinsic array containing the character data.
     *
     * @return an intrinsic array.
     */
    char[] toArray() {
        final int length = _text.length();
        if (_chars.length < length) {
            _chars = new char[length * 2];
        }
        for (int i=0; i < length;) {
            _chars[i] = _text.charAt(i++);
        }
        return _chars;
    }
    private char[] _chars = new char[0];

    /**
     * Default constructor.
     */
    private CharacterData() {
    }

    /**
     * Returns the length of this character data.
     *
     * @return the number of characters (16-bits Unicode).
     */
    public int length() {
        return _text.length();
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
        return _text.charAt(index);
    }

    /**
     * Returns a new character data that is a subsequence of this one.
     *
     * @param  start the index of the first character inclusive.
     * @param  end the index of the last character exclusive.
     * @return the corresponding character data instance.
     * @throws IndexOutOfBoundsException if start or end are negative, start 
     *         is greater than end, or end is greater than 
     *         <code>this.length()</code>.
     */
    public CharSequence subSequence(int start, int end) {
        return _text.subSequence(start, end);
    }

    /**
     * Compares this character data against the specified object. This method 
     * uses a {@link FastComparator#LEXICAL lexical comparator}
     * to make this determination.
     * 
     * @param  that the object to compare with.
     * @return <code>FastComparator.LEXICAL.areEqual(this, that)</code>
     */
    public final boolean equals(Object that) {
        if ((that instanceof CharSequence) || (that instanceof String)) {
            return FastComparator.LEXICAL.areEqual(this, that);
        }
        return false;
    }

    /**
     * Returns the hash code for this character data.
     *
     * @return the hash code value.
     */
    public final int hashCode() {
        return _text.hashCode();
    }

    // Overrides.
    public boolean move(ObjectSpace os) {
        if (super.move(os)) {
            _text.move(os);
            return true;
        }
        return false;
    }
}