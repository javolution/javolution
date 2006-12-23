/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2006 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.context;

import javolution.Javolution;
import javolution.util.FastTable;
import javolution.xml.XMLFormat;
import javolution.xml.stream.XMLStreamException;
import j2mex.realtime.MemoryArea;

/**
 * <p> This class represents the thread-local pools.</p>
 *     
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 4.2, December 14, 2006
 */
final class LocalPools {

    /**
     * Holds the XML format for pools (mapping between the factory class and
     * the size of the pool).
     */
    final static XMLFormat XML = new XMLFormat(
            Javolution.j2meGetClass("javolution.context.LocalPools")) {
        public Object newInstance(Class cls, InputElement xml) throws XMLStreamException {
            return new LocalPools(xml.getAttribute("isStack", false));
        }

        public void read(InputElement xml, Object obj) throws XMLStreamException {
            LocalPools localPools = (LocalPools)obj;
            while (xml.hasNext()) {
                Class factoryClass = (Class) xml.get("Factory", CLASS_CLASS);
                int size = ((Integer)xml.get("PoolSize", INTEGER_CLASS)).intValue();
                ObjectFactory factory = ObjectFactory.getInstance(factoryClass);
                ObjectPool pool = localPools._isStack ?
                        factory.newStackPool() : factory.newHeapPool();
                localPools._pools[factory._index] = pool;
                pool.setSize(size);
            }
        }

        public void write(Object obj, OutputElement xml) throws XMLStreamException {
            LocalPools localPools = (LocalPools)obj;
            xml.setAttribute("isStack", localPools._isStack);
            for (int i =0, n = ObjectFactory._Count; i < n; i++) {
                ObjectPool pool = localPools._pools[i];
                if (pool != null) {
                    xml.add(ObjectFactory._Instances[i].getClass(), "Factory", CLASS_CLASS);
                    xml.add(new Integer(pool.getSize()), "PoolSize", INTEGER_CLASS);
                }
            }
        }
    };
    private static final Class INTEGER_CLASS = new Integer(0).getClass();
    private static final Class CLASS_CLASS = "".getClass().getClass();
    
    /**
     * Holds direct mapping from factory index to pool.
     */
    private final ObjectPool[] _pools = new ObjectPool[ObjectFactory._Instances.length];

    /**
     * Holds the pools currently in use.
     */
    private final FastTable _inUsePools = new FastTable();

    /**
     * Holds the pools currently activated.
     */
    private final FastTable _activatedPools = new FastTable();

    /**
     * Holds the owner of this pool (unique) <code>null</code> if no owner yet
     * (e.g. deserialized).
     */
    Thread _owner;

    /**
     * Indicates if stack or heap pool.
     */
    private final boolean _isStack;

    /**
     * Creates a new pool.
     * 
     * @param isStack <code>true</code> if stack pool; <code>false</code>
     *        if heap pool.
     */
    LocalPools( boolean isStack) {
        _isStack = isStack;
    }

    /**
     * Returns the pool form the specified factory.
     * 
     * @param factory the factory of the pool to return.
     * @param activate <code>true</code> if the pool returned is activated 
     *        as the current pool for thread owner; <code>false</code>
     *        otherwise.
     * @return the pool for the specified factory.       
     */
    ObjectPool getPool(final ObjectFactory factory, boolean activate) {
        final int index = factory._index;
        ObjectPool pool = _pools[index];
        if (pool == null) {
            MemoryArea.getMemoryArea(this).executeInArea(new Runnable() {
                public void run() {
                    _pools[factory._index] = _isStack ? factory.newStackPool()
                            : factory.newHeapPool();
                }
            });
            pool = _pools[index];
        }
        if (!pool._inUse) { // Marks it in use.
            pool._inUse = true;
            _inUsePools.add(pool);
        }
        if (activate) {
            pool._user = _owner;
            _activatedPools.add(pool);
        }
        return pool;
    }

    /**
     * Clears all pools.
     */
    void clear() {
        for (int i =0, n = ObjectFactory._Count; i < n; i++) {
            ObjectPool pool = _pools[i];
            if (pool != null) {
                pool._user = null;
                pool._inUse = false;
                pool.clearAll();
            }
        }
        _inUsePools.clear();
        _activatedPools.clear();
    }

    /**
     * Deactivates pools (not the current pools anymore).
     */
    void deactivate() {
        for (int i =0, n = _activatedPools.size(); i < n;) {
            ObjectPool pool = (ObjectPool) _activatedPools.get(i++);
            pool._user = null;
        }
        _activatedPools.clear();
    }

    /**
     * Recycles all the pools which have been used and deactivates them.
     */
    public void reset() {
        for (int i =0, n = _inUsePools.size(); i < n;) {
            ObjectPool pool = (ObjectPool) _inUsePools.get(i++);
            pool.recycleAll();
            pool._user = null;
            pool._inUse = false;
        }
        _inUsePools.clear();
        _activatedPools.clear();
    }
    
    /**
     * Returns the string representation of the pools (e.g. size) for 
     * debugging purpose.
     * 
     * @return the size of all pools.
     */
    public String toString() { // For debugging.
        String str = (_isStack ? "StackPool@" : "HeapPool@") + hashCode() + ": ";
        for (int i =0, n = ObjectFactory._Count; i < n; i++) {
            ObjectPool pool = _pools[i];
            if (pool != null) {
                str += ObjectFactory._Instances[i].getClass().getName() +
                       "(" + _pools[i].getSize() + ") ";
            }
        }
        return str;
    }
}
