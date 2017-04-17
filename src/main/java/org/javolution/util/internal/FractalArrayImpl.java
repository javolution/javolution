/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2016 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.util.internal;

import org.javolution.util.FractalArray;

/**
 * Holds fractal array default implementation.
 */
public final class FractalArrayImpl<E> extends FractalArray<E> {

    private static final long serialVersionUID = 0x700L;
    private static final FractalArrayImpl<Object> INSTANCE = new FractalArrayImpl<Object>();

    /**  Prevents instantiation (singleton). */
    private FractalArrayImpl() {
    }

    /** Returns an empty instance. */
    @SuppressWarnings("unchecked")
    public static final <E> FractalArray<E> newInstance() {
        return (FractalArray<E>) INSTANCE;
    }

    @Override
    public E get(int index) {
        if (index < 0)
            throw new ArrayIndexOutOfBoundsException();
        return null;
    }

    @Override
    public FractalArray<E> set(int index, E element) {
        if (index < 0)
            throw new ArrayIndexOutOfBoundsException();
        return (element != null) ? new Block<E>().set(index, element) : this;
    }

    @Override
    public FractalArray<E> clone() {
        return this;
    }

    @Override
    public FractalArray<E> add(int index, E inserted) {
        if (index < 0)
            throw new ArrayIndexOutOfBoundsException();
        if (inserted == null)
            return this;
        return new Block<E>().add(index, inserted);
    }

    @Override
    public FractalArray<E> remove(int index) {
        if (index < 0)
            throw new ArrayIndexOutOfBoundsException();
        return null;
    }

    /** Fractal Block Base Class. */
    private static abstract class FractalBlock<E> extends FractalArray<E> {
        private static final long serialVersionUID = 0x700L;
        public static final int SHIFT_INCREMENT = 4;

        public int offset; // Index of elements[0]
        public int last = -1; // Index of last element different from null (not maintained when inner block)

        @Override
        public FractalArray<E> set(int index, E element) {
            if (index < 0)
                throw new ArrayIndexOutOfBoundsException();
            if (element == null)
                return clear(index);
            if (index >= size())
                return newOuter().set(index, element);
            setElementAt(index, element);
            if (index > last)
                last = index;
            return this;
        }

        @SuppressWarnings("unchecked")
        public FractalArray<E> clear(int index) {
            if (index > last) // Nothing to clear.
                return this;
            setElementAt(index, null);
            if (index == last) { // Decreases last.
                while (--last >= 0)
                    if (elementAt(last) != null)
                        return this;
                return (FractalArray<E>) INSTANCE;
            }
            return this;
        }

        @Override
        public E get(int index) {
            if (index < 0)
                throw new ArrayIndexOutOfBoundsException();
            return (index <= last) ? elementAt(index) : null;
        }

        @Override
        public FractalArray<E> remove(int index) {
            if (index < 0)
                throw new ArrayIndexOutOfBoundsException();
            if (index >= last)
                return (index == last) ? clear(last) : this;
            if (last - index <= index) {
                shiftLeft(index, last--, null);
            } else { // shift right should be faster.
                shiftRight(0, index, null);
                ++offset; // Full shift left.
                --last;
            }
            return this;
        }

        @Override
        public FractalArray<E> add(int index, E inserted) {
            if (index < 0)
                throw new ArrayIndexOutOfBoundsException();
            if (index > last) // Nothing to shift.
                return set(index, inserted);
            if (last == size() - 1) // Overflow.
                return newOuter().add(index, inserted);
            if (last - index <= index) {
                shiftRight(index, ++last, inserted);
            } else { // shift left should be faster.
                --offset;
                ++last;
                shiftLeft(0, index, inserted);
            }
            return this;
        }

        @Override
        public FractalBlock<E> clone() {
            return (FractalBlock<E>) super.clone();
        }

        public abstract FractalBlock<E> newOuter();

        public abstract E shiftLeft(int head, int tail, E insertTail);

        public abstract E shiftRight(int head, int tail, E insertHead);

        public abstract E shiftLeft(E element); // Full shift left.

        public abstract E shiftRight(E element); // Full shift right.

        public abstract E elementAt(int index);

        public abstract void setElementAt(int index, E element);

        public abstract int shift();

        public final int size() {
            return 1 << shift();
        }
    }

    private static final class Block<E> extends FractalBlock<E> {
        private static final long serialVersionUID = 0x700L;
        public static final int SHIFT = 4;
        public static final int SIZE = 1 << SHIFT;

