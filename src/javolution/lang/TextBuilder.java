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
 * <p> This class represents an {@link Appendable} text whose size expands 
 *     gently without incurring expensive resize/copy operations ever.</p>
 *     
 * <p> This class is not intended for large documents manipulations which 
 *     should be performed with the {@link Text} class directly 
 *     (<code>O(Log(n))</code> {@link Text#insert insertion} and 
 *     {@link Text#delete deletion} capabilities).</p>
 *     
 * <p> {@link TextBuilder} are {@link Reusable} and can be part of 
 *     higher level {@link Reusable} components (the text maximum length 
 *     determinates the maximum number of allocations performed).</p>
 *  
 * <p> This implementation is not synchronized.</p>
 *     
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 3.0, February 6, 2005
 */
public final class TextBuilder extends RealtimeObject implements Appendable,
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
     *  Default constructor (heap allocated). 
     */
    public TextBuilder() {
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
    public int length() {
        return _length;
    }

    /**
     * Returns the character at the specified index.
     *
     * @param  i the index of the character.
     * @return the character at the specified index.
     * @throws IndexOutOfBoundsException if index is negative, or index 
     *         is equal or greater than <code>this.length()</code>.
     */
    public char charAt(int i) {
        if ((i >= 0) && (i < _length)) {
            if (i < C0) {
                return _chars0[i];
            } else if (i < C1) {
                return _chars1[(i>>R1)][i&M0];
            } else if (i < C2) {
                return _chars2[(i>>R2)][(i>>R1)&M1][i&M0];
            } else {
                return _chars3[(i>>R3)][(i>>R2)&M2][(i>>R1)&M1][i&M0];
            }
        }
        throw new IndexOutOfBoundsException("index: " + i);
    }
                                         
    /**
     * Sets the character at the specified position.
     *
     * @param i the index of the character to modify.
     * @param c the new character. 
     * @throws IndexOutOfBoundsException if index is negative, or index 
     *         is equal or greater than <code>this.length()</code>.
     */
    public void setCharAt(int i, char c) {
        if ((i >= 0) && (i < _length)) {
            if (i < C0) {
                _chars0[i] = c;
            } else if (i < C1) {
                _chars1[(i>>R1)][i&M0] = c;
            } else if (i < C2) {
                _chars2[(i>>R2)][(i>>R1)&M1][i&M0] = c;
            } else {
                _chars3[(i>>R3)][(i>>R2)&M2][(i>>R1)&M1][i&M0] = c;
            }
        }
        throw new IndexOutOfBoundsException("index: " + i);
    }

    /**
     * Sets the length of this character builder.
     * If the length is greater than the current length; the 
     * null character <code>'&#92;u0000'</code> is inserted. 
     *
     * @param newLength the new length of this builder.
     * @throws IndexOutOfBoundsException if newLength is negative.
     */
    public void setLength(int newLength) {
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
     * @throws IndexOutOfBoundsException if start or end are negative, start 
     *         is greater than end, or end is greater than 
     *         <code>this.length()</code>.
     */
    public CharSequence subSequence(int start, int end) {
        return toText().subtext(start, end);
    }

    /**
     * Appends the specified character.
     *
     * @param  c the character to append.
     * @return <code>this</code>
     */
    public Appendable append(char c) {
        if (_length >= _capacity) {
            increaseCapacity();
        }
        final int i = _length++;
        if (i < C0) {
            _chars0[i] = c;
        } else if (i < C1) {
            _chars1[(i>>R1)][i&M0] = c;
        } else if (i < C2) {
            _chars2[(i>>R2)][(i>>R1)&M1][i&M0] = c;
        } else {
            _chars3[(i>>R3)][(i>>R2)&M2][(i>>R1)&M1][i&M0] = c;
        }
        return this;
    }


    /**
     * Appends the specified character sequence.
     *
     * @param  csq the character sequence to append or <code>"null"</code>
     *         if <code>(csq == null)</code>.
     * @return <code>this</code>
     */
    public Appendable append(CharSequence csq) {
        if (csq != null) {
            final int length = csq.length();
            for (int i = 0; i < length;) {
                append(csq.charAt(i++));
            }
            return this;
        } else {
            return append("null");
        }
    }

    /**
     * Appends a subsequence of the specified character sequence. 
     *
     * @param  csq the character sequence to append.
     * @param  start the index of the first character to append.
     * @param  end the index after the last character to append.
     * @return <code>this</code>
     */
    public Appendable append(CharSequence csq, int start, int end) {
        if (csq != null) {
            for (int i = start; i < end;) {
                append(csq.charAt(i++));
            }
            return this;
        } else {
            return append("null");
        }
    }

    /**
     * Appends the specified {@link String} instance (not a 
     * <code>CharSequence</code> prior to JDK 1.4). 
     *
     * @param  str the string to append.
     * @return <code>this</code>
     */
    public TextBuilder append(String str) {
        final int length = str.length();
        for (int i = 0; i < length;)
            append(str.charAt(i++));
        return this;
    }

    /**
     * Appends the textual representation of the specified object. 
     *
     * @param  obj the object whose textual representatin is appended.
     * @return <code>this</code>
     */
    public TextBuilder append(Object obj) {
        if (obj instanceof Realtime) {
            append(((Realtime)obj).toText());
        } else if (obj != null) {
            append(obj.toString());
        } else {
            append("null");
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
    public TextBuilder append(boolean b) {
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
    public TextBuilder append(int i) {
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
    public TextBuilder append(int i, int radix) {
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
    public TextBuilder append(long l) {
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
    public TextBuilder append(long l, int radix) {
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
    public TextBuilder append(float f) {
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
    public TextBuilder append(double d) {
        try {
            TypeFormat.format(d, this);
            return this;
        } catch (IOException e) {
            throw new JavolutionError(e);
        }
    }
    /**/


    /**
     * Returns the {@link Text} corresponding to this {@link TextBuilder}
     * (allocated on the "stack" when executing in a 
     * {@link javolution.realtime.PoolContext PoolContext}).
     *
     * @return the corresponding {@link Text} instance.
     */
    public Text toText() {
        return Text.valueOf(this);
    }

    /**
     * Resets this text builder for reuse (sets its length to <code>0</code>).
     */
    public void reset() {
        setLength(0);
    }

    /**
     * Increases this text builder capacity.
     */
    private void increaseCapacity() {
        final int c = _capacity;
        _capacity += 1 << D0;
        if (c < C1) {
            if (_chars1 == null) {
                _chars1 = (char[][]) CHARS1_FACTORY.heapPool().next();
            }
            _chars1[(c>>R1)] = (char[]) CHARS0_FACTORY.heapPool().next();
            
        } else if (c < C2) {
            if (_chars2 == null) {
                _chars2 = (char[][][]) CHARS2_FACTORY.heapPool().next();
            }
            if (_chars2[(c>>R2)] == null) {
                _chars2[(c>>R2)] = (char[][]) CHARS1_FACTORY.heapPool().next();
            }
            _chars2[(c>>R2)][(c>>R1)&M1] = (char[]) CHARS0_FACTORY.heapPool().next();

        } else {
            if (_chars3 == null) {
                _chars3 = (char[][][][]) CHARS3_FACTORY.heapPool().next();
            }
            if (_chars3[(c>>R3)] == null) {
                _chars3[(c>>R3)] = (char[][][]) CHARS2_FACTORY.heapPool().next();
            }
            if (_chars3[(c>>R3)][(c>>R2)&M2] == null) {
                _chars3[(c>>R3)][(c>>R2)&M2] = (char[][]) CHARS1_FACTORY.heapPool().next();
            }
            _chars3[(c>>R3)][(c>>R2)&M2][(c>>R1)&M1] = (char[]) CHARS0_FACTORY.heapPool().next();
        }
        
    }

}