/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.internal.context;

import javolution.context.LocalContext;
import javolution.context.LocalParameter;
import javolution.util.FastMap;

/**
 * Holds the default implementation of LocalContext.
 * 
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0, December 12, 2012
 */
public final class LocalContextImpl extends LocalContext {

    private FastMap<LocalParameter<?>, Object> localSettings; 
    private LocalContextImpl outer;
    
    @Override
    protected LocalContext inner() {
        LocalContextImpl ctx = new LocalContextImpl();
        ctx.outer = this;
        return ctx;
    }

    @Override
    public <T> void setLocalValue(LocalParameter<T> param, T localValue) {
        if (localSettings == null) {
            localSettings = new FastMap();
        }
        localSettings.put(param, localValue);
    }

    @Override
    protected <T> T getLocalValueInContext(LocalParameter<T> param) {
        if ((localSettings != null) && localSettings.containsKey(param)) 
            return (T) localSettings.get(param); 
        if (outer != null) return outer.getLocalValueInContext(param);
        return param.getDefaultValue();
    }

}
