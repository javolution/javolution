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

import java.io.IOException;

import javolution.JavolutionError;
import javolution.lang.Text;
import javolution.lang.TextBuilder;
import javolution.lang.TypeFormat;

/**
 * <p> This class provides a default implementation of the {@link Realtime} 
 *     interface.</p>
 * <p> Instances of this class should be created using a {@link Factory}.
 *     For example:<pre>
 *     public class Foo extends RealtimeObject {
 *         static final Factory FACTORY = new Factory() {
 *             public Object create() {
 *                 return new Foo();
 *             }
 *         };
 *         private Realtime externalReference = ...;
 *         protected Foo() {} // Default constructor for sub-classes. 
 *         public static Foo newInstance() { // Static factory method.
 *             return (Foo) FACTORY.object();
 *         }
 *         
 *         // Optional, only if Foo has direct or indirect references to external
 *         // {@link Realtime} objects (intrinsic heap-allocated members
 *         // themselves are moved implicitly with Foo). 
 *         public boolean move(ObjectSpace os) { 
 *             if (super.move(os)) {
 *                 externalReference.move(os);
 *                 return true;
 *             }
 *             return false;
 *         }
 *     }</pre></p>
 * <p> Instances of this class can be immutable. Instances allocated in a
 *     pool context must be {@link #export exported} (return value) 
 *     or {@link #preserve preserved} (static instance) if referenced
 *     after exiting the pool context.</p>
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 3.0, February 16, 2004
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
        TextBuilder tb = TextBuilder.newInstance();
        tb.append(getClass().getName());
        tb.append('@');
        try {
            TypeFormat.format(System.identityHashCode(this), 16, tb);
            return tb.toText();
        } catch (IOException e) {
            throw new JavolutionError(e);
        }
    }

    /**
     * Exports this object and its <b>local</b> real-time associations out of 
     * the current pool context 
     * (equivalent to <code>{@link #move move}(ObjectSpace.OUTER)</code>).
     * This method affects only local objects allocated on the stack
     * and has no effect on heap objects or objects allocated outside of 
     * the current pool context. 
     * To avoid pool depletion, a "free" object from the outer pool
     * is moved to replace the object being exported.
     * 
     * @return <code>this</code>
     */
    public final Object export() {
        move(ObjectSpace.OUTER);
        return this;
    }

    /**
     * Moves this object and its real-time associations to the heap
     * (equivalent to <code>{@link #move move}(ObjectSpace.HEAP)</code>).
     * 
     * @return <code>this</code>
     */
    public final Object moveHeap() {
        move(ObjectSpace.HEAP);
        return this;
    }

    /**
     * Prevents this object and its real-time associations to be recycled 
     * (equivalent to <code>{@link #move move}(ObjectSpace.HOLD)</code>).
     * This method increments this object preserved counter.
     * 
     * @return <code>this</code>
     * @see    #unpreserve
     */
    public final Object preserve() {
        move(ObjectSpace.LOCAL);
        return this;
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
    public final Object unpreserve() {
        move(ObjectSpace.LOCAL);
        return this;
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
                    insertBefore(((Pool)outer)._next);
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
            if (_pool == null) {
                return false; // On the heap.
            }
            synchronized (_pool) { // Might not be local.
                if (_preserved++ == 0) {
                    detach();
                    insertBefore(_pool._holdTail);
                    return true;
                } else {
                    return false;
                }
            }

        // unpreserve()    
        } else if (os == ObjectSpace.LOCAL) {
            if ((_pool == null) || (_preserved == 0)) {
                return false; // On the heap or already local.
            }
            synchronized (_pool) { // Might not be local.
                if (--_preserved == 0) {
                    detach();
                    insertBefore(_pool._next);
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
        final RealtimeObject previous = next._previous;
        _previous = previous;
        _next = next;
        _pool = next._pool;
        next._previous = this;
        previous._next = this;
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
    public static abstract class Factory extends ObjectFactory {

        /**
         * Holds the last used pools from this factory.
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
        public final Object object() {
            final Pool pool = _cachedPool;
            if (pool.getUser() == Thread.currentThread()) {
                // Inline next()
                final RealtimeObject next = pool._next;
                final RealtimeObject tmp = pool._next = next._next;
                return (tmp != null) ? next : pool.allocate();
            } else {
                final ObjectPool currentPool = currentPool();
                if (currentPool != heapPool()) {
                    _cachedPool = (Pool) currentPool;
                }
                return currentPool.next();
            }
        }

        // Overrides.
        protected ObjectPool newPool() {
            return new Pool(this);
        }
    }
    
    /**
     * This inner class represents a pool of {@link RealtimeObject}.
     */
    private static final class Pool extends ObjectPool {

        /**
         * Indicates if clean-up has to be performed (switches to false if 
         * UnsupportedOperationException raised during clean-up).  
         */
        private boolean _doCleanup = true;

        /**
         * Holds reference to heap pool.
         */
        private final ObjectFactory _factory;

        /**
         * Holds the head object.
         */
        private final RealtimeObject _activeHead = new Bound();

        /**
         * Holds the tail object.
         */
        private final RealtimeObject _activeTail = new Bound();
        
        /**
         * Holds the objects on hold
         */
        private final RealtimeObject _holdHead = new Bound();

        /**
         * Holds the objects on hold
         */
        private final RealtimeObject _holdTail = new Bound();
        
        /**
         * Holds the next object to return.
         */
        private RealtimeObject _next = _activeTail;

        /**
         * Creates a pool having the specified heap pool.
         * 
         * @param factory owner of this pool.
         */
        private Pool(Factory factory) {
            _factory = factory;
            _activeHead._next = _activeTail;
            _activeTail._previous = _activeHead;
            _holdHead._next = _holdTail;
            _holdTail._previous = _holdHead;
        }

        public Object next() {
            final RealtimeObject next = _next;
            _next = next._next;
            return (_next != null) ? next : allocate();
        }    

        private RealtimeObject allocate() {            
            _next = _activeTail;
            ObjectPool outer = getOuter();
            RealtimeObject obj;
            if (outer == null) { // Heap.
                obj = (RealtimeObject) _factory.heapPool().next();
            } else {
                synchronized (outer) {
                     obj = (RealtimeObject) outer.next();
                     obj.detach();
                }
            }
            obj.insertBefore(_activeTail);
            return obj;
        }
        
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
                throw new JavolutionError("Object not in the pool");
            }
        }

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

        protected void clearAll() {
            _activeHead._next = _activeTail;
            _activeTail._previous = _activeHead;
        }
   }

    /**
     * This inner class represents internal linked list bounds
     * (to avoid testing for null when inserting/removing). 
     */
    private static final class Bound extends RealtimeObject { }
}