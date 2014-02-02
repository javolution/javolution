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
import javolution.lang.MathLib;
import javolution.lang.Realtime;
import javolution.lang.ValueType;
import javolution.text.Cursor;
import javolution.text.DefaultTextFormat;
import javolution.text.TextContext;
import javolution.text.TextFormat;
import javolution.text.TypeFormat;
import javolution.util.internal.table.ConstantTableImpl;

/**
 * <p> A non-negative number representing a position in an arrangement.
 * [code]
 * interface Vector<F> {
 *     Vector<F> getSubVector(List<Index> indices); 
 *         // e.g. getSubVector(Index.rangeOf(0, n)) for the n first elements.
 *     ...
 * }[/code]</p>

 * <p> Index performance is on-par with the primitive {@code int} type
 *     for small values and similar to {@link Integer} instances for large
 *     values. Small indexes have no adverse effect on the garbage collector
 *     and have fast {@link #equals(Object) equals} method due to their unicity.</p>
 * 
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 5.1, July 26, 2007
 */
@Realtime
@DefaultTextFormat(Index.Decimal.class)
public final class Index extends Number implements Comparable<Index>,
		ValueType {

	/**
	 * Default text format for indices (decimal value representation).
	 */
	public static class Decimal extends TextFormat<Index> {

		@Override
		public Appendable format(Index obj, Appendable dest) throws IOException {
			return TypeFormat.format(obj.intValue(), dest);
		}

		@Override
		public Index parse(CharSequence csq, Cursor cursor)
				throws IllegalArgumentException {
			return Index.of(TypeFormat.parseInt(csq, cursor));
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
			return MathLib.min(value, 65536); // Hard-limiting
		}

		@Override
		protected Integer reconfigured(Integer oldCount, Integer newCount) {
			throw new UnsupportedOperationException(
					"Unicity reconfiguration not supported.");
		}
	};

	/**
	 * Holds the index zero (value <code>0</code>).
	 */
	public static final Index ZERO = new Index(0);

	private static final long serialVersionUID = 0x600L; // Version.
	private static final Index[] INSTANCES = new Index[UNIQUE.get()];
	static {
		INSTANCES[0] = ZERO;
		for (int i = 1; i < INSTANCES.length; i++) {
			INSTANCES[i] = new Index(i);
		}
	}
	private static final ConstantTable<Index> INSTANCES_TABLE = new ConstantTable<Index>(
			new ConstantTableImpl<Index>(INSTANCES));

	/**
	 * Returns the index for the specified {@code int} non-negative
	 * value (returns a preallocated instance if the specified value is 
	 * small).
	 * 
	 * @param value the index value.
	 * @return the corresponding index.
	 * @throws IndexOutOfBoundsException if <code>value &lt; 0</code>
	 */
	public static Index of(int value) {
		return (value < INSTANCES.length) ? INSTANCES[value] : new Index(value);
	}

	/**
	 * Returns the indices having the specified {@code int} non-negative
	 * values (convenience method).
	 * 
	 * @param values the indices values.
	 * @return the corresponding table.
	 */
	public static FastTable<Index> listOf(int... values) {
		Index[] indices = new Index[values.length];
		for (int i = 0; i < indices.length; i++) {
			indices[i] = Index.of(values[i]);
		}
		return new ConstantTable<Index>(new ConstantTableImpl<Index>(indices));
	}

	/**
	 * Returns the indices having the specified range of {@code int} non-negative
	 * values (convenience method). A sub-table view over preallocated 
	 * instances is returned when possible. 
	 * 
	 * @param fromIndex low endpoint (inclusive) of the list to return.
	 * @param toIndex high endpoint (exclusive) of the list to return.
	 * @return the corresponding indices.
	 */
	public static ConstantTable<Index> rangeOf(int fromIndex, int toIndex) {
		if (toIndex <= INSTANCES.length)
			return INSTANCES_TABLE.subTable(fromIndex, toIndex); // Optimization.
		Index[] indices = new Index[toIndex - fromIndex];
		for (int i = fromIndex, j = 0; i < toIndex; i++) {
			indices[j++] = Index.of(i);
		}
		return new ConstantTable<Index>(new ConstantTableImpl<Index>(indices));
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
		return Index.of(value + 1);
	}

	/**
	 * Returns the index before this one.
	 * 
	 * @throws IndexOutOfBoundsException if (this == Index.ZERO)
	 */
	public Index previous() {
		return Index.of(value - 1);
	}

	/**
	 * Ensures index unicity during deserialization.
	 */
	protected final Object readResolve() throws ObjectStreamException {
		return Index.of(value);
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

}