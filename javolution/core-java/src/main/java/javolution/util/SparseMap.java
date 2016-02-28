/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util;

import java.io.Serializable;
import java.util.Iterator;
import java.util.NoSuchElementException;

import javolution.lang.Index;
import javolution.util.function.Equality;
import javolution.util.function.Order;

/**
 * <p> A <a href="http://en.wikipedia.org/wiki/Trie">trie-based</a> map 
 *     allowing for quick searches, insertions and deletion.</p> 
 *     
 * <p> Worst-case execution time when adding new entries is 
 *     significantly better than when using standard hash table since there is
 *     no resize/rehash ever performed.</p> 
 *   
 * <p> Sparse maps are efficient for indexing multi-dimensional information 
 *     such as dictionaries, multi-keys attributes, geographical coordinates,
 *     sparse matrix elements, etc.
 * <pre>{@code
 * // Prefix Maps.
 * SparseMap<String, President> presidents = new SparseMap<>(Order.LEXICAL);
 * presidents.put("John Adams", johnAdams);
 * presidents.put("John Tyler", johnTyler);
 * presidents.put("John Kennedy", johnKennedy);
 * ...
 * presidents.subMap("J", "K").clear(); // Removes all president whose first name starts with "J" ! 
 * 
 * // Multi-Keys Attributes.
 * Indexer<Person> indexer = person -> (person.isMale() ? 0 : 128) + person.age();
 * FastMap<Person, Integer> population = new FastMap<>(indexer); // Population count by gender/age.
 * ...
 * int numberOfBoys = population.subMap(new Person(MALE, 0), new Person(MALE, 19)).values().sum();
 * 
 * // Spatial indexing (R-Tree).
 * class LatLong implements Binary<Latitude, Longitude> {...}
 * Indexer<LatLong> mortonCode = latLong -> MathLib.interleave(latLong.lat(DEGREE) + 90, latLong.long(DEGREE) + 180);
 * SparseMap<LatLong, City> cities = new SparseMap<>(mortonCode); // Keep space locality.
 * cities.put(parisLatLong, paris);
 * cities.put(londonLatLong, london);
 * ...
 * cities.subMap(EUROPE_LAT_LONG_MIN, EUROPE_LAT_LONG_MAX).values().filter(inEurope).forEach(...); // Iterates over European cities.
 * Comparator<City> distanceToParisComparator = ...; // city1 < city2 if city1 is closer to Paris than city2
 * City nearestParis = cities.subMap(aroundParisMin, aroundParisMax).values()
 *     .filter(notParis) // Avoid returning Paris! 
 *     .comparator(distanceToParisComparator)
 *     .min();
 *     
 * // Sparse Matrix.
 * class RowColumn extends Binary<Index, Index> { ... }
 * SparseMap<RowColumn, E> sparseMatrix = new SparseMap<>(Order.QUADTREE); 
 * sparseMatrix.put(RowColumn.of(2, 44), e);
 * ...
 * // Iterates only over the diagonal entries of the sparse matrix.
 * Consumer<Entry<RowColumn, E>> consumer = ...; 
 * forEachOnDiagonal(consumer, sparseMatrix, 0, 1024); // Assuming 1024 is the maximum row/column dimension (and a power of 2).
 * ...
 * static void forEachOnDiagonal(Consumer<Entry<RowColumn, E>> consumer, FastMap<RowColumn, E> sparseMatrix, int i, int length) {
 *     if (sparseMatrix.isEmpty()) return; 
 *     if (length == 1) {
 *          consumer.accept(sparseMatrix.getEntry(RowColumn.of(i,i)));  
 *     } else { // The quadtree order allows for perfect binary split!
 *          int half = length >> 1; 
 *          RowColumn start = RowColumn.of(i,i);
 *          RowColumn middle = RowColumn.of(i+half,i+half); 
 *          RowColumn end = RowColumn.of(i+length,i+length);
 *          forEachOnDiagonal(sparseMatrix.subMap(start, middle), i, half);
 *          forEachOnDiagonal(sparseMatrix.subMap(middle, end), i+half, length-half);
 *     }
 * }}</pre></p>
 * 
 * <p> In addition to concurrent map interface being implemented,  
 *     fast map supports direct entry access/removal ({@link #getEntry getEntry}
 *     / {@link #removeEntry removeEntry}).
 * [code]
 * FastMap<CharSequence, Index> wordCount = new FastMap<>(Order.LEXICAL);    
 * void incrementCount(CharSequence word) {
 *     Entry<CharSequence, Index> entry = wordCount.getEntry(word); // Fast !
 *     if (entry != null) {
 *         entry.setValue(entry.getValue().next()); // Immediate !
 *     } else {
 *         wordCount.put(word, Index.ONE); // New entry.
 *     }
 * }[/code]</p>
 * 
 * <p> The memory footprint of the map is automatically adjusted up or down
 *     based on the map size (minimal when the map is cleared).</p>
 *      
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 7.0, September 13, 2015
 */
