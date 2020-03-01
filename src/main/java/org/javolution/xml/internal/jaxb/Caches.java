/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 *
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.xml.internal.jaxb;

import org.javolution.context.LogContext;
import org.javolution.text.CharArray;
import org.javolution.text.TextBuilder;
import org.javolution.util.AbstractMap;
import org.javolution.util.AbstractSet;
import org.javolution.util.FastMap;
import org.javolution.util.FastSet;
import org.javolution.util.function.Equality;
import org.javolution.util.function.Order;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchema;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static org.javolution.xml.internal.jaxb.Caches.CacheMode.READER;

public class Caches {
	private static final CharArray _GET = new CharArray("get");
	private static final CharArray _IS = new CharArray("is");
	private static final CharArray _SET = new CharArray("set");
	private static final CharArray _VALUE = new CharArray("value");

	final CacheMode _cacheMode;
	final AbstractMap<Class<?>, String> _classElementNameCache;
	final AbstractMap<Class<?>,String> _classNameSpaceCache;
	final AbstractMap<Class<?>,Object> _classObjectFactoryCache;
	final AbstractMap<CharArray,Class<?>> _elementClassCache;
	final AbstractMap<Field,Class<?>> _genericFieldTypeCache;
	final AbstractMap<Method,Class<?>> _genericMethodTypeCache;

	final AbstractMap<Method,CharArray> _methodAttributeNameCache;
	final AbstractMap<Method,String> _methodElementNameCache;
	final AbstractMap<Class<?>, Method> _objectFactoryCache;

	final AbstractMap<Class<?>, AbstractSet<CharArray>> _propOrderCache;
	final AbstractMap<Class<?>, AbstractSet<CharArray>> _requiredCache;
	final AbstractSet<Class<?>> _registeredClassesCache;
	final AbstractMap<CharArray,CharArray> _xmlElementNameCache;
	@SuppressWarnings("rawtypes")
	final AbstractMap<Method,Class<? extends XmlAdapter>> _xmlJavaTypeAdapterCache;
	final AbstractMap<Method,XmlSchemaTypeEnum> _xmlSchemaTypeCache;
	final AbstractSet<Class<?>> _xmlSeeAlsoCache;

	private final AbstractMap<Class<?>, CacheData> _classCacheData;
	private final AbstractMap<Class<?>,XmlAccessType> _xmlAccessTypeCache;
	private final AbstractMap<Class<?>,Boolean> _basicInstanceCache;
	private final AbstractMap<Class<?>,AbstractSet<Field>> _declaredFieldsCache;
	private final AbstractMap<String,Object> _namespaceObjectFactoryCache;

