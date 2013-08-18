/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util.internal.collection;

import java.util.Iterator;

import javolution.util.function.Equalities;
import javolution.util.function.Equality;
import javolution.util.function.Function;
import javolution.util.service.CollectionService;

/**
 * A mapped view over a collection.
 */
public class MappedCollectionImpl<E, R> extends CollectionView<R> {

    /** Mapping iterator. */
    private class IteratorImpl implements Iterator<R> {
        private final Iterator<E> targetIterator;

        @SuppressWarnings("unchecked")
        public IteratorImpl() {
            targetIterator = (Iterator<E>) target().iterator();
        }

        @Override
        public boolean hasNext() {
            return targetIterator.hasNext();
        }

        @Override
        public R next() {
            return function.apply(targetIterator.next());
        }

        @Override
        public void remove() {
            targetIterator.remove();
        }

    }

    private static final long serialVersionUID = 0x600L; // Version.
    protected final Function<? super E, ? extends R> function;

    @SuppressWarnings("unchecked")
    public MappedCollectionImpl(CollectionService<E> target,
            Function<? super E, ? extends R> function) {
        super((CollectionService<R>) target); // Beware target is of type <E>
        this.function = function;
    }

    @Override
    public boolean add(R element) {
        throw new UnsupportedOperationException(
                "New elements cannot be added to mapped views");
    }

    @Override
    public void clear() {
        target().clear();
    }

    @Override
    public Equality<? super R> comparator() {
        return Equalities.STANDARD;
    }

    @Override
    public boolean isEmpty() {
        return target().isEmpty();
    }

    @Override
    public Iterator<R> iterator() {
        return new IteratorImpl();
    }
    
    @Override
    public int size() {
        return target().size();
    }
    
}
