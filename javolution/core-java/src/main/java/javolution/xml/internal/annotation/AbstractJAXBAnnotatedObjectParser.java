/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 *
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.xml.internal.annotation;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchema;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.datatype.XMLGregorianCalendar;

import javolution.context.LogContext;
import javolution.text.CharArray;
import javolution.text.TextBuilder;
import javolution.util.FastMap;
import javolution.util.FastSet;
import javolution.util.function.Equalities;

public abstract class AbstractJAXBAnnotatedObjectParser {

	private final boolean _useObjectFactories;

	protected final FastMap<Class<?>, CacheData> _classCacheData;
	protected final FastMap<Class<?>, String> _classElementNameCache;
	protected final FastMap<Class<?>,String> _classNameSpaceCache;
	protected final FastMap<Class<?>,Object> _classObjectFactoryCache;
	protected final FastMap<CharArray,Class<?>> _elementClassCache;
	protected final FastMap<Field,CharArray> _fieldAttributeNameCache;
	protected final FastMap<Field, String> _fieldElementNameCache;
	protected final FastMap<Field,Class<?>> _genericTypeCache;
	protected final FastMap<Class<?>,XmlAccessType> _xmlAccessTypeCache;
	protected final FastMap<Class<?>,Boolean> _basicInstanceCache;
	protected final FastMap<Class<?>,FastSet<Field>> _declaredFieldsCache;
	protected final FastMap<String,Object> _namespaceObjectFactoryCache;
	protected final FastMap<Class<?>, Method> _objectFactoryCache;
	protected final FastMap<Class<?>, FastSet<CharArray>> _propOrderCache;
	protected final FastMap<Class<?>, FastSet<CharArray>> _requiredCache;
	protected final FastSet<Class<?>> _registeredClassesCache;
	protected final FastMap<CharArray,CharArray> _xmlElementNameCache;
	protected final FastMap<Field,XmlSchemaTypeEnum> _xmlSchemaTypeCache;
	protected final FastSet<Class<?>> _xmlSeeAlsoCache;
	protected final FastMap<Class<?>,Field> _xmlValueFieldCache;

	public AbstractJAXBAnnotatedObjectParser(final boolean useObjectFactories){
		_basicInstanceCache = new FastMap<Class<?>,Boolean>(Equalities.IDENTITY, Equalities.IDENTITY);
		_classCacheData = new FastMap<Class<?>, CacheData>(Equalities.IDENTITY, Equalities.IDENTITY);
		_classNameSpaceCache = new FastMap<Class<?>, String>(Equalities.IDENTITY, Equalities.LEXICAL);
		_declaredFieldsCache = new FastMap<Class<?>,FastSet<Field>>(Equalities.IDENTITY, Equalities.IDENTITY);
		_elementClassCache = new FastMap<CharArray,Class<?>>(Equalities.CHAR_ARRAY_FAST, Equalities.IDENTITY);
		_fieldAttributeNameCache = new FastMap<Field,CharArray>(Equalities.IDENTITY, Equalities.IDENTITY);
		_fieldElementNameCache = new FastMap<Field, String>(Equalities.IDENTITY, Equalities.LEXICAL_FAST);
		_genericTypeCache = new FastMap<Field,Class<?>>(Equalities.IDENTITY, Equalities.IDENTITY);
		_propOrderCache = new FastMap<Class<?>, FastSet<CharArray>>(Equalities.IDENTITY, Equalities.IDENTITY);
		_registeredClassesCache = new FastSet<Class<?>>(Equalities.IDENTITY);
		_requiredCache = new FastMap<Class<?>, FastSet<CharArray>>(Equalities.IDENTITY, Equalities.IDENTITY);
		_xmlAccessTypeCache = new FastMap<Class<?>,XmlAccessType>(Equalities.IDENTITY, Equalities.IDENTITY);
		_xmlElementNameCache = new FastMap<CharArray, CharArray>(Equalities.CHAR_ARRAY_FAST, Equalities.CHAR_ARRAY_FAST);
		_xmlSchemaTypeCache = new FastMap<Field,XmlSchemaTypeEnum>(Equalities.IDENTITY, Equalities.IDENTITY);
		_xmlSeeAlsoCache = new FastSet<Class<?>>(Equalities.IDENTITY);
		_xmlValueFieldCache = new FastMap<Class<?>,Field>(Equalities.IDENTITY, Equalities.IDENTITY);

		if (useObjectFactories) {
			_classElementNameCache = null;
			_classObjectFactoryCache = new FastMap<Class<?>, Object>(Equalities.IDENTITY, Equalities.IDENTITY);
			_namespaceObjectFactoryCache = new FastMap<String,Object>(Equalities.LEXICAL,Equalities.IDENTITY);
			_objectFactoryCache = new FastMap<Class<?>,Method>(Equalities.IDENTITY, Equalities.IDENTITY);
		}
		else {
			_classElementNameCache = new FastMap<Class<?>,String>(Equalities.IDENTITY, Equalities.LEXICAL_FAST);
			_classObjectFactoryCache = null;
			_namespaceObjectFactoryCache = null;
			_objectFactoryCache = null;
		}

		_useObjectFactories = useObjectFactories;
	}

