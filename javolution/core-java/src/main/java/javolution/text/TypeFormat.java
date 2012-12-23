/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.text;

import java.io.IOException;
import javolution.annotation.StackSafe;
import javolution.lang.MathLib;

/**
 * <p> This class provides utility methods to parse 
 *     {@link CharSequence} into primitive types and to format 
 *     primitive types into any {@link Appendable}.</p>
 *
 * <p> Methods from this class <b>do not create temporary objects</b> and
 *     are typically faster than standard library methods (see 
 *     <a href="http://javolution.org/doc/benchmark.html">benchmark</a>).</p>
 *     
 * <p> The number of digits when formatting floating point numbers can be 
 *     specified. The default setting for <code>double</code> is 17 digits 
 *     or even 16 digits when the conversion is lossless back and forth
 *     (mimic the standard library formatting). For example:[code]
 *         TypeFormat.format(0.2, a) = "0.2" // 17 or 16 digits (as long as lossless conversion), remove trailing zeros.
 *         TypeFormat.format(0.2, 17, false, false, a) = "0.20000000000000001" // Closest 17 digits number.
 *         TypeFormat.format(0.2, 19, false, false, a) = "0.2000000000000000111" // Closest 19 digits.
 *         TypeFormat.format(0.2, 4, false, false, a) = "0.2" // Fixed-point notation, remove trailing zeros.
 *         TypeFormat.format(0.2, 4, false, true, a) = "0.2000" // Fixed-point notation, fixed number of digits.
 *         TypeFormat.format(0.2, 4, true, false, a) = "2.0E-1" // Scientific notation, remove trailing zeros.  
 *         TypeFormat.format(0.2, 4, true, true, a) = "2.000E-1" // Scientific notation, fixed number of digits.
 *         [/code]</p>        
 *
 * <p> For non-primitive objects, formatting is typically performed using 
 *     specialized {@link TextFormat} instances.</p>
 * 
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 5.3, February 15, 2009
 */
@StackSafe
public final class TypeFormat {

    /**
     * Default constructor (forbids derivation).
     */
    private TypeFormat() {
    }

    /////////////
    // PARSING //
    /////////////

    /**
     * Parses the specified character sequence from the specified position 
     * as a <code>boolean</code> ignoring cases.
     *
     * @param csq the character sequence to parse.
     * @param cursor the cursor position (being maintained).
     * @return the next boolean value.
     * @throws IllegalArgumentException if the character sequence from the 
     *         specified position is different from "true" or "false" ignoring
     *         cases.
     */
    public static boolean parseBoolean(CharSequence csq, Cursor cursor) {
        int start = cursor.getIndex();
        int end = csq.length();
        if ((end >= start + 5) && (csq.charAt(start) == 'f' || csq.charAt(start) == 'F')) { // False.
            if ((csq.charAt(++start) == 'a' || csq.charAt(start) == 'A') && (csq.charAt(++start) == 'l' || csq.charAt(start) == 'L') && (csq.charAt(++start) == 's' || csq.charAt(start) == 'S') && (csq.charAt(++start) == 'e' || csq.charAt(start) == 'E')) {
                cursor.increment(5);
                return false;
            }
        } else if ((end >= start + 4) && (csq.charAt(start) == 't' || csq.charAt(start) == 'T')) // True.
            if ((csq.charAt(++start) == 'r' || csq.charAt(start) == 'R') && (csq.charAt(++start) == 'u' || csq.charAt(start) == 'U') && (csq.charAt(++start) == 'e' || csq.charAt(start) == 'E')) {
                cursor.increment(4);
                return true;
            }
        throw new IllegalArgumentException("Invalid boolean representation");
    }

    /**
     * Parses the whole specified character sequence as a <code>boolean</code>.
     *
     * @param  csq the character sequence to parse.
     * @return <code>parseBoolean(csq, new Cursor())</code>
     * @throws IllegalArgumentException if the specified character sequence 
     *         is different from "true" or "false" ignoring cases.
     */
    public static boolean parseBoolean(CharSequence csq) {
        Cursor cursor = new Cursor();
        boolean result = parseBoolean(csq, cursor);
        if (!cursor.atEnd(csq)) 
            throw new IllegalArgumentException("Extraneous characters \"" + cursor.tail(csq) + "\"");
        return result;
    }
    
