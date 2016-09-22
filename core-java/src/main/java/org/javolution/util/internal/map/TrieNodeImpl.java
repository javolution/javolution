/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.util.internal.map;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

import org.javolution.lang.MathLib;
import org.javolution.util.FastMap;
import org.javolution.util.ReadOnlyIterator;
import org.javolution.util.function.Order;

/**
 * A Trie is either a map entry or a table of Trie elements. 
 * To ensure minimal depth and memory footprint, there is no table with less
 * than two sub-nodes.
 */
public abstract class TrieNodeImpl<K,V> implements Cloneable, Serializable {
    public static final Object SUB_MAP = new Object();
    public static final EntryNode<?,?> UPSIZE = new EntryNode<Object, Object>(-1);
    public static final EntryNode<?,?> DOWNSIZE = new EntryNode<Object, Object>(-1);
    private static final int SHIFT = 4;
    private static final int SIZE = 1 << SHIFT;
    private static final int MASK = SIZE - 1;
    private static final long serialVersionUID = 0x700L; // Version.
 
    /** Returns an empty node. */
    @SuppressWarnings("unchecked")
    public static <K,V> TrieNodeImpl<K,V> empty() {
        return ( TrieNodeImpl<K,V>) NULL;
    }    
    
    @Override
    public abstract TrieNodeImpl<K,V> clone();
        
    /** Returns the new node with the specified index removed. */
    public abstract TrieNodeImpl<K,V> downsize(int indexRemoved);
    
    /** Returns the entry at the specified index or {@code null} if none. */
    public abstract EntryNode<K,V> getEntry(int index); 
    
    /** Returns the entry at the specified index, a new entry created at the specified index 
     * (key set to NOT_INITIALIZED) or UPSIZE if there is no slot for the specified entry. */
    public abstract EntryNode<K,V> entry(int index); 

    /** Returns the entry at or above specified index or null if there is none. */
    public abstract EntryNode<K,V> ceilingEntry(int index);
    
    /** Returns entry at or below specified index or null if there is none. */
    public abstract EntryNode<K,V> floorEntry(int index);

    /** Removes and returns the entry at the specified index. Returns null if there is no entry or 
     *  DOWNSIZE if the entry slot should be removed (e.g. EntryNode removed or TrieNode holding a single element). 
     *  (capacity adjustment).*/
    public abstract EntryNode<K,V> removeEntry(int index);

    /** Returns the new node with the specified index inserted. */      
    public abstract TrieNodeImpl<K,V> upsize(int indexAdded);
    
    /** Defines the entry (leaf node) */
    public static final class EntryNode<K,V> extends TrieNodeImpl<K,V> implements Entry<K,V> {
        private static final long serialVersionUID = 0x700L; // Version. 
        public static final Object NOT_INITIALIZED = new Object();
        public final int index;
        private K key;
        private V value;

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

        /** Sets the key/value pair for this entry. */
        public void init(K key, V value) {
            this.key = key;
            this.value = value;
        }
        
        @Override
        public EntryNode<K,V> clone() {
            return new EntryNode<K,V>(index, key, value);
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
            return (index == i) ? (EntryNode<K,V>)DOWNSIZE : null;
        }

        @Override
        public TrieNode<K,V> upsize(int indexAdded) {
            return new TrieNode<K,V>(commonShift(index, indexAdded), index, this);
        }

        @SuppressWarnings("unchecked")
        @Override
        public TrieNodeImpl<K,V> downsize(int indexRemoved) {
            return (TrieNodeImpl<K, V>) NULL;
        }

        @Override
        public EntryNode<K,V> ceilingEntry(int i) {
            return MathLib.unsigned(index) >=  MathLib.unsigned(i) ? this : null;
        }
        
