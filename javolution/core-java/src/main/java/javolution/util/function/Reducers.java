/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util.function;

import static javolution.lang.Realtime.Limit.LINEAR;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import javolution.lang.Parallelizable;
import javolution.lang.Realtime;
import javolution.util.FastCollection;

/**
 * <p> A set of useful {@link Reducer reducers} of collection elements.</p>
 *     
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0, July 21, 2013
 * @see     FastCollection#reduce(Reducer)
 */
public class Reducers {

    // Utility class, no default constructor.
    private Reducers() {}

    /**
     * Returns any non-null element of the specified type. 
     * This reducer stops iterating as soon as an element with the matching 
     * type is found.
     */
    @Parallelizable
    @Realtime(limit = LINEAR)
    public static <E> Reducer<E> any(Class<? extends E> type) {
        return new AnyReducer<E>(type);
    }

    private static class AnyReducer<E> implements Reducer<E>  {
        private final Class<? extends E> type;
        private volatile E found;
        
        public AnyReducer(Class<? extends E> type) {
            this.type = type;
        }

        @Override
        public void accept(Collection<E> param) {
            Iterator<E> it = param.iterator();
            while (it.hasNext() && (found == null)) {
                E e = it.next();
                if (type.isInstance(e)) {
                    found = e;
                    break;
                }
            }
        }

        @Override
        public E get() {
            return found;
        }
    }

    /**
     * Returns the greatest element of a collection according to the 
     * specified comparator (returns {@code null} if the collection is empty).
     */
    @Parallelizable(mutexFree = true, comment = "Internal use of AtomicReference")
    @Realtime(limit = LINEAR)
    public static <E> Reducer<E> max(Comparator<? super E> comparator) {
        return new MaxReducer<E>(comparator);
    }

    private static class MaxReducer<E> implements Reducer<E> {
        private final Comparator<? super E> cmp;
        private final AtomicReference<E> max = new AtomicReference<E>(null);

        public MaxReducer(Comparator<? super E> cmp) {
            this.cmp = cmp;
        }

        @Override
        public void accept(Collection<E> param) {
            Iterator<E> it = param.iterator();
            while (it.hasNext()) {
                E e = it.next();
                E read = max.get();
                while ((read == null) || (cmp.compare(e, read) > 0)) {
                    if (max.compareAndSet(read, e)) break;
                    read = max.get();
                }
            }
        }

        @Override
        public E get() {
            return max.get();
        }
    }

    /**
     * Returns the smallest element of a collection according to the collection
     * comparator (returns {@code null} if the collection is empty).
     */
    @Parallelizable(mutexFree = true, comment = "Internal use of AtomicReference")
    @Realtime(limit = LINEAR)
    public static <E> Reducer<E> min(Comparator<? super E> comparator) {
        return new MinReducer<E>(comparator);
    }

    private static class MinReducer<E> implements Reducer<E> {
        private final Comparator<? super E> cmp;
        private final AtomicReference<E> min = new AtomicReference<E>(null);

        public MinReducer(Comparator<? super E> cmp) {
            this.cmp = cmp;
        }

        @Override
        public void accept(Collection<E> param) {
            Iterator<E> it = param.iterator();
            while (it.hasNext()) {
                E e = it.next();
                E read = min.get();
                while ((read == null) || (cmp.compare(e, read) < 0)) {
                    if (min.compareAndSet(read, e)) break;
                    read = min.get();
                }
            }
        }

        @Override
        public E get() {
            return min.get();
        }
    }

    /**
    * Conditional 'and' operator (returns {@code true} if the collection is 
    * empty). This operator stops iterating as soon as a {@code false} value
    * is found.
    */
    @Parallelizable
    @Realtime(limit = LINEAR)
    public static Reducer<Boolean> and() {
        return new AndReducer();
    }

    private static class AndReducer implements Reducer<Boolean> {
        volatile boolean result = true;

        @Override
        public void accept(Collection<Boolean> param) {
            Iterator<Boolean> it = param.iterator();
            while (result && it.hasNext()) {
                if (!it.next()) result = false;
            }
        }

        @Override
        public Boolean get() {
            return result;
        }
    }

    /**
    * Conditional 'or' operator (returns {@code false} if the collection is 
    * empty). This operator stops iterating as soon as a {@code true} value
    * is found.
     */
    @Parallelizable
    @Realtime(limit = LINEAR)
    public static Reducer<Boolean> or() {
        return new OrReducer();
    }

    private static class OrReducer implements Reducer<Boolean> {
        volatile boolean result = false;

        @Override
        public void accept(Collection<Boolean> param) {
            Iterator<Boolean> it = param.iterator();
            while (!result && it.hasNext()) {
                if (!it.next()) result = true;
            }
        }

        @Override
        public Boolean get() {
            return result;
        }
    }

    /**
     * Returns the sum of the specified integers value (returns {@code 0} 
     * if the collection is empty).
     */
    @Parallelizable(comment = "Internal use of AtomicInteger")
    @Realtime(limit = LINEAR)
    public static Reducer<Integer> sum() {
        return new SumReducer();
    }

    private static class SumReducer implements Reducer<Integer> {
        private final AtomicInteger sum = new AtomicInteger(0);

        @Override
        public void accept(Collection<Integer> param) {
            Iterator<Integer> it = param.iterator();
            while (it.hasNext()) {
                sum.getAndAdd(it.next().intValue());
            }
        }

        @Override
        public Integer get() {
            return sum.get();
        }
    }

}