    /**
     * Parses the specified character sequence from the specified position 
     * as a signed <code>byte</code> in the specified radix.
     *
     * @param  csq the character sequence to parse.
     * @param  radix the radix to be used while parsing.
     * @param cursor the cursor position being updated.
     * @return the corresponding <code>byte</code>.
     * @throws NumberFormatException if the specified character sequence
     *         does not contain a parsable <code>byte</code>.
     */
    public static byte parseByte(CharSequence csq, int radix, Cursor cursor) {
        int i = parseInt(csq, radix, cursor);
        if ((i < Byte.MIN_VALUE) || (i > Byte.MAX_VALUE))
            throw new NumberFormatException("Overflow");
        return (byte) i;
    }

    /**
     * Parses the whole specified character sequence  
     * as a signed <code>byte</code> in the specified radix.
     *
     * @param  csq the character sequence to parse.
     * @param  radix the radix to be used while parsing.
     * @return the corresponding <code>byte</code>.
     * @throws NumberFormatException if the specified character sequence
     *         does not contain a parsable <code>byte</code> or contains 
     *         extraneous characters.
     */
    public static byte parseByte(CharSequence csq, int radix) {
        Cursor cursor = new Cursor();
        byte result = parseByte(csq, radix, cursor);
        if (!cursor.atEnd(csq)) 
            throw new IllegalArgumentException("Extraneous characters \"" + cursor.tail(csq) + "\"");
        return result;
    }

    /**
     * Parses the specified character sequence from the specified position 
     * as a signed decimal <code>byte</code>.
     *
     * @param  csq the character sequence to parse.
     * @param cursor the cursor position being updated.
     * @return the corresponding <code>byte</code>.
     * @throws NumberFormatException if the specified character sequence
     *         does not contain a parsable <code>byte</code>.
     */
    public static byte parseByte(CharSequence csq, Cursor cursor) {
        return parseByte(csq, 10, cursor);
    }

    /**
     * Parses the whole specified character sequence as a signed decimal 
     * <code>byte</code>.
     *
     * @param  csq the character sequence to parse.
     * @return <code>parseByte(csq, 10)</code>
    *  @throws NumberFormatException if the specified character sequence
     *         does not contain a parsable <code>byte</code> or contains 
     *         extraneous characters.
     */
    public static byte parseByte(CharSequence csq) {
        return parseByte(csq, 10);
    }

    /**
     * Parses the specified character sequence from the specified position 
     * as a signed <code>short</code> in the specified radix.
     *
     * @param  csq the character sequence to parse.
     * @param  radix the radix to be used while parsing.
     * @param cursor the cursor position being updated.
     * @return the corresponding <code>short</code>.
     * @throws NumberFormatException if the specified character sequence
     *         does not contain a parsable <code>short</code>.
     */
    public static short parseShort(CharSequence csq, int radix, Cursor cursor) {
        int i = parseInt(csq, radix, cursor);
        if ((i < Short.MIN_VALUE) || (i > Short.MAX_VALUE))
            throw new NumberFormatException("Overflow");
        return (short) i;
    }

    /**
     * Parses the whole specified character sequence  
     * as a signed <code>short</code> in the specified radix.
     *
     * @param  csq the character sequence to parse.
     * @param  radix the radix to be used while parsing.
     * @return the corresponding <code>short</code>.
     * @throws NumberFormatException if the specified character sequence
     *         does not contain a parsable <code>short</code> or contains 
     *         extraneous characters.
     */
    public static short parseShort(CharSequence csq, int radix) {
        Cursor cursor = new Cursor();
        short result = parseShort(csq, radix, cursor);
        if (!cursor.atEnd(csq)) 
            throw new IllegalArgumentException("Extraneous characters \"" + cursor.tail(csq) + "\"");
        return result;
    }

    /**
     * Parses the specified character sequence from the specified position 
     * as a signed decimal <code>short</code>.
     *
     * @param  csq the character sequence to parse.
     * @param cursor the cursor position being updated.
     * @return the corresponding <code>short</code>.
     * @throws NumberFormatException if the specified character sequence
     *         does not contain a parsable <code>short</code>.
     */
    public static short parseShort(CharSequence csq, Cursor cursor) {
        return parseShort(csq, 10, cursor);
    }

    /**
     * Parses the whole specified character sequence as a signed decimal 
     * <code>short</code>.
     *
     * @param  csq the character sequence to parse.
     * @return <code>parseShort(csq, 10)</code>
    *  @throws NumberFormatException if the specified character sequence
     *         does not contain a parsable <code>short</code> or contains 
     *         extraneous characters.
     */
    public static short parseShort(CharSequence csq) {
        return parseShort(csq, 10);
    }

