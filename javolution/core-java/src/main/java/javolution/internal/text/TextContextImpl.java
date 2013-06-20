/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.internal.text;

import java.io.IOException;
import javolution.annotation.DefaultTextFormat;
import javolution.text.CharSet;
import javolution.text.Cursor;
import javolution.text.TextContext;
import javolution.text.TextFormat;
import javolution.text.TypeFormat;
import javolution.util.FastMap;

/**
 * Holds the default implementation of TextContext.
 * 
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0, December 12, 2012
 */
public final class TextContextImpl extends TextContext {

    private final FastMap<Class<?>, DefaultTextFormat<?>> formats = new FastMap<Class<?>, DefaultTextFormat<?>>();

    @Override
    protected TextContext inner() {
        TextContextImpl ctx = new TextContextImpl();
        ctx.formats.putAll(formats);
        return ctx;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <T> DefaultTextFormat<T> getFormatInContext(Class<? extends T> type) {
        DefaultTextFormat<T> tf = (DefaultTextFormat<T>) formats.get(type);
        if (tf != null)
            return tf;
        DefaultTextFormat format = type.getAnnotation(DefaultTextFormat.class);
        if ((format != null)
                && (format.text() != DefaultTextFormat.UnsupportedTextFormat.class)) {
            Class<? extends DefaultTextFormat<?>> formatClass = format.text();
            try {
                tf = (DefaultTextFormat<T>) formatClass.newInstance();
                synchronized (formats) { // Required since possible concurrent use 
                    // (getFormatInContext is not a configuration method).
                    formats.put(type, tf);
                }
                return tf;
            } catch (Throwable ex) {
                throw new RuntimeException(ex);
            }
        }
        // Check predefined formats.
        return (DefaultTextFormat<T>) PREDEFINED.get(type);

    }

    @Override
    public <T> void setFormat(Class<? extends T> type, DefaultTextFormat<T> format) {
        formats.put(type, format);
    }

    ////////////////////////
    // PREDEFINED FORMATS //
    ////////////////////////
    private static final FastMap<Class<?>, DefaultTextFormat<?>> PREDEFINED = new FastMap<Class<?>, DefaultTextFormat<?>>();

    static {
        PREDEFINED.put(Boolean.class, new DefaultTextFormat<Boolean>() {

            public Appendable format(Boolean obj, Appendable dest)
                    throws IOException {
                return TypeFormat.format(obj.booleanValue(), dest);
            }

            public Boolean parse(CharSequence csq, Cursor cursor) {
                return TypeFormat.parseBoolean(csq, cursor);
            }

        });

        PREDEFINED.put(Character.class, new DefaultTextFormat<Character>() {

            public Appendable format(Character obj, Appendable dest)
                    throws IOException {
                return dest.append(obj.charValue());
            }

            public Character parse(CharSequence csq, Cursor cursor) {
                return Character.valueOf(cursor.nextChar(csq));
            }

        });

        PREDEFINED.put(Byte.class, new DefaultTextFormat<Byte>() {

            public Appendable format(Byte obj, Appendable dest)
                    throws IOException {
                return TypeFormat.format(obj.byteValue(), dest);
            }

            public Byte parse(CharSequence csq, Cursor cursor) {
                return Byte.valueOf(TypeFormat.parseByte(csq, 10, cursor));
            }

        });

        PREDEFINED.put(Short.class, new DefaultTextFormat<Short>() {

            public Appendable format(Short obj, Appendable dest)
                    throws IOException {
                return TypeFormat.format(obj.shortValue(), dest);
            }

            public Short parse(CharSequence csq, Cursor cursor) {
                return Short.valueOf(TypeFormat.parseShort(csq, 10, cursor));
            }

        });

        PREDEFINED.put(Integer.class, new DefaultTextFormat<Integer>() {

            public Appendable format(Integer obj, Appendable dest)
                    throws IOException {
                return TypeFormat.format(obj.intValue(), dest);
            }

            public Integer parse(CharSequence csq, Cursor cursor) {
                return Integer.valueOf(TypeFormat.parseInt(csq, 10, cursor));
            }

        });

        PREDEFINED.put(Long.class, new DefaultTextFormat<Long>() {

            public Appendable format(Long obj, Appendable dest)
                    throws IOException {
                return TypeFormat.format(obj.longValue(), dest);
            }

            public Long parse(CharSequence csq, Cursor cursor) {
                return Long.valueOf(TypeFormat.parseLong(csq, 10, cursor));
            }

        });

        PREDEFINED.put(Float.class, new DefaultTextFormat<Float>() {

            public Appendable format(Float obj, Appendable dest)
                    throws IOException {
                return TypeFormat.format(obj.floatValue(), dest);
            }

            public Float parse(CharSequence csq, Cursor cursor) {
                return new Float(TypeFormat.parseFloat(csq, cursor));
            }

        });

        PREDEFINED.put(Double.class, new DefaultTextFormat<Double>() {

            public Appendable format(Double obj, Appendable dest)
                    throws IOException {
                return TypeFormat.format(obj.doubleValue(), dest);
            }

            public Double parse(CharSequence csq, Cursor cursor) {
                return new Double(TypeFormat.parseDouble(csq, cursor));
            }

        });

        PREDEFINED.put(String.class, new DefaultTextFormat<String>() {

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

        PREDEFINED.put(Class.class, new DefaultTextFormat<Class<?>>() {

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