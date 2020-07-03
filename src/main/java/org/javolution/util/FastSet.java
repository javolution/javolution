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
import static org.javolution.lang.MathLib.unsignedLessThan;

import java.util.NoSuchElementException;

import org.javolution.annotations.Nullable;
import org.javolution.annotations.Parallel;
import org.javolution.annotations.Realtime;
import org.javolution.lang.MathLib;
import org.javolution.util.function.Equality;
import org.javolution.util.function.Indexer;
import org.javolution.util.function.Order;
import org.javolution.util.function.Predicate;
import org.javolution.util.internal.set.SortedSetImpl;

/**
 * High-performance ordered set / multiset based upon fast-access {@link FractalArray}. 
 * 
 * Instances of this class can advantageously replace all {@code java.util.*} sets in terms of adaptability, 
 * space or performance. 
 *     
 * ```java
 * import static javolution.util.function.Order.*;
 * 
 * // Instances. 
 * FastSet<Foo> hashSet = new FastSet<Foo>(); // Arbitrary order (hash indexing) 
 * FastSet<Foo> identitySet = new FastSet<Foo>(IDENTITY); 
 * FastSet<String> treeSet = new FastSet<String>(LEXICAL); 
 * FastSet<Integer> customIndexing = new FastSet<Integer>(i -> i.intValue());
 * 
 * // Specialized Views.
 * AbstractSet<Foo> multiset = new FastSet<Foo>().multi(); // A multiset also called a bag.
 * AbstractSet<Foo> linkedHashSet = new FastSet<Foo>().linked(); // Keeps insertion order.
 * AbstractSet<Foo> linkedIdentitySet = new FastSet<Foo>(IDENTITY).linked(); 
 * AbstractSet<Foo> concurrentHashSet = new FastSet<Foo>().shared(); // Thread-safe.
 * AbstractSet<String> concurrentSkipListSet = new FastSet<String>(LEXICAL).shared(); // Thread-safe.
 * AbstractSet<Foo> copyOnWriteArraySet = new FastSet<Foo>().atomic(); // Thread-safe.
 * AbstractSet<Foo> concurrentLinkedHashSet = new FastSet<Foo>().linked().shared(); // Thread-safe.
 * ...
 * AbstractSet<Foo> sharedLinkedMultiset = new FastSet<Foo>().multi().linked().shared(); // Thread-safe.
 * ... 
 * ``` 
 * 
 * This class inherits collection closures and support the {@link #subSet subSet} view over a portion of a set.
 * 
 * ```java
 * FastSet<String> names = new FastSet<String>(LEXICAL).with("Oscar Thon", "Claire Delune", "Jean Emare"); 
 * ...
 * names.subSet("A", "B").clear(); // Removes the names starting with "A"  (see java.util.SortedSet.subSet()).
 * names.filter(str -> str.length < 5).clear(); // Removes all short names (Java 8 notation).
 * names.filter(str -> str.length < 5).parallel().clear(); // Same as above but removal performed concurrently.
 * ``` 
 *      
 * Multiple instances of set's elements are supported (multiset). For two sets / multisets to be considered equal 
 * the cardinality of each of their elements must be the same (the cardinality of standard sets elements is one).
 * 
 * ```java
 * // Prime factors of 120 are {2, 2, 2, 3, 5}.
 * AbstractSet<Integer> primeFactors120 = new FastSet<Integer>().multi().with(2, 2, 2, 3, 5); 
 * int twoCount = primeFactors120.subSet(2).size();
 * AbstractSet<Integer> primeFactors120Linked = new FastSet<Integer>().multi().linked().with(5, 2, 3, 2, 2); 
 *  
 * System.out.println(twoCount);
 * System.out.println(primeFactors120Linked);
 * System.out.println(primeFactors120.equals(primeFactors120Linked)); // Checks cardinalities.
 * 
 * >> 3
 * >> { 5, 2, 3, 2, 2 }
 * >> true
 * ``` 
 *      
 * @param <E> the type of set elements (cannot be {@code null})
 * 
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 7.0, March 14, 2016
 * @see <a href="https://en.wikipedia.org/wiki/Multiset">Wikipedia: Multiset</a>
 */