    /**
     * Parses the specified character sequence from the specified position 
     * as a signed <code>int</code> in the specified radix.
     *
     * @param  csq the character sequence to parse.
     * @param  radix the radix to be used while parsing.
     * @param cursor the cursor position being updated.
     * @return the corresponding <code>int</code>.
     * @throws NumberFormatException if the specified character sequence
     *         does not contain a parsable <code>int</code>.
     */
    public static int parseInt(CharSequence csq, int radix, Cursor cursor) {
        int start = cursor.getIndex();
        int end = csq.length();
        boolean isNegative = false;
        int result = 0; // Accumulates negatively (avoid MIN_VALUE overflow).
        int i = start;
        for (; i < end; i++) {
            char c = csq.charAt(i);
            int digit = (c <= '9') ? c - '0'
                    : ((c <= 'Z') && (c >= 'A')) ? c - 'A' + 10
                    : ((c <= 'z') && (c >= 'a')) ? c - 'a' + 10 : -1;
            if ((digit >= 0) && (digit < radix)) {
                int newResult = result * radix - digit;
                if (newResult > result)
                    throw new NumberFormatException("Overflow parsing " + csq.subSequence(start, end));
                result = newResult;
            } else if ((c == '-') && (i == start))
                isNegative = true;
            else if ((c == '+') && (i == start)) {
                // Ok.
            } else
                break;
        }
        // Requires one valid digit character and checks for opposite overflow.
        if ((result == 0) && ((end == 0) || (csq.charAt(i - 1) != '0')))
            throw new NumberFormatException("Invalid integer representation for " + csq.subSequence(start, end));
        if ((result == Integer.MIN_VALUE) && !isNegative)
            throw new NumberFormatException("Overflow parsing " + csq.subSequence(start, end));
        cursor.increment(i - start);
        return isNegative ? result : -result;
    }

    /**
     * Parses the whole specified character sequence  
     * as a signed <code>int</code> in the specified radix.
     *
     * @param  csq the character sequence to parse.
     * @param  radix the radix to be used while parsing.
     * @return the corresponding <code>int</code>.
     * @throws NumberFormatException if the specified character sequence
     *         does not contain a parsable <code>int</code> or contains 
     *         extraneous characters.
     */
    public static int parseInt(CharSequence csq, int radix) {
        Cursor cursor = new Cursor();
        int result = parseInt(csq, radix, cursor);
        if (!cursor.atEnd(csq)) 
            throw new IllegalArgumentException("Extraneous characters \"" + cursor.tail(csq) + "\"");
        return result;
    }

    /**
     * Parses the specified character sequence from the specified position 
     * as a signed decimal <code>int</code>.
     *
     * @param  csq the character sequence to parse.
     * @param cursor the cursor position being updated.
     * @return the corresponding <code>int</code>.
     * @throws NumberFormatException if the specified character sequence
     *         does not contain a parsable <code>int</code>.
     */
    public static int parseInt(CharSequence csq, Cursor cursor) {
        return parseInt(csq, 10, cursor);
    }

    /**
     * Parses the whole specified character sequence as a signed decimal 
     * <code>int</code>.
     *
     * @param  csq the character sequence to parse.
     * @return <code>parseInt(csq, 10)</code>
    *  @throws NumberFormatException if the specified character sequence
     *         does not contain a parsable <code>int</code> or contains 
     *         extraneous characters.
     */
    public static int parseInt(CharSequence csq) {
        return parseInt(csq, 10);
    }

    /**
     * Parses the specified character sequence from the specified position 
     * as a signed <code>long</code> in the specified radix.
     *
     * @param  csq the character sequence to parse.
     * @param  radix the radix to be used while parsing.
     * @param cursor the cursor position being updated.
     * @return the corresponding <code>long</code>.
     * @throws NumberFormatException if the specified character sequence
     *         does not contain a parsable <code>long</code>.
     */
    public static long parseLong(CharSequence csq, int radix, Cursor cursor) {
        final int start = cursor.getIndex();
        final int end = csq.length();
        boolean isNegative = false;
        long result = 0; // Accumulates negatively (avoid MIN_VALUE overflow).
        int i = start;
        for (; i < end; i++) {
            char c = csq.charAt(i);
            int digit = (c <= '9') ? c - '0'
                    : ((c <= 'Z') && (c >= 'A')) ? c - 'A' + 10
                    : ((c <= 'z') && (c >= 'a')) ? c - 'a' + 10 : -1;
            if ((digit >= 0) && (digit < radix)) {
                long newResult = result * radix - digit;
                if (newResult > result)
                    throw new NumberFormatException("Overflow parsing " + csq.subSequence(start, end));
                result = newResult;
            } else if ((c == '-') && (i == start))
                isNegative = true;
            else if ((c == '+') && (i == start)) {
                // Ok.
            } else
                break;
        }
        // Requires one valid digit character and checks for opposite overflow.
        if ((result == 0) && ((end == 0) || (csq.charAt(i - 1) != '0')))
            throw new NumberFormatException("Invalid integer representation for " + csq.subSequence(start, end));
        if ((result == Long.MIN_VALUE) && !isNegative)
            throw new NumberFormatException("Overflow parsing " + csq.subSequence(start, end));
        cursor.increment(i - start);
        return isNegative ? result : -result;
    }

