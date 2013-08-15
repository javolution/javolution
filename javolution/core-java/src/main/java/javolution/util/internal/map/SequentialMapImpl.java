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

import javolution.util.function.Consumer;
import javolution.util.function.Equality;
import javolution.util.service.MapService;

/**
 * A sequential view over a map.
 */
public class SequentialMapImpl<K, V> extends MapView<K, V> {

    private static final long serialVersionUID = 0x600L; // Version.

    public SequentialMapImpl(MapService<K, V> target) {
        super(target);
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
    public Iterator<java.util.Map.Entry<K, V>> iterator() {
        return target().iterator();
    }

    @Override
    public Equality<? super K> keyComparator() {
        return target().keyComparator();
    }

    @Override
    public void perform(Consumer<MapService<K, V>> action, MapService<K, V> view) {
        action.accept(view); // Executes immediately.
    }

    @Override
    public V put(K key, V value) {
        return target().put(key, value);
    }

    @Override
    public V remove(Object key) {
        return target().remove(key);
    }

    @Override
    public void update(Consumer<MapService<K, V>> action, MapService<K, V> view) {
        action.accept(view); // Executes immediately.
    }

    @Override
    public Equality<? super V> valueComparator() {
        return target().valueComparator();
    }

}
