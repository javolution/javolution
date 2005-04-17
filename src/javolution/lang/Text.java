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
import j2me.lang.Comparable;

import javolution.JavolutionError;
import javolution.realtime.Realtime;
import javolution.realtime.RealtimeObject;
import javolution.util.FastComparator;
import javolution.util.FastMap;
import javolution.xml.XmlElement;
import javolution.xml.XmlFormat;

/**
 * <p> This class represents an immutable character sequence with extremely
 *     fast {@link #concat concatenation}, {@link #insert insertion} and 
 *     {@link #delete deletion} capabilities (<b>O[Log(n)]</b>).</p>
 * <p> Instances of this class have the following advantages over 
 *     {@link String}</code>:<ul>
 *     <li> No need for an intermediate {@link StringBuffer} in order to 
 *          manipulate textual documents. {@link Text} methods are much 
 *          faster (especially for large documents).</li>
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
 * <p><i> Implementation Note: To avoid expensive copy operations , 
 *        {@link Text} instances are broken down into smaller immutable 
 *        sequences (they form a minimal-depth binary tree). 
 *        Internal copies are then performed in <code>O[Log(n)]</code>
 *        instead of <code>O[n]</code>).</i></p>
 * 
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 3.1, March 16, 2005
 */
public abstract class Text extends RealtimeObject implements CharSequence,
        Comparable, Serializable {

    /**
     * Holds the text interned.
     */
    private static final FastMap INTERN_TEXT = new FastMap();

    /**
     * Holds an empty character sequence.
     */
    public static final Text EMPTY = Text.valueOf("").intern();

    /**
     * Holds the default XML representation for this class and its sub-classes.
     * This representation consists of a <code>"value"</code> attribute 
     * holding the character sequence.
     * Instances are created using the {@link #valueOf(Object)}
     * factory method during deserialization (on the stack when
     * executing in a {@link javolution.realtime.PoolContext PoolContext}).
     */
    protected static final XmlFormat TEXT_XML = new XmlFormat(EMPTY.getClass()) {
        public void format(Object obj, XmlElement xml) {
            xml.setAttribute("value", (Text) obj);
        }

        public Object parse(XmlElement xml) {
            return Text.valueOf(xml.getAttribute("value"));
        }
    };

    /**
     * Holds the <code>"null"</code> character sequence.
     */
    public static final Text NULL = Text.valueOf("null").intern();

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
        if (obj instanceof String) {
            return StringWrapper.newInstance((String) obj);
        } else if (obj instanceof Realtime) {
            return ((Realtime) obj).toText();
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

    private static final Text TRUE = Text.valueOf("true").intern();

    private static final Text FALSE = Text.valueOf("false").intern();

    /**
     * Returns the text representation of the <code>char</code> argument.
     *
     * @param c a character.
     * @return a text of length <code>1</code> containing <code>'c'</code>.
     */
    public static Text valueOf(char c) {
        Primitive text = Primitive.newInstance(1);
        text._data[0] = c;
        return text;
    }

    /**
     * Returns the text representation of the <code>int</code> argument.
     *
     * @param i a 32 bits integer.
     * @return the text representation of the <code>int</code> argument.
     */
    public static Text valueOf(int i) {
        try {
            Primitive text = Primitive.newInstance(0);
            TypeFormat.format(i, text);
            return text;
        } catch (IOException e) {
            throw new JavolutionError(e);
        }
    }

    /**
     * Returns the text representation of the <code>long</code> argument.
     *
     * @param l a 64 bits integer.
     * @return the text representation of the <code>long</code> argument.
     */
    public static Text valueOf(long l) {
        try {
            Primitive text = Primitive.newInstance(0);
            TypeFormat.format(l, text);
            return text;
        } catch (IOException e) {
            throw new JavolutionError(e);
        }
    }

    /**
     * Returns the text representation of the <code>float</code> argument.
     *
     * @param f a 32 bits floati.
     * @return the text representation of the <code>float</code> argument.
     /*@FLOATING_POINT@
     public static Text valueOf(float f) {
     try {
     Primitive text = Primitive.newInstance(0);
     TypeFormat.format(f, text);
     return text;
     } catch (IOException e) {
     throw new JavolutionError(e);
     }
     }
     /**/

    /**
     * Returns the text representation of the <code>double</code> argument.
     *
     * @param d a 64 bits float.
     * @return the text representation of the <code>float</code> argument.
     /*@FLOATING_POINT@
     public static Text valueOf(double d) {
     try {
     Primitive text = Primitive.newInstance(0);
     TypeFormat.format(d, text);
     return text;
     } catch (IOException e) {
     throw new JavolutionError(e);
     }
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
        } else if (((that._count << 1) < this._count)
                && (this instanceof Composite)) { // this too large, break up?
            Composite thisComposite = (Composite) this;
            if (thisComposite._head._count > thisComposite._tail._count) {
                return Composite.newInstance(thisComposite._head,
                        thisComposite._tail.concat(that));
            } else {
                return Composite.newInstance(this, that);
            }
        } else if (((this._count << 1) < that._count)
                && (that instanceof Composite)) { // that too large, break up?
            Composite thatComposite = (Composite) that;
            if (thatComposite._head._count < thatComposite._tail._count) {
                return Composite.newInstance(this.concat(thatComposite._head),
                        thatComposite._tail);
            } else {
                return Composite.newInstance(this, that);
            }
        } else { // 
            return Composite.newInstance(this, that);
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
     * @param target the text to be replaced.
     * @param replacement the replacement text.
     * @return the resulting text.
     */
    public final Text replace(Text target, Text replacement) {
        int i = indexOf(target);
        return (i < 0) ? this : // No target sequence found.
                this.subtext(0, i).concat(replacement).concat(
                        this.subtext(i + target.length()).replace(target,
                                replacement));
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
     * Returns a text equals to this text from a pool of unique text
     * instances.  
     * For any two text t1 and t2, <code>(t1.intern() == t2.intern())</code>
     * if and only if <code>(t1.equals(t2))</code>.
     * 
     * @return an unique instance allocated on the heap and equals to this text.
     */
    public final Text intern() {
        Text text = (Text) INTERN_TEXT.get(this); // FastMap supports concurrency.
        if (text == null) {
            synchronized (INTERN_TEXT) {
                text = (Text) INTERN_TEXT.get(this); // Ensures unicity.
                if (text == null) {
                    text = this;
                    text.moveHeap();
                    INTERN_TEXT.put(text, text);
                }
            }
        }
        return text;
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
        return FastComparator.LEXICAL.compare(this, csq);
    }

    /**
     * Returns <code>this</code> (implements 
     * {@link javolution.realtime.Realtime Realtime} interface).
     *
     * @return <code>this</code>
     */
    public final Text toText() {
        return this;
    }

    /**
     * Returns a compact copy of this text allocated on the stack when 
     * executing in a {@link javolution.realtime.PoolContext PoolContext}.
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

    /**
     * This class represents a text block (up to 32 characters).
     */
    private static final class Primitive extends Text implements Appendable {

        /**
         * Holds the default size for primitive blocks of characters.
         */
        private static final int BLOCK_SIZE = 32;

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
            Primitive text = (Primitive) FACTORY.object();
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

        // Implements appendable (for primitive types formatting).
        public Appendable append(char c) throws IOException {
            _data[_count++] = c;
            return this;
        }

        // Implements appendable (for primitive types formatting).
        public Appendable append(CharSequence csq) throws IOException {
            return append(csq, 0, csq.length());
        }

        // Implements appendable (for primitive types formatting).
        public Appendable append(CharSequence csq, int start, int end)
                throws IOException {
            for (int i = start; i < end;) {
                _data[_count++] = csq.charAt(i++);
            }
            return this;
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
            Composite text = (Composite) FACTORY.object();
            text._hashCode = 0;
            text._count = head._count + tail._count;
            text._head = head;
            text._tail = tail;
            return text;
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