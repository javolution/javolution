/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.context;

import javolution.lang.Copyable;
import javolution.lang.Factory;

/**
 * <p> The parent class for all memory allocation contexts.
 *     Sub-classes specifies how the memory is allocated ("new" objects).
 *     The default implementation is an instance of {@link HeapContext}. 
 *     Specialized JVMs support additional modes of allocations (e.g. 
 *     stack allocations using RTSJ scoped memory).</p>
 * 
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0 December 12, 2012
 * @link HeapContext
 * @link StackContext
 */
public abstract class AllocatorContext<C extends AllocatorContext> extends AbstractContext<C> {

    /**
     * Default constructor.
     */
    protected AllocatorContext() {
    }

    /**
     * Returns the current allocator context or <code>null</code> if none
     * (default heap allocation).
     */
    public static AllocatorContext currentAllocatorContext() {
        return AbstractContext.current(AllocatorContext.class);        
    }

    /**
     * Executes the specified logic allocating memory using this 
     * allocator context.
     */
    protected abstract void executeInContext(Runnable logic);
    
    /**
     * Returns a new instance allocated on the heap and produced by the 
     * specified factory (convenience method).
     */
    protected <T> T allocateInContext(Factory<T> factory) {
        Allocator<T> allocator = new Allocator(factory);
        executeInContext(allocator);
        return allocator.instance;
    }

    /**
     * Returns a copy of the specified object allocated on the heap
     * (convenience method).
     */
    protected <T> T copyInContext(Copyable<T> obj) {
        Copier<T> copier = new Copier(obj);
        executeInContext(copier);
        return copier.objCopy;
    }

    // Runnable to allocate a new instance on the heap.
    private static class Allocator<T> implements Runnable {

        private final Factory<T> factory;

        T instance;

        public Allocator(Factory<T> factory) {
            this.factory = factory;
        }

        public void run() {
            instance = factory.create();
        }

    }

    // Runnable to allocate a new instance on the heap.
    private static class Copier<T> implements Runnable {

        private final Copyable<T> obj;

        T objCopy;

        public Copier(Copyable<T> obj) {
            this.obj = obj;
        }

        public void run() {
            objCopy = obj.copy();
        }

    }
    
}