/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2005 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.lang;

import j2me.lang.CharSequence;
import javolution.lang.TextFormat.Cursor;
import java.io.IOException;

/**
 * <p> This class provides utility methods to parse <code>CharSequence</code>
 *     into primitive types and to format primitive types into an 
 *     <code>Appendable</code>.</p>
 *
 * <p> Methods from this class <b>do not create temporary objects</b>
 *     and are typically faster than standard library methods (e.g
 *     <code>TypeFormat#parseDouble</code> is up to 15x faster than 
 *     <code>Double.parseDouble</code>).</p>
 *
 * <p> For non-primitive objects, formatting is typically performed using 
 *     specialized {@link TextFormat} instances.</p>
 * 
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 3.7, January 1, 2006
 */
public final class TypeFormat {

    /**
     * Holds the digits to character mapping. 
     */
    private final static char[] DIGIT_TO_CHAR = { '0', '1', '2', '3', '4', '5',
            '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i',
            'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v',
            'w', 'x', 'y', 'z' };

    /**
     * Holds the character to digit mapping. 
     */
    private final static int[] CHAR_TO_DIGIT = new int[128];
    static {
        for (int i = 0; i < CHAR_TO_DIGIT.length; i++) {
            CHAR_TO_DIGIT[i] = -1;
        }
        for (int i = 0; i < DIGIT_TO_CHAR.length; i++) {
            CHAR_TO_DIGIT[DIGIT_TO_CHAR[i]] = i;
            CHAR_TO_DIGIT[Character.toUpperCase(DIGIT_TO_CHAR[i])] = i;
        }
    }

    /**
     * Default constructor (forbids derivation).
     */
    private TypeFormat() {
    }

    /**
     * Parses the specified character sequence as a <code>boolean</code>.
     *
     * @param  csq the character sequence to parse.
     * @return the corresponding boolean value.
     * @throws IllegalArgumentException if the specified character sequence 
     *         is different from "true" or "false" ignoring cases.
     */
    public static boolean parseBoolean(CharSequence csq) {
        if ((csq.length() == 4)
                && (csq.charAt(0) == 't' || csq.charAt(0) == 'T')
                && (csq.charAt(1) == 'r' || csq.charAt(1) == 'R')
                && (csq.charAt(2) == 'u' || csq.charAt(2) == 'U')
                && (csq.charAt(3) == 'e' || csq.charAt(3) == 'E')) {
            return true;
        } else if ((csq.length() == 5)
                && (csq.charAt(0) == 'f' || csq.charAt(0) == 'F')
                && (csq.charAt(1) == 'a' || csq.charAt(1) == 'A')
                && (csq.charAt(2) == 'l' || csq.charAt(2) == 'L')
                && (csq.charAt(3) == 's' || csq.charAt(3) == 'S')
                && (csq.charAt(4) == 'e' || csq.charAt(4) == 'E')) {
            return false;
        }
        throw new IllegalArgumentException("Cannot parse " + csq
                + " as boolean");
    }

    /**
     * Parses the specified character sequence from the specified position 
     * as a <code>boolean</code>.
     *
     * @param csq the character sequence to parse.
     * @param cursor the current cursor position (being maintained).
     * @return the next boolean value.
     * @throws IllegalArgumentException if the character sequence from the 
     *         specified position is different from "true" or "false" ignoring
     *         cases.
     */
    public static boolean parseBoolean(CharSequence csq, Cursor cursor) {
        final int i = cursor.getIndex();
        if ((csq.length() > i + 4)
                && (csq.charAt(i) == 't' || csq.charAt(i) == 'T')
                && (csq.charAt(i + 1) == 'r' || csq.charAt(i + 1) == 'R')
                && (csq.charAt(i + 2) == 'u' || csq.charAt(i + 2) == 'U')
                && (csq.charAt(i + 3) == 'e' || csq.charAt(i + 3) == 'E')) {
            cursor.increment(4);
            return true;
        }
        if ((csq.length() > i + 5)
                && (csq.charAt(i) == 'f' || csq.charAt(i) == 'F')
                && (csq.charAt(i + 1) == 'a' || csq.charAt(i + 1) == 'A')
                && (csq.charAt(i + 2) == 'l' || csq.charAt(i + 2) == 'L')
                && (csq.charAt(i + 3) == 's' || csq.charAt(i + 3) == 'S')
                && (csq.charAt(i + 4) == 'e' || csq.charAt(i + 4) == 'E')) {
            cursor.increment(5);
            return true;
        }
        throw new IllegalArgumentException("Cannot parse boolean at "
                + cursor.getIndex());
    }

