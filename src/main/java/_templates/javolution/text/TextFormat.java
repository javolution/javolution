/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2006 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package _templates.javolution.text;


import java.io.IOException;

import _templates.java.lang.CharSequence;
import _templates.javolution.Javolution;
import _templates.javolution.context.LocalContext;
import _templates.javolution.lang.ClassInitializer;
import _templates.javolution.lang.Reflection;
import _templates.javolution.util.FastMap;
import _templates.javolution.text.Appendable;

/**
 * <p> This class represents the base format for text parsing and formatting; 
 *     it supports the {@link CharSequence} and {@link Appendable} interfaces 
 *     for greater flexibility.</p>
 * 
 * <p> It is possible to {@link #getInstance retrieve} the format for any class
 *     for which the format has been {@link #setInstance registered} 
 *     (typically during class initialization).
 *     For example:[code]
 *     public class Complex extends RealtimeObject {
 *         private static final TextFormat<Complex> CARTESIAN = ...;
 *         static { // Sets default format to cartesian, users may locally change it later (see after).
 *             TextFormat.setInstance(Complex.class, CARTESIAN);
 *         }
 *         public Complex valueOf(CharSequence csq) {
 *             return TextFormat.getInstance(Complex.class).parse(csq);
 *         }
 *         public Text toText() {
 *             return TextFormat.getInstance(Complex.class).format(this);
 *         }
 *     }
 *     TextFormat<Complex> polar = ...;
 *     LocalContext.enter();
 *     try {
 *         TextFormat.setInstance(Complex.class, polar);
 *         Vector<Complex> vector ...
 *         System.out.println(vect); // Current thread displays the complex vector in polar coordinates.
 *     } finally {
 *         LocalContext.exit(); // Revert to default cartesian representation for complex numbers.
 *     }
 * [/code]</p>
 * <p>  The following standard types have a default {@link TextFormat} representation:<code><ul>
 *    <li>java.lang.Boolean</li>
 *    <li>java.lang.Character</li>
 *    <li>java.lang.Byte</li>
 *    <li>java.lang.Short</li>
 *    <li>java.lang.Integer</li>
 *    <li>java.lang.Long</li>
 *    <li>java.lang.Float</li>
 *    <li>java.lang.Double</li>
 *    <li>java.lang.Class</li>
 *    Users may register additional types.[code]
 *    TextFormat<Font> fontFormat = new TextFormat() {
 *        public Appendable format(Font font, Appendable dest) throws IOException {
 *            return dest.append(font.getName());
 *        }
 *        public Font parse(CharSequence csq, Cursor cursor) {
 *            CharSequence fontName = cursor.nextToken(csq, CharSet.WHITESPACES); // Trim whitespaces.
 *            return Font.decode(fontName.toString());
 *        }
 *    });
 *    TextFormat.setInstance(Font.class, fontFormat); // Registers text format for java.awt.Font
 * [/code]
 *
 * <p> For parsing/formatting of primitive types, the {@link TypeFormat}
 *     utility class is recommended.</p>
 *     
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle </a>
 * @version 5.3, Februrary 15, 2009
 */
public abstract class TextFormat/*<T>*/ {

    /**
     * Holds the class to format mapping.
     */
    private static final FastMap FORMATS = new FastMap().setShared(true);

    /**
     * Default constructor.
     */
    protected TextFormat() {
    }

    /**
     * Returns the most specialized text format for instances of specified 
     * type. The following types are always recognized:<code><ul>
     *    <li>java.lang.Boolean</li>
     *    <li>java.lang.Character</li>
     *    <li>java.lang.Byte</li>
     *    <li>java.lang.Short</li>
     *    <li>java.lang.Integer</li>
     *    <li>java.lang.Long</li>
     *    <li>java.lang.Float</li>
     *    <li>java.lang.Double</li>
     *    <li>java.lang.Class</li>
     * </ul></code>
     * @param  cls the class for which the default format is returned.
     * @return the format for instances of the specified class or 
     *         <code>null</code> if unkown.
     */
    public static/*<T>*/ TextFormat/*<T>*/ getInstance(Class/*<T>*/ cls) {
        LocalContext.Reference formatRef = (LocalContext.Reference) FORMATS.get(cls);
        return (formatRef != null) ? (TextFormat) formatRef.get() : searchFormat(cls);
    }