    /**
     * Parses the whole specified character sequence  
     * as a signed <code>long</code> in the specified radix.
     *
     * @param  csq the character sequence to parse.
     * @param  radix the radix to be used while parsing.
     * @return the corresponding <code>long</code>.
     * @throws NumberFormatException if the specified character sequence
     *         does not contain a parsable <code>long</code> or contains 
     *         extraneous characters.
     */
    public static long parseLong(CharSequence csq, int radix) {
        Cursor cursor = new Cursor();
        long result = parseLong(csq, radix, cursor);
        if (!cursor.atEnd(csq)) 
            throw new IllegalArgumentException("Extraneous characters \"" + cursor.tail(csq) + "\"");
        return result;
    }

    /**
     * Parses the specified character sequence from the specified position 
     * as a signed decimal <code>long</code>.
     *
     * @param  csq the character sequence to parse.
     * @param cursor the cursor position being updated.
     * @return the corresponding <code>long</code>.
     * @throws NumberFormatException if the specified character sequence
     *         does not contain a parsable <code>long</code>.
     */
    public static long parseLong(CharSequence csq, Cursor cursor) {
        return parseLong(csq, 10, cursor);
    }

    /**
     * Parses the whole specified character sequence as a signed decimal 
     * <code>long</code>.
     *
     * @param  csq the character sequence to parse.
     * @return <code>parseLong(csq, 10)</code>
     *  @throws NumberFormatException if the specified character sequence
     *         does not contain a parsable <code>long</code> or contains 
     *         extraneous characters.
     */
    public static long parseLong(CharSequence csq) {
        return parseLong(csq, 10);
    }

    /**
     * Parses the specified character sequence from the specified position 
     * as a <code>float</code>.
     *
     * @param  csq the character sequence to parse.
     * @param cursor the cursor position (being maintained) or
     *        <code>null></code> to parse the whole character sequence.
     * @return the float number represented by the specified character sequence.
     */
    public static float parseFloat(CharSequence csq, Cursor cursor) {
        return (float) parseDouble(csq, cursor);
    }

    /**
     * Parses the whole specified character sequence as a <code>float</code>.
     *
     * @param  csq the character sequence to parse.
     * @return the float number represented by the specified character sequence.
     * @throws NumberFormatException if the specified character sequence
     *         does not contain a parsable <code>long</code> or contains 
     *         extraneous characters.
     */
    public static float parseFloat(CharSequence csq) {
        return (float) parseDouble(csq);
    }

