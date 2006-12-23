/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2006 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.context;

import javolution.lang.ClassInitializer;
import javolution.lang.Configurable;
import javolution.util.FastTable;
import j2me.lang.ThreadLocal;
import j2me.lang.UnsupportedOperationException;
import j2mex.realtime.MemoryArea;

/**
 * <p> This class represents an object factory; it allows for object 
 *     recycling and pre-allocation.
 *     
 * <p> Object factories are recommended over class constructors (ref. "new" 
 *     keyword) in order to facilitate start-up pre-allocation and/or 
 *     object reuse. For example:[code]
 *     static ObjectFactory<int[][]> BOARD_FACTORY = new ObjectFactory<int[][]>() { 
 *         protected int[][] create() {
 *             return new int[8][8];
 *         }
 *     };
 *     ...
 *     int[][] board = BOARD_FACTORY.object(); 
 *         // The board object might have been preallocated at start-up,
 *         // it might also be on the thread "stack/pool" for threads 
 *         // executing in a pool context. 
 *     ...
 *     BOARD_FACTORY.recycle(board); // Immediate recycling of the board object (optional).                      
 *     [/code]</p>
 *     
 * <p> {@link ObjectFactory} instances are uniquely identified by their class
 *     (one instance per sub-class). The number of instances is voluntarely 
 *     limited (see <a href="{@docRoot}/overview-summary.html#configuration">
 *     Javolution Configuration</a> for details).</p> 
 *          
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 3.8, May 14, 2006
 */
public abstract class ObjectFactory/*<T>*/{

    /**
     * Holds the maximum number of {@link ObjectFactory}
     * (see <a href="{@docRoot}/overview-summary.html#configuration">
     * Javolution Configuration</a> for details).
     */
    public static final Configurable/*<Integer>*/ COUNT = new Configurable(
            new Integer(256)) {
        protected void notifyChange() {
            synchronized (LOCK) {
                final int newCount = ((Integer)COUNT.get()).intValue();
                if (_Count >= newCount)
                    throw new j2me.lang.UnsupportedOperationException(
                            "Already " + _Count
                                    + " factories, cannot reduce to "
                                    + newCount);
                MemoryArea.getMemoryArea(_Instances).executeInArea(
                        new Runnable() {
                            public void run() {
                                ObjectFactory[] tmp = new ObjectFactory[newCount];
                                System.arraycopy(_Instances, 0, tmp, 0, _Count);
                                _Instances = tmp;
                            }
                        });
            }
        }
    };

    private static final Object LOCK = new Object();

    /**
     * Holds the factory instances.
     */
    static ObjectFactory[] _Instances = new ObjectFactory[((Integer)COUNT.get()).intValue()];

    /**
     * Holds the current number of instances.
     */
    static int _Count;

    /**
     * Holds the factory index (range [0..MAX_COUNT[).
     */
    final int _index;

    /**
     * Indicates if the objects products of this factory require
     * {@link #cleanup(Object) cleanup} when recycled.
     */
    private boolean _doCleanup = true;

    /**
     * Default constructor.
     */
    protected ObjectFactory() {
        synchronized (LOCK) {
            if (_Count >= _Instances.length)
                throw new UnsupportedOperationException(
                        "Configuration setting of a maximum " + _Instances.length
                                + " factories has been reached");
            Class factoryClass = this.getClass();
            for (int i = 0; i < _Count; i++) {
                if (factoryClass == _Instances[i].getClass()) {
                    throw new UnsupportedOperationException(factoryClass
                            + "  cannot have more than one instance");
                }
            }
            _index = _Count++;
            _Instances[_index] = this;
        }
    }

    /**
     * Constructs a new object for this factory (using the <code>new</code> 
     * keyword).
     *
     * @return a new factory object.
     */
    protected abstract Object/*{T}*/create();

    /**
     * Returns a factory object possibly recycled or preallocated.
     * This method is equivalent to <code>currentPool().next()</code>.
     * 
     * @return a recycled, pre-allocated or new factory object.
     */
    public Object/*{T}*/object() {
        return currentPool().next();
    }

    /**
     * Recycles the specified object in the current pool of this factory.
     * This method is equivalent to <code>currentPool().recycle(obj)</code>.
     * 
     * @param obj the object to be recycled.
     */
    public void recycle(Object/*{T}*/obj) {
        currentPool().recycle(obj);
    }

    /**
     * Returns this factory pool for the current thread.
     * The pool is also activated (user is the current thread).
     * 
     * @return a context-local pool for this factory. 
     */
    public final ObjectPool/*<T>*/currentPool() {
        ObjectPool pool = (ObjectPool) _currentPool.get();
        return  (pool._user != null) ? pool : activatePool();
    }
    private ObjectPool activatePool() {
        LocalPools pools = Context.current().getLocalPools();
        ObjectPool pool = pools.getPool(this, true);
        _currentPool.set(pool);
        return pool;
    }
    private ThreadLocal _currentPool = new ThreadLocal() {
        protected Object initialValue() {
            return newHeapPool(); // Dummy.
        }
    };