    private static TextFormat searchFormat(Class cls) {
        if (cls == null) 
            return null;
        ClassInitializer.initialize(cls); // Ensures class static initializer are run.
        LocalContext.Reference formatRef = (LocalContext.Reference) FORMATS.get(cls);
        return (formatRef != null) ? (TextFormat) formatRef.get() : searchFormat(superclassOf(cls));
    }

    private static Class superclassOf(Class cls) {
        /*@JVM-1.4+@
        if (true) return cls.getSuperclass();
        /**/
        return null;
    }

    /**
     * Associates the specified format to the specified type (class or 
     * interface).
     * 
     * @param  cls the class for which the default format is returned.
     * @param format the format for instances of the specified calss class.
     * @see #getInstance
     */
    public static/*<T>*/ void setInstance(Class/*<T>*/ cls,
            TextFormat/*<T>*/ format) {

        // The specified class is initialized prior to setting 
        // the format to ensure that the default format (typically in the 
        // class static initializer) does not override the new format.
        ClassInitializer.initialize(cls);
        LocalContext.Reference formatRef = (LocalContext.Reference) FORMATS.get(cls);
        if (formatRef == null) { // Static instance being set.
             formatRef = new LocalContext.Reference(format);
             FORMATS.put(cls, formatRef);
             return;
        }
        formatRef.set(format); // Local reference.
    }

    /**
     * Formats the specified object into an <code>Appendable</code> 
     * 
     * @param obj the object to format.
     * @param dest the appendable destination.
     * @return the specified <code>Appendable</code>.
     * @throws IOException if an I/O exception occurs.
     */
    public abstract Appendable format(Object/*{T}*/ obj, Appendable dest)
            throws IOException;

    /**
     * Parses a portion of the specified <code>CharSequence</code> from the
     * specified position to produce an object. If parsing succeeds, then the
     * index of the <code>cursor</code> argument is updated to the index after
     * the last character used. 
     * 
     * @param csq the <code>CharSequence</code> to parse.
     * @param cursor the cursor holding the current parsing index.
     * @return the object parsed from the specified character sub-sequence.
     * @throws IllegalArgumentException if any problem occurs while parsing the
     *         specified character sequence (e.g. illegal syntax).
     */
    public abstract Object/*{T}*/ parse(CharSequence csq, Cursor cursor) throws IllegalArgumentException;

    /**
     * Formats the specified object into a {@link TextBuilder} (convenience 
     * method which does not raise IOException). 
     * 
     * @param obj the object to format.
     * @param dest the text builder destination.
     * @return the specified text builder.
     */
    public final Appendable format(Object/*{T}*/ obj, TextBuilder dest) {
        try {
            return format(obj, (Appendable) dest);
        } catch (IOException e) {
            throw new Error(); // Cannot happen.
        }
    }

    /**
     * Formats the specified object to a {@link Text} instance
     * (convenience method equivalent to 
     * <code>format(obj, TextBuilder.newInstance()).toText()</code>).
     * 
     * @param obj the object being formated.
     * @return the text representing the specified object.
     */
    public final Text format(Object/*{T}*/ obj) {
        TextBuilder tb = TextBuilder.newInstance();
        try {
            format(obj, tb);
            return tb.toText();
        } finally {
            TextBuilder.recycle(tb);
        }
    }

