/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.osgi.internal;

import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

/**
 * Bridge to service tracker (does not trigger class loading exception if running outside OSGi).
 */
public final class ServiceTrackerImpl<C> {

    private volatile ServiceTracker<C, C> tracker;
    private final Class<C> type;
    private final Class<? extends C> defaultImplClass;
    private C defaultImpl;

    /** Creates a context tracker for the specified context type. */
    public ServiceTrackerImpl(Class<C> type, Class<? extends C> defaultImplClass) {
        this.defaultImplClass = defaultImplClass;
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

    /** Returns the published services or the default implementation if none. */
	public Object[] getServices() {
        ServiceTracker<C, C> trk = tracker;
        if (trk != null) {
            Object[] services = trk.getServices();
            if (services != null) return services;        
        }
        if (defaultImplClass == null) return new Object[0];
        synchronized (this) {
            if (defaultImpl == null) {
                try {
                    defaultImpl = defaultImplClass.newInstance();
                } catch (Throwable error) {
                    throw new RuntimeException(error);
                } 
            }
        }
        return new Object[] { defaultImpl };
    }
}
