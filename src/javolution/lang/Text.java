/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2005 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.lang;

import j2me.io.Serializable;
import j2me.lang.CharSequence;
import j2me.lang.Comparable;

import javolution.realtime.RealtimeObject;
import javolution.util.FastComparator;
import javolution.util.FastMap;
import javolution.util.MathLib;
import javolution.xml.XmlElement;
import javolution.xml.XmlFormat;

/**
 * <p> This class represents an immutable character sequence with extremely
 *     fast {@link #plus concatenation}, {@link #insert insertion} and 
 *     {@link #delete deletion} capabilities (<b>O[Log(n)]</b>).</p>
 * <p> Instances of this class have the following advantages over 
 *     {@link String}</code>:<ul>
 *     <li> No need for an intermediate {@link StringBuffer} in order to 
 *          manipulate textual document. {@link Text} methods are much 
 *          faster (especially for large document).</li>
 *     <li> Bug free. They are not plagued by the {@link String#substring} <a
 *          href="http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4513622">
 *          memory leak bug</a> (when small substrings prevent memory from 
 *          larger string from being garbage collected).</li>
 *     <li> More flexible as they allows for search, concatenation and
 *          comparison with any <code>CharSequence</code> such as itself, 
 *          <code>java.lang.String</code>, <code>java.lang.StringBuffer</code>
 *          or <code>java.lang.StringBuilder (JDK1.5)</code>.</li>
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
 * @version 2.2, January 30, 2004
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
     * Instances are created using the {@link #valueOf(CharSequence)}
     * factory method during deserialization (on the stack when
     * executing in a {@link javolution.realtime.PoolContext PoolContext}).
     */
    protected static final XmlFormat TEXT_XML = new XmlFormat(EMPTY.getClass()) {
        public void format(Object obj, XmlElement xml) {
            xml.setAttribute("value", (Text)obj);
        }
        public Object parse(XmlElement xml) {
            return Text.valueOf(xml.getAttribute("value"));
        }
    };

    /**
     * Holds the <code>"null"</code> character sequence.
     */
    static final Text NULL = Text.valueOf("null").intern();

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
                text._data[i] = str.charAt(start + i++);
            }
            text._count = length;
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
                text._data[i] = csq.charAt(start + i++);
            }
            text._count = length;
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
                text._data[i] = data[offset + i++];
            }
            text._count = length;
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
     * Returns the length of this text.
     *
     * @return the number of characters (16-bits Unicode) composing this text.
     */
    public final int length() {
        return _count;
    }

    /**
     * Returns a copy of this text allocated on the current "stack".
     * This method compacts this text (maximize usage of primitive text nodes). 
     *
     * @return a local copy of <code>this</code> text.
     */
    public final Text copy() {
        return Text.valueOf(this);
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
     * Returns the text with the specified character sequence inserted at 
     * the specified location (convenience method).
     *
     * @param index the insertion position.
     * @param csq the character sequence being inserted.
     * @return <code>subtext(0, index).concat(csq).plus(subtext(index))</code>
     * @throws IndexOutOfBoundsException if index is negative or greater
     *         than <code>this.length()</code>.
     */
    public Text insert(int index, CharSequence csq) {
        return this.subtext(0, index).concat(csq).plus(this.subtext(index));
    }

    /**
     * Returns the text with the characters between the specified indexes 
     * deleted (convenience method).
     *
     * @param start the beginning index, inclusive.
     * @param end the ending index, exclusive.
     * @return <code>subtext(0, start).plus(subtext(end))</code>
     * @throws IndexOutOfBoundsException if start or end are negative, start 
     *         is greater than end, or end is greater than 
     *         <code>this.length()</code>.
     */
    public Text delete(int start, int end) {
        return this.subtext(0, start).plus(this.subtext(end));
    }

    /**
     * Replaces each character sequence of this text that matches the specified 
     * target sequence with the specified replacement sequence.
     *
     * @param target the sequence to be replaced.
     * @param replacement the replacement sequence of char values.
     * @return the resulting text.
     * @throws IndexOutOfBoundsException if index is negative or greater
     *         than <code>this.length()</code>.
     */
    public Text replace(CharSequence target, CharSequence replacement) {
        int i = indexOf(target);
        return (i < 0) ?  
            this : // No target sequence found.
            this.subtext(0, i).concat(replacement).plus(
            this.subtext(i + target.length()).replace(target, replacement));        
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
        Text text = (Text) INTERN_TEXT.get(this);
        if (text == null) {
            text = this;
            text.moveHeap();
            synchronized (INTERN_TEXT) { // Only put need synchronizing.
                INTERN_TEXT.put(text, text);
            }
        }
        return text;
    }

    /**
     * Compares this text against the specified object. This method 
     * uses a {@link FastComparator#LEXICAL lexical comparator}
     * to make this determination.
     *
     * <p> Note: Unfortunately, due to the current (JDK 1.4+) implementation
     *          of <code>java.lang.String</code> and <code>
     *          java.lang.StringBuffer</code>, this method is not symmetric.</p>
     *
     * @param  that the object to compare with.
     * @return <code>FastComparator.LEXICAL.areEqual(this, that)</code>
     */
    public final boolean equals(Object that) {
        if (this == that) {
            return true;
        } else if ((that instanceof CharSequence) || (that instanceof String)) {
            return FastComparator.LEXICAL.areEqual(this, that);
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
     * Returns the {@link Text} representation of this text.
     *
     * @return <code>this</code>
     */
    public final Text toText() {
        return this;
    }

    /**
     * Returns the depth of the internal tree used to represent this text.
     * Text operations keep this depth minimal; although it is possible
     * to slightly reduce it through {@link #copy} (compacting). 
     *
     * @return the maximum depth of the text internal binary tree.
     */
    public abstract int depth();

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
     * This method is extremely fast (faster even than 
     * <code>StringBuffer.append(String)</code>) and still returns
     * a text instance with an internal binary tree of minimal depth!
     *
     * @param  that the character sequence that is concatenated.
     * @return <code>this + that</code>
     */
    public final Text plus(Text that) {
        if (this._count == 0) {
            return that;
        } else if (that._count == 0) {
            return this;
        } else if (((that._count << 1) < this._count) &&
                (this instanceof Composite)) { // this too large, break up?
            Composite thisComposite = (Composite) this;
            if (thisComposite._head._count > thisComposite._tail._count) {
                return Composite.newInstance(thisComposite._head, thisComposite._tail.plus(that));
            } else {
                return Composite.newInstance(this, that);
            } 
        } else if (((this._count << 1) < that._count) &&
                   (that instanceof Composite)) { // that too large, break up?
            Composite thatComposite = (Composite) that;
            if (thatComposite._head._count < thatComposite._tail._count) {
                return Composite.newInstance(this.plus(thatComposite._head), thatComposite._tail);
            } else {
                return Composite.newInstance(this, that);
            } 
        } else { // 
            return Composite.newInstance(this, that);
        }
    }

    /**
     * Returns the <code>String</code> value  corresponding to this text.
     *
     * @return the <code>java.lang.String</code> for this text.
     */
    public abstract String stringValue();

    /**
     * This class represents a text block (up to 32 characters).
     */
    private static final class Primitive extends Text {


        /**
         * Holds the default size for primitive blocks of characters.
         */
        private static final int BLOCK_SIZE = 32;

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
         * Returns a new/recycled primitive text.
         * 
         * @return a primitive text. 
         */
        private static Primitive newInstance() {
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
        public int depth() {
            return 0;
        }

       // Implements abstract method.
        public char charAt(int index) {
            if (index >= _count) {
                throw new IndexOutOfBoundsException();
            }
            return _data[index];
        }

        // Implements abstract method.
        public Text subtext(int start, int end) {
            if ((start == 0) && (end == _count)) {
                return this;
            } else if ((start >= 0) && (start <= end) && (end <= _count)) {
                if (start == end) {
                    return Text.EMPTY;
                } else {
                    Primitive text = Primitive.newInstance();
                    for (int i = start, j = 0; i < end;) {
                        text._data[j++] = _data[i++];
                    }
                    text._count = end - start;
                    return text;
                }    
            } else {
                throw new IndexOutOfBoundsException(
                        "start: " + start + ", end: " + end);
            }
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
        public String stringValue() {
            return new String(_data, 0, _count);
        }
        
    }

    /**
     * This class represents a text composite.
     */
    private static final class Composite extends Text {

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
        public int depth() {
            return MathLib.max(_head.depth(), _tail.depth()) + 1;
        }
        
        // Implements abstract method.
        public char charAt(int index) {
            return (index < _head._count) ? _head.charAt(index) : _tail
                    .charAt(index - _head._count);
        }

        // Implements abstract method.
        public Text subtext(int start, int end) {
            if ((start == 0) && (end == _count)) {
                return this;
            } else if ((start >= 0) && (start <= end) && (end <= _count)) {
                if (start == end) {
                    return Text.EMPTY;
                } else {
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
            } else {
                throw new IndexOutOfBoundsException(
                        "start: " + start + ", end: " + end);
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

}