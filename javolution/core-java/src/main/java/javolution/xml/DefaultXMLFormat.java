/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.xml;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * <p> Specifies the default xml format of a class (for xml serialization/deserialization). 
 *     The default format is used by the {@link javolution.xml.XMLObjectReader}
 *     and {@link javolution.xml.XMLObjectWriter} classes. It can be locally overridden 
 *     in the scope of a {@link javolution.xml.XMLContext XMLContext}.</p>
 *     
 * [code]
 * @DefaultXMLFormat(Complex.XML.class) 
 * public class Complex {
 *     public Complex(double real, double imaginary) { ... }
 *     public double getReal() { ... }
 *     public double getImaginary() { ... }
 *     public static class XML extends XMLFormat<Complex> { 
 *          public Complex newInstance(Class<? extends Complex> cls, InputElement xml) throws XMLStreamException {
 *              return new Complex(xml.getAttribute("real", 0.0), xml.getAttribute("imaginary", 0.0)); 
 *          }
 *          public void read(InputElement xml, Complex c) throws XMLStreamException {
 *              // Immutable object, no further processing.
 *          }
 *          public void write(Complex c, OutputElement xml) throws XMLStreamException {
 *              xml.setAttribute("real", c.getReal()); 
 *              xml.setAttribute("imaginary", c.getImaginary());
 *          }
 *     };      
 * }[/code] 
 * 
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0, July 21, 2013
 */
@Documented
@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DefaultXMLFormat {

    /**
     * Returns the default xml format of the annotated class.
     */
    @SuppressWarnings("rawtypes")
    Class<? extends XMLFormat> value();

}
