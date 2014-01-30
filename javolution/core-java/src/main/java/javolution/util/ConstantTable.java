/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2014 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util;

import java.util.Collection;

import javolution.lang.Immutable;
import javolution.lang.ValueType;
import javolution.util.internal.table.ConstantTableImpl;
import javolution.util.internal.table.ReversedTableImpl;
import javolution.util.internal.table.SubTableImpl;
import javolution.util.service.TableService;
import javolution.xml.DefaultXMLFormat;
import javolution.xml.XMLFormat;
import javolution.xml.stream.XMLStreamException;

/**
 * <p> A table for which {@link Immutable immutability} is guaranteed by 
 *     construction.</p>
 *
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.1, July 29, 2014
 */
@DefaultXMLFormat(ConstantTable.XML.class)
public final class ConstantTable<E> extends FastTable<E> implements
		ValueType<ConstantTable<E>> {

	/**
	 * The default XML representation for constant tables 
	 * (list of elements). 
	 */
	public static class XML extends XMLFormat<ConstantTable<?>> {

		@Override
		public ConstantTable<?> newInstance(
				Class<? extends ConstantTable<?>> cls, InputElement xml)
				throws XMLStreamException {
			int size = xml.getAttribute("size", 0);
			Object[] elements = new Object[size];
			for (int i = 0; i < size; i++) {
				elements[i] = xml.getNext();
			}
			return new ConstantTable<Object>(
					new ConstantTableImpl<Object>(elements));
		}

		@Override
		public void write(ConstantTable<?> that,
				javolution.xml.XMLFormat.OutputElement xml)
				throws XMLStreamException {
			int n = that.size();
			xml.setAttribute("size", n);
			for (int i = 0; i < n; i++) {
				xml.add(that.get(i));
			}
		}

		@Override
		public void read(javolution.xml.XMLFormat.InputElement xml,
				ConstantTable<?> that) throws XMLStreamException {
			// Do nothing (read during instantiation).			
		}

	};

	private static final long serialVersionUID = 0x600L; // Version.

	/**
	 * Package private constructor.
	 */
	ConstantTable(TableService<E> service) {
		super(service);
	}

	/**
	 * Returns a new constant table holding the same elements as the ones 
	 * specified.
	 */
	public static <E> ConstantTable<E> of(E... elements) {
		ConstantTableImpl<E> service = new ConstantTableImpl<E>(
				elements.clone());
		return new ConstantTable<E>(service);
	}

	/**
	 * Returns a new constant table holding the same elements as the specified 
	 * collection.
	 */
	public static <E> ConstantTable<E> of(Collection<? extends E> that) {	
		@SuppressWarnings("unchecked")
		E[] elements = (E[]) new Object[that.size()];
		int i = 0;
		for (E e : that)
			elements[i++] = e;
		ConstantTableImpl<E> service = new ConstantTableImpl<E>(elements);
		return new ConstantTable<E>(service);
	}

	////////////////////////////////////////////////////////////////////////////
	// Views.
	//

	@Override
	public ConstantTable<E> atomic() {
		return this; // Thread-Safe (unmodifiable)
	}

	@Override
	public ConstantTable<E> reversed() {
		return new ConstantTable<E>(new ReversedTableImpl<E>(service()));
	}

	@Override
	public ConstantTable<E> shared() {
		return this; // Thread-Safe (unmodifiable)
	}

	@Override
	public ConstantTable<E> unmodifiable() {
		return this;
	}

	@Override
	public ConstantTable<E> subTable(int fromIndex, int toIndex) {
		return new ConstantTable<E>(new SubTableImpl<E>(service(), fromIndex,
				toIndex));
	}

	@Override
	public ConstantTable<E> value() {
		return this; // As per ValueType contract.
	}

}