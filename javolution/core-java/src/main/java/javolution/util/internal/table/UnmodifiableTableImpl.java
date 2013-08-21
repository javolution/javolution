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
 * An unmodifiable view over a table.
 */
public class UnmodifiableTableImpl<E> extends TableView<E> {

    private static final long serialVersionUID = 0x600L; // Version.

    public UnmodifiableTableImpl(TableService<E> target) {
        super(target);
    }

    @Override
    public boolean add(E element) {
        throw new UnsupportedOperationException("Read-Only Collection.");
    }

    @Override
    public void add(int index, E element) {
        throw new UnsupportedOperationException("Unmodifiable");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("Read-Only Collection.");
    }

    @Override
    public Equality<? super E> comparator() {
        return target().comparator();
    }

    @Override
    public E get(int index) {
        return target().get(index);
    }

    @Override
    public int indexOf(Object o) {
        return target().indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return target().lastIndexOf(o);
    }

    @Override
    public E remove(int index) {
        throw new UnsupportedOperationException("Read-Only Collection.");
    }

    @Override
    public E set(int index, E element) {
        throw new UnsupportedOperationException("Read-Only Collection.");
    }

    @Override
    public int size() {
        return target().size();
    }

    @Override
    public TableService<E>[] split(int n, boolean updateable) {
        return SubTableImpl.splitOf(this, n, false); // Sub-views over this.
    }
    
    @Override
    protected TableService<E> target() {
        return (TableService<E>) super.target();
    }
 }
