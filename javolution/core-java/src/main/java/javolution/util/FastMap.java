/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javolution.context.LogContext;
import javolution.lang.Copyable;
import javolution.lang.Functor;
import javolution.lang.Immutable;
import javolution.lang.Predicate;

/**
 * <p> Hash map with real-time behavior; smooth capacity increase and 
 *     <i>thread-safe</i> behavior without external synchronization when
 *     {@link #shared shared}. The capacity of a fast map is automatically 
 *     adjusted to best fit its size (e.g. when a map is cleared its memory 
 *     footprint is minimal).</p>
 *     <img src="doc-files/map-put.png"/>
 * 
 * <p> The iteration order for the fast map is non-deterministic, 
 *     for a predictable insersion order, the  {@link #ordered ordered}
 *     view can be used (equivalent to {@link LinkedHashMap}).</p> 
 *     
 * <p> Fast maps may use custom {@link #keyComparator key comparator}
 *     or {@link #valueComparator value comparator}, <code>null</code> 
 *     keys are supported.
 *     [code]
 *     FastMap<Foo, Bar> identityMap = new FastMap() {
 *          public FastComparator<Foo> keyComparator() {
 *              return FastComparator.IDENTITY;
 *          }
 *     }
 *     [/code]
 *     Fast maps can advantageously replace any of the standard 
 *     <code>java.util</code> maps. For example:
 *     [code]
 *     Map<Foo, Bar> concurrentHashMap = new FastMap().shared(); // ConcurrentHashMap
 *     Map<Foo, Bar> linkedHashMap = new FastMap().ordered(); // LinkedHashMap
 *     Map<Foo, Bar> linkedConcurrentHashMap = new FastMap().ordered().shared(); // Does not exist in java.util
 *     Map<Foo, Bar> unmodifiableConcurrentMap = new FastMap().shared().unmodifiable(); // Const. view on a concurrent map.
 *     ...
 *     [/code]</p>
 * 
 * <p> Implementation Notes: {@link FastTable#shared Shared} maps  
 *     use multiples {@link ReentrantReadWriteLock read-write locks}
 *     (hash-code based distribution) in order to support concurrent 
 *     reads/writes (or closure-based iterations) without blocking.</p>
 *            
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle </a>
 * @version 6.0.0, December 12, 2012
 */
public class FastMap<K, V> implements Map<K, V>, Copyable<FastMap<K, V>> {

    // We only do a resize (and rehash) one block at a time.
    private static final int BLOCKS_LENGTH = 1 << 4; // Number of blocks (16), impacts concurrency.

    private static final int MASK = BLOCKS_LENGTH - 1; // Mask when accessing blocks.

    // Emptiness level. Can be 1 (load factor 0.5), 2 (load factor 0.25) or any greater value.
    private static final int EMPTINESS_LEVEL = 2;

    // Initial block capacity, no resize until count > 2 (third entry for the block).  
    private static final int INITIAL_BLOCK_CAPACITY = 2 << EMPTINESS_LEVEL;

    private final Block<K, V>[] blocks = new Block[BLOCKS_LENGTH];

    // Comparator for hash calculations, might be different from null
    // if either rehash is performed or the keyComparator is overloaded.
    private final FastComparator comparatorForHash;

    /**
     * Creates an empty map whose capacity increment or decrement smoothly
     * without large resize/rehash operations.
     */
    public FastMap() {
        System.arraycopy(NULL_BLOCKS, 0, blocks, 0, NULL_BLOCKS.length);
        FastComparator<K> keyComp = keyComparator();
        comparatorForHash = (keyComp == FastComparator.DEFAULT)
                ? (REHASH_DEFAULT_HASHCODE ? REHASH_COMPARATOR : null) : keyComp;
    }

    /**
     * Creates a map containing the specified entries, in the order they
     * are returned by the map iterator.
     *
     * @param map the map whose entries are to be placed into this map.
     */
    public FastMap(Map<? extends K, ? extends V> map) {
        this();
        putAll(map);
    }

    /**
     * Returns an {@link Unmodifiable}/Immutable view of this map.
     * Attempts to modify the map returned will result in an 
     * {@link UnsupportedOperationException} being thrown. 
     */
    public Unmodifiable<K, V> unmodifiable() {
        return new Unmodifiable<K, V>(this);
    }

