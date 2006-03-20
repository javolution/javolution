/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2005 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.xml;

import javolution.lang.PersistentReference;
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
 *     "CDATA" section ([code]<![CDATA[...]]>[/code]).</p>
 *
 * <p> Note: During deserialization, instances of this class are wrappers 
 *           around the parser characters buffer; therefore immutability 
 *           is not guarantee.</p>
 *           
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 3.5, August 29, 2005
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
            ((CharacterData) obj)._chars = null;
        }
    };
    
    /**
     * Holds the configurable length for the CDATA buffer.
     */
    private static final PersistentReference LENGTH = new PersistentReference(
            "javolution.xml.CharacterData#LENGTH", new Integer(0));

    /**
     * Holds the characters.
     */
    private char[] _chars;

    /**
     * Holds the index of the first character.
     */
    private int _offset;

    /**
     * Holds the length of the character data.
     */
    private int _length;

    /**
     * Holds the internal buffer.
     */
    private char[] _buffer = new char[((Integer) LENGTH.get()).intValue()];
    
    /**
     * Default constructor.
     */
    private CharacterData() {
    }

    /**
     * Returns the character that contains the characters from the specified 
     * subarray of characters.
     *
     * @param chars the source of the characters.
     * @param offset the index of the first character in the data soure.
     * @param length the length of the text returned.
     * @return the corresponding instance.
     * @throws IndexOutOfBoundsException if <code>(offset < 0) || 
     *         (length < 0) || ((offset + length) > chars.length)</code>
     */
    public static CharacterData valueOf(char[] chars, int offset, int length) {
        if ((offset < 0) || (length < 0) || ((offset + length) > chars.length))
            throw new IndexOutOfBoundsException();
        CharacterData cd = (CharacterData) FACTORY.object();
        cd._chars = chars;
        cd._offset = offset;
        cd._length = length;
        return cd;
    }

    /**
     * Returns the character data for the specified character sequence
     * (convenience method).
     * 
     * @param csq the character sequence being wrapped.
     * @return the corresponding character data instance.
     */
    public static CharacterData valueOf(CharSequence csq) {
        final int length = csq.length();
        CharacterData cd = (CharacterData) FACTORY.object();
        if (length > cd._buffer.length) { // Resizes.
            cd._buffer = new char[length];
            LENGTH.setMinimum(new Integer(length)); 
        }
        cd._chars = cd._buffer;
        cd._offset = 0;
        cd._length = length;
        for (int i=0; i < length;) {
            cd._chars[i] = csq.charAt(i++);
        }
        return cd;
    }

    /**
     * Returns the characters source of this character data.
     *
     * @return the character array.
     */
    public char[] chars() {
        return _chars;
    }

    /**
     * Returns the index of the first character in this character data.
     *
     * @return the first character index.
     */
    public int offset() {
        return _offset;
    }

    /**
     * Returns the length of this character data.
     *
     * @return the number of characters.
     */
    public int length() {
        return _length;
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
        if (index >= _length)
            throw new IndexOutOfBoundsException();
        return _chars[index];
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
        if ((start < 0) || (start > end) || (end > _length))
            throw new IndexOutOfBoundsException();
        return CharacterData.valueOf(_chars, _offset + start, end - start);
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
        return FastComparator.LEXICAL.areEqual(this, obj);
    }

    /**
     * Returns the hash code for this character data.
     *
     * @return the hash code value.
     */
    public final int hashCode() {
        return FastComparator.LEXICAL.hashCodeOf(this);
    }

    // Overrides.
    public Text toText() {
        return Text.valueOf(_chars, _offset, _length);
    }

}