	/**
	 * This method will scan the input class and all subclasses and
	 * register any JAXB objects as part of this reader
	 */
	protected void registerContextClasses(final Class<?> inputClass){
		_registeredClassesCache.add(inputClass);

		final FastSet<Field> fields = getDeclaredFields(inputClass);

		// Iterate the fields of this class to scan for sub-objects
		for(final Field field : fields){
			final Class<?> type = field.getType();
			final Class<?> scanClass;

			// If it's a list we need to grab the generic to scan
			if(type.isAssignableFrom(List.class)){
				scanClass = getGenericType(field);
			}
			else {
				scanClass = type;
			}

			// Only register classes that are JAXB objects and that we haven't seen yet
			if(!_registeredClassesCache.contains(scanClass) && (scanClass.isAnnotationPresent(XmlRootElement.class) ||
					scanClass.isAnnotationPresent(XmlType.class))){
				_registeredClassesCache.add(scanClass);
				registerContextClasses(scanClass);
			}
		}

		// Scan the class and cache all fields, attributes, etc.
		scanClass(inputClass, fields, !_useObjectFactories);
	}

	private static boolean isElementSkippableBasedOnFieldAnnotations(final Field field, final XmlAccessType type){
		if(type == XmlAccessType.FIELD){
			if(field.isAnnotationPresent(XmlTransient.class)){
				return true;
			}
		}
		else if(!field.isAnnotationPresent(XmlElement.class) && !field.isAnnotationPresent(XmlAttribute.class)
				&& !field.isAnnotationPresent(XmlValue.class)){
			return true;
		}

		return false;
	}

