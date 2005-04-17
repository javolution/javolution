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

import java.io.IOException;

/**
 * <p> This class provides utility methods to parse <code>CharSequence</code>
 *     into primitive types and to format primitive types into an
 *     <code>Appendable</code>.</p>
 *
 * <p> Methods from this utility class <b>do not create temporary objects</b>
 *     and are typically faster than standard library methods (e.g
 *     <code>TypeFormat#parseDouble</code> is up to 15x faster than 
 *     <code>Double.parseDouble</code>).</p>
 *
 * <p> For non-primitive objects, formatting is typically performed using 
 *     specialized {@link TextFormat} instances.</p>
 * 
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 3.0, February 16, 2005
 */
public final class TypeFormat {

    /**
     * Holds the characters used to represent numbers.
     */
    private final static char[] DIGITS = { '0', '1', '2', '3', '4', '5', '6',
            '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j',
            'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w',
            'x', 'y', 'z' };

    /**
     * Default constructor (forbids derivation).
     */
    private TypeFormat() {
    }

    /**
     * Returns the index within the specified character sequence of the first
     * occurrence of the specified pattern searching forward from the specified
     * index (general purpose parsing function).
     *
     * @param  pattern the character sequence to search for.
     * @param  chars the character sequence being searched.
     * @param  fromIndex the index in <code>chars</code> to start the search
     *         from.
     * @return the index in the range
     *         <code>[fromIndex, chars.length()-pattern.length()]</code>
     *         or <code>-1</code> if the character sequence is not found.
     */
    public static int indexOf(CharSequence pattern, CharSequence chars,
            int fromIndex) {
        int patternLength = pattern.length();
        fromIndex = Math.max(0, fromIndex);
        if (patternLength != 0) { // At least one character to search for.
            char firstChar = pattern.charAt(0);
            int last = chars.length() - patternLength;
            for (int i = fromIndex; i <= last; i++) {
                if (chars.charAt(i) == firstChar) {
                    boolean match = true;
                    for (int j = 1; j < patternLength; j++) {
                        if (chars.charAt(i + j) != pattern.charAt(j)) {
                            match = false;
                            break;
                        }
                    }
                    if (match) {
                        return i;
                    }
                }
            }
            return -1;
        } else {
            return Math.min(0, fromIndex);
        }
    }

    /**
     * Returns the index within the specified character sequence of the last
     * occurrence of the specified pattern searching backward from the specified
     * index (general purpose parsing function).
     *
     * @param  pattern the character sequence to search for.
     * @param  chars the character sequence being searched.
     * @param  fromIndex the index in <code>chars</code> to start the backward
     *         search from.
     * @return the index in the range
     *         <code>[fromIndex, chars.length()-pattern.length()]</code>
     *         or <code>-1</code> if the character sequence is not found.
     */
    public static int lastIndexOf(CharSequence pattern, CharSequence chars,
            int fromIndex) {
        int patternLength = pattern.length();
        fromIndex = Math.min(chars.length() - pattern.length(), fromIndex);
        if (patternLength != 0) { // At least one character to search for.
            char firstChar = pattern.charAt(0);
            for (int i = fromIndex; i >= 0; i--) {
                if (chars.charAt(i) == firstChar) {
                    boolean match = true;
                    for (int j = 1; j < patternLength; j++) {
                        if (chars.charAt(i + j) != pattern.charAt(j)) {
                            match = false;
                            break;
                        }
                    }
                    if (match) {
                        return i;
                    }
                }
            }
            return -1;
        } else {
            return Math.max(-1, fromIndex);
        }
    }

    /**
     * Parses the specified <code>CharSequence</code> as a <code>boolean</code>.
     *
     * @param  chars the character sequence to parse.
     * @return the corresponding <code>boolean</code>.
     */
    public static boolean parseBoolean(CharSequence chars) {
        return (chars.length() == 4)
                && (chars.charAt(0) == 't' || chars.charAt(0) == 'T')
                && (chars.charAt(1) == 'r' || chars.charAt(1) == 'R')
                && (chars.charAt(2) == 'u' || chars.charAt(2) == 'U')
                && (chars.charAt(3) == 'e' || chars.charAt(3) == 'E');
    }

