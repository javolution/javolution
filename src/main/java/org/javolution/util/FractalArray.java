/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.util;

import static org.javolution.annotations.Realtime.Limit.CONSTANT;
import static org.javolution.annotations.Realtime.Limit.LINEAR;
import static org.javolution.annotations.Realtime.Limit.LOG_N;

import java.io.Serializable;
import java.util.NoSuchElementException;

import org.javolution.annotations.Nullable;
import org.javolution.annotations.Realtime;
import org.javolution.util.function.Predicate;
import org.javolution.util.internal.FractalArrayImpl;

/**
 * A [fractal-based] array supporting 64-bits indexing, fast rotation and minimal memory footprint (they can 
 * be used for dense or sparse vectors alike).
 *       
 * Updates operations on fractals may return new instances with greater or lesser capacity. 
 * 
 * ```java
 * class SparseVector<E> {
 *      private FractalArray<E> elements = FractalArray.empty();
 *      void set(int index, E element) {
 *           elements = elements.set(index, element);
 *      }
 * }
 * ```
 * 
 * Fractal arrays index {@link #shift} operation is performed in {@link Realtime.Limit#LOG_N O(Log(n))}
 * where {@code n} is the number of elements held by the array, up to 2^64 !    
 *  
 * [fractal-based]: http://en.wikipedia.org/wiki/Fractal
 * 
 * @author <jean-marie@dautelle.com>
 * @version 7.0, July 1st, 2017
 */
@Realtime(limit=CONSTANT)
public abstract class FractalArray<E> implements Cloneable, Serializable, Iterable<E> {
    private static final long serialVersionUID = 0x700L; // Version.
  
    /** 
     * Iterator over fractal arrays.
     */
    public interface Iterator<E> extends FastIterator<E> {
        
        /** Returns the unsigned 64-bits index of the next {@code non-null} element , {@code -1} when there is none. */
        long nextIndex();

    }

    /** 
     * Returns a new empty instance.
     */
    public static <E> FractalArray<E> empty() {
        return FractalArrayImpl.empty();
    }

    /** 
     * Returns a copy of this fractal array; updates of the copy should not impact the original. 
     * 
     * @return a copy of this fractal array.
     */
    @Realtime(limit = LINEAR)
    public abstract FractalArray<E> clone();
    
    /** 
     * Indicates if this fractal has no element
     * 
     * @return {@code true} if there is no elements different from {@code null}; {@code false} otherwise.
     */
    @Realtime(limit=CONSTANT)
    public abstract boolean isEmpty();
    
    /** 
     * Returns the value at the specified index (unsigned 64-bits).
     * 
     * @param index the unsigned 64-bits index of the value to return.
     * @return the value at the specified index or {@code null} if none.
     */
    @Realtime(limit=CONSTANT)
    public abstract @Nullable E get(long index);
    
    /** 
     * Returns the value at the specified index (unsigned 64-bits) or the specified default if that value
     * is {@code null}. 
     * 
     * @param index the unsigned 64-bits index of the value to return.
     * @param defaultIfNull the value to return instead of {@code null}.
     * @return the value at the specified index or the specified default.
     */
    @Realtime(limit=CONSTANT)
    public final E get(long index, E defaultIfNull) {
        E value = get(index);
        return (value != null) ? value : defaultIfNull;
    }

    /**
     * Clears the element at the specified index (unsigned 64-bits).
     * 
     * @param index the unsigned 64-bits index of the value to clear.
     * @return a new fractal array or {@code this}. 
     */
    @Realtime(limit=CONSTANT)
    public abstract FractalArray<E> clear(long index);

    /**
     * Sets the element at the specified index (unsigned 64-bits), equivalent to {@link clear}
     * if the element is {@code null}.
     * 
     * @param index the unsigned 64-bits index of the value to be set.
     * @param element the element at the specified position (can be {@code null}). 
     * @return a new fractal array or {@code this}. 
     */
    @Realtime(limit=CONSTANT)
    public abstract FractalArray<E> set(long index,  @Nullable E element);

    /**
     * Inserts the specified elements at the specified position.
     * 
     * @param index the unsigned 64-bits index of the element to be inserted.
     * @param inserted the element being inserted.
     * @return a new fractal array with higher capacity or {@code this}. 
     */
    @Realtime(limit = LOG_N)
    public abstract FractalArray<E> insert(long index, @Nullable E inserted);
    
    /**
     * Deletes the element at the specified position.
     * 
     * @param index the index of the element to be deleted.
     * @return the array with the specified elements shifted one position.
     * @return a new fractal array with lower capacity or {@code this}. 
     */
    @Realtime(limit = LOG_N)
    public abstract FractalArray<E> delete(long index);
    
    /**
     * Returns the index of the next non-null element after the specified position, matching the specified
     * predicate (if any). This method calls the specified predicate on the first non-null element found.
     * 
     * @param after the unsigned 64-bits starting position for the search (excluded).
     * @param matching the filter called on {@code non-null} elements; or {@code null} if no test is performed.
     * @return the index of the next element from the specified index and matching the specified predicate or
     *         {@code 0} if none.
     */
    @Realtime(limit = LINEAR)
    public abstract long next(long after, @Nullable Predicate<? super E> matching);
    