    /**
     * Parses the specified character sequence as a signed decimal 
     * <code>byte</code>.
     *
     * @param  csq the character sequence to parse.
     * @return <code>parseByte(csq, 10)</code>
     * @throws NumberFormatException if the specified character sequence
     *         does not contain a parsable <code>byte</code>.
     * @see    #parseByte(CharSequence, int)
     */
    public static byte parseByte(CharSequence csq) {
        return parseByte(csq, 10);
    }

    /**
     * Parses the specified character sequence as a signed <code>byte</code> 
     * in the specified radix.
     *
     * @param  csq the character sequence to parse.
     * @param  radix the radix to be used while parsing.
     * @return the corresponding <code>byte</code>.
     * @throws NumberFormatException if the specified character sequence
     *         does not contain a parsable <code>byte</code>.
     */
    public static byte parseByte(CharSequence csq, int radix) {
        int i = parseInt(csq, radix);
        if ((i < Byte.MIN_VALUE) || (i > Byte.MAX_VALUE))
            throw new NumberFormatException("Overflow");
        return (byte) i;
    }

    /**
     * Parses the specified character sequence from the specified position 
     * as a signed <code>byte</code> in the specified radix.
     *
     * @param  csq the character sequence to parse.
     * @param  radix the radix to be used while parsing.
     * @param cursor the current cursor position (being maintained).
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
     * Parses the specified character sequence as a signed decimal 
     * <code>short</code>.
     *
     * @param  csq the character sequence to parse.
     * @return <code>parseShort(csq, 10)</code>
     * @throws NumberFormatException if the specified character sequence
     *         does not contain a parsable <code>short</code>.
     * @see    #parseShort(CharSequence, int)
     */
    public static short parseShort(CharSequence csq) {
        return parseShort(csq, 10);
    }

    /**
     * Parses the specified character sequence as a signed <code>short</code> 
     * in the specified radix.
     *
     * @param  csq the character sequence to parse.
     * @param  radix the radix to be used while parsing.
     * @return the corresponding <code>short</code>.
     * @throws NumberFormatException if the specified character sequence
     *         does not contain a parsable <code>short</code>.
     */
    public static short parseShort(CharSequence csq, int radix) {
        int i = parseInt(csq, radix);
        if ((i < Short.MIN_VALUE) || (i > Short.MAX_VALUE))
            throw new NumberFormatException("Overflow");
        return (short) i;
    }

    /**
     * Parses the specified character sequence from the specified position 
     * as a signed <code>short</code> in the specified radix.
     *
     * @param  csq the character sequence to parse.
     * @param  radix the radix to be used while parsing.
     * @param cursor the current cursor position (being maintained).
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
     * Parses the specified character sequence as a signed <code>int</code>.
     *
     * @param  csq the character sequence to parse.
     * @return <code>parseInt(csq, 10)</code>
     * @throws NumberFormatException if the specified character sequence
     *         does not contain a parsable <code>int</code>.
     * @see    #parseInt(CharSequence, int)
     */
    public static int parseInt(CharSequence csq) {
        return parseInt(csq, 10);
    }

    /**
     * Parses the specified character sequence as a signed <code>int</code> 
     * in the specified radix.
     *
     * @param  csq the character sequence to parse.
     * @param  radix the radix to be used while parsing.
     * @return the corresponding <code>int</code>.
     * @throws NumberFormatException if the specified character sequence
     *         does not contain a parsable <code>int</code>.
     */
    public static int parseInt(CharSequence csq, int radix) {
        try {
            int length = csq.length();
            int i = 0;
            boolean isNegative = (csq.charAt(i) == '-') ? true : false;
            i += (isNegative || (csq.charAt(i) == '+')) ? 1 : 0;
            char c0 = csq.charAt(i);
            int digit = (c0 < CHAR_TO_DIGIT.length) ? CHAR_TO_DIGIT[c0] : -1;
            if ((digit < 0) || (digit >= radix))
                throw new NumberFormatException("Digit expected");
            int result = -digit; // Accumulates negatively.
            while (true) {
                if (++i >= length)
                    break;
                final char c = csq.charAt(i);
                digit = (c < CHAR_TO_DIGIT.length) ? CHAR_TO_DIGIT[c] : -1;
                if ((digit < 0) || (digit >= radix))
                    break;
                if (result <= Integer.MIN_VALUE / 36) { // Checks for potential overflow.
                    if (result < (Integer.MIN_VALUE + digit) / radix)
                        throw new NumberFormatException("Overflow");
                }
                result = result * radix - digit;
            }
            if (!isNegative && (result == Integer.MIN_VALUE)) // Negation overflow. 
                throw new NumberFormatException("Overflow");
            return isNegative ? result : -result;
        } catch (IndexOutOfBoundsException e) {
            throw new NumberFormatException();
        }
    }

