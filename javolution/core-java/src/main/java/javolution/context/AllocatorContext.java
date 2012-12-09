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
 * <p> This abstract class represents the memory allocator context.
 *     The allocation context specifies how the memory is allocated for new 
 *     objects. The default allocator context is an instance of 
 *     {@link HeapContext}. Some JVM (e.g. RTSJ compliant VM) may support 
 *     additional mode of allocation such as the stack 
 *     (see {@link StackContext}).</p>
 * 
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0 December 12, 2012
 */
public abstract class AllocatorContext extends AbstractContext<AllocatorContext> {
            
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