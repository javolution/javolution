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

import javolution.util.function.Function;
import javolution.util.function.Predicate;
import javolution.util.service.CollectionService;
import javolution.util.service.OperatorService;

/**
 * <p> Common collection operators instances.</p>
 *                  
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0, December 12, 2012
 */
public class Operators {

    /**
     * Any element operator. This operator stops 
     * iterating as soon as a non-null element is found.
     */
    public static final OperatorService<Object> ANY = new OperatorService<Object>() {
        
        @Override
        public Object apply(CollectionService<Object> objects) {
            final Object[] found = new Object[1];
            Predicate<Object> search = new Predicate<Object>() {

                @Override
                public boolean test(Object obj) {
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
     * comparator.
     */
    public static final OperatorService<Object> MAX = new OperatorService<Object>() {
        
        @Override
        public Object apply(CollectionService<Object> objects) {
            final Comparator<? super Object> cmp = objects.getComparator();
            final Object[] max = new Object[1];
            Predicate<Object> search = new Predicate<Object>() {

                @Override
                public boolean test(Object obj) {
                    if ((obj != null) 
                            && ((max[0] == null) || (cmp.compare(obj, max[0]) > 0))) {
                        max[0] = obj;
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
     * comparator.
     */
    public static final OperatorService<Object> MIN = new OperatorService<Object>() {
        
        @Override
        public Object apply(CollectionService<Object> objects) {
            final Comparator<? super Object> cmp = objects.getComparator();
            final Object[] max = new Object[1];
            Predicate<Object> search = new Predicate<Object>() {

                @Override
                public boolean test(Object obj) {
                    if ((obj != null) 
                            && ((max[0] == null) || (cmp.compare(obj, max[0]) < 0))) {
                        max[0] = obj;
                    }
                    return true; 
                }
                
            };             
            objects.doWhile(search);
            return max[0];
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
    * Conditional 'and' operator. This operator stops 
    * iterating as soon as a <code>false</code> element is found.
    */
   public static final Function<CollectionService<Boolean>, Boolean> TEST 
   = new Function<CollectionService<Boolean>, Boolean> () {
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
          
}