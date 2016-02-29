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

import java.util.NoSuchElementException;

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
public class SparseArray<E> extends FastMap<Index, E> {
	
	/** The SparseArray indexed entry. */
	public interface SparseEntry<E> extends Entry<Index, E> {
		/** Returns the unsigned key index value of this entry */
		int index(); 
	}

	private static final long serialVersionUID = 0x700L; // Version.
	private static final int SHIFT = 4;
	private static final int SIZE = 1 << SHIFT;
	private static final int MASK = SIZE - 1;

	private static final Object UPSIZE = new Object();
	private static final Object DOWNSIZE = new Object();
	private static final Object DELETE = new Object();

	/** Holds the trie structure of minimal depth. */
	private Node<E> root = null;

	/** Holds the number of non-null elements */
	private int size;

	/**
	 * Creates an empty sparse array (32-bits unsigned indices).
	 */
	public SparseArray() {
	}

	@Override
	@Realtime(limit = CONSTANT)
	public void clear() {
		root = null;
	}

	@Override
	public SparseArray<E> clone() {
		SparseArray<E> copy = new SparseArray<E>();
		copy.root = (root != null) ? root.clone() : null;
		copy.size = size;
		return copy;
	}

	/**
	 * Returns the element at the specified index.
	 */
	@Realtime(limit = CONSTANT)
	public E get(int index) {
		return (root != null) ? root.get(index) : null;
	}

	@Override
	public SparseEntry<E> getEntry(Index key) {
		return getEntry(key.intValue());
	}

	@Override
	public Order<? super Index> keyOrder() {
		return Order.INDEX;
	}

	@Override
	public E put(Index key, E value) {
		return set(key.intValue(), value);
	}

	/**
	 * Removes and returns the element at the specified index.
	 */
	@SuppressWarnings("unchecked")
	@Realtime(limit = CONSTANT)
	public E remove(int index) {
		if (root == null)
			return null;
		Object previous = root.remove(index);
		if (previous != null)
			size--;
		if (previous == DOWNSIZE) {
			previous = root.get(index);
			root = root.downsize(index);
		} else if (previous == DELETE) {
			previous = root.get(index);
			root = null;
		}
		return (E) previous;
	}

	@Override
	public E remove(Object key) {
		return (key instanceof Index) ? set(((Index)key).intValue(), null) : null;
	}

	/**
	 * Sets the element at the specified index and returns the previous element.
	 */
	@SuppressWarnings("unchecked")
	@Realtime(limit = CONSTANT)
	public E set(int index, E element) {
		if (element == null)
			return remove(index);
		if (root != null) {
			Object previous = root.set(index, element);
			if (previous == null)
				size++;
			if (previous != UPSIZE)
				return (E) previous;
			root = root.upsize(index);
			root.set(index, element);
		} else {
			root = new EntryNode<E>(index, element);
		}
		size++; // We had to up-size.
		return null;
	}

	/**
	 * Sets the element at the specified index only if none and returns the
	 * previous element.
	 */
	@Realtime(limit = CONSTANT)
	@SuppressWarnings("unchecked")
	public E setIfAbsent(int index, E element) {
		if (element == null)
			return get(index);
		if (root != null) {
			Object previous = root.setIfAbsent(index, element);
			if (previous == null)
				size++;
			if (previous != UPSIZE)
				return (E) previous;
			root = root.upsize(index);
			root.setIfAbsent(index, element);
		} else {
			root = new EntryNode<E>(index, element);
		}
		size++; // We had to up-size.
		return null;
	}

	/** Returns the sparse entry at the specified index. */
	public SparseEntry<E> getEntry(int index) {
		return (root != null) ? root.getEntry(index) : null;
	}
	
	/** Returns the sparse entry after the specified index. */
	public SparseEntry<E> getEntryAfter(int index) {
		if ((root == null) || (index == -1)) return null;
		return root.getEntryUpFrom(index+1);
	}
	
	/** Returns the sparse entry before the specified index. */
	public SparseEntry<E> getEntryBefore(int index) {
		if ((root == null) || (index == 0)) return null;
		return root.getEntryDownFrom(index-1);
	}

	@Override
	@Realtime(limit = CONSTANT)
	public int size() {
		return size;
	}

	@Override
	public Equality<? super E> valueEquality() {
		return Equality.STANDARD;
	}
	
