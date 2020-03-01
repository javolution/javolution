/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 *
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.xml.internal.jaxb;

import org.javolution.text.CharArray;
import org.javolution.util.AbstractMap;
import org.javolution.util.AbstractSet;
import org.javolution.util.FastMap;
import org.javolution.util.FastSet;
import org.javolution.util.function.Order;
import org.javolution.xml.internal.jaxb.Caches.CacheMode;

import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

public abstract class AbstractJAXBAnnotatedObjectParser {
	protected final Caches caches;

	public AbstractJAXBAnnotatedObjectParser(final Class<?> inputClass, final CacheMode cacheMode){
		caches = new Caches(inputClass, cacheMode);
	}

	protected Class<?> getGenericType(final Field field){
		Class<?> genericType = caches._genericFieldTypeCache.get(field);

		if(genericType == null){
			if(field.getGenericType() == Object.class){
				genericType = Object.class;
			}
			else {
				final ParameterizedType type = (ParameterizedType)field.getGenericType();
				genericType = (Class<?>)type.getActualTypeArguments()[0];
			}

			caches._genericFieldTypeCache.put(field, genericType);
		}

		return genericType;
	}

	protected Class<?> getGenericType(final Method method){
		Class<?> genericType = caches._genericMethodTypeCache.get(method);

		if(genericType == null){
			if(caches._cacheMode == CacheMode.WRITER || method.getReturnType() == List.class){
				if (method.getGenericReturnType() == Object.class) {
					genericType = Object.class;
				}
				else {
					final ParameterizedType type = (ParameterizedType) method.getGenericReturnType();
					genericType = (Class<?>) type.getActualTypeArguments()[0];
				}
			}
			else {
				if (method.getGenericParameterTypes()[0] == Object.class) {
					genericType = Object.class;
				}
				else {
					final ParameterizedType type = (ParameterizedType) method.getGenericParameterTypes()[0];
					genericType = (Class<?>) type.getActualTypeArguments()[0];
				}
			}

			caches._genericMethodTypeCache.put(method, genericType);
		}

		return genericType;
	}

	protected Iterator<CharArray> getXmlPropOrder(final Class<?> classObject){
		AbstractSet<CharArray> propOrderSet = caches._propOrderCache.get(classObject);

		if(propOrderSet == null && classObject.isAnnotationPresent(XmlType.class)){
			Class<?> thisClass = classObject;

			// Note: The reversed view logic makes sure super class prop orders appear first
			// in the final set, and are in order going all the way down to the final implementation
			// class.
			propOrderSet = new FastSet<CharArray>(Order.lexical()).linked();

			do {
				final XmlType xmlType = thisClass.getAnnotation(XmlType.class);

				final AbstractSet<CharArray> localPropOrderSet = new FastSet<CharArray>(Order.lexical()).linked();

				for(final String prop : xmlType.propOrder()){
					localPropOrderSet.add(getXmlElementName(prop));
				}

				propOrderSet.addAll(localPropOrderSet.reversed());
			}
			while((thisClass = thisClass.getSuperclass()) != null && thisClass != Object.class);

			final AbstractSet<CharArray> propOrderSetCopy = new FastSet<CharArray>().linked();
			propOrderSetCopy.addAll(propOrderSet.reversed());
			propOrderSet = propOrderSetCopy;

			// LogContext.info("Prop Order - "+classObject+" | "+propOrderSet.toString());

			caches._propOrderCache.put(classObject, propOrderSet);
		}

		return propOrderSet == null ? null : propOrderSet.iterator();
	}

	protected CharArray getXmlElementName(final String nameString){
		final CharArray name = new CharArray(nameString);
		CharArray xmlElementName = caches._xmlElementNameCache.get(name);

		if(xmlElementName == null){
			//LogContext.info("<NEW INSTANCE XML ELEMENT NAME>");
			synchronized(caches._xmlElementNameCache){
				xmlElementName = caches._xmlElementNameCache.putIfAbsent(name, name);
				if(xmlElementName == null) return name;
			}
		}

		return xmlElementName;
	}

	protected CharArray getXmlElementName(final CharArray localName){
		CharArray xmlElementName = caches._xmlElementNameCache.get(localName);

		if(xmlElementName == null){
			//LogContext.info("<NEW INSTANCE XML ELEMENT NAME>");
			xmlElementName = copyCharArrayViewport(localName);
			caches._xmlElementNameCache.put(xmlElementName, xmlElementName);
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

	protected enum InvocationClassType {
		STRING(String.class),
		LONG(Long.class),
		XML_GREGORIAN_CALENDAR(XMLGregorianCalendar.class),
		INT(Integer.class),
		INTEGER(BigInteger.class),
		BOOLEAN(Boolean.class),
		DOUBLE(Double.class),
		BYTE(Byte.class),
		BYTE_ARRAY(Byte[].class),
		FLOAT(Float.class),
		SHORT(Short.class),
		DECIMAL(BigDecimal.class),
		PRIMITIVE_LONG(long.class),
		PRIMITIVE_INT(int.class),
		PRIMITIVE_BOOLEAN(boolean.class),
		PRIMITIVE_DOUBLE(double.class),
		PRIMITIVE_BYTE(byte.class),
		PRIMITIVE_BYTE_ARRAY(byte[].class),
		PRIMITIVE_FLOAT(float.class),
		PRIMITIVE_SHORT(short.class),
		ENUM(Enum.class),
		DURATION(Duration.class),
		QNAME(QName.class),
		OBJECT(Object.class);

		private static final AbstractMap<Class<?>,InvocationClassType> types;

		static {
			types = new FastMap<Class<?>,InvocationClassType>(Order.identity()).linked();
		
			for(final InvocationClassType type : EnumSet.allOf(InvocationClassType.class)){
				types.put(type.type, type);
			}
		}

		private final Class<?> type;

		InvocationClassType(final Class<?> type){
			this.type = type;
		}

		public static InvocationClassType valueOf(final Class<?> type){
			return types.get(type);
		}
	}
}
