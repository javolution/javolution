/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.util;

import static org.javolution.annotations.Realtime.Limit.LINEAR;

import java.util.Comparator;
import java.util.Map;

import org.javolution.annotations.Nullable;
import org.javolution.annotations.Realtime;
import org.javolution.lang.MathLib;
import org.javolution.util.FastSet.SortedTable;
import org.javolution.util.function.Equality;
import org.javolution.util.function.Indexer;
import org.javolution.util.function.Order;

/**
 * High-performance ordered map / multimap based upon fast-access {@link SparseArray}. 
 *     
 * Iterations order over map keys, values or entries is determined by the map {@link #keyOrder key order} except 
 * for specific views such as the {@link #linked linked view} for which iteration is performed according to the 
 * insertion order.
 *     
 * Instances of this class can advantageously replace any {@code java.util.*} map in terms of adaptability, 
 * space or performance.
 *  
 * ```java
 * import static javolution.util.function.Order.*;
 * 
 * // Instances
 * FastMap<Foo, Bar> hashMap = new FastMap<Foo, Bar>(); // Arbitrary order (hash-based).
 * FastMap<Foo, Bar> identityMap = new FastMap<Foo, Bar>(IDENTITY);
 * FastMap<String, Bar> treeMap = new FastMap<String, Bar>(LEXICAL);
 * FastMap<Integer, Foo> customIndexing = new FastMap<Integer, Foo>(i -> i.intValue()));
 * 
 * // Specialized Views.
 * AbstractMap<Foo, Bar> multimap = new FastMap<Foo, Bar>().multi(); // More than one value per key.
 * AbstractMap<Foo, Bar> linkedHashMap = new FastMap<Foo, Bar>().linked(); // Insertion order (in place of key order).
 * AbstractMap<Foo, Bar> linkedIdentityMap = new FastMap<Foo, Bar>(IDENTITY).linked();
 * AbstractMap<Foo, Bar> concurrentHashMap = new FastMap<Foo, Bar>().shared();  // Thread-safe.
 * AbstractMap<String, Bar> concurrentSkipListMap = new FastMap<Foo, Bar>(LEXICAL).shared(); // Thread-safe.
 * AbstractMap<Foo, Bar> linkedMultimap = new FastMap<Foo, Bar>().multi().linked(); 
 * ...
 * AbstractMap<Foo, Bar> identityLinkedAtomicMap = new FastMap<Foo, Bar>(IDENTITY).linked().atomic(); // Thread-safe.
 * ```
 * 
 * FastMap supports a great diversity of views.
 * <ul>
 *    <li>{@link #multi} - View for which the {@link #put} method does not check for previously contained mapping for 
 *                         the key (allowing for multiple entries with the same key).</li>
 *    <li>{@link #subMap} - View over a range of entries (based on map's order).</li>
 *    <li>{@link #headMap} - View over the head portion of the map.</li>
 *    <li>{@link #tailMap} - View over the tail portion of the map.</li>
 *    <li>{@link #entrySet} - View over the map entries.</li>
 *    <li>{@link #keySet} - View over the map keys allowing keys to be removed or added (entries with {@code null} values).</li>
 *    <li>{@link #values} - View over the map values (removal is supported but not adding new values).</li>
 *    <li>{@link #shared} - Thread-safe view based on <a href=
 *                          "http://en.wikipedia.org/wiki/Readers%E2%80%93writer_lock">readers-writer locks</a>.</li>
 *    <li>{@link #atomic} - Thread-safe view for which all reads are mutex-free and map updates 
 *                           (e.g. {@link #putAll putAll}) are atomic.</li>
 *    <li>{@link #reversed} - Reversed order view.</li>
 *    <li>{@link #linked} - View exposing each entry based on the {@link #put insertion} order in the view.</li>
 *    <li>{@link #unmodifiable} - View which does not allow for modification.</li>
 *    <li>{@link #valuesEquality} - View using the specified equality comparator for the map's values.</li>
 * </ul>      
 * 
 * The entry/key/value views over a map are instances of {@link AbstractCollection} which supports parallel processing.
 * 
 * ```java
 * FastMap<String, Integer> ranking = new FastMap<>().with("John Doe", 234).with("Jane Dee", 123).with("Sam Anta", null); 
 * ranking.values().removeIf(v -> v == null); // Remove all entries with null values.
 * ranking.values().parallel().removeIf(v -> v == null); // Same but performed in parallel.
 * ```
 * 
 * Unlike {@code ConcurrentHashMap}, FastMap allows for {@code null} values; to differentiate between no entry
 * and a {@code null} value, the method {@link #getEntry} can be used in place of {@link #get}.
 *  
 * ```java
 * FastMap<String, Index> wordCounts = new FastMap<>(Order.LEXICAL); 
 * Entry<String, Index> count = wordCounts.getEntry(word); 
 * wordsCount.put(word, (count != null) ? count.getValue().next() : Index.ONE);
 * ```
 * 
 * Finally, this class provides full support for multimaps (multimaps stores pairs of (key, value) where both 
 * key and value can appear several times).
 *  
 * ```java
 *  FastMap<String, String> multimap = new FastMap<String, String>().multi().linked(); // Keep insertion order.
 *  for (President pres : US_PRESIDENTS_IN_ORDER) {
 *      multimap.put(pres.firstName(), pres.lastName());
 *  }
 *  for (String firstName : multimap.keySet().distinct()) { // keySet() returns a multiset (duplicate keys)
 *      FastCollection<String> lastNames = multimap.subMap(firstName).values();
 *      System.out.println(firstName + ": " + lastNames);
 *  }
 *  >> Zachary: {Taylor}
 *  >> John: {Adams, Adams, Tyler, Kennedy} 
 *  >> George: {Washington, Bush, Bush}
 *  >> Grover: {Cleveland, Cleveland}
 *  >> ...
 * ```
 * 
 * @param <K> the type of keys ({@code null} values are not supported)
 * @param <V> the type of values 
 *             
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle </a>
 * @version 7.0, September 13, 2015
 */
