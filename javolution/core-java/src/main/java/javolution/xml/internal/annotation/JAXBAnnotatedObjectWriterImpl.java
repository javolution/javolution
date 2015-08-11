/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 *
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.xml.internal.annotation;

import java.io.OutputStream;
import java.io.Writer;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.MarshalException;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import javolution.osgi.internal.OSGiServices;
import javolution.text.CharArray;
import javolution.util.FastCollection;
import javolution.xml.annotation.JAXBAnnotatedObjectWriter;
import javolution.xml.internal.stream.XMLStreamWriterImpl;
import javolution.xml.stream.XMLOutputFactory;
import javolution.xml.stream.XMLStreamException;
import javolution.xml.stream.XMLStreamWriter;

/**
 * Class to provide basic support for serializing JAXB Annotated Objects to XML
 *
 * This class is implemented as a generic Javolution StAX Writer that uses
 * reflection to parse the annotation data.
 *
 * This initial version is aimed at schema objects that are generated using XJC
 * using the default settings, and it should be sufficient to support those.
 * It does not support every JAXB annotation yet.
 *
 * This class is implemented as a fairly basic recursive loop. It has not
 * been heavily optimized or performance tuned yet.
 *
 * @author  <a href="mailto:starlightknight@slkdev.net">Aaron Knight</a>
 * @version 6.2, August 11th, 2015
 *
 */
public class JAXBAnnotatedObjectWriterImpl extends AbstractJAXBAnnotatedObjectParser implements JAXBAnnotatedObjectWriter {

	private static final Pattern CDATA_CHARACTERS = Pattern.compile("[<>&]");

	private final XMLOutputFactory _XMLFactory;

	private boolean _isValidating;
	private boolean _isUsingCDATA;

	public <T> JAXBAnnotatedObjectWriterImpl(final Class<T> inputClass) throws JAXBException {
		super(false);
		if(!inputClass.isAnnotationPresent(XmlRootElement.class) && !inputClass.isAnnotationPresent(XmlType.class))
			throw new JAXBException("Input Class Must Be A JAXB Element!");

		_XMLFactory = OSGiServices.getXMLOutputFactory();

		_isUsingCDATA = false;
		_isValidating = false;

		_registeredClassesCache.add(inputClass);
		registerContextClasses(inputClass);
	}

	@Override
	public <T> void write(final T object, final OutputStream outputStream) throws JAXBException {
		XMLStreamWriter writer = null;

		try {
			if(_registeredClassesCache.contains(object.getClass())){
				writer = _XMLFactory.createXMLStreamWriter(outputStream);
				writeObject(object, writer, null);
			}
			else {
				throw new MarshalException("Input Object's Class Is Not Registered In This Writer!");
			}
		}
		catch (final Exception e) {
			throw new MarshalException("An Unexpected Error Occurred While Writing XML!", e);
		}
		finally {
			try {
				if(writer!=null){
					writer.close();
				}
			}
			catch (final XMLStreamException e) {
			}
		}
	}

	@Override
	public <T> void write(final T object, final Writer writer) throws MarshalException{
		XMLStreamWriter xmlWriter = null;

		try {
			if(_registeredClassesCache.contains(object.getClass())){
				xmlWriter = _XMLFactory.createXMLStreamWriter(writer);
				writeObject(object, xmlWriter, null);
			}
			else {
				throw new MarshalException("Input Object's Class Is Not Registered In This Writer!");
			}
		}
		catch (final Exception e) {
			throw new MarshalException("An Unexpected Error Occurred While Reading XML!", e);
		}
		finally {
			try {
				if(xmlWriter!=null){
					xmlWriter.close();
				}
			}
			catch (final XMLStreamException e) {
			}
		}
	}

	@Override
	public <T> void write(final JAXBElement<T> jaxbElement, final OutputStream outputStream)
			throws JAXBException {
		XMLStreamWriter writer = null;

		try {
			if(_registeredClassesCache.contains(jaxbElement.getDeclaredType())){
				writer = _XMLFactory.createXMLStreamWriter(outputStream);
				writeObject(jaxbElement.getValue(), writer, jaxbElement.getName().getNamespaceURI());
			}
			else {
				throw new MarshalException("Input Object's Class Is Not Registered In This Writer!");
			}
		}
		catch (final Exception e) {
			throw new MarshalException("An Unexpected Error Occurred While Writing XML!", e);
		}
		finally {
			try {
				if(writer!=null){
					writer.close();
				}
			}
			catch (final XMLStreamException e) {
			}
		}
	}

	@Override
	public <T> void write(final JAXBElement<T> jaxbElement, final Writer writer)
			throws JAXBException {
		XMLStreamWriter xmlWriter = null;

		try {
			if(_registeredClassesCache.contains(jaxbElement.getDeclaredType())){
				xmlWriter = _XMLFactory.createXMLStreamWriter(writer);
				writeObject(jaxbElement.getValue(), xmlWriter, jaxbElement.getName().getNamespaceURI());
			}
			else {
				throw new MarshalException("Input Object's Class Is Not Registered In This Writer!");
			}
		}
		catch (final Exception e) {
			throw new MarshalException("An Unexpected Error Occurred While Reading XML!", e);
		}
		finally {
			try {
				if(xmlWriter!=null){
					xmlWriter.close();
				}
			}
			catch (final XMLStreamException e) {
			}
		}
	}

