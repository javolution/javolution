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
import javolution.lang.Index;
import javolution.lang.Realtime;
import javolution.util.function.Equality;
import javolution.util.function.Order;

/**
 * <p> A <a href="http://en.wikipedia.org/wiki/Trie">trie-based</a> associative
 *     array in which most of the elements are {@code null)}.</p>
 * 
 * <p> The trie-based structure allows for extremely fast (constant time)
 *     access/insertion/deletion.</p>
 * 
 * <p> The memory footprint of the array is automatically adjusted up or down in
 *     constant time (minimal when the array is cleared).</p>
 * 
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 7.0, September 13, 2015
 */
public final class SparseArray<E> extends FastMap<Index, E> {
	
	/**
	 * Custom entry holding an additional index field (unsigned 32-bits).
	 */
	public interface SparseEntry<K,V> extends Entry<K, V> {
		
		/**
		 * Returns the index of the entry (unsigned 32-bits value).
		 */
		int getIndex(); 
		
	}

	private static final long serialVersionUID = 0x700L; // Version.
	private static final int SHIFT = 4;
	private static final int SIZE = 1 << SHIFT;
	private static final int MASK = SIZE - 1;

	/** Holds the trie structure of minimal depth. */
	private Node<Index, E> root = NullNode.getInstance();

	/** Holds the number of non-null elements */
	private int size;

	/**
	 * Creates an empty sparse array (32-bits unsigned indices).
	 */
	public SparseArray() {
	}

	/**
	 * Returns the element at the specified index.
	 */
	@Realtime(limit = CONSTANT)
	public E get(int index) {
		EntryNode<Index, E> entry = root.getEntry(index);
		return (entry != null) ? entry.getValue() : null;
	}

	/** Returns the entry at the specified index. */
	public SparseEntry<Index,E> getEntry(int index) {
		return root.getEntry(index);
	}
	
    /**
	 * Removes and returns the entry at the specified index.
	 */
	@Realtime(limit = CONSTANT)
	public SparseEntry<Index,E> removeEntry(int index) {
		EntryNode<Index,E> previous = root.removeEntry(index);
		if (previous != null)
			size--;
		if (previous == DOWNSIZE) {
			previous = root.getEntry(index); 
			root = root.downsize(index);
		} else if (previous == DELETE) {
			previous = root.getEntry(index);
			root = NullNode.getInstance();
		}
		return previous;
	}

	/**
	 * Sets the element at the specified index and returns the previous element.
	 */
	@Realtime(limit = CONSTANT)
	public E set(int index, E element) {
		if (element == null)
			return remove(index);
		EntryNode<Index, E> entry = root.entry(index);
		if (entry == UPSIZE) { // Resizes.
			root = root.upsize(index);
			entry = root.entry(index);
		}
		if (entry.key == EntryNode.NOT_INITIALIZED) {
			entry.key = Index.of(index);
			size++;
		}
		return entry.setValue(element);
	}

	/** Returns the sparse entry after the specified index or {@code null}
	 * if none. */
	public SparseEntry<Index,E> higherEntry(int index) {
		if (index == -1) return null;
		return root.ceilingEntry(index+1);
	}
	
	/** Returns the sparse entry before the specified index or {@code null}
	 * if none. */
	public SparseEntry<Index,E> lowerEntry(int index) {
		if (index == 0) return null;
		return root.floorEntry(index-1);
	}

	/** Returns the sparse entry at or after the specified index or {@code null}
	 * if none. */
	public SparseEntry<Index,E> ceilingEntry(int index) {
		return root.ceilingEntry(index);
	}
	
	/** Returns the sparse entry at or before the specified index or {@code null}
	 * if none. */
	public SparseEntry<Index,E> floorEntry(int index) {
		return root.floorEntry(index);
	}

	////////////////////////////////////////////////////////////////////////////
	// FastMap<Index,E> 
	
	@Override
	public SparseEntry<Index,E> getEntry(Index key) {
		return getEntry(key.intValue());
	}

	@Override
	public SparseEntry<Index,E> removeEntry(Index key) {
		return removeEntry(key.intValue());
	}
	
	@Override
	public Order<? super Index> comparator() {
		return Order.INDEX;
	}

	@Override
	public E put(Index key, E value) {
		return set(key.intValue(), value);
	}

	@Override
	public void clear() {
		root = NullNode.getInstance();
		size = 0;
	}

	@Override
	public SparseArray<E> clone() {
		SparseArray<E> copy = (SparseArray<E>) super.clone();
		copy.root = root.clone();
		return copy;
	}

	@Override
	@Realtime(limit = CONSTANT)
	public int size() {
		return size;
	}

	@Override
	public Equality<? super E> valuesEquality() {
		return Equality.DEFAULT;
	}
	
	@Override
	public SparseEntry<Index,E> firstEntry() {
		return root.ceilingEntry(0);
	}

	@Override
	public SparseEntry<Index,E> lastEntry() {
		return root.floorEntry(-1);
	}

	@Override
	public SparseEntry<Index,E> higherEntry(Index key) {
		return higherEntry(key.intValue());
	}
	
