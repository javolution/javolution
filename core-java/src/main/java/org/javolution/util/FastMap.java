/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.util;

import static org.javolution.lang.Realtime.Limit.LINEAR;

import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentMap;

import org.javolution.lang.MathLib;
import org.javolution.lang.Realtime;
import org.javolution.text.Cursor;
import org.javolution.text.DefaultTextFormat;
import org.javolution.text.TextContext;
import org.javolution.text.TextFormat;
import org.javolution.util.function.Equality;
import org.javolution.util.function.Order;
import org.javolution.util.function.Predicate;
import org.javolution.util.internal.map.AtomicMapImpl;
import org.javolution.util.internal.map.CustomValuesEqualityMapImpl;
import org.javolution.util.internal.map.EntrySetImpl;
import org.javolution.util.internal.map.FilteredMapImpl;
import org.javolution.util.internal.map.KeySetImpl;
import org.javolution.util.internal.map.LinkedMapImpl;
import org.javolution.util.internal.map.ReversedMapImpl;
import org.javolution.util.internal.map.SharedMapImpl;
import org.javolution.util.internal.map.SubMapImpl;
import org.javolution.util.internal.map.UnmodifiableMapImpl;
import org.javolution.util.internal.map.ValuesImpl;

/**
 * <p> A high-performance ordered map (trie-based) with {@link Realtime strict timing constraints}.</p>
 *     
 * <p> In general, fast map methods have a limiting behavior in {@link Realtime#Limit#CONSTANT O(1)} (constant) 
 *     to be compared with {@link Realtime#Limit#LOG_N O(log n)} for most sorted maps.</p>
 * 
 * <p> Iterations order over map keys, values or entries is determined by the map {@link #comparator() order} 
 *     except for specific views such as the {@link #linked linked} view for which the iterations order is 
 *     the insertion order.</p>
 *     
 * <p> Instances of this class can advantageously replace any {@code java.util.*} map in terms of adaptability, 
 *     space or performance. 
 * <pre>{@code
 * FastMap<Foo, Bar> hashMap = FastMap.newMap(); // Hash order (default).
 * FastMap<Foo, Bar> identityHashMap = FastMap.newMap(Order.IDENTITY);
 * FastMap<String, Bar> treeMap = FastMap.newMap(Order.LEXICAL); 
 * FastMap<Foo, Bar> linkedHashMap = FastMap.<Foo, Bar>newMap().linked(); // Insertion order.
 * FastMap<Foo, Bar> concurrentHashMap = FastMap.<Foo, Bar>newMap().shared(); 
 * FastMap<String, Bar> concurrentSkipListMap = FastMap.<Foo, Bar>newMap(Order.LEXICAL).shared();
 * FastMap<Index, Foo> sparseArray = FastMap.newMap(Order.INDEX);
 * ...
 * }</pre> </p> 
 * <p> FastMap supports a great diversity of views.
 * <ul>
 *    <li>{@link #subMap} - View over a range of entries (based on map's order).</li>
 *    <li>{@link #headMap} - View over the head portion of the map.</li>
 *    <li>{@link #tailMap} - View over the tail portion of the map.</li>
 *    <li>{@link #entrySet} - View over the map entries allowing entries to be added/removed.</li>
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
 * </ul></p>      
 * 
 *  <p> The entry/key/value views over a map are instances of {@link FastCollection} which supports parallel processing.
 * <pre>{@code
 * FastMap<String, Integer> ranking = FastMap.newMap();
 * ranking.putAll("John Doe", 234, "Jane Dee", 123, "Sam Anta", null); 
 * ranking.values().removeIf(v -> v == null); // Remove all entries with null values.
 * ranking.values().parallel().removeIf(v -> v == null); // Same but performed in parallel.
 * }</pre></p>
 * 
 * <p> Finally, it should be noted that FastMap allows for {@code null} keys and values (unlike 
 *     {@code ConcurrentHashMap}); to differentiate between no entry and a {@code null} value,
 *     the method {@link #getEntry} can be used in place of {@link #get}. 
 *     The method {@link #getEntry} can also be used to perform direct modifications of the map values.
 * <pre>{@code
 * FastMap<String, Index> wordCount = ...;
 * Entry<String, Index> count = wordCount.getEntry(word);
 * if (count != null) count.setValue(count.getValue().next());
 * else wordCount.put(word, Index.ONE);     
 * }</pre></p>
 *  
 * @param <K> the type of keys maintained by this map (can be {@code null})
 * @param <V> the type of mapped values (can be {@code null})
 *             
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle </a>
 * @version 7.0, September 13, 2015
 */