    /**
     * Parses the specified character sequence from the specified position 
     * as a signed <code>int</code> in the specified radix.
     *
     * @param  csq the character sequence to parse.
     * @param  radix the radix to be used while parsing.
     * @param  cursor the current cursor position (being maintained).
     * @return the corresponding <code>int</code>.
     * @throws NumberFormatException if the specified character sequence
     *         does not contain a parsable <code>int</code>.
     */
    public static int parseInt(CharSequence csq, int radix, Cursor cursor) {
        try {
            int length = csq.length();
            int i = cursor.getIndex();
            boolean isNegative = (csq.charAt(i) == '-') ? true : false;
            i += (isNegative || (csq.charAt(i) == '+')) ? 1 : 0;
            char c0 = csq.charAt(i);
            int digit = (c0 < CHAR_TO_DIGIT.length) ? CHAR_TO_DIGIT[c0] : -1;
            if ((digit < 0) || (digit >= radix))
                throw new NumberFormatException("Digit expected");
            int result = -digit; // Accumulates negatively.
            while (true) {
                if (++i >= length)
                    break;
                final char c = csq.charAt(i);
                digit = (c < CHAR_TO_DIGIT.length) ? CHAR_TO_DIGIT[c] : -1;
                if ((digit < 0) || (digit >= radix))
                    break;
                if (result <= Integer.MIN_VALUE / 36) { // Checks for potential overflow.
                    if (result < (Integer.MIN_VALUE + digit) / radix)
                        throw new NumberFormatException("Overflow");
                }
                result = result * radix - digit;
            }
            if (!isNegative && (result == Integer.MIN_VALUE)) // Negation overflow. 
                throw new NumberFormatException("Overflow");
            cursor.setIndex(i);
            return isNegative ? result : -result;
        } catch (IndexOutOfBoundsException e) {
            throw new NumberFormatException();
        }
    }

    /**
     * Parses the specified character sequence as a decimal <code>long</code>.
     *
     * @param  csq the character sequence to parse.
     * @return <code>parseLong(csq, 10)</code>
     * @throws NumberFormatException if the specified character sequence
     *         does not contain a parsable <code>long</code>.
     * @see    #parseLong(CharSequence, int)
     */
    public static long parseLong(CharSequence csq) {
        return parseLong(csq, 10);
    }

    /**
     * Parses the specified character sequence as a signed <code>long</code>
     * in the specified radix.
     *
     * @param  csq the character sequence to parse.
     * @param  radix the radix to be used while parsing.
     * @return the corresponding <code>long</code>.
     * @throws NumberFormatException if the specified character sequence
     *         does not contain a parsable <code>long</code>.
     */
    public static long parseLong(CharSequence csq, int radix) {
        try {
            int length = csq.length();
            int i = 0;
            boolean isNegative = (csq.charAt(i) == '-') ? true : false;
            i += (isNegative || (csq.charAt(i) == '+')) ? 1 : 0;
            char c0 = csq.charAt(i);
            int digit = (c0 < CHAR_TO_DIGIT.length) ? CHAR_TO_DIGIT[c0] : -1;
            if ((digit < 0) || (digit >= radix))
                throw new NumberFormatException("Digit expected");
            long result = -digit; // Accumulates negatively.
            while (true) {
                if (++i >= length)
                    break;
                final char c = csq.charAt(i);
                digit = (c < CHAR_TO_DIGIT.length) ? CHAR_TO_DIGIT[c] : -1;
                if ((digit < 0) || (digit >= radix))
                    break;
                if (result <= Long.MIN_VALUE / 36) { // Checks for potential overflow.
                    if (result < (Long.MIN_VALUE + digit) / radix)
                        throw new NumberFormatException("Overflow");
                }
                result = result * radix - digit;
            }
            if (!isNegative && (result == Long.MIN_VALUE)) // Negation overflow. 
                throw new NumberFormatException("Overflow");
            return isNegative ? result : -result;
        } catch (IndexOutOfBoundsException e) {
            throw new NumberFormatException();
        }
    }

