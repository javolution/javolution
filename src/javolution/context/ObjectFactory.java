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

/**
 * <p> This class represents an object factory; it allows for object 
 *     recycling, pre-allocation and stack allocations.
 *     
 * <p> Object factories are recommended over class constructors (ref. "new" 
 *     keyword) to allows for custom allocation policy (see 
 *     {@link AllocatorContext}). For example:[code]
 *     static ObjectFactory<int[][]> BOARD_FACTORY = new ObjectFactory<int[][]>() { 
 *         protected int[][] create() {
 *             return new int[8][8];
 *         }
 *     };
 *     ...
 *     int[][] board = BOARD_FACTORY.object(); 
 *         // The board object might have been preallocated at start-up,
 *         // it might also be on the thread "stack/pool" for threads 
 *         // executing in a StackContext. 
 *     ...
 *     BOARD_FACTORY.recycle(board); // Immediate recycling of the board object (optional).                      
 *     [/code]</p>
 *     
 * <p> For arrays of variable length {@link ArrayFactory} is recommended.</p>
 *          
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 5.0, May 6, 2007
 */
public abstract class ObjectFactory/*<T>*/{

    /**
     * Indicates if object queues are used. Runtime compiler should 
     * be able to optimize generated code based upon this flag (static final).
     */
    static final boolean USE_QUEUES = ((Boolean) ObjectQueue.QUEUES_ENABLED.get())
            .booleanValue();

    /**
     * Indicates if the objects products of this factory require
     * {@link #cleanup(Object) cleanup} when recycled.
     */
    private boolean _doCleanup = true;

    /**
     * Holds the thread-local queue for this factory (not always current).
     */
    private ThreadLocal _currentQueue = new ThreadLocal() {
        protected Object initialValue() {
            return NULL;
        }
    };

    private static final ObjectQueue NULL = new ObjectQueue(null) {

        protected Object allocate() {
            return null;
        }

        protected void recycle(Object object) {
        }

    };

    /**
     * Default constructor.
     */
    protected ObjectFactory() {
    }

    /**
     * Returns a factory object possibly recycled or preallocated.
     * This method is equivalent to <code>currentQueue().next()</code>.
     * 
     * @return a recycled, pre-allocated or new factory object.
     */
    public final Object/*{T}*/object() {
        if (!USE_QUEUES)
            return create();
        final ObjectQueue/*<T>*/queue = _queue;
        return queue.user == Thread.currentThread() ? queue.next() 
                : currentQueue().next();
    }

    private ObjectQueue/*<T>*/_queue = NULL; // Hopefully in the cache.   

    /**
     * Recycles the specified object in the current queue of this factory.
     * This method is equivalent to <code>currentQueue().recycle(obj)</code>.
     * 
     * @param obj the object to be recycled.
     */
    public final void recycle(Object/*{T}*/obj) {
        if (USE_QUEUES) {
            currentQueue().recycle(obj);
        }
    }

    /**
     * Returns this object queue for the current thread (equivalent 
     * to <code>AllocatorContext.current().getQueue(this)</code>).
     * The current queue is used only if queues are 
     * {@link ObjectQueue#QUEUES_ENABLED enabled}.
     * 
     * @return the current object queue for this factory. 
     */
    public final ObjectQueue/*<T>*/currentQueue() {
        ObjectQueue/*<T>*/queue = (ObjectQueue/*<T>*/) _currentQueue.get();
        if (queue.user != null)
            return _queue = queue;
        queue = ((AllocatorContext) AllocatorContext.current()).getQueue(this);
        _currentQueue.set(queue);
        return _queue = queue;
    }

    /**
     * Constructs a new object for this factory (using the <code>new</code> 
     * keyword).
     *
     * @return a new factory object.
     */
    protected abstract Object/*{T}*/create();

    /**
     * Cleans-up this factory's objects for future reuse. 
     * When overriden, this method is called on objects being recycled to 
     * dispose of system resources or to clear references to external
     * objects potentially on the heap (it allows these external objects to
     * be garbage collected immediately and therefore reduces the memory 
     * footprint). For example:[code]
     *     static ObjectFactory<ArrayList> ARRAY_LIST_FACTORY = new ObjectFactory<ArrayList>() { 
     *         protected ArrayList create() {
     *             return new ArrayList();
     *         }
     *         protected void cleanup(ArrayList obj) {
     *             obj.clear(); // Clears external references.
     *         }
     *     };[/code]
     *
     * @param  obj the factory object being recycled.
     */
    protected void cleanup(Object/*{T}*/obj) {
        _doCleanup = false;
    }

    /**
     * Indicates if this factory requires cleanup. 
     *
     * @return <code>true</code> if {@link #cleanup} is overriden and 
     *         {@link #cleanup} has been called at least once; 
     *         <code>false</code> otherwise.
     */
    protected final boolean doCleanup() {
        return _doCleanup;
    }

}