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

import javolution.util.FastComparator;
import javolution.util.function.Predicate;
import javolution.util.service.CollectionService;

/**
 * The entries view over a map.
 */
public final class EntrySetImpl<K, V> implements
        CollectionService<Map.Entry<K, V>>, Serializable {

    private final AbstractMapImpl<K, V> map;

    public EntrySetImpl(AbstractMapImpl<K, V> map) {
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
    public boolean add(Map.Entry<K, V> entry) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean contains(Map.Entry<K, V> entry) {
        V value = map.get(entry.getKey());
        return FastComparator.DEFAULT.areEqual(entry.getValue(), value);
    }

    @Override
    public boolean remove(Map.Entry<K, V> entry) {
        return map.remove(entry.getKey(), entry.getValue());
    }

    @Override
    public void doWhile(Predicate<Map.Entry<K, V>> predicate) {
        for (Iterator<Map.Entry<K, V>> i = map.entriesIterator(); i.hasNext();) {
            if (!predicate.apply(i.next()))
                return;
        }
    }

    @Override
    public boolean removeAll(Predicate<Map.Entry<K, V>> predicate) {
        boolean hasChanged = false;
        for (Iterator<Map.Entry<K, V>> i = map.entriesIterator(); i.hasNext();) {
            if (predicate.apply(i.next())) {
                i.remove();
                hasChanged = true;
            }
        }
        return hasChanged;
    }

    @Override
    public Iterator<Map.Entry<K, V>> iterator() {
        return map.entriesIterator();
    }

    private static final long serialVersionUID = 6545160313862150259L;
}
