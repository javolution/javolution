/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.lang;

import javolution.util.FastCollection;

/**
 * <p> This interface represents a {@link Functor} specialization 
 *     which states or affirms the attribute or quality of something.</p>
 * 
 * <p> This interface is particularly useful when working with 
 *     {@link FastCollection}.</p>
 * 
 * @see <a href="http://en.wikipedia.org/wiki/Predicate_functor_logic">
 * Wikipedia: Predicate functor logic<a>    
 *                  
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0, December 12, 2012
 */
public interface Predicate<P> extends Functor<P, Boolean> {

}