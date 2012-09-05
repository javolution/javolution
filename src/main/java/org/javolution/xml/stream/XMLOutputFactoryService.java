/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2005 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.xml.stream;

/**
 * <p> The class represents the OSGi service to construct
 *     {@link XMLOutputFactory} intances.</p>
 * 
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 5.0, July 28, 2011
 */
public interface XMLOutputFactoryService {

    /**
     * Holds the property that defines the name of this service implementation
     * (Javolution's property value is <code>"org.javolution"</code>)
     */
    public static final String OSGI_SERVICE_PROPERTY_IMPLEMENTATION_NAME = "name";

    /**
     * Holds the service property that defines the version of the Javolution/StAX
     * implementation that this provider represents
     * (Javolution's implementation value is the Javolution version).
     */
    public static final String OSGI_SERVICE_PROPERTY_IMPLEMENTATION_VERSION = "version";

    /**
     * Returns a new {@link XMLOutputFactory} instance which can be configured.
     *
     * @return a new XMLOutputFactory instance.
     */
    XMLOutputFactory createXMLOutputFactory();

}