	/**
	 * This method scans a given JAXB class and builds up the caches for it
	 * @param scanClass Class to Scan
	 * @param fields Fields for the Class
	 * @param skipFactory TRUE to skip factory scanning, FALSE otherwise
	 */
	protected void scanClass(final Class<?> scanClass, final FastSet<Field> fields, final boolean skipFactory){
		// Get or Start a Cache for the Class
		CacheData cacheData = _classCacheData.get(scanClass);

		if(cacheData == null){
			cacheData = new CacheData();
			_classCacheData.put(scanClass, cacheData);
		}

		// Cache NameSpace Data
		final XmlType xmlType = scanClass.getAnnotation(XmlType.class);
		String namespace = "##default";

		if(xmlType == null || "##default".equals(namespace)){
			final XmlRootElement xmlRootElement = scanClass.getAnnotation(XmlRootElement.class);
			if(xmlRootElement == null || "##default".equals(namespace)){
				final XmlSchema xmlSchema = scanClass.getPackage().getAnnotation(XmlSchema.class);
				if(xmlSchema != null){
					namespace = xmlSchema.namespace();
				}
			}
			else {
				namespace = xmlRootElement.namespace();
			}
		}
		else {
			namespace = xmlType.namespace();
		}

		_classNameSpaceCache.put(scanClass, namespace);

		// Detect Object Factory (Reader Uses)
		if(_useObjectFactories) {
			if(!skipFactory && !"##default".equals(namespace) && !_namespaceObjectFactoryCache.containsKey(namespace)){
				final TextBuilder objectFactoryBuilder = new TextBuilder(scanClass.getPackage().getName());
				objectFactoryBuilder.append(".ObjectFactory");

				try {
					final Class<?> objectFactoryClass = Class.forName(objectFactoryBuilder.toString());
					final Object objectFactory = objectFactoryClass.newInstance();

					scanObjectFactory(objectFactory, false);

					_namespaceObjectFactoryCache.put(namespace, objectFactory);
				}
				catch (final Exception e) {
					LogContext.warning(String.format("Failed to Locate Object Factory for Namespace = %s",namespace));
				}
			}
		}
		else {
			String localName = xmlType.name();

			if((localName == null || localName.length()==0) && scanClass.isAnnotationPresent(XmlRootElement.class)){
				final XmlRootElement xmlRootElement = scanClass.getAnnotation(XmlRootElement.class);
				localName = xmlRootElement.name();
			}

			_classElementNameCache.put(scanClass, localName);
		}

		// Prepare Data Structures
		final FastMap<CharArray, Field> cachedAttributeFields = cacheData._attributeFieldsCache;;
		final FastSet<CharArray> requiredFieldsSet = new FastSet<CharArray>(Equalities.CHAR_ARRAY_FAST);

		field : for(final Field field : fields){

			// XmlAccessType is required to know how to treat fields that do not have an explicit
			// JAXB annotation attached to them. The most common type is Field, which XJC generated objects use.
			// Field is currently the only implemented type, but you can explicitly use annotations however you want.
			final XmlAccessType xmlAccessType = getXmlAccessType(scanClass);

			// Optimization: Use access type and other annotations to determine skip.
			if(isElementSkippableBasedOnFieldAnnotations(field, xmlAccessType))
				continue field;

			// Cache Value Field
			if(field.isAnnotationPresent(XmlValue.class)) {
				cacheData._xmlValueField = field;
				continue field;
			}

			// Check Schema Type Data
			final XmlSchemaType xmlSchemaType = field.getAnnotation(XmlSchemaType.class);

			if(xmlSchemaType != null){
				// We only care about types we have enumerated (for special handling later)
				final XmlSchemaTypeEnum xmlSchemaTypeEnum = XmlSchemaTypeEnum.fromString(xmlSchemaType.name());

				if(xmlSchemaTypeEnum != null){
					_xmlSchemaTypeCache.put(field, xmlSchemaTypeEnum);
				}
			}

			// Caching Attribute Data
			final XmlAttribute xmlAttribute = field.getAnnotation(XmlAttribute.class);

			if(xmlAttribute != null){
				// Cache Attribute Data
				final CharArray xmlAttributeName = getXmlAttributeName(field);
				cachedAttributeFields.put(xmlAttributeName, field);

				if(xmlAttribute.required()){
					requiredFieldsSet.add(xmlAttributeName);
				}

				continue field;
			}

			// Caching Element Data
			CharArray xmlElementName;
			final XmlElements xmlElements = field.getAnnotation(XmlElements.class);

			// Standalone Elements
			if(xmlElements == null){
				xmlElementName = getXmlElementName(field);
				cacheData._elementFieldCache.put(xmlElementName, field);
				_fieldElementNameCache.put(field, xmlElementName.toString());
			}
			// Mapped Elements
			else {
				xmlElementName = getXmlElementNameWithMappedElements(xmlElements,
						cacheData._mappedElementsCache, cacheData._elementFieldCache, field);
				cacheData._elementFieldCache.put(xmlElementName, field);
			}

			cacheData._propOrderFieldCache.put(new CharArray(field.getName()), field);

			// Cache Element -> Class Mapping
			final Class<?> type = field.getType();
			final Class<?> typeClass;

			if(type.isAssignableFrom(List.class)){
				typeClass = getGenericType(field);
			}
			else {
				typeClass = type;
			}

			_elementClassCache.put(xmlElementName, typeClass);

			// For validation, capture required data.
			final XmlElement xmlElement = field.getAnnotation(XmlElement.class);

			if(xmlElement != null && xmlElement.required()){
				requiredFieldsSet.add(xmlElementName);
			}
		}

		_requiredCache.put(scanClass, requiredFieldsSet);

		// Check @XmlSeeAlso
		final XmlSeeAlso xmlSeeAlso = scanClass.getAnnotation(XmlSeeAlso.class);

		if(xmlSeeAlso != null){
			final Class<?>[] seeAlso = xmlSeeAlso.value();
			_xmlSeeAlsoCache.add(scanClass);

			for(final Class<?> seeAlsoClass : seeAlso){
				if(!_registeredClassesCache.contains(seeAlsoClass)){
					registerContextClasses(seeAlsoClass);
				}
			}
		}
	}

