/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2005 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.realtime;

import j2me.lang.UnsupportedOperationException;
import javolution.JavolutionError;
import javolution.lang.Text;

/**
 * <p> This class provides a default implementation of the {@link Realtime} 
 *     interface.</p>
 * <p> Instances of this class should be created using the inner 
 *     {@link Factory Factory} class. For example:<pre>
 *     public class Foo extends RealtimeObject {
 *         static final Factory&lt;Foo&gt; FACTORY = new Factory&lt;Foo&gt;() {
 *             protected Foo create() {
 *                 return new Foo();
 *             }
 *         };
 *         protected Foo() {} // Default constructor for sub-classes. 
 *         public static Foo newInstance() { // Static factory method.
 *             return FACTORY.object();
 *         }
 *         
 *         // Optional (see {@link Realtime} interface). 
 *         public boolean move(ObjectSpace os) { ... }
 *     }</pre></p>
 * <p> Instances of this class can be immutable. Instances allocated in a
 *     {@link PoolContext pool context} must be {@link #export exported} 
 *     (e.g. return value) or {@link #preserve preserved} (e.g. static instance) 
 *     if referenced after exiting the pool context.</p>
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 3.0, February 16, 2004
 */
public abstract class RealtimeObject implements Realtime {

    /**
     * The pool this object belongs to or <code>null</code> if this object 
     * is on the heap (e.g. created using a class constructor).  
     */
    private transient Factory.Pool _pool;

    /**
     * Holds the next object in the pool.  
     */
    private transient RealtimeObject _next;

    /**
     * Holds the previous object in the pool.  
     */
    private transient RealtimeObject _previous;

    /**
     * Holds the preserve counter.  
     */
    private transient int _preserved;

    /**
     * Default constructor.
     */
    protected RealtimeObject() {
    }

    /**
     * Returns the <code>String</code> representation of this object.
     * This method is final to ensure consistency with {@link #toText()}
     * (which is the method to override).
     * 
     * @return <code>toText().stringValue()</code>
     */
    public final String toString() {
        return toText().stringValue();
    }

    /**
     * Returns the default textual representation of this realtime object.
     * 
     * @return the textual representation of this object.
     */
    public Text toText() {
        return Text.valueOf(getClass().getName()).concat(Text.valueOf('@'))
                .concat(Text.valueOf(System.identityHashCode(this), 16));
    }

    /**
     * Exports this object and its <b>local</b> real-time associations out of 
     * the current pool context 
     * (equivalent to <code>{@link #move move}(ObjectSpace.OUTER)</code>).
     * This method affects only local objects allocated on the stack
     * and has no effect on heap objects or objects allocated outside of 
     * the current pool context. 
     * 
     * @return <code>this</code>
     */
    public final /*<T>*/ Object/*T*/ export() {
        move(ObjectSpace.OUTER);
        return (Object/*T*/) this;
    }

    /**
     * Moves this object and its real-time associations to the heap
     * (equivalent to <code>{@link #move move}(ObjectSpace.HEAP)</code>).
     * 
     * @return <code>this</code>
     */
    public final /*<T>*/ Object/*T*/ moveHeap() {
        move(ObjectSpace.HEAP);
        return (Object/*T*/) this;
    }

    /**
     * Prevents this object and its real-time associations to be recycled 
     * (equivalent to <code>{@link #move move}(ObjectSpace.HOLD)</code>).
     * This method increments this object preserved counter.
     * 
     * @return <code>this</code>
     * @see    #unpreserve
     */
    public final /*<T>*/ Object/*T*/ preserve() {
        move(ObjectSpace.HOLD);
        return (Object/*T*/) this;
    }

    /**
     * Allows this object and its real-time associations to  
     * be recycled if not preserved any more (equivalent to 
     * <code>{@link #move move}(ObjectSpace.LOCAL)</code>).
     * This method decrements this object preserved counter.
     * 
     * @return <code>this</code>
     * @see    #preserve
     */
    public final /*<T>*/ Object/*T*/ unpreserve() {
        move(ObjectSpace.LOCAL);
        return (Object/*T*/) this;
    }

    // Implements Realtime interface.
    public boolean move(ObjectSpace os) {

        // export()
        if (os == ObjectSpace.OUTER) {
            if ((_pool == null) || (!_pool.isLocal())) {
                return false; // Not on the stack.
            }
            detach();
            ObjectPool outer = _pool.getOuter();
            if (outer == null) { // Heap.
                _next = null;
                _previous = null;
                _pool = null;
            } else {
                synchronized (outer) {
                    _pool = (Factory.Pool) outer;
                    insertBefore(_pool._next);
                }
            }
            return true;

            // moveHeap()    
        } else if (os == ObjectSpace.HEAP) {
            if (_pool == null) {
                return false; // Already on the heap.
            }
            synchronized (_pool) { // Might not be local.
                detach();
            }
            _next = null;
            _previous = null;
            _pool = null;
            return true;

            // preserve()    
        } else if (os == ObjectSpace.HOLD) {
            synchronized (this) {
                if (_preserved++ == 0) {
                    if (_pool != null) {
                        synchronized (_pool) { // Might not be local.
                            detach();
                            insertBefore(_pool._holdTail);
                        }
                    }
                    return true;
                } else {
                    return false;
                }
            }

            // unpreserve()    
        } else if (os == ObjectSpace.LOCAL) {
            synchronized (this) {
                if ((_preserved != 0) && (--_preserved == 0)) {
                    if (_pool != null) {
                        synchronized (_pool) { // Might not be local
                            detach();
                            insertBefore(_pool._next);
                        }
                    }
                    return true;
                } else {
                    return false;
                }
            }

            // Ignores others context space (possible extensions).            
        } else {
            return true; // Propagates by default. 
        }
    }