public class SparseMap<K,V> extends FastMap<K,V> {
	
	private static final long serialVersionUID = 0x700L; // Version. 
	
	private final Order<? super K> order; // Null if no order.
	private final SparseArray<Object> nodes;
	private int size;
	
	/**
     * Creates an empty map using an arbitrary order (hash based).
     */
    public SparseMap() {
    	this(Order.HASH);
    }
    
	/**
     * Creates an empty map using the specified order.
     * 
     * @param order the ordering of the map.
     */
    public SparseMap(Order<? super K> order) {
    	this.order = order;
    	this.nodes = new SparseArray<Object>();
    }
    
	@Override
	public int size() {
		return size;
	}
	
	@Override
	public Order<? super K> keyOrder() {
		return order;
	}
	
	@Override
	public Equality<? super V> valueEquality() {
		return Equality.STANDARD;
	}
	
	/**
	 * Returns a new map having the exact same entries (not duplicated) as
	 * this map.
	 */
	@Override
	public SparseMap<K, V> clone() {
		SparseMap<K,V> copy = new SparseMap<K,V>(order);
		copy.entrySet().addAll(this.entrySet()); 
		return copy;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Entry<K, V> getEntry(K key) {
        Object node = nodes.get(order.indexOf(key));
        if (node == null) return null;
        if (node instanceof FastMap) 
        	return ((FastMap<K,V>)node).getEntry(key);
        Entry<K,V> entry = (Entry<K,V>)node;
        return order.areEqual(entry.getKey(), key) ? entry : null;
    }		

	@SuppressWarnings("unchecked")
	@Override
    public Entry<K,V> putEntry(Entry<K,V> entry) { // Lock only required for writes.
		int i = order.indexOf(entry.getKey());
        Object node = nodes.setIfAbsent(i, entry);
        if (node != null) {
             if (node instanceof FastMap) {
           	     Entry<K,V> previous = ((FastMap<K,V>)node).putEntry(entry);
        	     if (previous != null) return previous;
             } else { // Entry
                 Entry<K,V> previous = (Entry<K,V>)node;
                 if (order.areEqual(previous.getKey(), entry.getKey())) {
        	         nodes.set(i, entry);
        	         return previous;
                 } // Collision.
                 Order<? super K> subOrder = order.subOrder(entry.getKey());
                 FastMap<K,V> subMap = (subOrder != null) ? 
        		 new SparseMap<K,V>(subOrder) : new SortedMap<K,V>(order);
                 subMap.putEntry(previous);
                 subMap.putEntry(entry);
                 nodes.set(i, subMap);
             }
        }
        size++;
        return null;
	}
	
	@SuppressWarnings("unchecked")
	@Override
    public Entry<K,V> removeEntry(K key) {
		int i = order.indexOf(key);
        Object node = nodes.get(i);        
        if (node == null) return null;
        if (node instanceof FastMap) {
        	FastMap<K,V> map = (FastMap<K,V>)node;
        	Entry<K,V> previous = map.removeEntry(key);
        	if (previous == null) return null;
        	if (map.size() <= 1) nodes.set(i, map.entrySet().iterator().next());
        	size--;
        	return previous;
        }
        Entry<K,V> entry = (Entry<K,V>)node;
        if (!order.areEqual(entry.getKey(), key)) return null;
        nodes.set(i, null);
        size--;
        return entry;
	}
	
	@Override
	public void clear() {
		nodes.clear();
		size = 0;
	}

    
}