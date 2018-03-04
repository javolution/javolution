/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2016 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.util.internal;

import static org.javolution.lang.MathLib.unsignedLessThan;

import org.javolution.lang.Immutable;
import org.javolution.util.FractalArray;
import org.javolution.util.function.Consumer;

/**
 * The fractal array default implementation (key for all Javolution classes). 
 */
public abstract class FractalArrayImpl<E> extends FractalArray<E> {
    private static final long serialVersionUID = 0x700L;
    
    private static final Empty<Object> EMPTY = new Empty<Object>();

    @SuppressWarnings("unchecked")
    public static <E> Empty<E> empty() {
         return (Empty<E>) EMPTY;
    }
    
    @Override
    public final boolean isEmpty() {
        return this == EMPTY;
    }
    
    /** Empty array singleton (unbounded). */
    private static final class Empty<E> extends FractalArrayImpl<E> implements Immutable {
        private static final long serialVersionUID = FractalArrayImpl.serialVersionUID;

        @Override
        public FractalArray<E> clone() {
            return this; // Immutable.
        }

        @Override
        public E get(int index) {
            return null;
        }

        @Override
        public FractalArray<E> set(int index, E element) {
            if (element == null) return this;
            return Block.inRange(index) ? new Block<E>(index, element) : new Single<E>(index, element);
        }
    
        @Override
        public FractalArray<E> insert(int index, E element) {
            if (element == null) return this;
            return Block.inRange(index) ? new Block<E>(index, element) : new Single<E>(index, element);
        }

        @Override
        public FractalArray<E> remove(int index) {
            return this;
        }

        @Override
        public int ceiling(int minIndex, Consumer<? super E> found) {
            return -1;
        }

        @Override
        public int floor(int maxIndex, Consumer<? super E> found) {
            return 0;
        }

        
    }

    /** Single element (unbounded). 
     *  Only for indices greater or equal to Block.LENGTH to extra cost of Trie structures. */
    private static final class Single<E> extends FractalArrayImpl<E>  {
        private static final long serialVersionUID = FractalArrayImpl.serialVersionUID;
     
        private int index; // Greater or equal to Block.LENGTH
        private E element;

        public Single(int index, E element) {
            this.index = index;
            this.element = element;
        }
        
        @Override
        public FractalArray<E> clone() {
            return new Single<E>(index, element);
        }

        @Override
        public E get(int i) {
            return (index == i) ? element : null;
        }

        @Override
        public FractalArray<E> set(int i, E e) {
            if (index == i) {
                if (e == null) return empty();    
                element = e;
                return this;
            } 
            if (e == null) return this;
            return (Block.inRange(index)) ? new Block<E>(index, element).set(i, e) : new Trie<E>(this).set(i, e); 
        }

        @Override
        public FractalArray<E> insert(int i, E e) {
            if (!unsignedLessThan(index, i)) index++; 
            if (e == null) return this;
            return (Block.inRange(index)) ? new Block<E>(index, element).set(i, e) : new Trie<E>(this).set(i, e); 
        }
    
        @Override
        public FractalArray<E> remove(int i) {
            if (i == index) return empty();
            if (unsignedLessThan(i, index)) --index; 
            return this;
        }

        @Override
        public int ceiling(int minIndex, Consumer<? super E> found) {
            if (unsignedLessThan(index, minIndex)) return -1;
            found.accept(element);
            return index;
        }

        @Override
        public int floor(int maxIndex, Consumer<? super E> found) {
            if (unsignedLessThan(maxIndex, index)) return 0;
            found.accept(element);
            return index;
        }
        
    }

    /** Block (bounded, only for small indices). */
    private static final class Block<E> extends FractalArrayImpl<E>  {
        private static final long serialVersionUID = FractalArrayImpl.serialVersionUID;
        private static final int INDEX_BIT_SIZE = 4;
        private static final int LENGTH = 1 << INDEX_BIT_SIZE; 

        private final E[] elements;
        
        @SuppressWarnings("unchecked")
        public Block(int index, E element) { // index < LENGTH
            elements = (E[]) new Object[LENGTH];
            elements[index] = element;
        }
        
        private Block(E[] elements) {
            this.elements = elements;
        }
        
        @Override
        public FractalArray<E> clone() {
            return new Block<E>(elements.clone());
        }

        @Override
        public E get(int index) {
            return inRange(index) ? elements[index] : null;
        }
       
        @Override
        public FractalArray<E> set(int index, E element) {
            if (!inRange(index)) return (element != null) ? new Trie<E>(this).set(index, element) : this;
            elements[index] = element;
            return (element != null) ? this : checkEmpty();
        }

        @Override
        public FractalArray<E> insert(int index, E element) {
            if (!inRange(index)) return set(index, element);
            E carry = elements[LENGTH-1];
            for (int i = LENGTH-1; i > index;) elements[i] = elements[--i];
            elements[index] = element;
            if (carry == null) return this;
            if (element != null) return new Trie<E>(this).set(LENGTH, carry);
            return checkEmpty().set(LENGTH,  carry);
        }

        @Override
        public FractalArray<E> remove(int index) {
            if (!inRange(index)) return this;
            for (int i=index; i < LENGTH-1;) elements[i] = elements[++i];
            elements[LENGTH-1] = null;
            return checkEmpty();
        }

        @Override
        public int ceiling(int minIndex, Consumer<? super E> found) {
            for (int i=minIndex; inRange(i); i++) {
                E e = elements[i];
                if (e == null) continue;
                found.accept(e);  
                return i;
            }
            return -1;
        }

