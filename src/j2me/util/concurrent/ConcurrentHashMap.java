/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2005 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package j2me.util.concurrent;
import javolution.util.FastMap;
public class ConcurrentHashMap extends FastMap {
    
    public ConcurrentHashMap() {
        setShared(true);
    }
    public ConcurrentHashMap(int capacity) {
        super(capacity);
        setShared(true);
    }
    
}