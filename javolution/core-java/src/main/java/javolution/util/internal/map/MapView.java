/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util.internal.map;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;

import javolution.util.function.Consumer;
import javolution.util.function.Equality;
import javolution.util.function.Function;
import javolution.util.internal.collection.MappedCollectionImpl;
import javolution.util.internal.set.MappedSetImpl;
import javolution.util.internal.set.SetView;
import javolution.util.service.CollectionService;
import javolution.util.service.MapService;
import javolution.util.service.SetService;

/**
 * Map view implementation; can be used as root class for implementations 
 * if target is {@code null}.
 * When possible sub-classes should forward to the actual target for the methods
 * isEmpty, size and clear rather than using the default implementation.
 */
public abstract class MapView<K, V> implements MapService<K, V> {

    /**
     * Entry comparator. Entries are considered equals if they have the same 
     * keys regardless of their associated values.
     */
    protected class EntryComparator implements Equality<Entry<K,V>>, Serializable {
        private static final long serialVersionUID = MapView.serialVersionUID;

        public EntryComparator() {
        }

        @Override
        public boolean areEqual(Entry<K, V> left, Entry<K, V> right) {
            return keyComparator().areEqual(left.getKey(),
                    right.getKey());
        }

        @Override
        public int compare(Entry<K, V> left, Entry<K, V> right) {
            return keyComparator().compare(left.getKey(),
                    right.getKey());
        }

        @Override
        public int hashCodeOf(Entry<K, V> e) {
            return keyComparator().hashCodeOf(e.getKey());
        }     
    }
    
    /** Entry Set View */
    protected class EntrySet extends SetView<Entry<K, V>> {
        private static final long serialVersionUID = MapView.serialVersionUID;
        public EntrySet() {
            super(null); // Actual target is the outer map. 
        }

        @Override
        public boolean add(Entry<K, V> entry) {
            put(entry.getKey(), entry.getValue());
            return true;
        }

        @Override
        public Equality<? super Entry<K, V>> comparator() {
            return new EntryComparator();
        }

        @SuppressWarnings("unchecked")
        @Override
        public boolean contains(Object obj) {
            if (obj instanceof Entry) {
                Entry<K, V> e = (Entry<K, V>) obj;
                return contains(e.getKey());
            }
            return false;
        }

        @Override
        public boolean isEmpty() {
            return MapView.this.isEmpty();
        }

        @Override
        public Iterator<Entry<K, V>> iterator() {
            return MapView.this.iterator();
        }

        @Override
        public void perform(
                final Consumer<CollectionService<Entry<K, V>>> action,
                final CollectionService<Entry<K, V>> view) {
            Consumer<MapService<K, V>> mapAction = new Consumer<MapService<K, V>>() {
                @Override
                public void accept(MapService<K, V> param) {
                    action.accept(view);
                }
            };
            MapView.this.perform(mapAction, MapView.this);
        }

        @Override
        @SuppressWarnings("unchecked")
        public boolean remove(Object obj) {
            if (obj instanceof Entry) {
                Entry<K, V> e = (Entry<K, V>) obj;
                if (!contains(e.getKey())) return false;
                MapView.this.remove(e.getKey());
                return true;
            }
            return false;
        }

        @Override
        public int size() {
            return MapView.this.size();
        }

        @Override
        public void update(
                final Consumer<CollectionService<Entry<K, V>>> action,
                final CollectionService<Entry<K, V>> view) {
            Consumer<MapService<K, V>> mapAction = new Consumer<MapService<K, V>>() {
                @Override
                public void accept(MapService<K, V> param) {
                    action.accept(view);
                }
            };
            MapView.this.update(mapAction, MapView.this);
        }
    }

    /** Entry to key mapping function */
    class EntryToKey implements Function<Entry<K, V>, K>, Serializable {
        private static final long serialVersionUID = MapView.serialVersionUID;
        @Override
        public K apply(java.util.Map.Entry<K, V> param) {
            return param.getKey();
        }        
    }
    
    /** Key Set View */
    protected class KeySet extends MappedSetImpl<Entry<K, V>, K> {
        private static final long serialVersionUID = MapView.serialVersionUID;

        public KeySet() {
            super(entrySet(), new EntryToKey());
        }

