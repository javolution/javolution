/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2005 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.realtime;


import javolution.Configuration;
import javolution.JavolutionError;
import javolution.lang.ClassInitializer;
import j2me.lang.UnsupportedOperationException;
import j2mex.realtime.MemoryArea;

/**
 * <p> This class represents an object factory; it allows for object 
 *     recycling and pre-allocation.
 *     
 * <p> Object factories are recommended over class constructors (ref. "new" 
 *     keyword) in order to benefit from context-based allocation policies.
 *     For example:[code]
 *     static ObjectFactory<int[][]> BOARD_FACTORY = new ObjectFactory<int[][]>() { 
 *         protected int[][] create() {
 *             return new int[8][8];
 *         }
 *     };
 *     ...
 *     int[][] board = BOARD_FACTORY.object(); // On the stack if current thread executes 
 *     ...                                     // in a PoolContext; heap otherwise.
 *     [/code]</p>
 *     
 * <p> {@link ObjectFactory} instances are uniquely identified by their class
 *     (one instance per sub-class). The number of instances is voluntarely 
 *     limited (see <a href="{@docRoot}/overview-summary.html#configuration">
 *     Javolution Configuration</a> for details).</p>
 *     
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 3.7, January 1, 2006
 */
public abstract class ObjectFactory/*<T>*/{

    /**
     * Holds the maximum number of {@link ObjectFactory}.
     */
    static final int MAX = Configuration.factories();

    /**
     * Holds the factory instances.
     */
    static ObjectFactory[] INSTANCES = new ObjectFactory[MAX];

    /**
     * Holds the current number of instances.
     */
    static volatile int Count;

    /**
     * Holds the factory index (range [0..MAX[).
     */
    final int _index;

    /**
     * Holds the current pool when executing in a heap context.
     */
    private final HeapPool _heapPool = new HeapPool();

    /**
     * Default constructor.
     * 
     * @throws UnsupportedOperationException if more than one instance per
     *         factory sub-class or if the {@link #MAX} number of factories
     *         has been reached. 
     */
    protected ObjectFactory() {
        _index = ObjectFactory.add(this);
    }

    private static synchronized int add(ObjectFactory factory) {
        final int count = ObjectFactory.Count;
        if (count >= MAX) {
            throw new UnsupportedOperationException(
                    "Maximum number of factories (system property "
                            + "\"javolution.factories\", value " + MAX
                            + ") has been reached");
        }
        Class factoryClass = factory.getClass();
        for (int i = 0; i < count; i++) {
            if (factoryClass == INSTANCES[i].getClass()) {
                throw new UnsupportedOperationException(factoryClass
                        + "  cannot have more than one instance");
            }
        }
        INSTANCES[count] = factory;
        return ObjectFactory.Count++;
    }

    /**
     * Constructs a new object for this factory (using the <code>new</code> 
     * keyword).
     *
     * @return a new factory object.
     */
    protected abstract Object/*T*/create();

    /**
     * Returns a factory object possibly recycled or preallocated.
     * This method is equivalent to <code>object(PoolContext.current())</code>.
     * 
     * @return a recycled, pre-allocated or new factory object.
     */
    public Object/*T*/object() {
        return object(Context.current().inheritedPoolContext);
    }

    /**
     * Returns a factory object from the specified pool context.
     * If a new object has to be created, it is allocated from the same 
     * memory area as the specified pool.
     * 
     * @param pool the pool context 
     * @return a recycled, pre-allocated or new factory object.
     */
    public final Object/*T*/object(PoolContext pool) {
        return (pool != null) ? (Object/*T*/) pool.getLocalPool(_index).next()
                : create();
    }

    /**
     * Returns the local pool for the current thread or the {@link #heapPool}
     * when the current thread executes in a {@link HeapContext}. 
     * 
     * @return the local pool or a pool representing the heap. 
     */
    public final ObjectPool/*<T>*/currentPool() {
        PoolContext poolContext = Context.current().inheritedPoolContext;
        return (poolContext != null) ? (ObjectPool/*<T>*/) poolContext
                .getLocalPool(_index) : _heapPool;
    }

    /**
     * Returns the pool representing the heap; this pool always returns 
     * objects allocated on the heap.
     * 
     * @return the heap pool for this factory. 
     */
    public final ObjectPool/*<T>*/heapPool() {
        return _heapPool;
    }

    /**
     * Cleans-up this factory's objects for future reuse. 
     * When overriden, this method is called on objects being recycled to 
     * dispose of system resources or to clear references to external
     * objects potentially on the heap (it allows these external objects to
     * be garbage collected immediately and therefore reduces the memory 
     * footprint).
     *
     * @param  obj the object product of this factory being recycled.
     * @throws UnsupportedOperationException if this factory does not 
     *         support object clean-up (default).
     */
    protected void cleanup(Object/*T*/obj) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns a new local pool for this object factory.
     * Sub-classes may override this method in order to use specialized pools.
     * 
     * @return a new pool for stack allocation.
     */
    protected ObjectPool/*<T>*/newPool() {
        return new LocalPool();
    }

