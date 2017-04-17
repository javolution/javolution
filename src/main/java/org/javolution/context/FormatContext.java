/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.context;

/**
 * The parent class for all serializer/deserializer contexts. The context format type (plain text, XML, JSON, ...) 
 * is specified by sub-classes. Classes may identify the plain text format through the 
 * {@link javolution.text.DefaultTextFormat DefaultTextFormat} annotation
 * or the default XML format through the 
 * {@link javolution.xml.DefaultXMLFormat DefaultXMLFormat} annotation.
 * 
 * ```java
 * {@literal@}DefaultTextFormat(Complex.Cartesian.class)
 * {@literal@}DefaultXMLFormat(Complex.XML.class)
 * public Complex {
 *     public static final class Cartesian extends javolution.text.TextFormat<Complex> { ... }
 *     public static final class Polar extends javolution.text.TextFormat<Complex> { ... }
 *     public static final class XML extends javolution.text.XMLFormat<Complex> { ... }
 *     ...
 * }
 * ```
 * 
 * @author  <jean-marie@dautelle.com>
 * @version 7.0, March 31, 2017
 */
public abstract class FormatContext extends AbstractContext {

    /**
     * Default constructor.
     */
    protected FormatContext() {}

}