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
 * <p> The memory footprint of the array is automatically adjusted up or down
 *     in constant time (minimal when the array is cleared).</p>
 *      
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 7.0, September 13, 2015
 */
public class SparseArray<E> extends FastMap<Index,E> {
	
	private static final long serialVersionUID = 0x700L; // Version. 
    private static final int SHIFT = 4;
    private static final int SIZE = 1 << SHIFT;
    private static final int MASK = SIZE - 1;

    private static final Object UPSIZE = new Object();
    
	private interface Node<E> {
		E get(int index);
		Object set(int index, E element); // May return UPSIZE request.
		E remove(int index); 
		int size();
		int index();
	}

	private static final class EntryNode<E> implements Node<E> {
		private final int index;
		private E element;
		
		public EntryNode(int index, E element) {
			this.index = index;
			this.element = element;
		}
		
		@Override
		public E get(int index) {
			return (this.index == index) ? element : null;
		}

		@Override
		public Object set(int index, E element) {
			if ((this.index != index) && (this.element != null)) return UPSIZE;
		    E previous = element;
			this.element = element;
			return previous;
		}

		@Override
		public E remove(int index) {
			if (this.index != index) return null;
			E previous = element;
			element = null;
			return previous;
		}
		
		@Override
		public int size() {
			return (element != null) ? 1 : 0;
		}
		
		@Override
		public int index() {
			return index;
		}
		
	}
	
	private static final class TrieNode<E> implements Node<E> {
		@SuppressWarnings("unchecked")
		private final Node<E>[] trie = (Node<E>[]) new Node[SIZE];
		private final int shift; // 32 - SHIFT - 'prefix bit length'  
		private final int prefix; // Lower bits are undefined. 
		int size; 
		
		public TrieNode(int shift, int prefix) {
			this.shift = prefix;
			this.prefix = prefix;
		}
		
		@Override
		public E get(int index) {
			// We don't check the prefix -> Is it really faster ??
			Node<E> n = trie[(index >>> shift) & MASK];
			return (n != null) ? n.get(index) : null;
		}
		
		@Override
		public Object set(int index, E element) {
			if ((index >>> (shift + SHIFT)) != prefix) return UPSIZE;
			final int i = (index >>> shift) & MASK;
			Node<E> n = trie[i];
			if (n == null) {
				trie[i] = new EntryNode<E>(index, element);
				size++;
				return null;
			}
			Object tmp = n.set(index, element);			
			if (tmp != UPSIZE) {
				if (tmp == null) size++;
				return tmp;
			}
			int prefixLength = prefixLength(index, n.index());
			TrieNode<E> trieNode = new TrieNode<E>(32 - SHIFT - prefixLength, index);
			trieNode.setFirstNode(n.index(), n);
			trie[i] = trieNode;
			return trieNode.set(index, element);
		}
		private void setFirstNode(int index, Node<E> node) {
			 trie[(index >>> shift) & MASK] = node;
			 size = node.size();
		}
		
		@Override
		public int index() {
			return prefix;
		}

		@Override
		public E remove(int index) {
			// TODO Check number of elements set (count?) if only one -> compact.
			return null;
		}

		@Override
		public int size() {
			return size;
		}
		
	}

	/** Holds the trie structure of minimal depth (no non-leaf node with 
	 * less than two sub-nodes). */
	private Node<E> trie = new EntryNode<E>(0 ,null);
	
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
    	return trie.get(index);
    }

	/**
     * Sets the element at the specified index and returns the previous element.
     */
    @SuppressWarnings("unchecked")
	@Realtime(limit = CONSTANT)
    public E set(int index, E element) {
    	if (element == null) return remove(index);
    	Object tmp = trie.set(index, element);
    	if (tmp != UPSIZE) return (E) tmp; 
		int prefixLength = prefixLength(index, trie.index());
		TrieNode<E> trieNode = new TrieNode<E>(32 - SHIFT - prefixLength, index);
		trieNode.setFirstNode(trie.index(), trie);
		trie = trieNode;
		return (E) trie.set(index, element);
    }
    
	/**
     * Removes and returns the element at the specified index.
     */
    @Realtime(limit = CONSTANT)
    public E remove(int index) {
    	return null; // TODO
    }
    
	/**
     * Sets the element at the specified index only if none and returns 
     * the previous element.
     */
    @Realtime(limit = CONSTANT)
    public E setIfAbsent(int index, E node) {
        return null; // TBD	
    }
   
	@Override
    @Realtime(limit = CONSTANT)
	public int size() {
		return trie.size();
	}
	
	@Override
    @Realtime(limit = CONSTANT)
	public void clear() {
		trie = new EntryNode<E>(0 ,null);
	}

	private static int prefixLength(int index1, int index2) {
		int mask = MASK << (32-SHIFT);
		for (int s=0;;s += SHIFT) {
			if ((index1 & mask) != (index2 & mask)) return s;
			mask >>= SHIFT;
		}
	}

	@Override
	public Order<? super Index> keyOrder() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Equality<? super E> valueEquality() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FastMap<Index, E> clone() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public java.util.Map.Entry<Index, E> getEntry(Index key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public java.util.Map.Entry<Index, E> putEntry(
			java.util.Map.Entry<Index, E> entry) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public java.util.Map.Entry<Index, E> removeEntry(Index key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public java.util.Map.Entry<Index, E> firstEntry() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public java.util.Map.Entry<Index, E> lastEntry() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public java.util.Map.Entry<Index, E> entryAfter(Index key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public java.util.Map.Entry<Index, E> entryBefore(Index key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public java.util.Map.Entry<Index, E> midEntry(Index fromKey, Index toKey) {
		// TODO Auto-generated method stub
		return null;
	}
}