        @SuppressWarnings("unchecked")
        private E[] elements = (E[]) new Object[SIZE]; // At least one element is non-null

        @Override
        public Block4<E> newOuter() {
            Block4<E> block = new Block4<E>();
            block.elements[0] = this;
            block.last = last;
            return block;
        }

        @Override
        public Block<E> clone() {
            Block<E> copy = (Block<E>) super.clone();
            copy.elements = elements.clone();
            return copy;
        }

        @Override
        public E shiftLeft(int head, int tail, E insertTail) {
            if (tail - head > SIZE / 2) { // Optimization
                offset += head + 1;
                E removed = shiftRight(tail - head, SIZE - 1, insertTail);
                offset -= head;
                return removed;
            }
            E removed = elementAt(head);
            for (int i = head; i < tail;)
                setElementAt(i, elementAt(++i));
            setElementAt(tail, insertTail);
            return removed;
        }

        @Override
        public E shiftRight(int head, int tail, E insertHead) {
            if (tail - head > SIZE / 2) { // Optimization
                offset += head;
                E removed = shiftLeft(tail - head, SIZE - 1, insertHead);
                offset -= head + 1;
                return removed;
            }
            E removed = elementAt(tail);
            for (int i = tail; i > head;)
                setElementAt(i, elementAt(--i));
            setElementAt(head, insertHead);
            return removed;
        }

        @Override
        public E shiftLeft(E element) { // Full shift left.
            int i = mask(offset++);
            E carry = elements[i];
            elements[i] = element;
            return carry;
        }

        @Override
        public E shiftRight(E element) { // Full shift right.
            int i = mask(--offset);
            E carry = elements[i];
            elements[i] = element;
            return carry;
        }

        @Override
        public E elementAt(int index) {
            return elements[mask(index + offset)];
        }

        @Override
        public void setElementAt(int index, E element) {
            elements[mask(index + offset)] = element;
        }

        @Override
        public final int shift() {
            return SHIFT;
        }

        private static int mask(int i) {
            return i << (32 - SHIFT) >>> (32 - SHIFT);
        }
    }

    private static abstract class OuterBlock<E> extends FractalBlock<E> {
        private static final long serialVersionUID = 0x700L;
        protected static final int BLOCK_COUNT = 1 << SHIFT_INCREMENT;

        @Override
        public FractalBlock<E> clone() {
            OuterBlock<E> copy = (OuterBlock<E>) super.clone();
            int iZero = mask(offset);
            int iiZero = iZero >>> innerShift();
            int iLast = mask(last + offset);
            int iiLast = iLast >>> innerShift();
            if ((iiZero == iiLast) && (iZero < iLast))
                return blockAt(iiZero).clone();
            for (int i = iiZero;; i = (i + 1) & (BLOCK_COUNT - 1)) {
                copy.setBlockAt(i, blockAt(i).clone());
                if (i == iLast)
                    break;
            }
            return copy;
        }

        @SuppressWarnings("unchecked")
        public FractalArray<E> clear(int index) {
            if (index > last) // Nothing to clear.
                return this;
            setElementAt(index, null);
            if (index == last) { // Decreases last.
                int iZero = mask(offset);
                int iiZero = iZero >>> innerShift();
                while (last >= 0) { // While last element is null.
                    int iLast = mask(last + offset);
                    int iiLast = iLast >>> innerShift();
                    if ((iiLast << innerShift() == iLast) && (iiLast != iiZero)) 
                        setBlockAt(iiLast, null); // Empty block.
                    if (elementAt(--last) != null) { // Exit.
                        if ((iiLast == iiZero) && (iZero < iLast)) { // Single block left!
                            FractalBlock<E> lastBlock = blockAt(iiLast);
                            lastBlock.last = last;
                            lastBlock.offset += offset;                            
                            return lastBlock;
                        }
                        return this;
                    }
                }
                return (FractalArray<E>) INSTANCE;
            }
            return this;
        }

