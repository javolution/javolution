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

import javolution.util.FastTable;
import javolution.util.function.Equality;
import javolution.util.internal.comparator.WrapperComparatorImpl;
import javolution.util.service.CollectionService;

/**
 * A sorted view over a collection.
 */
public class SortedCollectionImpl<E> extends CollectionView<E>  {

    /** Sorting Iterator. */
    private class IteratorImpl implements Iterator<E> {
        private final Iterator<E> iterator;
        private E next;
        
        public IteratorImpl() {
            FastTable<E> sorted = new FastTable<E>(comparator);
            Iterator<E> it = target().iterator();            
            while (it.hasNext()) {
                sorted.add(it.next());
            }
            sorted.sort();
            iterator = sorted.iterator();
        }
        
        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public E next() {
            next = iterator.next();
            return next;
        }

        @Override
        public void remove() {
            if (next == null) throw new IllegalStateException();
            target().remove(next);
            next = null;
        }

    }
   
    private static final long serialVersionUID = 0x600L; // Version.    
    protected final Equality<E> comparator;

    @SuppressWarnings("unchecked")
    public SortedCollectionImpl(CollectionService<E> target, Comparator<? super E> comparator) {
        super(target);
        this.comparator = (comparator instanceof Equality) ?
                (Equality<E>) comparator : new WrapperComparatorImpl<E>(comparator);
    }

    @Override
    public boolean add(E e) {
        return target().add(e);
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
    public boolean contains(Object obj) {
        return target().contains(obj);
    }

    @Override
    public boolean isEmpty() {
        return target().isEmpty();
    }

    @Override
    public Iterator<E> iterator() {
        return new IteratorImpl();
    }

    @Override
    public boolean remove(Object obj) {
        return target().remove(obj);
    }

    @Override
    public int size() {
        return target().size();
    }
    
}
