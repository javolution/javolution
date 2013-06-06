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
import javolution.context.ImmortalContext;
import javolution.context.LocalContext;
import javolution.context.LogContext;
import javolution.context.SecurityContext;
import javolution.context.StackContext;
import javolution.internal.context.ContextTracker;
import javolution.osgi.ConfigurableService;
import javolution.text.TextContext;
import javolution.util.Initializer;
import javolution.xml.XMLContext;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.log.LogService;
import org.osgi.util.tracker.ServiceTracker;

/**
 * Javolution OSGi bundle activator.
 * 
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0, December 12, 2012
 */
public class JavolutionActivator implements BundleActivator {

    public final static ContextTracker<ConcurrentContext> CONCURRENT_CONTEXT_TRACKER 
        = new ContextTracker<ConcurrentContext>(ConcurrentContext.class);
    public final static ContextTracker<HeapContext> HEAP_CONTEXT_TRACKER 
       = new ContextTracker<HeapContext>(HeapContext.class);
    public final static ContextTracker<ImmortalContext> IMMORTAL_CONTEXT_TRACKER 
    = new ContextTracker<ImmortalContext>(ImmortalContext.class);
    public final static ContextTracker<LocalContext> LOCAL_CONTEXT_TRACKER 
       = new ContextTracker<LocalContext>(LocalContext.class);
    public final static ContextTracker<LogContext> LOG_CONTEXT_TRACKER 
        = new ContextTracker<LogContext>(LogContext.class);
    public final static ContextTracker<SecurityContext> SECURITY_CONTEXT_TRACKER 
        = new ContextTracker<SecurityContext>(SecurityContext.class);
    public final static ContextTracker<StackContext> STACK_CONTEXT_TRACKER 
        = new ContextTracker<StackContext>(StackContext.class);
    public final static ContextTracker<TextContext> TEXT_CONTEXT_TRACKER
       = new ContextTracker<TextContext>(TextContext.class);
    public final static ContextTracker<XMLContext> XML_CONTEXT_TRACKER
       = new ContextTracker<XMLContext>(XMLContext.class);

    private ServiceTracker<LogService, LogService> logServiceTracker;
    private static JavolutionActivator INSTANCE;

    // Services provided by Javolution.
    private ServiceRegistration<ManagedService> configurableServiceRegistration;

    public void start(BundleContext bc) throws Exception {
        INSTANCE = this;

        // Initialize all classes during bundle activation.
        initializeAll();

        // Tracks the OSGi log service
        logServiceTracker = new ServiceTracker<LogService, LogService>(bc,
                LogService.class.getName(), null);
        logServiceTracker.open();

        CONCURRENT_CONTEXT_TRACKER.activate(bc);
        HEAP_CONTEXT_TRACKER.activate(bc);
        IMMORTAL_CONTEXT_TRACKER.activate(bc);
        LOCAL_CONTEXT_TRACKER.activate(bc);
        LOG_CONTEXT_TRACKER.activate(bc);
        SECURITY_CONTEXT_TRACKER.activate(bc);
        STACK_CONTEXT_TRACKER.activate(bc);
        TEXT_CONTEXT_TRACKER.activate(bc);
        XML_CONTEXT_TRACKER.activate(bc);

        // Publish Javolution Configuration service.
        ConfigurableService cs = new ConfigurableService("Javolution");
        configurableServiceRegistration = bc.registerService(
                ManagedService.class, cs, cs.getProperties());
    }

    public void stop(BundleContext bc) throws Exception {
        CONCURRENT_CONTEXT_TRACKER.deactivate(bc);
        HEAP_CONTEXT_TRACKER.deactivate(bc);
        IMMORTAL_CONTEXT_TRACKER.deactivate(bc);
        LOCAL_CONTEXT_TRACKER.deactivate(bc);
        LOG_CONTEXT_TRACKER.deactivate(bc);
        SECURITY_CONTEXT_TRACKER.deactivate(bc);
        STACK_CONTEXT_TRACKER.deactivate(bc);
        TEXT_CONTEXT_TRACKER.deactivate(bc);
        XML_CONTEXT_TRACKER.deactivate(bc);

        if (configurableServiceRegistration != null) {
            configurableServiceRegistration.unregister();
            configurableServiceRegistration = null;
        }
        INSTANCE = null;
    }

    public static LogService getLogService() {
        JavolutionActivator instance = INSTANCE;
        if (instance == null)
            return null;
        ServiceTracker<LogService, LogService> tracker = instance.logServiceTracker;
        if (tracker == null)
            return null;
        return tracker.getService();
    }

    /** Initializes all Javolution classes; returns <code>true</code> if 
     *  all classes have been successfully initialized. */
    public static boolean initializeAll() {
        Initializer initializer = new Initializer(
                JavolutionActivator.class.getClassLoader());
        // Loads classes not yet referenced (directly or indirectly).
        initializer.loadClass("javolution.internal.xml.stream.XMLInputFactoryImpl");
        initializer.loadClass("javolution.internal.xml.stream.XMLOutputFactoryImpl");
        initializer.initializeLoadedClasses(); // Recursive loading/initialization.
        return initializer.isInitializationSuccessful();
    }
}