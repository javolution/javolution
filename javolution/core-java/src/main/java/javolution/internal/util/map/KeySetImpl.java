/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.internal.util.map;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;

import javolution.lang.Predicate;
import javolution.util.service.CollectionService;

/**
 * The keys view over a map.
 */
public final class KeySetImpl<K, V> implements
        CollectionService<K>, Serializable {

    private final AbstractMapImpl<K, V> map;

    public KeySetImpl(AbstractMapImpl<K, V> map) {
        this.map = map;
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public boolean add(K key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean contains(K key) {
        return map.containsKey(key);
    }

    @Override
    public boolean remove(K key) {
        if (!map.containsKey(key)) return false;
        map.remove(key);
        return true;
    }

    @Override
    public void doWhile(Predicate<K> predicate) {
        for (Iterator<Map.Entry<K, V>> i = map.entriesIterator(); i.hasNext();) {
            if (!predicate.evaluate(i.next().getKey()))
                return;
        }
    }

    @Override
    public boolean removeAll(Predicate<K> predicate) {
        boolean hasChanged = false;
        for (Iterator<Map.Entry<K, V>> i = map.entriesIterator(); i.hasNext();) {
            if (predicate.evaluate(i.next().getKey())) {
                i.remove();
                hasChanged = true;
            }
        }
        return hasChanged;
    }

    @Override
    public Iterator<K> iterator() {
        return new Iterator<K>() {
            final Iterator<Map.Entry<K, V>> entriesIterator = map.entriesIterator();
            @Override
            public boolean hasNext() {
                return entriesIterator.hasNext();
            }

            @Override
            public K next() {
                return entriesIterator.next().getKey();
            }

            @Override
            public void remove() {
                entriesIterator.remove();                
            }
            
        };
    }

    private static final long serialVersionUID = -2146924466361188899L;
}
