/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.text;

import java.io.PrintStream;

import javolution.lang.MathLib;
import javolution.lang.Realtime;
import javolution.lang.ValueType;
import javolution.util.FastMap;
import javolution.util.function.Equalities;
import javolution.xml.XMLSerializable;

/**
 * <p> An immutable character sequence with fast {@link #concat concatenation}, 
 *     {@link #insert insertion} and 
 *     {@link #delete deletion} capabilities (O[Log(n)]) instead of 
 *     O[n] for StringBuffer/StringBuilder).</p>
 * <p> This class has the same methods as 
 *     <a href="http://java.sun.com/j2se/1.5.0/docs/api/java/lang/String.html">
 *     Java String</a> and 
 *     <a href="http://msdn2.microsoft.com/en-us/library/system.string.aspx">
 *     .NET String</a> with the following benefits:<ul>
 *     <li> No need for an intermediate 
 *          {@link StringBuffer}/{@link StringBuilder} in order to manipulate 
 *          textual documents (insertion, deletion or concatenation).</li>
 *     <li> Bug free. They are not plagued by the {@link String#substring} <a
 *          href="http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4513622">
 *          memory leak bug</a> (when small substrings prevent memory from 
 *          larger string from being garbage collected).</li>
 *     <li> More flexible as they allows for search and comparison with any 
 *          <code>java.lang.String</code> or <code>CharSequence</code>.</li>
 *     </ul></p>
 * <p> {@link Text} literals should be explicitly {@link #intern interned}. 
 *     Unlike strings literals and strings-value constant expressions,
 *     interning is not implicit. For example:[code]
 *         final static Text TRUE = new Text("true").intern();
 *         final static Text FALSE = new Text("true").intern("false");
 *     [/code]</p>
 *     
 * <p><i> Implementation Note: To avoid expensive copy operations , 
 *        {@link Text} instances are broken down into smaller immutable 
 *        sequences, they form a minimal-depth binary tree.
 *        The tree is maintained balanced automatically through <a 
 *        href="http://en.wikipedia.org/wiki/Tree_rotation">tree rotations</a>. 
 *        Insertion/deletions are performed in <code>O[Log(n)]</code>
 *        instead of <code>O[n]</code> for 
 *        <code>StringBuffer/StringBuilder</code>.</i></p>
 * 
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @author Wilfried Middleton
 * @version 6.0, July 21, 2013
 */
