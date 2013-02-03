/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.internal.context;

import javolution.context.AbstractContext;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

/**
 * This class represents a context service tracker.
 * 
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0, December 12, 2012
 */
public final class ContextTracker<C extends AbstractContext<C>> {

    ServiceTracker<C, C> tracker;

    final Class<C> type;

    final Class<? extends C> defaultImplClass;

    C defaultImpl;

    // This constructor does not cause the initialization/creation of the 
    // default implementation (avoid class initialization circularities.
    public ContextTracker(Class<C> type, Class<? extends C> defaultImplClass) {
        this.type = type;
        this.defaultImplClass = defaultImplClass;
    }

    public synchronized void activate(BundleContext bc) {
        tracker = new ServiceTracker<C, C>(bc, type, null);
        tracker.open();
        this.notify();
    }

    public synchronized void deactivate(BundleContext bc) {
        tracker.close();
        tracker = null;
    }

    public synchronized C getService(boolean waitForService) {
        try {
            if (waitForService && (tracker == null)) {
                while (tracker == null) {
                    this.wait();
                }
            }
            if (tracker != null) { // Activated.
                if (waitForService) return tracker.waitForService(0);
                C ctx = tracker.getService();
                if (ctx != null) return ctx;
            }
        } catch (InterruptedException ex) {
            // Stop waiting. 
        }
        // No OSGi service.
        if (defaultImpl != null) return defaultImpl;
        try {         
            defaultImpl = defaultImplClass.newInstance();
            return defaultImpl;
        } catch (InstantiationException ex) {
            throw new RuntimeException(ex);
        } catch (IllegalAccessException ex) {
            throw new RuntimeException(ex);
        }
    }
}
