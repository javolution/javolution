/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2005 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util;

import java.io.IOException;

import j2me.io.ObjectInputStream;
import j2me.io.ObjectOutputStream;
import j2me.util.Collection;
import j2me.util.Set;
import javolution.lang.Reusable;

/**
 * <p> This class represents a set collection backed by a {@link FastMap};
 *     smooth capacity increase and no rehashing ever performed.</p>
 * 
 * <p> Instances of this class can directly be allocated from the current
 *     thread stack using the {@link #newInstance} factory method
 *     (e.g. for throw-away set to avoid the creation cost).</p>  
 * 
 * <p> {@link FastSet}, as for any {@link FastCollection} sub-class, supports
 *     thread-safe fast iterations without using iterators. For example:[code]
 *     for (FastSet.Record r = set.head(), end = set.tail(); (r = r.getNext()) != end;) {
 *         Object value = set.valueOf(r);    
 *     }[/code]</p>
 *     
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 3.6, September 24, 2005
 */
public class FastSet/*<E>*/ extends FastCollection/*<E>*/ implements Set/*<E>*/, Reusable {

    /**
     * Holds the set factory.
     */
    private static final Factory FACTORY = new Factory() {

        public Object create() {
            return new FastSet();
        }

        public void cleanup(Object obj) {
            ((FastSet) obj).reset();
        }
    };

    /**
     * Holds the backing map.
     */
    private transient FastMap/*<E,E>*/ _map;

    /**
     * Creates a set of small initial capacity.
     */
    public FastSet() {
        this(new FastMap/*<E,E>*/());
    }

    /**
     * Creates a persistent set associated to the specified unique identifier
     * (convenience method).
     * 
     * @param id the unique identifier for this map.
     * @throws IllegalArgumentException if the identifier is not unique.
     * @see javolution.lang.PersistentReference
     */
    public FastSet(String id) {
        this(new FastMap/*<E,E>*/(id));
    }

    /**
     * Creates a set of specified initial capacity; unless the set size 
     * reaches the specified capacity, operations on this set will not allocate
     * memory (no lazy object creation).
     * 
     * @param capacity the initial capacity.
     */
    public FastSet(int capacity) {
        this(new FastMap/*<E,E>*/(capacity));
    }

    /**
     * Creates a set containing the specified elements, in the order they
     * are returned by the set iterator.
     *
     * @param elements the elements to be placed into this fast set.
     */
    public FastSet(Set/*<? extends E>*/ elements) {
        this(new FastMap/*<E,E>*/(elements.size()));
        addAll(elements);
    }

    /**
     * Creates a set implemented using the specified map.
     * 
     * @param map the backing map.
     */
    private FastSet(FastMap/*<E,E>*/ map) {
        _map = map;
    }

    /**
     * Returns a set allocated from the stack when executing in a
     * {@link javolution.realtime.PoolContext PoolContext}).
     *
     * @return a new, pre-allocated or recycled set instance.
     */
    public static /*<E>*/ FastSet/*<E>*/ newInstance() {
        return (FastSet/*<E>*/) FACTORY.object();
    }

    /**
     * Returns the number of elements in this set (its cardinality). 
     *
     * @return the number of elements in this set (its cardinality).
     */
    public final int size() {
        return _map.size();
    }

    /**
     * Adds the specified value to this set if it is not already present.
     *
     * @param value the value to be added to this set.
     * @return <code>true</code> if this set did not already contain the 
     *         specified element.
     */
    public final boolean add(Object/*E*/ value) {
        return _map.put(value, value) == null;
    }

    // Overrides to return a set (JDK1.5+).
    public Collection/*Set<E>*/unmodifiable() {
        return (Collection/*Set<E>*/) super.unmodifiable();
    }

    // Overrides (optimization).
    public final void clear() {
        _map.clear();
    }

    // Overrides (optimization).
    public final boolean contains(Object o) {
        return _map.containsKey(o);
    }

    // Overrides (optimization).
    public final boolean remove(Object o) {
        return _map.remove(o) == o;
    }

    // Overrides.
    public FastCollection/*<E>*/ setValueComparator(FastComparator comparator) {
        super.setValueComparator(comparator);
        _map.setKeyComparator(comparator);
        return this;
    }

    // Implements Reusable.
    public void reset() {
        super.setValueComparator(FastComparator.DIRECT);
        _map.reset();
    }

    // Requires special handling during de-serialization process.
    private void readObject(ObjectInputStream stream) throws IOException,
            ClassNotFoundException {
        final int size = stream.readInt();
        _map = new FastMap/*<E,E>*/(size);
        setValueComparator((FastComparator) stream.readObject());
        for (int i = size; i-- != 0;) {
            add((Object/*E*/)stream.readObject());
        }
    }

    // Requires special handling during serialization process.
    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.writeInt(size());
        stream.writeObject(getValueComparator());
        for (FastMap.Entry e = _map.head(), end = _map.tail(); 
              (e = (FastMap.Entry) e.getNext()) != end;) {
            stream.writeObject(e.getKey());
        }
    }

    // Implements FastCollection abstract method.
    public final Record head() {
        return _map.head();
    }

    // Implements FastCollection abstract method.
    public final Record tail() {
        return _map.tail();
    }

    // Implements FastCollection abstract method.
    public final Object/*E*/ valueOf(Record record) {
        return ((FastMap.Entry/*<E,E>*/) record).getKey();
    }

    // Implements FastCollection abstract method.
    public final void delete(Record record) {
        _map.remove(((FastMap.Entry/*<E,E>*/) record).getKey());
    }

    private static final long serialVersionUID = 3257563997099275574L;
}