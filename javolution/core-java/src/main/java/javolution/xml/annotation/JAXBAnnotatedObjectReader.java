/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 *
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.xml.annotation;

import java.io.InputStream;
import java.io.Reader;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.xml.sax.InputSource;

/**
 * <p> This interface provides support for reading XML and parsing it into
 *     JAXB Annotated Objects. These JAXB Annotation Readers are stateless as far
 *     as parsed data - only annotation meta info is cached as fields.</p>
 *
 * <p> While this class provides compatibility with JAXB Annotations, it is NOT a
 *     full JAXB implementation.
 *
 *     The following JAXB annotations are supported: </p>
 *     <ul>
 *     <li>XmlAccessorType (with XmlAccessType.FIELD or XmlAccessType.NONE only)</li>
 *     <li>XmlAttribute</li>
 *     <li>XmlElement</li>
 *     <li>XmlElements</li>
 *     <li>XmlRegistry</li>
 *     <li>XmlRootElement</li>
 *     <li>XmlSchema</li>
 *     <li>XmlTransient</li>
 *     <li>XmlType</li>
 *     </ul>
 *
 * <p> The implementation provided in Javolution is aimed at schema objects
 *     that are generated from an XSD with the XJC Binding Compiler distributed
 *     with the JDK.</p>
 *
 * <p> This implementation supports both XJC-generated ObjectFactories and extended
 *     custom object factories. To register custom object factories, use the setObjectFactories
 *     method.</p>
 *
 *     <p>If an ObjectFactory is not found for a given element's namespace (should only be possible
 *     in the case of hand-made contract objects), then the implementation will attempt to create the
 *     objects via Reflection using the default constructors.</p>
 *
 * <p><b>NOTE:</b> You do not need to call setObjectFactories for the default XJC object
 *    factories. They will be detected automatically.</p>
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
 * JAXBAnnotatedObjectReader reader = factory.createJAXBAnnotatedObjectReader(TestRoot.class);
 * TestRoot testRoot = reader.read(new StringReader(XML_STRING));
 * [/code]
 *
 * @author  <a href="mailto:starlightknight@slkdev.net">Aaron Knight</a>
 * @version 6.2 August 9th, 2015
 */
public interface JAXBAnnotatedObjectReader {

	/**
	 * <p>Method to read a root element registered with this reader with an InputSource</p>
	 *
	 * @param inputSource InputSource to Read
	 * @param <T> Type of JAXB Object
	 * @return JAXB Object Representing the XML
	 * @throws JAXBException if an error occurs while reading the object
	 */
	<T> T read(final InputSource inputSource) throws JAXBException;

	/**
	 * <p>Method to read a root element registered with this reader with an
	 * InputStream. Encoding with be detected automatically.</p>
	 *
	 * @param inputStream InputStream to Read
	 * @param <T> Type of JAXB Object
	 * @return JAXB Object Representing the XML
	 * @throws JAXBException if an error occurs while reading the object
	 */
	<T> T read(final InputStream inputStream) throws JAXBException;

	/**
	 * <p>Method to read a root element registered with this reader with an
	 * InputStream and the Specified Encoding</p>
	 *
	 * @param inputStream InputStream to Read
	 * @param encoding Encoding of the XML Stream
	 * @param <T> Type of JAXB Object
	 * @return JAXB Object Representing the XML
	 * @throws JAXBException if an error occurs while reading the object
	 */
	<T> T read(final InputStream inputStream, final String encoding) throws JAXBException;

	/**
	 * <p>Method to read a root element registered with this reader with a Reader</p>
	 *
	 * @param reader Reader to Read With
	 * @param <T> Type of JAXB Object
	 * @return JAXB Object Representing the XML
	 * @throws JAXBException if an error occurs while reading the object
	 */
	<T> T read(final Reader reader) throws JAXBException;

	/**
	 * <p>Method to read a root element registered with this reader with a Source.</p>
	 *
	 * <p><b>NOTE:</b> Only StreamSource is supported. This is a convenience method for those declaring
	 * interface types.</p>
	 *
	 * @param source Source to Read
	 * @param <T> Type of JAXB Object
	 * @return JAXB Object Representing the XML
	 * @throws JAXBException if an error occurs while reading the object
	 */
	<T> T read(final Source source) throws JAXBException;

	/**
	 * <p>Method to read a non-root element registered with this reader with a Source.</p>
	 *
	 * <p><b>NOTE:</b> Only StreamSource is supported. This is a convenience method for those declaring
	 * interface types.</p>
	 *
	 * @param source Source to Read
	 * @param targetClass Class to Read
	 * @param <T> Type of JAXB Object
	 * @return JAXB Object Representing the XML
	 * @throws JAXBException if an error occurs while reading the object
	 */
	<T> JAXBElement<T> read(final Source source, final Class<T> targetClass) throws JAXBException;

	/**
	 * <p>Method to read a root element registered with this reader with a StreamSource.</p>
	 *
	 * @param streamSource Source to Read
	 * @param <T> Type of JAXB Object
	 * @return JAXB Object Representing the XML
	 * @throws JAXBException if an error occurs while reading the object
	 */
	<T> T read(final StreamSource streamSource) throws JAXBException;

	/**
	 * <p>Method to read a non-root element registered with this reader with a StreamSource.</p>
	 *
	 * @param streamSource Source to Read
	 * @param targetClass Class to Read
	 * @param <T> Type of JAXB Object
	 * @return JAXBElement Wrapping the Read Class
	 * @throws JAXBException if an error occurs while reading the object
	 */
	<T> JAXBElement<T> read(final StreamSource streamSource, final Class<T> targetClass) throws JAXBException;

	/**
	 * <p>Method to Set Custom Object Factories. These Object factories my extend and
	 * enhance the default XJC-generated object factories.</p>
	 *
	 * <p><b>NOTE:</b> You do not need to set default XJC object factories with this method - they
	 * will be detected automatically</p>
	 *
	 * @param objectFactories A List of Custom Object Factories to Register
	 * @throws JAXBException If an error occurs while initializing the custom object factory
	 */
	void setObjectFactories(final Object... objectFactories) throws JAXBException;

	/**
	 * <p>Method to Turn On/Off Annotation Validation. Validation is off by default.</p>
	 *
	 * <p>The following validations are supported:</p>
	 * <ul>
	 * <li>Unmapped Elements and Attributes (via inference from Reflection)</li>
	 * <li>Missing Required Elements and Attributes (via “required” annotation attribute)</li>
	 * <li>Out of Order Elements (via “propOrder” attribute)</li>
	 * </ul>
	 *
	 * @param validating TRUE if validation should be enabled, FALSE otherwise
	 */
	void setValidating(final boolean validating);
}
