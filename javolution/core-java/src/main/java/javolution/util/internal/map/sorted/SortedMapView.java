/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util.internal.map.sorted;

import java.util.Comparator;
import java.util.Map;

import javolution.util.internal.map.MapView;
import javolution.util.internal.set.sorted.SubSortedSetImpl;
import javolution.util.service.SortedMapService;
import javolution.util.service.SortedSetService;

/**
 * Sorted map view implementation; can be used as root class for implementations 
 * if target is {@code null}.
 * When possible sub-classes should forward to the actual target for the methods
 * isEmpty, size and clear rather than using the default implementation.
 */
public abstract class SortedMapView<K,V> extends MapView<K,V> implements SortedMapService<K,V> {

    /** Entry Set View */
    protected class EntrySortedSet extends EntrySet implements SortedSetService<Entry<K,V>> {
        private static final long serialVersionUID = SortedMapView.serialVersionUID;

        @Override
        public Entry<K, V> first() {
            K key = SortedMapView.this.firstKey();
            V value = SortedMapView.this.get(key);
            return new MapEntryImpl<K,V>(key, value);
        }

        @Override
        public SortedSetService<Entry<K, V>> headSet(Entry<K, V> toElement) {
            return new SubSortedSetImpl<Entry<K, V>>(this, null, toElement);
        }

        @Override
        public java.util.Map.Entry<K, V> last() {
            K key = SortedMapView.this.lastKey();
            V value = SortedMapView.this.get(key);
            return new MapEntryImpl<K,V>(key, value);
        }

        @SuppressWarnings("unchecked")
        @Override
        public SortedSetService<Entry<K, V>>[] split(int n, boolean updateable) { 
            return new SortedSetService[] { this }; // Split not supported.
        }

        @Override
        public SortedSetService<java.util.Map.Entry<K, V>> subSet(
               Entry<K, V> fromElement,
               Entry<K, V> toElement) {
            return new SubSortedSetImpl<Entry<K, V>>(this, fromElement, toElement);
        }     
     
        @Override
        public SortedSetService<Entry<K, V>> tailSet(Entry<K, V> fromElement) {
            return new SubSortedSetImpl<Entry<K, V>>(this, fromElement, null);
        }
     
    }
  
    /** Entry Key View */
    protected class KeySortedSet extends KeySet implements SortedSetService<K> {
        private static final long serialVersionUID = SortedMapView.serialVersionUID;

        @Override
        public K first() {
            return SortedMapView.this.firstKey();
        }

        @Override
        public SortedSetService<K> headSet(K toElement) {
            return new SubSortedSetImpl<K>(this, null, toElement);
        }

        @Override
        public K last() {
            return SortedMapView.this.lastKey();
        }

        @SuppressWarnings("unchecked")
        @Override
        public SortedSetService<K>[] split(int n, boolean updateable) { 
            return new SortedSetService[] { this }; // Split not supported.
        }

        @Override
        public SortedSetService<K> subSet(K fromElement, K toElement) {
            return new SubSortedSetImpl<K>(this, fromElement, toElement);
        }
  
        @Override
        public SortedSetService<K> tailSet(K fromElement) {
            return new SubSortedSetImpl<K>(this, fromElement, null);
        }
    }
    
    private static final long serialVersionUID = 0x600L; // Version.

    /**
     * The view constructor or root class constructor if target is {@code null}.
     */
    public SortedMapView(SortedMapService<K,V> target) {
        super(target);
    }
    
    @Override
    public Comparator<? super K> comparator() {
        return keyComparator();
    }

    @Override
    public SortedSetService<Map.Entry<K, V>> entrySet() {
        return new EntrySortedSet();
    }

    @Override
    public abstract K firstKey();

    @Override
    public SortedMapService<K, V> headMap(K toKey) {
        return new SubSortedMapImpl<K,V>(this, firstKey(), toKey);
    }

    @Override
    public SortedSetService<K> keySet() {
        return new KeySortedSet();
    }

    @Override
    public abstract K lastKey();

    @SuppressWarnings("unchecked")
    @Override
    public SortedMapService<K, V>[] split(int n, boolean updateable) { 
        return new SortedMapService[] { this }; // Split not supported.
    }

    @Override
    public SortedMapService<K, V> subMap(K fromKey, K toKey) {
        return new SubSortedMapImpl<K,V>(this, fromKey, toKey);
    }

    @Override
    public SortedMapService<K, V> tailMap(K fromKey) {
        return new SubSortedMapImpl<K,V>(this, fromKey, lastKey());
    }

    /** Returns the actual target */
    @Override
    protected SortedMapService<K,V> target() {
        return (SortedMapService<K,V>) super.target();
    }

}
