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
import static org.javolution.lang.MathLib.unsignedMax;
import static org.javolution.lang.MathLib.unsignedMin;
import static org.javolution.lang.MathLib.max;

import org.javolution.lang.Immutable;
import org.javolution.util.FractalArray;
import org.javolution.util.function.Consumer;

/**
 * The fractal array default implementation (key class in org.javolution.util package). 
 * Each instance has a bounded capacity and returns an enclosing instance with higher capacity when its capacity 
 * is reached (no resize ever).
 */
public abstract class FractalArrayImpl<E> extends FractalArray<E> {
    private static final long serialVersionUID = 0x700L;
    
    private static final Empty<Object> EMPTY = new Empty<Object>(); // Singleton.
    
    /** Returns the immutable empty instance. */
    @SuppressWarnings("unchecked")
    public static <E> Empty<E> empty() {
         return (Empty<E>) EMPTY;
    }
        
	@Override
	public final boolean isEmpty() {
		return this == EMPTY;
	}

	@Override
	public abstract FractalArrayImpl<E> clone();

	@Override
    public abstract FractalArrayImpl<E> clear(long index);
    
    @Override
    public abstract FractalArrayImpl<E> set(long index, E element);

    @Override
    public abstract FractalArrayImpl<E> shift(long from, long to, E inserted);
  
    /** Full shift right, element at last index in range becomes the element at first index in range (return this). */
    abstract FractalArrayImpl<E> shiftRight();
  
    /** Full shift left, element at last index in range becomes the element at first index in range (return this). */
    abstract FractalArrayImpl<E> shiftLeft();
  
    /** Clears the highest bits, bits should be in range 0..63 (1L << 64 == 1L). */
    private static final long clearHighBits(long value, int bits) {
		return value << bits >>> bits;
	}

    /** Clears the lowest bits, bits should be in range 0..63 (1L << 64 == 1L). */
     private static final long clearLowBits(long value, int bits) {
		return value >>> bits << bits;
	}

   	/** The empty singleton. */
    private static final class Empty<E> extends FractalArrayImpl<E> implements Immutable {
        private static final long serialVersionUID = FractalArrayImpl.serialVersionUID;
        
        private Empty() {
        }

        @Override
        public Empty<E> clone() {
            return this; // Immutable.
        }

        @Override
        public E get(long index) {
            return null;
        }

        @Override
        public FractalArrayImpl<E> clear(long index) {
            return this;
        }
        
        @Override
        public FractalArrayImpl<E> set(long index, E element) {
        	if (element == null) return this;
        	return new Single<E>(index, element);
        }
    
        @Override
        public FractalArrayImpl<E> shift(long from, long to, E inserted) {
            return set(from, inserted);
        }

        @Override
        public long ceiling(long minIndex, Consumer<? super E> found) {
            return 0;
        }

        @Override
        public long floor(long maxIndex, Consumer<? super E> found) {
            return -1;
        }

		@Override
		FractalArrayImpl<E> shiftRight() {
			return this;
		}

		@Override
		FractalArrayImpl<E> shiftLeft() {
			return this;
		}
        
    }
 
    /** A single element. */
    private static final class Single<E> extends FractalArrayImpl<E>  {
        private static final long serialVersionUID = FractalArrayImpl.serialVersionUID;
        private long index;
        private E element;
    
        public Single(long index, E element) {
            this.index = index;
            this.element = element;
        }
        
        @Override
        public Single<E> clone() {
            return new Single<E>(index, element);
        }

        @Override
        public E get(long i) {
            return (index == i) ? element : null;
        }
      
        @Override
        public FractalArrayImpl<E> clear(long i) {
            if(index == i) return empty();
            return this;
        }
      
        @Override
        public FractalArrayImpl<E> set(long i, E e) {
        	if (e == null) return clear(i);
        	if (index != i) return overflow(i, e);
        	element = e;
        	return this;  		
         }        

        @Override
        public FractalArrayImpl<E> shift(long from, long to, E inserted) {
       		if (to == index) { // Current element is discarded.
    			index = from;
    			element = inserted;
    			return this;
    		}
         	if (unsignedLessThan(from, to)) { // Right shift.
        		if (!unsignedLessThan(index, from) && !unsignedLessThan(to, index)) index++;
        	} else { // Left shift.
           		if (!unsignedLessThan(index, to) && !unsignedLessThan(from, index)) index--;
        	}
    		return set(from, inserted);
        }

