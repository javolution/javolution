/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2006 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.context;

import j2me.lang.UnsupportedOperationException;
import j2me.lang.ThreadLocal;
import j2mex.realtime.MemoryArea;
import javolution.Javolution;
import javolution.JavolutionError;
import javolution.text.Text;
import javolution.util.FastTable;
import javolution.xml.XMLFormat;
import javolution.xml.stream.XMLStreamException;

/**
 * <p> This class represents a pool context; it is used to recycle objects 
 *     transparently, reduce memory allocation and avoid garbage collection.
 *     Pool contexts do not require any form of synchronization (internally or
 *     externally) and result in faster execution time when many temporary 
 *     objects are produced (they are recycled all at once).</p>
 *     
 * <p> Threads executing in a pool context may allocate objects from 
 *     the context's pools (also called "stack") through an 
 *     {@link ObjectFactory}. Allocated objects are recycled automatically 
 *     upon {@link PoolContext#exit exit}. This recycling is almost 
 *     instantaneous (unlike garbage collection). For example:[code]
 *     public class DenseVector<F extends Field<F>> extends Vector<F> {
 *         ...
 *         public F times(Vector<F> that) {
 *             if (this.getDimension() != that.getDimension())
 *                   throw new DimensionException();
 *             PoolContext.enter(); // Marks thread-local stack/pool.
 *             try {
 *                 F sum = this.get(0).times(that.get(0));
 *                 for (int i = 1, n = getDimension(); i < n; i++) {
 *                     sum = sum.plus(this.get(i).times(that.get(i))); // Stack allocated objects 
 *                 }
 *                 return sum.export(); // Moves sum outside of the current marked stack.
 *             } finally {
 *                 PoolContext.exit(); // Resets the thread-local stack/pool (immediate).
 *             }
 *         }
 *     }[/code]</p>
 *     
 * <p> Objects allocated within a pool context should not be directly 
 *     referenced outside of the context unless they are  
 *     {@link RealtimeObject#export exported} (e.g. result being returned)
 *     or {@link RealtimeObject#preserve preserved} (e.g. shared static 
 *     instance). If this simple rule is followed, then pool context are
 *     completely safe. In fact, pool contexts promote the use of immutable
 *     objects (as their allocation cost is then negligible) and often lead 
 *     to safer, faster and more robust applications.</p>
 *     
 * <p> Finally for RTSJ VM, the framework guarantees that all pool objects
 *     belongs to the same memory area (the memory area where the pool context
 *     resides). Pool contexts allocated in ImmortalMemory (e.g. static) can
 *     safely be used by <code>NoHeapRealtimeThread</code> with Javolution 
 *     doing the recycling (Note: ImmortalMemory is never garbage 
 *     collected and ScopedMemory can only be used for temporary data).</p>
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 4.2, December 14, 2006
 */
public class PoolContext extends Context {

    /**
     * Holds the context factory.
     */
    private static Factory FACTORY = new Factory() {
        protected Object create() {
            return new PoolContext();
        }
    };

    /**
     * Indicates if pools are enabled.
     */
    private static final LocalContext.Reference ENABLED 
         = new LocalContext.Reference(new Boolean(true));

    /**
     * Holds the XML format.
     */
    final static XMLFormat XML = new XMLFormat(
            Javolution.j2meGetClass("javolution.context.PoolContext")) {
 
        public void read(InputElement xml, Object obj) throws XMLStreamException {
            PoolContext ctx = (PoolContext)obj;
            Class cls = ctx._allPools.getClass();
            ctx._allPools.addAll((FastTable)xml.get("Pools", cls));            
        }

        public void write(Object obj, OutputElement xml) throws XMLStreamException {
            PoolContext ctx = (PoolContext)obj;
            Class cls = ctx._allPools.getClass();
            xml.add(ctx._allPools, "Pools", cls);
        }
    };

