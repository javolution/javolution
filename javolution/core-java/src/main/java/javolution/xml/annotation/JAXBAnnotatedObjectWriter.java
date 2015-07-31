/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 *
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.xml.annotation;

import java.io.OutputStream;
import java.io.Writer;

import javax.xml.bind.JAXBException;

/**
 * <p> This interface provides basic support for reading a JAXB Annotated Object and 
 *     serializing it to XML. These JAXB Annotation Writers are stateless as far 
 *     as read data - only annotation meta info is cached as fields.</p>
 *     
 * <p> While this class provides basic compatibility with JAXB Annotations, it is NOT a
 *     full JAXB implementation.</p> 
 *     
 *     <p>The following JAXB annotations are supported:</p>
 *     <ul>
 *     <li>XmlAccessorType (with XmlAccessType.FIELD or XmlAccessType.NONE only)</li>
 *     <li>XmlAttribute</li>
 *     <li>XmlElement</li>
 *     <li>XmlRootElement</li>
 *     <li>XmlTransient</li>
 *     <li>XmlType</li>
 *     </ul>
 *
 * <p> The implementation provided in Javolution is aimed at schema objects
 *     that are generated from an XSD with the XJC Binding Compiler distributed 
 *     with the JDK.</p>
 *     
 * <p> When validation is enabled with setValidating(true), this class uses JAXB Annotation
 *     meta-data to validate.</p> 
 *     
 *     <p>The following validations are supported:</p>
 *     <ul>
 *     <li>Unmapped Elements and Attributes (via inference from Reflection)</li>
 *     <li>Missing Required Elements and Attributes (via “required” annotation attribute)</li>
 *     <li>Out of Order Elements (via “propOrder” attribute)</li>
 *     </ul>
 *
 * <p> Usage: </p>
 * 
 * [code]
 * JAXBAnnotationFactory factory = OSGiServices.getJAXBAnnotationFactory(); 
 * JAXBAnnotatedObjectWriter writer = factory.createJAXBAnnotatedObjectWriter(TestRoot.class);
 * TestRoot testRoot = new TestRoot();
 * testRoot.setType("Test1");
 * 
 * StringWriter stringWriter = new StringWriter();
 * writer.write(testRoot, stringWriter);
 * 
 * [/code] 
 *     
 * @author  <a href="mailto:starlightknight@slkdev.net">Aaron Knight</a>
 * @version 6.2 July 30th, 2015
 */
public interface JAXBAnnotatedObjectWriter {
	<T> void write(final T object, final OutputStream outputStream) throws JAXBException;
	<T> void write(final T object, final Writer writer) throws JAXBException;
	void setValidating(final boolean validating);
}
