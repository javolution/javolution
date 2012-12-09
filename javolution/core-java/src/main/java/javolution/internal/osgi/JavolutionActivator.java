/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2006 - Javolution (http://javolution.org/)
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
import javolution.osgi.ConfigurableService;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.log.*;
import org.osgi.util.tracker.ServiceTracker;

/**
 * This class implements Javolution OSGi bundle activator.
 * This class makes available all the services tracker required by Javolution to 
 * perform.
 */
public class JavolutionActivator implements BundleActivator {

    // Holds the activator instance.
    private static JavolutionActivator CURRENT = new JavolutionActivator(); 

    // Trackers for the services used by Javolution.
    private ServiceTracker<LogService, LogService> logServiceTracker;
    
    // Services provided by Javolution.
    private ServiceRegistration<ManagedService> configurableServiceRegistration;
   
    // Trackers for context factories.
    private ServiceTracker<ConcurrentContext.Factory, ConcurrentContext.Factory> 
            concurrentContextFactoryTracker;
    private ServiceTracker<HeapContext.Factory, HeapContext.Factory> 
            heapContextFactoryTracker;
    private ServiceTracker<LocalContext.Factory, LocalContext.Factory> 
            localContextFactoryTracker;
    private ServiceTracker<LogContext.Factory, LogContext.Factory> 
            logContextFactoryTracker;
    private ServiceTracker<SecurityContext.Factory, SecurityContext.Factory> 
            securityContextFactoryTracker;
    private ServiceTracker<StackContext.Factory, StackContext.Factory> 
            stackContextFactoryTracker;

    
    public void start(BundleContext bc) throws Exception {
        CURRENT = this;
        
        // Tracks LogService implementations.
        logServiceTracker = new ServiceTracker(bc, LogService.class, null);
        logServiceTracker.open();
        
        // Tracks ConcurrentContext.Factory implementations.
        concurrentContextFactoryTracker = new ServiceTracker(bc, ConcurrentContext.Factory.class, null);
        concurrentContextFactoryTracker.open();
       
        // Tracks HeapContext.Factory implementations.
        heapContextFactoryTracker = new ServiceTracker(bc, HeapContext.Factory.class, null);
        heapContextFactoryTracker.open();
       
        // Tracks LocalContext.Factory implementations.
        localContextFactoryTracker = new ServiceTracker(bc, LocalContext.Factory.class, null);
        localContextFactoryTracker.open();
        
        // Tracks LogContext.Factory implementations.
        logContextFactoryTracker = new ServiceTracker(bc, LogContext.Factory.class, null);
        logContextFactoryTracker.open();
        
        // Tracks SecurityContext.Factory implementations.
        securityContextFactoryTracker = new ServiceTracker(bc, SecurityContext.Factory.class, null);
        securityContextFactoryTracker.open();
       
        // Tracks StackContext.Factory implementations.
        stackContextFactoryTracker = new ServiceTracker(bc, StackContext.Factory.class, null);
        stackContextFactoryTracker.open();

        // Publish Javolution Configuration service.
        ConfigurableService cs = new ConfigurableService("Javolution");
        configurableServiceRegistration = bc.registerService(ManagedService.class, cs, cs.getProperties());
    }

    public void stop(BundleContext bc) throws Exception {
        if (configurableServiceRegistration != null) {
             configurableServiceRegistration.unregister();
             configurableServiceRegistration = null;
        }

        CURRENT = new JavolutionActivator();
    }

    /**
     * Returns an OSGi published implementation of {@link ConcurrentContext.Factory}
     * or <code>null</code> if none.
     * 
     * @return the published service implementation or or <code>null</code>.
     */
    public static ConcurrentContext.Factory getConcurrentContextFactory() {
        ServiceTracker<ConcurrentContext.Factory, ConcurrentContext.Factory> tracker = 
                JavolutionActivator.CURRENT.concurrentContextFactoryTracker;
        return tracker != null ? tracker.getService() : null;
    }
    
    /**
     * Returns an OSGi published implementation of {@link HeapContext.Factory}
     * or <code>null</code> if none.
     * 
     * @return the published service implementation or or <code>null</code>.
     */
    public static HeapContext.Factory getHeapContextFactory() {
        ServiceTracker<HeapContext.Factory, HeapContext.Factory> tracker = 
                JavolutionActivator.CURRENT.heapContextFactoryTracker;
        return tracker != null ? tracker.getService() : null;
    }
    
    /**
     * Returns an OSGi published implementation of {@link LocalContext.Factory}
     * or <code>null</code> if none.
     * 
     * @return the published service implementation or or <code>null</code>.
     */
    public static LocalContext.Factory getLocalContextFactory() {
        ServiceTracker<LocalContext.Factory, LocalContext.Factory> tracker = 
                JavolutionActivator.CURRENT.localContextFactoryTracker;
        return tracker != null ? tracker.getService() : null;
    }

    /**
     * Returns an OSGi published implementation of {@link LogContext.Factory}
     * or <code>null</code> if none.
     * 
     * @return the published service implementation or or <code>null</code>.
     */
    public static LogContext.Factory getLogContextFactory() {
        ServiceTracker<LogContext.Factory, LogContext.Factory> tracker = 
                JavolutionActivator.CURRENT.LogContextFactoryTracker;
        return tracker != null ? tracker.getService() : null;
    }

    /**
     * Returns an OSGi published implementation of {@link SecurityContext.Factory}
     * or <code>null</code> if none.
     * 
     * @return the published service implementation or or <code>null</code>.
     */
    public static SecurityContext.Factory getSecurityContextFactory() {
        ServiceTracker<SecurityContext.Factory, SecurityContext.Factory> tracker = 
                JavolutionActivator.CURRENT.securityContextFactoryTracker;
        return tracker != null ? tracker.getService() : null;
    }

    /**
     * Returns an OSGi published implementation of {@link StackContext.Factory}
     * or <code>null</code> if none.
     * 
     * @return the published service implementation or or <code>null</code>.
     */
    public static StackContext.Factory getStackContextFactory() {
        ServiceTracker<StackContext.Factory, StackContext.Factory> tracker = 
                JavolutionActivator.CURRENT.stackContextFactoryTracker;
        return tracker != null ? tracker.getService() : null;
    }
        
}