    /**
     * Parses the specified character sequence from the specified position 
     * as a signed <code>long</code> in the specified radix.
     *
     * @param  csq the character sequence to parse.
     * @param  radix the radix to be used while parsing.
     * @param  cursor the current cursor position (being maintained).
     * @return the corresponding <code>long</code>.
     * @throws NumberFormatException if the specified character sequence
     *         does not contain a parsable <code>long</code>.
     */
    public static long parseLong(CharSequence csq, int radix, Cursor cursor) {
        try {
            int length = csq.length();
            int i = cursor.getIndex();
            boolean isNegative = (csq.charAt(i) == '-') ? true : false;
            i += (isNegative || (csq.charAt(i) == '+')) ? 1 : 0;
            char c0 = csq.charAt(i);
            int digit = (c0 < CHAR_TO_DIGIT.length) ? CHAR_TO_DIGIT[c0] : -1;
            if ((digit < 0) || (digit >= radix))
                throw new NumberFormatException("Digit expected");
            long result = -digit; // Accumulates negatively.
            while (true) {
                if (++i >= length)
                    break;
                final char c = csq.charAt(i);
                digit = (c < CHAR_TO_DIGIT.length) ? CHAR_TO_DIGIT[c] : -1;
                if ((digit < 0) || (digit >= radix))
                    break;
                if (result <= Long.MIN_VALUE / 36) { // Checks for potential overflow.
                    if (result < (Long.MIN_VALUE + digit) / radix)
                        throw new NumberFormatException("Overflow");
                }
                result = result * radix - digit;
            }
            if (!isNegative && (result == Long.MIN_VALUE)) // Negation overflow. 
                throw new NumberFormatException("Overflow");
            cursor.setIndex(i);
            return isNegative ? result : -result;
        } catch (IndexOutOfBoundsException e) {
            throw new NumberFormatException();
        }
    }

    /**
     * Parses the specified character sequence as a <code>float</code>.
     *
     * @param  csq the character sequence to parse.
     * @return the float number represented by the specified character sequence.
     * @throws NumberFormatException if the character sequence does not contain
     *         a parsable <code>float</code>.
     /*@FLOATING_POINT@
    public static float parseFloat(CharSequence csq) {
        double d = parseDouble(csq);
        if ((d >= -Float.MAX_VALUE) && (d <= Float.MAX_VALUE)) {
            return (float) d;
        } else {
            throw new NumberFormatException("Overflow");
        }
    }
    /**/

    /**
     * Parses the specified character sequence from the specified position 
     * as a <code>float</code>.
     *
     * @param  csq the character sequence to parse.
     * @param  cursor the current cursor position (being maintained).
     * @return the float number represented by the specified character sequence.
     * @throws NumberFormatException if the character sequence does not contain
     *         a parsable <code>float</code>.
     /*@FLOATING_POINT@
    public static float parseFloat(CharSequence csq, Cursor cursor) {
        double d = parseDouble(csq, cursor);
        if ((d >= -Float.MAX_VALUE) && (d <= Float.MAX_VALUE)) {
            return (float) d;
        } else {
            throw new NumberFormatException("Overflow");
        }
    }
    /**/

    /**
     * Parses the specified character sequence as a <code>double</code>.
     *
     * @param  csq the character sequence to parse.
     * @return the double number represented by this character sequence.
     * @throws NumberFormatException if the character sequence does not contain
     *         a parsable <code>double</code>.
     /*@FLOATING_POINT@
    public static double parseDouble(CharSequence csq)
            throws NumberFormatException {
        Cursor cursor = Cursor.newInstance();
        double result = TypeFormat.parseDouble(csq, cursor);
        cursor.recycle();
        return result;
    }
    /**/

