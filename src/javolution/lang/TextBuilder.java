/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2005 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.lang;

import java.io.IOException;

import j2me.io.Serializable;
import j2me.lang.CharSequence;

import javolution.JavolutionError;
import javolution.realtime.ObjectFactory;
import javolution.realtime.Realtime;
import javolution.realtime.RealtimeObject;
import javolution.xml.XmlElement;
import javolution.xml.XmlFormat;

/**
 * <p> This class represents an {@link Appendable} text whose capacity expands 
 *     gently without incurring expensive resize/copy operations ever.</p>
 *     
 * <p> This class is not intended for large documents manipulations which 
 *     should be performed with the {@link Text} class directly 
 *     (<code>O(Log(n))</code> {@link Text#insert insertion} and 
 *     {@link Text#delete deletion} capabilities).</p>
 *     
 * <p> This implementation is not synchronized.</p>
 *     
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 3.1, March 16, 2005
 */
public class TextBuilder extends RealtimeObject implements Appendable,
        CharSequence, Reusable, Serializable {

    /**
     * Holds the default XML representation for this class and its sub-classes.
     * This representation consists of a <code>"text"</code> attribute 
     * holding the character sequence.
     */
    protected static final XmlFormat TEXT_BUILDER_XML = new XmlFormat(
            new TextBuilder().getClass()) {
        public void format(Object obj, XmlElement xml) {
            xml.setAttribute("text", (TextBuilder) obj);
        }

        public Object parse(XmlElement xml) {
            TextBuilder tb = TextBuilder.newInstance();
            tb.append(xml.getAttribute("text"));
            return tb;
        }
    };

    /**
     * Holds the factory for this text builder.
     */
    private static final Factory FACTORY = new Factory() {
        public Object create() {
            return new TextBuilder();
        }

        public void cleanup(Object obj) {
            ((TextBuilder) obj).reset();
        }
    };

    //
    // Holds the character buffers. The array sizes are adjusted to ensures that
    // no more than 4 time the required space is ever allocated.
    //
    // char[1<<D3][1<<D2][1<<D1][1<<D0]
    // with charAt(i) = char[(i>>R3)&M3][(i>>R2)&M2][(i>>R1)&M1][(i>>R0)&M0]
    // 

    private static final int D0 = 5;

    private static final int R0 = 0;

    private static final int M0 = (1 << D0) - 1;

    private static final int C0 = 1 << D0; // capacity chars0

    private static final int D1 = D0 + 2;

    private static final int R1 = D0;

    private static final int M1 = (1 << D1) - 1;

    private static final int C1 = 1 << (D0 + D1); // capacity chars1

    private static final int D2 = D1 + 2;

    private static final int R2 = D0 + D1;

    private static final int M2 = (1 << D2) - 1;

    private static final int C2 = 1 << (D0 + D1 + D2); // capacity chars2

    private static final int D3 = D2 + 2;

    private static final int R3 = D0 + D1 + D2;

    private static final int M3 = (1 << D3) - 1;

    private final char[] _chars0 = new char[1 << D0]; // 5 bits (32).

    private char[][] _chars1; // new char[1<<7][1<<5]; // 12 bits (4096)

    private char[][][] _chars2; // new char[1<<9][1<<7][1<<5]; // 21 bits (2097152)

    private char[][][][] _chars3; // new char[1<<11][1<<9][1<<7][1<<5]; 

    private static final ObjectFactory CHARS0_FACTORY = new ObjectFactory() {
        public Object create() {
            return new char[1 << D0];
        }
    };

    private static final ObjectFactory CHARS1_FACTORY = new ObjectFactory() {
        public Object create() {
            return new char[1 << D1][];
        }
    };

    private static final ObjectFactory CHARS2_FACTORY = new ObjectFactory() {
        public Object create() {
            return new char[1 << D2][][];
        }
    };

    private static final ObjectFactory CHARS3_FACTORY = new ObjectFactory() {
        public Object create() {
            return new char[1 << D3][][][];
        }
    };

    /**
     * Holds the current capacity. 
     */
    private int _capacity = 1 << D0;

    /**
     * Holds the current length.
     */
    private int _length;

    /**
     * Creates a text builder of small initial capacity.
     */
    public TextBuilder() {
    }

    /**
     * Creates a text builder holding the specified character sequence.
     * 
     * @param csq the initial character sequence of this text builder.
     */
    public TextBuilder(CharSequence csq) {
        append(csq);
    }

    /**
     * Creates a text builder of specified initial capacity.
     * Unless the text length exceeds the specified capacity, operations 
     * on this text builder will not allocate memory.
     * 
     * @param capacity the initial capacity.
     */
    public TextBuilder(int capacity) {
        while (capacity > _capacity) {
            increaseCapacity();
        }
    }

    /**
     * Returns a text builder allocated from the "stack" when executing in
     * a {@link javolution.realtime.PoolContext PoolContext}).
     *
     * @return a new, preallocated or recycled text builder instance.
     */
    public static TextBuilder newInstance() {
        return (TextBuilder) FACTORY.object();
    }

    /**
     * Returns the length (character count) of this text builder.
     *
     * @return the number of characters (16-bits Unicode).
     */
    public final int length() {
        return _length;
    }

    /**
     * Returns the character at the specified index.
     *
     * @param  index the index of the character.
     * @return the character at the specified index.
     * @throws IndexOutOfBoundsException if <code>(index < 0) || 
     *         (index >= this.length())</code>.
     */
    public final char charAt(int index) {
        if ((index < 0) || (index >= _length))
            throw new IndexOutOfBoundsException("index: " + index);
        if (index < C0) {
            return _chars0[index];
        } else if (index < C1) {
            return _chars1[(index >> R1)][index & M0];
        } else if (index < C2) {
            return _chars2[(index >> R2)][(index >> R1) & M1][index & M0];
        } else {
            return _chars3[(index >> R3)][(index >> R2) & M2][(index >> R1)
                    & M1][index & M0];
        }
    }

    /**
     * Copies the character from this text builder into the destination
     * character array. 
     *
     * @param srcBegin this text start index.
     * @param srcEnd this text end index (not included).
     * @param dst the destination array to copy the data into.
     * @param dstBegin the offset into the destination array. 
     * @throws IndexOutOfBoundsException if <code>(srcBegin < 0) ||
     *  (dstBegin < 0) || (srcBegin > srcEnd) || (srcEnd > this.length())
     *  || ((dstBegin + srcEnd - srcBegin) >  dst.length)</code>
     */
    public final void getChars(int srcBegin, int srcEnd, char[] dst,
            int dstBegin) {
        if ((srcBegin < 0) || (dstBegin < 0) || (srcBegin > srcEnd)
                || (srcEnd > this.length())
                || ((dstBegin + srcEnd - srcBegin) > dst.length))
            throw new IndexOutOfBoundsException();
        for (int i = srcBegin, j = dstBegin; i < srcEnd;) {
            dst[j++] = charAt(i++);
        }
    }

    /**
     * Sets the character at the specified position.
     *
     * @param index the index of the character to modify.
     * @param c the new character. 
     * @throws IndexOutOfBoundsException if <code>(index < 0) || 
     *          (index >= this.length())</code>
     */
    public final void setCharAt(int index, char c) {
        if ((index < 0) && (index >= _length))
            throw new IndexOutOfBoundsException("index: " + index);
        if (index < C0) {
            _chars0[index] = c;
        } else if (index < C1) {
            _chars1[(index >> R1)][index & M0] = c;
        } else if (index < C2) {
            _chars2[(index >> R2)][(index >> R1) & M1][index & M0] = c;
        } else {
            _chars3[(index >> R3)][(index >> R2) & M2][(index >> R1) & M1][index
                    & M0] = c;
        }
    }

    /**
     * Sets the length of this character builder.
     * If the length is greater than the current length; the 
     * null character <code>'&#92;u0000'</code> is inserted. 
     *
     * @param newLength the new length of this builder.
     * @throws IndexOutOfBoundsException if <code>(newLength < 0)</code>
     */
    public final void setLength(int newLength) {
        if (newLength < 0)
            throw new IndexOutOfBoundsException();
        if (newLength <= _length) {
            _length = newLength;
        } else {
            for (int i = _length; i++ < newLength;) {
                append('\u0000');
            }
        }
    }

    /**
     * Returns an instance of {@link Text} (immutable) corresponding 
     * to the character sequence between the specified indexes.
     *
     * @param  start the index of the first character inclusive.
     * @param  end the index of the last character exclusive.
     * @return an immutable character sequence.
     * @throws IndexOutOfBoundsException if <code>(start < 0) || (end < 0) ||
     *         (start > end) || (end > this.length())</code>
     */
    public final CharSequence subSequence(int start, int end) {
        if ((start < 0) || (end < 0) || (start > end) || (end > _length))
            throw new IndexOutOfBoundsException();
        return Text.valueOf(this, start, end);
    }

    /**
     * Appends the specified character.
     *
     * @param  c the character to append.
     * @return <code>this</code>
     */
    public final Appendable append(char c) {
        if (_length >= _capacity)
            increaseCapacity();
        final int i = _length++;
        if (i < C0) {
            _chars0[i] = c;
        } else if (i < C1) {
            _chars1[(i >> R1)][i & M0] = c;
        } else if (i < C2) {
            _chars2[(i >> R2)][(i >> R1) & M1][i & M0] = c;
        } else {
            _chars3[(i >> R3)][(i >> R2) & M2][(i >> R1) & M1][i & M0] = c;
        }
        return this;
    }

    /**
     * Appends the specified character sequence. If the specified character
     * sequence is <code>null</code> this method is equivalent to
     * <code>append("null")</code>.
     *
     * @param  csq the character sequence to append or <code>null</code>.
     * @return <code>this</code>
     */
    public final Appendable append(CharSequence csq) {
        return (csq == null) ? append("null") : append(csq, 0, csq.length());
    }

    /**
     * Appends a subsequence of the specified character sequence.
     * If the specified character sequence is <code>null</code> this method 
     * is equivalent to <code>append("null")</code>. 
     *
     * @param  csq the character sequence to append or <code>null</code>.
     * @param  start the index of the first character to append.
     * @param  end the index after the last character to append.
     * @return <code>this</code>
     * @throws IndexOutOfBoundsException if <code>(start < 0) || (end < 0) 
     *         || (start > end) || (end > csq.length())</code>
     */
    public final Appendable append(CharSequence csq, int start, int end) {
        if (csq == null)
            return append("null");
        if ((start < 0) || (end < 0) || (start > end) || (end > csq.length()))
            throw new IndexOutOfBoundsException();
        for (int i = start; i < end;) {
            append(csq.charAt(i++));
        }
        return this;
    }

    /**
     * Appends the textual representation of the specified object. 
     * If the specified object is <code>null</code> this method 
     * is equivalent to <code>append("null")</code>. 
     *
     * @param obj the object to represent or <code>null</code>.
     * @return <code>this</code>
     */
    public final TextBuilder append(Object obj) {
        if (obj instanceof String) {
            return append((String) obj);
        } else if (obj instanceof CharSequence) {
            return (TextBuilder) append((CharSequence) obj);
        } else if (obj instanceof Realtime) {
            return append(((Realtime) obj).toText());
        } else if (obj != null) {
            return append(obj.toString());
        } else {
            return append("null");
        }
    }

    /**
     * Appends the specified string to this text builder. 
     * If the specified string is <code>null</code> this method 
     * is equivalent to <code>append("null")</code>. 
     *
     * @param str the string to append or <code>null</code>.
     * @return <code>this</code>
     */
    public final TextBuilder append(String str) {
        if (str == null)
            return append("null");
        final int length = str.length();
        for (int i = 0; i < length;) {
            append(str.charAt(i++));
        }
        return this;
    }

    /**
     * Appends a subsequence of the specified string.
     * If the specified character sequence is <code>null</code> this method 
     * is equivalent to <code>append("null")</code>. 
     *
     * @param  str the string to append or <code>null</code>.
     * @param  start the index of the first character to append.
     * @param  end the index after the last character to append.
     * @return <code>this</code>
     * @throws IndexOutOfBoundsException if <code>(start < 0) || (end < 0) 
     *         || (start > end) || (end > csq.length())</code>
     */
    public final TextBuilder append(String str, int start, int end) {
        if (str == null)
            return append("null");
        if ((start < 0) || (end < 0) || (start > end) || (end > str.length()))
            throw new IndexOutOfBoundsException();
        for (int i = start; i < end;) {
            append(str.charAt(i++));
        }
        return this;
    }

    /**
     * Appends the specified text to this text builder. 
     * If the specified text is <code>null</code> this method 
     * is equivalent to <code>append("null")</code>. 
     *
     * @param text the text to append or <code>null</code>.
     * @return <code>this</code>
     */
    public TextBuilder append(Text text) {
        if (text == null)
            return append("null");
        final int length = text.length();
        for (int i = 0; i < length;) {
            append(text.charAt(i++));
        }
        return this;
    }

    /**
     * Appends the characters from the char array argument.
     *
     * @param  chars the character array source.
     * @return <code>this</code>
     */
    public final TextBuilder append(char chars[]) {
        return append(chars, 0, chars.length);
    }

    /**
     * Appends the characters from a subarray of the char array argument.
     *
     * @param  chars the character array source.
     * @param  offset the index of the first character to append.
     * @param  length the number of character to append.
     * @return <code>this</code>
     * @throws IndexOutOfBoundsException if <code>(offset < 0) || 
     *         (length < 0) || ((offset + length) > chars.length)</code>
     */
    public final TextBuilder append(char chars[], int offset, int length) {
        if ((offset < 0) || (length < 0) || ((offset + length) > chars.length))
            throw new IndexOutOfBoundsException();
        final int end = offset + length;
        for (int i = offset; i < end;) {
            append(chars[i++]);
        }
        return this;
    }

    /**
     * Appends the textual representation of the specified <code>boolean</code>
     * (equivalent to <code>TypeFormat.format(b, this)</code>).
     *
     * @param  b the <code>boolean</code> to format.
     * @return <code>this</code>
     * @see    TypeFormat
     */
    public final TextBuilder append(boolean b) {
        try {
            TypeFormat.format(b, this);
            return this;
        } catch (IOException e) {
            throw new JavolutionError(e);
        }
    }

    /**
     * Appends the decimal representation of the specified <code>int</code>
     * (equivalent to <code>TypeFormat.format(i, this)</code>).
     *
     * @param  i the <code>int</code> to format.
     * @return <code>this</code>
     * @see    TypeFormat
     */
    public final TextBuilder append(int i) {
        try {
            TypeFormat.format(i, this);
            return this;
        } catch (IOException e) {
            throw new JavolutionError(e);
        }
    }

    /**
     * Appends the radix representation of the specified <code>int</code>
     * argument.
     *
     * @param  i the <code>int</code> to format.
     * @param  radix the radix (e.g. <code>16</code> for hexadecimal).
     * @return <code>this</code>
     * @see    TypeFormat
     */
    public final TextBuilder append(int i, int radix) {
        try {
            TypeFormat.format(i, radix, this);
            return this;
        } catch (IOException e) {
            throw new JavolutionError(e);
        }
    }

    /**
     * Appends the decimal representation of the specified <code>long</code>
     * (equivalent to <code>TypeFormat.format(l, this)</code>).
     *
     * @param  l the <code>long</code> to format.
     * @return <code>this</code>
     * @see    TypeFormat
     */
    public final TextBuilder append(long l) {
        try {
            TypeFormat.format(l, this);
            return this;
        } catch (IOException e) {
            throw new JavolutionError(e);
        }
    }

    /**
     * Appends the radix representation of the specified <code>long</code>
     * argument.
     *
     * @param  l the <code>long</code> to format.
     * @param  radix the radix (e.g. <code>16</code> for hexadecimal).
     * @return <code>this</code>
     * @see    TypeFormat
     */
    public final TextBuilder append(long l, int radix) {
        try {
            TypeFormat.format(l, radix, this);
            return this;
        } catch (IOException e) {
            throw new JavolutionError(e);
        }
    }

    /**
     * Appends the textual representation of the specified <code>float</code>
     * (equivalent to <code>TypeFormat.format(f, this)</code>).
     *
     * @param  f the <code>float</code> to format.
     * @return <code>this</code>
     * @see    TypeFormat
     /*@FLOATING_POINT@
     public final TextBuilder append(float f) {
     try {
     TypeFormat.format(f, this);
     return this;
     } catch (IOException e) {
     throw new JavolutionError(e);
     }
     }
     /**/

    /**
     * Appends the textual representation of the specified <code>double</code>
     * (equivalent to <code>TypeFormat.format(d, this)</code>).
     *
     * @param  d the <code>double</code> to format.
     * @return <code>this</code>
     * @see    TypeFormat
     /*@FLOATING_POINT@
     public final TextBuilder append(double d) {
     try {
     TypeFormat.format(d, this);
     return this;
     } catch (IOException e) {
     throw new JavolutionError(e);
     }
     }
     /**/

    /**
     * Inserts the specified character sequence at the specified location.
     *
     * @param index the insertion position.
     * @param csq the character sequence being inserted.
     * @return <code>this</code>
     * @throws IndexOutOfBoundsException if <code>(index < 0) || 
     *         (index > this.length())</code>
     */
    public final TextBuilder insert(int index, CharSequence csq) {
        if ((index < 0) || (index > _length))
            throw new IndexOutOfBoundsException("index: " + index);
        final int shift = csq.length();
        _length += shift;
        while (_length >= _capacity) {
            increaseCapacity();
        }
        for (int i = _length - shift; --i >= index;) {
            this.setCharAt(i + shift, this.charAt(i));
        }
        for (int i = csq.length(); --i >= 0;) {
            this.setCharAt(index + i, csq.charAt(i));
        }
        return this;
    }

    /**
     * Removes the characters between the specified indices.
     * 
     * @param start the beginning index, inclusive.
     * @param end the ending index, exclusive.
     * @return <code>this</code>
     * @throws IndexOutOfBoundsException if <code>(start < 0) || (end < 0) 
     *         || (start > end) || (end > this.length())</code>
     */
    public final TextBuilder delete(int start, int end) {
        if ((start < 0) || (end < 0) || (start > end) || (end > this.length()))
            throw new IndexOutOfBoundsException();
        for (int i = end, j = start; i < _length;) {
            this.setCharAt(j++, this.charAt(i++));
        }
        _length -= end - start;
        return this;
    }

    /**
     * Reverses this character sequence.
     *
     * @return <code>this</code>
     */
    public final TextBuilder reverse() {
        final int n = _length - 1;
        for (int j = (n - 1) >> 1; j >= 0;) {
            char c = charAt(j);
            setCharAt(j, charAt(n - j));
            setCharAt(n - j--, c);
        }
        return this;
    }

    /**
     * Returns the {@link Text} corresponding to this {@link TextBuilder}
     * (allocated on the "stack" when executing in a 
     * {@link javolution.realtime.PoolContext PoolContext}).
     *
     * @return the corresponding {@link Text} instance.
     */
    public final Text toText() {
        return Text.valueOf(this, 0, this.length());
    }

    /**
     * Resets this text builder for reuse (sets its length to <code>0</code>).
     */
    public final void reset() {
        setLength(0);
    }

    /**
     * Returns the hash code for this text builder.
     *
     * @return the hash code value.
     */
    public final int hashCode() {
        int h = 0;
        for (int i = 0; i < _length;) {
            h = 31 * h + charAt(i++);
        }
        return h;
    }

    /**
     * Compares this text builder against the specified object for equality.
     * Returns <code>true</code> if the specified object is a text builder 
     * having the same character content.
     * 
     * @param  obj the object to compare with or <code>null</code>.
     * @return <code>true</code> if that is a text builder with the same 
     *         character content as this text; <code>false</code> otherwise.
     */
    public final boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof TextBuilder))
            return false;
        TextBuilder that = (TextBuilder) obj;
        if (this._length != that._length)
            return false;
        for (int i = 0; i < _length;) {
            if (this.charAt(i) != that.charAt(i++))
                return false;
        }
        return true;
    }

    /**
     * Increases this text builder capacity.
     */
    private void increaseCapacity() {
        final int c = _capacity;
        _capacity += 1 << D0;
        if (c < C1) {
            if (_chars1 == null) {
                _chars1 = (char[][]) CHARS1_FACTORY.newObject();
            }
            _chars1[(c >> R1)] = (char[]) CHARS0_FACTORY.newObject();

        } else if (c < C2) {
            if (_chars2 == null) {
                _chars2 = (char[][][]) CHARS2_FACTORY.newObject();
            }
            if (_chars2[(c >> R2)] == null) {
                _chars2[(c >> R2)] = (char[][]) CHARS1_FACTORY.newObject();
            }
            _chars2[(c >> R2)][(c >> R1) & M1] = (char[]) CHARS0_FACTORY
                    .newObject();

        } else {
            if (_chars3 == null) {
                _chars3 = (char[][][][]) CHARS3_FACTORY.newObject();
            }
            if (_chars3[(c >> R3)] == null) {
                _chars3[(c >> R3)] = (char[][][]) CHARS2_FACTORY.newObject();
            }
            if (_chars3[(c >> R3)][(c >> R2) & M2] == null) {
                _chars3[(c >> R3)][(c >> R2) & M2] = (char[][]) CHARS1_FACTORY
                        .newObject();
            }
            _chars3[(c >> R3)][(c >> R2) & M2][(c >> R1) & M1] = (char[]) CHARS0_FACTORY
                    .newObject();
        }
    }
}