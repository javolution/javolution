/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2004 - The Javolution Team (http://javolution.org/)
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package j2me.nio;
import j2me.lang.UnsupportedOperationException;

/**
 * Clean-room implementation of ByteOrder to support 
 * <code>javolution.util.Struct</code> when <code>j2me.nio</code> is
 * not available.
 */
public final class ByteOrder {
    public static final ByteOrder BIG_ENDIAN = new ByteOrder();

    public static final ByteOrder LITTLE_ENDIAN = new ByteOrder();

    public static ByteOrder nativeOrder() {
        throw new UnsupportedOperationException();
    }

}