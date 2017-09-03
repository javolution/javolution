/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2016 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.util.internal;

import java.io.Serializable;

import org.javolution.lang.MathLib;
import org.javolution.util.FractalArray;

/**
 * Sparse array fractal implementation (used for higher capacity sparse arrays).
 */
public final class DenseArrayFractalImpl<E> extends FractalArray<E> {
        private static final long serialVersionUID = 0x700L;
        private static final int INNER_SHIFT = DenseArrayFractalImpl.SHIFT;
        private static final int SHIFT_INCREMENT = 8;
        private static final int SHIFT = INNER_SHIFT + SHIFT_INCREMENT;
        private static final int MAX_BLOCKS = 1 << SHIFT;
        
        @SuppressWarnings("unchecked")
        private final DenseArrayFractalImpl<E>[] blocks = new DenseArrayFractalImpl[MAX_BLOCKS];

        /** Constructor from the specified base fractal. */
        @SuppressWarnings("unchecked")
        public Block16(DenseArrayFractalImpl<E> block0) {
            blocks[0] = block0;
            for (int i=1; i < MAX_BLOCKS; i++) 
                blocks[i] = (DenseArrayFractalImpl<E>) EMPTY;
        }
        
        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // COMMON TO ALL IMPLEMENTATIONS
        // 

        private int offset; // Index of first element.
        private int last = -1; // Index of the last element different from null 
                               // -1 when empty, MAX_CAPACITY if inner fractal.

        @Override
        public boolean isEmpty() {
            return last < 0;
        }

        @Override
        public E get(int index) {
            return (index >= 0) & (index <= last) ? getNoCheck(index) : null;
        }

        @Override
        public FractalArray<E> set(int index, E element) {
            if (element == null) return clear(index);
            if (index > last) {
                if (index >= capacity()) return upsize().set(index, element);
                last = index;
            }
            setNoCheck(index, element);
            return this;
        }

        @Override
        public FractalArray<E> remove(int index) {
            if (index >= last) return clear(index); // Nothing to shift.
            if (last - index < index) {
                shiftLeft(index, last, null);
            } else {
                shiftRight(0, index, null);
                offset++;
            }
            --last;
            return isOverCapacity() ? downsize() : this;
        }

        @Override
        public FractalArray<E> add(int index, E inserted) {
            if (index > last) return set(index, inserted); // Nothing to shift.
            if (last + 1 >= capacity()) return upsize().add(index, inserted);
            if (++last - index < index) {
                shiftRight(index, last, inserted);
            } else {
                offset--;
                shiftLeft(0, index, inserted);
            }
            return this;
        }

        private FractalArray<E> clear(int index) {
            if (index > last) return this; // Nothing to clear.
            setNoCheck(index, null);
            if (index != last) return this;
            while (last >= 0)
                if (getNoCheck(--last) != null) break;
            return isOverCapacity() ? downsize() : this;
        }

        //
        // END COMMON PART
        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

        @Override
        public Block16<E> clone() {
            Block16<E> copy = (Block16<E>) super.clone();
            for (int i = 0; i < blocks.length; i++)
                if (blocks[i] != null) copy.blocks[i] = blocks[i].clone();
            return copy;
        }

        private void shiftLeft(int head, int tail, E insertTail) {
            int iHead = mask(head + offset);
            int iiHead = iHead >>> INNER_SHIFT;
            int iTail = mask(tail + offset);
            int iiTail = iTail >>> INNER_SHIFT;
            if ((iiHead == iiTail) && (iHead <= iTail)) { // Local to inner block. 
                blockAt(iiTail).shiftLeft(innerMask(iHead), innerMask(iTail), insertTail);
            } else { 
                    return blockAt(iiTail).shiftLeft(innerMask(iHead), innerMask(iTail), insertTail);
                E carry = blockAt(iiTail).shiftLeft(0, innerMask(iTail), insertTail);
                while (true) {
                     iiTail = (iiTail - 1) & (BLOCK_COUNT - 1);
                     if (iiTail == iiHead) break;
                     carry = blockAt(iiTail).shiftLeft(0, getNoCheck(0);
                     blockAt(iiTail).offset++;
                }
                return blockAt(iiHead).shiftLeft(innerMask(iHead), innerSize() - 1, carry);
        }

        public E shiftRight(int head, int tail, E insertHead) {
            if (tail - head > (capacity() >> 1)) { // Optimization
                offset += head;
                E removed = shiftLeft(tail - head, capacity() - 1, insertHead);
                offset -= head + 1;
                return removed;
            }
            int iHead = mask(head + offset);
            int iiHead = iHead >>> innerShift();
            int iTail = mask(tail + offset);
            int iiTail = iTail >>> innerShift();
            if ((iiHead == iiTail) && (iHead <= iTail)) // Local to inner block.
                return fractalAt(iiHead).shiftRight(innerMask(iHead), innerMask(iTail), insertHead);
            E carry = fractalAt(iiHead).shiftRight(innerMask(iHead), innerSize() - 1, insertHead);
            while (true) {
                iiHead = (iiHead + 1) & (BLOCK_COUNT - 1);
                if (iiHead == iiTail) break;
                carry = fractalAt(iiHead).shiftRight(carry);
            }
            return fractalAt(iiTail).shiftRight(0, innerMask(iTail), carry);
        }

        private FractalArray<E> upsize() { // Double the capacity.
            return null; // TODO;
        }

        @SuppressWarnings("unchecked")
        private FractalArray<E> downsize() {
            int newLength = DenseArrayFractalImpl.MAX_CAPACITY;
            while ((newLength > Block8.MIN_CAPACITY) && (newLength >= 4 * last))
                newLength /= 2;
            if (newLength == Block8.MAX_CAPACITY) return this;
            E[] tmp = (E[]) new Object[newLength];
            for (int i = 0; i < last; i++)
                tmp[i] = getNoCheck(i++);
            Block8<E> block = new Block8<E>();
            block.elements = tmp;
            block.last = last;
            return block;
        }

        /////////////////////////////////////////////
        // Small convenient methods to be inlined. // 
        /////////////////////////////////////////////

        private E getNoCheck(int index) {
            int i = mask(index + offset);
            return blocks[i >>> INNER_SHIFT].getNoCheck(i);
        }

        private void setNoCheck(int index, E element) {
            int i = mask(index + offset);
            blocks[i >>> INNER_SHIFT].setNoCheck(i, element);
        }

        private DenseArrayFractalImpl<E> blockAt(int index) {
            int i = mask(index + offset);
            return blocks[i >>> INNER_SHIFT];
        }

        private int mask(int i) {
            return i << (32 - SHIFT) >>> (32 - SHIFT);
        }

        private int innerMask(int i) {
            return i << (32 - INNER_SHIFT) >>> (32 - INNER_SHIFT);
        }

        private boolean isOverCapacity() {
            return DenseArrayFractalImpl.MAX_CAPACITY >= last * 4;
        }

        private int capacity() {
            return MAX_CAPACITY;
        }

    }

}