    /**
     * Parses the specified character sequence from the specified position 
     * as a <code>double</code>.
     *
     * @param  csq the character sequence to parse.
     * @param cursor the cursor position (being maintained) or
     *        <code>null></code> to parse the whole character sequence.
     * @return the double number represented by this character sequence.
     * @throws NumberFormatException if the character sequence does not contain
     *         a parsable <code>double</code>.
     */
    public static double parseDouble(CharSequence csq, Cursor cursor)
            throws NumberFormatException {
        final int start = cursor.getIndex();
        final int end = csq.length();
        int i = start;
        char c = csq.charAt(i);

        // Checks for NaN.
        if ((c == 'N') && match("NaN", csq, i, end)) {
            cursor.increment(3);
            return Double.NaN;
        }

        // Reads sign.
        boolean isNegative = (c == '-');
        if ((isNegative || (c == '+')) && (++i < end))
            c = csq.charAt(i);

        // Checks for Infinity.
        if ((c == 'I') && match("Infinity", csq, i, end)) {
            cursor.increment(i + 8 - start);
            return isNegative ? Double.NEGATIVE_INFINITY
                    : Double.POSITIVE_INFINITY;
        }

        // At least one digit or a '.' required.
        if (((c < '0') || (c > '9')) && (c != '.'))
            throw new NumberFormatException("Digit or '.' required");

        // Reads decimal and fraction (both merged to a long).
        long decimal = 0;
        int decimalPoint = -1;
        while (true) {
            int digit = c - '0';
            if ((digit >= 0) && (digit < 10)) {
                long tmp = decimal * 10 + digit;
                if (tmp < decimal)
                    throw new NumberFormatException(
                            "Too many digits - Overflow");
                decimal = tmp;
            } else if ((c == '.') && (decimalPoint < 0))
                decimalPoint = i;
            else
                break;
            if (++i >= end)
                break;
            c = csq.charAt(i);
        }
        if (isNegative)
            decimal = -decimal;
        int fractionLength = (decimalPoint >= 0) ? i - decimalPoint - 1 : 0;

        // Reads exponent.
        int exp = 0;
        if ((i < end) && ((c == 'E') || (c == 'e'))) {
            c = csq.charAt(++i);
            boolean isNegativeExp = (c == '-');
            if ((isNegativeExp || (c == '+')) && (++i < end))
                c = csq.charAt(i);
            if ((c < '0') || (c > '9')) // At least one digit required.  
                throw new NumberFormatException("Invalid exponent");
            while (true) {
                int digit = c - '0';
                if ((digit >= 0) && (digit < 10)) {
                    int tmp = exp * 10 + digit;
                    if (tmp < exp)
                        throw new NumberFormatException("Exponent Overflow");
                    exp = tmp;
                } else
                    break;
                if (++i >= end)
                    break;
                c = csq.charAt(i);
            }
            if (isNegativeExp)
                exp = -exp;
        }
        cursor.increment(i - start);
        return javolution.lang.MathLib.toDoublePow10(decimal, exp - fractionLength);
    }

    /**
     * Parses the whole specified character sequence as a <code>double</code>.
     * The format must be of the form:<code>
     * &lt;decimal&gt;{'.'&lt;fraction&gt;}{'E|e'&lt;exponent&gt;}</code>.
     *
     * @param  csq the character sequence to parse.
     * @return the double number represented by this character sequence.
     * @throws NumberFormatException if the specified character sequence
     *         does not contain a parsable <code>long</code> or contains 
     *         extraneous characters.
     */
    public static double parseDouble(CharSequence csq)
            throws NumberFormatException {
        Cursor cursor = new Cursor();
        double result = parseDouble(csq, cursor);
        if (!cursor.atEnd(csq)) 
            throw new IllegalArgumentException("Extraneous characters \"" + cursor.tail(csq) + "\"");
        return result;
    }

    static boolean match(String str, CharSequence csq, int start, int length) {
        for (int i = 0; i < str.length(); i++) {
            if ((start + i >= length) || csq.charAt(start + i) != str.charAt(i))
                return false;
        }
        return true;
    }

    static boolean match(String str, String csq, int start, int length) {
        for (int i = 0; i < str.length(); i++) {
            if ((start + i >= length) || csq.charAt(start + i) != str.charAt(i))
                return false;
        }
        return true;
    }

    ////////////////
    // FORMATTING //
    ////////////////
    /**
     * Formats the specified <code>boolean</code> and appends the resulting
     * text to the <code>Appendable</code> argument.
     *
     * @param  b a <code>boolean</code>.
     * @param  a the <code>Appendable</code> to append.
     * @return the specified <code>StringBuffer</code> object.
     * @throws IOException if an I/O exception occurs.
     */
    public static Appendable format(boolean b, Appendable a) throws IOException {
        return b ? a.append("true") : a.append("false");
    }
 
    /**
     * Formats the specified <code>int</code> and appends the resulting
     * text (decimal representation) to the <code>Appendable</code> argument.
     *
     *
     * @param  i the <code>int</code> number.
     * @param  a the <code>Appendable</code> to append.
     * @return the specified <code>Appendable</code> object.
     * @throws IOException if an I/O exception occurs.
     */
    public static Appendable format(int i, Appendable a) throws IOException {
        if (a instanceof TextBuilder)
            return ((TextBuilder) a).append(i);
        TextBuilder tb = new TextBuilder();
        tb.append(i);
        return a.append(tb);
    }

