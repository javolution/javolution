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
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.bind.MarshalException;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchema;
import javax.xml.bind.annotation.XmlType;

import javolution.osgi.internal.OSGiServices;
import javolution.text.CharArray;
import javolution.util.FastMap;
import javolution.util.FastSet;
import javolution.util.function.Equalities;
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
 * @version 6.2, July 29th, 2015
 * 
 */
public class JAXBAnnotatedObjectWriterImpl extends AbstractJAXBAnnotationReflectionSupport implements JAXBAnnotatedObjectWriter {

	private final FastMap<Class<?>,FastSet<Field>> _attributeFieldsCache;	
	private final FastMap<Class<?>,FastSet<Field>> _elementFieldsCache;
	private final Class<?> _inputClass;
	private final XMLOutputFactory _XMLFactory;
	
	private String _defaultNamespace;
	private boolean _isValidating;

	public <T> JAXBAnnotatedObjectWriterImpl(final Class<T> inputClass) throws JAXBException {
		super();
		if(!inputClass.isAnnotationPresent(XmlRootElement.class))
			throw new MarshalException("Input Class Must Be A JAXB Annotated Root Element!");
		_attributeFieldsCache = new FastMap<Class<?>,FastSet<Field>>(Equalities.IDENTITY);
		_elementFieldsCache = new FastMap<Class<?>,FastSet<Field>>(Equalities.IDENTITY);
		_inputClass = inputClass;
		_XMLFactory = OSGiServices.getXMLOutputFactory();
		_isValidating = false;
	}

