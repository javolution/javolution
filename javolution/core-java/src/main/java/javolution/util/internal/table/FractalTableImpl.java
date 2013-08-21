/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util.internal.table;

import javolution.lang.MathLib;

/**
 * A fractal-based table with fast insertion/deletion capabilities regardless 
 * of the collection size. It is based on a fractal structure with self-similar
 * patterns at any scale (large tables have the same structure as smaller tables
 * which have similar structure as even smaller tables and so on). 
  */
final class FractalTableImpl {

    static final int BASE_CAPACITY_MIN = 16;
    static final int SHIFT = 8;
    private static final int BASE_CAPACITY_MAX = 1 << SHIFT;

    /** Offset value, it is the index of the first element (modulo data.length). */
    int offset;

    /** An array of data elements or fractal tables (recursion). 
     Data length varies from 2 to BASE_CAPACITY_MAX  */
    private Object[] data;

    /** The index shift, zero if base table. */
    private final int shift;

    public FractalTableImpl() {
        this.shift = 0;
        data = new Object[BASE_CAPACITY_MIN];
    }

    public FractalTableImpl(int shift) {
        this.shift = shift;
        data = new Object[2];
    }

    public FractalTableImpl(int shift, Object[] data, int offset) {
        this.shift = shift;
        this.data = data;
        this.offset = offset;
    }

    public int capacity() {
        // Reports lower capacity to ensure that there is no fractal holding 
        // wrapping data (head and tail in the same fractal).
        return (data.length - 1) << shift;
    }

    public Object get(int index) {
        Object fractal = data[((index + offset) >> shift) & (data.length - 1)];
        return (shift == 0) ? fractal : ((FractalTableImpl) fractal).get(index
                + offset);
    }

    public Object set(int index, Object element) {
        int i = ((index + offset) >> shift) & (data.length - 1);
        if (shift != 0) return F(i).set(index + offset, element);
        Object previous = data[i];
        data[i] = element;
        return previous;
    }

    /** Shifts the specified elements(]last - length, last] modulo capacity) 
     one position to the left. No shift if length (modulo capacity) is zero. */
    public void shiftLeft(Object inserted, int last, int length) {
        int mask = (data.length << shift) - 1;
        int tail = (last + offset) & mask;
        int head = (last + offset - length) & mask;
        if (shift == 0) {
            int n = tail - head;
            if (head > tail) { // Wrapping
                System.arraycopy(data, head + 1, data, head, mask - head);
                data[mask] = data[0];
                n = tail;
            }
            System.arraycopy(data, tail - n + 1, data, tail - n, n);
            data[tail] = inserted;
        } else if ((head <= tail) && ((head >> shift) == (tail >> shift))) { // Shift local to inner table.
            F(head >> shift).shiftLeft(inserted, tail, length); // (no wrapping).
        } else {
            int low = head >> shift;
            int high = (low != data.length - 1) ? low + 1 : 0;
            F(low).shiftLeft(F(high).get(0), -1, mask - head);
            while (high != (tail >> shift)) {
                low = high;
                high = (low != data.length - 1) ? low + 1 : 0;
                F(low).offset++; // Full shift left.
                F(low).set(-1, F(high).get(0));
            }
            F(high).shiftLeft(inserted, tail, tail);
        }
    }

    /** Shifts the specified element ([first, first + length[ modulo capacity) 
     one position to the right. No shift if length (modulo capacity) is zero. */
    public void shiftRight(Object inserted, int first, int length) {
        int mask = (data.length << shift) - 1;
        int head = (first + offset) & mask;
        int tail = (first + offset + length) & mask;
        if (shift == 0) {
            int n = tail - head;
            if (head > tail) { // Wrapping
                System.arraycopy(data, 0, data, 1, tail);
                data[0] = data[mask];
                n = mask - head;
            }
            System.arraycopy(data, head, data, head + 1, n);
            data[head] = inserted;
        } else if ((head <= tail) && ((head >> shift) == (tail >> shift))) { // Shift local to inner table.
            F(head >> shift).shiftRight(inserted, head, length); // (no wrapping).
        } else {
            int high = tail >> shift;
            int low = (high != 0) ? high - 1 : data.length - 1;
            F(high).shiftRight(F(low).get(-1), 0, tail);
            while (low != (head >> shift)) {
                high = low;
                low = (high != 0) ? high - 1 : data.length - 1;
                F(high).offset--; // Full shift right.
                F(high).set(0, F(low).get(-1));
            }
            F(low).shiftRight(inserted, head, mask - head);
        }
    }

    public FractalTableImpl upsize() {
        if (data.length >= BASE_CAPACITY_MAX) { // Creates outer fractal.
            FractalTableImpl table = new FractalTableImpl(shift + SHIFT);
            copyTo(table.F(0));
            return table;
        } else {
            FractalTableImpl table = new FractalTableImpl(shift,
                    new Object[data.length << 1], 0);
            copyTo(table);
            return table;
        }
    }

    private FractalTableImpl allocate(int i) {
        FractalTableImpl fractal = new FractalTableImpl(shift - SHIFT,
                new Object[1 << SHIFT], 0);
        data[i] = fractal;
        return fractal;
    }

    // Copy to the specified table. 
    private void copyTo(FractalTableImpl that) {
        int n = MathLib.min(this.data.length, that.data.length);
        offset &= (data.length << shift) - 1; // Makes it positive.
        int o = offset >> shift;
        if ((o + n) > data.length) { // Wrapping.
            int w = (o + n) - data.length;
            n -= w;
            System.arraycopy(data, 0, that.data, n, w);
        }
        System.arraycopy(data, o, that.data, 0, n);
        that.offset = offset - (o << shift);
    }

    private FractalTableImpl F(int i) {
        FractalTableImpl table = (FractalTableImpl) data[i];
        return (table != null) ? table : allocate(i);
    }

}