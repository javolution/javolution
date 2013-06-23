/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.internal.util.set;

import java.io.Serializable;
import java.util.Iterator;

import javolution.internal.util.collection.SharedCollectionImpl;
import javolution.util.function.Consumer;
import javolution.util.function.EqualityComparator;
import javolution.util.function.Predicate;
import javolution.util.service.CollectionService;
import javolution.util.service.SetService;

/**
 * A shared view over a set allowing concurrent access and sequential updates.
 */
public class SharedSetImpl<E> implements SetService<E>, Serializable {

    private static final long serialVersionUID = 0x600L; // Version.
    private final SetService<E> target;

    public SharedSetImpl(SetService<E> target) {
        this.target = target;
    }

    @Override
    public boolean add(E element) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void atomic(Runnable action) {
        // TODO Auto-generated method stub

    }

    @Override
    public EqualityComparator<? super E> comparator() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void forEach(
            Consumer<? super E> consumer,
            javolution.util.service.CollectionService.IterationController controller) {
        // TODO Auto-generated method stub

    }

    @Override
    public Iterator<E> iterator() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean removeIf(
            Predicate<? super E> filter,
            javolution.util.service.CollectionService.IterationController controller) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public CollectionService<E>[] trySplit(int n) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int size() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void clear() {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean contains(E e) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean remove(E e) {
        // TODO Auto-generated method stub
        return false;
    }
}
