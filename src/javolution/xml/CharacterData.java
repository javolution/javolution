/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2004 - The Javolution Team (http://javolution.org/)
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.xml;

import javolution.lang.Text;
import javolution.realtime.RealtimeObject;

/**
 * <p> This class represents the text that is not markup and constitutes
 *     the "Character Data" of a XML document.</p>
 * <p> During deserialization, instances of this class are generated for 
 *     character data containing at least one non-whitespace character.</p>
 * <p> During serialization, instances of this class are written in a 
 *     "CDATA" section (<code>&gt;![CDATA[...]]&lt;</code>).</p>
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>/**
 * @version 2.0, December 1, 2004
 */
public final class CharacterData extends RealtimeObject implements CharSequence {

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
    private Text _text;

    /**
     * Returns the character data for the specified character sequence.
     *
     * @param  csq the <code>CharSequence</code> source.
     * @return the corresponding character data instance.
     */
    public static CharacterData valueOf(CharSequence csq) {
        CharacterData cd = (CharacterData) FACTORY.object();
        cd._text = (csq instanceof Text) ? (Text)csq : Text.valueOf(csq);
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
        cd._text = Text.valueOf(data, offset, length);
        return cd;
    }

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
        CharacterData cd = (CharacterData) FACTORY.object();
        cd._text = _text.subtext(start, end);
        return cd;
    }

    // Overrides.
    public void move(ContextSpace cs) {
        super.move(cs);
        _text.move(cs);
    }
}