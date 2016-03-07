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
import javolution.util.function.Consumer;
import javolution.util.function.Equality;
import javolution.util.function.Predicate;
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
public final class ConstantTable<E> extends FastTable<E> {

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

	}
	
	private static final long serialVersionUID = 0x700L; // Version.

	/**
	 * Returns a new constant table holding the same elements as the specified 
	 * collection (convenience method).
	 * 
	 * @param <E> Element Type
	 * @param that the collection holding the elements to place in the table.
	 * @return the table containing the elements specified in the collection
	 */
	@SuppressWarnings("unchecked")
	public static <E> ConstantTable<E> of(Collection<? extends E> that) {	
		return (ConstantTable<E>) ConstantTable.of(that.toArray());
	}

	/**
	 * Returns a new constant table holding the specified {@link Constant 
	 * constant} elements.
	 * 
	 * @param <E> Element Type 
	 * @param elements the elements to place in the table
	 * @return the table containing the specified elements
	 */
	public static <E> ConstantTable<E> of(@SuppressWarnings("unchecked") @Constant E... elements) {
		return new ConstantTable<E>(elements);
	}

	private final E[] elements;

	/** Default constructor */
	private ConstantTable(E[] elements) {
		this.elements = elements;
	}

	@Override
	public boolean add(E element) {
		throw new UnsupportedOperationException(
				"Constant tables cannot be modified.");
	}

	@Override
	public void add(int index, E element) {
		throw new UnsupportedOperationException(
				"Constant tables cannot be modified.");
	}

	@Constant
	@Override
	public ConstantTable<E> atomic() {
		return this; // Thread-Safe (unmodifiable)
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException(
				"Constant tables cannot be modified.");
	}
	
	@Override
	public ConstantTable<E> clone() {
		return this;
	}

	@Override
	public Equality<? super E> equality() {
		return Equality.DEFAULT;
	}

	@Override
	public E get(int index) {
		return elements[index];
	}

	@Override
	public boolean isEmpty() {
		return elements.length == 0;
	}

	@Override
	public E remove(int index) {
		throw new UnsupportedOperationException(
				"Constant tables cannot be modified.");
	}

	@Override
	public E set(int index, E element) {
		throw new UnsupportedOperationException(
				"Constant tables cannot be modified.");
	}

	@Constant
	@Override
	public ConstantTable<E> shared() {
		return this; // Thread-Safe (unmodifiable)
	}

	@Override
	public int size() {
		return elements.length;
	}

	@Constant
	@Override
	public ConstantTable<E> subTable(int fromIndex, int toIndex) {
		return ConstantTable.of(this.subTable(fromIndex, toIndex));
	}

	@Constant
	@Override
	public ConstantTable<E> unmodifiable() {
		return this;
	}

	@Override
	public void forEach(Consumer<? super E> consumer) { // Optimization.
		for (E e : elements) consumer.accept(e);
	}

	@Override
	public boolean removeIf(Predicate<? super E> filter) {
		throw new UnsupportedOperationException(
				"Constant tables cannot be modified.");
	}

}