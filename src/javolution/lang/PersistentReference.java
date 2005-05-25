/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2005 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.lang;

import j2me.util.Iterator;
import j2me.util.Map;
import javolution.util.FastMap;

/**
 * <p> This class represents a reference whose value can be kept persistent
 *     accross multiple program executions. Instances of this class can be  
 *     used to retrieve/set profiling configuration parameters, such as the
 *     appropriate array size to avoid resizing. For example:<pre>
 *     public class Foo {
 *        // Holds the configurable nominal size to avoid resizing.
 *        private static final PersistentReference&lt;Integer&gt; SIZE
 *            = new PersistentReference&lt;Integer&gt;("Foo#SIZE", new Integer(100));
 *        private Entry[] _entries = new Entry[SIZE.get().intValue()];
 *        private int _count; 
 *        
 *        public void addEntry(Entry entry) {
 *            if (_count >= _entries.length) { // Ooops, resizes.
 *                 Entry[] tmp = new Entry[_entries.length * 2];
 *                 System.arraycopy(_entries, 0, tmp, 0, _entries.length);
 *                 _entries = tmp;
 *                 SIZE.set(new Integer(_entries.length)); // Saves.
 *            }
 *            _entries[_count++] = entry; 
 *        }
 *     }</pre></p>
 *     
 * <p> Real-time application may use persistent references for pre-built data 
 *     structure to avoid delaying time critical code. For example:<pre>
 *     public class Unit {
 *         FastMap&lt;Unit, Unit&gt; _multiply = new FastMap&lt;Unit, Unit&gt;();
 *         
 *         // Allows units collection to be persistent (very expensive to build).
 *         private static final PersistentReference&lt;FastSet&lt;Unit&lt;&gt;&gt; UNITS 
 *              = new PersistentReference("org.jscience.physics.units.Unit#UNITS", new FastSet&lt;Unit&gt;);
 *
 *         public Unit multiply(Unit that) {
 *             Unit result = _multiply.get(that); // Checks internal table (saved with units).
 *             if (result == null) {
 *                 result = this.multiplyImpl(that); // Long operation.
 *                 _multiply.put(that, result);
 *             }
 *             return result;
 *         }    
 *     }</pre></p>
 * 
 *  <p> How persistent references are loaded/saved is application specific. 
 *      Although, the simplest way is to use Javolution xml serialization 
 *      facility. For example:<pre>
 *      import javolution.xml.ObjectReader;
 *      import javolution.xml.ObjectWriter;
 *      public void main(String[]) {
 *           Map values  = (Map) new ObjectReader().read(new FileInputStream("C:/persistent.xml"));
 *           PersistentReference.putAll(values)
 *           ... 
 *           new ObjectWriter().write(PersistentReference.values(), new FileOutputStream("C:/persistent.xml"));
 *      }</pre></p>
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 3.3, May 10, 2005
 */
public final class PersistentReference /*<T>*/implements Reference/*<T>*/{

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