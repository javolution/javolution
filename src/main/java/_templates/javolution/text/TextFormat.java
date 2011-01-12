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
import _templates.javolution.context.LocalContext;
import _templates.javolution.lang.Realtime;
import _templates.javolution.lang.Reflection;
import _templates.javolution.text.Appendable;

/**
 * <p> This class represents the base format for text parsing and formatting; 
 *     it supports the {@link CharSequence} and {@link Appendable} interfaces 
 *     for greater flexibility.</p>
 * 
 * <p> Instances of this class are typically used as static member of a 
 *     class to define the default textual representation of its instances.
 *     [code]
 *     public class Complex extends Number {
 * 
 *         // Defines the default format for complex numbers (Cartesian form)
 *         protected static TextFormat<Complex> TEXT_FORMAT = new TextFormat<Complex> (Complex.class) { ... }
 *
 *         public static Complex valueOf(CharSequence csq) {
 *             return TEXT_FORMAT.parse(csq);
 *         }
 *
 *     }[/code]
 *     The format associated to any given class/object can be dynamically retrieved.
 *     [code]
 *     public abstract class Number implements ValueType {
 *
 *         public final Text toText() {
 *             return TextFormat.getInstance(this.getClass()).format(this);
 *         }
 *
 *         public final String toString() {
 *             return TextFormat.getInstance(this.getClass()).formatToString(this);
 *         }
 *     }[/code]
 *     The default format can be locally overriden.
 *     [code]
 *     LocalContext.enter();
 *     try {
 *          TextFormat<Complex> polarFormat = new TextFormat<Complex>(null) {...} // Unbound format.
 *          TextFormat.setInstance(Complex.class, polarFormat); // Local setting (no impact on others thread).
 *          System.out.println(complex); // Displays complex in polar coordinates.
 *     } finally {
 *          LocalContext.exit(); // Reverts to previous cartesian setting.
 *     }[/code]
 * </p>
 *
 * <p> For parsing/formatting of primitive types, the {@link TypeFormat}
 *     utility class is recommended.</p>
 *     
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle </a>
 * @version 5.5, March 20, 2010
 */
public abstract class TextFormat/*<T>*/ {

    /**
     * Defines the static format bound to the specified class.
     *
     * @param forClass the class to which the format is bound or <code>null</code>
     *        if the format is not bound to any class.
     * @throws IllegalArgumentException if the specified class is already
     *         bound to another format.
     */
    protected TextFormat(Class/*<T>*/ forClass) {
        if (forClass == null)
            return; // Dynamic format.
        Reflection.getInstance().setField(new LocalReference(this), forClass, LocalReference.class);
    }

    private static class LocalReference extends LocalContext.Reference {

        public LocalReference(TextFormat defaultFormat) {
            super(defaultFormat);
        }
    }

    /**
     * <p> Returns the default format for instances of the specified class.</p>
     *
     * <p> A default format exist for the following predefined types:
     *     <code><ul>
     *       <li>java.lang.Object (formatting only, e.g. "org.acmes.Foo#123")</li>
     *       <li>java.lang.String</li>
     *       <li>java.lang.Boolean</li>
     *       <li>java.lang.Character</li>
     *       <li>java.lang.Byte</li>
     *       <li>java.lang.Short</li>
     *       <li>java.lang.Integer</li>
     *       <li>java.lang.Long</li>
     *       <li>java.lang.Float</li>
     *       <li>java.lang.Double</li>
     *       <li>java.lang.Class</li>
     *       <li>javolution.util.Index</li>
     *       <li>javolution.text.Text</li>
     *    </ul></code>
     *
     * <p> If there is no format found for the specified class, the
     *     default format for <code>java.lang.Object</code> is returned.</p>
     *
     * @param  forClass the class for which a compatible format is returned.
     * @return the most specialized format compatible with the specified class.
     */
    public static/*<T>*/ TextFormat/*<T>*/ getDefault(Class/*<? extends T>*/ forClass) {
        Predefined.init(); // Forces initialization.
        LocalReference localReference = (LocalReference) Reflection.getInstance().getField(forClass, LocalReference.class, true);
        return (localReference == null) ? Predefined.OBJECT_FORMAT : (TextFormat/*<T>*/) localReference.getDefault();
    }

