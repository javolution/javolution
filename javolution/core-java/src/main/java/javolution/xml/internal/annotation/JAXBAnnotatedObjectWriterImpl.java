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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.MarshalException;
import javax.xml.bind.ValidationException;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;

import javolution.osgi.internal.OSGiServices;
import javolution.text.CharArray;
import javolution.util.FastCollection;
import javolution.util.FastMap;
import javolution.xml.annotation.JAXBAnnotatedObjectWriter;
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
 * Note: Logging is left commented out, as it's too slow to leave on in a
 * release build - even at a non-visible level such as debug. To enable,
 * find/replace //LogContext -> //LogContext
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

		final String rootElementName = _classElementNameCache.get(object.getClass());
		final String rootNamespace = _classNameSpaceCache.get(object.getClass());
		writeElement(defaultNamespace, rootNamespace, object, rootElementName, writer);

		writer.writeEndDocument();
		writer.flush();
	}

	private void writeAttributes(final Object element, final XMLStreamWriter writer) throws IllegalArgumentException, IllegalAccessException, XMLStreamException, ValidationException {
		final Class<?> elementClass = element.getClass();
		final CacheData cacheData = _classCacheData.get(elementClass);
		final FastCollection<Field> attributeFields = cacheData._attributeFieldsCache.values();

		for(final Field field : attributeFields){
			writeAttributeValue(element, field, writer);
		}
	}

	private void writeAttributeValue(final Object element, final Field field, final XMLStreamWriter writer) throws IllegalArgumentException, IllegalAccessException, XMLStreamException, ValidationException{
		final CharArray attributeName = getXmlAttributeName(field);

		field.setAccessible(true);
		final Object value = field.get(element);

		if(_isValidating && value == null && field.isAnnotationPresent(XmlAttribute.class) &&
				field.getAnnotation(XmlAttribute.class).required()){
			throw new ValidationException("Missing Required Attribute Value: Field = "+field.getName());
		}
		else if(value != null){
			writer.writeAttribute(attributeName, String.valueOf(value));
		}
	}

	private void writeElement(final String defaultNamespace, final String rootNamespace, final Object element, final String elementName, final XMLStreamWriter writer) throws MarshalException{
		final Class<?> elementClass = element.getClass();

		try {
			if(_registeredClassesCache.contains(elementClass)){
				//LogContext.info("writeElement: "+elementName);

				final CacheData cacheData = _classCacheData.get(elementClass);
				final FastMap<CharArray, Field> elementFieldCache = cacheData._elementFieldCache;
				final Field xmlValueField = cacheData._xmlValueField;

				// Normal Element Processing
				final Iterator<CharArray> propOrder = getXmlPropOrder(elementClass);
				final boolean noProperties = !propOrder.hasNext();

				if(xmlValueField == null) {

					// Attempt to mimic how the JDK handles namespace prefixes for 1:1
					// output compatibility. From test observations, it seems that the
					// JDK will prefix ns2 to the root element, with the namespace gotten
					// from annotation scan, except if the element is wrapped with JAXBElement<T>,
					// in which case the root element's scanned value will become the default namespace,
					/// and ns2 prefix is given to the namespace specified in JAXBElement<T>
					if(noProperties || elementFieldCache.isEmpty()){
						writer.writeEmptyElement(elementName);
					}
					else if(rootNamespace == null){
						writer.writeStartElement(elementName);
					}
					else {
						writer.writeStartElement("ns2", elementName, rootNamespace);
					}

					if(rootNamespace != null){
						if(defaultNamespace == null) {
							writer.writeNamespace("ns2", rootNamespace);
						}
						else {
							writer.writeNamespace("ns2", defaultNamespace);
							writer.writeDefaultNamespace(rootNamespace);
						}
					}

					writeAttributes(element, writer);

					// If the element has only attributes, return here (the end element will be
					// written after returning)
					if(elementFieldCache.isEmpty()){
						return;
					}
				}
				// Complex Types W/ Simple Values - @XmlValue detection
				else {
					final Object fieldValue = xmlValueField.get(element);

					if(fieldValue == null) {
						writer.writeEmptyElement(elementName);
						writeAttributes(element, writer);
					}
					else {
						//LogContext.info("writeElementXmlValue: "+xmlValueField.getName());

						writer.writeStartElement(elementName);
						writeAttributes(element, writer);

						final InvocationClassType invocationClassType = getInvocationClassType(fieldValue.getClass());

						writeDirectElementValue(xmlValueField, invocationClassType, elementName, fieldValue, writer);
					}

					//LogContext.info("writeEndElement: "+elementName);
					writer.writeEndElement();
					return;
				}

				while(propOrder.hasNext()){
					final CharArray prop = propOrder.next();
					final Field field = elementFieldCache.get(prop);
					final Object fieldValue = field.get(element);

					if(fieldValue == null) {
						if(_isValidating && fieldValue == null && field.isAnnotationPresent(XmlElement.class) &&
								field.getAnnotation(XmlElement.class).required()){
							throw new ValidationException("Missing Required Element Value: Field = "+field.getName());
						}

						continue;
					}

					final Class<?> fieldClass = field.getType();
					String fieldElementName = _fieldElementNameCache.get(field);

					InvocationClassType invocationClassType = getInvocationClassType(fieldClass);

					if(invocationClassType != null) {
						writeBasicElementValue(field, invocationClassType, fieldElementName, fieldValue, writer);
					}
					else if(fieldClass.isAssignableFrom(List.class)){
						final List<?> list = (List<?>) fieldValue;
						final Class<?> genericClass = getGenericType(field);
						//LogContext.info("writeElementList: "+fieldElementName);

						for(final Object listElement : list){
							final Class<?> listElementClass;

							// If the list has mapped elements, it's generic type will
							// be Object. In that case we need to probe the real type of
							// each object in the list.
							if(genericClass == Object.class || _xmlSeeAlsoCache.contains(genericClass)) {
								listElementClass = listElement.getClass();
								fieldElementName = _classElementNameCache.get(listElementClass);
							}
							else {
								listElementClass = genericClass;
							}

							invocationClassType = getInvocationClassType(listElementClass);

							if(invocationClassType == null) {
								//LogContext.info("writeElementListComplex: "+fieldElementName);
								writeElement(null, null, listElement, fieldElementName, writer);
							}
							else {
								//LogContext.info("writeElementListBasicOrEnum: "+fieldElementName);
								writeBasicElementValue(field, invocationClassType, fieldElementName, listElement, writer);
							}
						}
					}
					else {
						//LogContext.info("writeElementComplex: "+fieldElementName);
						writeElement(null, null, fieldValue, fieldElementName, writer);
					}
				}

				// Prevent a Double Pop in the Case of an Empty Element
				if(!noProperties){
					//LogContext.info("writeEndElement: "+elementName);
					writer.writeEndElement();
				}
			}
		}
		catch(final Exception e){
			throw new MarshalException("Error Writing Element", e);
		}
	}

	private void writeBasicElementValue(final Field field, final InvocationClassType invocationClassType, final String fieldName, final Object fieldValue, final XMLStreamWriter writer) throws XMLStreamException {
		//LogContext.info("writeBasicOrEnum: "+fieldName);

		final String stringValue = getElementValue(field, invocationClassType, fieldValue);

		//LogContext.info("WriteBasicOrEnumValue: "+stringValue);

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

	private void writeDirectElementValue(final Field field, final InvocationClassType invocationClassType, final String fieldName, final Object fieldValue, final XMLStreamWriter writer) throws XMLStreamException {
		//LogContext.info("writeDirect: "+fieldName);

		final String stringValue = getElementValue(field, invocationClassType, fieldValue);

		//LogContext.info("WriteDirectElementValue: "+stringValue);

		if(_isUsingCDATA && CDATA_CHARACTERS.matcher(stringValue).find()){
			writer.writeCData(stringValue);
		}
		else {
			writer.writeCharacters(stringValue);
		}
	}

	private InvocationClassType getInvocationClassType(final Class<?> classType){
		InvocationClassType invocationClassType = InvocationClassType.valueOf(classType);

		if(invocationClassType == null && classType.isEnum()){
			invocationClassType = InvocationClassType.ENUM;
		}

		return invocationClassType;
	}

	private String getDateValue(final XmlSchemaTypeEnum dateType, final Object fieldValue){
		final Date date = ((XMLGregorianCalendar)fieldValue).toGregorianCalendar().getTime();
		final DateFormat dateFormat;
		final String dateValue;

		switch(dateType){

		case DATE:
			dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			dateValue = dateFormat.format(date);
			break;

		case TIME:
			dateFormat = new SimpleDateFormat("hh:mm:ssZ");
			dateValue = dateFormat.format(date);
			break;

		default:
			dateFormat = null;
			dateValue = String.valueOf(fieldValue);
			break;
		}

		return dateValue;
	}

	private String getElementValue(final Field field, final InvocationClassType invocationClassType, final Object fieldValue){
		final String elementValue;

		switch(invocationClassType){

		case XML_GREGORIAN_CALENDAR:
			final XmlSchemaTypeEnum dateType = _xmlSchemaTypeCache.get(field);
			elementValue = getDateValue(dateType, fieldValue);
			break;

		default:
			elementValue = String.valueOf(fieldValue);
			break;

		}

		return elementValue;
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