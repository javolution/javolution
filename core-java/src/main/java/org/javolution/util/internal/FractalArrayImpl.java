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
 * Holds the fractal array default implementations.
 */
public class FractalArrayImpl  {
    
    /** Returns an empty sparse array. */
    public static <E> FractalArray<E> empty() {
        return new FractalImpl<E>();
    }
   
    /** Not Implemented Yet ! Use a standard array (no downsizing). */
    private static final class FractalImpl<E> extends FractalArray<E> {
        private static final long serialVersionUID = 0x700L;
        private final E[] data;
        private int offset;

        @SuppressWarnings("unchecked")
        public FractalImpl() {
            data = (E[])new Object[2];
        }

        private FractalImpl(E[] newData) {
            data = newData;
        }

        @Override
        public E get(int index) {
            return data[index + offset];
        }

        @Override
        public FractalArray<E> set(int index, E newElement) {
            data[index + offset] = newElement;
            return this;
        }
        
        private FractalArray<E> upsizeRight() {
            @SuppressWarnings("unchecked")
            E[] tmp = (E[])new Object[data.length * 2];
            System.arraycopy(data, 0, tmp, 0, data.length);
            return new FractalImpl<E>(tmp);
        }

        private FractalArray<E> upsizeLeft() {
            @SuppressWarnings("unchecked")
            E[] tmp = (E[])new Object[data.length * 2];
            System.arraycopy(data, 0, tmp, data.length, data.length);
            offset += data.length;
            return new FractalImpl<E>(tmp);
        }
        
        @Override
        public FractalArray<E> shiftRight(E inserted, int first, int length) {
            int last = first + length;
            if (last >= data.length) return upsizeRight().shiftRight(inserted, first, length);
            System.arraycopy(data, first, data, first+1, length);
            data[first] = inserted;
            return this;
        }

        @Override
        public FractalArray<E> shiftLeft(E inserted, int last, int length) {
            int first = last - length;
            if (first <= 0) return upsizeLeft().shiftLeft(inserted, last, length);
            System.arraycopy(data, first, data, first-1, length);
            data[last] = inserted;
            return this;
        } 

  
    }
 }
