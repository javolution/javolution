/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2006 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.internal.osgi;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import javolution.osgi.OSGi;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;

/**
 * Holds minimalist bundle implementation.
 */
public class BundleImpl implements Bundle{

   /////////////////////////////////////////////////////////////////////////////
   // Converted from BundleImpl.hpp (C++)
   //

    OSGi osgi;
    String symbolicName;
    BundleActivator activator;
    int state;
    ArrayList<ServiceReferenceImpl> serviceReferences;
    ArrayList<ServiceListener> serviceListeners;
    ArrayList<String> serviceListenerFilters;
    BundleContext context; // Null when not active. This context is set by the framework.

    /**
     * Creates a new BundleImpl instance.
     */
    public BundleImpl(OSGi osgi, String symbolicName, BundleActivator activator) {
        this.osgi = osgi;
        this.symbolicName = symbolicName;
        this.activator = activator;
        this.state = Bundle.RESOLVED;
        this.serviceReferences = new ArrayList<ServiceReferenceImpl>();
        this.serviceListeners = new ArrayList<ServiceListener>();
        this.serviceListenerFilters = new ArrayList<String>();
    }

    public BundleContext getBundleContext() {
        return context;
    }

    @Override
    public String getSymbolicName() {
        return symbolicName;
    }

    @Override
    public int getState() {
        return state;
    }

    @Override
    public void start() throws BundleException {
        if (state != Bundle.RESOLVED)
            throw new RuntimeException("Bundle " + symbolicName + " not in a resolved state");
        state = Bundle.STARTING;
        try {
            activator.start(context);
            state = Bundle.ACTIVE;
        } catch (Exception error) {
            /* If the BundleActivator is invalid or throws an exception then:
                 - This bundle's state is set to STOPPING.
                 - A bundle event of type BundleEvent.STOPPING is fired.
                 - Any services registered by this bundle must be unregistered.
                 - Any services used by this bundle must be released.
                 - Any listeners registered by this bundle must be removed.
                 - This bundle's state is set to RESOLVED.
                 - A bundle event of type BundleEvent.STOPPED is fired.
                 - A BundleException is then thrown.
             */
            state = Bundle.STOPPING;
            unregisterServices();
            unregisterListeners();
            state = Bundle.RESOLVED;
            context = null;
            throw new BundleException("Cannot start bundle " + symbolicName, error);
        }
    }

    @Override
    public void stop() throws BundleException {
        /*  If this bundle's state is not STARTING or ACTIVE then this method returns immediately.
            - This bundle's state is set to STOPPING.
            - A bundle event of type BundleEvent.STOPPING is fired.
            - If this bundle's state was ACTIVE prior to setting the state to STOPPING,
              the BundleActivator.stop(org.osgi.framework.BundleContext) method of this bundle's BundleActivator, if one is specified, is called.
            - If that method throws an exception, this method must continue to stop this bundle and a BundleException must be thrown after
              completion of the remaining steps.
            - Any services registered by this bundle must be unregistered.
            - Any services used by this bundle must be released.
            - Any listeners registered by this bundle must be removed.
            - If this bundle's state is UNINSTALLED, because this bundle was uninstalled while the BundleActivator.stop method was running, a BundleException must be thrown.
            - This bundle's state is set to RESOLVED.
            - A bundle event of type BundleEvent.STOPPED is fired.
         */
        if (state != Bundle.ACTIVE)
            return;
        state = Bundle.STOPPING;
        Exception error = null;
        try {
            activator.stop(context);
        } catch (Exception e) {
            error = e;
        }
        unregisterServices();
        unregisterListeners();
        state = Bundle.RESOLVED;
        if (error != null) throw new BundleException("Cannot stop bundle" + symbolicName, error); // Rethrow after cleanup (no finally block in C++)
    }

    @Override
    public Dictionary getHeaders() {
        Hashtable headers = new Hashtable();
        headers.put(Constants.BUNDLE_NAME, symbolicName);
        headers.put(Constants.BUNDLE_VENDOR, "com.thalesraytheon");
        headers.put(Constants.BUNDLE_VERSION, "N/A");
        return headers;
    }

    // Unregisters all services from this bundle.

    private void unregisterServices() {
        for (int i = 0; i < serviceReferences.size(); i++) {
            ServiceReferenceImpl serviceReference = serviceReferences.get(i);
            // Fire event to listeners from all bundles.
            ServiceEvent serviceEvent = new ServiceEvent(ServiceEvent.UNREGISTERING, serviceReference);
            osgi.fireServiceEvent(serviceEvent);
            serviceReference.bundle = null; // No more active.
        }
        serviceReferences.clear();
    }

    // Unregisters all listeners from this bundle.

    private void unregisterListeners() {
        serviceListeners.clear();
        serviceListenerFilters.clear();
    }


    // End conversion.
    ///////////////////////////////////////////////////////////////////////////
  
    @Override
    public void update() throws BundleException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void update(InputStream in) throws BundleException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void uninstall() throws BundleException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public long getBundleId() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getLocation() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ServiceReference[] getRegisteredServices() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ServiceReference[] getServicesInUse() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean hasPermission(Object o) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public URL getResource(String string) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Dictionary getHeaders(String string) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Class loadClass(String string) throws ClassNotFoundException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Enumeration getResources(String string) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Enumeration getEntryPaths(String string) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public URL getEntry(String string) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public long getLastModified() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Enumeration findEntries(String string, String string1, boolean bln) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}