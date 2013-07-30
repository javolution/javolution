/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.osgi.internal;

import org.osgi.service.log.LogService;

import javolution.context.ConcurrentContext;
import javolution.context.HeapContext;
import javolution.context.ImmortalContext;
import javolution.context.LocalContext;
import javolution.context.LogContext;
import javolution.context.SecurityContext;
import javolution.context.StackContext;
import javolution.context.internal.ConcurrentContextImpl;
import javolution.context.internal.HeapContextImpl;
import javolution.context.internal.ImmortalContextImpl;
import javolution.context.internal.LocalContextImpl;
import javolution.context.internal.LogContextImpl;
import javolution.context.internal.SecurityContextImpl;
import javolution.context.internal.StackContextImpl;
import javolution.text.TextContext;
import javolution.text.internal.TextContextImpl;
import javolution.xml.XMLContext;
import javolution.xml.internal.XMLContextImpl;
import javolution.xml.internal.stream.XMLInputFactoryImpl;
import javolution.xml.internal.stream.XMLOutputFactoryImpl;
import javolution.xml.stream.XMLInputFactory;
import javolution.xml.stream.XMLOutputFactory;

/**
 * The OSGi services tracked by the Javolution bundle.
 * When running outside OSGi or when the Javolution bundle is not started
 * the default service implementation is returned. 
 */
public class OSGiServices {

    final static ServiceTrackerImpl<ConcurrentContext> CONCURRENT_CONTEXT_TRACKER = new ServiceTrackerImpl<ConcurrentContext>(
            ConcurrentContext.class, new ConcurrentContextImpl());
    final static ServiceTrackerImpl<HeapContext> HEAP_CONTEXT_TRACKER = new ServiceTrackerImpl<HeapContext>(
            HeapContext.class, new HeapContextImpl());
    final static ServiceTrackerImpl<ImmortalContext> IMMORTAL_CONTEXT_TRACKER = new ServiceTrackerImpl<ImmortalContext>(
            ImmortalContext.class, new ImmortalContextImpl());
    final static ServiceTrackerImpl<LocalContext> LOCAL_CONTEXT_TRACKER = new ServiceTrackerImpl<LocalContext>(
            LocalContext.class, new LocalContextImpl());
    final static ServiceTrackerImpl<LogContext> LOG_CONTEXT_TRACKER = new ServiceTrackerImpl<LogContext>(
            LogContext.class, new LogContextImpl());
    final static ServiceTrackerImpl<LogService> LOG_SERVICE_TRACKER = new ServiceTrackerImpl<LogService>(
            LogService.class, new LogServiceImpl());
    final static ServiceTrackerImpl<SecurityContext> SECURITY_CONTEXT_TRACKER = new ServiceTrackerImpl<SecurityContext>(
            SecurityContext.class, new SecurityContextImpl());
    final static ServiceTrackerImpl<StackContext> STACK_CONTEXT_TRACKER = new ServiceTrackerImpl<StackContext>(
            StackContext.class, new StackContextImpl());
    final static ServiceTrackerImpl<TextContext> TEXT_CONTEXT_TRACKER = new ServiceTrackerImpl<TextContext>(
            TextContext.class, new TextContextImpl());
    final static ServiceTrackerImpl<XMLContext> XML_CONTEXT_TRACKER = new ServiceTrackerImpl<XMLContext>(
            XMLContext.class, new XMLContextImpl());
    final static ServiceTrackerImpl<XMLInputFactory> XML_INPUT_FACTORY_TRACKER = new ServiceTrackerImpl<XMLInputFactory>(
            XMLInputFactory.class, new XMLInputFactoryImpl());
    final static ServiceTrackerImpl<XMLOutputFactory> XML_OUTPUT_FACTORY_TRACKER = new ServiceTrackerImpl<XMLOutputFactory>(
            XMLOutputFactory.class, new XMLOutputFactoryImpl());

    /** Returns concurrent context service. */
    public static ConcurrentContext getConcurrentContext() {
        return CONCURRENT_CONTEXT_TRACKER.getService();
    }

    /** Returns heap context service. */
    public static HeapContext getHeapContext() {
        return HEAP_CONTEXT_TRACKER.getService();
    }

    /** Returns immortal context service. */
    public static ImmortalContext getImmortalContext() {
        return IMMORTAL_CONTEXT_TRACKER.getService();
    }

    /** Returns local context service. */
    public static LocalContext getLocalContext() {
        return LOCAL_CONTEXT_TRACKER.getService();
    }

    /** Returns log context service. */
    public static LogContext getLogContext() {
        return LOG_CONTEXT_TRACKER.getService();
    }

    /** Returns OSGi log service. */
    public static LogService getLogService() {
        return LOG_SERVICE_TRACKER.getService();
    }

    /** Returns security context service. */
    public static SecurityContext getSecurityContext() {
        return SECURITY_CONTEXT_TRACKER.getService();
    }

    /** Returns stack context service. */
    public static StackContext getStackContext() {
        return STACK_CONTEXT_TRACKER.getService();
    }

    /** Returns text context service. */
    public static TextContext getTextContext() {
        return TEXT_CONTEXT_TRACKER.getService();
    }

    /** Returns xml context service. */
    public static XMLContext getXMLContext() {
        return XML_CONTEXT_TRACKER.getService();
    }

    /** Returns xml input factory service. */
    public static XMLInputFactory getXMLInputFactory() {
        return XML_INPUT_FACTORY_TRACKER.getService();
    }

    /** Returns xml output factory service. */
    public static XMLOutputFactory getXMLOutputFactory() {
        return XML_OUTPUT_FACTORY_TRACKER.getService();
    }
}