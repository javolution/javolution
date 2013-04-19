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
import javolution.util.FastComparator;
import javolution.util.service.CollectionService;
import javolution.util.service.MapService;

/**
 * An unmodifiable view over a table.
 */
public final class EntrySetImpl<K,V> implements
        CollectionService<Map.Entry<K,V>>, Serializable {

    private final MapService<K,V> map;

    public EntrySetImpl(MapService<K,V> map) {
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
    public boolean add(Map.Entry<K,V> entry) {
        V previous = map.put(entry.getKey(), entry.getValue());
        return !FastComparator.DEFAULT.areEqual(entry.getValue(), previous);
    }

    @Override
    public boolean contains(Map.Entry<K,V> entry) {
        V value = map.get(entry.getKey());
        return FastComparator.DEFAULT.areEqual(entry.getValue(), value);
    }

    @Override
    public boolean remove(Map.Entry<K,V> entry) {
       
    }

    @Override
    public void doWhile(Predicate<E> predicate) {
     ;
    }

    @Override
    public boolean removeAll(Predicate<E> predicate) {
        throw new UnsupportedOperationException("Unmodifiable");
    }

    @Override
    public Iterator<E> iterator() {
        final Iterator<E> thatIterator = that.iterator();
        return new Iterator<E>() {

            @Override
            public boolean hasNext() {
                return thatIterator.hasNext();
            }

            @Override
            public E next() {
                return thatIterator.next();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("Unmodifiable");
            }

        };
    }

    private static final long serialVersionUID = 6545160313862150259L;
}