	/**
	 * This method scans an ObjectFactory and builds the caches for it
	 * @param objectFactory Object Factory to Scan
	 * @param customFactory TRUE if this is a custom factory set in by the user. If so
	 * then it must be scanned here. If FALSE, its a default factory and is being scanned
	 * as part of the scan class call.
	 */
	protected void scanObjectFactory(final Object objectFactory, final boolean customFactory){
		final FastSet<Method> objectFactoryMethods = getDeclaredMethods(objectFactory.getClass());

		for(final Method method : objectFactoryMethods){
			final Class<?> objectClass = method.getReturnType();
			_classObjectFactoryCache.put(objectClass, objectFactory);

			if(customFactory){
				try {
					if(method.getName().contains("create")) {
						final Object customObject = method.invoke(objectFactory, (Object[])null);
						final Class<?> customClass = customObject.getClass();

						if(!_registeredClassesCache.contains(customClass)){
							final FastSet<Field> fields = getDeclaredFields(customClass);
							scanClass(customClass, fields, true);
						}
					}
				}
				catch (final Exception e){
					LogContext.error(String.format("Error Scanning Custom Object Factory <%s>!",
							objectFactory.getClass()), e);
				}

			}
			_objectFactoryCache.put(objectClass, method);
		}
	}

	private CharArray getXmlElementName(final Field field){
		CharArray xmlElementName;

		final XmlElement xmlElement = field.getAnnotation(XmlElement.class);

		if(xmlElement == null){
			xmlElementName = new CharArray(field.getName());
		}
		else {
			xmlElementName = new CharArray(xmlElement.name());
		}

		_xmlElementNameCache.put(xmlElementName, xmlElementName);

		return xmlElementName;
	}

	private CharArray getXmlElementNameWithMappedElements(final XmlElements xmlElements,
			final FastMap<CharArray,FastSet<CharArray>> mappedElementsCache,
			final FastMap<CharArray,Field> elementFieldCache, final Field field){
		final CharArray thisXmlElementName = getXmlElementName(field);
		final FastSet<CharArray> mappedElementsSet = new FastSet<CharArray>(Equalities.CHAR_ARRAY_FAST);
		final XmlElement[] elements = xmlElements.value();

		for(final XmlElement element : elements){
			final CharArray nameKey = new CharArray(element.name());
			CharArray name = _xmlElementNameCache.get(nameKey);

			if(name == null){
				_xmlElementNameCache.put(nameKey, nameKey);
				name = nameKey;
			}

			final Class<?> elementType = element.type();
			_elementClassCache.put(name, elementType);
			elementFieldCache.put(name, field);

			// Scan Choice Classes
			if(!_registeredClassesCache.contains(elementType)) {
				registerContextClasses(elementType);
			}

			//LogContext.info("<XML-ELEMENTS SCAN> Field: "+field.getName()+" | Element Name: "+name+" | Element Type: "+element.type());

			// Mapped elements will be used later to switch detection
			mappedElementsSet.add(name);
			mappedElementsCache.put(name, mappedElementsSet);
			//LogContext.info("Store Mapped Elements: Element Key = "+name+", Mapped Elements: "+mappedElementsSet);
		}

		return thisXmlElementName;
	}

	protected boolean isInstanceOfBasicType(final Class<?> objClass){
		Boolean basicInstance = _basicInstanceCache.get(objClass);

		if(basicInstance == null){
			basicInstance = (objClass.isAssignableFrom(Long.class) ||
					objClass.isAssignableFrom(Integer.class) ||
					objClass.isAssignableFrom(String.class) ||
					objClass.isAssignableFrom(XMLGregorianCalendar.class) ||
					objClass.isAssignableFrom(Boolean.class) ||
					objClass.isEnum() || objClass.isPrimitive() ||
					objClass.isAssignableFrom(Double.class) ||
					objClass.isAssignableFrom(Float.class) ||
					objClass.isAssignableFrom(Byte.class) ||
					objClass.isAssignableFrom(Short.class));
			_basicInstanceCache.put(objClass, basicInstance);
		}

		return basicInstance;
	}

