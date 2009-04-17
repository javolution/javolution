/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2006 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package _templates.javolution.text;

import java.io.IOException;


/**
 * <p> This interface is replaced by <code>java.lang.Appendable</code>
 *     in JDK 1.5+.</p>
 * 
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 5.3, January 10, 2008
 */
public interface Appendable  {

    /**
     * Appends the specified character. 
     *
     * @param  c the character to append.
     * @return <code>this</code>
     */
    Appendable append(char c) throws IOException;

    /**
     * Appends the specified character sequence. 
     *
     * @param  csq the character sequence to append.
     * @return <code>this</code>
     */
    Appendable append(_templates.java.lang.CharSequence csq) throws IOException;

    /**
     * Appends a subsequence of the specified character sequence. 
     *
     * @param  csq the character sequence to append.
     * @param  start the index of the first character to append.
     * @param  end the index after the last character to append.
     * @return <code>this</code>
     */
    Appendable append(_templates.java.lang.CharSequence csq, int start, int end) throws IOException;

}