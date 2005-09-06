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
import j2me.util.Iterator;
import j2me.util.Map;
import javolution.util.FastMap;

/**
 * <p> This class represents a reference whose value can be kept persistent
 *     accross multiple program executions. Instances of this class can be  
 *     used to retrieve/set profiling configuration parameters, such as the
 *     appropriate array size to avoid resizing. For example:<pre>
 *     public class Foo {
 *         // Holds the configurable nominal size to avoid resizing.
 *         private static final PersistentReference&lt;Integer&gt; CAPACITY
 *             = new PersistentReference&lt;Integer&gt;("Foo#CAPACITY", 100);
 *         private Object[] _entries = new Object[CAPACITY.get()];
 *         private int _length; 
 *       
 *         public void add(Object entry) {
 *            if (_length >= _entries.length) { // Ooops, resizes.
 *                 Object[] tmp = new Object[_entries.length * 2];
 *                 System.arraycopy(_entries, 0, tmp, 0, _entries.length);
 *                 _entries = tmp;
 *                 CAPACITY.setMinimum(_entries.length); // Saves.
 *            }
 *            _entries[_length++] = entry; 
 *         }
 *     }</pre></p>
 *     
 * <p> Real-time application may use persistent references for pre-built data 
 *     structures to avoid delaying time critical code. For example:<pre>
 *     public class Unit {
 *         // Holds the unit multiplication table. Allows for persistency.
 *         private static final PersistentReference&lt;FastMap&lt;Unit, FastMap&lt;Unit, Unit>>>
 *             MULT_TABLE = new PersistentReference&lt;FastMap&lt;Unit, FastMap&lt;Unit, Unit>>>(
 *                 "org.jscience.physics.units.Unit#MULT_TABLE",
 *                 new FastMap&lt;Unit, FastMap&lt;Unit, Unit>>());
 *
 *         public final Unit times(Unit that) {
 *             // Checks the multiplication table first, 
 *             // if not present calculates (slow).            
 *             FastMap&lt;Unit, Unit> thisMult = MULT_TABLE.get().get(this);
 *             ...
 *         }
 *    }</pre></p>
 * 
 *  <p> How persistent references are loaded/saved is application specific. 
 *      Although, the simplest way is to use Javolution xml serialization 
 *      facility. For example:<pre>
 *      import javolution.xml.ObjectReader;
 *      import javolution.xml.ObjectWriter;
 *      public void main(String[]) {
 *           Map values  = new ObjectReader&lt;Map&lt;().read(new FileInputStream("C:/persistent.xml"));
 *           PersistentReference.putAll(values)
 *           ... 
 *           new ObjectWriter&lt;Map>().write(PersistentReference.values(), new FileOutputStream("C:/persistent.xml"));
 *      }</pre></p>
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 3.3, May 10, 2005
 */
public final class PersistentReference /*<T>*/implements Reference/*<T>*/,
        Serializable {

    /**
     * Holds the reference collection (id to reference).
     */
    private static final FastMap COLLECTION = new FastMap();

    /**
     * Holds reference current values (id to value mapping).
     */
    private static final FastMap VALUES = new FastMap();

    /**
     * Holds the collection update lock.
     */
    private static final Object LOCK = new Object();

    /**
     * Holds the unique identifier.
     */
    private final String _id;

    /**
     * Holds the reference value.
     */
    private volatile Object/*T*/_value;

    /**
     * Creates a persistent reference having the specified identifier 
     * and default value.
     * 
     * @param id the unique identifier.
     * @param defaultValue the associated default value.
     * @throws IllegalArgumentException if the id is in use.
     */
    public PersistentReference(String id, Object/*T*/defaultValue) {
        synchronized (LOCK) {
            if (COLLECTION.containsKey(id))
                throw new IllegalArgumentException("id: " + id + " in use");
            _id = id;
            if (VALUES.containsKey(id)) { // Mapping already set.
                _value = (Object/*T*/) VALUES.get(id);
                ;
            } else {
                _value = defaultValue;
            }
            COLLECTION.put(id, this);
            VALUES.put(id, _value);
        }
    }

    /**
     * Returns the unique identifier for this persistent reference.
     * 
     * @return this reference identifier.
     */
    public String id() {
        return _id;
    }

    // Implements Reference interface.
    public Object/*T*/get() {
        return _value;
    }

    // Implements Reference interface.
    public void set(Object/*T*/value) {
        synchronized (LOCK) {
            _value = value;
            VALUES.put(_id, _value);
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
        synchronized (LOCK) {
            if (value instanceof Comparable) {
                if (((Comparable) value).compareTo(_value) > 0) {
                    _value = value;
                    VALUES.put(_id, _value);
                }
            } else if (value instanceof Integer) {
                if (((Integer) value).intValue() > ((Integer) _value)
                        .intValue()) {
                    _value = value;
                    VALUES.put(_id, _value);
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
        synchronized (LOCK) {
            if (value instanceof Comparable) {
                if (((Comparable) value).compareTo(_value) < 0) {
                    _value = value;
                    VALUES.put(_id, _value);
                }
            } else if (value instanceof Integer) {
                if (((Integer) value).intValue() < ((Integer) _value)
                        .intValue()) {
                    _value = value;
                    VALUES.put(_id, _value);
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
        return VALUES.unmodifiable(); // Thread-safe, ids cannot be removed.
    }

    /**
     * Sets the value of the reference identified by the specified id.
     * 
     * @param id the persistent reference identifier.
     * @param value the value to use instead of the current value; or if the 
     *        reference does not exist, in place of the default value when 
     *        reference is created.
     */
    public static void put(String id, Object value) {
        synchronized (LOCK) {
            if (COLLECTION.containsKey(id)) {
                PersistentReference ref = (PersistentReference) COLLECTION
                        .get(id);
                ref._value = value;
            }
            VALUES.put(id, value);
        }
    }

    /**
     * Overwrites the current values with the ones specified (equivalent to 
     * {@link #put put(id, value)} for each entry of the specified map).
     * 
     * @param values the new values (id to value mapping).
     */
    public static void putAll(Map values) {
        for (Iterator i = values.entrySet().iterator(); i.hasNext();) {
            Map.Entry e = (Map.Entry) i.next();
            put((String) e.getKey(), e.getValue());
        }
    }
}