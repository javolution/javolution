/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.internal.util.table;



/**
 * A fractal-based implementation of a table with fast insertion/deletion 
 * capabilities regardless of the collection size. 
 * It is based on a fractal structure with self-similar patterns at any scale
 * (large tables have the same structure as smaller tables which have similar 
 * structure as even smaller tables and so on). 
 * This implementation ensures that no more than 3/4 of the table capacity is
 * ever wasted. 
 */
public final class FractalTableImpl<E> extends AbstractTableImpl<E>  {

    private static final int SHIFT = 10;

    private static final int BASE_CAPACITY_MIN = 16;

    private static final int BASE_CAPACITY_MAX = 1 << SHIFT;

    private FractalTable fractal;

    private int size;

    private int capacity; // Actual memory allocated is usually far less than
    // capacity since inner fractal tables can be null.

    public FractalTableImpl() {
    }

    @Override
    public int size() {
        return size;
    }

    @SuppressWarnings("unchecked")
    @Override
    public E get(int index) {
        if ((index < 0) && (index >= size)) indexError(index);
        return (E) fractal.get(index);
    }

    @SuppressWarnings("unchecked")
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
            if (index >= (size >> 1)) {
                fractal.shiftRight(element, index, size - index);
            } else {
                fractal.shiftLeft(element, index - 1, index);
                fractal.offset--;
            }
            size++;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public E remove(int index) {
        if (index == 0) return removeFirst();
        if (index == (size - 1)) return removeLast();
        if ((index < 0) || (index >= size)) indexError(index);
        E removed = (E) fractal.get(index);
        if (index >= (size >> 1)) {
            fractal.shiftLeft(null, size - 1, size - index - 1);
        } else {
            fractal.shiftRight(null, 0, index);
            fractal.offset++;
        }
        size--;
        checkDownsize();
        return removed;
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

    @SuppressWarnings("unchecked")
    @Override
    public E removeFirst() {
        if (size == 0) emptyError();
        E first = (E) fractal.set(0, null);
        fractal.offset++;
        size--;
        checkDownsize();
        return first;
    }

    @SuppressWarnings("unchecked")
    @Override
    public E removeLast() {
        if (size == 0) emptyError();
        E last = (E) fractal.set(--size, null);
        checkDownsize();
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
        fractal = (fractal == null) ? new FractalTable() : fractal.upsize();
        capacity = fractal.capacity();
    }

    private void checkDownsize() {
        if ((capacity > BASE_CAPACITY_MIN) && (size <= (capacity >> 2)))
            downsize();
    }

    private void downsize() {
        fractal = fractal.downsize(size);
        capacity = fractal.capacity();
    }

    /** Fractal table (unbounded through structural recursion). */
    private static final class FractalTable {

        /** The index shift, zero if base table. */
        private final int shift;

        /** Offset value, it is the index of the first element (modulo data.length). */
        private int offset;

        /** An array of data elements or fractal tables (recursion). 
         Data length varies from 2 to BASE_CAPACITY_MAX  */
        private Object[] data;

        public FractalTable() {
            this.shift = 0;
            data = new Object[BASE_CAPACITY_MIN];
        }

        public FractalTable(int shift) {
            this.shift = shift;
            data = new Object[2];
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
            if (shift != 0) return F(i).set(index + offset, element);
            Object previous = data[i];
            data[i] = element;
            return previous;
        }

        public FractalTable upsize() {
            offset &= (data.length << shift) - 1; // Makes it positive.
            if (data.length >= BASE_CAPACITY_MAX) {
                FractalTable table = new FractalTable(shift + SHIFT);
                table.offset = offset;
                this.offset = 0;
                table.data[0] = this;
                table.data[1] = this.extract(0, table.offset);
                return table;
            }
            Object[] tmp = new Object[data.length << 1];
            int i = (offset >> shift);
            System.arraycopy(data, i, tmp, i, data.length - i);
            System.arraycopy(data, 0, tmp, data.length, i);
            if (shift > 0) tmp[data.length + i] = F(i).extract(0, offset);
            data = tmp;
            return this;
        }

        public FractalTable downsize(int minsize) {
            if (data.length == 1) {
                F(0).offset += offset;
                return F(0).downsize(minsize);
            }
            int length = data.length >> 1; // New length.
            int alignment = offset & ((1 << shift) - 1);
            if (alignment != 0) { // Align subtables.
                if (alignment <= (1 << (shift - 1))) { // Left shift.
                    for (int i = 0; i < alignment; i++) {
                        this.shiftLeft(null, minsize - 1, minsize);
                        offset--;
                    }
                } else { // Right shift.
                    alignment = (1 << shift) - alignment;
                    for (int i = 0; i < alignment; i++) {
                        this.shiftRight(null, 0, minsize);
                        offset++;
                    }
                }
            }
            Object[] tmp = new Object[length];
            int i = (offset >> shift) & (data.length - 1);
            if (i + length > data.length) { // Wrapping
                System.arraycopy(data, 0, tmp, data.length - i, i - length);
                length = data.length - i;
            }
            System.arraycopy(data, i, tmp, 0, length);
            data = tmp;
            offset = 0;
            return this;
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
                F(head >> shift).shiftRight(inserted, head, length);             // (no wrapping).
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
                F(head >> shift).shiftLeft(inserted, tail, length);              // (no wrapping).
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

        /** Extracts the specified elements if any.
         Nothing extracted if length (modulo capacity) is zero.*/
        public FractalTable extract(int first, int length) {
            FractalTable result = new FractalTable(shift, new Object[data.length], offset);
            int mask = (data.length << shift) - 1;
            int head = (first + offset) & mask;
            int tail = (first + offset + length) & mask;
            int hh = head >> shift;
            int tt = tail >> shift;
            if (hh != tt) {
                int n = tt - hh;
                if (head > tail) { // Wrapping
                    System.arraycopy(data, 0, result.data, 0, tt);
                    System.arraycopy(NULL, 0, data, 0, tt); // Dereference for GC. 
                    n = data.length - hh;
                }
                System.arraycopy(data, hh, result.data, hh, n);
                System.arraycopy(NULL, hh, data, hh, n); // Dereference for GC.
                if (shift != 0) {
                    data[hh] = result.F(hh).extract(0, head);
                    result.data[tt] = F(tt).extract(0, tail);
                }
            } else { // Head and tail in the same inner table.
                if (head > tail) { // Wrapping.
                    switchData(this, result);
                    data[hh] = result.F(hh).extract(tail, head - tail);
                } else if (shift != 0) {
                    result.data[hh] = F(hh).extract(head, tail - head);
                }
            }
            return result;
        }

        private static final Object[] NULL = new Object[BASE_CAPACITY_MAX];

        //
        // Misc. utilities.
        //
        private int capacity() {
            return data.length << shift;
        }

        private FractalTable F(int i) {
            FractalTable table = (FractalTable) data[i];
            return (table != null) ? table : allocate(i);
        }

        private FractalTable allocate(int i) {
            FractalTable fractal = new FractalTable(shift - SHIFT, new Object[1 << SHIFT], 0);
            data[i] = fractal;
            return fractal;
        }

        private static void switchData(FractalTable left, FractalTable right) {
            Object[] tmp = left.data;
            left.data = right.data;
            right.data = tmp;
        }
    }

    private static final long serialVersionUID = 8016235981181245144L;

}