    /**
     * Parses the specified character sequence from the specified position 
     * as a <code>double</code>.
     *
     * @param  csq the character sequence to parse.
     * @param  cursor the current cursor position (being maintained).
     * @return the double number represented by this character sequence.
     * @throws NumberFormatException if the character sequence does not contain
     *         a parsable <code>double</code>.
     /*@FLOATING_POINT@
    public static double parseDouble(CharSequence csq, Cursor cursor)
            throws NumberFormatException {
        try {
            int i = cursor.getIndex();
            // Reads the sign now (which might not be the sign of the decimal part, e.g. -0.XXX).
            boolean isNegative = (csq.charAt(i) == '-') ? true : false;
            i += (isNegative || (csq.charAt(i) == '+')) ? 1 : 0;
            char c0 = csq.charAt(i);
            if ((c0 == 'N') && startWith(csq, i, "NaN")) {
                cursor.setIndex(i + 3);
                return Double.NaN;
            }
            if ((c0 == 'I') && startWith(csq, i, "Infinity")) {
                cursor.setIndex(i + 8);
                return isNegative ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
            }
            cursor.setIndex(i);
            long decimal = parseLong(csq, 10, cursor);
            final int length = csq.length();

            // Reads fraction.
            long fraction = 0;
            int fractionLength = 0;
            if (cursor.getIndex() < length) {
                char c = csq.charAt(cursor.getIndex());
                if (c == '.') {
                    cursor.increment();
                    int startFraction = cursor.getIndex();
                    fraction = TypeFormat.parseLong(csq, 10, cursor);
                    fractionLength = cursor.getIndex() - startFraction;
                }
            }

            // Reads exponent.
            int exponent = 0;
            if (cursor.getIndex() < length) {
                char c = csq.charAt(cursor.getIndex());
                if ((c == 'e') || (c == 'E')) {
                    cursor.increment();
                    exponent = TypeFormat.parseInt(csq, 10, cursor);
                }
            }

            // Adds decimal and fraction part.  
            double decimalAsDouble = multE(decimal, exponent);
            double fractionAsDouble = multE(decimal < 0 ? -fraction : fraction,
                exponent - fractionLength);
            
            return isNegative ? - decimalAsDouble - fractionAsDouble :
                 decimalAsDouble + fractionAsDouble;
            
        } catch (IndexOutOfBoundsException e) {
            throw new NumberFormatException();
        }
    }
    /**/

    static boolean startWith(CharSequence csq, int start, String str) {
        for (int i = 0; i < str.length(); i++) {
            if ((start + i >= csq.length())
                    || csq.charAt(start + i) != str.charAt(i))
                return false;
        }
        return true;
    }

    /**
     * Formats the specified <code>boolean</code> and appends the resulting
     * text to the <code>Appendable</code> argument.
     *
     * @param  b a <code>boolean</code>.
     * @param  csq the <code>Appendable</code> to append.
     * @return the specified <code>StringBuffer</code> object.
     * @throws IOException if an I/O exception occurs.
     * @see    #parseBoolean
     */
    public static Appendable format(boolean b, Appendable csq)
            throws IOException {
        return b ? append(csq, "true") : append(csq, "false");
    }

    /**
     * Formats the specified <code>int</code> and appends the resulting
     * text (decimal representation) to the <code>Appendable</code> argument.
     *
     * <p> Note: This method is preferred to <code>Appendable.append(int)
     *           </code> as it does not create temporary <code>String</code>
     *           objects (several times faster for small numbers).</p>
     *
     * @param  i the <code>int</code> number.
     * @param  csq the <code>Appendable</code> to append.
     * @return the specified <code>Appendable</code> object.
     * @throws IOException if an I/O exception occurs.
     * @see    #parseInt
     */
    public static Appendable format(int i, Appendable csq) throws IOException {
        if (i <= 0) {
            if (i == Integer.MIN_VALUE) { // Negation would overflow.
                return append(csq, "-2147483648"); // 11 char max.
            } else if (i == 0) {
                return csq.append('0');
            }
            i = -i;
            csq.append('-');
        }
        int j = 1;
        for (; (j < 10) && (i >= INT_POW_10[j]); j++) {
        }
        // POW_10[j] > i >= POW_10[j-1]
        for (j--; j >= 0; j--) {
            int pow10 = INT_POW_10[j];
            int digit = i / pow10;
            i -= digit * pow10;
            csq.append(DIGIT_TO_CHAR[digit]);
        }
        return csq;
    }

    private static final int[] INT_POW_10 = new int[10];
    static {
        int pow = 1;
        for (int i = 0; i < 10; i++) {
            INT_POW_10[i] = pow;
            pow *= 10;
        }
    }

    /**
     * Formats the specified <code>int</code> in the specified radix and appends
     * the resulting text to the <code>Appendable</code> argument.
     *
     * @param  i the <code>int</code> number.
     * @param  radix the radix.
     * @param  csq the <code>Appendable</code> to append.
     * @return the specified <code>Appendable</code> object.
     * @throws  IllegalArgumentException if radix is not in [2 .. 36] range.
     * @throws IOException if an I/O exception occurs.
     * @see    #parseInt(CharSequence, int)
     */
    public static Appendable format(int i, int radix, Appendable csq)
            throws IOException {
        if (radix == 10) {
            return format(i, csq); // Faster version.
        } else if (radix < 2 || radix > 36) {
            throw new IllegalArgumentException("radix: " + radix);
        }
        if (i < 0) {
            csq.append('-');
        } else {
            i = -i;
        }
        format2(i, radix, csq);
        return csq;
    }

