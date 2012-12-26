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

/**
 * <p> This abstract class represents the current memory allocator context.
 *     This context specifies how the memory is allocated ("new" object).
 *     The default implementation is an instance of {@link HeapContext}. 
 *     Specialized JVM  (e.g. RTSJ compliant) can support additional modes of
 *     allocation (e.g. stack allocation).</p>
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
     * Executes the specified logic allocating memory using this 
     * allocator context regardless of the current allocator context.
     *
     * @param logic the logic to be executed.
     */
    protected abstract void execute(Runnable logic);

    /**
     * Copies the specified object allocating memory using this allocator  
     * context regardless of the current allocator context.
     *
     * @param copyable the object to be deeply copied.
     */
    protected abstract <T extends Copyable> T copy(T copyable);
    
}