        @Override
        public long ceiling(long minIndex, Consumer<? super E> found) {
            if (unsignedLessThan(index, minIndex)) return 0;
            found.accept(element);
            return index;
        }

        @Override
        public long floor(long maxIndex, Consumer<? super E> found) {
            if (unsignedLessThan(maxIndex, index)) return -1;
            found.accept(element);
            return index;
        }

		@Override
		FractalArrayImpl<E> shiftRight() {
			return this;
		}

		@Override
		FractalArrayImpl<E> shiftLeft() {
			return this;
		}

		private FractalArrayImpl<E> overflow(long i, E e) {
        	return ((index ^ i) >>> Array.INDEX_SIZE == 0) ? new Array<E>(index, element, i, e) :
        		new Fractal<E>(i, e, index, this);
        }
    }

    /** An array of elements. */
    private static final class Array<E> extends FractalArrayImpl<E>  {
        private static final long serialVersionUID = FractalArrayImpl.serialVersionUID;
        private static final int INDEX_SIZE = 4;
    	private static final int ARRAY_LENGTH = 1 << INDEX_SIZE;
    	private static final int MASK = ARRAY_LENGTH - 1;
    		     			
    	private final E[] elements;
    	private long prefix; // Lower bits holding offset.  
    	private int count; // Number of non-null elements (> 1)
    	
    	@SuppressWarnings("unchecked")
		public Array(long i0, E e0, long i1, E e1) {
    		elements = (E[]) new Object[ARRAY_LENGTH];
    	    prefix = i0 >>> (64 - INDEX_SIZE) << (64 - INDEX_SIZE);
    	    elements[(int) (i0 - prefix)] = e0;
       	    elements[(int) (i1 - prefix)] = e1;
            count = 2;       	    	    
    	}
    	
    	private Array(Array<E> that) {
    		elements = that.elements.clone();
    		prefix = that.prefix;
    		count = that.count;
    	}

		@Override
		public E get(long index) {
			return inRange(index) ? elements[arrayIndex(index)] : null;
		}

		@Override
		public FractalArrayImpl<E> clear(long index) {
			if (!inRange(index)) return this;
			int i = arrayIndex(index);
			if (elements[i] == null) return this;
			elements[i] = null;
			return (--count > 1) ? this : anyInner(index);
		}		

		@Override
		public FractalArrayImpl<E> set(long index, E element) {
			if (element == null) return clear(index);
			if (!inRange(index)) return new Fractal<E>(index, element, prefix, this);
			int i = arrayIndex(index);
			if (elements[i] == null) ++count;
			elements[i] = element;
			return this;
		}

		@Override
		public FractalArrayImpl<E> shift(long from, long to, E inserted) {
         	if (unsignedLessThan(from, to)) { // Right shift.
         		if (isUnderflow(from)) return shift(firstIndex(), to, null).set(from, inserted);
         		if (isOverflow(to)) return set(lastIndex() + 1, get(lastIndex())).shift(from, lastIndex(), inserted);
         		if ((to - from) > ARRAY_LENGTH / 2) // Faster to do full shift right and two shifts left.
         			return shift(lastIndex(), to, get(firstIndex())).shift(from, firstIndex(), inserted).shiftRight();
         		if (inserted != null) count++; // non-null introduced.
         		for (int i = arrayIndex(from), j=arrayIndex(to);;i = ++i & MASK) {
         			E previous = elements[i];
         			elements[i] = inserted;
         			inserted = previous;
          	        if (i == j) break;
         		} 
         		if (inserted != null) count--; // inserted is discarded.
        	} else { // Left shift.
         		if (isUnderflow(to)) return set(firstIndex() - 1, get(firstIndex())).shift(from, firstIndex(), inserted);
         		if (isOverflow(from)) return shift(lastIndex(), to, null).set(from, inserted);
         		if ((from - to) > ARRAY_LENGTH / 2) // Faster to do full shift left and two shifts right.
         			return shift(firstIndex(), to, get(lastIndex())).shift(from, lastIndex(), inserted).shiftLeft();
         		if (inserted != null) count++; // non-null introduced.
         		for (int i = arrayIndex(from), j=arrayIndex(to);;i = --i & MASK) {
         			E previous = elements[i];
         			elements[i] = inserted;
         			inserted = previous;
          	        if (i == j) break;
         		} 
         		if (inserted != null) count--; // inserted is discarded.
        	}
			return (count > 1) ? this : anyInner(from);
		}

