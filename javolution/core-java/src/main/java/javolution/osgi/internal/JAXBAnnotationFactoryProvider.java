/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.osgi.internal;

import javolution.xml.annotation.JAXBAnnotationFactory;
import javolution.xml.internal.annotation.JAXBAnnotationFactoryImpl;

import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;

/**
 * Holds the service factory providing JAXBAnnotationFactory instances.
 */
public class JAXBAnnotationFactoryProvider implements ServiceFactory<JAXBAnnotationFactory> {

	@Override
	public JAXBAnnotationFactory getService(Bundle bundle,
			ServiceRegistration<JAXBAnnotationFactory> registration) {
		return new JAXBAnnotationFactoryImpl();
	}

	@Override
	public void ungetService(Bundle bundle,
			ServiceRegistration<JAXBAnnotationFactory> registration,
			JAXBAnnotationFactory service) {
	}
	
}
