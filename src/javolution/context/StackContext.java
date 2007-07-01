/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2006 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.context;

import j2me.lang.ThreadLocal;
import javolution.JavolutionError;
import javolution.lang.Configurable;
import javolution.lang.ValueType;
import javolution.util.FastMap;
import javolution.util.FastTable;

/**
 * <p> This class represents a stack {@link AllocatorContext allocator context};
 *     it is used to allocate objects from thread-local pools or 
 *     <code>ScopedMemory</code> (RTSJ).</p>
 *       
 * <p> Stacks allocations reduce heap memory allocation and often result in 
 *     faster execution time for almost all objects but the smallest one.</p>
 *     
 * <p> Stack allocated objects should never be assigned to static members 
 *     (see {@link HeapContext}). Also, methods entering/exiting stack contexts
 *     should ensure that stack allocated objects do not escape from their 
 *     context scope. If necessary, stack objects can be exported using 
 *     {@link #outerExecute} or {@link #outerCopy}. For  example:[code]
 *     public class LargeInteger implements ValueType, Realtime {
 *         public LargeInteger sqrt() {
 *             StackContext.enter(); 
 *             try { 
 *                 LargeInteger result = ZERO;
 *                 LargeInteger k = this.shiftRight(this.bitLength() / 2)); // First approximation.
 *                 while (true) { // Newton Iteration.
 *                     result = (k.plus(this.divide(k))).shiftRight(1);
 *                     if (result.equals(k)) return StackContext.outerCopy(result); // Exports result.
 *                     k = result;
 *                 }
 *             } finally { 
 *                 StackContext.exit(); 
 *             }
 *         }
 *     }[/code]</p>
 *     
 * <p> It should be noted that future versions of the JVM may provide some 
 *     limited support for stack allocation through escape analysis.
 *     Users can turn-off stack allocation by 
 *     {@link ObjectQueue#QUEUES_ENABLED disabling} objects' queues and revert
 *     to the standard JVM behavior.</p>
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 5.0, May 6, 2007
 */
public abstract class StackContext extends AllocatorContext {

    /**
     * Holds the default implementation. This implementation uses thread-local
     * pools. RTSJ alternative implementations could use 
     * <code>ScopedMemory</code> for their stack allocations.
     */
    public static final Configurable/*<Class>*/CLASS 
        = new Configurable/*<Class>*/(Default.CLASS);

    /**
     * Holds the default implementation factory.
     */
    private static ObjectFactory FACTORY = new ObjectFactory() {
        protected Object create() {
            Class cls = (Class) CLASS.get();
            if (cls == Default.CLASS) 
                return new Default(); 
            try {
                return cls.newInstance();
            } catch (InstantiationException e) {
                throw new JavolutionError(e);
            } catch (IllegalAccessException e) {
                throw new JavolutionError(e);
            }
        }
    };

    /**
     * Enters a new {@link StackContext} potentially recycled.
     */
    public static void enter() {
        StackContext ctx = (StackContext) FACTORY.object();
        Context.enter(ctx);
    }

    /**
     * Exits and recycled the current {@link StackContext}.
     */
    public static void exit() {
        StackContext ctx = (StackContext) Context.current();
        Context.exitNoCheck(ctx);
        FACTORY.recycle(ctx);
    }

    /**
     * Performs a copy of the specified value using the 
     * {@link #getOuterAllocator() outer allocator}.
     * 
     * @param value the value to be copied.
     * @return a copy allocated using the outer allocator.
     * @see #outerExecute(Runnable)
     */
    public static/*<T extends ValueType>*/ValueType/*{T}*/outerCopy(ValueType/*{T}*/value) {
        CopyLogic logic = (CopyLogic) CopyLogic.FACTORY.object();
        logic._value = value;
        ((StackContext) AllocatorContext.current()).executeInOuter(logic);
        Object copy = logic._copy;
        logic._value = null;
        logic._copy = null;
        return (ValueType/*{T}*/) copy;
    }

    private static class CopyLogic implements Runnable {
        private static ObjectFactory FACTORY = new ObjectFactory() {
            protected Object create() {
                return new CopyLogic();
            }
        };

        ValueType _value;

        Object _copy;

        public void run() {
            _copy = _value.copy();
        }
    }