	protected XmlAccessType getXmlAccessType(final Class<?> objectClass){
		XmlAccessType xmlAccessType = _xmlAccessTypeCache.get(objectClass);

		if(xmlAccessType == null && !_xmlAccessTypeCache.containsKey(objectClass)){
			if(objectClass.isAnnotationPresent(XmlAccessorType.class)){
				xmlAccessType = objectClass.getAnnotation(XmlAccessorType.class).value();
				_xmlAccessTypeCache.put(objectClass, xmlAccessType);
			}
		}

		return xmlAccessType;
	}

	protected Class<?> getGenericType(final Field field){
		Class<?> genericType = _genericTypeCache.get(field);

		if(genericType == null){
			final ParameterizedType type = (ParameterizedType)field.getGenericType();
			genericType = (Class<?>)type.getActualTypeArguments()[0];
			_genericTypeCache.put(field, genericType);
		}

		return genericType;
	}

	protected FastSet<Field> getDeclaredFields(final Class<?> classObject){
		FastSet<Field> declaredFields = _declaredFieldsCache.get(classObject);

		if(declaredFields == null){
			Class<?> thisClassObject = classObject;
			declaredFields = new FastSet<Field>(Equalities.IDENTITY);

			do {
				final Field[] fields = thisClassObject.getDeclaredFields();

				for(final Field field : fields){
					field.setAccessible(true);
					declaredFields.add(field);
				}
			}
			while((thisClassObject = thisClassObject.getSuperclass()) != null);

			_declaredFieldsCache.put(classObject, declaredFields);
		}

		return declaredFields;
	}

	protected FastSet<Method> getDeclaredMethods(final Class<?> classObject){
		Class<?> thisClassObject = classObject;
		final FastSet<Method> declaredMethods = new FastSet<Method>(Equalities.IDENTITY);

		do {
			final Method[] methods = thisClassObject.getDeclaredMethods();

			for(final Method method : methods){
				method.setAccessible(true);
				declaredMethods.add(method);
			}
		}
		while((thisClassObject = thisClassObject.getSuperclass()) != null);

		return declaredMethods;
	}

	protected Iterator<CharArray> getXmlPropOrder(final Class<?> classObject){
		FastSet<CharArray> propOrderSet = _propOrderCache.get(classObject);

		if(propOrderSet == null && classObject.isAnnotationPresent(XmlType.class)){
			Class<?> thisClass = classObject;

			// Note: The reversed view logic makes sure super class prop orders appear first
			// in the final set, and are in order going all the way down to the final implementation
			// class.
			propOrderSet = new FastSet<CharArray>(Equalities.CHAR_ARRAY_FAST);

			do {
				final XmlType xmlType = thisClass.getAnnotation(XmlType.class);

				final FastSet<CharArray> localPropOrderSet = new FastSet<CharArray>(Equalities.CHAR_ARRAY_FAST);

				for(final String prop : xmlType.propOrder()){
					localPropOrderSet.add(getXmlElementName(prop));
				}

				propOrderSet.addAll(localPropOrderSet.reversed());
			}
			while((thisClass = thisClass.getSuperclass()) != null && thisClass != Object.class);

			propOrderSet = FastSet.of(propOrderSet.reversed());

			_propOrderCache.put(classObject, propOrderSet);
		}

		return propOrderSet == null ? null : propOrderSet.iterator();
	}

	protected CharArray getXmlElementName(final String nameString){
		final CharArray name = new CharArray(nameString);
		CharArray xmlElementName = _xmlElementNameCache.get(name);

		if(xmlElementName == null){
			//LogContext.info("<NEW INSTANCE XML ELEMENT NAME>");
			synchronized(_xmlElementNameCache){
				xmlElementName = _xmlElementNameCache.putIfAbsent(name, name);
				if(xmlElementName == null) return name;
			}
		}

		return xmlElementName;
	}

	protected CharArray getXmlElementName(final CharArray localName){
		CharArray xmlElementName = _xmlElementNameCache.get(localName);

		if(xmlElementName == null){
			//LogContext.info("<NEW INSTANCE XML ELEMENT NAME>");
			xmlElementName = copyCharArrayViewport(localName);
			_xmlElementNameCache.put(xmlElementName, xmlElementName);
		}

		return xmlElementName;
	}

