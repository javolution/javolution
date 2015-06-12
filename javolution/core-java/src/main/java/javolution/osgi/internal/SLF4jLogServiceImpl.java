/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.osgi.internal;

import javolution.util.FastTable;

import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of LogService to route through SLF4j API 
 * outside OSGi or when the Javolution bundle is not started.
 * 
 * Will be used instead of the Default LogServiceImpl if SLF4j
 * API is present on the classpath
 */
public final class SLF4jLogServiceImpl extends Thread implements LogService {
	
	private static final Logger LOG = LoggerFactory.getLogger(SLF4jLogServiceImpl.class);
	
	private static class LogEvent {
		Throwable exception;
		int level;
		String message;
	}
	
	private final FastTable<LogEvent> eventQueue = new FastTable<LogEvent>();
	
	public SLF4jLogServiceImpl() {
		super("SLF4j Logging-Thread");
		setDaemon(true);
		this.start();
		Thread hook = new Thread(new Runnable() {
			@Override
			public void run() { // Maintains the VM alive until the event queue is flushed 
				synchronized (eventQueue) {
					try {
						while (!eventQueue.isEmpty())
							eventQueue.wait();
					} catch (InterruptedException e) {}
				}
			}
		});
		Runtime.getRuntime().addShutdownHook(hook);
	}

	@Override
	public void log(int level, String message) {
		log(level, message, null);
	}

	@Override
	public void log(int level, String message, Throwable exception) {
		LogEvent event = new LogEvent();
		event.level = level;
		event.message = message;
		event.exception = exception;
		synchronized (eventQueue) {
			eventQueue.addFirst(event);
			eventQueue.notify();
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void log(ServiceReference sr, int level, String message) {
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void log(ServiceReference sr, int level, String message,
			Throwable exception) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void run() {
		while (true) {
			try {
				LogEvent event;
				synchronized (eventQueue) {
					while (eventQueue.isEmpty())
						eventQueue.wait();
					event = eventQueue.pollLast();
					eventQueue.notify();
				}
				
				if(event.exception==null){
					log(event);
				}
				else {
					logWithException(event);
				}
				
			} catch (InterruptedException error) { 
				LOG.error("An Error Occurred While Logging", error);
			}
		}
	}
	
	private void log(LogEvent event){
		switch (event.level) {
		case LogService.LOG_DEBUG:						
			LOG.debug(event.message);
			break;
		case LogService.LOG_WARNING:
			LOG.warn(event.message);
			break;
		case LogService.LOG_ERROR:
			LOG.error(event.message);
			break;
		default:
			LOG.info(event.message);
			break;
		}
	}
	
	private void logWithException(LogEvent event){
		switch (event.level) {
		case LogService.LOG_DEBUG:						
			LOG.debug(event.message, event.exception);
			break;
		case LogService.LOG_WARNING:
			LOG.warn(event.message, event.exception);
			break;
		case LogService.LOG_ERROR:
			LOG.error(event.message, event.exception);
			break;
		default:
			LOG.info(event.message, event.exception);
			break;
		}
	}
}
