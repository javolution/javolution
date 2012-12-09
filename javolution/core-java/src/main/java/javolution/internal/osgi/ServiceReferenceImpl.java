/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2006 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.internal.osgi;

import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;

/**
 *  Holds minimalist service reference implementation.
 */
public class ServiceReferenceImpl implements ServiceReference {

   /////////////////////////////////////////////////////////////////////////////
   // Converted from ServiceReferenceImpl.hpp (C++)
   //
   Bundle bundle;

   String serviceName;

   Object service;

   public ServiceReferenceImpl(Bundle bundle, String serviceName, Object service) {
        this.bundle = bundle;
        this.serviceName = serviceName;
        this.service = service;
    }

    @Override
    public Bundle getBundle() {
        return bundle;
    }

    @Override
    public String toString() {
        return new StringBuilder()
                .append("Service ")
                .append(serviceName)
                .append(" from ")
                .append(bundle)
                .toString();
    }

    // End conversion.
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public Object getProperty(String string) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String[] getPropertyKeys() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Bundle[] getUsingBundles() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isAssignableTo(Bundle bundle, String string) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}