        @Override
        public E shiftLeft(int head, int tail, E insertTail) {
            if (tail - head > size() / 2) { // Optimization
                offset += head + 1;
                E removed = shiftRight(tail - head, size() - 1, insertTail);
                offset -= head;
                return removed;
            }
            int iHead = mask(head + offset);
            int iiHead = iHead >>> innerShift();
            int iTail = mask(tail + offset);
            int iiTail = iTail >>> innerShift();
            if ((iiHead == iiTail) && (iHead <= iTail)) // Local to inner block.
                return blockAt(iiTail).shiftLeft(innerMask(iHead), innerMask(iTail), insertTail);
            E carry = blockAt(iiTail).shiftLeft(0, innerMask(iTail), insertTail);
            while (true) {
                iiTail = (iiTail - 1) & (BLOCK_COUNT - 1);
                if (iiTail == iiHead)
                    break;
                carry = blockAt(iiTail).shiftLeft(carry);
            }
            return blockAt(iiHead).shiftLeft(innerMask(iHead), innerSize() - 1, carry);
        }

        public E shiftRight(int head, int tail, E insertHead) {
            if (tail - head > size() / 2) { // Optimization
                offset += head;
                E removed = shiftLeft(tail - head, size() - 1, insertHead);
                offset -= head + 1;
                return removed;
            }
            int iHead = mask(head + offset);
            int iiHead = iHead >>> innerShift();
            int iTail = mask(tail + offset);
            int iiTail = iTail >>> innerShift();
            if ((iiHead == iiTail) && (iHead <= iTail)) // Local to inner block.
                return blockAt(iiHead).shiftRight(innerMask(iHead), innerMask(iTail), insertHead);
            E carry = blockAt(iiHead).shiftRight(innerMask(iHead), innerSize() - 1, insertHead);
            while (true) {
                iiHead = (iiHead + 1) & (BLOCK_COUNT - 1);
                if (iiHead == iiTail)
                    break;
                carry = blockAt(iiHead).shiftRight(carry);
            }
            return blockAt(iiTail).shiftRight(0, innerMask(iTail), carry);
        }

        @Override
        public E shiftLeft(E element) {
            int i = mask(offset++);
            E carry = blockAt(i >>> innerShift()).elementAt(i);
            blockAt(i >>> innerShift()).setElementAt(i, element);
            return carry;
        }

        @Override
        public E shiftRight(E element) {
            int i = mask(--offset);
            E carry = blockAt(i >>> innerShift()).elementAt(i);
            blockAt(i >>> innerShift()).setElementAt(i, element);
            return carry;
        }

        @Override
        public E elementAt(int index) {
            int i = mask(index + offset);
            return blockAt(i >>> innerShift()).elementAt(i);
        }

        @Override
        public void setElementAt(int index, E element) {
            int i = mask(index + offset);
            blockAt(i >>> innerShift()).setElementAt(i, element);
        }

        private final int mask(int i) {
            return i << (32 - shift()) >>> (32 - shift());
        }

        private final int innerShift() {
            return shift() - SHIFT_INCREMENT;
        }

        public final int innerMask(int i) {
            return i << (32 - innerShift()) >>> (32 - innerShift());
        }

        public final int innerSize() {
            return 1 << innerShift();
        }

        public abstract FractalBlock<E> blockAt(int i); // Creates a new inner block if it does not exist.

        public abstract void setBlockAt(int i, FractalBlock<E> block);

    }

    private static final class Block4<E> extends OuterBlock<E> {
        private static final long serialVersionUID = 0x700L;
        private static final int SHIFT = Block.SHIFT + SHIFT_INCREMENT;

        @SuppressWarnings("unchecked")
        private final Block<E>[] elements = new Block[BLOCK_COUNT];

        @Override
        public Block8<E> newOuter() {
            Block8<E> block = new Block8<E>();
            block.elements[0] = this;
            block.last = last;
            return block;
        }

        @Override
        public Block<E> blockAt(int i) {
            Block<E> block = elements[i];
            return (block != null) ? block : (elements[i] = new Block<E>());
        }

        @Override
        public void setBlockAt(int i, FractalBlock<E> block) {
            elements[i] = (Block<E>) block;
        }

        @Override
        public final int shift() {
            return SHIFT;
        }

    }

    private static final class Block8<E> extends OuterBlock<E> {
        private static final long serialVersionUID = 0x700L;
        private static final int SHIFT = Block4.SHIFT + SHIFT_INCREMENT;

        @SuppressWarnings("unchecked")
        private final Block4<E>[] elements = new Block4[BLOCK_COUNT];

        @Override
        public Block12<E> newOuter() {
            Block12<E> block = new Block12<E>();
            block.elements[0] = this;
            block.last = last;
            return block;
        }

        @Override
        public Block4<E> blockAt(int i) {
            Block4<E> block = elements[i];
            return (block != null) ? block : (elements[i] = new Block4<E>());
        }

