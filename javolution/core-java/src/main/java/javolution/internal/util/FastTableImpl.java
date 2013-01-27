/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.internal.util;

import javolution.lang.Copyable;
import javolution.text.TextBuilder;
import javolution.util.AbstractTable;

/**
 * A fast table implementation with fast insertion/deletion capabilities
 * regardless of the collection size (see test results). 
 * It is based on a fractal structure with self-similar patterns at any scale
 * (large tables have the same structure as smaller tables which have similar 
 * structure as even smaller tables and so on). 
 * This implementation ensures that no more than 3/4 of the table capacity is
 * ever wasted. 
 */
public final class FastTableImpl<E> extends AbstractTable<E> {

    private static final int SHIFT = 2;

    private static final int BASE_CAPACITY_MIN = 2;

    private static final int BASE_CAPACITY_MAX = 1 << SHIFT;

    private FractalTable fractal;

    private int size;

    private int capacity; // Actual memory allocated is usually far less than
    // capacity since inner fractal tables can be null.

    public FastTableImpl() {
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public E get(int index) {
        if ((index < 0) && (index >= size)) indexError(index);
        return (E) fractal.get(index);
    }

    @Override
    public E set(int index, E element) {
        if ((index < 0) && (index >= size)) indexError(index);
        return (E) fractal.set(index, element);
    }

    @Override
    public void add(int index, E element) {
        if (index == 0) {
            addFirst(element);
        } else if (index == size) {
            addLast(element);
        } else {
            if ((index < 0) || (index > size)) indexError(index);
            checkUpsize();
            if (index >= (size >> 1))
                fractal.shiftRight(element, index, size - index);
            else {
                fractal.shiftLeft(element, index - 1, index);
                fractal.offset--;
            }
            size++;
        }
    }

    @Override
    public E remove(int index) {
        if (index == 0) return removeFirst();
        if (index == (size - 1)) return removeLast();
        if ((index < 0) || (index >= size)) indexError(index);
        checkDownsize();
        E removed = (E) fractal.get(index);
        if (index >= (size >> 1)) {
            fractal.shiftLeft(null, size - 1, size - index - 1);
        } else {
            fractal.shiftRight(null, 0, index);
            fractal.offset++;
        }
        size--;
        return removed;
    }

    @Override
    public FastTableImpl<E> copy() {
        FastTableImpl<E> that = new FastTableImpl<E>();
        for (int i = 0; i < size; i++) {
            E e = get(i);
            that.add((e instanceof Copyable) ? ((Copyable<E>) e).copy() : e);
        }
        return that;
    }

    //
    // Optimizations.
    //
    @Override
    public boolean add(E element) {
        addLast(element);
        return true;
    }

    @Override
    public void addFirst(E element) {
        checkUpsize();
        fractal.offset--;
        fractal.set(0, element);
        size++;
    }

    @Override
    public void addLast(E element) {
        checkUpsize();
        fractal.set(size++, element);
    }

    @Override
    public E removeFirst() {
        if (size == 0) emptyError();
        checkDownsize();
        E first = (E) fractal.get(0);
        fractal.offset++;
        size--;
        return first;
    }

    @Override
    public E removeLast() {
        if (size == 0) emptyError();
        checkDownsize();
        E last = (E) fractal.get(--size);
        return last;
    }

    @Override
    public void clear() {
        fractal = null;
        capacity = 0;
        size = 0;
    }

    //
    // Utilities.
    //
    private void checkUpsize() {
        if (size >= capacity) upsize();
    }

    private void upsize() {
        fractal = (fractal == null) ? new FractalTable(0) : fractal.upsize();
        capacity = fractal.capacity();
    }

    private void checkDownsize() {
        if ((size < (capacity >> 2)) && (capacity > BASE_CAPACITY_MIN))
            downsize();
    }

    private void downsize() {
        fractal = fractal.downsize();
        capacity = fractal.capacity();
    }

    /** Fractal table (unbounded through structural recursion). */
    private static final class FractalTable {

        /** The index shift, zero if primitive table. */
        private final int shift;

        /** Offset value, it is the index of the first element (always positive). */
        private int offset;

        /** An array of data elements (primitive tables) or fractal tables. 
         Data length varies from 2 (or BASE_CAPACITY_MIN for primitive) to BASE_CAPACITY_MAX.  */
        private Object[] data;

        public FractalTable(int shift) {
            this.shift = shift;
            data = new Object[shift == 0 ? BASE_CAPACITY_MIN : 2];
        }

        public FractalTable(int shift, Object[] data, int offset) {
            this.shift = shift;
            this.data = data;
            this.offset = offset;
        }

        public Object get(int index) {
            Object fractal = data[((index + offset) >> shift) & (data.length - 1)];
            return (shift == 0) ? fractal : ((FractalTable) fractal).get(index + offset);
        }

        public Object set(int index, Object element) {
            int i = ((index + offset) >> shift) & (data.length - 1);
            Object previous = data[i];
            if (shift == 0) {
                data[i] = element;
                return previous;
            }
            FractalTable fractal = (previous != null) ? (FractalTable) previous : allocate(i);
            return fractal.set(index + offset, element);
        }

        public FractalTable upsize() {
            offset &= (data.length << shift) - 1; // Makes it positive.
            if (data.length >= BASE_CAPACITY_MAX) {
                FractalTable table = new FractalTable(shift + SHIFT);
                table.offset = offset;
                offset = 0;
                table.data[0] = this;
                table.data[1] = this.splitAt(table.offset);
                return table;
            }
            Object[] tmp = new Object[data.length << 1];
            int i = offset >> shift;
            System.arraycopy(data, i, tmp, i, data.length - i);
            System.arraycopy(data, 0, tmp, data.length, i);
            if (shift > 0) {
                assert (((FractalTable) data[i]).offset == 0);
                tmp[data.length + i] = ((FractalTable) data[i]).splitAt(offset);
            }
            data = tmp;
            return this;
        }

        /** Splits this table in two at index */
        private FractalTable splitAt(int index) {
            int i = ((index + offset) >> shift) & (data.length - 1);
            Object[] tmp = new Object[data.length];
            System.arraycopy(data, 0, tmp, 0, i);
            System.arraycopy(NULL, 0, data, 0, i); // Dereference for GC.
            if (shift != 0) tmp[i] = ((FractalTable) data[i]).splitAt(index);
            return new FractalTable(shift, tmp, offset);
        }

        public String toString() {
            TextBuilder txt = new TextBuilder();
            txt.append("Offset: " + offset);
            for (int i = 0; i < data.length; i++) {
                txt.append('{').append(data[i]).append('}');
            }
            return txt.toString();
        }

        private static final Object[] NULL = new Object[BASE_CAPACITY_MAX];

        public FractalTable downsize() {
            if (true) return this;
            if (data.length == 1) return ((FractalTable) data[0]).downsize();
            Object[] tmp = new Object[data.length >> 1];
            int first = offset & (data.length - 1);
            int length = tmp.length;
            if (first + length > data.length) { // Wrapping
                length = data.length - first;
                System.arraycopy(data, 0, tmp, length, tmp.length - length);
            }
            System.arraycopy(data, first, tmp, 0, length);
            data = tmp;
            offset = 0;
            return this;
        }

        /** Shifts the elements specified [first, first + length[ one position to the right (length &lt; data.length) */
        public void shiftRight(Object inserted, int first, int length) {
            if (shift == 0) {
                int i = (first + offset) & (data.length - 1);
                int w = i + length - data.length + 1;
                if (w > 0) { // Wrapping.
                    if (w > 1) System.arraycopy(data, 0, data, 1, w - 1);
                    data[0] = data[data.length - 1];
                    length -= w;
                }
                System.arraycopy(data, i, data, i + 1, length);
                data[i] = inserted;
            } else {
                shiftRightFractal(inserted, first, length);
            }
        }

        /** Shifts the elements specified ]last - length, last] one position to the left (length &lt; data.length) */
        public void shiftLeft(Object inserted, int last, int length) {
            if (shift == 0) {
                int i = (last + offset) & (data.length - 1);
                int w = length - i;
                if (w > 0) { // Wrapping.
                    if (w > 1)
                        System.arraycopy(data, data.length - w + 1, data, data.length - w, w - 1);
                    data[data.length - 1] = data[0];
                    length -= w;
                }
                System.arraycopy(data, i - length + 1, data, i - length, length);
                data[i] = inserted;
            } else {
                shiftLeftFractal(inserted, last, length);
            }
        }

        //
        // Fractal implementation.
        // 
        private FractalTable allocate(int i) {
            FractalTable fractal = new FractalTable(shift - SHIFT, new Object[1 << SHIFT], 0);
            data[i] = fractal;
            return fractal;
        }

        private void shiftRightFractal(Object inserted, int first, int length) {
            int i = (first + offset) & ((data.length << shift) - 1);
            int j = (first + offset + length) & ((data.length << shift) - 1);
            FractalTable high = (FractalTable) data[(j >> shift)];
            for (int jj = (j >> shift); jj != (i >> shift);) {
                jj = (jj - 1) & (data.length - 1);
                FractalTable low = (FractalTable) data[jj];
                high.offset--; // Full shift right.
                high.set(0, low.get(-1));
                high = low;
            }
            int mask = (1 << shift) - 1;
            if ((i >> shift) == (j >> shift)) { // Partially filled insertion table.
                high.shiftRight(inserted, i & mask, length & mask);
            } else {
                high.shiftRight(inserted, i & mask, mask - (i & mask));
            }
        }

        private void shiftLeftFractal(Object inserted, int last, int length) {
            int i = (last + offset) & ((data.length << shift) - 1);
            int j = (last + offset - length) & ((data.length << shift) - 1);
            FractalTable low = (FractalTable) data[(j >> shift)];
            for (int jj = (j >> shift); jj != (i >> shift);) {
                jj = (jj + 1) & (data.length - 1);
                FractalTable high = (FractalTable) data[jj];
                low.offset++; // Full shift left.
                low.set(-1, high.get(0));
                low = high;
            }
            int mask = (1 << shift) - 1;
            if ((i >> shift) == (j >> shift)) { // Partially filled insertion table.
                low.shiftLeft(inserted, i & mask, length & mask);
            } else {
                low.shiftLeft(inserted, i & mask, i & mask);
            }
        }

        private int capacity() {
            return data.length << shift;
        }
    }
}
