/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 *
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.xml.internal.jaxb;

import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.ValidationException;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchema;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.javolution.osgi.internal.OSGiServices;
import org.javolution.text.CharArray;
import org.javolution.util.AbstractMap;
import org.javolution.util.AbstractSet;
import org.javolution.util.FastSet;
import org.javolution.util.FastTable;
import org.javolution.util.function.Order;
import org.javolution.xml.internal.jaxb.Caches.CacheData;
import org.javolution.xml.internal.jaxb.Caches.XmlSchemaTypeEnum;
import org.javolution.xml.jaxb.JAXBAnnotatedObjectReader;
import org.javolution.xml.stream.XMLInputFactory;
import org.javolution.xml.stream.XMLStreamConstants;
import org.javolution.xml.stream.XMLStreamException;
import org.javolution.xml.stream.XMLStreamReader;
import org.xml.sax.InputSource;

import static org.javolution.xml.internal.jaxb.Caches.CacheMode.READER;

/**
 * Class to provide basic support for deserializing JAXB Annotated XML Objects
 *
 * This class is implemented as a generic Javolution StAX handler that uses
 * reflection to parse the annotation data.
 *
 * This initial version is aimed at schema objects that are generated using XJC
 * It does not support every possible JAXB annotation yet.
 *
 * Note: Logging is left commented out, as it's too slow to leave on in a
 * release build - even at a non-visible level such as debug. To enable,
 * find/replace //LogContext -> //LogContext
 *
 * @author  <a href="mailto:starlightknight@slkdev.net">Aaron Knight</a>
 * @version 6.2, August 9th, 2015
 */
public class JAXBAnnotatedObjectReaderImpl extends AbstractJAXBAnnotatedObjectParser implements JAXBAnnotatedObjectReader {

	private boolean _isValidating;
	private final Class<?> _rootClass;
	private final DatatypeFactory _dataTypeFactory;
	private final XMLInputFactory _XMLFactory;

	public <T> JAXBAnnotatedObjectReaderImpl(final Class<T> inputClass) throws JAXBException {
		super(inputClass, READER);
		if(!inputClass.isAnnotationPresent(XmlRootElement.class) && !inputClass.isAnnotationPresent(XmlType.class))
			throw new JAXBException("Input Class Must Be A JAXB Element!");

		// Store a Handle to the Root
		_rootClass = inputClass;

		// StAX Factory
		_XMLFactory = OSGiServices.getXMLInputFactory();

		// This flag turns on/off annotation validation. There is no support yet for full schema validation
		_isValidating = false;

		// The Data Type factory is used only for interpreting XMLGregorianCalendars, until a better/faster solution is made.
		try {
			// Register Context Classes
			caches.registerContextClasses(inputClass);

			_dataTypeFactory = DatatypeFactory.newInstance();
		}
		catch (final Exception e) {
			throw new JAXBException("An Error Occurred While Scanning Context Classes!", e);
		}
	}

	@Override
	public <T> T read(final InputSource inputSource) throws JAXBException {
		if(inputSource == null){
			throw new JAXBException("Input Source Cannot Be Null!");
		}

		final T object;

		final Reader reader = inputSource.getCharacterStream();

		if(reader == null){
			object = read(inputSource.getByteStream(), inputSource.getEncoding());
		}
		else {
			object = read(reader);
		}

		return object;
	}

