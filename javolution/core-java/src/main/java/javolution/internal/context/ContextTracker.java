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
import javolution.context.LogContext;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

/**
 * This class represents a context service tracker.
 * 
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0, December 12, 2012
 */
public final class ContextTracker<C extends AbstractContext> {

    ServiceTracker<C, C> tracker;
    final Class<C> type;
    final C defaultImpl;

    public ContextTracker(Class<C> type, C defaultImpl) {
        this.type = type;
        this.defaultImpl = defaultImpl;
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

    public C getService(boolean waitForService) {
        try {
            ServiceTracker<C, C> trk = tracker;
            if (trk != null) {
                if (waitForService) {
                    synchronized (this) {
                        while (tracker == null) {
                            this.wait();
                        }
                        trk = tracker;
                    }
                } else {
                    return defaultImpl;
                }
            }
            C ctx = waitForService ? trk.waitForService(0) : trk.getService();
            return (ctx != null) ? ctx : defaultImpl;
        } catch (InterruptedException ex) {
            LogContext.error(ex);
            return defaultImpl;
        }
    }
}
