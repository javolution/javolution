/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2007 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.xml;

import java.io.Serializable;

/**
 * <p> This interface identifies classes supporting XML serialization 
 *     (XML serialization is still possible for classes not implementing this
 *     interface through dynamic {@link XMLBinding} though).</p>
 *     
 * <p> Typically, classes implementing this interface have a protected static
 *     {@link XMLFormat} holding their default XML representation. 
 *     For example:[code]
 *     public final class Complex implements XMLSerializable {
 *       
 *         // Use the cartesien form for the default XML representation.        
 *         protected static final XMLFormat<Complex> XML = new XMLFormat<Complex>(Complex.class) {
 *             public Complex newInstance(Class<Complex> cls, InputElement xml) throws XMLStreamException {
 *                 return Complex.valueOf(xml.getAttribute("real", 0.0), 
 *                                        xml.getAttribute("imaginary", 0.0));
 *             }
 *             public void write(Complex complex, OutputElement xml) throws XMLStreamException {
 *                 xml.setAttribute("real", complex.getReal());
 *                 xml.setAttribute("imaginary", complex.getImaginary());
 *             }
 *             public void read(InputElement xml, Complex complex) {
 *                 // Immutable, deserialization occurs at creation, ref. newIntance(...) 
*              }
 *         };
 *         ...
 *     }[/code]</p>      
 *     
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 4.2, April 15, 2007
 */
public interface XMLSerializable extends Serializable {

  // No method. Tagging interface.
    
}
