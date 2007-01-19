/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2005 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.text;

import j2me.io.Serializable;
import j2me.lang.CharSequence;
import j2me.lang.Comparable;
import j2mex.realtime.MemoryArea;

import javolution.Javolution;
import javolution.context.Realtime;
import javolution.context.RealtimeObject;
import javolution.lang.Immutable;
import javolution.util.FastComparator;
import javolution.util.FastMap;
import javolution.xml.XMLFormat;
import javolution.xml.stream.XMLStreamException;

/**
 * <p> This class represents an immutable character sequence with extremely
 *     fast {@link #concat concatenation}, {@link #insert insertion} and 
 *     {@link #delete deletion} capabilities (O[Log(n)]) instead of 
 *     O[n] for StringBuffer/StringBuilder).</p>
 * <p> This class has the same methods as 
 *     <a href="http://java.sun.com/j2se/1.5.0/docs/api/java/lang/String.html">
 *     Java String</a> and 
 *     <a href="http://msdn2.microsoft.com/en-us/library/system.string.aspx">
 *     .NET String</a> with the following benefits:<ul>
 *     <li> No need for an intermediate 
 *          {@link StringBuffer}/{@link StringBuilder} in order to manipulate 
 *          textual documents (insertion, deletion, concatenation). {@link Text}
 *          methods are also much faster for large documents.</li>
 *     <li> Bug free. They are not plagued by the {@link String#substring} <a
 *          href="http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4513622">
 *          memory leak bug</a> (when small substrings prevent memory from 
 *          larger string from being garbage collected).</li>
 *     <li> More flexible as they allows for search and comparison with any 
 *          <code>java.lang.String</code> or <code>CharSequence</code>.</li>
 *     <li> Easy {@link TextFormat formatting} using the {@link TextBuilder}
 *          class (no need to specify the buffer capacity as
 *          it gently increases without incurring expensive resize/copy 
 *          operations).</li>
 *     <li> Real-time compliant (instances allocated on the "stack" when 
 *          executing in a {@link javolution.context.PoolContext PoolContext}).</li>
 *     </ul></p>
 * <p> As for any <code>CharSequence</code>, parsing of primitive types can
 *     be achieved using the {@link javolution.text.TypeFormat} utility class.</p>
 * <p> {@link Text} literals should be explicitely {@link #intern interned}. 
 *     Unlike strings literals and strings-value constant expressions,
 *     interning is not implicit. For example:[code]
 *         final static Text TRUE = Text.intern("true");
 *         final static Text FALSE = Text.intern("false");
 *     [/code]
 *     Interned texts are always allocated in ImmortalMemory (RTSJ VMs).</p>
 * <p><i> Implementation Note: To avoid expensive copy operations , 
 *        {@link Text} instances are broken down into smaller immutable 
 *        sequences (they form a minimal-depth binary tree). 
 *        Internal copies are then performed in <code>O[Log(n)]</code>
 *        instead of <code>O[n]</code>).</i></p>
 * 
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @author Wilfried Middleton
 * @version 4.2, January 11, 2007
 */
