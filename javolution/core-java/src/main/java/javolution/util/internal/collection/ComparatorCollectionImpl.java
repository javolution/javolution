/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util.internal.collection;

import java.util.Comparator;
import java.util.Iterator;

import javolution.util.function.Equality;
import javolution.util.internal.comparator.WrapperComparatorImpl;
import javolution.util.service.CollectionService;

/**
 * A view using a custom comparator.
 */
public class ComparatorCollectionImpl<E> extends CollectionView<E> {

    private static final long serialVersionUID = 0x600L; // Version.
    protected final Equality<E> comparator;
    
    @SuppressWarnings("unchecked")
    public ComparatorCollectionImpl(CollectionService<E> target, Comparator<? super E> comparator) {
        super(target);
        this.comparator = (comparator instanceof Equality) ?
                (Equality<E>) comparator : new WrapperComparatorImpl<E>(comparator);
    }

    @Override
    public boolean add(E element) {
        return target().add(element);
    }

    @Override
    public void clear() {
        target().clear();
    }

    @Override
    public Equality<? super E> comparator() {
        return comparator;
    }

    @Override
    public boolean isEmpty() {
        return target().isEmpty();
    }

    @Override
    public Iterator<E> iterator() {
        return target().iterator();
    }

    @Override
    public int size() {
        return target().size();
    }

}
