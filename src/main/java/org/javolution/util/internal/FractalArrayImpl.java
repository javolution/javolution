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

import org.javolution.annotations.Nullable;
import org.javolution.lang.Immutable;
import org.javolution.util.FractalArray;
import org.javolution.util.function.Predicate;

/**
 * The fractal array default implementation (core class for org.javolution.util package). 
 * Each instance has a bounded capacity and returns an enclosing instance with higher capacity
 * when its capacity is reached (no resize ever).
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
	public abstract FractalArrayImpl<E> insert(long index, @Nullable E inserted);

	@Override
	public abstract FractalArrayImpl<E> delete(long index);
	
	/** Shifts all elements to the right **/
	abstract FractalArrayImpl<E> shiftRight();

	/** Shifts all elements to the left **/
	abstract FractalArrayImpl<E> shiftLeft();
	
	/** Clears the highest bits, bits should be in range 0..63  */
	private static final long clearHighBits(long value, int bits) {
		return value << bits >>> bits;
	}

	/** Clears the lowest bits, bits should be in range 0..63  */
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
			return (element == null) ? this : new Single<E>(index, element);
		}

		@Override
		public FractalArrayImpl<E> insert(long index, E inserted) {
			return set(index, inserted);
		}

		@Override
		public FractalArrayImpl<E> delete(long index) {
			return this;
		}

		@Override
		public long next(long after, Predicate<? super E> matching) {
			return 0;
		}

		@Override
		public long previous(long before, Predicate<? super E> matching) {
			return -1;
		}

		@Override
		Empty<E> shiftRight() {
			return this;
		}

		@Override
		Empty<E> shiftLeft() {
			return this;
		}	

	}

	/** A single element. */
	private static final class Single<E> extends FractalArrayImpl<E> {
		private static final long serialVersionUID = FractalArrayImpl.serialVersionUID;
		private E element;
		private long index;

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
			return (index == i) ? empty() : this;
		}

		@Override
		public FractalArrayImpl<E> set(long i, E e) {
			if (e == null) return clear(i);
			if (index != i) return new Array<E>(index, element, i, e);
				//return ((index ^ i) >>> Array.LOG2_CAPACITY == 0) ? 
				//		new Array<E>(index, element, i, e) // Same high bits.
				//		: new Fractal<E>(i, e, this);
			element = e;
			return this;
		}

		@Override
		public FractalArrayImpl<E> insert(long i, E inserted) {
			if (!unsignedLessThan(index, i)) index++; // Shift right.
			return set(i, inserted);
		}

		@Override
		public FractalArrayImpl<E> delete(long i) {
			if (unsignedLessThan(i, index)) index--; // Shift left.
			return clear(i);
		}

		@Override
		public long next(long after, Predicate<? super E> matching) {
			if (unsignedLessThan(after, index) && matching.test(element)) return index;
			return 0;
		}

		@Override
		public long previous(long before, Predicate<? super E> matching) {
			if (unsignedLessThan(index, before) && matching.test(element)) return index;
			return -1;
		}

		@Override
		Single<E> shiftRight() {
			index++;
			return this;
		}

		@Override
		Single<E> shiftLeft() {
			index--;
			return this;
		}
	}

	// Trivial Implementation, until Fractal implementation rework is completed !
	private static final class Array<E> extends FractalArrayImpl<E> {	
	private static final long serialVersionUID = FractalArrayImpl.serialVersionUID;
	    private static final int INITIAL_CAPACITY = 16;
	    private long[] indices;
	    private E[] elements;
	    private int length;

		@SuppressWarnings("unchecked")
		public Array(long i0, E e0, long i1, E e1) {
			indices = new long[INITIAL_CAPACITY];
			elements = (E[]) new Object[INITIAL_CAPACITY];
			length = 2;
			if (unsignedLessThan(i0, i1)) {
				indices[0] = i0; 
				elements[0] = e0; 
				indices[1] = i1; 
				elements[1] = e1; 
			} else {
				indices[0] = i1; 
				elements[0] = e1; 
				indices[1] = i0; 
				elements[1] = e0; 
			}
		}

		private Array(Array<E> that) {
			indices = that.indices.clone();
			elements = that.elements.clone();
			length = that.length;
		}
		
		@Override
		public FractalArrayImpl<E> clone() {
			return new Array<E>(this);
		}
		
		@Override
		public FractalArrayImpl<E> clear(long index) {
			int i = positionOf(index, 0, length);
			if (i >= 0) { // Found it.
				System.arraycopy(indices, i+1, indices, i, length - i - 1);
				System.arraycopy(elements, i+1, elements, i, length - i - 1);
				if (--length * 4 < indices.length) return downsize();
		    } 
			return this;
		}
		
		@Override
		public FractalArrayImpl<E> set(long index, E element) {
			int i = positionOf(index, 0, length);
			if (i >= 0) { // Replace element.
				elements[i] = element;
			} else {
				if (length >= indices.length) return upsize().set(index, element);
				i = -i - 1; // The "should be" position.
				System.arraycopy(indices, i, indices, i+1, length - i);
				System.arraycopy(elements, i, elements, i+1, length - i);
				length++;
			}
			return this;
		}
		
		@Override
		public FractalArrayImpl<E> insert(long index, E inserted) {
			if (length >= indices.length) return upsize().insert(index, inserted);
			int i = positionOf(index, 0, length);
			i = (i >= 0) ? i : -i - 1;
			for (int j=i; j < length; ++j) indices[j]++;
			System.arraycopy(indices, i, indices, i+1, length - i);
			System.arraycopy(elements, i, elements, i+1, length - i);
			indices[i] = index;
			elements[i] = inserted;	
			return this;
		}

		@Override
		public FractalArrayImpl<E> delete(long index) {
			int i = positionOf(index, 0, length);
			if (i >= 0) { // Remove element.
				System.arraycopy(indices, i+1, indices, i, length - i - 1);
				System.arraycopy(elements, i+1, elements, i, length - i - 1);
				if (--length * 4 < indices.length) downsize();				
			} else {
				i = -i - 1;					
			}
			for (int j=i; j < length; ++j) indices[j]--;
			return this;
		}
		
		@Override
		public E get(long index) {	
			int i = positionOf(index, 0, length);
			return i >= 0 ? elements[i] : null;
		}
	
		@Override
		FractalArrayImpl<E> shiftRight() {
			if ((length != 0) && (indices[length-1] == -1)) 
				throw new ArithmeticException("Index Overflow");
			for (int i=0; i < length; ++i) indices[i]++;
			return this;
		}

		@Override
		FractalArrayImpl<E> shiftLeft() {
			if ((length != 0) && (indices[0] == 0)) 
				throw new ArithmeticException("Index Underflow");
			for (int i=0; i < length; ++i) indices[i]--;
			return this;
		}
	
		@Override
		public long next(long after, Predicate<? super E> matching) {
			int i = positionOf(after, 0, length);
			i = (i >= 0) ? i + 1 : -i - 1;
			while (i < length) {
				if (matching.test(elements[i])) return indices[i];
				i++;
			}
			return 0;
		}
		@Override
		public long previous(long before, Predicate<? super E> matching) {
			int i = positionOf(before, 0, length);
			i = (i >= 0) ? i - 1 : -i - 2;
			while (i >= 0) {
				if (matching.test(elements[i])) return indices[i];
				i--;
			}
			return -1;
		}
	    
		private int positionOf(long index, int start, int length) {
			while (length != 0) {
				int half = length >> 1;
				int midPos = start+half;
				long midIndex = indices[midPos];
			    if (midIndex == index) return midPos;
			    if (unsignedLessThan(index, midIndex)) {
			    	length = half;
			    } else {
			    	start = midPos + 1;
			    	length = length - half - 1;
			    }
			}
			return -start - 1; // Not found.	
		}		
		
 		@SuppressWarnings("unchecked")
 		private FractalArrayImpl<E> upsize() {
    		long[] indicesTmp = new long[indices.length * 2];
    		E[] elementsTmp = (E[]) new Object[elements.length * 2];
    	    System.arraycopy(indices, 0, indicesTmp, 0, length);
     		System.arraycopy(elements, 0, elementsTmp, 0, length);
    		indices = indicesTmp;
    	    elements = elementsTmp;
    		return this;
    	}

 		@SuppressWarnings("unchecked")
 	 	private FractalArrayImpl<E> downsize() {
 			if (length == 1) return new Single<E>(indices[0], elements[0]);
 			if (indices.length <= INITIAL_CAPACITY) return this; 
 			long[] indicesTmp = new long[indices.length / 2];
 	 		E[] elementsTmp = (E[]) new Object[indices.length / 2];
 	 	    System.arraycopy(indices, 0, indicesTmp, 0, length);
    		System.arraycopy(elements, 0, elementsTmp, 0, length);
    		indices = indicesTmp;
        	elements = elementsTmp;
    		return this;
    	}

	}
	