    /**
     * <p> Returns the current format for instances of the specified  class.</p>
     * 
     * @param  forClass the class to which a format has been bound.
     * @return the most specialized format compatible with the specified class.
     */
    public static/*<T>*/ TextFormat/*<T>*/ getInstance(Class/*<? extends T>*/ forClass) {
        Predefined.init(); // Forces initialization.
        LocalReference localReference = (LocalReference) Reflection.getInstance().getField(forClass, LocalReference.class, true);
        return (localReference == null) ? Predefined.OBJECT_FORMAT : (TextFormat/*<T>*/) localReference.get();
    }

    /**
     * Overrides the default format for the specified class ({@link LocalContext local setting}).
     *
     * @param forClass the class for which the format is locally overriden.
     * @param format the new format (typically unbound).
     * @throws IllegalArgumentException if the speficied class has not default format defined.
     */
    public static/*<T>*/ void setInstance(Class/*<? extends T>*/ forClass, TextFormat/*<T>*/ format) {
        Predefined.init(); // Forces initialization.
        LocalReference localReference = (LocalReference) Reflection.getInstance().getField(forClass, LocalReference.class, false);
        if (localReference == null)
            throw new IllegalArgumentException("Cannot override default format for class " + forClass + " (no default format defined)");
        localReference.set(format);
    }