    /**
     * Executes the specified logic within the  
     * {@link #getOuterAllocator() outer allocator}.
     * 
     * @see #outerCopy
     */
    public static void outerExecute(Runnable logic) {
        ((StackContext) AllocatorContext.current()).executeInOuter(logic);
    }

    /**
     * Executes the specified logic within the outer allocator context.
     */
    public abstract void executeInOuter(Runnable logic);

    /**
     * Default implementation. 
     */
    static final class Default extends StackContext {

        /**
         * Holds the class identifier.
         */
        private static final Class CLASS = new Default().getClass();

        /**
         * Holds the thread-local factory to pool mapping (FastMap). 
         */
        private final ThreadLocal _factoryToPool = new ThreadLocal() {
            protected Object initialValue() {
                return new FastMap();
            }
        };

        /**
         * Holds the thread-local active pools (FastTable). 
         */
        private final ThreadLocal _activePools = new ThreadLocal() {
            protected Object initialValue() {
                return new FastTable();
            }
        };

        /**
         * Holds the pools used by this context (for all threads). 
         */
        private final FastTable _usedPools = new FastTable();

        /**
         * Indicates if outer execution is performed.
         */
        private boolean _outerExecution;

        /**
         * Default constructor.
         */
        Default() {
        }

        // Implements AllocatorContext abstract method.
        protected ObjectQueue getQueue(final ObjectFactory factory) {
            if (_outerExecution)
                return getOuterAllocator().getQueue(factory);
            final FastMap factoryToPool = (FastMap) _factoryToPool.get();
            Pool pool = (Pool) factoryToPool.get(factory);
            if (pool == null) {
                pool = new Pool(factory);
                factoryToPool.put(factory, pool);
            }
            if (pool.user == null) { // Activate.
                pool.user = Thread.currentThread();
                FastTable activePools = (FastTable) _activePools.get();
                activePools.add(pool);
                if (!pool._inUse) {
                    pool._inUse = true;
                    synchronized (this) { // TBD: Avoid synchronizing.
                        _usedPools.add(pool);
                    }
                }
            }
            return pool;
        }

        // Implements AllocatorContext abstract method.
        protected void deactivate() {
            FastTable activePools = (FastTable) _activePools.get();
            for (int i = 0, n = activePools.size(); i < n;) {
                ((Pool) activePools.get(i++)).user = null;
            }
            activePools.clear();
        }

        // Overrides.
        protected void exitAction() {
            for (int i = 0, n = _usedPools.size(); i < n;) {
                ((Pool) _usedPools.get(i++)).reset();
            }
            _usedPools.clear();
        }

        // Implements StackContext abstract method.
        public void executeInOuter(Runnable logic) {
            deactivate();
            _outerExecution = true;
            logic.run();
            deactivate();
            _outerExecution = false;
        }
    }

    // Holds stack pool implementation.
    private static final class Pool/*<T>*/extends ObjectQueue/*<T>*/{

        private boolean _inUse;

        private Pool(ObjectFactory/*<T>*/factory) {
            super(factory);
        }

        protected Object/*{T}*/allocate() {
            Object/*{T}*/obj = factory.create();
            if (size >= objects.length) {
                resize();
            }
            objects[size] = obj;
            nextIndex = ++size;
            return obj;
        }

        protected void recycle(Object/*{T}*/object) {
            // Cleanup if necessary.
            if (factory.doCleanup()) {
                factory.cleanup(object);
            }

            // Searches for object.
            for (int i = nextIndex; --i >= 0;) {
                if (objects[i] == object) { // Found it.
                    objects[i] = objects[--nextIndex];
                    objects[nextIndex] = object;
                    return;
                }
            }
            throw new IllegalArgumentException("Object not on the stack");
        }

        // Resets the pool.
        private void reset() {
            // Cleanup if necessary.
            if (factory.doCleanup()) {
                for (int i = 0; i < nextIndex;) {
                    Object/*{T}*/obj = objects[i++];
                    factory.cleanup(obj);
                }
            }
            nextIndex = 0;
            _inUse = false;
            user = null; // Deactivates.
        }

        public String toString() {
            return "Stack for " + factory.getClass() + "(size: " + size + ")";
        }

    }

}