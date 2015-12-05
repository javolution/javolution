/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util;

import static javolution.lang.Realtime.Limit.CONSTANT;
import static javolution.lang.Realtime.Limit.LINEAR;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentMap;

import javolution.lang.Parallelizable;
import javolution.lang.Realtime;
import javolution.text.Cursor;
import javolution.text.DefaultTextFormat;
import javolution.text.TextContext;
import javolution.text.TextFormat;
import javolution.util.function.Equality;
import javolution.util.function.Order;
import javolution.util.internal.map.EntryImpl;
import javolution.util.internal.map.EntrySetImpl;
import javolution.util.internal.map.KeySetImpl;
import javolution.util.internal.map.MapIteratorImpl;
import javolution.util.internal.map.SubMapImpl;
import javolution.util.internal.map.ValuesImpl;

/**
 * <p> A high-performance map ({@link SparseMap trie-based}) 
 *     with documented {@link Realtime real-time} behavior.</p>
 * 
 * <p> Iterations order over map keys, values or entries is typically determined 
 *     by the map {@link #order() order} except for {@link #newLinkedMap() 
 *     linked maps} for which the iterations order is based on the insertion 
 *     order.</p> 
 * 
 * <p> Instances of this class can advantageously replace {@code java.util.*} 
 *     maps in terms of adaptability, space or performance. 
 * <pre>{@code
 * FastMap<Foo, Bar> hashMap = FastMap.newMap(); // Hash order.
 * FastMap<Foo, Bar> linkedHashMap = FastMap.newLinkedMap(); // Insertion order.
 * FastMap<Foo, Bar> concurrentHashMap = FastMap.newSharedMap(); // Implements ConcurrentMap interface.
 * FastMap<String, Bar> treeMap = FastMap.newMap(Order.LEXICAL); 
 * FastMap<String, Bar> linkedTreeMap = FastMap.newLinkedMap(Order.LEXICAL); // Does not exist in standard Java! 
 * FastMap<String, Bar> concurrentSkipListMap = FastMap.newSharedMap(Order.LEXICAL);
 * FastMap<Foo, Bar> identityHashMap = FastMap.newMap(Order.IDENTITY_HASH);
 * ...
 * }</pre> </p> 
 * <p> FastMap supports various views.
 * <ul>
 *    <li>{@link #atomic} - Thread-safe view for which all reads are mutex-free and
 *                          map updates (e.g. {@link #putAll putAll}) are atomic.</li>
 *    <li>{@link #shared} - Thread-safe view based on <a href=
 *                          "http://en.wikipedia.org/wiki/Readers%E2%80%93writer_lock">readers-writer
 *                          locks</a>.</li>
 *    <li>{@link #subMap} - View over a range of entries (based on map's order).</li>
 *    <li>{@link #entrySet} - View over the map entries allowing entries to be added/removed.</li>
 *    <li>{@link #keySet} - View over the map keys allowing keys to be removed or added (entries with {@code null} values).</li>
 *    <li>{@link #values} - View over the map values (removal is supported but not adding new values).</li>
 *    <li>{@link #unmodifiable} - View which does not allow for modification.</li>
 * </ul></p>      
 * 
 *  <p> All views (entry, key, values) over a map are {@link FastCollection} 
 *      and allow for parallel processing.
 * <pre>{@code
 * FastMap<Person, String> names = FastMap.newMap();
 * ...
 * names.values().removeIf(v -> v == null); // Remove all entries with null values.
 * names.values().parallel().removeIf(v -> v == null); // Same but performed in parallel.
 * }</pre></p>
 * 
 * <p> In addition to concurrent map interface being implemented,  
 *     fast map supports direct entry access/addition/removal 
 *     ({@link #getEntry getEntry} / {@link #putEntry putEntry} / 
 *     {@link #removeEntry removeEntry}).
 * [code]
 * FastMap<CharSequence, Index> wordCount = FastMap.newMap(Order.LEXICAL);    
 * void incrementCount(CharSequence word) {
 *     Entry<CharSequence, Index> entry = wordCount.getEntry(word); // Fast !
 *     if (entry != null) {
 *         entry.setValue(entry.getValue().next()); // Immediate !
 *     } else {
 *         wordCount.put(word, Index.ONE); // New entry.
 *     }
 * }[/code]</p> 
 *             
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle </a>
 * @version 7.0, September 13, 2015
 */