	@Override
	public SparseEntry<E> firstEntry() {
		if (root == null) throw new NoSuchElementException();
		return root.getEntryUpFrom(0);
	}

	@Override
	public SparseEntry<E> lastEntry() {
		if (root == null) throw new NoSuchElementException();
		return root.getEntryDownFrom(-1);
	}

	@Override
	public SparseEntry<E> getEntryAfter(Index key) {
		return getEntryAfter(key.intValue());
	}
	
	@Override
	public SparseEntry<E> getEntryBefore(Index key) {
		return getEntryBefore(key.intValue());
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

	/** Converts to unsigned */
	private static long toUnsigned(int i) {
		return 0xFFFFFFFFL & i;
	}
	
	/**
	 * A Node is either an entry node (leaf) or a trie structure. To ensure
	 * minimal depth and memory footprint, there is no trie structure with less
	 * than two sub-nodes. Also there is no entry node with null elements.
	 */
	private interface Node<V> {
		Node<V> clone();
		Node<V> downsize(int indexRemoved); // Returns the down-sized node.
		V get(int index);
		EntryNode<V> getEntry(int index);
		EntryNode<V> getEntryUpFrom(int index);
		EntryNode<V> getEntryDownFrom(int index);
		Object remove(int index); // May return DOWNSIZE (trie node) or DELETE (entry node)
		Object set(int index, V value); // May return UPSIZE request, value should never be null.
		Object setIfAbsent(int index, V value); // May return UPSIZE request, value should never be null.
		Node<V> upsize(int indexAdded); // Returns the up-sized node.
		
	}

	/** Defines the entry (leaf node) */
	private static final class EntryNode<V> implements Node<V>, SparseEntry<V> {
		private final int index;
		private V value; // Always different from null.

		public EntryNode(int index, V value) {
			this.index = index;
			this.value = value;
		}

		@Override
		public EntryNode<V> clone() {
			return new EntryNode<V>(index, value);
		}

		@Override
		public Node<V> downsize(int indexRemoved) {
			throw new UnsupportedOperationException("Cannot downsize entries");
		}

		public boolean equals(EntryNode<V> that) { 
			return (this.index == that.index) && (this.value.equals(that.value));
		}

		@Override
		@SuppressWarnings("unchecked")
		public boolean equals(Object obj) { // As per Map.Entry contract.
			if (obj instanceof EntryNode) return equals((EntryNode<V>)obj);
			if (!(obj instanceof Entry)) return false;
			Entry<Index, V> that = (Entry<Index, V>) obj;
			Index thatKey = that.getKey();
			return (thatKey != null) && (index == thatKey.intValue())
					&& value.equals(that.getValue());
		}

		@Override
		public V get(int i) {
			return (index == i) ? value : null;
		}

		@Override
		public EntryNode<V> getEntry(int i) {
			return (index == i) ? this : null;
		}

		@Override
		public EntryNode<V> getEntryUpFrom(int i) {
			return toUnsigned(index) >=  toUnsigned(i) ? this : null;
		}
		
		@Override
		public EntryNode<V> getEntryDownFrom(int i) {
			return toUnsigned(index) <=  toUnsigned(i) ? this : null;
		}
		
		@Override
		public int index() {
			return index;
		}
		@Override
		public Index getKey() {
			return Index.of(index);
		}

		@Override
		public V getValue() {
			return value;
		}

		@Override
		public int hashCode() { // As per Map.Entry contract.
			return index ^ value.hashCode();
		}

		@Override
		public Object remove(int i) {
			return (index == i) ? DELETE : null;
		}

		@Override
		public Object set(int i, V newValue) {
			if (index != i)
				return UPSIZE;
			V previous = value;
			value = newValue;
			return previous;
		}
		
		@Override
		public Object setIfAbsent(int i, V newValue) {
			if (index != i)
				return UPSIZE;
			return value; // Always different from null.
		}

		@Override
		public V setValue(V newValue) {
			V previous = value;
			value = newValue;
			return previous;
		}

		@Override
		public String toString() {
			return "(" + index + '=' + value + ')'; // For debug.
		}

		@Override
		public Node<V> upsize(int indexAdded) {
			return new TrieNode<V>(commonShift(index, indexAdded), index, this);
		}
	}
	
	/** Defines the trie node */
	private static final class TrieNode<V> implements Node<V> {
		@SuppressWarnings("unchecked")
		private final Node<V>[] trie = (Node<V>[]) new Node[SIZE];
		private final int shift; // 0 for leaf trie-nodes
		private final int prefix; // Lower (shift) bits are reset.
		int count; // Number of direct sub-nodes different from null.

		public TrieNode(int shift, int index, Node<V> subNodeAtIndex) {
			this.shift = shift;
			this.prefix = index & (~MASK << shift);
			trie[(index >>> shift) & MASK] = subNodeAtIndex;
			count = 1;
		}

		@SuppressWarnings("unchecked")
		@Override
		public TrieNode<V> clone() {
			try {
				TrieNode<V> copy = (TrieNode<V>)super.clone();
				for (int i=0, n=copy.trie.length; i < n; i++)
					if (trie[i] != null) trie[i] = trie[i].clone();
				return copy;
			} catch (CloneNotSupportedException e) {
				throw new AssertionError(e);
			}
		}

		@Override
		public Node<V> downsize(int indexRemoved) { // Called if count goes to 1
			int j = (indexRemoved >>> shift) & MASK;
			for (int i = 0; (i < trie.length) & (i != j); i++) {
				Node<V> n = trie[i];
				if (n != null)
					return n;
			}
			throw new IllegalStateException("Trie Corruption");
		}

		@Override
		public V get(int i) {
			// We don't check the prefix, since the index will be validated
			// (or not) by the entry node.
			Node<V> n = trie[(i >>> shift) & MASK];
			return (n != null) ? n.get(i) : null;
		}

		@Override
		public EntryNode<V> getEntry(int i) {
			Node<V> n = trie[(i >>> shift) & MASK];
			return (n != null) ? n.getEntry(i) : null;
		}

		@Override
		public EntryNode<V> getEntryUpFrom(int i) {
			for (int j=(i >>> shift) & MASK; j < SIZE; j++) {
				Node<V> n = trie[j];
				if (n == null) continue;
				return n.getEntryUpFrom(i); 
			}
			return null;
		}
		
		@Override
		public EntryNode<V> getEntryDownFrom(int i) {
			for (int j=(i >>> shift) & MASK; j >= 0; j--) {
				Node<V> n = trie[j];
				if (n == null) continue;
				return n.getEntryDownFrom(i); 
			}
			return null;
		}

		@Override
		public Object remove(int index) {
			final int i = (index >>> shift) & MASK;
			Node<V> n = trie[i];
			if (n == null)
				return null;
			Object previous = n.remove(index);
			if (previous == DOWNSIZE) {
				previous = n.get(index);
				trie[i] = n.downsize(index);
			} else if (previous == DELETE) { // EntryNode 
				if (count <= 2)
					return DOWNSIZE;
				previous = n.get(index);
				trie[i] = null;
				count--;
			}
			return previous;
		}

		@Override
		public Object set(int index, V value) {
			if (((prefix ^ index) & (0xFFFFFFF0 << shift)) != 0)
				return UPSIZE;
			final int i = (index >>> shift) & MASK;
			Node<V> n = trie[i];
			if (n == null) {
				trie[i] = new EntryNode<V>(index, value);
				count++;
				return null;
			}
			Object previous = n.set(index, value);
			if (previous != UPSIZE)
				return previous;
			Node<V> tmp = n.upsize(index);
			trie[i] = tmp;
			tmp.set(index, value);
			return null;
		}

		@Override
		public Object setIfAbsent(int index, V newValue) {
			if (((prefix ^ index) & (0xFFFFFFF0 << shift)) != 0)
				return UPSIZE;
			final int i = (index >>> shift) & MASK;
			Node<V> n = trie[i];
			if (n == null) {
				trie[i] = new EntryNode<V>(index, newValue);
				count++;
				return null;
			}
			Object previous = n.setIfAbsent(index, newValue);
			if (previous != UPSIZE)
				return previous;
			Node<V> tmp = n.upsize(index);
			trie[i] = tmp;
			tmp.setIfAbsent(index, newValue);
			return null;
		}

		@Override
		public Node<V> upsize(int indexAdded) {
			return new TrieNode<V>(commonShift(prefix, indexAdded), prefix,
					this);
		}
	}


}