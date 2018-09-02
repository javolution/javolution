/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2016 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.util.internal;

import static org.javolution.lang.MathLib.max;
import static org.javolution.lang.MathLib.unsignedLessThan;
import static org.javolution.lang.MathLib.unsignedMax;
import static org.javolution.lang.MathLib.unsignedMin;

import org.javolution.lang.Immutable;
import org.javolution.util.FractalArray;
import org.javolution.util.function.Predicate;

/**
 * The fractal array default implementation (key class in org.javolution.util package). 
 * Each instance has a bounded capacity and returns an enclosing instance with higher capacity when its capacity 
 * is reached (no resize ever).
 */
public abstract class FractalArrayImpl<E> extends FractalArray<E> {
    private static final long serialVersionUID = 0x700L;    
    private static final Empty<Object> EMPTY = new Empty<Object>(); // Singleton.
    
    /** High bits hold the prefix while the low bits may be holding an offset if any. */ 
    long prefix; // In the form: <--- prefix ---><--- offset --->
    
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
        public long ceiling(long minIndex, Predicate<? super E> matching) {
            return 0;
        }

        @Override
        public long floor(long maxIndex, Predicate<? super E> matching) {
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
        private E element;
    
        public Single(long index, E element) {
            this.prefix = index;
            this.element = element;
        }
        
        @Override
        public Single<E> clone() {
            return new Single<E>(prefix, element);
        }

        @Override
        public E get(long i) {
            return (prefix == i) ? element : null;
        }
      
        @Override
        public FractalArrayImpl<E> clear(long i) {
            if(prefix == i) return empty();
            return this;
        }
      
        @Override
        public FractalArrayImpl<E> set(long i, E e) {
        	if (e == null) return clear(i);
        	if (prefix != i) return overflow(i, e);
        	element = e;
        	return this;  		
         }        

        @Override
        public FractalArrayImpl<E> shift(long from, long to, E inserted) {
       		if (to == prefix) { // Current element is discarded.
    			prefix = from;
    			element = inserted;
    			return this;
    		}
         	if (unsignedLessThan(from, to)) { // Right shift.
        		if (!unsignedLessThan(prefix, from) && !unsignedLessThan(to, prefix)) prefix++;
        	} else { // Left shift.
           		if (!unsignedLessThan(prefix, to) && !unsignedLessThan(from, prefix)) prefix--;
        	}
    		return set(from, inserted);
        }

        @Override
        public long ceiling(long minIndex, Predicate<? super E> matching) {
            return !unsignedLessThan(prefix, minIndex) && matching.test(element) ? prefix : 0;
        }

        @Override
        public long floor(long maxIndex, Predicate<? super E> matching) {
            return !unsignedLessThan(maxIndex, prefix) && matching.test(element) ? prefix : -1;
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
        	return ((prefix ^ i) >>> Array.INDEX_SIZE == 0) ? new Array<E>(prefix, element, i, e) :
        		new Fractal<E>(i, e, this);
        }
    }

    /** An array of elements (for elements very close together; e.g. having the same most of high bits). */
    private static final class Array<E> extends FractalArrayImpl<E>  {
        private static final long serialVersionUID = FractalArrayImpl.serialVersionUID;
        private static final int INDEX_SIZE = 4;
    	private static final int ARRAY_LENGTH = 1 << INDEX_SIZE;
    	private static final int MASK = ARRAY_LENGTH - 1;
    		     			
    	private final E[] elements;
    	private int count; // Number of non-null elements (> 1)
    	
    	@SuppressWarnings("unchecked")
		public Array(long i0, E e0, long i1, E e1) {
    		elements = (E[]) new Object[ARRAY_LENGTH];
    	    prefix = clearLowBits(i0, INDEX_SIZE);
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
			return (--count > 1) ? this : anyInner();
		}		

		@Override
		public FractalArrayImpl<E> set(long index, E element) {
			if (element == null) return clear(index);
			if (!inRange(index)) return new Fractal<E>(index, element, this);
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
         		for (int i = arrayIndex(from), n=arrayIndex(to);;i = ++i & MASK) {
         			E previous = elements[i];
         			elements[i] = inserted;
         			inserted = previous;
          	        if (i == n) break;
         		} 
         		if (inserted != null) count--; // inserted is discarded.
        	} else { // Left shift.
         		if (isUnderflow(to)) return set(firstIndex() - 1, get(firstIndex())).shift(from, firstIndex(), inserted);
         		if (isOverflow(from)) return shift(lastIndex(), to, null).set(from, inserted);
         		if ((from - to) > ARRAY_LENGTH / 2) // Faster to do full shift left and two shifts right.
         			return shift(firstIndex(), to, get(lastIndex())).shift(from, lastIndex(), inserted).shiftLeft();
         		if (inserted != null) count++; // non-null introduced.
         		for (int i = arrayIndex(from), n=arrayIndex(to);;i = --i & MASK) {
         			E previous = elements[i];
         			elements[i] = inserted;
         			inserted = previous;
          	        if (i == n) break;
         		} 
         		if (inserted != null) count--; // inserted is discarded.
        	}
			return (count > 1) ? this : anyInner();
		}