        @Override
        public boolean add(K key) { // Supports adding new key with null value.
            if (containsKey(key)) return false;
            put(key, null);
            return true;
        }

        @Override
        public Equality<? super K> comparator() {
            return keyComparator();
        }

        @SuppressWarnings("unchecked")
        @Override
        public boolean contains(Object obj) {
            return containsKey((K) obj);
        }

        @SuppressWarnings("unchecked")
        @Override
        public boolean remove(Object obj) {
            if (!containsKey((K) obj)) return false;
            MapView.this.remove((K) obj);
            return true;
        }
    }

    /** Values View */
    protected class Values extends MappedCollectionImpl<Entry<K, V>, V> {
        private static final long serialVersionUID = MapView.serialVersionUID;

        public Values() {
            super(entrySet(), new Function<Entry<K, V>, V>() {
                @Override
                public V apply(Map.Entry<K, V> e) {
                    return e.getValue();
                }
            });
        }

        @Override
        public Equality<? super V> comparator() {
            return valueComparator();
        }
    }

    private static final long serialVersionUID = 0x600L; // Version.
    private MapService<K, V> target;

    /**
     * The view constructor or root class constructor if target is {@code null}.
     */
    public MapView(MapService<K, V> target) {
        this.target = target;
    }

    @Override
    public void clear() {
        Iterator<Entry<K, V>> it = iterator();
        while (it.hasNext()) {
            it.remove();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public MapView<K, V> clone() {
        try {
            MapView<K, V> copy = (MapView<K, V>) super.clone();
            if (target != null) { // Not a root class.
                copy.target = target.clone();
            }
            return copy;
        } catch (CloneNotSupportedException e) {
            throw new Error("Should not happen since target is cloneable");
        }
    }

    @Override
    public abstract boolean containsKey(Object key);

    @Override
    public boolean containsValue(Object value) {
        return values().contains(value);
    }

    @Override
    public SetService<Entry<K, V>> entrySet() {
        return new EntrySet();
    }

    @Override
    public abstract V get(Object key);

    @Override
    public boolean isEmpty() {
        return !iterator().hasNext();
    }

    @Override
    public abstract Iterator<Entry<K, V>> iterator();

    @Override
    public abstract Equality<? super K> keyComparator();

    @Override
    public SetService<K> keySet() {
        return new KeySet();
    }

    @Override
    public void perform(Consumer<MapService<K, V>> action, MapService<K, V> view) {
        if (target == null) {
            action.accept(view);
        } else {
            target.perform(action, view);
        }
    }

    @Override
    public abstract V put(K key, V value);

    @SuppressWarnings("unchecked")
    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        Iterator<?> it = m.entrySet().iterator();
        while (it.hasNext()) {
            Entry<K, V> e = (Entry<K, V>) it.next();
            put(e.getKey(), e.getValue());
        }
    }

    @Override
    public V putIfAbsent(K key, V value) {
        if (!containsKey(key)) return put(key, value);
        else return get(key);
    }

    @Override
    public abstract V remove(Object key);

    @Override
    public boolean remove(Object key, Object value) {
        if (containsKey(key) && get(key).equals(value)) {
            remove(key);
            return true;
        } else return false;
    }

    @Override
    public V replace(K key, V value) {
        if (containsKey(key)) {
            return put(key, value);
        } else return null;
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        if (containsKey(key) && get(key).equals(oldValue)) {
            put(key, newValue);
            return true;
        } else return false;
    }

    @Override
    public int size() {
        int count = 0;
        Iterator<Entry<K, V>> it = iterator();
        while (it.hasNext()) {
            count++;
            it.next();
        }
        return count;
    }

    @SuppressWarnings("unchecked")
    @Override
    public MapService<K, V>[] split(int n, boolean threadsafe) {
        return new MapService[] { this }; // Splits not supported.
    }

    @Override
    public void update(Consumer<MapService<K, V>> action, MapService<K, V> view) {
        if (target == null) {
            action.accept(view);
        } else {
            target.update(action, view);
        }
    }

    @Override
    public abstract Equality<? super V> valueComparator();

    @Override
    public CollectionService<V> values() {
        return new Values();
    }

    /** Returns the actual target */
    protected MapService<K, V> target() {
        return target;
    }

}
