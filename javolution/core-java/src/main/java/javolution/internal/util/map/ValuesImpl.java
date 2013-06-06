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

import javolution.util.Comparators;
import javolution.util.function.Predicate;
import javolution.util.service.CollectionService;

/**
 * The values view over a map.
 */
public final class ValuesImpl<K, V> implements
        CollectionService<V>, Serializable {

    private final AbstractMapImpl<K, V> map;

    public ValuesImpl(AbstractMapImpl<K, V> map) {
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
    public boolean add(V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean contains(V value) {
        for (Iterator<V> i = iterator(); i.hasNext();) {
            if (Comparators.DEFAULT.areEqual(i.next(), value)) return true;
        }
        return false;
    }

    @Override
    public boolean remove(V value) {
        for (Iterator<V> i = iterator(); i.hasNext();) {
            if (Comparators.DEFAULT.areEqual(i.next(), value)) {
                i.remove();
                return true;
            }
        }
        return false;
    }

    @Override
    public void doWhile(Predicate<V> predicate) {
        for (Iterator<Map.Entry<K, V>> i = map.entriesIterator(); i.hasNext();) {
            if (!predicate.apply(i.next().getValue()))
                return;
        }
    }

    @Override
    public boolean removeIf(Predicate<V> predicate) {
        boolean hasChanged = false;
        for (Iterator<Map.Entry<K, V>> i = map.entriesIterator(); i.hasNext();) {
            if (predicate.apply(i.next().getValue())) {
                i.remove();
                hasChanged = true;
            }
        }
        return hasChanged;
    }

    @Override
    public Iterator<V> iterator() {
        return new Iterator<V>() {
            final Iterator<Map.Entry<K, V>> entriesIterator = map.entriesIterator();
            @Override
            public boolean hasNext() {
                return entriesIterator.hasNext();
            }

            @Override
            public V next() {
                return entriesIterator.next().getValue();
            }

            @Override
            public void remove() {
                entriesIterator.remove();                
            }
            
        };
    }

    private static final long serialVersionUID = 7879330225511200690L;
}
