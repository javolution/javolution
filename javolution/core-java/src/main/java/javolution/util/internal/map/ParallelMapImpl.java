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

import javolution.context.ConcurrentContext;
import javolution.util.function.Consumer;
import javolution.util.function.Equality;
import javolution.util.service.MapService;

/**
 * A parallel view over a map. 
 */
public class ParallelMapImpl<K, V> extends MapView<K, V> {

    private static final long serialVersionUID = 0x600L; // Version.

    public ParallelMapImpl(MapService<K, V> target) {
        super(target);
    }

    @Override
    public void clear() {
        target().clear();
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
    public boolean isEmpty() {
        return target().isEmpty();
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
    public void perform(final Consumer<MapService<K, V>> action,
            MapService<K, V> view) {
        ConcurrentContext ctx = ConcurrentContext.enter();
        try {
            int concurrency = ctx.getConcurrency();
            MapService<K, V>[] subViews = view.split(concurrency + 1, false);
            for (int i = 1; i < subViews.length; i++) {
                final MapService<K, V> subView = subViews[i];
                ctx.execute(new Runnable() {
                    @Override
                    public void run() {
                        target().perform(action, subView);
                    }
                });
            }
            target().perform(action, subViews[0]); // This thread works too !
        } finally {
            // Any exception raised during parallel iterations will be re-raised here.                       
            ctx.exit();
        }
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
    public int size() {
        return target().size();
    }

    @Override
    public void update(final Consumer<MapService<K, V>> action,
            MapService<K, V> view) {
        ConcurrentContext ctx = ConcurrentContext.enter();
        try {
            int concurrency = ctx.getConcurrency();
            MapService<K, V>[] subViews = view.split(concurrency + 1, true);
            for (int i = 1; i < subViews.length; i++) {
                final MapService<K, V> subView = subViews[i];
                ctx.execute(new Runnable() {
                    @Override
                    public void run() {
                        target().update(action, subView);
                    }
                });
            }
            target().perform(action, subViews[0]); // This thread works too !
        } finally {
            // Any exception raised during parallel iterations will be re-raised here.                       
            ctx.exit();
        }
    }

    @Override
    public Equality<? super V> valueComparator() {
        return target().valueComparator();
    }

    @Override
    public MapService<K, V>[] split(int n, boolean threadsafe) {
        return target().split(n, threadsafe); // Forwards.
    }

}
