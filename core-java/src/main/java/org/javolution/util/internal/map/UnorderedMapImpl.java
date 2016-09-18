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
import java.util.NoSuchElementException;

import org.javolution.util.FastMap;
import org.javolution.util.FastTable;
import org.javolution.util.function.Equality;
import org.javolution.util.function.Order;

/**
 * An unordered map .
 */
public final class UnorderedMapImpl<K, V> extends FastMap<K, V> {

	/**
	 * The entry implementation.
	 */
	private static final class EntryImpl<K, V> implements Entry<K, V>, Serializable {
		private static final long serialVersionUID = 0x700L; // Version.
		public K key;
		public V value;

		public EntryImpl(K key, V value) {
			this.key = key;
			this.value = value;
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
		public K getKey() {
			return key;
		}

		@Override
		public V getValue() {
			return value;
		}

		@Override
		public int hashCode() { // As per Map.Entry contract.
			return Order.DEFAULT.indexOf(key) ^ Order.DEFAULT.indexOf(value);
		}

		@Override
		public V setValue(V newValue) {
			throw new UnsupportedOperationException();
		}

        public V setValueByPass(V newValue) {
            V oldValue = value;
            this.value = newValue;
            return oldValue;
        }

        @Override
		public String toString() {
			return "(" + key + '=' + value + ')'; // For debug.
		}
	}
	private static final long serialVersionUID = 0x700L; // Version.
	private final FastTable<Entry<K, V>> entries = FastTable.newTable();
	private final Equality<? super K> keyEquality;

	public UnorderedMapImpl(Equality<? super K> keyEquality) {
	    this.keyEquality = keyEquality;
	}

    @Override
    public int size() {
        return entries.size();
    }

    @Override
    public boolean isEmpty() {
        return entries.isEmpty();
    }

    @Override
    public V put(K key, V value) {
        EntryImpl<K,V> entry = getEntry(key);
        if (entry != null) return entry.setValueByPass(value);
        entry = new EntryImpl<K,V>(key, value);
        entries.addFirst(entry);
        return null;
    }
    
    /** Returns the entry with the specified key.*/
    public EntryImpl<K,V> getEntry(K key) {
        int i = indexOf(key);
        if (i < 0) return null;
        return (EntryImpl<K,V>) entries.get(i);
    }
    
    @Override
    public Iterator<Entry<K, V>> iterator() {
        return (Iterator<Entry<K, V>>) entries.iterator();
    }

    @Override
    public Iterator<Entry<K, V>> descendingIterator() {
        return entries.descendingIterator();
    }

    @Override
    public Iterator<Entry<K, V>> iterator(K fromKey) {
        int i = indexOf(fromKey);
        if (i < 0) throw new NoSuchElementException(fromKey + ": not found");
        return entries.listIterator(i); 
    }

    @Override
    public Iterator<Entry<K, V>> descendingIterator(K fromKey) {
        int i = indexOf(fromKey);
        if (i < 0) throw new NoSuchElementException(fromKey + ": not found");
        return entries.subTable(0, i+1).descendingIterator(); 
    }

    @Override
    public Entry<K, V> removeEntry(K key) {
        int i = indexOf(key);
        return i >= 0 ? entries.remove(i) : null;
    }

    @Override
    public Equality<? super V> valuesEquality() {
        return null;
    }

    @Override
    public UnorderedMapImpl<K,V> clone() {
        UnorderedMapImpl<K,V> copy = new UnorderedMapImpl<K,V>(keyEquality);
        copy.entries.addAll(entries);
        return copy;
    }

    @Override
    public Order<? super K> comparator() {
        return null;
    }

    /** Returns the index for the specified key.*/
    private int indexOf(K key) {
        for (int i=0, n=entries.size(); i < n; i++) 
            if (keyEquality.areEqual(entries.get(i).getKey(), key)) return i;                  
        return -1;
    }
}
