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
 *     public Piece[][] getChessboard() { // On the "stack" when in PoolContext. 
 *         Piece[][] board = (Piece[][]) BOARD_FACTORY.object();
 *         ... // Populates board.
 *         return board;
 *     }
 *     private static final ObjectFactory BOARD_FACTORY = new ObjectFactory() {
 *         public Object create() {
 *              return new Piece[8][8];
 *         }
 *     };</pre>
 * <p> {@link ObjectFactory} instances are uniquely identified by their class
 *     (one instance per sub-class). The number of instances is voluntarely 
 *     limited (see <a href="{@docRoot}/overview-summary.html#configuration">
 *     Javolution Configuration</a> for details).</p>
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 3.0, February 16, 2005
 */
public abstract class ObjectFactory {

    /**
     * Holds the maximum number of {@link ObjectFactory}.
     */
    public static final int MAX = Configuration.factories();

    /**
     * Holds the factory instances.
     */
    static ObjectFactory[] INSTANCES = new ObjectFactory[MAX];

    /**
     * Holds the current number of instances.
     */
    static volatile int Count;

    /**
     * Holds the number of object allocated since last preallocation.
     */
    volatile int _allocatedCount;

    /**
     * Holds the total number of preallocated object at last preallocation.
     */
    volatile int _preallocatedCount;

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
     * @throws JavolutionError if more than one instance per factory sub-class
     *         or if the {@link #MAX} number of factories has been reached. 
     */
    protected ObjectFactory() {
        _index = ObjectFactory.add(this);
    }

    private static synchronized int add(ObjectFactory factory) {
        final int count = ObjectFactory.Count;
        if (count >= MAX) {
            throw new JavolutionError(
                    "Maximum number of factories (system property "
                            + "\"javolution.factories\", value " + MAX
                            + ") has been reached");
        }
        Class factoryClass = factory.getClass();
        for (int i = 0; i < count; i++) {
            if (factoryClass.equals(INSTANCES[i].getClass())) {
                throw new JavolutionError(
                        "No more than one instance per factory sub-class allowed");
            }
        }
        INSTANCES[count] = factory;
        return ObjectFactory.Count++;
    }

    /**
     * Creates a new object product of this factory.
     *
     * @return a new factory object.
     */
    protected abstract Object create();

    /**
     * Returns an factory object allocated from the {@link #currentPool 
     * current pool}.
     * 
     * @return a recycled or pre-allocated or new factory object.
     */
    public Object object() {
        return currentPool().next();
    }

    /**
     * Pre-allocates (replenish the pool).
     */
    final void preallocate() {
        synchronized (_heapPool) {
            for (int i = 0; i < _allocatedCount; i++) {
                Node node = new Node();
                node._object = this.create();
                node._next = _heapPool._preallocated;
                _heapPool._preallocated = node;
            }
            if (_allocatedCount > _preallocatedCount) {
                _preallocatedCount = _allocatedCount;
            }
            _allocatedCount = 0;
        }
    }

    /**
     * Returns the local pool for the current thread or the {@link #heapPool}
     * when the current thread executes in a {@link HeapContext}. 
     * 
     * @return the local pool or a pool representing the heap. 
     */
    public final ObjectPool currentPool() {
        PoolContext poolContext = Context.currentContext().poolContext();
        return (poolContext != null) ? poolContext.getLocalPool(_index)
                : _heapPool;
    }

    /**
     * Returns the pool representing the heap; although recycling is 
     * always done through garbage collection, this pool can be 
     * {@link #preallocate preallocated} at start-up.
     * 
     * @return the heap pool for this factory. 
     * @see     AllocationProfile
     */
    public final ObjectPool heapPool() {
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
    protected void cleanup(Object obj) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns a new local pool for this object factory.
     * Sub-classes may override this method in order to use specialized pools.
     * 
     * @return a new pool for stack allocation.
     */
    protected ObjectPool newPool() {
        return new LocalPool();
    }

    /**
     * This class represents the heap pool. 
     */
    final class HeapPool extends ObjectPool {

        /**
         * Holds the heap pool factory pre-allocated objects.
         */
        private Node _preallocated;

        // Implements ObjectPool abstract method.
        public Object next() {
            synchronized (this) {
                if ((_allocatedCount++ == _preallocatedCount) && 
                        AllocationProfile.OverflowHandler != null) { // Notifies.
                    AllocationProfile.OverflowHandler.run();
                }
                if (_preallocated != null) {
                    Object obj = _preallocated._object;
                    _preallocated = _preallocated._next;
                    return obj;
                }
            }
            return create();
        }

        // Implements ObjectPool abstract method.
        public void recycle(Object obj) {
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

        // Creates a new node holding a new factory object.
        private Node newNode() {
            synchronized (this) {
                if ((_allocatedCount++ == _preallocatedCount) && 
                        AllocationProfile.OverflowHandler != null) { // Notifies.
                    AllocationProfile.OverflowHandler.run();
                }
                if (_preallocated != null) {
                    Node node = _preallocated;
                    _preallocated = _preallocated._next;
                    return node;
                }
            }
            Node node = new Node();
            node._object = create();
            return node;
        }
    }

    /**
     * This class represents the default local pool implementation.
     */
    private final class LocalPool extends ObjectPool {

        /**
         * Indicates if clean-up has to be performed (switches to false if 
         * UnsupportedOperationException raised during clean-up).  
         */
        private boolean _doCleanup = true;

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

        // Implements ObjectPool abstract method.
        public Object next() {
            Node node;
            if (_availNodes != null) { // Gets node from recycled.
                node = _availNodes;
                _availNodes = node._next;
            } else { // Gets node from heapPool. 
                node = _heapPool.newNode();
            }
            if (_usedNodes == null) { // Marks tail node.
                _usedNodesTail = node;
            }
            node._next = _usedNodes;
            _usedNodes = node;
            return node._object;
        }

        // Implements ObjectPool abstract method.
        public void recycle(Object obj) {
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
                throw new JavolutionError("Object not in the pool");
            }
        }

        // Implements ObjectPool abstract method.
        protected void recycleAll() {
            // Cleanups objects.
            if (_doCleanup) {
                try {
                    for (Node node = _usedNodes; node != null;) {
                        cleanup(node._object);
                        node = node._next;
                    }
                } catch (UnsupportedOperationException ex) {
                    _doCleanup = false;
                }
            }

            _usedNodesTail._next = _availNodes;
            _availNodes = _usedNodes;
            _usedNodes = null;
        }

        // Implements ObjectPool abstract method.
        protected void clearAll() {
            _availNodes = null;
            _usedNodes = null;
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
