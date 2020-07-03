/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.util;

import static org.javolution.annotations.Realtime.Limit.CONSTANT;
import static org.javolution.annotations.Realtime.Limit.LINEAR;
import static org.javolution.annotations.Realtime.Limit.LOG_N;

import java.util.NoSuchElementException;

import org.javolution.annotations.Nullable;
import org.javolution.annotations.Realtime;
import org.javolution.util.function.Equality;
import org.javolution.util.function.Predicate;

/**
 * A high-performance table based upon fast-rotating {@link FractalArray}.
 *  
 * Instances of this class can advantageously replace {@link java.util.ArrayList ArrayList},
 * {@link java.util.LinkedList LinkedList} or {@link java.util.ArrayDeque ArrayDeque}
 * in terms of adaptability, space or performance. They inherit all the fast collection views
 * and support the new {@link #subTable subTable} view over a portion of the table.
 *     
 * ```java
 * FastTable<CharSequence> names = new FastTable<>(); 
 * ...
 * names.sort(Order.lexical()_CASE_INSENSITIVE); // Sorts the names in place (different from sorted() which returns a sorted view).
 * names.subTable(0, names.size() / 2).clear(); // Removes the first half of the table (see java.util.List.subList specification).
 * names.filter(str -> str.startsWith("A")).clear(); // Removes all the names starting with "A" (Java 8 notation).
 * names.filter(str -> str.startsWith("A")).parallel().clear(); // Same as above but removal performed concurrently.
 * ``` 
 *
 * As for any {@link AbstractCollection}, iterations can be performed using closures.
 * 
 * ```java
 * FastTable<Person> persons = ...;
 * Person john = persons.filter(new Predicate<Person>() { 
 *         public boolean test(Person person) {
 *             return (person.getName().equals("John"));
 *         }
 *     }).any();
 * }
 * ``` 
 * 
 * The notation is shorter with Java 8.
 * 
 * ```java
 * Person john = persons.filter(person -> person.getName().equals("John")).any();
 * }
 * ``` 
 * 
 * @param <E> the type of table elements ({@code null} instances are supported)
 * 
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 7.0, September 13, 2015
 */
public class FastTable<E> extends AbstractTable<E> {

    private static final long serialVersionUID = 0x700L; // Version.

    /** Immutable Table (can only be created through the {@link #freeze()} method). */
    public static final class Immutable<E> extends FastTable<E> implements org.javolution.lang.Immutable {
        private static final long serialVersionUID = FastTable.serialVersionUID;

        private Immutable(FractalArray<E> array, int length) {
            super(array, length);
        }
        
        @Override
        public void clear() {
            throw new UnsupportedOperationException("Immutable");
        }
 
    }

    private FractalArray<E> array;
    private int length; // Keep tracks of the length since fractal arrays are unbounded.


    /**  Creates an empty table using default object equality for elements comparisons. */
    public FastTable() {
        array = FractalArray.empty();
        length = 0;
    }

    /**  Base constructor (private). */
    private FastTable(FractalArray<E> array, int length) {
       this.array = array; 
       this.length = length;
    }

    /** Freezes this table and returns the corresponding {@link Immutable} instance (cannot be reversed). */
    public final Immutable<E> freeze() {
        array = array.unmodifiable();
        return new Immutable<E>(array, length);
    }

    @Override
    public FastTable<E> with(@SuppressWarnings("unchecked") E... elements) {
        addAll(elements);
        return this;
    }

    @Override
    @Realtime(limit = CONSTANT)
    public final boolean add(@Nullable E element) {
        array = array.set(length++, element);
        return true;
    }

    @Override
    @Realtime(limit = LOG_N)
    public final void add(int index, @Nullable E element) {
        if (index < 0 || index > length) throw new IndexOutOfBoundsException();
        array = array.insert(index, element);
        length++;
    }

    @Override
    @Realtime(limit = CONSTANT)
    public  void clear() {
        array = FractalArray.empty();
        length = 0;
    }

    @Override
    @Realtime(limit = LINEAR)
    public FastTable<E> clone() {
        FastTable<E> copy = (FastTable<E>) super.clone();
        copy.array = array.clone();
        return copy;
    }

    @Override
    @Realtime(limit = CONSTANT)
    public final Equality<? super E> equality() {
        return Equality.standard();
    }

    @Override
    @Realtime(limit = CONSTANT)
    public final @Nullable E get(int index) {
        if (index < 0 || index >= length) throw new IndexOutOfBoundsException();
        return array.get(index);
    }

    @Override
    @Realtime(limit = CONSTANT)
    public final FastListIterator<E> listIterator(int index) {
        return new IteratorImpl<E>(array, index, length);
    }

    @Override
    @Realtime(limit = LOG_N)
    public final @Nullable E remove(int index) {
        if (index < 0 || index >= length) throw new IndexOutOfBoundsException();
        E removed = array.get(index);
        array = array.delete(index);
        length--;
        return removed;
    }

    @Override
    @Realtime(limit = CONSTANT)
    public final @Nullable E set(int index, @Nullable E element) {
        if (index < 0 || index >= length) throw new IndexOutOfBoundsException();
        E previous = array.get(index);
        array = array.set(index, element);
        return previous;
    }

    @Override
    @Realtime(limit = CONSTANT)
    public final int size() {
        return length;
    }

    /** List Iterator Implementation. */
    private static final class IteratorImpl<E> implements FastListIterator<E> {
        private final FractalArray<E> array;
        private int nextIndex;
        private final int length;

        public IteratorImpl(FractalArray<E> array, int nextIndex, int length) {
            this.array = array;
            this.nextIndex = nextIndex;
            this.length = length;
        }

        @Override
        public boolean hasNext() {
            return nextIndex < length;
        }

        @Override
        public boolean hasNext(Predicate<? super E> matching) {       
        	E next = array.get(nextIndex);
        	if ((next != null) && matching.test(next)) return true;
        	long i = array.next(nextIndex, matching);
            nextIndex = (i == 0) ? length : (int) i;
          	return nextIndex < length;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void add(E arg0) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean hasPrevious() {
            return nextIndex > 0;
        }

        @Override
        public boolean hasPrevious(Predicate<? super E> matching) {
        	long i = array.previous(nextIndex, matching);
        	nextIndex = (i == -1) ? 0 : (int) (i+1);
        	return nextIndex != 0;
        }

        @Override
        public E next() {
            if (nextIndex >= length) throw new NoSuchElementException();
            return array.get(nextIndex++);
        }

        @Override
        public int nextIndex() {
            return nextIndex;
        }

        @Override
        public E previous() {
            if (nextIndex <= 0) throw new NoSuchElementException();
            return array.get(--nextIndex);
        }

        @Override
        public int previousIndex() {
            return nextIndex - 1;
        }

        @Override
        public void set(E arg0) {
            throw new UnsupportedOperationException();
        }

    }

}