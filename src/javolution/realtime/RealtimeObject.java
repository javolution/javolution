/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2004 - The Javolution Team (http://javolution.org/)
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.realtime;


import java.io.IOException;

import javolution.Javolution;
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
 *         protected Foo() {} // Default constructor for sub-classes. 
 *         public static Foo newInstance() { // Static factory method.
 *             return (Foo) FACTORY.object();
 *         }
 *         // Method to override when new real-time variable members are added.
 *         public void move(ContextSpace cs) {
 *             super.move(cs);
 *             ... // Moves the additional real-time members.
 *         }
 *     }</pre></p>
 * <p> Instances of this class can be immutable. Instances allocated in a
 *     pool context must be {@link #export exported} if referenced
 *     after exiting the pool context.</p>
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 1.0, October 4, 2004
 */
public abstract class RealtimeObject implements Realtime {

    /**
     * The pool this object belongs to or <code>null</code> if this object 
     * does not belong to a pool (e.g. created using a class constructor).  
     */
    private transient Pool _pool;

    /**
     * The index of this object in its pool (-1 for preserved objects).
     */
    private transient int _poolIndex;

    /**
     * Default constructor.
     */
    protected RealtimeObject() {
    }

    /**
     * Returns the <code>String</code> representation of this object.
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
            TypeFormat.format(this.hashCode(), 16, tb);
            return tb.toText();
        } catch (IOException e) {
            throw new Javolution.InternalError(e);
        }
    }

    /**
     * Exports this object and its <b>local</b> real-time associations out of 
     * the current pool context 
     * (equivalent to <code>{@link #move move}(ContextSpace.OUTER)</code>).
     * This method affects only local objects allocated on the stack
     * and has no effect on heap objects or objects allocated outside of 
     * the current pool context. 
     * To avoid pool depletion, a "free" object from the outer pool
     * is moved to replace the object being exported.
     * 
     * @return <code>this</code>
     */
    public final Object export() {
        move(ContextSpace.OUTER);
        return this;
    }

    /**
     * Moves this object and its real-time associations to the heap
     * (equivalent to <code>{@link #move move}(ContextSpace.HEAP)</code>).
     * 
     * @return <code>this</code>
     */
    public final Object moveHeap() {
        move(ContextSpace.HEAP);
        return this;
    }

    /**
     * Prevents/authorizes this object and its real-time associations 
     * from being recycled (equivalent to 
     * <code>{@link #move move}(ContextSpace.SHARED)</code> 
     * or <code>{@link #move move}(ContextSpace.LOCAL)</code>).
     * 
     * @param isPreserved <code>true</code> if this object can be recycled
     *        upon pool contex exit; <code>false</code> otherwise.
     * @return <code>this</code>
     */
    public final Object preserve(boolean isPreserved) {
        move(isPreserved ? ContextSpace.SHARED : ContextSpace.LOCAL);
        return this;
    }

    // Implements Realtime interface.
    public void move(ContextSpace cs) {
        if (cs == ContextSpace.OUTER) { // export()
            if (((_pool != null) && _pool.isLocal())) { // Local object.
                _pool.export(this);
            }
        } else if (cs == ContextSpace.HEAP) {
            if (_pool != null) {
                if (_pool.isLocal()) {
                    _pool.remove(this);
                } else { // Non-local pools require synchronization.
                    synchronized (_pool) {
                        _pool.remove(this);
                    }
                }
            } // Else already on the heap.
        } else if (cs == ContextSpace.SHARED) {
            if (_pool != null) {
                if (_pool.isLocal()) {
                    _pool.preserve(this);
                } else { // Non-local pools require synchronization.
                    synchronized (_pool) {
                        _pool.preserve(this);
                    }
                }
            }
        } else if (cs == ContextSpace.LOCAL) {
            if (_pool != null) {
                if (_pool.isLocal()) {
                    _pool.unpreserve(this);
                } else { // Non-local pools require synchronization.
                    synchronized (_pool) {
                        _pool.unpreserve(this);
                    }
                }
            }
        } // Ignores others context space (possible extensions).
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
     * Throws CloneNotSupportedException (prevents sub-classes from implementing
     * the Cloneable interface).
     * 
     * Note: As per "Effective Java" by Joshua Blosh p.51, the use of a copy 
     *       constructor or its static factory variant instead of clone() 
     *       is highly recommended.  
     *
     * @return N/A
     */
    protected final Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    /**
     * This abstract class represents the factory responsible for the 
     * creation of {@link RealtimeObject} instances.
     */
    protected static abstract class Factory extends ObjectFactory {

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
            Thread thread = Thread.currentThread();
            Pool pool = _cachedPool;
            if (pool.getUser() == thread) {
                return (pool._index > 0) ? // Inline pool.next()
                        pool._objects[--pool._index] : pool.allocate();
            } else {
                return object2(); // Cache miss.
            }
        }
        private final Object object2() {
            ObjectPool objectPool = currentPool();
            if (objectPool != heap()) {
                Pool pool = (Pool) objectPool;
                Object obj = pool.next();
                _cachedPool = pool;
                return obj;
            } else {
                return create();
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
         * Holds the pool's objects.
         */
        private RealtimeObject[] _objects = new RealtimeObject[DEFAULT_POOL_SIZE];

        /**
         * Holds the current pool's index.
         */
        private int _index;

        /**
         * Holds the current pool's length.
         */
        private int _length;

        /**
         * Indicates if object cleanup is enabled (default <code>true</code>).
         */
        private boolean _isCleanupEnabled = true;

        /**
         * Holds this pool's factory.
         */
        private final Factory _factory;

        /**
         * Creates a pool for the specified factory.
         * 
         * @param factory the factory for this pool.
         */
        public Pool(Factory factory) {
            _factory = factory;
        }

        /**
         * Returns a text representation of this pool for debugging purpose.
         * 
         * @return a text representation of this pool's state.
         */
        public String toString() {
            if (this.getUser() != null) {
                return "LOCAL POOL (Nesting Level: " + getNesting()
                        + ", Usage: " + (_length - _index) + ", Capacity: "
                        + _length + ")";
            } else {
                return "INACTIVE POOL (Usage: " + (_length - _index)
                        + ", Capacity: " + _length + ")";
            }
        }
        private int getNesting() {
            return (getOuter() != null)
                    ? ((Pool) getOuter()).getNesting() + 1
                    : 0;
        }

        // Implements ObjectPool abstract method.
        public Object next() {
            return (_index > 0) ? _objects[--_index] : allocate();
        }

        private Object allocate() {
            RealtimeObject rtObj = (RealtimeObject) _factory.create();
            if (_length >= _objects.length) { // Resizes.
                RealtimeObject[] tmp = new RealtimeObject[_length * 2];
                System.arraycopy(_objects, 0, tmp, 0, _length);
                _objects = tmp;
            }
            rtObj._pool = this;
            rtObj._poolIndex = _length;
            _objects[_length++] = rtObj;
            return rtObj;
        }

        // Implements ObjectPool abstract method.
        public void recycle(Object obj) {
            RealtimeObject rtObj = (RealtimeObject) obj;
            if (rtObj._pool == this) {
                RealtimeObject usedObject = _objects[_index];
                _objects[rtObj._poolIndex] = usedObject;
                usedObject._poolIndex = rtObj._poolIndex;
                _objects[_index] = rtObj;
                rtObj._poolIndex = _index++;
                if (_isCleanupEnabled) {
                    cleanup(obj);
                }
            } else {
                throw new IllegalArgumentException(
                        "obj: Object not in the pool");
            }
        }

        // Implements ObjectPool abstract method.
        protected void recycleAll() {
            for (int i = _index; _isCleanupEnabled && (i < _length);) {
                cleanup(_objects[i++]);
            }
            _index = _length;
        }

        // Implements ObjectPool abstract method.
        protected void clearAll() {
            for (int i = _length; i > 0;) {
                _objects[--i]._pool = null;
            }
            _objects = new RealtimeObject[DEFAULT_POOL_SIZE];
            _index = 0;
            _length = 0;
        }

        /**
         * Exports the specified object to the outer pool.
         * 
         * @param rtObj the object to export.
         */
        private void export(RealtimeObject rtObj) {
            Pool outerPool = (Pool) getOuter();
            if (outerPool == null) { // Exports to heap is equivalent to remove.
                remove(rtObj);
            } else if (rtObj._poolIndex < 0) { // Exports shared object.
                unpreserve(rtObj);
                export(rtObj);
                preserve(rtObj);
            } else { // Swaps with free outer object.
                RealtimeObject outerObj;
                synchronized (outerPool) { // Not local.
                    outerObj = (RealtimeObject) outerPool.next();
                }
                this._objects[rtObj._poolIndex] = outerObj;
                outerPool._objects[outerObj._poolIndex] = rtObj;
                outerObj._pool = this;
                rtObj._pool = outerPool;
                int tmp = rtObj._poolIndex;
                rtObj._poolIndex = outerObj._poolIndex;
                outerObj._poolIndex = tmp;
            }
        }

        /**
         * Removes the specified object to the outer pool.
         * 
         * @param rtObj the object to export.
         */
        private void remove(RealtimeObject rtObj) {
            if (rtObj._poolIndex >= 0) { // Not currently shared.
                _objects[rtObj._poolIndex] = _objects[--_length];
                _objects[_length]._poolIndex = rtObj._poolIndex;
                _objects[_length] = null;
            }
            rtObj._pool = null;
            rtObj._poolIndex = 0;
        }

        /**
         * Prevents the specified object from being recycled.
         * 
         * @param rtObj the object to preserve.
         */
        private void preserve(RealtimeObject rtObj) {
            if (rtObj._poolIndex >= 0) { // Not currently shared.
                // Removes from pool.
                _objects[rtObj._poolIndex] = _objects[--_length];
                _objects[_length]._poolIndex = rtObj._poolIndex;
                _objects[_length] = null;
                rtObj._poolIndex = -1;
            }
        }

        /**
         * Allows the specified object to be recycled.
         * 
         * @param rtObj the object to unpreserve.
         */
        private void unpreserve(RealtimeObject rtObj) {
            if (rtObj._poolIndex < 0) { // Currently shared.
                // Puts back into its pool.
                rtObj._poolIndex = _length;
                _objects[_length++] = rtObj;
            }
        }

        /**
         * Attempts to clean-up this object. If the attempt fails, 
         * object cleaning is disabled.
         *
         * @param  obj the object to cleanup.
         */
        private void cleanup(Object obj) {
            try {
                _factory.cleanup(obj);
            } catch (UnsupportedOperationException ex) {
                _isCleanupEnabled = false;
            }
        }
    }
}