    /**
     * Recycles this object and its internals only. This method should only be
     * called when it can be asserted that this object is not going to be 
     * referenced anymore. 
     * This method affects only local objects and has no effect on heap objects
     * or objects allocated outside of the current pool context. 
     * Unlike the {@link #move move} operations, recycling is limited to this
     * object and its internals and has no effect on shared 
     * variable members ({@link javolution.realtime.Realtime real-time} or not).
     */
    protected void recycle() {
        if (((_pool != null) && _pool.isLocal())) {
            _pool.recycle(this);
        }
    }

    /**
     * Inserts this object before the one specified.
     * 
     * @param the next object after insertion.
     */
    final void insertBefore(RealtimeObject next) {
        _previous = next._previous;
        _next = next;
        _next._previous = this;
        _previous._next = this;
    }

    /**
     * Detaches this object from its linked list (but does not reset the
     * objects variable members).
     */
    final void detach() {
        _next._previous = _previous;
        _previous._next = _next;
    }

    /**
     * This abstract class represents the factory responsible for the 
     * creation of {@link RealtimeObject} instances.
     */
    public static abstract class Factory/*<T>*/ extends ObjectFactory/*<T>*/{

        /**
         * Holds the last used pools from this factory.
         */
        private Pool _cachedPool = new Pool();

        /**
         * Default constructor.
         */
        protected Factory() {
        }

        /**
         * Returns a new or recycled object from this factory. 
         * 
         * @return an object from the local stack or from the heap if not 
         *         executing in a pool context.
         */
        public final Object/*T*/object() {
            Pool pool = _cachedPool;
            if (pool.getUser() == Thread.currentThread()) {
                // Inline next()
                final RealtimeObject next = pool._next;
                final RealtimeObject tmp = pool._next = next._next;
                return (Object/*T*/) ((tmp != null) ? next : pool.allocate());
            } else {
                final ObjectPool/*<T>*/currentPool = currentPool();
                if (currentPool == heapPool()) {
                    return newObject();
                } else {
                    _cachedPool = pool = (Pool) currentPool;
                    return pool.next();
                }
            }
        }

        // Overrides.
        protected ObjectPool/*<T>*/newPool() {
            return new Pool();
        }

        /**
         * This inner class represents a pool of {@link RealtimeObject}.
         */
        private final class Pool extends ObjectPool/*<T>*/{

            /**
             * Indicates if clean-up has to be performed (switches to false if 
             * UnsupportedOperationException raised during clean-up).  
             */
            private boolean _doCleanup = true;

            /**
             * Holds the head object.
             */
            private final RealtimeObject _activeHead;

            /**
             * Holds the tail object.
             */
            private final RealtimeObject _activeTail;

            /**
             * Holds the objects on hold
             */
            private final RealtimeObject _holdHead;

            /**
             * Holds the objects on hold
             */
            private final RealtimeObject _holdTail;

            /**
             * Holds the next object to return.
             */
            private RealtimeObject _next;

            /**
             * Default constructor.
             */
            private Pool() {
                _activeHead = new Bound();
                _activeTail = new Bound();
                _activeHead._next = _activeTail;
                _activeTail._previous = _activeHead;

                _holdHead = new Bound();
                _holdTail = new Bound();
                _holdHead._next = _holdTail;
                _holdTail._previous = _holdHead;

                _next = _activeTail;
            }

            public Object/*T*/next() {
                final RealtimeObject next = _next;
                _next = next._next;
                return (Object/*T*/) ((_next != null) ? next : allocate());
            }

            private RealtimeObject allocate() {
                _next = _activeTail;
                ObjectPool outer = getOuter();
                RealtimeObject obj;
                if (outer == null) { // Heap.
                    obj = (RealtimeObject) newObject();
                } else {
                    synchronized (outer) {
                        obj = (RealtimeObject) outer.next();
                        obj.detach();
                    }
                }
                obj.insertBefore(_activeTail);
                obj._pool = this;
                return obj;
            }

            public void recycle(Object/*T*/obj) {
                // Cleanups object.
                if (_doCleanup) {
                    try {
                        cleanup(obj);
                    } catch (UnsupportedOperationException ex) {
                        _doCleanup = false;
                    }
                }

                RealtimeObject rtObj = (RealtimeObject) obj;
                if (rtObj._pool == this) {
                    rtObj.detach();
                    rtObj.insertBefore(_next);
                    _next = _next._previous;
                } else {
                    throw new JavolutionError("Object not in the pool");
                }
            }

            protected void recycleAll() {
                // Cleanups objects.
                if (_doCleanup) {
                    try {
                        for (RealtimeObject rt = _activeHead._next; rt != _next;) {
                            cleanup((Object/*T*/) rt);
                            rt = rt._next;
                        }
                    } catch (UnsupportedOperationException ex) {
                        _doCleanup = false;
                    }
                }
                _next = _activeHead._next;
            }

            protected void clearAll() {
                _activeHead._next = _activeTail;
                _activeTail._previous = _activeHead;
            }
        }
    }

    /**
     * This inner class represents internal linked list bounds
     * (to avoid testing for null when inserting/removing). 
     */
    private static final class Bound extends RealtimeObject {
    }
}