    /**
     * Returns a thread-safe read-write {@link Shared} view of this map.
     * It uses multiple {@link ReentrantReadWriteLock read-write lock} in
     * order to support concurrent reads/writes. Iterators methods have 
     * been deprecated since they don't prevent concurrent modifications. 
     * Closures (e.g. {@link FastCollection#doWhile}) 
     * are the preferred mean of iterating over the map entries.
     * The shared view is a {@link ConcurrentMap} instance.
     */
    public Shared<K, V> shared() {
        return new Shared<K, V>(this);
    }

    /**
     * Returns an ordered view of this map (insertion-order).
     * Closures (and iterations) over the map elements (key, values,
     * entries) have a predictable iteration order.
     */
    public Ordered<K, V> ordered() {
        return new Ordered<K, V>(this);
    }

    /**
     * Returns the key comparator used by this map;  it is also 
     * the comparator of the {@link #keySet()} of this map.
     */
    public FastComparator<K> keyComparator() {
        return (FastComparator<K>) FastComparator.DEFAULT;
    }

    /**
     * Returns the value comparator used by this map; it is the comparator
     * of this map {@link #values() }.
     */
    public FastComparator<V> valueComparator() {
        return (FastComparator<V>) FastComparator.DEFAULT;
    }

    //
    // Map implementation
    //
    /**
     * Returns the number of key-value mappings in this map.
     * 
     * @return this map's size.
     */
    public int size() {
        int sum = 0;
        for (Block b : blocks) {
            sum += b.count;
        }
        return sum;
    }

    /**
     * Indicates if this map contains no key-value mappings.
     * 
     * @return <code>true</code> if this map contains no key-value mappings;
     *         <code>false</code> otherwise.
     */
    public boolean isEmpty() {
        return size() == 0;
    }

    /**
     * Indicates if this map contains a mapping for the specified key.
     * 
     * @param key the key whose presence in this map is to be tested.
     * @return <code>true</code> if this map contains a mapping for the
     *         specified key; <code>false</code> otherwise.
     */
    public boolean containsKey(Object key) {
        int hash = hashFor(key);
        return blocks[hash & MASK].containsKey(key, hash);
    }

    /**
     * Indicates if this map associates one or more keys to the specified value.
     * 
     * @param value the value whose presence in this map is to be tested.
     * @return <code>true</code> if this map maps one or more keys to the
     *         specified value.
     */
    public boolean containsValue(Object value) {
        return values().contains(value);
    }

    /**
     * Returns the value to which this map associates the specified key.
     * 
     * @param key the key whose associated value is to be returned.
     * @return the value to which this map maps the specified key, or
     *         <code>null</code> if there is no mapping for the key.
     */
    public V get(Object key) {
        int hash = hashFor(key);
        return blocks[hash & MASK].get(key, hash);
    }

    /**
     * Associates the specified value with the specified key in this map.
     * If this map previously contained a mapping for this key, the old value
     * is replaced.
     * 
     * @param key the key with which the specified value is to be associated.
     * @param value the value to be associated with the specified key.
     * @return the previous value associated with specified key, or
     *         <code>null</code> if there was no mapping for key. A
     *         <code>null</code> return can also indicate that the map
     *         previously associated <code>null</code> with the specified key.
     */
    public V put(K key, V value) {
        int hash = hashFor((K) key);
        Block<K, V> block = blocks[hash & MASK];
        if (block == NULL_BLOCK) {
            block = blocks[hash & MASK] = new Block();
        }
        return block.put(key, value, hash);
    }

    /**
     * Copies all of the mappings from the specified map to this map.
     * If the entry set of the specified map is an instance of 
     * {@link FastCollection}, closure-based iterations are performed 
     * (safe even when the specified map is shared and concurrently 
     * modified).
     * 
     * @param map the mappings to be stored in this map.
     * @throws NullPointerException the specified map is <code>null</code>.
     */
    public final void putAll(Map<? extends K, ? extends V> map) {
        Set<Entry<K, V>> entries = this.entrySet();
        if (entries instanceof FastCollection) {
            FastCollection<Map.Entry<K, V>> fc =
                    (FastCollection<Map.Entry<K, V>>) entries;
            fc.doWhile(new Predicate<Map.Entry<K, V>>() {

                public Boolean evaluate(Entry<K, V> param) {
                    put(param.getKey(), param.getValue());
                    return true;
                }

            });
        }
        for (Iterator i = map.entrySet().iterator(); i.hasNext();) {
            Map.Entry<K, V> e = (Map.Entry<K, V>) i.next();
            put(e.getKey(), e.getValue());
        }
    }