		@Override
		public Array<E> clone() {
			return new Array<E>(this);
		}


		@Override
		public long ceiling(long minIndex, Consumer<? super E> found) {
	   		for (long i=minIndex; inRange(i); i++) {
    			E e = elements[arrayIndex(i)];
    			if (e == null) continue;
    			found.accept(e);  
    			return i;
    		}
    		return 0;
 		}

		@Override
		public long floor(long maxIndex, Consumer<? super E> found) {
	  		for (long i=maxIndex; inRange(i); i--) {
    			E e = elements[arrayIndex(i)];
    			if (e == null) continue;
    			found.accept(e);  
    			return i;
    		}
    		return -1;
 		}
		
		@Override
     	Array<E> shiftRight() { 
     		prefix = clearLowBits(prefix, INDEX_SIZE)  | clearHighBits(prefix - 1, 64 - INDEX_SIZE);
    		return this;
    	}
  
		@Override
     	Array<E> shiftLeft() { 
     		prefix = clearLowBits(prefix, INDEX_SIZE)  | clearHighBits(prefix + 1, 64 - INDEX_SIZE);
    		return this;
    	}
		
		private FractalArrayImpl<E> anyInner(long lastCleared) { // Called when count == 1
			int j = arrayIndex(lastCleared);
			for (int i = (j-1) & MASK; i != j; i = --i & MASK)   // Go backward from last cleared.
				if (elements[i] != null) return new Single<E>(indexFor(i), elements[i]);
			throw new IllegalStateException("Corruption, single element not found!");
		}

		private boolean inRange(long index) { // Check prefix equality 
    		return (index ^ prefix) >>> INDEX_SIZE == 0;
    	}
    	
       	private boolean isUnderflow(long index) { // Too small. 
    		return (index >>> INDEX_SIZE) < (prefix >>> INDEX_SIZE);
    	}
    	
       	private boolean isOverflow(long index) { // Too large. 
    		return (index >>> INDEX_SIZE) > (prefix >>> INDEX_SIZE);
    	}

       	private long firstIndex() {  
    		return clearLowBits(prefix, INDEX_SIZE); 
    	}
  	
       	private long lastIndex() {  
    		return firstIndex() + MASK;  
    	}
  	
     	private int arrayIndex(long index) {  
    		return (int) clearHighBits(index + prefix, 64 - INDEX_SIZE);
    	}
  	
     	private long indexFor(int arrayIndex) { 
     		return clearLowBits(prefix, INDEX_SIZE) | clearHighBits(arrayIndex - prefix, 64 - INDEX_SIZE);
    	}
   	
    }
    
    /** The fractal structure. */
    private static final class Fractal<E> extends FractalArrayImpl<E>  {
        private static final long serialVersionUID = FractalArrayImpl.serialVersionUID;
        private static final int SIZE_INC = 4; 
    	private static final int ARRAY_LENGTH = 1 << SIZE_INC;
    	private static final int MASK = ARRAY_LENGTH - 1;
    	@SuppressWarnings("rawtypes")
		private static final FractalArrayImpl[] EMPTY_INIT;
    	static {
    		EMPTY_INIT = new FractalArrayImpl[ARRAY_LENGTH];
    		for (int i=0; i < ARRAY_LENGTH; i++) EMPTY_INIT[i] = EMPTY;
    	}

    	private final FractalArrayImpl<E>[] inners;
    	private long prefix; 
    	private final int innerIndexSize; // Always in range [Array.INDEX_SIZE .. 63] by construction.
       	private int count; // Number of non-empty inner fractals.
        
