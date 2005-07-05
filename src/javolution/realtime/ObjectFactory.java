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
import j2me.lang.UnsupportedOperationException;

/**
 * <p> This class represents an object factory; it provides control upon how 
 *     and when objects are allocated (or pre-allocated). Object factories 
 *     should be used preferably to class constructors (ref. "new" keyword)
 *     to allow for {@link PoolContext stack} allocations and objects 
 *     {@link AllocationProfile pre-allocations}. For example:<pre>
 *     static ObjectFactory&lt;int[][]&gt; CHESSBOARD_FACTORY = new ObjectFactory&lt;int[][]&gt;() { 
 *         protected int[][] create() {
 *             return new int[8][8];
 *         }
 *     };
 *     ...
 *     int[][] chessboard = CHESSBOARD_FACTORY.object(); // On the stack if current thread executes 
 *     ...                                               // in a {@link PoolContext}; heap otherwise.
 *     </pre>
 * <p> {@link ObjectFactory} instances are uniquely identified by their class
 *     (one instance per sub-class). The number of instances is voluntarely 
 *     limited (see <a href="{@docRoot}/overview-summary.html#configuration">
 *     Javolution Configuration</a> for details).</p>
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 3.3, May 10, 2005
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
     * Indicates if allocation profiling has been enabled.
     */
    static volatile boolean IsAllocationProfileEnabled;

    /**
     * Holds the number of object allocated since last preallocation.
     */
    volatile int _allocatedCount;

    /**
     * Holds the total number of preallocated object at last preallocation.
     */
    volatile int _preallocatedCount;

    /**
     * Holds the class product of this factory (for logging only)
     */
    volatile Class _productClass;

    /**
     * Holds pre-allocated objects.
     */
    Node/*<T>*/_preallocated;

    /**
     * Holds the factory index (range [0..MAX[).
     */
    private final int _index;

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
     * Creates a new factory object on the heap (using the <code>new</code> 
     * keyword).
     *
     * @return a new factory object.
     */
    protected abstract Object/*T*/create();

    /**
     * Returns a {@link #create new}, potentially pre-allocated factory object.
     *
     * @return a pre-allocated or new factory object.
     */
    public final Object/*T*/newObject() {
        if (!IsAllocationProfileEnabled)
            return create();
        Node/*<T>*/node = preallocatedNode();
        Object/*T*/obj = (node != null) ? node._object : create();
        if (_productClass == null) {
            _productClass = obj.getClass();
        }
        return obj;
    }

    /**
     * Returns a factory object allocated on the stack when executing in a 
     * {@link PoolContext}.
     * 
     * @return a recycled, pre-allocated or new factory object.
     */
    public Object/*T*/object() {
        PoolContext poolContext = Context.currentContext().poolContext();
        return (poolContext != null) ? (Object/*T*/) poolContext.getLocalPool(
                _index).next() : newObject();
    }

    /**
     * Returns the local pool for the current thread or the {@link #heapPool}
     * when the current thread executes in a {@link HeapContext}. 
     * 
     * @return the local pool or a pool representing the heap. 
     */
    public final ObjectPool/*<T>*/currentPool() {
        PoolContext poolContext = Context.currentContext().poolContext();
        return (poolContext != null) ? (ObjectPool/*<T>*/) poolContext
                .getLocalPool(_index) : _heapPool;
    }

    /**
     * Returns the pool representing the heap; this pool always returns 
     * {@link #newObject} (potentially pre-allocated).
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
     * Pre-allocates (replenish the pool).
     */
    synchronized void preallocate() {
        for (int i = 0; i < _allocatedCount; i++) {
            Node/*<T>*/ node = new Node/*<T>*/();
            node._object = this.create();
            node._next = _preallocated;
            _preallocated = node;
        }
        if (_allocatedCount > _preallocatedCount) {
            _preallocatedCount = _allocatedCount;
        }
        _allocatedCount = 0;
    }

    /**
     * Clears preallocated pool and resets counters.
     */
    final synchronized void reset() {
        _allocatedCount = 0;
        _preallocatedCount = 0;
        _preallocated = null;
    }

    /**
     * Returns a preallocated node or <code>null</code> if none.
     * Should only be called when (IsAllocationProfileEnabled == true)
     */
    private synchronized Node/*<T>*/ preallocatedNode() {
        if ((_allocatedCount++ == _preallocatedCount)
                && AllocationProfile.OverflowHandler != null) { // Notifies.
            AllocationProfile.OverflowHandler.run();
        }
        if (_preallocated != null) {
            Node/*<T>*/ node = _preallocated;
            _preallocated = _preallocated._next;
            return node;
        } else {
            return null;
        }
    }

    /**
     * This class represents the heap pool. 
     */
    private final class HeapPool extends ObjectPool/*<T>*/{

        // Implements ObjectPool abstract method.
        public Object/*T*/next() {
            return newObject();
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
    private final class LocalPool extends ObjectPool/*<T>*/{

        /**
         * Indicates if clean-up has to be performed (switches to false if 
         * UnsupportedOperationException raised during clean-up).  
         */
        private boolean _doCleanup = true;

        /**
         * Holds used objects. 
         */
        private Node/*<T>*/_usedNodes;

        /**
         * Holds available objects. 
         */
        private Node/*<T>*/_availNodes;

        /**
         * Holds the tail node of the used list.
         */
        private Node/*<T>*/_usedNodesTail;

        // Implements ObjectPool abstract method.
        public Object/*T*/next() {
            Node/*<T>*/node;
            if (_availNodes != null) { // Gets node from recycled.
                node = _availNodes;
                _availNodes = node._next;
            } else { // Gets preallocated node or create a new node.
                if (IsAllocationProfileEnabled) {
                    node = preallocatedNode();
                    if (node == null) {
                        node = new Node/*<T>*/();
                        node._object = create();
                    }
                } else {
                    node = new Node/*<T>*/();
                    node._object = create();
                }
            }
            if (_usedNodes == null) { // Marks tail node.
                _usedNodesTail = node;
            }
            node._next = _usedNodes;
            _usedNodes = node;
            return node._object;
        }

        // Implements ObjectPool abstract method.
        public void recycle(Object/*T*/obj) {
            // Cleanups object.
            if (_doCleanup) {
                try {
                    cleanup(obj);
                } catch (UnsupportedOperationException ex) {
                    _doCleanup = false;
                }
            }
            // Moves associated node from used to available list.
            if (_usedNodes._object == obj) { // Last one allocated.
                Node/*<T>*/node = _usedNodes;
                if (node == _usedNodesTail) { // Only one node used.
                    _usedNodesTail = null;
                    if (node._next != null) // Sanity check.
                        throw new JavolutionError("Pool Corrupted");
                }
                _usedNodes = node._next;
                node._next = _availNodes;
                _availNodes = node;
            } else { // Search 
                Node/*<T>*/previous = _usedNodes;
                for (Node/*<T>*/node = previous._next; node != null;) {
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
                throw new JavolutionError("Object not in the pool");
            }
        }

        // Implements ObjectPool abstract method.
        protected void recycleAll() {
            // Cleanups objects.
            if (_doCleanup) {
                try {
                    for (Node/*<T>*/node = _usedNodes; node != null;) {
                        cleanup(node._object);
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
    static final class Node/*<T>*/{

        Object/*T*/_object;

        Node/*<T>*/_next;
    }
}