    /**
     * Parses the specified <code>CharSequence</code> as a signed decimal
     * <code>short</code>.
     *
     * @param  chars the character sequence to parse.
     * @return <code>parseShort(chars, 10)</code>
     * @throws NumberFormatException if the specified character sequence
     *         does not contain a parsable <code>short</code>.
     * @see    #parseShort(CharSequence, int)
     */
    public static short parseShort(CharSequence chars) {
        return parseShort(chars, 10);
    }

    /**
     * Parses the specified <code>CharSequence</code> as a signed
     * <code>short</code> in the specified radix. The characters in the string
     * must all be digits of the specified radix, except the first character
     * which may be a plus sign <code>'+'</code> or a minus sign
     * <code>'-'</code>.
     *
     * @param  chars the character sequence to parse.
     * @param  radix the radix to be used while parsing.
     * @return the corresponding <code>short</code>.
     * @throws NumberFormatException if the specified character sequence
     *         does not contain a parsable <code>short</code>.
     */
    public static short parseShort(CharSequence chars, int radix) {
        try {
            boolean isNegative = (chars.charAt(0) == '-') ? true : false;
            int result = 0;
            int limit = (isNegative) ? Short.MIN_VALUE : -Short.MAX_VALUE;
            int multmin = limit / radix;
            int length = chars.length();
            int i = (isNegative || (chars.charAt(0) == '+')) ? 1 : 0;
            while (true) {
                int digit = Character.digit(chars.charAt(i), radix);
                int tmp = result * radix;
                if ((digit < 0) || (result < multmin) || (tmp < limit + digit)) { // Overflow.
                    throw new NumberFormatException("For input characters: \""
                            + chars.toString() + "\"");
                }
                // Accumulates negatively.
                result = tmp - digit;
                if (++i >= length) {
                    break;
                }
            }
            return (short) (isNegative ? result : -result);
        } catch (IndexOutOfBoundsException e) {
            throw new NumberFormatException("For input characters: \""
                    + chars.toString() + "\"");
        }
    }

    /**
     * Parses the specified <code>CharSequence</code> as a signed decimal
     * <code>int</code>.
     *
     * @param  chars the character sequence to parse.
     * @return <code>parseInt(chars, 10)</code>
     * @throws NumberFormatException if the specified character sequence
     *         does not contain a parsable <code>int</code>.
     * @see    #parseInt(CharSequence, int)
     */
    public static int parseInt(CharSequence chars) {
        return parseInt(chars, 10);
    }

    /**
     * Parses the specified <code>CharSequence</code> as a signed
     * <code>int</code> in the specified radix. The characters in the string
     * must all be digits of the specified radix, except the first character
     * which may be a plus sign <code>'+'</code> or a minus sign
     * <code>'-'</code>.
     *
     * @param  chars the character sequence to parse.
     * @param  radix the radix to be used while parsing.
     * @return the corresponding <code>int</code>.
     * @throws NumberFormatException if the specified character sequence
     *         does not contain a parsable <code>int</code>.
     */
    public static int parseInt(CharSequence chars, int radix) {
        try {
            boolean isNegative = (chars.charAt(0) == '-') ? true : false;
            int result = 0;
            int limit = (isNegative) ? Integer.MIN_VALUE : -Integer.MAX_VALUE;
            int multmin = limit / radix;
            int length = chars.length();
            int i = (isNegative || (chars.charAt(0) == '+')) ? 1 : 0;
            while (true) {
                int digit = Character.digit(chars.charAt(i), radix);
                int tmp = result * radix;
                if ((digit < 0) || (result < multmin) || (tmp < limit + digit)) { // Overflow.
                    throw new NumberFormatException("For input characters: \""
                            + chars.toString() + "\"");
                }
                // Accumulates negatively to avoid surprises near MAX_VALUE
                result = tmp - digit;
                if (++i >= length) {
                    break;
                }
            }
            return isNegative ? result : -result;
        } catch (IndexOutOfBoundsException e) {
            throw new NumberFormatException("For input characters: \""
                    + chars.toString() + "\"");
        }
    }

