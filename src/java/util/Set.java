/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2004 - The Javolution Team (http://javolution.org/)
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package java.util;

public interface Set extends Collection {
    int size();

    boolean isEmpty();

    boolean contains(Object o);

    Iterator iterator();

    Object[] toArray();

    Object[] toArray(Object[] a);

    boolean add(Object o);

    boolean remove(Object o);

    boolean containsAll(Collection c);

    boolean addAll(Collection c);

    boolean retainAll(Collection c);

    boolean removeAll(Collection c);

    void clear();

    boolean equals(Object o);

    int hashCode();
}