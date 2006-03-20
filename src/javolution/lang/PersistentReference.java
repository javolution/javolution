/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2005 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.lang;

import j2me.io.Serializable;
import j2me.lang.Comparable;
import j2me.util.Map;
import javolution.util.FastMap;
import javolution.util.FastSet;

/**
 * <p> This class represents a reference over an object which can be kept 
 *     persistent accross multiple program executions. Instances of this class 
 *     are typically used to hold global data time consuming to regenerate. 
 *     For example:[code]
 *     public class FastMap<K,V> implements Map<K, V> {
 *         // Provides constructor for persistent maps.
 *         public FastMap(String id) {
 *             PersistentReference<FastMap<K,V>> ref = new PersistentReference<FastMap<K,V>>(id);
 *             FastMap<K,V> persistentMap = ref.get();
 *             if (persistentMap != null) this.putAll(persistentMap);
 *             ref.set(this); // Sets this map as the persistent map.
 *         }
 *     }
 *     ...
 *     // Persistent lookup table for units multiplications.
 *     static FastMap<Unit, FastMap<Unit, Unit>> UNITS_MULT_LOOKUP 
 *          =  new FastMap<Unit, FastMap<Unit, Unit>>("UNITS_MULT_LOOKUP").setShared(true);
 *    [/code]</p>
 *    
 * <p> Persistent references may also be used to hold optimum configuration 
 *     values set from previous executions. For example:[code]
 *     public Targets {  
 *          private static PersistentReference<Integer> CAPACITY 
 *               = new PersistentReference<Integer>(Targets#CAPACITY, 256);
 *          private Target[] _targets = new Target[CAPACITY.get()];
 *          private int _count;
 *          public void add(Target target) {
 *              if (_count == _targets.length) { // Ooops, resizes.
 *                  Target[] tmp = new Target[_count * 2];
 *                  System.arraycopy(_targets, 0, tmp, 0, _count);
 *                  _targets = tmp;
 *                  CAPACITY.setMinimum(_targets.length); // Persists. 
 *              }
 *              _targets[_count++] target;
 *         }
 *     }[/code]
 * 
 *  <p> How persistent references are loaded/saved is application specific. 
 *      Although, the simplest way is to use Javolution xml serialization 
 *      facility. For example:[code]
 *      import javolution.xml.ObjectReader;
 *      import javolution.xml.ObjectWriter;
 *      public void main(String[]) {
 *           // Loads persistent reference values at start-up.
 *           Map values  = new ObjectReader<Map>().read(
 *                new FileInputStream("C:/persistent.xml"));
 *           PersistentReference.putAll(values)
 *           ... 
 *           new ObjectWriter<Map>().write(PersistentReference.values(),
 *                new FileOutputStream("C:/persistent.xml"));
 *      }[/code]</p>
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 3.7, February 24, 2006
 */
public class PersistentReference /*<T>*/implements Reference/*<T>*/,
        Serializable {

    /**
     * Holds the identifiers collection (for unicity).
     */
    private static final FastSet IDENTIFIERS = new FastSet();

    /**
     * Holds current id to value mapping).
     */
    private static final FastMap ID_TO_VALUE = new FastMap();

    /**
     * Holds the unique identifier.
     */
    private final String _id;

    /**
     * Creates a persistent reference having the specified unique identifier.
     * 
     * @param id the unique identifier.
     * @throws IllegalArgumentException if the identifier is not unique.
     */
    public PersistentReference(String id) {
        synchronized (IDENTIFIERS) {
            if (IDENTIFIERS.contains(id))
                throw new IllegalArgumentException("id: " + id
                        + " already in use");
            IDENTIFIERS.add(id);
        }
        _id = id;
    }

    /**
     * Creates a persistent reference having the specified unique identifier
     * and default value if not set.
     * 
     * @param id the unique identifier.
     * @param defaultValue the default value if not set.
     * @throws IllegalArgumentException if the identifier is not unique.
     */
    public PersistentReference(String id, Object/*T*/defaultValue) {
        this(id);
        synchronized (ID_TO_VALUE) {
            if (!ID_TO_VALUE.containsKey(_id)) {
                ID_TO_VALUE.put(id, defaultValue);
            }
        }
    }

    /**
     * Returns the unique identifier for this persistent reference.
     * 
     * @return this reference identifier.
     */
    public final String id() {
        return _id;
    }

    // Implements Reference interface.
    public Object/*T*/get() {
        synchronized (ID_TO_VALUE) {
            return (Object/*T*/) ID_TO_VALUE.get(_id);
        }
    }

    // Implements Reference interface.
    public void set(Object/*T*/value) {
        synchronized (ID_TO_VALUE) {
            ID_TO_VALUE.put(_id, value);
        }
    }

    /**
     * Sets this reference to the specified value only if 
     * <code>(value.compareTo(this.get()) &gt; 0)</code>.
     * 
     * @param value the minimum value for this reference.
     * @throws IllegalArgumentException if the specified value is not 
     *         {@link Comparable} or an {@link Integer} instance (J2ME).
     */
    public void setMinimum(Object/*T*/value) {
        synchronized (ID_TO_VALUE) {
            if (value instanceof Comparable) {
                Object prevValue = get();
                if (((Comparable) value).compareTo(prevValue) > 0) {
                    ID_TO_VALUE.put(_id, value);
                }
            } else if (value instanceof Integer) {
                Object prevValue = get();
                if (((Integer) value).intValue() > ((Integer) prevValue)
                        .intValue()) {
                    ID_TO_VALUE.put(_id, value);
                }
            } else {
                throw new IllegalArgumentException();
            }
        }
    }

    /**
     * Sets this reference to the specified value only if 
     * <code>(value.compareTo(this.get()) &lt; 0)</code>.
     * 
     * @param value the maximum value for this reference.
     * @throws IllegalArgumentException if the specified value is not 
     *         {@link Comparable} or an {@link Integer} instance (J2ME).
     */
    public void setMaximum(Object/*T*/value) {
        synchronized (ID_TO_VALUE) {
            if (value instanceof Comparable) {
                Object prevValue = get();
                if (((Comparable) value).compareTo(prevValue) < 0) {
                    ID_TO_VALUE.put(_id, value);
                }
            } else if (value instanceof Integer) {
                Object prevValue = get();
                if (((Integer) value).intValue() < ((Integer) prevValue)
                        .intValue()) {
                    ID_TO_VALUE.put(_id, value);
                }
            } else {
                throw new IllegalArgumentException();
            }
        }
    }

    /**
     * Returns a thread-safe, unmodifiable view over the persistent references 
     * values (id to value mapping).
     * 
     * @return a view over the references mapping.
     */
    public static Map values() {
        return ID_TO_VALUE.unmodifiable(); // Thread-safe, ids cannot be removed.
    }

    /**
     * Sets the value of the referenced object associated to the specified id.
     * 
     * @param id the object identifier.
     * @param value the value to use instead of the current value.
     */
    public static void put(String id, Object value) {
        ID_TO_VALUE.put(id, value);
    }

    /**
     * Overwrites the current values with the ones specified (equivalent to 
     * {@link #put put(id, value)} for each entry of the specified map).
     * 
     * @param values the new values (id to value mapping).
     */
    public static void putAll(Map values) {
        ID_TO_VALUE.putAll(values);
    }

    /**
     * Returns the string representation of the current value of this 
     * reference.
     *
     * @return <code>String.valueOf(this.get())</code>
     */
    public String toString() {
        return String.valueOf(this.get());
    }
}