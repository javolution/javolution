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
import javolution.text.Text;
import javolution.util.FastTable;
import javolution.xml.XMLFormat;
import javolution.xml.stream.XMLStreamException;

/**
 * <p> This class represents a pool context; it is used to recycle objects 
 *     transparently, reduce memory allocation and avoid garbage collection.</p>
 *     
 * <p> Threads executing in a pool context may allocate objects from 
 *     the context's pools (also called "stack") through an 
 *     {@link ObjectFactory}. Allocated objects are recycled automatically 
 *     upon {@link PoolContext#exit exit}. This recycling is almost 
 *     instantaneous and has no impact on performance.</p>  
 *     
 * <p> Objects allocated within a pool context should not be directly 
 *     referenced outside of the context unless they are  
 *     {@link RealtimeObject#export exported} (e.g. result being returned)
 *     or {@link RealtimeObject#preserve preserved} (e.g. shared static 
 *     instance). If this simple rule is followed, then pool context are
 *     completely safe. In fact, pool contexts promote the use of immutable
 *     objects (as their allocation cost is then negligible with no adverse 
 *     effect on garbarge collection) and often lead to safer, faster and
 *     more robust applications.</p>
 *     
 * <p> Finally, the framework guarantees that all pool objects belongs to 
 *     the same memory area (the memory area where the pool context resides).
 *     In other words, pool contexts allocated in ImmortalMemory can safely 
 *     be used by <code>NoHeapRealtimeThread</code> executing in 
 *     <code>ScopedMemory</code>.</p>
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 3.8, May 14, 2006
 */
public class PoolContext extends Context {

    /**
     * Holds the class object (cannot use .class with j2me).
     */
    private static final Class CLASS = new PoolContext().getClass();

    /**
     * Holds the XML representation for pool contexts
     * (holds sizes of associated pools).
     */
    protected static final XMLFormat/*<PoolContext>*/ XML = new XMLFormat(CLASS) {
        public void read(InputElement xml, Object obj) throws XMLStreamException {
            final PoolContext ctx = (PoolContext) obj;
            FastTable pools = (FastTable) xml.get("pools");
            ctx.setPools(pools);
        }

        public void write(Object obj, OutputElement xml) throws XMLStreamException {
            final PoolContext ctx = (PoolContext) obj;
            xml.add(ctx._pools, "pools");
        }
    };

    /**
     * Holds the pools held by this context.
     */
    final ObjectPool[] _pools = new ObjectPool[ObjectFactory._Instances.length];

    /**
     * Holds the pools currently in use. 
     */
    private final ObjectPool[] _inUsePools = new ObjectPool[ObjectFactory._Instances.length];

    /**
     * Holds the number of pools used.
     */
    private int _inUsePoolsLength;

    /**
     * Default constructor.
     */
    public PoolContext() {
    }

    /**
     * Returns the pools used by this pool context.
     * 
     * @return the pools.
     */
    public FastTable getPools() {
        FastTable pools = FastTable.newInstance();
        for (int i = ObjectFactory._Count; i > 0;) {
            ObjectPool pool = _pools[--i];
            if (pool != null) {
                pools.add(pool);
            }
        }
        return pools;
    }
    
    /**
     * Sets the pools for this pool context.
     * 
     * @param pools the context new pools.
     */
    public void setPools(FastTable pools) {
        for (int i = 0;  i < pools.size(); i++) {
            ObjectPool pool = (ObjectPool) pools.get(i);
            _pools[pool.getFactory()._index] = pool;
        }
    }

    /**
     * Returns the current pool context or <code>null<code> if the current
     * thread executes within a {@link HeapContext}.  
     *
     * @return the current pool context.
     */
    public static/*PoolContext*/Context current() {
        for (Context ctx = Context.current(); ctx != null; ctx = ctx.getOuter()) {
            if (ctx instanceof PoolContext)
                return (PoolContext) ctx;
            if (ctx instanceof HeapContext)
                return null;
        }
        throw new JavolutionError("No heap context or pool context");
    }

    /**
     * Enters a {@link PoolContext} allocated in the same memory area
     * as the memory area of the current context.
     */
    public static void enter() {
        Context.enter(PoolContext.CLASS);
    }

    /**
     * Exits the current {@link PoolContext}.
     *
     * @throws j2me.lang.IllegalStateException if the current context 
     *         is not an instance of PoolContext. 
     */
    public static void exit() {
        Context.exit(PoolContext.CLASS);
    }

    /**
     * Moves all objects belonging to this pool context to the heap.
     */
    public void clear() {
        super.clear();
        for (int i = ObjectFactory._Count; i > 0;) {
            ObjectPool pool = _pools[--i];
            if (pool != null) {
                pool.clearAll();
            }
        }
        _inUsePoolsLength = 0;
    }

