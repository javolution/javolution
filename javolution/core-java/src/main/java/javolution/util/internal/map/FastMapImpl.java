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
import java.util.NoSuchElementException;

import javolution.util.function.Equality;
import javolution.util.service.MapService;

/**
 * The default {@link javolution.util.FastMap FastMap} implementation 
 * based on {@link FractalMapImpl fractal maps}. 
 * This implementation ensures that no more than 3/4 of the map capacity is
 * ever wasted.
 */
public class FastMapImpl<K, V> extends MapView<K, V> {

    private static final long serialVersionUID = 0x600L; // Version.
    transient FastMapEntryImpl<K,V> firstEntry = null; 
    transient FractalMapImpl fractal = new FractalMapImpl(); 
    transient FastMapEntryImpl<K,V> freeEntry = new FastMapEntryImpl<K,V>();
    final Equality<? super K> keyComparator;
    transient FastMapEntryImpl<K,V> lastEntry = null;
    transient int size;
    final Equality<? super V> valueComparator;
    
    public FastMapImpl(Equality<? super K> keyComparator, 
            final Equality<? super V> valueComparator) {
        super(null); // Root.
        this.keyComparator = keyComparator;
        this.valueComparator = valueComparator;
    }

    @Override
    public void clear() {
        firstEntry = null;
        lastEntry = null;
        fractal = new FractalMapImpl();
        size = 0;
    }

    @Override
    public FastMapImpl<K,V> clone() { // Makes a copy.
        FastMapImpl<K,V> copy = new FastMapImpl<K,V>(keyComparator(), valueComparator());
        copy.putAll(this);
        return copy;
    }
 
    @SuppressWarnings("unchecked")
    @Override
    public boolean containsKey(Object key) {        
        return fractal.getEntry(key, keyComparator.hashCodeOf((K)key)) != null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public V get(Object key) {
        FastMapEntryImpl<K,V> entry = fractal.getEntry(key, keyComparator.hashCodeOf((K)key));
        if (entry == null) return null;
        return entry.value;
    }
    
    @Override
    public Equality<? super K> keyComparator() {
        return keyComparator;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public V put(K key, V value) {
        int hash = keyComparator.hashCodeOf(key);
        FastMapEntryImpl<K,V> tmp = fractal.addEntry(freeEntry, key, hash);
        if (tmp == freeEntry) { // New entry.
            freeEntry = new FastMapEntryImpl<K,V>();
            attachEntry(tmp);
            size++;
            tmp.hash = hash;
            tmp.key = key;
            tmp.value = value;
            return null;
        } else { // Existing entry.
            V oldValue = (V) tmp.value;
            tmp.value = value;
            return oldValue;
        }
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public V putIfAbsent(K key, V value) {
        int hash = keyComparator.hashCodeOf(key);
        FastMapEntryImpl<K,V> tmp = fractal.addEntry(freeEntry, key, hash);
        if (tmp == freeEntry) { // New entry.
            freeEntry = new FastMapEntryImpl<K,V>();
            attachEntry(tmp);
            size++;
            tmp.hash = hash;
            tmp.key = key;
            tmp.value = value;
            return null;
        } else { // Existing entry.
            return (V) tmp.value;
        }
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public V remove(Object key) {
        FastMapEntryImpl<K,V> entry = fractal.removeEntry(key, keyComparator.hashCodeOf((K)key));
        if (entry == null) return null;
        detachEntry(entry); // Entry is not referenced anymore and will be gc.
        size--;
        return entry.value;  
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean remove(Object key, Object value) {
        int hash = keyComparator.hashCodeOf((K)key);
        FastMapEntryImpl<K,V> entry = fractal.getEntry(key, hash);
        if (entry == null) return false;
        if (!valueComparator.areEqual((V)entry.value, (V)value)) return false;
        fractal.removeEntry(key, hash);
        detachEntry(entry); // Entry is not referenced anymore and will be gc.
        size--;
        return true;  
    }

    @SuppressWarnings("unchecked")
    @Override
    public V replace(K key, V value) {
        FastMapEntryImpl<K,V> entry = fractal.getEntry(key, keyComparator.hashCodeOf(key));
        if (entry == null) return null;
        V oldValue = entry.value;
        entry.value = value;
        return oldValue;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        FastMapEntryImpl<K,V> entry = fractal.getEntry(key, keyComparator.hashCodeOf(key));
        if (entry == null) return false;
        if (!valueComparator.areEqual(entry.value, oldValue)) return false;
        entry.value = newValue;
        return true;
    }

    @Override
    public int size() {
        return size;
    }

    @SuppressWarnings("unchecked")
    @Override
    public MapService<K, V>[] subViews(int n) {
        return new MapService[] { this };  // No splitting supported yet.
    }

    @Override
    public Equality<? super V> valueComparator() {
        return valueComparator;
    }
    
    /** Iterator over this map entries */
    protected Iterator<Entry<K, V>> iterator() {
        return new Iterator<Entry<K,V>>() {
            FastMapEntryImpl<K,V> current; 
            FastMapEntryImpl<K,V> next = firstEntry; 
                   
                    
            @Override
            public boolean hasNext() {
                return (next != null);
            }

            @Override
            public java.util.Map.Entry<K, V> next() {
                if (next == null) throw new NoSuchElementException();
                current = next;
                next = next.next;
                return current;
            }

            @Override
            public void remove() {
                if (current == null) throw new IllegalStateException();
                fractal.removeEntry(current.key, current.hash);
                detachEntry(current); // Entry is not referenced anymore and will be gc.
                size--;
            }};
       
    }

    private void attachEntry(FastMapEntryImpl<K,V> entry) {
        if (lastEntry != null) {
            lastEntry.next = entry;
            entry.previous = lastEntry;
        }
        lastEntry = entry;
        if (firstEntry == null) {
            firstEntry = entry;
        }
    }
    private void detachEntry(FastMapEntryImpl<K,V> entry) {
        if (entry == firstEntry) {
            firstEntry = entry.next;
        }
        if (entry == lastEntry) {
            lastEntry = entry.previous;
        }
        FastMapEntryImpl<K,V> previous = entry.previous;
        FastMapEntryImpl<K,V> next = entry.next;
        if (previous != null) {
            previous.next = next;
        }
        if (next != null) {
            next.previous = previous;
        }       
    }    
    
    /** For serialization support */
    @SuppressWarnings("unchecked")
    private void readObject(java.io.ObjectInputStream s)
        throws java.io.IOException, ClassNotFoundException {
        s.defaultReadObject(); // Deserialize comparator.
        fractal = new FractalMapImpl(); 
        freeEntry = new FastMapEntryImpl<K,V>();
        int n = s.readInt();
        for (int i = 0; i < n; i++) {
            put((K)s.readObject(), (V)s.readObject());
        }
    }

    /** For serialization support */
    private void writeObject(java.io.ObjectOutputStream s)
        throws java.io.IOException{
        s.defaultWriteObject(); // Serialize comparators.
        s.writeInt(size);
        Iterator<Entry<K,V>> it = iterator();
        while (it.hasNext()) {
            Entry<K,V> e = it.next();
            s.writeObject(e.getKey());
            s.writeObject(e.getValue());
        }
    }
    
}