		@Override
		public Array<E> clone() {
			return new Array<E>(this);
		}


		@Override
		public long ceiling(long minIndex, Predicate<? super E> matching) {
			if (isOverflow(minIndex)) return 0;
			if (isUnderflow(minIndex)) minIndex = firstIndex();
	   		for (int i=arrayIndex(minIndex), n=arrayIndex(lastIndex());; i = ++i & MASK) {
    			E e = elements[i];
    			if ((e != null) && matching.test(e)) return indexFor(i);
    			if (i == n) return 0;
    		}
  		}

		@Override
		public long floor(long maxIndex, Predicate<? super E> matching) {
			if (isUnderflow(maxIndex)) return -1;
			if (isOverflow(maxIndex)) maxIndex = lastIndex();
	  		for (int i=arrayIndex(maxIndex), n=arrayIndex(firstIndex());; i = --i & MASK) {
    			E e = elements[i];
    			if ((e != null) && matching.test(e)) return indexFor(i);
    			if (i == n) return -1;
    		}
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
		
		private FractalArrayImpl<E> anyInner() { // Called when count == 1
			for (int i = arrayIndex(0);; i = ++i & MASK) 
				if (elements[i] != null) return new Single<E>(indexFor(i), elements[i]);
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
  
    	// <-- index --> = <-- prefix --><-- array index --><-- sub-index -->
    	
    	private final FractalArrayImpl<E>[] inners;
    	private final int innerIndexSize; // Always in range [Array.INDEX_SIZE .. 63] by construction.
       	private int count; // Number of non-empty inner fractals.
        
    	@SuppressWarnings("unchecked")
		public Fractal(long index, E element, FractalArrayImpl<E> inner) {
     		inners = new FractalArrayImpl[ARRAY_LENGTH];
    		int s = Array.INDEX_SIZE;
    		long l = inner.prefix ^ index;
    		while ((l >>> s >>> SIZE_INC) != 0)  s += SIZE_INC;
    		innerIndexSize = s;
    		prefix = clearLowBits(index >>> SIZE_INC, s) << SIZE_INC;
    		inners[arrayIndex(index)] = new Single<E>(subIndex(index), element);
    		inners[arrayIndex(inner.prefix)] = inner;
    		inner.prefix = subIndex(inner.prefix); // Makes it relative to enclosing fractal.
            count = 2;       	    	    
    	}
    	
    	private Fractal(Fractal<E> that) {
    		inners = that.inners.clone();
    		prefix = that.prefix;
    		innerIndexSize = that.innerIndexSize;
    		count = that.count;
    		for (int i=0; i < ARRAY_LENGTH; i++) {
    			FractalArrayImpl<E> fractal = inners[i];
    			if (fractal != null) inners[i] = fractal.clone();
    		}
    	}

    	@Override
		public E get(long index) {
    		if (!inRange(index)) return null;
    		FractalArrayImpl<E> fractal = inners[arrayIndex(index)];
    		return (fractal != null) ? fractal.get(subIndex(index)) : null;
		}

		@Override
		public FractalArrayImpl<E> clear(long index) {
			if (!inRange(index)) return this;
			int i = arrayIndex(index);
			FractalArrayImpl<E> fractal = inners[i];
			if (fractal == null) return this;
			FractalArrayImpl<E> newFractal = fractal.clear(subIndex(index));
			if (newFractal != fractal) inners[i] = newFractal.isEmpty() ? null : newFractal;
			return newFractal.isEmpty() && (--count < 2) ? anyInner() : this;
		}		

		@Override
		public FractalArrayImpl<E> set(long index, E element) {
			if (element == null) return clear(index);
			if (!inRange(index)) return new Fractal<E>(index, element, this);
			int i = arrayIndex(index);
			FractalArrayImpl<E> fractal = inners[i];
			if (fractal == null) {
				inners[i] = new Single<E>(subIndex(index), element);
				count++;
			} else {
				FractalArrayImpl<E> newFractal = fractal.set(subIndex(index), element);
				if (newFractal != fractal) inners[i] = newFractal;	
			}
			return this;
		}

		@Override
		public FractalArrayImpl<E> shift(long from, long to, E inserted) {
         	if (unsignedLessThan(from, to)) { // Right shift.
         		if (isUnderflow(from)) return shift(firstIndex(), to, null).set(from, inserted);
         		if (isOverflow(to)) return set(lastIndex() + 1, get(lastIndex())).shift(from, lastIndex(), inserted);
         		
         		if ((to - from) >>> innerIndexSize > ARRAY_LENGTH / 2) // Faster to do full shift right and two shifts left.
         			return shift(lastIndex(), to, get(firstIndex())).shift(from, firstIndex(), inserted).shiftRight();
         		
         		int iFrom = arrayIndex(from);
         		int iTo = arrayIndex(to);
         		for (int i = iFrom;;i = ++i & MASK) {
         	 		long minSubIndex = (i == iFrom) ? subIndex(from) : 0;
             		long maxSubIndex = (i == iTo) ? subIndex(to) : subIndex(-1);
        			FractalArrayImpl<E> fractal = inners[i];
        			if (fractal == null) {
        				inners[i] = new Single<E>(minSubIndex, inserted);
        				count++;
        				inserted = null;
        			} else {
        				E carry = fractal.get(maxSubIndex);
        				FractalArrayImpl<E> newFractal = fractal.shift(minSubIndex, maxSubIndex, inserted);
                		if (newFractal != fractal) inners[i] = newFractal.isEmpty() ? null : newFractal;
                		if (newFractal.isEmpty()) count--;
                		inserted = carry;
        			}
          	        if (i == iTo) break;
         		} 
         		
        	} else { // Left shift.
         		if (isUnderflow(to)) return set(firstIndex() - 1, get(firstIndex())).shift(from, firstIndex(), inserted);
         		if (isOverflow(from)) return shift(lastIndex(), to, null).set(from, inserted);
         		
         		if ((from - to) >>> innerIndexSize > ARRAY_LENGTH / 2) // Faster to do full shift left and two shifts right.
         			return shift(firstIndex(), to, get(lastIndex())).shift(from, lastIndex(), inserted).shiftLeft();
  
           		int iFrom = arrayIndex(from);
         		int iTo = arrayIndex(to);
         		for (int i = iFrom;;i = --i & MASK) {
         	 		long minSubIndex = (i == iTo) ? subIndex(to) : 0;
             		long maxSubIndex = (i == iFrom) ? subIndex(from) : subIndex(-1);
        			FractalArrayImpl<E> fractal = inners[i];
        			if (fractal == null) {
        				inners[i] = new Single<E>(maxSubIndex, inserted);
        				count++;
        				inserted = null;
        			} else {
        				E carry = fractal.get(minSubIndex);
        				FractalArrayImpl<E> newFractal = fractal.shift(maxSubIndex, minSubIndex, inserted);
                		if (newFractal != fractal) inners[i] = newFractal.isEmpty() ? null : newFractal;
                		if (newFractal.isEmpty()) count--;
                		inserted = carry;
        			}
          	        if (i == iFrom) break;
         		} 
        	}

         	return (count > 1) ? this : anyInner();
		}

		@Override
		public Fractal<E> clone() {
			return new Fractal<E>(this);
		}

		@Override
		public long ceiling(long minIndex, Predicate<? super E> matching) {
			if (isOverflow(minIndex)) return 0;
			if (isUnderflow(minIndex)) minIndex = firstIndex();
			long minSubIndex = subIndex(minIndex);
	   		for (int i=arrayIndex(minIndex), n=arrayIndex(lastIndex());; i = ++i & MASK) {
	   			FractalArrayImpl<E> fractal = inners[i];
	   			if (fractal != null) {
	   				if (minSubIndex == 0) { // Avoid zero because of ambiguity in ceiling result.
		   				E atZero = fractal.get(0);
		   				if ((atZero != null) && matching.test(atZero)) return indexFor(i, 0);
		   				minSubIndex++;
		   			}
	   				long subIndex = fractal.ceiling(minSubIndex, matching);
	   			   	if (subIndex != 0) return indexFor(i, subIndex);
	   			}
    			if (i == n) return 0;
    			minSubIndex = 0;
    		}
		}

		@Override
		public long floor(long maxIndex, Predicate<? super E> matching) {
			if (isUnderflow(maxIndex)) return -1;
			if (isOverflow(maxIndex)) maxIndex = lastIndex();
			long maxSubIndex = subIndex(maxIndex);
	   		for (int i=arrayIndex(maxIndex), n=arrayIndex(firstIndex());; i = --i & MASK) {
	   			FractalArrayImpl<E> fractal = inners[i];
	   			if (fractal != null) {
	   				long subIndex = fractal.floor(maxSubIndex, matching);
	   			   	if (subIndex != -1) return indexFor(i, subIndex);
	   			}
    			if (i == n) return -1;
    			maxSubIndex = subIndex(-1);
    		}
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
		
		private FractalArrayImpl<E> anyInner() { // Called when count == 1
			for (int i = arrayIndex(0);; i = ++i & MASK) {
				FractalArrayImpl<E> inner = inners[i];
				if (inner == null) continue;
				// Appends this prefix to inner prefix.
				inner.prefix = clearLowBits(indexFor(i, 0), innerIndexSize) | inner.prefix; 
				return inner;
			}
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
    		return (int) clearHighBits((index + prefix) >>> innerIndexSize, 64 - SIZE_INC);
    	}
  	
     	private long subIndex(long index) {  
    		return clearHighBits(index + prefix, 64 - innerIndexSize);
    	}
  	
     	private long indexFor(long arrayIndex, long subIndex) { 
     		return (clearLowBits(prefix >>> SIZE_INC, innerIndexSize) << SIZE_INC) | 
     				clearHighBits((arrayIndex << innerIndexSize) + subIndex - prefix, max(0, 64 - SIZE_INC - innerIndexSize));
    	}
   	   		    
    }  
  
    
}
