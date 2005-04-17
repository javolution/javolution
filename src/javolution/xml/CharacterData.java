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
 * @version 3.2, March 20, 2005
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
            charData._text = null;
            charData._chars = null;
        }
    };

    /**
     * Holds the text (or <code>null</code> to use the buffer view).
     */
    private Text _text;

    /**
     * Holds a view on character buffer.
     */
    private char[] _chars;

    /**
     * Holds the character buffer offset.
     */
    private int _offset;

    /**
     * Holds the length in character buffer.
     */
    private int _length;

    /**
     * Default constructor.
     */
    private CharacterData() {
    }

    /**
     * Returns the character data corresponding to the specified character 
     * sequence.
     * 
     * @param seq the character sequence.
     * @return a new, preallocated or recycled instance.
     */
    public static CharacterData valueOf(CharSequence seq) {
        CharacterData charData = (CharacterData) FACTORY.object();
        charData._text = Text.valueOf(seq);
        return charData;
    }

    /**
     * Returns the character data corresponding to the specified string.
     * 
     * @param str the string.
     * @return a new, preallocated or recycled instance.
     */
    public static CharacterData valueOf(String str) {
        CharacterData charData = (CharacterData) FACTORY.object();
        charData._text = Text.valueOf(str);
        return charData;
    }

    /**
     * Returns the character data corresponding to the specified character 
     * sequence (used by {@link ObjectReader}).
     * 
     * @param chars the character buffer.
     * @param offset the character buffer offset.
     * @param length the length in character buffer.
     * @return a new, preallocated or recycled instance.
     */
    static CharacterData valueOf(char[] chars, int offset, int length) {
        CharacterData charData = (CharacterData) FACTORY.object();
        charData._chars = chars;
        charData._offset = offset;
        charData._length = length;
        return charData;
    }

    /**
     * Returns the length of this character data.
     *
     * @return the number of characters.
     */
    public int length() {
        return (_text != null) ? _text.length() : _length;
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
        return (_text != null) ? _text.charAt(index) : _chars[_offset + index];
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
        if (_text != null) {
            return CharacterData.valueOf(_text.subtext(start, end));
        } else {
            return CharacterData.valueOf(_chars, _offset + start, end - start);
        }
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
        if (_text != null) {
            _text.getChars(start, end, dest, destPos);
        } else {
            System.arraycopy(_chars, _offset + start, dest, destPos, end - start);
        }
    }


    /**
     * Compares this character data against the specified object for equality.
     * Returns <code>true</code> if the specified object is a character 
     * data having the same character sequence as this text. 
     * 
     * @param  obj the object to compare with or <code>null</code>.
     * @return <code>true</code> if that is a character data with the same 
     *         character sequence as this one; <code>false</code> otherwise.
     */
    public final boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof CharacterData))
            return false;
        final CharacterData that = (CharacterData) obj;
        if (this._text != null) return this._text.contentEquals(that);
        if (that._text != null) return that._text.contentEquals(this);
        if (this._length != that._length) return false;
        for (int i = 0; i < _length; i++) {
            if (this._chars[this._offset + i] != that._chars[that._offset + i]) 
                return false;
        }
        return true;
    }

    /**
     * Returns the hash code for this character data.
     *
     * @return the hash code value.
     */
    public final int hashCode() {
        if (_text != null) return _text.hashCode();
        return FastComparator.LEXICAL.hashCodeOf(this);
    }

    // Overrides.
    public Text toText() {
        return (_text != null) ? _text : Text.valueOf(this, 0, _length);
    }

    // Overrides.
    public boolean move(ObjectSpace os) {
        if (super.move(os)) {
            if (_text != null) _text.move(os);
            return true;
        }
        return false;
    }

}