	@SuppressWarnings("rawtypes")
	public Caches(final Class<?> inputClass, final CacheMode cacheMode){
		_cacheMode = cacheMode;
		_basicInstanceCache = new FastMap<Class<?>,Boolean>(Order.identity()).linked();
		_classCacheData = new FastMap<Class<?>, CacheData>(Order.identity()).linked();
		_classNameSpaceCache = new FastMap<Class<?>,String>(Order.identity()).linked();
		_declaredFieldsCache = new FastMap<Class<?>,AbstractSet<Field>>(Order.identity()).linked();
		_elementClassCache = new FastMap<CharArray,Class<?>>(Order.lexical(), Equality.identity()).linked();
		_genericFieldTypeCache = new FastMap<Field,Class<?>>(Order.identity()).linked();
		_genericMethodTypeCache = new FastMap<Method,Class<?>>(Order.identity()).linked();
		_methodAttributeNameCache = new FastMap<Method,CharArray>(Order.identity()).linked();
		_methodElementNameCache = new FastMap<Method,String>(Order.identity()).linked();
		_propOrderCache = new FastMap<Class<?>, AbstractSet<CharArray>>(Order.identity()).linked();
		_registeredClassesCache = new FastSet<Class<?>>();
		_requiredCache = new FastMap<Class<?>, AbstractSet<CharArray>>(Order.identity()).linked();
		_xmlAccessTypeCache = new FastMap<Class<?>,XmlAccessType>(Order.identity()).linked();
		_xmlElementNameCache = new FastMap<CharArray,CharArray>(Order.lexical(), Order.lexical()).linked();
		_xmlJavaTypeAdapterCache = new FastMap<Method,Class<? extends XmlAdapter>>(Order.identity()).linked();
		_xmlSchemaTypeCache = new FastMap<Method,XmlSchemaTypeEnum>(Order.identity()).linked();
		_xmlSeeAlsoCache = new FastSet<Class<?>>(Order.identity());

		if (cacheMode == READER) {
			_classElementNameCache = null;
			_classObjectFactoryCache = new FastMap<Class<?>,Object>(Order.identity()).linked();
			_namespaceObjectFactoryCache = new FastMap<String,Object>(Order.lexical(),Equality.identity()).linked();
			_objectFactoryCache = new FastMap<Class<?>, Method>(Order.identity()).linked();
		}
		else {
			_classElementNameCache = new FastMap<Class<?>, String>(Order.identity()).linked();
			_classObjectFactoryCache = null;
			_namespaceObjectFactoryCache = null;
			_objectFactoryCache = null;
		}

		final XmlRootElement xmlRootElement = inputClass.getAnnotation(XmlRootElement.class);
		final XmlType xmlType = inputClass.getAnnotation(XmlType.class);

		final CharArray rootElementName;

		if(xmlRootElement==null){
			rootElementName = new CharArray(xmlType.name());
		}
		else {
			rootElementName = new CharArray(xmlRootElement.name());
		}

		_registeredClassesCache.add(inputClass);
		_elementClassCache.put(rootElementName, inputClass);
	}

	static <T> T workaroundGet(AbstractMap<CharArray, T> map, CharArray key) {
		T result = map.get(key); // regular call
		if (result == null) {
			// regular call didn't work => try a workaround
			for (Entry<CharArray, T> entry : map.entrySet()) {
				if (entry.getKey().equals(key)) {
					result = entry.getValue();
					break;
				}
			}
		}
		return result;
	}

	/**
	 * This method will scan the input class and all subclasses and
	 * register any JAXB objects as part of this reader
	 */
	void registerContextClasses(final Class<?> inputClass) throws NoSuchMethodException, NoSuchFieldException {
		final AbstractSet<Field> fields = getDeclaredFields(inputClass);

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
		if(_cacheMode == READER) {
			scanClass(inputClass, fields, false);
		}
		else {
			scanClass(inputClass, fields, true);
		}
	}

	private static boolean isElementSkippableBasedOnFieldAnnotations(final Field field, final XmlAccessType type){
		if(type == XmlAccessType.FIELD){
			return field.isAnnotationPresent(XmlTransient.class);
		}
		else return !field.isAnnotationPresent(XmlElement.class) && !field.isAnnotationPresent(XmlAttribute.class)
				&& !field.isAnnotationPresent(XmlValue.class);

	}

