/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.util.internal.map;

import java.util.Iterator;

import org.javolution.util.FastCollection;
import org.javolution.util.FastMap;
import org.javolution.util.FastMap.Entry;
import org.javolution.util.function.Equality;
import org.javolution.util.function.Predicate;

/**
 * A collection view over the map values.
 */
public final class ValuesImpl<K, V> extends FastCollection<V> {

	/** Then generic iterator over the map values */
    private static class ValuesIterator<K, V> implements Iterator<V> {
        final Iterator<Entry<K,V>> mapItr;

        public ValuesIterator(Iterator<Entry<K,V>> mapItr) {
            this.mapItr = mapItr;
        }

        @Override
        public boolean hasNext() {
            return mapItr.hasNext();
        }

        @Override
        public V next() {
            return mapItr.next().getValue();
        }
        
        @Override
        public void remove() {
            mapItr.remove();
        }        
        
    }
	private static final long serialVersionUID = 0x700L; // Version.
	private final FastMap<K, V> map;

	public ValuesImpl(FastMap<K, V> map) {
		this.map = map;
	}

	@Override
	public boolean add(V element) {
		throw new UnsupportedOperationException(
				"Values cannot be added directly to maps");
	}

	@Override
	public void clear() {
		map.clear();
	}

	@Override
	public FastCollection<V> clone() {
		return new ValuesImpl<K, V>(map.clone());
	}

	@Override
	public Equality<? super V> equality() {
		return map.valuesEquality();
	}

	@Override
	public Iterator<V> iterator() {
		return new ValuesIterator<K, V>(map.iterator());
	}

	@Override
	public int size() {
		return map.size();
	}

    @Override
    public boolean removeIf(final Predicate<? super V> filter) {
        
        return map.entrySet().removeIf(new Predicate<java.util.Map.Entry<K,V>>() {

            @Override
            public boolean test(java.util.Map.Entry<K, V> param) {
                return filter.test(param.getValue());
            }});
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public FastCollection<V>[] trySplit(int n) {
        FastMap<K,V>[] maps = map.trySplit(n);
        @SuppressWarnings("unchecked")
        FastCollection<V>[] split = new FastCollection[n];
        for (int i=0; i < n; i++) split[i] = new ValuesImpl<K,V>(maps[i]);
        return split;
    }

}
