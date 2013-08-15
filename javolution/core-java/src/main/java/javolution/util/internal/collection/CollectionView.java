/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util.internal.collection;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javolution.util.FastCollection;
import javolution.util.function.Consumer;
import javolution.util.function.Equalities;
import javolution.util.function.Equality;
import javolution.util.service.CollectionService;
import javolution.util.service.MapService;

/**
 * Collection view implementation; can be used as root class for implementations 
 * if target is {@code null}.
 * When possible sub-classes should forward to the actual target for the methods
 * clear, remove, contains, size and isEmpty rather than using the default 
 * implementation.
 */
public abstract class CollectionView<E> extends FastCollection<E> implements CollectionService<E> {
//public abstract class CollectionView<E> implements CollectionService<E> {

    private static final long serialVersionUID = 0x600L; // Version.
    private CollectionService<E> target;

    /**
     * The view constructor or root class constructor if target is {@code null}.
     */
    public CollectionView(CollectionService<E> target) {
        this.target = target;
    }

    @Override
    public abstract boolean add(E element);

    @Override
    public boolean addAll(Collection<? extends E> c) {
        boolean changed = false;
        Iterator<? extends E> it = c.iterator();
        while (it.hasNext()) {
            if (add(it.next())) changed = true;
        }
        return changed;
    }

    @Override
    public void clear() {
        Iterator<? extends E> it = iterator();
        while (it.hasNext()) {
            it.next();
            it.remove();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public CollectionView<E> clone() {
        try {
            CollectionView<E> copy = (CollectionView<E>) super.clone();
            if (target != null) { // Not a root class.
                copy.target = target.clone();
            }
            return copy;
        } catch (CloneNotSupportedException e) {
            throw new Error("Should not happen since target is cloneable");
        }
    }

    @Override
    public abstract Equality<? super E> comparator();

    @Override
    @SuppressWarnings("unchecked")
    public boolean contains(Object obj) {
        Iterator<? extends E> it = iterator();
        Equality<Object> cmp = (Equality<Object>) comparator();
        while (it.hasNext()) {
            if (cmp.areEqual(obj, it.next())) return true;
        }
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object e : c) {
            if (!contains(e)) return false;
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object o) {
        // Follow Collection.equals specification if this comparator is standard.        
        if (this == o) return true;
        // Check comparators consistency.
        if (o instanceof CollectionService) {
            if (!comparator().equals(((CollectionService<E>) o).comparator())) return false; // Different comparators.
        } else {
            if (!comparator().equals(Equalities.STANDARD)) return false;
        }
        // Collection.equals contract.
        if (this instanceof Set) {
            if (!(o instanceof Set)) return false;
            Set<E> set = (Set<E>) o;
            return (size() == set.size()) && containsAll(set);
        } else if (this instanceof List) {
            if (!(o instanceof List)) return false;
            List<E> list = (List<E>) o;
            if (size() != list.size()) return false; // Short-cut.
            Equality<? super E> cmp = comparator();
            Iterator<E> it1 = this.iterator();
            Iterator<E> it2 = list.iterator();
            while (it1.hasNext()) {
                if (!it2.hasNext()) return false;
                if (!cmp.areEqual(it1.next(), it2.next())) return false;
            }
            if (it2.hasNext()) return false;
            return true;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        // Follow Collection.equals specification if this comparator is standard.        
        Equality<? super E> cmp = comparator();
        Iterator<E> it = this.iterator();
        int hash = 0;
        if (this instanceof Set) {
            while (it.hasNext()) {
                hash += cmp.hashCodeOf(it.next());
            }
        } else if (this instanceof List) {
            while (it.hasNext()) {
                hash += 31 * hash + cmp.hashCodeOf(it.next());
            }
        } else {
            hash = super.hashCode();
        }
        return hash;
    }

    @Override
    public boolean isEmpty() {
        return iterator().hasNext();
    }

    @Override
    public abstract Iterator<E> iterator();

    @Override
    public void perform(Consumer<CollectionService<E>> action, CollectionService<E> view) {
        if (target == null) {
            action.accept(view);
        } else {
            target.perform(action, view);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean remove(Object obj) {
        Iterator<? extends E> it = iterator();
        Equality<Object> cmp = (Equality<Object>) comparator();
        while (it.hasNext()) {
            if (cmp.areEqual(obj, it.next())) {
                it.remove();
                return true;
            }
        }
        return false;
    }
    
    @Override
    public boolean removeAll(Collection<?> c) {
        boolean changed = false;
        Iterator<? extends E> it = iterator();
        while (it.hasNext()) {
            if (c.contains(it.next())) {
                it.remove();
                changed = true;
            }
        }
        return changed;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        boolean changed = false;
        Iterator<? extends E> it = iterator();
        while (it.hasNext()) {
            if (!c.contains(it.next())) {
                it.remove();
                changed = true;
            }
        }
        return changed;
    }

    @Override
    public int size() {
        int count = 0;
        Iterator<? extends E> it = iterator();
        while (it.hasNext()) {
            count++;
            it.next();
        }
        return count;
    }

    @SuppressWarnings("unchecked")
    @Override
    public CollectionService<E>[] split(int n) {
        if (target == null) return new CollectionService[] { this }; // No split.
        CollectionService<E>[] subTargets = target.split(n);
        CollectionService<E>[] result = new CollectionService[subTargets.length];
        for (int i = 0; i < subTargets.length; i++) {
            CollectionView<E> copy = this.clone();
            copy.target = subTargets[i];
            result[i] = copy;
        }
        return result;
    }

    @Override
    public CollectionService<E> threadSafe() {
        // We use shared collection as default (they can split).
        return new SharedCollectionImpl<E>(this); 
    }

    @Override
    public Object[] toArray() {
        return toArray(new Object[size()]);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a) {
        final int size = size();
        final T[] result = (size <= a.length) ? a
                : (T[]) java.lang.reflect.Array.newInstance(a.getClass()
                        .getComponentType(), size);
        int i = 0;
        Iterator<E> it = iterator();
        while (it.hasNext()) {
            result[i++] = (T) it.next();
        }
        if (result.length > size) {
            result[size] = null; // As per Collection contract.
        }
        return result;
    }

    @Override
    public void update(Consumer<CollectionService<E>> action, CollectionService<E> view) {
        if (target == null) {
            action.accept(view);
        } else {
            target.perform(action, view);
        }
    }
    
    protected CollectionService<E> service() {
        return this;
    }
    
    /** Returns the actual target */
    protected CollectionService<E> target() {
        return target;
    }
}
