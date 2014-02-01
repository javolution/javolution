/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util.internal.table;

import javolution.util.function.Equalities;
import javolution.util.function.Equality;
import javolution.util.function.Function;
import javolution.util.service.TableService;

/**
 * A mapped view over a table.
 */
public class MappedTableImpl<E, R> extends TableView<R> {

	private static final long serialVersionUID = 0x600L; // Version.
	protected final Function<? super E, ? extends R> function;

	@SuppressWarnings("unchecked")
	public MappedTableImpl(TableService<E> target,
			Function<? super E, ? extends R> function) {
		super((TableService<R>) target); // Beware target is of type <E>
		this.function = function;
	}

	@Override
	public void add(int index, R element) {
		throw new UnsupportedOperationException(
				"New elements cannot be added to mapped views");
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

	@SuppressWarnings("unchecked")
	@Override
	public R get(int index) {
		return function.apply((E) target().get(index));
	}

	@SuppressWarnings("unchecked")
	@Override
	public R remove(int index) {
		return function.apply((E) target().remove(index));
	}

	@Override
	public R set(int index, R element) {
		throw new UnsupportedOperationException(
				"Elements cannot be set to mapped views");
	}

	@Override
	public int size() {
		return target().size();
	}

}
