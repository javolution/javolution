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
import javolution.xml.internal.stream.XMLOutputFactoryImpl;
import javolution.xml.stream.XMLOutputFactory;

import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;

/**
 * Holds the service factory providing XMLInputFactory instances.
 */
public final class XMLOutputFactoryProvider implements ServiceFactory<XMLOutputFactory> {

    @Override
    public XMLOutputFactory getService(Bundle bundle,
            ServiceRegistration<XMLOutputFactory> registration) {
        LogContext.debug("Creates a new XMLOutputFactory for ", bundle.getSymbolicName());
        return new XMLOutputFactoryImpl();
    }

    @Override
    public void ungetService(Bundle bundle,
            ServiceRegistration<XMLOutputFactory> registration,
            XMLOutputFactory service) {
        LogContext.debug("Release XMLOutputFactory for ", bundle.getSymbolicName());
    }
}
