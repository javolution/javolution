/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util.function;

import javolution.annotation.StackSafe;
import javolution.context.ConcurrentContext;
import javolution.context.LocalContext;
import javolution.util.service.CollectionService;

/**
* <p> Defines common operators to perform operations over collections in parallel.</p>
* 
* <p> The implementation of the operators of this class is based upon 
*     {@link ConcurrentContext}, the collection is broken up into smaller 
*     collections, the number of sub-collections depends on the 
*     {@link ConcurrentContext#CONCURRENCY local concurrency}. If the 
*     concurrency is disabled, no split-up occurs and the 
*     {@link SequentialOperators sequential operators} are called.</p>
 *                  
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0, December 12, 2012
 */
@StackSafe(initialization = false)
public class ParallelOperators {

    /**
     * The logical 'and' operator. 
     */
    public static final Operator<Boolean> AND = new Operator<Boolean>() {

        @Override
        public Boolean apply(CollectionService<Boolean> booleans) {
            ParallelLogic<Boolean>[] logics = executeInParallel(
                    SequentialOperators.AND, booleans);
            if (logics == null)
                return SequentialOperators.AND.apply(booleans);
            for (int i = 0; i < logics.length; i++) {
                if (!logics[i].result.booleanValue())
                    return false;
            }
            return true;
        }

    };

    /**
     * The logical 'or' operator.
     */
    public static final Operator<Boolean> OR = new Operator<Boolean>() {

        @Override
        public Boolean apply(CollectionService<Boolean> booleans) {
            ParallelLogic<Boolean>[] logics = executeInParallel(
                    SequentialOperators.OR, booleans);
            if (logics == null)
                return SequentialOperators.OR.apply(booleans);
            for (int i = 0; i < logics.length; i++) {
                if (logics[i].result.booleanValue())
                    return true;
            }
            return false;
        }

    };

    /**
     * Returns an operator which returns any object of the specified 
     * type (and different from <code>null</code>). 
     */
    public static <T> Operator<T> any(final Class<T> type) {
        return new Operator<T>() {

            @Override
            public T apply(CollectionService<T> collection) {
                Operator<T> sequentialOperator = SequentialOperators.any(type);
                ParallelLogic<T>[] logics = executeInParallel(
                        sequentialOperator, collection);
                if (logics == null)
                    return sequentialOperator.apply(collection);
                for (int i = 0; i < logics.length; i++) {
                    if (type.isInstance(logics[i].result))
                        return logics[i].result;
                }
                return null; // None found.
            }

        };
    }

    /**
     * The integer sum operator.
     */
    public static final Operator<Integer> INTEGER_SUM = new Operator<Integer>() {

        @Override
        public Integer apply(CollectionService<Integer> integers) {
            ParallelLogic<Integer>[] logics = executeInParallel(
                    SequentialOperators.INTEGER_SUM, integers);
            if (logics == null)
                return SequentialOperators.INTEGER_SUM.apply(integers);
            int sum = 0;
            for (int i = 0; i < logics.length; i++) {
                sum += logics[i].result;
            }
            return sum;
        }

    };

    /**
     * The long sum operator.
     */
    public static final Operator<Long> LONG_SUM = new Operator<Long>() {

        @Override
        public Long apply(CollectionService<Long> longs) {
            ParallelLogic<Long>[] logics = executeInParallel(
                    SequentialOperators.LONG_SUM, longs);
            if (logics == null)
                return SequentialOperators.LONG_SUM.apply(longs);
            long sum = 0;
            for (int i = 0; i < logics.length; i++) {
                sum += logics[i].result;
            }
            return sum;
        }

    };

    /** The logic to be executed in parallel. */
    private static class ParallelLogic<T> implements Runnable {
        private final CollectionService<T> collection;
        private final Operator<T> operator;
        T result;

        public ParallelLogic(CollectionService<T> collection,
                Operator<T> operator) {
            this.collection = collection;
            this.operator = operator;
        }

        @Override
        public void run() {
            result = operator.apply(collection);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> ParallelLogic<T>[] executeInParallel(Operator<T> op,
            CollectionService<T> collection) {
        int concurrency = LocalContext
                .getLocalValue(ConcurrentContext.CONCURRENCY);
        CollectionService<T>[] splits = collection.trySplit(concurrency);
        if (splits == null)
            return null;
        ParallelLogic<T>[] logics = new ParallelLogic[splits.length];
        for (int i = 0; i < splits.length; i++) {
            logics[i] = new ParallelLogic<T>(splits[i], op);
        }
        ConcurrentContext.execute(logics);
        return logics;
    }

}