	private void writeObject(final Object object, final XMLStreamWriter writer, final String defaultNamespace) throws XMLStreamException, MarshalException {
		writer.writeStartDocument(StandardCharsets.UTF_8.name(), "1.0", true);
		((XMLStreamWriterImpl) writer).setRepairingNamespaces(true);

		if(defaultNamespace == null || "##default".equals(defaultNamespace)){
			final String namespace = _classNameSpaceCache.get(object.getClass());
			writer.setDefaultNamespace(namespace);
		}
		else {
			writer.setDefaultNamespace(defaultNamespace);
		}

		final String rootElementName = _classElementNameCache.get(object.getClass());
		writeElement(object, rootElementName, writer);

		writer.writeEndDocument();
	}

	private void writeAttributes(final Object element, final XMLStreamWriter writer) throws MarshalException, IllegalArgumentException, IllegalAccessException, XMLStreamException {
		final Class<?> elementClass = element.getClass();
		final CacheData cacheData = _classCacheData.get(elementClass);
		final FastCollection<Field> attributeFields = cacheData._attributeFieldsCache.values();

		for(final Field field : attributeFields){
			writeAttributeValue(element, field, writer);
		}
	}

	private void writeAttributeValue(final Object element, final Field field, final XMLStreamWriter writer) throws IllegalArgumentException, IllegalAccessException, XMLStreamException, MarshalException{
		final CharArray attributeName = getXmlAttributeName(field);

		field.setAccessible(true);
		final Object value = field.get(element);

		if(_isValidating && value == null && field.isAnnotationPresent(XmlAttribute.class) &&
				field.getAnnotation(XmlAttribute.class).required()){
			throw new MarshalException("Missing Required Attribute Value: Field = "+field.getName());
		}
		else if(value != null){
			writer.writeAttribute(attributeName, String.valueOf(value));
		}
	}

	private void writeElement(final Object element, final String elementName, final XMLStreamWriter writer) throws MarshalException{
		final Class<?> elementClass = element.getClass();

		try {
			if(_registeredClassesCache.contains(elementClass)){
				final Iterator<CharArray> xmlProperties = getXmlPropOrder(elementClass);

				//LogContext.info("writeElement: "+elementName);

				writer.writeStartElement(elementName);

				if(!xmlProperties.hasNext()){
					//LogContext.info("No Properties");
					writer.writeEmptyElement(elementName);
					writeAttributes(element, writer);
					writer.writeEndElement();
					return;
				}

				writeAttributes(element, writer);

				final CacheData cacheData = _classCacheData.get(elementClass);
				final FastCollection<Field> cachedFieldsSet = cacheData._elementFieldCache.values();

				for(final Field field : cachedFieldsSet){
					final Object fieldValue = field.get(element);

					if(fieldValue == null) {
						if(_isValidating && fieldValue == null && field.isAnnotationPresent(XmlElement.class) &&
								field.getAnnotation(XmlElement.class).required()){
							throw new MarshalException("Missing Required Element Value: Field = "+field.getName());
						}

						continue;
					}

					final Class<?> fieldClass = field.getType();
					final String fieldElementName = _fieldElementNameCache.get(field);

					if(isInstanceOfBasicType(fieldClass)) {
						writeBasicElementValue(fieldElementName, fieldValue, writer);
					}
					else if(fieldClass.isEnum()) {
						writeBasicElementValue(fieldElementName, fieldValue, writer);
					}
					else if(fieldClass.isAssignableFrom(List.class)){
						final List<?> list = (List<?>) fieldValue;
						final Class<?> genericClass = getGenericType(field);
						//LogContext.info("writeElementList: "+fieldElementName);

						for(final Object listElement : list){
							if(isInstanceOfBasicType(genericClass) || genericClass.isEnum()) {
								writeBasicElementValue(fieldElementName, listElement, writer);
							}
							else {
								writeElement(listElement, fieldElementName, writer);
							}
						}
					}
					else {
						//LogContext.info("writeElementComplex: "+fieldElementName);
						writeElement(fieldValue, fieldElementName, writer);
					}
				}

				//LogContext.info("writeEndElement: "+elementName);

				writer.writeEndElement();
			}
		}
		catch(final Exception e){
			throw new MarshalException("Error Writing Element", e);
		}
	}

	private void writeBasicElementValue(final String fieldName, final Object fieldValue, final XMLStreamWriter writer) throws XMLStreamException {
		//LogContext.info("writeBasicOrEnum: "+fieldName);

		final String stringValue = String.valueOf(fieldValue);

		if(stringValue == null || stringValue.isEmpty()) {
			writer.writeEmptyElement(fieldName);
		}
		else if(_isUsingCDATA && CDATA_CHARACTERS.matcher(stringValue).find()){
			writer.writeStartElement(fieldName);
			writer.writeCData(stringValue);
		}
		else {
			writer.writeStartElement(fieldName);
			writer.writeCharacters(stringValue);
		}

		writer.writeEndElement();
	}

	@Override
	public void setUseCDATA(final boolean useCDATA) {
		_isUsingCDATA = useCDATA;
	}

	@Override
	public void setValidating(final boolean validating) {
		_isValidating = validating;
	}

}