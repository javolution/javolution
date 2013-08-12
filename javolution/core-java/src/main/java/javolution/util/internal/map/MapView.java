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
import java.util.Map;

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
 * if target is {@code null}, then of course methods calling target have to be
 * overridden.
 * For efficiency sub-classes should forward to the actual target for the methods
 * clear, remove, contains, size and isEmpty rather than to use their default 
 * implementation.
 */
public class MapView<K, V> implements MapService<K, V> {

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
        target.clear();
    }

    @Override
    public boolean containsKey(Object key) {
        return target.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return target.containsValue(value);
    }

    @SuppressWarnings("serial")
    @Override
    public SetService<Entry<K, V>> entrySet() {
        return new SetView<Entry<K, V>>(null) {

            @Override
            public boolean add(Entry<K, V> entry) {
                put(entry.getKey(), entry.getValue());
                return true;
            }

            @Override
            public Equality<? super Entry<K, V>> comparator() {
                return new Equality<Entry<K, V>>() {
                    // Entries are considered equals if they have the same 
                    // keys regardless of their associated values.

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
                };
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

            @SuppressWarnings("unchecked")
            @Override
            public SetView<Entry<K, V>>[] subViews(int n) {
                MapService<K, V>[] subMaps = MapView.this.subViews(n);
                SetView<Entry<K, V>>[] result = new SetView[subMaps.length];
                for (int i = 0; i < result.length; i++) {
                    result[i] = (SetView<Entry<K, V>>) subMaps[i].entrySet();
                }
                return result;
            }

        };
    }

    @Override
    public V get(Object key) {
        return target.get(key);
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public Equality<? super K> keyComparator() {
        return target.keyComparator();
    }

    @SuppressWarnings("serial")
    @Override
    public SetService<K> keySet() {
        Function<Entry<K, V>, K> mapping = new Function<Entry<K, V>, K>() {
            @Override
            public K apply(Map.Entry<K, V> e) {
                return e.getKey();
            }
        };
        SetService<Entry<K, V>> entries = entrySet();
        return new MappedSetImpl<Entry<K, V>, K>(entries, mapping) {
         
            @Override
            public Equality<? super K> comparator() {
                return keyComparator();
            }

            @Override
            public boolean add(K key) { // Supports adding new key with null value.
                if (containsKey(key)) return false;
                put(key, null);
                return true;
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
        };
    }

    @Override
    public V put(K key, V value) {
        return target.put(key, value);
    }

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
    public V remove(Object key) {
        return target.remove(key);
    }

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
        return target.size();
    }

    @SuppressWarnings("unchecked")
    @Override
    public MapService<K, V>[] subViews(int n) {
        MapService<K, V>[] tmp = target.subViews(n);
        MapView<K, V>[] result = new MapView[tmp.length];
        for (int i = 0; i < tmp.length; i++) {
            result[i] = this.clone();
            result[i].target = tmp[i];
        }
        return result;
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
    public Equality<? super V> valueComparator() {
        return target.valueComparator();
    }

    @SuppressWarnings("serial")
    @Override
    public CollectionService<V> values() {
        Function<Entry<K, V>, V> mapping = new Function<Entry<K, V>, V>() {
            @Override
            public V apply(Map.Entry<K, V> e) {
                return e.getValue();
            }
        };
        return new MappedCollectionImpl<Entry<K, V>, V>(entrySet(), mapping) {
            @Override
            public Equality<? super V> comparator() {
                return valueComparator();
            }
        };
    }

    /** Iterator over this map entries */
    protected Iterator<Entry<K, V>> iterator() {
        return (target instanceof MapView) ? ((MapView<K, V>) target)
                .iterator() : target.entrySet().iterator();
    }

    /** Returns the actual target */
    protected MapService<K, V> target() {
        return target;
    }

    /** Returns a clone copy of target. */
    protected MapService<K,V> cloneTarget() {
        try {
            return target.clone();
        } catch (CloneNotSupportedException e) {
            throw new Error("Cannot happen since target is Cloneable.");
        }
    }
}
