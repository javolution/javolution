/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.osgi.internal;

import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

/**
 * Mutex-free wrapper around a service tracker.
 */
public final class ServiceTrackerImpl<C> {

    private volatile ServiceTracker<C, C> tracker;
    private final Class<C> type;
    private final C defaultImpl;

    /** Creates a context tracker for the specified context type. */
    public ServiceTrackerImpl(Class<C> type, C defaultImpl) {
        this.defaultImpl = defaultImpl;
        this.type = type;
    }

    /** Activates OSGi tracking. */
    public void activate(BundleContext bc) {
        ServiceTracker<C, C> trk = new ServiceTracker<C, C>(bc, type, null);
        trk.open();
        tracker = trk;
    }

    /** Deactivates OSGi tracking. */
    public void deactivate(BundleContext bc) {
        tracker.close();
        tracker = null;
    }

    /** Returns the published service or the default implementation if none. */
    public C getService() {
        ServiceTracker<C, C> trk = tracker;
        if (trk == null)
            return defaultImpl;
        C service = trk.getService();
        return (service != null) ? service : defaultImpl;
    }
}
