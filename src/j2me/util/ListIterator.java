/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2004 - The Javolution Team (http://javolution.org/)
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package j2me.util;

public interface ListIterator extends Iterator {
    boolean hasNext();

    Object next();

    boolean hasPrevious();

    Object previous();

    int nextIndex();

    int previousIndex();

    void remove();

    void set(Object o);

    void add(Object o);
}