	private static CharArray copyCharArrayViewport(final CharArray charArray){
		final CharArray outputArray = new CharArray();
		final char[] array = new char[charArray.length()];
		System.arraycopy(charArray.array(), charArray.offset(), array, 0, array.length);
		outputArray.setArray(array, 0, array.length);
		return outputArray;
	}

	protected CharArray getXmlAttributeName(final Field field){
		CharArray xmlAttributeName = _fieldAttributeNameCache.get(field);

		if(xmlAttributeName == null){
			final XmlAttribute thisAttribute = field.getAnnotation(XmlAttribute.class);
			xmlAttributeName = new CharArray(thisAttribute.name());
			_fieldAttributeNameCache.put(field, xmlAttributeName);
		}

		return xmlAttributeName;
	}

	protected enum InvocationClassType {

		STRING(String.class),
		LONG(Long.class),
		XML_GREGORIAN_CALENDAR(XMLGregorianCalendar.class),
		INTEGER(Integer.class),
		BOOLEAN(Boolean.class),
		DOUBLE(Double.class),
		BYTE(Byte.class),
		FLOAT(Float.class),
		SHORT(Short.class),
		PRIMITIVE_LONG(long.class),
		PRIMITIVE_INTEGER(int.class),
		PRIMITIVE_BOOLEAN(boolean.class),
		PRIMITIVE_DOUBLE(double.class),
		PRIMITIVE_BYTE(byte.class),
		PRIMITIVE_FLOAT(float.class),
		PRIMITIVE_SHORT(short.class),
		ENUM(Enum.class),
		OBJECT(Object.class);

		private static final HashMap<Class<?>,InvocationClassType> types;

		static {
			types = new HashMap<Class<?>,InvocationClassType>(17);

			for(final InvocationClassType type : EnumSet.allOf(InvocationClassType.class)){
				types.put(type.type, type);
			}
		}

		private final Class<?> type;

		private InvocationClassType(final Class<?> type){
			this.type = type;
		}

		public static InvocationClassType valueOf(final Class<?> type){
			return types.get(type);
		}

	}

	protected enum XmlSchemaTypeEnum {

		DATE("date"),
		TIME("time"),
		DATE_TIME("dateTime");

		private static final HashMap<String,XmlSchemaTypeEnum> types;

		static {
			types = new HashMap<String,XmlSchemaTypeEnum>(3);

			for(final XmlSchemaTypeEnum type : EnumSet.allOf(XmlSchemaTypeEnum.class)){
				types.put(type.type, type);
			}
		}

		private final String type;

		private XmlSchemaTypeEnum(final String type){
			this.type = type;
		}

		public static XmlSchemaTypeEnum fromString(final String type){
			return types.get(type);
		}

	}

	protected class CacheData {
		final FastMap<CharArray,Field> _attributeFieldsCache;
		final FastMap<CharArray,Method> _directSetValueCache;
		final FastMap<CharArray,Field> _elementFieldCache;
		final FastMap<CharArray,Enum<?>> _enumValueCache;
		final FastMap<CharArray,Field> _propOrderFieldCache;

		final FastMap<CharArray,FastSet<CharArray>> _mappedElementsCache;
		Field _xmlValueField;

		public CacheData() {
			_attributeFieldsCache = new FastMap<CharArray,Field>(Equalities.CHAR_ARRAY_FAST, Equalities.IDENTITY);
			_directSetValueCache = new FastMap<CharArray, Method>(Equalities.CHAR_ARRAY_FAST, Equalities.IDENTITY);
			_elementFieldCache = new FastMap<CharArray,Field>(Equalities.CHAR_ARRAY_FAST, Equalities.IDENTITY);
			_enumValueCache = new FastMap<CharArray,Enum<?>>(Equalities.CHAR_ARRAY_FAST, Equalities.IDENTITY);
			_mappedElementsCache = new FastMap<CharArray,FastSet<CharArray>>(Equalities.CHAR_ARRAY_FAST, Equalities.IDENTITY);
			_propOrderFieldCache = new FastMap<CharArray,Field>(Equalities.CHAR_ARRAY_FAST, Equalities.IDENTITY);
			_xmlValueField = null;
		}
	}
}
