/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.text.internal;

import java.io.IOException;

import javolution.context.LogContext;
import javolution.text.CharSet;
import javolution.text.Cursor;
import javolution.text.DefaultTextFormat;
import javolution.text.TextContext;
import javolution.text.TextFormat;
import javolution.text.TypeFormat;
import javolution.util.FastMap;

/**
 * Holds the default implementation of TextContext.
 * 
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0, July 21, 2013
 */
public final class TextContextImpl extends TextContext {

    // Holds class->format local mapping. 
    private final FastMap<Class<?>, TextFormat<?>> localFormats;

    // Caches class->format from class annotations. 
    private final FastMap<Class<?>, TextFormat<?>> annotatedFormats;

    /** Default constructor for root */
    public TextContextImpl() {
        localFormats = new FastMap<Class<?>, TextFormat<?>>(); // Updated only during configuration.
        annotatedFormats = new FastMap<Class<?>, TextFormat<?>>().shared(); // Can be updated concurrently.
        storePrimitiveTypesFormats();
    }

    /** Inner constructor */
    public TextContextImpl(TextContextImpl parent) {
        localFormats = new FastMap<Class<?>, TextFormat<?>>()
                .putAll(parent.localFormats);
        annotatedFormats = parent.annotatedFormats;
    }

    @Override
    protected TextContext inner() {
        return new TextContextImpl(this);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <T> TextFormat<T> searchFormat(Class<? extends T> type) {
        if (localFormats.size() > 0) { // Checks local settings.
            TextFormat<T> tf = (TextFormat<T>) localFormats.get(type);
            if (tf != null)
                return tf;
        }
        TextFormat<T> tf = (TextFormat<T>) annotatedFormats.get(type);
        if (tf != null)
            return tf;
        if (annotatedFormats.containsKey(type))
            return null;
        return (TextFormat<T>) searchDefaultFormat(type);
    }

    private TextFormat<?> searchDefaultFormat(Class<?> type) {
        DefaultTextFormat format = type.getAnnotation(DefaultTextFormat.class);
        if (format != null) {
            Class<? extends TextFormat<?>> formatClass = format.value();
            try {
                TextFormat<?> tf = formatClass.newInstance();
                annotatedFormats.put(type, tf);
                return tf;
            } catch (Throwable error) {
                LogContext.warning(error);
            }
        }
        annotatedFormats.put(type, null);
        return null;
    }

    @Override
    public <T> void setFormat(Class<? extends T> type, TextFormat<T> format) {
        localFormats.put(type, format);
    }

    ////////////////////////
    // PREDEFINED FORMATS //
    ////////////////////////

    private void storePrimitiveTypesFormats() {
        annotatedFormats.put(Boolean.class, new TextFormat<Boolean>() {

            public Appendable format(Boolean obj, Appendable dest)
                    throws IOException {
                return TypeFormat.format(obj.booleanValue(), dest);
            }

            public Boolean parse(CharSequence csq, Cursor cursor) {
                return TypeFormat.parseBoolean(csq, cursor);
            }

        });

        annotatedFormats.put(Character.class, new TextFormat<Character>() {

            public Appendable format(Character obj, Appendable dest)
                    throws IOException {
                return dest.append(obj.charValue());
            }

            public Character parse(CharSequence csq, Cursor cursor) {
                return Character.valueOf(cursor.nextChar(csq));
            }

        });

        annotatedFormats.put(Byte.class, new TextFormat<Byte>() {

            public Appendable format(Byte obj, Appendable dest)
                    throws IOException {
                return TypeFormat.format(obj.byteValue(), dest);
            }

            public Byte parse(CharSequence csq, Cursor cursor) {
                return Byte.valueOf(TypeFormat.parseByte(csq, 10, cursor));
            }

        });

        annotatedFormats.put(Short.class, new TextFormat<Short>() {

            public Appendable format(Short obj, Appendable dest)
                    throws IOException {
                return TypeFormat.format(obj.shortValue(), dest);
            }

            public Short parse(CharSequence csq, Cursor cursor) {
                return Short.valueOf(TypeFormat.parseShort(csq, 10, cursor));
            }

        });

        annotatedFormats.put(Integer.class, new TextFormat<Integer>() {

            public Appendable format(Integer obj, Appendable dest)
                    throws IOException {
                return TypeFormat.format(obj.intValue(), dest);
            }

            public Integer parse(CharSequence csq, Cursor cursor) {
                return Integer.valueOf(TypeFormat.parseInt(csq, 10, cursor));
            }

        });

        annotatedFormats.put(Long.class, new TextFormat<Long>() {

            public Appendable format(Long obj, Appendable dest)
                    throws IOException {
                return TypeFormat.format(obj.longValue(), dest);
            }

            public Long parse(CharSequence csq, Cursor cursor) {
                return Long.valueOf(TypeFormat.parseLong(csq, 10, cursor));
            }

        });

        annotatedFormats.put(Float.class, new TextFormat<Float>() {

            public Appendable format(Float obj, Appendable dest)
                    throws IOException {
                return TypeFormat.format(obj.floatValue(), dest);
            }

            public Float parse(CharSequence csq, Cursor cursor) {
                return new Float(TypeFormat.parseFloat(csq, cursor));
            }

        });

        annotatedFormats.put(Double.class, new TextFormat<Double>() {

            public Appendable format(Double obj, Appendable dest)
                    throws IOException {
                return TypeFormat.format(obj.doubleValue(), dest);
            }

            public Double parse(CharSequence csq, Cursor cursor) {
                return new Double(TypeFormat.parseDouble(csq, cursor));
            }

        });

        annotatedFormats.put(String.class, new TextFormat<String>() {

            public Appendable format(String obj, Appendable dest)
                    throws IOException {
                return dest.append(obj);
            }

            public String parse(CharSequence csq, Cursor cursor) {
                CharSequence tmp = csq.subSequence(cursor.getIndex(),
                        csq.length());
                cursor.setIndex(csq.length());
                return tmp.toString();
            }

        });

        annotatedFormats.put(Class.class, new TextFormat<Class<?>>() {

            public Appendable format(Class<?> obj, Appendable dest)
                    throws IOException {
                return dest.append(obj.getName());
            }

            public Class<?> parse(CharSequence csq, Cursor cursor) {
                CharSequence name = cursor.nextToken(csq, CharSet.WHITESPACES);
                try {
                    return Class.forName(name.toString());
                } catch (ClassNotFoundException e) {
                    throw new IllegalArgumentException("Class " + name
                            + " Not Found");
                }
            }

        });

    }

}