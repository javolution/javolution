/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2004 - The Javolution Team (http://javolution.org/)
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package java.util;

public interface SortedSet extends Set {
    Comparator comparator();

    SortedSet subSet(Object fromElement, Object toElement);

    SortedSet headSet(Object toElement);

    SortedSet tailSet(Object fromElement);

    Object first();

    Object last();
}