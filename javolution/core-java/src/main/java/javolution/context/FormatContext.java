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
 *     The default text format is given by the  
 *     {@link javolution.text.DefaultTextFormat DefaultTextFormat} annotation
 *     and the default xml format is given by the 
 *     {@link javolution.xml.DefaultXMLFormat DefaultXMLFormat} annotation 
 *     (both are inheritable runtime annotations).
 *     [code]
 *     @DefaultTextFormat(Complex.TextFormat.class)
 *     @DefaultXMLFormat(Complex.XMLFormat.class)
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
public abstract class FormatContext extends AbstractContext {

    /**
     * Default constructor.
     */
    protected FormatContext() {}

}