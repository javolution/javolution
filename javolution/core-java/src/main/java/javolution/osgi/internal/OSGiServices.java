/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.osgi.internal;

import javolution.context.ConcurrentContext;
import javolution.context.LocalContext;
import javolution.context.LogContext;
import javolution.context.SecurityContext;
import javolution.context.StorageContext;
import javolution.context.internal.ConcurrentContextImpl;
import javolution.context.internal.LocalContextImpl;
import javolution.context.internal.LogContextImpl;
import javolution.context.internal.SecurityContextImpl;
import javolution.context.internal.StorageContextImpl;
import javolution.io.Struct;
import javolution.lang.Configurable;
import javolution.lang.Initializer;
import javolution.lang.MathLib;
import javolution.text.Text;
import javolution.text.TextContext;
import javolution.text.TypeFormat;
import javolution.text.internal.TextContextImpl;
import javolution.util.FastBitSet;
import javolution.util.FastSortedMap;
import javolution.util.FastSortedSet;
import javolution.util.FastSortedTable;
import javolution.util.Index;
import javolution.util.function.Equalities;
import javolution.util.function.Reducers;
import javolution.xml.XMLContext;
import javolution.xml.internal.XMLContextImpl;
import javolution.xml.internal.stream.XMLInputFactoryImpl;
import javolution.xml.internal.stream.XMLOutputFactoryImpl;
import javolution.xml.internal.stream.XMLStreamReaderImpl;
import javolution.xml.internal.stream.XMLStreamWriterImpl;
import javolution.xml.stream.XMLInputFactory;
import javolution.xml.stream.XMLOutputFactory;

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
    final static ServiceTrackerImpl<LogContext> LOG_CONTEXT_TRACKER = new ServiceTrackerImpl<LogContext>(
            LogContext.class, LogContextImpl.class);
    final static ServiceTrackerImpl<LogService> LOG_SERVICE_TRACKER = new ServiceTrackerImpl<LogService>(
            LogService.class, LogServiceImpl.class);
    final static ServiceTrackerImpl<SecurityContext> SECURITY_CONTEXT_TRACKER = new ServiceTrackerImpl<SecurityContext>(
            SecurityContext.class, SecurityContextImpl.class);
    final static ServiceTrackerImpl<StorageContext> STORAGE_CONTEXT_TRACKER = new ServiceTrackerImpl<StorageContext>(
            StorageContext.class, StorageContextImpl.class);
    final static ServiceTrackerImpl<TextContext> TEXT_CONTEXT_TRACKER = new ServiceTrackerImpl<TextContext>(
            TextContext.class, TextContextImpl.class);
    final static ServiceTrackerImpl<XMLContext> XML_CONTEXT_TRACKER = new ServiceTrackerImpl<XMLContext>(
            XMLContext.class, XMLContextImpl.class);
    final static ServiceTrackerImpl<XMLInputFactory> XML_INPUT_FACTORY_TRACKER = new ServiceTrackerImpl<XMLInputFactory>(
            XMLInputFactory.class, XMLInputFactoryImpl.class);
    final static ServiceTrackerImpl<XMLOutputFactory> XML_OUTPUT_FACTORY_TRACKER = new ServiceTrackerImpl<XMLOutputFactory>(
            XMLOutputFactory.class, XMLOutputFactoryImpl.class);
 
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

    /** Initializes all real-time classes.  */ 
    public static boolean initializeRealtimeClasses() {
        Initializer initializer = new Initializer(OSGiServices.class.getClassLoader());
        initializer.loadClass(MathLib.class);
        initializer.loadClass(Text.class);
        initializer.loadClass(TypeFormat.class);
        initializer.loadClass(Struct.class);
        initializer.loadClass(FastBitSet.class);
        initializer.loadClass(FastSortedMap.class);
        initializer.loadClass(FastSortedSet.class);
        initializer.loadClass(FastSortedTable.class);
        initializer.loadClass(Index.class); // Preallocates.
        initializer.loadClass(Reducers.class);
        initializer.loadClass(Equalities.class);
        initializer.loadClass(XMLStreamReaderImpl.class); 
        initializer.loadClass(XMLStreamWriterImpl.class); 
        return initializer.initializeLoadedClasses();                                              
    }
}