/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.context.internal;

import javolution.context.LocalContext;
import javolution.util.FastMap;

/**
 * Holds the default implementation of LocalContext.
 */
public final class LocalContextImpl extends LocalContext {

    private FastMap<Parameter<?>, Object> localSettings = new FastMap<Parameter<?>, Object>();
    private LocalContextImpl parent;

    @Override
    protected LocalContext inner() {
        LocalContextImpl ctx = new LocalContextImpl();
        ctx.parent = this;
        return ctx;
    }

    @Override
    public <T> void supersede(Parameter<T> param, T localValue) {
        if (localValue == null) throw new NullPointerException();
        localSettings.put(param, localValue);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <T> T getValue(Parameter<T> param, T defaultValue) {
        Object value = localSettings.get(param);
        if (value != null) return (T) value;
        if (parent != null) return parent.getValue(param, defaultValue);
        return defaultValue;
    }

}
