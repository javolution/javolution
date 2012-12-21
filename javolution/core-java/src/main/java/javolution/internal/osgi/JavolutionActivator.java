/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.internal.osgi;

import javolution.context.ConcurrentContext;
import javolution.context.HeapContext;
import javolution.context.LocalContext;
import javolution.context.LogContext;
import javolution.context.SecurityContext;
import javolution.context.StackContext;
import javolution.internal.context.ConcurrentContextImpl;
import javolution.internal.context.ContextTracker;
import javolution.internal.context.HeapContextImpl;
import javolution.internal.context.LocalContextImpl;
import javolution.internal.context.LogContextImpl;
import javolution.internal.context.SecurityContextImpl;
import javolution.internal.context.StackContextImpl;
import javolution.internal.text.TextContextImpl;
import javolution.osgi.ConfigurableService;
import javolution.text.TextContext;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ManagedService;

/**
 * This class implements Javolution OSGi bundle activator.
 * 
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0, December 12, 2012
 */
public class JavolutionActivator implements BundleActivator {

    public final static ContextTracker<ConcurrentContext> CONCURRENT_CONTEXT_TRACKER 
            = new ContextTracker(ConcurrentContext.class, new ConcurrentContextImpl());
    public final static ContextTracker<HeapContext> HEAP_CONTEXT_TRACKER 
            = new ContextTracker(HeapContext.class, new HeapContextImpl());
    public final static ContextTracker<LocalContext> LOCAL_CONTEXT_TRACKER 
            = new ContextTracker(LocalContext.class, new LocalContextImpl());
    public final static ContextTracker<LogContext> LOG_CONTEXT_TRACKER 
            = new ContextTracker(LogContext.class, new LogContextImpl());
    public final static ContextTracker<SecurityContext> SECURITY_CONTEXT_TRACKER 
            = new ContextTracker(SecurityContext.class, new SecurityContextImpl());
    public final static ContextTracker<StackContext> STACK_CONTEXT_TRACKER 
            = new ContextTracker(StackContext.class, new StackContextImpl());
    public final static ContextTracker<TextContext> TEXT_CONTEXT_TRACKER 
            = new ContextTracker(TextContext.class, new TextContextImpl());
    
    // Services provided by Javolution.
    private ServiceRegistration<ManagedService> configurableServiceRegistration;
   
    public void start(BundleContext bc) throws Exception {
        CONCURRENT_CONTEXT_TRACKER.activate(bc);
        HEAP_CONTEXT_TRACKER.activate(bc);
        LOCAL_CONTEXT_TRACKER.activate(bc);
        LOG_CONTEXT_TRACKER.activate(bc);
        SECURITY_CONTEXT_TRACKER.activate(bc);
        STACK_CONTEXT_TRACKER.activate(bc);
        TEXT_CONTEXT_TRACKER.activate(bc);
 
        // Publish Javolution Configuration service.
        ConfigurableService cs = new ConfigurableService("Javolution");
        configurableServiceRegistration = bc.registerService(ManagedService.class, cs, cs.getProperties());
    }

    public void stop(BundleContext bc) throws Exception {
        CONCURRENT_CONTEXT_TRACKER.deactivate(bc);
        HEAP_CONTEXT_TRACKER.deactivate(bc);
        LOCAL_CONTEXT_TRACKER.deactivate(bc);
        LOG_CONTEXT_TRACKER.deactivate(bc);
        SECURITY_CONTEXT_TRACKER.deactivate(bc);
        STACK_CONTEXT_TRACKER.deactivate(bc);
        TEXT_CONTEXT_TRACKER.deactivate(bc);
        
        if (configurableServiceRegistration != null) {
             configurableServiceRegistration.unregister();
             configurableServiceRegistration = null;
        }
    }
}