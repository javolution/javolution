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

import javolution.util.function.Consumer;
import javolution.util.function.EqualityComparator;
import javolution.util.function.Predicate;
import javolution.util.service.CollectionService;

/**
 * The values view over a map.
 */
public final class ValuesImpl<K, V> implements CollectionService<V>,
        Serializable {

    private static final long serialVersionUID = 0x600L; // Version.
    private final FastMapImpl<K, V> map;

    public ValuesImpl(FastMapImpl<K, V> map) {
        this.map = map;
    }

    @Override
    public boolean add(V element) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void atomic(Runnable action) {
        // TODO Auto-generated method stub

    }

    @Override
    public EqualityComparator<? super V> comparator() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void forEach(
            Consumer<? super V> consumer,
            javolution.util.service.CollectionService.IterationController controller) {
        // TODO Auto-generated method stub

    }

    @Override
    public Iterator<V> iterator() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean removeIf(
            Predicate<? super V> filter,
            javolution.util.service.CollectionService.IterationController controller) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public CollectionService<V>[] trySplit(int n) {
        // TODO Auto-generated method stub
        return null;
    }
}
