/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.context;

/**
 * <p> The parent class for all serializer/deserializer contexts.
 *     The context format type (plain text, XML, JSON, ...) is specified by sub-classes.
 *     The default format for any given class is retrieved from the  
 *     annotation {@link javolution.annotation.Format Format} (inheritable).
 *     [code]
 *     @Format(text=Complex.TextFormat.class, xml=Complex.XMLFormat.class)
 *     public Complex {
 *           public static final class TextFormat extends javolution.text.TextFormat<Complex> { ... }
 *           public static final class XMLFormat extends javolution.text.XMLFormat<Complex> { ... }
 *           ...
 *     }
 *     [/code]</p>
 * 
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0 December 12, 2012
 */
public abstract class FormatContext<C extends FormatContext<C>> extends AbstractContext<C> {

    /**
     * Default constructor.
     */
    protected FormatContext() {
    }

}