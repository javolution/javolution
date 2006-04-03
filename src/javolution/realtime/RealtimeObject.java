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
import j2mex.realtime.MemoryArea;
import javolution.lang.Text;

/**
 * <p> This class provides a default implementation of the {@link Realtime} 
 *     interface.</p>
 *     
 * <p> Instances of this class should be created using the inner 
 *     {@link Factory Factory} class. For example:[code]
 *     public class Foo extends RealtimeObject {
 *         static final Factory<Foo> FACTORY = new Factory<Foo>() {
 *             protected Foo create() {
 *                 return new Foo();
 *             }
 *         };
 *         protected Foo() {} // Default constructor for sub-classes. 
 *         public static Foo newInstance() { // Static factory method.
 *             return FACTORY.object();
 *         }
 *         
 *         // Optional. 
 *         public boolean move(ObjectSpace os) { ... }
 *     }[/code]</p>
 *     
 * <p> Instances of this class can be immutable. Instances allocated in a
 *     {@link PoolContext pool context} must be {@link #export exported} 
 *     (e.g. return value) or {@link #preserve preserved} (e.g. static instance) 
 *     if referenced after exiting the pool context.</p>
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 3.7, January 1, 2006
 */
public abstract class RealtimeObject implements Realtime {

    /**
     * The pool this object belongs to or <code>null</code> if this object 
     * is on the heap (e.g. created using a class constructor).  
     */
    private transient Pool _pool;

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
     * the current pool context (equivalent to <code>{@link #move move}
     * (ObjectSpace.OUTER)</code>).
     * This method affects only local objects allocated on the stack
     * and has no effect on heap objects or objects allocated outside of 
     * the current pool context. 
     * 
     * <p>Note: To avoid pool depletion when exporting to outer pool, 
     *          the object is actually exchanged with an outer pool object.</p> 
     * 
     * @return <code>this</code>
     */
    public final/*<T>*/Object/*T*/export() {
        move(ObjectSpace.OUTER);
        return (Object/*T*/) this;
    }

    /**
     * Moves this object and its real-time associations to the heap
     * (equivalent to <code>{@link #move move}(ObjectSpace.HEAP)</code>).
     * 
     * @return <code>this</code>
     */
    public final/*<T>*/Object/*T*/moveHeap() {
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
    public final/*<T>*/Object/*T*/preserve() {
        move(ObjectSpace.HOLD);
        return (Object/*T*/) this;
    }

    /**
     * Allows this object and its real-time associations to  
     * be recycled if not preserved any more (equivalent to 
     * <code>{@link #move move}(ObjectSpace.STACK)</code>).
     * This method decrements this object preserved counter.
     * 
     * @return <code>this</code>
     * @see    #preserve
     */
    public final/*<T>*/Object/*T*/unpreserve() {
        move(ObjectSpace.STACK);
        return (Object/*T*/) this;
    }

    // Implements Realtime interface.
    public boolean move(ObjectSpace os) {
        if (os == ObjectSpace.OUTER) { // export()
            if ((_pool == null) || (!_pool.isLocal()))
                return false; // Not on the stack.
            Pool outer = (Pool) _pool.outer;
            if (outer == null)
                return move(ObjectSpace.HEAP);
            detach();
            // Exchanges with outer.
            synchronized (outer) { // Might be shared.
                RealtimeObject outerObj = (RealtimeObject) outer.next();
                outerObj.detach();
                outerObj.insertBefore(_pool._activeTail); // Marks unused.
                outerObj._pool = _pool;
                insertBefore(outer._next); // Marks used.
                _pool = outer;
            }
            return true;

        } else if (os == ObjectSpace.HEAP) { // moveHeap()
            synchronized (this) { // Might not be local.
                if (_pool == null)
                    return false; // Already on the heap.
                synchronized (_pool) { // Might be shared. 
                    detach();
                    _pool._size--; // Object removed from pool.
                    _next = null;
                    _previous = null;
                    _pool = null;
                    return true;
                }
            }

        } else if (os == ObjectSpace.HOLD) { // preserve()
            synchronized (this) { // Might not be local.
                if (_pool == null)
                    return false; // On the heap.
                if (_preserved++ == 0) {
                    synchronized (_pool) { // Might be shared.
                        detach();
                        insertBefore(_pool._holdTail);
                    }
                    return true;
                } else {
                    return false;
                }
            }

        } else if (os == ObjectSpace.STACK) { // unpreserve()
            synchronized (this) { // Might not be local.
                if ((_preserved != 0) && (--_preserved == 0)) {
                    if (_pool != null) {
                        synchronized (_pool) { // Might be shared.
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
     * object and its internals and has no effect on external 
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
     * Note: pool._next should never be detached.
     */
    final void detach() {
        _next._previous = _previous;
        _previous._next = _next;
    }

    /**
     * This abstract class represents the factory responsible for the 
     * creation of {@link RealtimeObject} instances.
     */
    public static abstract class Factory/*<T extends RealtimeObject>*/extends
            ObjectFactory/*<T>*/{

        /**
         * Holds the last used pool from this factory.
         */
        private Pool _cachedPool = new Pool(null);

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
            final Thread currentThread = Thread.currentThread();
            Pool pool = _cachedPool;
            if (pool.user == currentThread) { // Inline next()
                final RealtimeObject next = pool._next;
                return (Object/*T*/) (((pool._next = next._next) != null) ? next
                        : pool.allocate());
            }
            final PoolContext poolContext = Context.poolContext(currentThread);
            if (poolContext == null)
                return create();
            pool = (Pool) poolContext.getLocalPool(_index);
            Object/*T*/ obj = (Object/*T*/) pool.next();
            _cachedPool = pool; // Do it last.
            return obj;
        }

        // Overrides.
        protected ObjectPool/*<T>*/newPool() {
            ObjectPool pool = new Pool(this);
            return (ObjectPool/*<T>*/) pool;
        }
    }

    /**
     * This inner class represents a pool of {@link RealtimeObject}.
     */
    private static final class Pool extends ObjectPool {

        /**
         * Holds the factory. 
         */
        private final Factory _factory;

        /**
         * Holds the memory area of this pool. 
         */
        private final MemoryArea _memoryArea;

        /**
         * Holds number of objects held by this pool. 
         */
        private int _size;

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
        private Pool(Factory factory) {
            _factory = factory;
            _memoryArea = MemoryArea.getMemoryArea(this);

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

        // Implements ObjectPool abstract method.
        public int size() {
            return _size;
        }

        // Implements ObjectPool abstract method.
        public Object next() {
            final RealtimeObject next = _next;
            _next = next._next;
            return ((_next != null) ? next : allocate());
        }

        private RealtimeObject allocate() {
            _next = _activeTail;
            _memoryArea.executeInArea(new Runnable() {
                public void run() {
                    RealtimeObject obj = (RealtimeObject) _factory.create();
                    _size++;
                    obj.insertBefore(_activeTail);
                    obj._pool = Pool.this;
                }
            });
            return _activeTail._previous;
        }

        // Implements ObjectPool abstract method.
        public void recycle(Object obj) {
            // Cleanups object.
            if (_doCleanup) {
                try {
                    _factory.cleanup(obj);
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
                throw new IllegalArgumentException("Object not in the pool");
            }
        }

        // Implements ObjectPool abstract method.
        protected void recycleAll() {
            // Cleanups objects.
            if (_doCleanup) {
                try {
                    for (RealtimeObject rt = _activeHead._next; rt != _next;) {
                        _factory.cleanup(rt);
                        rt = rt._next;
                    }
                } catch (UnsupportedOperationException ex) {
                    _doCleanup = false;
                }
            }
            _next = _activeHead._next;
        }

        // Implements ObjectPool abstract method.
        protected void clearAll() {
            _activeHead._next = _activeTail;
            _activeTail._previous = _activeHead;
        }
    }

    /**
     * This inner class represents internal linked list bounds
     * (to avoid testing for null when inserting/removing). 
     */
    private static final class Bound extends RealtimeObject {
    }
}