public class FastSet<E> extends AbstractSet<E> {
    private static final long serialVersionUID = 0x700L; // Version.

    /** Immutable Set (can only be created through the {@link #freeze()} method). */
    public static final class Immutable<E> extends FastSet<E> implements org.javolution.lang.Immutable {
        private static final long serialVersionUID = FastSet.serialVersionUID;
        private Immutable(Order<? super E> order, FractalArray<E> singles, FractalArray<AbstractSet<E>> multiples, int size) {
            super(order, singles, multiples, size);
        }
        
        @Override
        public void clear() {
            throw new UnsupportedOperationException("Immutable");
        }
    }

    final Order<? super E> order;
    FractalArray<E> singles; // Hold instances for which there is no collisions.  
    FractalArray<AbstractSet<E>> multiples; // Holds instances for which there are collisions (same index value). 
    int size; // Keep tracks of the size since fractal arrays are unbounded.

    /** Creates a {@link Equality#STANDARD standard} set arbitrarily ordered (hash order). */
    public FastSet() {
        this(Order.standard());
    }

    /** Creates a {@link Equality#STANDARD standard} set ordered using the specified indexer function.*/
    public FastSet(Indexer<? super E> indexer) {
        this(Order.valueOf(indexer));
    }

    /** 
     * Creates a custom set ordered using the specified order. 
     * 
     * @param order the set order or {@code null} if no specific order (bag).
     */
    public FastSet(@Nullable Order<? super E> order) {
        this.order = order;
        this.singles = FractalArray.empty();
        this.multiples = FractalArray.empty();        
    }

    /**  Base constructor (package private). */
    FastSet(Order<? super E> order, FractalArray<E> singles, FractalArray<AbstractSet<E>> multiples, int size) {
       this.order = order;
       this.singles = singles;
       this.multiples = multiples;
       this.size = size;
    }

    /** Freezes this set and returns the corresponding {@link Immutable} instance (cannot be reversed). */
    public final Immutable<E> freeze() {
        singles = singles.unmodifiable();
        for (FractalArray.Iterator<AbstractSet<E>> itr = multiples.iterator(); itr.hasNext();) {
            long index = itr.nextIndex();
            multiples.set(index, itr.next().unmodifiable()); // Replaces.
        }
        multiples = multiples.unmodifiable();
        return new Immutable<E>(order, singles, multiples, size);
    }

    @Override
    public FastSet<E> with(@SuppressWarnings("unchecked") E... elements) {
        addAll(elements);
        return this;
    }

    @Override
    @Realtime(limit = CONSTANT)
    public final boolean add(E element) {
        return add(element, false);
    }
       
    @Override
    @Realtime(limit = CONSTANT)
    public final boolean add(E element, boolean allowDuplicate) {
        long index = order.indexOf(element);
        AbstractSet<E> multiple = multiples.get(index);
        if (multiple != null) {
            if (!multiple.add(element, allowDuplicate)) return false;            
        } else {
            E single = singles.get(index);
            if (single != null) {
                if (!allowDuplicate && order.areEqual(element, single)) return false;
                singles = singles.clear(index); // No more single.
                Order<? super E> subOrder = order.subOrder(element); 
                multiple = (subOrder != null) ? new FastSet<E>(subOrder) : new SortedSetImpl<E>(order);
                multiples = multiples.set(index, multiple);
            } else { // Empty slot.
                singles = singles.set(index, element);
            }
        }
        size++; 
        return true;
    }
         
    @Parallel(false)
    @Realtime(limit = CONSTANT)
    @Override
    public final E getAny(E element) {
        long index = order.indexOf(element);
        AbstractSet<E> multiple = multiples.get(index);
        if (multiple != null) {
            return multiple.getAny(element);            
        } else {
            E single = singles.get(index);
            if ((single != null) && order.areEqual(element, single)) return single;
            return null;
        }
    }
    
