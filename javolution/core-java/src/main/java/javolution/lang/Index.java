/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.lang;

import java.io.IOException;
import java.io.ObjectStreamException;

import javolution.text.Cursor;
import javolution.text.DefaultTextFormat;
import javolution.text.TextContext;
import javolution.text.TextFormat;
import javolution.text.TypeFormat;

/**
 * <p> A non-negative number (32-bits unsigned) representing a position in 
 *     an arrangement.
 * <pre>{@code
 * FastMap<Index, Double> sparseVector = FastMap.newMap(Order.INDEX);
 * FastMap<Binary<Index,Index>, Double> sparseMatrix = FastMap.newMap(Order.QUADTREE);
 * }</pre></p>

 * <p> Index performance is on-par with the primitive {@code int} type
 *     for small values and similar to {@link Integer} instances for large
 *     values (but extended to a 32-bits unsigned range).</p>
 *     
 * <p> {@link #UNIQUE Unicity} is maintained for small indices 
 *     (no adverse effect on garbage collection).</p>
 * 
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.1, February 25, 2014
 * @see javolution.util.function.Indexer
 */
@Realtime
@DefaultTextFormat(Index.Format.class)
public final class Index extends Number implements Comparable<Index>, ValueType {

    /**
     * Default text format for indices (32-bits unsigned decimal 
     * representation).
     */
    public static class Format extends TextFormat<Index> {

        @Override
        public Appendable format(Index index, Appendable dest) throws IOException {
            return TypeFormat.format(index.longValue(), dest);
        }

        @Override
        public Index parse(CharSequence csq, Cursor cursor)
                throws IllegalArgumentException {
            return Index.of((int) TypeFormat.parseLong(csq, cursor));
        }

    }
    /**
     * Holds the number of unique preallocated instances (default {@code 1024}). 
     * This number is configurable, for example with
     * {@code -Djavolution.util.Index#UNIQUE=0} there is no unique instance.
     */
    public static final Configurable<Integer> UNIQUE = new Configurable<Integer>() {

        @Override
        protected Integer getDefault() {
            return 1024;
        }

        @Override
        protected Integer initialized(Integer value) {
            if (value < 0)
                throw new IllegalArgumentException();
            return value;
        }

        @Override
        protected Integer reconfigured(Integer oldCount, Integer newCount) {
            throw new UnsupportedOperationException(
                    "Unicity reconfiguration not supported.");
        }
    };

    private static final Index[] INSTANCES = new Index[UNIQUE.get()];
    private static final long LONG_MASK = 0xFFFFFFFFL;

    private static final long serialVersionUID = 0x610L; // Version.

    /**
     * Holds the index unsigned {@code int} value.
     */
    private final int unsigned;

    static {
        for (int i = 0; i < INSTANCES.length; i++) {
            INSTANCES[i] = new Index(i);
        }
    }

    /**
     * Holds the index zero ({@code Index.of(0)}).
     */
    public static final Index ZERO = Index.of(0);
    
    /**
     * Holds the index one({@code Index.of(1)}).
     */
    public static final Index ONE = Index.of(1);

    /**
     * Holds the index maximum ({@code Index.of(0xFFFFFFFF)}).
     */
    public static final Index MAX = Index.of(-1);


    /**
     * Returns the index for the specified 32-bits unsigned value 
     * (a preallocated instance if the specified value is small).
     * 
     * @param unsigned the unsigned 32-bits value.
     * @return the corresponding index.
     */
    public static Index of(int unsigned) {
        return (unsigned >= 0) & (unsigned < INSTANCES.length) ?
        		INSTANCES[unsigned] : new Index(unsigned);
    }

    /**
     * Creates an index having the specified 32-bits unsigned value.
     */
    private Index(int unsigned) {
        this.unsigned = unsigned;
    }

    @Override
    public int compareTo(Index that) {
        return MathLib.compareUnsigned(this.unsigned, that.unsigned);
    }

    /**
     * Compares this index with the specified 32-bits unsigned value for order.
     * @see #compareTo(Index)
     */
    public int compareTo(int unsignedValue) {
        return MathLib.compareUnsigned(this.unsigned, unsignedValue);
    }

    @Override
    public double doubleValue() {
        return (double) longValue();
    }

    /**
     * Indicates if this index has the same value as the one specified.
     */
    public boolean equals(Index that) {
        return (this.unsigned == that.unsigned);
    }

    /**
     * Indicates if this index has the specified 32-bits unsigned value.
     * @see #equals(Index)
     */
    public boolean equals(int unsigned) {
        return (this.unsigned == unsigned);
    }

    @Override
    public boolean equals(Object obj) {
        return (this == obj) || (obj instanceof Index) ? equals((Index) obj)
                : false;
    }

    @Override
    public float floatValue() {
        return (float) longValue();
    }

    /**
     * Returns {@code intValue()}
     */
    @Override
    public int hashCode() {
        return unsigned;
    }

    @Override
    public int intValue() {
        return unsigned;
    }

    @Override
    public long longValue() {
        return unsigned & LONG_MASK;
    }

    /**
     * Returns the index after this one.
     * 
     * @throws IndexOutOfBoundsException if {@code this.equals(Index.MAX)}
     */
    public Index next() {
        if (unsigned == -1)
            throw new IndexOutOfBoundsException();
        return Index.of(unsigned + 1);
    }

    /**
     * Returns the index before this one.
     * 
     * @throws IndexOutOfBoundsException if {@code this.equals(Index.ZERO)}
     */
    public Index previous() {
        if (unsigned == 0)
            throw new IndexOutOfBoundsException();
        return Index.of(unsigned - 1);
    }

    @Override
    public String toString() {
        return TextContext.getFormat(Index.class).format(this);
    }

    /**
     * Ensures index unicity during deserialization.
     */
    protected Object readResolve() throws ObjectStreamException {
        return Index.of(unsigned);
    }

}