public abstract class Text extends RealtimeObject implements CharSequence,
        Comparable, Serializable, Immutable {

    /**
     * Holds the texts interned in ImmortalMemory
     */
    private static final FastMap INTERN_INSTANCES = new FastMap()
            .setKeyComparator(FastComparator.LEXICAL);

    private static String _tmpString;

    private static final Runnable INTERN_STRING = new Runnable() {
        public void run() {
            StringWrapper txt = new StringWrapper();
            txt._count = _tmpString.length();
            txt._string = _tmpString;
            INTERN_INSTANCES.put(_tmpString, txt);
        }
    };

    /**
     * Holds an empty character sequence.
     */
    public static final Text EMPTY = Text.intern("");

    /**
     * Holds the <code>"null"</code> character sequence.
     */
    public static final Text NULL = Text.intern("null");

    /**
     * Holds the default XML representation for Text instances. 
     * This representation consists of a <code>"value"</code> attribute 
     * holding the characters.
     */
    protected static final XMLFormat/*<Text>*/XML = new XMLFormat(Javolution
            .j2meGetClass("javolution.text.Text")) {

        public Object newInstance(Class cls,
                javolution.xml.XMLFormat.InputElement xml)
                throws XMLStreamException {
            CharSequence csq = xml.getAttribute("value");
            return csq != null ? Text.valueOf(csq) : Text.EMPTY;
        }

        public void read(InputElement xml, Object obj)
                throws XMLStreamException {
            // Do nothing.
        }

        public void write(Object obj, OutputElement xml)
                throws XMLStreamException {
            xml.setAttribute("value", (Text) obj);
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
     * Returns the text representing the specified object.
     * If the specified object is <code>null</code> this method 
     * returns {@link #NULL}. 
     *
     * @param  obj the object to represent as text.
     * @return the textual representation of the specified object.
     * @see    Realtime#toText
     */
    public static Text valueOf(Object obj) {
        if (obj instanceof Realtime) {
            return ((Realtime) obj).toText();
        } else if (obj instanceof String) {
            return StringWrapper.newInstance((String) obj);
        } else if (obj instanceof CharSequence) {
            final CharSequence csq = (CharSequence) obj;
            return Text.valueOf(csq, 0, csq.length());
        } else if (obj != null) {
            return StringWrapper.newInstance(obj.toString());
        } else {
            return NULL;
        }
    }

    /**
     * Returns the text that contains the specified subsequence of characters.
     *   
     * @param  csq the character sequence source.
     * @param  start the index of the first character.
     * @param  end the index after the last character.
     * @return the corresponding text instance.
     * @throws IndexOutOfBoundsException if <code>(start < 0) || (end < 0) 
     *         || (start > end) || (end > csq.length())</code>
     */
    public static Text valueOf(CharSequence csq, int start, int end) {
        if ((start < 0) || (end < 0) || (start > end) || (end > csq.length()))
            throw new IndexOutOfBoundsException();
        if (csq instanceof TextBuilder)
            return valueOf((TextBuilder) csq, start, end);
        final int length = end - start;
        if (length <= Primitive.BLOCK_SIZE) {
            Primitive text = Primitive.newInstance(length);
            for (int i = 0; i < length;) {
                text._data[i] = csq.charAt(start + i++);
            }
            return text;
        } else {
            final int middle = start + (length >> 1);
            Composite text = Composite.newInstance(Text.valueOf(csq, start,
                    middle), Text.valueOf(csq, middle, end));
            return text;
        }
    }

    // Optimized version for TextBuilder.
    static Text valueOf(TextBuilder tb, int start, int end) {
        final int length = end - start;
        if (length <= Primitive.BLOCK_SIZE) {
            Primitive text = Primitive.newInstance(length);
            if ((start & TextBuilder.M0) + length <= TextBuilder.C0) {
                // Single block copy.
                char[] chars = tb.charsAt(start);
                System.arraycopy(chars, start & TextBuilder.M0, text._data, 0,
                        length);
            } else { // Crossing of block boundary.
                for (int i = 0; i < length;) {
                    text._data[i] = tb.charAt(start + i++);
                }
            }
            return text;
        } else { // Cut on the middle on a block boundary if possible.
            int middle = (start + (length >> 1));
            if ((middle & ~TextBuilder.M0) > start) {
                middle &= ~TextBuilder.M0;
            }
            Composite text = Composite.newInstance(Text.valueOf(tb, start,
                    middle), Text.valueOf(tb, middle, end));
            return text;
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
        return valueOf(chars, 0, chars.length);
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
        if (length <= Primitive.BLOCK_SIZE) {
            Primitive text = Primitive.newInstance(length);
            for (int i = 0; i < length;) {
                text._data[i] = chars[offset + i++];
            }
            return text;
        } else {
            final int middle = offset + (length >> 1);
            Composite text = Composite.newInstance(Text.valueOf(chars, offset,
                    middle - offset), Text.valueOf(chars, middle, offset
                    + length - middle));
            return text;
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

    private static final Text TRUE = Text.intern("true");

    private static final Text FALSE = Text.intern("false");

    /**
     * Returns the {@link #intern unique} text instance corresponding to the 
     * specified character. 
     *
     * @param c a character.
     * @return a text of length <code>1</code> containing <code>'c'</code>.
     */
    public static Text valueOf(char c) {
        if ((c < 128) && (ASCII[c] != null))
            return ASCII[c];
        Primitive text = Primitive.newInstance(1);
        text._data[0] = c;
        Text textIntern = Text.intern(text);
        if (c < 128) {
            ASCII[c] = textIntern;
        }
        return textIntern;
    }

    private static final Text[] ASCII = new Text[128];

    /**
     * Returns the decimal representation of the specified <code>int</code>
     * argument.
     *
     * @param  i the <code>int</code> to format.
     * @return the corresponding text instance.
     */
    public static Text valueOf(int i) {
        TextBuilder tmp = TextBuilder.newInstance();
        tmp.append(i);
        Text txt = tmp.toText();
        TextBuilder.recycle(tmp);
        return txt;
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
        TextBuilder tmp = TextBuilder.newInstance();
        tmp.append(i, radix);
        Text txt = tmp.toText();
        TextBuilder.recycle(tmp);
        return txt;
    }

    /**
     * Returns the decimal representation of the specified <code>long</code>
     * argument.
     *
     * @param  l the <code>long</code> to format.
     * @return the corresponding text instance.
     */
    public static Text valueOf(long l) {
        TextBuilder tmp = TextBuilder.newInstance();
        tmp.append(l);
        Text txt = tmp.toText();
        TextBuilder.recycle(tmp);
        return txt;
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
        TextBuilder tmp = TextBuilder.newInstance();
        tmp.append(l, radix);
        Text txt = tmp.toText();
        TextBuilder.recycle(tmp);
        return txt;
    }

    /**
     * Returns the textual representation of the specified <code>float</code>
     * instance.
     *
     * @param  f the <code>float</code> to format.
     * @return the corresponding text instance.
     /*@JVM-1.1+@
     public static Text valueOf(float f) {
     TextBuilder tmp = TextBuilder.newInstance();
     tmp.append(f);
     Text txt = tmp.toText();
     TextBuilder.recycle(tmp);
     return txt;
     }
     /**/

    /**
     * Returns the textual representation of the specified <code>double</code>
     * argument.
     *
     * @param  d the <code>double</code> to format.
     * @return the corresponding text instance.
     /*@JVM-1.1+@
     public static Text valueOf(double d) {
     TextBuilder tmp = TextBuilder.newInstance();
     tmp.append(d);
     Text txt = tmp.toText();
     TextBuilder.recycle(tmp);
     return txt;
     }
     /**/

    /**
     * Returns the length of this text.
     *
     * @return the number of characters (16-bits Unicode) composing this text.
     */
    public final int length() {
        return _count;
    }

    /**
     * Returns the concatenation of this text and the textual 
     * representation of the specified object.
     * 
     * @param  obj the object whose textual representation is appended.
     * @return <code>this.concat(Text.valueOf(obj))</code>
     */
    public final Text plus(Object obj) {
        return this.concat(Text.valueOf(obj));
    }

    /**
     * Concatenates the specified text to the end of this text. 
     * This method is extremely fast (faster even than 
     * <code>StringBuffer.append(String)</code>) and still returns
     * a text instance with an internal binary tree of minimal depth!
     *
     * @param  that the text that is concatenated.
     * @return <code>this + that</code>
     */
    public final Text concat(Text that) {
        if (this._count == 0) {
            return that;
        } else if (that._count == 0) {
            return this;
        } else {
            return this.concat(that, (Factory.Pool) Composite.FACTORY
                    .currentPool());
        }
    }

    private final Text concat(Text that, Factory.Pool pool) {
        // All Text instances are maintained balanced:
        //   (head < tail * 2) & (tail < head * 2)
        if (((this._count << 1) < that._count) && (that instanceof Composite)) {
            // this too small, returns (this + that/2) + (that/2) 
            Composite thatComposite = (Composite) that;
            if (thatComposite._head._count > thatComposite._tail._count) {
                // Rotates to concatenate with smaller part.
                thatComposite = thatComposite.rightRotation(pool);
            }
            return Composite.newInstance(
                    this.concat(thatComposite._head, pool),
                    thatComposite._tail, pool);
        } else if (((that._count << 1) < this._count)
                && (this instanceof Composite)) {
            // that too small, returns (this/2) + (this/2 concat that)
            Composite thisComposite = (Composite) this;
            if (thisComposite._tail._count > thisComposite._head._count) {
                // Rotates to concatenate with smaller part.
                thisComposite = thisComposite.leftRotation(pool);
            }
            return Composite.newInstance(thisComposite._head,
                    thisComposite._tail.concat(that, pool), pool);
        } else { // this and that balanced (or not composite).
            return Composite.newInstance(this, that, pool);
        }
    }

    /**
     * Returns a portion of this text.
     * 
     * @param  start the index of the first character inclusive.
     * @return the sub-text starting at the specified position.
     * @throws IndexOutOfBoundsException if <code>(start < 0) || 
     *          (start > this.length())</code>
     */
    public final Text subtext(int start) {
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
    public final Text insert(int index, Text txt) {
        // Concatenates the smallest part first.
        return ((index << 1) < _count) ? this.subtext(0, index).concat(txt)
                .concat(this.subtext(index)) : this.subtext(0, index).concat(
                txt.concat(this.subtext(index)));
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
    public final Text delete(int start, int end) {
        return this.subtext(0, start).concat(this.subtext(end));
    }

    /**
     * Replaces each character sequence of this text that matches the specified 
     * target sequence with the specified replacement sequence.
     *
     * @param target the character sequence to be replaced.
     * @param replacement the replacement sequence.
     * @return the resulting text.
     */
    public final Text replace(CharSequence target, CharSequence replacement) {
        int i = indexOf(target);
        return (i < 0) ? this : // No target sequence found.
                this.subtext(0, i).concat(Text.valueOf(replacement)).concat(
                        this.subtext(i + target.length()).replace(target,
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
    public final Text replace(CharSet charSet, CharSequence replacement) {
        int i = indexOfAny(charSet);
        return (i < 0) ? this : // No character to replace.
                this.subtext(0, i).concat(Text.valueOf(replacement)).concat(
                        this.subtext(i + 1).replace(charSet, replacement));
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
    public final CharSequence subSequence(int start, int end) {
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

        // Limit cases.
        final int csqLength = csq.length();
        final int min = Math.max(0, fromIndex);
        final int max = _count - csqLength;
        if (csqLength == 0) {
            return (min > max) ? -1 : min;
        }

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
            if (match) {
                return i;
            }
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
    public final int lastIndexOf(CharSequence csq) {
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
    public final int lastIndexOf(CharSequence csq, int fromIndex) {

        // Limit cases.
        final int csqLength = csq.length();
        final int min = 0;
        final int max = Math.min(fromIndex, _count - csqLength);
        if (csqLength == 0) {
            return (min > max) ? -1 : max;
        }

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
            if (match) {
                return i;
            }
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
    public final boolean startsWith(CharSequence prefix) {
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
        final int prefixLength = prefix.length();
        if ((index >= 0) && (index <= (this.length() - prefixLength))) {
            for (int i = 0, j = index; i < prefixLength;) {
                if (prefix.charAt(i++) != this.charAt(j++)) {
                    return false;
                }
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
    public final Text trim() {
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
     * Returns a text equals to the specified character sequence from a pool of
     * unique text instances in <code>ImmortalMemory</code>.  
     * 
     * @return an unique text instance allocated in <code>ImmortalMemory</code>.
     */
    public static Text intern(final CharSequence csq) {
        Text text = (Text) INTERN_INSTANCES.get(csq); // Thread-Safe - No entry removed.
        if (text != null)
            return text;
        return Text.intern(csq.toString());
    }

    /**
     * Returns a text equals to the specified string from a pool of
     * unique text instances in <code>ImmortalMemory</code>.  
     * 
     * @return an unique text instance allocated in <code>ImmortalMemory</code>.
     */
    public static Text intern(final String str) {
        Text text = (Text) INTERN_INSTANCES.get(str); // Thread-Safe - No entry removed.
        if (text != null)
            return text;
        synchronized (INTERN_INSTANCES) {
            // Synchronized check to ensure unicity.
            text = (Text) INTERN_INSTANCES.get(str);
            if (text != null)
                return text;
            _tmpString = str;
            MemoryArea.getMemoryArea(INTERN_INSTANCES).executeInArea(
                    INTERN_STRING);
        }
        return (Text) INTERN_INSTANCES.get(str);
    }

    /**
     * Indicates if this text has the same character content as the specified
     * character sequence.
     *
     * @param csq the character sequence to compare with.
     * @return <code>true</code> if the specified character sequence has the 
     *        same character content as this text; <code>false</code> otherwise.
     */
    public final boolean contentEquals(CharSequence csq) {
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
    public final boolean contentEqualsIgnoreCase(CharSequence csq) {
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
    public final boolean equals(Object obj) {
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
    public final int hashCode() {
        if (_hashCode != 0)
            return _hashCode;
        int h = _hashCode;
        final int length = this.length();
        for (int i = 0; i < length;) {
            h = 31 * h + charAt(i++);
        }
        return _hashCode = h;
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
    public final int compareTo(Object csq) {
        return ((FastComparator) FastComparator.LEXICAL).compare(this, csq);
    }

    /**
     * Returns <code>this</code> (implements 
     * {@link javolution.context.Realtime Realtime} interface).
     *
     * @return <code>this</code>
     */
    public final Text toText() {
        return this;
    }

    /**
     * Returns a compact copy of this text allocated on the stack when 
     * executing in a {@link javolution.context.PoolContext PoolContext}.
     *
     * @return a local compact copy of this text.
     */
    public final Text copy() {
        return Text.valueOf(this, 0, _count);
    }

    /**
     * Returns the depth of the internal tree used to represent this text.
     *
     * @return the maximum depth of the text internal binary tree.
     */
    public abstract int depth();

    /**
     * Returns the character at the specified index.
     *
     * @param  index the index of the character.
     * @return the character at the specified index.
     * @throws IndexOutOfBoundsException if <code>(index < 0) || 
     *         (index >= this.length())</code>
     */
    public abstract char charAt(int index);

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
    public abstract int indexOf(char c, int fromIndex);

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
    public abstract int lastIndexOf(char c, int fromIndex);

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
    public abstract Text subtext(int start, int end);

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
    public abstract void getChars(int start, int end, char dest[], int destPos);

    /**
     * Converts the characters of this text to lower case.
     * 
     * @return the text in lower case.
     * @see Character#toLowerCase(char) 
     */
    public abstract Text toLowerCase();

    /**
     * Converts the characters of this text to upper case.
     * 
     * @return the text in lower case.
     * @see Character#toUpperCase(char) 
     */
    public abstract Text toUpperCase();

    /**
     * Returns the <code>String</code> value  corresponding to this text.
     *
     * @return the <code>java.lang.String</code> for this text.
     */
    public abstract String stringValue();

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
        if (length <= Primitive.BLOCK_SIZE) {
            Primitive text = Primitive.newInstance(length);
            for (int i = 0; i < length;) {
                text._data[i++] = c;
            }
            return text;
        } else {
            final int middle = (length >> 1);
            return Composite.newInstance(Text.valueOf(c, middle), Text.valueOf(
                    c, length - middle));
        }
    }

    /**
     * Indicates if all characters of this text are whitespaces
     * (no characters greater than the space character).
     *
     *@return <code>true</code> if this text  contains only whitespace.
     */
    public final boolean isBlank() {
        return isBlank(0, length());
    }

    /**
     * Indicates if the specified sub-range of characters of this text
     * are whitespaces (no characters greater than the space character).
     *
     *@param start the start index.
     *@param length the number of characters to inspect.
     */
    public final boolean isBlank(int start, int length) {
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
    public final Text trimStart() {
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
    public final Text trimEnd() {
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
    public final Text padLeft(int len) {
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
    public final Text padLeft(int len, char c) {
        final int padSize = (len <= length()) ? 0 : len - length();
        return this.insert(0, Text.valueOf(c, padSize));
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
    public final Text padRight(int len) {
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
    public final Text padRight(int len, char c) {
        final int padSize = (len <= length()) ? 0 : len - length();
        return this.concat(Text.valueOf(c, padSize));
    }

    /**
     * Returns the index within this text of the first occurrence
     * of any character in the specified character set.
     *
     * @param  charSet the character set.
     * @return the index of the first character that matches one of the
     *         characters in the supplied set; or <code>-1</code> if none.
     */
    public final int indexOfAny(CharSet charSet) {
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
    public final int indexOfAny(CharSet charSet, int start) {
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
    public final int indexOfAny(CharSet charSet, int start, int length) {
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
    public final int lastIndexOfAny(CharSet charSet) {
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
    public final int lastIndexOfAny(CharSet charSet, int start) {
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
    public final int lastIndexOfAny(CharSet charSet, int start, int length) {
        for (int i = start + length; --i >= start;) {
            if (charSet.contains(charAt(i)))
                return i;
        }
        return -1;
    }

    //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * This class represents a text block (up to 32 characters).
     */
    private static final class Primitive extends Text {

        /**
         * Holds the default size for primitive blocks of characters.
         */
        private static final int BLOCK_SIZE = 1 << TextBuilder.D0;

        /**
         * Holds the associated factory.
         */
        private static final Factory FACTORY = new Factory() {

            public Object create() {
                return new Primitive();
            }
        };

        /**
         * Holds the raw data (primitive).
         */
        private final char[] _data = new char[BLOCK_SIZE];

        /**
         * Default constructor.
         */
        private Primitive() {
        }

        /**
         * Returns a new/recycled primitive text of specified length.
         * 
         * @param the length of this primitive text.
         */
        private static Primitive newInstance(int length) {
            return newInstance(length, (Factory.Pool) FACTORY.currentPool());
        }

        private static Primitive newInstance(int length, Factory.Pool pool) {
            Primitive text = (Primitive) pool.next();
            text._count = length;
            text._hashCode = 0;
            return text;
        }

        // Implements abstract method.
        public int depth() {
            return 0;
        }

        // Implements abstract method.
        public char charAt(int index) {
            if (index >= _count)
                throw new IndexOutOfBoundsException();
            return _data[index];
        }

        // Implements abstract method.
        public int indexOf(char c, int fromIndex) {
            for (int i = Math.max(fromIndex, 0); i < _count; i++) {
                if (_data[i] == c)
                    return i;
            }
            return -1;
        }

        // Implements abstract method.
        public int lastIndexOf(char c, int fromIndex) {
            for (int i = Math.min(fromIndex, _count - 1); i >= 0; i--) {
                if (_data[i] == c)
                    return i;
            }
            return -1;
        }

        // Implements abstract method.
        public Text subtext(int start, int end) {
            if ((start == 0) && (end == _count))
                return this;
            if ((start < 0) || (start > end) || (end > _count))
                throw new IndexOutOfBoundsException();
            if (start == end)
                return Text.EMPTY;
            Primitive text = Primitive.newInstance(end - start);
            for (int i = start, j = 0; i < end;) {
                text._data[j++] = _data[i++];
            }
            return text;
        }

        // Implements abstract method.
        public void getChars(int start, int end, char dest[], int destPos) {
            if ((end > _count) || (end < start))
                throw new IndexOutOfBoundsException();
            for (int i = start, j = destPos; i < end;) {
                dest[j++] = _data[i++];
            }
        }

        // Implements abstract method.
        public Text toLowerCase() {
            Primitive text = newInstance(_count);
            for (int i = 0; i < _count;) {
                text._data[i] = Character.toLowerCase(_data[i++]);
            }
            return text;
        }

        // Implements abstract method.
        public Text toUpperCase() {
            Primitive text = newInstance(_count);
            for (int i = 0; i < _count;) {
                text._data[i] = Character.toUpperCase(_data[i++]);
            }
            return text;
        }

        // Implements abstract method.
        public String stringValue() {
            return new String(_data, 0, _count);
        }
    }

    /**
     * This class represents a text composite.
     */
    private static final class Composite extends Text {

        /**
         * Holds the associtate factory.
         */
        private static final Factory FACTORY = new Factory() {

            public Object create() {
                return new Composite();
            }
        };

        /**
         * Holds the head block of character (composite).
         */
        private Text _head;

        /**
         * Holds the tail block of character (composite).
         */
        private Text _tail;

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
        private static Composite newInstance(Text head, Text tail) {
            return newInstance(head, tail, (Factory.Pool) FACTORY.currentPool());
        }

        private static Composite newInstance(Text head, Text tail,
                Factory.Pool pool) {
            Composite text = (Composite) pool.next();
            text._hashCode = 0;
            text._count = head._count + tail._count;
            text._head = head;
            text._tail = tail;
            return text;
        }

        /**
         * Returns the right rotation of this composite.
         * The resulting text is still balanced if head > tail.
         * 
         * @param pool the current pool.
         * @return the same text with a different structure.
         */
        private Composite rightRotation(Factory.Pool pool) {
            // See: http://en.wikipedia.org/wiki/Tree_rotation
            if (!(this._head instanceof Composite))
                return this; // Cannot rotate.
            Composite P = (Composite) this._head;
            Text A = P._head;
            Text B = P._tail;
            Text C = this._tail;
            return Composite.newInstance(A, Composite.newInstance(B, C, pool),
                    pool);
        }

        /**
         * Returns the left rotation of this composite.
         * The resulting text is still balanced if tail > head.
         * 
         * @param pool the current pool.
         * @return the same text with a different structure.
         */
        private Composite leftRotation(Factory.Pool pool) {
            // See: http://en.wikipedia.org/wiki/Tree_rotation
            if (!(this._tail instanceof Composite))
                return this; // Cannot rotate.
            Composite Q = (Composite) this._tail;
            Text B = Q._head;
            Text C = Q._tail;
            Text A = this._head;
            return Composite.newInstance(Composite.newInstance(A, B, pool), C,
                    pool);
        }

        // Implements abstract method.
        public int depth() {
            return Math.max(_head.depth(), _tail.depth()) + 1;
        }

        // Implements abstract method.
        public char charAt(int index) {
            return (index < _head._count) ? _head.charAt(index) : _tail
                    .charAt(index - _head._count);
        }

        // Implements abstract method.
        public int indexOf(char c, int fromIndex) {
            final int cesure = _head._count;
            if (fromIndex < cesure) {
                final int headIndex = _head.indexOf(c, fromIndex);
                if (headIndex >= 0)
                    return headIndex; // Found in head.
            }
            final int tailIndex = _tail.indexOf(c, fromIndex - cesure);
            return (tailIndex >= 0) ? tailIndex + cesure : -1;
        }

        // Implements abstract method.
        public int lastIndexOf(char c, int fromIndex) {
            final int cesure = _head._count;
            if (fromIndex >= cesure) {
                final int tailIndex = _tail.lastIndexOf(c, fromIndex - cesure);
                if (tailIndex >= 0)
                    return tailIndex + cesure; // Found in tail.
            }
            return _head.lastIndexOf(c, fromIndex);
        }

        // Implements abstract method.
        public Text subtext(int start, int end) {
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
        public Text toLowerCase() {
            return newInstance(_head.toLowerCase(), _tail.toLowerCase());
        }

        // Implements abstract method.
        public Text toUpperCase() {
            return newInstance(_head.toUpperCase(), _tail.toUpperCase());
        }

        // Implements abstract method.
        public String stringValue() {
            char[] data = new char[_count];
            this.getChars(0, _count, data, 0);
            return new String(data, 0, _count);
        }

        // Overrides.
        public boolean move(ObjectSpace os) {
            if (super.move(os)) {
                _head.move(os);
                _tail.move(os);
                return true;
            }
            return false;
        }
    }

    /**
     * This class represents a String Wrapper.
     */
    private static final class StringWrapper extends Text {

        /**
         * Holds the associated factory.
         */
        private static final Factory FACTORY = new Factory() {

            public Object create() {
                return new StringWrapper();
            }
        };

        /**
         * Holds the string.
         */
        private String _string;

        /**
         * Holds the offset.
         */
        private int _offset;

        /**
         * Default constructor.
         */
        private StringWrapper() {
        }

        /**
         * Returns a new/recycled text wrapping the specified string.
         * 
         * @param str the string to wrap.
         * @return the corresponding text instance. 
         */
        private static StringWrapper newInstance(String str) {
            StringWrapper text = (StringWrapper) FACTORY.object();
            text._count = str.length();
            text._hashCode = 0;
            text._string = str;
            text._offset = 0;
            return text;
        }

        // Implements abstract method.
        public int depth() {
            return 0;
        }

        // Implements abstract method.
        public char charAt(int index) {
            if ((index >= _count) || (index < 0))
                throw new IndexOutOfBoundsException();
            return _string.charAt(_offset + index);
        }

        // Implements abstract method.
        public int indexOf(char c, int fromIndex) {
            for (int i = Math.max(fromIndex, 0); i < _count; i++) {
                if (_string.charAt(_offset + i) == c)
                    return i;
            }
            return -1;
        }

        // Implements abstract method.
        public int lastIndexOf(char c, int fromIndex) {
            for (int i = Math.min(fromIndex, _count - 1); i >= 0; i--) {
                if (_string.charAt(_offset + i) == c)
                    return i;
            }
            return -1;
        }

        // Implements abstract method.
        public Text subtext(int start, int end) {
            if ((start == 0) && (end == _count))
                return this;
            if ((start < 0) || (start > end) || (end > _count))
                throw new IndexOutOfBoundsException();
            if (start == end)
                return Text.EMPTY;
            StringWrapper text = (StringWrapper) FACTORY.object();
            text._count = end - start;
            text._hashCode = 0;
            text._string = _string;
            text._offset = _offset + start;
            return text;
        }

        // Implements abstract method.
        public void getChars(int start, int end, char dest[], int destPos) {
            if ((end > _count) || (end < start) || (start < 0))
                throw new IndexOutOfBoundsException();
            _string.getChars(start + _offset, end + _offset, dest, destPos);
        }

        // Implements abstract method.
        public Text toLowerCase() {
            return copy().toLowerCase(); // To avoid dynamic heap allocation.
        }

        // Implements abstract method.
        public Text toUpperCase() {
            return copy().toUpperCase(); // To avoid dynamic heap allocation.
        }

        // Implements abstract method.
        public String stringValue() {
            if ((_offset == 0) && (_count == _string.length()))
                return _string;
            return _string.substring(_offset, _offset + _count);
        }

    }
}