	@Override
	public SparseEntry<Index,E> lowerEntry(Index key) {
		return lowerEntry(key.intValue());
	}

	@Override
	public SparseEntry<Index,E> ceilingEntry(Index key) {
		return ceilingEntry(key.intValue());
	}

	@Override
	public SparseEntry<Index,E> floorEntry(Index key) {
		return floorEntry(key.intValue());
	}

	/** Converts to unsigned */
	private static long toUnsigned(int i) {
		return 0xFFFFFFFFL & i;
	}
	
	/**
	 * A Node is either an entry node (leaf) or a trie structure. To ensure
	 * minimal depth and memory footprint, there is no trie structure with less
	 * than two sub-nodes. Also there is no entry node with null elements.
	 */
	interface Node<K,V> extends Cloneable {
		Node<K,V> clone();
		Node<K,V> downsize(int indexRemoved); // Returns the down-sized node.
		EntryNode<K,V> getEntry(int index); // Returns the entry or null.
		EntryNode<K,V> entry(int index); // Returns the entry, a new entry or UPSIZE 
		EntryNode<K,V> ceilingEntry(int index); // Returns entry at or above specified index.
		EntryNode<K,V> floorEntry(int index); // Returns entry at or below specified index.
		EntryNode<K,V> removeEntry(int index); // May return DOWNSIZE (trie node) or DELETE (entry node)
		Node<K,V> upsize(int indexAdded); // Returns the up-sized node.		
	}
	static final EntryNode<?,?> UPSIZE = new EntryNode<Object, Object>(-1);
	static final EntryNode<?,?> DOWNSIZE = new EntryNode<Object, Object>(-1);
	static final EntryNode<?,?> DELETE = new EntryNode<Object, Object>(-1);
	

	/** Defines the entry (leaf node) */
	static final class EntryNode<K,V> implements Node<K,V>, SparseEntry<K,V> {
        static final Object NOT_INITIALIZED = new Object();
		private final int index;
		K key;
		V value;

		@SuppressWarnings("unchecked")
		public EntryNode(int index) {
			this.index = index;
			this.key = (K) NOT_INITIALIZED;
		}
		public EntryNode(int index, K key, V value) {
			this.index = index;
			this.key = key;
			this.value = value;
		}

		@SuppressWarnings("unchecked")
		@Override
		public EntryNode<K,V> clone() {
			try {
				return (EntryNode<K, V>) super.clone();
			} catch (CloneNotSupportedException e) {
				throw new AssertionError(e);
			}
		}

		@Override
		public EntryNode<K, V> getEntry(int i) {
			return (index == i) ? this : null;
		}

		@SuppressWarnings("unchecked")
		@Override
		public EntryNode<K,V> entry(int i) {
			if (index != i)
				return (EntryNode<K,V>)UPSIZE;
			return this;
		}

		@SuppressWarnings("unchecked")
		@Override
		public EntryNode<K,V> removeEntry(int i) {
			return (index == i) ? (EntryNode<K,V>)DELETE : null;
		}

		@Override
		public TrieNode<K,V> upsize(int indexAdded) {
			return new TrieNode<K,V>(commonShift(index, indexAdded), index, this);
		}

		@Override
		public Node<K,V> downsize(int indexRemoved) {
			throw new AssertionError();
		}

		@Override
		public EntryNode<K,V> ceilingEntry(int i) {
			return toUnsigned(index) >=  toUnsigned(i) ? this : null;
		}
		
		@Override
		public EntryNode<K,V> floorEntry(int i) {
			return toUnsigned(index) <=  toUnsigned(i) ? this : null;
		}

		///////////////////////////////////////////////////////////////////////
		// SparseEntry<K,V> Implementation.
		
		@Override
		public int getIndex() {
			return index;
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
			V previous = value;
			value = newValue;
			return previous;
		}

		@Override
		public boolean equals(Object obj) { // As per Map.Entry contract.
			if (!(obj instanceof Entry))
				return false;
			@SuppressWarnings("unchecked")
			Entry<K, V> that = (Entry<K, V>) obj;
			return Order.DEFAULT.areEqual(key, that.getKey())
					&& Order.DEFAULT.areEqual(value, that.getValue());
		}

		@Override
		public int hashCode() { // As per Map.Entry contract.
			return Order.DEFAULT.indexOf(key) ^ Order.DEFAULT.indexOf(value);
		}

		@Override
		public String toString() {
			return "(" + key + '=' + value + ')'; // For debug.
		}
	}
	
	/** Defines the trie node */
	private static final class TrieNode<K,V> implements Node<K,V> {
		@SuppressWarnings("unchecked")
		private final Node<K,V>[] trie = (Node<K,V>[]) new Node[SIZE];
		private final int shift; // 0 for leaf trie-nodes
		private final int prefix; // Lower (shift) bits are reset.
		int count; // Number of direct sub-nodes different from null.

		public TrieNode(int shift, int index, Node<K,V> subNodeAtIndex) {
			this.shift = shift;
			this.prefix = index & (~MASK << shift);
			trie[(index >>> shift) & MASK] = subNodeAtIndex;
			count = 1;
		}

