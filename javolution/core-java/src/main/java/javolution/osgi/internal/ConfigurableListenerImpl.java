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
import javolution.lang.Configurable;

/**
 * Holds the default implementation of Configurable.Listener when running 
 * outside OSGi (logs configuration events).
 */
public final class ConfigurableListenerImpl implements Configurable.Listener {

    @Override
    public <T> void configurableInitialized(Configurable<T> configurable,
            T value) {
        LogContext.debug(configurable.getName(), "=", value);
    }

    @Override
    public <T> void configurableReconfigured(Configurable<T> configurable,
            T oldValue, T newValue) {
        LogContext.debug(configurable.getName(),  
             " reconfigured from ", oldValue, " to ", newValue);
    }
}
