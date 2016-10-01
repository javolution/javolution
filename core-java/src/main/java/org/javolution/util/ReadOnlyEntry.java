/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2014 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.util;

import java.util.Map.Entry;

/**
 * <p> An entry which does not allow for modification (the {@link #setValue} method throws 
 *     {@link UnsupportedOperationException}).</p>
 * 
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 7.0, March 14, 2016
 */
public abstract class ReadOnlyEntry<K, V> implements Entry<K, V> {

    /** 
     * Returns a read-only entry from the specified entry.
     * 
     * @param that the entry to convert.
     * @return a read-only entry wrapping the entry specified or {@code this} if the specified 
     *         entry is a read-only entry.
     */
    public static <K, V> ReadOnlyEntry<K, V> of(final Entry<K, V> that) {
        if (that instanceof ReadOnlyEntry)
            return (ReadOnlyEntry<K, V>) that;
        return new ReadOnlyEntry<K, V>() {
            @Override
            public K getKey() {
                return that.getKey();
            }

            @Override
            public V getValue() {
                return that.getValue();
            }

            @Override
            public boolean equals(Object obj) { // As per Map.Entry contract.
                return that.equals(obj);
            }

            @Override
            public int hashCode() { // As per Map.Entry contract.
                return that.hashCode();
            }

            @Override
            public String toString() {
                return that.toString(); // For debug.
            }

        };
    }

    /** 
     * Guaranteed to throw an exception and leave the collection unmodified.
     * @deprecated Should never be used on immutable table.
     */
    @Override
    public final V setValue(V newValue) {
        throw new UnsupportedOperationException("Read-Only Entry");
    }

}