        @Override
        public EntryNode<K,V> floorEntry(int i) {
            return MathLib.unsigned(index) <=  MathLib.unsigned(i) ? this : null;
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

    /**  
     *  Each trie-node holds entries with indices in the range [prefix<<SHIFT<<shift .. (prefix+1)<<SHIFT<<shift)[ 
     */
    private static final class TrieNode<K,V> extends TrieNodeImpl<K,V> {
        private static final long serialVersionUID = 0x700L; // Version. 
        @SuppressWarnings("unchecked")
        private final TrieNodeImpl<K,V>[] trie = (TrieNodeImpl<K,V>[]) new TrieNodeImpl[SIZE];
        private final int shift; // 0 for leaf trie-nodes
        private final int prefix; 
        int count; // Number of direct sub-nodes different from null.

        public TrieNode(int shift, int index, TrieNodeImpl<K,V> subNodeAtIndex) {
            this.shift = shift;
            this.prefix = index >>> SHIFT >>> shift;
            trie[(index >>> shift) & MASK] = subNodeAtIndex;
            count = 1;
        }
        
        private TrieNode(int shift, int prefix) { // Constructor for cloning.
            this.shift = shift;
            this.prefix = prefix;
        }

        @Override
        public TrieNode<K,V> clone() {
            TrieNode<K,V> copy = new TrieNode<K,V>(shift, prefix);
            for (int i=0; i < SIZE; i++) {
                if (trie[i] != null) {
                    copy.trie[i] = trie[i].clone();
                    if (++copy.count == count) return copy; // Done.
                }
            }
            throw new AssertionError("Trie Corruption !?");
        }

        @Override
        public TrieNodeImpl<K,V> downsize(int indexRemoved) { // Called only when count goes to 1
            int j = (indexRemoved >>> shift) & MASK;
            for (int i = 0; (i < SIZE) && (i != j); i++) { // Search non-null node.
                TrieNodeImpl<K,V> n = trie[i];
                if (n != null) return n;
            }
            throw new AssertionError("Trie Corruption !?");
        }

        @Override
        public EntryNode<K,V> getEntry(int i) {
            // We don't check the prefix, since the index will be validated (or not) by the entry node.
            TrieNodeImpl<K,V> n = trie[(i >>> shift) & MASK];
            return (n != null) ? n.getEntry(i) : null;
        }

        @Override
        public EntryNode<K,V> ceilingEntry(int i) {
            int iPrefix = i >>> SHIFT >>> shift;
            if (iPrefix > prefix) return null; 
            else if (iPrefix < prefix) i = prefix << SHIFT << shift;
            for (int j=(i >>> shift) & MASK; j < SIZE; j++) {
                TrieNodeImpl<K,V> n = trie[j];
                if (n == null) continue;
                EntryNode<K,V> entry = n.ceilingEntry(i);
                if (entry != null) return entry;
            }
            return null;
        }
        
        @Override
        public EntryNode<K,V> floorEntry(int i) {
            int iPrefix = i >>> SHIFT >>> shift;
            if (iPrefix > prefix) i = ((prefix+1) << SHIFT << shift) - 1; 
            else if (iPrefix < prefix) return null;
            for (int j=(i >>> shift) & MASK; j >= 0; j--) {
                TrieNodeImpl<K,V> n = trie[j];
                if (n == null) continue;
                EntryNode<K,V> entry = n.floorEntry(i);
                if (entry != null) return entry;
            }
            return null;
        }

        @SuppressWarnings("unchecked")
        @Override
        public EntryNode<K,V> removeEntry(int index) {
            if (index >>> SHIFT >>> shift != prefix) return null; // Not in range.
            final int i = (index >>> shift) & MASK;
            TrieNodeImpl<K,V> n = trie[i];
            if (n == null) return null;
            EntryNode<K,V> previous = n.removeEntry(index);
            if (previous == DOWNSIZE) { // Two cases: Trie -> Node or Node -> NULL
                if ((count == 2) && (n instanceof EntryNode))
                    return (EntryNode<K,V>) DOWNSIZE;
                previous = n.getEntry(index);
                TrieNodeImpl<K,V> tmp = trie[i] = n.downsize(index);
                if (tmp == NULL) {
                    count--;
                    trie[i] = null;
                }
            }
            return previous;
        }

        @SuppressWarnings("unchecked")
        @Override
        public EntryNode<K,V> entry(int index) {
            if (index >>> SHIFT >>> shift != prefix) return (EntryNode<K,V>) UPSIZE; // Not in range.
            final int i = (index >>> shift) & MASK;
            TrieNodeImpl<K,V> n = trie[i];
            if (n == null) {
                count++;
                EntryNode<K,V> newEntry = new EntryNode<K,V>(index);
                trie[i] = newEntry;
                return newEntry;
            }
            EntryNode<K,V> previous = n.entry(index);
            if (previous != UPSIZE)
                return previous;
            TrieNodeImpl<K,V> tmp = n.upsize(index);
            trie[i] = tmp;
            return tmp.entry(index);
        }

        @Override
        public TrieNode<K,V> upsize(int indexAdded) {
            int thisIndex = prefix << SHIFT << shift;
            return new TrieNode<K,V>(commonShift(thisIndex, indexAdded), thisIndex, this);
        }     
    }
    
    
    /** Iterator over the entries of a sparse map. */
    public static final class NodeIterator<K,V> extends ReadOnlyIterator<Entry<K,V>> {
        private Iterator<Entry<K,V>> subItr; // Set when iterating sub-maps (subItr.hasNext() is always true)
        private EntryNode<Object,Object> next; 
        private Entry<K,V> current;
        private final TrieNodeImpl<Object,Object> root;
        public NodeIterator(TrieNodeImpl<Object,Object> root) {
            this.root = root;
            next = root.ceilingEntry(0);
            if ((next != null) && (next.key == SUB_MAP)) {
                @SuppressWarnings("unchecked")
                FastMap<K,V> subMap = ((FastMap<K,V>)next.value);
                subItr = subMap.iterator(); // subMap cannot be empty.
            }    
        }
        
        public NodeIterator(TrieNodeImpl<Object,Object> root, K from, int fromIndex) {
            this.root = root;
            next = root.ceilingEntry(fromIndex);
            if ((next != null) && (next.key == SUB_MAP)) {
                @SuppressWarnings("unchecked")
                FastMap<K,V> subMap = ((FastMap<K,V>)next.value);
                if (fromIndex == next.index) {
                    subItr = subMap.iterator(from);
                    if (!subItr.hasNext()) {
                        subItr = null;
                        next();                    
                    }
                } else { // We can ignore from.
                    subItr = subMap.iterator(); // subMap cannot be empty.
                }
            }    
        }

        @Override
        public boolean hasNext() {
            return (subItr != null) || (next != null);
        }

        @SuppressWarnings("unchecked")
        @Override
        public Entry<K,V> next() {
            if (subItr != null) {
                current = subItr.next();
                if (subItr.hasNext()) return current;
                subItr = null;
            } else {
                if (next == null) throw new NoSuchElementException();
                current = (Entry<K,V>)next;
            }
            // Moves to next node.
            next = (next.index != -1) ?
                    root.ceilingEntry((int) (MathLib.unsigned(next.index)+1)) : null;             
            if ((next != null) && (next.key == SUB_MAP)) {
                FastMap<K,V> subMap = ((FastMap<K,V>)next.value);
                subItr = subMap.iterator();
            }
            return current;
        }
  
    }
    
    /** Descending iterator overs the entries of a sparse map. */
    public static final class DescendingNodeIterator<K,V> extends ReadOnlyIterator<Entry<K,V>> {
        private Iterator<Entry<K,V>> subItr; // Set when iterating sub-maps (subItr.hasNext() is always true)
        private EntryNode<Object,Object> next; 
        private Entry<K,V> current = null;
        private final TrieNodeImpl<Object,Object> root;
        public DescendingNodeIterator(TrieNodeImpl<Object,Object> root) {
            this.root = root;
            next = root.floorEntry(-1);
            if ((next != null) && (next.key == SUB_MAP)) {
                @SuppressWarnings("unchecked")
                FastMap<K,V> subMap = ((FastMap<K,V>)next.value);
                subItr = subMap.iterator(); // subMap cannot be empty.
            }    
        }
        public DescendingNodeIterator(TrieNodeImpl<Object,Object> root, K from, int fromIndex) {
            this.root = root;
            next = root.floorEntry(fromIndex);
            if ((next != null) && (next.key == SUB_MAP)) {
                @SuppressWarnings("unchecked")
                FastMap<K,V> subMap = ((FastMap<K,V>)next.value);
                if (fromIndex == next.index) {
                    subItr = subMap.descendingIterator(from);
                    if (!subItr.hasNext()) {
                        subItr = null;
                        next();                    
                    }
                } else { // We can ignore from.
                    subItr = subMap.descendingIterator(); // subMap cannot be empty.
                }
            }    
        }

        @Override
        public boolean hasNext() {
            return (subItr != null) || (next != null);
        }

        @SuppressWarnings("unchecked")
        @Override
        public Entry<K,V> next() {
            if (subItr != null) {
                current = subItr.next();
                if (subItr.hasNext()) return current;
                subItr = null;
            } else {
                if (next == null) throw new NoSuchElementException();
                current = (Entry<K,V>) next;
            }
            // Moves to next node.
            next = (next.index != 0) ?
                    root.floorEntry((int) (MathLib.unsigned(next.index)-1)) : null;             
            if ((next != null) && (next.key == SUB_MAP)) {
                FastMap<K,V> subMap = ((FastMap<K,V>)next.value);
                subItr = subMap.descendingIterator();
            }
            return current;
        }
   
    }
       
    /**
     * Returns the minimal shift for two indices (based on common high-bits which can be masked).
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

    private static TrieNodeImpl<Object,Object> NULL = new TrieNodeImpl<Object,Object>() {
        private static final long serialVersionUID = 0x700L; // Version. 

        @Override
        public TrieNodeImpl<Object, Object> clone() {
            return null;
        }

        @Override
        public TrieNodeImpl<Object, Object> downsize(int indexRemoved) {
            throw new AssertionError();
        }

        @Override
        public EntryNode<Object, Object> getEntry(int index) {
            return null;
        }

        @SuppressWarnings("unchecked")
        @Override
        public EntryNode<Object, Object> entry(int index) {
            return (EntryNode<Object, Object>) UPSIZE;
        }

        @Override
        public EntryNode<Object, Object> ceilingEntry(int index) {
            return null;
        }

        @Override
        public EntryNode<Object, Object> floorEntry(int index) {
            return null;
        }

        @Override
        public EntryNode<Object, Object> removeEntry(int index) {
            return null;
        }

        @Override
        public TrieNodeImpl<Object, Object> upsize(int indexAdded) {
            return new EntryNode<Object,Object>(indexAdded);
        }
        
    };
 }
