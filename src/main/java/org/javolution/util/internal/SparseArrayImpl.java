/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2016 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.util.internal;

import java.util.Iterator;

import org.javolution.lang.Immutable;
import org.javolution.lang.MathLib;
import org.javolution.util.SparseArray;

/**
 * Holds the sparse array default implementations.
 */
public class SparseArrayImpl  {

    /** SparseArray inner structure must be implemented by array elements which must propagate cloning. */
    public interface Inner<K, E> extends Cloneable {
        Inner<K, E> clone();
        Iterator<E> iterator();
        Iterator<E> iterator(K from);
        Iterator<E> descendingIterator();
        Iterator<E> descendingIterator(K from);
    }
    
    /** Holds the unique empty instance. */
    private static Empty<Object> EMPTY = new Empty<Object>();
    
    /** Returns an empty sparse array. */
    @SuppressWarnings("unchecked")
    public static <E> SparseArray<E> empty() {
        return (SparseArray<E>) EMPTY;
    }
        
    /** Empty sparse array implementation (singleton). */
    private static final class Empty<E> extends SparseArray<E> implements Immutable {
         private static final long serialVersionUID = 0x700L; 

         @Override
         public final E get(int index) {
             return null;
         }
         
         @Override
         public SparseArray<E> set(int index, E element) {
             return (element != null) ? new Unary<E>(index, element) : this;
         }

         @Override
         public final int next(int after) {
             return 0;    
         }

         @Override
         public final int previous(int before) {
             return -1;    
         }

         @Override
         public Empty<E> clone() {
             return this; // Immutable.
         }
         
         @Override
         public boolean equals(Object obj) {
             return obj instanceof Empty;
         }

         @Override
         public int hashCode() {
             return 0;
         }

    }    

    /** Sparse array holding a single element . */
    private static final class Unary<E> extends SparseArray<E> {
        private static final long serialVersionUID = 0x700L; 
        private final int index;
        private E element; // Always different from null.
        
        private Unary(int index, E element) {
            this.index = index;
            this.element = element;
        }
  
        @Override
        public E get(int i) {
            return (index == i) ? element : null;
        }            

        @Override
        public SparseArray<E> set(int i, E e) {
            if (e == null) if (index == i) return empty(); else return this;
            if (index != i) return new Trie<E>(index, element, i, e);
            element = e;
            return this;
        }
       
        @Override
        public int next(int after) {
            return MathLib.unsignedLessThan(after, index) ? index : 0;
        }

        @Override
        public int previous(int before) {
            return MathLib.unsignedLessThan(index, before) ? index : -1;
        }
  
    }
    
    /** Sparse Array Trie Structure. */
    private static final class Trie<E> extends SparseArray<E> {
        private static final long serialVersionUID = 0x700L; 
        private static final int SHIFT = 4;
        private static final int SIZE = 1 << SHIFT;
        private static final int MASK = SIZE - 1;         
        
        private final Object[] trie; 
        private final int[] indices;
        private final int shift;
        private final int prefix;
        private int count; // Always greater than l. 
        
        private Trie(int xIndex, Object x, int yIndex, Object y) {
            trie = new Object[SIZE];
            indices = new int[SIZE];
            shift = commonShift(xIndex, yIndex);
            prefix = xIndex >>> shift >>> SHIFT;
            indices[(xIndex >>> shift) & MASK] = xIndex;
            trie[(xIndex >>> shift) & MASK] = x;
            indices[(yIndex >>> shift) & MASK] = yIndex;
            trie[(yIndex >>> shift) & MASK] = y;
            count = 2;
        }
        
        private Trie(Object[] trie, int[] indices, int shift, int prefix, int count) { // For cloning (ok since final)
            this.trie = trie;
            this.indices = indices;
            this.shift = shift;
            this.prefix = prefix;
            this.count = count;
        }
        
        @SuppressWarnings("unchecked")
        @Override
        public E get(int index) {
            if (isOutOfRange(index)) return null;
            int i = (index >>> shift) & MASK; 
            Object obj = trie[i];
            if (obj instanceof Trie) return ((Trie<E>)obj).get(index);
            return (obj != null) && (indices[i] == index) ? (E) obj : null;
        }            

        @SuppressWarnings("unchecked")
        @Override
        public SparseArray<E> set(int index, E element) {
            if (element == null) return remove(index);
            if (isOutOfRange(index)) return new Trie<E>(prefix << shift << SHIFT, this, index, element);
            int i = (index >>> shift) & MASK;
            Object obj = trie[i];
            if (obj instanceof Trie) {
                 SparseArray<E> tmp = ((Trie<E>)obj).set(index, element);
                 if (tmp != obj) trie[i] = tmp;
            } else if (obj == null) {
                trie[i] = element;
                indices[i] = index;
                count++;
            } else {
                int j = indices[i];
                trie[i] = (j == index) ? element : new Trie<E>(j, obj, index, element);
            }
            return this;
        }
         
