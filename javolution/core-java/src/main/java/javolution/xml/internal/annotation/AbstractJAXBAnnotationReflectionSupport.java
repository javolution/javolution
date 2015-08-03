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
import java.lang.reflect.ParameterizedType;
import java.util.Iterator;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;

import javolution.text.CharArray;
import javolution.util.FastMap;
import javolution.util.FastSet;
import javolution.util.function.Equalities;

public abstract class AbstractJAXBAnnotationReflectionSupport {
		
	protected final FastMap<Field,Class<?>> _genericTypeCache;
	protected final FastMap<Class<?>,XmlAccessType> _xmlAccessTypeCache;
	protected final FastMap<Class<?>,Boolean> _basicInstanceCache;
	protected final FastMap<Class<?>,FastSet<Field>> _declaredFieldsCache;
	protected final FastMap<Class<?>, FastSet<CharArray>> _propOrderCache;
	protected final FastMap<Class<?>, FastSet<CharArray>> _requiredCache;
	protected final FastMap<CharArray,CharArray> _xmlElementNameCache;
	protected final FastMap<Field,CharArray> _fieldAttributeNameCache;
	
	public AbstractJAXBAnnotationReflectionSupport(){		
		_genericTypeCache = new FastMap<Field,Class<?>>(Equalities.IDENTITY);
		_xmlAccessTypeCache = new FastMap<Class<?>,XmlAccessType>(Equalities.IDENTITY);
		_basicInstanceCache = new FastMap<Class<?>,Boolean>(Equalities.IDENTITY);
		_declaredFieldsCache = new FastMap<Class<?>,FastSet<Field>>(Equalities.IDENTITY);
		_xmlElementNameCache = new FastMap<CharArray, CharArray>(Equalities.CHAR_ARRAY_FAST);
		_propOrderCache = new FastMap<Class<?>, FastSet<CharArray>>(Equalities.IDENTITY);
		_requiredCache = new FastMap<Class<?>, FastSet<CharArray>>(Equalities.IDENTITY);
		_fieldAttributeNameCache = new FastMap<Field,CharArray>(Equalities.IDENTITY);
	}

	protected boolean isInstanceOfBasicType(final Class<?> objClass){
		Boolean basicInstance = _basicInstanceCache.get(objClass);
		
		if(basicInstance == null){
			basicInstance = (objClass.isAssignableFrom(Long.class) ||
					objClass.isAssignableFrom(Long.class) ||
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
				Field[] fields = thisClassObject.getDeclaredFields();
				
				for(final Field field : fields){
					declaredFields.add(field);
				}
			}
			while((thisClassObject = thisClassObject.getSuperclass()) != null);
			
			_declaredFieldsCache.put(classObject, declaredFields);
		}
		
		return declaredFields;
	}

	protected Iterator<CharArray> getXmlPropOrder(final Class<?> classObject){
		FastSet<CharArray> propOrderSet = _propOrderCache.get(classObject);
		
		if(propOrderSet == null && classObject.isAnnotationPresent(XmlType.class)){
			final XmlType xmlType = classObject.getAnnotation(XmlType.class);
			
			propOrderSet = new FastSet<CharArray>(Equalities.CHAR_ARRAY_FAST);
			
			for(final String prop : xmlType.propOrder()){
				propOrderSet.add(getXmlElementName(prop));
			}
			
			_propOrderCache.put(classObject, propOrderSet);
		}
		
		return propOrderSet == null ? null : propOrderSet.iterator();
	}

	protected CharArray getXmlElementName(final String nameString){
		CharArray name = new CharArray(nameString);
		CharArray xmlElementName = _xmlElementNameCache.get(name);

		if(xmlElementName == null){
			//LogContext.info("<NEW INSTANCE XML ELEMENT NAME>");
			_xmlElementNameCache.put(name, name);
			return name;
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
		charArray.getChars(0, charArray.length(), array, 0);
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
}
