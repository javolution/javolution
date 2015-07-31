/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 *
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.xml.internal.annotation;

import javax.xml.bind.JAXBException;

import javolution.util.FastMap;
import javolution.xml.annotation.JAXBAnnotatedObjectReader;
import javolution.xml.annotation.JAXBAnnotatedObjectWriter;
import javolution.xml.annotation.JAXBAnnotationFactory;

public class JAXBAnnotationFactoryImpl implements JAXBAnnotationFactory {

	private FastMap<Class<?>,JAXBAnnotatedObjectReader> _cachedReaders = new FastMap<Class<?>,JAXBAnnotatedObjectReader>();
	private FastMap<Class<?>,JAXBAnnotatedObjectWriter> _cachedWriters = new FastMap<Class<?>,JAXBAnnotatedObjectWriter>();
	
	@Override
	public JAXBAnnotatedObjectReader createJAXBAnnotatedObjectReader(final Class<?> inputClass) throws JAXBException {
		JAXBAnnotatedObjectReader reader;
		
		synchronized(_cachedReaders){
			reader = _cachedReaders.get(inputClass);
			
			if(reader == null){
				reader = new JAXBAnnotatedObjectReaderImpl(inputClass);
				_cachedReaders.put(inputClass, reader);
			}
		}
		
		return reader;
	}

	@Override
	public JAXBAnnotatedObjectWriter createJAXBAnnotatedObjectWriter(final Class<?> outputClass) throws JAXBException {
		JAXBAnnotatedObjectWriter writer;
		
		synchronized(_cachedWriters){
			writer = _cachedWriters.get(outputClass);
			
			if(writer == null){
				writer = new JAXBAnnotatedObjectWriterImpl(outputClass);
				_cachedWriters.put(outputClass, writer);
			}
		}
		
		return writer;
	}

}
