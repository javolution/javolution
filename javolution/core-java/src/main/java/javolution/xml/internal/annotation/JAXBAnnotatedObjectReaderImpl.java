/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 *
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.xml.internal.annotation;

import static javolution.xml.internal.annotation.AnnotationStackType.BASIC;
import static javolution.xml.internal.annotation.AnnotationStackType.BOUNDED;
import static javolution.xml.internal.annotation.AnnotationStackType.ROOT;
import static javolution.xml.internal.annotation.AnnotationStackType.UNBOUNDED;

import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.ValidationException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.xml.sax.InputSource;

import javolution.osgi.internal.OSGiServices;
import javolution.text.CharArray;
import javolution.util.FastMap;
import javolution.util.FastSet;
import javolution.util.FastTable;
import javolution.util.function.Equalities;
import javolution.xml.annotation.JAXBAnnotatedObjectReader;
import javolution.xml.stream.XMLInputFactory;
import javolution.xml.stream.XMLStreamConstants;
import javolution.xml.stream.XMLStreamException;
import javolution.xml.stream.XMLStreamReader;

/**
 * Class to provide basic support for deserializing JAXB Annotated XML Objects
 * 
 * This class is implemented as a generic Javolution StAX handler that uses 
 * reflection to parse the annotation data.
 * 
 * This initial version is aimed at schema objects that are generated using XJC
 * using the default settings, and it should be sufficient to support those. 
 * It does not support every JAXB annotation yet.
 * 
 * Note: Logging is left commented out, as it's too slow to leave on in a
 * release build - even at a non-visible level such as debug. To enable,
 * find/replace //LogContext -> LogContext
 * 
 * @author  <a href="mailto:starlightknight@slkdev.net">Aaron Knight</a>
 * @version 6.2, July 30th, 2015
 * 
 */
public class JAXBAnnotatedObjectReaderImpl extends AbstractJAXBAnnotationReflectionSupport implements JAXBAnnotatedObjectReader {

	private boolean _isValidating;
	private final Class<?> _inputClass;
	private final DatatypeFactory _dataTypeFactory;
	private final XMLInputFactory _XMLFactory;
	private final FastMap<Class<?>,FastMap<CharArray,Field>> _attributeFieldsCache;
	private final FastMap<CharArray,Method> _directSetValueCache;
	private final FastMap<CharArray,Class<?>> _elementClassCache;
	private final FastMap<Class<?>,FastMap<CharArray,Field>> _classElementFieldCache;
	private final FastMap<CharArray,Enum<?>> _enumValueCache;
	private final FastMap<CharArray,FastSet<CharArray>> _mappedElementsCache;
	private final FastMap<Field,Method> _methodCache;

	public <T> JAXBAnnotatedObjectReaderImpl(final Class<T> inputClass) throws JAXBException {
		super();
		if(!inputClass.isAnnotationPresent(XmlRootElement.class))
			throw new UnmarshalException("Input Class Must Be A JAXB Annotated Root Element!");

		_inputClass = inputClass;
		_XMLFactory = OSGiServices.getXMLInputFactory();

		// Identity Equality is Safely Used Wherever Possible. See usage of these for details
		_attributeFieldsCache = new FastMap<Class<?>,FastMap<CharArray,Field>>(Equalities.IDENTITY);
		_elementClassCache = new FastMap<CharArray,Class<?>>(Equalities.CHAR_ARRAY_FAST);
		_classElementFieldCache = new FastMap<Class<?>,FastMap<CharArray,Field>>(Equalities.IDENTITY);
		_directSetValueCache = new FastMap<CharArray, Method>(Equalities.CHAR_ARRAY_FAST);
		_enumValueCache = new FastMap<CharArray,Enum<?>>(Equalities.CHAR_ARRAY_FAST);
		_mappedElementsCache = new FastMap<CharArray,FastSet<CharArray>>(Equalities.CHAR_ARRAY_FAST);
		_methodCache = new FastMap<Field, Method>(Equalities.IDENTITY);
		
		// This flag turns on/off annotation validation. There is no support yet for full schema validation
		_isValidating = false;

		// The Data Type factory is used only for interpreting XMLGregorianCalendars, until a better/faster solution is made.
		try {
			_dataTypeFactory = DatatypeFactory.newInstance();
		}
		catch (final DatatypeConfigurationException e) {
			throw new JAXBException("Unable to Instantiate DataTypeFactory!", e);
		}
	}