    /**
     * Parses a whole character sequence from the beginning to produce an object
     * (convenience method). 
     * 
     * @param csq the whole character sequence to parse.
     * @return <code>parse(csq, new Cursor())</code>
     * @throws IllegalArgumentException if the specified character sequence
     *        cannot be fully parsed (extraneous characters).
     */
    public final Object/*{T}*/ parse(CharSequence csq) throws IllegalArgumentException {
        Cursor cursor = new Cursor();
        Object/*{T}*/ obj = parse(csq, cursor);
        if (cursor.getIndex() < csq.length()) {
            throw new IllegalArgumentException(
                    "Extraneous characters in \"" + csq + "\"");
        }
        return obj;
    }

    // Predefined formats.

    static {
        FORMATS.put(Boolean.class, new TextFormat() {

            public Appendable format(Object obj, Appendable dest)
                    throws IOException {
                return TypeFormat.format(((Boolean) obj).booleanValue(), dest);
            }

            public Object parse(CharSequence csq, Cursor cursor) {
                return TypeFormat.parseBoolean(csq, cursor) ? Boolean.TRUE : Boolean.FALSE;
            }
        });
        FORMATS.put(Character.class, new TextFormat() {

            public Appendable format(Object obj, Appendable dest)
                    throws IOException {
                return dest.append(((Character) obj).charValue());
            }

            public Object parse(CharSequence csq, Cursor cursor) {
                return new Character(cursor.next(csq));
            }
        });
        FORMATS.put(Byte.class, new TextFormat() {

            public Appendable format(Object obj, Appendable dest)
                    throws IOException {
                return TypeFormat.format(((Byte) obj).byteValue(), dest);
            }

            public Object parse(CharSequence csq, Cursor cursor) {
                return new Byte(TypeFormat.parseByte(csq, 10, cursor));
            }
        });
        FORMATS.put(Short.class, new TextFormat() {

            public Appendable format(Object obj, Appendable dest)
                    throws IOException {
                return TypeFormat.format(((Short) obj).shortValue(), dest);
            }

            public Object parse(CharSequence csq, Cursor cursor) {
                return new Short(TypeFormat.parseShort(csq, 10, cursor));
            }
        });
        FORMATS.put(Integer.class, new TextFormat() {

            public Appendable format(Object obj, Appendable dest)
                    throws IOException {
                return TypeFormat.format(((Integer) obj).intValue(), dest);
            }

            public Object parse(CharSequence csq, Cursor cursor) {
                return new Integer(TypeFormat.parseInt(csq, 10, cursor));
            }
        });
        FORMATS.put(Long.class, new TextFormat() {

            public Appendable format(Object obj, Appendable dest)
                    throws IOException {
                return TypeFormat.format(((Long) obj).longValue(), dest);
            }

            public Object parse(CharSequence csq, Cursor cursor) {
                return new Long(TypeFormat.parseLong(csq, 10, cursor));
            }
        });
        FORMATS.put(Class.class, new TextFormat() {

            public Appendable format(Object obj, Appendable dest)
                    throws IOException {
                return dest.append(Javolution.j2meToCharSeq(((Class) obj).getName()));
            }

            public Object parse(CharSequence csq, Cursor cursor) {
                CharSequence className = cursor.nextToken(csq, CharSet.WHITESPACES);
                if (className == null) throw new IllegalArgumentException("No class name found");
                Class cls;
                try {
                    cls = Reflection.getClass(className);
                    return cls;
                } catch (ClassNotFoundException e) {
                    throw new IllegalArgumentException("Class \"" + className + "\" not found");
                }
            }
        });

        FORMATS.put(Float.class, new TextFormat() {

            public Appendable format(Object obj, Appendable dest)
                    throws IOException {
                return TypeFormat.format(((Float) obj).floatValue(), dest);
            }

            public Object parse(CharSequence csq, Cursor cursor) {
                return new Float(TypeFormat.parseFloat(csq, cursor));
            }
        });
        FORMATS.put(Double.class, new TextFormat() {

            public Appendable format(Object obj, Appendable dest)
                    throws IOException {
                return TypeFormat.format(((Double) obj).doubleValue(), dest);
            }

            public Object parse(CharSequence csq, Cursor cursor) {
                return new Double(TypeFormat.parseDouble(csq, cursor));
            }
        });

    }
}