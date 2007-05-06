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
import javolution.lang.Configurable;
import javolution.util.FastMap;
import javolution.util.FastTable;

/**
 * <p> This class represents a heap {@link AllocatorContext allocator context};
 *     it is used to allocate objects from the current heap space.</p>
 * 
 * <p> The default implementation allows for explicit object 
 *     {@link ObjectFactory#recycle(Object) recycling}. For example:[code]
 *         char[] buffer = CHAR_4096_FACTORY.object(); // Possibly recycled.
 *         while (reader.read(buffer) > 0) { ... }
 *         CHAR_4096_FACTORY.recycle(buffer); // Recycles the buffer.
 *         ...
 *         static ObjectFactory<char[]> CHAR_4096_FACTORY = new ObjectFactory<char[]>() { 
 *             protected char[] create() {
 *                 return new char[4096];
 *             }
 *         };
 *     [/code]</p>
 *     
 * <p> {@link HeapContext} are typically used to prevent static fields from 
 *     being allocated on the {@link StackContext stack} (or whatever is the 
 *     the current {@link AllocatorContext}). For example:[code]
 *     public class Rational {
 *         public static final Rational ZERO;
 *         public static final Rational ONE;
 *         ...
 *         static { // Allocates constants in ImmortalMemory.
 *             HeapContext.enter();
 *             try {
 *                 ZERO = Rational.valueOf(0, 1);
 *                 ONE = Rational.valueOf(1, 1);
 *             } finally {
 *                 HeapContext.exit();
 *             } 
 *         }
 *     }[/code]
 *     {@link javolution.lang.Realtime Realtime} classes should also ensure
 *     that dynamically allocated static fields are allocated in ImmortalMemory,
 *     otherwise these classes would not be usable by 
 *     <code>NonHeapRealtimeThread</code> (RTSJ). For example:[code]
 *         public synchronized Text intern() {
 *             if (!INTERN_INSTANCES.containsKey(this)) {
 *                 HeapContext.enter(); // Use standard allocation mechanism.
 *                 try {
 *                     MemoryArea.getMemoryArea(INTERN_INSTANCES).executeInArea(new Runnable() {
 *                         public void run() {
 *                              Text txt = this.copy(); // In ImmortalMemory.
 *                              INTERN_INSTANCES.put(txt, txt);
 *                     }});
 *                 } finally {
 *                     HeapContext.exit();
 *                 }
 *             }
 *             return (Text) INTERN_INSTANCES.get(str);
 *         }[/code]</p>
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 5.0, April 14, 2006
 */
public abstract class HeapContext extends AllocatorContext {

    /**
     * Holds the default implementation. This implementation allows 
     * for explicit object recycling unless object pooling has been 
     * {@link ObjectQueue#QUEUES_ENABLED disabled}.
     */
    public static final Configurable/*<Class>*/CLASS = new Configurable/*<Class>*/(
            new HeapContextImpl().getClass()) {
        protected void notifyChange() {
            _Default = (HeapContext) FACTORY.create();
        }
    };

    /**
     * Holds the default heap context.
     */
    static HeapContext _Default = new HeapContextImpl();

    /**
     * Holds the default implementation factory.
     */
    private static ObjectFactory FACTORY = new ObjectFactory() {
        protected Object create() {
            Class cls = (Class) CLASS.get();
            try {
                return cls.newInstance();
            } catch (InstantiationException e) {
                LogContext.error(e);
            } catch (IllegalAccessException e) {
                LogContext.error(e);
            }
            return new HeapContextImpl();
        }
    };

    /**
     * Enters a new {@link HeapContext}.
     */
    public static void enter() {
        HeapContext ctx = (HeapContext) FACTORY.object();
        Context.enter(ctx);
    }

    /**
     * Exits the current {@link HeapContext}.
     */
    public static void exit() {
        HeapContext ctx = (HeapContext) Context.current();
        Context.exitNoCheck(ctx);
    }

    /**
     * Default implementation. 
     */
    static final class HeapContextImpl extends HeapContext {

        /**
         * Holds the thread-local factory to pool mapping (FastMap). 
         */
        private static final ThreadLocal FACTORY_TO_POOL = new ThreadLocal() {
            protected Object initialValue() {
                return new FastMap();
            }
        };

        /**
         * Holds the thread-local active pools (FastTable). 
         */
        private static final ThreadLocal ACTIVE_POOLS = new ThreadLocal() {
            protected Object initialValue() {
                return new FastTable();
            }
        };

        /**
         * Default constructor.
         */
        HeapContextImpl() {
        }

        // Implements AllocatorContext abstract method.
        protected ObjectQueue getQueue(final ObjectFactory factory) {
            final FastMap factoryToPool = (FastMap) FACTORY_TO_POOL.get();
            Pool pool = (Pool) factoryToPool.get(factory);
            if (pool == null) {
                pool = new Pool(factory);
                factoryToPool.put(factory, pool);
            }
            if (pool.user == null) { // Activate.
                pool.user = Thread.currentThread();
                FastTable activePools = (FastTable) ACTIVE_POOLS.get();
                activePools.add(pool);
            }
            return pool;
        }

        // Implements AllocatorContext abstract method.
        protected void deactivate() {
            FastTable activePools = (FastTable) ACTIVE_POOLS.get();
            for (int i = 0, n= activePools.size(); i < n;) {
                ((Pool) activePools.get(i++)).user = null;
            }
            activePools.clear();
        }
    }

    // Holds heap pool implementation.
    private static final class Pool/*<T>*/extends ObjectQueue/*<T>*/{
        
        private final ObjectFactory/*<T>*/_factory;

        private Pool(ObjectFactory/*<T>*/factory) {
            _factory = factory;
            nextIndex = Integer.MAX_VALUE;
        }
        
        protected Object/*{T}*/allocate() {
            if (size == 0) return _factory.create();
            Object/*{T}*/ obj = objects[--size];
            objects[size] = null;
            return obj;
        }

        protected void recycle(Object/*{T}*/object) {
            if(_factory.doCleanup()) {
                _factory.cleanup(object);
            }
            if (size >= objects.length) {
                resize();
            }
            objects[size++] = object;
        }
        
        public String toString() {
            return "Heap for " + _factory.getClass() + "(size: " + size + ")";
        }

    }
}