	@Override
	public <T> T read(final InputSource inputSource) throws JAXBException {
		if(inputSource == null){
			throw new JAXBException("Input Source Cannot Be Null!");
		}
		
		T object = null;
		
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
		XMLStreamReader reader = null;;
		T object;

		try {
			reader = (XMLStreamReader) _XMLFactory.createXMLStreamReader(inputStream, encoding);
			object = (T) readObject(_inputClass, reader);
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
			}
		}

		return object;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T read(final Reader reader) throws JAXBException {
		XMLStreamReader xmlReader = null;;
		T object;

		try {
			xmlReader = (XMLStreamReader) _XMLFactory.createXMLStreamReader(reader);
			object = (T) readObject(_inputClass, xmlReader);
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
			}
		}

		return object;
	}
	
	@Override
	public <T> T read(Source source) throws JAXBException {
		// For Compatibility Reasons for those who use StreamSources but declare w/ interface type
		if(source instanceof StreamSource){
			return read((StreamSource)source);
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

	private <T> T readObject(final Class<T> inputClass, final XMLStreamReader reader) throws JAXBException, SecurityException, XMLStreamException {
		T outputObject = null;

		// Start by instantiating our final output object
		outputObject = reflectNewInstance(inputClass);

		// The processing in this implementation is stack based. We will make use of FastTable's implementation of the Deque interface 
		final FastTable<AnnotationStackData> outputStack = new FastTable<AnnotationStackData>(Equalities.IDENTITY);

		// We'll push the output object onto the stack as an initial entry. All stack entries get wrapped in an AnnotationStackData class.
		// The fields in this class are package-private to provide as cheap of access as possible since they are used frequently.
		AnnotationStackData stackData;
		if(_isValidating){
			stackData = new AnnotationStackData(ROOT, null, outputObject, null, _inputClass, null, 
					new FastSet<CharArray>(Equalities.CHAR_ARRAY_FAST), getXmlPropOrder(_inputClass));
		}
		else {
			stackData = new AnnotationStackData(ROOT, null, outputObject, null, _inputClass, null, null, null);
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

		// Main Processing Loop
		while(reader.hasNext()){
			// This version of local name is the one read from the StAX Reader. It is a viewport over a large CharArray that
			// Javolution moves as we process the XML
			CharArray localName = null;

			// This version is a persisted cache of an individual element name. It is a viewport copy of the localName. The
			// reason we create and store this is so that when we push the element name as part of the stack data, we can
			// use identity comparison on anything in the stack data, cutting out a lot of repetitive CharSequence comparison
			CharArray localXmlElementName = null;

			// This flag will let us know later whether we are continuing with a new instance of the same element, or switching.
			boolean continuingSameElement = false;

			// Pull the StAX Event
			final int event = reader.next();

			event : switch(event){

			case XMLStreamConstants.CHARACTERS:
				// Small Optimization: Only grab the character data if we've just stared an element, otherwise we don't care.
				if(lastEvent == XMLStreamConstants.START_ELEMENT){
					characters = reader.getText();
				}
				break;

			case XMLStreamConstants.START_ELEMENT:

				// If we're in skip mode from an unmapped element, we'll break here. The code will keep skipping until the 
				// end of that element triggers an END_ELEMENT event.
				if(skipUnmappedMode){
					break event;
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
					boolean mappedElement = _mappedElementsCache.containsKey(localXmlElementName);
					FastSet<CharArray> mappedElements = _mappedElementsCache.get(localXmlElementName);
					
					// If the current element is unbounded and the current element does not match this list's element, then
					// we are done with this list and need to pop it off of the stack. In the case of xs:choice elements
					// we have to compare against the alternate element choices as well
					if(stackData._annotationStackType == UNBOUNDED && ((!mappedElement && localXmlElementName != stackData._xmlElementName) || (mappedElement &&
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
					}

					// If at this point, the element name matches the now-current stack data, then we're going to continue on with that element.
					// In the case of xs:choice elements, we have to check mapped elements for the other choices as well
					if(localXmlElementName == stackData._xmlElementName || (mappedElement && mappedElements.contains(stackData._xmlElementName))){
						final Class<?> elementClass = _elementClassCache.get(localXmlElementName);
						final Class<?> currentType = stackData._type;

						final AnnotationStackData elementStackData;

						// If the element class is an instance of a basic wrapper type, an enum, or a primitive, then we can't
						// record the value until after we read the characters event, so we'll push the class alone onto the stack,
						// and then commit later during the end element event.
						if(isInstanceOfBasicType(elementClass)){
							elementStackData = new AnnotationStackData(BASIC, stackData, elementClass, null, elementClass, null, null, null);	
							outputStack.push(elementStackData);

							//LogContext.info("<STACK PUSH> [CTE-BASIC]- New Head: "+elementStackData._type);

							pushedElement = true;
							lastEvent = event;

							// In this case, we're done here. These types of data can't have attributes to parse.
							break event;
						}
						// For complex types, we can make the instance now and include it in the data that is pushed
						// onto the stack.
						else {
							// Start by getting the new instance of the complex type
							final Object newObject;
							
							// In xs:choice the list will have the interface type so we need to ensure we have the implementation type
							if(_mappedElementsCache.containsKey(localXmlElementName)){
								newObject = reflectNewInstance(_elementClassCache.get(localXmlElementName));
							}
							else {
								newObject = reflectNewInstance(currentType);
							}

							// If we're validating, we're going to need to look up required fields (which should always be
							// cached already at this point (see code below the special handling block for more details).
							// This data is needed on the stack so that later when the element ends, we can check if we have
							// all of the required fields.
							FastSet<CharArray> requiredFieldsSet = null;
							Iterator<CharArray> propOrderIterator = null;

							if(_isValidating){
								requiredFieldsSet = _requiredCache.get(currentType);
								propOrderIterator = _propOrderCache.get(currentType).iterator();
							}

							// Bundle up the new stack data
							elementStackData = new AnnotationStackData(BOUNDED, stackData, newObject, null, elementClass, 
									localXmlElementName, requiredFieldsSet, propOrderIterator);

							// If our parent is a list, we add to the list now
							if(stackData._annotationStackType == UNBOUNDED){
								addToList(stackData._list, newObject, null);
							}
							// If it's not, then we set it into it's field now
							else {
								setValue(stackData._object, newObject, localXmlElementName);
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
					else if(stackData._annotationStackType == UNBOUNDED){
						outputStack.pop();

						//LogContext.info("<STACK POP> [ND] - Old Head: (List) "+stackData._xmlElementName);

						stackData = outputStack.peek();

						//LogContext.info("<STACK PEEK> [ND] - Current Head: "+stackData._xmlElementName);
					}
				}

				// Capture state about the current object.
				Object currentObj = stackData._object;
				Class<?> currentObjClass = currentObj.getClass();

				// XmlAccessType is required to know how to treat fields that do not have an explicit
				// JAXB annotation attached to them. The most common type is Field, which XJC generated objects use.
				// Field is currently the only implemented type, but you can explicitly use annotations however you want.
				final XmlAccessType xmlAccessType = getXmlAccessType(currentObjClass);

				// We're going to start probing cache data so that we can go down an optimized code path, and
				// avoid eating the overhead on reflection for any data that we have already obtained.
				// If this element has already been hit before, we'll have it's Field cached.
				// By using this, we can avoid a lot of reflection overhead
				final FastMap<CharArray,Field> elementFieldCache = getClassElementFieldCache(currentObjClass);

				Field cachedField = null;

				//LogContext.info("Field Cache - Class: "+currentObjClass.getName()+" Map: "+System.identityHashCode(elementFieldCache)+" Object: "+System.identityHashCode(localXmlElementName)+" Continung: "+continuingSameElement);

				// Next we are determining if we've already cached which fields are XML Attributes				
				FastMap<CharArray, Field> cachedAttributeFields = _attributeFieldsCache.get(currentObjClass);
				FastSet<CharArray> requiredFieldsSet = null;
				FastMap<CharArray, Field> attributeFieldsMap = null;
				FastSet<Field> fields = null;

				// If we're continuing the same element, we can skip here because the element is already done and we only
				// need to parse attributes.
				if(continuingSameElement) {
					requiredFieldsSet = _requiredCache.get(currentObjClass);
				}
				else {
					cachedField = elementFieldCache.get(localXmlElementName);

					// If we don't have a cached field, we'll gather data here.
					if(cachedField == null){
						
						// If we're validating, we need to structure to store required field data for use later.
						if(_isValidating){
							requiredFieldsSet = new FastSet<CharArray>(Equalities.CHAR_ARRAY_FAST);
						}

						// For uncached data, we're going to have to reflect the fields list and traverse it.
						fields = getDeclaredFields(currentObjClass);

						//LogContext.info("Field Cache Miss - Local Name: "+localName.toString());
						
						field : for(final Field field : fields){
							
							// Optimization: Use access type and other annotations to determine skip.
							if(isElementSkippableBasedOnFieldAnnotations(field, xmlAccessType))
								continue field;

							// Get a copy of the Xml Element Name
							CharArray xmlElementName = getXmlElementName(elementFieldCache, localXmlElementName, field);
							
							if(xmlElementName == null){
								xmlElementName = new CharArray(field.getName());
							}

							// If we're validating, capture required data.
							if(_isValidating){
								XmlElement xmlElement = field.getAnnotation(XmlElement.class);

								if(xmlElement != null && xmlElement.required()){
									requiredFieldsSet.add(xmlElementName);
								}
							}

							// Before we can cache the field, we need to make sure we got the right one. By validating this here,
							// we ensure we have the right element. In addition, since we're comparing against the persistent copy,
							// we can use identity comparison on it later when we hit the special end processing above.
							if(xmlElementName.equals(localXmlElementName)) {
								// We have to make sure our copy of the field is accessible. For lists, there are no setters in JAXB.
								field.setAccessible(true);
								
								// Cache the element, versus the persistent copy
								elementFieldCache.put(localXmlElementName, field);

								// Optimization: to avoid wasting type just gathering the cache, we also process
								// the element here since we have all the data.
								stackData = handleFieldStartElement(stackData, localXmlElementName, currentObj, field, outputStack, requiredFieldsSet);
								
								// Mark that we pushed an element
								pushedElement = true;
							}
							// Even if this isn't the field we're looking for, we might as well cache with while we're here.
							else {
								// Cache the element field for later use.
								field.setAccessible(true);
								elementFieldCache.put(xmlElementName, field);
								
								// This might look funny, but the key and value are intentionally the same. This will become
								// the persistent reference. It is equal to localName with a charsequence comparison, but we
								// want the persistent version as a key so we can use identity comparison on lookup wherever 
								// possible.
								_xmlElementNameCache.put(xmlElementName, xmlElementName);
							}
						}

						// If we're validating, we need to cache the required fields data we gathered.
						if(_isValidating){
							_requiredCache.put(currentObjClass, requiredFieldsSet);
						}

						// Optimization: Classes (Primitive or Enums) won't have attributes to scrape)
						if(stackData._annotationStackType == BASIC){
							lastEvent = event;
							break event;
						}
					}
					// Optimization: Directly process the field if we had it cached - no need to reflect it
					else {
						//LogContext.info("Field Cache Hit - Local Name: "+localName.toString());
						
						// This method will push the incoming element onto the stack, and return the corresponding stack data
						stackData = handleFieldStartElement(stackData, localXmlElementName, currentObj, cachedField,
								outputStack, requiredFieldsSet);					

						// Optimization: Classes (Primitive or Enums) won't have attributes to scrape
						if(stackData._annotationStackType == BASIC){
							lastEvent = event;
							break event;
						}

						pushedElement = true;

						// Re-probe object data as it has changed.
						currentObj = stackData._object;
						currentObjClass = currentObj.getClass();

						cachedAttributeFields = _attributeFieldsCache.get(currentObjClass);
						
						// If we haven't cached attribute's, we'll prepare a data structure to gather data to cache with.
						if(cachedAttributeFields == null){
							attributeFieldsMap = new FastMap<CharArray,Field>(Equalities.CHAR_ARRAY_FAST);
							
							if(_isValidating){
								requiredFieldsSet = _requiredCache.get(currentObjClass);
							
								if(requiredFieldsSet == null){
									requiredFieldsSet = new FastSet<CharArray>();
									_requiredCache.put(currentObjClass, requiredFieldsSet);
								}
							
								stackData._requiredSet = requiredFieldsSet;
								stackData._processedSet = new FastSet<CharArray>();
							}
						}
						// Optimization: If we already had the attribute fields cached, we can process them here.
						else {
							int attributeCount = reader.getAttributeCount();
							
							if(_isValidating){
								stackData._requiredSet = _requiredCache.get(currentObjClass);
								stackData._processedSet = new FastSet<CharArray>();
							}
							
							for(int i = 0; i < attributeCount; i++){
								final Field field = cachedAttributeFields.get(reader.getAttributeLocalName(i));
								parseAttribute(reader, field, currentObj, stackData._processedSet);
							}
							
							// Optimization Since we've done fields and attributes now, we're done.
							break event;
						}
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
					cachedAttributeFields = _attributeFieldsCache.get(currentObjClass);
					requiredFieldsSet = _requiredCache.get(currentObjClass);

					// If we don't have any required data yet, we need a data structure to start gathering it.
					if(_isValidating && requiredFieldsSet == null){
						requiredFieldsSet = new FastSet<CharArray>(Equalities.CHAR_ARRAY_FAST);
					}
				}

				// We don't have attribute data cached so we need to scan for it.
				if(cachedAttributeFields == null){
					attributeFieldsMap = new FastMap<CharArray,Field>(Equalities.CHAR_ARRAY_FAST);

					// Only fetch declared fields if we don't already ahve them or the class changed
					if(fields == null || originalObjClass != currentObjClass){
						fields = getDeclaredFields(currentObjClass);
					}

					for(final Field field : fields){
						if(field.isAnnotationPresent(XmlAttribute.class)){
							// Cache Attribute Data
							CharArray xmlAttributeName = getXmlAttributeName(field);
							attributeFieldsMap.put(xmlAttributeName, field);

							// If we're validating, also add required info
							if(_isValidating){
								XmlAttribute xmlAttribute = field.getAnnotation(XmlAttribute.class);

								if(xmlAttribute.required()){
									requiredFieldsSet.add(xmlAttributeName);
								}
							}
						}
						else if(_isValidating){
							XmlElement xmlElement = field.getAnnotation(XmlElement.class);

							if(xmlElement != null && xmlElement.required()){
								CharArray xmlElementName = getXmlElementName(elementFieldCache, localXmlElementName, field);
								requiredFieldsSet.add(xmlElementName);
							}
						}
					}

					// Cache the full set of attributes for later use
					_attributeFieldsCache.put(currentObjClass, attributeFieldsMap);
					cachedAttributeFields = attributeFieldsMap;
				}

				// Actually parse the attributes
				for(int i = 0; i < reader.getAttributeCount(); i++){
					Field field = cachedAttributeFields.get(reader.getAttributeLocalName(i));
					parseAttribute(reader, field, currentObj, stackData._processedSet);
				}

				// If we haven't pushed an element yet, then it's an unmapped one.
				if(!pushedElement && currentObjClass!=_inputClass){
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
					break event;
				}

				//LogContext.info("End:   "+localName);

				stackData = outputStack.pop();

				//LogContext.info("<STACK POP> [EE] - Old Head: "+((stackData._xmlElementName == null) ? stackData._type : stackData._xmlElementName));

				// Optimization: If we're back down to the starting element, we can break here.
				if(stackData._annotationStackType == ROOT)
					break event;

				final AnnotationStackData parentStackData = stackData._parent;

				// For elements that come through with a list directly below them, we add the value to that list here.
				if(stackData._annotationStackType == BASIC){
					if(parentStackData._annotationStackType == UNBOUNDED){
						addToList(parentStackData._list, stackData._object, characters);
					}
					else {
						localXmlElementName = getXmlElementName(localName);
						setValue(parentStackData._object, localXmlElementName, characters);
					}
				}
				else if(stackData._annotationStackType == UNBOUNDED){
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

				if(_isValidating && stackData._annotationStackType != BASIC && !stackData._processedSet.containsAll(stackData._requiredSet)){
					throw new ValidationException(String.format("Missing Required Elements: Has %s, Requires %s", 
							stackData._processedSet, stackData._requiredSet));
				}

				lastEvent = event;
				break event;
			}
		}

		return outputObject;
	}

	private Method getSetterMethod(final Object currentObj, final Field field, final Class<?> fieldType, final CharArray xmlAttributeName) throws NoSuchMethodException, SecurityException{
		Method method = _methodCache.get(field);

		if(method == null){
			final String setterName = getSetterName(xmlAttributeName);
			method = currentObj.getClass().getMethod(setterName, fieldType);
			_methodCache.put(field, method);
		}

		return method;
	}

	private static String getSetterName(final CharArray attribute){
		final char[] array = new char[attribute.length()];
		attribute.getChars(0, attribute.length(), array, 0);
		array[0] = Character.toUpperCase(array[0]);

		final StringBuilder setterBuilder = new StringBuilder(3+array.length);
		setterBuilder.append("set");
		setterBuilder.append(array);

		return setterBuilder.toString();
	}

	private CharArray getXmlElementName(final FastMap<CharArray,Field> elementFieldCache, final CharArray localXmlElementName, final Field field){
		CharArray xmlElementName = null;

		// First We Probe for @XmlElement
		XmlElement xmlElement = field.getAnnotation(XmlElement.class);
		
		// If we don't find it, try @XmlElements (xs:choice support)
		if(xmlElement == null){
			XmlElements xmlElements = field.getAnnotation(XmlElements.class);
			
			if(xmlElements != null){
				XmlElement[] elements = xmlElements.value();
				
				FastSet<CharArray> mappedElementsSet = new FastSet<CharArray>(Equalities.CHAR_ARRAY_FAST);
				
				for(final XmlElement element : elements){
					final CharArray nameKey = new CharArray(element.name());
					CharArray name = _xmlElementNameCache.get(nameKey);
					
					// We need to pre-cache everything about the elements
					if(name == null){
						name = _xmlElementNameCache.put(nameKey, nameKey);
						name = nameKey;
					}
					
					_elementClassCache.putIfAbsent(name, element.type());
					elementFieldCache.putIfAbsent(name, field);
					
					//LogContext.info("<XML-ELEMENTS SCAN> Field: "+field.getName()+" | Element Name: "+name+" | Element Type: "+element.type());
					
					if(name == localXmlElementName){
						xmlElementName = name;
						//LogContext.info("<XML-ELEMENTS SCAN> Match Found, Element Name: "+name);
					}
					
					// Mapped elements will be used later to switch detection
					mappedElementsSet.add(name);
					_mappedElementsCache.put(name, mappedElementsSet);
				}
			}
		}
		else {
			xmlElementName = new CharArray(xmlElement.name());
			_xmlElementNameCache.putIfAbsent(xmlElementName, xmlElementName);
		}
		
		return xmlElementName;
	}

	private AnnotationStackData handleFieldStartElement(final AnnotationStackData parentStackData, final CharArray xmlElementName, final Object currentObj, final Field field,
			final Deque<AnnotationStackData> outputStack, final FastSet<CharArray> requiredFieldsSet) throws UnmarshalException, ValidationException{
		final AnnotationStackData elementStackData;

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
			if(fieldType.isAssignableFrom(List.class)){
				final Class<?> genericType = getGenericType(field);

				final AnnotationStackData listStackData;

				if(parentStackData._annotationStackType == UNBOUNDED){
					listStackData = parentStackData;
					//LogContext.info("<STACK NOOP> - [KEEP CURRENT LIST]: (List) "+listStackData._xmlElementName);
				}
				else {
					final FastTable<Object> list = new FastTable<Object>(Equalities.STANDARD);
					listStackData = new AnnotationStackData(UNBOUNDED, parentStackData, null,
							list, genericType, xmlElementName, null, null);
					setList(listStackData);

					outputStack.push(listStackData);
					//LogContext.info("<STACK PUSH> - [START-LIST] New Head: (List) "+listStackData._xmlElementName);
				}

				if(isInstanceOfBasicType(genericType)){
					elementStackData = new AnnotationStackData(BASIC, listStackData, genericType, null, genericType, null, null, null);
					outputStack.push(elementStackData);
					//LogContext.info("<STACK PUSH> - [START-BASIC] New Head: "+elementStackData._type);
				}
				else {
					final Object genericInstance; 
					
					// In xs:choice the list will have the interface type so we need to ensure we have the implementation type
					if(_mappedElementsCache.containsKey(xmlElementName)){
						genericInstance = reflectNewInstance(_elementClassCache.get(xmlElementName));
					}
					else {
						genericInstance = genericType.newInstance();
					}
					
					if(_isValidating){
						propOrderIterator = getXmlPropOrder(genericType);
					}
					
					elementStackData = new AnnotationStackData(BOUNDED, listStackData,
							genericInstance, null, genericType, xmlElementName, requiredFieldsSet, propOrderIterator);
					addToList(listStackData._list, genericInstance, null);
					outputStack.push(elementStackData);
					//LogContext.info("<STACK PUSH> - [START-COMPLEX] New Head: "+elementStackData._xmlElementName);
				}

				_elementClassCache.put(xmlElementName, genericType);
			}
			else if(isInstanceOfBasicType(fieldType)){
				elementStackData = new AnnotationStackData(BASIC, parentStackData, fieldType, null, fieldType, null, null, null);
				outputStack.push(elementStackData);
				_elementClassCache.put(xmlElementName, fieldType);
				//LogContext.info("<STACK PUSH> - [START-BASIC] New Head: "+elementStackData._type);
			}
			else {
				final Object newInstance; 
					
				// In xs:choice the list will have the interface type so we need to ensure we have the implementation type
				if(_mappedElementsCache.containsKey(xmlElementName)){
					newInstance = reflectNewInstance(_elementClassCache.get(xmlElementName));
				}
				else {
					newInstance = fieldType.newInstance();
				}
				
				if(_isValidating){
					propOrderIterator = getXmlPropOrder(fieldType);
				}
				
				elementStackData = new AnnotationStackData(BOUNDED, parentStackData,
						newInstance, null, fieldType, xmlElementName, requiredFieldsSet, propOrderIterator);

				if(parentStackData._annotationStackType == UNBOUNDED){
					addToList(parentStackData._list, newInstance, null);
				}
				else {
					setValue(parentStackData._object, newInstance, xmlElementName);
				}

				outputStack.push(elementStackData);
				//LogContext.info("<STACK PUSH> - [START-COMPLEX] New Head: "+elementStackData._xmlElementName);
				_elementClassCache.put(xmlElementName, fieldType);
			}
		}
		catch (final Exception e){
			throw new UnmarshalException("Input XML Does Not Match The Provided Class!",e);
		}

		return elementStackData;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void invokeMethod(final Method method, final Class<?> type, final Object object, final CharArray value) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, UnmarshalException, ParseException{
		if (type==String.class){
			method.invoke(object, value.toString());
		}
		else if (type==Long.class){
			method.invoke(object, value.toLong());
		}
		else if (type == XMLGregorianCalendar.class){
			final XMLGregorianCalendar calendar = _dataTypeFactory.newXMLGregorianCalendar(value.toString());
			method.invoke(object, calendar);
		}
		else if (type==Integer.class){
			method.invoke(object, value.toInt());
		}
		else if (type==Boolean.class){
			method.invoke(object, value.toBoolean());
		}
		else if (type==Double.class){
			method.invoke(object, value.toDouble());
		}
		else if (type==Byte.class){
			method.invoke(object, (byte)value.toInt());
		}
		else if (type==Float.class){
			method.invoke(object, value.toFloat());
		}
		else if (type==Short.class){
			method.invoke(object, (short)value.toInt());
		}
		else if(type.isEnum()){
			Enum<?> enumValue = _enumValueCache.get(value);

			if(enumValue==null){
				enumValue = Enum.valueOf((Class<Enum>)type, value.toString());
				_enumValueCache.put(value, enumValue);
			}

			method.invoke(object, enumValue);
		}
		else if(type.isPrimitive()){
			if (type==long.class){
				method.invoke(object, value.toLong());
			}			
			else if (type==int.class){
				method.invoke(object, value.toInt());
			}
			else if (type==boolean.class){
				method.invoke(object, value.toBoolean());
			}
			else if (type==double.class){
				method.invoke(object, value.toDouble());
			}
			else if (type==byte.class){
				method.invoke(object, (byte)value.toInt());
			}
			else if (type==float.class){
				method.invoke(object, value.toFloat());
			}
			else if (type==short.class){
				method.invoke(object, (short)value.toInt());
			}
		}
		else {
			try {
				method.invoke(object, type.newInstance());
			}
			catch (final InstantiationException e) {
				throw new UnmarshalException("Error Excecuting Setter - UnMapped Type!", e);
			}
		}
	}

	private static boolean isElementSkippableBasedOnFieldAnnotations(final Field field, final XmlAccessType type){
		if(type == XmlAccessType.FIELD){
			if(field.isAnnotationPresent(XmlTransient.class)){
				return true;
			}
		}
		else if(!field.isAnnotationPresent(XmlElement.class)){
			return true;
		}

		return false;
	}

	private void parseAttribute(final XMLStreamReader reader, final Field field, final Object currentObj, final FastSet<CharArray> processedSet) throws UnmarshalException, ValidationException{

		if(_isValidating && field == null){
			throw new ValidationException("Unmapped Attribute Encountered");
		}
		else if(field == null){
			return;
		}

		CharArray xmlAttributeName = getXmlAttributeName(field);

		try {
			final CharArray attributeValue = reader.getAttributeValue(null, xmlAttributeName);

			if(attributeValue != null){
				final Class<?> fieldType = field.getType();
				final Method method = getSetterMethod(currentObj, field, fieldType, xmlAttributeName);
				invokeMethod(method, fieldType, currentObj, attributeValue);

				if(_isValidating){
					processedSet.add(xmlAttributeName);
				}
			}
		}
		catch (final Exception e){
			throw new UnmarshalException("Error Getting JAXB Attribute Setter!", e);
		}
	}

	private static <T> T reflectNewInstance(final Class<T> objClass) throws UnmarshalException{
		T outputObject = null;

		try {
			outputObject = objClass.newInstance();
		}
		catch (final Exception e) {
			throw new UnmarshalException("Error Instantating the JAXB Class!");
		}

		return outputObject;
	}

	private void setList(final AnnotationStackData listStackData) throws UnmarshalException{
		final AnnotationStackData parentStackData = listStackData._parent;
		final Object parentObj = parentStackData._object;
		final Class<?> parentType = parentStackData._type;
		final CharArray xmlElementName = listStackData._xmlElementName;

		try {
			final Map<CharArray,Field> elementFieldCache = getClassElementFieldCache(parentType);
			Field field = elementFieldCache.get(xmlElementName);

			if(field == null){
				field = parentType.getDeclaredField(xmlElementName.toString());
				field.setAccessible(true);
				elementFieldCache.put(xmlElementName, field);
			}

			field.set(parentObj, listStackData._list);

			//LogContext.info("<SAVE-LIST> - [setList]: "+listStackData._xmlElementName);
		}
		catch (final Exception e) {
			throw new UnmarshalException("Error Getting JAXB List!",e);
		}
	}

	private void setValue(final Object currentObj, final Object element, final CharArray xmlElementName) throws UnmarshalException{
		try {
			Method method = _directSetValueCache.get(xmlElementName);

			if(method == null){
				final Class<?> currentObjClass = currentObj.getClass();
				final Map<CharArray,Field> elementFieldCache = getClassElementFieldCache(currentObjClass);
				Field field = elementFieldCache.get(xmlElementName);

				if(field == null){
					field = currentObj.getClass().getDeclaredField(xmlElementName.toString());
					field.setAccessible(true);
					elementFieldCache.put(xmlElementName, field);
				}

				final Class<?> fieldType = field.getType();

				method = getSetterMethod(currentObj, field, fieldType, xmlElementName);
				_directSetValueCache.put(xmlElementName, method);
			}

			method.invoke(currentObj, element);
			//LogContext.info("<DIRECT SET VALUE> - [setValue]: "+element.getClass());
		}
		catch (final Exception e){
			throw new UnmarshalException("Error Getting JAXB Setter!", e);
		}
	}

	private void setValue(final Object currentObj, final CharArray localXmlElementName, final CharArray characters) throws UnmarshalException{
		try {
			final Class<?> currentObjClass = currentObj.getClass();
			//LogContext.info("<SET VALUE> - [setValue]: "+currentObj.getClass());
			
			final Map<CharArray,Field> elementFieldCache = getClassElementFieldCache(currentObjClass);
			Field field = elementFieldCache.get(localXmlElementName);
			Class<?> fieldType = null;;

			Method method = null;

			if(field == null){
				field = currentObjClass.getDeclaredField(localXmlElementName.toString());
				field.setAccessible(true);
				elementFieldCache.put(localXmlElementName, field);
			}
			else {
				method = _methodCache.get(field);
			}

			fieldType = field.getType();

			if(method==null){
				method = getSetterMethod(currentObj, field, fieldType, localXmlElementName);
				_methodCache.put(field, method);
			}

			invokeMethod(method, fieldType, currentObj, characters);
		}
		catch (final Exception e){
			throw new UnmarshalException("Error Getting JAXB Setter!", e);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void addToList(final List<Object> listObj, Object element, final CharArray characters){
		// If a class came in, it's a sign that special handling is needed.
		if(element instanceof Class){
			final Class<?> elementClass = (Class<?>)element;

			// Special Handling for Enums: We have the value now so we need to convert it
			// to the enum value before adding it to the list.
			if(elementClass.isEnum()){
				element = _enumValueCache.get(characters);

				if(element == null){
					element = Enum.valueOf((Class<Enum>)elementClass, characters.toString());
					_enumValueCache.put(characters, (Enum<?>)element);
				}
			}
		}

		listObj.add(element);
		//LogContext.info("<ADD-LIST> - [addToList]: "+element.getClass());
	}

	private FastMap<CharArray,Field> getClassElementFieldCache(final Class<?> classObject){
		FastMap<CharArray,Field> elementFieldCache = _classElementFieldCache.get(classObject);

		if(elementFieldCache == null){
			elementFieldCache = new FastMap<CharArray,Field>(Equalities.CHAR_ARRAY_FAST);
			_classElementFieldCache.put(classObject, elementFieldCache);
		}

		return elementFieldCache;
	}

	public void setValidating(final boolean isValidating){
		_isValidating = isValidating;
	}

}