        @Override
        public int floor(int maxIndex, Consumer<? super E> found) {
            if (!inRange(maxIndex)) maxIndex = LENGTH-1;    
            for (int i=maxIndex; inRange(i); --i)  {
                E e = elements[i];
                if (e == null) continue;
                found.accept(e);
                return i;
            }
            return 0;
        }
        
        private FractalArray<E> checkEmpty() { // Returns empty() if no element set. 
            for (E e : elements) if (e != null) return this;
            return empty();
        }
        
        private static boolean inRange(int index) {
            return index >>> INDEX_BIT_SIZE == 0;
        }
    }

    /** Trie / Fractal Structure. */
    private static final class Trie<E> extends FractalArrayImpl<E>  {
        private static final long serialVersionUID = FractalArrayImpl.serialVersionUID;
        private static final int SHIFT_INC = 4;
        private static final int LENGTH = 1 << SHIFT_INC;
  
        private final FractalArray<E>[] fractals;
        private int shift; // Bit size of inner fractals (>= Block.INDEX_BIT_SIZE).
        private int count; // Number of inner fractals (> 1 except at creation)
        
        @SuppressWarnings("unchecked")
        public Trie(FractalArray<E> inner) {
             fractals = new FractalArrayImpl[LENGTH];
             for (int i=0; i < LENGTH; i++) fractals[i] = empty();
             
             shift = Block.INDEX_BIT_SIZE;
             int maxIndex = inner.floor(-1, Consumer.DO_NOTHING);
             while (maxIndex >>> (shift + SHIFT_INC) != 0) shift += SHIFT_INC;
             fractals[maxIndex >>> shift] = inner;
             count = 1;
        }
        
        private Trie(FractalArray<E>[] fractals, int shift, int count) {
            this.fractals = fractals;
            this.shift = shift;
            this.count = count;
        }
        
        @Override
        public E get(int index) {
            int i = index >>> shift;    
            return (i < LENGTH) ? fractals[i].get(inner(index)) : null;
        }
        
        @Override
        public FractalArray<E> set(int index, E element) {
            int i = index >>> shift;
            if (i >= LENGTH) return (element != null) ? new Trie<E>(this).set(index, element) : this;
            FractalArray<E> oldFractal = fractals[i]; 
            FractalArray<E> newFractal = oldFractal.set(inner(index), element);
            if (oldFractal == newFractal) return this;
            fractals[i] = newFractal;
            updateCount(oldFractal, newFractal);
            return (count == 1) ? single() : this;
        }
 
        @Override
        public FractalArray<E> remove(int index) {
            int i = index >>> shift;
            if (i >= LENGTH) return this; // Out of range.
            int ii = inner(index);
            do { 
                FractalArray<E> oldFractal = fractals[i];                
                FractalArray<E> newFractal = oldFractal.remove(ii);
                if (i + 1 < LENGTH) newFractal = newFractal.set(inner(-1), fractals[i+1].get(0));
                ii = 0;
                if (oldFractal != newFractal) {
                    fractals[i] = newFractal;
                    updateCount(oldFractal, newFractal);
                }
            } while (++i < LENGTH);
            return (count <= 1) ? single() : this;
        }
      
        @Override
        public FractalArray<E> insert(int index, E element) {
            int i = index >>> shift;
            if (i >= LENGTH) return set(index, element); // Out of range.
            int ii = inner(index);
            do {
                FractalArray<E> oldFractal = fractals[i];
                E carry = oldFractal.get(inner(-1));
                FractalArray<E> newFractal = oldFractal.set(inner(-1), null); // Clears last element to avoid overflow.
                newFractal = newFractal.insert(ii, element);
                ii = 0;
                element = carry;
                if (oldFractal != newFractal) {
                    fractals[i] = newFractal;
                    updateCount(oldFractal, newFractal);
                }
            } while (++i < LENGTH); 
            FractalArray<E> that = (count <= 1) ? single() : this;
            return (element != null) ? that.set(LENGTH << shift, element) : that;
        }
        
        @Override
        public int ceiling(int index, Consumer<? super E> found) {
            for (int i = index >>> shift, ii=inner(index); i < LENGTH; i++) {
                 int innerIndex = fractals[i].ceiling(ii, found);
                 if (innerIndex != -1) return (i << shift) | innerIndex; // No ambiguity, -1 cannot be an inner index.
                 ii = 0;
            }
            return -1;
        }
        
        @Override
        public int floor(int index, Consumer<? super E> found) {
            if (index >>> shift >= LENGTH) index = (LENGTH << shift) - 1; // Out of bounds.
            for (int i = index >>> shift, ii=inner(index); i >= 0; --i) {
                int innerIndex = fractals[i].floor(ii, found);
                if ((innerIndex != 0) || (fractals[i].get(0) != null)) return (i << shift) | innerIndex;
                ii = inner(-1);
            }
            return 0;
        }
  
        @Override
        public FractalArray<E> clone() {
            Trie<E> trie = new Trie<E>(fractals.clone(), shift, count);    
            for (int i=0; i < trie.fractals.length; i++) trie.fractals[i] = trie.fractals[i].clone();
            return trie;
        }
        
        /** Returns inner index. */
        private int inner(int i) {
            return i << (32 - shift) >>> (32 - shift);
        }
        
        /** Returns the single inner fractal when (count == 1). */
        private FractalArray<E> single() {
            for (int i=0; i < LENGTH; i++) if (fractals[i] != EMPTY) return fractals[i];
            throw new Error("Array Corruption");
        }
        
        /** Updates count of inner fractal. */
        private void updateCount(FractalArray<E> oldFractal, FractalArray<E> newFractal) {
            if ((oldFractal == EMPTY) && (newFractal != EMPTY)) count++;
            if ((oldFractal != EMPTY) && (newFractal == EMPTY)) count--;
        }       

    }
    
}
