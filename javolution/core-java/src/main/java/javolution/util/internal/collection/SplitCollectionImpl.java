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
import java.util.NoSuchElementException;

import javolution.util.function.Consumer;
import javolution.util.function.EqualityComparator;
import javolution.util.function.Predicate;
import javolution.util.internal.table.FastTableImpl;
import javolution.util.service.CollectionService;
import javolution.util.service.SetService;
import javolution.util.service.TableService;

/**
 * A split view over a collection. Elements removal is supported only if 
 * the target collection is a set.
 */
public class SplitCollectionImpl<E> implements CollectionService<E> {

    private final CollectionService<E> target;
    private final TableService<E> elements;

    @SuppressWarnings("unchecked")
    public static <E> SplitCollectionImpl<E>[] splitOf(
            CollectionService<E> target, int n) {
        FastTableImpl<E> table = new FastTableImpl<E>(target.comparator());
        table.addAll(target);
        TableService<E>[] subTables = table.trySplit(n);
        SplitCollectionImpl<E>[] subCollections = new SplitCollectionImpl[subTables.length];
        for (int i = 0; i < subCollections.length; i++) {
            subCollections[i] = new SplitCollectionImpl<E>(target,
                    subTables[i]);
        }
        return subCollections;
    }

    private SplitCollectionImpl(CollectionService<E> target,
            TableService<E> elements) {
        this.target = target;
        this.elements = elements;
    }
    
    /**
     * Removes the specified element from the target collection.
     * 
     * @throws UnsupportedOperationException if the target service is not a set.
     */
    public boolean remove(E e) {
        if (target instanceof SetService) 
            return ((SetService<E>)target).remove(e);
        throw new UnsupportedOperationException(
                "Removal not supported (not a set)");
    }

    @Override
    public boolean add(E element) {
        return target.add(element);
    }

    @Override
    public void atomic(Runnable update) {
        target.atomic(update);
    }

    @Override
    public EqualityComparator<? super E> comparator() {
        return target.comparator();
    }

    @Override
    public void forEach(Consumer<? super E> consumer,
            IterationController controller) {
        if (!controller.doReversed()) {
            for (int i = 0; i < elements.size(); i++) {
                consumer.accept(elements.get(i));
                if (controller.isTerminated())
                    break;
            }
        } else { // Reversed.
            for (int i = elements.size(); --i >= 0;) {
                consumer.accept(elements.get(i));
                if (controller.isTerminated())
                    break;
            }
        }
    }

    @Override
    public Iterator<E> iterator() {
        return new Iterator<E>() {
            int nextIndex;
            int currentIndex = -1;

            @Override
            public boolean hasNext() {
                return (nextIndex < elements.size());
            }

            @Override
            public E next() {
                if (nextIndex >= elements.size())
                    throw new NoSuchElementException();
                currentIndex = nextIndex++;
                return elements.get(currentIndex);
            }

            @Override
            public void remove() {
                if (currentIndex < 0)
                    throw new IllegalStateException();
                SplitCollectionImpl.this.remove(elements.get(currentIndex));
                currentIndex = -1;
            }
        };
    }

    @Override
    public boolean removeIf(Predicate<? super E> filter,
            IterationController controller) {
        boolean removed = false;
        if (!controller.doReversed()) {
            for (int i = 0; i < elements.size(); i++) {
                if (filter.test(elements.get(i))) {
                    if (remove(elements.get(i))) {
                        removed = true;
                    }
                }
                if (controller.isTerminated())
                    break;
            }
        } else { // Reversed.
            for (int i = elements.size(); --i >= 0;) {
                if (filter.test(elements.get(i))) {
                    if (remove(elements.get(i))) {
                        removed = true;
                    }
                }
                if (controller.isTerminated())
                    break;
            }
        }
        return removed;
    }

    @SuppressWarnings("unchecked")
    @Override
    public SplitCollectionImpl<E>[] trySplit(int n) {
        return new SplitCollectionImpl[] { this }; // Already split.
    }

}
