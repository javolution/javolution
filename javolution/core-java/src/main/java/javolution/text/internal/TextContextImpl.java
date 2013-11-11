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

    // Holds class->format mapping. 
    private final FastMap<Class<?>, TextFormat<?>> classToFormat = new FastMap<Class<?>, TextFormat<?>>()
            .shared();

    // Holds parent (null if root).
    private final TextContextImpl parent;

    /** Default constructor for root */
    public TextContextImpl() {
        parent = null;
        classToFormat.put(Boolean.class, BOOLEAN_FORMAT);
        classToFormat.put(Byte.class, BYTE_FORMAT);
        classToFormat.put(Character.class, CHARACTER_FORMAT);
        classToFormat.put(Class.class, CLASS_FORMAT);
        classToFormat.put(Double.class, DOUBLE_FORMAT);
        classToFormat.put(Float.class, FLOAT_FORMAT);
        classToFormat.put(Integer.class, INTEGER_FORMAT);
        classToFormat.put(Long.class, LONG_FORMAT);
        classToFormat.put(Short.class, SHORT_FORMAT);
        classToFormat.put(String.class, STRING_FORMAT);
    }

    /** Inner constructor */
    public TextContextImpl(TextContextImpl parent) {
        this.parent = parent;
    }

    @Override
    protected TextContext inner() {
        return new TextContextImpl(this);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <T> TextFormat<T> searchFormat(Class<? extends T> type) {
        TextFormat<T> format = (TextFormat<T>) classToFormat.get(type);
        if (format != null) return format;
        if (parent != null) { // Searches parent.
            format = parent.searchFormat(type);
            classToFormat.put(type, format);
            return format;
        }
        // Root context (search inheritable annotations).
        DefaultTextFormat annotation = type
                .getAnnotation(DefaultTextFormat.class);
        if (annotation != null) { // Found it.
            try {
                format = (TextFormat<T>) annotation.value().newInstance();
                classToFormat.put(type, format);
                return format;
            } catch (Throwable error) {
                LogContext.warning(error);
            }
        }
        classToFormat.put(type, OBJECT_FORMAT);
        return (TextFormat<T>) OBJECT_FORMAT;
    }

    @Override
    public <T> void setFormat(Class<? extends T> type, TextFormat<T> format) {
        classToFormat.put(type, format);
    }

    ////////////////////////
    // PREDEFINED FORMATS //
    ////////////////////////

    private static final TextFormat<?> OBJECT_FORMAT = new TextFormat<Object>() {
        ThreadLocal<Object> objToString = new ThreadLocal<Object>();

        @Override
        public Appendable format(Object obj, Appendable dest)
                throws IOException {
            if (obj == null) return dest.append("null");
            if (objToString.get() == obj) return TypeFormat.format(
                    System.identityHashCode(obj), dest.append("Object#")); // Circularity in toString !
            objToString.set(obj);
            try {
                String str = obj.toString();
                return dest.append(str);
            } finally {
                objToString.set(null);
            }
        }

        @Override
        public Object parse(CharSequence csq, Cursor cursor) {
            throw new UnsupportedOperationException(
                    "Generic object parsing not supported.");
        }

    };

    private static TextFormat<Boolean> BOOLEAN_FORMAT = new TextFormat<Boolean>() {

        @Override
        public Appendable format(Boolean obj, Appendable dest)
                throws IOException {
            return TypeFormat.format(obj.booleanValue(), dest);
        }

        @Override
        public Boolean parse(CharSequence csq, Cursor cursor) {
            return TypeFormat.parseBoolean(csq, cursor);
        }
    };

    private static TextFormat<Character> CHARACTER_FORMAT = new TextFormat<Character>() {

        @Override
        public Appendable format(Character obj, Appendable dest)
                throws IOException {
            return dest.append(obj.charValue());
        }

        @Override
        public Character parse(CharSequence csq, Cursor cursor) {
            return Character.valueOf(cursor.nextChar(csq));
        }

    };

    private static TextFormat<Byte> BYTE_FORMAT = new TextFormat<Byte>() {

        @Override
        public Appendable format(Byte obj, Appendable dest) throws IOException {
            return TypeFormat.format(obj.byteValue(), dest);
        }

        @Override
        public Byte parse(CharSequence csq, Cursor cursor) {
            return Byte.valueOf(TypeFormat.parseByte(csq, 10, cursor));
        }

    };

    private static TextFormat<Short> SHORT_FORMAT = new TextFormat<Short>() {

        @Override
        public Appendable format(Short obj, Appendable dest) throws IOException {
            return TypeFormat.format(obj.shortValue(), dest);
        }

        @Override
        public Short parse(CharSequence csq, Cursor cursor) {
            return Short.valueOf(TypeFormat.parseShort(csq, 10, cursor));
        }

    };

    private static TextFormat<Integer> INTEGER_FORMAT = new TextFormat<Integer>() {

        @Override
        public Appendable format(Integer obj, Appendable dest)
                throws IOException {
            return TypeFormat.format(obj.intValue(), dest);
        }

        @Override
        public Integer parse(CharSequence csq, Cursor cursor) {
            return Integer.valueOf(TypeFormat.parseInt(csq, 10, cursor));
        }

    };

    private static TextFormat<Long> LONG_FORMAT = new TextFormat<Long>() {

        @Override
        public Appendable format(Long obj, Appendable dest) throws IOException {
            return TypeFormat.format(obj.longValue(), dest);
        }

        @Override
        public Long parse(CharSequence csq, Cursor cursor) {
            return Long.valueOf(TypeFormat.parseLong(csq, 10, cursor));
        }

    };

    private static TextFormat<Float> FLOAT_FORMAT = new TextFormat<Float>() {

        @Override
        public Appendable format(Float obj, Appendable dest) throws IOException {
            return TypeFormat.format(obj.floatValue(), dest);
        }

        @Override
        public Float parse(CharSequence csq, Cursor cursor) {
            return new Float(TypeFormat.parseFloat(csq, cursor));
        }

    };

    private static TextFormat<Double> DOUBLE_FORMAT = new TextFormat<Double>() {

        @Override
        public Appendable format(Double obj, Appendable dest)
                throws IOException {
            return TypeFormat.format(obj.doubleValue(), dest);
        }

        @Override
        public Double parse(CharSequence csq, Cursor cursor) {
            return new Double(TypeFormat.parseDouble(csq, cursor));
        }

    };

    private static TextFormat<String> STRING_FORMAT = new TextFormat<String>() {

        @Override
        public Appendable format(String obj, Appendable dest)
                throws IOException {
            return dest.append(obj);
        }

        @Override
        public String parse(CharSequence csq, Cursor cursor) {
            CharSequence tmp = csq.subSequence(cursor.getIndex(), csq.length());
            cursor.setIndex(csq.length());
            return tmp.toString();
        }

    };

    private static TextFormat<Class<?>> CLASS_FORMAT = new TextFormat<Class<?>>() {

        @Override
        public Appendable format(Class<?> obj, Appendable dest)
                throws IOException {
            return dest.append(obj.getName());
        }

        @Override
        public Class<?> parse(CharSequence csq, Cursor cursor) {
            CharSequence name = cursor.nextToken(csq, CharSet.WHITESPACES);
            try {
                return Class.forName(name.toString());
            } catch (ClassNotFoundException e) {
                throw new IllegalArgumentException("Class " + name
                        + " Not Found");
            }
        }

    };

}