    /**
     * Removes the entry for the specified key if present.
     * 
     * @param key the key whose mapping is to be removed from the map.
     * @return previous value associated with specified key, or
     *         <code>null</code> if there was no mapping for key. A
     *         <code>null</code> return can also indicate that the map
     *         previously associated <code>null</code> with the specified key.
     */
    public final V remove(Object key) {
        int hash = hashFor((K) key);
        Block<K, V> block = blocks[hash & MASK];
        if (block == NULL_BLOCK) return null;
        return block.remove(key, hash);
    }

    /**
     * Removes all of the mappings from this map; the capacity of the map
     * is then reduced to its minimum (reduces memory footprint).
     */
    public void clear() {
        for (Block b : blocks) {
            b.clear();
        }
    }

    /**
     * Returns a {@link FastCollection} view of the keys contained in this map.
     * The set is backed by the map, so changes to the map are
     * reflected in the set, and vice-versa.
     */
    public KeySet<K> keySet() {
        return new KeySet<K>(this);
    }

    /**
     * Returns a {@link FastCollection} view of the values contained in this map.
     * The collection is backed by the map, so changes to the map are
     * reflected in the collection, and vice-versa. 
     */
    public Values<V> values() {
        return new Values<V>(this);
    }

    /**
     * Returns a {@link FastCollection} view of the mappings contained in 
     * this map. The set is backed by the map, so changes to the map are
     * reflected in the set, and vice-versa. 
     */
    public EntrySet<K, V> entrySet() {
        return new EntrySet<K, V>(this);
    }

    /**
     * Returns a deep copy of this object. 
     * 
     * @return an object identical to this object but possibly allocated 
     *         in a different memory space.
     * @see Copyable
     */
    public FastMap<K, V> copy() {
        final FastComparator<K> keyComp = keyComparator();
        final FastComparator<V> valueComp = valueComparator();
        final FastMap<K, V> newMap = new FastMap() {

            @Override
            public FastComparator<K> keyComparator() {
                return keyComp;
            }

            @Override
            public FastComparator<V> valueComparator() {
                return valueComp;
            }

        };
        this.entrySet().doWhile(new Predicate<Entry<K, V>>() {

            public Boolean evaluate(Entry<K, V> param) {
                K k = param.getKey();
                k = (k instanceof Copyable) ? ((Copyable<K>) k).copy() : k;
                V v = param.getValue();
                v = (v instanceof Copyable) ? ((Copyable<V>) v).copy() : v;
                newMap.put(k, v);
                return true;
            }

        });
        return newMap;
    }

    /**
     * Associates the specified value only if the specified key is not already
     * associated. This is equivalent to:[code]
     *   if (!map.containsKey(key))
     *       return map.put(key, value);
     *   else
     *       return map.get(key);[/code]
     * except that for shared maps the action is performed atomically.
     * For shared maps, this method guarantees that if two threads try to 
     * put the same key concurrently only one of them will succeed.
     *
     * @param key the key with which the specified value is to be associated.
     * @param value the value to be associated with the specified key.
     * @return the previous value associated with specified key, or
     *         <code>null</code> if there was no mapping for key. A
     *         <code>null</code> return can also indicate that the map
     *         previously associated <code>null</code> with the specified key.
     * @throws NullPointerException if the key is <code>null</code>.
     */
    public V putIfAbsent(K key, V value) {
        return null;
    }

    //
    // Views Inner Classes 
    //  
    /**
     * An unmodifiable/{@link Immutable immutable} view over a fast map.
     */
    public static final class Unmodifiable<K, V> implements Map<K, V>, Immutable {

        private final Map<K, V> that;

        Unmodifiable(Map<K, V> that) {
            this.that = that;
        }

        public int size() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public boolean isEmpty() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public boolean containsKey(Object key) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public boolean containsValue(Object value) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public V get(Object key) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public V put(K key, V value) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public V remove(Object key) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void putAll(Map<? extends K, ? extends V> m) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void clear() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Set<K> keySet() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Collection<V> values() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Set<Entry<K, V>> entrySet() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

    }

    /**
     * A shared view over a fast map. It uses multiples 
     * {@link ReentrantReadWriteLock read-write locks} in order to support 
     * concurrent read and write of the map.
     * Iterators methods have been deprecated since they don't prevent 
     * concurrent modifications. Closures (e.g. {@link FastCollection#doWhile}) 
     * are the preferred mean of iterating over the map elements.
     */
    public static final class Shared<K, V> implements ConcurrentMap<K, V> {

        final ReentrantReadWriteLock rwl[] = new ReentrantReadWriteLock[BLOCKS_LENGTH];

        final FastMap<K, V> that;

