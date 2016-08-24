/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 *
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.osgi.internal;

import org.javolution.context.ComputeContext;
import org.javolution.context.ConcurrentContext;
import org.javolution.context.LocalContext;
import org.javolution.context.LogContext;
import org.javolution.context.SecurityContext;
import org.javolution.context.StorageContext;
import org.javolution.context.internal.ComputeContextImpl;
import org.javolution.context.internal.ConcurrentContextImpl;
import org.javolution.context.internal.LocalContextImpl;
import org.javolution.context.internal.LogContextImpl;
import org.javolution.context.internal.SLF4jLogContextImpl;
import org.javolution.context.internal.SecurityContextImpl;
import org.javolution.context.internal.StorageContextImpl;
import org.javolution.io.Struct;
import org.javolution.lang.Configurable;
import org.javolution.lang.Index;
import org.javolution.lang.Initializer;
import org.javolution.lang.MathLib;
import org.javolution.text.Text;
import org.javolution.text.TextContext;
import org.javolution.text.TypeFormat;
import org.javolution.text.internal.TextContextImpl;
import org.javolution.util.BitSet;
import org.javolution.xml.XMLContext;
import org.javolution.xml.internal.XMLContextImpl;
import org.javolution.xml.internal.jaxb.JAXBAnnotatedObjectReaderImpl;
import org.javolution.xml.internal.jaxb.JAXBAnnotatedObjectWriterImpl;
import org.javolution.xml.internal.jaxb.JAXBAnnotationFactoryImpl;
import org.javolution.xml.internal.stream.XMLInputFactoryImpl;
import org.javolution.xml.internal.stream.XMLOutputFactoryImpl;
import org.javolution.xml.internal.stream.XMLStreamReaderImpl;
import org.javolution.xml.internal.stream.XMLStreamWriterImpl;
import org.javolution.xml.jaxb.JAXBAnnotationFactory;
import org.javolution.xml.stream.XMLInputFactory;
import org.javolution.xml.stream.XMLOutputFactory;
import org.osgi.service.log.LogService;

/**
 * The OSGi services tracked by the Javolution bundle.
 * When running outside OSGi or when the Javolution bundle is not started
 * the default service implementation is returned.
 */
public class OSGiServices {

	final static ServiceTrackerImpl<ConcurrentContext> CONCURRENT_CONTEXT_TRACKER = new ServiceTrackerImpl<ConcurrentContext>(
			ConcurrentContext.class, ConcurrentContextImpl.class);
	final static ServiceTrackerImpl<Configurable.Listener> CONFIGURABLE_LISTENER_TRACKER = new ServiceTrackerImpl<Configurable.Listener>(
			Configurable.Listener.class, ConfigurableListenerImpl.class);
	final static ServiceTrackerImpl<LocalContext> LOCAL_CONTEXT_TRACKER = new ServiceTrackerImpl<LocalContext>(
			LocalContext.class, LocalContextImpl.class);
	final static ServiceTrackerImpl<LogContext> LOG_CONTEXT_TRACKER;
	final static ServiceTrackerImpl<LogService> LOG_SERVICE_TRACKER;
	final static ServiceTrackerImpl<ComputeContext> COMPUTE_CONTEXT_TRACKER = new ServiceTrackerImpl<ComputeContext>(
			ComputeContext.class, ComputeContextImpl.class);
	final static ServiceTrackerImpl<SecurityContext> SECURITY_CONTEXT_TRACKER = new ServiceTrackerImpl<SecurityContext>(
			SecurityContext.class, SecurityContextImpl.class);
	final static ServiceTrackerImpl<StorageContext> STORAGE_CONTEXT_TRACKER = new ServiceTrackerImpl<StorageContext>(
			StorageContext.class, StorageContextImpl.class);
	final static ServiceTrackerImpl<TextContext> TEXT_CONTEXT_TRACKER = new ServiceTrackerImpl<TextContext>(
			TextContext.class, TextContextImpl.class);
	final static ServiceTrackerImpl<XMLContext> XML_CONTEXT_TRACKER = new ServiceTrackerImpl<XMLContext>(
			XMLContext.class, XMLContextImpl.class);
	final static ServiceTrackerImpl<JAXBAnnotationFactory> JAXB_ANNOTATION_FACTORY_TRACKER = new ServiceTrackerImpl<JAXBAnnotationFactory>(
			JAXBAnnotationFactory.class, JAXBAnnotationFactoryImpl.class);
	final static ServiceTrackerImpl<XMLInputFactory> XML_INPUT_FACTORY_TRACKER = new ServiceTrackerImpl<XMLInputFactory>(
			XMLInputFactory.class, XMLInputFactoryImpl.class);
	final static ServiceTrackerImpl<XMLOutputFactory> XML_OUTPUT_FACTORY_TRACKER = new ServiceTrackerImpl<XMLOutputFactory>(
			XMLOutputFactory.class, XMLOutputFactoryImpl.class);