    /**
     * Parses the specified <code>CharSequence</code> as a signed decimal
     * <code>long</code>.
     *
     * @param  chars the character sequence to parse.
     * @return <code>parseLong(chars, 10)</code>
     * @throws NumberFormatException if the specified character sequence
     *         does not contain a parsable <code>long</code>.
     * @see    #parseLong(CharSequence, int)
     */
    public static long parseLong(CharSequence chars) {
        return parseLong(chars, 10);
    }

    /**
     * Parses the specified <code>CharSequence</code> as a signed
     * <code>long</code> in the specified radix. The characters in the string
     * must all be digits of the specified radix, except the first character
     * which may be a plus sign <code>'+'</code> or a minus sign
     * <code>'-'</code>.
     *
     * @param  chars the character sequence to parse.
     * @param  radix the radix to be used while parsing.
     * @return the corresponding <code>long</code>.
     * @throws NumberFormatException if the specified character sequence
     *         does not contain a parsable <code>long</code>.
     */
    public static long parseLong(CharSequence chars, int radix) {
        try {
            boolean isNegative = (chars.charAt(0) == '-') ? true : false;
            long result = 0;
            long limit = (isNegative) ? Long.MIN_VALUE : -Long.MAX_VALUE;
            long multmin = limit / radix;
            int length = chars.length();
            int i = (isNegative || (chars.charAt(0) == '+')) ? 1 : 0;
            while (true) {
                int digit = Character.digit(chars.charAt(i), radix);
                long tmp = result * radix;
                if ((digit < 0) || (result < multmin) || (tmp < limit + digit)) { // Overflow.
                    throw new NumberFormatException("For input characters: \""
                            + chars.toString() + "\"");
                }
                // Accumulates negatively to avoid surprises near MAX_VALUE
                result = tmp - digit;
                if (++i >= length) {
                    break;
                }
            }
            return isNegative ? result : -result;
        } catch (IndexOutOfBoundsException e) {
            throw new NumberFormatException("For input characters: \""
                    + chars.toString() + "\"");
        }
    }

    /**
     * Parses this <code>CharSequence</code> as a <code>float</code>.
     *
     * @param  chars the character sequence to parse.
     * @return the float number represented by the specified character sequence.
     * @throws NumberFormatException if the character sequence does not contain
     *         a parsable <code>float</code>.
     /*@FLOATING_POINT@
     public static float parseFloat(CharSequence chars) {
     double d = parseDouble(chars);
     if ((d >= -3.4028235e+38f) && (d <= 3.4028235e+38f)) {
     return (float) d;
     } else {
     throw new NumberFormatException(
     "Float overflow for input characters: \""
     + chars.toString() + "\"");
     }
     }
     /**/

    /**
     * Parses this <code>CharSequence</code> as a <code>double</code>.
     *
     * @param  chars the character sequence to parse.
     * @return the double number represented by this character sequence.
     * @throws NumberFormatException if the character sequence does not contain
     *         a parsable <code>double</code>.
     /*@FLOATING_POINT@
     public static double parseDouble(CharSequence chars)
     throws NumberFormatException {
     try {
     int length = chars.length();
     double result = 0.0;
     int exp = 0;

     boolean isNegative = (chars.charAt(0) == '-') ? true : false;
     int i = (isNegative || (chars.charAt(0) == '+')) ? 1 : 0;

     // Checks special cases NaN or Infinity.
     if ((chars.charAt(i) == 'N') || (chars.charAt(i) == 'I')) {
     if (chars.toString().equals("NaN")) {
     return 0.0 / 0.0; // Double.NaN;
     } else if (chars.subSequence(i, length).toString().equals(
     "Infinity")) {
     return isNegative ? NEGATIVE_INFINITY : POSITIVE_INFINITY;
     }
     }

     // Reads decimal number.
     boolean fraction = false;
     while (true) {
     char c = chars.charAt(i);
     if ((c == '.') && (!fraction)) {
     fraction = true;
     } else if ((c == 'e') || (c == 'E')) {
     break;
     } else if ((c >= '0') && (c <= '9')) {
     result = result * 10 + (c - '0');
     if (fraction) {
     exp--;
     }
     } else {
     throw new NumberFormatException("For input characters: \""
     + chars.toString() + "\"");
     }
     if (++i >= length) {
     break;
     }
     }
     result = isNegative ? -result : result;

     // Reads exponent (if any).
     if (i < length) {
     i++;
     boolean negE = (chars.charAt(i) == '-') ? true : false;
     i = (negE || (chars.charAt(i) == '+')) ? i + 1 : i;
     int valE = 0;
     while (true) {
     char c = chars.charAt(i);
     if ((c >= '0') && (c <= '9')) {
     valE = valE * 10 + (c - '0');
     if (valE > 10000000) { // Hard-limit to avoid overflow.
     valE = 10000000;
     }
     } else {
     throw new NumberFormatException(
     "For input characters: \"" + chars.toString()
     + "\"");
     }
     if (++i >= length) {
     break;
     }
     }
     exp += negE ? -valE : valE;
     }

     // Returns product decimal number with exponent.
     return multE(result, exp);

     } catch (IndexOutOfBoundsException e) {
     throw new NumberFormatException("For input characters: \""
     + chars.toString() + "\"");
     }
     }
     /**/

