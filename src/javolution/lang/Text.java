/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2004 - The Javolution Team (http://javolution.org/)
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.lang;

import java.io.Serializable;
import java.util.Comparator;

import javolution.realtime.ArrayPool;
import javolution.realtime.ObjectPool;
import javolution.realtime.RealtimeObject;
import javolution.util.FastMap;
import javolution.util.MathLib;
import javolution.xml.XmlElement;
import javolution.xml.XmlFormat;

/**
 * <p> This class represents an immutable character sequence with extremely
 *     fast {@link #plus concatenation} speed.</p>
 * <p> Instances of this class have the following advantages over 
 *     {@link String}</code>:<ul>
 *     <li> No need for an intermediate {@link StringBuffer} in order to 
 *          concatenate {@link String}, the {@link #plus plus} or 
 *          {@link #concat concat} methods are faster.</li>
 *     <li> Bug free. They are not plagued by the {@link String#substring} <a
 *          href="http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4513622">
 *          memory leak bug</a> (when small substrings prevent memory from 
 *          larger string from being garbage collected).</li>
 *     <li> More flexible as they allows for search, concatenation and
 *          comparison with any <code>CharSequence</code> such as itself, 
 *          <code>java.lang.String</code>, <code>java.lang.StringBuffer</code>
 *          or <code>java.lang.StringBuilder (JDK1.5)</code>.</li>
 *     <li> Easy/fast creation using the {@link TextBuilder} class 
 *          (no need to specify the buffer capacity as it gently increases
 *          without incurring expensive resize/copy operations).</li>
 *     <li> Real-time compliant (instances allocated on the "stack" when 
 *          executing in a {@link javolution.realtime.PoolContext PoolContext}).</li>
 *     </ul></p>
 * <p> As for any <code>CharSequence</code>, parsing of primitive types can
 *     be achieved using the {@link javolution.lang.TypeFormat} utility class.</p>
 * <p> {@link Text} literals should be explicitely {@link #intern interned}. 
 *     Unlike strings literals and strings-value constant expressions,
 *     interning is not implicit. For example:<pre>
 *         final static Text TRUE = Text.valueOf("true").intern();
 *         final static Text FALSE = Text.valueOf("false").intern();
 *     </pre></p>
 * <p><i> Implementation Note: To avoid expensive copy operations (as in 
 *        {@link #concat concat}, {@link #subtext subtext}, 
 *        {@link #replace replace}, etc.), {@link Text} instances are broken
 *        down into smaller immutable sequences (they form a minimal-depth 
 *        binary tree). Copies are then performed in <code>O(Log(n))</code>
 *        instead of <code>O(n)</code>).</i></p>
 * 
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 1.0, October 4, 2004
 */
public abstract class Text extends RealtimeObject implements CharSequence,
        Comparable, Serializable {

    /**
     * Holds the default XML representation for this class and its sub-classes.
     * This representation consists of a <code>"value"</code> attribute 
     * holding the character sequence.
     * Instances are created using the {@link #valueOf(CharSequence)}
     * factory method during deserialization (on the stack when
     * executing in a {@link javolution.realtime.PoolContext PoolContext}).
     */
    protected static final XmlFormat TEXT_XML = new XmlFormat(Text.class) {
        public void format(Object obj, XmlElement xml) {
            xml.setAttribute("value", (Text)obj);
        }
        public Object parse(XmlElement xml) {
            return Text.valueOf(xml.getAttribute("value"));
        }
    };

    /**
     * Holds the text interned.
     */
    private static final FastMap INTERN_TEXT = new FastMap();

    /**
     * Holds an empty character sequence.
     */
    public static final Text EMPTY = Text.valueOf("").intern();

    /**
     * Holds the <code>"null"</code> character sequence.
     */
    static final Text NULL = Text.valueOf("null").intern();

    /**
     * Holds a lexicographic comparator for any {@link CharSequence} 
     * (including {@link Text} instances).
     */
    public static final Comparator COMPARATOR = new Comparator() {

        /**
         * Compares two character sequences lexicographically.
         *
         * @param left the first character sequence.
         * @param right the second character sequence.
         * @return the value <code>0</code> if both sequences represent the same
         *         characters; a value less than <code>0</code> if the left 
         *         sequence is lexicographically less than the right sequence;
         *         and a value greater than <code>0</code> if the left sequence
         *         is lexicographically greater than the right sequence.
         * @throws ClassCastException if any of the specified object is not a
         *          <code>CharSequence</code>.
         */
        public int compare(Object left, Object right) {
            CharSequence seq1 = (CharSequence) left;
            CharSequence seq2 = (CharSequence) right;
            int i = 0;
            int n = MathLib.min(seq1.length(), seq2.length());
            while (n-- != 0) {
                char c1 = seq1.charAt(i);
                char c2 = seq2.charAt(i++);
                if (c1 != c2) {
                    return c1 - c2;
                }
            }
            return seq1.length() - seq2.length();
        }
    };

    /**
     * Holds the total number of characters.
     */
    int _count;

    /**
     * Holds the hash code or <code>0</code> if unknown.
     */
    int _hashCode;

    /**
     * Default constructor.
     */
    private Text() {
    }

    /**
     * Returns the text representing the specified string.
     *
     * @param  str the string to represent as text.
     * @return the textual representation of the specified string.
     */
    public static Text valueOf(String str) {
        return valueOf(str, 0, str.length());
    }
    static Text valueOf(String str, int start, int end) {
        final int length = end - start;
        if (length <= Primitive.BLOCK_SIZE) {
            Primitive text = Primitive.newInstance();
            for (int i=0; i < length;) {
                text._data[text._count++] = str.charAt(start + i++);
            }
            return text;
        } else {
            final int middle = start + (length >> 1);
            Composite text = Composite.newInstance(
                    Text.valueOf(str, start, middle),
                    Text.valueOf(str, middle, end));
            return text;
        }
    }

    /**
     * Returns the text representing the specified character sequence.
     *
     * @param  csq the <code>CharSequence</code> source.
     * @return the corresponding instance.
     */
    public static Text valueOf(CharSequence csq) {
        return valueOf(csq, 0, csq.length());
    }
    static Text valueOf(CharSequence csq, int start, int end) {
        final int length = end - start;
        if (length <= Primitive.BLOCK_SIZE) {
            Primitive text = Primitive.newInstance();
            for (int i=0; i < length;) {
                text._data[text._count++] = csq.charAt(start + i++);
            }
            return text;
        } else {
            final int middle = start + (length >> 1);
            Composite text = Composite.newInstance(
                    Text.valueOf(csq, start, middle),
                    Text.valueOf(csq, middle, end));
            return text;
        }
    }

    /**
     * Returns the text that contains the characters from the specified 
     * subarray of characters.
     *
     * @param data the source of the characters.
     * @param offset the index of the first character in the data soure.
     * @param length the length of the subarray.
     * @return the corresponding instance.
     */
    public static Text valueOf(char[] data, int offset, int length) {
        if (length <= Primitive.BLOCK_SIZE) {
            Primitive text = Primitive.newInstance();
            for (int i=0; i < length;) {
                text._data[text._count++] = data[offset + i++];
            }
            return text;
        } else {
            final int middle = offset + (length >> 1);
            Composite text = Composite.newInstance(
                    Text.valueOf(data, offset, middle - offset),
                    Text.valueOf(data, middle, offset + length - middle));
            return text;
        }
    }

    /**
     * Returns the text representing the specified object 
     * (allocated on the "stack" when executing in a 
     * {@link javolution.realtime.PoolContext PoolContext}).
     *
     * @param  obj the object to represent as text.
     * @return the textual representation of the specified object.
     */
    public static Text valueOf(Object obj) {
        TextBuilder tb = TextBuilder.newInstance();
        tb.append(obj);
        return tb.toText();
    }

    /**
     * Returns the text representing the <code>boolean</code> argument.
     *
     * @param  b a <code>boolean</code>.
     * @return if the argument is <code>true</code>, a text equals
     *         to <code>"true"</code> is returned; otherwise, a text 
     *         equals to <code>"false"</code> is returned.
     * @see    TypeFormat#format(boolean, Appendable)
     */
    public static Text valueOf(boolean b) {
        return b ? TRUE : FALSE;
    }

    private final static Text TRUE = Text.valueOf("true").intern();

    private final static Text FALSE = Text.valueOf("false").intern();

    /**
     * Returns a text of one character.
     *
     * @param  c the single character.
     * @return a text of length <code>1</code> containing
     *         as its single character the argument <code>c</code>.
     */
    public static Text valueOf(char c) {
        if (c < ASCII_CHARS.length) {
            return ASCII_CHARS[c];
        } else {
            Primitive text = Primitive.newInstance();
            text._data[text._count++] = c;
            return text;
        }
    }
    private static final Text[] ASCII_CHARS = new Text[128];
    static {
        for (int i=0; i < ASCII_CHARS.length; i++) {
            Primitive text = Primitive.newInstance();
            text._data[text._count++] = (char)i;
            ASCII_CHARS[i] = text.intern();
        }
    }

    /**
     * Returns the decimal representation of the specified <code>int</code>
     * argument.
     *
     * @param  i the <code>int</code> number.
     * @return the decimal representation of the specified number.
     * @see    TypeFormat#format(int, Appendable)
     */
    public static Text valueOf(int i) {
        TextBuilder tb = TextBuilder.newInstance();
        tb.append(i);
        return tb.toText();
    }

    /**
     * Returns the radix representation of the specified <code>int</code>
     * argument.
     *
     * @param  i the <code>int</code> number.
     * @param  radix the radix (e.g. <code>16</code> for hexadecimal).
     * @return the radix representation of the specified number.
     * @see    TypeFormat#format(int, int, Appendable)
     */
    public static Text valueOf(int i, int radix) {
        TextBuilder tb = TextBuilder.newInstance();
        tb.append(i, radix);
        return tb.toText();
    }

    /**
     * Returns the decimal representation of the specified <code>long</code>
     * argument.
     *
     * @param  l the <code>long</code> number.
     * @return the decimal representation of the specified number.
     * @see    TypeFormat#format(long, Appendable)
     */
    public static Text valueOf(long l) {
       TextBuilder tb = TextBuilder.newInstance();
       tb.append(l);
       return tb.toText();
    }

    /**
     * Returns the radix representation of the specified <code>long</code>
     * argument.
     *
     * @param  l the <code>long</code> number.
     * @param  radix the radix (e.g. <code>16</code> for hexadecimal).
     * @return the radix representation of the specified number.
     * @see    TypeFormat#format(long, int, Appendable)
     */
    public static Text valueOf(long l, int radix) {
        TextBuilder tb = TextBuilder.newInstance();
        tb.append(l, radix);
        return tb.toText();
    }

    /**
     * Returns the text representing the specified
     * <code>float</code> argument. The error is assumed to be
     * the intrinsic <code>float</code> error (32 bits IEEE 754 format).
     *
     * @param  f the <code>float</code> number.
     * @return the floating point representation of this float.
     * @see    TypeFormat#format(float, Appendable)
     */
    public static Text valueOf(float f) {
        TextBuilder tb = TextBuilder.newInstance();
        tb.append(f);
        return tb.toText();
    }
    
    /**
     * Returns the text representing the specified
     * <code>double</code> argument. The error is assumed to be
     * the intrinsic <code>double</code> error (64 bits IEEE 754 format).
     *
     * @param  d the <code>double</code> number.
     * @return the floating point representation of this double.
     * @see    TypeFormat#format(double, Appendable)
     */
    public static Text valueOf(double d) {
        TextBuilder tb = TextBuilder.newInstance();
        tb.append(d);
        return tb.toText();
    }

    /**
     * Returns the length of this text.
     *
     * @return the number of characters (16-bits Unicode) composing this text.
     */
    public final int length() {
        return _count;
    }

    /**
     * Returns a portion of this text.
     * 
     * @param  start the index of the first character inclusive.
     * @return the sub-text starting at the specified position.
     * @throws IndexOutOfBoundsException if start is negative or greater 
     *         than <code>this.length()</code>.
     */
    public final Text subtext(int start) {
        return subtext(start, length());
    }

    /**
     * Returns a new character sequence that is a subsequence of this text.
     *
     * @param  start the index of the first character inclusive.
     * @param  end the index of the last character exclusive.
     * @return <code>this.subtext(start, end)</code>
     * @throws IndexOutOfBoundsException if start or end are negative, start 
     *         is greater than end, or end is greater than 
     *         <code>this.length()</code>.
     */
    public CharSequence subSequence(int start, int end) {
        return this.subtext(start, end);
    }

    /**
     * Returns the index within this text of the first occurrence
     * of the specified character sequence searching forward.
     *
     * @param  csq a character sequence.
     * @return the index of the first character of the character sequence found;
     *         or <code>-1</code> if the character sequence is not found.
     */
    public final int indexOf(CharSequence csq) {
        return indexOf(csq, 0);
    }

    /**
     * Returns the index within this text of the first occurrence
     * of the specified characters sequence searching forward from
     * the specified index.
     *
     * @param  csq a character sequence.
     * @param  fromIndex the index to start the search from.
     * @return the index in the range
     *         <code>[fromIndex, length() - csq.length()]</code> 
     *         or <code>-1</code> if the character sequence is not found.
     */
    public final int indexOf(CharSequence csq, int fromIndex) {
        return TypeFormat.indexOf(csq, this, fromIndex);
    }

    /**
     * Returns the index within this text of the last occurrence of
     * the specified characters sequence searching backward.
     *
     * @param  csq a character sequence.
     * @return the index of the first character of the character sequence found;
     *         or <code>-1</code> if the character sequence is not found.
     */
    public final int lastIndexOf(CharSequence csq) {
        return TypeFormat.lastIndexOf(csq, this, length());
    }

    /**
     * Returns the index within this text of the last occurrence of
     * the specified character sequence searching backward from the specified
     * index.
     *
     * @param  csq a character sequence.
     * @param  fromIndex the index to start the backward search from.
     * @return the index in the range <code>[0, fromIndex]</code> or
     *         <code>-1</code> if the character sequence is not found.
     */
    public final int lastIndexOf(CharSequence csq, int fromIndex) {
        return TypeFormat.lastIndexOf(csq, this, fromIndex);
    }

    /**
     * Indicates if this text starts with the specified prefix.
     *
     * @param  prefix the prefix.
     * @return <code>true</code> if the character sequence represented by the
     *         argument is a prefix of the character sequence represented by
     *         this string; <code>false</code> otherwise.
     */
    public final boolean startsWith(CharSequence prefix) {
        return startsWith(prefix, 0);
    }

    /**
     * Indicates if this text ends with the specified suffix.
     *
     * @param  suffix the suffix.
     * @return <code>true</code> if the character sequence represented by the
     *         argument is a suffix of the character sequence represented by
     *         this string; <code>false</code> otherwise.
     */
    public final boolean endsWith(CharSequence suffix) {
        return startsWith(suffix, length() - suffix.length());
    }

    /**
     * Indicates if this text starts with the specified prefix
     * at the specified index.
     *
     * @param  prefix the prefix.
     * @param  index the index of the prefix location in this string.
     * @return <code>this.substring(index).startsWith(prefix)</code>
     */
    public final boolean startsWith(CharSequence prefix, int index) {
        if ((index >= 0) && (index <= (this.length() - prefix.length()))) {
            for (int i = 0; i < prefix.length(); i++) {
                if (prefix.charAt(i) != this.charAt(i + index)) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * Concatenates the specified character sequence to the end of this text.
     *
     * @param  csq the character sequence that is concatenated.
     * @return the new character sequence result of the concatenation.
     * @see    #plus
     */
    public final Text concat(CharSequence csq) {
        return (csq instanceof Text) ? this.plus((Text) csq) : this.plus(Text
                .valueOf(csq));
    }

    /**
     * Returns a copy of this text, with leading and trailing
     * whitespace omitted.
     *
     * @return a copy of this text with leading and trailing white
     *          space removed, or this text if it has no leading or
     *          trailing white space.
     */
    public final Text trim() {
        int first = 0; // First character index.
        int last = length() - 1; // Last character index.
        while ((first <= last) && (charAt(first) <= ' ')) {
            first++;
        }
        while ((last >= first) && (charAt(last) <= ' ')) {
            last--;
        }
        int newLength = last - first + 1;
        return (newLength == length()) ? this : subtext(first, last + 1);
    }

    /**
     * Returns a text equals to this text but from a pool of unique text
     * instances.  
     * For any two text t1 and t2, <code>(t1.intern() == t2.intern())</code>
     * if and only if <code>(t1.equals(t2))</code>.
     * 
     * @return an unique instance allocated on the heap and equals to this text.
     */
    public final Text intern() {
        synchronized (INTERN_TEXT) {
            Text text = (Text) INTERN_TEXT.get(this);
            if (text == null) {
                text = this;
                text.moveHeap();
                INTERN_TEXT.put(text, text);
            }
            return text;
        }
    }

    /**
     * Compares this text against the specified object.
     *
     * <p> Note: Unfortunately, due to the current (JDK 1.4+) implementation
     *          of <code>java.lang.String</code> and <code>
     *          java.lang.StringBuffer</code>, this method is not symmetric.</p>
     *
     * @param  that the object to compare with.
     * @return <code>true</code> if that objects is a <code>CharSequence</code>
     *         or <code>String</code> represent the same sequence as this text;
     *         <code>false</code> otherwise.
     */
    public final boolean equals(Object that) {
        if (this == that) {
            return true;
        } else if (that instanceof CharSequence) {
            CharSequence seq = (CharSequence) that;
            final int length = this.length();
            if (length == seq.length()) {
                for (int i = 0; i < length;) {
                    if (this.charAt(i) != seq.charAt(i++)) {
                        return false;
                    }
                }
                return true;
            }
        } else if (that instanceof String) {
            // Prior to 1.4 String was not a CharSequence
            String str = (String) that;
            final int length = this.length();
            if (length == str.length()) {
                for (int i = 0; i < length;) {
                    if (this.charAt(i) != str.charAt(i++)) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Compares this text to the specified character sequence
     * ignoring case considerations. The two character sequence are considered
     * equal ignoring case if they are of the same length, and corresponding
     * characters in the two strings are equal ignoring case.
     *
     * @param  csq the <code>CharSequence</code> to compare this text against.
     * @return <code>true</code> if the argument is not <code>null</code>
     *         and the character sequences are equal, ignoring case;
     *         <code>false</code> otherwise.
     * @see     #equals(Object)
     */
    public final boolean equalsIgnoreCase(CharSequence csq) {
        if (this == csq) {
            return true;
        } else {
            final int length = this.length();
            if (length == csq.length()) {
                for (int i = 0; i < length;) {
                    char u1 = Character.toUpperCase(this.charAt(i));
                    char u2 = Character.toUpperCase(csq.charAt(i++));
                    if ((u1 != u2)
                            && (Character.toLowerCase(u1) != Character
                                    .toLowerCase(u2))) {
                        return false;
                    }
                }
                return true;
            } else {
                return false;
            }
        }
    }

    /**
     * Returns the hash code for this text.
     *
     * <p> Note: Returns the same hashCode as <code>java.lang.String</code>
     *           (consistent with {@link #equals})</p>
     *
     * @return the hash code value.
     */
    public final int hashCode() {
        int h = _hashCode;
        if (h == 0) {
            final int length = this.length();
            for (int i = 0; i < length;) {
                h = 31 * h + charAt(i++);
            }
            _hashCode = h;
        }
        return h;
    }

    /**
     * Compares this text to another character sequence lexicographically.
     *
     * @param   csq the character sequence to be compared.
     * @return  <code>{@link #COMPARATOR}.compare(this, that)</code>
     * @throws  ClassCastException if the specifed object is not a
     *          <code>CharSequence</code>.
     */
    public final int compareTo(Object csq) {
        return COMPARATOR.compare(this, csq);
    }

    /**
     * Returns the {@link Text} representation of this text.
     *
     * @return <code>this</code>
     */
    public final Text toText() {
        return this;
    }

    /**
     * Returns the character at the specified index.
     *
     * @param  index the index of the character.
     * @return the character at the specified index.
     * @throws IndexOutOfBoundsException if index is negative, or index 
     *         is equal or greater than <code>this.length()</code>.
     */
    public abstract char charAt(int index);

    /**
     * Returns a portion of this text.
     *
     * @param  start the index of the first character inclusive.
     * @param  end the index of the last character exclusive.
     * @return the sub-text starting at the specified start position and 
     *         ending just before the specified end position.
     * @throws IndexOutOfBoundsException if start or end are negative, start 
     *         is greater than end, or end is greater than 
     *         <code>this.length()</code>.
     */
    public abstract Text subtext(int start, int end);

    /**
     * Returns a copy of this text allocated on the current "stack".
     *
     * @return a local copy of <code>this</code> text.
     */
    public abstract Text copy();

    /**
     * Copies the characters from this text into the destination
     * character array.
     *
     * @param start the index of the first character to copy.
     * @param end the index after the last character to copy.
     * @param dest the destination array.
     * @param destPos the start offset in the destination array.
     * @throws IndexOutOfBoundsException if start or end are negative, start 
     *         is greater than end, or end is greater than 
     *         <code>this.length()</code>.
     */
    public abstract void getChars(int start, int end, char dest[], int destPos);

    /**
     * Concatenates the specified text to the end of this text.
     * 
     * <p> Note: This method is extremely fast (faster even than 
     *     <code>StringBuffer.append(String)</code>) and still returns
     *     a text instance with an internal binary tree of minimal depth!</p>
     *
     * @param  that the character sequence that is concatenated.
     * @return <code>this + that</code>
     */
    public abstract Text plus(Text that);

    /**
     * Returns a text where all occurrences of <code>oldChar</code> have been 
     * replaced with <code>newChar</code>.
     *
     * @param  oldChar the old character.
     * @param  newChar the new character.
     * @return this text if it does not contain any occurence of
     *         the specifed <code>oldChar</code> or a text derived from this
     *         text by replacing every occurrence of <code>oldChar</code>
     *         with <code>newChar</code>.
     */
    public abstract Text replace(char oldChar, char newChar);

    /**
     * Returns the <code>String</code> value  corresponding to this text.
     *
     * @return the <code>java.lang.String</code> for this text.
     */
    public abstract String stringValue();

    /**
     * This class represents a text block (up to 32 characters).
     */
   static final class Primitive extends Text {

        /**
         * Holds the default size for primitive blocks of characters.
         */
        static final int BLOCK_SIZE = 32;

        /**
         * Holds the raw data (primitive).
         */
        final char[] _data = new char[BLOCK_SIZE];

        /**
         * Default constructor.
         */
        private Primitive() {
        }

        /**
         * Returns a new/recycled primitive text.
         * 
         * @return a primitive text. 
         */
        static Primitive newInstance() {
            Primitive text = (Primitive) PRIMITIVE_FACTORY.object();
            text._hashCode = 0;
            text._count = 0;
            return text;
        }

        private static final Factory PRIMITIVE_FACTORY = new Factory() {

            public Object create() {
                return new Primitive();
            }
        };

        // Implements abstract method.
        public char charAt(int index) {
            if (index >= _count) {
                throw new IndexOutOfBoundsException();
            }
            return _data[index];
        }

        // Implements abstract method.
        public Text subtext(int start, int end) {
            final int length = end - start;
            if (length >= 0) {
                Primitive text = Primitive.newInstance();
                text._count = length;
                for (int i = start, j = 0; i < end;) {
                    text._data[j++] = _data[i++];
                }
                return text;
            } else {
                throw new IndexOutOfBoundsException();
            }
        }

        // Implements abstract method.
        public Text copy() {
            Primitive text = Primitive.newInstance();
            text._count = _count;
            for (int i = _count; i > 0;) {
                text._data[--i] = _data[i];
            }
            return text;
        }

        // Implements abstract method.
        public void getChars(int start, int end, char dest[], int destPos) {
            if ((end <= _count) && (end >= start)) {
                for (int i = start, j = destPos; i < end;) {
                    dest[j++] = _data[i++];
                }
            } else {
                throw new IndexOutOfBoundsException();
            }
        }

        // Implements abstract method.
        public Text plus(Text that) {
            final int length = this._count + that._count;
            if (length <= BLOCK_SIZE) {
                Primitive text = Primitive.newInstance();
                text._count = _count;
                for (int i = _count; i > 0;) {
                    text._data[--i] = _data[i];
                }
                for (int i = 0; i < that._count;) {
                    text._data[text._count++] = that.charAt(i++);
                }
                return text;
            } else {
                return Composite.newInstance(this, that);
            }
        }

        // Implements abstract method.
        public Text replace(char oldChar, char newChar) {
            for (int i = 0; i < _count;) {
                if (_data[i++] == oldChar) { // Found at least one occurence.
                    Primitive text = Primitive.newInstance();
                    text._count = _count;
                    for (int j = _count; j > 0;) {
                        char c = _data[--j];
                        text._data[j] = (c == oldChar) ? newChar : c;
                    }
                    return text;
                }
            }
            return this; // No occurrence found.
        }
        
        // Implements abstract method.
        public String stringValue() {
            return new String(_data, 0, _count);
        }
        
    }

    /**
     * This class represents a text composite.
     */
    static final class Composite extends Text {

        /**
         * Holds the head block of character (composite).
         */
        Text _head;

        /**
         * Holds the tail block of character (composite).
         */
        Text _tail;

        /**
         * Default constructor.
         */
        private Composite() {
        }

        /**
         * Returns a new/recycled composite text.
         * 
         * @param head the head of this composite.
         * @param tail the tail of this composite.
         * @return the corresponding composite text. 
         */
        static Composite newInstance(Text head, Text tail) {
            Composite text = (Composite) COMPOSITE_FACTORY.object();
            text._hashCode = 0;
            text._count = head._count + tail._count;
            text._head = head;
            text._tail = tail;
            return text;
        }

        private static final Factory COMPOSITE_FACTORY = new Factory() {

            public Object create() {
                return new Composite();
            }
        };

        // Implements abstract method.
        public char charAt(int index) {
            return (index < _head._count) ? _head.charAt(index) : _tail
                    .charAt(index - _head._count);
        }

        // Implements abstract method.
        public Text subtext(int start, int end) {
            final int cesure = _head._count;
            if (end <= cesure) {
                return _head.subtext(start, end);
            } else if (start >= cesure) {
                return _tail.subtext(start - cesure, end - cesure);
            } else { // Overlaps head and tail.
                return _head.subtext(start, cesure).plus(
                        _tail.subtext(0, end - cesure));
            }
        }

        // Implements abstract method.
        public Text copy() {
            if (_count <= Primitive.BLOCK_SIZE) { // Packs.
                Primitive text = Primitive.newInstance();
                text._count = _count;
                for (int i = _count; i > 0;) {
                    text._data[--i] = this.charAt(i);
                }
                return text;
            } else {
                return Composite.newInstance(_head.copy(), _tail.copy());
            }
        }

        // Implements abstract method.
        public void getChars(int start, int end, char dest[], int destPos) {
            final int cesure = _head._count;
            if (end <= cesure) {
                _head.getChars(start, end, dest, destPos);
            } else if (start >= cesure) {
                _tail.getChars(start - cesure, end - cesure, dest, destPos);
            } else { // Overlaps head and tail.
                _head.getChars(start, cesure, dest, destPos);
                _tail.getChars(0, end - cesure, dest, destPos + cesure - start);
            }
        }

        // Implements abstract method.
        public Text plus(Text that) {
            if (_head._count <= _tail._count) { // this is full
                if ((that._count <= this._count) || (that instanceof Primitive)) {
                    return Composite.newInstance(this, that);
                } else { // that is too big, break it down.
                    Composite compositeThat = (Composite) that;
                    return this.plus(compositeThat._head).plus(
                            compositeThat._tail);
                }
            } else { // Some space on tail.
                if ((that._count + _tail._count <= _head._count)
                        || (that instanceof Primitive)) {
                    // Enough space on tail or no choice.
                    return Composite.newInstance(_head, _tail.plus(that));
                } else { // that is too big, break it down.
                    Composite compositeThat = (Composite) that;
                    return this.plus(compositeThat._head).plus(
                            compositeThat._tail);
                }
            }
        }


        // Implements abstract method.
        public Text replace(char oldChar, char newChar) {
            Text head = _head.replace(oldChar, newChar);
            Text tail = _tail.replace(oldChar, newChar);
            if ((head == _head) && (tail == _tail)) { // No occurence found.
                return this;
            } else {
                return Composite.newInstance(head, tail);
            }
        }

        // Implements abstract method.
        public String stringValue() {
            ObjectPool pool = ArrayPool.charArray(_count);
            char[] data = (char[]) pool.next();
            this.getChars(0, _count, data, 0);
            pool.recycle(data); // Puts back local data buffer.
            return new String(data, 0, _count);
        }

        // Overrides.
        public void move(ContextSpace cs) {
            super.move(cs);
            _head.move(cs);
            _tail.move(cs);
        }
    }

}