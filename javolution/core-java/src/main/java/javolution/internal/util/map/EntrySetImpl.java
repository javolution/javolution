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
import java.util.Map.Entry;

import javolution.util.function.Consumer;
import javolution.util.function.EqualityComparator;
import javolution.util.function.Predicate;
import javolution.util.service.CollectionService;
import javolution.util.service.SetService;

/**
 * The entries view over a map.
 */
public final class EntrySetImpl<K, V> implements SetService<Map.Entry<K, V>>,
        Serializable {

    private static final long serialVersionUID = 0x600L; // Version.
    private final FastMapImpl<K, V> map;

    public EntrySetImpl(FastMapImpl<K, V> map) {
        this.map = map;
    }

    @Override
    public boolean add(Entry<K, V> element) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void atomic(Runnable action) {
        // TODO Auto-generated method stub

    }

    @Override
    public void clear() {
        // TODO Auto-generated method stub

    }

    @Override
    public EqualityComparator<? super Entry<K, V>> comparator() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean contains(Entry<K, V> e) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void forEach(
            Consumer<? super Entry<K, V>> consumer,
            javolution.util.service.CollectionService.IterationController controller) {
        // TODO Auto-generated method stub

    }

    @Override
    public Iterator<Entry<K, V>> iterator() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean remove(Entry<K, V> e) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean removeIf(
            Predicate<? super Entry<K, V>> filter,
            javolution.util.service.CollectionService.IterationController controller) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public int size() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public CollectionService<Entry<K, V>>[] trySplit(int n) {
        // TODO Auto-generated method stub
        return null;
    }
}