		@SuppressWarnings("unchecked")
		@Override
		public TrieNode<K,V> clone() {
			try {
				TrieNode<K,V> copy = (TrieNode<K,V>)super.clone();
				for (int i=0, n=copy.trie.length; i < n; i++)
					if (trie[i] != null) trie[i] = trie[i].clone();
				return copy;
			} catch (CloneNotSupportedException e) {
				throw new AssertionError(e);
			}
		}

		@Override
		public Node<K,V> downsize(int indexRemoved) { // Called if count goes to 1
			int j = (indexRemoved >>> shift) & MASK;
			for (int i = 0; (i < trie.length) & (i != j); i++) {
				Node<K,V> n = trie[i];
				if (n != null)
					return n;
			}
			throw new AssertionError("Trie Corruption !?");
		}

		@Override
		public EntryNode<K,V> getEntry(int i) {
			// We don't check the prefix, since the index will be validated
			// (or not) by the entry node.
			Node<K,V> n = trie[(i >>> shift) & MASK];
			return (n != null) ? n.getEntry(i) : null;
		}

		@Override
		public EntryNode<K,V> ceilingEntry(int i) {
			for (int j=(i >>> shift) & MASK; j < SIZE; j++) {
				Node<K,V> n = trie[j];
				if (n == null) continue;
				return n.ceilingEntry(i); 
			}
			return null;
		}
		
		@Override
		public EntryNode<K,V> floorEntry(int i) {
			for (int j=(i >>> shift) & MASK; j >= 0; j--) {
				Node<K,V> n = trie[j];
				if (n == null) continue;
				return n.floorEntry(i); 
			}
			return null;
		}

		@SuppressWarnings("unchecked")
		@Override
		public EntryNode<K,V> removeEntry(int index) {
			final int i = (index >>> shift) & MASK;
			Node<K,V> n = trie[i];
			if (n == null)
				return null;
			EntryNode<K,V> previous = n.removeEntry(index);
			if (previous == DOWNSIZE) {
				previous = n.getEntry(index);
				trie[i] = n.downsize(index);
			} else if (previous == DELETE) { // EntryNode 
				if (count <= 2)
					return (EntryNode<K,V>)DOWNSIZE;
				previous = n.getEntry(index);
				trie[i] = null;
				count--;
			}
			return previous;
		}

		@SuppressWarnings("unchecked")
		@Override
		public EntryNode<K,V> entry(int index) {
			if (((prefix ^ index) & (0xFFFFFFF0 << shift)) != 0)
				return (EntryNode<K,V>) UPSIZE;
			final int i = (index >>> shift) & MASK;
			Node<K,V> n = trie[i];
			if (n == null) {
				count++;
			    EntryNode<K,V> newEntry = new EntryNode<K,V>(index);
			    trie[i] = newEntry;
				return newEntry;
			}
			EntryNode<K,V> previous = n.entry(index);
			if (previous != UPSIZE)
				return previous;
			Node<K,V> tmp = n.upsize(index);
			trie[i] = tmp;
			return tmp.entry(index);
		}

		@Override
		public TrieNode<K,V> upsize(int indexAdded) {
			return new TrieNode<K,V>(commonShift(prefix, indexAdded), prefix,
					this);
		}
	}
	
	/** Defines the trie node */
	static final class NullNode<K,V> implements Node<K,V> {
		private static final NullNode<?,?> NULL = new NullNode<Object, Object>();

		@SuppressWarnings("unchecked")
		public static <K,V> NullNode<K,V> getInstance() {
			return (NullNode<K, V>) NULL;
		}
		@Override
		public Node<K, V> clone() {
			return this;
		}

		@Override
		public Node<K, V> downsize(int indexRemoved) {
			throw new AssertionError();
		}

		@Override
		public EntryNode<K, V> getEntry(int index) {
			return null;
		}

		@SuppressWarnings("unchecked")
		@Override
		public EntryNode<K, V> entry(int index) {
			return (EntryNode<K, V>) UPSIZE;
		}

		@Override
		public EntryNode<K, V> ceilingEntry(int index) {
			return null;
		}

		@Override
		public EntryNode<K, V> floorEntry(int index) {
			return null;
		}

		@Override
		public EntryNode<K, V> removeEntry(int index) {
			return null;
		}

		@Override
		public Node<K, V> upsize(int indexAdded) {
			return new EntryNode<K,V>(indexAdded);
		}
		
	}

	/**
	 * Returns the minimal shift for two indices (the higher the number of
	 * common high bits the minimal the shift)
	 */
	private static int commonShift(int i, int j) {
		int xor = i ^ j;
		if ((xor & 0xFFFF0000) == 0)
			if ((xor & 0xFFFFFF00) == 0)
				if ((xor & 0xFFFFFFF0) == 0)
					return 0;
				else
					return 4;
			else if ((xor & 0xFFFFF000) == 0)
				return 8;
			else
				return 12;
		else if ((xor & 0xFF000000) == 0)
			if ((xor & 0xFFF00000) == 0)
				return 16;
			else
				return 20;
		else if ((xor & 0xF0000000) == 0)
			return 24;
		else
			return 28;
	}

}