    /**
     * Formats the specified <code>boolean</code> and appends the resulting
     * text to the <code>Appendable</code> argument.
     *
     * @param  b a <code>boolean</code>.
     * @param  chars the <code>Appendable</code> to append.
     * @return the specified <code>StringBuffer</code> object.
     * @throws IOException if an I/O exception occurs.
     * @see    #parseBoolean
     */
    public static Appendable format(boolean b, Appendable chars)
            throws IOException {
        return b ? append(chars, "true") : append(chars, "false");
    }

    /**
     * Formats the specified <code>short</code> and appends the resulting
     * text (decimal representation) to the <code>Appendable</code> argument.
     *
     * <p> Note: This method is preferred to <code>Appendable.append(short)
     *           </code> as it does not create temporary <code>String</code>
     *           objects (several times faster for small numbers).</p>
     *
     * @param  s the <code>short</code> number.
     * @param  sb the <code>StrinBuffer</code> to append.
     * @return the specified <code>Appendable</code> object.
     * @throws IOException if an I/O exception occurs.
     * @see    #parseShort
     */
    public static Appendable format(short s, Appendable sb) throws IOException {
        return format((int) s, sb); // Forwards to int formatting (fast).
    }

    /**
     * Formats the specified <code>short</code> in the specified radix and
     * appends the resulting text to the <code>Appendable</code> argument.
     *
     * @param  s the <code>short</code> number.
     * @param  radix the radix.
     * @param  chars the <code>Appendable</code> to append.
     * @return the specified <code>Appendable</code> object.
     * @throws  IllegalArgumentException if radix is not in [2 .. 36] range.
     * @throws IOException if an I/O exception occurs.
     * @see    #parseShort(CharSequence, int)
     */
    public static Appendable format(short s, int radix, Appendable chars)
            throws IOException {
        return format((int) s, radix, chars); // Forwards to int formatting (fast).
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
     * @param  chars the <code>Appendable</code> to append.
     * @return the specified <code>Appendable</code> object.
     * @throws IOException if an I/O exception occurs.
     * @see    #parseInt
     */
    public static Appendable format(int i, Appendable chars) throws IOException {
        if (i <= 0) {
            if (i == Integer.MIN_VALUE) { // Negation would overflow.
                return append(chars, "-2147483648"); // 11 char max.
            } else if (i == 0) {
                return chars.append('0');
            }
            i = -i;
            chars.append('-');
        }
        int j = 1;
        for (; (j < 10) && (i >= INT_POW_10[j]); j++) {
        }
        // POW_10[j] > i >= POW_10[j-1]
        for (j--; j >= 0; j--) {
            int pow10 = INT_POW_10[j];
            int digit = i / pow10;
            i -= digit * pow10;
            chars.append(DIGITS[digit]);
        }
        return chars;
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
     * @param  chars the <code>Appendable</code> to append.
     * @return the specified <code>Appendable</code> object.
     * @throws  IllegalArgumentException if radix is not in [2 .. 36] range.
     * @throws IOException if an I/O exception occurs.
     * @see    #parseInt(CharSequence, int)
     */
    public static Appendable format(int i, int radix, Appendable chars)
            throws IOException {
        if (radix == 10) {
            return format(i, chars); // Faster version.
        } else if (radix < 2 || radix > 36) {
            throw new IllegalArgumentException("radix: " + radix);
        }
        if (i < 0) {
            chars.append('-');
        } else {
            i = -i;
        }
        format2(i, radix, chars);
        return chars;
    }

    private static void format2(int i, int radix, Appendable chars)
            throws IOException {
        if (i <= -radix) {
            format2(i / radix, radix, chars);
            chars.append(DIGITS[-(i % radix)]);
        } else {
            chars.append(DIGITS[-i]);
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
     * @param  chars the <code>Appendable</code> to append.
     * @return the specified <code>Appendable</code> object.
     * @throws IOException if an I/O exception occurs.
     * @see    #parseLong
     */
    public static Appendable format(long l, Appendable chars)
            throws IOException {
        if (l <= 0) {
            if (l == Long.MIN_VALUE) { // Negation would overflow.
                return append(chars, "-9223372036854775808"); // 20 characters max.
            } else if (l == 0) {
                return chars.append('0');
            }
            l = -l;
            chars.append('-');
        }
        int j = 1;
        for (; (j < 19) && (l >= LONG_POW_10[j]); j++) {
        }
        // POW_10[j] > l >= POW_10[j-1]
        for (j--; j >= 0; j--) {
            long pow10 = LONG_POW_10[j];
            int digit = (int) (l / pow10);
            l -= digit * pow10;
            chars.append(DIGITS[digit]);
        }
        return chars;
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
     * @param  chars the <code>Appendable</code> to append.
     * @return the specified <code>Appendable</code> object.
     * @throws  IllegalArgumentException if radix is not in [2 .. 36] range.
     * @throws IOException if an I/O exception occurs.
     * @see    #parseLong(CharSequence, int)
     */
    public static Appendable format(long l, int radix, Appendable chars)
            throws IOException {
        if (radix == 10) {
            return format(l, chars); // Faster version.
        } else if (radix < 2 || radix > 36) {
            throw new IllegalArgumentException("radix: " + radix);
        }
        if (l < 0) {
            chars.append('-');
        } else {
            l = -l;
        }
        format2(l, radix, chars);
        return chars;
    }

    private static void format2(long l, int radix, Appendable chars)
            throws IOException {
        if (l <= -radix) {
            format2(l / radix, radix, chars);
            chars.append(DIGITS[(int) -(l % radix)]);
        } else {
            chars.append(DIGITS[(int) -l]);
        }
    }

    /**
     * Formats the specified <code>float</code> and appends the resulting
     * text to the <code>Appendable</code> argument.
     *
     * @param  f the <code>float</code> number.
     * @param  chars the <code>Appendable</code> to append.
     * @return <code>format(f, 0.0, sb)</code>
     * @throws IOException if an I/O exception occurs.
     * @see    #format(float, double, Appendable)
     /*@FLOATING_POINT@
     public static Appendable format(float f, Appendable chars)
     throws IOException {
     return format(f, 0.0, chars);
     }
     /**/

    /**
     * Formats the specified <code>float</code> and appends the resulting text
     * to the <code>Appendable</code> argument; the number of significative
     * digits is deduced from the specifed accuracy. All digits at least as
     * significant as the specified accuracy are represented. For example:
     * <ul>
     * <li><code>format(5.6f, 0.01, sb)</code> appends <code>"5.60"</code></li>
     * <li><code>format(5.6f, 0.1, sb)</code> appends <code>"5.6"</code></li>
     * <li><code>format(5.6f, 1, sb)</code> appends <code>"6"</code></li>
     * </ul>
     * If the accuracy is <code>0.0</code>, the accuracy is assumed to be
     * the intrinsic <code>float</code> precision (32 bits IEEE 754 format);
     * no formatting is performed, all significant digits are displayed and
     * trailing zeros are removed.
     *
     * @param  f the <code>float</code> number.
     * @param  accuracy the maximum weight of the last digit represented.
     * @param  chars the <code>Appendable</code> to append.
     * @return the specified <code>Appendable</code> object.
     * @throws IllegalArgumentException if the specified precision is negative
     *         or would result in too many digits (19+).
     * @throws IOException if an I/O exception occurs.
     /*@FLOATING_POINT@
     public static Appendable format(float f, double accuracy, Appendable chars)
     throws IOException {
     boolean fixedFormat;
     if (accuracy > 0.0) {
     fixedFormat = true;
     } else if (accuracy == 0.0) {
     if (f != 0.0f) {
     fixedFormat = false;
     accuracy = Math.max(Math.abs(f) * FLOAT_RELATIVE_ERROR,
     1.4e-45);
     } else {
     return append(chars, "0.0"); // Exact zero.
     }
     } else {
     throw new IllegalArgumentException(
     "accuracy: Negative values not allowed");
     }
     return format(f, accuracy, fixedFormat, chars);
     }
     /**/
   
    /**
     * Formats the specified <code>double</code> and appends the resulting
     * text to the <code>Appendable</code> argument.
     *
     * <p> Note : This method is preferred to <code>Double.toString(double)
     *            </code> or even <code>String.valueOf(double)</code> as it
     *            does not create temporary <code>String</code> or <code>
     *            FloatingDecimal</code> objects (several times faster,
     *            e.g. 15x faster for <code>Double.MAX_VALUE</code>).</p>
     *
     * @param  d the <code>double</code> number.
     * @param  chars the <code>Appendable</code> to append.
     * @return <code>format(d, 0.0, sb)</code>
     * @throws IOException if an I/O exception occurs.
     * @see    #format(double, double, Appendable)
     /*@FLOATING_POINT@
     public static Appendable format(double d, Appendable chars)
     throws IOException {
     return format(d, 0.0, chars);
     }
     /**/

    /**
     * Formats the specified <code>double</code> and appends the resulting text
     * to the <code>Appendable</code> argument; the number of significative
     * digits is deduced from the specifed accuracy. All digits at least as
     * significant as the specified accuracy are represented. For example:
     * <ul>
     * <li><code>format(5.6, 0.01, sb)</code> appends <code>"5.60"</code></li>
     * <li><code>format(5.6, 0.1, sb)</code> appends <code>"5.6"</code></li>
     * <li><code>format(5.6, 1, sb)</code> appends <code>"6"</code></li>
     * </ul>
     * If the accuracy is <code>0.0</code>, the accuracy is derived from 
     * the intrinsic <code>double</code> precision (64 bits IEEE 754 format)
     * and no more than one trailing zero is allowed.
     *
     * @param  d the <code>double</code> number.
     * @param  accuracy the maximum weight of the last digit represented.
     * @param  chars the <code>Appendable</code> to append.
     * @return the specified <code>Appendable</code> object.
     * @throws IllegalArgumentException if the specified accuracy is negative
     *         or would result in too many digits (19+).
     * @throws IOException if an I/O exception occurs.
     /*@FLOATING_POINT@
     public static Appendable format(double d, double accuracy, Appendable chars)
     throws IOException {
     if (accuracy > 0.0) { // Accuracy on last digit.
     return format(d, accuracy, true, chars);
     } else if (accuracy == 0.0) { // Floating format.
     if (d != 0.0) {
     accuracy = Math.max(Math.abs(d) * DOUBLE_RELATIVE_ERROR,
     4.9e-324);
     return format(d, accuracy, false, chars);
     } else {
     return append(chars, "0.0"); // Exact zero.
     }
     } else { // accuracy < 0.0) 
     throw new IllegalArgumentException(
     "accuracy: Negative values not allowed");
     }
     }
     /**/

    /**
     * Formats the specified <code>double</code> and appends the resulting text
     * to the <code>Appendable</code> argument; the number of significative
     * digits is deduced from the specifed accuracy.
     *
     * @param  d the <code>double</code> number.
     * @param  accuracy the maximum weight of the last digit represented.
     * @param  fixedFormat indicates if the number of digits is 
     *         characteristic of the accuracy.
     * @param  chars the <code>Appendable</code> to append.
     * @return the specified <code>Appendable</code> object.
     * @throws IllegalArgumentException if the specified accuracy would result 
     *         in too many digits (19+).
     * @throws IOException if an I/O exception occurs.
     /*@FLOATING_POINT@
     private static Appendable format(double d, double accuracy,
     boolean fixedFormat, Appendable chars) throws IOException {
     if (d != d) { // NaN
     return append(chars, "NaN");
     } else if (d == POSITIVE_INFINITY) {
     return append(chars, "Infinity");
     } else if (d == NEGATIVE_INFINITY) {
     return append(chars, "-Infinity");
     }
     if (d < 0) {
     d = -d;
     chars.append('-');
     }
     int rank = rankFor(accuracy);
     double digitValue = multE(d, -rank);
     if (digitValue >= Long.MAX_VALUE) {
     throw new IllegalArgumentException(
     "Specified accuracy would result in too many digits");
     }
     long mantissa = (long) (digitValue + 0.5);
     int digits = 1;
     while ((mantissa >= LONG_POW_10[digits])
     && (++digits != LONG_POW_10.length)) {
     }
     int dotPos = digits + rank;
     if ((dotPos <= -LEADING_ZEROS.length) || (dotPos > digits)) {
     // Scientific notation has to be used ("x.xxxEyy").
     format(mantissa / LONG_POW_10[digits - 1], chars);
     if (digits > 1) {
     chars.append('.');
     formatFraction(mantissa % LONG_POW_10[digits - 1], digits - 1,
     fixedFormat, chars);
     }
     chars.append('E');
     format(dotPos - 1, chars);
     } else if (dotPos <= 0) { // Leading zeros ("0.xxxxx").
     append(chars, LEADING_ZEROS[-dotPos]);
     formatFraction(mantissa, digits, fixedFormat, chars);
     } else if (dotPos == digits) { // Dot at last position ("xxxxx.").
     format(mantissa, chars);
     if (!fixedFormat) { // Adds trailing zero ("xxx.0")
     append(chars, ".0");
     }
     } else { // Dot within the string ("xxxx.xxxxx").
     format(mantissa / LONG_POW_10[digits - dotPos], chars);
     chars.append('.');
     formatFraction(mantissa % LONG_POW_10[digits - dotPos], digits
     - dotPos, fixedFormat, chars);
     }
     return chars;
     }

     private static final double POSITIVE_INFINITY = 1.0 / 0.0;

     private static final double NEGATIVE_INFINITY = -1.0 / 0.0;

     private static void formatFraction(long fraction, int digits,
     boolean fixedFormat, Appendable chars) throws IOException {
     for (int i = digits; i > 0;) {
     long pow10 = LONG_POW_10[--i];
     int digit = (int) (fraction / pow10);
     fraction -= digit * pow10;
     chars.append(DIGITS[digit]);
     if ((fraction == 0) && !fixedFormat) {
     return; // No more than one trailing zero.
     }
     }
     }

     private static int rankFor(double accuracy) {
     int rank = 0;
     if (accuracy > 1.0) {
     while (accuracy > 1.0) {
     rank++;
     accuracy /= 10.0;
     }
     } else if (accuracy < 1.0) {
     while (accuracy < 1.0) {
     rank--;
     accuracy *= 10.0;
     }
     }
     return rank;
     }

     private static final double FLOAT_RELATIVE_ERROR = 0.000000059604644775390625;

     private static final double DOUBLE_RELATIVE_ERROR = 1.1102230246251565404236316680908e-16;

     private static String[] LEADING_ZEROS = { "0.", "0.0", "0.00" };

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
     value *= 1E21; // Exact multiplicand.
     E = Math.min(308, E - 21);
     return value * DOUBLE_POW_10[E];
     }
     } else {
     if (E >= -308) {
     return value / DOUBLE_POW_10[-E];
     } else {
     // Min: 4.9E-324
     value /= 1E21; // Exact divisor.
     E = Math.max(-308, E + 21);
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