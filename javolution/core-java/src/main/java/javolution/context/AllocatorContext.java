/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2007 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.context;

import javolution.lang.Configurable;
import javolution.lang.ValueType;

/**
 * <p> This class represents an allocator context; it defines the 
 *     the allocation policy of the objects produced by
 *     {@link ObjectFactory}.</p>
 *     
 * <p> The {@link #DEFAULT default} context used by new threads is
 *     {@link HeapContext}. {@link ConcurrentContext} threads inherits
 *     the allocator context from their parent thread (the one which
 *     entered the concurrent context).</p>
 *     
 * <p> Specializations may allocate from thread-local stacks
 *     (e.g.{@link StackContext}), shared pools (e.g. {@link PoolContext}),
 *     specific memory areas (e.g. {@link ImmortalContext}) or using any user
 *     defined policy such as aging pools (where
 *     objects sufficiently old are recycled), switchable spaces (objects from
 *     recycled when buffers are swapped), etc.</p>
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 5.2, August 19, 2007
 */
public abstract class AllocatorContext extends Context {

    /**
     * Holds the default allocator context instance.
     */
    private static volatile AllocatorContext _Default = new HeapContext();
    /**
     * Holds the default allocator context shared by all newly created threads
     * (the default is a {@link HeapContext} instance).
     * The default allocator context is configurable. For example the following
     * runs the javolution built-in tests with a {@link PoolContext} as default
     * (javolution built-in tests {@link javolution.lang.Configurable#read
     * loads} their configuration from systems properties)[code]
     *   > java -Djavolution.AllocatorContext.Default=javolution.context.PoolContext -jar javolution.jar time
     * [/code]
     */
    public static final Configurable <Class<? extends AllocatorContext>>  DEFAULT = new Configurable(HeapContext.class) {

        protected void notifyChange(Object oldValue, Object newValue) {
            _Default = (AllocatorContext) ObjectFactory.getInstance(
                    (Class) newValue).object();
        }
    };

    /**
     * Default constructor.
     */
    protected AllocatorContext() {
    }

    /**
     * Returns the current allocator context. If the current thread has 
     * not entered an allocator context (e.g. new thread) then 
     * {@link #getDefault()} is returned.
     *
     * @return the current allocator context.
     */
    public static AllocatorContext getCurrentAllocatorContext() {
        return Context.getCurrentContext().getAllocatorContext();
    }

    /**
     * Returns the default instance ({@link #DEFAULT} implementation).
     * 
     * @return the default instance.
     */
    public static AllocatorContext getDefault() {
        return AllocatorContext._Default;
    }

    /**
     * Returns the allocator for the specified factory in this context.
     * 
     * @param factory the factory for which the allocator is returned.
     * @return the allocator producing instances of the specified factory.
     */
    protected abstract Allocator getAllocator(ObjectFactory factory);

    /**
     * Deactivates the {@link Allocator allocators} belonging to this context
     * for the current thread. This method is typically called when an inner 
     * allocator context is entered by the current thread, when exiting an 
     * allocator context or when a concurrent executor has completed its task
     * within this allocator context. Deactivated allocators have no
     * {@link Allocator#user user} (<code>null</code>).
     */
    protected abstract void deactivate();

    /**
     * Performs a copy of the specified value allocated outside of the
     * current allocator context.
     *
     * @param value the value to be copied.
     * @return a copy allocated using the outer allocator.
     */
    public static <T extends ValueType>   T  outerCopy(
             T  value) {
        Context.enter(OuterContext.class);
        try {
            Object copy = value.copy();
            return ( T ) copy;
        } finally {
            Context.exit(OuterContext.class);
        }
    }

    /**
     * Performs a copy of the specified values outside of the
     * current stack context (convenience method). This method is
     * equivalent to:[code]
     *  AllocatorContext.outerExecute(new Runnable() {
     *      public void run() {
     *         for (int i = 0; i < values.length; i++) {
     *             values[i] = {ValueType) values[i].copy();
     *         }
     *     }
     *  });[/code]
     * @param values the array whose elements are exported.
     */
    public static void outerCopy(ValueType[] values) {
        Context.enter(OuterContext.class);
        try {
            for (int i = 0; i < values.length; i++) {
                values[i] = (ValueType) values[i].copy();
            }
        } finally {
            Context.exit(OuterContext.class);
        }
    }

    /**
     * Executes the specified logic outside of the current allocator context.
     *
     * @param logic the logic to be executed outside of the current stack
     *        context.
     */
    public static void outerExecute(Runnable logic) {
        Context.enter(OuterContext.class);
        try {
            logic.run();
        } finally {
            Context.exit(OuterContext.class);
        }
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
    public static class Reference <T>  implements javolution.lang.Reference <T>  {

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
        private  T  _value;

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
        public static <T>  Reference  <T>  newInstance() {
            return (Reference) FACTORY.object();
        }

        /**
         * Returns the string representation of the current value of 
         * this reference.
         * 
         * @return <code>String.valueOf(this.get())</code>
         */
        public String toString() {
            return String.valueOf(_value);
        }

        // Implements Reference interface.
        public final  T  get() {
            return _value;
        }

        // Implements Reference interface.
        public final void set( T  value) {
            _value = value;
        }
    }

    private static class OuterContext extends AllocatorContext {

        private AllocatorContext _outer;
        private AllocatorContext _outerOuter;

        protected Allocator getAllocator(ObjectFactory factory) {
            return _outerOuter.getAllocator(factory);
        }

        protected void deactivate() {
            _outerOuter.deactivate();
        }

        protected void enterAction() {
            _outer = getOuter().getAllocatorContext();
            Context outer = _outer.getOuter();
            if (outer == null)
                // If no outer allocator context, then we keep the current one.
                _outerOuter = _outer;
            else {
                _outerOuter = outer.getAllocatorContext();
                _outer.deactivate();
            }
        }

        protected void exitAction() {
            if (_outer != _outerOuter)
                _outerOuter.deactivate();
            _outer = null;
            _outerOuter = null;
        }
    }

    // Allows instances of private classes to be factory produced.
    static {
        ObjectFactory.setInstance(new ObjectFactory() {

            protected Object create() {
                return new OuterContext();
            }
        }, OuterContext.class);
    }
}