    @Override
    public void clear() {
        singles = FractalArray.empty();
        multiples = FractalArray.empty();
        size = 0;
    }

    @Override
    @Realtime(limit = LINEAR)
    public FastSet<E> clone() {
        FastSet<E> copy = (FastSet<E>) super.clone();
        copy.singles = singles.clone();
        copy.multiples = multiples.clone();
        for (FractalArray.Iterator<AbstractSet<E>> itr = multiples.iterator(); itr.hasNext();) {
            long index = itr.nextIndex();
            AbstractSet<E> multiple = itr.next();
            copy.multiples.set(index, multiple.clone()); // Replaces.
        }
        return copy;
    }

    @Override
    @Realtime(limit = CONSTANT)
    public final Order<? super E> order() {
        return order;
    }

    @Realtime(limit = CONSTANT)
    @Override
    public final DescendingIteratorImpl descendingIterator() {
        return descendingIterator(null);
     }


    @Realtime(limit = CONSTANT)
    @Override
    public final DescendingIteratorImpl descendingIterator(@Nullable E from) {
        return new DescendingIteratorImpl(from);
     }

    @Realtime(limit = CONSTANT)
    @Override
    public final boolean isEmpty() {
        return size == 0;
    }

    @Realtime(limit = CONSTANT)
    @Override
    public final AscendingIteratorImpl iterator() {
        return new AscendingIteratorImpl(null);
    }
    
    @Realtime(limit = CONSTANT)
    @Override
    public final AscendingIteratorImpl iterator(@Nullable E from) {
        return new AscendingIteratorImpl(from);
    }
        
    @Parallel(false)
    @Override
    @Realtime(limit = CONSTANT)
    public final E removeAny(E element) {
        E removed;
        long index = order.indexOf(element);
        AbstractSet<E> multiple = multiples.get(index);
        if (multiple != null) {
            removed = multiple.removeAny(element);
            if (removed == null) return null;
            if (multiple.size() == 1) { // Go back to single.
                singles = singles.set(index, multiple.findAny());
                multiples = multiples.clear(index);
            }
        } else {
            removed = singles.get(index);
            if ((removed == null) || !order.areEqual(element, removed)) return null;
            singles = singles.clear(index);
        }
        --size;
        return removed;
    }

    @Override
    public final boolean removeIf(Predicate<? super E> filter) {
        int initialSize = size;
        for (FractalArray.Iterator<AbstractSet<E>> itr = multiples.iterator(); itr.hasNext();) {
            long index = itr.nextIndex();
            AbstractSet<E> multiple = itr.next();
            int sizeBefore = multiple.size();
            multiple.removeIf(filter);
            int sizeAfter = multiple.size();
            if (sizeAfter <= 1) multiples = multiples.clear(index);
            if (sizeAfter == 1) singles = singles.set(index, multiple.findAny());
            size += sizeAfter - sizeBefore;
        }
        for (FractalArray.Iterator<E> itr = singles.iterator(); itr.hasNext(filter); itr.next()) {
            singles = singles.clear(itr.nextIndex());
            --size;
        }
        return initialSize != size;
    }

    @Realtime(limit = CONSTANT)
    @Override
    public final int size() {
        return size;
    }       

    @Override
    @Realtime(limit = CONSTANT)
    public final E findAny() {
        if (!singles.isEmpty()) return singles.iterator().next();
        if (!multiples.isEmpty()) return multiples.iterator().next().findAny();
        return null;
    }

    @Override
    @Realtime(limit = CONSTANT)
    public final E first() {
    	return iterator().next();
    }

    @Override
    @Realtime(limit = CONSTANT)
    public final E last() {
    	return descendingIterator().next();
    }
        
