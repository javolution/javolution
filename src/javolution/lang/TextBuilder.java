/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2004 - The Javolution Team (http://javolution.org/)
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.lang;

import j2me.io.Serializable;
import j2me.lang.CharSequence;

import java.io.IOException;

import javolution.JavolutionError;
import javolution.lang.Text.Composite;
import javolution.lang.Text.Primitive;
import javolution.realtime.Realtime;
import javolution.realtime.RealtimeObject;
import javolution.xml.XmlElement;
import javolution.xml.XmlFormat;

/**
 * <p> This class represents a mutable character sequence whose size expands 
 *     gently without incurring expensive resize/copy operations ever.</p>
 * <p> Unlike <code>StringBuffer</code> or <code>StringBuilder</code> instances
 *     of this classes do not have a capacity.</p>
 * <p> Instances of this class are real-time compliant, they are  
 *     constructed on the stack when executing in a 
 *     {@link javolution.realtime.PoolContext PoolContext}.</p>
 * <p> As for any {@link Appendable}, formatting of primitive types can
 *     be achieved using the {@link javolution.lang.TypeFormat} utility class.</p>
 * <p> Instances of {@link TextBuilder} require synchronization if used 
 *     by multiple threads. Also, <code>static/shared</code> instances modified
 *     while in pool context have to be {@link #moveHeap moved to the heap} to
 *     avoid referencing local (stack) objects (see <a href=
 *     "{@docRoot}/javolution/realtime/package-summary.html#FAQ-4">
 *     Real-Time FAQ, Item 4.2</a>).</p>  
 *  
 * <p> <i>This class is public domain (not copyrighted).</i></p>
 * 
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 1.0, October 4, 2004
 */
public final class TextBuilder extends RealtimeObject implements Appendable,
        CharSequence, Serializable {

    /**
     * Holds the default XML representation for this class and its sub-classes.
     * This representation consists of a <code>"text"</code> attribute 
     * holding the character sequence.
     * Instances are created using the {@link #newInstance}
     * factory method during deserialization (on the stack when
     * executing in a {@link javolution.realtime.PoolContext PoolContext}).
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
     * Holds the text head (immutable).
     */
    private Text _head;

    /**
     * Holds the text tail (mutable).
     */
    private Primitive _tail;

    /**
     *  Default constructor (heap allocated). 
     */
    private TextBuilder() {
    }

    /**
     * Returns a text builder allocated from the "stack" when executing in
     * a {@link javolution.realtime.PoolContext PoolContext}).
     *
     * @return a new or recycled text builder instance.
     */
    public static TextBuilder newInstance() {
        TextBuilder cb = (TextBuilder) FACTORY.object();
        cb._head = Text.EMPTY;
        cb._tail = Primitive.newInstance();
        return cb;
    }

    /**
     * Returns the length (character count) of this text builder.
     *
     * @return the number of characters (16-bits Unicode).
     */
    public int length() {
        return _head.length() + _tail.length();
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
        return (index < _head._count) ? _head.charAt(index) : _tail
                .charAt(index - _head._count);
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
        if (_tail._count < Text.Primitive.BLOCK_SIZE) {
            _tail._data[_tail._count++] = c;
        } else { // tail is full.
            flushTail();
            _tail._data[_tail._count++] = c;
        }
        return this;
    }

    /**
     * Appends the textual representation of this object.
     *
     * @param  obj the object whose textual representation is appended.
     * @return <code>this</code>
     */
    public TextBuilder append(Object obj) {
        if (obj instanceof Realtime) {
            return append(((Realtime) obj).toText());
        } else if (obj != null) {
            return append(obj.toString());
        } else {
            return append(Text.NULL);
        }
    }

    /**
     * Appends the specified {@link Text text} instance. 
     *
     * @param  text the text to append.
     * @return <code>this</code>
     */
    public TextBuilder append(Text text) {
        if (_tail._count + text._count <= Text.Primitive.BLOCK_SIZE) {
            // Enough space to append.
            for (int i=0; i < text._count;) {
                _tail._data[_tail._count++] = text.charAt(i++);
            }
        } else {
            flushTail();
            _head = _head.plus(text);
        }
        return this;
    }

    /**
     * Appends the specified {@link String} instance. 
     *
     * @param  str the string to append.
     * @return <code>this</code>
     */
    public TextBuilder append(String str) {
        if (_tail._count + str.length() <= Text.Primitive.BLOCK_SIZE) {
            // Enough space to append.
            for (int i=0; i < str.length();) {
                _tail._data[_tail._count++] = str.charAt(i++);
            }
        } else {
            flushTail();
            _head = _head.plus(Text.valueOf(str));
        }
        return this;
    }

    /**
     * Appends the specified character sequence.
     *
     * @param  csq the character sequence to append or <code>"null"</code>
     *         if <code>(chars == null)</code>.
     * @return <code>this</code>
     */
    public Appendable append(CharSequence csq) {
        if (csq != null) {
            return append(csq, 0, csq.length());
        } else {
            return append(Text.NULL);
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
            final int length = end - start;
            if (_tail._count + length <= Text.Primitive.BLOCK_SIZE) {
                // Enough space to append.
                for (int i=start; i < end;) {
                    _tail._data[_tail._count++] = csq.charAt(i++);
                }
            } else {
                flushTail();
                _head = _head.plus(Text.valueOf(csq, start, end));
            }
            return this;
        } else {
            return append(Text.NULL);
        }
    }

    /**
     * Appends the specified characters from the character array.
     *
     * @param str the characters to be appended.
     * @param offset the index of the first character to append.
     * @param length the number of characters to append.
     * @return <code>this</code>
     */
    public TextBuilder append(char str[], int offset, int length) {
        if (_tail._count + length <= Text.Primitive.BLOCK_SIZE) { 
            // Enough space to append.
            for (int i=offset; i < offset+length;) {
                _tail._data[_tail._count++] = str[i++];
            }
        } else {
            flushTail();
            _head = _head.plus(Text.valueOf(str, offset, length));
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
     * Inserts the specified character at the specified location.
     *
     * @param index the insertion position.
     * @param c the character being inserted.
     * @return <code>this</code>.
     * @throws IndexOutOfBoundsException if index is negative or greater
     *         than <code>this.length()</code>.
     */
    public TextBuilder insert(int index, char c) {
        flushTail();
        Text head = _head.subtext(0, index);
        Text tail = _head.subtext(index);
        _head = head.plus(Text.valueOf(c)).plus(tail);
        return this;
    }

    /**
     * Inserts the specified character sequence at the specified location.
     *
     * @param index the insertion position.
     * @param csq the character sequence being inserted.
     * @return <code>this</code>.
     * @throws IndexOutOfBoundsException if index is negative or greater
     *         than <code>this.length()</code>.
     */
    public TextBuilder insert(int index, CharSequence csq) {
        flushTail();
        Text head = _head.subtext(0, index);
        Text tail = _head.subtext(index);
        _head = head.plus(Text.valueOf(csq)).plus(tail);
        return this;
    }

    /**
     * Inserts the specified characters from the character array.
     *
     * @param index the insertion position.
     * @param str the characters to be inserted.
     * @param offset the index of the first character to insert.
     * @param length the number of characters to insert.
     * @return <code>this</code>
     */
    public TextBuilder insert(int index, char str[], int offset, int length) {
        flushTail();
        Text head = _head.subtext(0, index);
        Text tail = _head.subtext(index);
        _head = head.plus(Text.valueOf(str, offset, length)).plus(tail);
        return this;
    }

    /**
     * Removes the character at the specified position.
     *
     * @param index the index of the character to remove.
     * @return <code>this</code>
     * @throws IndexOutOfBoundsException if index is negative, or index 
     *         is equal or greater than <code>this.length()</code>.
     */
    public TextBuilder deleteCharAt(int index) {
        return delete(index, index + 1);
    }

    /**
     * Removes the characters between the specified indexes.
     *
     * @param start the beginning index, inclusive.
     * @param end the ending index, exclusive.
     * @return <code>this</code>
     * @throws IndexOutOfBoundsException if start or end are negative, start 
     *         is greater than end, or end is greater than 
     *         <code>this.length()</code>.
     */
    public TextBuilder delete(int start, int end) {
        flushTail();
        Text head = _head.subtext(0, start);
        Text tail = _head.subtext(end);
        _head = head.plus(tail);
        return this;
    }

    /**
     * Sets the character at the specified position.
     *
     * @param index the index of the character to modify.
     * @param c the new character. 
     * @throws IndexOutOfBoundsException if index is negative, or index 
     *         is equal or greater than <code>this.length()</code>.
     */
    public void setCharAt(int index, char c) {
        flushTail();
        Text head = _head.subtext(0, index);
        Text tail = _head.subtext(index + 1);
        _head = head.plus(Text.valueOf(c)).plus(tail);
    }

    /**
     * Replaces a subsequence of characters with the specified character 
     * sequence.
     * 
     * @param start the beginning index, inclusive.
     * @param end the ending index, exclusive.
     * @param csq the character sequence that will replace previous contents.
     * @return <code>this</code>
     * @throws IndexOutOfBoundsException if start or end are negative, start 
     *         is greater than end, or end is greater than 
     *         <code>this.length()</code>.
     */
    public TextBuilder replace(int start, int end, CharSequence csq) {
        flushTail();
        Text head = _head.subtext(0, start);
        Text tail = _head.subtext(end);
        _head = head.plus(Text.valueOf(csq)).plus(tail);
        return this;
    }

    /**
     * Reverses this character sequence.
     *
     * @return <code>this</code>
     */
    public TextBuilder reverse() {
        flushTail();
        Text source = _head;
        _head = Text.EMPTY;
        for (int i=source._count; i > 0;) {
            append(source.charAt(--i));
        }
        return this;
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
        flushTail();
        if (newLength <= _head._count) {
            _head = _head.subtext(0, newLength);
        } else {
            for (int i = _head._count; i < newLength;) {
                append('\u0000');
            }
        }
    }

    /**
     * Returns the {@link Text} representation of this character sequence
     * (immediate).
     *
     * @return the corresponding {@link Text} instance.
     */
    public Text toText() {
        flushTail();
        return _head;
    }

    /**
     * Moves the tail to the immutable head.
     */
    private void flushTail() {
        if (_tail._count > 0) {
            if (_head != Text.EMPTY) {
                _head = Composite.newInstance(_head, _tail);
            } else {
                _head = _tail;
            }
            _tail = Primitive.newInstance();
        }
    }

    // Factory of TextBuilder instances.
    private static final Factory FACTORY = new Factory() {
        public Object create() {
            return new TextBuilder();
        }
    };

    // Overrides.
    public void move(ContextSpace cs) {
        super.move(cs);
        _head.move(cs);
        _tail.move(cs);
    }

    private static final long serialVersionUID = 3258408430636380468L;

}