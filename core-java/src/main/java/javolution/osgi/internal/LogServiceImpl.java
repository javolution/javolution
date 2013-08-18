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

/**
 * Holds the default implementation of LogService to be used when running 
 * outside OSGi or when the Javolution bundle is not started.
 */
public final class LogServiceImpl extends Thread implements LogService {

    private static class LogEvent {
        Throwable exception;
        int level;
        String message;
    }

    private final FastTable<LogEvent> eventQueue = new FastTable<LogEvent>();

    public LogServiceImpl() {
        super("Logging-Thread");
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
                switch (event.level) {
                    case LogService.LOG_DEBUG:
                        System.out.println("[DEBUG] " + event.message);
                        break;
                    case LogService.LOG_INFO:
                        System.out.println("[INFO] " + event.message);
                        break;
                    case LogService.LOG_WARNING:
                        System.out.println("[WARNING] " + event.message);
                        break;
                    case LogService.LOG_ERROR:
                        System.out.println("[ERROR] " + event.message);
                        break;
                    default:
                        System.out.println("[UNKNOWN] " + event.message);
                        break;
                }
                if (event.exception != null) {
                    event.exception.printStackTrace(System.out);
                }
            } catch (InterruptedException error) { 
                error.printStackTrace(System.err);
            }
        }
    }

}