        Shared(FastMap<K, V> that) {
            this.that = that;
            for (int i = 0; i < BLOCKS_LENGTH;) {
                rwl[i] = new ReentrantReadWriteLock();
            }
        }

        public V putIfAbsent(K key, V value) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public boolean remove(Object key, Object value) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public boolean replace(K key, V oldValue, V newValue) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public V replace(K key, V value) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public int size() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public boolean isEmpty() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public boolean containsKey(Object key) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public boolean containsValue(Object value) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public V get(Object key) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public V put(K key, V value) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public V remove(Object key) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void putAll(Map<? extends K, ? extends V> m) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void clear() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public KeySet<K> keySet() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Collection<V> values() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Set<Entry<K, V>> entrySet() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

    }

    /**
     * A view over a fast map with predictable iteration order (insertion-order).
     */
    public static final class Ordered<K, V> implements Map<K, V> {

        final FastMap<K, V> that;

        Ordered(FastMap<K, V> that) {
            this.that = that;
        }

        public int size() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public boolean isEmpty() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public boolean containsKey(Object key) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public boolean containsValue(Object value) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public V get(Object key) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public V put(K key, V value) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public V remove(Object key) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void putAll(Map<? extends K, ? extends V> m) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void clear() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Set<K> keySet() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Collection<V> values() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Set<Entry<K, V>> entrySet() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

    }

    /**
     * A fast collection view over the map keys.
     */
    public static final class KeySet<K> extends FastCollection<K> implements Set<K> {

        final FastMap<K, ?> that;

        KeySet(FastMap<K, ?> that) {
            this.that = that;
        }

        @Override
        public FastCollection<K> unmodifiable() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public FastCollection<K> shared() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public <R> FastCollection<R> forEach(Functor<K, R> functor) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void doWhile(Predicate<K> predicate) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean removeAll(Predicate<K> predicate) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Iterator<K> iterator() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public FastCollection<K> usingComparator(FastComparator<K> comparator) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

    }

    /**
     * A fast collection view over the map entries.
     */
    public static final class EntrySet<K, V> extends FastCollection<Entry<K, V>> implements Set<Entry<K, V>> {

        final FastMap<K, V> that;

        EntrySet(FastMap<K, V> that) {
            this.that = that;
        }

        @Override
        public FastCollection<Entry<K, V>> unmodifiable() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public FastCollection<Entry<K, V>> shared() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public <R> FastCollection<R> forEach(Functor<Entry<K, V>, R> functor) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void doWhile(Predicate<Entry<K, V>> predicate) {
            if (that.size() == 0) return;
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean removeAll(Predicate<Entry<K, V>> predicate) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Iterator<Entry<K, V>> iterator() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public FastCollection<Entry<K, V>> usingComparator(FastComparator<Entry<K, V>> comparator) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

    }

    /**
     * A fast collection view over the map values.
     */
    public static final class Values<V> extends FastCollection<V> {

        final FastMap<?, V> that;

        Values(FastMap<?, V> that) {
            this.that = that;
        }

        @Override
        public FastCollection<V> unmodifiable() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public FastCollection<V> shared() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public <R> FastCollection<R> forEach(Functor<V, R> functor) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void doWhile(Predicate<V> predicate) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean removeAll(Predicate<V> predicate) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Iterator<V> iterator() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public FastCollection<V> usingComparator(FastComparator<V> comparator) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

    }

    //
    // Internal implementation. 
    // 
    private static final Block NULL_BLOCK = new Block();

    private static final Block[] NULL_BLOCKS = new Block[BLOCKS_LENGTH];

    static {
        Arrays.fill(NULL_BLOCKS, NULL_BLOCK);
    }

    static final class Block<K, V> {

        int count; // Number of entries different from null in this block.

        EntryImpl<K, V>[] entries = (EntryImpl<K, V>[]) new EntryImpl[INITIAL_BLOCK_CAPACITY];

        boolean containsKey(Object key, int hash) {
            return entries[indexOfKey(key, hash)] != null;
        }

        V get(Object key, int hash) {
            EntryImpl<K, V> entry = entries[indexOfKey(key, hash)];
            return (entry != null) ? entry.value : null;
        }

        V put(K key, V value, int hash) {
            int i = indexOfKey(key, hash);
            EntryImpl<K, V> entry = entries[i];
            if (entry != null) { // Entry exists.
                V oldValue = entry.value;
                entry.value = value;
                return oldValue;
            }
            entries[i] = new EntryImpl(key, value, hash);
            // Check if we need to resize.
            if ((++count << EMPTINESS_LEVEL) > entries.length) {
                resize(entries.length << 1);
            }
            return null;
        }