	@Override
	public <T> void write(final T object, final OutputStream outputStream) throws JAXBException {
		XMLStreamWriter writer = null;;

		try {
			if(_inputClass.isAssignableFrom(object.getClass())){
				writer = _XMLFactory.createXMLStreamWriter(outputStream);
				writeObject(object, writer);
			}
			else {
				throw new MarshalException("Input Object Type Differs From JAXB Writer Type!");
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
			if(_inputClass.isAssignableFrom(object.getClass())){
				xmlWriter = _XMLFactory.createXMLStreamWriter(writer);
				writeObject(object, xmlWriter);
			}
			else {
				throw new MarshalException("Input Object Type Differs From JAXB Writer Type!");
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
	
	private void writeObject(final Object object, final XMLStreamWriter writer) throws XMLStreamException, MarshalException {
		writer.writeStartDocument(StandardCharsets.UTF_8.name(), "1.0", true);
		((XMLStreamWriterImpl) writer).setRepairingNamespaces(true);
		
		if(_defaultNamespace == null){
			final XmlSchema rootElementSchema = object.getClass().getPackage().getAnnotation(XmlSchema.class);
		
			if(rootElementSchema != null){
				String namespace = rootElementSchema.namespace();
				writer.setDefaultNamespace(namespace);
				_defaultNamespace = namespace;
			}
		}
		else {
			writer.setDefaultNamespace(_defaultNamespace);
		}
		
		writeElement(object, writer);
		writer.writeEndDocument();
	}

	private void handleElement(Object element, Field field, XMLStreamWriter writer) throws IllegalArgumentException, IllegalAccessException, MarshalException, XMLStreamException{
		handleElement(element, field, writer, false);
	}
	
	private void handleElement(Object element, Field field, XMLStreamWriter writer, boolean handleEnum) throws IllegalArgumentException, IllegalAccessException, MarshalException, XMLStreamException{
		// Special Handling for Enums
		if(handleEnum && element != null){
			final String fieldName = field.getName();
			writer.writeStartElement(fieldName);
			writer.writeCharacters(String.valueOf(element));
			writer.writeEndElement();
			return;
		}
		
		final Class<?> fieldType = field.getType();
		
		if(isInstanceOfBasicType(fieldType)){
			writeElementValue(element, field, writer);
		}		
		else {
			field.setAccessible(true);
			Object elementValue = field.get(element);

			if(elementValue != null){
				writeElement(elementValue, writer, field);
			}
		}		
	}
	
	private void writeAttributes(final Object element, final XMLStreamWriter writer) throws MarshalException, IllegalArgumentException, IllegalAccessException, XMLStreamException {
		final Class<?> elementClass = element.getClass();
		FastSet<Field> attributeFields = _attributeFieldsCache.get(elementClass);

		if(attributeFields == null){
			attributeFields = new FastSet<Field>();

			final Field[] fields = element.getClass().getDeclaredFields();

			for(final Field field : fields){
				if(field.isAnnotationPresent(XmlAttribute.class)){
					attributeFields.add(field);
					writeAttributeValue(element, field, writer);
				}
			}

			_attributeFieldsCache.put(elementClass, attributeFields);
		}
		else {
			for(final Field field : attributeFields){
				writeAttributeValue(element, field, writer);
			}
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

	private void writeElement(final Object element, final XMLStreamWriter writer) throws MarshalException{
		writeElement(element, writer, null);
	}
	
	private void writeElement(final Object element, final XMLStreamWriter writer, final Field lastField) throws MarshalException{
		final Class<?> elementClass = element.getClass();

		if(element instanceof List){
			final List<?> list = (List<?>) element;

			for(final Object listElement : list){
				writeElement(listElement, writer, lastField);
			}

			return;
		}
		
		if(elementClass.isEnum()){
			try {
				handleElement(element, lastField, writer, true);
			} 
			catch (Exception e){
				throw new MarshalException("Error Handling Enum!",e);
			}
			
			return;
		}

		try {
			if(elementClass.isAnnotationPresent(XmlType.class)){
				final XmlType xmlType = elementClass.getAnnotation(XmlType.class);
				String localName = xmlType.name();

				if((localName == null || localName.length()==0) && elementClass.isAnnotationPresent(XmlRootElement.class)){
					final XmlRootElement xmlRootElement = elementClass.getAnnotation(XmlRootElement.class);
					localName = xmlRootElement.name();
				}

				final String[] xmlProperties = xmlType.propOrder();
								
				if(xmlProperties.length == 1 && xmlProperties[0].equals("")){
					writer.writeStartElement(localName);
					writeAttributes(element, writer);
					writer.writeEndElement();
					return;
				}
				else {
					writer.writeStartElement(localName);
				}

				writeAttributes(element, writer);

				FastSet<Field> cachedFieldsSet = _elementFieldsCache.get(elementClass);
				
				if(cachedFieldsSet!=null){
					for(final Field field : cachedFieldsSet){
						handleElement(element, field, writer);
					}
					writer.writeEndElement();
					
					return;
				}
				
				cachedFieldsSet = new FastSet<Field>(Equalities.IDENTITY);
				
				for(final String xmlProperty : xmlProperties){
					final Field field = elementClass.getDeclaredField(xmlProperty);
					cachedFieldsSet.add(field);
					
					handleElement(element, field, writer);
					
					_elementFieldsCache.put(elementClass, cachedFieldsSet);
				}
				
				writer.writeEndElement();
			}
		}
		catch(final Exception e){
			throw new MarshalException("Error Writing Element", e);
		}
	}

	private void writeElementValue(final Object element, final Field field, final XMLStreamWriter writer) throws MarshalException, IllegalArgumentException, IllegalAccessException, XMLStreamException {
		field.setAccessible(true);
		final Object value = field.get(element);

		if(_isValidating && value == null && field.isAnnotationPresent(XmlElement.class) && 
				field.getAnnotation(XmlElement.class).required()){
			throw new MarshalException("Missing Required Element Value: Field = "+field.getName());
		}
		else if(value != null){
			final String fieldName = field.getName();
			writer.writeStartElement(fieldName);
			writer.writeCharacters(String.valueOf(value));
			writer.writeEndElement();
		}
	}

	@Override
	public void setValidating(boolean validating) {
		_isValidating = validating;
	}
	
}