    // Implements Context abstract method.
    protected void enterAction() {
        Context ctx = this.getOuter();
        if (ctx != null) {
            ctx.setPoolsActive(false);
        }
        _poolsShortcut = _pools;
    }

    // Implements Context abstract method.
    protected void exitAction() {
        recyclePools();
        Context ctx = this.getOuter();
        if (ctx != null) {
            ctx.setPoolsActive(true);
        }
    }

    // Overrides. 
    void setPoolsActive(boolean value) {
        Thread user = value ? getOwner() : null;
        for (int i = _inUsePoolsLength; i > 0;) {
            _inUsePools[--i]._user = user;
        }
    }

    // Overrides.
    ObjectPool getPool(int index, boolean activate) {
        ObjectPool pool = _pools[index];
        if (pool == null) {
            _tmpIndex = index;
            MemoryArea.getMemoryArea(this).executeInArea(NEW_POOL);
            pool = _pools[index];
        }
        if (!pool._inUse) { // Marks it in use.
            pool._inUse = true;
            _inUsePools[_inUsePoolsLength++] = pool;
        }
        if (activate) {
            pool._user = getOwner();
        }
        return pool;
    }

    private int _tmpIndex;

    private Runnable NEW_POOL = new Runnable() {
        public void run() {
            _pools[_tmpIndex] = ObjectFactory._Instances[_tmpIndex]
                    .newStackPool();
        }

    };

    /**
     * Recycles pools.
     */
    void recyclePools() {
        // Recycles pools and reset pools used.
        for (int i = _inUsePoolsLength; i > 0;) {
            ObjectPool pool = _inUsePools[--i];
            pool.recycleAll();
            pool._user = null;
            pool._inUse = false;
        }
        _inUsePoolsLength = 0;
    }
    
    /**
     * <p> This class encapsulates a reference allocated on the current stack
     *     when executing in {@link PoolContext}. The reachability level of a
     *     stack reference is the scope of the {@link PoolContext} in which it
     *     has been {@link #newInstance created}.</p>
     *     
     * <p> Stack references are automatically cleared based upon their 
     *     reachability level like any <code>java.lang.ref.Reference</code>.
     *     In other words, stack references are automatically cleared when exiting
     *     the {@link PoolContext} where they have been factory produced.</p>
     *     
     * <p> Stack references are typically used by functions having more than one
     *     return value to avoid creating new objects on the heap. For example:[code]
     *     // Returns both the position and its status.
     *     public Coordinates getPosition(Reference<Status> status) {
     *         ...
     *     }
     *     ...
     *     PoolContext.Reference<Status> statusRef = PoolContext.Reference.newInstance(); // On the stack.
     *     Coordinates position = getPosition(status);
     *     Status status = statusRef.get();
     *     statusRef.recycle(); // Immediate recycling (optional). 
     *     if (status == ACCURATE) ...[/code] 
     *     See also {@link ConcurrentContext} for usage examples.</p>
     *          
     * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
     * @version 3.4, July 3, 2005
     */
    public static final class Reference/*<T>*/extends RealtimeObject implements
            javolution.lang.Reference/*<T>*/{

        /**
         * Holds the factory.
         */
        private static final Factory FACTORY = new Factory() {
            protected Object create() {
                return new Reference();
            }

            protected void cleanup(Object obj) {
                ((Reference) obj)._value = null;
            }
        };

        /**
         * Holds the reference value.
         */
        private Object/*{T}*/_value;

        /**
         * Default constructor (private, instances should be created using 
         * factories).
         */
        private Reference() {
        }

        /**
         * Returns a new stack reference instance allocated on the current stack
         * when executing in {@link PoolContext}.
         * 
         * @return a local reference object.
         */
        public static/*<T>*/Reference /*<T>*/newInstance() {
            return (Reference) FACTORY.object();
        }
        
        /**
         * Recycles this reference immediately regardless if the current 
         * thread is in a pool context or a heap context. The value held
         * by this reference is automatically cleared.
         */
        public void recycle() {
            FACTORY.recycle(this);
        }

        /**
         * Returns the text representation of the current value of this 
         * reference.
         *
         * @return <code>Text.valueOf(this.get())</code>
         */
        public Text toText() {
            return Text.valueOf(this.get());
        }
        
        // Implements Reference interface.
        public Object/*{T}*/get() {
            return _value;
        }

        // Implements Reference interface.
        public void set(Object/*{T}*/value) {
            _value = value;
        }
    }    
}