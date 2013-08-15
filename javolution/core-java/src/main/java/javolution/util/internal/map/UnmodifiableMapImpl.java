/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util.internal.map;

import java.util.Iterator;

import javolution.util.function.Equality;
import javolution.util.service.MapService;

/**
 *  * An unmodifiable view over a map.
 */
public class UnmodifiableMapImpl<K, V> extends MapView<K, V> {

    /** Read-Only Iterator. */
    private class IteratorImpl implements Iterator<Entry<K, V>> {
        private final Iterator<Entry<K, V>> targetIterator = target()
                .iterator();

        @Override
        public boolean hasNext() {
            return targetIterator.hasNext();
        }

        @Override
        public Entry<K, V> next() {
            return targetIterator.next();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Read-Only Map.");
        }
    }

    private static final long serialVersionUID = 0x600L; // Version.

    public UnmodifiableMapImpl(MapService<K, V> target) {
        super(target);
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("Unmodifiable");
    }

    @Override
    public boolean containsKey(Object key) {
        return target().containsKey(key);
    }

    @Override
    public V get(Object key) {
        return target().get(key);
    }

    @Override
    public Iterator<Entry<K, V>> iterator() {
        return new IteratorImpl();
    }

    @Override
    public Equality<? super K> keyComparator() {
        return target().keyComparator();
    }

    @Override
    public V put(K key, V value) {
        throw new UnsupportedOperationException("Unmodifiable");
    }

    @Override
    public V remove(Object key) {
        throw new UnsupportedOperationException("Unmodifiable");
    }

    @Override
    public MapService<K, V> threadSafe() {
        return this;
    }

    @Override
    public Equality<? super V> valueComparator() {
        return target().valueComparator();
    }

}
