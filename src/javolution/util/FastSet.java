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
import j2me.io.Serializable;
import j2me.util.Collection;
import j2me.util.Iterator;
import j2me.util.Set;
import javolution.lang.Reusable;

/**
 * <p> This class represents a <code>Set</code> backed by a {@link FastMap}
 *     instance.</p>
 * 
 * <p> Instances of this class can directly be allocated from the current
 *     thread stack using the {@link #newInstance} factory method
 *     (e.g. for throw-away set to avoid the creation cost).</p>  
 * 
 * <p> {@link FastSet} has a predictable iteration order, which is the order
 *     in which the element were added to the set. The set iterator
 *     is also real-time compliant (allocated on the stack when running 
 *     in a pool context).</p>
 * 
 * <p> {@link FastSet} supports concurrent read without synchronization
 *     if the set elements are never removed. Structural modifications
 *     (element being added/removed) should always be synchronized.</p>
 * 
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 3.0, February 20, 2005
 */
public class FastSet extends FastCollection implements Set, Reusable, Serializable {

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
    private transient FastMap _map;

    /**
     * Holds the capacity.
     */
    private transient int _capacity;

    /**
     * Default constructor (default capacity of 16 elements).
     */
    public FastSet() {
        this(16);
    }

    /**
     * Creates a fast set of specified capacity.
     * 
     * @param capacity the minimum length of the internal hash table.
     */
    public FastSet(int capacity) {
        _capacity = capacity;
        _map = new FastMap(capacity);
    }

    /**
     * Returns a {@link FastSet} allocated from the stack when executing in a
     * {@link javolution.realtime.PoolContext PoolContext}).
     *
     * @return a new, pre-allocated or recycled set instance.
     */
    public static FastSet newInstance() {
        return (FastSet) FACTORY.object();
    }

    /**
     * Returns the number of elements in this set (its cardinality). 
     *
     * @return the number of elements in this set (its cardinality).
     */
    public int size() {
        return _map.size();
    }

    /**
     * Returns an iterator over the elements in this set
     * (allocated from the "stack" when executing in a
     * {@link javolution.realtime.PoolContext PoolContext}).
     * The elements are returned in is the order in which they were inserted 
     * into the map.
     *
     * @return an iterator over the elements in this set.
     */
    public Iterator iterator() {
        return _map.keySet().iterator();
    }

    /**
     * Adds the specified element to this set if it is not already present.
     *
     * @param o element to be added to this set.
     * @return <code>true</code> if this set did not already contain the 
     *         specified element.
     */
    public boolean add(Object o) {
        return _map.put(o, o) == null;
    }

    /**
     * Removes all of the elements from this set.
     */
    public void clear() {
        _map.clear();
    }

    // Implements abstract method.    
    public final Iterator fastIterator() {
        return _map.fastKeyIterator();
    }
        
    // Overrides (optimization).
    public boolean contains(Object o) {
        return _map.containsKey(o);
    }

    // Overrides (optimization).
    public boolean remove(Object o) {
        return _map.remove(o) == o;
    }

    // Overrides.
    public Collection setElementComparator(FastComparator comparator) {
        super.setElementComparator(comparator);
        _map.setKeyComparator(comparator);
        return this;
    }
    
    // Implements Reusable.
    public void reset() {
        _map.reset();
    }

    // Requires special handling during de-serialization process.
    private void readObject(ObjectInputStream stream) throws IOException,
            ClassNotFoundException {
        stream.defaultReadObject();
        final int capacity = stream.readInt();
        _map = new FastMap(capacity);
        _map.setKeyComparator(this.getElementComparator());
        final int size = stream.readInt();
        for (int i = size; i-- != 0;) {
            add(stream.readObject());
        }
    }

    // Requires special handling during serialization process.
    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.defaultWriteObject();
        stream.writeInt(_capacity);
        stream.writeInt(size());
        Iterator i = iterator();
        for (int j = size(); j-- != 0;) {
            stream.writeObject(i.next());
        }
    }

}