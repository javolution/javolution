/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util.function;

import java.util.Comparator;
import java.util.concurrent.atomic.AtomicInteger;

import javolution.annotation.StackSafe;
import javolution.annotation.ThreadSafe;
import javolution.util.service.CollectionService;

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
     * Returns any non-null element. This operator stops iterating as soon as
     * a non-null element is found.
     */
    public static final CollectionOperator<Object> ANY = new CollectionOperator<Object>() {

        @Override
        public Object apply(CollectionService<Object> objects) {
            final Object[] found = new Object[1];
            objects.forEach(new CollectionConsumer<Object>() {

                @Override
                public void accept(Object element, Controller controller) {
                    if (element == null)
                        return;
                    found[0] = element;
                    controller.terminate();
                }
            });
            return found[0];
        }

    };

    /**
     * Returns the greatest element of a collection according to the collection
     * comparator (returns {@code null} if the collection is empty).
     */
    public static final CollectionOperator<Object> MAX = new CollectionOperator<Object>() {

        @Override
        public Object apply(final CollectionService<Object> objects) {
            final Object[] max = new Object[1];
            objects.forEach(new CollectionConsumer<Object>() {
                final Comparator<? super Object> cmp = objects.comparator();
                
                @Override
                public void accept(Object element, Controller controller) {
                    Object currentMax = max[0];
                    if ((currentMax == null) || cmp.compare(element, currentMax) > 0) {
                        synchronized (this) {  // Exclusive lock.
                            if ((currentMax == max[0]) || (cmp.compare(element, max[0]) > 0)) {
                                max[0] = element;
                            }
                        }
                    }
                }
            });         
            return max[0];
        }

    };

    /**
     * Returns the smallest element of a collection according to the collection
     * comparator (returns {@code null} if the collection is empty).
     */
    public static final CollectionOperator<Object> MIN = new CollectionOperator<Object>() {
        
        @Override
        public Object apply(final CollectionService<Object> objects) {
            final Object[] min = new Object[1];
            objects.forEach(new CollectionConsumer<Object>() {
                final Comparator<? super Object> cmp = objects.comparator();
                
                @Override
                public void accept(Object element, Controller controller) {
                    Object currentMin = min[0];
                    if ((currentMin == null) || cmp.compare(element, currentMin) < 0) {
                        synchronized (this) { // Exclusive lock.
                            if ((currentMin == min[0]) || (cmp.compare(element, min[0]) < 0)) {
                                min[0] = element;
                            }
                        }
                    }
                }
            });         
            return min[0];
        }

    };

    /**
    * Conditional 'and' operator (returns {@code true} if the collection is 
    * empty). This operator stops iterating as soon as a {@code false} value
    * is found.
    */
    public static final CollectionOperator<Boolean> AND = new CollectionOperator<Boolean>() {
               
        @Override
        public Boolean apply(final CollectionService<Boolean> booleans) {
            final boolean[] result = new boolean[] { true };
            booleans.forEach(new CollectionConsumer<Boolean>() {
                  
                @Override
                public void accept(Boolean element, Controller controller) {
                    if (element.booleanValue()) return;
                    result[0] = false;
                    controller.terminate();
                }
            });         
            return result[0];
        }
    };

    /**
    * Conditional 'or' operator (returns {@code false} if the collection is 
    * empty). This operator stops iterating as soon as a {@code true} value
    * is found.
     */
    public static final CollectionOperator<Boolean> OR = new CollectionOperator<Boolean>() {
        
        @Override
        public Boolean apply(final CollectionService<Boolean> booleans) {
            final boolean[] result = new boolean[] { false };
            booleans.forEach(new CollectionConsumer<Boolean>() {
                  
                @Override
                public void accept(Boolean element, Controller controller) {
                    if (!element.booleanValue()) return;
                    result[0] = true;
                    controller.terminate();
                }
            });         
            return result[0];
        }
    };

    /**
     * Returns the sum of the specified integers value (returns {@code 0} 
     * if the collection is empty).
     */
    public static final CollectionOperator<Integer> SUM = new CollectionOperator<Integer>() {
     
        @Override
        public Integer apply(final CollectionService<Integer> integers) {
            final AtomicInteger sum = new AtomicInteger(0);
            integers.forEach(new CollectionConsumer<Integer>() {
                  
                @Override
                public void accept(Integer element, Controller controller) {
                    sum.getAndAdd(element.intValue());
                }
            });         
            return sum.get();
        }
        
    };

}