        V remove(Object key, int hash) {
            int i = indexOfKey(key, hash);
            EntryImpl<K, V> oldEntry = entries[i];
            if (oldEntry == null) return null; // Entry does not exist.
            entries[i] = null;
            // Since we have made a hole, adjacent keys might have to shift.
            for (;;) {
                // We use a step of 1 (improve caching through memory locality).
                i = (i + 1) & (entries.length - 1);
                EntryImpl<K, V> entry = entries[i];
                if (entry == null) break; // Done.
                int correctIndex = indexOfKey(entry.key, entry.hash);
                if (correctIndex != i) { // Misplaced.
                    entries[correctIndex] = entries[i];
                    entries[i] = null;
                }
            }
            // Check if we need to resize.
            if (((--count << (EMPTINESS_LEVEL + 1)) <= entries.length)
                    && (entries.length > INITIAL_BLOCK_CAPACITY)) {
                resize(entries.length >> 1);
            }
            return oldEntry.value;
        }

        void clear() {
            entries = (EntryImpl<K, V>[]) new EntryImpl[INITIAL_BLOCK_CAPACITY];
            count = 0;
        }

        // The capacity is a power of two such as: 
        //    (count * 2**EMPTINESS_LEVEL) <=  capacity < (count * 2**(EMPTINESS_LEVEL+1))
        void resize(int newCapacity) {
            EntryImpl<K, V>[] newEntries = (EntryImpl<K, V>[]) new EntryImpl[newCapacity];
            int newMask = newEntries.length - 1;
            for (int i = 0, n = entries.length; i < n; i++) {
                EntryImpl<K, V> entry = entries[i];
                if (entry == null) continue;
                int newIndex = entry.hash & newMask;
                while (newEntries[newIndex] != null) { // Find empty slot.
                    newIndex = (newIndex + 1) & newMask;
                }
                newEntries[newIndex] = entry;
            }
            entries = newEntries;
        }

        // Returns the index of the specified key in the map (points to a null key if key not present).
        int indexOfKey(Object key, int hash) {
            int mask = entries.length - 1;
            int i = hash & mask;
            while (true) {
                EntryImpl<K, V> entry = entries[i];
                if (entry == null) return i;
                if ((entry.hash == hash) && key.equals(entry.key)) return i;
                i = (i + 1) & mask;
            }
        }

    }

    // Entry implementation.
    static final class EntryImpl<K, V> implements Map.Entry<K, V> {

        final K key;

        V value;

        int hash;

        EntryImpl(K key, V value, int hash) {
            this.key = key;
            this.value = value;
            this.hash = hash;
        }

        public K getKey() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public V getValue() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public V setValue(V value) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

    }

    // Calculates the hash value, this value has to be well distributed.
    private int hashFor(Object key) {
        return (comparatorForHash == null)
                ? ((key != null) ? key.hashCode() : 0)
                : comparatorForHash.hashCodeOf((K) key);
    }

    private static final boolean REHASH_DEFAULT_HASHCODE = FastMap.isPoorSystemHash();

    private static boolean isPoorSystemHash() {
        boolean[] dist = new boolean[64]; // Length power of 2.
        for (int i = 0; i < dist.length; i++) {
            dist[new Object().hashCode() & (dist.length - 1)] = true;
        }
        int occupied = 0;
        for (int i = 0; i < dist.length;) {
            occupied += dist[i++] ? 1 : 0; // Count occupied slots.
        }
        boolean rehash = occupied < (dist.length >> 2); // Less than 16 slots on 64.
        if (rehash)
            LogContext.info("Poorly distributed system hash code - Rehashing performed.");
        return rehash;
    }

    private static final FastComparator<?> REHASH_COMPARATOR = new Rehash();

    private static final class Rehash<T> extends FastComparator<T> {

        public int hashCodeOf(T obj) {
            if (obj == null)
                return 0;
            // Formula identical <code>java.util.HashMap</code> to ensures
            // similar behavior for ill-conditioned hashcode keys.
            int h = obj.hashCode();
            h += ~(h << 9);
            h ^= (h >>> 14);
            h += (h << 4);
            return h ^ (h >>> 10);
        }

        public boolean areEqual(T o1, T o2) {
            return (o1 == null) ? (o2 == null) : (o1 == o2) || o1.equals(o2);
        }

        public int compare(T o1, T o2) {
            return ((Comparable) o1).compareTo(o2);
        }

        public FastComparator<T> copy() {
            return this;
        }

    };

}
