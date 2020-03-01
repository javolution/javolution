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

import org.javolution.annotations.Realtime;
import org.javolution.util.function.Equality;
import org.javolution.util.function.Indexer;
import org.javolution.util.function.Order;

/**
 * High-performance ordered map / multimap based upon fast-access {@link FractalArray}. 
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
 * AbstractMap<Foo, Bar> linkedMultimap = new FastMap<Foo, Bar>().linked().multi(); 
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
 * Map updates can be especially fast using lambda expression (one call).
 *  
 * ```java
 * FastMap<String, Index> wordCounts = new FastMap<>(LEXICAL); // Sorted. 
 * ...
 * wordCounts.put(word, (previousCount) -> previousCount != null ? previousCount.next() : Index.ONE); 
 * ```
 * 
 * Finally, this class provides full support for multimaps (multimaps stores pairs of (key, value) where both 
 * key and value can appear several times).
 *  
 * ```java
 * AbstractMap<String, String> multimap = new FastMap<String, String>().linked().multi(); // Keep insertion order.
 * for (President pres : US_PRESIDENTS_IN_ORDER) {
 *     multimap.put(pres.firstName(), pres.lastName()); 
 * }
 * for (String firstName : multimap.keySet().distinct()) { // keySet() returns a multiset (duplicate keys)
 *     Collection<String> lastNames = multimap.subMap(firstName).values();
 *     System.out.println(firstName + ": " + lastNames);
 * }
 * >> Zachary: {Taylor}
 * >> John: {Adams, Adams, Tyler, Kennedy} 
 * >> George: {Washington, Bush, Bush}
 * >> Grover: {Cleveland, Cleveland}
 * >> ...
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

    /** Immutable Map (can only be created through the {@link #freeze()} method). */
    public static final class Immutable<K,V> extends FastMap<K,V> implements org.javolution.lang.Immutable {
        private static final long serialVersionUID = FastMap.serialVersionUID;
        
        private Immutable(Order<? super K> keyOrder, Equality<? super V> valuesEquality, FastSet<Entry<K,V>> entries) {
            super(keyOrder, valuesEquality, entries);
        }
 
        public V updateValue(Entry<K, V> entry, V newValue) {  
            throw new UnsupportedOperationException("Immutable map");
        }

    }
 
    private final Order<? super K> keyOrder; 
    private final Equality<? super V> valuesEquality; 
    private final FastSet<Entry<K,V>> entries; 
    
    /** Creates a {@link Equality#STANDARD standard} map arbitrarily ordered. */
    public FastMap() {
        this(Order.standard());
    }

    /** Creates a {@link Equality#STANDARD standard} map ordered using the specified indexer function 
     * (convenience method).*/
    public FastMap(final Indexer<? super K> indexer) {
        this(Order.valueOf(indexer));
     }

    /** Creates a custom map ordered using the specified key order. */
    public FastMap(final Order<? super K> keyOrder) {
        this(keyOrder, Equality.standard());
    }

    /** Creates a custom map ordered using the specified key order and using the specified equality for 
     *  its map's values. */
    public FastMap(Order<? super K> keyOrder, Equality<? super V> valuesEquality) {
        this.keyOrder = keyOrder;
        this.valuesEquality = valuesEquality;
        this.entries = new FastSet<Entry<K,V>>(new Order<Entry<K,V>>() {
            private static final long serialVersionUID = FastMap.serialVersionUID;

            @Override
            public boolean areEqual(Entry<K, V> left, Entry<K, V> right) {
            	if (left == right) return true;
            	if ((left == null) || (right == null)) return false;
                boolean sameKey = FastMap.this.keyOrder.areEqual(left.getKey(), right.getKey());
                boolean anyEntryWithoutValue = (left instanceof EntryWithoutValue) || (right instanceof EntryWithoutValue);
                boolean sameValue = FastMap.this.valuesEquality.areEqual(left.getValue(), right.getValue());
                return sameKey && (anyEntryWithoutValue || sameValue);
            }

            @Override
            public int compare(Entry<K, V> left, Entry<K, V> right) {
            	if (left == null) return -1;
            	if (right == null) return 1;
                return FastMap.this.keyOrder.compare(left.getKey(), right.getKey());
            }

            @Override
            public long indexOf(Entry<K, V> entry) {
            	if (entry == null) return 0;
                return FastMap.this.keyOrder.indexOf(entry.getKey());
            }

        });
    }
    
    /**  Base constructor (private). */
    private FastMap(Order<? super K> keyOrder, Equality<? super V> valuesEquality, FastSet<Entry<K,V>> entries) {
       this.keyOrder = keyOrder;
       this.valuesEquality = valuesEquality;
       this.entries = entries;
    }
    
    @Override
    public final FastSet<Entry<K, V>> entries() {
        return entries;
    }

    /** Freezes this map and returns the corresponding {@link Immutable} instance (cannot be reversed). */
    public final Immutable<K,V> freeze() {
        return new Immutable<K,V>(keyOrder, valuesEquality, entries.freeze());
    }

    @Override
    public FastMap<K,V> with(K key, V value) {
        put(key, value);
        return this;
    }

    @Override
    public final int size() {
        return entries.size();
    }

    @Override
    public final Equality<? super V> valuesEquality() {
        return valuesEquality;
    }

    @Override
    @Realtime(limit = LINEAR)
    public FastMap<K, V> clone() {
        return new FastMap<K,V>(keyOrder, valuesEquality, entries.clone());
    }

    public static class EntryWithoutValue<K, V> extends Entry<K, V> {

        public EntryWithoutValue(K key) {
            super(key, null);
        }
    }

    @Override
    public final Entry<K, V> getEntry(K key) {
        return entries.getAny(new EntryWithoutValue<K,V>(key));
    }
   
    @Override
    public final Entry<K, V> removeEntry(K key) {
        return entries.removeAny(new EntryWithoutValue<>(key));
    }

    @Override
    public final void clear() {
        entries.clear();
    }

    @Override
    public final boolean isEmpty() {
        return entries.isEmpty();
    }

    @Override
    public final Order<? super K> keyOrder() {
        return keyOrder;
    }

    @Override
    public final Entry<K, V> addEntry(K key, V value) {
        Entry<K,V> entry = new Entry<K,V>(key, value);
        entries.add(entry, true /* allowDuplicate */);
        return entry;
    }
    
}
