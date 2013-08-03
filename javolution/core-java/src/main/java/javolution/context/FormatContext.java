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
 *     The context format type (plain text, XML, JSON, ...) is specified by 
 *     sub-classes. Classes may identify the plain text format through the  
 *     {@link javolution.text.DefaultTextFormat DefaultTextFormat} annotation
 *     or the default XML format through the 
 *     {@link javolution.xml.DefaultXMLFormat DefaultXMLFormat} annotation.
 * [code]
 * @DefaultTextFormat(Complex.Cartesian.class)
 * @DefaultXMLFormat(Complex.XML.class)
 * public Complex {
 *     public static final class Cartesian extends javolution.text.TextFormat<Complex> { ... }
 *     public static final class Polar extends javolution.text.TextFormat<Complex> { ... }
 *     public static final class XML extends javolution.text.XMLFormat<Complex> { ... }
 *     ...
 * }
 * [/code]</p>
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