    private static void format2(int i, int radix, Appendable csq)
            throws IOException {
        if (i <= -radix) {
            format2(i / radix, radix, csq);
            csq.append(DIGIT_TO_CHAR[-(i % radix)]);
        } else {
            csq.append(DIGIT_TO_CHAR[-i]);
        }
    }

    /**
     * Formats the specified <code>long</code> and appends the resulting
     * text (decimal representation) to the <code>Appendable</code> argument.
     *
     * <p> Note: This method is preferred to <code>Appendable.append(long)
     *           </code> as it does not create temporary <code>String</code>
     *           objects (several times faster for small numbers).</p>
     *
     * @param  l the <code>long</code> number.
     * @param  csq the <code>Appendable</code> to append.
     * @return the specified <code>Appendable</code> object.
     * @throws IOException if an I/O exception occurs.
     * @see    #parseLong
     */
    public static Appendable format(long l, Appendable csq) throws IOException {
        if (l <= 0) {
            if (l == Long.MIN_VALUE) { // Negation would overflow.
                return append(csq, "-9223372036854775808"); // 20 characters max.
            } else if (l == 0) {
                return csq.append('0');
            }
            l = -l;
            csq.append('-');
        }
        int j = 1;
        for (; (j < 19) && (l >= LONG_POW_10[j]); j++) {
        }
        // POW_10[j] > l >= POW_10[j-1]
        for (j--; j >= 0; j--) {
            long pow10 = LONG_POW_10[j];
            int digit = (int) (l / pow10);
            l -= digit * pow10;
            csq.append(DIGIT_TO_CHAR[digit]);
        }
        return csq;
    }

    private static final long[] LONG_POW_10 = new long[19];
    static {
        long pow = 1;
        for (int i = 0; i < 19; i++) {
            LONG_POW_10[i] = pow;
            pow *= 10;
        }
    }

    /**
     * Formats the specified <code>long</code> in the specified radix and
     * appends the resulting text to the <code>Appendable</code> argument.
     *
     * @param  l the <code>long</code> number.
     * @param  radix the radix.
     * @param  csq the <code>Appendable</code> to append.
     * @return the specified <code>Appendable</code> object.
     * @throws  IllegalArgumentException if radix is not in [2 .. 36] range.
     * @throws IOException if an I/O exception occurs.
     * @see    #parseLong(CharSequence, int)
     */
    public static Appendable format(long l, int radix, Appendable csq)
            throws IOException {
        if (radix == 10) {
            return format(l, csq); // Faster version.
        } else if (radix < 2 || radix > 36) {
            throw new IllegalArgumentException("radix: " + radix);
        }
        if (l < 0) {
            csq.append('-');
        } else {
            l = -l;
        }
        format2(l, radix, csq);
        return csq;
    }

    private static void format2(long l, int radix, Appendable csq)
            throws IOException {
        if (l <= -radix) {
            format2(l / radix, radix, csq);
            csq.append(DIGIT_TO_CHAR[(int) -(l % radix)]);
        } else {
            csq.append(DIGIT_TO_CHAR[(int) -l]);
        }
    }

    /**
     * Formats the specified <code>float</code> value.
     *
     * @param  value the <code>float</code> value.
     * @param  csq the <code>Appendable</code> to append.
     * @return <code>format(value, 10, Math.abs(value) > 1E7, false, sb)</code>
     * @throws IOException if an I/O exception occurs.
     * @see    #format(double, int , boolean, boolean, Appendable)
     /*@FLOATING_POINT@
     public static Appendable format(float value, Appendable csq)
     throws IOException {
     return format(value, 10, MathLib.abs(value) > 1E7, false, csq);
     }
     /**/

    /**
     * Formats the specified <code>double</code> value.
     *
     * <p> Note : This method is preferred to <code>Double.toString(double)
     *            </code> or even <code>String.valueOf(double)</code> as it
     *            does not create temporary <code>String</code> or <code>
     *            FloatingDecimal</code> objects (several times faster,
     *            e.g. 15x faster for <code>Double.MAX_VALUE</code>).</p>
     *
     * @param  value the <code>double</code> value.
     * @param  csq the <code>Appendable</code> to append.
     * @return <code>format(value, 17, Math.abs(value) > 1E7, false, sb)</code>
     * @throws IOException if an I/O exception occurs.
     * @see    #format(double, int , boolean, boolean, Appendable)
     /*@FLOATING_POINT@
     public static Appendable format(double value, Appendable csq)
     throws IOException {
     return format(value, 17, MathLib.abs(value) > 1E7, false, csq);
     }
     /**/