        @Override
        public void setBlockAt(int i, FractalBlock<E> block) {
            elements[i] = (Block4<E>) block;
        }

        @Override
        public final int shift() {
            return SHIFT;
        }

    }

    private static final class Block12<E> extends OuterBlock<E> {
        private static final long serialVersionUID = 0x700L;
        private static final int SHIFT = Block8.SHIFT + SHIFT_INCREMENT;

        @SuppressWarnings("unchecked")
        private final Block8<E>[] elements = new Block8[BLOCK_COUNT];

        @Override
        public Block16<E> newOuter() {
            Block16<E> block = new Block16<E>();
            block.elements[0] = this;
            block.last = last;
            return block;
        }

        @Override
        public Block8<E> blockAt(int i) {
            Block8<E> block = elements[i];
            return (block != null) ? block : (elements[i] = new Block8<E>());
        }

        @Override
        public void setBlockAt(int i, FractalBlock<E> block) {
            elements[i] = (Block8<E>) block;
        }

        @Override
        public final int shift() {
            return SHIFT;
        }

    }

    private static final class Block16<E> extends OuterBlock<E> {
        private static final long serialVersionUID = 0x700L;
        private static final int SHIFT = Block12.SHIFT + SHIFT_INCREMENT;

        @SuppressWarnings("unchecked")
        private final Block12<E>[] elements = new Block12[BLOCK_COUNT];

        @Override
        public Block20<E> newOuter() {
            Block20<E> block = new Block20<E>();
            block.elements[0] = this;
            block.last = last;
            return block;
        }

        @Override
        public Block12<E> blockAt(int i) {
            Block12<E> block = elements[i];
            return (block != null) ? block : (elements[i] = new Block12<E>());
        }

        @Override
        public void setBlockAt(int i, FractalBlock<E> block) {
            elements[i] = (Block12<E>) block;
        }

        @Override
        public final int shift() {
            return SHIFT;
        }

    }

    private static final class Block20<E> extends OuterBlock<E> {
        private static final long serialVersionUID = 0x700L;
        private static final int SHIFT = Block16.SHIFT + SHIFT_INCREMENT;

        @SuppressWarnings("unchecked")
        private final Block16<E>[] elements = new Block16[BLOCK_COUNT];

        @Override
        public Block24<E> newOuter() {
            Block24<E> block = new Block24<E>();
            block.elements[0] = this;
            block.last = last;
            return block;
        }

        @Override
        public Block16<E> blockAt(int i) {
            Block16<E> block = elements[i];
            return (block != null) ? block : (elements[i] = new Block16<E>());
        }

        @Override
        public void setBlockAt(int i, FractalBlock<E> block) {
            elements[i] = (Block16<E>) block;
        }

        @Override
        public final int shift() {
            return SHIFT;
        }

    }

    private static final class Block24<E> extends OuterBlock<E> {
        private static final long serialVersionUID = 0x700L;
        private static final int SHIFT = Block20.SHIFT + SHIFT_INCREMENT;

        @SuppressWarnings("unchecked")
        private final Block20<E>[] elements = new Block20[BLOCK_COUNT];

        @Override
        public Block28<E> newOuter() {
            Block28<E> block = new Block28<E>();
            block.elements[0] = this;
            block.last = last;
            return block;
        }

        @Override
        public Block20<E> blockAt(int i) {
            Block20<E> block = elements[i];
            return (block != null) ? block : (elements[i] = new Block20<E>());
        }

        @Override
        public void setBlockAt(int i, FractalBlock<E> block) {
            elements[i] = (Block20<E>) block;
        }

        @Override
        public final int shift() {
            return SHIFT;
        }

    }

    private static final class Block28<E> extends OuterBlock<E> {
        private static final long serialVersionUID = 0x700L;
        private static final int SHIFT = Block24.SHIFT + SHIFT_INCREMENT;

        @SuppressWarnings("unchecked")
        private final Block24<E>[] elements = new Block24[BLOCK_COUNT];

        @Override
        public OuterBlock<E> newOuter() {
            throw new UnsupportedOperationException("FractalArray Overflow!");
        }

        @Override
        public Block24<E> blockAt(int i) {
            Block24<E> block = elements[i];
            return (block != null) ? block : (elements[i] = new Block24<E>());
        }

        @Override
        public void setBlockAt(int i, FractalBlock<E> block) {
            elements[i] = (Block24<E>) block;
        }

        @Override
        public final int shift() {
            return SHIFT;
        }
    }

}
