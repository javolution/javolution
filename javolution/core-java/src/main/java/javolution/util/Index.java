/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util;

import java.io.IOException;
import java.io.ObjectStreamException;

import javolution.lang.Configurable;
import javolution.lang.RealTime;
import javolution.lang.ValueType;
import javolution.text.Cursor;
import javolution.text.DefaultTextFormat;
import javolution.text.TextContext;
import javolution.text.TextFormat;
import javolution.text.TypeFormat;

/**
 * <p> A non-negative number representing a position in an arrangement.
 *     For example:[code]
 *         class SparseVector<F> {
 *             FastMap<Index, F> elements = new FastMap<Index, F>();
 *             ...
 *         }[/code]</p>

 * <p> Index performance is on-par with the primitive {@code int} type
 *     for small values and similar to {@link Integer} instances for large
 *     values. Small indexes have no adverse effect on the garbage collector
 *     (permanent memory) and have fast {@link #equals(Object) equals}  
 *     method due to their unicity.</p>
 * 
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 5.1, July 26, 2007
 */
@RealTime
@DefaultTextFormat(Index.Decimal.class)
public final class Index extends Number implements Comparable<Index>,
        ValueType<Index> {

    /**
     * Holds the default text format for indices (decimal value representation).
     */
    public static class Decimal extends TextFormat<Index> {

        @Override
        public Appendable format(Index obj, Appendable dest) throws IOException {
            return TypeFormat.format(obj.intValue(), dest);
        }

        @Override
        public Index parse(CharSequence csq, Cursor cursor)
                throws IllegalArgumentException {
            return Index.valueOf(TypeFormat.parseInt(csq, cursor));
        }

    }

    /**
     * Holds the number of preallocated instances (default {@code 1024}).
     * Preallocate instances are allocated in permanent memory (e.g. 
     * immortal memory) and are unique (fast equals method).
     */
    public static final Configurable<Integer> PREALLOCATED = new Configurable<Integer>(
            1024);

    /**
     * Holds the index zero (value <code>0</code>).
     */
    public static final Index ZERO = new Index(0);

    private static final long serialVersionUID = 0x600L; // Version.
    private static final Index[] INSTANCES = new Index[PREALLOCATED.get()];
    static {
        INSTANCES[0] = ZERO;
        for (int i = 1; i < INSTANCES.length; i++) {
            INSTANCES[i] = new Index(i);
        }
    }

    /**
     * Returns the index for the specified {@code int} non-negative
     * value (returns a preallocated instance if the specified value is 
     * small).
     * 
     * @param value the index value.
     * @return the corresponding index.
     * @throws IndexOutOfBoundsException if <code>value &lt; 0</code>
     */
    public static Index valueOf(int value) {
        return (value < INSTANCES.length) ? INSTANCES[value] : new Index(value);
    }

    /**
     * Holds the index value.
     */
    private final int value;

    /**
     * Creates an index having the specified value.
     */
    private Index(int value) {
        this.value = value;
    }

    /**
     * Compares this index with the specified index for order.  Returns a
     * negative integer, zero, or a positive integer as this index is less
     * than, equal to, or greater than the specified index.
     *
     * @param   that the index to be compared.
     * @return  a negative integer, zero, or a positive integer as this index
     *          is less than, equal to, or greater than the specified index.
     */
    public int compareTo(Index that) {
        return this.value - that.value;
    }

    /**
     * Compares this index with the specified integer value for order.  Returns a
     * negative integer, zero, or a positive integer as this index is less
     * than, equal to, or greater than the specified value.
     *
     * @param   value the value to be compared.
     * @return  a negative integer, zero, or a positive integer as this index
     *          is less than, equal to, or greater than the specified value.
     */
    public int compareTo(int value) {
        return this.value - value;
    }

    /**
     * Returns a copy of this index or <code>this</code> if the indexes 
     * is small (in permanent memory) in order to maintain unicity.
     */
    public Index copy() {
        return value < INSTANCES.length ? this : new Index(value);
    }

    /**
     * Returns the index value as <code>double</code>.
     * 
     * @return the index value.
     */
    public double doubleValue() {
        return (double) value;
    }

    /**
     * Indicates if this index is equals to the one specified (for small 
     * indices this method is equivalent to <code>==</code>).
     */
    @Override
    public boolean equals(Object obj) {
        return (this.value < INSTANCES.length) ? (this == obj)
                : ((obj instanceof Index) ? (((Index) obj).value == value)
                        : false);
    }

    /**
     * Returns the index value as <code>float</code>.
     * 
     * @return the index value.
     */
    public float floatValue() {
        return (float) value;
    }

    /**
     * Returns the hash code for this index.
     */
    @Override
    public int hashCode() {
        return value;
    }

    /**
     * Returns the index value as <code>int</code>.
     * 
     * @return the index value.
     */
    public int intValue() {
        return value;
    }

    /**
     * Indicates if this index is zero.
     * 
     * @return {@code this == ZERO} 
     */
    public boolean isZero() {
        return this == ZERO;
    }

    /**
     * Returns the index value as <code>long</code>.
     * 
     * @return the index value.
     */
    public long longValue() {
        return value;
    }

    /**
     * Returns the index after this one.
     */
    public Index next() {
        return Index.valueOf(value + 1);
    }

    /**
     * Returns the index before this one.
     * 
     * @throws IndexOutOfBoundsException if (this == Index.ZERO)
     */
    public Index previous() {
        return Index.valueOf(value - 1);
    }

    /**
     * Ensures index unicity during deserialization.
     */
    protected final Object readResolve() throws ObjectStreamException {
        return Index.valueOf(value);
    }

    /**
     * Returns the {@link String} representation of this index.
     * 
     * @return {@code TextContext.getFormat(Index.class).format(this)}
     */
    @Override
    public String toString() {
        return TextContext.getFormat(Index.class).format(this);
    }

    @Override
    public Index value() {
        return this;
    }
}