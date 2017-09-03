/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.context.internal;

import org.javolution.context.LogContext.Level;
import org.javolution.osgi.internal.OSGiServices;
import org.javolution.text.TextBuilder;
import org.javolution.util.FastTable;
import org.osgi.service.log.LogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** 
 * Thread providing asynchronous processing of the log events.
 */
class LoggingThread extends Thread {
    private static final Logger LOG = LoggerFactory.getLogger(LoggingThread.class);
    private final FastTable<Event> eventQueue = FastTable.newInstance();

    /** Default Constructor.*/
    public LoggingThread() {
        super("LoggingThread");
        setDaemon(true);
        this.start();
        Thread hook = new Thread(new Runnable() {
            @Override
            public void run() { // Maintains the VM alive until the event queue is flushed 
                synchronized (eventQueue) {
                    try {
                        while (!eventQueue.isEmpty())
                            eventQueue.wait();
                    } catch (InterruptedException e) {
                    }
                }
            }
        });
        Runtime.getRuntime().addShutdownHook(hook);
    }

    @Override
    public void run() {
        while (true) {
            try {
                Event event;
                synchronized (eventQueue) {
                    while (eventQueue.isEmpty())
                        eventQueue.wait();
                    event = eventQueue.pollLast();
                    eventQueue.notify();
                }
               TextBuilder tmp = new TextBuilder(event.prefix);
               for (Object obj : event.messages) 
                   tmp.append(obj);
               tmp.append(event.suffix);
               String message = tmp.toString();
               Object[] logServices = OSGiServices.getLogServices();
               for (Object obj : logServices) {
                   log((LogService)obj, event.level, message, event.error);
               }
               logSLF4J(event.level, message, event.error);    
            } catch (InterruptedException error) {
                LOG.error("An Error Occurred While Logging", error);
            }
        }
    }

    public void queueEvent(Level level, String prefix, String suffix, Object[] messages, Throwable error) {
        Event event = new Event();
        event.level = level;
        event.prefix = prefix;
        event.suffix = suffix;
        event.messages = messages;
        event.error = error;
        synchronized (eventQueue) {
              eventQueue.addFirst(event);
              eventQueue.notify();
        }
    }
    
    private void log(LogService logService, Level level, String message, Throwable error) {
        switch (level) {
        case DEBUG:
            if (error == null) logService.log(LogService.LOG_DEBUG, message);
            else logService.log(LogService.LOG_DEBUG, message, error);
            break;
        case INFO:
            if (error == null) logService.log(LogService.LOG_INFO, message);
            else logService.log(LogService.LOG_INFO, message, error);
            break;
        case WARNING:
            if (error == null) logService.log(LogService.LOG_WARNING, message);
            else logService.log(LogService.LOG_WARNING, message, error);
            break;
        case ERROR:
            if (error == null) logService.log(LogService.LOG_ERROR, message);
            else logService.log(LogService.LOG_ERROR, message, error);
            break;
        case FATAL:                                              
            if (error == null) logService.log(LogService.LOG_ERROR, message);
            else logService.log(LogService.LOG_ERROR, message, error);
            break;
        }
    }

    private void logSLF4J(Level level, String message, Throwable error) {
        switch (level) {
        case DEBUG:
            if (error == null)  LOG.debug(message);
            else  LOG.debug(message, error);
            break;
        case INFO:
            if (error == null) LOG.info(message);
            else LOG.info(message, error);
            break;
        case WARNING:
            if (error == null) LOG.warn(message);
            else LOG.warn(message, error);
            break;
        case ERROR:
            if (error == null) LOG.error(message);
            else LOG.error(message, error);
            break;
        case FATAL:                                              
            if (error == null) LOG.error(message);
            else LOG.error(message, error);
            break;
        }
    }     
   
    /** Log event. */
    private static class Event {
        Level level;
        String prefix;
        String suffix;
        Object[] messages;
        Throwable error;
    }

}
