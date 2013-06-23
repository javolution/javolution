/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.internal.util.map;

import java.io.Serializable;
import java.util.Map;

/**
 * The map entry implementation.
 */
public final class EntryImpl implements Map.Entry<Object, Object>, Serializable {

    private static final long serialVersionUID = 0x600L; // Version.
    int hash;
    Object key;
    EntryImpl next;
    EntryImpl previous;
    Object value;

    EntryImpl(Object key, Object value, int hash) {
        this.key = key;
        this.value = value;
        this.hash = hash;
    }

    @Override
    public Object getKey() {
        return key;
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public Object setValue(Object value) {
        Object oldValue = this.value;
        this.value = value;
        return oldValue;
    }
}
