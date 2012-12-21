/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.context;

import static javolution.internal.osgi.JavolutionActivator.HEAP_CONTEXT_TRACKER;
import javolution.text.TypeFormat;

/**
 * <p> This abstract class represents an {@link AllocatorContext} always 
 *     allocating from the heap (default Java allocation). This context 
 *     can be useful when executing in a {@link StackContext} to perform 
 *     allocations on the heap (e.g. to update static variables).</p>
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0, December 12, 2012
 */
public abstract class HeapContext extends AllocatorContext<HeapContext> {

    /**
     * Indicates whether or not static methods will block for an OSGi published
     * implementation this class (default configuration <code>false</code>).
     * This parameter cannot be locally overriden.
     */
    public static final LocalParameter<Boolean> WAIT_FOR_SERVICE = new LocalParameter(false) {
        @Override
        public void configure(CharSequence configuration) {
            setDefault(TypeFormat.parseBoolean(configuration));
        }

        @Override
        public void checkOverridePermission() throws SecurityException {
            throw new SecurityException(this + " cannot be overriden");
        }
    };

    /**
     * Default constructor.
     */
    protected HeapContext() {
    }

    /**
     * Enters a new heap context instance.
     * 
     * @return the new heap context implementation entered.
     */
    public static HeapContext enter() {
        HeapContext ctx = AbstractContext.current(HeapContext.class);
        if (ctx != null) return ctx.inner().enterScope();
        return HEAP_CONTEXT_TRACKER.getService(WAIT_FOR_SERVICE.getDefault()).inner().enterScope();
    }
}