	static {
		// Attempt to Bridge to SLF4j By Default If SLF4j-API Is Present On the Classpath
		boolean slf4jDetection;

		try {
			Class.forName("org.slf4j.LoggerFactory");
			slf4jDetection = false;
		}
		catch (final ClassNotFoundException e) {
			slf4jDetection = false;
		}

		if(slf4jDetection){
			LOG_CONTEXT_TRACKER = new ServiceTrackerImpl<LogContext>(
					LogContext.class, SLF4jLogContextImpl.class);
			LOG_SERVICE_TRACKER = new ServiceTrackerImpl<LogService>(
					LogService.class, SLF4jLogServiceImpl.class);
		}
		else {
			LOG_CONTEXT_TRACKER = new ServiceTrackerImpl<LogContext>(
					LogContext.class, LogContextImpl.class);
			LOG_SERVICE_TRACKER = new ServiceTrackerImpl<LogService>(
					LogService.class, LogServiceImpl.class);
		}
	}

	/** Returns concurrent context services. */
	public static ConcurrentContext getConcurrentContext() {
		return (ConcurrentContext)CONCURRENT_CONTEXT_TRACKER.getServices()[0];
	}

	/** Returns configurable listener services. */
	public static Object[] getConfigurableListeners() {
		return CONFIGURABLE_LISTENER_TRACKER.getServices();
	}

	/** Returns local context service. */
	public static LocalContext getLocalContext() {
		return (LocalContext)LOCAL_CONTEXT_TRACKER.getServices()[0];
	}

	/** Returns log context service. */
	public static LogContext getLogContext() {
		return (LogContext)LOG_CONTEXT_TRACKER.getServices()[0];
	}

	/** Returns OSGi log service. */
	public static Object[] getLogServices() {
		return LOG_SERVICE_TRACKER.getServices();
	}

	/** Returns compute context service. */
	public static ComputeContext getComputeContext() {
		return (ComputeContext) COMPUTE_CONTEXT_TRACKER.getServices()[0];
	}

	/** Returns security context service. */
	public static SecurityContext getSecurityContext() {
		return (SecurityContext) SECURITY_CONTEXT_TRACKER.getServices()[0];
	}

	/** Returns storage context service. */
	public static StorageContext getStorageContext() {
		return (StorageContext) STORAGE_CONTEXT_TRACKER.getServices()[0];
	}

	/** Returns text context service. */
	public static TextContext getTextContext() {
		return (TextContext)TEXT_CONTEXT_TRACKER.getServices()[0];
	}

	/** Returns xml context service. */
	public static XMLContext getXMLContext() {
		return (XMLContext)XML_CONTEXT_TRACKER.getServices()[0];
	}

	/** Returns xml input factory service. */
	public static XMLInputFactory getXMLInputFactory() {
		return (XMLInputFactory)XML_INPUT_FACTORY_TRACKER.getServices()[0];
	}

	/** Returns xml output factory service. */
	public static XMLOutputFactory getXMLOutputFactory() {
		return (XMLOutputFactory)XML_OUTPUT_FACTORY_TRACKER.getServices()[0];
	}
	
	/** Returns JAXB Annotation Factory Service */
	public static JAXBAnnotationFactory getJAXBAnnotationFactory() {
		return (JAXBAnnotationFactory)JAXB_ANNOTATION_FACTORY_TRACKER.getServices()[0];
	}

	/** Initializes all real-time classes.  */
	public static synchronized boolean initializeRealtimeClasses() {
		final Initializer initializer = new Initializer(OSGiServices.class.getClassLoader());
		initializer.loadClass(MathLib.class);
		initializer.loadClass(Text.class);
		initializer.loadClass(TypeFormat.class);
		initializer.loadClass(Struct.class);
		initializer.loadClass(BitSet.class);
		initializer.loadClass(Index.class); // Preallocates.
		initializer.loadClass(JAXBAnnotatedObjectReaderImpl.class);
		initializer.loadClass(JAXBAnnotatedObjectWriterImpl.class);
		initializer.loadClass(XMLStreamReaderImpl.class);
		initializer.loadClass(XMLStreamWriterImpl.class);
		return initializer.initializeLoadedClasses();
	}
}