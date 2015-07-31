/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 *
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.xml.annotation;

import javax.xml.bind.JAXBException;

/**
 * <p> This interface provides factory methods for creating JAXB Annotation
 *     Readers &amp; Writers for given root element classes.</p>
 *     
 * [code]
 * JAXBAnnotationFactory factory = OSGiServices.getJAXBAnnotationFactory(); 
 * JAXBAnnotatedObjectReader reader = factory.createJAXBAnnotatedObjectReader(TestRoot.class);
 * TestRoot testRoot = reader.read(new StringReader(XML_STRING));
 * 
 * JAXBAnnotatedObjectWriter writer = factory.createJAXBAnnotatedObjectWriter(TestRoot.class);
 * writer.write(testRoot);
 * [/code] 
 *     
 * @author  <a href="mailto:starlightknight@slkdev.net">Aaron Knight</a>
 * @version 6.2 July 30th, 2015
 */
public interface JAXBAnnotationFactory {
	JAXBAnnotatedObjectReader createJAXBAnnotatedObjectReader(final Class<?> inputClass) throws JAXBException;
	JAXBAnnotatedObjectWriter createJAXBAnnotatedObjectWriter(final Class<?> outputClass) throws JAXBException;
}