    /**
     * Formats the specified <code>double</code> value according to the 
     * specified formatting arguments.
     *
     * @param  value the <code>double</code> value.
     * @param  digits the number of significative digits (excludes exponent).
     * @param  scientific <code>true</code> to forces the use of the scientific 
     *         notation (e.g. <code>1.23E3</code>); <code>false</code> 
     *         otherwise. 
     * @param  showZero <code>true</code> if trailing fractional zeros are 
     *         represented; <code>false</code> otherwise.
     * @param  csq the <code>Appendable</code> to append.
     * @return the specified <code>Appendable</code> object.
     * @throws IllegalArgumentException if <code>((digits > 19) || 
     *         (digits <= 0))</code>)
     * @throws IOException if an I/O exception occurs.
     /*@FLOATING_POINT@
     public static Appendable format(double value, int digits, 
     boolean scientific, boolean showZero, Appendable csq) throws IOException {
     if ((digits > 19) || (digits <= 0))
     throw new IllegalArgumentException("digits: " + digits);
     if (value != value) { // NaN
     return append(csq, "NaN");
     } else if (value == POSITIVE_INFINITY) {
     return append(csq, "Infinity");
     } else if (value == NEGATIVE_INFINITY) {
     return append(csq, "-Infinity");
     } else if (value == 0.0) {
     if (digits == 1)
     return append(csq, "0");
     if (!showZero)
     return append(csq, "0.0");
     append(csq, "0.0");
     for (int i = 2; i < digits; i++) {
     csq.append('0');
     }
     return csq;
     }
     if (value < 0) {
     value = -value;
     csq.append('-');
     }
     // Find the exponent e such as: value == 0.xxx * 10^e
     int e = (value >= 1.0) ? 1 + minPow10(value) : - minPow10(1.0 / value);
     double digitValue = multE(value, digits - e);
     long mantissa = (long) (digitValue + 0.5);
     if (scientific || (e <= 0) || (e > digits)) {
     // Scientific notation has to be used ("x.xxxEyy").
     format(mantissa / LONG_POW_10[digits - 1], csq);
     formatFraction(mantissa % LONG_POW_10[digits - 1], digits - 1,
     showZero, csq);
     csq.append('E');
     format(e - 1, csq);
     } else if (e == digits) { // Dot at last position ("xxxxx").
     format(mantissa, csq);
     } else { // Dot within the string ("xxxx.xxxxx").
     format(mantissa / LONG_POW_10[digits - e], csq);
     formatFraction(mantissa % LONG_POW_10[digits - e], digits
     - e, showZero, csq);
     }
     return csq;
     }


     private static final double POSITIVE_INFINITY = 1.0 / 0.0;

     private static final double NEGATIVE_INFINITY = -1.0 / 0.0;

     private static void formatFraction(long fraction, int digits,
     boolean showZero, Appendable csq) throws IOException {
     if (digits == 0)
     return;
     csq.append('.');
     for (int i = digits; i > 0;) {
     long pow10 = LONG_POW_10[--i];
     int digit = (int) (fraction / pow10);
     fraction -= digit * pow10;
     csq.append(DIGIT_TO_CHAR[digit]);
     if ((fraction == 0) && !showZero) {
     return; // No more than one trailing zero.
     }
     }
     }
     // Returns e such as 10^e <= value < 10^(e+1), value >= 1.0
     private static int minPow10(double value) {
     int minE = 0;
     int maxE = DOUBLE_POW_10.length;
     while (maxE - minE > 1) {
     final int exp = (minE + maxE) >> 1;
     if (value >= DOUBLE_POW_10[exp]) {
     minE = exp;
     } else {
     maxE = exp;
     }
     }
     return minE;
     }
     /**/

    /**
     * Appends the specified string argument to the specified appendable
     * for backward compatibility when {@link String} was not a 
     * {@link CharSequence}.
     *
     * @param  to the appendable.
     * @param  str the string to append.
     * @return the specified appendable
     * @throws IOException if an I/O exception occurs.
     */
    private static Appendable append(Appendable to, String str)
            throws IOException {
        for (int i = 0; i < str.length(); i++) {
            to.append(str.charAt(i));
        }
        return to;
    }

