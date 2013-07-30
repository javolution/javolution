/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.osgi.internal;

import javolution.context.LogContext;
import javolution.xml.internal.stream.XMLInputFactoryImpl;
import javolution.xml.stream.XMLInputFactory;

import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;

/**
 * Holds the service factory providing XMLInputFactory instances.
 */
public final class XMLInputFactoryProvider implements ServiceFactory<XMLInputFactory> {

    @Override
    public XMLInputFactory getService(Bundle bundle,
            ServiceRegistration<XMLInputFactory> registration) {
        LogContext.debug("Creates a new XMLInputFactory for ", bundle.getSymbolicName());
        return new XMLInputFactoryImpl();
    }

    @Override
    public void ungetService(Bundle bundle,
            ServiceRegistration<XMLInputFactory> registration,
            XMLInputFactory service) {
        LogContext.debug("Release XMLInputFactory for ", bundle.getSymbolicName());
    }
}
