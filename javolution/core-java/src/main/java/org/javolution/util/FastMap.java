/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.util;

import static org.javolution.lang.Realtime.Limit.CONSTANT;
import static org.javolution.lang.Realtime.Limit.LINEAR;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentMap;

import org.javolution.lang.Realtime;
import org.javolution.text.Cursor;
import org.javolution.text.DefaultTextFormat;
import org.javolution.text.TextContext;
import org.javolution.text.TextFormat;
import org.javolution.util.function.Equality;
import org.javolution.util.function.Order;
import org.javolution.util.function.Predicate;
import org.javolution.util.internal.map.EntrySetImpl;
import org.javolution.util.internal.map.KeySetImpl;
import org.javolution.util.internal.map.SubMapImpl;
import org.javolution.util.internal.map.ValuesImpl;

/**
 * <p> A high-performance ordered map (trie-based) with 
 *     {@link Realtime strict timing constraints}.</p>
 *     
 * <p> In general, fast map methods have a limiting behavior in 
 *     {@link Realtime#Limit#CONSTANT O(1)} (constant) to be compared 
 *     with {@link Realtime#Limit#LOG_N O(log n)} for most sorted maps.</p>
 * 
 * <p> Iterations order over map keys, values or entries is determined 
 *     by the map {@link #comparator() order} except for specific views 
 *     such as the {@link #linked linked} view for which the iterations 
 *     order is the insertion order.</p>
 *     
 * <p> Instances of this class can advantageously replace any 
 *     {@code java.util.*} map in terms of adaptability, space or performance. 
 * <pre>{@code
 * FastMap<Foo, Bar> hashMap = FastMap.newMap(); // Hash order (default).
 * FastMap<Foo, Bar> identityHashMap = FastMap.newMap(Order.IDENTITY);
 * FastMap<String, Bar> treeMap = FastMap.newMap(Order.LEXICAL); 
 * FastMap<Foo, Bar> linkedHashMap = new SparseMap<Foo, Bar>().linked(); // Insertion order.
 * FastMap<Foo, Bar> concurrentHashMap = new SparseMap<Foo, Bar>().shared(); // Implements ConcurrentMap interface.
 * FastMap<String, Bar> concurrentSkipListMap = new SparseMap<String, Bar>(Order.LEXICAL).shared();
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
 *                          "http://en.wikipedia.org/wiki/Readers%E2%80%93writer_lock">readers-writer
 *                          locks</a>.</li>
 *    <li>{@link #atomic} - Thread-safe view for which all reads are mutex-free and
 *                          map updates (e.g. {@link #putAll putAll}) are atomic.</li>
 *    <li>{@link #reversed} - Reversed order view.</li>
 *    <li>{@link #linked} - View exposing each entry based on the {@link #put 
 *                          insertion} order in the view.</li>
 *    <li>{@link #unmodifiable} - View which does not allow for modification.</li>
 *    <li>{@link #valuesEquality} - View using the specified equality for its values.</li>
 * </ul></p>      
 * 
 *  <p> The entry/key/value views over a map are instances 
 *      of {@link FastCollection} which supports parallel processing.
 * <pre>{@code
 * FastMap<String, Value> names = new SparseMap<String, Value>
 *     .putAll("Oscar Thon", v0, "Yvon Tremblay", v1);
 * ...
 * names.values().removeIf(v -> v == null); // Remove all entries with null values.
 * names.values().parallel().removeIf(v -> v == null); // Same but performed in parallel.
 * }</pre></p>
 * 
 * <p> Finally, it should be noted that FastMap entries are immutable, 
 *     any attempt to set the value of an entry directly will raise 
 *     an {@link UnsupportedOperationException}. </p>
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
     * Returns a new high-performance map ordered according to the specified
     * key order.
     */
    public static <K,V> FastMap<K,V> newMap(Order<? super K> keyOrder) {
    	return new SparseMap<K,V>(keyOrder);
    }

    /**
     * Returns a new high-performance map ordered according to the specified
     * key order and using the specified value equality.
     */
    public static <K,V> FastMap<K,V> newMap(Order<? super K> keyOrder,
    		Equality<? super V> valuesEquality) {
    	return new SparseMap<K,V>(keyOrder).valuesEquality(valuesEquality);
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Views.
    //

	/**
	 * Returns an atomic view over this map. All operations that write or
	 * access multiple elements in the map are atomic. 
	 * All read operations are mutex-free.
	 */
	public FastMap<K,V> atomic() {
		return null;
	}

	/**
	 * Returns a thread-safe view over this map. The shared view allows
	 * for concurrent read as long as there is no writer. The default
	 * implementation is based on <a href=
	 * "http://en.wikipedia.org/wiki/Readers%E2%80%93writer_lock">
	 * readers-writers locks</a> giving priority to writers. 
	 */
	public FastMap<K,V> shared() {
		return null;
	}

	/**
	 * Returns a reverse order view of this map.
	 */
	public FastMap<K,V> reversed() {
		return null;
	}

	/**
	 * Returns a view exposing only the entries matching the specified filter.
	 * Adding new entries not matching the specified filter has no effect. If this
	 * map is initially empty, using a filtered view to put new entries
	 * ensures that this map has only entries satisfying the filter
	 * predicate.
	 */
	public FastMap<K,V> filter(Predicate<? super Entry<K, V>> filter) {
		return null;
	}

	/**
	 * Returns a view keeping track of the insertion order and exposing 
	 * entries/keys/values in that order (first added, first to iterate).
	 * This view can be useful for compatibility with Java linked collections
	 * (e.g. {@code LinkedHashMap}). Elements not added through this 
	 * view are ignored when iterating.
	 */
	public FastMap<K,V> linked() {
		return null;
	}

	/**
	 * Returns an unmodifiable view over this map. Any attempt to modify
	 * the map through this view will result into a
	 * {@link java.lang.UnsupportedOperationException} being raised.
	 */
	public FastMap<K,V> unmodifiable() {
		return null;
	}
	
    /**
     * Returns a view over this map using the specified equality for 
     * values comparisons.
     */
    public FastMap<K,V> valuesEquality(Equality<? super V> equality) {
        return null;
    }
    
     /**
     * Returns a set view of the keys contained in this map.
     * The set is backed by the map, so changes to the map are
     * reflected in the set, and vice-versa.  The set supports 
     * adding new keys for which the corresponding entry value 
     * is always {@code null}.
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
     * Returns a collection view of the values contained in this map.
     * The collection is backed by the map, so changes to the map are
     * reflected in the collection, and vice-versa. The collection
     * supports removing values (hence entries) but not adding new values.
     */
    @Override
    public FastCollection<V> values() {
        return new ValuesImpl<K,V>(this);
    }

    /**
     * Returns a set view of the mappings contained in 
     * this map. The set is backed by the map, so changes to the map are
     * reflected in the set, and vice-versa. The set support 
     * adding/removing entries.
     */
    @Override
    public FastSet<Entry<K, V>> entrySet() {
    	return new EntrySetImpl<K,V>(this);
    }

    /** 
     * Returns a view of the portion of this map whose keys range from 
     * fromKey, inclusive, to toKey, exclusive.
     */
    @Override
    public FastMap<K, V> subMap(K fromKey, K toKey) {
        return subMap(fromKey, true, toKey, false);
    }

	/** 
     * Returns a view of the portion of this map whose keys range from 
     * fromKey to toKey.
     */
	@Override
	public FastMap<K, V> subMap(K fromKey, boolean fromInclusive, K toKey, boolean toInclusive) {
		return new SubMapImpl<K,V>(this, fromKey, fromInclusive, toKey, toInclusive);
	}

	/** 
	 * Returns a view of the portion of this map whose keys are strictly
     * less than toKey.
     */
    @Override
    public FastMap<K, V> headMap(K toKey) {
        return headMap(toKey, false);
    }

	/** 
	 * Returns a view of the portion of this map whose keys are less than 
	 * (or equal to, if inclusive is true) toKey.
     */
	@Override
	public FastMap<K, V> headMap(K toKey, boolean inclusive) {
        return subMap(firstKey(), true, toKey, inclusive);
	}

    /** 
     * Returns a view of the portion of this map whose keys are greater than
     * or equal to fromKey.
     */
    @Override
    public FastMap<K, V> tailMap(K fromKey) {
        return tailMap(fromKey, true);
    }

	/** 
     * Returns a view of the portion of this map whose keys are greater than
     * (or equal to, if inclusive is true) fromKey.
     */
	@Override
	public FastMap<K, V> tailMap(K fromKey, boolean inclusive) {
        return subMap(fromKey, inclusive, lastKey(), true);
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
    @Realtime(limit = LINEAR, comment="Views may count the number of entries (e.g. subMap)")
    public abstract int size();
    
    @Override
    @Realtime(limit = CONSTANT)
    public boolean isEmpty() {
        return firstEntry() != null;
    }

    /** 
     * Indicates if this map contains the specified key according to
     * this map {@link #comparator() comparator}.
     */
    @SuppressWarnings("unchecked")
	@Override
    @Realtime(limit = CONSTANT)
    public boolean containsKey(Object key) {
    	return getEntry((K)key) != null;  // Cast has no effect here.
    }

    /** 
     * Indicates if this map contains the specified value according to
     * this map {@link #valuesEquality() values equality}.
     */
    @Override
    @Realtime(limit = LINEAR)
    public boolean containsValue(Object value) {
    	return values().contains(value);
    }

    @Override
    @Realtime(limit = CONSTANT)
    public V get(Object key) {
    	@SuppressWarnings("unchecked")
		Entry<K,V> entry = getEntry((K)key); // Cast has no effect here.
    	return entry != null ? entry.getValue() : null;
    }

    /** 
     * Removes the entry for the specified key and returns
     * the previous value associated to that key.
     */
    @Override
    @Realtime(limit = CONSTANT)
    public V remove(Object key) {
    	@SuppressWarnings("unchecked")
		Entry<K,V> entry = removeEntry((K)key); // Cast has no effect here.
    	return entry != null ? entry.getValue() : null;
    }

    /** 
     * Associates the specified value with the specified key and returns 
     * the previous value associated to that key or {@code null} if none.
     */
    @Override
	@Realtime(limit = CONSTANT)
    public abstract V put(K key, V value);
        
    @Override
    @Realtime(limit = LINEAR)
    public void putAll(Map<? extends K, ? extends V> that) {
    	for (Entry<? extends K, ? extends V> entry : that.entrySet()) 
			put(entry.getKey(), entry.getValue());
    }

    @Override
    @Realtime(limit = LINEAR, comment="Views may remove entries one at a time (e.g. subMap)")
    public abstract void clear();

    ////////////////////////////////////////////////////////////////////////////
    // ConcurrentMap Interface.
    //

    @Override
    @Realtime(limit = CONSTANT)
    public V putIfAbsent(K key, V value) {
		Entry<K,V> entry = getEntry(key);
		return (entry == null) ? put(key, value) : entry.getValue();
    }

    @Override
    @Realtime(limit = CONSTANT)
	@SuppressWarnings("unchecked")
    public boolean remove(Object key, Object value) {
		Equality<Object> valueEquality = (Equality<Object>) valuesEquality();
		Entry<K,V> entry = getEntry((K)key);
		if ((entry != null) && valueEquality.areEqual(entry.getValue(), value)) {
			remove(key);
			return true;
		}
	    return false;
    }

    @Override
    @Realtime(limit = CONSTANT)
    public boolean replace(K key, V oldValue, V newValue) {
    	@SuppressWarnings("unchecked")
		Equality<Object> valueEquality = (Equality<Object>) valuesEquality();
    	Entry<K,V> entry = getEntry(key);
		if ((entry != null) && valueEquality.areEqual(entry.getValue(), oldValue)) {
			put(key, newValue);
			return true;
		}
		return false;
    }

    @Override
    @Realtime(limit = CONSTANT)
    public V replace(K key, V value) {
    	Entry<K,V> entry = getEntry(key);
    	if (entry != null)
			return put(key, value);
	    return null;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // SortedMap/NavigableMap Interface.
    //

	@Realtime(limit = CONSTANT)
	public abstract Entry<K,V> firstEntry();

	@Realtime(limit = CONSTANT)
	public abstract Entry<K,V> lastEntry();
		
	@Realtime(limit = CONSTANT)
	public abstract Entry<K,V> higherEntry(K key);
	
	@Realtime(limit = CONSTANT)
	public abstract Entry<K,V> lowerEntry(K key);

	@Override
	@Realtime(limit = CONSTANT)
	public abstract Entry<K, V> ceilingEntry(K key);

	@Override
	@Realtime(limit = CONSTANT)
	public K ceilingKey(K key) {
		Entry<K,V> ceilingEntry = ceilingEntry(key);
		return ceilingEntry != null ? ceilingEntry.getKey() : null;
	}

	@Override
	@Realtime(limit = CONSTANT)
	public K firstKey() {
		Entry<K,V> firstEntry = firstEntry();
		if (firstEntry == null) throw new NoSuchElementException();
		return firstEntry.getKey();
	}

	@Override
	@Realtime(limit = CONSTANT)
	public abstract Entry<K, V> floorEntry(K key);

	@Override
	@Realtime(limit = CONSTANT)
	public K floorKey(K key) {
		Entry<K,V> floorEntry = floorEntry(key);
		return floorEntry != null ? floorEntry.getKey() : null;
	}

	@Override
	@Realtime(limit = CONSTANT)
	public K higherKey(K key) {
		Entry<K,V> higherEntry = higherEntry(key);
		return higherEntry != null ? higherEntry.getKey() : null;
	}

	@Override
	@Realtime(limit = CONSTANT)
	public K lastKey() {
		Entry<K,V> lastEntry = lastEntry();
		if (lastEntry == null) throw new NoSuchElementException();
		return lastEntry.getKey();
	}

	@Override
	@Realtime(limit = CONSTANT)
	public K lowerKey(K key) {
		Entry<K,V> lowerEntry = lowerEntry(key);
		return lowerEntry != null ? lowerEntry.getKey() : null;
	}

	@Override
	@Realtime(limit = CONSTANT)
	public Entry<K, V> pollFirstEntry() {
		return isEmpty() ? null : removeEntry(firstKey());
	}

	@Override
	@Realtime(limit = CONSTANT)
	public Entry<K, V> pollLastEntry() {
		return isEmpty() ? null : removeEntry(lastKey());
	}

	////////////////////////////////////////////////////////////////////////////
    // Misc.
    //
	
	/**
	 * Returns an immutable map holding the same key/value pairs, the same
	 * comparator and the same value equality as this map.
	 */
	@Realtime(limit = LINEAR)
	public ConstantMap<K,V> constant() {
		SparseMap<K,V> sparse = new SparseMap<K,V>(comparator());
		sparse.putAll(this);
		return new ConstantMap<K,V>(sparse, valuesEquality());
	}
	
    /** 
     * Maps the specified (key, value) pairs and returns this map
     * (convenience method).
     */
	@SuppressWarnings("unchecked")
	@Realtime(limit = LINEAR)
    public FastMap<K,V> putAll(K key, V value, Object... others) {
		put(key, value);
		for (int i=0; i < others.length;) 
			put((K)others[i++], (V)others[i++]);
		return this;
	}

    /** 
     * Returns the entry for the specified key or {@code null} if none.
     * Sub-classes may return specific entry types 
     * (e.g. {@link SparseArray SparseArray#SparseEntry SparseEntry} for 
     * sparse arrays).
     */
	@Realtime(limit = CONSTANT)
    public abstract Entry<K,V> getEntry(K key);

	/** 
     * Removes and returns the entry for the specified key or {@code null} 
     * if none. Sub-classes may return specific entry types 
     * (e.g. {@link SparseArray SparseArray#SparseEntry SparseEntry} for 
     * sparse arrays).
     */
	@Realtime(limit = CONSTANT)
    public abstract Entry<K,V> removeEntry(K key);

    /**
     * Returns the value equality of this map.
     */
	@Realtime(limit = CONSTANT)   
	public abstract Equality<? super V> valuesEquality();

	/**
	 * Returns a copy of this map; updates of the copy should not impact
	 * the original.
	 */
	@Realtime(limit = LINEAR)
	public abstract FastMap<K,V> clone();
    
	/**
     * Compares the specified object with this map for equality.
     * This method follows the {@link Map#equals(Object)} specification 
     * regardless of the fast map's keys/values equalities.
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
     * Returns the hash code of this map.
     * This method follows the {@link Map#hashCode} specification 
     * regardless of the map's keys/values equalities.
     */
    @Override
    @Realtime(limit = LINEAR)   
	public int hashCode() {
		return entrySet().hashCode();
    }

    @Override
    @Realtime(limit = CONSTANT)   
	public abstract Order<? super K> comparator();

	/** 
     * Returns the string representation of this map using its 
     * {@link TextContext contextual format}.
     */
    @Override
    @Realtime(limit = LINEAR)
    public String toString() {
        return TextContext.getFormat(FastMap.class).format(this);
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
            for (Entry<?,?> entry : that.entrySet()) {
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
