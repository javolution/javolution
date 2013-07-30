/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.osgi.internal;

import javolution.lang.Initializer;
import javolution.osgi.ConfigurableService;
import javolution.xml.stream.XMLInputFactory;
import javolution.xml.stream.XMLOutputFactory;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ManagedService;

/**
 * Javolution OSGi bundle activator.
 * 
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0, July 21, 2013
 */
public class JavolutionActivator implements BundleActivator {

    // Services provided by Javolution.
    private ServiceRegistration<ManagedService> configurableServiceRegistration;
    private ServiceRegistration<XMLInputFactory> xmlInputFactoryRegistration;
    private ServiceRegistration<XMLOutputFactory> xmlOutputFactoryRegistration;

    @SuppressWarnings("unchecked")
    public void start(BundleContext bc) throws Exception {
        // Initialize all classes during bundle activation.
        initializeAll();

        OSGiServices.CONCURRENT_CONTEXT_TRACKER.activate(bc);
        OSGiServices.HEAP_CONTEXT_TRACKER.activate(bc);
        OSGiServices.IMMORTAL_CONTEXT_TRACKER.activate(bc);
        OSGiServices.LOCAL_CONTEXT_TRACKER.activate(bc);
        OSGiServices.LOG_CONTEXT_TRACKER.activate(bc);
        OSGiServices.LOG_SERVICE_TRACKER.activate(bc);
        OSGiServices.SECURITY_CONTEXT_TRACKER.activate(bc);
        OSGiServices.STACK_CONTEXT_TRACKER.activate(bc);
        OSGiServices.TEXT_CONTEXT_TRACKER.activate(bc);
        OSGiServices.XML_CONTEXT_TRACKER.activate(bc);
        OSGiServices.XML_INPUT_FACTORY_TRACKER.activate(bc);
        OSGiServices.XML_OUTPUT_FACTORY_TRACKER.activate(bc);

        // Publish Javolution Configuration service.
        ConfigurableService cs = new ConfigurableService("Javolution");
        configurableServiceRegistration = bc.registerService(
                ManagedService.class, cs, cs.getProperties());

        // Publish XMLInputFactory/XMLOutputFactory services.
        xmlInputFactoryRegistration = (ServiceRegistration<XMLInputFactory>) bc
                .registerService(XMLInputFactory.class.getName(),
                        new XMLInputFactoryProvider(), null);
        xmlOutputFactoryRegistration = (ServiceRegistration<XMLOutputFactory>) bc
                .registerService(XMLOutputFactory.class.getName(),
                        new XMLOutputFactoryProvider(), null);

    }

    public void stop(BundleContext bc) throws Exception {
        OSGiServices.CONCURRENT_CONTEXT_TRACKER.deactivate(bc);
        OSGiServices.HEAP_CONTEXT_TRACKER.deactivate(bc);
        OSGiServices.IMMORTAL_CONTEXT_TRACKER.deactivate(bc);
        OSGiServices.LOCAL_CONTEXT_TRACKER.deactivate(bc);
        OSGiServices.LOG_CONTEXT_TRACKER.deactivate(bc);
        OSGiServices.LOG_SERVICE_TRACKER.deactivate(bc);
        OSGiServices.SECURITY_CONTEXT_TRACKER.deactivate(bc);
        OSGiServices.STACK_CONTEXT_TRACKER.deactivate(bc);
        OSGiServices.TEXT_CONTEXT_TRACKER.deactivate(bc);
        OSGiServices.XML_CONTEXT_TRACKER.deactivate(bc);
        OSGiServices.XML_INPUT_FACTORY_TRACKER.deactivate(bc);
        OSGiServices.XML_OUTPUT_FACTORY_TRACKER.deactivate(bc);

        configurableServiceRegistration.unregister();
        xmlInputFactoryRegistration.unregister();
        xmlOutputFactoryRegistration.unregister();   
    }

    /** Initializes all Javolution classes; returns <code>true</code> if 
     *  all classes have been successfully initialized. */
    public static boolean initializeAll() {
        Initializer init = new Initializer(
                JavolutionActivator.class.getClassLoader());
        // Loads classes not yet referenced (directly or indirectly).
        init.loadClass("javolution.xml.internal.stream.XMLInputFactoryImpl");
        init.loadClass("javolution.xml.internal.stream.XMLOutputFactoryImpl");
        init.initializeLoadedClasses(); // Recursive loading/initialization.
        return init.isInitializationSuccessful();
    }
}