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
import javolution.util.service.CollectionService;

/**
* <p> Defines common operators to perform operations over collections sequentially.</p>
 *                  
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0, December 12, 2012
 */
@StackSafe(initialization=false)
public class SequentialOperators {

 
     /**
     * The logical 'and' operator. This operator stops 
     * iterating as soon as a <code>false</code> element is found.
     */
    public static final Operator<Boolean> AND = new Operator<Boolean>() {

        @Override
        public Boolean apply(CollectionService<Boolean> booleans) {
            return booleans.doWhile(IS_TRUE);
        }
        
    };
    private static final Predicate<Boolean> IS_TRUE = new Predicate<Boolean>() {

        @Override
        public Boolean apply(Boolean param) {
            return param; 
        }
        
    }; 


    /**
     * The logical 'or' operator. This operator stops 
     * iterating as soon as a <code>true</code> element is found.
     */
    public static final Operator<Boolean> OR = new Operator<Boolean>() {


        @Override
        public Boolean apply(CollectionService<Boolean> booleans) {
            return !booleans.doWhile(IS_FALSE);
        }
        
    };
   
    private static final Predicate<Boolean> IS_FALSE = new Predicate<Boolean>() {

        @Override
        public Boolean apply(Boolean param) {
            return !param; 
        }
        
    }; 
    
    /**
     * Returns an operator which returns any object of the specified 
     * type (and different from <code>null</code>). This operator stops 
     * iterating as soon as a match is found.
     */
    public static <T> Operator<T> any(final Class<T> type) {
        return new Operator<T>() {

            @SuppressWarnings("unchecked")
            @Override
            public T apply(CollectionService<T> collection) {
                final Object[] found = new Object[1];                
                collection.doWhile(new Predicate<T>() {

                    @Override
                    public Boolean apply(T param) {
                        if (!type.isInstance(param)) return true; // Continue.
                        found[0] = param;
                        return false;
                    }
                    
                });
                return (T) found[0];
            }
            
        };
    }

    /**
     * The integer sum operator.
     */
    public static final Operator<Integer> INTEGER_SUM = new Operator<Integer>() {

        @Override
        public Integer apply(CollectionService<Integer> integers) {
            final int[] sum = new int[1];
            integers.doWhile(new Predicate<Integer>() {

                @Override
                public Boolean apply(Integer param) {
                    sum[0] += param;
                    return true;
                }
                
            });
            return sum[0];
        }
        
    };
       
    /**
     * The long sum operator.
     */
    public static final Operator<Long> LONG_SUM = new Operator<Long>() {

        @Override
        public Long apply(CollectionService<Long> longs) {
            final long[] sum = new long[1];
            longs.doWhile(new Predicate<Long>() {

                @Override
                public Boolean apply(Long param) {
                    sum[0] += param;
                    return true;
                }
                
            });
            return sum[0];
        }
        
    };
       
}