    /**
     * Returns the index of the previous non-null element before the specified position, matching the specified
     * predicate (if any). This method calls the specified predicate on the first non-null element found.
     * 
     * @param before the unsigned 64-bits starting position for the search (excluded).
     * @param matching the filter called on {@code non-null} elements; or {@code null} if no test is performed.
     * @return the index of the previous element before the specified index and matching the specified predicate or
     *         {@code -1} if none.
     */
    @Realtime(limit = LINEAR)
    public abstract long previous(long before, @Nullable Predicate<? super E> matching);
    
    /** 
     * Returns an ascending iterator over non-null elements starting from the specified (unsigned 64-bits) index.
     * 
     * @param from the the unsigned 64-bits starting index (inclusive).
     */  
    public Iterator<E> iterator(long from) {
        return new AscendingIterator<E>(this, from);
    }

    /** 
     * Returns a descending iterator over non-null elements starting from the specified  (unsigned 64-bits) index.
     * 
     * @param from the starting index (inclusive).
     */  
    public Iterator<E> descendingIterator(long from) {
        return new DescendingIterator<E>(this, from);
    }

    /** 
     * Returns an unmodifiable view over this array. 
     */  
    @Realtime(limit=CONSTANT)
    public FractalArray<E> unmodifiable() {
        return new Unmodifiable<E>(this);
    }

    @Override
    public Iterator<E> iterator() {
        return iterator(0);
    }
    
    /** Ascending array iterator. */
    private static final class AscendingIterator<E> implements Iterator<E>, Predicate<E> {
        private final FractalArray<E> fractal;
        private long nextIndex;
        private E next;
   
        public AscendingIterator(FractalArray<E> fractal, long from) {
             this.fractal = fractal;
             nextIndex = from;
             next = fractal.get(from);
             if (next == null) nextIndex = fractal.next(from, this);
        }
        
        @Override
        public boolean hasNext() {
            return next != null;
        }

        @Override
        public boolean hasNext(Predicate<? super E> matching) {
        	if (next == null) return false;
        	if (matching.test(next)) return true;
        	next = null;        	
        	nextIndex = fractal.next(nextIndex, new Predicate<E>() {

				@Override
				public boolean test(E param) {
					if (!matching.test(param)) return false;
					next = param;
					return true;
				}
        		
        	});
            return next != null;
        }

        @Override
        public E next() {
            if (next == null) throw new NoSuchElementException();
            E tmp = next;
        	next = null;
            nextIndex = fractal.next(nextIndex, this);
            return tmp;
        }

        @Override
        public long nextIndex() {
            return nextIndex;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException(); // As per contract.                
        }

		@Override
		public boolean test(E param) {
			next = param;
			return true;
		}
        
    }
    
    /** Descending array iterator. */
    private static final class DescendingIterator<E> implements Iterator<E>, Predicate<E> {
        private final FractalArray<E> fractal;
        private long previousIndex;
        private E previous;
 
        public DescendingIterator(FractalArray<E> fractal, long from) {
            this.fractal = fractal;
            previousIndex = from;
            previous = fractal.get(from);
            if (previous == null) previousIndex = fractal.previous(from, this);
        }
        
         @Override
        public boolean hasNext() {
            return previous != null;
        }

        @Override
        public boolean hasNext(Predicate<? super E> matching) {
           	if (previous == null) return false;
        	if (matching.test(previous)) return true;
          	previous = null;
        	previousIndex = fractal.previous(previousIndex, new Predicate<E>() {

				@Override
				public boolean test(E param) {
					if (!matching.test(param)) return false;
					previous = param;
					return true;
				}
        		
        	});
            return previous != null;
        }

        @Override
        public E next() {
            if (previous == null) throw new NoSuchElementException();
            E tmp = previous;
        	previous = null;
            previousIndex = fractal.previous(previousIndex, this);
            return tmp;
        }

        @Override
        public long nextIndex() {
            return previousIndex;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException(); // As per contract.                
        }

		@Override
		public boolean test(E param) {
			previous = param;
			return true;
		}

    }
    
    /** Unmodifiable view over fractal array. */
    private static final class Unmodifiable<E> extends FractalArray<E> {
        private static final long serialVersionUID = FractalArray.serialVersionUID;
        private final FractalArray<E> target;

        public Unmodifiable(FractalArray<E> target) {
            this.target = target;
        }
        
		@Override
        public boolean isEmpty() {
            return target.isEmpty();
        }

		@Override
        public FractalArray<E> clone() {
            return target.clone().unmodifiable();
        }

        @Override
        public E get(long index) {
            return target.get(index);
        }

		@Override
		public FractalArray<E> clear(long index) {
	        throw new UnsupportedOperationException("Unmodifiable");
		}      

		@Override
        public FractalArray<E> set(long index, E element) {
            throw new UnsupportedOperationException("Unmodifiable");
        }

		@Override
		public FractalArray<E> insert(long index, E inserted) {
            throw new UnsupportedOperationException("Unmodifiable");
		}

		@Override
		public FractalArray<E> delete(long index) {
            throw new UnsupportedOperationException("Unmodifiable");
		}

		@Override
		public long next(long after, Predicate<? super E> matching) {
            return target.next(after, matching);
       }

		@Override
		public long previous(long before, Predicate<? super E> matching) {
	        return target.previous(before, matching);
	 	}
       
    }
    
 }

    