    /**
     * Formats the specified <code>int</code> in the specified radix and appends
     * the resulting text to the <code>Appendable</code> argument.
     *
     * @param  i the <code>int</code> number.
     * @param  radix the radix.
     * @param  a the <code>Appendable</code> to append.
     * @return the specified <code>Appendable</code> object.
     * @throws IllegalArgumentException if radix is not in [2 .. 36] range.
     * @throws IOException if an I/O exception occurs.
     */
    public static Appendable format(int i, int radix, Appendable a) throws IOException {
        if (a instanceof TextBuilder)
            return ((TextBuilder) a).append(i, radix);
        TextBuilder tb = new TextBuilder();
        tb.append(i, radix);
        return a.append(tb);
    }

    /**
     * Formats the specified <code>long</code> and appends the resulting
     * text (decimal representation) to the <code>Appendable</code> argument.
     *
     * @param  l the <code>long</code> number.
     * @param  a the <code>Appendable</code> to append.
     * @return the specified <code>Appendable</code> object.
     * @throws IOException if an I/O exception occurs.
     * @see    #parseLong
     */
    public static Appendable format(long l, Appendable a) throws IOException {
        if (a instanceof TextBuilder)
            return ((TextBuilder) a).append(l);
        TextBuilder tb = new TextBuilder();
        tb.append(l);
        return a.append(tb);
    }

    /**
     * Formats the specified <code>long</code> in the specified radix and
     * appends the resulting text to the <code>Appendable</code> argument.
     *
     * @param  l the <code>long</code> number.
     * @param  radix the radix.
     * @param  a the <code>Appendable</code> to append.
     * @return the specified <code>Appendable</code> object.
     * @throws  IllegalArgumentException if radix is not in [2 .. 36] range.
     * @throws IOException if an I/O exception occurs.
     * @see    #parseLong(CharSequence, int)
     */
    public static Appendable format(long l, int radix, Appendable a)
            throws IOException {
        if (a instanceof TextBuilder)
            return ((TextBuilder) a).append(l, radix);
        TextBuilder tb = new TextBuilder();
        tb.append(l, radix);
        return a.append(tb);
    }

    /**
     * Formats the specified <code>float</code> value.
     *
     * @param  f the <code>float</code> value.
     * @param  a the <code>Appendable</code> to append.
     * @return <code>TypeFormat.format(f, 10, (MathLib.abs(f) >= 1E7) || (MathLib.abs(f) < 0.001), false, a)</code>
     * @throws IOException if an I/O exception occurs.
     */
    public static Appendable format(float f, Appendable a) throws IOException {
        return TypeFormat.format(f, 10, (MathLib.abs(f) >= 1E7) || (MathLib.abs(f) < 0.001), false, a);
    }

    /**
     * Formats the specified <code>double</code> value (16 or 17 digits output).
     *
     * @param  d the <code>double</code> value.
     * @param  a the <code>Appendable</code> to append.
     * @return <code>TypeFormat.format(d, -1, (MathLib.abs(d) >= 1E7) || (MathLib.abs(d) < 0.001), false, a)</code>
     * @throws IOException if an I/O exception occurs.
     * @see    TextBuilder#append(double)
     */
    public static Appendable format(double d, Appendable a) throws IOException {
        return TypeFormat.format(d, -1, (MathLib.abs(d) >= 1E7) || (MathLib.abs(d) < 0.001), false, a);
    }

    /**
     * Formats the specified <code>double</code> value according to the
     * specified formatting arguments.
     *
     * @param  d the <code>double</code> value.
     * @param  digits the number of significative digits (excludes exponent) or
     *         <code>-1</code> to mimic the standard library (16 or 17 digits).
     * @param  scientific <code>true</code> to forces the use of the scientific
     *         notation (e.g. <code>1.23E3</code>); <code>false</code>
     *         otherwise.
     * @param  showZero <code>true</code> if trailing fractional zeros are
     *         represented; <code>false</code> otherwise.
     * @param  a the <code>Appendable</code> to append.
     * @return the specified <code>Appendable</code> object.
     * @throws IllegalArgumentException if <code>(digits &gt; 19)</code>)
     * @throws IOException if an I/O exception occurs.
     * @see    TextBuilder#append(double, int, boolean, boolean)
     */
    public static Appendable format(double d, int digits, boolean scientific,
            boolean showZero, Appendable a) throws IOException {
        if (a instanceof TextBuilder)
            return ((TextBuilder) a).append(d, digits, scientific, showZero);
        TextBuilder tb = new TextBuilder();
        tb.append(d, digits, scientific, showZero);
        return a.append(tb);
    }

}