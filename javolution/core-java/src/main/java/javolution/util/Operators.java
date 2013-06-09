/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util;

import java.util.Comparator;
import java.util.concurrent.atomic.AtomicInteger;

import javolution.annotation.StackSafe;
import javolution.annotation.ThreadSafe;
import javolution.util.function.Predicate;
import javolution.util.service.CollectionService;
import javolution.util.service.OperatorService;

/**
 * <p> A collection of {@link StackSafe stack-safe} and 
 *     {@link ThreadSafe thread-safe} collection operators.</p>
 *     
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0, December 12, 2012
 */
@StackSafe
@ThreadSafe
public class Operators {

    /**
     * Any non-null element operator. This operator stops 
     * iterating as soon as a non-null element is found.
     */
    public static final OperatorService<Object> ANY = new OperatorService<Object>() {
        
        @Override
        public Object apply(CollectionService<Object> objects) {
            final Object[] found = new Object[1];
            Predicate<Object> search = new Predicate<Object>() {

                @Override
                public boolean test(Object obj) { // Thread-safe (if parallel collection).
                    if (obj == null) return true;
                    found[0] = obj;
                    return true; 
                }
                
            };             
            objects.doWhile(search);
            return found[0];
        }
        
    };
  
    /**
     * Returns the greatest element of a collection according to the collection
     * comparator or <code>null</code> if the collection is empty.
     */
    public static final OperatorService<Object> MAX = new OperatorService<Object>() {
        
        @Override
        public Object apply(final CollectionService<Object> objects) {
            final Comparator<? super Object> cmp = objects.comparator();
            final Object[] max = new Object[1];
            Predicate<Object> search = new Predicate<Object>() {

                @Override
                public boolean test(Object obj) { // Thread-safe (if parallel collection).
                    Object currentMax = max[0];
                    if (cmp.compare(obj, currentMax) > 0) {
                        synchronized (objects) { 
                            if ((currentMax == max[0]) || (cmp.compare(obj, max[0]) > 0)) {
                                max[0] = obj;
                            }
                        }
                    }
                    return true; 
                }
                
            };             
            objects.doWhile(search);
            return max[0];
        }
        
    };
  
    /**
     * Returns the smallest element of a collection according to the collection
     * comparator or <code>null</code> if the collection is empty.
     */
    public static final OperatorService<Object> MIN = new OperatorService<Object>() {
        
        @Override
        public Object apply(final CollectionService<Object> objects) {
            final Comparator<? super Object> cmp = objects.comparator();
            final Object[] min = new Object[1];
            Predicate<Object> search = new Predicate<Object>() {

                @Override
                public boolean test(Object obj) { // Thread-safe (if parallel collection).
                    Object currentMin = min[0];
                    if (cmp.compare(obj, currentMin) < 0) {
                        synchronized (objects) { 
                            if ((currentMin == min[0]) || (cmp.compare(obj, min[0]) < 0)) {
                                min[0] = obj;
                            }
                        }
                    }
                    return true; 
                }
                
            };             
            objects.doWhile(search);
            return min[0];
        }
        
    };
  
    /**
    * Conditional 'and' operator. This operator stops 
    * iterating as soon as a <code>false</code> element is found.
    */
   public static final OperatorService<Boolean> AND = new OperatorService<Boolean>() {
       final Predicate<Boolean> IS_TRUE = new Predicate<Boolean>() {

           @Override
           public boolean test(Boolean param) {
               return param; 
           }
           
       }; 
       
       @Override
       public Boolean apply(CollectionService<Boolean> booleans) {
           return booleans.doWhile(IS_TRUE);
       }
       
   };
 
   /**
    * Conditional 'or' operator. This operator stops 
    * iterating as soon as a <code>true</code> element is found.
    */
   public static final OperatorService<Boolean> OR = new OperatorService<Boolean>() {

       final Predicate<Boolean> IS_FALSE = new Predicate<Boolean>() {

           @Override
           public boolean test(Boolean param) {
               return !param; 
           }
           
       }; 

       @Override
       public Boolean apply(CollectionService<Boolean> booleans) {
           return !booleans.doWhile(IS_FALSE);
       }
       
   };     
          

   /**
    * Returns the sum of the specified integers value or <code>0</code> if the
    * collection is empty.
    */
   public static final OperatorService<Integer> SUM = new OperatorService<Integer>() {
       
       @Override
       public Integer apply(CollectionService<Integer> numbers) {
           final AtomicInteger sum = new AtomicInteger(0);
           Predicate<Integer> accumulate = new Predicate<Integer>() {

               @Override
               public boolean test(Integer i) { // Thread-safe (if parallel collection).
                   sum.getAndAdd(i);
                   return true; 
               }
               
           };             
           numbers.doWhile(accumulate);
           return sum.get();
       }
       
   };

 }