/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.lang;

import javolution.context.StackContext;
import javolution.util.FastCollection;

/**
 * <p> A function that perform some operation and returns the result of 
 *     that operation.</p>
 * 
 * <p> Multifunctors (functors taking an arbitrary numbers of arguments) 
 *     can be constructed using the {@link MultiVariable} class.
 *     Functors taking or returning no argument may be constructed using the 
 *     standard {@link Void} class.  
 *  [code]
 *  // Functor adding n indices to a list.
 *  Functor<MultiVariable<Index, List<Index>>, Void> addToList = new Functor<MultiVariable<Index, List<Index>>, Void>() {
 *      public Void evaluate(MultiVariable<Index, List<Index>> param) {
 *          List<Index> list = param.getRight();
 *          for (Index i = Index.ZERO; i != param.getLeft(); i = i.next()) {
 *              list.add(i);
 *          }
 *          return null;
 *      }
 *  };
 *  ...
 *  FastTable<Index> list = new FastTable();
 *  addToList.evaluate(new MultiVariable(Index.valueOf(100), list));
 *  ...
 *  [/code] 
 * 
 * <p> This interface is particularly useful when working with 
 *     {@link FastCollection} or {@link StackContext}.</p>
 * 
 * @see <a href="http://en.wikipedia.org/wiki/Functor">Wikipedia: Functor<a>    
 *                  
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0, December 12, 2012
 */
public interface Functor<P, R> {

    /**
     * Returns the result of an evaluation on the specified parameter. 
     * 
     * @param param the parameter object on which the evaluation is performed. 
     * @return the result of the evaluation.
     */
    R evaluate(P param);

}