@Realtime
public class FastMap<K, V> extends AbstractMap<K, V> {

    private static final long serialVersionUID = 0x700L; // Version.

    /** Immutable Map (can only be created through the {@link #immutable()} method). */
    public static final class Immutable<K,V> extends FastMap<K,V> implements org.javolution.lang.Immutable {
        private static final long serialVersionUID = FastMap.serialVersionUID;
        private Immutable(Order<? super K> order, SparseArray<Entry<K,V>> first, 
                SparseArray<SortedEntries<K,V>> collisions, int size) {
            super(order, first, collisions, size);
        }
    }
 
    private final Order<? super K> keyOrder;
    private SparseArray<Entry<K,V>> first; // Hold element at first position at index.
    private SparseArray<SortedEntries<K,V>> collisions; // Holds others elements (sorted). 
    private int size; // Keep tracks of the size since sparse array are unbounded.
    
    /** Creates a {@link Equality#STANDARD standard} map arbitrarily ordered. */
    public FastMap() {
        this(Equality.STANDARD);
    }

    /** Creates a {@link Equality#STANDARD standard} map ordered using the specified indexer function 
     * (convenience method).*/
    public FastMap(final Indexer<? super K> indexer) {
        this(new Order<K>() {
            private static final long serialVersionUID = FastMap.serialVersionUID;

            @Override
            public boolean areEqual(K left, K right) {
                return left.equals(right); // K cannot be null.
            }

            @Override
            public int compare(K left, K right) {
                int leftIndex = indexer.indexOf(left);
                int rightIndex = indexer.indexOf(right);
                if (leftIndex == rightIndex) return 0;
                return MathLib.unsignedLessThan(leftIndex, rightIndex) ? -1 : 1;
            }

            @Override
            public int indexOf(K obj) {
                return indexer.indexOf(obj);
            }

         });
    }

    /** Creates a custom map ordered using the specified key order. */
    public FastMap(Order<? super K> keyOrder) {
        this.keyOrder = keyOrder;
        this.first = SparseArray.empty();
        this.collisions = SparseArray.empty();        
    }

    /**  Base constructor (private). */
    private FastMap(Order<? super K> keyOrder,  SparseArray<Entry<K,V>> first, 
            SparseArray<SortedEntries<K,V>> collisions, int size) {
       this.keyOrder = keyOrder;
       this.first = first;
       this.collisions = collisions;
       this.size = size;
    }
    
    /** Makes this map immutable and returns the corresponding {@link Immutable} instance (cannot be reversed). */
    public final Immutable<K,V> immutable() {
        first = first.unmodifiable();
        collisions = collisions.unmodifiable();
        for (SortedEntries<K,V> set : collisions) set.immutable();
        return new Immutable<K,V>(keyOrder, first, collisions, size);
    }

    @Override
    public final FastMap<K,V> with(K key, V value) {
        put(key, value);
        return this;
    }

    @Override
    public final FastMap<K,V> with(Map<? extends K, ? extends V> that) {
        putAll(that);
        return this;
    }

    @Override
    public final V put(K key, @Nullable V value) {
        return super.put(key, value);
    }

    @Override
    public final int size() {
        return size;
    }

    @Override
    public final Equality<? super V> valuesEquality() {
        return Equality.STANDARD;
    }

