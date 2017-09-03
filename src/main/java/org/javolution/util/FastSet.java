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

import java.util.Collection;
import java.util.Comparator;
import java.util.NoSuchElementException;

import org.javolution.annotations.Nullable;
import org.javolution.annotations.Parallel;
import org.javolution.annotations.Realtime;
import org.javolution.lang.MathLib;
import org.javolution.util.FastMap.Immutable;
import org.javolution.util.function.Equality;
import org.javolution.util.function.Function;
import org.javolution.util.function.Indexer;
import org.javolution.util.function.Order;
import org.javolution.util.function.Predicate;

/**
 * High-performance ordered set / multiset based upon fast-access {@link SparseArray}. 
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
 * the cardinality of their elements must be the same.
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

    /** Immutable Set (can only be created through the {@link #immutable()} method). */
    public static final class Immutable<E> extends FastSet<E> implements org.javolution.lang.Immutable {
        private static final long serialVersionUID = FastSet.serialVersionUID;
        private Immutable(Order<? super E> order, SparseArray<E> first, SparseArray<SortedTable<E>> others, int size) {
            super(order, first, others, size);
        }
    }

    private final Order<? super E> order;
    private SparseArray<E> first; // Hold element at first position at index.
    private SparseArray<SortedTable<E>> collisions; // Holds others elements (sorted). 
    private int size; // Keep tracks of the size since sparse array are unbounded.

    /** Creates a {@link Equality#STANDARD standard} set arbitrarily ordered. */
    public FastSet() {
        this(Equality.STANDARD);
    }

    /** Creates a {@link Equality#STANDARD standard} set ordered using the specified indexer function 
     * (convenience method).*/
    public FastSet(final Indexer<? super E> indexer) {
        this(new Order<E>() {
            private static final long serialVersionUID = FastSet.serialVersionUID;

            @Override
            public boolean areEqual(E left, E right) {
                return left.equals(right); // E cannot be null.
            }

            @Override
            public int compare(E left, E right) {
                int leftIndex = indexer.indexOf(left);
                int rightIndex = indexer.indexOf(right);
                if (leftIndex == rightIndex) return 0;
                return MathLib.unsignedLessThan(leftIndex, rightIndex) ? -1 : 1;
            }

            @Override
            public int indexOf(E obj) {
                return indexer.indexOf(obj);
            }

         });
    }

    /** Creates a custom set ordered using the specified order. */
    public FastSet(Order<? super E> order) {
        this.order = order;
        this.first = SparseArray.empty();
        this.collisions = SparseArray.empty();        
    }

    /**  Base constructor (package private). */
    FastSet(Order<? super E> order, SparseArray<E> first, SparseArray<SortedTable<E>> collisions, int size) {
       this.order = order;
       this.first = first;
       this.collisions = collisions;
       this.size = size;
    }

    /** Makes this set immutable and returns the corresponding {@link Immutable} instance (cannot be reversed). */
    public final Immutable<E> immutable() {
        first = first.unmodifiable();
        collisions = collisions.unmodifiable();
        for (SortedTable<E> set : collisions) set.immutable();
        return new Immutable<E>(order, first, collisions, size);
    }

    @Override
    public final FastSet<E> with(E... elements) {
        addAll(elements);
        return this;
    }

    @Override
    public final FastSet<E> with(Collection<? extends E> elements) {
        addAll(elements);
        return this;
    }

    @Override
    @Realtime(limit = CONSTANT)
    public final boolean add(E element) {
        int index = order.indexOf(element);
        E existing = first.get(index);
        if (existing == null) { // Most frequent.
            first = first.set(index, element);
        } else if (order.areEqual(element, existing)) {
            return false;
        } else { // Collision.
            SortedTable<E> others = collisions.get(index);
            if (others == null) collisions = collisions.set(index, (others = new SortedTable<E>()));
            if (order.compare(element, existing) < 0) { // New element should be first.
                others.addFirst(existing);
                first.set(index, element); // Replaces existing as first.
            } else {
                int insertionIndex = others.firstIndex(element, order);
                if ((insertionIndex < others.size()) && order.areEqual(element, others.get(insertionIndex)))
                     return false; // Already present.
                others.add(insertionIndex, element);
            }
        }
        size++;
        return true;
    }

    @Override
    @Realtime(limit = CONSTANT)
    public final boolean addMulti(E element) {
        int index = order.indexOf(element);
        E existing = first.get(index);
        if (existing == null) { // Most frequent.
            first = first.set(index, element);
        } else { // Multiple instances at index.
            SortedTable<E> others = collisions.get(index);
            if (others == null) collisions = collisions.set(index, (others = new SortedTable<E>()));
            if (order.compare(element, existing) < 0) { // New element should be first.
                others.addFirst(existing);
                first.set(index, element); // Replaces existing as first.
            } else {
                int insertionIndex = others.firstIndex(element, order);
                others.add(insertionIndex, element);
            }
        }
        size++;
        return true;
   } 

    @Parallel(false)
    @Realtime(limit = CONSTANT)
    @SuppressWarnings("unchecked")
    @Override
    public final boolean contains(Object element) {
        int index = order.indexOf((E) element);
        E existing  = first.get(index);
        if (existing == null) return false;
        if (order.areEqual((E)element, existing)) return true;
        SortedTable<E> others = collisions.get(index);
        if (others == null) return false;
        int insertionIndex = others.firstIndex((E)element, order);
        if (insertionIndex >= others.size()) return false;
        existing = others.get(insertionIndex);
        return order.areEqual((E)element, existing);
    }

    @Override
    public final void clear() {
        first = SparseArray.empty();
        collisions = SparseArray.empty();
        size = 0;
    }

    @Override
    @Realtime(limit = LINEAR)
    public FastSet<E> clone() {
        FastSet<E> copy = (FastSet<E>) super.clone();
        copy.first = first.clone();
        copy.collisions = collisions.clone();
        for ( FastListIterator<SortedTable<E>> itr = collisions.iterator(0); itr.hasNext();) {
            int index = itr.nextIndex();
            SortedTable<E> set = itr.next();
            copy.collisions.set(index, set.clone());
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
        return new DescendingIteratorImpl(null);
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
    @SuppressWarnings("unchecked")
    public final boolean remove(Object element) {
        int index = order.indexOf((E) element);
        E removed = first.get(index);
        if (removed == null) return false; // Not found.
        SortedTable<E> others = collisions.get(index);
        if (order.areEqual((E)element, removed)) {
            first = first.set(index, others != null ? others.removeFirst() : null);
        } else {
            if (others == null) return false;
            int insertionIndex = others.firstIndex((E)element, order);
            if ((insertionIndex >= others.size()) || !order.areEqual((E)element, others.get(insertionIndex)))
                return false;
            others.remove(insertionIndex);
        }
        if ((others != null) && (others.size() == 0)) collisions = collisions.set(index, null);
        --size;
        return true;
    }

    @Override
    public final boolean removeIf(Predicate<? super E> filter) {
        int initialSize = size;
        FastListIterator<E> firstItr = first.iterator(0);
        while (firstItr.hasNext()) {
            int index = firstItr.nextIndex();
            E firstElement = firstItr.next();
            SortedTable<E> others = collisions.get(index);
            if (filter.test(firstElement)) {
                first.set(index, (others != null) ? others.removeFirst() : null);
                --size;
            }
            int initialOtherSize = others.size();
            others.removeIf(filter);
            size -= initialOtherSize - others.size();
            if (others.isEmpty()) collisions = collisions.set(index, null);
        }
        return initialSize != size();
    }

    @Realtime(limit = CONSTANT)
    @Override
    public final int size() {
        return size;
    }       
   
    /** Ascending iterator implementation. */
    private final class AscendingIteratorImpl implements FastIterator<E> {
        private final FastListIterator<E> firstItr;
        private FastIterator<E> nextItr; // if 'null' then next() is firstItr.next()

        public AscendingIteratorImpl(@Nullable E from) {
             firstItr = first.iterator(from != null ? order.indexOf(from) : 0);
             if ((from == null) || !firstItr.hasNext()) return; // Ready.
             int index = firstItr.nextIndex();
             E firstElement = first.get(index);
             if (order.compare(from, firstElement) <=  0) return; // 'from' not greater than first element at index.
             SortedTable<E> others = collisions.get(index);
             if (others != null) {
                 nextItr = others.listIterator(others.firstIndex(from, order));
             } else { 
                firstItr.next(); 
             }
        }

        @Override
        public boolean hasNext() {
            return firstItr.hasNext() || ((nextItr != null) && (nextItr.hasNext()));
        }

        @Override
        public boolean hasNext(final Predicate<? super E> matching) { // Move iterators until matching verified.
            while (true) {
                if (nextItr != null) { // next iteration in progress.
                    if (nextItr.hasNext(matching)) return true;
                    if (!firstItr.hasNext()) return false;
                    nextItr = null;
                    firstItr.next(); // Skips current index.
                } else { // next is first at index.
                    if (!firstItr.hasNext()) return false;
                    int index = firstItr.nextIndex();
                    if (matching.test(first.get(index))) return true;
                    SortedTable<E> others = collisions.get(index);
                    if (others != null) {
                        nextItr = others.iterator();
                    } else {
                        firstItr.next(); // Skips current index.
                    }
                }
            }
         }

        @Override
        public E next() {
            if ((nextItr != null) && (nextItr.hasNext())) return nextItr.next(); // next iteration in progress.
            int index = firstItr.nextIndex();
            SortedTable<E> others = collisions.get(index);
            nextItr = (others != null) ? others.iterator() : null;
            return firstItr.next();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    /** Descending iterator implementation. */
    private final class DescendingIteratorImpl implements FastIterator<E> { // TODO
        private final FastListIterator<E> firstItr;
        private FastIterator<E> nextItr; // if 'null' then next() is firstItr.previous()

        public DescendingIteratorImpl(@Nullable E from) {
            throw new UnsupportedOperationException("TODO"); 
        }

        @Override
        public boolean hasNext() {
            return firstItr.hasPrevious() || ((nextItr != null) && (nextItr.hasNext()));
        }

        @Override
        public boolean hasNext(final Predicate<? super E> matching) { // Move iterators until matching verified.
            throw new UnsupportedOperationException("TODO"); 
        }

        @Override
        public E next() {
            throw new UnsupportedOperationException("TODO"); 
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

      }

    /** 
     * Sorted table (fast insertion/deletion operations). 
     * Elements should always be inserted at their proper sorted position (firstIndex or lastIndex). */
    static class SortedTable<E> extends FastTable<E> { // TODO
        private static final long serialVersionUID = 0x700L; // Version.
    
        // Returns the (smallest) insertion index of the specified element (range 0..size())
        public final int firstIndex(E element, Comparator<? super E> cmp) { 
            return firstIndex(element, cmp, 0, size());
        }
    
        // Returns the (largest) insertion index of the specified element (range 0..size()).
        public final int lastIndex(E element, Comparator<? super E> cmp) { 
            return lastIndex(element, cmp, 0, size());
        }
    
        public SortedTable<E> clone() {
            return (SortedTable<E>) super.clone();
        }
                
        /** In sorted table find the first position real or "would be" of the specified element in the given range. */
        private int firstIndex(E element, Comparator<? super E> cmp, int start, int length) {
            if (length == 0) return start;
            int half = length >> 1;
            return cmp.compare(element, get(start + half)) <= 0 ? firstIndex(element, cmp, start, half) :
                firstIndex(element, cmp, start + half, length - half);
        }
  
        /** In sorted table find the last position real or "would be" of the specified element in the given range. */
        private int lastIndex(E element, Comparator<? super E> cmp, int start, int length) {
            if (length == 0) return start;
            int half = length >> 1;
            return cmp.compare(element, get(start + half)) < 0 ? lastIndex(element, cmp, start, half) :
                lastIndex(element, cmp, start + half, length - half);
        }
      
    }

}