    /**
     * Indicates if this format supports parsing (default <code>true</code>).
     *
     * @return <code>false</code> if any of the parse method throws
     *         <code>UnsupportedOperationException</code>
     */
    public boolean isParsingSupported() {
        return true;
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
    public final TextBuilder format(Object/*{T}*/ obj, TextBuilder dest) {
        try {
            format(obj, (Appendable) dest);
            return dest;
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
            format(obj, (Appendable) tb);
            return tb.toText();
        } catch (IOException e) {
            throw new Error(); // Cannot happen.
        } finally {
            TextBuilder.recycle(tb);
        }
    }

    /**
     * Convenience methods equivalent to but faster than
     * <code>format(obj).toString())</code>
     *
     * @param obj the object being formated.
     * @return the string representing the specified object.
     */
    public final String formatToString(Object/*{T}*/ obj) {
        TextBuilder tb = TextBuilder.newInstance();
        try {
            format(obj, (Appendable) tb);
            return tb.toString();
        } catch (IOException e) {
            throw new Error(); // Cannot happen.
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
     *        cannot be fully parsed (e.g. extraneous characters).
     */
    public final Object/*{T}*/ parse(CharSequence csq) throws IllegalArgumentException {
        Cursor cursor = Cursor.newInstance();
        try {
            Object/*{T}*/ obj = parse(csq, cursor);
            if (cursor.getIndex() < csq.length())
                throw new IllegalArgumentException(
                        "Extraneous characters in \"" + csq + "\"");
            return obj;
        } finally {
            Cursor.recycle(cursor);
        }
    }

    private static class Predefined {
        //
        // Sets the default formats for predefined types.
        //

        static final TextFormat OBJECT_FORMAT = new TextFormat(Object.class) {

            public Appendable format(Object obj, Appendable dest)
                    throws IOException {
                dest.append(j2meToCharSeq(obj.getClass().getName()));
                dest.append('#');
                return TypeFormat.format(System.identityHashCode(obj), dest);
            }

            public boolean isParsingSupported() {
                return false;
            }

            public Object parse(CharSequence csq, Cursor cursor) {
                throw new _templates.java.lang.UnsupportedOperationException("Parsing not supported");
            }
        };

        static final TextFormat STRING_FORMAT = new TextFormat(String.class) {

            public Appendable format(Object obj, Appendable dest)
                    throws IOException {
                return dest.append(j2meToCharSeq(obj));
            }

            public Object parse(CharSequence csq, Cursor cursor) {
                String str = csq.subSequence(cursor.getIndex(), csq.length()).toString();
                cursor.setIndex(csq.length());
                return str;
            }
        };

        static final TextFormat BOOLEAN_FORMAT = new TextFormat(Boolean.class) {

            public Appendable format(Object obj, Appendable dest)
                    throws IOException {
                return TypeFormat.format(((Boolean) obj).booleanValue(), dest);
            }

            public Object parse(CharSequence csq, Cursor cursor) {
                return TypeFormat.parseBoolean(csq, cursor) ? Boolean.TRUE : Boolean.FALSE;
            }
        };

        static final TextFormat CHARACTER_FORMAT = new TextFormat(Character.class) {

            public Appendable format(Object obj, Appendable dest)
                    throws IOException {
                return dest.append(((Character) obj).charValue());
            }

            public Object parse(CharSequence csq, Cursor cursor) {
                return new Character(cursor.nextChar(csq));
            }
        };

        static final TextFormat BYTE_FORMAT = new TextFormat(Byte.class) {

            public Appendable format(Object obj, Appendable dest)
                    throws IOException {
                return TypeFormat.format(((Byte) obj).byteValue(), dest);
            }

            public Object parse(CharSequence csq, Cursor cursor) {
                return new Byte(TypeFormat.parseByte(csq, 10, cursor));
            }
        };

        static final TextFormat SHORT_FORMAT = new TextFormat(Short.class) {

            public Appendable format(Object obj, Appendable dest)
                    throws IOException {
                return TypeFormat.format(((Short) obj).shortValue(), dest);
            }

            public Object parse(CharSequence csq, Cursor cursor) {
                return new Short(TypeFormat.parseShort(csq, 10, cursor));
            }
        };

        static final TextFormat INTEGER_FORMAT = new TextFormat(Integer.class) {

            public Appendable format(Object obj, Appendable dest)
                    throws IOException {
                return TypeFormat.format(((Integer) obj).intValue(), dest);
            }

            public Object parse(CharSequence csq, Cursor cursor) {
                return new Integer(TypeFormat.parseInt(csq, 10, cursor));
            }
        };

        static final TextFormat LONG_FORMAT = new TextFormat(Long.class) {

            public Appendable format(Object obj, Appendable dest)
                    throws IOException {
                return TypeFormat.format(((Long) obj).longValue(), dest);
            }

            public Object parse(CharSequence csq, Cursor cursor) {
                return new Long(TypeFormat.parseLong(csq, 10, cursor));
            }
        };

        static final TextFormat FLOAT_FORMAT = new TextFormat(Float.class) {

            public Appendable format(Object obj, Appendable dest)
                    throws IOException {
                return TypeFormat.format(((Float) obj).floatValue(), dest);
            }

            public Object parse(CharSequence csq, Cursor cursor) {
                return new Float(TypeFormat.parseFloat(csq, cursor));
            }
        };

        static final TextFormat DOUBLE_FORMAT = new TextFormat(Double.class) {

            public Appendable format(Object obj, Appendable dest)
                    throws IOException {
                return TypeFormat.format(((Double) obj).doubleValue(), dest);
            }

            public Object parse(CharSequence csq, Cursor cursor) {
                return new Double(TypeFormat.parseDouble(csq, cursor));
            }
        };

        static final TextFormat CLASS_FORMAT = new TextFormat(Class.class) {

            public Appendable format(Object obj, Appendable dest)
                    throws IOException {
                return dest.append(j2meToCharSeq(((Class) obj).getName()));
            }

            public Object parse(CharSequence csq, Cursor cursor) {
                CharSequence className = cursor.nextToken(csq, CharSet.WHITESPACES);
                if (className == null)
                    throw new IllegalArgumentException("No class name found");
                Class cls = Reflection.getInstance().getClass(className);
                if (cls != null)
                    return cls;
                throw new IllegalArgumentException("Class \"" + className + "\" not found (see javolution.lang.Reflection)");
            }
        };

        static final TextFormat TEXT_FORMAT = new TextFormat(Text.class) {

            public Appendable format(Object obj, Appendable dest)
                    throws IOException {
                return dest.append((Text) obj);
            }

            public Object parse(CharSequence csq, Cursor cursor) {
                CharSequence subCsq = csq.subSequence(cursor.getIndex(), csq.length());
                if (subCsq instanceof Realtime)
                    return ((Realtime) subCsq).toText();
                return Text.valueOf(subCsq.toString());
            }
        };

        private static void init() {
            // Do nothing.
        }

    }

    private static CharSequence j2meToCharSeq(Object str) {
        /*@JVM-1.4+@
        return (CharSequence) str;
        }
        private static Text dummy(Object str) { // Never used.
        /**/
        return str == null ? null : Text.valueOf(str);
    }
}
