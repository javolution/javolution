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

import javolution.lang.Constant;
import javolution.util.internal.table.ConstantTableImpl;
import javolution.util.internal.table.ReversedTableImpl;
import javolution.util.internal.table.SubTableImpl;
import javolution.util.service.TableService;
import javolution.xml.DefaultXMLFormat;
import javolution.xml.XMLFormat;
import javolution.xml.stream.XMLStreamException;

/**
 * <p> A table for which immutability is guaranteed by construction.</p>
 *
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.1, February 2, 2014
 */
@Constant(comment = "Immutable")
@DefaultXMLFormat(ConstantTable.XML.class)
public class ConstantTable<E> extends FastTable<E> {

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
			return ConstantTable.of(elements);
		}

		@Override
		public void read(javolution.xml.XMLFormat.InputElement xml,
				ConstantTable<?> that) throws XMLStreamException {
			// Do nothing (read during instantiation).			
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

	};

	private static final long serialVersionUID = 0x600L; // Version.

	/**
	 * Returns a new constant table holding the specified {@link Constant 
	 * constant} elements.
	 * @param <E> Type of the ConstantTable
	 * @param elements Elements to place in the a ConstantTable
	 * @return ConstantTable containing the specified elements
	 */
	public static <E> ConstantTable<E> of(@Constant E... elements) {
		return new ConstantTable<E>(new ConstantTableImpl<E>(elements));
	}

	/**
	 * Returns a new constant table holding the same elements as the specified 
	 * collection (convenience method).
	 * @param <E> Type of the ConstantTable
	 * @param that Collection to convert to a ConstantTable
	 * @return A ConstantTable containing the elements specified in the collection
	 */
	@SuppressWarnings("unchecked")
	public static <E> ConstantTable<E> of(Collection<? extends E> that) {
		return ConstantTable.of((E[]) that.toArray(new Object[that.size()]));
	}

	/**
	 * Creates a constant table backed up by the specified{@link Constant 
	 * constant} service implementation.
	 * @param service A TableService to back the ConstantTable
	 */
	protected ConstantTable(@Constant TableService<E> service) {
		super(service);
	}

	////////////////////////////////////////////////////////////////////////////
	// Views.
	//

	@Constant
	@Override
	public ConstantTable<E> atomic() {
		return this; // Thread-Safe (unmodifiable)
	}

	@Constant
	@Override
	public ConstantTable<E> reversed() {
		return new ConstantTable<E>(new ReversedTableImpl<E>(service()));
	}

	@Constant
	@Override
	public ConstantTable<E> shared() {
		return this; // Thread-Safe (unmodifiable)
	}

	@Constant
	@Override
	public ConstantTable<E> subTable(int fromIndex, int toIndex) {
		return new ConstantTable<E>(new SubTableImpl<E>(service(), fromIndex,
				toIndex));
	}

	@Constant
	@Override
	public ConstantTable<E> unmodifiable() {
		return this;
	}

	@Constant
	@Override
	protected TableService<E> service() {
		return super.service();
	}
}