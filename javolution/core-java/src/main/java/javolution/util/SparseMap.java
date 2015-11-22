/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util;

import java.util.Iterator;

import javolution.util.function.Equality;
import javolution.util.function.Order;

/**
 *  <p> A high-performance map based on {@link SparseArray}.</p>
 * 
 * <p> The <a href="http://en.wikipedia.org/wiki/Trie">
 *     trie-based</a> implementation allows for quick searches, insertions and
 *     deletion. Worst-case execution time when adding new entries is 
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
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 7.0, September 13, 2015
 */
public class SparseMap<K,V> extends FastMap<K,V> {
	
	private static final long serialVersionUID = 0x700L; // Version. 
	static final int SHIFT = 4;
	static final int SIZE = 1 << SHIFT;
	static final int MASK = SIZE - 1;
		
	private final Order<? super K> order;
	private final Object[] nodes = new Object[SIZE]; 
	private final int shift;
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
    	this(order, Math.max(order.bitLength() - SHIFT, 0));
    	
    }
    
    private SparseMap(Order<? super K> order, int shift) {
    	this.order = order;
    	this.shift = shift;
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
	
	@Override
	public FastMap<K, V> clone() {
		// TODO: Optimize
		SparseMap<K,V> copy = new SparseMap<K,V>(order);
		copy.entrySet().addAll(this.entrySet());
		return copy;
	}
	
	@Override
	public EntryImpl<K, V> getEntry(K key) {
	    return getEntry(key, order.indexOf(key));
	}	
	
	@SuppressWarnings("unchecked")
    private EntryImpl<K, V> getEntry(K key, int index) {
    	Object node = nodes[(index >>> shift) & MASK];
		if (node == null) return null;
		if (node instanceof SparseMap) { // Recursion.
			SparseMap<K,V> subMap = (SparseMap<K,V>)node;
			return (subMap.order == order) ? 
					subMap.getEntry(key, index) : subMap.getEntry(key);
		}
		if (node instanceof EntryImpl) {
		     EntryImpl<K,V> entry = (EntryImpl<K,V>) node;
		     return (entry.index == index) && order.areEqual(key, entry.key)
		    		 ? entry : null;
		}
		// Hopefully we should not get there very often (no sub-order)
		FractalTable<EntryImpl<K,V>> colliding = (FractalTable<EntryImpl<K,V>>) node;
		for (EntryImpl<K,V> entry : colliding) 
			if (order.areEqual(key, entry.key)) return entry;
		return null;
	}	
	
	@Override
    public V put(K key, V value) {
		return put(key, value, order.indexOf(key));
	}
	
	@SuppressWarnings("unchecked")
    private V put(K key, V value, int index) {
      	Object node = nodes[(index >>> shift) & MASK];
    	if (node == null) {
    		nodes[(index >>> shift) & MASK] = new EntryImpl<K,V>(key, value, index);
    		size++;
    		return null;
    	} 
    	if (node instanceof SparseMap) { // Recursion.
    		SparseMap<K,V> subMap = (SparseMap<K,V>)node;
    		int subMapPreviousSize = subMap.size;
    		V previous = (subMap.order == order) ? 
    			subMap.put(key, value, index) : subMap.put(key, value);
    	    size += subMap.size - subMapPreviousSize; 
			return previous;
    	}
		if (node instanceof EntryImpl) {
			EntryImpl<K,V> entry = (EntryImpl<K,V>) node;
			if ((entry.index == index) && order.areEqual(key, entry.key)) {
				V previous = entry.value;
				entry.value = value;
				return previous;
			}
			size++;
			if (shift > 0) {
      			SparseMap<K,V> subMap = new SparseMap<K,V>(order, Math.max(shift - SHIFT, 0));
      			subMap.put(entry.key, entry.value, entry.index);
      			nodes[(index >>> shift) & MASK] = subMap;
      		    return subMap.put(key, value, index);
      	    }
			if (index != entry.index) throw new IllegalStateException("Check Order.bitLength");
			Order<? super K> subOrder = order.subOrder(entry.key);
			if (subOrder != null) {
			     SparseMap<K,V> subMap = new SparseMap<K,V>(subOrder);
  			     subMap.put(entry.key, entry.value);
  			     nodes[(index >>> shift) & MASK] = subMap;
  		         return subMap.put(key, value);
			}
			FractalTable<EntryImpl<K,V>> colliding = new FractalTable<EntryImpl<K,V>>();
		    colliding.add(entry);
		    nodes[(index >>> shift) & MASK] = colliding;
		    colliding.add(new EntryImpl<K,V>(key, value, index));
	  		return null;    
		}
		FractalTable<EntryImpl<K,V>> colliding = (FractalTable<EntryImpl<K,V>>) node;
		for (EntryImpl<K,V> entry : colliding) 
			if (order.areEqual(key, entry.key)) {
				V previous = entry.value;
				entry.value = value;
				return previous;
			}
		colliding.add(new EntryImpl<K,V>(key, value, index));
		size++;
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
    public EntryImpl<K,V> removeEntry(K key) {
		return removeEntry(key, order.indexOf((K)key));
	}
	
	@SuppressWarnings("unchecked")
    private EntryImpl<K,V> removeEntry(K key, int index) {
    	Object node = nodes[(index >>> shift) & MASK];
		if (node == null) return null;
		if (node instanceof SparseMap) { // Recursion.
    		SparseMap<K,V> subMap = (SparseMap<K,V>)node;
	  	    EntryImpl<K,V> previous = (subMap.order == order) ? 
	  	    		subMap.removeEntry(key, index) : subMap.removeEntry(key);
    	    if (previous != null) size--;
    	    if (subMap.size <= 1) // Cleanup.
    	    	nodes[(index >>> shift) & MASK] = subMap.iterator().next();
			return previous;
    	}
		if (node instanceof EntryImpl) {
		     EntryImpl<K,V> entry = (EntryImpl<K,V>) node;
		     if ((entry.index == index) &&  order.areEqual(key, entry.key)) {
		    	 size--;
		    	 nodes[(index >>> shift) & MASK] = null;
		    	 return entry;
		     }
		     return null;
		}
		FractalTable<EntryImpl<K,V>> colliding = (FractalTable<EntryImpl<K,V>>) node;
		for (Iterator<EntryImpl<K,V>> itr = colliding.iterator(); itr.hasNext();) {
			EntryImpl<K,V> entry = itr.next();
			if (order.areEqual(key, entry.key)) {
				itr.remove();
				size--;
				if (colliding.size() <= 1) 
					 nodes[(index >>> shift) & MASK] = colliding.iterator().next();
				return entry;
			}
		}
		return null;
	}	
		
	/** Entry Implementation **/
   private static class EntryImpl<K,V> implements Entry<K, V> { 
		private final int index; 
		private final K key;
		private V value; 
		private EntryImpl(K key, V value, int index) {
			this.key = key;
			this.value = value;
			this.index = index;
		}

		@Override
		public K getKey() {
			return key;
		}
		
		@Override
		public V getValue() {
			return value;
		}
		
		@Override
		public V setValue(V newValue) {
			V oldValue = value;	
		    this.value = newValue;
			return oldValue;
		}

		@Override
		public boolean equals(Object obj) { // As per Map.Entry contract.
			if (!(obj instanceof Entry)) return false;
			@SuppressWarnings("unchecked")
			Entry<K,V> that = (Entry<K,V>) obj;
			return Equality.STANDARD.areEqual(key, that.getKey())
					&& Equality.STANDARD.areEqual(value, that.getValue());
		}

		@Override
		public int hashCode() { // As per Map.Entry contract.
			return (key == null ? 0 : key.hashCode())
					^ (value == null ? 0 : value.hashCode());
		}

		@Override
		public String toString() {
			return "(" + key + '=' + value + ')'; // For debug.
		}
   }

	@Override
	public void clear() {
		for (int i=0; i < nodes.length; i++) nodes[i] = null;
		size = 0;
	}

	@Override
	public FastIterator<Entry<K, V>> iterator(K fromKey, K toKey) {
		// TODO Auto-generated method stub
		return null;
	}
	
}