@Realtime
public final class Text implements CharSequence, Comparable<CharSequence>,
        XMLSerializable, ValueType<Text> {

    private static final long serialVersionUID = 0x600L; // Version.

    /**
     * Holds the default size for primitive blocks of characters.
     */
    private static final int BLOCK_SIZE = 1 << 5;

    /**
     * Holds the mask used to ensure a block boundary cesures.
     */
    private static final int BLOCK_MASK = ~(BLOCK_SIZE - 1);

    /**
     * Holds the texts interned in immortal memory.
     */
    private static final FastMap<Text, Text> INTERN = new FastMap<Text, Text>()
            .shared();

    /**
     * Holds an empty character sequence.
     */
    public static final Text EMPTY = new Text("").intern();

    /**
     * Holds the raw data (primitive) or <code>null</code> (composite).
     */
    private final char[] _data;

    /**
     * Holds the total number of characters.
     */
    private int _count;

    /**
     * Holds the head block of character (composite).
     */
    private Text _head;

    /**
     * Holds the tail block of character (composite).
     */
    private Text _tail;

    /**
     * Creates a new text instance.
     * 
     * @param isPrimitive indicates if primitive or composite.
     */
    private Text(boolean isPrimitive) {
        _data = isPrimitive ? new char[BLOCK_SIZE] : null;
    }

    /**
     * Creates a text holding the characters from the specified <code>String
     * </code>.
     * 
     * @param str the string holding the character content. 
     */
    public Text(String str) {
        this(str.length() <= BLOCK_SIZE);
        _count = str.length();
        if (_data != null) { // Primitive.
            str.getChars(0, _count, _data, 0);
        } else { // Composite, splits on a block boundary. 
            int half = ((_count + BLOCK_SIZE) >> 1) & BLOCK_MASK;
            _head = new Text(str.substring(0, half));
            _tail = new Text(str.substring(half, _count));
        }
    }

    /**
     * Returns the text representing the specified object.
     *
     * @param  obj the object to represent as text.
     * @return {@code new TextBuilder().append(obj).toText()}
     */
    public static Text valueOf(Object obj) {
        return new TextBuilder().append(obj).toText();
    }

    private static Text valueOf(String str) {
        return Text.valueOf(str, 0, str.length());
    }

    private static Text valueOf(String str, int start, int end) {
        int length = end - start;
        if (length <= BLOCK_SIZE) {
            Text text = newPrimitive(length);
            str.getChars(start, end, text._data, 0);
            return text;
        } else { // Splits on a block boundary.
            int half = ((length + BLOCK_SIZE) >> 1) & BLOCK_MASK;
            return newComposite(Text.valueOf(str, start, start + half),
                    Text.valueOf(str, start + half, end));
        }
    }

    /**
     * Returns the text that contains the characters from the specified 
     * array.
     *
     * @param chars the array source of the characters.
     * @return the corresponding instance.
     */
    public static Text valueOf(char[] chars) {
        return Text.valueOf(chars, 0, chars.length);
    }

    /**
     * Returns the text that contains the characters from the specified 
     * subarray of characters.
     *
     * @param chars the source of the characters.
     * @param offset the index of the first character in the data soure.
     * @param length the length of the text returned.
     * @return the corresponding instance.
     * @throws IndexOutOfBoundsException if <code>(offset < 0) || 
     *         (length < 0) || ((offset + length) > chars.length)</code>
     */
    public static Text valueOf(char[] chars, int offset, int length) {
        if ((offset < 0) || (length < 0) || ((offset + length) > chars.length))
            throw new IndexOutOfBoundsException();
        if (length <= BLOCK_SIZE) {
            Text text = Text.newPrimitive(length);
            System.arraycopy(chars, offset, text._data, 0, length);
            return text;
        } else { // Splits on a block boundary.
            int half = ((length + BLOCK_SIZE) >> 1) & BLOCK_MASK;
            return Text.newComposite(Text.valueOf(chars, offset, half),
                    Text.valueOf(chars, offset + half, length - half));
        }
    }

    /**
     * Converts a text builder to a text instance (optimization for 
     * TextBuilder.toText()).
     * 
     * @param  start the index of the first character inclusive.
     * @param  end the index of the last character exclusive.
     * @return the corresponding text instance.
     */
    static Text valueOf(TextBuilder tb, int start, int end) {
        int length = end - start;
        if (length <= BLOCK_SIZE) {
            Text text = Text.newPrimitive(length);
            tb.getChars(start, end, text._data, 0);
            return text;
        } else { // Splits on a block boundary.
            int half = ((length + BLOCK_SIZE) >> 1) & BLOCK_MASK;
            return Text.newComposite(Text.valueOf(tb, start, start + half),
                    Text.valueOf(tb, start + half, end));
        }
    }

    /**
     * Returns the text representation of the <code>boolean</code> argument.
     *
     * @param b a <code>boolean</code>.
     * @return if the argument is <code>true</code>, the text 
     *          <code>"true"</code> is returned; otherwise, the text 
     *          <code>"false"</code> is returned.
     */
    public static Text valueOf(boolean b) {
        return b ? TRUE : FALSE;
    }

    private static final Text TRUE = new Text("true").intern();

    private static final Text FALSE = new Text("false").intern();

    /**
     * Returns the text instance corresponding to the specified character. 
     *
     * @param c a character.
     * @return a text of length <code>1</code> containing <code>'c'</code>.
     */
    public static Text valueOf(char c) {
        Text text = Text.newPrimitive(1);
        text._data[0] = c;
        return text;
    }

    /**
     * Returns the decimal representation of the specified <code>int</code>
     * argument.
     *
     * @param  i the <code>int</code> to format.
     * @return the corresponding text instance.
     */
    public static Text valueOf(int i) {
        TextBuilder tb = new TextBuilder();
        return tb.append(i).toText();
    }

    /**
     * Returns the radix representation of the specified <code>int</code>
     * argument.
     *
     * @param  i the <code>int</code> to format.
     * @param  radix the radix (e.g. <code>16</code> for hexadecimal).
     * @return the corresponding text instance.
     */
    public static Text valueOf(int i, int radix) {
        TextBuilder tb = new TextBuilder();
        return tb.append(i, radix).toText();
    }

    /**
     * Returns the decimal representation of the specified <code>long</code>
     * argument.
     *
     * @param  l the <code>long</code> to format.
     * @return the corresponding text instance.
     */
    public static Text valueOf(long l) {
        TextBuilder tb = new TextBuilder();
        return tb.append(l).toText();
    }

    /**
     * Returns the radix representation of the specified <code>long</code>
     * argument.
     *
     * @param  l the <code>long</code> to format.
     * @param  radix the radix (e.g. <code>16</code> for hexadecimal).
     * @return the corresponding text instance.
     */
    public static Text valueOf(long l, int radix) {
        TextBuilder tb = new TextBuilder();
        return tb.append(l, radix).toText();
    }

    /**
     * Returns the textual representation of the specified <code>float</code>
     * instance.
     *
     * @param  f the <code>float</code> to format.
     * @return the corresponding text instance.
     */
    public static Text valueOf(float f) {
        TextBuilder tb = new TextBuilder();
        return tb.append(f).toText();
    }

    /**
     * Returns the textual representation of the specified <code>double</code>
     * argument.
     *
     * @param  d the <code>double</code> to format.
     * @return the corresponding text instance.
     */
    public static Text valueOf(double d) {
        TextBuilder tb = new TextBuilder();
        return tb.append(d).toText();
    }

    /**
     * Returns the textual representation of the specified <code>double</code>
     * argument formatted as specified.
     *
     * @param  d the <code>double</code> to format.
     * @param  digits the number of significative digits (excludes exponent) or
     *         <code>-1</code> to mimic the standard library (16 or 17 digits).
     * @param  scientific <code>true</code> to forces the use of the scientific 
     *         notation (e.g. <code>1.23E3</code>); <code>false</code> 
     *         otherwise. 
     * @param  showZero <code>true</code> if trailing fractional zeros are 
     *         represented; <code>false</code> otherwise.
     * @return the corresponding text instance.
     * @throws IllegalArgumentException if <code>(digits &gt; 19)</code>)
     */
    public static Text valueOf(double d, int digits, boolean scientific,
            boolean showZero) {
        TextBuilder tb = new TextBuilder();
        return tb.append(d, digits, scientific, showZero).toText();
    }

    /**
     * Returns the length of this text.
     *
     * @return the number of characters (16-bits Unicode) composing this text.
     */
    public int length() {
        return _count;
    }

    /**
     * Returns the concatenation of this text and the textual 
     * representation of the specified object.
     * 
     * @param  obj the object whose textual representation is concatenated.
     * @return <code>this.concat(Text.valueOf(obj))</code>
     */
    public Text plus(Object obj) {
        return this.concat(Text.valueOf(obj));
    }

    /**
     * Returns the concatenation of this text and the specified 
     * <code>String</code> (optimization).
     * 
     * @param  str the string whose characters are concatenated.
     * @return <code>this.concat(Text.valueOf(obj))</code>
     */
    public Text plus(String str) {

        Text merge = this.append(str);
        return merge != null ? merge : concat(Text.valueOf(str));
    }

    private Text append(String str) { // Try to append, returns null if cannot.
        int length = str.length();
        if (_data == null) {
            Text merge = _tail.append(str);
            return merge != null ? Text.newComposite(_head, merge) : null;
        } else { // Primitive.
            if (_count + length > BLOCK_SIZE)
                return null; // Cannot merge.
            Text text = Text.newPrimitive(_count + length);
            System.arraycopy(_data, 0, text._data, 0, _count);
            str.getChars(0, length, text._data, _count);
            return text;
        }
    }

    /**
     * Concatenates the specified text to the end of this text. 
     * This method is very fast (faster even than 
     * <code>StringBuffer.append(String)</code>) and still returns
     * a text instance with an internal binary tree of minimal depth!
     *
     * @param  that the text that is concatenated.
     * @return <code>this + that</code>
     */
    public Text concat(Text that) {
        // All Text instances are maintained balanced:
        //   (head < tail * 2) & (tail < head * 2)

        final int length = this._count + that._count;
        if (length <= BLOCK_SIZE) { // Merges to primitive.
            Text text = Text.newPrimitive(length);
            this.getChars(0, this._count, text._data, 0);
            that.getChars(0, that._count, text._data, this._count);
            return text;

        } else { // Returns a composite.
            Text head = this;
            Text tail = that;

            if (((head._count << 1) < tail._count) && (tail._data == null)) { // tail is composite
                // head too small, returns (head + tail/2) + (tail/2) 
                if (tail._head._count > tail._tail._count) {
                    // Rotates to concatenate with smaller part.
                    tail = tail.rightRotation();
                }
                head = head.concat(tail._head);
                tail = tail._tail;

            } else if (((tail._count << 1) < head._count)
                    && (head._data == null)) { // head is composite.
                // tail too small, returns (head/2) + (head/2 concat tail)
                if (head._tail._count > head._head._count) {
                    // Rotates to concatenate with smaller part.
                    head = head.leftRotation();
                }
                tail = head._tail.concat(tail);
                head = head._head;
            }
            return Text.newComposite(head, tail);
        }
    }

    private Text rightRotation() {
        // See: http://en.wikipedia.org/wiki/Tree_rotation
        Text P = this._head;
        if (P._data != null)
            return this; // Head not a composite, cannot rotate.
        Text A = P._head;
        Text B = P._tail;
        Text C = this._tail;
        return Text.newComposite(A, Text.newComposite(B, C));
    }

    private Text leftRotation() {
        // See: http://en.wikipedia.org/wiki/Tree_rotation
        Text Q = this._tail;
        if (Q._data != null)
            return this; // Tail not a composite, cannot rotate.
        Text B = Q._head;
        Text C = Q._tail;
        Text A = this._head;
        return Text.newComposite(Text.newComposite(A, B), C);
    }

    /**
     * Returns a portion of this text.
     * 
     * @param  start the index of the first character inclusive.
     * @return the sub-text starting at the specified position.
     * @throws IndexOutOfBoundsException if <code>(start < 0) || 
     *          (start > this.length())</code>
     */
    public Text subtext(int start) {
        return subtext(start, length());
    }

    /**
     * Returns the text having the specified text inserted at 
     * the specified location.
     *
     * @param index the insertion position.
     * @param txt the text being inserted.
     * @return <code>subtext(0, index).concat(txt).concat(subtext(index))</code>
     * @throws IndexOutOfBoundsException if <code>(index < 0) ||
     *            (index > this.length())</code>
     */
    public Text insert(int index, Text txt) {
        return subtext(0, index).concat(txt).concat(subtext(index));
    }

    /**
     * Returns the text without the characters between the specified indexes.
     *
     * @param start the beginning index, inclusive.
     * @param end the ending index, exclusive.
     * @return <code>subtext(0, start).concat(subtext(end))</code>
     * @throws IndexOutOfBoundsException if <code>(start < 0) || (end < 0) ||
     *         (start > end) || (end > this.length()</code>
     */
    public Text delete(int start, int end) {
        if (start > end)
            throw new IndexOutOfBoundsException();
        return subtext(0, start).concat(subtext(end));
    }

    /**
     * Replaces each character sequence of this text that matches the specified 
     * target sequence with the specified replacement sequence.
     *
     * @param target the character sequence to be replaced.
     * @param replacement the replacement sequence.
     * @return the resulting text.
     */
    public Text replace(java.lang.CharSequence target,
            java.lang.CharSequence replacement) {
        int i = indexOf(target);
        return (i < 0) ? this : // No target sequence found.
                subtext(0, i).concat(Text.valueOf(replacement)).concat(
                        subtext(i + target.length()).replace(target,
                                replacement));
    }

    /**
     * Replaces the specified characters in this text with the specified 
     * replacement sequence.
     *
     * @param charSet the set of characters to be replaced.
     * @param replacement the replacement sequence.
     * @return the resulting text.
     */
    public Text replace(CharSet charSet, java.lang.CharSequence replacement) {
        int i = indexOfAny(charSet);
        return (i < 0) ? this : // No character to replace.
                subtext(0, i).concat(Text.valueOf(replacement)).concat(
                        subtext(i + 1).replace(charSet, replacement));
    }

    /**
     * Returns {@link #subtext(int, int) subtext(start, end)}.
     *
     * @param  start the index of the first character inclusive.
     * @param  end the index of the last character exclusive.
     * @return <code>this.subtext(start, end)</code>
     * @throws IndexOutOfBoundsException if <code>(start < 0) || (end < 0) ||
     *         (start > end) || (end > this.length())</code>
     */
    public java.lang.CharSequence subSequence(int start, int end) {
        return subtext(start, end);
    }

    /**
     * Returns the index within this text of the first occurrence
     * of the specified character sequence searching forward.
     *
     * @param  csq a character sequence.
     * @return the index of the first character of the character sequence found;
     *         or <code>-1</code> if the character sequence is not found.
     */
    public int indexOf(java.lang.CharSequence csq) {
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
    public int indexOf(java.lang.CharSequence csq, int fromIndex) {

        // Limit cases.
        final int csqLength = csq.length();
        final int min = Math.max(0, fromIndex);
        final int max = _count - csqLength;
        if (csqLength == 0) { return (min > max) ? -1 : min; }

        // Searches for csq.
        final char c = csq.charAt(0);
        for (int i = indexOf(c, min); (i >= 0) && (i <= max); i = indexOf(c,
                ++i)) {
            boolean match = true;
            for (int j = 1; j < csqLength; j++) {
                if (this.charAt(i + j) != csq.charAt(j)) {
                    match = false;
                    break;
                }
            }
            if (match) { return i; }
        }
        return -1;
    }

    /**
     * Returns the index within this text of the last occurrence of
     * the specified characters sequence searching backward.
     *
     * @param  csq a character sequence.
     * @return the index of the first character of the character sequence found;
     *         or <code>-1</code> if the character sequence is not found.
     */
    public int lastIndexOf(java.lang.CharSequence csq) {
        return lastIndexOf(csq, _count);
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
    public int lastIndexOf(java.lang.CharSequence csq, int fromIndex) {

        // Limit cases.
        final int csqLength = csq.length();
        final int min = 0;
        final int max = Math.min(fromIndex, _count - csqLength);
        if (csqLength == 0) { return (min > max) ? -1 : max; }

        // Searches for csq.
        final char c = csq.charAt(0);
        for (int i = lastIndexOf(c, max); (i >= 0); i = lastIndexOf(c, --i)) {
            boolean match = true;
            for (int j = 1; j < csqLength; j++) {
                if (this.charAt(i + j) != csq.charAt(j)) {
                    match = false;
                    break;
                }
            }
            if (match) { return i; }
        }
        return -1;

    }

    /**
     * Indicates if this text starts with the specified prefix.
     *
     * @param  prefix the prefix.
     * @return <code>true</code> if the character sequence represented by the
     *         argument is a prefix of the character sequence represented by
     *         this text; <code>false</code> otherwise.
     */
    public boolean startsWith(java.lang.CharSequence prefix) {
        return startsWith(prefix, 0);
    }

    /**
     * Indicates if this text ends with the specified suffix.
     *
     * @param  suffix the suffix.
     * @return <code>true</code> if the character sequence represented by the
     *         argument is a suffix of the character sequence represented by
     *         this text; <code>false</code> otherwise.
     */
    public boolean endsWith(java.lang.CharSequence suffix) {
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
    public boolean startsWith(java.lang.CharSequence prefix, int index) {
        final int prefixLength = prefix.length();
        if ((index >= 0) && (index <= (this.length() - prefixLength))) {
            for (int i = 0, j = index; i < prefixLength;) {
                if (prefix.charAt(i++) != this.charAt(j++)) { return false; }
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns a copy of this text, with leading and trailing
     * whitespace omitted.
     *
     * @return a copy of this text with leading and trailing white
     *          space removed, or this text if it has no leading or
     *          trailing white space.
     */
    public Text trim() {
        int first = 0; // First character index.
        int last = length() - 1; // Last character index.
        while ((first <= last) && (charAt(first) <= ' ')) {
            first++;
        }
        while ((last >= first) && (charAt(last) <= ' ')) {
            last--;
        }
        return subtext(first, last + 1);
    }

    /**
     * Returns a text equals to this one from a pool of
     * unique text instances.  
     * 
     * @return an unique text instance allocated in permanent memory.
     */
    public Text intern() {
        Text txt = INTERN.putIfAbsent(this, this);
        return txt == null ? this : txt;
    }

    /**
     * Indicates if this text has the same character content as the specified
     * character sequence.
     *
     * @param csq the character sequence to compare with.
     * @return <code>true</code> if the specified character sequence has the 
     *        same character content as this text; <code>false</code> otherwise.
     */
    public boolean contentEquals(java.lang.CharSequence csq) {
        if (csq.length() != _count)
            return false;
        for (int i = 0; i < _count;) {
            if (this.charAt(i) != csq.charAt(i++))
                return false;
        }
        return true;
    }

    /**
     * Indicates if this text has the same character contend as the specified
     * character sequence ignoring case considerations. 
     *
     * @param  csq the <code>CharSequence</code> to compare this text against.
     * @return <code>true</code> if the argument and this text are equal, 
     *         ignoring case; <code>false</code> otherwise.
     */
    public boolean contentEqualsIgnoreCase(java.lang.CharSequence csq) {
        if (this._count != csq.length())
            return false;
        for (int i = 0; i < _count;) {
            char u1 = this.charAt(i);
            char u2 = csq.charAt(i++);
            if (u1 != u2) {
                u1 = Character.toUpperCase(u1);
                u2 = Character.toUpperCase(u2);
                if ((u1 != u2)
                        && (Character.toLowerCase(u1) != Character
                                .toLowerCase(u2)))
                    return false;

            }
        }
        return true;
    }

    /**
     * Compares this text against the specified object for equality.
     * Returns <code>true</code> if the specified object is a text having
     * the same character sequence as this text. 
     * For generic comparaison with any character sequence the 
     * {@link #contentEquals(CharSequence)} should be used.
     * 
     * @param  obj the object to compare with or <code>null</code>.
     * @return <code>true</code> if that is a text with the same character
     *         sequence as this text; <code>false</code> otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof Text))
            return false;
        final Text that = (Text) obj;
        if (this._count != that._count)
            return false;
        for (int i = 0; i < _count;) {
            if (this.charAt(i) != that.charAt(i++))
                return false;
        }
        return true;
    }

    /**
     * Returns the hash code for this text.
     *
     * @return the hash code value.
     */
    @Override
    public int hashCode() {
        int h = 0;
        final int length = this.length();
        for (int i = 0; i < length;) {
            h = 31 * h + charAt(i++);
        }
        return h;
    }

    /**
     * Compares this text to another character sequence or string 
     * lexicographically.
     *
     * @param   csq the character sequence to be compared.
     * @return  <code>TypeFormat.LEXICAL_COMPARATOR.compare(this, csq)</code>
     * @throws  ClassCastException if the specifed object is not a
     *          <code>CharSequence</code> or a <code>String</code>.
     */
    public int compareTo(CharSequence csq) {
        return Equalities.LEXICAL.compare(this, csq);
    }

    /**
     * Returns <code>this</code> (implements 
     * {@link javolution.lang.ValueType Realtime} interface).
     *
     * @return <code>this</code>
     */
    public Text toText() {
        return this;
    }

    /**
     * Prints the current statistics on this text tree structure.
     *  
     * @param out the stream to use for output (e.g. <code>System.out</code>)
     */
    public void printStatistics(PrintStream out) {
        int length = this.length();
        int leaves = getNbrOfLeaves();
        synchronized (out) {
            out.print("LENGTH: " + length());
            out.print(", MAX DEPTH: " + getDepth());
            out.print(", NBR OF BRANCHES: " + getNbrOfBranches());
            out.print(", NBR OF LEAVES: " + leaves);
            out.print(", AVG LEAVE LENGTH: " + (length + (leaves >> 1))
                    / leaves);
            out.println();
        }
    }

    private int getDepth() {
        if (_data != null) // Primitive.
            return 0;
        return MathLib.max(_head.getDepth(), _tail.getDepth()) + 1;
    }

    private int getNbrOfBranches() {
        return (_data == null) ? _head.getNbrOfBranches()
                + _tail.getNbrOfBranches() + 1 : 0;
    }

    private int getNbrOfLeaves() {
        return (_data == null) ? _head.getNbrOfLeaves()
                + _tail.getNbrOfLeaves() : 1;
    }

    /**
     * Converts the characters of this text to lower case.
     * 
     * @return the text in lower case.
     * @see Character#toLowerCase(char) 
     */
    public Text toLowerCase() {
        if (_data == null) // Composite.
            return Text.newComposite(_head.toLowerCase(), _tail.toLowerCase());
        Text text = Text.newPrimitive(_count);
        for (int i = 0; i < _count;) {
            text._data[i] = Character.toLowerCase(_data[i++]);
        }
        return text;
    }

    /**
     * Converts the characters of this text to upper case.
     * 
     * @return the text in lower case.
     * @see Character#toUpperCase(char) 
     */
    public Text toUpperCase() {
        if (_data == null) // Composite.
            return newComposite(_head.toUpperCase(), _tail.toUpperCase());
        Text text = Text.newPrimitive(_count);
        for (int i = 0; i < _count;) {
            text._data[i] = Character.toUpperCase(_data[i++]);
        }
        return text;
    }

    /**
     * Returns the character at the specified index.
     *
     * @param  index the index of the character.
     * @return the character at the specified index.
     * @throws IndexOutOfBoundsException if <code>(index < 0) || 
     *         (index >= this.length())</code>
     */
    public char charAt(int index) {
        if (index >= _count)
            throw new IndexOutOfBoundsException();
        return (_data != null) ? _data[index] : (index < _head._count) ? _head
                .charAt(index) : _tail.charAt(index - _head._count);
    }

    /**
     * Returns the index within this text of the first occurrence of the
     * specified character, starting the search at the beginning.
     *
     * @param c the character to search for.
     * @return the index of the first occurrence of the character in this text
     *         that is greater than or equal to <code>0</code>,
     *         or <code>-1</code> if the character does not occur.
     */
    public int indexOf(char c) {
        return indexOf(c, 0);
    }

    /**
     * Returns the index within this text of the first occurrence of the
     * specified character, starting the search at the specified index.
     *
     * @param c the character to search for.
     * @param fromIndex the index to start the search from.
     * @return the index of the first occurrence of the character in this text
     *         that is greater than or equal to <code>fromIndex</code>, 
     *         or <code>-1</code> if the character does not occur.
     */
    public int indexOf(char c, int fromIndex) {
        if (_data != null) { // Primitive.
            for (int i = MathLib.max(fromIndex, 0); i < _count; i++) {
                if (_data[i] == c)
                    return i;
            }
            return -1;
        } else { // Composite.
            final int cesure = _head._count;
            if (fromIndex < cesure) {
                final int headIndex = _head.indexOf(c, fromIndex);
                if (headIndex >= 0)
                    return headIndex; // Found in head.
            }
            final int tailIndex = _tail.indexOf(c, fromIndex - cesure);
            return (tailIndex >= 0) ? tailIndex + cesure : -1;
        }
    }

    /**
     * Returns the index within this text of the first occurrence of the
     * specified character, searching backward and starting at the specified
     * index.
     *
     * @param c the character to search for.
     * @param fromIndex the index to start the search backward from.
     * @return the index of the first occurrence of the character in this text
     *         that is less than or equal to <code>fromIndex</code>, 
     *         or <code>-1</code> if the character does not occur.
     */
    public int lastIndexOf(char c, int fromIndex) {
        if (_data != null) { // Primitive.
            for (int i = MathLib.min(fromIndex, _count - 1); i >= 0; i--) {
                if (_data[i] == c)
                    return i;
            }
            return -1;
        } else { // Composite.
            final int cesure = _head._count;
            if (fromIndex >= cesure) {
                final int tailIndex = _tail.lastIndexOf(c, fromIndex - cesure);
                if (tailIndex >= 0)
                    return tailIndex + cesure; // Found in tail.
            }
            return _head.lastIndexOf(c, fromIndex);
        }
    }

    /**
     * Returns a portion of this text.
     *
     * @param  start the index of the first character inclusive.
     * @param  end the index of the last character exclusive.
     * @return the sub-text starting at the specified start position and 
     *         ending just before the specified end position.
     * @throws IndexOutOfBoundsException if <code>(start < 0) || (end < 0) ||
     *         (start > end) || (end > this.length())</code>
     */
    public Text subtext(int start, int end) {
        if (_data != null) { // Primitive.
            if ((start < 0) || (start > end) || (end > _count))
                throw new IndexOutOfBoundsException();
            if ((start == 0) && (end == _count))
                return this;
            if (start == end)
                return Text.EMPTY;
            int length = end - start;
            Text text = Text.newPrimitive(length);
            System.arraycopy(_data, start, text._data, 0, length);
            return text;
        } else { // Composite.
            final int cesure = _head._count;
            if (end <= cesure)
                return _head.subtext(start, end);
            if (start >= cesure)
                return _tail.subtext(start - cesure, end - cesure);
            if ((start == 0) && (end == _count))
                return this;
            // Overlaps head and tail.
            return _head.subtext(start, cesure).concat(
                    _tail.subtext(0, end - cesure));
        }
    }

    /**
     * Copies the characters from this text into the destination
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
        if (_data != null) { // Primitive.
            if ((start < 0) || (end > _count) || (start > end))
                throw new IndexOutOfBoundsException();
            System.arraycopy(_data, start, dest, destPos, end - start);
        } else { // Composite.
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
    }

    /**
     * Returns the <code>String</code> representation of this text.
     *
     * @return the <code>java.lang.String</code> for this text.
     */
    public String toString() {
        if (_data != null) { // Primitive.
            return new String(_data, 0, _count);
        } else { // Composite.
            char[] data = new char[_count];
            this.getChars(0, _count, data, 0);
            return new String(data, 0, _count);
        }
    }

    // Implements ValueType interface.
    public Text copy() {
        if (_data != null) { // Primitive.
            Text text = Text.newPrimitive(_count);
            System.arraycopy(_data, 0, text._data, 0, _count);
            return text;
        } else { // Composite.
            return Text.newComposite((Text) _head.copy(), (Text) _tail.copy());
        }
    }

    //////////////////////////////////////////////////////////////////
    // Wilfried add-ons (methods provided by Microsoft .Net in C#)
    //
    /**
     * Returns the text that contains a specific length sequence of the
     * character specified.
     *
     * @param c the character to fill this text with.
     * @param length the length of the text returned.
     * @return the corresponding instance.
     * @throws IndexOutOfBoundsException if <code>(length < 0)</code>
     */
    public static Text valueOf(char c, int length) {
        if (length < 0)
            throw new IndexOutOfBoundsException();
        if (length <= BLOCK_SIZE) {
            Text text = Text.newPrimitive(length);
            for (int i = 0; i < length;) {
                text._data[i++] = c;
            }
            return text;
        } else {
            final int middle = (length >> 1);
            return Text.newComposite(Text.valueOf(c, middle),
                    Text.valueOf(c, length - middle));
        }
    }

    /**
     * Indicates if all characters of this text are whitespaces
     * (no characters greater than the space character).
     *
     *@return <code>true</code> if this text  contains only whitespace.
     */
    public boolean isBlank() {
        return isBlank(0, length());
    }

    /**
     * Indicates if the specified sub-range of characters of this text
     * are whitespaces (no characters greater than the space character).
     *
     *@param start the start index.
     *@param length the number of characters to inspect.
     */
    public boolean isBlank(int start, int length) {
        for (; start < length; start++) {
            if (charAt(start) > ' ')
                return false;
        }
        return true;
    }

    /**
     * Returns a copy of this text, with leading whitespace omitted.
     *
     * @return a copy of this text with leading white space removed,
     * or this text if it has no leading white space.
     */
    public Text trimStart() {
        int first = 0; // First character index.
        int last = length() - 1; // Last character index.
        while ((first <= last) && (charAt(first) <= ' ')) {
            first++;
        }
        return subtext(first, last + 1);
    }

    /**
     * Returns a copy of this text, with trailing
     * whitespace omitted.
     *
     * @return a copy of this text with trailing white space removed,
     * or this text if it has no trailing white space.
     */
    public Text trimEnd() {
        int first = 0; // First character index.
        int last = length() - 1; // Last character index.
        while ((last >= first) && (charAt(last) <= ' ')) {
            last--;
        }
        return subtext(first, last + 1);
    }

    /**
     * Pads this text on the left with spaces to make the minimum total length
     * as specified.
     * The new length of the new text is equal to the original length plus
     * <code>(length()-len)</code> spaces.
     *
     * @param len the total number of characters to make this text equal to.
     * @return a new text or the same text if no padding required.
     * @throws an IllegalArgumentException if the <code>(len<0)</code>.
     */
    public Text padLeft(int len) {
        return padLeft(len, ' ');
    }

    /**
     * Pads this text on the left to make the minimum total length as specified.
     * Spaces or the given Unicode character are used to pad with.
     * <br>
     * The new length of the new text is equal to the original length plus
     * <code>(length()-len)</code> pad characters.
     *
     * @param len the total number of characters to make this text equal to.
     * @param c the character to pad using.
     * @return a new text or the same text if no padding required.
     * @throws an IllegalArgumentException if the <code>(len<0)</code>.
     */
    public Text padLeft(int len, char c) {
        final int padSize = (len <= length()) ? 0 : len - length();
        return insert(0, Text.valueOf(c, padSize));
    }

    /**
     * Pads this text on the right with spaces to make the minimum total length
     * as specified.
     * The new length of the new text is equal to the original length plus
     * <code>(length()-len)</code> spaces.
     *
     * @param len the total number of characters to make this text equal to.
     * @return a new text or the same text if no padding required.
     * @throws an IllegalArgumentException if the <code>(len<0)</code>.
     */
    public Text padRight(int len) {
        return padRight(len, ' ');
    }

    /**
     * Pads this text on the right to make the minimum total length as specified.
     * Spaces or the given Unicode character are used to pad with.
     * <br>
     * The new length of the new text is equal to the original length plus
     * <code>(length()-len)</code> pad characters.
     *
     * @param len the total number of characters to make this text equal to.
     * @param c the character to pad using.
     * @return a new text or the same text if no padding required.
     * @throws an IllegalArgumentException if the <code>(len<0)</code>.
     */
    public Text padRight(int len, char c) {
        final int padSize = (len <= length()) ? 0 : len - length();
        return concat(Text.valueOf(c, padSize));
    }

    /**
     * Returns the index within this text of the first occurrence
     * of any character in the specified character set.
     *
     * @param  charSet the character set.
     * @return the index of the first character that matches one of the
     *         characters in the supplied set; or <code>-1</code> if none.
     */
    public int indexOfAny(CharSet charSet) {
        return indexOfAny(charSet, 0, length());
    }

    /**
     * Returns the index within a region of this text of the first occurrence
     * of any character in the specified character set.
     *
     * @param charSet the character set.
     * @param start the index of the start of the search region in this text.
     * @return the index of the first character that matches one of the
     *         characters in the supplied set; or <code>-1</code> if none.
     */
    public int indexOfAny(CharSet charSet, int start) {
        return indexOfAny(charSet, start, length() - start);
    }

    /**
     * Returns the index within a region of this text of the first occurrence
     * of any character in the specified character set.
     *
     * @param charSet the character set.
     * @param start the index of the start of the search region in this text.
     * @param length the length of the region to search.
     * @return the index of the first character that matches one of the
     *         characters in the supplied array; or <code>-1</code> if none.
     */
    public int indexOfAny(CharSet charSet, int start, int length) {
        final int stop = start + length;
        for (int i = start; i < stop; i++) {
            if (charSet.contains(charAt(i)))
                return i;
        }
        return -1;
    }

    /**
     * Returns the index within this text of the last occurrence
     * of any character in the specified character set.
     *
     * @param charSet the character set.
     * @return the index of the last character that matches one of the
     *         characters in the supplied array; or <code>-1</code> if none.
     */
    public int lastIndexOfAny(CharSet charSet) {
        return lastIndexOfAny(charSet, 0, length());
    }

    /**
     * Returns the index within a region of this text of the last occurrence
     * of any character in the specified character set.
     *
     * @param charSet the character set.
     * @param start the index of the start of the search region in this text.
     * @return the index of the last character that matches one of the
     *         characters in the supplied array; or <code>-1</code> if none.
     */
    public int lastIndexOfAny(CharSet charSet, int start) {
        return lastIndexOfAny(charSet, start, length() - start);
    }

    /**
     * Returns the index within a region of this text of the last occurrence
     * of any character in the specified character set.
     *
     * @param charSet the character set.
     * @param start the index of the start of the search region in this text.
     * @param length the length of the region to search.
     * @return the index of the last character that matches one of the
     *         characters in the supplied array; or <code>-1</code> if none.
     */
    public int lastIndexOfAny(CharSet charSet, int start, int length) {
        for (int i = start + length; --i >= start;) {
            if (charSet.contains(charAt(i)))
                return i;
        }
        return -1;
    }

    //
    ////////////////////////////////////////////////////////////////////////////
    /**
     * Returns a {@link javolution.context.AllocatorContext context allocated}
     * primitive text instance.
     *
     * @param length the primitive length.
     */
    private static Text newPrimitive(int length) {
        Text text = new Text(true);
        text._count = length;
        return text;
    }

    /**
     * Returns a {@link javolution.context.AllocatorContext context allocated}
     * composite text instance.
     *
     * @param head the composite head.
     * @param tail the composite tail.
     */
    private static Text newComposite(Text head, Text tail) {
        Text text = new Text(false);
        text._count = head._count + tail._count;
        text._head = head;
        text._tail = tail;
        return text;
    }

    @Override
    public Text value() {
        return this;
    }

}