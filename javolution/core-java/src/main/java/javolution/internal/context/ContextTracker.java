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

    public ContextTracker(Class<C> type) {
        this.type = type;
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

    /**
     * Returns published context service or <code>null</code> if none. 
     * @param waitForService indicates if this method blocks until a service
     *        is available.
     */
    public synchronized C getService(boolean waitForService, C defaultImpl) {
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
        return defaultImpl; 
    }
}
