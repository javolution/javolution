/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.internal.osgi;

import java.util.Dictionary;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

/**
 *  Holds minimalist service registration implementation.
 */
public class ServiceRegistrationImpl<S> implements ServiceRegistration<S> {
    
   /////////////////////////////////////////////////////////////////////////////
   // Converted from ServiceRegistrationImpl.hpp (C++)
   //

   ServiceReferenceImpl<S> serviceReference;

    public ServiceRegistrationImpl(ServiceReferenceImpl<S> serviceReference) {
       this.serviceReference = serviceReference;
    }

    @Override
    public ServiceReference<S> getReference() {
        return serviceReference;
    }

    @Override
    public void unregister() {
        // Fire event to listeners from all bundles.
        ServiceEvent serviceEvent = new ServiceEvent(ServiceEvent.UNREGISTERING, serviceReference);
        ((BundleImpl)serviceReference.bundle).osgi.fireServiceEvent(serviceEvent);
    	((BundleImpl)serviceReference.bundle).serviceReferences.remove(serviceReference);
        serviceReference.bundle = null; // No more active.
    }

    // End conversion.
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void setProperties(Dictionary<String, ? > dctnr) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}