	/**
	 * This method scans a given JAXB class and builds up the caches for it
	 * @param scanClass Class to Scan
	 * @param fields Fields for the Class
	 * @param skipFactory TRUE to skip factory scanning, FALSE otherwise
	 */
	void scanClass(final Class<?> scanClass, final AbstractSet<Field> fields, final boolean skipFactory) throws NoSuchMethodException, NoSuchFieldException {
		// Get or Start a Cache for the Class
		CacheData cacheData = _classCacheData.get(scanClass);

		if(cacheData == null){
			cacheData = new CacheData(_cacheMode);
			_classCacheData.put(scanClass, cacheData);
		}

		final XmlType xmlType = scanClass.getAnnotation(XmlType.class);

		// Cache NameSpace Data
		final String namespace = scanForNamespace(scanClass, xmlType);

		// Detect Object Factory (Reader Uses)
		if(_cacheMode == READER) {
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
		final AbstractMap<CharArray, Method> cachedAttributeMethods = cacheData._attributeMethodsCache;
		final AbstractSet<Method> cachedAttributeSet = cacheData._attributeMethodsSet;
		final AbstractSet<CharArray> requiredFieldsSet = new FastSet<CharArray>(Order.lexical()).linked();

		for(final Field field : fields){

			// XmlAccessType is required to know how to treat fields that do not have an explicit
			// JAXB annotation attached to them. The most common type is Field, which XJC generated objects use.
			// Field is currently the only implemented type, but you can explicitly use annotations however you want.
			final XmlAccessType xmlAccessType = getXmlAccessType(scanClass);

			// Optimization: Use access type and other annotations to determine skip.
			if(isElementSkippableBasedOnFieldAnnotations(field, xmlAccessType))
				continue;

			// Caching Element Data
			CharArray xmlName;
			final XmlElements xmlElements = field.getAnnotation(XmlElements.class);

			// Caching Attribute Data
			final XmlAttribute xmlAttribute = field.getAnnotation(XmlAttribute.class);

			// Method Handle
			final Method method;

			if(xmlAttribute != null){
				// Cache Attribute Data
				xmlName = getXmlAttributeName(field);

				if(xmlAttribute.required()){
					requiredFieldsSet.add(xmlName);
				}

				final Class<?> fieldClass = field.getType();
				method = getMethodByXmlName(xmlName, scanClass, fieldClass);
				_methodAttributeNameCache.put(method, xmlName);

				if(_cacheMode == READER) {
					cachedAttributeMethods.put(xmlName, method);
				}
				else {
					cachedAttributeSet.add(method);
				}

				cacheData._elementMethodCache.put(xmlName, method);
			}
			// Cache Value Field
			else if(field.isAnnotationPresent(XmlValue.class)) {
				final Class<?> fieldClass = field.getType();
				method = getMethodByXmlName(_VALUE, scanClass, fieldClass);
				cacheData._xmlValueMethod = method;
				continue;
			}
			// Standalone Elements
			else if(xmlElements == null){
				xmlName = getXmlElementName(field);
				cacheData._elementFieldCache.put(xmlName, field);
				final String elementName = xmlName.toString();

				final Class<?> fieldClass = field.getType();
				method = getMethodByXmlName(xmlName, scanClass, fieldClass);
				_methodElementNameCache.put(method, elementName);

				cacheData._elementMethodCache.put(xmlName, method);
			}
			// Mapped Elements
			else {
				xmlName = getXmlElementNameWithMappedElements(scanClass, xmlElements,
						cacheData._mappedElementsCache, cacheData._elementFieldCache,
						cacheData._elementMethodCache, field);
				cacheData._elementFieldCache.put(xmlName, field);
				method = Caches.workaroundGet(cacheData._elementMethodCache, xmlName);
			}

			// Check Type Adapter
			final XmlJavaTypeAdapter xmlJavaTypeAdapter = field.getAnnotation(XmlJavaTypeAdapter.class);

			if(xmlJavaTypeAdapter != null){
				_xmlJavaTypeAdapterCache.put(method, xmlJavaTypeAdapter.value());
			}

			// Check Schema Type Data
			final XmlSchemaType xmlSchemaType = field.getAnnotation(XmlSchemaType.class);

			if(xmlSchemaType != null){
				// We only care about types we have enumerated (for special handling later)
				final XmlSchemaTypeEnum xmlSchemaTypeEnum = XmlSchemaTypeEnum.fromString(xmlSchemaType.name());

				if(xmlSchemaTypeEnum != null){
					_xmlSchemaTypeCache.put(method, xmlSchemaTypeEnum);
				}
			}

			if(xmlAttribute != null){
				continue;
			}

			cacheData._propOrderMethodCache.put(new CharArray(field.getName()), method);

			// Cache Element -> Class Mapping
			final Class<?> type = field.getType();
			final Class<?> typeClass;

			if(type.isAssignableFrom(List.class)){
				typeClass = getGenericType(field);
			}
			else {
				typeClass = type;
			}

			_elementClassCache.put(xmlName, typeClass);

			// For validation, capture required data.
			final XmlElement xmlElement = field.getAnnotation(XmlElement.class);

			if(xmlElement != null && xmlElement.required()){
				requiredFieldsSet.add(xmlName);
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

		// Check Enum Values
		if(scanClass.isEnum()){
			Enum<?>[] enumConstants = (Enum<?>[])scanClass.getEnumConstants();

			for(int i = 0; i < enumConstants.length; i++){
				final String enumFieldName = enumConstants[i].name();
				final Field enumField = scanClass.getField(enumFieldName);
				final XmlEnumValue xmlEnumValue = enumField.getAnnotation(XmlEnumValue.class);

				if(xmlEnumValue == null){
					cacheData._enumValueCache.put(getXmlElementName(enumFieldName), enumConstants[i]);
				}
				else {
					cacheData._enumValueCache.put(getXmlElementName(xmlEnumValue.value()), enumConstants[i]);
				}
			}
		}
	}

	String scanForNamespace(final Class<?> scanClass, final XmlType xmlType){
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

		return namespace;
	}

	/**
	 * This method scans an ObjectFactory and builds the caches for it
	 * @param objectFactory Object Factory to Scan
	 * @param customFactory TRUE if this is a custom factory set in by the user. If so
	 * then it must be scanned here. If FALSE, its a default factory and is being scanned
	 * as part of the scan class call.
	 */
	void scanObjectFactory(final Object objectFactory, final boolean customFactory){
		final AbstractSet<Method> objectFactoryMethods = getDeclaredMethods(objectFactory.getClass());

		for(final Method method : objectFactoryMethods){
			final Class<?> objectClass = method.getReturnType();
			_classObjectFactoryCache.put(objectClass, objectFactory);

			if(customFactory){
				try {
					if(method.getName().contains("create")) {
						final Object customObject = method.invoke(objectFactory, (Object[])null);
						final Class<?> customClass = customObject.getClass();

						if(!_registeredClassesCache.contains(customClass)){
							final AbstractSet<Field> fields = getDeclaredFields(customClass);
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

	CharArray getXmlElementName(final Field field){
		CharArray xmlElementName;

		final XmlElement xmlElement = field.getAnnotation(XmlElement.class);

		if(xmlElement == null || "##default".equals(xmlElement.name())){
			xmlElementName = new CharArray(field.getName());
		}
		else {
			xmlElementName = new CharArray(xmlElement.name());
		}

		_xmlElementNameCache.put(xmlElementName, xmlElementName);

		return xmlElementName;
	}

	CharArray getXmlElementNameWithMappedElements(final Class<?> scanClass, final XmlElements xmlElements,
												  final AbstractMap<CharArray, AbstractSet<CharArray>> mappedElementsCache,
												  final AbstractMap<CharArray, Field> elementFieldCache,
												  final AbstractMap<CharArray, Method> elementMethodCache, final Field field) throws NoSuchMethodException, NoSuchFieldException {
		final CharArray thisXmlElementName = getXmlElementName(field);
		final AbstractSet<CharArray> mappedElementsSet = new FastSet<CharArray>(Order.lexical()).linked();
		final XmlElement[] elements = xmlElements.value();

		final Class<?> fieldClass = field.getType();
		Method method = getMethodByXmlName(thisXmlElementName, scanClass, fieldClass);

		for(final XmlElement element : elements){
			final CharArray nameKey = new CharArray(element.name());
			CharArray name = _xmlElementNameCache.get(nameKey);

			if(name == null){
				_xmlElementNameCache.put(nameKey, nameKey);
				name = nameKey;
			}

			final Class<?> elementType = element.type();
			_elementClassCache.put(name, elementType);

			final String nameString = name.toString();
			_methodElementNameCache.put(method, nameString);

			elementFieldCache.put(name, field);
			if(method != null) {
				elementMethodCache.put(name, method);
			}

			// Scan Choice Classes
			if(!_registeredClassesCache.contains(elementType)) {
				_registeredClassesCache.add(elementType);
				registerContextClasses(elementType);
			}

			//LogContext.info("<XML-ELEMENTS SCAN> Field: "+field.getName()+" | Element Name: "+name+" | Element Type: "+element.type());

			// Mapped elements will be used later to switch detection
			mappedElementsSet.add(name);
			mappedElementsCache.put(name, mappedElementsSet);
			//LogContext.info("Store Mapped Elements: Element Key = "+name+", Mapped Elements: "+mappedElementsSet);
		}

		elementMethodCache.put(thisXmlElementName, method);

		return thisXmlElementName;
	}

	Method getMethodByXmlName(final CharArray xmlName, final Class<?> type, final Class<?> argumentType) throws NoSuchMethodException {
		Method method = null;
		String methodName = null;
		Class<?> typeClass = type;

		do {
			try {
				if (_cacheMode == CacheMode.WRITER || argumentType == List.class) {
					if(argumentType == Boolean.class || argumentType == boolean.class){
						methodName = getMethodName(_IS, xmlName);
					}
					else {
						methodName = getMethodName(_GET, xmlName);
					}
					method = typeClass.getMethod(methodName);
				} else {
					methodName = getMethodName(_SET, xmlName);
					method = typeClass.getMethod(methodName, argumentType);
				}
				break;
			}
			catch(final NoSuchMethodException e){
			}
		}
		while((typeClass = typeClass.getSuperclass()) != null);

		if(method == null){
			throw new NoSuchMethodException(
					String.format("Failed to Locate Method for Element, Name = %s, MethodName = %s, Type = %s, Argument Type = %s",
							xmlName, methodName, type, argumentType));
		}
		return method;
	}

	boolean isInstanceOfBasicType(final Class<?> objClass){
		Boolean basicInstance = _basicInstanceCache.get(objClass);

		if(basicInstance == null){
			basicInstance = (objClass.isAssignableFrom(Long.class) ||
					objClass.isAssignableFrom(Integer.class) ||
					objClass.isAssignableFrom(String.class) ||
					objClass.isAssignableFrom(XMLGregorianCalendar.class) ||
					objClass.isAssignableFrom(Duration.class) ||
					objClass.isAssignableFrom(QName.class) ||
					objClass.isAssignableFrom(Boolean.class) ||
					objClass.isEnum() || objClass.isPrimitive() ||
					objClass.isAssignableFrom(Double.class) ||
					objClass.isAssignableFrom(Float.class) ||
					objClass.isAssignableFrom(Byte.class) ||
					objClass.isAssignableFrom(Byte[].class) ||
					objClass.isAssignableFrom(byte[].class) ||
					objClass.isAssignableFrom(Short.class) ||
					objClass.isAssignableFrom(BigDecimal.class) ||
					objClass.isAssignableFrom(BigInteger.class) ||
					objClass == Object.class);
			_basicInstanceCache.put(objClass, basicInstance);
		}

		return basicInstance;
	}

	XmlAccessType getXmlAccessType(final Class<?> objectClass){
		XmlAccessType xmlAccessType = _xmlAccessTypeCache.get(objectClass);

		if(xmlAccessType == null && !_xmlAccessTypeCache.containsKey(objectClass)){
			if(objectClass.isAnnotationPresent(XmlAccessorType.class)){
				xmlAccessType = objectClass.getAnnotation(XmlAccessorType.class).value();
				_xmlAccessTypeCache.put(objectClass, xmlAccessType);
			}
		}

		return xmlAccessType;
	}

	Class<?> getGenericType(final Field field){
		Class<?> genericType = _genericFieldTypeCache.get(field);

		if(genericType == null){
			if(field.getGenericType() == Object.class){
				genericType = Object.class;
			}
			else {
				final ParameterizedType type = (ParameterizedType)field.getGenericType();
				genericType = (Class<?>)type.getActualTypeArguments()[0];
			}

			_genericFieldTypeCache.put(field, genericType);
		}

		return genericType;
	}

	AbstractSet<Field> getDeclaredFields(final Class<?> classObject){
		AbstractSet<Field> declaredFields = _declaredFieldsCache.get(classObject);

		if(declaredFields == null){
			Class<?> thisClassObject = classObject;
			declaredFields = new FastSet<Field>(Order.identity()).linked();

			do {
				if(!thisClassObject.isAnnotationPresent(XmlType.class) &&
						!thisClassObject.isAnnotationPresent(XmlRootElement.class)){
					continue;
				}

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

	AbstractSet<Method> getDeclaredMethods(final Class<?> classObject){
		Class<?> thisClassObject = classObject;
		final AbstractSet<Method> declaredMethods = new FastSet<Method>(Order.identity()).linked();

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

	CharArray getXmlElementName(final String nameString){
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

	private static CharArray getXmlAttributeName(final Field field){
		final XmlAttribute thisAttribute = field.getAnnotation(XmlAttribute.class);
		return new CharArray(thisAttribute.name());
	}

	static String getMethodName(final CharArray prefix, final CharArray xmlName){
		final char[] array = new char[xmlName.length()];
		xmlName.getChars(0, xmlName.length(), array, 0);
		array[0] = Character.toUpperCase(array[0]);

		final TextBuilder setterBuilder = new TextBuilder(3+array.length);
		setterBuilder.append(prefix);
		setterBuilder.append(array);

		return setterBuilder.toString();
	}

	CacheData getCacheData(Class<?> elementClass) {
		return _classCacheData.get(elementClass);
	}

	enum CacheMode {
		READER, WRITER
	}

	enum XmlSchemaTypeEnum {
		ANY_SIMPLE_TYPE("anySimpleType"),
		DATE("date"),
		DATE_TIME("dateTime"),
		TIME("time");

		private static final HashMap<String,XmlSchemaTypeEnum> types;

		static {
			types = new HashMap<String,XmlSchemaTypeEnum>(4);

			for(final XmlSchemaTypeEnum type : EnumSet.allOf(XmlSchemaTypeEnum.class)){
				types.put(type.type, type);
			}
		}

		private final String type;

		XmlSchemaTypeEnum(final String type){
			this.type = type;
		}

		public static XmlSchemaTypeEnum fromString(final String type){
			return types.get(type);
		}
	}

	static class CacheData {
		final AbstractSet<Method> _attributeMethodsSet;
		final AbstractMap<CharArray,Method> _attributeMethodsCache;
		final AbstractMap<CharArray,Method> _directSetValueCache;
		final AbstractMap<CharArray,Field> _elementFieldCache;
		final AbstractMap<CharArray,Method> _elementMethodCache;
		final AbstractMap<CharArray,Enum<?>> _enumValueCache;
		final AbstractMap<CharArray,Method> _propOrderMethodCache;

		final AbstractMap<CharArray,AbstractSet<CharArray>> _mappedElementsCache;
		Method _xmlValueMethod;

		public CacheData(CacheMode cacheMode) {
			if(cacheMode == READER) {
				_attributeMethodsCache = new FastMap<CharArray,Method>(Order.lexical(), Equality.identity()).linked();
				_attributeMethodsSet = null;
			}
			else {
				_attributeMethodsCache = null;
				_attributeMethodsSet = new FastSet<Method>(Order.identity()).linked();
			}

			_directSetValueCache = new FastMap<CharArray,Method>(Order.lexical(), Equality.identity()).linked();
			_elementFieldCache = new FastMap<CharArray,Field>(Order.lexical(), Equality.identity()).linked();
			_elementMethodCache = new FastMap<CharArray,Method>(Order.lexical(), Equality.identity()).linked();
			_enumValueCache = new FastMap<CharArray,Enum<?>>(Order.lexical(),Equality.identity()).linked();
			_mappedElementsCache = new FastMap<CharArray,AbstractSet<CharArray>>(Order.lexical(), Equality.identity()).linked();
			_propOrderMethodCache = new FastMap<CharArray,Method>(Order.lexical(), Equality.identity()).linked();
			_xmlValueMethod = null;
		}
	}
}
