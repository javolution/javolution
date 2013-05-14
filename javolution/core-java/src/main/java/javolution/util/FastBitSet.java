/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util;

import java.util.Iterator;
import java.util.Set;

import javolution.internal.util.bitset.BitSetIteratorImpl;
import javolution.internal.util.bitset.BitSetServiceImpl;
import javolution.internal.util.bitset.SharedBitSet;
import javolution.internal.util.bitset.UnmodifiableBitSet;
import javolution.util.function.Predicate;
import javolution.util.service.BitSetService;
import javolution.util.service.CollectionService;
import javolution.util.service.ComparatorService;

/**
 * <p> A table of bits equivalent to a packed set of non-negative numbers.</p>
 * 
 * <p> This class is integrated with the collection framework as 
 *     a set of {@link Index indices} and obeys the collection semantic
 *     for methods such as {@link #size} (cardinality) or {@link #equals}
 *     (same set of indices).</p>
 *   
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0.0, December 12, 2012
 */
public class FastBitSet extends FastCollection<Index> implements Set<Index> {

    /**
     * The actual implementation.
     */
    private final BitSetService impl;

    /**
     * Creates an empty bit set whose capacity increments/decrements smoothly
     * without large resize operations to best fit the set current size.
     */
    public FastBitSet() {
        impl = new BitSetServiceImpl(0);
    }

    /**
     * Creates a bit set of specified initial capacity (in bits). 
     * All bits are initially {@code false}.  This
     * constructor reserves enough space to represent the indices 
     * from {@code 0} to {@code bitSize-1}.
     * 
     * @param bitSize the initial capacity in bits.
     */
    public FastBitSet(int bitSize) {
        impl = new BitSetServiceImpl(bitSize);
    }

    /**
     * Creates a bit set using the specified implementation.
     */
    protected FastBitSet(BitSetService impl) {
        this.impl = impl;
    }

    /**
     * Performs the logical AND operation on this bit set and the
     * given bit set. This means it builds the intersection
     * of the two sets. The result is stored into this bit set.
     *
     * @param that the second bit set.
     */
    public void and(FastBitSet that) {
        impl.and(that.impl);
    }

    /**
     * Performs the logical AND operation on this bit set and the
     * complement of the given bit set.  This means it
     * selects every element in the first set, that isn't in the
     * second set. The result is stored into this bit set.
     *
     * @param that the second bit set
     */
    public void andNot(FastBitSet that) {
        impl.andNot(that.impl);
    }

    /**
     * Returns the number of bits set to {@code true} (or the size of this 
     * set).
     *
     * @return the number of bits being set.
     */
    public int cardinality() {
        return impl.cardinality();
    }

    /**
     * Sets all bits in the set to {@code false} (empty the set).
     */
    @Override
    public void clear() {
        impl.clear();
    }

    /**
     * Removes the specified integer value from this set. That is
     * the corresponding bit is cleared.
     *
     * @param bitIndex a non-negative integer.
     * @throws IndexOutOfBoundsException if {@code index < 0}
     */
    public void clear(int bitIndex) {
        impl.clear(bitIndex);
    }

    /**
     * Sets the bits from the specified {@code fromIndex} (inclusive) to the
     * specified {@code toIndex} (exclusive) to {@code false}.
     *
     * @param  fromIndex index of the first bit to be cleared.
     * @param  toIndex index after the last bit to be cleared.
     * @throws IndexOutOfBoundsException if 
     *          {@code (fromIndex < 0) | (toIndex < fromIndex)}
     */
    public void clear(int fromIndex, int toIndex) {     
        impl.clear(fromIndex, toIndex);
    }

    /**
     * Sets the bit at the index to the opposite value.
     *
     * @param bitIndex the index of the bit.
     * @throws IndexOutOfBoundsException if {@code bitIndex < 0}
     */
    public void flip(int bitIndex) {
        impl.flip(bitIndex);
    }

    /**
     * Sets a range of bits to the opposite value.
     *
     * @param fromIndex the low index (inclusive).
     * @param toIndex the high index (exclusive).
     * @throws IndexOutOfBoundsException if 
     *          {@code (fromIndex < 0) | (toIndex < fromIndex)}
     */
    public void flip(int fromIndex, int toIndex) {
        impl.flip(fromIndex, toIndex);
    }

    /**
     * Returns {@code true } if the specified integer is in 
     * this bit set; {@code false } otherwise.
     *
     * @param bitIndex a non-negative integer.
     * @return the value of the bit at the specified index.
     * @throws IndexOutOfBoundsException if {@code bitIndex < 0}
     */
    public boolean get(int bitIndex) {
        return impl.get(bitIndex);
    }

    /**
     * Returns a new bit set composed of a range of bits from this one.
     *
     * @param fromIndex the low index (inclusive).
     * @param toIndex the high index (exclusive).
     * @return a context allocated bit set instance.
     * @throws IndexOutOfBoundsException if 
     *          {@code (fromIndex < 0) | (toIndex < fromIndex)}
     */
    public FastBitSet get(int fromIndex, int toIndex) {     
        return new FastBitSet(impl.get(fromIndex, toIndex));
    }

    /**
     * Returns {@code true} if this bit set shares at least one
     * common bit with the specified bit set.
     *
     * @param that the bit set to check for intersection
     * @return {@code true} if the sets intersect; {@code false} otherwise.
     */
    public boolean intersects(FastBitSet that) {
        return impl.intersects(that.impl);
    }