@Realtime
@DefaultTextFormat(FastMap.Text.class)
public abstract class FastMap<K, V> implements ConcurrentMap<K, V>, SortedMap<K, V>, 
        Iterable<Map.Entry<K,V>>, Cloneable, Serializable {

    private static final long serialVersionUID = 0x700L; // Version.
    
    /**
     * Returns a new empty hash map (based on sparse arrays).
     * 
     * @return {@code new SparseMap<K,V>()}
     */
    public static <K,V> FastMap<K,V> newMap() {
    	return new SparseMap<K,V>();
    }
 
    /**
     * Returns a new empty map (based on sparse arrays) using 
     * the specified key order.
     * 
     * @return {@code new SparseMap<K,V>(keyOrder)}
     */
    public static <K,V> FastMap<K,V> newMap(Order<? super K> keyOrder) {
    	return new SparseMap<K,V>(keyOrder);    	
    }
    
    /**
     * Returns a new empty map (based on sparse arrays) using the specified 
     * key order and value equality.
     * 
     * @return {@code new SparseMap<K,V>(keyOrder).valuesUsing(valueEquality)}
     */
    public static <K,V> FastMap<K,V> newMap(Order<? super K> keyOrder, 
    		Equality<? super V> valueEquality) {
    	return new SparseMap<K,V>(keyOrder).valuesUsing(valueEquality);
    }
    
    /**
     * Returns a new empty shared hash map (based on sparse arrays).
     * 
     * @return {@code new SparseMap<K,V>().shared()}
     */
    public static <K,V> FastMap<K,V> newSharedMap() {
    	return new SparseMap<K,V>().shared();
    }
    
    /**
     * Returns a new empty shared map (based on sparse arrays) using the 
     * specified key order.
     * 
     * @return {@code new SparseMap<K,V>(keyOrder).shared()}
     */
    public static <K,V> FastMap<K,V> newSharedMap(Order<? super K> keyOrder) {
    	return new SparseMap<K,V>(keyOrder).shared();
    }
    
    /**
     * Returns a new empty shared map (based on sparse arrays) using 
     * the specified key order and value equality.
     * 
     * @return {@code new SparseMap<K,V>(keyOrder).valuesUsing(valueEquality).shared()}
     */
    public static <K,V> FastMap<K,V> newSharedMap(Order<? super K> keyOrder, 
    		Order<? super V> valueEquality) {
    	return new SparseMap<K,V>(keyOrder).valuesUsing(valueEquality).shared();
    }
    
    /**
     * Returns a new empty atomic hash map (based on sparse arrays).
     * 
     * @return {@code new SparseMap<K,V>().atomic()}
     */
    public static <K,V> FastMap<K,V> newAtomicMap() {
    	return new SparseMap<K,V>().atomic();
    }
 
    /**
     * Returns a new empty atomic map (based on sparse arrays) using the 
     * specified key order.
     * 
     * @return {@code new SparseMap<K,V>(keyOrder).atomic()}
     */
    public static <K,V> FastMap<K,V> newAtomicMap(Order<? super K> keyOrder) {
    	return new SparseMap<K,V>(keyOrder).atomic();
    }
    
    /**
     * Returns a new empty atomic map (based on sparse arrays) using 
     * the specified key order and value equality.
     * 
     * @return {@code new SparseMap<K,V>(keyOrder).valuesUsing(valueEquality).atomic()}
     */
    public static <K,V> FastMap<K,V> newAtomicMap(Order<? super K> keyOrder, 
    		Order<? super V> valueEquality) {
    	return new SparseMap<K,V>(keyOrder).valuesUsing(valueEquality).atomic();
    }
    
    /**
     * Returns a new empty linked hash map (based on sparse arrays).
     * Iterations order is based on insertion order.
     * 
     * @return {@code new LinkedMap<K,V>()}
     */
    public static <K,V> FastMap<K,V> newLinkedMap() {
    	return null;
    }
 
    /**
     * Returns a new empty linked map (based on sparse arrays) using the 
     * specified key order.
     * Iterations order is based on insertion order.
     * 
     * @return {@code new LinkedMap<K,V>(keyOrder)}
     */
    public static <K,V> FastMap<K,V> newLinkedMap(Order<? super K> keyOrder) {
    	return null;
    }
    
    /**
     * Returns a new empty linked map (based on sparse arrays) using 
     * the specified key order and value equality.
     * Iterations order is based on insertion order.
     * 
     * @return {@code new LinkedMap<K,V>(keyOrder).valuesUsing(valueEquality)}
     */
    public static <K,V> FastMap<K,V> newLinkedMap(Order<? super K> keyOrder, 
    		Order<? super V> valueEquality) {
    	return null;
    }   
    
    /**
     * Default constructor.
     */
    protected FastMap() {
    }

    ////////////////////////////////////////////////////////////////////////////
    // Views.
    //

	/**
	 * Returns an atomic view over this map. All operations that write or
	 * access multiple elements in the map are atomic. 
	 * All read operations are mutex-free.
	 */
	@Parallelizable(mutexFree = true, comment = "Except for write operations, all read operations are mutex-free.")
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
	@Parallelizable(mutexFree = false, comment = "Use multiple-readers/single-writer lock.")
	public FastMap<K,V> shared() {
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
     *      
     * @return the view over this map using the specified equality for values.
     */
    public FastMap<K,V> valuesUsing(Equality<? super V> equality) {
        return null;
    }
    
     /**
     * Returns a set view of the keys contained in this map.
     * The set is backed by the map, so changes to the map are
     * reflected in the set, and vice-versa.  The set supports 
     * adding new keys for which the corresponding entry value 
     * is always {@code null}.
     * 
     * @return the key set of this map
     */
    public FastSet<K> keySet() {
        return new KeySetImpl<K,V>(this);
    }

    /**
     * Returns a collection view of the values contained in this map.
     * The collection is backed by the map, so changes to the map are
     * reflected in the collection, and vice-versa. The collection
     * supports removing values (hence entries) but not adding new values.
     * 
     * @return the collection of values in this map. 
     */
    public FastCollection<V> values() {
        return new ValuesImpl<K,V>(this);
    }

    /**
     * Returns a set view of the mappings contained in 
     * this map. The set is backed by the map, so changes to the map are
     * reflected in the set, and vice-versa. The set support 
     * adding/removing entries. 
     *  
     * @return a set of entries contained in the map
     */
    public FastSet<Entry<K, V>> entrySet() {
    	return new EntrySetImpl<K,V>(this);
    }

    /** 
     * Returns a view of the portion of this map whose keys range from 
     * fromKey, inclusive, to toKey, exclusive.
     * 
     * @param fromKey the first key inclusive or {@code null} for head maps.
     * @param toKey the last key exclusive or {@code null} for tail maps.
     */
    @Override
    public FastMap<K, V> subMap(K fromKey, K toKey) {
        return new SubMapImpl<K,V>(this, fromKey, toKey);
    }

    /** Returns a view of the portion of this map whose keys are strictly
     *  less than toKey. */
    @Override
    public FastMap<K, V> headMap(K toKey) {
        return subMap(null, toKey);
    }

    /** Returns a view of the portion of this map whose keys are greater 
     *  than or equal to fromKey. */
    @Override
    public FastMap<K, V> tailMap(K fromKey) {
        return subMap(fromKey, null);
    }

    /**
     * Returns the key order of this map.
     */
	public abstract Order<? super K> keyOrder();

    /**
     * Returns the value equality of this map.
     */
	public abstract Equality<? super V> valueEquality();

	/**
	 * Returns a copy of this map; updates of the copy should not impact
	 * the original.
	 */
	@Realtime(limit = LINEAR)
	public abstract FastMap<K,V> clone();

    ////////////////////////////////////////////////////////////////////////////
    // Map Interface.
    //

    /** Returns the number of entries/keys/values in this map. */
    @Override
    @Realtime(limit = CONSTANT)
    public abstract int size();

    /** Indicates if this map is empty */
    @Override
    @Realtime(limit = CONSTANT)
    public boolean isEmpty() {
        return size() == 0;
    }

    /** 
     * Indicates if this map contains the specified key according to
     * this map {@link #keyOrder() key equality}.
     */
    @SuppressWarnings("unchecked")
	@Override
    @Realtime(limit = CONSTANT)
    public boolean containsKey(Object key) {
    	return getEntry((K)key) != null;  // Cast has no effect.
    }

    /** 
     * Indicates if this map contains the specified value according to
     * this map {@link #valueEquality() value equality}.
     */
    @Override
    @Realtime(limit = LINEAR)
    public boolean containsValue(Object value) {
    	@SuppressWarnings("unchecked")
		Equality<Object> valueEquality = (Equality<Object>) valueEquality();
		for (Entry<K,V> entry : this) 
			if (valueEquality.areEqual(entry.getValue(), value))
				return true;
        return false;
    }

    /** Returns the value for the specified key. */
    @Override
    @Realtime(limit = CONSTANT)
    public V get(Object key) {
    	@SuppressWarnings("unchecked")
		Entry<K,V> entry = getEntry((K)key); // Cast has no effect.
    	return entry != null ? entry.getValue() : null;
    }

    /** 
     * Removes the entry for the specified key.
     * @return the previous value associated to that key.
     */
    @Override
    @Realtime(limit = CONSTANT)
    public V remove(Object key) {
    	@SuppressWarnings("unchecked")
		Entry<K,V> previous = removeEntry((K)key);
    	return previous != null ? previous.getValue() : null;
    }

    /** 
     * Returns the entry for the specified key or {@code null} if none.
     * This method may return specific entry types (e.g. entry with additional
     * fields).
     */
    @Realtime(limit = CONSTANT)
    public abstract Entry<K,V> getEntry(K key);

    /** 
     * Adds the specified entry to this map and returns the previous entry
     * with the same key if any.
     */
    @Realtime(limit = CONSTANT)
    public abstract Entry<K,V> putEntry(Entry<K,V> entry);

    /** 
     * Removes and returns the entry for the specified key.
     */
    @Realtime(limit = CONSTANT)
    public abstract Entry<K,V> removeEntry(K key);

    /** 
     * Associates the specified value with the specified key.
     * 
     * @return the previous value associated to that key.
     */
    @Override
    @Realtime(limit = CONSTANT)
    public V put(K key, V value) {
    	return putEntry(new EntryImpl<K,V>(key,value)).getValue();
    }

    /** Adds the specified map entries to this map. */
    @Override
    @Realtime(limit = LINEAR)
    public void putAll(Map<? extends K, ? extends V> that) {
    	for (Entry<? extends K, ? extends V> entry : that.entrySet()) 
			put(entry.getKey(), entry.getValue());
    }

    /** Removes all this map's entries. */
    @Override
    @Realtime(limit = CONSTANT)
    public abstract void clear();

    ////////////////////////////////////////////////////////////////////////////
    // ConcurrentMap Interface.
    //

    /** Associates the specified value with the specified key only if the 
     * specified key has no current mapping. */
    @Override
    @Realtime(limit = CONSTANT)
    public V putIfAbsent(K key, V value) {
		Entry<K,V> entry = getEntry(key);
		return (entry == null) ? put(key, value) : entry.getValue();
    }

    /** 
     * Removes the entry for a key only if currently mapped to a given value.
     */
    @Override
    @Realtime(limit = CONSTANT)
	@SuppressWarnings("unchecked")
    public boolean remove(Object key, Object value) {
		Equality<Object> valueEquality = (Equality<Object>) valueEquality();
		Entry<K,V> entry = getEntry((K)key);
		if ((entry != null) && valueEquality.areEqual(entry.getValue(), value)) {
			remove(key);
			return true;
		}
	    return false;
    }

    /** 
     * Replaces the entry for a key only if currently mapped to a given value.
     * 
     * @see #valueEquality
     */
    @Override
    @Realtime(limit = CONSTANT)
    public boolean replace(K key, V oldValue, V newValue) {
    	@SuppressWarnings("unchecked")
		Equality<Object> valueEquality = (Equality<Object>) valueEquality();
    	Entry<K,V> entry = getEntry(key);
		if ((entry != null) && valueEquality.areEqual(entry.getValue(), oldValue)) {
			entry.setValue(newValue);
			return true;
		}
		return false;
    }

    /** 
     * Replaces the entry for a key only if currently mapped to some value.
     */
    @Override
    @Realtime(limit = CONSTANT)
    public V replace(K key, V value) {
    	Entry<K,V> entry = getEntry(key);
    	if (entry != null)
			return entry.setValue(value);
	    return null;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // SortedMap Interface.
    //

    /** 
     * Returns the first key of this map or {@link null} if the map is empty.
     */
	@Override
	@Realtime(limit = CONSTANT)
	public final K firstKey() {
		Entry<K,V> firstEntry = firstEntry();;
		return (firstEntry != null) ? firstEntry.getKey() : null;
	}

	/** 
     * Returns the last key of this map or {@link null} if the map is empty.
     */
	@Override
	@Realtime(limit = CONSTANT)
	public final K lastKey() {
		Entry<K,V> lastEntry = lastEntry();;
		return (lastEntry != null) ? lastEntry.getKey() : null;
	}

    ////////////////////////////////////////////////////////////////////////////
    // Misc.
    //

    /** 
     * Returns the first entry of this map or {@link null} if the map is empty.
     */
	@Realtime(limit = CONSTANT)
	public abstract Entry<K,V> firstEntry();

	/** 
     * Returns the last entry of this map or {@link null} if the map is empty.
     */
	@Realtime(limit = CONSTANT)
	public abstract Entry<K,V> lastEntry();

    /**
     * Returns the entry after the specified key or {@code null} if none. 
     */
	@Realtime(limit = CONSTANT)
	public abstract Entry<K,V> entryAfter(K key);
    
    /**
     * Returns the entry before the specified key or {@code null} if none. 
     */
	@Realtime(limit = CONSTANT)
	public abstract Entry<K,V> entryBefore(K key);
    
	/** 
     * Returns the entry at mid-point between the specified key or 
     * {@link null} if none.
     */
	@Realtime(limit = CONSTANT)
	public abstract Entry<K,V> midEntry(K fromKey, K toKey);

    /**
     * Compares the specified object with this map for equality.
     * This method follows the {@link Map#equals(Object)} specification 
     * regardless of the fast map's key/value equalities.
     *      
     * @param obj the object to be compared for equality with this map
     * @return <code>true</code> if this map is considered equals to the
     *         one specified; <code>false</code> otherwise. 
     * @see #keyOrder()
     * @see #valueEquality()
     */
	@Override
    @Realtime(limit = LINEAR)   
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof Map))
			return false;
		Map<?, ?> that = (Map<?, ?>) obj;
		return this.entrySet().equals(that.entrySet());
    }

    @Override
	public int hashCode() {
		return entrySet().hashCode();
    }

    /**
     * Returns the{@link #keyOrder() key order}. 
     */
    @Override
	public final Order<? super K> comparator() {
    	return keyOrder();
    }

    /**
     * Returns the iterator over this map's entries.
     */
	public FastIterator<Entry<K,V>> iterator() {
		return iterator(null, null);
	}

    /** Returns the iterator over this map's entries starting from 
     *  the specified key (inclusive).     */
	public FastIterator<Entry<K,V>> iterator(K fromKey) {
		return iterator(fromKey, null);
	}

    /**
     * Returns the iterator over this map's entries between the from 
     * key (inclusive) and the toKey (exclusive) (or {@code null} if
     * no upper bound). 
     */
	public FastIterator<Entry<K,V>> iterator(K fromKey, K toKey) {
		return new MapIteratorImpl<K,V>(this, fromKey, toKey);
	}

	/** 
     * Returns the string representation of this map using its 
     * {@link TextContext contextual format}.
     * @return String representation of the FastMap
     */
    @Override
    @Realtime(limit = LINEAR)
    public String toString() {
        return TextContext.getFormat(FastMap.class).format(this);
    }

    /**
     * Default text format for fast maps (parsing not supported).
     */
    @Parallelizable
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
            for (Entry<?,?> entry : that) {
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
