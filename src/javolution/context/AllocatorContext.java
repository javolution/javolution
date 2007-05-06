/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2007 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.context;

/**
 * <p> This class represents an allocator context; it defines 
 *     the allocation policy of {@link ObjectFactory} products.
 *     For example, {@link HeapContext} allocates from the heap while
 *     {@link StackContext} allocates from thread-local objects's queues
 *     or <code>ScopedMemory</code> (RTSJ).</p>
 *      
 * <p> Custom allocator contexts may use aging pools (where objects sufficiently 
 *     old are recycled), switchable spaces (objects from a particular frame are 
 *     recycled when buffers are swapped), etc.</p>
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 5.0, April 14, 2006
 */
public abstract class AllocatorContext extends Context {

    /**
     * Default constructor.
     */
    protected AllocatorContext() {
    }

    /**
     * Returns the current allocator context.
     *
     * @return the current allocator context.
     */
    public static /*AllocatorContext*/Context current() {
        for (Context ctx = Context.current(); ctx != null; ctx = ctx.getOuter()) {
            if (ctx instanceof AllocatorContext)
                return (AllocatorContext) ctx;
        }
        return HeapContext._Default;
    }

    /**
     * Returns the current queue for the specified factory in this context.
     * 
     * @param factory the factory responsible for the object fabrication.
     * @return the queue of ready to use objects.
     */
    protected abstract ObjectQueue getQueue(ObjectFactory factory);

   /**
    * This method is called when an inner allocator context is entered
    * by the current thread, when exiting this allocator context or 
    * when a concurrent executor has completed its task using this 
    * allocator context.
    */
    protected abstract void deactivate();
  
    // Implements Context abstract method.
    protected void enterAction() {
        // Find outer AllocatorContext.
        AllocatorContext outer = HeapContext._Default;
        for (Context ctx = this.getOuter(); ctx != null; ctx = ctx.getOuter()) {
            if (ctx instanceof AllocatorContext) {
                outer = (AllocatorContext) ctx;
                break;
            }
        }
        outer.deactivate();
    }

    // Implements Context abstract method.
    protected void exitAction() {
        this.deactivate();
    }

    /**
     * <p> This class represents a {@link javolution.lang.Reference reference}
     *     allocated from the current {@link AllocatorContext}. 
     *     The reachability level of this reference is the scope of the 
     *     {@link AllocatorContext} in which it has been 
     *     {@link #newInstance created}.</p>
     *     
     * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
     * @version 5.0, April 14, 2007
     */
    public static class Reference/*<T>*/ implements
            javolution.lang.Reference/*<T>*/{

        /**
         * Holds the factory.
         */
        private static final ObjectFactory FACTORY = new ObjectFactory() {
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
         * Default constructor.
         */
        public Reference() {
        }

        /**
         * Returns a new stack reference instance allocated on the current stack
         * when executing in a {@link StackContext}.
         * 
         * @return a reference object possibly recycled.
         */
        public static/*<T>*/Reference /*<T>*/newInstance() {
            return (Reference) FACTORY.object();
        }

        // Implements Reference interface.
        public final Object/*{T}*/get() {
            return _value;
        }

        // Implements Reference interface.
        public final void set(Object/*{T}*/value) {
            _value = value;
        }
    }

}