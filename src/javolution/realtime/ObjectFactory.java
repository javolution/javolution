/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2004 - The Javolution Team (http://javolution.org/)
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.realtime;
import j2me.lang.UnsupportedOperationException;

/**
 * <p> This class represents an object factory. How and when a factory allocates
 *     its objects depends upon its real-time context. The default context
 *     ({@link HeapContext}) allocates objects on-demand (from the heap)
 *     and recycling is done through garbage collection.</p>
 * <p> Object factories should be used preferably to class constructors 
 *     (ref. "new" keyword) to allow for "stack" allocation when the current 
 *     thread executes in a {@link PoolContext}:<pre>
 *     // Functions may return more than one object with no garbage generated!
 *     public PositionVelocity getPositionVelocity() {
 *         PositionVelocity pv = (PositionVelocity)PositionVelocity.FACTORY.object();
 *         pv.position = ...
 *         pv.velocity = ...
 *         return pv; // On the "stack" when executing in a {@link PoolContext}.
 *     }    
 *     public static class PositionVelocity { // Atomic data structure. 
 *         static final ObjectFactory FACTORY = new ObjectFactory() {
 *             public Object create() { return new PositionVelocity(); }
 *         };
 *         public Position position;
 *         public Velocity velocity;
 *     }</pre></p>
 * <p> It is also possible to {@link #cleanup cleanup} factories' objects for 
 *     future reuse (e.g. to release system resources or to clear external 
 *     references immediately after usage):<pre>
 *     static final ObjectFactory ARRAY_LIST_FACTORY = new ObjectFactory() {
 *         public Object create() {
 *              return new ArrayList(256);
 *         }
 *         public void cleanup(Object obj) {
 *             // Clear list after usage for immediate garbage collection.
 *             ((ArrayList)obj).clear(); 
 *         }  
 *     };</pre>
 * <p> The number of instances of this class is voluntarely limited (see 
 *     <a href="{@docRoot}/overview-summary.html#configuration">Javolution's 
 *     Configuration</a> for details). Instances of this class should be either 
 *     <code>static</code> or member of persistent objects.</p>
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 1.0, October 4, 2004
 */
public abstract class ObjectFactory {

    /**
     * Holds the maximum number of {@link ObjectFactory} (system property 
     * <code>"org.javolution.factories"</code>, default <code>1024</code>).
     */
    public static final int MAX;
    static {
        String str = System.getProperty("org.javolution.factories");
        MAX = (str != null) ? Integer.parseInt(str) : 1024;
    }

    /**
     * Holds the factory instances.
     */
    static ObjectFactory[] INSTANCES = new ObjectFactory[MAX];

    /**
     * Holds the current number of instances.
     */
    static volatile int Count;

    /**
     * Holds the factory index (range [0..MAX[).
     */
    private final int _index;

    /**
     * Holds the current pool when executing in a heap context.
     */
    private final Heap _heap = new Heap();

    /**
     * Default constructor.
     * 
     * @throws UnsupportedOperationException if already {@link #MAX} 
     *         factories exist. 
     */
    protected ObjectFactory() {
        synchronized (INSTANCES) {
            if (ObjectFactory.Count < MAX) {
                INSTANCES[ObjectFactory.Count] = this;
                _index = ObjectFactory.Count++;
            } else {
                throw new UnsupportedOperationException(
                        "Maximum number of factories (system property "
                                + "\"javolution.factories\", value " + MAX
                                + ") has been reached");
            }
        }
    }

    /**
     * Allocates a new object from this factory on the heap 
     * (using the <code>new</code> keyword).
     *
     * @return a new object allocated on the heap.
     */
    public abstract Object create();

    /**
     * Returns a new or recycled object from this factory.
     * Sub-classes may override this method to provide more efficient
     * implementations.
     *
     * @return <code>this.currentPool().next()</code>
     */
    public Object object() {
        PoolContext poolContext = Context.currentContext().poolContext();
        return (poolContext != null) ? 
                poolContext.getLocalPool(_index).next() : create();
    }
    
    /**
     * Cleans-up this factory's objects for future reuse. 
     * When overriden, this method is called on objects being recycled to 
     * dispose of system resources or to clear references to external
     * objects potentially on the heap (it allows these external objects to
     * be garbage collected immediately and therefore reduces the memory 
     * footprint).
     * <p> Note: Whether or not this method should be overriden depends mostly 
     *           on the association types that the factory's objects have with
     *           other objects. For composition associations (containment per 
     *           value), there is usually no need to cleanup. For other types 
     *           of relationship cleaning up is recommended.</p>   
     *
     * @param  obj the object product of this factory being recycled.
     * @throws UnsupportedOperationException if this factory does not 
     *         support object clean-up (default).
     */
    public void cleanup(Object obj) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the pool for the current thread. If the current thread is not
     * executing in a {@link PoolContext}, then {@link #heap} is returned.
     * 
     * @return the pool for the current thread or {@link #heap} when the
     *         current thread executes in a {@link HeapContext}. 
     */
    public final ObjectPool currentPool() {
        PoolContext poolContext = Context.currentContext().poolContext();
        return (poolContext != null) ? 
                poolContext.getLocalPool(_index) : _heap;
    }    

    /**
     * Returns the pool from this factory which represents the heap.
     * Allocating from this pool always calls {@link #create}.
     * 
     * @return the unique pool for which recycling is performed through garbage 
     *         collection exclusively.
     */
    public final ObjectPool heap() {
        return _heap;
    }

    /**
     * Creates a new pool for this object factory.
     * Sub-classes may override this method in order to use custom pools.
     * 
     * @return a default implementation for {@link ObjectPool}.
     */
    protected ObjectPool newPool() {
        return new DefaultPool(this);
    }

    /**
     * This inner class represents the heap pool (non-recycling pool). An 
     * instance of this class is returned by {@link ObjectFactory#currentPool}
     * when the current thread executes within a {@link HeapContext}. 
     */
    private final class Heap extends ObjectPool {

        // Implements ObjectPool abstract method.
        public Object next() {
            return create();
        }

        // Implements ObjectPool abstract method.
        public void recycle(Object obj) {
        }

        // Implements ObjectPool abstract method.
        public void export(Object obj) {
        }

        // Implements ObjectPool abstract method.
        public void remove(Object obj) {
        }

        // Implements ObjectPool abstract method.
        protected void recycleAll() {
        }

        // Implements ObjectPool abstract method.
        protected void clearAll() {
        }
    }
}