    /**
     * Returns the factory object of specified class.
     * 
     * @return the corresponding factory or <code>null</code>.
     */
    static ObjectFactory getInstance(Class factoryClass)  {
        // Ensures that enclosing class if any is initialized.
        String className = factoryClass.getName();
        int sep = className.lastIndexOf('$');
        if (sep > 0) {
            ClassInitializer.initialize(className.substring(0, sep));
        }
        ClassInitializer.initialize(factoryClass);
        for (int i=0; i < ObjectFactory.Count; i++) {
            if (INSTANCES[i].getClass().equals(factoryClass)) 
                return INSTANCES[i];
        }
        return null;
    }

    /**
     * This class represents the heap pool. 
     */
    private final class HeapPool extends ObjectPool/*<T>*/{

        // Implements ObjectPool abstract method.
        public int size() {
            return 0;
        }

        // Implements ObjectPool abstract method.
        public Object/*T*/next() {
            return create();
        }

        // Implements ObjectPool abstract method.
        public void recycle(Object/*T*/obj) {
            // No effect for heap pool.
        }

        // Implements ObjectPool abstract method.
        protected void recycleAll() {
            // No effect for heap pool.
        }

        // Implements ObjectPool abstract method.
        protected void clearAll() {
            // No effect for heap pool.
        }
    }

    /**
     * This class represents the default local pool implementation.
     */
    private final class LocalPool extends ObjectPool {

        /**
         * Holds the memory area of this pool. 
         */
        private final MemoryArea _memoryArea;

        /**
         * Indicates if clean-up has to be performed (switches to false if 
         * UnsupportedOperationException raised during clean-up).  
         */
        private boolean _doCleanup = true;

        /**
         * Holds number of objects held by this pool. 
         */
        private int _size;

        /**
         * Holds used objects. 
         */
        private Node _usedNodes;

        /**
         * Holds available objects. 
         */
        private Node _availNodes;

        /**
         * Holds the tail node of the used list.
         */
        private Node _usedNodesTail;

        /**
         * Default constructor.
         */
        private LocalPool() {
            _memoryArea = MemoryArea.getMemoryArea(this);
        }

        // Implements ObjectPool abstract method.
        public int size() {
            return _size;
        }

        // Implements ObjectPool abstract method.
        public Object next() {
            if (_availNodes != null) { // Gets node from recycled.
                _node = _availNodes;
                _availNodes = _node._next;
            } else { // Object creation in the same memory area as the pool.
                _memoryArea.executeInArea(new Runnable() {
                    public void run() {
                        _node = new Node ();
                        _node._object = create();
                    }
                });
                _size++;
            }
            if (_usedNodes == null) { // Marks tail node.
                _usedNodesTail = _node;
            }
            _node._next = _usedNodes;
            _usedNodes = _node;
            return _node._object;
        }
        private Node _node;

        // Implements ObjectPool abstract method.
        public void recycle(Object obj) {
            // Cleanups object.
            if (_doCleanup) {
                try {
                    cleanup((Object/*T*/)obj);
                } catch (UnsupportedOperationException ex) {
                    _doCleanup = false;
                }
            }
            // Moves associated node from used to available list.
            if (_usedNodes._object == obj) { // Last one allocated.
                Node node = _usedNodes;
                if (node == _usedNodesTail) { // Only one node used.
                    _usedNodesTail = null;
                    if (node._next != null) // Sanity check.
                        throw new JavolutionError("Pool Corrupted");
                }
                _usedNodes = node._next;
                node._next = _availNodes;
                _availNodes = node;
            } else { // Search 
                Node previous = _usedNodes;
                for (Node node = previous._next; node != null;) {
                    if (node._object == obj) { // Found it.
                        if (node == _usedNodesTail) { // Tail node being removed.
                            _usedNodesTail = previous;
                        }
                        previous._next = node._next;
                        node._next = _availNodes;
                        _availNodes = node;
                        return;
                    }
                    previous = node;
                    node = node._next;
                }
                throw new IllegalArgumentException("Object not in the pool");
            }
        }

        // Implements ObjectPool abstract method.
        protected void recycleAll() {
            // Cleanups objects.
            if (_doCleanup) {
                try {
                    for (Node node = _usedNodes; node != null;) {
                        cleanup((Object/*T*/)node._object);
                        node = node._next;
                    }
                } catch (UnsupportedOperationException ex) {
                    _doCleanup = false;
                }
            }

            if (_usedNodes != null) {
                _usedNodesTail._next = _availNodes;
                _availNodes = _usedNodes;
                _usedNodes = null;
                _usedNodesTail = null;
            }
        }

        // Implements ObjectPool abstract method.
        protected void clearAll() {
            _availNodes = null;
            _usedNodes = null;
            _usedNodesTail = null;
        }
    }

    /**
     * This inner class represents a simple pool node.
     */
    static final class Node {

        Object _object;

        Node _next;
    }

}