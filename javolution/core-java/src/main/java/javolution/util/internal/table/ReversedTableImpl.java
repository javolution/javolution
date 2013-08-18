/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util.internal.table;

import javolution.util.function.Equality;
import javolution.util.service.TableService;

/**
 * A reverse view over a table.
 */
public class ReversedTableImpl<E> extends TableView<E> {

    private static final long serialVersionUID = 0x600L; // Version.

    public ReversedTableImpl(TableService<E> that) {
        super(that);
    }

    @Override
    public void add(int index, E element) {
        target().add(size() - index - 1, element);
    }

    @Override
    public E get(int index) {
        return target().get(size() - index - 1);
    }

    @Override
    public int indexOf(Object o) {
        return size() - target().lastIndexOf(o) - 1;
    }

    @Override
    public int lastIndexOf(Object o) {
        return size() - target().indexOf(o) - 1;
    }

    @Override
    public E remove(int index) {
        return target().remove(size() - index - 1);
    }

    @Override
    public E set(int index, E element) {
        return target().set(size() - index - 1, element);
    }

    @Override
    public void clear() {
        target().clear();
    }

    @Override
    public int size() {
        return target().size();
    }

    @Override
    public boolean add(E e) {
        target().addFirst(e);
        return true;
    }

    @Override
    public Equality<? super E> comparator() {
        return target().comparator();
    }

}