    	@SuppressWarnings("unchecked")
		public Fractal(long index, E element, long innerPrefix, FractalArrayImpl<E> inner) {
     		inners = EMPTY_INIT.clone();
    		int s = Array.INDEX_SIZE;
    		long l = innerPrefix ^ index;
    		while ((l >>> s >>> SIZE_INC) != 0)  s += SIZE_INC;
    		innerIndexSize = s;
    		prefix = index >>> s >>> SIZE_INC << SIZE_INC << s;
    		inners[(int) (index - prefix) >>> s] = new Single<E>(index, element);
    		inners[(int) (innerPrefix - prefix) >>> s] = inner;
            count = 2;       	    	    
    	}
    	
    	private Fractal(Fractal<E> that) {
    		inners = that.inners.clone();
    		prefix = that.prefix;
    		innerIndexSize = that.innerIndexSize;
    		count = that.count;
    		for (int i=0; i < ARRAY_LENGTH; i++) inners[i] = inners[i].clone();
    	}

    	@Override
		public E get(long index) {
    		return inRange(index) ? inners[arrayIndex(index)].get(subIndex(index)) : null;
		}

		@Override
		public FractalArrayImpl<E> clear(long index) {
			if (!inRange(index)) return this;
			int i = arrayIndex(index);
			FractalArrayImpl<E> fractal = inners[i];
			if (fractal.isEmpty()) return this;
			FractalArrayImpl<E> newFractal = fractal.clear(subIndex(index));
			if (newFractal != fractal) inners[i] = newFractal;
			return newFractal.isEmpty() && (--count < 2) ? anyInner(index) : this;
		}		

		@Override
		public FractalArrayImpl<E> set(long index, E element) {
			if (element == null) return clear(index);
			if (!inRange(index)) return new Fractal<E>(index, element, prefix, this);
			int i = arrayIndex(index);
			FractalArrayImpl<E> fractal = inners[i];
			if (fractal.isEmpty()) count++;
			FractalArrayImpl<E> newFractal = fractal.set(subIndex(index), element);
			if (newFractal != fractal) inners[i] = newFractal;	
			return this;
		}

		@Override
		public FractalArrayImpl<E> shift(long from, long to, E inserted) {
         	if (unsignedLessThan(from, to)) { // Right shift.
         		if (isUnderflow(from)) return shift(firstIndex(), to, null).set(from, inserted);
         		if (isOverflow(to)) return set(lastIndex() + 1, get(lastIndex())).shift(from, lastIndex(), inserted);
         		if ((to - from) >>> innerIndexSize > ARRAY_LENGTH / 2) // Faster to do full shift right and two shifts left.
         			return shift(lastIndex(), to, get(firstIndex())).shift(from, firstIndex(), inserted).shiftRight();
         		for (int i = arrayIndex(from), j=arrayIndex(to);;i = ++i & MASK) {
         			long first = unsignedMax(indexFor(i, 0), from);
         			long last = unsignedMin(indexFor(i, 1L << innerIndexSize - 1L), to);
        			FractalArrayImpl<E> fractal = inners[i];
        			if (fractal.isEmpty()) count++;
        			E carry = fractal.get(subIndex(last));
        			FractalArrayImpl<E> newFractal = fractal.shift(subIndex(first), subIndex(last), inserted);
        			if (newFractal.isEmpty()) count--;
        			if (fractal != newFractal) inners[i] = newFractal;
          			inserted = carry;
          	        if (i == j) break;
         		} 
        	} else { // Left shift.
         		if (isUnderflow(to)) return set(firstIndex() - 1, get(firstIndex())).shift(from, firstIndex(), inserted);
         		if (isOverflow(from)) return shift(lastIndex(), to, null).set(from, inserted);
         		if ((from - to) >>> innerIndexSize > ARRAY_LENGTH / 2) // Faster to do full shift left and two shifts right.
         			return shift(firstIndex(), to, get(lastIndex())).shift(from, lastIndex(), inserted).shiftLeft();
         		for (int i = arrayIndex(from), j=arrayIndex(to);;i = --i & MASK) {
         			long first = unsignedMax(indexFor(i, 0), from);
         			long last = unsignedMin(indexFor(i, 1L << innerIndexSize - 1L), to);
        			FractalArrayImpl<E> fractal = inners[i];
        			if (fractal.isEmpty()) count++;
        			E carry = fractal.get(subIndex(first));
        			FractalArrayImpl<E> newFractal = fractal.shift(subIndex(last), subIndex(first), inserted);
        			if (newFractal.isEmpty()) count--;
        			if (fractal != newFractal) inners[i] = newFractal;
          			inserted = carry;
          	        if (i == j) break;
         		} 
        	}
			return (count > 1) ? this : anyInner(from);
		}

