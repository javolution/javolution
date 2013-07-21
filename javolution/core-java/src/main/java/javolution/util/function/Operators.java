/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util.function;

import static javolution.lang.RealTime.Limit.LINEAR;

import java.util.Comparator;
import java.util.concurrent.atomic.AtomicInteger;

import javolution.lang.Parallelizable;
import javolution.lang.RealTime;
import javolution.util.FastCollection;
import javolution.util.service.CollectionService;
import javolution.util.service.CollectionService.IterationController;

/**
 * <p> A set of useful {@link CollectionOperator operators} over collections.</p>
 *     
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0, July 21, 2013
 * @see     FastCollection#reduce(CollectionOperator)
 */
public class Operators {

    /**
     * Returns any non-null element. This operator stops iterating as soon as
     * a non-null element is found.
     */
    @Parallelizable
    @RealTime(limit = LINEAR)
    public static final CollectionOperator<Object> ANY = new CollectionOperator<Object>() {

        @Override
        public Object apply(CollectionService<Object> objects) {
            AnyConsumer<Object> anyConsumer = new AnyConsumer<Object>();
            objects.forEach(anyConsumer, anyConsumer);
            return anyConsumer.found;
        }

    };

    private static class AnyConsumer<E> implements Consumer<E>,
            IterationController {
        private volatile E found;

        @Override
        public void accept(E param) {
            if (param != null) {
                found = param;
            }

        }

        @Override
        public boolean doSequential() {
            return false;
        }

        @Override
        public boolean doReversed() {
            return false;
        }

        @Override
        public boolean isTerminated() {
            return found != null;
        }
    }

    /**
     * Returns the greatest element of a collection according to the collection
     * comparator (returns {@code null} if the collection is empty).
     */
    @Parallelizable(mutexFree = false, comment="Internal use of synchronization")
    @RealTime(limit = LINEAR)
    public static final CollectionOperator<Object> MAX = new CollectionOperator<Object>() {

        @Override
        public Object apply(final CollectionService<Object> objects) {
            MaxConsumer<Object> maxConsumer = new MaxConsumer<Object>(
                    objects.comparator());
            objects.forEach(maxConsumer, null);
            return maxConsumer.max;
        }

    };

    private static class MaxConsumer<E> implements Consumer<E> {
        private final Comparator<? super E> comparator;
        private E max;

        public MaxConsumer(Comparator<? super E> comparator) {
            this.comparator = comparator;
        }

        @Override
        public void accept(E param) {
            E currentMax = max;
            if ((currentMax == null)
                    || comparator.compare(param, currentMax) > 0) {
                synchronized (this) { // Exclusive lock.
                    if ((currentMax == max)
                            || (comparator.compare(param, max) > 0)) {
                        max = param;
                    }
                }
            }
        }
    }

    /**
     * Returns the smallest element of a collection according to the collection
     * comparator (returns {@code null} if the collection is empty).
     */
    @Parallelizable(mutexFree = false, comment="Internal use of synchronization")
    @RealTime(limit = LINEAR)
    public static final CollectionOperator<Object> MIN = new CollectionOperator<Object>() {

        @Override
        public Object apply(final CollectionService<Object> objects) {
            MinConsumer<Object> minConsumer = new MinConsumer<Object>(
                    objects.comparator());
            objects.forEach(minConsumer, null);
            return minConsumer.min;
        }

    };

    private static class MinConsumer<E> implements Consumer<E> {
        private final Comparator<? super E> comparator;
        private E min;

        public MinConsumer(Comparator<? super E> comparator) {
            this.comparator = comparator;
        }

        @Override
        public void accept(E param) {
            E currentMin = min;
            if ((currentMin == null)
                    || comparator.compare(param, currentMin) < 0) {
                synchronized (this) { // Exclusive lock.
                    if ((currentMin == min)
                            || (comparator.compare(param, min) < 0)) {
                        min = param;
                    }
                }
            }
        }
    }

    /**
    * Conditional 'and' operator (returns {@code true} if the collection is 
    * empty). This operator stops iterating as soon as a {@code false} value
    * is found.
    */
    @Parallelizable
    @RealTime(limit = LINEAR)
    public static final CollectionOperator<Boolean> AND = new CollectionOperator<Boolean>() {

        @Override
        public Boolean apply(final CollectionService<Boolean> booleans) {
            AndConsumer andConsumer = new AndConsumer();
            booleans.forEach(andConsumer, andConsumer);
            return andConsumer.result;
        }
    };

    private static class AndConsumer implements Consumer<Boolean>,
            IterationController {
        private volatile boolean result = true;

        @Override
        public void accept(Boolean param) {
            if (!param) {
                result = false;
            }
        }

        @Override
        public boolean doSequential() {
            return false;
        }

        @Override
        public boolean doReversed() {
            return false;
        }

        @Override
        public boolean isTerminated() {
            return !result;
        }
    }

    /**
    * Conditional 'or' operator (returns {@code false} if the collection is 
    * empty). This operator stops iterating as soon as a {@code true} value
    * is found.
     */
    @Parallelizable
    @RealTime(limit = LINEAR)
    public static final CollectionOperator<Boolean> OR = new CollectionOperator<Boolean>() {

        @Override
        public Boolean apply(final CollectionService<Boolean> booleans) {
            OrConsumer orConsumer = new OrConsumer();
            booleans.forEach(orConsumer, orConsumer);
            return orConsumer.result;
        }
    };

    private static class OrConsumer implements Consumer<Boolean>,
            IterationController {
        private volatile boolean result = false;

        @Override
        public void accept(Boolean param) {
            if (param) {
                result = true;
            }
        }

        @Override
        public boolean doSequential() {
            return false;
        }

        @Override
        public boolean doReversed() {
            return false;
        }

        @Override
        public boolean isTerminated() {
            return result;
        }
    }

    /**
     * Returns the sum of the specified integers value (returns {@code 0} 
     * if the collection is empty).
     */
    @Parallelizable(comment="Internal use of AtomicInteger")
    @RealTime(limit = LINEAR)
    public static final CollectionOperator<Integer> SUM = new CollectionOperator<Integer>() {

        @Override
        public Integer apply(final CollectionService<Integer> integers) {
            final AtomicInteger sum = new AtomicInteger(0);
            integers.forEach(new Consumer<Integer>() {

                @Override
                public void accept(Integer element) {
                    sum.getAndAdd(element.intValue());
                }
            }, null);
            return sum.get();
        }

    };

}