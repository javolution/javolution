/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2004 - The Javolution Team (http://javolution.org/)
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util;

import j2me.util.Iterator;
import j2me.util.Set;
import javolution.xml.XmlElement;
import javolution.xml.XmlFormat;

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
 * <p> This implementation is not synchronized. Multiple threads accessing or
 *     modifying the collection must be synchronized externally.</p>
 * 
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 1.0, October 4, 2004
 */
public class FastSet extends FastCollection implements Set {

    /**
     * Overrides {@link XmlFormat#COLLECTION_XML} format in order to use 
     * the {@link #newInstance(int)} factory method instead of the default 
     * constructor during the deserialization of {@link FastSet} instances.
     */
    protected static final XmlFormat FAST_SET_XML = new XmlFormat(new FastSet(null).getClass()) {
        public void format(Object obj, XmlElement xml) {
            FastSet fs = (FastSet) obj;
            xml.addAll(fs);
        }
        public Object parse(XmlElement xml) {
            FastSet fs = (xml.objectClass() == getMappedClass()) ?
                FastSet.newInstance(xml.size()) : (FastSet) xml.object();
            fs.addAll(xml);
            return fs;
        }
    };

    /**
     * Holds the backing map.
     */
    private FastMap _map;

    /**
     * Base constructor.
     * 
     * @param map the backing map.
     */
    private FastSet(FastMap map) {
        _map = map;
    }

    /**
     * Creates a {@link FastSet} with default capacity, allocated on the heap.
     */
    public FastSet() {
        this(new FastMap());
    }

    /**
     * Creates a {@link FastSet} with the specified capacity, allocated on the
     * heap. Unless the capacity is exceeded, operations on this set do not 
     * allocate memory. For optimum performance, the capacity should be of 
     * the same order of magnitude or larger than the expected set's size.
     * 
     * @param  capacity the initial capacity of the backing map.
     */
    public FastSet(int capacity) {
        this(new FastMap(capacity));
    }

    /**
     * Returns a {@link FastSet} of specified capacity, allocated from the stack
     * when executing in a {@link javolution.realtime.PoolContext PoolContext}).
     * Unless the capacity is exceeded, operations on this set do not allocate
     * new entries and even so the capacity is not increased (no re-hashing)
     * for enhanced predictability.
     *
     * @param capacity the minimum capacity for the set to return.
     * @return a new or recycled set instance.
     */
    public static FastSet newInstance(int capacity) {
        FastSet fastSet = (FastSet) FACTORY.object();
        fastSet._map = FastMap.newInstance(capacity);
        return fastSet;
    }
    private static final Factory FACTORY = new Factory() {
        public Object create() {
            return new FastSet((FastMap) null);
        }
    };

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
        return ((FastCollection)_map.keySet()).fastIterator();
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
    public void move(ContextSpace cs) {
        super.move(cs);
        _map.move(cs);
    }
}