@Realtime
@DefaultTextFormat(FastMap.Text.class)
public abstract class FastMap<K, V> implements ConcurrentMap<K, V>, NavigableMap<K, V>, 
        Cloneable, Serializable {

	private static final long serialVersionUID = 0x700L; // Version.
        
    /**
     * Default constructor.
     */
    protected FastMap() {
     }

    /**
     * Returns a new high-performance map sorted arbitrarily (hash order).
     */
    public static <K,V> FastMap<K,V> newMap() {
    	return new SparseMap<K,V>();
    }

    /**
     * Returns a new high-performance map sorted according to the specified key order.
     */
    public static <K,V> FastMap<K,V> newMap(Order<? super K> keyOrder) {
    	return new SparseMap<K,V>(keyOrder);
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Views.
    //
    
	/**
	 * Returns an atomic view over this map. All operations that write or access multiple elements in the map 
	 * are atomic. All read operations are mutex-free.
	 */
	public FastMap<K,V> atomic() {
		return new AtomicMapImpl<K,V>(this);
	}

    /**
     * Returns a view exposing only the entries matching the specified key filter. Mapping keys not 
     * matching the specified filter has no effect. If this map is initially empty, using a filtered view 
     * ensures that this map has only entries satisfying the specified filter predicate.
     */
    public FastMap<K,V> filter(Predicate<? super K> keyFilter) {
        return new FilteredMapImpl<K,V>(this, keyFilter); 
    }

    /**
	 * Returns a thread-safe view over this map. The shared view allows for concurrent read as long as 
	 * there is no writer. The default implementation is based on <a href=
	 * "http://en.wikipedia.org/wiki/Readers%E2%80%93writer_lock"> readers-writers locks</a> giving priority to writers. 
	 */
	public FastMap<K,V> shared() {
		return new SharedMapImpl<K,V>(this); 
	}

	/**
	 * Returns a reverse order view of this map.
	 */
	public FastMap<K,V> reversed() {
		return new ReversedMapImpl<K,V>(this);
	}

	/**
	 * Returns a view keeping track of the insertion order and exposing entries/keys/values in that order 
	 * (first added, first to iterate). This view can be useful for compatibility with Java linked collections
	 * (e.g. {@code LinkedHashMap}). Elements not added through this view are ignored when iterating.
	 */
    public FastMap<K,V> linked() {
		return new LinkedMapImpl<K,V>(this); 
	}

	/**
	 * Returns an unmodifiable view over this map. Any attempt to modify the map through this view will result into a
	 * {@link java.lang.UnsupportedOperationException} being raised.
	 */
	public FastMap<K,V> unmodifiable() {
		return new UnmodifiableMapImpl<K,V>(this);
	}
	
    /**
     * Returns a map using the specified equality comparator for its values.
     */
    public FastMap<K,V> valuesEquality(Equality<? super V> valuesEquality) {
        return new CustomValuesEqualityMapImpl<K,V>(this, valuesEquality);
    }
     /**
     * Returns a set view of the keys contained in this map. The set is backed by the map, so changes to the map are
     * reflected in the set, and vice-versa.  The set does not support adding new keys.
     * Keys equality comparisons (e.g. for removal) are performed using this map key {@link #comparator()}.
     */
    @Override
    public FastSet<K> keySet() {
        return new KeySetImpl<K,V>(this);
    }

	/** 
     * Equivalent to {@link #keySet()}.
     * @deprecated {@link #keySet()} should be used.
     */
	@Override
	public final FastSet<K> navigableKeySet() {
		return keySet();
	}

    /**
     * Returns a collection view of the values contained in this map.The collection is backed by the map, 
     * so changes to the map are reflected in the collection, and vice-versa. The collection
     * supports removing values (hence entries) but not adding new values.
     * Values equality comparisons (e.g. for removal) are performed using this map {@link #valuesEquality()}.
     */
    @Override
    public FastCollection<V> values() {
        return new ValuesImpl<K,V>(this);
    }

    /**
     * Returns a set view of the entries in this map. The set is backed by this map, so changes to this map are
     * reflected in the set, and vice-versa. The set supports element removal, which removes the corresponding 
     * mapping from the map. It does not support adding new entries.
     * Entries equality comparisons (e.g. for removal) are performed using this map key {@link #comparator()} 
     * and {@link #valuesEquality()}.  
     */
    @Override
    public FastSet<Entry<K, V>> entrySet() {
        return new EntrySetImpl<K,V>(this);
    }

    /** 
     * Returns a view of the portion of this map whose keys range from fromKey, inclusive, to toKey, exclusive.
     */
    @Override
    public FastMap<K, V> subMap(K fromKey, K toKey) {
        return subMap(fromKey, true, toKey, false);
    }

	/** 
     * Returns a view of the portion of this map whose keys range from fromKey to toKey.
     */
	@Override
	public FastMap<K, V> subMap(K fromKey, boolean fromInclusive, K toKey, boolean toInclusive) {
		return new SubMapImpl<K,V>(this, fromKey, fromInclusive, toKey, toInclusive);
	}

	/** 
	 * Returns a view of the portion of this map whose keys are strictly less than toKey.
     */
    @Override
    public FastMap<K, V> headMap(K toKey) {
        return headMap(toKey, false);
    }

	/** 
	 * Returns a view of the portion of this map whose keys are less than (or equal to, if inclusive is true) toKey.
     */
	@Override
	public FastMap<K, V> headMap(K toKey, boolean inclusive) {
        return new SubMapImpl<K,V>(this, null, null, toKey, inclusive);
	}

    /** 
     * Returns a view of the portion of this map whose keys are greater than or equal to fromKey.
     */
    @Override
    public FastMap<K, V> tailMap(K fromKey) {
        return tailMap(fromKey, true);
    }

	/** 
     * Returns a view of the portion of this map whose keys are greater than (or equal to, if inclusive is true) 
     * fromKey.
     */
	@Override
	public FastMap<K, V> tailMap(K fromKey, boolean inclusive) {
	     return new SubMapImpl<K,V>(this, fromKey, inclusive, null, null);
	}

	/** 
     * Equivalent to {@link #reversed reversed().keySet()}.
     * @deprecated {@link #reversed reversed().keySet()} should be used.
     */
	@Override
	public FastSet<K> descendingKeySet() {
		return keySet().reversed();
	}

	/** 
     * Equivalent to {@link #reversed()}.
     * @deprecated {@link #reversed()} should be used.
     */
	@Override
	public final FastMap<K, V> descendingMap() {
		return reversed();
	}
	
    ////////////////////////////////////////////////////////////////////////////
    // Map Interface.
    //

    @Override
    @Realtime(limit = LINEAR, comment="May count the number of entries (e.g. filtered views)")
    public abstract int size();
    
    @Override
    @Realtime(limit = LINEAR, comment = "Could iterate the whole collection (e.g. filtered views).")
    public abstract boolean isEmpty();

    @SuppressWarnings("unchecked")
	@Override
    public boolean containsKey(Object key) {
    	return getEntry((K)key) != null;  // Cast has no effect here.
    }

    @Override
    @Realtime(limit = LINEAR)
    public boolean containsValue(Object value) {
    	return values().contains(value);
    }

    @Override
    public V get(Object key) {
    	@SuppressWarnings("unchecked")
		Entry<K,V> entry = getEntry((K)key); // Cast has no effect here.
    	return entry != null ? entry.getValue() : null;
    }

    @Override
    public V remove(Object key) {
    	@SuppressWarnings("unchecked")
		Entry<K,V> entry = removeEntry((K)key); // Cast has no effect here.
    	return entry != null ? entry.getValue() : null;
    }

    @Override
    public abstract V put(K key, V value);
        
    @Override
    @Realtime(limit = LINEAR)
    public void putAll(Map<? extends K, ? extends V> that) {
    	for (java.util.Map.Entry<? extends K, ? extends V> entry : that.entrySet()) 
			put(entry.getKey(), entry.getValue());
    }

    /** Adds the specified key-value pairs to this maps.*/ 
    @SuppressWarnings("unchecked")
    @Realtime(limit = LINEAR)
    public void putAll(K key, V value, Object... others) {
        put(key, value);
        for (int i=0; i < others.length; i+=2)
            put((K)others[i], (V)others[i+1]);
    }

    @Override
    @Realtime(limit = LINEAR, comment="Views may have to remove entries one at a time (e.g. filtered views)")
    public abstract void clear();

    ////////////////////////////////////////////////////////////////////////////
    // ConcurrentMap Interface.
    //

    @Override
    public V putIfAbsent(K key, V value) {
        Entry<K,V> entry = getEntry(key);
        return (entry == null) ? put(key, value) : entry.getValue();
    }

    @Override
	@SuppressWarnings("unchecked")
    public boolean remove(Object key, Object value) {
		Entry<K,V> entry = getEntry((K)key);
		if ((entry != null) && valuesEquality().areEqual(entry.getValue(), (V)value)) {
			remove(key);
			return true;
		}
	    return false;
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
    	Entry<K,V> entry = getEntry(key);
		if ((entry != null) && valuesEquality().areEqual(entry.getValue(), oldValue)) {
			entry.setValue(newValue);
			return true;
		}
		return false;
    }

    @Override
    public V replace(K key, V value) {
    	Entry<K,V> entry = getEntry(key);
    	if (entry != null)
			return entry.setValue(value);
	    return null;
    }

    
    ////////////////////////////////////////////////////////////////////////////
    // SortedMap/NavigableMap Interface.
    //

    @Override
	public Entry<K,V> firstEntry() {
        Iterator<Entry<K,V>> itr = iterator();
	    return itr.hasNext() ? itr.next() : null;
	}

    @Override
	public Entry<K,V> lastEntry() {
        Iterator<Entry<K,V>> itr = descendingIterator();
        return itr.hasNext() ? itr.next() : null;
	}
		
    @Override
	public Entry<K,V> higherEntry(K key) {
        Iterator<Entry<K,V>> itr = iterator(key);
        if (!itr.hasNext()) return null;
        Entry<K,V> ceiling = itr.next();
        if (!comparator().areEqual(key, ceiling.getKey())) return ceiling;
        return itr.hasNext() ? itr.next() : null;               
    }
	
    @Override
	public Entry<K,V> lowerEntry(K key) {
        Iterator<Entry<K,V>> itr = descendingIterator(key);
        if (!itr.hasNext()) return null;
        Entry<K,V> floor = itr.next();
        if (!comparator().areEqual(key, floor.getKey())) return floor;
        return itr.hasNext() ? itr.next() : null;               
    }

	@Override
	public Entry<K, V> ceilingEntry(K key) {
	    Iterator<Entry<K,V>> itr = iterator(key);
        return itr.hasNext() ? itr.next() : null;	    
	}

	@Override
	public K ceilingKey(K key) {
		Entry<K,V> ceilingEntry = ceilingEntry(key);
		return ceilingEntry != null ? ceilingEntry.getKey() : null;
	}

	@Override
	public K firstKey() {
		Entry<K,V> firstEntry = firstEntry();
		if (firstEntry == null) throw new NoSuchElementException();
		return firstEntry.getKey();
	}

	@Override
	public Entry<K, V> floorEntry(K key) {
	     Iterator<Entry<K,V>> itr = descendingIterator(key);
	        return itr.hasNext() ? itr.next() : null;
	}

	@Override
	public K floorKey(K key) {
		Entry<K,V> floorEntry = floorEntry(key);
		return floorEntry != null ? floorEntry.getKey() : null;
	}

	@Override
	public K higherKey(K key) {
		Entry<K,V> higherEntry = higherEntry(key);
		return higherEntry != null ? higherEntry.getKey() : null;
	}

	@Override
	public K lastKey() {
		Entry<K,V> lastEntry = lastEntry();
		if (lastEntry == null) throw new NoSuchElementException();
		return lastEntry.getKey();
	}

	@Override
	public K lowerKey(K key) {
		Entry<K,V> lowerEntry = lowerEntry(key);
		return lowerEntry != null ? lowerEntry.getKey() : null;
	}

	@Override
	public Entry<K, V> pollFirstEntry() {
		return isEmpty() ? null : removeEntry(firstKey());
	}

	@Override
	public Entry<K, V> pollLastEntry() {
		return isEmpty() ? null : removeEntry(lastKey());
	}

	////////////////////////////////////////////////////////////////////////////
    // Misc.
    //
	
    /** Returns an entry iterator over this map's entries. */
    public abstract ReadOnlyIterator<Entry<K,V>> iterator();
    
    /** Returns a descending entry iterator over this map's entries. */
    public abstract ReadOnlyIterator<Entry<K,V>> descendingIterator();
        
    /** Returns an entry iterator over this map's entries starting from the specified key. */
    public abstract ReadOnlyIterator<Entry<K,V>> iterator(K fromKey);
    
    /** Returns a descending entry iterator over this map's entries starting from the specified key. */
    public abstract ReadOnlyIterator<Entry<K,V>> descendingIterator(K fromKey);
    
    /** 
     * Returns the entry for the specified key or {@code null} if none.
     * Unlike the standard {@link #get} method, if this method returns {@code null}, it indicates 
     * that there is no mapping for the specified key.
     */
    public abstract Entry<K,V> getEntry(K key);

	/** 
     * Removes and returns the entry for the specified key or {@code null} if none.
     * Unlike the standard {@link #remove} method, if this method returns {@code null}, it indicates 
     * that there was no mapping for the specified key.
     */
    public abstract Entry<K,V> removeEntry(K key);

    /**
     * Returns the value equality of this map.
     */
	public abstract Equality<? super V> valuesEquality();

	/**
	 * Returns a copy of this map; updates of the copy should not impact the original.
	 */
	@Realtime(limit = LINEAR)
	public abstract FastMap<K,V> clone();
    
	/**
     * Compares the specified object with this map for equality. This method follows the 
     * {@link Map#equals(Object)} specification regardless of the fast map's keys/values equalities.
     */
	@Override
    @Realtime(limit = LINEAR)   
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof Map))
			return false;
		Map<?,?> that = (Map<?,?>) obj;
		return this.entrySet().equals(that.entrySet());
    }

	/**
     * Returns the hash code of this map. This method follows the {@link Map#hashCode} specification 
     * regardless of the map's keys/values equalities.
     */
    @Override
    @Realtime(limit = LINEAR)   
	public int hashCode() {
		return entrySet().hashCode();
    }

    @Override
	public abstract Order<? super K> comparator();

	/** 
     * Returns the string representation of this map using its {@link TextContext contextual format}.
     */
    @Override
    @Realtime(limit = LINEAR)
    public String toString() {
        return TextContext.getFormat(FastMap.class).format(this);
    }

    /**
     * Returns multiple read-only views over this map (to support {@link #parallel} processing). 
     * How this collection splits (or does not split) is collection dependent (for example {@link #distinct}
     * views do not split). There is no guarantee over the iterative order of the sub-views which may 
     * be different from this collection iterative order.
     * 
     * <p> Any attempt to modify this collection through its sub-views will result 
     *     in a {@link UnsupportedOperationException} being thrown.</p>
     * 
     * @param n the desired number of independent views.
     * @return the sub-views (array of length in range [1..n])
     * @throws IllegalArgumentException if {@code n <= 0} 
     */
    @SuppressWarnings("unchecked")
    public FastMap<K,V>[] trySplit(final int n) {
        // Split into filtered maps with filter based on the element index (hashed to ensure balanced distribution). 
        final Order<? super K> order = comparator();
        FastMap<K,V>[] split = new FastMap[n];        
        for (int i=0; i < n; i++) {
            final int m = i;
            split[i] = this.filter(new Predicate<K>() {

                @Override
                public boolean test(K param) {
                    int hash = MathLib.hash(order.indexOf(param));
                    return Math.abs(hash) % n == m;
                }});
        }
        return split;
    }
    
    /**
     * Default text format for fast maps (parsing not supported).
     */
    public static class Text extends TextFormat<FastMap<?, ?>> {

        @Override
        public FastMap<Object, Object> parse(CharSequence csq, Cursor cursor)
                throws IllegalArgumentException {
            throw new UnsupportedOperationException();
        }

        @Override
        public Appendable format(FastMap<?, ?> that, final Appendable dest)
                throws IOException {
        	dest.append('{');
        	boolean firstEntry = true;
            for (java.util.Map.Entry<?,?> entry : that.entrySet()) {
                if (!firstEntry) dest.append(',').append(' ');
            	TextContext.format(entry.getKey(), dest);
        	    dest.append('=');
        	    TextContext.format(entry.getValue(), dest);
        	    firstEntry = false;
           	}
        	return dest.append('}');
        }

    }

}
