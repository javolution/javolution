/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util.internal.map.sorted;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Iterator;

import javolution.util.function.Equality;
import javolution.util.internal.table.sorted.FastSortedTableImpl;

/**
 * A map view over a sorted table of entries.
 */
public class FastSortedMapImpl<K, V> extends SortedMapView<K,V> {
     
    private static final long serialVersionUID = 0x600L; // Version.
    private final FastSortedTableImpl<Entry<K,V>> table;
    private final Equality<? super K> keyComparator;
    private final Equality<? super V> valueComparator;
    
    private class TableComparator implements Comparator<Object>, Serializable {
    	private static final long serialVersionUID = FastSortedMapImpl.serialVersionUID;
    	  
		@SuppressWarnings("unchecked")
		@Override
		public int compare(Object o1, Object o2) {
			if (o1 instanceof MapEntryImpl) {
				o1 = ((MapEntryImpl<K,V>)o1).getKey();
			}
			if (o2 instanceof MapEntryImpl) {
				o2 = ((MapEntryImpl<K,V>)o2).getKey();
			}
			return keyComparator.compare((K)o1, (K)o2);
		}    	
    }
    
    
    public FastSortedMapImpl(Equality<? super K> keyComparator,
            Equality<? super V> valueComparator) {
        super(null);
        this.keyComparator = keyComparator;
        this.valueComparator = valueComparator;
        table = new FastSortedTableImpl<Entry<K,V>>(new TableComparator());
    }

    @Override
    public void clear() {
        table.clear();
    }

	@Override
    public boolean containsKey(Object key) {
    	return table.indexOf(key) >= 0;
    }
    
	@Override
    public K firstKey() {
        return table.getFirst().getKey();
    }

	@Override
    public V get(Object key) {
        int i = table.indexOf(key);
        if (i < 0) return null;
        return table.get(i).getValue();
    }

    @Override
    public boolean isEmpty() {
        return table.isEmpty();
    }

	@Override
    public Iterator<Entry<K, V>> iterator() {
        return table.iterator();
    }

    @Override
    public Equality<? super K> keyComparator() {
        return keyComparator;
    }

	@Override
    public K lastKey() {
        return table.getLast().getKey();
     }

    @SuppressWarnings("unchecked")
	@Override
    public V put(K key, V value) {
        int i = ((FastSortedTableImpl<K>)table).positionOf(key);
        if (i >= 0) { // Entry found.
        	 Entry<K,V> entry = table.get(i);
        	 V previous = entry.getValue();
        	 entry.setValue(value);
        	 return previous;
        }
    	MapEntryImpl<K,V> entry = new MapEntryImpl<K,V>(key, value);
    	table.add(-i-1, entry);
    	return null;
    }

    @Override
    public V remove(Object key) {
        int i = table.indexOf(key);
        if (i < 0) return null;
        Entry<K,V> e = table.get(i);
        V previous = e.getValue();
        table.remove(i);
        return previous;
    }

    @Override
    public int size() {
        return table.size();
    }

    @Override
    public Equality<? super V> valueComparator() {
        return valueComparator;
    }

}