    /**
     * Holds the collection of thread-local pools (to recycle upon exit).
     * There is a limited number of concurrent executors; and therefore 
     * thread local pools. 
     */
    private final FastTable _allPools = new FastTable();

    /**
     * Indicates if object pooling is enabled.
     */
    private boolean _isEnabled = true;

    /**
     * Holds the thread-local pools (for explicit object recycling).
     */
    private final ThreadLocal _localPools = new ThreadLocal() {
        private Runnable _createLocalPools = new Runnable() {
            public void run() {
                LocalPools pools = new LocalPools(true);
                pools._owner = Thread.currentThread();
                _allPools.add(pools);
            }
        };

        protected synchronized Object initialValue() {
            // First try to reuse existing pool (e.g. deserialized context).
            for (int i=0; i < _allPools.size(); i++) {
                LocalPools pools = (LocalPools) _allPools.get(i);
                if (pools._owner == null) {
                    pools._owner = Thread.currentThread();
                    return pools;
                }
            }
            // Else create a new local pools in this context memory area.
            MemoryArea.getMemoryArea(PoolContext.this).executeInArea(
                    _createLocalPools);
            return _allPools.getLast();
        }
    };

    /**
     * Default constructor.
     */
    public PoolContext() {
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
     * Enables/disables {@link LocalContext local} object pooling.
     * 
     * @param enabled <code>true</code> if object pooling is locally enabled;
     *        <code>false</code> otherwise.
     */
    public static void setEnabled(boolean enabled) {
        ENABLED.set(enabled ? TRUE : FALSE);
    }

    private static final Boolean TRUE = new Boolean(true); // CLDC 1.0

    private static final Boolean FALSE = new Boolean(false); // CLDC 1.0

    /**
     * Indicates if object pooling is {@link LocalContext locally} enabled
     * (default <code>true</code>).
     * 
     * @return <code>true</code> if object pooling is locally enabled;
     *         <code>false</code> otherwise.
     */
    public static boolean isEnabled() {
        return ((Boolean) ENABLED.get()).booleanValue();
    }

    /**
     * Enters a {@link PoolContext} possibly recycled.
     */
    public static void enter() {
        PoolContext ctx = (PoolContext) FACTORY.object();
        ctx._isInternal = true;
        Context.enter(ctx);
    }
    private transient boolean _isInternal;

    /**
     * Exits and recycles the current {@link PoolContext}.
     *
     * @throws UnsupportedOperationException if the current context 
     *         has not been entered using PoolContext.enter() (no parameter). 
     */
    public static void exit() {
        PoolContext ctx = (PoolContext) Context.current();
        if (!ctx._isInternal) throw new UnsupportedOperationException
           ("The context to exit must be specified");
        ctx._isInternal = false;
        Context.exitNoCheck(ctx);
        FACTORY.recycle(ctx);
    }

    /**
     * Clears this context (pool objects can then be garbage collected).
     */
    public void clear() {
        for (int i=0; i < _allPools.size(); i++) {
            ((LocalPools) _allPools.get(i)).clear();
        }
    }

    /**
     * Returns the textual representation of this context (for each factory
     * the size of the pool).
     */
    public Text toText() {
        return _allPools.toText();
    }

    // Implements Context abstract method.
    protected void enterAction() {
        Context outer = this.getOuter();
        outer.getLocalPools().deactivatePools();
        this.getLocalPools().activatePools();
    }

    // Implements Context abstract method.
    protected void exitAction() {
        // Resets all pools (the are not used any more).
        if (_isEnabled) {
           for (int i=0; i < _allPools.size(); i++) {
               ((LocalPools) _allPools.get(i)).reset();
           }
        }

        this.getLocalPools().deactivatePools();
        Context outer = this.getOuter();
        outer.getLocalPools().activatePools();

    }

    // Overrides.
    final LocalPools getLocalPools() {
        return _isEnabled ? (LocalPools) _localPools.get() :
            Context.ROOT.getLocalPools();
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