    @Override
    @Realtime(limit = LINEAR)
    public FastMap<K, V> clone() {
        FastMap<K,V> copy = (FastMap<K,V>) super.clone();
        copy.first = first.clone();
        copy.collisions = collisions.clone();
        for ( FastListIterator<SortedEntries<K,V>> itr = collisions.iterator(0); itr.hasNext();) {
            int index = itr.nextIndex();
            SortedEntries<K,V> set = itr.next();
            copy.collisions.set(index, set.clone());
        }
        return copy;
    }

    @Override
    public final Entry<K, V> getEntry(K key) {
        int index = keyOrder.indexOf(key);
        Entry<K,V> existing  = first.get(index);
        if (existing == null) return null;
        if (keyOrder.areEqual(key, existing.getKey())) return existing;
        SortedEntries<K,V> others = collisions.get(index);
        if (others == null) return null;
        int insertionIndex = others.firstKeyIndex(key, keyOrder);
        if (insertionIndex >= others.size()) return null;
        existing = others.get(insertionIndex);
        return keyOrder.areEqual(key, existing.getKey()) ? existing : null; 
      }

    @Override
    public boolean addEntry(Entry<K, V> entry) { // Equivalent to SortedSet.addMulti
        int index = keyOrder.indexOf(entry.getKey());
        Entry<K,V> existing = first.get(index);
        if (existing == null) { // Most frequent.
            first = first.set(index, entry);
        } else { // Multiple instances at index.
            SortedEntries<K,V> others = collisions.get(index);
            if (others == null) collisions = collisions.set(index, (others = new SortedEntries<K,V>()));
            if (keyOrder.compare(entry.getKey(), existing.getKey()) < 0) { // New element should be first.
                others.addFirst(existing);
                first.set(index, entry); // Replaces existing as first.
            } else {
                int insertionIndex = others.firstKeyIndex(entry.getKey(), keyOrder);
                others.add(insertionIndex, entry);
            }
        }
        size++;
        return true;
    }

    @Override
    public Entry<K, V> removeEntry(K key) {
        int index = keyOrder.indexOf(key);
        Entry<K,V> removed  = first.get(index);
        if (removed == null) return null; // Not found.
        SortedEntries<K,V> others = collisions.get(index);
        if (keyOrder.areEqual(key, removed.getKey())) {
            first = first.set(index, others != null ? others.removeFirst() : null);
        } else {
            if (others == null) return null;
            int insertionIndex = others.firstKeyIndex(key, keyOrder);
            if ((insertionIndex >= others.size()) || !keyOrder.areEqual(key, others.get(insertionIndex).getKey()))
                return null;
            removed = others.remove(insertionIndex);
        }
        if ((others != null) && (others.size() == 0)) collisions = collisions.set(index, null);
        --size;
        return removed;
   }

    @Override
    public void clear() {
        first = SparseArray.empty();
        collisions = SparseArray.empty();
        size = 0;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public Order<? super K> keyOrder() {
        return keyOrder;
    }

    @Override
    public FastIterator<Entry<K, V>> iterator(@Nullable K from) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public FastIterator<Entry<K, V>> descendingIterator(@Nullable K from) {
        // TODO Auto-generated method stub
        return null;
    }
    
    /** Sorted table for Entry<K,V>.*/
    private static final class SortedEntries<K,V> extends SortedTable<Entry<K,V>> {
        private static final long serialVersionUID = FastMap.serialVersionUID;
        
        // Returns the (smallest) insertion index of the specified key (range 0..size())
        public final int firstKeyIndex(K key, Comparator<? super K> cmp) { 
            return firstKeyIndex(key, cmp, 0, size());
        }
    
        // Returns the (largest) insertion index of the specified key (range 0..size()).
        public final int lastKeyIndex(K key, Comparator<? super K> cmp) { 
            return lastKeyIndex(key, cmp, 0, size());
        }
                    
        @SuppressWarnings("unchecked")
        public SortedEntries<K,V> clone() {
            return (SortedEntries<K,V>) super.clone();
        }
                
          /** In sorted table find the first position real or "would be" of the specified element in the given range. */
        private int firstKeyIndex(K key, Comparator<? super K> cmp, int start, int length) {
            if (length == 0) return start;
            int half = length >> 1;
            return cmp.compare(key, get(start + half).getKey()) <= 0 ? firstKeyIndex(key, cmp, start, half) :
                firstKeyIndex(key, cmp, start + half, length - half);
        }
  
        /** In sorted table find the last position real or "would be" of the specified element in the given range. */
        private int lastKeyIndex(K key, Comparator<? super K> cmp, int start, int length) {
            if (length == 0) return start;
            int half = length >> 1;
            return cmp.compare(key, get(start + half).getKey()) < 0 ? lastKeyIndex(key, cmp, start, half) :
                lastKeyIndex(key, cmp, start + half, length - half);
        }   
        
    }

}
