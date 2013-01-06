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
import java.util.List;
import javolution.annotation.Format;
import javolution.annotation.StackSafe;
import javolution.context.HeapContext;
import javolution.context.StackContext;
import javolution.lang.Configurable;
import javolution.lang.Copyable;
import javolution.lang.ValueType;
import javolution.text.Cursor;
import javolution.text.TextContext;
import javolution.text.TypeFormat;

/**
 * <p> A <b>unique</b> non-negative number which can be used for ordering.
 *    For example:[code]
 *         class SparseVector<F> {
 *             FastMap<Index, F> elements = new FastMap();
 *             ...
 *         }[/code]</p>
 *          
 * <p> Unicity is guaranteed and direct equality (<code>==</code>) can be used 
 *     in place of object equality.</p>
 * 
 * <p> Indices have no adverse effect on the garbage collector (persistent 
 *     instances), but should not be used for large integer values as that  
 *     would increase the permanent memory footprint significantly.</p> 
 * 
 * <p> This class is {@link StackSafe} new unique indices are always allocated
 *     on the heap even when executing in a {@link StackContext}.</p>
 * 
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 5.1, July 26, 2007
 */
@Format(text = Index.TextFormat.class)
@StackSafe(initialization = false)
public final class Index extends Number implements Comparable<Index>, ValueType {

    /**
     * Holds the index zero (value <code>0</code>).
     */
    public static final Index ZERO = new Index(0);

    /**
     * Holds indices (to maintains unicity).
     */
    private static Index[] INSTANCES = new Index[256];

    static { 
        INSTANCES[0] = ZERO;
        for (int i = 1; i < INSTANCES.length; i++) {
            INSTANCES[i] = new Index(i);
        }
    }

    /**
     * Holds the number of indices preallocated (default <code>256</code>).
     */
    public static final Configurable<Integer> PREALLOCATED = new Configurable(INSTANCES.length) {

        @Override
        public void configure(CharSequence configuration) {
            int n = TypeFormat.parseInt(configuration);
            if (n <= 0)
                throw new IllegalArgumentException("Preallocated max cannot be zero or negative");
            this.setDefaultValue(n);
            Index.allocateOnHeap(n);
        }

    };

    /**
     * Holds the index value.
     */
    private final int value;

    /**
     * Creates an index having the specified value.
     * 
     * @param value the index value.
     */
    private Index(int value) {
        this.value = value;
    }

    /**
     * Returns the unique index for the specified <code>int</code> non-negative
     * value (creating it as well as the indices toward {@link #ZERO zero} 
     *  if they do not exist). 
     * 
     * @param value the index value.
     * @return the corresponding unique index.
     * @throws IndexOutOfBoundsException if <code>value &lt; 0</code>
     */
    public static Index valueOf(int value) { // Short to be inlined.
        if (value >= INSTANCES.length) Index.allocateOnHeap(2 * value);
        return INSTANCES[value];
    }

    /**
     * Returns all the indices greater or equal to <code>start</code>
     * but less than <code>end</code>.
     *
     * @param start the start index.
     * @param end the end index.
     * @return <code>[start .. end[</code>
     */
    public static List<Index> rangeOf(int start, int end) {
        FastTable<Index> list = new FastTable();
        for (int i = start; i < end; i++) {
            list.add(Index.valueOf(i));
        }
        return list;
    }

    /**
     * Returns the list of all the indices specified.
     *
     * @param indices the indices values.
     * @return <code>{indices[0], indices[1], ...}</code>
     */
    public static List<Index> valuesOf(int... indices) {
        FastTable<Index> list = new FastTable();
        for (int i : indices) {
            list.add(Index.valueOf(i));
        }
        return list;
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
        return INSTANCES[value - 1];
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
     * Returns the index value as <code>long</code>.
     * 
     * @return the index value.
     */
    public long longValue() {
        return value;
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
     * Returns the index value as <code>int</code>.
     * 
     * @return the index value.
     */
    public double doubleValue() {
        return (double) value;
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
    public final int compareTo(Index that) {
        return this.value - ((Index) that).value;
    }

    /**
     * Returns the <code>String</code> representation of this index.
     * 
     * @return <code>TextContext.getFormat(Index.class).format(this)</code>
     */
    @Override
    public String toString() {
        return TextContext.getFormat(Index.class).format(this);
    }

    /**
     * Indicates if this index is equals to the one specified (unicity 
     * ensures that this method is equivalent to <code>==</code>).
     * 
     * @return <code>this == obj</code>
     */
    @Override
    public final boolean equals(Object obj) {
        return this == obj;
    }

    /**
     * Returns the hash code for this index.
     *
     * @return the index value.
     */
    @Override
    public final int hashCode() {
        return value;
    }

    /**
     * Returns <code>this</code> in order to maintain unicity.
     * Index instances are always heap allocated.
     * 
     * @return <code>this</code>
     */
    public Copyable copy() {
        return this;
    }

    /**
     * Ensures index unicity during deserialization.
     * 
     * @return the unique instance for this deserialized index.
     */
    protected final Object readResolve() throws ObjectStreamException {
        return Index.valueOf(value);
    }

    /**
     * Holds the default text format for indices (decimal value representation).
     */
    public static class TextFormat extends javolution.text.TextFormat<Index> {

        @Override
        public Index parse(CharSequence csq, Cursor cursor) throws IllegalArgumentException {
            return Index.valueOf(TypeFormat.parseInt(csq, cursor));
        }

        @Override
        public Appendable format(Index obj, Appendable dest) throws IOException {
            return TypeFormat.format(obj.intValue(), dest);
        }

    }

    // Allocates on the heap at least min indices.
    private static synchronized void allocateOnHeap(final int n) {
        if (n <= INSTANCES.length) return; // Already done.
        HeapContext.execute(new Runnable() {

            public void run() {
                Index[] tmp = new Index[n];
                System.arraycopy(INSTANCES, 0, tmp, 0, INSTANCES.length);
                for (int i = INSTANCES.length; i < n; i++) {
                    tmp[i] = new Index(i);
                }
                INSTANCES = tmp;

            }

        });
    }

}