	@Override
	public <T> T read(final InputStream inputStream) throws JAXBException {
		return read(inputStream, null);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T read(final InputStream inputStream, final String encoding) throws JAXBException {
		XMLStreamReader reader = null;
		final T object;

		try {
			reader = _XMLFactory.createXMLStreamReader(inputStream, encoding);
			object = (T) readObject(_rootClass, reader);
		}
		catch (final SecurityException e){
			throw new UnmarshalException("An Error Occurred During Unmarshalling!", e);
		}
		catch (final XMLStreamException e) {
			throw new UnmarshalException("An Error Occurred During Unmarshalling!", e);
		}
		finally {
			try {
				if(reader!=null){
					reader.close();
				}
			}
			catch (final XMLStreamException e) {
				//Unused
			}
		}

		return object;
	}


	@Override
	@SuppressWarnings("unchecked")
	public <T> T read(final Reader reader) throws JAXBException {
		XMLStreamReader xmlReader = null;
		T object;

		try {
			xmlReader = _XMLFactory.createXMLStreamReader(reader);
			object = (T) readObject(_rootClass, xmlReader);
		}
		catch (final SecurityException e){
			throw new UnmarshalException("An Error Occurred During Unmarshalling!", e);
		}
		catch (final XMLStreamException e) {
			throw new UnmarshalException("An Error Occurred During Unmarshalling!", e);
		}
		finally {
			try {
				if(xmlReader!=null){
					xmlReader.close();
				}
			}
			catch(final XMLStreamException e){
				//Unused
			}
		}

		return object;
	}

	@Override
	public <T> T read(final Source source) throws JAXBException {
		// For Compatibility Reasons for those who use StreamSources but declare w/ interface type
		if(source instanceof StreamSource){
			return read((StreamSource)source);
		}
		else {
			throw new UnsupportedOperationException("Source Type is Not Supported!");
		}
	}

	@Override
	public <T> JAXBElement<T> read(final Source source, final Class<T> targetClass) throws JAXBException {
		if(!caches._registeredClassesCache.contains(targetClass)){
			throw new JAXBException(String.format("Class <%s> Is Not Recognized By This Reader!", targetClass));
		}

		// For Compatibility Reasons for those who use StreamSources but declare w/ interface type
		if(source instanceof StreamSource){
			return read((StreamSource)source, targetClass);
		}
		else {
			throw new UnsupportedOperationException("Source Type is Not Supported!");
		}
	}

	@Override
	public <T> T read(final StreamSource streamSource) throws JAXBException {
		if(streamSource == null){
			throw new JAXBException("Stream Source Cannot Be Null!");
		}

		T object = null;

		final Reader reader = streamSource.getReader();

		if(reader == null){
			object = read(streamSource.getInputStream());
		}
		else {
			object = read(reader);
		}

		return object;
	}

	@Override
	public <T> JAXBElement<T> read(final StreamSource streamSource, final Class<T> targetClass) throws JAXBException {
		if(!caches._registeredClassesCache.contains(targetClass)){
			throw new JAXBException(String.format("Class <%s> Is Not Recognized By This Reader!", targetClass));
		}

		T object = null;
		XMLStreamReader xmlReader = null;

		final Reader reader = streamSource.getReader();

		try{
			if(reader == null){
				xmlReader = _XMLFactory.createXMLStreamReader(streamSource.getInputStream());
			}
			else {
				xmlReader = _XMLFactory.createXMLStreamReader(reader);
			}

			object = readObject(targetClass, xmlReader);
		}
		catch (final SecurityException e){
			throw new UnmarshalException("An Error Occurred During Unmarshalling!", e);
		}
		catch (final XMLStreamException e) {
			throw new UnmarshalException("An Error Occurred During Unmarshalling!", e);
		}
		finally {
			try {
				if(xmlReader!=null){
					xmlReader.close();
				}
			}
			catch(final XMLStreamException e){
				//Unused
			}
		}

		final String namespace = caches._classNameSpaceCache.get(targetClass);
		JAXBElement<T> jaxbElement;

		if(namespace == null || "##default".equals(namespace)) {
			jaxbElement = new JAXBElement<T>(new QName(targetClass.getSimpleName()), targetClass, object);
		}
		else{
			jaxbElement = new JAXBElement<T>(new QName(namespace, targetClass.getSimpleName()), targetClass, object);
		}

		return jaxbElement;
	}

	/**
	 * Main processing for Reading a JAXB Object
	 * @param inputClass Class to Read
	 * @param reader Reader to Read With
	 */
	private <T> T readObject(final Class<T> inputClass, final XMLStreamReader reader) throws JAXBException, SecurityException, XMLStreamException {
		// Start by instantiating our final output object
		final T outputObject = reflectNewInstance(inputClass);

		// The processing in this implementation is stack based. We will make use of FastTable's implementation of the Deque interface
		final FastTable<AnnotationStackData> outputStack = new FastTable<AnnotationStackData>();

		// We'll push the output object onto the stack as an initial entry. All stack entries get wrapped in an AnnotationStackData class.
		// The fields in this class are package-private to provide as cheap of access as possible since they are used frequently.
		AnnotationStackData stackData;
		FastSet<CharArray> requiredSet = new FastSet<CharArray>(Order.lexical());
		
		if(_isValidating){
			stackData = new AnnotationStackData(AnnotationStackType.ROOT, null, outputObject, null, inputClass, null,
					requiredSet, getXmlPropOrder(inputClass));
		}
		else {
			stackData = new AnnotationStackData(AnnotationStackType.ROOT, null, outputObject, null, inputClass, null, null, null);
		}

		outputStack.push(stackData);

		// The last event state is used in future calculations as a factor in determining whether we are switching elements or not.
		// It only tracks two states: START_ELEMENT & END_ELEMENT, and will be updated as appropriate at the end of the loop.
		int lastEvent = -1;

		// This flag is used if validation is on and an unmapped element is encountered. In this case the flag will get flipped on,
		// and the code will skip until the end of the unmapped element is processed.
		boolean skipUnmappedMode = false;

		// If we do encounter an unmapped element, we will capture the name of it here.
		CharArray unmappedElement = null;

		// Storage for the last read characters across events.
		CharArray characters = null;

		// This version of local name is the one read from the StAX Reader. It is a viewport over a large CharArray that
		// Javolution moves as we process the XML
		CharArray localName;

		// This version is a persisted cache of an individual element name. It is a viewport copy of the localName. The
		// reason we create and store this is so that when we push the element name as part of the stack data, we can
		// use identity comparison on anything in the stack data, cutting out a lot of repetitive CharSequence comparison
		CharArray localXmlElementName;

		// This will reference cache data for the current stack data
		CacheData cacheData = null;
		CacheData parentCacheData = null;

		// Main Processing Loop
		while(reader.hasNext()){
			// This flag will let us know later whether we are continuing with a new instance of the same element, or switching.
			boolean continuingSameElement = false;

			// Pull the StAX Event
			final int event = reader.next();

			switch(event){

			case XMLStreamConstants.CHARACTERS:
				if(reader.isWhiteSpace()) {
					characters = null;
				}
				else {
					characters = reader.getText();
				}
				break;

			case XMLStreamConstants.START_ELEMENT:

				// If we're in skip mode from an unmapped element, we'll break here. The code will keep skipping until the
				// end of that element triggers an END_ELEMENT event.
				if(skipUnmappedMode){
					break;
				}

				if(!reader.hasText()) {
					characters = null;
				}

				// Read the normal localName
				localName = reader.getLocalName();
				// Fetch or make the persistent copy for identity comparison
				localXmlElementName = getXmlElementName(localName);

				// This flag will record whether we push an element on the stack for further processing or not.
				// It is used to determine whether we've hit an unmapped element or not, if we're validating data.
				boolean pushedElement = false;

				//LogContext.info("Start: "+localName+" / Local XML Identity: "+System.identityHashCode(localXmlElementName));

				// Grab the current stack data from the stack. On the first run this is the root element.
				stackData = outputStack.peek();

				// If our last event was an End Element, we have to trigger special handling before proceeding to determine
				// if we are continuing with a new instance of the same element, or switching to a new element.
				if(lastEvent == XMLStreamConstants.END_ELEMENT){
					//LogContext.info("END ELEMENT SPECIAL HANDLING - LOCAL NAME = "+localName);

					// Detect if this element is mapped to multiple things (xs:choice support)
					AbstractSet<CharArray> mappedElements = null;
					boolean mappedElement = false;

					// Detect Mapped Elements for use with xs:choice
					if(stackData._annotationStackType != AnnotationStackType.ROOT) {
						parentCacheData = caches.getCacheData(stackData._parent._type);
						//LogContext.info("Mapped Elements: Lookup Class = "+stackData._parent._type);
						mappedElements = parentCacheData._mappedElementsCache.get(localXmlElementName);
						mappedElement = (mappedElements != null && mappedElements.contains(localXmlElementName));
						//LogContext.info("Mapped Element: "+mappedElement+", Class Key: "+stackData._type+" Mapped Classes: "+((mappedElements)==null ? "None" : mappedElements.toString()));
					}

					// If the current element is unbounded and the current element does not match this list's element, then
					// we are done with this list and need to pop it off of the stack. In the case of xs:choice elements
					// we have to compare against the alternate element choices as well
					if(stackData._annotationStackType == AnnotationStackType.UNBOUNDED && ((!mappedElement && localXmlElementName != stackData._xmlElementName) || (mappedElement &&
							!mappedElements.contains(stackData._xmlElementName)))){
						outputStack.pop();

						//LogContext.info("<STACK POP> [DND] - Old Head: (List) "+stackData._xmlElementName);

						stackData = outputStack.peek();

						//LogContext.info("<STACK PEEK> [DND] - Current Head: "+stackData._xmlElementName);

						// If the local name matches that parent's element name, we need to pop again. This case
						// can happen when a unbounded element is the last element inside of another unbounded element.
						if(localXmlElementName == stackData._xmlElementName || (mappedElement && mappedElements.contains(stackData._xmlElementName))){
							outputStack.pop();

							//LogContext.info("<STACK POP> [DBE] - Old Head: "+stackData._xmlElementName);

							stackData = outputStack.peek();

							//LogContext.info("<STACK PEEK> [DBE] - Current Head: "+(stackData._list==null ? "" : "(List) ")+stackData._xmlElementName);
						}

						// Refresh Cache Data
						cacheData = caches.getCacheData(stackData._type);

						// Refresh mapped elements since we popped the stack (for xs:choice support)
						if(stackData._annotationStackType != AnnotationStackType.ROOT) {
							parentCacheData = caches.getCacheData(stackData._parent._type);
							//LogContext.info("Mapped Elements: Lookup Class = "+stackData._parent._type);
							mappedElements = parentCacheData._mappedElementsCache.get(localXmlElementName);
							mappedElement = (mappedElements != null && mappedElements.contains(localXmlElementName));
							//LogContext.info("Mapped Element: "+mappedElement+", Class Key: "+stackData._type+" Mapped Classes: "+((mappedElements)==null ? "None" : mappedElements.toString()));
						}
					}

					// If at this point, the element name matches the now-current stack data, then we're going to continue on with that element.
					// In the case of xs:choice elements, we have to check mapped elements for the other choices as well
					if(localXmlElementName == stackData._xmlElementName || (mappedElement && mappedElements.contains(stackData._xmlElementName))){
						final Class<?> elementClass = caches._elementClassCache.get(localXmlElementName);
						final Class<?> currentType = stackData._type;

						final AnnotationStackData elementStackData;

						// If the element class is an instance of a basic wrapper type, an enum, or a primitive, then we can't
						// record the value until after we read the characters event, so we'll push the class alone onto the stack,
						// and then commit later during the end element event.
						if(caches.isInstanceOfBasicType(elementClass)){
							elementStackData = new AnnotationStackData(AnnotationStackType.BASIC, stackData, elementClass, null, elementClass, null, null, null);
							outputStack.push(elementStackData);

							//LogContext.info("<STACK PUSH> [CTE-AnnotationStackType.BASIC]- New Head: "+elementStackData._type);

							lastEvent = event;

							// In this case, we're done here. These types of data can't have attributes to parse.
							break;
						}
						// For complex types, we can make the instance now and include it in the data that is pushed
						// onto the stack.
						else {
							// Start by getting the new instance of the complex type
							final Object newObject = reflectNewInstance(currentType, parentCacheData, localXmlElementName);

							// If we're validating, we're going to need to look up required fields (which should always be
							// cached already at this point (see code below the special handling block for more details).
							// This data is needed on the stack so that later when the element ends, we can check if we have
							// all of the required fields.
							AbstractSet<CharArray> requiredFieldsSet = null;
							Iterator<CharArray> propOrderIterator = null;

							if(_isValidating){
								requiredFieldsSet = caches._requiredCache.get(currentType);
								propOrderIterator = caches._propOrderCache.get(currentType).iterator();
							}

							// Bundle up the new stack data
							elementStackData = new AnnotationStackData(AnnotationStackType.BOUNDED, stackData, newObject, null, elementClass,
									localXmlElementName, requiredFieldsSet, propOrderIterator);

							// If our parent is a list, we add to the list now
							if(stackData._annotationStackType == AnnotationStackType.UNBOUNDED){
								addToList(stackData._list, newObject, null);
							}
							// If it's not, then we set it into it's field now
							else {
								setValue(cacheData, stackData._object, newObject, localXmlElementName);
							}

							// Then, we push the data onto the stack.
							outputStack.push(elementStackData);

							//LogContext.info("<STACK PUSH> - [CTE-COMPLEX] New Head: "+elementStackData._xmlElementName);

							// Lastly, we update state variable, but we DON'T break here because we'll need to parse attributes later.
							stackData = elementStackData;
							pushedElement = true;
							continuingSameElement = true;
						}
					}
					// If we didn't match, and we're a unbounded list - then we're done with the list and we pop it here before proceeding
					else if(stackData._annotationStackType == AnnotationStackType.UNBOUNDED){
						outputStack.pop();

						//LogContext.info("<STACK POP> [ND] - Old Head: (List) "+stackData._xmlElementName);

						stackData = outputStack.peek();

						//LogContext.info("<STACK PEEK> [ND] - Current Head: "+stackData._xmlElementName);
					}
				}

				// Capture state about the current object.
				Object currentObj = stackData._object;
				Class<?> currentObjClass = currentObj.getClass();
				cacheData = caches.getCacheData(currentObjClass);

				// Get the Cached Field
				final Field targetField = cacheData._elementFieldCache.get(localXmlElementName);
				final Class<?> elementClass = caches._elementClassCache.get(localXmlElementName);

				// Next we are determining if we've already cached which fields are XML Attributes
				AbstractMap<CharArray, Method> cachedAttributeFields = cacheData._attributeMethodsCache;
				AbstractSet<CharArray> requiredFieldsSet = caches._requiredCache.get(elementClass);

				// If we're continuing the same element, we can skip here because the element is already done and we only
				// need to parse attributes.
				if(!continuingSameElement) {
					if (targetField != null) {
						// This method will push the incoming element onto the stack, and return the corresponding stack data
						stackData = handleFieldStartElement(stackData, localXmlElementName, currentObj, targetField,
								outputStack, requiredFieldsSet);

						// Optimization: Classes (Primitive or Enums) won't have attributes to scrape
						if(stackData._annotationStackType == AnnotationStackType.BASIC){
							lastEvent = event;
							break;
						}

						pushedElement = true;
					}
					else if(outputStack.size() > 1) {
						// If we're validating, then we have to raise an exception
						if(_isValidating){
							throw new ValidationException("Unmapped Element");
						}
						// If we're not, we need to trigger the ignore flag.
						else {
							unmappedElement = localXmlElementName;
							skipUnmappedMode = true;
						}

						break;
					}
				}

				// We keep a reference to the old class before we probe again, because if it didn't change
				// we can skip some of the more expensive lookups.
				final Class<?> originalObjClass = currentObjClass;

				// Re-probe the current object data as it may have changed.
				currentObj = stackData._object;
				currentObjClass = currentObj.getClass();

				// If the data did change, we need to re-probe our attribute and required data.
				if(originalObjClass != currentObjClass){
					cacheData = caches.getCacheData(currentObjClass);
					cachedAttributeFields = cacheData._attributeMethodsCache;
				}

				// Parse the attributes
				for(int i = 0; i < reader.getAttributeCount(); i++){
					final CharArray attributeName = reader.getAttributeLocalName(i);
					final Method method = cachedAttributeFields.get(attributeName);
					parseAttribute(method, attributeName, cacheData._enumValueCache, reader, currentObj, stackData._processedSet);
				}

				// If we haven't pushed an element yet, then it's an unmapped one.
				if(!pushedElement && currentObjClass!=inputClass){
					// If we're validating, then we have to raise an exception
					if(_isValidating){
						throw new ValidationException("Unmapped Element");
					}
					// If we're not, we need to trigger the ignore flag.
					else {
						unmappedElement = localXmlElementName;
						skipUnmappedMode = true;
					}
				}

				break;

			case XMLStreamConstants.END_ELEMENT:
				localName = reader.getLocalName();

				if(skipUnmappedMode){
					if(localName.equals(unmappedElement)){
						skipUnmappedMode = false;
					}
					break;
				}

				//LogContext.info("End:   "+localName);

				stackData = outputStack.pop();

				//LogContext.info("<STACK POP> [EE] - Old Head: "+((stackData._xmlElementName == null) ? stackData._type : stackData._xmlElementName));

				// Optimization: If we're back down to the starting element, we can break here.
				if(stackData._annotationStackType == AnnotationStackType.ROOT)
					break;

				final AnnotationStackData parentStackData = stackData._parent;

				// For elements that come through with a list directly below them, we add the value to that list here.
				if(stackData._annotationStackType == AnnotationStackType.BASIC){
					parentCacheData = caches.getCacheData(parentStackData._type);

					if(parentStackData._annotationStackType == AnnotationStackType.UNBOUNDED){
						addToList(parentStackData._list, stackData._object, characters);
					}
					else {
						localXmlElementName = getXmlElementName(localName);
						setValue(parentCacheData, parentStackData._object, localXmlElementName, characters);
					}
				}
				else if(stackData._annotationStackType == AnnotationStackType.UNBOUNDED){
					if(lastEvent == XMLStreamConstants.END_ELEMENT){
						//LogContext.info("<STACK POP> [EEDP] - Old Head: "+((stackData._xmlElementName == null) ? stackData._type : stackData._xmlElementName));
						stackData = outputStack.pop();

						if(_isValidating && !stackData._processedSet.containsAll(stackData._requiredSet)){
							throw new ValidationException("Missing Required Elements!");
						}
					}
					else {
						//LogContext.info("<STACK PUSH> - [PUSH-BACK] New Head: (List) "+stackData._xmlElementName);
						outputStack.push(stackData);
					}
				}
				// Handle @XmlValue Elements
				else {
					final Method xmlValueMethod = cacheData._xmlValueMethod;

					if(xmlValueMethod != null && characters != null){
						try {
							xmlValueMethod.invoke(stackData._object, characters.toString().trim());
						}
						catch (final Exception e){
							throw new UnmarshalException("Error Setting @XmlValue - Field = "+
									xmlValueMethod.getName(), e);
						}
					}
				}

				if(_isValidating && stackData._annotationStackType != AnnotationStackType.BASIC && !stackData._processedSet.containsAll(stackData._requiredSet)){
					throw new ValidationException(String.format("Missing Required Elements: Has %s, Requires %s",
							stackData._processedSet, stackData._requiredSet));
				}

				lastEvent = event;
			}
		}

		return outputObject;
	}

	private AnnotationStackData handleFieldStartElement(final AnnotationStackData parentStackData, final CharArray xmlElementName, final Object currentObj, final Field field,
			final FastTable<AnnotationStackData> outputStack, final AbstractSet<CharArray> requiredFieldsSet) throws UnmarshalException, ValidationException{
		final AnnotationStackData elementStackData;
		final CacheData parentCacheData = caches.getCacheData(parentStackData._type);
		final Class<?> fieldType = field.getType();

		Iterator<CharArray> propOrderIterator = null;

		if(_isValidating){
			parentStackData._processedSet.add(xmlElementName);

			final Iterator<CharArray> parentPropOrderIterator = parentStackData._propOrderIterator;

			boolean propOrderMatch = false;

			while(parentPropOrderIterator.hasNext()){
				final CharArray propOrder = parentPropOrderIterator.next();

				// TODO: Try to guarantee identity
				if(propOrder == xmlElementName || propOrder.equals(xmlElementName)){
					propOrderMatch = true;
					break;
				}
			}

			if(!propOrderMatch){
				throw new ValidationException("Out of Order Element Detected: "+xmlElementName);
			}
		}

		try {
			if(fieldType.isAssignableFrom(List.class) && fieldType != Object.class){
				final Class<?> genericType = getGenericType(field);
				final AnnotationStackData listStackData;

				if(parentStackData._annotationStackType == AnnotationStackType.UNBOUNDED){
					listStackData = parentStackData;
					//LogContext.info("<STACK NOOP> - [KEEP CURRENT LIST]: (List) "+listStackData._xmlElementName);
				}
				else {
					final FastTable<Object> list = new FastTable<Object>();
					listStackData = new AnnotationStackData(AnnotationStackType.UNBOUNDED, parentStackData, null,
							list, genericType, xmlElementName, null, null);
					setList(listStackData);

					outputStack.push(listStackData);
					//LogContext.info("<STACK PUSH> - [START-LIST] New Head: (List) "+listStackData._xmlElementName);
				}

				if(caches.isInstanceOfBasicType(genericType)){
					elementStackData = new AnnotationStackData(AnnotationStackType.BASIC, listStackData, genericType, null, genericType, null, null, null);
					outputStack.push(elementStackData);
					//LogContext.info("<STACK PUSH> - [START-BASIC] New Head: "+elementStackData._type);
				}
				else {
					final Object genericInstance = reflectNewInstance(genericType, parentCacheData, xmlElementName);

					if(_isValidating){
						propOrderIterator = getXmlPropOrder(genericType);
					}

					elementStackData = new AnnotationStackData(AnnotationStackType.BOUNDED, listStackData,
							genericInstance, null, genericInstance.getClass(), xmlElementName, requiredFieldsSet, propOrderIterator);
					addToList(listStackData._list, genericInstance, null);
					outputStack.push(elementStackData);
					//LogContext.info("<STACK PUSH> - [START-COMPLEX] New Head: "+elementStackData._xmlElementName);
				}
			}
			else if(caches.isInstanceOfBasicType(fieldType)){
				elementStackData = new AnnotationStackData(AnnotationStackType.BASIC, parentStackData, fieldType, null, fieldType, null, null, null);
				outputStack.push(elementStackData);
				//LogContext.info("<STACK PUSH> - [START-AnnotationStackType.BASIC] New Head: "+elementStackData._type);
			}
			else {
				final Object newInstance = reflectNewInstance(fieldType, parentCacheData, xmlElementName);

				if(_isValidating){
					propOrderIterator = getXmlPropOrder(fieldType);
				}

				elementStackData = new AnnotationStackData(AnnotationStackType.BOUNDED, parentStackData,
						newInstance, null, newInstance.getClass(), xmlElementName, requiredFieldsSet, propOrderIterator);

				if(parentStackData._annotationStackType == AnnotationStackType.UNBOUNDED){
					addToList(parentStackData._list, newInstance, null);
				}
				else {
					setValue(parentCacheData, parentStackData._object, newInstance, xmlElementName);
				}

				outputStack.push(elementStackData);
				//LogContext.info("<STACK PUSH> - [START-COMPLEX] New Head: "+elementStackData._xmlElementName);
			}
		}
		catch (final Exception e){
			throw new UnmarshalException("Input XML Does Not Match The Provided Class!",e);
		}

		return elementStackData;
	}

	@SuppressWarnings({ "unchecked", "deprecation" })
	private void invokeMethod(final Method method, final Class<?> type, final Object object, final CharArray value, final Enum<?> enumValue) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, UnmarshalException, ParseException{
		InvocationClassType invocationClassType;

		if(enumValue == null){
			invocationClassType = InvocationClassType.valueOf(type);

			if(invocationClassType == null){
				invocationClassType = InvocationClassType.OBJECT;
			}
		}
		else {
			invocationClassType = InvocationClassType.ENUM;
		}

		switch(invocationClassType){

		case STRING:
			if(value == null) {
				method.invoke(object, ""); // JDK JAXB Unmarshalls Empty String as ""
			}
			else {
				method.invoke(object, value.toString());
			}
			break;

		case LONG:
		case PRIMITIVE_LONG:
			if(value == null) break;
			method.invoke(object, value.toLong());
			break;

		case XML_GREGORIAN_CALENDAR:
			if(value == null) break;

			final XMLGregorianCalendar calendar = _dataTypeFactory.newXMLGregorianCalendar(value.toString());
			method.invoke(object, calendar);
			break;

		case DURATION:
			if(value == null) break;

			final Duration duration = _dataTypeFactory.newDuration(value.toString());
			method.invoke(object, duration);
			break;

		case QNAME:
			if(value == null) break;

			final String valueString = value.toString();
			final CharArray namespaceClass;
			final String[] tokens = valueString.split(":");

			if(tokens.length == 2){
				namespaceClass = new CharArray(tokens[1]);
			}
			else {
				throw new UnmarshalException("Invalid QName Element Encountered: "+value.toString());
			}

			final Class<?> elementClass = caches._elementClassCache.get(namespaceClass);
			final String namespace = caches._classNameSpaceCache.get(elementClass);

			final QName qname = new QName(namespace, tokens[1], tokens[0]);
			method.invoke(object, qname);
			break;

		case DECIMAL:
			if(value == null) break;
			final String stringValue = value.toString().trim();
			method.invoke(object, new BigDecimal(stringValue));
			break;

		case INT:
		case PRIMITIVE_INT:
			if(value == null) {
				method.invoke(object, 0); // JDK JAXB Unmarshalls Empty Int as ""
			}
			else {
				method.invoke(object, value.toInt());
			}
			break;

		case INTEGER:
			if(value == null) break;
			method.invoke(object, new BigInteger(value.toString().trim()));
			break;

		case BOOLEAN:
		case PRIMITIVE_BOOLEAN:
			if(value == null) break;
			method.invoke(object, value.toBoolean());
			break;

		case DOUBLE:
		case PRIMITIVE_DOUBLE:
			if(value == null) break;
			method.invoke(object, value.toDouble());
			break;

		case BYTE:
		case PRIMITIVE_BYTE:
			if(value == null) {
				method.invoke(object, (byte)0);  // JDK JAXB Unmarshalls Empty Byte as ""
			}
			else {
				method.invoke(object, (byte)value.toInt());
			}
			break;

		case BYTE_ARRAY:
		case PRIMITIVE_BYTE_ARRAY:
			if(value == null) break;
			final String byteString = value.toString();
			@SuppressWarnings("rawtypes")
			final Class<? extends XmlAdapter> typeAdapter = caches._xmlJavaTypeAdapterCache.get(method);

			final byte[] byteArray;

			// Default byte[] type is Base64
			if(typeAdapter == null){
				byteArray = DatatypeConverter.parseBase64Binary(byteString);
			}
			// If a custom handler is specified, use it. Note: JAXB parses xs:hexBinary this way
			else {
				try {
					byteArray = (byte[])typeAdapter.newInstance().unmarshal(byteString);
				}
				catch (final Exception e) {
					throw new UnmarshalException("Error Excuting Type Adapter - "+method.getName(), e);
				}
			}

			method.invoke(object, byteArray);
			break;

		case FLOAT:
		case PRIMITIVE_FLOAT:
			if(value == null) break;
			method.invoke(object, value.toFloat());
			break;

		case SHORT:
		case PRIMITIVE_SHORT:
			if(value == null) break;
			method.invoke(object, (short)value.toInt());
			break;

		case ENUM:
			if(enumValue == null) break;
			method.invoke(object, enumValue);
			break;

		case OBJECT:
			if(value == null) break;
			final XmlSchemaTypeEnum xmlSchemaType = caches._xmlSchemaTypeCache.get(method);

			if(xmlSchemaType != null && xmlSchemaType == XmlSchemaTypeEnum.ANY_SIMPLE_TYPE){
				method.invoke(object, DatatypeConverter.parseAnySimpleType(value.toString().trim())); // TODO: Handle more than Strings
				break;
			}
			else {
				try {
					method.invoke(object, type.newInstance());
				}
				catch (final InstantiationException e) {
					throw new UnmarshalException("Error Excecuting Setter - UnMapped Type!", e);
				}
			}
			break;

		default:
			throw new UnmarshalException("Error Executing Method Invocation - Unhandled Type! Type = "+type);
		}
	}

	private void parseAttribute(final Method method, final CharArray attributeName, final AbstractMap<CharArray,Enum<?>> enumValueCache, final XMLStreamReader reader, final Object currentObj, final AbstractSet<CharArray> processedSet) throws UnmarshalException, ValidationException{

		if(_isValidating && method == null){
			throw new ValidationException("Unmapped Attribute Encountered");
		}
		else if(method == null){
			return;
		}

		try {
			final CharArray attributeValue = reader.getAttributeValue(null, attributeName);

			if(attributeValue != null){
				final Class<?> methodType = method.getParameterTypes()[0];

				if(methodType.isEnum()){
					final AbstractMap<CharArray,Enum<?>> classEnumValueCache = caches.getCacheData(methodType)._enumValueCache;
					final Enum<?> enumValue = classEnumValueCache.get(attributeValue);
					invokeMethod(method, methodType, currentObj, null, enumValue);
				}
				else {
					invokeMethod(method, methodType, currentObj, attributeValue, null);
				}

				if(_isValidating){
					processedSet.add(attributeName);
				}
			}
		}
		catch (final Exception e){
			throw new UnmarshalException("Error Getting JAXB Attribute Setter!", e);
		}
	}

	@SuppressWarnings("deprecation")
	private static <T> T reflectNewInstance(final Class<T> objClass) throws UnmarshalException{
		final T outputObject;

		try {
			outputObject = objClass.newInstance();
		}
		catch (final Exception e) {
			throw new UnmarshalException("Error Instantating the JAXB Class!");
		}

		return outputObject;
	}

	@SuppressWarnings("deprecation")
	private Object reflectNewInstance(final Class<?> instanceClass, final CacheData cacheData, final CharArray localXmlElementName) throws UnmarshalException {
		final Object instance;

		// In xs:choice the list will have the interface type so we need to ensure we have the implementation type
		try {
			final Class<?> targetClass;
			if(cacheData._mappedElementsCache.containsKey(localXmlElementName)){
				targetClass = caches._elementClassCache.get(localXmlElementName);
			}
			else {
				targetClass = instanceClass;
			}

			final Method targetFactoryMethod = caches._objectFactoryCache.get(targetClass);

			if(targetFactoryMethod == null) {
				instance = targetClass.newInstance();
			}
			else {
				instance = targetFactoryMethod.invoke(caches._classObjectFactoryCache.get(targetClass), (Object[])null);
			}
		}
		catch (final Exception e) {
			throw new UnmarshalException("Error Instantating the JAXB Class!", e);
		}

		return instance;
	}

	private void setList(final AnnotationStackData listStackData) throws UnmarshalException{
		final AnnotationStackData parentStackData = listStackData._parent;
		final Object parentObj = parentStackData._object;
		final Class<?> parentType = parentStackData._type;
		final CharArray xmlElementName = listStackData._xmlElementName;

		try {
			final CacheData parentCacheData = caches.getCacheData(parentType);
			final AbstractMap<CharArray,Field> elementFieldCache = parentCacheData._elementFieldCache;
			final Field field = elementFieldCache.get(xmlElementName);
			field.set(parentObj, listStackData._list);

			//LogContext.info("<SAVE-LIST> - [setList]: "+listStackData._xmlElementName);
		}
		catch (final Exception e) {
			throw new UnmarshalException("Error Getting JAXB List!",e);
		}
	}

	private void setValue(final CacheData cacheData, final Object currentObj, final Object element, final CharArray xmlElementName) throws UnmarshalException{
		try {
			final Method method = cacheData._elementMethodCache.get(xmlElementName);
			method.invoke(currentObj, element);
			//LogContext.info("<DIRECT SET VALUE> - [setValue]: "+element.getClass());
		}
		catch (final Exception e){
			throw new UnmarshalException("Error Getting JAXB Setter!", e);
		}
	}

	private void setValue(final CacheData cacheData, final Object currentObj, final CharArray localXmlElementName, final CharArray characters) throws UnmarshalException{
		try {
			final Method method = cacheData._elementMethodCache.get(localXmlElementName);
			invokeMethod(method, method.getParameterTypes()[0], currentObj, characters, null);
		}
		catch (final Exception e){
			throw new UnmarshalException("Error Getting JAXB Setter!", e);
		}
	}

	private void addToList(final FastTable<Object> listObj, Object element, final CharArray characters){
		// If a class came in, it's a sign that special handling is needed.
		if(element instanceof Class){
			final Class<?> elementClass = (Class<?>)element;

			// Special Handling for Enums: We have the value now so we need to convert it
			// to the enum value before adding it to the list.
			if(elementClass.isEnum()){
				final CacheData cacheData = caches.getCacheData(elementClass);
				element = cacheData._enumValueCache.get(characters);
			}
			else if (elementClass == Object.class){
				listObj.add(DatatypeConverter.parseAnySimpleType(characters.toString().trim())); // TODO: Handle more than Strings
				return;
			}
		}

		listObj.add(element);
		//LogContext.info("<ADD-LIST> - [addToList]: "+element.getClass());
	}

	@Override
	public void setObjectFactories(final Object... objectFactories) throws JAXBException {
		for(final Object objectFactory : objectFactories){
			final Class<?> objectFactoryClass = objectFactory.getClass();

			if(caches._registeredClassesCache.contains(objectFactoryClass)) {
				continue;
			}

			if(!objectFactoryClass.isAnnotationPresent(XmlRegistry.class) &&
					(objectFactoryClass.getSuperclass()!=null &&
					!objectFactoryClass.getSuperclass().isAnnotationPresent(XmlRegistry.class))){
				throw new JAXBException(
						String.format("Object Factory <%s> Is Not Annotated With @XmlRegistry",
								objectFactoryClass));
			}

			final XmlSchema xmlSchema = objectFactoryClass.getSuperclass().getPackage().getAnnotation(XmlSchema.class);

			if(xmlSchema == null){
				throw new JAXBException(
						String.format("Failed to Detect Schema Namespace for Object Factory <%s>",
								objectFactoryClass));
			}

			caches.scanObjectFactory(objectFactory, true);
			caches._registeredClassesCache.add(objectFactoryClass);
		}
	}

	@Override
	public void setValidating(final boolean isValidating){
		_isValidating = isValidating;
	}

	private enum AnnotationStackType {
		BASIC, BOUNDED, ROOT, UNBOUNDED
	}

	private class AnnotationStackData {

		final AnnotationStackType _annotationStackType;
		final FastTable<Object> _list;
		final Object _object;
		final AnnotationStackData _parent;
		final Class<?> _type;
		final CharArray _xmlElementName;
		final Iterator<CharArray> _propOrderIterator;

		AbstractSet<CharArray> _processedSet;
		AbstractSet<CharArray> _requiredSet;

		public AnnotationStackData(final AnnotationStackType annotationStackType,
				final AnnotationStackData parent, final Object object, final FastTable<Object> list,
				final Class<?> type, final CharArray xmlElementName, final AbstractSet<CharArray> requiredSet,
				final Iterator<CharArray> propOrderIterator){
			_annotationStackType = annotationStackType;
			_object = object;
			_parent = parent;
			_type = type;
			_xmlElementName = xmlElementName;
			_list = list;
			_requiredSet = requiredSet;
			_propOrderIterator = propOrderIterator;

			if(requiredSet == null){
				_processedSet = null;
			}
			else {
				_processedSet = new FastSet<CharArray>(Order.lexical()).linked();
			}
		}

	}
}