		@Override
		public Fractal<E> clone() {
			return new Fractal<E>(this);
		}

		@Override
		public long ceiling(long minIndex, Consumer<? super E> found) {
	   		for (long i=minIndex; inRange(i); i += 1L << innerIndexSize) {
	   			int aIndex = arrayIndex(i);
	   			FractalArrayImpl<E> fractal = inners[aIndex];
	   			long sIndex = subIndex(i);
	   			long foundIndex = fractal.ceiling(sIndex, found);
	   			if ((foundIndex == 0) && ((sIndex != 0) || (fractal.get(0) == null))) continue; // Not found.
      			return indexFor(aIndex, sIndex);
    		}
    		return 0;
		}

		@Override
		public long floor(long maxIndex, Consumer<? super E> found) {
	   		for (long i=maxIndex; inRange(i); i -= 1L << innerIndexSize) {
	   			int aIndex = arrayIndex(i);
	   			FractalArrayImpl<E> fractal = inners[aIndex];
	   			long sIndex = subIndex(i);
	   			long foundIndex = fractal.floor(sIndex, found);
	   			if ((foundIndex == -1) && ((sIndex != -1) || (fractal.get(-1) == null))) continue; // Not found.
      			return indexFor(aIndex, sIndex);
    		}
    		return -1;
		}
		
		@Override
     	Fractal<E> shiftRight() { 
     		prefix = (clearLowBits(prefix >>> SIZE_INC, innerIndexSize) << SIZE_INC) | 
     				clearHighBits(prefix - 1, max(0, 64 - SIZE_INC - innerIndexSize));
    		return this;
    	}
  
		@Override
     	Fractal<E> shiftLeft() { 
     		prefix = (clearLowBits(prefix >>> SIZE_INC, innerIndexSize) << SIZE_INC) | 
     				clearHighBits(prefix + 1, max(0, 64 - SIZE_INC - innerIndexSize));
    		return this;
    	}
		
		private FractalArrayImpl<E> anyInner(long lastCleared) { // Called when count == 1
			int j = arrayIndex(lastCleared);
			for (int i = (j-1) & MASK; i != j; i = --i & MASK)   // Go backward from last cleared.
				if (inners[i] != null) return inners[i];
			throw new IllegalStateException("Corruption, single inner fractal not found!");
		}

		private boolean inRange(long index) { // Check prefix equality 
    		return (index ^ prefix) >>> SIZE_INC >>> innerIndexSize == 0;
    	}
    	
       	private boolean isUnderflow(long index) { // Too small. 
    		return (index >>> SIZE_INC >>> innerIndexSize) < (prefix >>> SIZE_INC >>> innerIndexSize);
    	}
    	
       	private boolean isOverflow(long index) { // Too large. 
    		return (index >>> SIZE_INC >>> innerIndexSize) > (prefix >>> SIZE_INC >>> innerIndexSize);
    	}

       	private long firstIndex() {  
    		return clearLowBits(prefix >>> SIZE_INC, innerIndexSize) << SIZE_INC; 
    	}
  	
       	private long lastIndex() {  
    		return firstIndex() + (1L << SIZE_INC << innerIndexSize) - 1L; 
    	}
  	
     	private int arrayIndex(long index) {  
    		return (int) clearHighBits((index + prefix) >> innerIndexSize, 64 - SIZE_INC);
    	}
  	
     	private long subIndex(long index) {  
    		return (int) clearHighBits(index + prefix, 64 - innerIndexSize);
    	}
  	
     	private long indexFor(int arrayIndex, long subIndex) { 
     		return (clearLowBits(prefix >>> SIZE_INC, innerIndexSize) << SIZE_INC) | 
     				clearHighBits((arrayIndex << innerIndexSize) + subIndex - prefix, max(0, 64 - SIZE_INC - innerIndexSize));
    	}
   	   		    
    }  
  
    
}