    /**
     * Cleans-up this factory's objects for future reuse. 
     * When overriden, this method is called on objects being recycled to 
     * dispose of system resources or to clear references to external
     * objects potentially on the heap (it allows these external objects to
     * be garbage collected immediately and therefore reduces the memory 
     * footprint).
     *
     * @param  obj the factory object being recycled.
     */
    protected void cleanup(Object/*{T}*/obj) {
        _doCleanup = false;
    }

    /**
     * Indicates if this factory requires cleanup. 
     *
     * @return <code>true</code> if {@link #cleanup} is overriden and 
     *         {@link #cleanup} has been called at least once; 
     *         <code>false</code> otherwise.
     */
    protected final boolean doCleanup() {
        return _doCleanup;
    }

    /**
     * Returns a new stack pool for this object factory.
     * Sub-classes may override this method in order to use specialized pools.
     * 
     * @return a new stack pool for this factory.
     */
    protected ObjectPool newStackPool() {
        return new StackPool();
    }

    /**
     * Returns a new heap pool for this object factory.
     * Sub-classes may override this method in order to use specialized pools.
     * 
     * @return a new heap pool for this factory.
     */
    protected ObjectPool newHeapPool() {
        return new HeapPool();
    }

    /**
     * Returns the factory object of specified class.
     * 
     * @return the corresponding factory.
     * @throws IllegalArgumentException if not found.
     */
    static ObjectFactory getInstance(Class factoryClass) {
        // Ensures that enclosing class if any is initialized.
        String className = factoryClass.getName();
        int sep = className.lastIndexOf('$');
        if (sep > 0) {
            ClassInitializer.initialize(className.substring(0, sep));
        }
        ClassInitializer.initialize(factoryClass);
        for (int i = 0; i < ObjectFactory._Count; i++) {
            if (_Instances[i].getClass().equals(factoryClass))
                return _Instances[i];
        }
        throw new IllegalArgumentException("Factory class: " + factoryClass + " not found"
                + ", possibly container class not initialized");
    }

    /**
     * This class represents the default heap pool. 
     */
    private final class HeapPool extends ObjectPool {

        /**
         * Holds the objects in this pool. 
         */
        private final FastTable _objects = new FastTable();

        /**
         * Default constructor.
         */
        private HeapPool() {
        }

        // Implements ObjectPool abstract method.
        public int getSize() {
            return _objects.size();
        }

        // Implements ObjectPool abstract method.
        public void setSize(int size) {
            for (int i=getSize(); i < size; i++) {
                _objects.addLast(create());
            }
        }
        
        // Implements ObjectPool abstract method.
        public Object next() {
            return _objects.isEmpty() ? create() : _objects
                    .removeLast();
        }

        // Implements ObjectPool abstract method.
        public void recycle(Object obj) {
            cleanup((Object/*{T}*/)obj);
            if (MemoryArea.getMemoryArea(obj) != MemoryArea.getMemoryArea(this))
                 return; // Do not recycle accross memory areas.
            _objects.addLast(obj);
        }

        // Implements ObjectPool abstract method.
        protected void recycleAll() {
            // No effect for heap pool.
        }

        // Implements ObjectPool abstract method.
        protected void clearAll() {
            _objects.clear();
        }

    }

    /**
     * This class represents the default stack pool implementation.
     * 
     * It implements Runnable to facilitate object creation in memory area.
     */
    private final class StackPool extends ObjectPool implements Runnable {

        /**
         * Holds the objects in this pool. 
         */
        private final FastTable _objects = new FastTable();

        /**
         * Holds the current index. 
         */
        private int _index;

        /**
         * Default constructor.
         */
        private StackPool() {
        }

        // Implements ObjectPool abstract method.
        public int getSize() {
            return _objects.size();
        }

        // Implements ObjectPool abstract method.
        public void setSize(int size) {
            for (int i=getSize(); i < size; i++) {
                _objects.addLast(create());
            }
        }

        // Implements ObjectPool abstract method.
        public Object next() {
            return (_index < _objects.size()) ? _objects.get(_index++)
                    : newObject();
        }

        private Object newObject() {
            MemoryArea.getMemoryArea(this).executeInArea(this);
            _objects.add(_tmpObject);
            _index++;
            return _tmpObject;
        }

        // Implements ObjectPool abstract method.
        public void recycle(Object obj) {
            cleanup((Object/*{T}*/)obj);
            for (int i = _index; --i >= 0;) {
                if (_objects.get(i) == obj) { // Found it.
                    // Exchange it with the last used object and adjust index.
                    Object lastObj = _objects.get(--_index);
                    _objects.set(i, lastObj);
                    _objects.set(_index, obj);
                    return;
                }
            }
            throw new IllegalArgumentException("Object not in the pool");
        }

        // Implements ObjectPool abstract method.
        protected void recycleAll() {
            // Cleanups objects.
            for (int i = 0; i < _index; i++) {
                if (!_doCleanup)
                    break;
                Object obj = _objects.get(i);
                cleanup((Object/*{T}*/)obj);
            }
            _index = 0;
        }

        // Implements ObjectPool abstract method.
        protected void clearAll() {
            _objects.clear();
            _index = 0;
        }

        // Implements Runnable for object creation in memory area.
        public void run() {
            _tmpObject = create();
        }

        private Object _tmpObject;
    }

}