//	private static final int ARRAY_CAPACITY = 1 << LOG2_CAPACITY;
//	private static final int MASK = ARRAY_CAPACITY - 1;
//
//	private final E[] elements;
//	private long lastIndex; // Index of the last element (higher bits holds the array prefix).
//	private int firstPos; // Holds the position of the first element (lowest index).

	
	
//	/**
//	 * An array of elements (indexes all having the same high bits). 
//	 * It is an optimization to reduce memory usage in case of dense arrays.
//	 */
//	private static final class Array<E> extends FractalArrayImpl<E> {
//		private static final long serialVersionUID = FractalArrayImpl.serialVersionUID;
//		private static final int LOG2_CAPACITY = 4;
//		private static final int ARRAY_CAPACITY = 1 << LOG2_CAPACITY;
//		private static final int MASK = ARRAY_CAPACITY - 1;
//
//		private final E[] elements;
//		private long lastIndex; // Index of the last element (higher bits holds the array prefix).
//		private int firstPos; // Holds the position of the first element (lowest index).
//
//		@SuppressWarnings("unchecked")
//		public Array(long i0, E e0, long i1, E e1) {
//			elements = (E[]) new Object[ARRAY_CAPACITY];
//			elements[(int)clearHighBits(i0, 64 - LOG2_CAPACITY)] = e0;
//			elements[(int)clearHighBits(i1, 64 - LOG2_CAPACITY)] = e1;
//			lastIndex = unsignedLessThan(i0, i1) ? i1 : i0;
//			firstPos = 0;
//		}
//
//		private Array(Array<E> that) {
//			elements = that.elements.clone();
//			lastIndex = that.lastIndex;
//			firstPos = that.firstPos;
//		}
//
//		@Override
//		public E get(long index) {
//			return inRange(index) ? elements[posFor(index)] : null;
//		}
//
//		@Override
//		public FractalArrayImpl<E> clear(long index) {
//			if (!inRange(index)) return this;
//			elements[posFor(index)] = null;
//			if (index == lastIndex) lastIndex = previous(lastIndex, null);
//			if (previous(lastIndex, null) == -1) return new Single<E>(lastIndex, elements[posFor(lastIndex)]);
//			return this;
//	    }	
//
//		@Override
//		public FractalArrayImpl<E> set(long index, E element) {
//			if (element == null) return clear(index);
//			if (!inRange(index)) return new Fractal<E>(index, element, this);
//			elements[posFor(index)] = element;
//			if (unsignedLessThan(lastIndex, index)) lastIndex = index;
//			return this;
//		}
//
//		@Override
//		public FractalArrayImpl<E> insert(long index, E inserted) {
//			if (unsignedLessThan(lastIndex, index)) return set(index, inserted);
//			if (unsignedLessThan(index, firstIndex())) return shiftRight().set(index, inserted);
//			int indexPos = posFor(index);
//			int lastPos = posFor(lastIndex);			
//			E last = elements[lastPos];
//			for (int i=lastPos; i != indexPos;) elements[i] = elements[i = (i-1) & MASK];
//			elements[firstPos] = inserted;
//			return set(++lastIndex, last);
//		}
//
//		@Override
//		public FractalArrayImpl<E> delete(long index) {
//			if (!unsignedLessThan(index, lastIndex)) return clear(index); // index >= lastIndex
//			if (unsignedLessThan(index, firstIndex())) return shiftLeft(); // index < firstIndex
//			int indexPos = posFor(index);
//			int lastPos = posFor(lastIndex);			
//			for (int i=indexPos; i != lastPos;) elements[i] = elements[i = (i+1) & MASK];
//			lastIndex--;
//			return (previous(lastIndex, null) == -1) ? new Single<E>(lastIndex, elements[posFor(lastIndex)]) : this;
//		}
//
//		@Override
//		public Array<E> clone() {
//			return new Array<E>(this);
//		}
//
//		@Override
//		FractalArrayImpl<E> shiftRight() {
//			firstPos = (firstPos - 1) & MASK;
//			if ((++lastIndex & MASK) != 0) return this; // No overflow.
//			
//			
//			TO BE CONTINUED ******* 
//			
//			E carry = elements[firstPos]; 
//			elements[firstPos] = null;
//			lastIndex = previous(lastIndex, null);
//		}
//			
//			E carry = elements[--firstPos & MASK];
//			lastIndex++;
//			if (carry != null) return set()
//			
//		}
//
//		@Override
//		FractalArrayImpl<E> shiftLeft() {
//			long newOffset = clearHighBits(prefix + 1, 64 - INDEX_SIZE);
//			prefix = prefixWithoutOffset() | newOffset;
//			return this;
//		}
//
//	
//
//		private boolean inRange(long index) { // Check prefix equality
//			return (index ^ offset) >>> LOG2_CAPACITY == 0;
//		}
//
//		private boolean isUnderflow(long index) { // Too small.
//			return (index >>> INDEX_SIZE) < (prefix >>> INDEX_SIZE);
//		}
//
//		private boolean isOverflow(long index) { // Too large.
//			return (index >>> INDEX_SIZE) > (prefix >>> INDEX_SIZE);
//		}
//
//		private long prefixWithoutOffset() {
//			return clearLowBits(prefix, INDEX_SIZE);
//		}
//
//		private long firstIndex() {
//			return clearLowBits(prefix, INDEX_SIZE);
//		}
//
//		private long lastIndex() {
//			return firstIndex() + MASK;
//		}
//
//		private int posFor(long index) { // For index in range; returns the position in elements[]
//			return (int) ((index - offset) & MASK);
//		}
//
//		private long indexFor(int arrayPos) { // Returns the index corresponding to an array position.
//			return clearLowBits(offset, LOG2_CAPACITY) + ((arrayPos + offset) & MASK);
//		}
//
//	}
//
//	/** The fractal structure. */
//	private static final class Fractal<E> extends FractalArrayImpl<E> {
//		private static final long serialVersionUID = FractalArrayImpl.serialVersionUID;
//		private static final int SIZE_INC = 4;
//		private static final int ARRAY_LENGTH = 1 << SIZE_INC;
//		private static final int MASK = ARRAY_LENGTH - 1;
//
//		private final FractalArrayImpl<E>[] inners;
//		private final int innerIndexSize; // Always in range [Array.INDEX_SIZE .. 63] by construction.
//		private int count; // Number of non-empty inner fractals.
//
//		@SuppressWarnings("unchecked")
//		public Fractal(long index, E element, FractalArrayImpl<E> inner) {
//			inners = new FractalArrayImpl[ARRAY_LENGTH];
//			int shift = Array.INDEX_SIZE;
//			long diffBits = inner.prefix ^ index;
//			while ((diffBits >>> shift >>> SIZE_INC) != 0)
//				shift += SIZE_INC;
//			innerIndexSize = shift; // Minimal shift for element and inner to belong to the same fractal.
//			prefix = clearLowBits(index >>> SIZE_INC, innerIndexSize) << SIZE_INC;
//			inners[arrayIndex(index)] = new Single<E>(subIndex(index), element);
//			inners[arrayIndex(inner.prefix)] = inner;
//			inner.prefix = subIndex(inner.prefix); // Clears prefix and array index high bits (makes it inner).
//			count = 2;
//		}
//
//		private Fractal(Fractal<E> that) {
//			inners = that.inners.clone();
//			prefix = that.prefix;
//			innerIndexSize = that.innerIndexSize;
//			count = that.count;
//			for (int i = 0; i < ARRAY_LENGTH; i++) {
//				FractalArrayImpl<E> fractal = inners[i];
//				if (fractal != null)
//					inners[i] = fractal.clone();
//			}
//		}
//
//		@Override
//		public E get(long index) {
//			if (!inRange(index))
//				return null;
//			FractalArrayImpl<E> fractal = inners[arrayIndex(index)];
//			return (fractal != null) ? fractal.get(subIndex(index)) : null;
//		}
//
//		@Override
//		public FractalArrayImpl<E> clear(long index) {
//			if (!inRange(index))
//				return this;
//			int i = arrayIndex(index);
//			FractalArrayImpl<E> fractal = inners[i];
//			if (fractal == null)
//				return this;
//			FractalArrayImpl<E> newFractal = fractal.clear(subIndex(index));
//			if (newFractal != fractal)
//				inners[i] = newFractal.isEmpty() ? null : newFractal;
//			return newFractal.isEmpty() && (--count == 1) ? extractFractal() : this;
//		}
//
//		@Override
//		public FractalArrayImpl<E> set(long index, E element) {
//			if (element == null)
//				return clear(index);
//			if (!inRange(index))
//				return new Fractal<E>(index, element, this);
//			int i = arrayIndex(index);
//			FractalArrayImpl<E> fractal = inners[i];
//			if (fractal == null) {
//				inners[i] = new Single<E>(subIndex(index), element);
//				count++;
//			} else {
//				FractalArrayImpl<E> newFractal = fractal.set(subIndex(index), element);
//				if (newFractal != fractal)
//					inners[i] = newFractal;
//			}
//			return this;
//		}
//
//		@Override
//		public FractalArrayImpl<E> shift(long from, long to, E inserted) {
//			if (from == to)
//				return set(from, inserted);
//
//			if (unsignedLessThan(from, to)) { // Right shift.
//
//				if (isUnderflow(from))
//					return shift(firstIndex(), to, null).set(from, inserted);
//				if (isOverflow(to))
//					return set(lastIndex() + 1, get(lastIndex())).shift(from, lastIndex(), inserted);
//
//				if ((to - from) >>> innerIndexSize > ARRAY_LENGTH / 2) // Optimization
//					return shift(lastIndex(), to, get(firstIndex())).shiftRight().shift(from, firstIndex(), inserted);
//				int iFrom = arrayIndex(from);
//				int iTo = arrayIndex(to);
//				for (int i = iFrom;; i = ++i & MASK) {
//					long minSubIndex = (i == iFrom) ? subIndex(from) : 0;
//					long maxSubIndex = (i == iTo) ? subIndex(to) : clearHighBits(-1, 64 - innerIndexSize);
//					FractalArrayImpl<E> fractal = inners[i];
//					if (fractal == null) {
//						inners[i] = new Single<E>(minSubIndex, inserted);
//						count++;
//						inserted = null;
//					} else {
//						E carry = fractal.get(maxSubIndex);
//						FractalArrayImpl<E> newFractal = fractal.shift(minSubIndex, maxSubIndex, inserted);
//						if (newFractal != fractal)
//							inners[i] = newFractal.isEmpty() ? null : newFractal;
//						if (newFractal.isEmpty())
//							count--;
//						inserted = carry;
//					}
//					if (i == iTo)
//						break;
//				}
//
//			} else { // Left shift.
//
//				if (isUnderflow(to))
//					return set(firstIndex() - 1, get(firstIndex())).shift(from, firstIndex(), inserted);
//				if (isOverflow(from))
//					return shift(lastIndex(), to, null).set(from, inserted);
//
//				if ((from - to) >>> innerIndexSize > ARRAY_LENGTH / 2) // Optimization
//					return shift(firstIndex(), to, get(lastIndex())).shiftLeft().shift(from, lastIndex(), inserted);
//
//				int iFrom = arrayIndex(from);
//				int iTo = arrayIndex(to);
//				for (int i = iFrom;; i = --i & MASK) {
//					long minSubIndex = (i == iTo) ? subIndex(to) : 0;
//					long maxSubIndex = (i == iFrom) ? subIndex(from) : clearHighBits(-1, 64 - innerIndexSize);
//					FractalArrayImpl<E> fractal = inners[i];
//					if (fractal == null) {
//						inners[i] = new Single<E>(maxSubIndex, inserted);
//						count++;
//						inserted = null;
//					} else {
//						E carry = fractal.get(minSubIndex);
//						FractalArrayImpl<E> newFractal = fractal.shift(maxSubIndex, minSubIndex, inserted);
//						if (newFractal != fractal)
//							inners[i] = newFractal.isEmpty() ? null : newFractal;
//						if (newFractal.isEmpty())
//							count--;
//						inserted = carry;
//					}
//					if (i == iTo)
//						break;
//				}
//			}
//
//			return (count == 1) ? extractFractal() : this;
//		}
//
//		@Override
//		public Fractal<E> clone() {
//			return new Fractal<E>(this);
//		}
//
//		@Override
//		public long next(long index, long to, Predicate<? super E> matching) {
//			int inc;
//			if (unsignedLessThan(index, to)) {
//				if (isOverflow(index))
//					return index;
//				from = max(firstIndex(), from);
//				to = min(lastIndex(), to);
//				inc = 1;
//			} else {
//				if (isUnderflow(from))
//					return -1;
//				from = min(lastIndex(), from);
//				to = max(firstIndex(), to);
//				inc = -1;
//			}
//			for (int i = arrayIndex(from), first = i, last = arrayIndex(to);; i = (i + inc) & MASK) {
//				FractalArrayImpl<E> fractal = inners[i];
//				if (fractal != null) {
//					long fromSubIndex = (i == first) ? subIndex(from) : (inc > 0) ? 0 : -1;
//					long toSubIndex = (i == last) ? subIndex(to) : (inc > 0) ? -1 : 0;
//					long subIndex = fractal.next(fromSubIndex, toSubIndex, matching);
//					if (subIndex != -1)
//						return indexFor(i, subIndex);
//				}
//				if (i == last)
//					return -1;
//			}
//		}
//
//		@Override
//		Fractal<E> shiftRight() {
//			long newOffset = clearHighBits(prefix - 1, max(0, 64 - SIZE_INC - innerIndexSize));
//			prefix = prefixWithoutOffset() | newOffset;
//			return this;
//		}
//
//		@Override
//		Fractal<E> shiftLeft() {
//			long newOffset = clearHighBits(prefix + 1, max(0, 64 - SIZE_INC - innerIndexSize));
//			prefix = prefixWithoutOffset() | newOffset;
//			return this;
//		}
//
//		private FractalArrayImpl<E> extractFractal() { // Called when count == 1 to extract the single inner.
//			for (int i = arrayIndex(0);; i = ++i & MASK) {
//				FractalArrayImpl<E> inner = inners[i];
//				if (inner == null)
//					continue;
//				inner.prefix = indexFor(i, 0L); // Full prefix is the index of the first fractal element.
//				return inner;
//			}
//		}
//
//		private boolean inRange(long index) { // Check prefix equality
//			return (index ^ prefix) >>> SIZE_INC >>> innerIndexSize == 0;
//		}
//
//		private boolean isUnderflow(long index) { // Too small.
//			return (index >>> SIZE_INC >>> innerIndexSize) < (prefix >>> SIZE_INC >>> innerIndexSize);
//		}
//
//		private boolean isOverflow(long index) { // Too large.
//			return (index >>> SIZE_INC >>> innerIndexSize) > (prefix >>> SIZE_INC >>> innerIndexSize);
//		}
//
//		private long prefixWithoutOffset() {
//			return clearLowBits(prefix >>> SIZE_INC, innerIndexSize) << SIZE_INC;
//		}
//
//		private long firstIndex() {
//			return clearLowBits(prefix >>> SIZE_INC, innerIndexSize) << SIZE_INC;
//		}
//
//		private long lastIndex() {
//			return firstIndex() + (1L << SIZE_INC << innerIndexSize) - 1L;
//		}
//
//		private int arrayIndex(long index) {
//			return (int) clearHighBits((index + prefix) >>> innerIndexSize, 64 - SIZE_INC);
//		}
//
//		private long subIndex(long index) {
//			return clearHighBits(index + prefix, 64 - innerIndexSize);
//		}
//
//		private long indexFor(long arrayIndex, long subIndex) { // Input/Output index of this fractal.
//			long indexUnbound = (arrayIndex << innerIndexSize) + subIndex - prefix; // High bits should be ignored.
//			return prefixWithoutOffset() | clearHighBits(indexUnbound, max(0, 64 - SIZE_INC - innerIndexSize));
//		}
//
//	}
//
}
