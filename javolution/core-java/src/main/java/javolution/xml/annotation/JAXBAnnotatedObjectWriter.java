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

import javax.xml.bind.JAXBElement;
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
 * @version 6.2 August 11th, 2015
 */
public interface JAXBAnnotatedObjectWriter {

	/**
	 * Method to write a root element registered with this writer to XML via an OutputStream
	 * @param object Root Object to Write
	 * @param outputStream OutputStream to Write to
	 * @param <T> Type of JAXB Object
	 * @throws JAXBException if an exception occurs while writing the XML
	 */
	<T> void write(final T object, final OutputStream outputStream) throws JAXBException;

	/**
	 * Method to write a root element registered with this writer to XML via a Writer
	 * @param object Root Object to Write
	 * @param writer Writer to Write With
	 * @param <T> Type of JAXB Object
	 * @throws JAXBException if an exception occurs while writing the XML
	 */
	<T> void write(final T object, final Writer writer) throws JAXBException;

	/**
	 * Method to write a JAXBElement wrapped non-root element registered with this writer to XML via an OutputStream
	 * @param jaxbElement JAXBElement to Write Object to Write
	 * @param outputStream OutputStream to Write to
	 * @param <T> Type of JAXB Object
	 * @throws JAXBException if an exception occurs while writing the XML
	 */
	<T> void write(final JAXBElement<T> jaxbElement, final OutputStream outputStream) throws JAXBException;

	/**
	 * Method to write a JAXBElement wrapped non-root element registered with this writer to XML via a Writer
	 * @param jaxbElement JAXBElement to Write Object to Write
	 * @param writer Writer to Write With
	 * @param <T> Type of JAXB Object
	 * @throws JAXBException if an exception occurs while writing the XML
	 */
	<T> void write(final JAXBElement<T> jaxbElement, final Writer writer) throws JAXBException;

	/**
	 * Method to set whether to write elements containing XML-reserved characters as CData
	 * @param useCDATA TRUE to use CData, FALSE otherwise
	 */
	void setUseCDATA(final boolean useCDATA);

	/**
	 * Method to set whether to enable annotation-validation
	 * @param validating TRUE if enabling annotation-validation, FALSE otherwise
	 */
	void setValidating(final boolean validating);

}
