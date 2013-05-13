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
 * <p> Useful operators.</p>
 *                  
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0, December 12, 2012
 */
@StackSafe(initialization=false)
public class Operators {
  
    /**
     * Returns an operator which returns any object of the specified 
     * type (and different from <code>null</code>). 
     */
    public static <T> Operator<T> any(final Class<T> type) {
        return new Operator<T>() {

            @SuppressWarnings("unchecked")
            @Override
            public T evaluate(CollectionService<T> collection) {
                final Object[] found = new Object[1];                
                collection.doWhile(new Predicate<T>() {

                    @Override
                    public Boolean evaluate(T param) {
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
     * The logical 'and' operator. 
     */
    public static final Operator<Boolean> AND = new Operator<Boolean>() {

        @Override
        public Boolean evaluate(CollectionService<Boolean> booleans) {
            return booleans.doWhile(IS_TRUE);
        }
        
    };
    private static final Predicate<Boolean> IS_TRUE = new Predicate<Boolean>() {

        @Override
        public Boolean evaluate(Boolean param) {
            return param; 
        }
        
    }; 


    /**
     * The logical 'or' operator.
     */
    public static final Operator<Boolean> OR = new Operator<Boolean>() {


        @Override
        public Boolean evaluate(CollectionService<Boolean> booleans) {
            return !booleans.doWhile(IS_FALSE);
        }
        
    };
   
    private static final Predicate<Boolean> IS_FALSE = new Predicate<Boolean>() {

        @Override
        public Boolean evaluate(Boolean param) {
            return !param; 
        }
        
    }; 

    /**
     * The integer sum operator.
     */
    public static final Operator<Integer> INTEGER_SUM = new Operator<Integer>() {

        @Override
        public Integer evaluate(CollectionService<Integer> integers) {
            final int[] sum = new int[1];
            integers.doWhile(new Predicate<Integer>() {

                @Override
                public Boolean evaluate(Integer param) {
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
        public Long evaluate(CollectionService<Long> longs) {
            final long[] sum = new long[1];
            longs.doWhile(new Predicate<Long>() {

                @Override
                public Boolean evaluate(Long param) {
                    sum[0] += param;
                    return true;
                }
                
            });
            return sum[0];
        }
        
    };
       
}