        /** Sets null element at specified index. */
        @SuppressWarnings("unchecked")
        private SparseArray<E> remove(int index) {
            if (isOutOfRange(index)) return this;
            int i = (index >>> shift) & MASK;
            Object obj = trie[i];
            if (obj instanceof Trie) { 
                SparseArray<E> tmp = ((Trie<E>)obj).remove(index);
                if (obj != tmp) {
                     if (tmp instanceof Unary) { // We can access unary directly.
                          Unary<E> unary = (Unary<E>) tmp;
                          trie[i] = unary.element;
                          indices[i] = unary.index;
                     } else {
                         trie[i] = tmp;
                     }
                }
            } else if ((obj != null) && (indices[i] == index)) {
                trie[i] = null;
                if (--count == 1) {
                    int j = nonNull();
                    obj = trie[j];
                    return (obj instanceof Trie) ? (SparseArray<E>)obj : new Unary<E>(indices[j], (E)obj); 
                }
            }
            return this;
        }
        
        @Override
        public int next(int after) {
            int from = after + 1;
            if (from == 0) return 0; // Overflow.
            if (isOutOfRange(from)) { // Does not contains from.
                if (MathLib.unsignedLessThan(prefix, from >>> shift >>> SHIFT)) return 0; // All elements < from.
                from = 0; // All elements are after.
            }
            for(int i=(from >>> shift) & MASK; i < SIZE; i++) {
                Object obj = trie[i];
                if (obj instanceof Trie) {
                    @SuppressWarnings("unchecked")
                    int n = ((Trie<E>)obj).next(after);
                    if (n != 0) return n;
                } else if (obj != null) {
                    int index = indices[i];
                    if (MathLib.unsignedLessThan(after, index)) return index; 
                }
            }
            return 0;
        }

        @Override
        public int previous(int before) {
            int from = before - 1;
            if (from == -1) return -1; // Overflow.
            if (isOutOfRange(from)) { // Does not contains from.
                if (MathLib.unsignedLessThan(from >>> shift >>> SHIFT, prefix)) return -1; // All elements > from.
                from = -1; // All elements are before.
            }
            for(int i=(from >>> shift) & MASK; i < SIZE; i++) {
                Object obj = trie[i];
                if (obj instanceof Trie) {
                    @SuppressWarnings("unchecked")
                    int n = ((Trie<E>)obj).previous(before);
                    if (n != -1) return n;
                } else if (obj != null) {
                    int index = indices[i];
                    if (MathLib.unsignedLessThan(index, before)) return index; 
                }
            }
            return -1;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Trie<E> clone() {
            Trie<E> copy = new Trie<E>(trie.clone(), indices.clone(), shift, prefix, count);
            for (int i=0; i < SIZE; i++) {
                Object obj = copy.trie[i];
                if (obj instanceof SparseArray) {
                    SparseArray<E> subTrie  = (SparseArray<E>) obj;
                    copy.trie[i] = subTrie.clone();
                } else if (obj instanceof Inner) {
                    Inner<?, E> internal  = (Inner<?,E>) obj;
                    copy.trie[i] = internal.clone();
                }
            }
            return copy;
        }

        /** Returns the trie index of a non-null element. */
        private int nonNull() {
            for (int i=0; i < SIZE; i++) 
                if (trie[i] != null) return i; 
            return -1;
        }
        
        /** Indicates if the specified index if out of range. */
        private boolean isOutOfRange(int index) {
            return index >>> shift >>> SHIFT != prefix;
        }
    }
 
    /**
     * Returns the minimal shift for two indices (based on common high-bits which can be masked).
     */
    private static int commonShift(int i, int j) {
        int xor = i ^ j;
        if ((xor & 0xFFFF0000) == 0)
            if ((xor & 0xFFFFFF00) == 0)
                if ((xor & 0xFFFFFFF0) == 0)
                    return 0;
                else
                    return 4;
            else if ((xor & 0xFFFFF000) == 0)
                return 8;
            else
                return 12;
        else if ((xor & 0xFF000000) == 0)
            if ((xor & 0xFFF00000) == 0)
                return 16;
            else
                return 20;
        else if ((xor & 0xF0000000) == 0)
            return 24;
        else
            return 28;
    }

 }
