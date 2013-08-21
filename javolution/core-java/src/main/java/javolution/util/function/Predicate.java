/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util.function;

/**
 * <p> A function which states or affirms the attribute or quality of something.</p>
 * 
 * <p> Note: In future version this interface may derive from 
 *           {@code Function<P, Boolean>}.</p>

 * @param <T> The type of input object to test.
 *           
 * @see <a href="http://en.wikipedia.org/wiki/Predicate_(mathematical_logic)">
 * Wikipedia: Predicate<a>    
 *                  
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0, July 21, 2013
 */
public interface Predicate<T> {

    /**
     * Tests the specified value.
     */
    boolean test(T param);

}