    /** Ascending iterator implementation. */
    private final class AscendingIteratorImpl implements FastIterator<E> {
        private FractalArray.Iterator<E> singleItr;
        private FractalArray.Iterator<AbstractSet<E>> multipleItr;
        private FastIterator<E> subItr; // Takes precedence when subItr.hasNext()
        
        @SuppressWarnings("unchecked")
        public AscendingIteratorImpl(@Nullable E from) {
            long i = (from != null) ? order.indexOf(from) : 0;
            singleItr = singles.iterator(i);
            multipleItr = multiples.iterator(i);            
            if (multipleItr.hasNext() && !unsignedLessThan(singleItr.nextIndex(), multipleItr.nextIndex())) {
                subItr = multipleItr.next().iterator(from);
            } else {
                subItr = (FastIterator<E>) EMPTY_ITERATOR; 
            }
        }
        
        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean hasNext() {
            return subItr.hasNext() || singleItr.hasNext() || multipleItr.hasNext();
        }

        @Override
        public E next() {
            if (subItr.hasNext()) return subItr.next();
            if (multipleItr.hasNext() && !unsignedLessThan(singleItr.nextIndex(), multipleItr.nextIndex())) {
                subItr = multipleItr.next().iterator();
                return subItr.next();
            }
            return singleItr.next();            
        }
   
        @Override
        public boolean hasNext(Predicate<? super E> matching) {
            while (true) {
                if (subItr.hasNext(matching)) return true;
                if (multipleItr.hasNext() && !unsignedLessThan(singleItr.nextIndex(), multipleItr.nextIndex())) {
                    subItr = multipleItr.next().iterator();
                } else {
                    if (!singleItr.hasNext()) return false;
                    if (matching.test(singleItr.next())) return true;
                }
            }    
       }

    }

    /** Descending iterator implementation. */
    private final class DescendingIteratorImpl implements FastIterator<E> {
        private FractalArray.Iterator<E> singleItr;
        private FractalArray.Iterator<AbstractSet<E>> multipleItr;
        private FastIterator<E> subItr; // Takes precedence when subItr.hasNext()
        
        @SuppressWarnings("unchecked")
        public DescendingIteratorImpl(@Nullable E from) {
            long i = (from != null) ? order.indexOf(from) : -1;
            singleItr = singles.descendingIterator(i);
            multipleItr = multiples.descendingIterator(i);            
            if (multipleItr.hasNext() && !unsignedLessThan(multipleItr.nextIndex(), singleItr.nextIndex())) {
                subItr = multipleItr.next().descendingIterator(from);
            } else {
                subItr = (FastIterator<E>) EMPTY_ITERATOR; 
            }
        }
        
        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean hasNext() {
            return subItr.hasNext() || singleItr.hasNext() || multipleItr.hasNext();
        }

        @Override
        public E next() {
            if (subItr.hasNext()) return subItr.next();
            if (multipleItr.hasNext() && !unsignedLessThan(multipleItr.nextIndex(), singleItr.nextIndex())) {
                subItr = multipleItr.next().descendingIterator();
                return subItr.next();
            }
            return singleItr.next();            
        }
   
        @Override
        public boolean hasNext(Predicate<? super E> matching) {
            while (true) {
                if (subItr.hasNext(matching)) return true;
                if (multipleItr.hasNext() && !unsignedLessThan(multipleItr.nextIndex(), singleItr.nextIndex())) {
                    subItr = multipleItr.next().descendingIterator();
                } else {
                    if (!singleItr.hasNext()) return false;
                    if (matching.test(singleItr.next())) return true;
                }
            }    
       }

    }
    
    /** Iterator over empty collection. */
    private static final FastIterator<Object> EMPTY_ITERATOR = new FastIterator<Object> () {

        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public boolean hasNext(Predicate<? super Object> matching) {
            return false;
        }

        @Override
        public Object next() {
            throw new NoSuchElementException();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException(); // As per contract.                
        }
        
    };

}