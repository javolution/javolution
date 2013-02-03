/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.internal.osgi;

import java.util.ArrayList;
import javolution.context.LogContext;
import javolution.osgi.OSGi;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;

/**
 *  <p> This class holds the OSGi framework implementation.</p>
 */
public class OSGiImpl extends OSGi {

    ////////////////////////////////////////////////////////////////////////////
    // Converted from OSGiImpl.hpp (C++)
    //
    // Holds the bundles.
    ArrayList<Bundle> bundles;

    /**
     * Creates a new OSGiImpl instance.
     */
    public OSGiImpl() {
        bundles = new ArrayList<Bundle>();
    }

    @Override
    public void start(String symbolicName, BundleActivator activator) {
        try {
            BundleImpl bundle = (BundleImpl) getBundle(symbolicName);
            if (bundle == null) { // Creates the bundle.
                bundle = new BundleImpl(this, symbolicName, activator);
                bundles.add(bundle);
            }
            bundle.context = new BundleContextImpl(this, bundle);
            bundle.start();
        } catch (Throwable error) {
            LogContext.error(error);
        }
    }

    @Override
    public void stop(String symbolicName) {
        try {
            BundleImpl bundle = (BundleImpl) getBundle(symbolicName);
            if (bundle == null)
                throw new BundleException("Cannot find bundle " + symbolicName);
            bundle.stop();
            bundle.context = null;
        } catch (Throwable error) {
            LogContext.error(error);
        }
    }

    void fireServiceEvent(ServiceEvent serviceEvent) {
        ServiceReferenceImpl<?> serviceReference = (ServiceReferenceImpl<?>) serviceEvent.getServiceReference();
        String serviceName = serviceReference.serviceName;
        String filter = "(" + Constants.OBJECTCLASS + "=" + serviceName + ")";
        for (int i = 0; i < bundles.size(); i++) {
            BundleImpl bundle = (BundleImpl) bundles.get(i);
            for (int j = 0; j < bundle.serviceListenerFilters.size(); j++) {
                if (filter.equals(bundle.serviceListenerFilters.get(j))) {
                    ServiceListener listener = bundle.serviceListeners.get(j);
                    listener.serviceChanged(serviceEvent);
                }
            }
        }
    }

    @Override
    public Bundle getBundle(String symbolicName) {
        for (int i = 0; i < bundles.size(); i++) {
            Bundle bundle = bundles.get(i);
            if (bundle.getSymbolicName().equals(symbolicName))
                return bundle;
        }
        return null;
    }
    // End conversion.
    ///////////////////////////////////////////////////////////////////////////
}