    /**
     * Returns the logical number of bits actually used by this bit
     * set.  It returns the index of the highest set bit plus one.
     * 
     * <p> Note: This method does not return the number of set bits
     *           which is returned by {@link #size} </p>
     *
     * @return the index of the highest set bit plus one.
     */
    public int length() {
        return impl.length();
    }

    /**
     * Returns the index of the next {@code false} bit, from the specified bit
     * (inclusive).
     *
     * @param fromIndex the start location.
     * @return the first {@code false} bit.
     * @throws IndexOutOfBoundsException if {@code fromIndex < 0} 
     */
    public int nextClearBit(int fromIndex) {
        return impl.nextClearBit(fromIndex);
    }

    /**
     * Returns the index of the next {@code true} bit, from the specified bit
     * (inclusive). If there is none, {@code -1} is returned. 
     * The following code will iterates through the bit set:[code]
     *    for (int i=nextSetBit(0); i >= 0; i = nextSetBit(i)) {
     *         ...
     *    }[/code]
     *
     * @param fromIndex the start location.
     * @return the first {@code false} bit.
     * @throws IndexOutOfBoundsException if {@code fromIndex < 0} 
     */
    public int nextSetBit(int fromIndex) {
        return impl.nextSetBit(fromIndex);
    }

    /**
     * Performs the logical OR operation on this bit set and the one specified.
     * In other words, builds the union of the two sets.  
     * The result is stored into this bit set.
     *
     * @param that the second bit set.
     */
    public void or(FastBitSet that) {
        impl.or(that.impl);
    }

    /**
     * Adds the specified integer to this set (corresponding bit is set to 
     * {@code true}.
     *
     * @param bitIndex a non-negative integer.
     * @throws IndexOutOfBoundsException if {@code bitIndex < 0}
     */
    public void set(int bitIndex) {
        impl.set(bitIndex);
    }

    /**
     * Sets the bit at the given index to the specified value.
     *
     * @param bitIndex the position to set.
     * @param value the value to set it to.
     * @throws IndexOutOfBoundsException if {@code bitIndex < 0}
     */
    public void set(int bitIndex, boolean value) {
        impl.set(bitIndex, value);
    }

    /**
     * Sets the bits from the specified {@code fromIndex} (inclusive) to the
     * specified {@code toIndex} (exclusive) to {@code true}.
     *
     * @param  fromIndex index of the first bit to be set.
     * @param  toIndex index after the last bit to be set.
     * @throws IndexOutOfBoundsException if 
     *          {@code (fromIndex < 0) | (toIndex < fromIndex)}
     */
    public void set(int fromIndex, int toIndex) {
        if ((fromIndex < 0) || (toIndex < fromIndex))
            throw new IndexOutOfBoundsException();
        impl.set(fromIndex, toIndex);
    }

    /**
     * Sets the bits between from (inclusive) and to (exclusive) to the
     * specified value.
     *
     * @param fromIndex the start range (inclusive).
     * @param toIndex the end range (exclusive).
     * @param value the value to set it to.
     * @throws IndexOutOfBoundsException if {@code bitIndex < 0}
     */
    public void set(int fromIndex, int toIndex, boolean value) {
        impl.set(fromIndex, toIndex, value);
    }

    /**
     * Performs the logical XOR operation on this bit set and the one specified.
     * In other words, builds the symmetric remainder of the two sets 
     * (the elements that are in one set, but not in the other).  
     * The result is stored into this bit set.
     *
     * @param that the second bit set.
     */
    public void xor(FastBitSet that) {
        impl.xor(that.impl);
    }

    //
    // FastCollection methods override.
    //
    
    @Override
    public FastBitSet unmodifiable() {
        return new FastBitSet(new UnmodifiableBitSet(impl));
    }

    @Override
    public FastBitSet shared() {
        return new FastBitSet(new SharedBitSet(impl));
    }

    @Override
    public FastBitSet copy() {
        return this.get(0, length());
    }

    @Override
    protected CollectionService<Index> getService() {
        return new CollectionService<Index>() {

            @Override
            public void clear() {
                impl.clear();                
            }
            
            @Override
            public int size() {
                return cardinality();
            }

            @Override
            public boolean add(Index index) {
                return !impl.getAndSet(index.intValue(), true);
            }

            @Override
            public boolean contains(Index index) {
                return impl.get(index.intValue());        
            }
            
            @Override
            public boolean remove(Index index) {
                return impl.getAndSet(index.intValue(), false);
            }
   
            @Override
            public boolean doWhile(Predicate<? super Index> predicate) {
                for (int i = nextSetBit(0); i >= 0; i = nextSetBit(i)) {
                    if (!predicate.apply(Index.valueOf(i)))
                        return false;
                }
                return true;
            }

            @Override
            public boolean removeAll(Predicate<? super Index> predicate) {
                boolean modified = false;
                for (int i = nextSetBit(0); i >= 0; i = nextSetBit(i)) {
                    if (predicate.apply(Index.valueOf(i))) {
                        impl.clear(i);
                        modified = true;
                    }
                }
                return modified;
            }
            
            @Override
            public Iterator<Index> iterator() {
                return new BitSetIteratorImpl(impl, 0);
            }

            @Override
            public ComparatorService<? super Index> getComparator() {
                return FastComparator.IDENTITY;
            }

            @Override
            public void setComparator(ComparatorService<? super Index> cmp) {
                throw new UnsupportedOperationException();                
            }

        };
    }

    private static final long serialVersionUID = 2947704388849012297L;
}
