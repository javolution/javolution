/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2006 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.context;

import j2mex.realtime.MemoryArea;
import javolution.JavolutionError;
import javolution.lang.Reflection;
import javolution.text.Text;
import javolution.xml.XMLFormat;
import javolution.xml.stream.XMLStreamException;

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
 *         // Optional (see Realtime interface). 
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
     * has been created using a constructor (new keyword).  
     */
    private transient Pool _pool;

    /**
     * Holds the next object in the pool (or null if object not in the pool).   
     */
    private transient RealtimeObject _next;

    /**
     * Holds the previous object in the pool (or null if object not in the pool).  
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
     * Indicates if this real-time object is a local object (belongs  
     * to the current {@link PoolContext}).
     * 
     * @return <code>true</code> if this object is an object local to the 
     *         current pool context; <code>false</code> if this object belongs
     *         to the heap or to an outer pool context.
     */
    public final boolean isLocal() {
        if (_pool == null) return false;
        if (!_pool._isStack) return false;
        if (_pool._user == Thread.currentThread()) return true;
        if ((_pool._user != null) || (!_pool._inUse)) 
            throw new JavolutionError("Non Accessible Object");
        return false;
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
    public final/*<T>*/Object/*{T}*/export() {
        move(ObjectSpace.OUTER);
        return (Object/*{T}*/) this;
    }

    /**
     * Moves this object and its real-time associations to the heap
     * (equivalent to <code>{@link #move move}(ObjectSpace.HEAP)</code>).
     * 
     * @return <code>this</code>
     */
    public final/*<T>*/Object/*{T}*/moveHeap() {
        move(ObjectSpace.HEAP);
        return (Object/*{T}*/) this;
    }

    /**
     * Prevents this object and its real-time associations to be recycled 
     * (equivalent to <code>{@link #move move}(ObjectSpace.HOLD)</code>).
     * This method increments this object preserved counter.
     * 
     * @return <code>this</code>
     * @see    #unpreserve
     */
    public final/*<T>*/Object/*{T}*/preserve() {
        move(ObjectSpace.HOLD);
        return (Object/*{T}*/) this;
    }

    /**
     * Allows this object and its real-time associations to  
     * be immediatly recycled if not preserved any more (equivalent to 
     * <code>{@link #move move}(ObjectSpace.STACK)</code>).
     * This method decrements this object preserved counter.
     * 
     * @return <code>this</code>
     * @see    #preserve
     */
    public final/*<T>*/Object/*{T}*/unpreserve() {
        move(ObjectSpace.STACK);
        return (Object/*{T}*/) this;
    }

    // Implements Realtime interface.
    public boolean move(ObjectSpace os) {
        if (os == ObjectSpace.OUTER) { // export()
            if (!isLocal())
                return false; // Not on the stack.
            Pool outer = (Pool) PoolContext.current().getOuter().getPool(
                    _pool._factory._index, false);
            if (!outer._isStack) // Outer heap pool.
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
                if ((_pool == null) || (!_pool._isStack))
                    return false; // Already on the heap.
                synchronized (_pool) { // Might be shared. 
                    detach();
                    _pool._size--; // Object removed from pool.
                    _pool = null;
                    _next = null;
                    _previous = null;
                    return true;
                }
            }

        } else if (os == ObjectSpace.HOLD) { // preserve()
            synchronized (this) { // Might not be local.
                if ((_pool == null) || (!_pool._isStack))
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
                            _pool._next = this;
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
        private Pool _cachedPool = new Pool(null, false);

        /**
         * Default constructor.
         */
        protected Factory() {
        }

        // Overrides.
        public final Object/*{T}*/object() {
            Pool pool = _cachedPool;
            if (pool._user == Thread.currentThread()) { // Inline next()
                final RealtimeObject next = pool._next;
                return (Object/*{T}*/) (((pool._next = next._next) != null) ? next
                        : pool.allocate());
            }
            _cachedPool = pool = (Pool) currentPool();
            return (Object/*{T}*/) pool.next();
        }

        // Overrides.
        public void recycle(Object/*{T}*/obj) {
            Pool pool = ((RealtimeObject) obj)._pool;
            if (pool == null) {
                currentPool().recycle(obj);
            } else {
                pool.recycle(obj);
            } 
        }

        // Overrides.
        protected ObjectPool newStackPool() {
            return new Pool(this, true);
        }

        // Overrides.
        protected ObjectPool newHeapPool() {
            return new Pool(this, false);
        }
    }

    /**
     * This inner class represents a stack pool of {@link RealtimeObject}.
     * 
     * It implements Runnable for object creation in memory area.
     */
    private static final class Pool extends ObjectPool implements Runnable {

        // Overrides format to use private constructor.
        static final XMLFormat XML = new XMLFormat(new Pool(null, false)
                .getClass()) {
            public Object newInstance(Class cls, XMLFormat.InputElement xml)
                    throws XMLStreamException {
                Class factoryClass;
                try {
                    factoryClass = Reflection.getClass(xml
                            .getAttribute("factory"));
                } catch (ClassNotFoundException e) {
                    throw new XMLStreamException(e);
                }
                Factory factory = (Factory) ObjectFactory
                        .getInstance(factoryClass);
                boolean isShared = xml.getAttribute("isStack", false);
                return new Pool(factory, isShared);
            }

            public void read(InputElement xml, Object obj)
                    throws XMLStreamException {
                final Pool pool = (Pool) obj;
                ObjectPool.XML.read(xml, pool);
            }

            public void write(Object obj, OutputElement xml)
                    throws XMLStreamException {
                final Pool pool = (Pool) obj;
                xml.setAttribute("isStack", pool._isStack);
                ObjectPool.XML.write(pool, xml);
            }
        };

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
         * Indicates if stack pool.
         */
        private final boolean _isStack;

        /**
         * Default constructor.
         */
        private Pool(Factory factory, boolean isStack) {
            _factory = factory;
            _isStack = isStack;
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
        public ObjectFactory getFactory() {
            return _factory;
        }

        // Implements ObjectPool abstract method.
        public int getSize() {
            return _size;
        }

        // Implements ObjectPool abstract method.
        public void setSize(int size) {
            for (int i = _size; i < size; i++) {
                RealtimeObject obj = (RealtimeObject) _factory.create();
                _size++;
                if (_isStack) {
                    obj.insertBefore(_activeTail);
                    obj._pool = Pool.this;
                } else {
                    obj.insertBefore(_holdTail);                        
                }
            }
        }

        // Implements ObjectPool abstract method.
        public Object next() {
            final RealtimeObject next = _next;
            return ((_next = next._next)!= null) ? next : allocate();
        }

        private Object allocate() {
            _next = _activeTail; // Avoids null for _next.
            if (_isStack) {
                _memoryArea.executeInArea(this);
                return _activeTail._previous;
            } else { // Heap.
                if (_size != 0) removeUse(); // Avoids keeping reference to used objects. 
                return _factory.create();
            }
        }

        // Removes the oldest pool object used and never recycled.
        private void removeUse() {
            RealtimeObject rtObj = _activeHead._next;
            if (rtObj == _activeTail) 
                throw new JavolutionError("Non empty pool with size zero");
            rtObj.detach();
            rtObj._next = null;
            rtObj._previous = null;
            rtObj._pool = null;
            _size--;
        }

        // Implements Runnable for object creation in memory area.
        public void run() {
            RealtimeObject obj = (RealtimeObject) _factory.create();
            _size++;
            obj.insertBefore(_activeTail);
            obj._pool = Pool.this;
        }

        // Implements ObjectPool abstract method.
        public void recycle(Object obj) {
            if (_factory.doCleanup()) {
                _factory.cleanup(obj);
            }
            RealtimeObject rtObj = (RealtimeObject) obj;
            Pool pool = rtObj._pool;
            if (pool == this) {
                rtObj.detach();
                rtObj.insertBefore(_next);
                _next = _next._previous;
                return;
            }
            if (pool == null) { // Heap object.
                if (MemoryArea.getMemoryArea(rtObj) != _memoryArea)
                    return; // Do not recycle accross memory areas.
                rtObj.insertBefore(_next);
                rtObj._pool = this;
                _next = _next._previous;
                _size++;
                return;
            }
            throw new IllegalArgumentException(
                "Cannot recycle object belonging to a different context");
        }

        // Implements ObjectPool abstract method.
        protected void recycleAll() {
            // Cleanups objects.
            for (RealtimeObject rt = _activeHead._next; rt != _next;) {
                if (!_factory.doCleanup())
                    break;
                _factory.cleanup(rt);
                rt = rt._next;
            }
            _next = _activeHead._next;
        }

        // Implements ObjectPool abstract method.
        protected void clearAll() {
            _activeHead._next = _activeTail;
            _activeTail._previous = _activeHead;
            _holdHead._next = _holdTail;
            _holdTail._previous = _holdHead;
        }

    }

    /**
     * This inner class represents internal linked list bounds
     * (to avoid testing for null when inserting/removing). 
     */
    private static final class Bound extends RealtimeObject {
    }
}