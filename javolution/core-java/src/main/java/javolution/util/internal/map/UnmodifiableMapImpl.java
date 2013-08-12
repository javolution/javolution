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

import javolution.util.service.MapService;

/**
 *  * An unmodifiable view over a map.
 */
public class UnmodifiableMapImpl<K, V> extends MapView<K, V> {

    private static final long serialVersionUID = 0x600L; // Version.

    public UnmodifiableMapImpl(MapService<K, V> target) {
        super(target);
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("Unmodifiable");
    }

    @Override
    public V put(K key, V value) {
        throw new UnsupportedOperationException("Unmodifiable");
    }

    @Override
    public V remove(Object key) {
        throw new UnsupportedOperationException("Unmodifiable");
    }

    /** Iterator over this map entries */
    protected Iterator<Entry<K, V>> iterator() {
        return new Iterator<Entry<K, V>>() {
            Iterator<Entry<K, V>> it = target().entrySet().iterator();

            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public Entry<K, V> next() {
                return it.next();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("Read-Only Map.");
            }
        };

    }
}