    /**
     * Returns the product of the specified value with <code>10</code> raised
     * at the specified power exponent.
     *
     * @param  value the value.
     * @param  E the exponent.
     * @return <code>value * 10^E</code>
     /*@FLOATING_POINT@
    private static double multE(double value, int E) {
        if (E >= 0) {
            if (E <= 308) {
                // Max: 1.7976931348623157E+308
                return value * DOUBLE_POW_10[E];
            } else {
                value *= 1E308;
                E = Math.min(308, E - 308);
                return value * DOUBLE_POW_10[E];
            }
        } else {
            if (E >= -308) {
                return value / DOUBLE_POW_10[-E];
            } else {
                value /= 1E308; 
                E = Math.max(-308, E + 308);
                return value / DOUBLE_POW_10[-E];
            }
        }
    }

    // Note: Approximation for exponents > 21. This may introduce round-off
    //       errors (e.g. 1E23 represented as "9.999999999999999E22").
    private static final double[] DOUBLE_POW_10 = new double[] { 1E000, 1E001,
            1E002, 1E003, 1E004, 1E005, 1E006, 1E007, 1E008, 1E009, 1E010,
            1E011, 1E012, 1E013, 1E014, 1E015, 1E016, 1E017, 1E018, 1E019,
            1E020, 1E021, 1E022, 1E023, 1E024, 1E025, 1E026, 1E027, 1E028,
            1E029, 1E030, 1E031, 1E032, 1E033, 1E034, 1E035, 1E036, 1E037,
            1E038, 1E039, 1E040, 1E041, 1E042, 1E043, 1E044, 1E045, 1E046,
            1E047, 1E048, 1E049, 1E050, 1E051, 1E052, 1E053, 1E054, 1E055,
            1E056, 1E057, 1E058, 1E059, 1E060, 1E061, 1E062, 1E063, 1E064,
            1E065, 1E066, 1E067, 1E068, 1E069, 1E070, 1E071, 1E072, 1E073,
            1E074, 1E075, 1E076, 1E077, 1E078, 1E079, 1E080, 1E081, 1E082,
            1E083, 1E084, 1E085, 1E086, 1E087, 1E088, 1E089, 1E090, 1E091,
            1E092, 1E093, 1E094, 1E095, 1E096, 1E097, 1E098, 1E099,

            1E100, 1E101, 1E102, 1E103, 1E104, 1E105, 1E106, 1E107, 1E108,
            1E109, 1E110, 1E111, 1E112, 1E113, 1E114, 1E115, 1E116, 1E117,
            1E118, 1E119, 1E120, 1E121, 1E122, 1E123, 1E124, 1E125, 1E126,
            1E127, 1E128, 1E129, 1E130, 1E131, 1E132, 1E133, 1E134, 1E135,
            1E136, 1E137, 1E138, 1E139, 1E140, 1E141, 1E142, 1E143, 1E144,
            1E145, 1E146, 1E147, 1E148, 1E149, 1E150, 1E151, 1E152, 1E153,
            1E154, 1E155, 1E156, 1E157, 1E158, 1E159, 1E160, 1E161, 1E162,
            1E163, 1E164, 1E165, 1E166, 1E167, 1E168, 1E169, 1E170, 1E171,
            1E172, 1E173, 1E174, 1E175, 1E176, 1E177, 1E178, 1E179, 1E180,
            1E181, 1E182, 1E183, 1E184, 1E185, 1E186, 1E187, 1E188, 1E189,
            1E190, 1E191, 1E192, 1E193, 1E194, 1E195, 1E196, 1E197, 1E198,
            1E199,

            1E200, 1E201, 1E202, 1E203, 1E204, 1E205, 1E206, 1E207, 1E208,
            1E209, 1E210, 1E211, 1E212, 1E213, 1E214, 1E215, 1E216, 1E217,
            1E218, 1E219, 1E220, 1E221, 1E222, 1E223, 1E224, 1E225, 1E226,
            1E227, 1E228, 1E229, 1E230, 1E231, 1E232, 1E233, 1E234, 1E235,
            1E236, 1E237, 1E238, 1E239, 1E240, 1E241, 1E242, 1E243, 1E244,
            1E245, 1E246, 1E247, 1E248, 1E249, 1E250, 1E251, 1E252, 1E253,
            1E254, 1E255, 1E256, 1E257, 1E258, 1E259, 1E260, 1E261, 1E262,
            1E263, 1E264, 1E265, 1E266, 1E267, 1E268, 1E269, 1E270, 1E271,
            1E272, 1E273, 1E274, 1E275, 1E276, 1E277, 1E278, 1E279, 1E280,
            1E281, 1E282, 1E283, 1E284, 1E285, 1E286, 1E287, 1E288, 1E289,
            1E290, 1E291, 1E292, 1E293, 1E294, 1E295, 1E296, 1E297, 1E298,
            1E299,

            1E300, 1E301, 1E302, 1E303, 1E304, 1E305, 1E306, 1E307, 1E308 };
    /**/
}