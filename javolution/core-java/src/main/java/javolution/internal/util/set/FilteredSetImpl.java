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

import javolution.internal.util.collection.FilteredCollectionImpl;
import javolution.util.function.Predicate;
import javolution.util.service.SetService;

/**
 * A filtered view over a set.
 */
public class FilteredSetImpl<E> extends FilteredCollectionImpl<E> implements SetService<E>,
        Serializable {

    public FilteredSetImpl(SetService<E> that, Predicate<? super E> filter) {
        super(that, filter);
    }

    @Override
    public int size() {
        final int count[] = new int[1];
        doWhile(new Predicate<E>() {
            @Override
            public boolean test(E param) {
                if (filter.test(param)) count[0]++;
                return true;
            }
        });
        return count[0];
    }
    
    @Override
    public void clear() {
        removeIf(new Predicate<E>() {
            @Override
            public boolean test(E param) {
                return filter.test(param);
            }
        });
    }

    @Override
    public boolean contains(E e) {
        return filter.test(e) && ((SetService<E>)that).contains(e);
    }

    @Override
    public boolean remove(E e) {
        return filter.test(e) && ((SetService<E>)that).remove(e);
    }

    private static final long serialVersionUID = -218446112843771282L;
}
